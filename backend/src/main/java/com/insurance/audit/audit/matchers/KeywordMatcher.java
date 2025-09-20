package com.insurance.audit.audit.matchers;

import com.insurance.audit.audit.service.DocumentProvider.DocumentChunk;
import com.insurance.audit.audit.service.RuleProvider.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 关键词匹配器
 * 实现基于关键词和正则表达式的文本匹配功能
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class KeywordMatcher implements Matcher {

    @Override
    public List<MatchResult> match(Rule rule, List<DocumentChunk> chunks) {
        log.info("开始关键词匹配: ruleId={}, chunkCount={}", rule.getId(), chunks.size());

        List<MatchResult> results = new ArrayList<>();
        Map<String, Object> parameters = rule.getParameters();

        // 解析规则参数
        MatchConfig config = parseMatchConfig(parameters);

        for (DocumentChunk chunk : chunks) {
            MatchResult result = matchChunk(rule, chunk, config);
            if (result != null) {
                results.add(result);
            }
        }

        log.info("关键词匹配完成: ruleId={}, matchCount={}", rule.getId(), results.size());
        return results;
    }

    /**
     * 匹配单个文档块
     */
    private MatchResult matchChunk(Rule rule, DocumentChunk chunk, MatchConfig config) {
        String text = chunk.getText();
        List<Evidence> evidences = new ArrayList<>();
        boolean matched = false;

        switch (config.getMatchType()) {
            case "KEYWORD":
                matched = matchKeywords(text, config.getKeywords(), evidences);
                break;
            case "REGEX":
                matched = matchRegex(text, config.getRegexPattern(), evidences);
                break;
            case "PHRASE":
                matched = matchPhrases(text, config.getPhrases(), evidences);
                break;
            case "EXCLUSION":
                matched = matchExclusion(text, config.getExcludeKeywords(), evidences);
                break;
            case "COMBINATION":
                matched = matchCombination(text, config, evidences);
                break;
            default:
                log.warn("不支持的匹配类型: {}", config.getMatchType());
                return null;
        }

        if (matched || config.isReportNonMatches()) {
            String status = determineStatus(matched, evidences.size(), config);
            double score = calculateScore(evidences.size(), config);

            return MatchResult.builder()
                    .ruleId(rule.getId())
                    .chunkId(chunk.getId())
                    .text(chunk.getText())
                    .score(score)
                    .threshold(rule.getThreshold())
                    .status(status)
                    .evidences(evidences)
                    .metadata(createMetadata(chunk, config, evidences))
                    .build();
        }

        return null;
    }

    /**
     * 关键词匹配
     */
    private boolean matchKeywords(String text, List<String> keywords, List<Evidence> evidences) {
        boolean found = false;
        String lowerText = text.toLowerCase();

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int index = 0;

            while ((index = lowerText.indexOf(lowerKeyword, index)) != -1) {
                found = true;
                evidences.add(Evidence.builder()
                        .text(keyword)
                        .startPos(index)
                        .endPos(index + keyword.length())
                        .matchType("KEYWORD")
                        .context(Map.of("keyword", keyword))
                        .build());
                index += keyword.length();
            }
        }

        return found;
    }

    /**
     * 正则表达式匹配
     */
    private boolean matchRegex(String text, String regexPattern, List<Evidence> evidences) {
        if (regexPattern == null || regexPattern.isEmpty()) {
            return false;
        }

        try {
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            boolean found = false;

            while (matcher.find()) {
                found = true;
                evidences.add(Evidence.builder()
                        .text(matcher.group())
                        .startPos(matcher.start())
                        .endPos(matcher.end())
                        .matchType("REGEX")
                        .context(Map.of("pattern", regexPattern, "group", matcher.group()))
                        .build());
            }

            return found;
        } catch (PatternSyntaxException e) {
            log.error("正则表达式语法错误: {}", regexPattern, e);
            return false;
        }
    }

    /**
     * 短语匹配
     */
    private boolean matchPhrases(String text, List<String> phrases, List<Evidence> evidences) {
        boolean found = false;
        String lowerText = text.toLowerCase();

        for (String phrase : phrases) {
            String lowerPhrase = phrase.toLowerCase();
            int index = 0;

            while ((index = lowerText.indexOf(lowerPhrase, index)) != -1) {
                // 检查是否为完整单词边界
                boolean isWordBoundary = true;
                if (index > 0 && Character.isLetterOrDigit(text.charAt(index - 1))) {
                    isWordBoundary = false;
                }
                if (index + phrase.length() < text.length() &&
                    Character.isLetterOrDigit(text.charAt(index + phrase.length()))) {
                    isWordBoundary = false;
                }

                if (isWordBoundary) {
                    found = true;
                    evidences.add(Evidence.builder()
                            .text(phrase)
                            .startPos(index)
                            .endPos(index + phrase.length())
                            .matchType("PHRASE")
                            .context(Map.of("phrase", phrase))
                            .build());
                }
                index += phrase.length();
            }
        }

        return found;
    }

    /**
     * 排除匹配（检查是否包含不应该出现的词）
     */
    private boolean matchExclusion(String text, List<String> excludeKeywords, List<Evidence> evidences) {
        String lowerText = text.toLowerCase();

        for (String keyword : excludeKeywords) {
            String lowerKeyword = keyword.toLowerCase();
            int index = lowerText.indexOf(lowerKeyword);

            if (index != -1) {
                evidences.add(Evidence.builder()
                        .text(keyword)
                        .startPos(index)
                        .endPos(index + keyword.length())
                        .matchType("EXCLUSION")
                        .context(Map.of("excludedKeyword", keyword))
                        .build());
                return true; // 找到排除词，匹配失败
            }
        }

        return false; // 没有找到排除词，匹配成功
    }

    /**
     * 组合匹配（A-B关系等复杂匹配）
     */
    private boolean matchCombination(String text, MatchConfig config, List<Evidence> evidences) {
        String operator = config.getCombinationOperator();
        List<String> groupA = config.getGroupA();
        List<String> groupB = config.getGroupB();

        switch (operator) {
            case "AND":
                return matchAnd(text, groupA, groupB, evidences);
            case "OR":
                return matchOr(text, groupA, groupB, evidences);
            case "NEAR":
                return matchNear(text, groupA, groupB, config.getMaxDistance(), evidences);
            case "SEQUENCE":
                return matchSequence(text, groupA, groupB, evidences);
            default:
                log.warn("不支持的组合操作符: {}", operator);
                return false;
        }
    }

    /**
     * AND操作：两组关键词都必须出现
     */
    private boolean matchAnd(String text, List<String> groupA, List<String> groupB, List<Evidence> evidences) {
        List<Evidence> evidencesA = new ArrayList<>();
        List<Evidence> evidencesB = new ArrayList<>();

        boolean foundA = matchKeywords(text, groupA, evidencesA);
        boolean foundB = matchKeywords(text, groupB, evidencesB);

        if (foundA && foundB) {
            evidences.addAll(evidencesA);
            evidences.addAll(evidencesB);
            return true;
        }

        return false;
    }

    /**
     * OR操作：任一组关键词出现即可
     */
    private boolean matchOr(String text, List<String> groupA, List<String> groupB, List<Evidence> evidences) {
        List<Evidence> evidencesA = new ArrayList<>();
        List<Evidence> evidencesB = new ArrayList<>();

        boolean foundA = matchKeywords(text, groupA, evidencesA);
        boolean foundB = matchKeywords(text, groupB, evidencesB);

        if (foundA) {
            evidences.addAll(evidencesA);
        }
        if (foundB) {
            evidences.addAll(evidencesB);
        }

        return foundA || foundB;
    }

    /**
     * NEAR操作：两组关键词在指定距离内出现
     */
    private boolean matchNear(String text, List<String> groupA, List<String> groupB,
                             int maxDistance, List<Evidence> evidences) {
        List<Evidence> evidencesA = new ArrayList<>();
        List<Evidence> evidencesB = new ArrayList<>();

        matchKeywords(text, groupA, evidencesA);
        matchKeywords(text, groupB, evidencesB);

        boolean found = false;
        for (Evidence evidenceA : evidencesA) {
            for (Evidence evidenceB : evidencesB) {
                int distance = Math.abs(evidenceA.getStartPos() - evidenceB.getStartPos());
                if (distance <= maxDistance) {
                    evidences.add(evidenceA);
                    evidences.add(evidenceB);
                    found = true;
                }
            }
        }

        return found;
    }

    /**
     * SEQUENCE操作：关键词按顺序出现
     */
    private boolean matchSequence(String text, List<String> groupA, List<String> groupB, List<Evidence> evidences) {
        List<Evidence> evidencesA = new ArrayList<>();
        List<Evidence> evidencesB = new ArrayList<>();

        matchKeywords(text, groupA, evidencesA);
        matchKeywords(text, groupB, evidencesB);

        boolean found = false;
        for (Evidence evidenceA : evidencesA) {
            for (Evidence evidenceB : evidencesB) {
                if (evidenceA.getStartPos() < evidenceB.getStartPos()) {
                    evidences.add(evidenceA);
                    evidences.add(evidenceB);
                    found = true;
                }
            }
        }

        return found;
    }

    /**
     * 解析匹配配置
     */
    private MatchConfig parseMatchConfig(Map<String, Object> parameters) {
        MatchConfig config = new MatchConfig();

        config.setMatchType((String) parameters.getOrDefault("matchType", "KEYWORD"));
        config.setKeywords((List<String>) parameters.getOrDefault("keywords", List.of()));
        config.setPhrases((List<String>) parameters.getOrDefault("phrases", List.of()));
        config.setExcludeKeywords((List<String>) parameters.getOrDefault("excludeKeywords", List.of()));
        config.setRegexPattern((String) parameters.get("regexPattern"));
        config.setReportNonMatches((Boolean) parameters.getOrDefault("reportNonMatches", false));
        config.setCombinationOperator((String) parameters.getOrDefault("combinationOperator", "AND"));
        config.setGroupA((List<String>) parameters.getOrDefault("groupA", List.of()));
        config.setGroupB((List<String>) parameters.getOrDefault("groupB", List.of()));
        config.setMaxDistance((Integer) parameters.getOrDefault("maxDistance", 50));

        return config;
    }

    /**
     * 确定匹配状态
     */
    private String determineStatus(boolean matched, int evidenceCount, MatchConfig config) {
        if (!matched) {
            return "FAILED";
        }

        if (evidenceCount >= 3) {
            return "PASSED";
        } else if (evidenceCount >= 1) {
            return "WARNING";
        } else {
            return "FAILED";
        }
    }

    /**
     * 计算匹配分数
     */
    private double calculateScore(int evidenceCount, MatchConfig config) {
        if (evidenceCount == 0) {
            return 0.0;
        }

        // 基础分数：根据证据数量
        double baseScore = Math.min(1.0, evidenceCount / 3.0);

        // 根据匹配类型调整分数
        switch (config.getMatchType()) {
            case "REGEX":
                return Math.min(0.95, baseScore + 0.1); // 正则匹配更精确
            case "PHRASE":
                return Math.min(0.9, baseScore + 0.05); // 短语匹配较精确
            case "COMBINATION":
                return Math.min(0.95, baseScore + 0.15); // 组合匹配最复杂
            default:
                return baseScore;
        }
    }

    /**
     * 创建元数据
     */
    private Map<String, Object> createMetadata(DocumentChunk chunk, MatchConfig config, List<Evidence> evidences) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("chunkType", chunk.getType());
        metadata.put("matchType", config.getMatchType());
        metadata.put("evidenceCount", evidences.size());
        metadata.put("textLength", chunk.getText().length());

        if (chunk.getMetadata() != null) {
            metadata.put("chunkMetadata", chunk.getMetadata());
        }

        return metadata;
    }

    /**
     * 匹配配置类
     */
    private static class MatchConfig {
        private String matchType;
        private List<String> keywords = new ArrayList<>();
        private List<String> phrases = new ArrayList<>();
        private List<String> excludeKeywords = new ArrayList<>();
        private String regexPattern;
        private boolean reportNonMatches;
        private String combinationOperator;
        private List<String> groupA = new ArrayList<>();
        private List<String> groupB = new ArrayList<>();
        private int maxDistance;

        // Getters and Setters
        public String getMatchType() { return matchType; }
        public void setMatchType(String matchType) { this.matchType = matchType; }

        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }

        public List<String> getPhrases() { return phrases; }
        public void setPhrases(List<String> phrases) { this.phrases = phrases; }

        public List<String> getExcludeKeywords() { return excludeKeywords; }
        public void setExcludeKeywords(List<String> excludeKeywords) { this.excludeKeywords = excludeKeywords; }

        public String getRegexPattern() { return regexPattern; }
        public void setRegexPattern(String regexPattern) { this.regexPattern = regexPattern; }

        public boolean isReportNonMatches() { return reportNonMatches; }
        public void setReportNonMatches(boolean reportNonMatches) { this.reportNonMatches = reportNonMatches; }

        public String getCombinationOperator() { return combinationOperator; }
        public void setCombinationOperator(String combinationOperator) { this.combinationOperator = combinationOperator; }

        public List<String> getGroupA() { return groupA; }
        public void setGroupA(List<String> groupA) { this.groupA = groupA; }

        public List<String> getGroupB() { return groupB; }
        public void setGroupB(List<String> groupB) { this.groupB = groupB; }

        public int getMaxDistance() { return maxDistance; }
        public void setMaxDistance(int maxDistance) { this.maxDistance = maxDistance; }
    }
}