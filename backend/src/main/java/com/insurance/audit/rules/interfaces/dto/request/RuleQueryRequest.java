package com.insurance.audit.rules.interfaces.dto.request;

import com.insurance.audit.common.dto.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "规则来源", example = "SYSTEM")
    private String ruleSource;

    @Schema(description = "适用险种", example = "人寿保险")
    private String applicableInsurance;

    @Schema(description = "规则管理部门", example = "技术部门")
    private String manageDepartment;

    @Schema(description = "审核状态", example = "PENDING_TECH_EVALUATION")
    private String auditStatus;

    @Schema(description = "有效状态", example = "EFFECTIVE")
    private String effectiveStatus;

    @Schema(description = "适用章节", example = "第一章")
    private String applicableChapter;

    @Schema(description = "适用经营区域", example = "全国")
    private String businessArea;

    @Schema(description = "关键词", example = "保险金额")
    private String keyword;

    @Schema(description = "起始时间")
    private LocalDateTime startTime;

    @Schema(description = "终止时间")
    private LocalDateTime endTime;

    @Schema(description = "规则类型", example = "SINGLE")
    private String ruleType;

    @Schema(description = "是否关注")
    private Boolean followed;

    @Schema(description = "排序字段", example = "lastUpdatedAt")
    private String sortField;

    @Schema(description = "排序方向", example = "DESC")
    private String sortDirection;
}