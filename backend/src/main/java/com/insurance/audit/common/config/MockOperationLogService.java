package com.insurance.audit.common.config;

import com.insurance.audit.common.entity.OperationLog;
import com.insurance.audit.common.enums.OperationType;
import com.insurance.audit.common.enums.OperationStatus;
import com.insurance.audit.common.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock操作日志服务 - 用于调试
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Profile("debug")
@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")
@Slf4j
public class MockOperationLogService implements OperationLogService {
    
    @Override
    public void saveOperationLog(OperationLog operationLog) {
        log.debug("Mock OperationLogService - saveOperationLog: {}", operationLog);
    }
    
    @Override
    public void recordLoginLog(String userId, String username, boolean success, 
                              String clientIp, String userAgent, String errorMessage) {
        log.info("Mock OperationLogService - Login log: userId={}, username={}, success={}, clientIp={}", 
                userId, username, success, clientIp);
    }
    
    @Override
    public void recordLogoutLog(String userId, String username, String clientIp, String userAgent) {
        log.info("Mock OperationLogService - Logout log: userId={}, username={}, clientIp={}", 
                userId, username, clientIp);
    }
    
    @Override
    public void recordOperation(String userId, String username, OperationType operationType,
                               String operationDesc, OperationStatus status, String clientIp,
                               String userAgent, Long executionTime, String errorMessage) {
        log.info("Mock OperationLogService - Operation log: userId={}, username={}, operation={}, desc={}, status={}", 
                userId, username, operationType, operationDesc, status);
    }
}