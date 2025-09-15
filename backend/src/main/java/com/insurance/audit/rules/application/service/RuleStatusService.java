package com.insurance.audit.rules.application.service;

import com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest;
import com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest;
import com.insurance.audit.rules.interfaces.dto.response.RuleResponse;
import com.insurance.audit.rules.interfaces.dto.response.BatchOperationResponse;

import java.util.List;

/**
 * 规则状态管理服务接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
public interface RuleStatusService {

    /**
     * 更新规则审核状态
     *
     * @param ruleId 规则ID
     * @param request 更新请求
     * @return 更新后的规则响应
     */
    RuleResponse updateAuditStatus(String ruleId, UpdateAuditStatusRequest request);

    /**
     * 批量更新规则审核状态
     *
     * @param requests 更新请求列表
     * @return 批量操作响应
     */
    BatchOperationResponse batchUpdateAuditStatus(List<UpdateAuditStatusRequest> requests);

    /**
     * 更新规则有效状态
     *
     * @param ruleId 规则ID
     * @param request 更新请求
     * @return 更新后的规则响应
     */
    RuleResponse updateEffectiveStatus(String ruleId, UpdateEffectiveStatusRequest request);

    /**
     * 批量更新规则有效状态
     *
     * @param requests 更新请求列表
     * @return 批量操作响应
     */
    BatchOperationResponse batchUpdateEffectiveStatus(List<UpdateEffectiveStatusRequest> requests);

    /**
     * 提交规则到OA审核
     *
     * @param request 提交请求
     * @return 批量操作响应
     */
    BatchOperationResponse submitToOA(SubmitOARequest request);

    /**
     * 获取审核状态的可用流转路径
     *
     * @param currentStatus 当前审核状态
     * @return 可用流转状态列表
     */
    List<String> getAuditStatusTransitions(String currentStatus);

    /**
     * 获取有效状态的可用流转路径
     *
     * @param currentStatus 当前有效状态
     * @return 可用流转状态列表
     */
    List<String> getEffectiveStatusTransitions(String currentStatus);

    /**
     * 获取规则状态变更历史
     *
     * @param ruleId 规则ID
     * @return 状态变更历史列表
     */
    List<Object> getRuleStatusHistory(String ruleId);

    /**
     * 回滚规则状态
     *
     * @param ruleId 规则ID
     * @param reason 回滚原因
     * @return 回滚后的规则响应
     */
    RuleResponse rollbackRuleStatus(String ruleId, String reason);

    /**
     * 验证状态流转是否合法
     *
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @param statusType 状态类型（AUDIT/EFFECTIVE）
     * @return 是否合法
     */
    boolean validateStatusTransition(String currentStatus, String targetStatus, String statusType);

    /**
     * 自动推进规则状态
     *
     * @param ruleId 规则ID
     * @return 是否成功推进
     */
    boolean autoAdvanceRuleStatus(String ruleId);
}