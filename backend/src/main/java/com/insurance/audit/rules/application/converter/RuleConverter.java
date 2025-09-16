package com.insurance.audit.rules.application.converter;

import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.response.RuleResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则转换器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Component
public class RuleConverter {

    /**
     * 转换Rule实体为RuleResponse
     *
     * @param rule 规则实体
     * @return 规则响应对象
     */
    public RuleResponse convertToResponse(Rule rule) {
        if (rule == null) {
            return null;
        }

        RuleResponse response = new RuleResponse();
        response.setId(rule.getId());
        response.setRuleNumber(rule.getRuleNumber());
        response.setRuleName(rule.getRuleName());
        response.setRuleDescription(rule.getRuleDescription());
        response.setRuleType(rule.getRuleType());
        response.setRuleContent(rule.getRuleContent());
        response.setRuleSource(rule.getRuleSource());
        response.setAuditStatus(rule.getAuditStatus());
        response.setEffectiveStatus(rule.getEffectiveStatus());
        response.setAuditRemark(rule.getAuditRemark());
        response.setEffectiveRemark(rule.getEffectiveRemark());
        response.setFollowed(rule.getFollowed());
        response.setCreatedBy(rule.getCreatedBy());
        response.setUpdatedBy(rule.getUpdatedBy());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        response.setLastUpdatedAt(rule.getLastUpdatedAt());

        return response;
    }

    /**
     * 批量转换Rule实体列表为RuleResponse列表
     *
     * @param rules 规则实体列表
     * @return 规则响应对象列表
     */
    public List<RuleResponse> convertToResponseList(List<Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }

        return rules.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换CreateRuleRequest为Rule实体
     *
     * @param request 创建请求
     * @return 规则实体
     */
    public Rule convertFromCreateRequest(CreateRuleRequest request) {
        if (request == null) {
            return null;
        }

        Rule rule = new Rule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleDescription(request.getRuleDescription());
        rule.setRuleType(request.getRuleType());
        rule.setRuleContent(request.getRuleContent());
        rule.setRuleSource(request.getRuleSource());
        rule.setAuditStatus(request.getAuditStatus());
        rule.setEffectiveStatus(request.getEffectiveStatus());

        return rule;
    }

    /**
     * 将UpdateRuleRequest的数据更新到Rule实体
     *
     * @param rule 规则实体
     * @param request 更新请求
     */
    public void updateRuleFromRequest(Rule rule, UpdateRuleRequest request) {
        if (rule == null || request == null) {
            return;
        }

        if (request.getRuleName() != null) {
            rule.setRuleName(request.getRuleName());
        }
        if (request.getRuleDescription() != null) {
            rule.setRuleDescription(request.getRuleDescription());
        }
        if (request.getRuleContent() != null) {
            rule.setRuleContent(request.getRuleContent());
        }
        if (request.getRuleSource() != null) {
            rule.setRuleSource(request.getRuleSource());
        }
    }

    /**
     * 转换为简单响应对象（仅包含基本信息）
     *
     * @param rule 规则实体
     * @return 简单响应对象
     */
    public RuleResponse convertToSimpleResponse(Rule rule) {
        if (rule == null) {
            return null;
        }

        RuleResponse response = new RuleResponse();
        response.setId(rule.getId());
        response.setRuleNumber(rule.getRuleNumber());
        response.setRuleName(rule.getRuleName());
        response.setRuleDescription(rule.getRuleDescription());
        response.setRuleType(rule.getRuleType());
        response.setAuditStatus(rule.getAuditStatus());
        response.setEffectiveStatus(rule.getEffectiveStatus());
        response.setFollowed(rule.getFollowed());

        return response;
    }

    /**
     * 转换为统计响应对象
     *
     * @param rule 规则实体
     * @return 统计响应对象
     */
    public RuleResponse convertToStatisticsResponse(Rule rule) {
        if (rule == null) {
            return null;
        }

        RuleResponse response = new RuleResponse();
        response.setId(rule.getId());
        response.setRuleName(rule.getRuleName());
        response.setRuleType(rule.getRuleType());
        response.setAuditStatus(rule.getAuditStatus());
        response.setEffectiveStatus(rule.getEffectiveStatus());
        response.setCreatedAt(rule.getCreatedAt());
        response.setLastUpdatedAt(rule.getLastUpdatedAt());

        return response;
    }
}