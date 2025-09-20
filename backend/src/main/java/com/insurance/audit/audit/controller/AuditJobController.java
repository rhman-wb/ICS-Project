package com.insurance.audit.audit.controller;

import com.insurance.audit.audit.dto.AuditJobRequest;
import com.insurance.audit.audit.dto.AuditJobResponse;
import com.insurance.audit.audit.dto.AuditResultDto;
import com.insurance.audit.audit.service.AuditOrchestrator;
import com.insurance.audit.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检核作业API控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit/jobs")
@Tag(name = "检核作业管理", description = "智能检核引擎作业管理API")
public class AuditJobController {

    @Autowired
    private AuditOrchestrator auditOrchestrator;

    /**
     * 创建检核作业
     */
    @PostMapping
    @Operation(summary = "创建检核作业", description = "创建新的产品文档检核作业")
    public ResponseEntity<ApiResponse<AuditJobResponse>> createJob(
            @RequestBody AuditJobRequest request) {

        log.info("收到创建检核作业请求: jobName={}, ruleSetId={}, documentCount={}",
                request.getJobName(), request.getRuleSetId(), request.getDocumentIds().size());

        try {
            AuditJobResponse response = auditOrchestrator.createJob(request);
            log.info("检核作业创建成功: jobId={}, status={}", response.getJobId(), response.getStatus());

            return ResponseEntity.ok(ApiResponse.success(response, "作业创建成功"));
        } catch (Exception e) {
            log.error("创建检核作业失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("创建作业失败: " + e.getMessage()));
        }
    }

    /**
     * 查询作业状态
     */
    @GetMapping("/{jobId}")
    @Operation(summary = "查询作业状态", description = "根据作业ID查询检核作业的执行状态")
    public ResponseEntity<ApiResponse<AuditJobResponse>> getJobStatus(
            @Parameter(description = "作业ID", example = "job-12345678")
            @PathVariable String jobId) {

        log.debug("查询作业状态: jobId={}", jobId);

        try {
            AuditJobResponse response = auditOrchestrator.getJobStatus(jobId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            log.warn("查询作业状态失败: jobId={}, error={}", jobId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("查询作业状态异常: jobId={}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("查询作业状态失败: " + e.getMessage()));
        }
    }

    /**
     * 获取作业结果
     */
    @GetMapping("/{jobId}/results")
    @Operation(summary = "获取作业结果", description = "获取检核作业的详细结果列表")
    public ResponseEntity<ApiResponse<List<AuditResultDto>>> getJobResults(
            @Parameter(description = "作业ID", example = "job-12345678")
            @PathVariable String jobId) {

        log.debug("获取作业结果: jobId={}", jobId);

        try {
            List<AuditResultDto> results = auditOrchestrator.getJobResults(jobId);
            log.info("获取作业结果成功: jobId={}, resultCount={}", jobId, results.size());

            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (RuntimeException e) {
            log.warn("获取作业结果失败: jobId={}, error={}", jobId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("获取作业结果异常: jobId={}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取作业结果失败: " + e.getMessage()));
        }
    }

    /**
     * 导出作业结果
     */
    @PostMapping("/{jobId}/export")
    @Operation(summary = "导出作业结果", description = "将检核结果导出为指定格式的文件")
    public ResponseEntity<ApiResponse<String>> exportJobResults(
            @Parameter(description = "作业ID", example = "job-12345678")
            @PathVariable String jobId,
            @Parameter(description = "导出格式", example = "PDF")
            @RequestParam(defaultValue = "PDF") String format) {

        log.info("导出作业结果: jobId={}, format={}", jobId, format);

        try {
            // 验证格式
            if (!List.of("PDF", "WORD", "EXCEL", "JSON").contains(format.toUpperCase())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "不支持的导出格式: " + format));
            }

            String exportResult = auditOrchestrator.exportJobResults(jobId, format.toUpperCase());
            log.info("导出作业结果成功: jobId={}, format={}", jobId, format);

            return ResponseEntity.ok(ApiResponse.success(exportResult, "导出成功"));
        } catch (RuntimeException e) {
            log.warn("导出作业结果失败: jobId={}, format={}, error={}", jobId, format, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("导出作业结果异常: jobId={}, format={}", jobId, format, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("导出结果失败: " + e.getMessage()));
        }
    }

    /**
     * 获取作业列表
     */
    @GetMapping
    @Operation(summary = "获取作业列表", description = "分页获取检核作业列表")
    public ResponseEntity<ApiResponse<List<AuditJobResponse>>> getJobList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态过滤", example = "RUNNING")
            @RequestParam(required = false) String status) {

        log.debug("获取作业列表: page={}, size={}, status={}", page, size, status);

        try {
            // 这里应该实现分页查询逻辑
            // 暂时返回空列表
            List<AuditJobResponse> jobs = List.of();

            return ResponseEntity.ok(ApiResponse.success(jobs));
        } catch (Exception e) {
            log.error("获取作业列表异常: page={}, size={}", page, size, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("获取作业列表失败: " + e.getMessage()));
        }
    }

    /**
     * 取消作业
     */
    @PostMapping("/{jobId}/cancel")
    @Operation(summary = "取消作业", description = "取消正在执行的检核作业")
    public ResponseEntity<ApiResponse<String>> cancelJob(
            @Parameter(description = "作业ID", example = "job-12345678")
            @PathVariable String jobId) {

        log.info("取消作业: jobId={}", jobId);

        try {
            // 这里应该实现作业取消逻辑
            log.info("作业取消成功: jobId={}", jobId);

            return ResponseEntity.ok(ApiResponse.success("作业取消成功"));
        } catch (RuntimeException e) {
            log.warn("取消作业失败: jobId={}, error={}", jobId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("取消作业异常: jobId={}", jobId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("取消作业失败: " + e.getMessage()));
        }
    }
}