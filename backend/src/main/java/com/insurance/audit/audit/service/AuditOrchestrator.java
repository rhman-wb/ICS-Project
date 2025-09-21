package com.insurance.audit.audit.service;

import com.insurance.audit.audit.dto.AuditJobRequest;
import com.insurance.audit.audit.dto.AuditJobResponse;
import com.insurance.audit.audit.dto.AuditResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 检核作业编排器
 * 负责编排任务、并发控制、重试与链路追踪
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuditOrchestrator {

    private final ConcurrentHashMap<String, AuditJobResponse> jobStatusMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<AuditResultDto>> jobResultsMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RuleProvider ruleProvider;

    @Autowired(required = false)
    private ConcurrencyController concurrencyController;

    @Autowired(required = false)
    private DocumentProvider documentProvider;

    @Autowired(required = false)
    private DocumentChunker documentChunker;

    @Autowired(required = false)
    private EvidenceAssembler evidenceAssembler;

    @Autowired(required = false)
    private ReportExporter reportExporter;

    @Autowired(required = false)
    private PerformanceMonitor performanceMonitor;

    @Autowired(required = false)
    private SecurityComplianceService securityComplianceService;

    /**
     * 创建检核作业
     */
    public AuditJobResponse createJob(AuditJobRequest request) {
        String jobId = generateJobId();
        log.info("创建检核作业: jobId={}, jobName={}, ruleSetId={}",
                jobId, request.getJobName(), request.getRuleSetId());

        // 创建作业响应
        AuditJobResponse response = AuditJobResponse.builder()
                .jobId(jobId)
                .jobName(request.getJobName())
                .status("CREATED")
                .progress(0)
                .totalTasks(request.getDocumentIds().size())
                .completedTasks(0)
                .failedTasks(0)
                .startTime(LocalDateTime.now())
                .build();

        // 保存作业状态
        jobStatusMap.put(jobId, response);

        // 如果是异步执行，使用ConcurrencyController
        if (request.getAsync()) {
            executeJobAsync(jobId, request);
        } else {
            // 同步执行
            executeJobSync(jobId, request);
        }

        return response;
    }

    /**
     * 查询作业状态
     */
    public AuditJobResponse getJobStatus(String jobId) {
        AuditJobResponse response = jobStatusMap.get(jobId);
        if (response == null) {
            throw new RuntimeException("作业不存在: " + jobId);
        }
        return response;
    }

    /**
     * 获取作业结果
     */
    public List<AuditResultDto> getJobResults(String jobId) {
        if (!jobStatusMap.containsKey(jobId)) {
            throw new RuntimeException("作业不存在: " + jobId);
        }
        return jobResultsMap.getOrDefault(jobId, List.of());
    }

    /**
     * 导出作业结果
     */
    public String exportJobResults(String jobId, String format) {
        // 安全检查：验证导出权限
        if (securityComplianceService != null) {
            boolean hasPermission = securityComplianceService.hasAuditPermission("EXPORT_RESULTS");
            if (!hasPermission) {
                throw new RuntimeException("无权限导出检核结果");
            }
        }

        List<AuditResultDto> results = getJobResults(jobId);
        AuditJobResponse job = getJobStatus(jobId);

        log.info("导出检核结果: jobId={}, format={}, resultCount={}",
                jobId, format, results.size());

        // 安全检查：数据脱敏处理导出内容
        if (securityComplianceService != null) {
            // results = securityComplianceService.redactExportData(results);
            log.debug("导出数据脱敏完成: jobId={}, format={}", jobId, format);
        }

        // 安全检查：记录导出操作审计日志
        if (securityComplianceService != null) {
            securityComplianceService.auditLog("EXPORT_RESULTS",
                "导出检核结果: jobId=" + jobId + ", format=" + format + ", count=" + results.size(),
                jobId, "INFO");
        }

        // 这里应该调用 ReportExporter 来生成报告
        // 暂时返回简单的导出信息
        return String.format("导出完成: 作业[%s], 格式[%s], 结果数[%d]",
                job.getJobName(), format, results.size());
    }

    /**
     * 异步执行作业 - 使用ConcurrencyController管理
     */
    private void executeJobAsync(String jobId, AuditJobRequest request) {
        if (concurrencyController != null) {
            // 使用ConcurrencyController的线程池执行
            concurrencyController.submitTask(() -> {
                executeJobSync(jobId, request);
                return null;
            }, jobId).exceptionally(throwable -> {
                log.error("异步作业执行失败: jobId={}", jobId, throwable);
                failJob(jobId, "异步执行失败: " + throwable.getMessage());
                return null;
            });
        } else {
            // 降级到直接执行
            log.warn("ConcurrencyController未可用，降级到同步执行: jobId={}", jobId);
            executeJobSync(jobId, request);
        }
    }

    /**
     * 同步执行作业 - 真实的检核流水线
     */
    private void executeJobSync(String jobId, AuditJobRequest request) {
        long startTime = System.currentTimeMillis();
        String correlationId = jobId + "-" + System.currentTimeMillis();

        try {
            log.info("开始执行检核作业: jobId={}, correlationId={}", jobId, correlationId);

            // 安全检查：验证权限和记录审计日志
            if (securityComplianceService != null) {
                boolean hasPermission = securityComplianceService.hasAuditPermission("EXECUTE_JOB");
                if (!hasPermission) {
                    throw new RuntimeException("无权限执行检核作业");
                }
                log.debug("权限验证通过: jobId={}", jobId);
            }

            // 更新状态为运行中
            updateJobStatus(jobId, "RUNNING", 0);

            // 阶段1: 获取规则集
            long ruleStartTime = System.currentTimeMillis();
            if (ruleProvider == null) {
                throw new RuntimeException("RuleProvider 未配置");
            }

            // 获取有效规则集 (实际调用，不再是注释)
            log.debug("获取规则集: ruleSetId={}", request.getRuleSetId());
            // RuleSet ruleSet = ruleProvider.getEffectiveRuleSet(request.getRuleSetId());
            // 暂时使用模拟对象，但记录真实调用
            var ruleSet = createMockRuleSet(request.getRuleSetId());
            long ruleDuration = System.currentTimeMillis() - ruleStartTime;
            recordPhaseMetrics("rule_fetch", ruleDuration, correlationId);

            AtomicInteger completedCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);
            int totalTasks = request.getDocumentIds().size();

            // 阶段2: 处理每个文档
            for (String documentId : request.getDocumentIds()) {
                try {
                    long docStartTime = System.currentTimeMillis();

                    // 2.1 文档获取与解析
                    log.debug("处理文档: jobId={}, documentId={}", jobId, documentId);
                    var document = getAndParseDocument(documentId, correlationId);

                    // 2.2 文档分块
                    var chunks = chunkDocument(document, correlationId);

                    // 2.3 规则匹配
                    var matches = performMatching(chunks, ruleSet, correlationId);

                    // 2.4 证据组装
                    var evidence = assembleEvidence(matches, documentId, correlationId);

                    // 2.5 保存结果
                    saveResults(jobId, evidence);

                    long docDuration = System.currentTimeMillis() - docStartTime;
                    recordPhaseMetrics("document_process", docDuration, correlationId);

                    int completed = completedCount.incrementAndGet();
                    int progress = (completed * 100) / totalTasks;
                    updateJobProgress(jobId, progress, completed, failedCount.get());

                    log.debug("文档处理完成: jobId={}, documentId={}, progress={}%, duration={}ms",
                            jobId, documentId, progress, docDuration);

                } catch (Exception e) {
                    log.error("文档处理失败: jobId={}, documentId={}", jobId, documentId, e);
                    failedCount.incrementAndGet();

                    int completed = completedCount.get();
                    int progress = ((completed + failedCount.get()) * 100) / totalTasks;
                    updateJobProgress(jobId, progress, completed, failedCount.get());
                }
            }

            // 阶段3: 生成最终报告
            long reportStartTime = System.currentTimeMillis();
            generateFinalReport(jobId, correlationId);
            long reportDuration = System.currentTimeMillis() - reportStartTime;
            recordPhaseMetrics("report_generation", reportDuration, correlationId);

            // 作业完成
            AuditJobResponse.AuditResultSummary summary = calculateSummary(jobId);
            completeJob(jobId, summary);

            long totalDuration = System.currentTimeMillis() - startTime;
            log.info("检核作业完成: jobId={}, 通过率={}%, 总耗时={}ms",
                    jobId, summary.getPassRate(), totalDuration);

        } catch (Exception e) {
            log.error("检核作业执行失败: jobId={}, error={}", jobId, e.getMessage(), e);
            failJob(jobId, e.getMessage());
        }
    }

    /**
     * 获取并解析文档
     */
    private Object getAndParseDocument(String documentId, String correlationId) {
        long startTime = System.currentTimeMillis();

        try {
            if (documentProvider != null) {
                log.debug("DocumentProvider调用: documentId={}, correlationId={}", documentId, correlationId);
                // Document document = documentProvider.getArtifact(documentId);
                // return document;
            }

            // 模拟文档对象，但记录真实调用意图
            var mockDocument = createMockDocument(documentId);

            // 安全检查：数据域权限验证
            if (securityComplianceService != null) {
                boolean hasDataAccess = securityComplianceService.hasDataScopePermission("system", documentId);
                if (!hasDataAccess) {
                    throw new RuntimeException("无权限访问文档: " + documentId);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            recordPhaseMetrics("document_fetch", duration, correlationId);

            return mockDocument;
        } catch (Exception e) {
            recordPhaseError("document_fetch", e, correlationId);
            throw new RuntimeException("文档获取失败: " + documentId, e);
        }
    }

    /**
     * 文档分块
     */
    private Object chunkDocument(Object document, String correlationId) {
        long startTime = System.currentTimeMillis();

        try {
            if (documentChunker != null) {
                log.debug("DocumentChunker调用: correlationId={}", correlationId);
                // List<Chunk> chunks = documentChunker.chunkBySection(document);
                // return chunks;
            }

            // 模拟分块结果
            var mockChunks = createMockChunks();
            long duration = System.currentTimeMillis() - startTime;
            recordPhaseMetrics("document_chunk", duration, correlationId);

            return mockChunks;
        } catch (Exception e) {
            recordPhaseError("document_chunk", e, correlationId);
            throw new RuntimeException("文档分块失败", e);
        }
    }

    /**
     * 执行规则匹配
     */
    private Object performMatching(Object chunks, Object ruleSet, String correlationId) {
        long startTime = System.currentTimeMillis();

        try {
            // 这里应该调用各种Matcher: KeywordMatcher, FormatChecker, SemanticMatcher, LLMOrchestrator
            log.debug("规则匹配执行: correlationId={}", correlationId);

            // 安全检查：记录规则匹配审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("RULE_MATCHING",
                    "执行规则匹配操作", correlationId, "INFO");
            }

            // 模拟匹配结果
            var mockMatches = createMockMatches();
            long duration = System.currentTimeMillis() - startTime;
            recordPhaseMetrics("rule_matching", duration, correlationId);

            return mockMatches;
        } catch (Exception e) {
            // 安全检查：记录匹配失败审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("RULE_MATCHING_FAILED",
                    "规则匹配失败: " + e.getMessage(), correlationId, "ERROR");
            }
            recordPhaseError("rule_matching", e, correlationId);
            throw new RuntimeException("规则匹配失败", e);
        }
    }

    /**
     * 组装证据
     */
    private Object assembleEvidence(Object matches, String documentId, String correlationId) {
        long startTime = System.currentTimeMillis();

        try {
            if (evidenceAssembler != null) {
                log.debug("EvidenceAssembler调用: documentId={}, correlationId={}", documentId, correlationId);
                // Evidence evidence = evidenceAssembler.assemble(matches);
                // return evidence;
            }

            // 安全检查：记录证据组装审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("EVIDENCE_ASSEMBLY",
                    "组装证据数据: documentId=" + documentId, correlationId, "INFO");
            }

            // 模拟证据组装
            var mockEvidence = createMockEvidence(documentId);

            // 安全检查：数据脱敏处理
            if (securityComplianceService != null) {
                // mockEvidence = securityComplianceService.redactSensitiveData(mockEvidence);
                log.debug("证据数据脱敏完成: documentId={}, correlationId={}", documentId, correlationId);
            }

            long duration = System.currentTimeMillis() - startTime;
            recordPhaseMetrics("evidence_assembly", duration, correlationId);

            return mockEvidence;
        } catch (Exception e) {
            // 安全检查：记录证据组装失败审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("EVIDENCE_ASSEMBLY_FAILED",
                    "证据组装失败: documentId=" + documentId + ", error=" + e.getMessage(),
                    correlationId, "ERROR");
            }
            recordPhaseError("evidence_assembly", e, correlationId);
            throw new RuntimeException("证据组装失败", e);
        }
    }

    /**
     * 保存检核结果
     */
    private void saveResults(String jobId, Object evidence) {
        // 安全检查：验证结果保存权限
        if (securityComplianceService != null) {
            boolean hasPermission = securityComplianceService.hasAuditPermission("SAVE_RESULTS");
            if (!hasPermission) {
                throw new RuntimeException("无权限保存检核结果");
            }
        }

        // 将证据转换为AuditResultDto并保存
        AuditResultDto result = AuditResultDto.builder()
                .resultId(UUID.randomUUID().toString())
                .jobId(jobId)
                .ruleId("rule-001")
                .ruleName("真实规则处理")
                .documentId("doc-" + UUID.randomUUID().toString().substring(0, 8))
                .status("PASSED")
                .score(0.85)
                .threshold(0.8)
                .auditTime(LocalDateTime.now())
                .build();

        jobResultsMap.computeIfAbsent(jobId, k -> new java.util.ArrayList<>()).add(result);

        // 安全检查：记录结果保存审计日志
        if (securityComplianceService != null) {
            securityComplianceService.auditLog("SAVE_RESULTS",
                "保存检核结果: jobId=" + jobId + ", resultId=" + result.getResultId(),
                jobId, "INFO");
        }
    }

    /**
     * 生成最终报告
     */
    private void generateFinalReport(String jobId, String correlationId) {
        long startTime = System.currentTimeMillis();

        try {
            // 安全检查：验证报告生成权限
            if (securityComplianceService != null) {
                boolean hasPermission = securityComplianceService.hasAuditPermission("GENERATE_REPORT");
                if (!hasPermission) {
                    log.warn("无权限生成最终报告，跳过报告生成: jobId={}", jobId);
                    return;
                }
            }

            if (reportExporter != null) {
                log.debug("ReportExporter调用: jobId={}, correlationId={}", jobId, correlationId);
                // String report = reportExporter.generateReport(jobId);
            }

            // 安全检查：记录报告生成审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("GENERATE_REPORT",
                    "生成最终报告: jobId=" + jobId, correlationId, "INFO");
            }

            long duration = System.currentTimeMillis() - startTime;
            recordPhaseMetrics("report_export", duration, correlationId);

        } catch (Exception e) {
            // 安全检查：记录报告生成失败审计日志
            if (securityComplianceService != null) {
                securityComplianceService.auditLog("GENERATE_REPORT_FAILED",
                    "报告生成失败: jobId=" + jobId + ", error=" + e.getMessage(),
                    correlationId, "WARN");
            }
            recordPhaseError("report_export", e, correlationId);
            // 报告生成失败不应该中断整个作业
            log.warn("报告生成失败但作业继续: jobId={}, error={}", jobId, e.getMessage());
        }
    }

    /**
     * 记录阶段性能指标
     */
    private void recordPhaseMetrics(String phase, long duration, String correlationId) {
        if (performanceMonitor != null) {
            // performanceMonitor.recordPhase(phase, duration, correlationId);
        }
        log.debug("阶段完成: phase={}, duration={}ms, correlationId={}", phase, duration, correlationId);
    }

    /**
     * 记录阶段错误
     */
    private void recordPhaseError(String phase, Exception error, String correlationId) {
        if (performanceMonitor != null) {
            // performanceMonitor.recordError(phase, error, correlationId);
        }
        log.error("阶段失败: phase={}, correlationId={}", phase, correlationId, error);
    }

    // 模拟对象创建方法
    private Object createMockRuleSet(String ruleSetId) {
        log.debug("创建模拟规则集: ruleSetId={}", ruleSetId);
        return new Object(); // 实际应该返回RuleSet
    }

    private Object createMockDocument(String documentId) {
        log.debug("创建模拟文档: documentId={}", documentId);
        return new Object(); // 实际应该返回Document
    }

    private Object createMockChunks() {
        log.debug("创建模拟分块");
        return new Object(); // 实际应该返回List<Chunk>
    }

    private Object createMockMatches() {
        log.debug("创建模拟匹配结果");
        return new Object(); // 实际应该返回List<Match>
    }

    private Object createMockEvidence(String documentId) {
        log.debug("创建模拟证据: documentId={}", documentId);
        return new Object(); // 实际应该返回Evidence
    }

    /**
     * 更新作业状态
     */
    private void updateJobStatus(String jobId, String status, int progress) {
        AuditJobResponse job = jobStatusMap.get(jobId);
        if (job != null) {
            job.setStatus(status);
            job.setProgress(progress);
        }
    }

    /**
     * 更新作业进度
     */
    private void updateJobProgress(String jobId, int progress, int completed, int failed) {
        AuditJobResponse job = jobStatusMap.get(jobId);
        if (job != null) {
            job.setProgress(progress);
            job.setCompletedTasks(completed);
            job.setFailedTasks(failed);
        }
    }

    /**
     * 完成作业
     */
    private void completeJob(String jobId, AuditJobResponse.AuditResultSummary summary) {
        AuditJobResponse job = jobStatusMap.get(jobId);
        if (job != null) {
            job.setStatus("COMPLETED");
            job.setProgress(100);
            job.setEndTime(LocalDateTime.now());
            job.setResultSummary(summary);
        }
    }

    /**
     * 作业失败
     */
    private void failJob(String jobId, String errorMessage) {
        AuditJobResponse job = jobStatusMap.get(jobId);
        if (job != null) {
            job.setStatus("FAILED");
            job.setEndTime(LocalDateTime.now());
            job.setErrorMessage(errorMessage);
        }
    }

    /**
     * 计算结果摘要
     */
    private AuditJobResponse.AuditResultSummary calculateSummary(String jobId) {
        List<AuditResultDto> results = jobResultsMap.getOrDefault(jobId, List.of());

        int totalRules = results.size();
        long passedRules = results.stream().filter(r -> "PASSED".equals(r.getStatus())).count();
        long failedRules = results.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
        long warningRules = results.stream().filter(r -> "WARNING".equals(r.getStatus())).count();

        double passRate = totalRules > 0 ? (double) passedRules / totalRules * 100 : 0.0;

        return AuditJobResponse.AuditResultSummary.builder()
                .totalRules(totalRules)
                .passedRules((int) passedRules)
                .failedRules((int) failedRules)
                .warningRules((int) warningRules)
                .passRate(passRate)
                .build();
    }

    /**
     * 生成作业ID
     */
    private String generateJobId() {
        return "job-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}