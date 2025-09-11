package com.insurance.audit.common.service.impl;

import com.insurance.audit.common.entity.OperationLog;
import com.insurance.audit.common.enums.OperationType;
import com.insurance.audit.common.enums.OperationStatus;
import com.insurance.audit.common.mapper.OperationLogMapper;
import com.insurance.audit.common.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "true", matchIfMissing = true)
public class OperationLogServiceImpl implements OperationLogService {
    
    private final OperationLogMapper operationLogMapper;
    
    @Override
    @Async
    public void saveOperationLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("Failed to save operation log", e);
        }
    }
    
    @Override
    public void recordLoginLog(String userId, String username, boolean success, 
                              String clientIp, String userAgent, String errorMessage) {
        // 统一补全日志与表结构必需列，避免 NOT NULL/无默认值问题
        OperationLog operationLog = OperationLog.builder()
                .userId(userId)
                .username(username)
                .operationType(OperationType.LOGIN)
                .operationName(success ? "用户登录" : "用户登录")
                // 与现有表结构对齐：operation_name 为必填列
                .operationDesc(success ? "用户登录成功" : "用户登录失败")
                .operationStatus(success ? OperationStatus.SUCCESS : OperationStatus.FAILED)
                // 表结构里既有 client_ip，也保留 ip_address 列，全部写入以兼容
                .clientIp(clientIp)
                .ipAddress(clientIp)
                .requestMethod("POST")
                .method("POST")
                .requestUrl("/api/v1/auth/login")
                .userAgent(userAgent)
                .errorMessage(errorMessage)
                .module("认证模块")
                .businessType("USER_AUTH")
                .build();

        // 补充 operation_name 以兼容旧表结构
        // operationName 已在实体中定义并赋值
        
        saveOperationLog(operationLog);
        
        // 记录到应用日志
        if (success) {
            log.info("User login successful - userId: {}, username: {}, clientIp: {}", 
                    userId, username, clientIp);
        } else {
            log.warn("User login failed - username: {}, clientIp: {}, error: {}", 
                    username, clientIp, errorMessage);
        }
    }
    
    @Override
    public void recordLogoutLog(String userId, String username, String clientIp, String userAgent) {
        OperationLog operationLog = OperationLog.builder()
                .userId(userId)
                .username(username)
                .operationType(OperationType.LOGOUT)
                .operationName("用户退出登录")
                .operationDesc("用户退出登录")
                .operationStatus(OperationStatus.SUCCESS)
                .clientIp(clientIp)
                .ipAddress(clientIp)
                .requestMethod("POST")
                .method("POST")
                .userAgent(userAgent)
                .module("认证模块")
                .businessType("USER_AUTH")
                .build();
        
        saveOperationLog(operationLog);
        
        log.info("User logout successful - userId: {}, username: {}, clientIp: {}", 
                userId, username, clientIp);
    }
    
    @Override
    public void recordOperation(String userId, String username, OperationType operationType,
                               String operationDesc, OperationStatus status, String clientIp,
                               String userAgent, Long executionTime, String errorMessage) {
        OperationLog operationLog = OperationLog.builder()
                .userId(userId)
                .username(username)
                .operationType(operationType)
                .operationName(operationType != null ? operationType.name() : "业务操作")
                .operationDesc(operationDesc)
                .operationStatus(status)
                .clientIp(clientIp)
                .ipAddress(clientIp)
                .requestMethod("POST")
                .method("POST")
                .userAgent(userAgent)
                .executionTime(executionTime)
                .errorMessage(errorMessage)
                .module("系统模块")
                .businessType("SYSTEM")
                .build();
        
        saveOperationLog(operationLog);
    }
}