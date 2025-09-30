package com.insurance.audit.product.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段验证规则响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段验证规则响应")
public class FieldValidationRuleResponse {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1")
    private String id;

    /**
     * 字段名
     */
    @Schema(description = "字段名", example = "productName")
    private String fieldName;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "REQUIRED",
            allowableValues = {"REQUIRED", "LENGTH", "PATTERN", "RANGE", "CUSTOM"})
    private String ruleType;

    /**
     * 规则参数
     */
    @Schema(description = "规则参数", example = "{\"min\":1,\"max\":200}")
    private Object parameters;

    /**
     * 错误消息
     */
    @Schema(description = "错误消息", example = "产品名称不能为空")
    private String errorMessage;

    /**
     * 是否必填
     */
    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    /**
     * 依赖字段
     */
    @Schema(description = "依赖字段", example = "reportType")
    private String dependsOn;

    /**
     * 依赖条件
     */
    @Schema(description = "依赖条件", example = "reportType == '修订产品'")
    private String condition;

    /**
     * 规则优先级
     */
    @Schema(description = "规则优先级", example = "1")
    private Integer priority;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}