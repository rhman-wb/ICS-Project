package com.insurance.audit.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 并发控制器
 * 管理检核任务的并发执行、队列处理和资源限制
 * 增强版：支持熔断机制、性能监控、动态调整
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
public class ConcurrencyController {

    @Value("${audit.concurrency.max-threads:10}")
    private int maxThreads;

    @Value("${audit.concurrency.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${audit.concurrency.keep-alive-seconds:60}")
    private long keepAliveSeconds;

    @Value("${audit.concurrency.circuit-breaker.failure-threshold:5}")
    private int failureThreshold;

    @Value("${audit.concurrency.circuit-breaker.recovery-timeout:30000}")
    private long recoveryTimeoutMs;

    @Value("${audit.concurrency.performance.slow-task-threshold:5000}")
    private long slowTaskThresholdMs;

    private final ThreadPoolExecutor executor;
    private final Semaphore resourceSemaphore;
    private final AtomicInteger activeJobs = new AtomicInteger(0);

    // 熔断器状态
    private volatile CircuitBreakerState circuitBreakerState = CircuitBreakerState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;

    // 性能监控
    private final AtomicLong totalExecutedTasks = new AtomicLong(0);
    private final AtomicLong totalFailedTasks = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong slowTaskCount = new AtomicLong(0);

    public ConcurrencyController() {
        // 创建线程池
        this.executor = new ThreadPoolExecutor(
            2, // corePoolSize
            10, // maximumPoolSize (will be updated from config)
            60L, // keepAliveTime
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), // workQueue
            new CustomThreadFactory("audit-worker"),
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );

        // 资源信号量，控制同时访问外部服务的数量
        this.resourceSemaphore = new Semaphore(5);

