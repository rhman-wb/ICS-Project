package com.insurance.audit.audit.service;

import com.insurance.audit.audit.dto.AuditResultDto;
import com.insurance.audit.audit.matchers.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 证据装配器
 * 装配命中项（规则ID/版本、证据、阈值、定位、建议）
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class EvidenceAssembler {

    /**
     * 装配检核结果
     *
     * @param jobId 作业ID
     * @param ruleId 规则ID
     * @param ruleVersion 规则版本
     * @param matchResults 匹配结果列表
     * @return 装配后的检核结果
     */
    public AuditResultDto assembleResult(String jobId, String ruleId, String ruleVersion,
                                       List<Matcher.MatchResult> matchResults) {
        log.debug("装配检核结果: jobId={}, ruleId={}, resultCount={}", jobId, ruleId, matchResults.size());

        if (matchResults.isEmpty()) {
            return createEmptyResult(jobId, ruleId, ruleVersion);
        }

        // 聚合所有匹配结果
        String overallStatus = determineOverallStatus(matchResults);
        double maxScore = matchResults.stream().mapToDouble(Matcher.MatchResult::getScore).max().orElse(0.0);
        double threshold = matchResults.get(0).getThreshold();

        // 收集所有证据
        List<AuditResultDto.Evidence> evidences = new ArrayList<>();
        for (Matcher.MatchResult result : matchResults) {
            evidences.addAll(convertEvidences(result.getEvidences()));
        }

        // 生成建议
        String recommendation = generateRecommendation(overallStatus, matchResults, evidences);

        // 提取文档信息
        String documentId = extractDocumentId(matchResults);
        String ruleName = extractRuleName(matchResults);

        return AuditResultDto.builder()
                .resultId(generateResultId())
                .jobId(jobId)
                .ruleId(ruleId)
                .ruleName(ruleName)
                .documentId(documentId)
                .status(overallStatus)
                .score(maxScore)
                .threshold(threshold)
                .evidences(evidences)
                .recommendation(recommendation)
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 批量装配检核结果
     */
    public List<AuditResultDto> assembleResults(String jobId, Map<String, List<Matcher.MatchResult>> resultsByRule,
                                              Map<String, String> ruleVersions) {
        log.info("批量装配检核结果: jobId={}, ruleCount={}", jobId, resultsByRule.size());

        List<AuditResultDto> assembledResults = new ArrayList<>();

        for (Map.Entry<String, List<Matcher.MatchResult>> entry : resultsByRule.entrySet()) {
            String ruleId = entry.getKey();
            List<Matcher.MatchResult> matchResults = entry.getValue();
            String ruleVersion = ruleVersions.getOrDefault(ruleId, "unknown");

            AuditResultDto result = assembleResult(jobId, ruleId, ruleVersion, matchResults);
            assembledResults.add(result);
        }

        log.info("批量装配完成: jobId={}, resultCount={}", jobId, assembledResults.size());
        return assembledResults;
    }

    /**
     * 创建空结果（无匹配）
     */
    private AuditResultDto createEmptyResult(String jobId, String ruleId, String ruleVersion) {
        return AuditResultDto.builder()
                .resultId(generateResultId())
                .jobId(jobId)
                .ruleId(ruleId)
                .ruleName("未知规则")
                .documentId("unknown")
                .status("NO_MATCH")
                .score(0.0)
                .threshold(0.0)
                .evidences(List.of())
                .recommendation("未找到匹配内容")
                .auditTime(LocalDateTime.now())
                .build();
    }

    /**
     * 确定总体状态
     */
    private String determineOverallStatus(List<Matcher.MatchResult> matchResults) {
        Map<String, Long> statusCounts = matchResults.stream()
                .collect(Collectors.groupingBy(Matcher.MatchResult::getStatus, Collectors.counting()));

        long passedCount = statusCounts.getOrDefault("PASSED", 0L);
        long failedCount = statusCounts.getOrDefault("FAILED", 0L);
        long warningCount = statusCounts.getOrDefault("WARNING", 0L);
        long errorCount = statusCounts.getOrDefault("ERROR", 0L);

        // 有错误时优先返回错误状态
        if (errorCount > 0) {
            return "ERROR";
        }

        // 如果有失败项，返回失败
        if (failedCount > 0) {
            return "FAILED";
        }

        // 如果有警告项但没有失败项，返回警告
        if (warningCount > 0) {
            return "WARNING";
        }

        // 如果有通过项且没有失败和警告，返回通过
        if (passedCount > 0) {
            return "PASSED";
        }

        // 默认返回失败
        return "FAILED";
    }

    /**
     * 转换证据格式
     */
    private List<AuditResultDto.Evidence> convertEvidences(List<Matcher.Evidence> matcherEvidences) {
        if (matcherEvidences == null) {
            return List.of();
        }

        return matcherEvidences.stream()
                .map(this::convertEvidence)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个证据
     */
    private AuditResultDto.Evidence convertEvidence(Matcher.Evidence matcherEvidence) {
        return AuditResultDto.Evidence.builder()
                .text(matcherEvidence.getText())
                .pageNumber(extractPageNumber(matcherEvidence))
                .paragraphIndex(extractParagraphIndex(matcherEvidence))
                .startPos(matcherEvidence.getStartPos())
                .endPos(matcherEvidence.getEndPos())
                .matchType(matcherEvidence.getMatchType())
                .build();
    }

    /**
     * 提取页码
     */
    private Integer extractPageNumber(Matcher.Evidence evidence) {
        if (evidence.getContext() != null) {
            Object pageNum = evidence.getContext().get("pageNumber");
            if (pageNum instanceof Integer) {
                return (Integer) pageNum;
            }
        }
        return null;
    }

    /**
     * 提取段落索引
     */
    private Integer extractParagraphIndex(Matcher.Evidence evidence) {
        if (evidence.getContext() != null) {
            Object paraIndex = evidence.getContext().get("paragraphIndex");
            if (paraIndex instanceof Integer) {
                return (Integer) paraIndex;
            }
        }
        return null;
    }

    /**
     * 生成建议
     */
    private String generateRecommendation(String status, List<Matcher.MatchResult> matchResults,
                                        List<AuditResultDto.Evidence> evidences) {
        switch (status) {
            case "PASSED":
                return "检核通过，文档符合规则要求。";

            case "WARNING":
                return String.format("检核存在警告项，建议复查。发现 %d 处需要注意的内容。", evidences.size());

            case "FAILED":
                StringBuilder failureRecommendation = new StringBuilder();
                failureRecommendation.append("检核未通过，需要修改文档。");

                // 分析失败原因
                Map<String, Long> matchTypeCounts = evidences.stream()
                        .collect(Collectors.groupingBy(AuditResultDto.Evidence::getMatchType, Collectors.counting()));

                if (matchTypeCounts.containsKey("KEYWORD")) {
                    failureRecommendation.append(" 关键词匹配不符合要求。");
                }
                if (matchTypeCounts.containsKey("FORMAT_ERROR")) {
                    failureRecommendation.append(" 格式不规范。");
                }
                if (matchTypeCounts.containsKey("SEMANTIC_SIMILARITY")) {
                    failureRecommendation.append(" 语义内容需要调整。");
                }

                return failureRecommendation.toString();

            case "ERROR":
                return "检核过程出现错误，请检查规则配置或联系技术支持。";

            case "NO_MATCH":
                return "未找到相关内容，可能需要补充必要信息。";

            default:
                return "检核完成，请查看详细结果。";
        }
    }

    /**
     * 提取文档ID
     */
    private String extractDocumentId(List<Matcher.MatchResult> matchResults) {
        return matchResults.stream()
                .map(result -> (String) result.getMetadata().get("documentId"))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("unknown");
    }

    /**
     * 提取规则名称
     */
    private String extractRuleName(List<Matcher.MatchResult> matchResults) {
        return matchResults.stream()
                .map(result -> (String) result.getMetadata().get("ruleName"))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("未知规则");
    }

    /**
     * 生成结果ID
     */
    private String generateResultId() {
        return "result-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 创建结果摘要
     */
    public ResultSummary createSummary(List<AuditResultDto> results) {
        Map<String, Long> statusCounts = results.stream()
                .collect(Collectors.groupingBy(AuditResultDto::getStatus, Collectors.counting()));

        long totalRules = results.size();
        long passedRules = statusCounts.getOrDefault("PASSED", 0L);
        long failedRules = statusCounts.getOrDefault("FAILED", 0L);
        long warningRules = statusCounts.getOrDefault("WARNING", 0L);
        long errorRules = statusCounts.getOrDefault("ERROR", 0L);

        double passRate = totalRules > 0 ? (double) passedRules / totalRules * 100 : 0.0;

        return ResultSummary.builder()
                .totalRules((int) totalRules)
                .passedRules((int) passedRules)
                .failedRules((int) failedRules)
                .warningRules((int) warningRules)
                .errorRules((int) errorRules)
                .passRate(passRate)
                .build();
    }

    /**
     * 结果摘要
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultSummary {
        private Integer totalRules;
        private Integer passedRules;
        private Integer failedRules;
        private Integer warningRules;
        private Integer errorRules;
        private Double passRate;
    }
}