package com.insurance.audit.audit.matchers;

import com.insurance.audit.audit.service.DocumentProvider.DocumentChunk;
import com.insurance.audit.audit.service.RuleProvider.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 格式检查器
 * 检查文档的格式、样式和结构是否符合规范
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class FormatChecker implements Matcher {

    // 常用格式检查正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+86\\s*)?((1[3-9]\\d{9})|(\\d{3,4}-\\d{7,8}))$"
    );

    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"
    );

    private static final Pattern MONEY_PATTERN = Pattern.compile(
        "^[+-]?\\d{1,3}(,\\d{3})*(\\.\\d{2})?$"
    );

    @Override
    public List<MatchResult> match(Rule rule, List<DocumentChunk> chunks) {
        log.info("开始格式检查: ruleId={}, chunkCount={}", rule.getId(), chunks.size());

        List<MatchResult> results = new ArrayList<>();
        Map<String, Object> parameters = rule.getParameters();

        // 解析检查配置
        FormatCheckConfig config = parseFormatConfig(parameters);

        for (DocumentChunk chunk : chunks) {
            MatchResult result = checkChunkFormat(rule, chunk, config);
            if (result != null) {
                results.add(result);
            }
        }

        log.info("格式检查完成: ruleId={}, resultCount={}", rule.getId(), results.size());
        return results;
    }

    /**
     * 检查文档块格式
     */
    private MatchResult checkChunkFormat(Rule rule, DocumentChunk chunk, FormatCheckConfig config) {
        List<Evidence> evidences = new ArrayList<>();
        boolean passed = true;
        int violations = 0;

        // 执行各种格式检查
        for (String checkType : config.getCheckTypes()) {
            switch (checkType) {
                case "EMAIL":
                    violations += checkEmailFormat(chunk, evidences);
                    break;
                case "PHONE":
                    violations += checkPhoneFormat(chunk, evidences);
                    break;
                case "ID_CARD":
                    violations += checkIdCardFormat(chunk, evidences);
                    break;
                case "MONEY":
                    violations += checkMoneyFormat(chunk, evidences);
                    break;
                case "DATE":
                    violations += checkDateFormat(chunk, config, evidences);
                    break;
                case "STRUCTURE":
                    violations += checkStructureFormat(chunk, config, evidences);
                    break;
                case "LENGTH":
                    violations += checkLengthFormat(chunk, config, evidences);
                    break;
                case "ENCODING":
                    violations += checkEncodingFormat(chunk, config, evidences);
                    break;
                default:
                    log.warn("不支持的格式检查类型: {}", checkType);
                    break;
            }
        }

        // 判断是否通过检查
        passed = violations <= config.getMaxViolations();

        if (!passed || config.isReportAllResults()) {
            String status = determineFormatStatus(violations, config);
            double score = calculateFormatScore(violations, config);

            return MatchResult.builder()
                    .ruleId(rule.getId())
                    .chunkId(chunk.getId())
                    .text(chunk.getText())
                    .score(score)
                    .threshold(rule.getThreshold())
                    .status(status)
                    .evidences(evidences)
                    .metadata(createFormatMetadata(chunk, config, violations))
                    .build();
        }

        return null;
    }

    /**
     * 检查邮箱格式
     */
    private int checkEmailFormat(DocumentChunk chunk, List<Evidence> evidences) {
        String text = chunk.getText();
        String[] tokens = text.split("\\s+");
        int violations = 0;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.contains("@")) {
                if (!EMAIL_PATTERN.matcher(token).matches()) {
                    violations++;
                    evidences.add(Evidence.builder()
                            .text(token)
                            .startPos(text.indexOf(token))
                            .endPos(text.indexOf(token) + token.length())
                            .matchType("EMAIL_FORMAT_ERROR")
                            .context(Map.of("invalidEmail", token))
                            .build());
                }
            }
        }

        return violations;
    }

    /**
     * 检查电话号码格式
     */
    private int checkPhoneFormat(DocumentChunk chunk, List<Evidence> evidences) {
        String text = chunk.getText();
        Pattern phoneCandidate = Pattern.compile("\\b\\d[\\d\\s\\-\\+\\(\\)]{6,20}\\d\\b");
        java.util.regex.Matcher matcher = phoneCandidate.matcher(text);
        int violations = 0;

        while (matcher.find()) {
            String candidate = matcher.group().replaceAll("[\\s\\-\\(\\)]", "");
            if (!PHONE_PATTERN.matcher(candidate).matches()) {
                violations++;
                evidences.add(Evidence.builder()
                        .text(matcher.group())
                        .startPos(matcher.start())
                        .endPos(matcher.end())
                        .matchType("PHONE_FORMAT_ERROR")
                        .context(Map.of("invalidPhone", candidate))
                        .build());
            }
        }

        return violations;
    }

    /**
     * 检查身份证格式
     */
    private int checkIdCardFormat(DocumentChunk chunk, List<Evidence> evidences) {
        String text = chunk.getText();
        Pattern idCandidate = Pattern.compile("\\b\\d{15}|\\d{18}|\\d{17}[Xx]\\b");
        java.util.regex.Matcher matcher = idCandidate.matcher(text);
        int violations = 0;

        while (matcher.find()) {
            String candidate = matcher.group();
            if (!ID_CARD_PATTERN.matcher(candidate).matches()) {
                violations++;
                evidences.add(Evidence.builder()
                        .text(candidate)
                        .startPos(matcher.start())
                        .endPos(matcher.end())
                        .matchType("ID_CARD_FORMAT_ERROR")
                        .context(Map.of("invalidIdCard", candidate))
                        .build());
            }
        }

        return violations;
    }

    /**
     * 检查金额格式
     */
    private int checkMoneyFormat(DocumentChunk chunk, List<Evidence> evidences) {
        String text = chunk.getText();
        Pattern moneyCandidate = Pattern.compile("\\b\\d[\\d,\\.]*\\s*[元￥$]|[元￥$]\\s*\\d[\\d,\\.]*");
        java.util.regex.Matcher matcher = moneyCandidate.matcher(text);
        int violations = 0;

        while (matcher.find()) {
            String candidate = matcher.group().replaceAll("[元￥$\\s]", "");
            if (!MONEY_PATTERN.matcher(candidate).matches()) {
                violations++;
                evidences.add(Evidence.builder()
                        .text(matcher.group())
                        .startPos(matcher.start())
                        .endPos(matcher.end())
                        .matchType("MONEY_FORMAT_ERROR")
                        .context(Map.of("invalidMoney", candidate))
                        .build());
            }
        }

        return violations;
    }

    /**
     * 检查日期格式
     */
    private int checkDateFormat(DocumentChunk chunk, FormatCheckConfig config, List<Evidence> evidences) {
        String text = chunk.getText();
        List<String> dateFormats = config.getDateFormats();
        int violations = 0;

        // 查找可能的日期字符串
        Pattern dateCandidate = Pattern.compile("\\b\\d{4}[\\-/年]\\d{1,2}[\\-/月]\\d{1,2}[日]?\\b");
        java.util.regex.Matcher matcher = dateCandidate.matcher(text);

        while (matcher.find()) {
            String candidate = matcher.group();
            boolean validFormat = false;

            for (String format : dateFormats) {
                if (matchesDateFormat(candidate, format)) {
                    validFormat = true;
                    break;
                }
            }

            if (!validFormat) {
                violations++;
                evidences.add(Evidence.builder()
                        .text(candidate)
                        .startPos(matcher.start())
                        .endPos(matcher.end())
                        .matchType("DATE_FORMAT_ERROR")
                        .context(Map.of("invalidDate", candidate, "expectedFormats", dateFormats))
                        .build());
            }
        }

        return violations;
    }

    /**
     * 检查结构格式
     */
    private int checkStructureFormat(DocumentChunk chunk, FormatCheckConfig config, List<Evidence> evidences) {
        int violations = 0;

        // 检查必需的结构元素
        List<String> requiredElements = config.getRequiredElements();
        for (String element : requiredElements) {
            if (!chunk.getText().toLowerCase().contains(element.toLowerCase())) {
                violations++;
                evidences.add(Evidence.builder()
                        .text("")
                        .startPos(0)
                        .endPos(0)
                        .matchType("MISSING_REQUIRED_ELEMENT")
                        .context(Map.of("missingElement", element))
                        .build());
            }
        }

        // 检查禁止的结构元素
        List<String> forbiddenElements = config.getForbiddenElements();
        for (String element : forbiddenElements) {
            if (chunk.getText().toLowerCase().contains(element.toLowerCase())) {
                violations++;
                int index = chunk.getText().toLowerCase().indexOf(element.toLowerCase());
                evidences.add(Evidence.builder()
                        .text(element)
                        .startPos(index)
                        .endPos(index + element.length())
                        .matchType("FORBIDDEN_ELEMENT_FOUND")
                        .context(Map.of("forbiddenElement", element))
                        .build());
            }
        }

        return violations;
    }

    /**
     * 检查长度格式
     */
    private int checkLengthFormat(DocumentChunk chunk, FormatCheckConfig config, List<Evidence> evidences) {
        int violations = 0;
        int textLength = chunk.getText().length();

        if (config.getMinLength() > 0 && textLength < config.getMinLength()) {
            violations++;
            evidences.add(Evidence.builder()
                    .text(chunk.getText())
                    .startPos(0)
                    .endPos(textLength)
                    .matchType("TEXT_TOO_SHORT")
                    .context(Map.of("actualLength", textLength, "minLength", config.getMinLength()))
                    .build());
        }

        if (config.getMaxLength() > 0 && textLength > config.getMaxLength()) {
            violations++;
            evidences.add(Evidence.builder()
                    .text(chunk.getText())
                    .startPos(0)
                    .endPos(textLength)
                    .matchType("TEXT_TOO_LONG")
                    .context(Map.of("actualLength", textLength, "maxLength", config.getMaxLength()))
                    .build());
        }

        return violations;
    }

    /**
     * 检查编码格式
     */
    private int checkEncodingFormat(DocumentChunk chunk, FormatCheckConfig config, List<Evidence> evidences) {
        int violations = 0;
        String text = chunk.getText();

        // 检查非法字符
        Pattern illegalChars = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
        java.util.regex.Matcher matcher = illegalChars.matcher(text);

        while (matcher.find()) {
            violations++;
            evidences.add(Evidence.builder()
                    .text("控制字符")
                    .startPos(matcher.start())
                    .endPos(matcher.end())
                    .matchType("ILLEGAL_CHARACTER")
                    .context(Map.of("charCode", (int) matcher.group().charAt(0)))
                    .build());
        }

        return violations;
    }

    /**
     * 检查日期是否匹配指定格式
     */
    private boolean matchesDateFormat(String date, String format) {
        // 简化的日期格式匹配
        try {
            switch (format) {
                case "YYYY-MM-DD":
                    return Pattern.matches("\\d{4}-\\d{2}-\\d{2}", date);
                case "YYYY/MM/DD":
                    return Pattern.matches("\\d{4}/\\d{2}/\\d{2}", date);
                case "YYYY年MM月DD日":
                    return Pattern.matches("\\d{4}年\\d{1,2}月\\d{1,2}日", date);
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析格式检查配置
     */
    private FormatCheckConfig parseFormatConfig(Map<String, Object> parameters) {
        FormatCheckConfig config = new FormatCheckConfig();

        config.setCheckTypes((List<String>) parameters.getOrDefault("checkTypes", List.of("EMAIL", "PHONE")));
        config.setMaxViolations((Integer) parameters.getOrDefault("maxViolations", 0));
        config.setReportAllResults((Boolean) parameters.getOrDefault("reportAllResults", false));
        config.setDateFormats((List<String>) parameters.getOrDefault("dateFormats", List.of("YYYY-MM-DD")));
        config.setRequiredElements((List<String>) parameters.getOrDefault("requiredElements", List.of()));
        config.setForbiddenElements((List<String>) parameters.getOrDefault("forbiddenElements", List.of()));
        config.setMinLength((Integer) parameters.getOrDefault("minLength", 0));
        config.setMaxLength((Integer) parameters.getOrDefault("maxLength", 0));

        return config;
    }

    /**
     * 确定格式检查状态
     */
    private String determineFormatStatus(int violations, FormatCheckConfig config) {
        if (violations == 0) {
            return "PASSED";
        } else if (violations <= config.getMaxViolations()) {
            return "WARNING";
        } else {
            return "FAILED";
        }
    }

    /**
     * 计算格式检查分数
     */
    private double calculateFormatScore(int violations, FormatCheckConfig config) {
        if (violations == 0) {
            return 1.0;
        }

        double penaltyPerViolation = 0.1;
        return Math.max(0.0, 1.0 - violations * penaltyPerViolation);
    }

    /**
     * 创建格式检查元数据
     */
    private Map<String, Object> createFormatMetadata(DocumentChunk chunk, FormatCheckConfig config, int violations) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("checkTypes", config.getCheckTypes());
        metadata.put("violationCount", violations);
        metadata.put("maxViolations", config.getMaxViolations());
        metadata.put("textLength", chunk.getText().length());

        return metadata;
    }

    /**
     * 格式检查配置类
     */
    private static class FormatCheckConfig {
        private List<String> checkTypes = new ArrayList<>();
        private int maxViolations = 0;
        private boolean reportAllResults = false;
        private List<String> dateFormats = new ArrayList<>();
        private List<String> requiredElements = new ArrayList<>();
        private List<String> forbiddenElements = new ArrayList<>();
        private int minLength = 0;
        private int maxLength = 0;

        // Getters and Setters
        public List<String> getCheckTypes() { return checkTypes; }
        public void setCheckTypes(List<String> checkTypes) { this.checkTypes = checkTypes; }

        public int getMaxViolations() { return maxViolations; }
        public void setMaxViolations(int maxViolations) { this.maxViolations = maxViolations; }

        public boolean isReportAllResults() { return reportAllResults; }
        public void setReportAllResults(boolean reportAllResults) { this.reportAllResults = reportAllResults; }

        public List<String> getDateFormats() { return dateFormats; }
        public void setDateFormats(List<String> dateFormats) { this.dateFormats = dateFormats; }

        public List<String> getRequiredElements() { return requiredElements; }
        public void setRequiredElements(List<String> requiredElements) { this.requiredElements = requiredElements; }

        public List<String> getForbiddenElements() { return forbiddenElements; }
        public void setForbiddenElements(List<String> forbiddenElements) { this.forbiddenElements = forbiddenElements; }

        public int getMinLength() { return minLength; }
        public void setMinLength(int minLength) { this.minLength = minLength; }

        public int getMaxLength() { return maxLength; }
        public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    }
}