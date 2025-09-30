package com.insurance.audit.product.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

/**
 * 字段验证请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段验证请求")
public class FieldValidationRequest {

    /**
     * 模板类型
     */
    @NotBlank(message = "模板类型不能为空")
    @Pattern(regexp = "^(FILING|AGRICULTURAL)$", message = "模板类型必须是FILING或AGRICULTURAL")
    @Schema(description = "模板类型", example = "AGRICULTURAL",
            allowableValues = {"FILING", "AGRICULTURAL"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateType;

    /**
     * 要验证的字段数据（字段名 -> 字段值）
     */
    @NotNull(message = "字段数据不能为空")
    @Schema(description = "要验证的字段数据", example = "{\"productName\":\"测试产品\",\"reportType\":\"新产品\"}",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> fieldData;

    /**
     * 是否验证所有字段
     */
    @Schema(description = "是否验证所有字段，false时只验证提供的字段", example = "false", defaultValue = "false")
    private Boolean validateAll = false;

    /**
     * 是否检查字段依赖关系
     */
    @Schema(description = "是否检查字段依赖关系", example = "true", defaultValue = "true")
    private Boolean checkDependencies = true;
}