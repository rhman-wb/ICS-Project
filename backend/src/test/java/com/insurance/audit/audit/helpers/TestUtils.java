package com.insurance.audit.audit.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 测试工具类
 * 提供测试数据生成、性能测量、结果验证等功能
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 性能计时器
     */
    public static class PerformanceTimer {
        private final String operationName;
        private final long startTime;
        private long endTime;

        public PerformanceTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.currentTimeMillis();
            log.debug("开始计时: {}", operationName);
        }

        public long stop() {
            endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("操作完成: {}, 耗时: {}ms", operationName, duration);
            return duration;
        }

        public long getDuration() {
            return endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime;
        }
    }

    /**
     * 测试数据生成器
     */
    public static class TestDataGenerator {

        /**
         * 生成随机身份证号
         */
        public static String generateIdCard() {
            StringBuilder sb = new StringBuilder();

            // 地区码
            sb.append("310101");

            // 出生日期
            int year = ThreadLocalRandom.current().nextInt(1980, 2000);
            int month = ThreadLocalRandom.current().nextInt(1, 13);
            int day = ThreadLocalRandom.current().nextInt(1, 29);
            sb.append(String.format("%04d%02d%02d", year, month, day));

            // 顺序码
            sb.append(String.format("%03d", ThreadLocalRandom.current().nextInt(1, 999)));

            // 校验码
            sb.append(ThreadLocalRandom.current().nextInt(10));

            return sb.toString();
        }

        /**
         * 生成随机手机号
         */
        public static String generatePhoneNumber() {
            String[] prefixes = {"138", "139", "158", "159", "188", "189"};
            String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];

            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 8; i++) {
                sb.append(ThreadLocalRandom.current().nextInt(10));
            }

            return sb.toString();
        }

        /**
         * 生成随机邮箱
         */
        public static String generateEmail() {
            String[] domains = {"gmail.com", "163.com", "qq.com", "sina.com"};
            String username = "user" + ThreadLocalRandom.current().nextInt(1000, 9999);
            String domain = domains[ThreadLocalRandom.current().nextInt(domains.length)];

            return username + "@" + domain;
        }

        /**
         * 生成测试保险文档内容
         */
        public static String generateInsuranceDocument() {
            return String.format(
                "保险合同\n" +
                "被保险人：张三\n" +
                "身份证号：%s\n" +
                "联系电话：%s\n" +
                "邮箱地址：%s\n" +
                "保险金额：100万元\n" +
                "保险期间：2024年1月1日至2025年1月1日\n" +
                "保险责任：意外伤害、医疗费用、住院津贴\n" +
                "免赔额：1000元\n" +
                "保费：5000元/年\n" +
                "特别约定：等待期30天，既往症不予承保。",
                generateIdCard(),
                generatePhoneNumber(),
                generateEmail()
            );
        }

        /**
         * 生成大文档内容（用于性能测试）
         */
        public static String generateLargeDocument(int sizeKB) {
            StringBuilder sb = new StringBuilder();
            String baseContent = generateInsuranceDocument();

            int targetSize = sizeKB * 1024;
            while (sb.length() < targetSize) {
                sb.append(baseContent);
                sb.append("\n\n=== 第").append(sb.length() / baseContent.length()).append("部分 ===\n\n");
            }

            return sb.toString();
        }
    }

    /**
     * 结果验证器
     */
    public static class ResultValidator {

        /**
         * 验证身份证号格式
         */
        public static boolean validateIdCardFormat(String idCard) {
            if (idCard == null || idCard.length() != 18) {
                return false;
            }

            return idCard.matches("\\d{17}[\\dXx]");
        }

        /**
         * 验证手机号格式
         */
        public static boolean validatePhoneFormat(String phone) {
            if (phone == null || phone.length() != 11) {
                return false;
            }

            return phone.matches("1[3-9]\\d{9}");
        }

        /**
         * 验证邮箱格式
         */
        public static boolean validateEmailFormat(String email) {
            if (email == null) {
                return false;
            }

            return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        }

        /**
         * 验证检核结果完整性
         */
        public static boolean validateAuditResult(Map<String, Object> result) {
            if (result == null) {
                return false;
            }

            // 检查必需字段
            String[] requiredFields = {"jobId", "status", "results", "processingTime"};
            for (String field : requiredFields) {
                if (!result.containsKey(field) || result.get(field) == null) {
                    log.warn("检核结果缺少必需字段: {}", field);
                    return false;
                }
            }

            return true;
        }

        /**
         * 验证性能指标
         */
        public static boolean validatePerformanceMetrics(Map<String, Object> metrics,
                                                       long maxResponseTime,
                                                       double minSuccessRate) {
            if (metrics == null) {
                return false;
            }

            // 检查响应时间
            Object responseTimeObj = metrics.get("averageResponseTime");
            if (responseTimeObj instanceof Number) {
                long responseTime = ((Number) responseTimeObj).longValue();
                if (responseTime > maxResponseTime) {
                    log.warn("响应时间超过阈值: {}ms > {}ms", responseTime, maxResponseTime);
                    return false;
                }
            }

            // 检查成功率
            Object successRateObj = metrics.get("successRate");
            if (successRateObj instanceof Number) {
                double successRate = ((Number) successRateObj).doubleValue();
                if (successRate < minSuccessRate) {
                    log.warn("成功率低于阈值: {:.2f}% < {:.2f}%", successRate * 100, minSuccessRate * 100);
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * 数据加载器
     */
    public static class DataLoader {

        /**
         * 从JSON文件加载测试数据
         */
        public static Map<String, Object> loadJsonData(String resourcePath) throws IOException {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return objectMapper.readValue(inputStream, Map.class);
            }
        }

        /**
         * 加载黄金数据集
         */
        public static Map<String, Object> loadGoldenDataset() throws IOException {
            return loadJsonData("fixtures/golden/golden_dataset.json");
        }

        /**
         * 从黄金数据集中提取特定类别的测试用例
         */
        @SuppressWarnings("unchecked")
        public static List<Map<String, Object>> getTestCasesByCategory(String category) throws IOException {
            Map<String, Object> dataset = loadGoldenDataset();
            List<Map<String, Object>> testCases = (List<Map<String, Object>>) dataset.get("testCases");

            List<Map<String, Object>> filteredCases = new ArrayList<>();
            for (Map<String, Object> testCase : testCases) {
                if (category.equals(testCase.get("category"))) {
                    filteredCases.add(testCase);
                }
            }

            return filteredCases;
        }
    }

    /**
     * 性能基准测试器
     */
    public static class BenchmarkTester {

        /**
         * 执行单操作基准测试
         */
        public static BenchmarkResult runSingleOperationBenchmark(Runnable operation,
                                                                 int iterations) {
            List<Long> durations = new ArrayList<>();
            int failures = 0;

            for (int i = 0; i < iterations; i++) {
                try {
                    PerformanceTimer timer = new PerformanceTimer("benchmark_iteration_" + i);
                    operation.run();
                    long duration = timer.stop();
                    durations.add(duration);
                } catch (Exception e) {
                    failures++;
                    log.warn("基准测试迭代失败: {}", e.getMessage());
                }
            }

            return new BenchmarkResult(durations, failures, iterations);
        }

        /**
         * 执行并发基准测试
         */
        public static ConcurrentBenchmarkResult runConcurrentBenchmark(Runnable operation,
                                                                      int concurrency,
                                                                      int totalOperations) {
            // TODO: 实现并发基准测试逻辑
            // 这里应该使用线程池执行并发操作并收集结果

            return new ConcurrentBenchmarkResult(
                new ArrayList<>(), 0, totalOperations, concurrency
            );
        }
    }

    /**
     * 基准测试结果
     */
    public static class BenchmarkResult {
        private final List<Long> durations;
        private final int failures;
        private final int totalIterations;

        public BenchmarkResult(List<Long> durations, int failures, int totalIterations) {
            this.durations = new ArrayList<>(durations);
            this.failures = failures;
            this.totalIterations = totalIterations;
        }

        public double getAverageTime() {
            return durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        public long getMinTime() {
            return durations.stream().mapToLong(Long::longValue).min().orElse(0L);
        }

        public long getMaxTime() {
            return durations.stream().mapToLong(Long::longValue).max().orElse(0L);
        }

        public long getP95Time() {
            if (durations.isEmpty()) return 0L;

            List<Long> sorted = new ArrayList<>(durations);
            sorted.sort(Long::compareTo);

            int index = (int) Math.ceil(sorted.size() * 0.95) - 1;
            return sorted.get(Math.max(0, index));
        }

        public double getSuccessRate() {
            return totalIterations > 0 ?
                (double) (totalIterations - failures) / totalIterations : 0.0;
        }

        public String getSummary() {
            return String.format(
                "基准测试结果 - 总次数: %d, 成功率: %.2f%%, " +
                "平均时间: %.2fms, P95: %dms, 最小: %dms, 最大: %dms",
                totalIterations,
                getSuccessRate() * 100,
                getAverageTime(),
                getP95Time(),
                getMinTime(),
                getMaxTime()
            );
        }

        // Getters
        public List<Long> getDurations() { return new ArrayList<>(durations); }
        public int getFailures() { return failures; }
        public int getTotalIterations() { return totalIterations; }
    }

    /**
     * 并发基准测试结果
     */
    public static class ConcurrentBenchmarkResult extends BenchmarkResult {
        private final int concurrency;

        public ConcurrentBenchmarkResult(List<Long> durations, int failures,
                                       int totalOperations, int concurrency) {
            super(durations, failures, totalOperations);
            this.concurrency = concurrency;
        }

        public double getThroughput() {
            double avgTime = getAverageTime();
            return avgTime > 0 ? (concurrency * 1000.0) / avgTime : 0.0;
        }

        @Override
        public String getSummary() {
            return String.format(
                "并发基准测试结果 - 并发数: %d, 总操作: %d, 成功率: %.2f%%, " +
                "吞吐量: %.2f ops/s, P95: %dms",
                concurrency,
                getTotalIterations(),
                getSuccessRate() * 100,
                getThroughput(),
                getP95Time()
            );
        }

        public int getConcurrency() { return concurrency; }
    }

    /**
     * 错误统计器
     */
    public static class ErrorCollector {
        private final Map<String, Integer> errorCounts = new HashMap<>();
        private final List<String> errorMessages = new ArrayList<>();

        public void addError(String errorType, String message) {
            errorCounts.merge(errorType, 1, Integer::sum);
            errorMessages.add(String.format("[%s] %s - %s",
                FORMATTER.format(LocalDateTime.now()), errorType, message));
        }

        public void addError(String errorType, Throwable throwable) {
            addError(errorType, throwable.getMessage());
        }

        public boolean hasErrors() {
            return !errorCounts.isEmpty();
        }

        public int getTotalErrors() {
            return errorCounts.values().stream().mapToInt(Integer::intValue).sum();
        }

        public Map<String, Integer> getErrorCounts() {
            return new HashMap<>(errorCounts);
        }

        public List<String> getErrorMessages() {
            return new ArrayList<>(errorMessages);
        }

        public String getSummary() {
            if (!hasErrors()) {
                return "无错误";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("错误统计:\n");
            for (Map.Entry<String, Integer> entry : errorCounts.entrySet()) {
                sb.append(String.format("  %s: %d次\n", entry.getKey(), entry.getValue()));
            }
            sb.append("总错误数: ").append(getTotalErrors());

            return sb.toString();
        }

        public void clear() {
            errorCounts.clear();
            errorMessages.clear();
        }
    }

    /**
     * 测试报告生成器
     */
    public static class ReportGenerator {

        public static String generatePerformanceReport(BenchmarkResult result,
                                                     Map<String, Object> additionalMetrics) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 性能测试报告 ===\n");
            sb.append("生成时间: ").append(FORMATTER.format(LocalDateTime.now())).append("\n\n");

            sb.append(result.getSummary()).append("\n\n");

            if (additionalMetrics != null && !additionalMetrics.isEmpty()) {
                sb.append("附加指标:\n");
                for (Map.Entry<String, Object> entry : additionalMetrics.entrySet()) {
                    sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

            return sb.toString();
        }

        public static String generateQualityReport(double precision, double recall,
                                                 Map<String, Map<String, Double>> categoryMetrics) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 质量测试报告 ===\n");
            sb.append("生成时间: ").append(FORMATTER.format(LocalDateTime.now())).append("\n\n");

            sb.append(String.format("总体指标:\n"));
            sb.append(String.format("  精确率: %.2f%%\n", precision * 100));
            sb.append(String.format("  召回率: %.2f%%\n", recall * 100));
            sb.append(String.format("  F1分数: %.2f%%\n", 2 * precision * recall / (precision + recall) * 100));
            sb.append("\n");

            if (categoryMetrics != null && !categoryMetrics.isEmpty()) {
                sb.append("分类指标:\n");
                for (Map.Entry<String, Map<String, Double>> category : categoryMetrics.entrySet()) {
                    sb.append(String.format("  %s:\n", category.getKey()));
                    Map<String, Double> metrics = category.getValue();
                    sb.append(String.format("    精确率: %.2f%%\n", metrics.get("precision") * 100));
                    sb.append(String.format("    召回率: %.2f%%\n", metrics.get("recall") * 100));
                }
            }

            return sb.toString();
        }
    }
}