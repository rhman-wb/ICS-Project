package com.insurance.audit.rules.application.service.impl;

import com.insurance.audit.common.exception.BusinessException;
import com.insurance.audit.common.exception.ErrorCode;
import com.insurance.audit.rules.application.service.RuleService;
import com.insurance.audit.rules.application.service.RuleStatusService;
import com.insurance.audit.rules.domain.Rule;
import com.insurance.audit.rules.enums.RuleAuditStatus;
import com.insurance.audit.rules.enums.RuleEffectiveStatus;
import com.insurance.audit.rules.infrastructure.mapper.RuleMapper;
import com.insurance.audit.rules.infrastructure.mapper.RuleOperationLogMapper;
import com.insurance.audit.rules.interfaces.dto.request.SubmitOARequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateAuditStatusRequest;
import com.insurance.audit.rules.interfaces.dto.request.UpdateEffectiveStatusRequest;
import com.insurance.audit.rules.interfaces.dto.response.BatchOperationResponse;
import com.insurance.audit.rules.interfaces.dto.response.RuleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则状态管理服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleStatusServiceImpl implements RuleStatusService {

    private final RuleService ruleService;
    private final RuleMapper ruleMapper;
    private final RuleOperationLogMapper operationLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleResponse updateAuditStatus(String ruleId, UpdateAuditStatusRequest request) {
        log.info("更新规则审核状态: {}, 目标状态: {}", ruleId, request.getAuditStatus());

        // 获取规则
        Rule rule = ruleService.getRuleById(ruleId);

        // 验证状态流转
        if (!validateStatusTransition(rule.getAuditStatus().name(),
                request.getAuditStatus().name(), "AUDIT")) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                    "无效的审核状态流转: " + rule.getAuditStatus() + " -> " + request.getAuditStatus());
        }

        // 更新状态
        rule.setAuditStatus(request.getAuditStatus());
        rule.setAuditRemark(request.getRemark());
        rule.setUpdatedBy(getCurrentUserId());
        rule.setLastUpdatedAt(LocalDateTime.now());

        // 保存更新
        int result = ruleMapper.updateById(rule);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新审核状态失败");
        }

        // 记录操作日志
        recordOperationLog(ruleId, "UPDATE_AUDIT_STATUS",
                "审核状态更新: " + rule.getAuditStatus(), request.getRemark());

        log.info("更新规则审核状态成功: {}", rule.getRuleName());
        return convertToResponse(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse batchUpdateAuditStatus(List<UpdateAuditStatusRequest> requests) {
        log.info("批量更新规则审核状态, 数量: {}", requests.size());

        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        Map<String, String> errorMessages = new HashMap<>();

        for (UpdateAuditStatusRequest request : requests) {
            try {
                updateAuditStatus(request.getRuleId(), request);
                successIds.add(request.getRuleId());
            } catch (Exception e) {
                failedIds.add(request.getRuleId());
                errorMessages.put(request.getRuleId(), e.getMessage());
                log.error("批量更新审核状态失败: {}", request.getRuleId(), e);
            }
        }

        BatchOperationResponse response = new BatchOperationResponse();
        response.setSuccessCount(successIds.size());
        response.setFailedCount(failedIds.size());
        response.setSuccessIds(successIds);
        response.setFailedIds(failedIds);
        response.setErrorMessages(errorMessages);

        log.info("批量更新审核状态完成, 成功: {}, 失败: {}", successIds.size(), failedIds.size());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleResponse updateEffectiveStatus(String ruleId, UpdateEffectiveStatusRequest request) {
        log.info("更新规则有效状态: {}, 目标状态: {}", ruleId, request.getEffectiveStatus());

        // 获取规则
        Rule rule = ruleService.getRuleById(ruleId);

        // 验证状态流转
        if (!validateStatusTransition(rule.getEffectiveStatus().name(),
                request.getEffectiveStatus().name(), "EFFECTIVE")) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                    "无效的有效状态流转: " + rule.getEffectiveStatus() + " -> " + request.getEffectiveStatus());
        }

        // 更新状态
        rule.setEffectiveStatus(request.getEffectiveStatus());
        rule.setEffectiveRemark(request.getRemark());
        rule.setUpdatedBy(getCurrentUserId());
        rule.setLastUpdatedAt(LocalDateTime.now());

        // 保存更新
        int result = ruleMapper.updateById(rule);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新有效状态失败");
        }

        // 记录操作日志
        recordOperationLog(ruleId, "UPDATE_EFFECTIVE_STATUS",
                "有效状态更新: " + rule.getEffectiveStatus(), request.getRemark());

        log.info("更新规则有效状态成功: {}", rule.getRuleName());
        return convertToResponse(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse batchUpdateEffectiveStatus(List<UpdateEffectiveStatusRequest> requests) {
        log.info("批量更新规则有效状态, 数量: {}", requests.size());

        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        Map<String, String> errorMessages = new HashMap<>();

        for (UpdateEffectiveStatusRequest request : requests) {
            try {
                updateEffectiveStatus(request.getRuleId(), request);
                successIds.add(request.getRuleId());
            } catch (Exception e) {
                failedIds.add(request.getRuleId());
                errorMessages.put(request.getRuleId(), e.getMessage());
                log.error("批量更新有效状态失败: {}", request.getRuleId(), e);
            }
        }

        BatchOperationResponse response = new BatchOperationResponse();
        response.setSuccessCount(successIds.size());
        response.setFailedCount(failedIds.size());
        response.setSuccessIds(successIds);
        response.setFailedIds(failedIds);
        response.setErrorMessages(errorMessages);

        log.info("批量更新有效状态完成, 成功: {}, 失败: {}", successIds.size(), failedIds.size());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse submitToOA(SubmitOARequest request) {
        log.info("提交规则到OA审核, 数量: {}", request.getRuleIds().size());

        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        Map<String, String> errorMessages = new HashMap<>();

        for (String ruleId : request.getRuleIds()) {
            try {
                // 获取规则
                Rule rule = ruleService.getRuleById(ruleId);

                // 验证规则状态是否可提交
                if (!canSubmitToOA(rule)) {
                    throw new BusinessException(ErrorCode.INVALID_PARAMETER,
                            "规则当前状态不允许提交OA: " + rule.getAuditStatus());
                }

                // 更新为待审核状态
                rule.setAuditStatus(RuleAuditStatus.PENDING);
                rule.setAuditRemark("已提交OA审核");
                rule.setUpdatedBy(getCurrentUserId());
                rule.setLastUpdatedAt(LocalDateTime.now());

                ruleMapper.updateById(rule);

                // 记录操作日志
                recordOperationLog(ruleId, "SUBMIT_OA",
                        "提交OA审核, 预期审核日期: " + request.getExpectedReviewDate(),
                        request.getSubmitReason());

                successIds.add(ruleId);

            } catch (Exception e) {
                failedIds.add(ruleId);
                errorMessages.put(ruleId, e.getMessage());
                log.error("提交OA审核失败: {}", ruleId, e);
            }
        }

        BatchOperationResponse response = new BatchOperationResponse();
        response.setSuccessCount(successIds.size());
        response.setFailedCount(failedIds.size());
        response.setSuccessIds(successIds);
        response.setFailedIds(failedIds);
        response.setErrorMessages(errorMessages);

        log.info("提交OA审核完成, 成功: {}, 失败: {}", successIds.size(), failedIds.size());
        return response;
    }

    @Override
    public List<String> getAuditStatusTransitions(String currentStatus) {
        log.info("获取审核状态流转路径: {}", currentStatus);

        List<String> transitions = new ArrayList<>();

        try {
            RuleAuditStatus status = RuleAuditStatus.valueOf(currentStatus);
            switch (status) {
                case DRAFT:
                    transitions.addAll(Arrays.asList("PENDING", "CANCELLED"));
                    break;
                case PENDING:
                    transitions.addAll(Arrays.asList("APPROVED", "REJECTED", "CANCELLED"));
                    break;
                case APPROVED:
                    transitions.addAll(Arrays.asList("EFFECTIVE", "CANCELLED"));
                    break;
                case REJECTED:
                    transitions.addAll(Arrays.asList("DRAFT", "CANCELLED"));
                    break;
                case EFFECTIVE:
                    transitions.addAll(Arrays.asList("CANCELLED"));
                    break;
                case CANCELLED:
                    // 已取消状态无法流转
                    break;
            }
        } catch (IllegalArgumentException e) {
            log.warn("无效的审核状态: {}", currentStatus);
        }

        log.info("审核状态流转路径: {} -> {}", currentStatus, transitions);
        return transitions;
    }

    @Override
    public List<String> getEffectiveStatusTransitions(String currentStatus) {
        log.info("获取有效状态流转路径: {}", currentStatus);

        List<String> transitions = new ArrayList<>();

        try {
            RuleEffectiveStatus status = RuleEffectiveStatus.valueOf(currentStatus);
            switch (status) {
                case INACTIVE:
                    transitions.addAll(Arrays.asList("ACTIVE", "SUSPENDED"));
                    break;
                case ACTIVE:
                    transitions.addAll(Arrays.asList("INACTIVE", "SUSPENDED", "EXPIRED"));
                    break;
                case SUSPENDED:
                    transitions.addAll(Arrays.asList("ACTIVE", "INACTIVE", "EXPIRED"));
                    break;
                case EXPIRED:
                    transitions.addAll(Arrays.asList("INACTIVE"));
                    break;
            }
        } catch (IllegalArgumentException e) {
            log.warn("无效的有效状态: {}", currentStatus);
        }

        log.info("有效状态流转路径: {} -> {}", currentStatus, transitions);
        return transitions;
    }

    @Override
    public List<Object> getRuleStatusHistory(String ruleId) {
        log.info("获取规则状态变更历史: {}", ruleId);

        List<Object> history = operationLogMapper.selectByRuleId(ruleId);

        log.info("获取状态变更历史完成, 记录数: {}", history.size());
        return history;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleResponse rollbackRuleStatus(String ruleId, String reason) {
        log.info("回滚规则状态: {}, 原因: {}", ruleId, reason);

        // 获取规则
        Rule rule = ruleService.getRuleById(ruleId);

        // 获取上一个有效状态（简化实现，实际应从历史记录获取）
        RuleAuditStatus previousStatus = getPreviousAuditStatus(rule.getAuditStatus());

        rule.setAuditStatus(previousStatus);
        rule.setAuditRemark("状态回滚: " + reason);
        rule.setUpdatedBy(getCurrentUserId());
        rule.setLastUpdatedAt(LocalDateTime.now());

        ruleMapper.updateById(rule);

        // 记录操作日志
        recordOperationLog(ruleId, "ROLLBACK_STATUS",
                "状态回滚: " + previousStatus, reason);

        log.info("回滚规则状态成功: {}", rule.getRuleName());
        return convertToResponse(rule);
    }

    @Override
    public boolean validateStatusTransition(String currentStatus, String targetStatus, String statusType) {
        log.debug("验证状态流转: {} -> {}, 类型: {}", currentStatus, targetStatus, statusType);

        if (!StringUtils.hasText(currentStatus) || !StringUtils.hasText(targetStatus)) {
            return false;
        }

        List<String> allowedTransitions;
        if ("AUDIT".equals(statusType)) {
            allowedTransitions = getAuditStatusTransitions(currentStatus);
        } else if ("EFFECTIVE".equals(statusType)) {
            allowedTransitions = getEffectiveStatusTransitions(currentStatus);
        } else {
            return false;
        }

        boolean isValid = allowedTransitions.contains(targetStatus);
        log.debug("状态流转验证结果: {}", isValid);
        return isValid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoAdvanceRuleStatus(String ruleId) {
        log.info("自动推进规则状态: {}", ruleId);

        try {
            Rule rule = ruleService.getRuleById(ruleId);

            // 根据业务规则自动推进状态
            boolean advanced = false;
            if (rule.getAuditStatus() == RuleAuditStatus.APPROVED &&
                rule.getEffectiveStatus() == RuleEffectiveStatus.INACTIVE) {

                rule.setEffectiveStatus(RuleEffectiveStatus.ACTIVE);
                rule.setEffectiveRemark("自动激活");
                rule.setUpdatedBy("system");
                rule.setLastUpdatedAt(LocalDateTime.now());

                ruleMapper.updateById(rule);
                recordOperationLog(ruleId, "AUTO_ADVANCE", "自动推进状态", "系统自动操作");
                advanced = true;
            }

            log.info("自动推进规则状态完成: {}, 是否推进: {}", ruleId, advanced);
            return advanced;

        } catch (Exception e) {
            log.error("自动推进规则状态失败: {}", ruleId, e);
            return false;
        }
    }

    /**
     * 检查规则是否可以提交OA
     */
    private boolean canSubmitToOA(Rule rule) {
        return rule.getAuditStatus() == RuleAuditStatus.DRAFT ||
               rule.getAuditStatus() == RuleAuditStatus.REJECTED;
    }

    /**
     * 获取上一个审核状态（简化实现）
     */
    private RuleAuditStatus getPreviousAuditStatus(RuleAuditStatus currentStatus) {
        switch (currentStatus) {
            case PENDING:
                return RuleAuditStatus.DRAFT;
            case APPROVED:
            case REJECTED:
                return RuleAuditStatus.PENDING;
            case EFFECTIVE:
                return RuleAuditStatus.APPROVED;
            default:
                return RuleAuditStatus.DRAFT;
        }
    }

    /**
     * 记录操作日志
     */
    private void recordOperationLog(String ruleId, String operationType, String operationDescription, String remark) {
        try {
            // TODO: 实现操作日志记录
            log.info("记录操作日志: 规则ID={}, 操作类型={}, 描述={}", ruleId, operationType, operationDescription);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", ruleId, e);
        }
    }

    /**
     * 转换为响应对象
     */
    private RuleResponse convertToResponse(Rule rule) {
        // TODO: 实现转换逻辑，这里简化处理
        RuleResponse response = new RuleResponse();
        response.setId(rule.getId());
        response.setRuleName(rule.getRuleName());
        response.setAuditStatus(rule.getAuditStatus());
        response.setEffectiveStatus(rule.getEffectiveStatus());
        return response;
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        return "system"; // 临时返回
    }
}