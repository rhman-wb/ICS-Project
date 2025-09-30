package com.insurance.audit.product.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板信息响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板信息响应")
public class TemplateInfoResponse {

    /**
     * 模板ID
     */
    @Schema(description = "模板ID", example = "1")
    private String id;

    /**
     * 模板类型
     */
    @Schema(description = "模板类型", example = "AGRICULTURAL", allowableValues = {"FILING", "AGRICULTURAL"})
    private String templateType;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称", example = "农险产品信息登记表")
    private String templateName;

    /**
     * 模板描述
     */
    @Schema(description = "模板描述")
    private String description;

    /**
     * 文件路径
     */
    @Schema(description = "文件路径", example = "/templates/agricultural_template.xls")
    private String filePath;

    /**
     * 文件名
     */
    @Schema(description = "文件名", example = "农险产品信息登记表.xls")
    private String fileName;

    /**
     * 模板版本
     */
    @Schema(description = "模板版本", example = "1.0")
    private String version;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 字段数量
     */
    @Schema(description = "字段数量", example = "30")
    private Integer fieldCount;

    /**
     * 验证规则数量
     */
    @Schema(description = "验证规则数量", example = "15")
    private Integer validationRuleCount;
}