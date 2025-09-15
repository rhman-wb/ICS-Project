package com.insurance.audit.rules.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.insurance.audit.common.base.BaseEntity;
import com.insurance.audit.rules.enums.RuleAuditStatus;
import com.insurance.audit.rules.enums.RuleEffectiveStatus;
import com.insurance.audit.rules.enums.RuleSource;
import com.insurance.audit.rules.enums.RuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 规则实体
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
@TableName("rule")
@Schema(description = "规则实体")
public class Rule extends BaseEntity {

    /**
     * 规则编号
     */
    @TableField("rule_number")
    @Schema(description = "规则编号", example = "R001")
    private String ruleNumber;

    /**
     * 规则名称
     */
    @TableField("rule_name")
    @Schema(description = "规则名称", example = "保险金额限制规则")
    private String ruleName;

    /**
     * 规则描述
     */
    @TableField("rule_description")
    @Schema(description = "规则描述", example = "保险金额不能超过投保人年收入的10倍")
    private String ruleDescription;

    /**
     * 规则类型
     */
    @TableField("rule_type")
    @Schema(description = "规则类型", example = "SINGLE")
    private RuleType ruleType;

    /**
     * 规则来源
     */
    @TableField("rule_source")
    @Schema(description = "规则来源", example = "SYSTEM")
    private RuleSource ruleSource;

    /**
     * 规则管理部门
     */
    @TableField("manage_department")
    @Schema(description = "规则管理部门", example = "技术部门")
    private String manageDepartment;

    /**
     * 适用险种
     */
    @TableField("applicable_insurance")
    @Schema(description = "适用险种", example = "人寿保险")
    private String applicableInsurance;

    /**
     * 适用要件
     */
    @TableField("applicable_requirements")
    @Schema(description = "适用要件", example = "投保条件")
    private String applicableRequirements;

    /**
     * 适用章节
     */
    @TableField("applicable_chapter")
    @Schema(description = "适用章节", example = "第一章")
    private String applicableChapter;

    /**
     * 适用经营区域
     */
    @TableField("business_area")
    @Schema(description = "适用经营区域", example = "全国")
    private String businessArea;

    /**
     * 审核状态
     */
    @TableField("audit_status")
    @Schema(description = "审核状态", example = "PENDING_TECH_EVALUATION")
    private RuleAuditStatus auditStatus;

    /**
     * 有效状态
     */
    @TableField("effective_status")
    @Schema(description = "有效状态", example = "EFFECTIVE")
    private RuleEffectiveStatus effectiveStatus;

    /**
     * 规则内容JSON
     */
    @TableField("rule_content")
    @Schema(description = "规则内容JSON", example = "{\"conditions\": [], \"actions\": []}")
    private String ruleContent;

    /**
     * 规则配置JSON
     */
    @TableField("rule_config")
    @Schema(description = "规则配置JSON", example = "{\"priority\": 1, \"enabled\": true}")
    private String ruleConfig;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "生效时间", example = "2024-01-01 00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    @TableField("expiry_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "失效时间", example = "2024-12-31 23:59:59")
    private LocalDateTime expiryTime;

    /**
     * 最后更新时间 (索引字段)
     */
    @TableField("last_updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后更新时间", example = "2024-09-16 09:12:21")
    private LocalDateTime lastUpdatedAt;

    /**
     * 提交人ID
     */
    @TableField("submitted_by")
    @Schema(description = "提交人ID")
    private String submittedBy;

    /**
     * 提交时间
     */
    @TableField("submitted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "提交时间", example = "2024-09-16 09:12:21")
    private LocalDateTime submittedAt;

    /**
     * 审核人ID
     */
    @TableField("audited_by")
    @Schema(description = "审核人ID")
    private String auditedBy;

    /**
     * 审核时间
     */
    @TableField("audited_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审核时间", example = "2024-09-16 09:12:21")
    private LocalDateTime auditedAt;

    /**
     * 审核意见
     */
    @TableField("audit_comments")
    @Schema(description = "审核意见", example = "规则设计合理，建议通过")
    private String auditComments;

    /**
     * 是否关注
     */
    @TableField("is_followed")
    @Schema(description = "是否关注", example = "false")
    private Boolean followed;

    /**
     * 优先级
     */
    @TableField("priority")
    @Schema(description = "优先级", example = "1")
    private Integer priority;

    /**
     * 排序号
     */
    @TableField("sort_order")
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 标签（JSON数组）
     */
    @TableField("tags")
    @Schema(description = "标签", example = "[\"重要\", \"紧急\"]")
    private String tags;

    /**
     * 备注
     */
    @TableField("remarks")
    @Schema(description = "备注", example = "特殊业务场景使用")
    private String remarks;
}