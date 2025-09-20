package com.insurance.audit.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 性能监控服务
 * 收集和监控系统性能指标
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class PerformanceMonitor {

    @Autowired
    private ConcurrencyController concurrencyController;

    // 性能计数器
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();

    // 响应时间统计
    private final ConcurrentHashMap<String, ResponseTimeStats> responseTimeStats = new ConcurrentHashMap<>();

    // 资源使用统计
    private final AtomicLong totalTokensUsed = new AtomicLong(0);
    private final AtomicLong totalApiCalls = new AtomicLong(0);
    private final AtomicLong totalCacheHits = new AtomicLong(0);
    private final AtomicLong totalCacheMisses = new AtomicLong(0);

    /**
     * 记录请求开始
     */
    public void recordRequestStart(String operation) {
        totalRequests.increment();

        ResponseTimeStats stats = responseTimeStats.computeIfAbsent(operation,
            k -> new ResponseTimeStats());
        stats.incrementActive();

        log.debug("记录请求开始: operation={}, totalRequests={}",
                operation, totalRequests.sum());
    }

    /**
     * 记录请求完成
     */
    public void recordRequestComplete(String operation, long durationMs, boolean success) {
        if (success) {
            successfulRequests.increment();
        } else {
            failedRequests.increment();
        }

        ResponseTimeStats stats = responseTimeStats.get(operation);
        if (stats != null) {
            stats.recordResponse(durationMs, success);
        }

        log.debug("记录请求完成: operation={}, duration={}ms, success={}",
                operation, durationMs, success);
    }

    /**
     * 记录Token使用
     */
    public void recordTokenUsage(long tokens) {
        totalTokensUsed.addAndGet(tokens);
        log.debug("记录Token使用: tokens={}, total={}", tokens, totalTokensUsed.get());
    }

    /**
     * 记录API调用
     */
    public void recordApiCall(String service) {
        totalApiCalls.incrementAndGet();
        log.debug("记录API调用: service={}, total={}", service, totalApiCalls.get());
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheKey) {
        totalCacheHits.incrementAndGet();
        log.debug("记录缓存命中: key={}, hits={}", cacheKey, totalCacheHits.get());
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheKey) {
        totalCacheMisses.incrementAndGet();
        log.debug("记录缓存未命中: key={}, misses={}", cacheKey, totalCacheMisses.get());
    }

    /**
     * 获取性能报告
     */
    public PerformanceReport getPerformanceReport() {
        // 获取并发状态
        ConcurrencyController.ExecutorStatus executorStatus = concurrencyController.getExecutorStatus();

        // 计算总体指标
        long total = totalRequests.sum();
        long successful = successfulRequests.sum();
        long failed = failedRequests.sum();
        double successRate = total > 0 ? (double) successful / total * 100 : 0.0;

        // 计算缓存命中率
        long hits = totalCacheHits.get();
        long misses = totalCacheMisses.get();
        double cacheHitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0.0;

        return PerformanceReport.builder()
                .timestamp(LocalDateTime.now())
                .totalRequests(total)
                .successfulRequests(successful)
                .failedRequests(failed)
                .successRate(successRate)
                .responseTimeStats(new java.util.HashMap<>(responseTimeStats))
                .totalTokensUsed(totalTokensUsed.get())
                .totalApiCalls(totalApiCalls.get())
                .cacheHitRate(cacheHitRate)
                .executorStatus(executorStatus)
                .build();
    }

    /**
     * 获取操作的P95响应时间
     */
    public long getP95ResponseTime(String operation) {
        ResponseTimeStats stats = responseTimeStats.get(operation);
        return stats != null ? stats.getP95() : 0L;
    }

    /**
     * 检查性能是否达标
     */
    public boolean isPerformanceHealthy() {
        PerformanceReport report = getPerformanceReport();

        // 检查成功率 (>95%)
        if (report.getSuccessRate() < 95.0) {
            log.warn("成功率过低: {}%", report.getSuccessRate());
            return false;
        }

        // 检查关键操作的P95响应时间
        for (String operation : java.util.List.of("match", "parse", "export")) {
            long p95 = getP95ResponseTime(operation);
            if (p95 > getTargetP95(operation)) {
                log.warn("操作{}的P95响应时间过高: {}ms", operation, p95);
                return false;
            }
        }

        // 检查队列积压
        if (report.getExecutorStatus().getQueueSize() >
            report.getExecutorStatus().getQueueCapacity() * 0.8) {
            log.warn("任务队列积压过多");
            return false;
        }

        return true;
    }

    /**
     * 获取目标P95响应时间
     */
    private long getTargetP95(String operation) {
        switch (operation) {
            case "match":
                return 2000L; // 2秒
            case "parse":
                return 5000L; // 5秒
            case "export":
                return 10000L; // 10秒
            default:
                return 3000L; // 默认3秒
        }
    }

    /**
     * 重置统计数据
     */
    public void resetStats() {
        log.info("重置性能统计数据");

        totalRequests.reset();
        successfulRequests.reset();
        failedRequests.reset();
        totalTokensUsed.set(0);
        totalApiCalls.set(0);
        totalCacheHits.set(0);
        totalCacheMisses.set(0);
        responseTimeStats.clear();
    }

    /**
     * 响应时间统计
     */
    private static class ResponseTimeStats {
        private final LongAdder totalRequests = new LongAdder();
        private final LongAdder activeRequests = new LongAdder();
        private final LongAdder successfulRequests = new LongAdder();
        private final LongAdder totalDuration = new LongAdder();

        // 简化的百分位数计算（实际应该使用更精确的算法）
        private final java.util.concurrent.ConcurrentLinkedQueue<Long> recentDurations =
            new java.util.concurrent.ConcurrentLinkedQueue<>();

        void incrementActive() {
            activeRequests.increment();
        }

        void recordResponse(long durationMs, boolean success) {
            totalRequests.increment();
            activeRequests.decrement();
            totalDuration.add(durationMs);

            if (success) {
                successfulRequests.increment();
            }

            // 保留最近1000个响应时间用于百分位数计算
            recentDurations.offer(durationMs);
            while (recentDurations.size() > 1000) {
                recentDurations.poll();
            }
        }

        double getAverageResponseTime() {
            long total = totalRequests.sum();
            return total > 0 ? (double) totalDuration.sum() / total : 0.0;
        }

        long getP95() {
            java.util.List<Long> durations = new java.util.ArrayList<>(recentDurations);
            if (durations.isEmpty()) {
                return 0L;
            }

            durations.sort(Long::compareTo);
            int index = (int) Math.ceil(durations.size() * 0.95) - 1;
            return durations.get(Math.max(0, index));
        }

        public long getTotalRequests() { return totalRequests.sum(); }
        public long getActiveRequests() { return activeRequests.sum(); }
        public long getSuccessfulRequests() { return successfulRequests.sum(); }
    }

    /**
     * 性能报告
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PerformanceReport {
        private LocalDateTime timestamp;
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private java.util.Map<String, ResponseTimeStats> responseTimeStats;
        private Long totalTokensUsed;
        private Long totalApiCalls;
        private Double cacheHitRate;
        private ConcurrencyController.ExecutorStatus executorStatus;
    }
}