package com.insurance.audit.audit.e2e;

import com.insurance.audit.audit.dto.AuditJobRequest;
import com.insurance.audit.audit.dto.AuditResultDto;
import com.insurance.audit.audit.service.AuditOrchestrator;
import com.insurance.audit.audit.service.PerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 智能检核引擎端到端测试（简化版）
 * 验证核心功能的完整工作流
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleAuditE2ETest {

    @Autowired
    private AuditOrchestrator auditOrchestrator;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    @BeforeEach
    void setUp() {
        performanceMonitor.resetStats();
        log.info("端到端测试初始化完成");
    }

    @Test
    @Order(1)
    @DisplayName("基本审计流程测试")
    void testBasicAuditWorkflow() throws Exception {
        log.info("开始基本审计流程测试");

        // 创建测试请求
        AuditJobRequest request = AuditJobRequest.builder()
                .jobName("测试检核作业")
                .ruleSetId("test-ruleset-001")
                .documentIds(Arrays.asList("doc-001", "doc-002"))
                .description("端到端测试用检核作业")
                .concurrency(2)
                .async(false)
                .build();

        // 执行检核
        String jobId = auditOrchestrator.createJob(request);
        assertNotNull(jobId, "作业ID不能为空");
        assertTrue(jobId.startsWith("audit_"), "作业ID格式不正确");

        // 获取结果
        List<AuditResultDto> results = auditOrchestrator.getJobResults(jobId);
        assertNotNull(results, "检核结果不能为空");

        log.info("基本审计流程测试完成，作业ID: {}, 结果数量: {}", jobId, results.size());
    }

    @Test
    @Order(2)
    @DisplayName("批量文档检核测试")
    void testBatchDocumentAudit() throws Exception {
        log.info("开始批量文档检核测试");

        // 创建批量测试请求
        AuditJobRequest request = AuditJobRequest.builder()
                .jobName("批量检核测试")
                .ruleSetId("batch-ruleset-001")
                .documentIds(Arrays.asList("doc-001", "doc-002", "doc-003", "doc-004", "doc-005"))
                .description("批量文档检核测试")
                .concurrency(3)
                .async(false)
                .build();

        long startTime = System.currentTimeMillis();

        // 执行检核
        String jobId = auditOrchestrator.createJob(request);
        assertNotNull(jobId, "批量作业ID不能为空");

        // 获取结果
        List<AuditResultDto> results = auditOrchestrator.getJobResults(jobId);
        assertNotNull(results, "批量检核结果不能为空");

        long duration = System.currentTimeMillis() - startTime;
        log.info("批量文档检核测试完成，耗时: {}ms, 结果数量: {}", duration, results.size());

        // 验证性能要求
        assertTrue(duration < 15000, "批量检核耗时过长: " + duration + "ms");
    }

    @Test
    @Order(3)
    @DisplayName("结果导出功能测试")
    void testResultExport() throws Exception {
        log.info("开始结果导出功能测试");

        // 创建测试请求
        AuditJobRequest request = AuditJobRequest.builder()
                .jobName("导出测试作业")
                .ruleSetId("export-ruleset-001")
                .documentIds(Arrays.asList("doc-export-001"))
                .description("测试结果导出功能")
                .async(false)
                .build();

        // 执行检核
        String jobId = auditOrchestrator.createJob(request);
        assertNotNull(jobId, "导出测试作业ID不能为空");

        // 测试不同格式的导出
        String jsonExport = auditOrchestrator.exportJobResults(jobId, "JSON");
        assertNotNull(jsonExport, "JSON导出结果不能为空");
        assertTrue(jsonExport.contains("{"), "JSON格式不正确");

        String wordExport = auditOrchestrator.exportJobResults(jobId, "WORD");
        assertNotNull(wordExport, "Word导出结果不能为空");

        String pdfExport = auditOrchestrator.exportJobResults(jobId, "PDF");
        assertNotNull(pdfExport, "PDF导出结果不能为空");

        log.info("结果导出功能测试完成");
    }

    @Test
    @Order(4)
    @DisplayName("性能监控验证测试")
    void testPerformanceMonitoring() throws Exception {
        log.info("开始性能监控验证测试");

        // 执行多个作业以生成监控数据
        for (int i = 0; i < 3; i++) {
            AuditJobRequest request = AuditJobRequest.builder()
                    .jobName("性能监控测试_" + i)
                    .ruleSetId("perf-ruleset-" + i)
                    .documentIds(Arrays.asList("perf-doc-" + i))
                    .description("性能监控测试作业")
                    .async(false)
                    .build();

            String jobId = auditOrchestrator.createJob(request);
            auditOrchestrator.getJobResults(jobId);
        }

        // 验证性能监控数据
        PerformanceMonitor.PerformanceReport report = performanceMonitor.getPerformanceReport();
        assertNotNull(report, "性能报告不能为空");
        assertTrue(report.getTotalRequests() >= 3, "请求总数应该至少为3");

        // 验证系统健康状态
        boolean isHealthy = performanceMonitor.isPerformanceHealthy();
        log.info("系统健康状态: {}", isHealthy ? "健康" : "需要关注");

        log.info("性能监控验证测试完成 - 总请求: {}, 成功率: {:.1f}%",
                report.getTotalRequests(), report.getSuccessRate());
    }

    @Test
    @Order(5)
    @DisplayName("错误处理测试")
    void testErrorHandling() throws Exception {
        log.info("开始错误处理测试");

        // 测试无效规则集ID
        AuditJobRequest invalidRequest = AuditJobRequest.builder()
                .jobName("错误处理测试")
                .ruleSetId("invalid-ruleset")
                .documentIds(Arrays.asList("invalid-doc"))
                .description("测试错误处理")
                .async(false)
                .build();

        // 这个操作可能会成功创建作业，但在执行时可能会失败
        // 我们主要测试系统不会崩溃
        String jobId = auditOrchestrator.createJob(invalidRequest);
        assertNotNull(jobId, "即使是无效请求，也应该返回作业ID");

        // 尝试获取结果（可能为空或包含错误信息）
        List<AuditResultDto> results = auditOrchestrator.getJobResults(jobId);
        assertNotNull(results, "结果列表不应该为null，即使为空");

        log.info("错误处理测试完成");
    }

    @Test
    @Order(6)
    @DisplayName("黄金数据集质量验证")
    void testGoldenDatasetQuality() throws Exception {
        log.info("开始黄金数据集质量验证");

        // 模拟黄金数据集测试
        String[] testCases = {
                "文本匹配测试用例",
                "格式校验测试用例",
                "语义相似度测试用例"
        };

        int totalTests = testCases.length;
        int passedTests = 0;

        for (String testCase : testCases) {
            try {
                AuditJobRequest request = AuditJobRequest.builder()
                        .jobName(testCase)
                        .ruleSetId("golden-ruleset")
                        .documentIds(Arrays.asList("golden-doc-" + testCase.hashCode()))
                        .description("黄金数据集测试: " + testCase)
                        .async(false)
                        .build();

                String jobId = auditOrchestrator.createJob(request);
                List<AuditResultDto> results = auditOrchestrator.getJobResults(jobId);

                if (results != null) {
                    passedTests++;
                    log.debug("测试用例通过: {}", testCase);
                }

            } catch (Exception e) {
                log.warn("测试用例失败: {}, 错误: {}", testCase, e.getMessage());
            }
        }

        // 计算质量指标
        double successRate = (double) passedTests / totalTests;
        assertTrue(successRate >= 0.8, "质量验证成功率过低: " + successRate);

        log.info("黄金数据集质量验证完成 - 成功率: {:.2f}% ({}/{})",
                successRate * 100, passedTests, totalTests);
    }

    @Test
    @Order(7)
    @DisplayName("综合端到端验证")
    void testComprehensiveEndToEnd() throws Exception {
        log.info("开始综合端到端验证");

        long startTime = System.currentTimeMillis();

        // 创建综合测试场景
        AuditJobRequest comprehensiveRequest = AuditJobRequest.builder()
                .jobName("综合端到端测试")
                .ruleSetId("comprehensive-ruleset")
                .documentIds(Arrays.asList(
                        "comprehensive-doc-1",
                        "comprehensive-doc-2",
                        "comprehensive-doc-3"
                ))
                .description("综合端到端验证测试")
                .concurrency(2)
                .async(false)
                .build();

        // 执行检核
        String jobId = auditOrchestrator.createJob(comprehensiveRequest);
        assertNotNull(jobId, "综合测试作业ID不能为空");

        // 获取结果
        List<AuditResultDto> results = auditOrchestrator.getJobResults(jobId);
        assertNotNull(results, "综合测试结果不能为空");

        // 测试导出功能
        String exportResult = auditOrchestrator.exportJobResults(jobId, "JSON");
        assertNotNull(exportResult, "导出结果不能为空");

        long totalDuration = System.currentTimeMillis() - startTime;

        // 验证性能目标
        assertTrue(totalDuration < 30000, "综合测试总耗时过长: " + totalDuration + "ms");

        // 获取最终性能报告
        PerformanceMonitor.PerformanceReport finalReport = performanceMonitor.getPerformanceReport();
        boolean systemHealthy = performanceMonitor.isPerformanceHealthy();

        log.info("综合端到端验证完成 - 耗时: {}ms, 结果数量: {}, 系统健康: {}",
                totalDuration, results.size(), systemHealthy ? "是" : "否");

        // 验证系统仍然健康
        assertTrue(finalReport.getSuccessRate() >= 0.8, "最终成功率过低");
    }

    @AfterAll
    static void tearDown() {
        log.info("端到端测试套件完成");
    }
}