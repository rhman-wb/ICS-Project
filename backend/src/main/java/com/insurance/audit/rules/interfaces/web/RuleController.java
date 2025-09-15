package com.insurance.audit.rules.interfaces.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.common.dto.PageResponse;
import com.insurance.audit.rules.application.service.RuleService;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.request.RuleQueryRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest;
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
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Validated
@Tag(name = "规则管理", description = "规则管理相关API")
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    @Operation(summary = "分页查询规则列表", description = "根据条件分页查询规则列表")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<PageResponse<RuleResponse>> getRules(
            @Parameter(description = "查询条件") RuleQueryRequest queryRequest) {

        log.info("查询规则列表，查询条件: {}", queryRequest);

        IPage<Rule> rulePage = ruleService.getRulePage(queryRequest);

        // 转换为响应DTO
        PageResponse<RuleResponse> response = PageResponse.of(
            rulePage.getRecords().stream()
                .map(this::convertToResponse)
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
        RuleResponse response = convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(summary = "创建规则", description = "创建新的规则")
    @PreAuthorize("hasAuthority('RULE_CREATE')")
    public ApiResponse<RuleResponse> createRule(
            @Parameter(description = "创建规则请求") @Valid @RequestBody CreateRuleRequest request) {

        log.info("创建规则，请求: {}", request);

        Rule rule = ruleService.createRule(request);
        RuleResponse response = convertToResponse(rule);

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
        RuleResponse response = convertToResponse(rule);

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
    public ApiResponse<Map<String, Object>> batchDeleteRules(
            @Parameter(description = "规则ID列表") @RequestBody List<String> ids) {

        log.info("批量删除规则，IDs: {}", ids);

        int deletedCount = ruleService.batchDeleteRules(ids);

        Map<String, Object> result = Map.of(
            "totalRequested", ids.size(),
            "deletedCount", deletedCount,
            "success", deletedCount > 0
        );

        return ApiResponse.success(result);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有规则", description = "获取所有规则（无分页）")
    @PreAuthorize("hasAuthority('RULE_VIEW')")
    public ApiResponse<List<RuleResponse>> getAllRules() {

        log.info("获取所有规则");

        List<Rule> rules = ruleService.getAllRules();
        List<RuleResponse> response = rules.stream()
            .map(this::convertToResponse)
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
            .map(this::convertToResponse)
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
        RuleResponse response = convertToResponse(rule);

        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/copy")
    @Operation(summary = "复制规则", description = "复制现有规则创建新规则")
    @PreAuthorize("hasAuthority('RULE_CREATE')")
    public ApiResponse<RuleResponse> copyRule(
            @Parameter(description = "规则ID") @PathVariable String id) {

        log.info("复制规则，ID: {}", id);

        Rule rule = ruleService.copyRule(id);
        RuleResponse response = convertToResponse(rule);

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

    /**
     * 转换Rule实体为RuleResponse
     */
    private RuleResponse convertToResponse(Rule rule) {
        RuleResponse response = new RuleResponse();
        response.setId(rule.getId());
        response.setRuleNumber(rule.getRuleNumber());
        response.setRuleName(rule.getRuleName());
        response.setRuleDescription(rule.getRuleDescription());
        response.setRuleType(rule.getRuleType());
        response.setRuleSource(rule.getRuleSource());
        response.setManageDepartment(rule.getManageDepartment());
        response.setApplicableInsurance(rule.getApplicableInsurance());
        response.setApplicableRequirements(rule.getApplicableRequirements());
        response.setApplicableChapter(rule.getApplicableChapter());
        response.setBusinessArea(rule.getBusinessArea());
        response.setAuditStatus(rule.getAuditStatus());
        response.setEffectiveStatus(rule.getEffectiveStatus());
        response.setRuleContent(rule.getRuleContent());
        response.setRuleConfig(rule.getRuleConfig());
        response.setEffectiveTime(rule.getEffectiveTime());
        response.setExpiryTime(rule.getExpiryTime());
        response.setLastUpdatedAt(rule.getLastUpdatedAt());
        response.setSubmittedBy(rule.getSubmittedBy());
        response.setSubmittedAt(rule.getSubmittedAt());
        response.setAuditedBy(rule.getAuditedBy());
        response.setAuditedAt(rule.getAuditedAt());
        response.setAuditComments(rule.getAuditComments());
        response.setFollowed(rule.getFollowed());
        response.setPriority(rule.getPriority());
        response.setSortOrder(rule.getSortOrder());
        response.setTags(rule.getTags());
        response.setRemarks(rule.getRemarks());
        response.setCreatedBy(rule.getCreatedBy());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedBy(rule.getUpdatedBy());
        response.setUpdatedAt(rule.getUpdatedAt());
        response.setVersion(rule.getVersion());

        // TODO: 根据规则类型加载子规则数据
        // response.setSubRuleData(loadSubRuleData(rule));

        return response;
    }
}