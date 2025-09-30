package com.insurance.audit.product.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 字段验证结果响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段验证结果响应")
public class FieldValidationResultResponse {

    /**
     * 验证是否通过
     */
    @Schema(description = "验证是否通过", example = "true")
    private Boolean valid;

    /**
     * 验证消息
     */
    @Schema(description = "验证消息", example = "所有字段验证通过")
    private String message;

    /**
     * 验证的字段数量
     */
    @Schema(description = "验证的字段数量", example = "25")
    private Integer validatedFieldCount;

    /**
     * 通过验证的字段数量
     */
    @Schema(description = "通过验证的字段数量", example = "23")
    private Integer passedFieldCount;

    /**
     * 失败的字段数量
     */
    @Schema(description = "失败的字段数量", example = "2")
    private Integer failedFieldCount;

    /**
     * 字段验证详情列表
     */
    @Schema(description = "字段验证详情列表")
    private List<FieldValidationDetail> fieldValidations;

    /**
     * 依赖关系验证结果
     */
    @Schema(description = "依赖关系验证结果")
    private List<DependencyValidationResult> dependencyValidations;

    /**
     * 字段验证详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "字段验证详情")
    public static class FieldValidationDetail {
        @Schema(description = "字段名")
        private String fieldName;

        @Schema(description = "字段显示名称")
        private String fieldLabel;

        @Schema(description = "验证是否通过")
        private Boolean valid;

        @Schema(description = "字段值")
        private Object value;

        @Schema(description = "错误消息")
        private String errorMessage;

        @Schema(description = "验证规则类型")
        private String ruleType;
    }

    /**
     * 依赖关系验证结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "依赖关系验证结果")
    public static class DependencyValidationResult {
        @Schema(description = "主字段名")
        private String mainField;

        @Schema(description = "依赖字段名")
        private String dependentField;

        @Schema(description = "验证是否通过")
        private Boolean valid;

        @Schema(description = "错误消息")
        private String errorMessage;

        @Schema(description = "依赖条件")
        private String condition;
    }
}