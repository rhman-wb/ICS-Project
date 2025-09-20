package com.insurance.audit.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检核结果DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "检核结果")
public class AuditResultDto {

    /**
     * 结果ID
     */
    @Schema(description = "结果ID", example = "result-123456")
    private String resultId;

    /**
     * 作业ID
     */
    @Schema(description = "作业ID", example = "job-123456")
    private String jobId;

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "rule-001")
    private String ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "免责条款必须明确")
    private String ruleName;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "doc-123")
    private String documentId;

    /**
     * 检核状态
     */
    @Schema(description = "检核状态", example = "PASSED")
    private String status;

    /**
     * 置信度/相似度分数
     */
    @Schema(description = "置信度分数", example = "0.95")
    private Double score;

    /**
     * 阈值
     */
    @Schema(description = "阈值", example = "0.8")
    private Double threshold;

    /**
     * 证据列表
     */
    @Schema(description = "证据列表")
    private List<Evidence> evidences;

    /**
     * 建议
     */
    @Schema(description = "建议", example = "建议在第3页补充相关条款")
    private String recommendation;

    /**
     * 检核时间
     */
    @Schema(description = "检核时间")
    private LocalDateTime auditTime;

    /**
     * 证据信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "证据信息")
    public static class Evidence {
        /**
         * 文本内容
         */
        @Schema(description = "文本内容", example = "免责条款：本保险不承担...")
        private String text;

        /**
         * 页码
         */
        @Schema(description = "页码", example = "3")
        private Integer pageNumber;

        /**
         * 段落编号
         */
        @Schema(description = "段落编号", example = "2")
        private Integer paragraphIndex;

        /**
         * 起始位置
         */
        @Schema(description = "起始位置", example = "100")
        private Integer startPos;

        /**
         * 结束位置
         */
        @Schema(description = "结束位置", example = "200")
        private Integer endPos;

        /**
         * 匹配类型
         */
        @Schema(description = "匹配类型", example = "KEYWORD")
        private String matchType;
    }
}