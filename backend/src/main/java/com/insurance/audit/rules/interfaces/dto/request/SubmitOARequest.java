package com.insurance.audit.rules.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 提交OA审核请求DTO
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交OA审核请求")
public class SubmitOARequest {

    @NotEmpty(message = "规则ID列表不能为空")
    @Schema(description = "规则ID列表", required = true)
    private List<String> ruleIds;

    @NotBlank(message = "提交人不能为空")
    @Schema(description = "提交人ID", required = true)
    private String submittedBy;

    @Size(max = 500, message = "提交说明长度不能超过500字符")
    @Schema(description = "提交说明", example = "批量提交规则进行OA审核")
    private String submitComments;

    @Schema(description = "提交原因", example = "规则需要进行OA审核")
    private String submitReason;

    @Schema(description = "优先级", example = "NORMAL")
    private String priority = "NORMAL";

    @Schema(description = "期望审核完成时间")
    private String expectedReviewDate;

    @Schema(description = "审核类型", example = "BATCH")
    private String reviewType = "BATCH";

    @Schema(description = "业务分类", example = "RULE_MANAGEMENT")
    private String businessCategory = "RULE_MANAGEMENT";

    @Schema(description = "附加信息")
    private String additionalInfo;
}