        log.info("并发控制器初始化完成: maxThreads={}, queueCapacity={}, failureThreshold={}",
                maxThreads, queueCapacity, failureThreshold);
    }

    /**
     * 提交任务执行（带熔断机制）
     */
    public <T> CompletableFuture<T> submitTask(Callable<T> task, String taskId) {
        // 检查熔断器状态
        if (!isCircuitBreakerClosed()) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("熔断器开启，拒绝执行任务: " + taskId));
            return future;
        }

        log.debug("提交任务: taskId={}, activeJobs={}, queueSize={}, circuitState={}",
                taskId, activeJobs.get(), executor.getQueue().size(), circuitBreakerState);

        // 检查队列是否已满
        if (executor.getQueue().size() >= queueCapacity * 0.9) {
            log.warn("任务队列接近满载: queueSize={}, capacity={}", executor.getQueue().size(), queueCapacity);
        }

        CompletableFuture<T> future = new CompletableFuture<>();

        executor.submit(() -> {
            activeJobs.incrementAndGet();
            totalExecutedTasks.incrementAndGet();
            long startTime = System.currentTimeMillis();

            try {
                T result = task.call();
                long duration = System.currentTimeMillis() - startTime;
                totalExecutionTime.addAndGet(duration);

                // 检查是否为慢任务
                if (duration > slowTaskThresholdMs) {
                    slowTaskCount.incrementAndGet();
                    log.warn("检测到慢任务: taskId={}, duration={}ms", taskId, duration);
                }

                log.debug("任务完成: taskId={}, duration={}ms", taskId, duration);
                future.complete(result);

                // 成功执行，尝试重置熔断器
                onTaskSuccess();

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                totalFailedTasks.incrementAndGet();

                log.error("任务执行失败: taskId={}, duration={}ms, error={}", taskId, duration, e.getMessage(), e);
                future.completeExceptionally(e);

                // 任务失败，更新熔断器状态
                onTaskFailure();
            } finally {
                activeJobs.decrementAndGet();
            }
        });

        return future;
    }

    /**
     * 检查熔断器是否关闭
     */
    private boolean isCircuitBreakerClosed() {
        switch (circuitBreakerState) {
            case CLOSED:
                return true;
            case OPEN:
                // 检查是否可以尝试恢复
                if (System.currentTimeMillis() - lastFailureTime > recoveryTimeoutMs) {
                    circuitBreakerState = CircuitBreakerState.HALF_OPEN;
                    log.info("熔断器状态变更: OPEN -> HALF_OPEN，尝试恢复");
                    return true;
                }
                return false;
            case HALF_OPEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * 任务成功回调
     */
    private void onTaskSuccess() {
        if (circuitBreakerState == CircuitBreakerState.HALF_OPEN) {
            circuitBreakerState = CircuitBreakerState.CLOSED;
            failureCount.set(0);
            log.info("熔断器状态变更: HALF_OPEN -> CLOSED，恢复正常");
        }
    }

    /**
     * 任务失败回调
     */
    private void onTaskFailure() {
        int currentFailures = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();

        if (currentFailures >= failureThreshold) {
            if (circuitBreakerState != CircuitBreakerState.OPEN) {
                circuitBreakerState = CircuitBreakerState.OPEN;
                log.warn("熔断器状态变更: {} -> OPEN，失败次数达到阈值: {}/{}",
                        circuitBreakerState, currentFailures, failureThreshold);
            }
        }
    }

    /**
     * 批量提交任务
     */
    public <T> CompletableFuture<Void> submitBatchTasks(java.util.List<Callable<T>> tasks, String batchId) {
        log.info("提交批量任务: batchId={}, taskCount={}", batchId, tasks.size());

        java.util.List<CompletableFuture<T>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            String taskId = batchId + "-" + i;
            CompletableFuture<T> future = submitTask(tasks.get(i), taskId);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * 获取资源访问许可
     */
    public boolean acquireResource(long timeoutMs) {
        try {
            return resourceSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("获取资源许可被中断");
            return false;
        }
    }

    /**
     * 释放资源访问许可
     */
    public void releaseResource() {
        resourceSemaphore.release();
    }

    /**
     * 获取执行器状态（增强版）
     */
    public ExecutorStatus getExecutorStatus() {
        long executedTasks = totalExecutedTasks.get();
        long totalTime = totalExecutionTime.get();
        double avgExecutionTime = executedTasks > 0 ? (double) totalTime / executedTasks : 0.0;
        double failureRate = executedTasks > 0 ? (double) totalFailedTasks.get() / executedTasks : 0.0;

        return ExecutorStatus.builder()
                .activeThreads(executor.getActiveCount())
                .poolSize(executor.getPoolSize())
                .maxPoolSize(executor.getMaximumPoolSize())
                .queueSize(executor.getQueue().size())
                .queueCapacity(queueCapacity)
                .completedTasks(executor.getCompletedTaskCount())
                .activeJobs(activeJobs.get())
                .availableResourcePermits(resourceSemaphore.availablePermits())
                // 新增性能指标
                .circuitBreakerState(circuitBreakerState.name())
                .failureCount(failureCount.get())
                .totalExecutedTasks(executedTasks)
                .totalFailedTasks(totalFailedTasks.get())
                .avgExecutionTimeMs(avgExecutionTime)
                .failureRate(failureRate)
                .slowTaskCount(slowTaskCount.get())
                .build();
    }

    /**
     * 获取性能统计信息
     */
    public PerformanceMetrics getPerformanceMetrics() {
        long executedTasks = totalExecutedTasks.get();
        long totalTime = totalExecutionTime.get();
        double avgExecutionTime = executedTasks > 0 ? (double) totalTime / executedTasks : 0.0;
        double failureRate = executedTasks > 0 ? (double) totalFailedTasks.get() / executedTasks : 0.0;
        double slowTaskRate = executedTasks > 0 ? (double) slowTaskCount.get() / executedTasks : 0.0;

        return PerformanceMetrics.builder()
                .totalExecutedTasks(executedTasks)
                .totalFailedTasks(totalFailedTasks.get())
                .totalExecutionTimeMs(totalTime)
                .avgExecutionTimeMs(avgExecutionTime)
                .failureRate(failureRate)
                .slowTaskCount(slowTaskCount.get())
                .slowTaskRate(slowTaskRate)
                .circuitBreakerState(circuitBreakerState.name())
                .failureThreshold(failureThreshold)
                .currentFailureCount(failureCount.get())
                .build();
    }

    /**
     * 重置性能统计
     */
    public void resetPerformanceMetrics() {
        totalExecutedTasks.set(0);
        totalFailedTasks.set(0);
        totalExecutionTime.set(0);
        slowTaskCount.set(0);
        failureCount.set(0);
        circuitBreakerState = CircuitBreakerState.CLOSED;
        log.info("性能统计已重置");
    }

    /**
     * 手动重置熔断器
     */
    public void resetCircuitBreaker() {
        circuitBreakerState = CircuitBreakerState.CLOSED;
        failureCount.set(0);
        log.info("熔断器手动重置为CLOSED状态");
    }

    /**
     * 调整并发参数
     */
    public void adjustConcurrency(int newMaxThreads, int newQueueCapacity) {
        log.info("调整并发参数: maxThreads={}->{}, queueCapacity={}->{}",
                executor.getMaximumPoolSize(), newMaxThreads, this.queueCapacity, newQueueCapacity);

        executor.setMaximumPoolSize(newMaxThreads);
        this.queueCapacity = newQueueCapacity;
    }

    /**
     * 优雅关闭
     */
    public void shutdown() {
        log.info("开始关闭并发控制器");

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("等待任务完成超时，强制关闭");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }

        log.info("并发控制器已关闭");
    }

    /**
     * 自定义线程工厂
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 执行器状态
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ExecutorStatus {
        private Integer activeThreads;
        private Integer poolSize;
        private Integer maxPoolSize;
        private Integer queueSize;
        private Integer queueCapacity;
        private Long completedTasks;
        private Integer activeJobs;
        private Integer availableResourcePermits;
        // 新增性能指标
        private String circuitBreakerState;
        private Integer failureCount;
        private Long totalExecutedTasks;
        private Long totalFailedTasks;
        private Double avgExecutionTimeMs;
        private Double failureRate;
        private Long slowTaskCount;
    }

    /**
     * 性能指标
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PerformanceMetrics {
        private Long totalExecutedTasks;
        private Long totalFailedTasks;
        private Long totalExecutionTimeMs;
        private Double avgExecutionTimeMs;
        private Double failureRate;
        private Long slowTaskCount;
        private Double slowTaskRate;
        private String circuitBreakerState;
        private Integer failureThreshold;
        private Integer currentFailureCount;
    }

    /**
     * 熔断器状态枚举
     */
    public enum CircuitBreakerState {
        CLOSED,   // 关闭状态，正常工作
        OPEN,     // 开启状态，拒绝所有请求
        HALF_OPEN // 半开状态，允许部分请求通过以测试服务恢复
    }
}