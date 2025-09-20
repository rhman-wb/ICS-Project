package com.insurance.audit.audit.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审计日志服务
 * 记录系统的各种审计事件
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuditLogger {

    // 内存中的审计日志存储（实际项目中应该使用数据库）
    private final Map<String, Object> auditLogs = new ConcurrentHashMap<>();

    /**
     * 记录权限检查
     */
    public void logPermissionCheck(String username, String operation, boolean granted) {
        String logKey = generateLogKey("PERMISSION_CHECK");

        PermissionCheckLog log = PermissionCheckLog.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .operation(operation)
                .granted(granted)
                .build();

        auditLogs.put(logKey, log);

        // 输出到日志文件
        if (granted) {
            this.log.info("权限检查通过: user={}, operation={}", username, operation);
        } else {
            this.log.warn("权限检查失败: user={}, operation={}", username, operation);
        }
    }

    /**
     * 记录未授权访问
     */
    public void logUnauthorizedAccess(String username, String resource) {
        String logKey = generateLogKey("UNAUTHORIZED_ACCESS");

        UnauthorizedAccessLog log = UnauthorizedAccessLog.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .resource(resource)
                .build();

        auditLogs.put(logKey, log);

        this.log.error("检测到未授权访问: user={}, resource={}", username, resource);
    }

    /**
     * 记录数据脱敏
     */
    public void logDataRedaction(String username, int originalLength, int redactedLength) {
        String logKey = generateLogKey("DATA_REDACTION");

        DataRedactionLog log = DataRedactionLog.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .originalLength(originalLength)
                .redactedLength(redactedLength)
                .redactionRatio((double) (originalLength - redactedLength) / originalLength * 100)
                .build();

        auditLogs.put(logKey, log);

        this.log.debug("执行数据脱敏: user={}, originalLength={}, redactedLength={}",
                username, originalLength, redactedLength);
    }

    /**
     * 记录检核决策
     */
    public void logDecision(SecurityComplianceService.AuditDecisionLog decisionLog) {
        String logKey = generateLogKey("AUDIT_DECISION");
        auditLogs.put(logKey, decisionLog);

        this.log.info("记录检核决策: user={}, jobId={}, ruleId={}, decision={}",
                decisionLog.getUsername(), decisionLog.getJobId(),
                decisionLog.getRuleId(), decisionLog.getDecision());
    }

    /**
     * 记录数据访问
     */
    public void logDataAccess(SecurityComplianceService.DataAccessLog accessLog) {
        String logKey = generateLogKey("DATA_ACCESS");
        auditLogs.put(logKey, accessLog);

        this.log.info("记录数据访问: user={}, operation={}, resource={}:{}",
                accessLog.getUsername(), accessLog.getOperation(),
                accessLog.getResourceType(), accessLog.getResourceId());
    }

    /**
     * 记录安全威胁
     */
    public void logSecurityThreat(String threatType, String details) {
        String logKey = generateLogKey("SECURITY_THREAT");

        SecurityThreatLog log = SecurityThreatLog.builder()
                .timestamp(LocalDateTime.now())
                .threatType(threatType)
                .details(details.length() > 500 ? details.substring(0, 500) + "..." : details)
                .severity("HIGH")
                .build();

        auditLogs.put(logKey, log);

        this.log.error("检测到安全威胁: type={}, details={}", threatType, log.getDetails());
    }

    /**
     * 记录系统事件
     */
    public void logSystemEvent(String eventType, String description, Map<String, Object> context) {
        String logKey = generateLogKey("SYSTEM_EVENT");

        SystemEventLog log = SystemEventLog.builder()
                .timestamp(LocalDateTime.now())
                .eventType(eventType)
                .description(description)
                .context(context)
                .build();

        auditLogs.put(logKey, log);

        this.log.info("系统事件: type={}, description={}", eventType, description);
    }

    /**
     * 获取审计日志统计
     */
    public AuditLogStats getAuditLogStats() {
        Map<String, Long> eventCounts = new ConcurrentHashMap<>();

        for (Object logEntry : auditLogs.values()) {
            String eventType = getEventType(logEntry);
            eventCounts.merge(eventType, 1L, Long::sum);
        }

        return AuditLogStats.builder()
                .totalLogs(auditLogs.size())
                .eventCounts(eventCounts)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * 清理过期日志
     */
    public void cleanupExpiredLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int cleanedCount = 0;

        var iterator = auditLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            LocalDateTime logTime = getLogTimestamp(entry.getValue());

            if (logTime != null && logTime.isBefore(cutoffDate)) {
                iterator.remove();
                cleanedCount++;
            }
        }

        this.log.info("清理过期审计日志: 删除{}条记录, 保留{}天内的日志", cleanedCount, retentionDays);
    }

    /**
     * 生成日志键
     */
    private String generateLogKey(String eventType) {
        return eventType + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * 获取事件类型
     */
    private String getEventType(Object logEntry) {
        if (logEntry instanceof PermissionCheckLog) {
            return "PERMISSION_CHECK";
        } else if (logEntry instanceof UnauthorizedAccessLog) {
            return "UNAUTHORIZED_ACCESS";
        } else if (logEntry instanceof DataRedactionLog) {
            return "DATA_REDACTION";
        } else if (logEntry instanceof SecurityComplianceService.AuditDecisionLog) {
            return "AUDIT_DECISION";
        } else if (logEntry instanceof SecurityComplianceService.DataAccessLog) {
            return "DATA_ACCESS";
        } else if (logEntry instanceof SecurityThreatLog) {
            return "SECURITY_THREAT";
        } else if (logEntry instanceof SystemEventLog) {
            return "SYSTEM_EVENT";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 获取日志时间戳
     */
    private LocalDateTime getLogTimestamp(Object logEntry) {
        try {
            if (logEntry instanceof PermissionCheckLog) {
                return ((PermissionCheckLog) logEntry).getTimestamp();
            } else if (logEntry instanceof UnauthorizedAccessLog) {
                return ((UnauthorizedAccessLog) logEntry).getTimestamp();
            } else if (logEntry instanceof DataRedactionLog) {
                return ((DataRedactionLog) logEntry).getTimestamp();
            } else if (logEntry instanceof SecurityComplianceService.AuditDecisionLog) {
                return ((SecurityComplianceService.AuditDecisionLog) logEntry).getTimestamp();
            } else if (logEntry instanceof SecurityComplianceService.DataAccessLog) {
                return ((SecurityComplianceService.DataAccessLog) logEntry).getTimestamp();
            } else if (logEntry instanceof SecurityThreatLog) {
                return ((SecurityThreatLog) logEntry).getTimestamp();
            } else if (logEntry instanceof SystemEventLog) {
                return ((SystemEventLog) logEntry).getTimestamp();
            }
        } catch (Exception e) {
            this.log.warn("获取日志时间戳失败: {}", e.getMessage());
        }
        return null;
    }

    // 各种日志实体类
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PermissionCheckLog {
        private LocalDateTime timestamp;
        private String username;
        private String operation;
        private Boolean granted;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UnauthorizedAccessLog {
        private LocalDateTime timestamp;
        private String username;
        private String resource;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DataRedactionLog {
        private LocalDateTime timestamp;
        private String username;
        private Integer originalLength;
        private Integer redactedLength;
        private Double redactionRatio;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecurityThreatLog {
        private LocalDateTime timestamp;
        private String threatType;
        private String details;
        private String severity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SystemEventLog {
        private LocalDateTime timestamp;
        private String eventType;
        private String description;
        private Map<String, Object> context;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditLogStats {
        private Integer totalLogs;
        private Map<String, Long> eventCounts;
        private LocalDateTime lastUpdated;
    }
}