package com.insurance.audit.audit.service;

import com.insurance.audit.audit.dto.AuditJobRequest;
import com.insurance.audit.audit.dto.AuditJobResponse;
import com.insurance.audit.audit.dto.AuditResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    private DocumentProvider documentProvider;

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

        // 如果是异步执行，启动异步任务
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
        List<AuditResultDto> results = getJobResults(jobId);
        AuditJobResponse job = getJobStatus(jobId);

        log.info("导出检核结果: jobId={}, format={}, resultCount={}",
                jobId, format, results.size());

        // 这里应该调用 ReportExporter 来生成报告
        // 暂时返回简单的导出信息
        return String.format("导出完成: 作业[%s], 格式[%s], 结果数[%d]",
                job.getJobName(), format, results.size());
    }

    /**
     * 异步执行作业
     */
    @Async
    public CompletableFuture<Void> executeJobAsync(String jobId, AuditJobRequest request) {
        return CompletableFuture.runAsync(() -> executeJobSync(jobId, request));
    }

    /**
     * 同步执行作业
     */
    private void executeJobSync(String jobId, AuditJobRequest request) {
        try {
            log.info("开始执行检核作业: jobId={}", jobId);

            // 更新状态为运行中
            updateJobStatus(jobId, "RUNNING", 0);

            // 获取规则集
            // RuleSet ruleSet = ruleProvider.getEffectiveRuleSet(request.getRuleSetId());

            AtomicInteger completedCount = new AtomicInteger(0);
            AtomicInteger failedCount = new AtomicInteger(0);
            int totalTasks = request.getDocumentIds().size();

            // 处理每个文档
            for (String documentId : request.getDocumentIds()) {
                try {
                    // 模拟文档处理
                    processDocument(jobId, documentId, request.getRuleSetId());

                    int completed = completedCount.incrementAndGet();
                    int progress = (completed * 100) / totalTasks;
                    updateJobProgress(jobId, progress, completed, failedCount.get());

                    log.debug("文档处理完成: jobId={}, documentId={}, progress={}%",
                            jobId, documentId, progress);

                } catch (Exception e) {
                    log.error("文档处理失败: jobId={}, documentId={}", jobId, documentId, e);
                    failedCount.incrementAndGet();

                    int completed = completedCount.get();
                    int progress = ((completed + failedCount.get()) * 100) / totalTasks;
                    updateJobProgress(jobId, progress, completed, failedCount.get());
                }
            }

            // 作业完成
            AuditJobResponse.AuditResultSummary summary = calculateSummary(jobId);
            completeJob(jobId, summary);

            log.info("检核作业完成: jobId={}, 通过率={}%", jobId, summary.getPassRate());

        } catch (Exception e) {
            log.error("检核作业执行失败: jobId={}", jobId, e);
            failJob(jobId, e.getMessage());
        }
    }

    /**
     * 处理单个文档
     */
    private void processDocument(String jobId, String documentId, String ruleSetId) {
        // 模拟文档处理逻辑
        // 这里应该调用 DocumentProvider, 各种 Matcher 等组件

        // 模拟生成一些检核结果
        AuditResultDto result = AuditResultDto.builder()
                .resultId(UUID.randomUUID().toString())
                .jobId(jobId)
                .ruleId("rule-001")
                .ruleName("示例规则")
                .documentId(documentId)
                .status("PASSED")
                .score(0.85)
                .threshold(0.8)
                .auditTime(LocalDateTime.now())
                .build();

        // 保存结果
        jobResultsMap.computeIfAbsent(jobId, k -> new java.util.ArrayList<>()).add(result);

        // 模拟处理时间
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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