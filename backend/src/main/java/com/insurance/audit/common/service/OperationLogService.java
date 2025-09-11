package com.insurance.audit.common.service;

import com.insurance.audit.common.entity.OperationLog;
import com.insurance.audit.common.enums.OperationType;
import com.insurance.audit.common.enums.OperationStatus;

/**
 * 操作日志服务接口
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface OperationLogService {
    
    /**
     * 记录操作日志
     * 
     * @param operationLog 操作日志
     */
    void saveOperationLog(OperationLog operationLog);
    
    /**
     * 记录登录日志
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param success 是否成功
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @param errorMessage 错误信息
     */
    void recordLoginLog(String userId, String username, boolean success, 
                       String clientIp, String userAgent, String errorMessage);
    
    /**
     * 记录退出日志
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     */
    void recordLogoutLog(String userId, String username, String clientIp, String userAgent);
    
    /**
     * 记录操作日志（简化版本）
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param operationType 操作类型
     * @param operationDesc 操作描述
     * @param status 操作状态
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @param executionTime 执行时间
     * @param errorMessage 错误信息
     */
    void recordOperation(String userId, String username, OperationType operationType,
                        String operationDesc, OperationStatus status, String clientIp,
                        String userAgent, Long executionTime, String errorMessage);
}