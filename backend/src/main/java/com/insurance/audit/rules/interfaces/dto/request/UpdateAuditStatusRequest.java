package com.insurance.audit.rules.interfaces.dto.request;

import com.insurance.audit.rules.enums.RuleAuditStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新审核状态请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "更新审核状态请求")
public class UpdateAuditStatusRequest {

    @Schema(description = "规则ID（批量操作时使用）")
    private String ruleId;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "目标审核状态", example = "OA_APPROVED", required = true)
    private RuleAuditStatus targetStatus;

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态", example = "OA_APPROVED", required = true)
    private RuleAuditStatus auditStatus;

    @Size(max = 500, message = "审核意见长度不能超过500字符")
    @Schema(description = "审核意见", example = "规则设计合理，建议通过")
    private String auditComments;

    @Schema(description = "备注", example = "审核通过")
    private String remark;

    @NotBlank(message = "审核人不能为空")
    @Schema(description = "审核人ID", required = true)
    private String auditedBy;

    @Schema(description = "强制流转", example = "false")
    private Boolean forceTransition = false;

    @Schema(description = "流转原因")
    private String transitionReason;
}