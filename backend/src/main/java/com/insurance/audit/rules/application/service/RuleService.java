package com.insurance.audit.rules.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.interfaces.dto.request.RuleQueryRequest;
import com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest;

import java.util.List;
import java.util.Map;

/**
 * 规则业务服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
public interface RuleService {

    /**
     * 分页查询规则列表
     *
     * @param queryRequest 查询请求
     * @return 分页规则列表
     */
    IPage<Rule> getRulePage(RuleQueryRequest queryRequest);

    /**
     * 根据ID获取规则详情
     *
     * @param id 规则ID
     * @return 规则实体
     */
    Rule getRuleById(String id);

    /**
     * 创建规则
     *
     * @param createRequest 创建请求
     * @return 创建后的规则实体
     */
    Rule createRule(CreateRuleRequest createRequest);

    /**
     * 更新规则
     *
     * @param id 规则ID
     * @param updateRequest 更新请求
     * @return 更新后的规则实体
     */
    Rule updateRule(String id, UpdateRuleRequest updateRequest);

    /**
     * 删除规则
     *
     * @param id 规则ID
     * @return 是否删除成功
     */
    boolean deleteRule(String id);

    /**
     * 批量删除规则
     *
     * @param ids 规则ID列表
     * @return 删除成功的数量
     */
    int batchDeleteRules(List<String> ids);

    /**
     * 获取所有规则（无分页）
     *
     * @return 规则列表
     */
    List<Rule> getAllRules();

    /**
     * 根据规则类型查询规则
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    List<Rule> getRulesByType(String ruleType);

    /**
     * 搜索规则
     *
     * @param keyword 关键词
     * @return 规则列表
     */
    List<Rule> searchRules(String keyword);

    /**
     * 关注/取消关注规则
     *
     * @param id 规则ID
     * @param followed 是否关注
     * @return 更新后的规则实体
     */
    Rule toggleRuleFollow(String id, boolean followed);

    /**
     * 复制规则
     *
     * @param id 规则ID
     * @return 复制后的规则实体
     */
    Rule copyRule(String id);

    /**
     * 获取规则统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getRuleStatistics();
}