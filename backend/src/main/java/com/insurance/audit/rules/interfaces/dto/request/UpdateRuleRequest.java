package com.insurance.audit.rules.interfaces.dto.request;

import com.insurance.audit.rules.enums.RuleAuditStatus;
import com.insurance.audit.rules.enums.RuleEffectiveStatus;
import com.insurance.audit.rules.enums.RuleType;
import com.insurance.audit.rules.enums.RuleSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 更新规则请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新规则请求")
public class UpdateRuleRequest {

    @Size(max = 200, message = "规则名称长度不能超过200字符")
    @Schema(description = "规则名称", example = "保险金额限制规则")
    private String ruleName;

    @Size(max = 300, message = "规则描述长度不能超过300字符")
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

    @Schema(description = "规则内容JSON", example = "{\"conditions\": [], \"actions\": []}")
    private String ruleContent;

    @Schema(description = "规则配置JSON", example = "{\"priority\": 1, \"enabled\": true}")
    private String ruleConfig;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间")
    private LocalDateTime expiryTime;

    @Schema(description = "审核意见", example = "规则设计合理，建议通过")
    private String auditComments;

    @Schema(description = "是否关注", example = "false")
    private Boolean followed;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "标签", example = "[\"重要\", \"紧急\"]")
    private String tags;

    @Schema(description = "备注", example = "特殊业务场景使用")
    private String remarks;

    // 子规则类型特定字段（根据ruleType不同而不同）
    @Schema(description = "子规则数据（根据规则类型动态变化）")
    private Object subRuleData;
}