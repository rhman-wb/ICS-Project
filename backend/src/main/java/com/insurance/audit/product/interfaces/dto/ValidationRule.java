package com.insurance.audit.product.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段验证规则DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段验证规则")
public class ValidationRule {

    /**
     * 规则类型
     */
    @Schema(description = "规则类型", example = "required", allowableValues = {"required", "minLength", "maxLength", "pattern", "custom"})
    private String type;

    /**
     * 规则值
     */
    @Schema(description = "规则值", example = "64")
    private Object value;

    /**
     * 错误提示信息
     */
    @Schema(description = "错误提示信息", example = "产品名称不能超过64个字符")
    private String message;

    /**
     * 自定义验证器名称
     */
    @Schema(description = "自定义验证器名称", example = "productNameValidator")
    private String validator;
}