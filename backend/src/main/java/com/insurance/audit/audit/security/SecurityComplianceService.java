package com.insurance.audit.audit.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 安全合规服务
 * 接入user-auth-system权限与数据域；脱敏与审计
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SecurityComplianceService {

    @Autowired
    private AuditLogger auditLogger;

    // 敏感数据模式
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\b\\d{15}|\\d{18}|\\d{17}[Xx]\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b1[3-9]\\d{9}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\b\\d{16,19}\\b");

    /**
     * 检查用户是否有权限访问检核功能
     */
    @PreAuthorize("hasRole('AUDIT_USER') or hasRole('ADMIN')")
    public boolean hasAuditPermission(String operation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        log.debug("检查审计权限: user={}, operation={}", username, operation);

        // 记录权限检查
        auditLogger.logPermissionCheck(username, operation, true);

        return true; // 通过@PreAuthorize已经验证了权限
    }

    /**
     * 检查用户数据域权限
     */
    public boolean hasDataScopePermission(String userId, String dataScope) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("未认证用户尝试访问数据域: dataScope={}", dataScope);
            auditLogger.logUnauthorizedAccess("anonymous", dataScope);
            return false;
        }

        String currentUser = auth.getName();

        // 简化的数据域检查逻辑
        // 实际项目中应该调用user-auth-system的API
        boolean hasPermission = checkDataScopePermission(currentUser, dataScope);

        if (!hasPermission) {
            log.warn("用户无数据域权限: user={}, dataScope={}", currentUser, dataScope);
            auditLogger.logUnauthorizedAccess(currentUser, dataScope);
        }

        return hasPermission;
    }

    /**
     * 脱敏敏感数据
     */
    public String redactSensitiveData(String text) {
        if (text == null) {
            return null;
        }

        String result = text;

        // 脱敏身份证号
        result = ID_CARD_PATTERN.matcher(result).replaceAll("***身份证号***");

        // 脱敏电话号码
        result = PHONE_PATTERN.matcher(result).replaceAll("***电话号码***");

        // 脱敏邮箱地址
        result = EMAIL_PATTERN.matcher(result).replaceAll("***邮箱地址***");

        // 脱敏银行卡号
        result = BANK_CARD_PATTERN.matcher(result).replaceAll("***银行卡号***");

        // 记录脱敏操作
        if (!result.equals(text)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "system";
            auditLogger.logDataRedaction(username, text.length(), result.length());
        }

        return result;
    }

    /**
     * 记录检核决策审计日志
     */
    public void logAuditDecision(String jobId, String ruleId, String decision, String reasoning) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "system";

        AuditDecisionLog decisionLog = AuditDecisionLog.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .jobId(jobId)
                .ruleId(ruleId)
                .decision(decision)
                .reasoning(redactSensitiveData(reasoning)) // 脱敏推理过程
                .build();

        auditLogger.logDecision(decisionLog);

        log.info("记录检核决策: user={}, jobId={}, ruleId={}, decision={}",
                username, jobId, ruleId, decision);
    }

    /**
     * 记录数据访问审计日志
     */
    public void logDataAccess(String operation, String resourceType, String resourceId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        DataAccessLog accessLog = DataAccessLog.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .operation(operation)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(getCurrentIpAddress())
                .userAgent(getCurrentUserAgent())
                .build();

        auditLogger.logDataAccess(accessLog);

        log.debug("记录数据访问: user={}, operation={}, resource={}:{}",
                username, operation, resourceType, resourceId);
    }

    /**
     * 验证输入数据安全性
     */
    public boolean validateInputSecurity(String input) {
        if (input == null) {
            return true;
        }

        // 检查SQL注入模式
        String lowerInput = input.toLowerCase();
        String[] sqlKeywords = {"select", "insert", "update", "delete", "drop", "union", "script"};
        for (String keyword : sqlKeywords) {
            if (lowerInput.contains(keyword)) {
                log.warn("检测到潜在的SQL注入攻击: input={}", input.substring(0, Math.min(100, input.length())));
                auditLogger.logSecurityThreat("SQL_INJECTION", input);
                return false;
            }
        }

        // 检查XSS模式
        if (input.contains("<script") || input.contains("javascript:") || input.contains("onload=")) {
            log.warn("检测到潜在的XSS攻击: input={}", input.substring(0, Math.min(100, input.length())));
            auditLogger.logSecurityThreat("XSS_ATTACK", input);
            return false;
        }

        return true;
    }

    /**
     * 检查数据域权限（简化版）
     */
    private boolean checkDataScopePermission(String username, String dataScope) {
        // 实际项目中应该调用user-auth-system的API
        // 这里是简化的实现

        // 管理员拥有所有权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // 普通用户只能访问自己的数据域
        return dataScope.startsWith("user:" + username) || dataScope.equals("public");
    }

    /**
     * 获取当前IP地址
     */
    private String getCurrentIpAddress() {
        // 简化实现，实际项目中应该从HttpServletRequest获取
        return "127.0.0.1";
    }

    /**
     * 获取当前用户代理
     */
    private String getCurrentUserAgent() {
        // 简化实现，实际项目中应该从HttpServletRequest获取
        return "unknown";
    }

    /**
     * 审计决策日志
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditDecisionLog {
        private LocalDateTime timestamp;
        private String username;
        private String jobId;
        private String ruleId;
        private String decision;
        private String reasoning;
    }

    /**
     * 数据访问日志
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DataAccessLog {
        private LocalDateTime timestamp;
        private String username;
        private String operation;
        private String resourceType;
        private String resourceId;
        private String ipAddress;
        private String userAgent;
    }
}