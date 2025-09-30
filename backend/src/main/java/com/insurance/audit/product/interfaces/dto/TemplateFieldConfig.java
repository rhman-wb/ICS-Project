package com.insurance.audit.product.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板字段配置DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板字段配置")
public class TemplateFieldConfig {

    /**
     * 字段名称
     */
    @Schema(description = "字段名称", example = "product_name")
    private String fieldName;

    /**
     * 字段标签
     */
    @Schema(description = "字段标签", example = "产品名称")
    private String fieldLabel;

    /**
     * 字段类型
     */
    @Schema(description = "字段类型", example = "input", allowableValues = {"input", "select", "textarea", "number", "date"})
    private String fieldType;

    /**
     * 是否必填
     */
    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    /**
     * 最大长度
     */
    @Schema(description = "最大长度", example = "200")
    private Integer maxLength;

    /**
     * 下拉选项列表
     */
    @Schema(description = "下拉选项列表")
    private List<String> options;

    /**
     * 占位符
     */
    @Schema(description = "占位符", example = "请输入产品名称")
    private String placeholder;

    /**
     * 验证规则列表
     */
    @Schema(description = "验证规则列表")
    private List<ValidationRule> validationRules;

    /**
     * 依赖字段名称
     */
    @Schema(description = "依赖字段名称", example = "development_type")
    private String dependsOn;

    /**
     * 显示条件
     */
    @Schema(description = "显示条件")
    private DependencyCondition showWhen;

    /**
     * 字段排序顺序
     */
    @Schema(description = "字段排序顺序", example = "1")
    private Integer sortOrder;

    /**
     * 字段分组
     */
    @Schema(description = "字段分组", example = "基本信息")
    private String group;
}