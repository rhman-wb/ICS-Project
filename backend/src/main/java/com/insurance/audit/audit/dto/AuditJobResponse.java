package com.insurance.audit.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检核作业响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "检核作业响应")
public class AuditJobResponse {

    /**
     * 作业ID
     */
    @Schema(description = "作业ID", example = "job-123456")
    private String jobId;

    /**
     * 作业名称
     */
    @Schema(description = "作业名称", example = "产品条款检核")
    private String jobName;

    /**
     * 作业状态
     */
    @Schema(description = "作业状态", example = "RUNNING")
    private String status;

    /**
     * 进度百分比
     */
    @Schema(description = "进度百分比", example = "75")
    private Integer progress;

    /**
     * 总任务数
     */
    @Schema(description = "总任务数", example = "100")
    private Integer totalTasks;

    /**
     * 已完成任务数
     */
    @Schema(description = "已完成任务数", example = "75")
    private Integer completedTasks;

    /**
     * 失败任务数
     */
    @Schema(description = "失败任务数", example = "2")
    private Integer failedTasks;

    /**
     * 作业开始时间
     */
    @Schema(description = "作业开始时间")
    private LocalDateTime startTime;

    /**
     * 作业结束时间
     */
    @Schema(description = "作业结束时间")
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 结果摘要
     */
    @Schema(description = "结果摘要")
    private AuditResultSummary resultSummary;

    /**
     * 检核结果摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "检核结果摘要")
    public static class AuditResultSummary {
        /**
         * 总检核项数
         */
        @Schema(description = "总检核项数", example = "50")
        private Integer totalRules;

        /**
         * 通过项数
         */
        @Schema(description = "通过项数", example = "45")
        private Integer passedRules;

        /**
         * 失败项数
         */
        @Schema(description = "失败项数", example = "3")
        private Integer failedRules;

        /**
         * 警告项数
         */
        @Schema(description = "警告项数", example = "2")
        private Integer warningRules;

        /**
         * 通过率
         */
        @Schema(description = "通过率", example = "90.0")
        private Double passRate;
    }
}