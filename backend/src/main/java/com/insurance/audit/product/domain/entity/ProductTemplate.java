package com.insurance.audit.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.insurance.audit.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 产品模板实体
 * 用于管理产品模板配置和元数据
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-29
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("product_templates")
@Schema(description = "产品模板实体")
public class ProductTemplate extends BaseEntity {

    /**
     * 模板类型
     */
    @TableField("template_type")
    @Schema(description = "模板类型", example = "FILING", allowableValues = {"FILING", "AGRICULTURAL"})
    private String templateType;

    /**
     * 模板名称
     */
    @TableField("template_name")
    @Schema(description = "模板名称", example = "备案产品自主注册信息登记表", maxLength = 100)
    private String templateName;

    /**
     * 模板文件路径
     */
    @TableField("template_file_path")
    @Schema(description = "模板文件路径", example = "/templates/filing_product_template.xlsx", maxLength = 500)
    private String templateFilePath;

    /**
     * 字段配置JSON
     */
    @TableField("field_config")
    @Schema(description = "字段配置JSON", example = "{\"fields\":[{\"name\":\"productName\",\"type\":\"input\",\"required\":true}]}")
    private String fieldConfig;

    /**
     * 验证规则JSON
     */
    @TableField("validation_rules")
    @Schema(description = "验证规则JSON", example = "{\"rules\":[{\"field\":\"productName\",\"rules\":[{\"type\":\"required\",\"message\":\"产品名称不能为空\"}]}]}")
    private String validationRules;

    /**
     * 是否启用
     */
    @TableField("enabled")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 模板版本
     */
    @TableField("template_version")
    @Schema(description = "模板版本", example = "1.0.0", maxLength = 20)
    private String templateVersion;

    /**
     * 模板描述
     */
    @TableField("description")
    @Schema(description = "模板描述", example = "用于备案产品信息登记的标准模板", maxLength = 500)
    private String description;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    @Schema(description = "文件大小（字节）", example = "2048576")
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    @TableField("mime_type")
    @Schema(description = "文件MIME类型", example = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", maxLength = 100)
    private String mimeType;

    /**
     * 排序序号
     */
    @TableField("sort_order")
    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        /**
         * 备案产品模板
         */
        FILING("备案产品模板"),
        /**
         * 农险产品模板
         */
        AGRICULTURAL("农险产品模板");

        private final String description;

        TemplateType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 获取模板类型枚举
     */
    public TemplateType getTemplateTypeEnum() {
        if (templateType == null) {
            return null;
        }
        try {
            return TemplateType.valueOf(templateType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 设置模板类型枚举
     */
    public void setTemplateTypeEnum(TemplateType templateTypeEnum) {
        this.templateType = templateTypeEnum != null ? templateTypeEnum.name() : null;
    }
}