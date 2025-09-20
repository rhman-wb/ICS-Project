package com.insurance.audit.audit.llm;

import com.insurance.audit.audit.matchers.Matcher;
import com.insurance.audit.audit.service.DocumentProvider.DocumentChunk;
import com.insurance.audit.audit.service.RuleProvider.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * LLM检核编排器
 * 支持指令直推和逻辑拆解两种模式，处理复杂语义/跨段落场景的裁决
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class LLMOrchestrator implements Matcher {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${audit.llm-service.base-url:http://localhost:8080}")
    private String llmServiceBaseUrl;

    @Value("${audit.llm-service.timeout:30000}")
    private int timeoutMs;

    @Value("${audit.llm-service.max-retries:3}")
    private int maxRetries;

    @Override
    public List<MatchResult> match(Rule rule, List<DocumentChunk> chunks) {
        log.info("开始LLM检核: ruleId={}, chunkCount={}", rule.getId(), chunks.size());

        List<MatchResult> results = new ArrayList<>();
        Map<String, Object> parameters = rule.getParameters();

        // 解析LLM配置
        LLMConfig config = parseLLMConfig(parameters);

        // 根据规则类型和上下文选择执行模式
        String mode = selectExecutionMode(rule, chunks, config);
        log.info("选择执行模式: ruleId={}, mode={}", rule.getId(), mode);

        switch (mode) {
            case "DIRECT_PROMPT":
                results.addAll(executeDirectPromptMode(rule, chunks, config));
                break;
            case "CHAIN_OF_THOUGHT":
                results.addAll(executeChainOfThoughtMode(rule, chunks, config));
                break;
            case "MIXED":
                results.addAll(executeMixedMode(rule, chunks, config));
                break;
            default:
                log.warn("未知的LLM执行模式: {}", mode);
                break;
        }

        log.info("LLM检核完成: ruleId={}, resultCount={}", rule.getId(), results.size());
        return results;
    }

    /**
     * 选择执行模式
     */
    private String selectExecutionMode(Rule rule, List<DocumentChunk> chunks, LLMConfig config) {
        // 强制模式
        if (config.getForceMode() != null) {
            return config.getForceMode();
        }

        // 智能选择模式
        int complexity = calculateComplexity(rule, chunks);

        if (complexity >= 80) {
            return "CHAIN_OF_THOUGHT";
        } else if (complexity >= 50) {
            return "MIXED";
        } else {
            return "DIRECT_PROMPT";
        }
    }

    /**
     * 计算复杂度分数
     */
    private int calculateComplexity(Rule rule, List<DocumentChunk> chunks) {
        int complexity = 0;

        // 文档复杂度
        complexity += Math.min(30, chunks.size() * 2);

        // 规则复杂度
        String ruleDescription = rule.getDescription();
        if (ruleDescription != null) {
            if (ruleDescription.contains("跨段落") || ruleDescription.contains("关联")) {
                complexity += 25;
            }
            if (ruleDescription.contains("复杂") || ruleDescription.contains("推理")) {
                complexity += 20;
            }
            if (ruleDescription.contains("上下文") || ruleDescription.contains("语境")) {
                complexity += 15;
            }
        }

        // 文本长度复杂度
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.getText().length()).sum();
        complexity += Math.min(25, totalLength / 1000);

        return Math.min(100, complexity);
    }

    /**
     * 执行指令直推模式
     */
    private List<MatchResult> executeDirectPromptMode(Rule rule, List<DocumentChunk> chunks, LLMConfig config) {
        log.debug("执行指令直推模式: ruleId={}", rule.getId());

        List<MatchResult> results = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            try {
                LLMRequest request = buildDirectPromptRequest(rule, chunk, config);
                LLMResponse response = callLLMService(request);

                MatchResult result = parseDirectPromptResponse(rule, chunk, response, config);
                if (result != null) {
                    results.add(result);
                }

            } catch (Exception e) {
                log.error("指令直推模式处理失败: ruleId={}, chunkId={}, error={}",
                         rule.getId(), chunk.getId(), e.getMessage(), e);

                // 添加错误结果
                results.add(createErrorResult(rule, chunk, e.getMessage()));
            }
        }

        return results;
    }

    /**
     * 执行逻辑拆解模式（思维链）
     */
    private List<MatchResult> executeChainOfThoughtMode(Rule rule, List<DocumentChunk> chunks, LLMConfig config) {
        log.debug("执行逻辑拆解模式: ruleId={}", rule.getId());

        List<MatchResult> results = new ArrayList<>();

        // 将相关块组合处理
        List<List<DocumentChunk>> chunkGroups = groupRelatedChunks(chunks, config);

        for (List<DocumentChunk> group : chunkGroups) {
            try {
                ChainOfThoughtResult cotResult = executeChainOfThought(rule, group, config);

                for (DocumentChunk chunk : group) {
                    MatchResult result = buildResultFromCoT(rule, chunk, cotResult, config);
                    if (result != null) {
                        results.add(result);
                    }
                }

            } catch (Exception e) {
                log.error("逻辑拆解模式处理失败: ruleId={}, groupSize={}, error={}",
                         rule.getId(), group.size(), e.getMessage(), e);

                // 为组内每个块添加错误结果
                for (DocumentChunk chunk : group) {
                    results.add(createErrorResult(rule, chunk, e.getMessage()));
                }
            }
        }

        return results;
    }

    /**
     * 执行混合模式
     */
    private List<MatchResult> executeMixedMode(Rule rule, List<DocumentChunk> chunks, LLMConfig config) {
        log.debug("执行混合模式: ruleId={}", rule.getId());

        List<MatchResult> results = new ArrayList<>();

        // 简单块使用直推模式
        List<DocumentChunk> simpleChunks = new ArrayList<>();
        // 复杂块使用逻辑拆解模式
        List<DocumentChunk> complexChunks = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (isComplexChunk(chunk)) {
                complexChunks.add(chunk);
            } else {
                simpleChunks.add(chunk);
            }
        }

        // 分别处理
        if (!simpleChunks.isEmpty()) {
            results.addAll(executeDirectPromptMode(rule, simpleChunks, config));
        }
        if (!complexChunks.isEmpty()) {
            results.addAll(executeChainOfThoughtMode(rule, complexChunks, config));
        }

        return results;
    }

    /**
     * 执行思维链推理
     */
    private ChainOfThoughtResult executeChainOfThought(Rule rule, List<DocumentChunk> chunks, LLMConfig config) {
        // Step 1: 理解任务
        LLMRequest understandRequest = buildUnderstandTaskRequest(rule, chunks, config);
        LLMResponse understandResponse = callLLMService(understandRequest);

        // Step 2: 分析文档
        LLMRequest analyzeRequest = buildAnalyzeDocumentRequest(rule, chunks, understandResponse, config);
        LLMResponse analyzeResponse = callLLMService(analyzeRequest);

        // Step 3: 应用规则
        LLMRequest applyRuleRequest = buildApplyRuleRequest(rule, chunks, analyzeResponse, config);
        LLMResponse applyRuleResponse = callLLMService(applyRuleRequest);

        // Step 4: 生成结论
        LLMRequest concludeRequest = buildConcludeRequest(rule, chunks, applyRuleResponse, config);
        LLMResponse concludeResponse = callLLMService(concludeRequest);

        return ChainOfThoughtResult.builder()
                .understanding(understandResponse.getContent())
                .analysis(analyzeResponse.getContent())
                .ruleApplication(applyRuleResponse.getContent())
                .conclusion(concludeResponse.getContent())
                .reasoning(List.of(
                    understandResponse.getContent(),
                    analyzeResponse.getContent(),
                    applyRuleResponse.getContent(),
                    concludeResponse.getContent()
                ))
                .build();
    }

    /**
     * 调用LLM服务
     */
    private LLMResponse callLLMService(LLMRequest request) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<LLMRequest> entity = new HttpEntity<>(request, headers);

                String url = llmServiceBaseUrl + "/api/v1/llm/chat";
                ResponseEntity<LLMResponse> response = restTemplate.postForEntity(url, entity, LLMResponse.class);

                if (response.getBody() != null) {
                    // 记录使用情况
                    logLLMUsage(request, response.getBody());
                    return response.getBody();
                } else {
                    throw new RuntimeException("LLM服务返回空响应");
                }

            } catch (Exception e) {
                log.warn("LLM服务调用失败 (尝试 {}/{}): {}", attempt, maxRetries, e.getMessage());

                if (attempt == maxRetries) {
                    throw new RuntimeException("LLM服务调用失败，已重试" + maxRetries + "次: " + e.getMessage(), e);
                }

                // 重试前等待
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("等待重试时被中断", ie);
                }
            }
        }

        throw new RuntimeException("LLM服务调用失败");
    }

    /**
     * 构建直推提示请求
     */
    private LLMRequest buildDirectPromptRequest(Rule rule, DocumentChunk chunk, LLMConfig config) {
        String prompt = String.format("""
            请根据以下规则检查文档内容：

            规则：%s
            描述：%s

            文档内容：
            %s

            请直接回答：
            1. 是否符合规则（是/否）
            2. 置信度（0-1）
            3. 理由（简短说明）

            输出格式：JSON
            """,
            rule.getName(),
            rule.getDescription(),
            redactSensitiveData(chunk.getText(), config)
        );

        return LLMRequest.builder()
                .prompt(prompt)
                .model(config.getModel())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .metadata(Map.of(
                    "ruleId", rule.getId(),
                    "chunkId", chunk.getId(),
                    "mode", "DIRECT_PROMPT"
                ))
                .build();
    }

    /**
     * 脱敏敏感数据
     */
    private String redactSensitiveData(String text, LLMConfig config) {
        if (!config.isRedactSensitiveData()) {
            return text;
        }

        String result = text;

        // 脱敏身份证号
        result = result.replaceAll("\\b\\d{15}|\\d{18}|\\d{17}[Xx]\\b", "***身份证号***");

        // 脱敏电话号码
        result = result.replaceAll("\\b1[3-9]\\d{9}\\b", "***电话号码***");

        // 脱敏邮箱
        result = result.replaceAll("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b", "***邮箱地址***");

        return result;
    }

    /**
     * 记录LLM使用情况
     */
    private void logLLMUsage(LLMRequest request, LLMResponse response) {
        Map<String, Object> usage = response.getUsage();
        if (usage != null) {
            log.info("LLM使用统计: model={}, promptTokens={}, completionTokens={}, totalTokens={}, cost={}",
                    request.getModel(),
                    usage.get("promptTokens"),
                    usage.get("completionTokens"),
                    usage.get("totalTokens"),
                    usage.get("estimatedCost"));
        }
    }

    /**
     * 判断是否为复杂块
     */
    private boolean isComplexChunk(DocumentChunk chunk) {
        String text = chunk.getText();

        // 长文本认为复杂
        if (text.length() > 500) {
            return true;
        }

        // 包含复杂语法结构
        if (text.contains("但是") || text.contains("然而") || text.contains("如果") ||
            text.contains("除非") || text.contains("尽管")) {
            return true;
        }

        // 表格类型认为复杂
        if ("table_row".equals(chunk.getType()) || "table_header".equals(chunk.getType())) {
            return true;
        }

        return false;
    }

    /**
     * 将相关块分组
     */
    private List<List<DocumentChunk>> groupRelatedChunks(List<DocumentChunk> chunks, LLMConfig config) {
        List<List<DocumentChunk>> groups = new ArrayList<>();
        List<DocumentChunk> currentGroup = new ArrayList<>();
        int currentGroupSize = 0;
        int maxGroupSize = config.getMaxContextWindow();

        for (DocumentChunk chunk : chunks) {
            if (currentGroupSize + chunk.getText().length() > maxGroupSize && !currentGroup.isEmpty()) {
                groups.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroupSize = 0;
            }

            currentGroup.add(chunk);
            currentGroupSize += chunk.getText().length();
        }

        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }

        return groups;
    }

    /**
     * 解析LLM配置
     */
    private LLMConfig parseLLMConfig(Map<String, Object> parameters) {
        LLMConfig config = new LLMConfig();

        config.setModel((String) parameters.getOrDefault("model", "gpt-3.5-turbo"));
        config.setTemperature((Double) parameters.getOrDefault("temperature", 0.3));
        config.setMaxTokens((Integer) parameters.getOrDefault("maxTokens", 1000));
        config.setMaxContextWindow((Integer) parameters.getOrDefault("maxContextWindow", 4000));
        config.setRedactSensitiveData((Boolean) parameters.getOrDefault("redactSensitiveData", true));
        config.setForceMode((String) parameters.get("forceMode"));

        return config;
    }

    /**
     * 构建理解任务请求
     */
    private LLMRequest buildUnderstandTaskRequest(Rule rule, List<DocumentChunk> chunks, LLMConfig config) {
        String prompt = String.format("""
            请理解以下检核任务：

            规则名称：%s
            规则描述：%s

            请说明：
            1. 这个规则要检查什么？
            2. 重点关注哪些方面？
            3. 可能的例外情况？
            """, rule.getName(), rule.getDescription());

        return LLMRequest.builder()
                .prompt(prompt)
                .model(config.getModel())
                .temperature(0.1)
                .maxTokens(500)
                .build();
    }

    /**
     * 构建分析文档请求
     */
    private LLMRequest buildAnalyzeDocumentRequest(Rule rule, List<DocumentChunk> chunks,
                                                  LLMResponse understanding, LLMConfig config) {
        StringBuilder content = new StringBuilder();
        for (DocumentChunk chunk : chunks) {
            content.append(redactSensitiveData(chunk.getText(), config)).append("\n\n");
        }

        String prompt = String.format("""
            基于对任务的理解：
            %s

            请分析以下文档内容：
            %s

            分析要点：
            1. 文档的主要内容是什么？
            2. 哪些部分与规则相关？
            3. 有无明显的合规或违规迹象？
            """, understanding.getContent(), content.toString());

        return LLMRequest.builder()
                .prompt(prompt)
                .model(config.getModel())
                .temperature(0.2)
                .maxTokens(800)
                .build();
    }

    /**
     * 构建应用规则请求
     */
    private LLMRequest buildApplyRuleRequest(Rule rule, List<DocumentChunk> chunks,
                                           LLMResponse analysis, LLMConfig config) {
        String prompt = String.format("""
            基于文档分析：
            %s

            现在请严格应用规则：%s

            具体要求：
            1. 逐条检查规则要求
            2. 找出支持证据
            3. 识别潜在问题
            4. 给出合规判断
            """, analysis.getContent(), rule.getDescription());

        return LLMRequest.builder()
                .prompt(prompt)
                .model(config.getModel())
                .temperature(0.1)
                .maxTokens(1000)
                .build();
    }

    /**
     * 构建结论请求
     */
    private LLMRequest buildConcludeRequest(Rule rule, List<DocumentChunk> chunks,
                                          LLMResponse ruleApplication, LLMConfig config) {
        String prompt = String.format("""
            基于规则应用结果：
            %s

            请给出最终结论：
            1. 整体合规状态（通过/失败/警告）
            2. 置信度（0-1）
            3. 关键证据摘要
            4. 改进建议（如适用）

            输出格式：JSON
            """, ruleApplication.getContent());

        return LLMRequest.builder()
                .prompt(prompt)
                .model(config.getModel())
                .temperature(0.1)
                .maxTokens(600)
                .build();
    }

    // 其他必要的方法实现...
    private MatchResult parseDirectPromptResponse(Rule rule, DocumentChunk chunk, LLMResponse response, LLMConfig config) {
        // 实现解析直推响应逻辑
        return null; // TODO: 实现
    }

    private MatchResult buildResultFromCoT(Rule rule, DocumentChunk chunk, ChainOfThoughtResult cotResult, LLMConfig config) {
        // 实现从思维链结果构建匹配结果逻辑
        return null; // TODO: 实现
    }

    private MatchResult createErrorResult(Rule rule, DocumentChunk chunk, String errorMessage) {
        return MatchResult.builder()
                .ruleId(rule.getId())
                .chunkId(chunk.getId())
                .text(chunk.getText())
                .score(0.0)
                .threshold(rule.getThreshold())
                .status("ERROR")
                .evidences(List.of())
                .metadata(Map.of("error", errorMessage, "timestamp", LocalDateTime.now()))
                .build();
    }

    // 内部类定义
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class LLMConfig {
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Integer maxContextWindow;
        private boolean redactSensitiveData;
        private String forceMode;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class LLMRequest {
        private String prompt;
        private String model;
        private Double temperature;
        private Integer maxTokens;
        private Map<String, Object> metadata;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class LLMResponse {
        private String content;
        private String model;
        private Map<String, Object> usage;
        private Map<String, Object> metadata;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ChainOfThoughtResult {
        private String understanding;
        private String analysis;
        private String ruleApplication;
        private String conclusion;
        private List<String> reasoning;
    }
}