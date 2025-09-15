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
 * 高级规则实体
 * 基于复杂逻辑的高级规则检查
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
@TableName("rule_advanced")
@Schema(description = "高级规则实体")
public class AdvancedRule extends BaseEntity {

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

    /**
     * 高级规则配置JSON
     * 用于存储复杂的规则逻辑配置，后续可能集成LLM服务
     */
    @TableField("advanced_rule_config")
    @Schema(description = "高级规则配置JSON", example = "{\"complexity\": \"high\", \"llm_enabled\": false}")
    private String advancedRuleConfig;

    /**
     * 执行脚本或表达式
     * 用于存储高级规则的执行逻辑
     */
    @TableField("execution_script")
    @Schema(description = "执行脚本或表达式")
    private String executionScript;

    /**
     * 规则权重
     * 用于复杂规则场景下的优先级控制
     */
    @TableField("rule_weight")
    @Schema(description = "规则权重", example = "100")
    private Integer ruleWeight;

    /**
     * 是否启用LLM
     * 预留字段，用于后续LLM服务集成
     */
    @TableField("llm_enabled")
    @Schema(description = "是否启用LLM", example = "false")
    private Boolean llmEnabled;

    /**
     * LLM模型配置
     * 预留字段，用于LLM服务配置
     */
    @TableField("llm_model_config")
    @Schema(description = "LLM模型配置JSON")
    private String llmModelConfig;
}