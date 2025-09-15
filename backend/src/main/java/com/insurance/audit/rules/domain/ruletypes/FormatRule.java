package com.insurance.audit.rules.domain.ruletypes;

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
 * 格式规则实体
 * 基于格式验证的规则检查
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("rule_format")
@Schema(description = "格式规则实体")
public class FormatRule extends BaseEntity {

    /**
     * 关联规则ID
     */
    @TableField("rule_id")
    @Schema(description = "关联规则ID")
    private String ruleId;

    /**
     * 规则描述 (必填)
     */
    @TableField("rule_description")
    @Schema(description = "规则描述", example = "请输入规则描述", required = true)
    private String ruleDescription;

    /**
     * 匹配位置 (必填)
     */
    @TableField("match_position")
    @Schema(description = "匹配位置", example = "请填写匹配位置（如段落，序号，标题）", required = true)
    private String matchPosition;

    /**
     * 匹配内容 (必填)
     */
    @TableField("match_content")
    @Schema(description = "匹配内容", example = "具体文本匹配内容", required = true)
    private String matchContent;

    /**
     * 段落格式要求 (必填)
     */
    @TableField("paragraph_format_requirements")
    @Schema(description = "段落格式要求", example = "段落格式要求（支持识别缩进，居中，对齐等）", required = true)
    private String paragraphFormatRequirements;

    /**
     * 字体格式要求 (必填)
     */
    @TableField("font_format_requirements")
    @Schema(description = "字体格式要求", example = "字体格式要求（支持识别字号字体加粗等）", required = true)
    private String fontFormatRequirements;

    /**
     * 行文格式要求 (必填)
     */
    @TableField("text_format_requirements")
    @Schema(description = "行文格式要求", example = "行文格式要求，及其他要求", required = true)
    private String textFormatRequirements;

    /**
     * 提示类型 (必填)
     */
    @TableField("alert_type")
    @Schema(description = "提示类型", example = "请选择提示类型", required = true)
    private String alertType;

    /**
     * 提示内容 (必填)
     */
    @TableField("alert_content")
    @Schema(description = "提示内容", required = true)
    private String alertContent;

    /**
     * 适用要件 (必填)
     */
    @TableField("applicable_requirements")
    @Schema(description = "适用要件", required = true)
    private String applicableRequirements;

    /**
     * 适用险种
     */
    @TableField("applicable_insurance")
    @Schema(description = "适用险种", example = "请选择险种类型")
    private String applicableInsurance;

    /**
     * 适用经营区域
     */
    @TableField("applicable_business_area")
    @Schema(description = "适用经营区域", example = "请选择经营区域")
    private String applicableBusinessArea;

    /**
     * 具体地方区域
     */
    @TableField("specific_local_area")
    @Schema(description = "具体地方区域", example = "请选择具体地方区域")
    private String specificLocalArea;

    /**
     * 产品性质
     */
    @TableField("product_nature")
    @Schema(description = "产品性质", example = "请填写产品性质")
    private String productNature;

    /**
     * 规则管理部门
     */
    @TableField("rule_management_department")
    @Schema(description = "规则管理部门", example = "请填写规则管理部门")
    private String ruleManagementDepartment;

    /**
     * 规则来源
     */
    @TableField("rule_source")
    @Schema(description = "规则来源", example = "请填写规则来源")
    private String ruleSource;
}