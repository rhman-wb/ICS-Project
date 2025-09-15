package com.insurance.audit.rules.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 导入验证请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "导入验证请求")
public class ImportValidationRequest {

    @Schema(description = "规则类型", example = "SINGLE")
    private String ruleType;

    @Schema(description = "是否验证重复", example = "true")
    private Boolean checkDuplicates = true;

    @Schema(description = "是否严格模式", example = "false")
    private Boolean strictMode = false;

    @Min(value = 1, message = "起始行必须大于0")
    @Schema(description = "起始行", example = "2")
    private Integer startRow = 2;

    @Min(value = 1, message = "最大行数必须大于0")
    @Max(value = 10000, message = "最大行数不能超过10000")
    @Schema(description = "最大行数", example = "5000")
    private Integer maxRows = 5000;

    @Schema(description = "编码格式", example = "UTF-8")
    private String encoding = "UTF-8";

    @Schema(description = "工作表名称", example = "规则数据")
    private String sheetName;

    @Schema(description = "是否允许空行", example = "false")
    private Boolean allowEmptyRows = false;

    @Schema(description = "验证级别", example = "STANDARD")
    private String validationLevel = "STANDARD"; // MINIMAL, STANDARD, STRICT
}