package com.insurance.audit.rules.application.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.rules.application.service.RuleService;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.infrastructure.mapper.RuleMapper;
import com.insurance.audit.rules.interfaces.dto.request.CreateRuleRequest;
import com.insurance.audit.rules.interfaces.dto.request.RuleQueryRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateRuleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则业务服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final RuleMapper ruleMapper;

    @Override
    public IPage<Rule> getRulePage(RuleQueryRequest queryRequest) {
        log.info("查询规则列表, 查询条件: {}", queryRequest);

        // 创建分页对象
        Page<Rule> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());

        // 执行分页查询
        IPage<Rule> resultPage = ruleMapper.selectPageWithQuery(page, queryRequest);

        log.info("查询规则列表完成, 总记录数: {}, 当前页: {}, 每页大小: {}",
                resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());

        return resultPage;
    }

    @Override
    public Rule getRuleById(String id) {
        log.info("根据ID查询规则: {}", id);

        if (!StringUtils.hasText(id)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "规则ID不能为空");
        }

        Rule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "规则不存在: " + id);
        }

        log.info("查询规则成功: {}", rule.getRuleName());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Rule createRule(CreateRuleRequest createRequest) {
        log.info("创建规则: {}", createRequest.getRuleName());

        // 构建规则实体
        Rule rule = new Rule();
        rule.setRuleNumber(generateRuleNumber());
        rule.setRuleName(createRequest.getRuleName());
        rule.setRuleDescription(createRequest.getRuleDescription());
        rule.setRuleType(createRequest.getRuleType());
        rule.setRuleContent(createRequest.getRuleContent());
        rule.setRuleSource(createRequest.getRuleSource());
        rule.setAuditStatus(createRequest.getAuditStatus());
        rule.setEffectiveStatus(createRequest.getEffectiveStatus());
        rule.setCreatedBy(getCurrentUserId());
        rule.setUpdatedBy(getCurrentUserId());
        rule.setLastUpdatedAt(LocalDateTime.now());

        // 保存规则
        int result = ruleMapper.insert(rule);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建规则失败");
        }

        log.info("创建规则成功: {}, ID: {}", rule.getRuleName(), rule.getId());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Rule updateRule(String id, UpdateRuleRequest updateRequest) {
        log.info("更新规则: {}", id);

        // 检查规则是否存在
        Rule existingRule = getRuleById(id);

        // 更新规则信息
        existingRule.setRuleName(updateRequest.getRuleName());
        existingRule.setRuleDescription(updateRequest.getRuleDescription());
        existingRule.setRuleContent(updateRequest.getRuleContent());
        existingRule.setUpdatedBy(getCurrentUserId());
        existingRule.setLastUpdatedAt(LocalDateTime.now());

        // 保存更新
        int result = ruleMapper.updateById(existingRule);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新规则失败");
        }

        log.info("更新规则成功: {}", existingRule.getRuleName());
        return existingRule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(String id) {
        log.info("删除规则: {}", id);

        // 检查规则是否存在
        Rule rule = getRuleById(id);

        // 执行删除
        int result = ruleMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除规则失败");
        }

        log.info("删除规则成功: {}", rule.getRuleName());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteRules(List<String> ids) {
        log.info("批量删除规则, 数量: {}", ids.size());

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "删除规则ID列表不能为空");
        }

        // 执行批量删除
        int deleteCount = ruleMapper.deleteBatchIds(ids);

        log.info("批量删除规则完成, 删除数量: {}", deleteCount);
        return deleteCount;
    }

    @Override
    public List<Rule> getAllRules() {
        log.info("查询所有规则");

        List<Rule> rules = ruleMapper.selectList(null);

        log.info("查询所有规则完成, 数量: {}", rules.size());
        return rules;
    }

    @Override
    public List<Rule> getRulesByType(String ruleType) {
        log.info("根据规则类型查询规则: {}", ruleType);

        if (!StringUtils.hasText(ruleType)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "规则类型不能为空");
        }

        List<Rule> rules = ruleMapper.selectByRuleType(ruleType);

        log.info("根据类型查询规则完成, 类型: {}, 数量: {}", ruleType, rules.size());
        return rules;
    }

    @Override
    public List<Rule> searchRules(String keyword) {
        log.info("搜索规则, 关键词: {}", keyword);

        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "搜索关键词不能为空");
        }

        List<Rule> rules = ruleMapper.searchByKeyword(keyword);

        log.info("搜索规则完成, 关键词: {}, 数量: {}", keyword, rules.size());
        return rules;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Rule toggleRuleFollow(String id, boolean followed) {
        log.info("切换规则关注状态: {}, 关注: {}", id, followed);

        // 获取规则
        Rule rule = getRuleById(id);

        // 更新关注状态
        rule.setFollowed(followed);
        rule.setUpdatedBy(getCurrentUserId());
        rule.setLastUpdatedAt(LocalDateTime.now());

        // 保存更新
        int result = ruleMapper.updateById(rule);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新规则关注状态失败");
        }

        log.info("切换规则关注状态成功: {}", rule.getRuleName());
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Rule copyRule(String id) {
        log.info("复制规则: {}", id);

        // 获取原规则
        Rule originalRule = getRuleById(id);

        // 创建副本
        Rule copyRule = new Rule();
        copyRule.setRuleNumber(generateRuleNumber());
        copyRule.setRuleName(originalRule.getRuleName() + "_副本");
        copyRule.setRuleDescription(originalRule.getRuleDescription());
        copyRule.setRuleType(originalRule.getRuleType());
        copyRule.setRuleContent(originalRule.getRuleContent());
        copyRule.setRuleSource(originalRule.getRuleSource());
        copyRule.setAuditStatus(originalRule.getAuditStatus());
        copyRule.setEffectiveStatus(originalRule.getEffectiveStatus());
        copyRule.setCreatedBy(getCurrentUserId());
        copyRule.setUpdatedBy(getCurrentUserId());
        copyRule.setLastUpdatedAt(LocalDateTime.now());

        // 保存副本
        int result = ruleMapper.insert(copyRule);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "复制规则失败");
        }

        log.info("复制规则成功: {}, 新ID: {}", copyRule.getRuleName(), copyRule.getId());
        return copyRule;
    }

    @Override
    public Map<String, Object> getRuleStatistics() {
        log.info("获取规则统计信息");

        Map<String, Object> statistics = new HashMap<>();

        // 获取统计数据
        Long totalCount = ruleMapper.selectCount(null);
        Map<String, Long> typeCount = ruleMapper.countByType();
        Map<String, Long> statusCount = ruleMapper.countByStatus();

        statistics.put("totalCount", totalCount);
        statistics.put("typeCount", typeCount);
        statistics.put("statusCount", statusCount);

        log.info("获取规则统计信息完成: {}", statistics);
        return statistics;
    }

    /**
     * 生成规则编号
     * 格式: RULE + 时间戳后8位 + 随机2位数字
     */
    private String generateRuleNumber() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp).substring(5);
        int random = (int) (Math.random() * 99) + 1;
        return "RULE" + timestampStr + String.format("%02d", random);
    }

    /**
     * 获取当前用户ID
     * TODO: 从Security Context中获取
     */
    private String getCurrentUserId() {
        // 临时返回默认值，后续从安全上下文中获取
        return "system";
    }
}