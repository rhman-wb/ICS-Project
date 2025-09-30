package com.insurance.audit.product.interfaces.dto.response;

import com.insurance.audit.product.interfaces.dto.TemplateFieldConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板配置响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板配置响应")
public class TemplateConfigResponse {

    /**
     * 模板基本信息
     */
    @Schema(description = "模板基本信息")
    private TemplateInfoResponse templateInfo;

    /**
     * 字段配置列表
     */
    @Schema(description = "字段配置列表")
    private List<TemplateFieldConfig> fields;

    /**
     * 验证规则列表
     */
    @Schema(description = "验证规则列表")
    private List<FieldValidationRuleResponse> validationRules;

    /**
     * 额外配置
     */
    @Schema(description = "额外配置")
    private Object additionalConfig;
}