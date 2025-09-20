package com.insurance.audit.audit.matchers;

import com.insurance.audit.audit.service.DocumentProvider.DocumentChunk;
import com.insurance.audit.audit.service.RuleProvider.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 语义相似度匹配器
 * 使用向量检索进行语义相似度匹配，支持TopK和阈值过滤
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class SemanticMatcher implements Matcher {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${audit.embedding-service.base-url:http://localhost:8080}")
    private String embeddingServiceBaseUrl;

    @Value("${audit.vector-db.base-url:http://localhost:8080}")
    private String vectorDbBaseUrl;

    @Override
    public List<MatchResult> match(Rule rule, List<DocumentChunk> chunks) {
        log.info("开始语义匹配: ruleId={}, chunkCount={}", rule.getId(), chunks.size());

        List<MatchResult> results = new ArrayList<>();
        Map<String, Object> parameters = rule.getParameters();

        // 解析语义匹配配置
        SemanticMatchConfig config = parseSemanticConfig(parameters);

        // 批量处理文档块以提高效率
        List<List<DocumentChunk>> batches = createBatches(chunks, config.getBatchSize());

        for (List<DocumentChunk> batch : batches) {
            List<MatchResult> batchResults = processBatch(rule, batch, config);
            results.addAll(batchResults);
        }

        log.info("语义匹配完成: ruleId={}, matchCount={}", rule.getId(), results.size());
        return results;
    }

    /**
     * 处理文档块批次
     */
    private List<MatchResult> processBatch(Rule rule, List<DocumentChunk> chunks, SemanticMatchConfig config) {
        List<MatchResult> results = new ArrayList<>();

        try {
            // 1. 获取查询文本的嵌入向量
            List<double[]> queryEmbeddings = getEmbeddings(config.getQueryTexts());

            // 2. 获取文档块的嵌入向量
            List<String> chunkTexts = chunks.stream().map(DocumentChunk::getText).toList();
            List<double[]> chunkEmbeddings = getEmbeddings(chunkTexts);

            // 3. 计算相似度并匹配
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = chunks.get(i);
                double[] chunkEmbedding = chunkEmbeddings.get(i);

                MatchResult result = matchChunkWithQueries(rule, chunk, chunkEmbedding, queryEmbeddings, config);
                if (result != null) {
                    results.add(result);
                }
            }

        } catch (Exception e) {
            log.error("语义匹配批次处理失败: ruleId={}, error={}", rule.getId(), e.getMessage(), e);

            // 降级到基础文本匹配
            results.addAll(fallbackToBasicMatch(rule, chunks, config));
        }

        return results;
    }

    /**
     * 将文档块与查询进行语义匹配
     */
    private MatchResult matchChunkWithQueries(Rule rule, DocumentChunk chunk, double[] chunkEmbedding,
                                             List<double[]> queryEmbeddings, SemanticMatchConfig config) {
        List<Evidence> evidences = new ArrayList<>();
        double maxSimilarity = 0.0;
        String bestMatch = "";

        // 与每个查询文本计算相似度
        for (int i = 0; i < queryEmbeddings.size(); i++) {
            double[] queryEmbedding = queryEmbeddings.get(i);
            double similarity = calculateCosineSimilarity(chunkEmbedding, queryEmbedding);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = config.getQueryTexts().get(i);
            }

            // 如果相似度超过阈值，添加证据
            if (similarity >= rule.getThreshold()) {
                evidences.add(Evidence.builder()
                        .text(chunk.getText())
                        .startPos(0)
                        .endPos(chunk.getText().length())
                        .matchType("SEMANTIC_SIMILARITY")
                        .context(Map.of(
                            "queryText", config.getQueryTexts().get(i),
                            "similarity", similarity,
                            "threshold", rule.getThreshold()
                        ))
                        .build());
            }
        }

        // 应用节约束
        if (config.getSectionConstraints() != null && !config.getSectionConstraints().isEmpty()) {
            if (!matchesSectionConstraints(chunk, config.getSectionConstraints())) {
                return null; // 不符合节约束
            }
        }

        // 判断是否匹配
        boolean matched = maxSimilarity >= rule.getThreshold();

        if (matched || config.isReportLowScores()) {
            String status = determineSemanticStatus(maxSimilarity, rule.getThreshold(), config);

            return MatchResult.builder()
                    .ruleId(rule.getId())
                    .chunkId(chunk.getId())
                    .text(chunk.getText())
                    .score(maxSimilarity)
                    .threshold(rule.getThreshold())
                    .status(status)
                    .evidences(evidences)
                    .metadata(createSemanticMetadata(chunk, config, maxSimilarity, bestMatch))
                    .build();
        }

        return null;
    }

    /**
     * 获取文本的嵌入向量
     */
    private List<double[]> getEmbeddings(List<String> texts) {
        try {
            // 构建请求
            Map<String, Object> request = Map.of(
                "texts", texts,
                "model", "text-embedding-ada-002"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            // 发送请求到嵌入服务
            String url = embeddingServiceBaseUrl + "/api/v1/embeddings";
            ResponseEntity<EmbeddingResponse> response = restTemplate.postForEntity(url, entity, EmbeddingResponse.class);

            if (response.getBody() != null && response.getBody().getEmbeddings() != null) {
                return response.getBody().getEmbeddings();
            } else {
                throw new RuntimeException("嵌入服务返回空结果");
            }

        } catch (Exception e) {
            log.error("获取嵌入向量失败: textCount={}, error={}", texts.size(), e.getMessage(), e);
            throw new RuntimeException("嵌入向量获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    /**
     * 检查节约束
     */
    private boolean matchesSectionConstraints(DocumentChunk chunk, List<String> sectionConstraints) {
        String chunkType = chunk.getType();
        Map<String, Object> metadata = chunk.getMetadata();

        for (String constraint : sectionConstraints) {
            switch (constraint) {
                case "HEADING_ONLY":
                    if (!"heading".equals(chunkType)) {
                        return false;
                    }
                    break;
                case "PARAGRAPH_ONLY":
                    if (!"paragraph".equals(chunkType) && !"sentence".equals(chunkType)) {
                        return false;
                    }
                    break;
                case "TABLE_ONLY":
                    if (!"table_row".equals(chunkType) && !"table_header".equals(chunkType)) {
                        return false;
                    }
                    break;
                case "EXCLUDE_TABLES":
                    if ("table_row".equals(chunkType) || "table_header".equals(chunkType)) {
                        return false;
                    }
                    break;
                default:
                    log.warn("未知的节约束: {}", constraint);
                    break;
            }
        }

        return true;
    }

    /**
     * 降级到基础文本匹配
     */
    private List<MatchResult> fallbackToBasicMatch(Rule rule, List<DocumentChunk> chunks, SemanticMatchConfig config) {
        log.info("语义匹配失败，降级到基础文本匹配: ruleId={}", rule.getId());

        List<MatchResult> results = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            String text = chunk.getText().toLowerCase();
            double maxSimilarity = 0.0;
            String bestMatch = "";

            // 使用简单的文本包含检查作为降级策略
            for (String queryText : config.getQueryTexts()) {
                double similarity = calculateBasicSimilarity(text, queryText.toLowerCase());
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    bestMatch = queryText;
                }
            }

            if (maxSimilarity >= 0.5) { // 降级阈值
                results.add(MatchResult.builder()
                        .ruleId(rule.getId())
                        .chunkId(chunk.getId())
                        .text(chunk.getText())
                        .score(maxSimilarity)
                        .threshold(rule.getThreshold())
                        .status("WARNING") // 降级匹配标记为警告
                        .evidences(List.of(Evidence.builder()
                                .text(chunk.getText())
                                .startPos(0)
                                .endPos(chunk.getText().length())
                                .matchType("FALLBACK_TEXT_MATCH")
                                .context(Map.of("queryText", bestMatch, "fallback", true))
                                .build()))
                        .metadata(Map.of("fallback", true, "originalMethod", "semantic"))
                        .build());
            }
        }

        return results;
    }

    /**
     * 计算基础文本相似度
     */
    private double calculateBasicSimilarity(String text, String query) {
        if (text.contains(query)) {
            return 0.8; // 包含查询文本
        }

        // 计算单词重叠度
        Set<String> textWords = Set.of(text.split("\\s+"));
        Set<String> queryWords = Set.of(query.split("\\s+"));

        int intersection = 0;
        for (String word : queryWords) {
            if (textWords.contains(word)) {
                intersection++;
            }
        }

        return queryWords.isEmpty() ? 0.0 : (double) intersection / queryWords.size();
    }

    /**
     * 创建文档块批次
     */
    private List<List<DocumentChunk>> createBatches(List<DocumentChunk> chunks, int batchSize) {
        List<List<DocumentChunk>> batches = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            batches.add(chunks.subList(i, end));
        }

        return batches;
    }

    /**
     * 解析语义匹配配置
     */
    private SemanticMatchConfig parseSemanticConfig(Map<String, Object> parameters) {
        SemanticMatchConfig config = new SemanticMatchConfig();

        config.setQueryTexts((List<String>) parameters.getOrDefault("queryTexts", List.of()));
        config.setTopK((Integer) parameters.getOrDefault("topK", 10));
        config.setBatchSize((Integer) parameters.getOrDefault("batchSize", 20));
        config.setReportLowScores((Boolean) parameters.getOrDefault("reportLowScores", false));
        config.setSectionConstraints((List<String>) parameters.getOrDefault("sectionConstraints", List.of()));
        config.setEmbeddingModel((String) parameters.getOrDefault("embeddingModel", "text-embedding-ada-002"));

        return config;
    }

    /**
     * 确定语义匹配状态
     */
    private String determineSemanticStatus(double similarity, double threshold, SemanticMatchConfig config) {
        if (similarity >= threshold) {
            return similarity >= 0.9 ? "PASSED" : "WARNING";
        } else {
            return "FAILED";
        }
    }

    /**
     * 创建语义匹配元数据
     */
    private Map<String, Object> createSemanticMetadata(DocumentChunk chunk, SemanticMatchConfig config,
                                                      double maxSimilarity, String bestMatch) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("embeddingModel", config.getEmbeddingModel());
        metadata.put("maxSimilarity", maxSimilarity);
        metadata.put("bestMatch", bestMatch);
        metadata.put("chunkType", chunk.getType());
        metadata.put("sectionConstraints", config.getSectionConstraints());

        return metadata;
    }

    /**
     * 嵌入服务响应DTO
     */
    private static class EmbeddingResponse {
        private List<double[]> embeddings;

        public List<double[]> getEmbeddings() { return embeddings; }
        public void setEmbeddings(List<double[]> embeddings) { this.embeddings = embeddings; }
    }

    /**
     * 语义匹配配置类
     */
    private static class SemanticMatchConfig {
        private List<String> queryTexts = new ArrayList<>();
        private int topK = 10;
        private int batchSize = 20;
        private boolean reportLowScores = false;
        private List<String> sectionConstraints = new ArrayList<>();
        private String embeddingModel = "text-embedding-ada-002";

        // Getters and Setters
        public List<String> getQueryTexts() { return queryTexts; }
        public void setQueryTexts(List<String> queryTexts) { this.queryTexts = queryTexts; }

        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

        public boolean isReportLowScores() { return reportLowScores; }
        public void setReportLowScores(boolean reportLowScores) { this.reportLowScores = reportLowScores; }

        public List<String> getSectionConstraints() { return sectionConstraints; }
        public void setSectionConstraints(List<String> sectionConstraints) { this.sectionConstraints = sectionConstraints; }

        public String getEmbeddingModel() { return embeddingModel; }
        public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
    }
}