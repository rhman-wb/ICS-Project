package com.insurance.audit.product.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 模板解析结果响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板解析结果响应")
public class TemplateParseResultResponse {

    /**
     * 解析是否成功
     */
    @Schema(description = "解析是否成功", example = "true")
    private Boolean success;

    /**
     * 解析消息
     */
    @Schema(description = "解析消息", example = "模板解析成功")
    private String message;

    /**
     * 解析后的字段数据（字段名 -> 字段值）
     */
    @Schema(description = "解析后的字段数据")
    private Map<String, Object> parsedData;

    /**
     * 解析的字段数量
     */
    @Schema(description = "解析的字段数量", example = "25")
    private Integer parsedFieldCount;

    /**
     * 错误列表
     */
    @Schema(description = "错误列表")
    private List<ParseError> errors;

    /**
     * 警告列表
     */
    @Schema(description = "警告列表")
    private List<ParseWarning> warnings;

    /**
     * 验证结果（如果启用了验证）
     */
    @Schema(description = "验证结果")
    private FieldValidationResultResponse validationResult;

    /**
     * 解析错误详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "解析错误详情")
    public static class ParseError {
        @Schema(description = "字段名")
        private String fieldName;

        @Schema(description = "错误消息")
        private String message;

        @Schema(description = "错误类型", example = "FORMAT_ERROR")
        private String errorType;

        @Schema(description = "错误位置（行号、列号等）")
        private String location;
    }

    /**
     * 解析警告详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "解析警告详情")
    public static class ParseWarning {
        @Schema(description = "字段名")
        private String fieldName;

        @Schema(description = "警告消息")
        private String message;

        @Schema(description = "警告类型", example = "MISSING_OPTIONAL_FIELD")
        private String warningType;
    }
}