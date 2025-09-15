package com.insurance.audit.rules.interfaces.dto.request;

import com.insurance.audit.rules.enums.RuleEffectiveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 更新有效状态请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Schema(description = "更新有效状态请求")
public class UpdateEffectiveStatusRequest {

    @Schema(description = "规则ID（批量操作时使用）")
    private String ruleId;

    @NotNull(message = "有效状态不能为空")
    @Schema(description = "目标有效状态", example = "EFFECTIVE", required = true)
    private RuleEffectiveStatus targetStatus;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间")
    private LocalDateTime expiryTime;

    @Size(max = 500, message = "变更原因长度不能超过500字符")
    @Schema(description = "变更原因", example = "规则需要立即生效")
    private String changeReason;

    @NotBlank(message = "操作人不能为空")
    @Schema(description = "操作人ID", required = true)
    private String operatorId;

    @Schema(description = "强制流转", example = "false")
    private Boolean forceTransition = false;

    @Schema(description = "流转原因")
    private String transitionReason;
}