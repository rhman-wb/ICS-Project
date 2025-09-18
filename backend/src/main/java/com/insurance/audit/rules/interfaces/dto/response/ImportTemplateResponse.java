package com.insurance.audit.rules.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 导入模板响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "导入模板响应")
public class ImportTemplateResponse {

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "模板版本")
    private String templateVersion;

    @Schema(description = "表头列表")
    private List<String> headers;

    @Schema(description = "必填字段列表")
    private List<String> requiredFields;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "字段定义")
    private List<FieldDefinition> fields;

    @Schema(description = "验证规则")
    private List<ValidationRule> validationRules;

    @Schema(description = "示例数据")
    private List<Map<String, Object>> sampleData;

    @Schema(description = "使用说明")
    private String instructions;

    @Schema(description = "注意事项")
    private List<String> notes;

    @Schema(description = "支持的格式")
    private List<String> supportedFormats;

    @Data
    @Schema(description = "字段定义")
    public static class FieldDefinition {
        @Schema(description = "字段名")
        private String fieldName;

        @Schema(description = "显示名称")
        private String displayName;

        @Schema(description = "字段类型")
        private String fieldType;

        @Schema(description = "是否必填")
        private Boolean required;

        @Schema(description = "最大长度")
        private Integer maxLength;

        @Schema(description = "最小长度")
        private Integer minLength;

        @Schema(description = "数据格式")
        private String format;

        @Schema(description = "可选值")
        private List<String> allowedValues;

        @Schema(description = "默认值")
        private String defaultValue;

        @Schema(description = "示例值")
        private String exampleValue;

        @Schema(description = "字段说明")
        private String description;

        @Schema(description = "列索引")
        private Integer columnIndex;
    }

    @Data
    @Schema(description = "验证规则")
    public static class ValidationRule {
        @Schema(description = "规则名称")
        private String ruleName;

        @Schema(description = "规则类型")
        private String ruleType;

        @Schema(description = "规则表达式")
        private String ruleExpression;

        @Schema(description = "错误消息")
        private String errorMessage;

        @Schema(description = "严重级别")
        private String severity; // ERROR, WARNING, INFO

        @Schema(description = "适用字段")
        private List<String> applicableFields;
    }
}