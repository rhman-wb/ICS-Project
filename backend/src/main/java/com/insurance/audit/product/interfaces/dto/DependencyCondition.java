package com.insurance.audit.product.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字段依赖条件DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段依赖条件")
public class DependencyCondition {

    /**
     * 依赖的字段名称
     */
    @Schema(description = "依赖的字段名称", example = "development_type")
    private String fieldName;

    /**
     * 条件操作符
     */
    @Schema(description = "条件操作符", example = "equals", allowableValues = {"equals", "notEquals", "in", "notIn", "contains"})
    private String operator;

    /**
     * 条件值
     */
    @Schema(description = "条件值", example = "使用示范条款")
    private Object value;

    /**
     * 条件值列表(用于in/notIn操作符)
     */
    @Schema(description = "条件值列表")
    private List<String> values;
}