package com.insurance.audit.rules.interfaces.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.insurance.audit.rules.enums.RuleAuditStatus;
import com.insurance.audit.rules.enums.RuleEffectiveStatus;
import com.insurance.audit.rules.enums.RuleType;
import com.insurance.audit.rules.enums.RuleSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则响应DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "规则响应")
public class RuleResponse {

    @Schema(description = "规则ID", example = "123e4567e89b12d3a456426614174000")
    private String id;

    @Schema(description = "规则编号", example = "R001")
    private String ruleNumber;

    @Schema(description = "规则名称", example = "保险金额限制规则")
    private String ruleName;

    @Schema(description = "规则描述", example = "保险金额不能超过投保人年收入的10倍")
    private String ruleDescription;

    @Schema(description = "规则类型", example = "SINGLE")
    private RuleType ruleType;

    @Schema(description = "规则来源", example = "SYSTEM")
    private RuleSource ruleSource;

    @Schema(description = "规则管理部门", example = "技术部门")
    private String manageDepartment;

    @Schema(description = "适用险种", example = "人寿保险")
    private String applicableInsurance;

    @Schema(description = "适用要件", example = "投保条件")
    private String applicableRequirements;

    @Schema(description = "适用章节", example = "第一章")
    private String applicableChapter;

    @Schema(description = "适用经营区域", example = "全国")
    private String businessArea;

    @Schema(description = "审核状态", example = "PENDING_TECH_EVALUATION")
    private RuleAuditStatus auditStatus;

    @Schema(description = "有效状态", example = "EFFECTIVE")
    private RuleEffectiveStatus effectiveStatus;

    @Schema(description = "规则内容JSON")
    private String ruleContent;

    @Schema(description = "规则配置JSON")
    private String ruleConfig;

    @Schema(description = "生效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expiryTime;

    @Schema(description = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastUpdatedAt;

    @Schema(description = "提交人ID")
    private String submittedBy;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime submittedAt;

    @Schema(description = "审核人ID")
    private String auditedBy;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditedAt;

    @Schema(description = "审核意见")
    private String auditComments;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "生效备注")
    private String effectiveRemark;

    @Schema(description = "是否关注")
    private Boolean followed;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "创建人ID")
    private String createdBy;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @Schema(description = "更新人ID")
    private String updatedBy;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updatedAt;

    @Schema(description = "版本号")
    private Integer version;

    // 子规则数据（根据规则类型不同而不同）
    @Schema(description = "子规则数据")
    private Object subRuleData;
}