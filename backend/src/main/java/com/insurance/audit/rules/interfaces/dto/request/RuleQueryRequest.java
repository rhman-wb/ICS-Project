package com.insurance.audit.rules.interfaces.dto.request;

import com.insurance.audit.common.dto.PageRequest;
import com.insurance.audit.rules.enums.RuleAuditStatus;
import com.insurance.audit.rules.enums.RuleEffectiveStatus;
import com.insurance.audit.rules.enums.RuleSource;
import com.insurance.audit.rules.enums.RuleType;
import com.insurance.audit.rules.enums.RuleSortField;
import com.insurance.audit.rules.enums.SortDirection;
import com.insurance.audit.rules.validation.EnumValid;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 规则查询请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "规则查询请求")
public class RuleQueryRequest extends PageRequest {

    @EnumValid(enumClass = RuleSource.class, message = "规则来源必须是: SYSTEM, CUSTOM, IMPORTED, TEMPLATE")
    @Schema(description = "规则来源", example = "SYSTEM", allowableValues = {"SYSTEM", "CUSTOM", "IMPORTED", "TEMPLATE"})
    private RuleSource ruleSource;

    @Schema(description = "适用险种", example = "人寿保险")
    private String applicableInsurance;

    @Schema(description = "规则管理部门", example = "技术部门")
    private String manageDepartment;

    @EnumValid(enumClass = RuleAuditStatus.class, message = "审核状态必须是: PENDING_TECH_EVALUATION, PENDING_OA_SUBMISSION, SUBMITTED_TO_OA, OA_APPROVED, OA_REJECTED, TECH_APPROVED, TECH_REJECTED")
    @Schema(description = "审核状态", example = "PENDING_TECH_EVALUATION",
            allowableValues = {"PENDING_TECH_EVALUATION", "PENDING_OA_SUBMISSION", "SUBMITTED_TO_OA", "OA_APPROVED", "OA_REJECTED", "TECH_APPROVED", "TECH_REJECTED"})
    private RuleAuditStatus auditStatus;

    @EnumValid(enumClass = RuleEffectiveStatus.class, message = "有效状态必须是: EFFECTIVE, INACTIVE, PENDING_DEPLOYMENT, OFFLINE, TESTING")
    @Schema(description = "有效状态", example = "EFFECTIVE",
            allowableValues = {"EFFECTIVE", "INACTIVE", "PENDING_DEPLOYMENT", "OFFLINE", "TESTING"})
    private RuleEffectiveStatus effectiveStatus;

    @Schema(description = "适用章节", example = "第一章")
    private String applicableChapter;

    @Schema(description = "适用经营区域", example = "全国")
    private String businessArea;

    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]{1,100}$", message = "关键词只能包含中文、英文、数字和空格，长度不超过100字符")
    @Schema(description = "关键词", example = "保险金额")
    private String keyword;

    @Schema(description = "起始时间")
    private LocalDateTime startTime;

    @Schema(description = "终止时间")
    private LocalDateTime endTime;

    @EnumValid(enumClass = RuleType.class, message = "规则类型必须是: SINGLE, DOUBLE, FORMAT, ADVANCED")
    @Schema(description = "规则类型", example = "SINGLE", allowableValues = {"SINGLE", "DOUBLE", "FORMAT", "ADVANCED"})
    private RuleType ruleType;

    @Schema(description = "是否关注")
    private Boolean followed;

    @EnumValid(enumClass = RuleSortField.class, message = "排序字段必须是: ruleNumber, ruleName, createdAt, lastUpdatedAt, submittedAt, auditedAt, effectiveTime, expiryTime")
    @Schema(description = "排序字段", example = "lastUpdatedAt",
            allowableValues = {"ruleNumber", "ruleName", "createdAt", "lastUpdatedAt", "submittedAt", "auditedAt", "effectiveTime", "expiryTime"})
    private RuleSortField sortField;

    @EnumValid(enumClass = SortDirection.class, message = "排序方向必须是: ASC, DESC")
    @Schema(description = "排序方向", example = "DESC", allowableValues = {"ASC", "DESC"})
    private SortDirection sortDirection;
}