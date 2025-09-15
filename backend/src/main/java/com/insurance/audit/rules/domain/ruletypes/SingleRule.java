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
 * 单句规则实体
 * 基于单个条件的简单规则检查
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
@TableName("rule_single")
@Schema(description = "单句规则实体")
public class SingleRule extends BaseEntity {

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
     * 匹配章节 (必填)
     */
    @TableField("match_chapter")
    @Schema(description = "匹配章节", example = "第一章", required = true)
    private String matchChapter;

    /**
     * 匹配类型 (必填)
     */
    @TableField("match_type")
    @Schema(description = "匹配类型", example = "关键词匹配", required = true)
    private String matchType;

    /**
     * 匹配内容 (必填)
     */
    @TableField("match_content")
    @Schema(description = "匹配内容", example = "请填写匹配内容", required = true)
    private String matchContent;

    /**
     * 触发条件 (必填)
     */
    @TableField("trigger_condition")
    @Schema(description = "触发条件", example = "包含关键词", required = true)
    private String triggerCondition;

    /**
     * 提示类型 (必填)
     */
    @TableField("alert_type")
    @Schema(description = "提示类型", example = "错误", required = true)
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