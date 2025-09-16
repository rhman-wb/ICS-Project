package com.insurance.audit.rules.interfaces.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.common.dto.PageResponse;
import com.insurance.audit.rules.application.converter.RuleConverter;
import com.insurance.audit.rules.application.service.RuleService;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.request.RuleQueryRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.response.BatchOperationResponse;
import com.insurance.audit.rules.interfaces.dto.response.RuleResponse;
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
import java.util.Map;

/**
 * 规则管理控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Slf4j
@RestController
@RequestMapping("/v1/rules")
@RequiredArgsConstructor
@Validated
@Tag(name = "规则管理", description = "规则管理相关API")
public class RuleController {

    private final RuleService ruleService;
    private final RuleConverter ruleConverter;

    @GetMapping
    @Operation(summary = "分页查询规则列表", description = "根据条件分页查询规则列表")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<PageResponse<RuleResponse>> getRules(
            @Parameter(description = "查询条件") @Valid RuleQueryRequest queryRequest) {

        log.info("查询规则列表，查询条件: {}", queryRequest);

        IPage<Rule> rulePage = ruleService.getRulePage(queryRequest);

        // 转换为响应DTO
        PageResponse<RuleResponse> response = PageResponse.of(
            rulePage.getRecords().stream()
                .map(ruleConverter::convertToResponse)
                .toList(),
            rulePage.getCurrent(),
            rulePage.getSize(),
            rulePage.getTotal()
        );

        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取规则详情", description = "根据ID获取规则详情")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<RuleResponse> getRuleById(
            @Parameter(description = "规则ID") @PathVariable String id) {

        log.info("获取规则详情，ID: {}", id);

        Rule rule = ruleService.getRuleById(id);
        RuleResponse response = ruleConverter.convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(summary = "创建规则", description = "创建新的规则")
    @PreAuthorize("hasAuthority('RULE_CREATE')")
    public ApiResponse<RuleResponse> createRule(
            @Parameter(description = "创建规则请求") @Valid @RequestBody CreateRuleRequest request) {

        log.info("创建规则，请求: {}", request);

        Rule rule = ruleService.createRule(request);
        RuleResponse response = ruleConverter.convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新规则", description = "根据ID更新规则")
    @PreAuthorize("hasAuthority('RULE_EDIT')")
    public ApiResponse<RuleResponse> updateRule(
            @Parameter(description = "规则ID") @PathVariable String id,
            @Parameter(description = "更新规则请求") @Valid @RequestBody UpdateRuleRequest request) {

        log.info("更新规则，ID: {}, 请求: {}", id, request);

        Rule rule = ruleService.updateRule(id, request);
        RuleResponse response = ruleConverter.convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除规则", description = "根据ID删除规则")
    @PreAuthorize("hasAuthority('RULE_DELETE')")
    public ApiResponse<Void> deleteRule(
            @Parameter(description = "规则ID") @PathVariable String id) {

        log.info("删除规则，ID: {}", id);

        boolean success = ruleService.deleteRule(id);

        if (success) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error("删除规则失败");
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除规则", description = "批量删除多个规则")
    @PreAuthorize("hasAuthority('RULE_DELETE')")
    public ApiResponse<BatchOperationResponse> batchDeleteRules(
            @Parameter(description = "规则ID列表") @RequestBody List<String> ids) {

        log.info("批量删除规则，IDs: {}", ids);

        int deletedCount = ruleService.batchDeleteRules(ids);

        BatchOperationResponse response = new BatchOperationResponse();
        response.setSuccessCount(deletedCount);
        response.setFailedCount(ids.size() - deletedCount);
        response.setTotalCount(ids.size());

        return ApiResponse.success(response);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有规则", description = "获取所有规则（无分页）")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<List<RuleResponse>> getAllRules() {

        log.info("获取所有规则");

        List<Rule> rules = ruleService.getAllRules();
        List<RuleResponse> response = rules.stream()
            .map(ruleConverter::convertToResponse)
            .toList();

        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索规则", description = "根据关键词搜索规则")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<List<RuleResponse>> searchRules(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {

        log.info("搜索规则，关键词: {}", keyword);

        List<Rule> rules = ruleService.searchRules(keyword);
        List<RuleResponse> response = rules.stream()
            .map(ruleConverter::convertToResponse)
            .toList();

        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/follow")
    @Operation(summary = "关注/取消关注规则", description = "切换规则的关注状态")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<RuleResponse> toggleRuleFollow(
            @Parameter(description = "规则ID") @PathVariable String id,
            @Parameter(description = "是否关注") @RequestParam boolean followed) {

        log.info("切换规则关注状态，ID: {}, 关注: {}", id, followed);

        Rule rule = ruleService.toggleRuleFollow(id, followed);
        RuleResponse response = ruleConverter.convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制规则", description = "复制现有规则创建新规则")
    @PreAuthorize("hasAuthority('RULE_CREATE')")
    public ApiResponse<RuleResponse> copyRule(
            @Parameter(description = "规则ID") @PathVariable String id) {

        log.info("复制规则，ID: {}", id);

        Rule rule = ruleService.copyRule(id);
        RuleResponse response = ruleConverter.convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取规则统计信息", description = "获取规则的统计信息")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<Map<String, Object>> getRuleStatistics() {

        log.info("获取规则统计信息");

        Map<String, Object> statistics = ruleService.getRuleStatistics();

        return ApiResponse.success(statistics);
    }
}