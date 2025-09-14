package com.insurance.audit.product.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文档校验结果DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档校验结果")
public class DocumentValidationResult {

    /**
     * 校验是否通过
     */
    @Schema(description = "校验是否通过", example = "false")
    private Boolean isValid;

    /**
     * 产品ID
     */
    @Schema(description = "产品ID", example = "product_001")
    private String productId;

    /**
     * 校验错误列表
     */
    @Schema(description = "校验错误列表")
    private List<ValidationError> errors;

    /**
     * 校验警告列表
     */
    @Schema(description = "校验警告列表")
    private List<ValidationWarning> warnings;

    /**
     * 校验摘要信息
     */
    @Schema(description = "校验摘要信息")
    private ValidationSummary summary;

    /**
     * 校验错误
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "校验错误")
    public static class ValidationError {

        /**
         * 错误类型
         */
        @Schema(description = "错误类型", example = "MISSING_DOCUMENT")
        private String errorType;

        /**
         * 错误代码
         */
        @Schema(description = "错误代码", example = "DOC_001")
        private String errorCode;

        /**
         * 错误消息
         */
        @Schema(description = "错误消息", example = "缺少必需的条款文档")
        private String message;

        /**
         * 涉及的要件类型
         */
        @Schema(description = "涉及的要件类型", example = "TERMS")
        private String documentType;

        /**
         * 涉及的文档ID（如果存在）
         */
        @Schema(description = "涉及的文档ID", example = "doc_001")
        private String documentId;

        /**
         * 严重级别
         */
        @Schema(description = "严重级别", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
        private String severity;

        /**
         * 建议解决方案
         */
        @Schema(description = "建议解决方案", example = "请上传产品条款文档")
        private String suggestion;
    }

    /**
     * 校验警告
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "校验警告")
    public static class ValidationWarning {

        /**
         * 警告类型
         */
        @Schema(description = "警告类型", example = "NAME_INCONSISTENCY")
        private String warningType;

        /**
         * 警告代码
         */
        @Schema(description = "警告代码", example = "WARN_001")
        private String warningCode;

        /**
         * 警告消息
         */
        @Schema(description = "警告消息", example = "产品名称与文档名称不一致")
        private String message;

        /**
         * 涉及的要件类型
         */
        @Schema(description = "涉及的要件类型", example = "TERMS")
        private String documentType;

        /**
         * 涉及的文档ID
         */
        @Schema(description = "涉及的文档ID", example = "doc_001")
        private String documentId;

        /**
         * 建议操作
         */
        @Schema(description = "建议操作", example = "请检查并统一产品名称")
        private String recommendation;
    }

    /**
     * 校验摘要
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "校验摘要")
    public static class ValidationSummary {

        /**
         * 总文档数
         */
        @Schema(description = "总文档数", example = "4")
        private Integer totalDocuments;

        /**
         * 必需文档数
         */
        @Schema(description = "必需文档数", example = "4")
        private Integer requiredDocuments;

        /**
         * 已上传文档数
         */
        @Schema(description = "已上传文档数", example = "3")
        private Integer uploadedDocuments;

        /**
         * 校验通过的文档数
         */
        @Schema(description = "校验通过的文档数", example = "2")
        private Integer validDocuments;

        /**
         * 错误总数
         */
        @Schema(description = "错误总数", example = "2")
        private Integer totalErrors;

        /**
         * 警告总数
         */
        @Schema(description = "警告总数", example = "1")
        private Integer totalWarnings;

        /**
         * 完整性百分比
         */
        @Schema(description = "完整性百分比", example = "75.0")
        private Double completenessPercentage;
    }
}