package com.insurance.audit.rules.interfaces.web;

import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.rules.application.service.RuleStatusService;
import com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest;
import com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest;
import com.insurance.audit.rules.interfaces.dto.response.RuleResponse;
import com.insurance.audit.rules.interfaces.dto.response.BatchOperationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 规则状态管理控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Slf4j
@RestController
@RequestMapping("/api/rules/status")
@RequiredArgsConstructor
@Validated
@Tag(name = "规则状态管理", description = "规则状态流转相关API")
public class RuleStatusController {

    private final RuleStatusService ruleStatusService;

    @PatchMapping("/{id}/audit-status")
    @Operation(summary = "更新规则审核状态", description = "更新单个规则的审核状态")
    @PreAuthorize("hasAuthority('RULE_AUDIT')")
    public ApiResponse<RuleResponse> updateAuditStatus(
            @Parameter(description = "规则ID") @PathVariable String id,
            @Parameter(description = "更新审核状态请求") @Valid @RequestBody UpdateAuditStatusRequest request) {

        log.info("更新规则审核状态，ID: {}, 请求: {}", id, request);

        RuleResponse response = ruleStatusService.updateAuditStatus(id, request);

        return ApiResponse.success(response);
    }

    @PatchMapping("/batch/audit-status")
    @Operation(summary = "批量更新规则审核状态", description = "批量更新多个规则的审核状态")
    @PreAuthorize("hasAuthority('RULE_AUDIT')")
    public ApiResponse<BatchOperationResponse> batchUpdateAuditStatus(
            @Parameter(description = "批量更新审核状态请求") @Valid @RequestBody List<UpdateAuditStatusRequest> requests) {

        log.info("批量更新规则审核状态，请求数量: {}", requests.size());

        BatchOperationResponse response = ruleStatusService.batchUpdateAuditStatus(requests);

        return ApiResponse.success(response);
    }

    @PatchMapping("/{id}/effective-status")
    @Operation(summary = "更新规则有效状态", description = "更新单个规则的有效状态")
    @PreAuthorize("hasAuthority('RULE_ADMIN')")
    public ApiResponse<RuleResponse> updateEffectiveStatus(
            @Parameter(description = "规则ID") @PathVariable String id,
            @Parameter(description = "更新有效状态请求") @Valid @RequestBody UpdateEffectiveStatusRequest request) {

        log.info("更新规则有效状态，ID: {}, 请求: {}", id, request);

        RuleResponse response = ruleStatusService.updateEffectiveStatus(id, request);

        return ApiResponse.success(response);
    }

    @PatchMapping("/batch/effective-status")
    @Operation(summary = "批量更新规则有效状态", description = "批量更新多个规则的有效状态")
    @PreAuthorize("hasAuthority('RULE_ADMIN')")
    public ApiResponse<BatchOperationResponse> batchUpdateEffectiveStatus(
            @Parameter(description = "批量更新有效状态请求") @Valid @RequestBody List<UpdateEffectiveStatusRequest> requests) {

        log.info("批量更新规则有效状态，请求数量: {}", requests.size());

        BatchOperationResponse response = ruleStatusService.batchUpdateEffectiveStatus(requests);

        return ApiResponse.success(response);
    }

    @PostMapping("/submit-oa")
    @Operation(summary = "提交OA审核", description = "将规则提交到OA系统进行审核")
    @PreAuthorize("hasAuthority('RULE_SUBMIT_OA')")
    public ApiResponse<BatchOperationResponse> submitToOA(
            @Parameter(description = "提交OA请求") @Valid @RequestBody SubmitOARequest request) {

        log.info("提交规则到OA审核，请求: {}", request);

        BatchOperationResponse response = ruleStatusService.submitToOA(request);

        return ApiResponse.success(response);
    }

    @GetMapping("/flow/audit")
    @Operation(summary = "获取审核状态流转图", description = "获取审核状态的可用流转路径")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<List<String>> getAuditStatusFlow(
            @Parameter(description = "当前审核状态") @RequestParam String currentStatus) {

        log.info("获取审核状态流转图，当前状态: {}", currentStatus);

        List<String> availableTransitions = ruleStatusService.getAuditStatusTransitions(currentStatus);

        return ApiResponse.success(availableTransitions);
    }

    @GetMapping("/flow/effective")
    @Operation(summary = "获取有效状态流转图", description = "获取有效状态的可用流转路径")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<List<String>> getEffectiveStatusFlow(
            @Parameter(description = "当前有效状态") @RequestParam String currentStatus) {

        log.info("获取有效状态流转图，当前状态: {}", currentStatus);

        List<String> availableTransitions = ruleStatusService.getEffectiveStatusTransitions(currentStatus);

        return ApiResponse.success(availableTransitions);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "获取规则状态变更历史", description = "获取规则的状态变更历史记录")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<List<Object>> getRuleStatusHistory(
            @Parameter(description = "规则ID") @PathVariable String id) {

        log.info("获取规则状态变更历史，ID: {}", id);

        List<Object> statusHistory = ruleStatusService.getRuleStatusHistory(id);

        return ApiResponse.success(statusHistory);
    }

    @PostMapping("/{id}/rollback")
    @Operation(summary = "回滚规则状态", description = "将规则状态回滚到上一个状态")
    @PreAuthorize("hasAuthority('RULE_ADMIN')")
    public ApiResponse<RuleResponse> rollbackRuleStatus(
            @Parameter(description = "规则ID") @PathVariable String id,
            @Parameter(description = "回滚原因") @RequestParam String reason) {

        log.info("回滚规则状态，ID: {}, 原因: {}", id, reason);

        RuleResponse response = ruleStatusService.rollbackRuleStatus(id, reason);

        return ApiResponse.success(response);
    }
}