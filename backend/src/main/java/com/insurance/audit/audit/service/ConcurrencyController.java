package com.insurance.audit.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发控制器
 * 管理检核任务的并发执行、队列处理和资源限制
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

    private final ThreadPoolExecutor executor;
    private final Semaphore resourceSemaphore;
    private final AtomicInteger activeJobs = new AtomicInteger(0);

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
    }

    /**
     * 提交任务执行
     */
    public <T> CompletableFuture<T> submitTask(Callable<T> task, String taskId) {
        log.debug("提交任务: taskId={}, activeJobs={}, queueSize={}",
                taskId, activeJobs.get(), executor.getQueue().size());

        // 检查队列是否已满
        if (executor.getQueue().size() >= queueCapacity * 0.9) {
            log.warn("任务队列接近满载: queueSize={}, capacity={}", executor.getQueue().size(), queueCapacity);
        }

        CompletableFuture<T> future = new CompletableFuture<>();

        executor.submit(() -> {
            activeJobs.incrementAndGet();
            try {
                long startTime = System.currentTimeMillis();
                T result = task.call();
                long duration = System.currentTimeMillis() - startTime;

                log.debug("任务完成: taskId={}, duration={}ms", taskId, duration);
                future.complete(result);

            } catch (Exception e) {
                log.error("任务执行失败: taskId={}, error={}", taskId, e.getMessage(), e);
                future.completeExceptionally(e);
            } finally {
                activeJobs.decrementAndGet();
            }
        });

        return future;
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
     * 获取执行器状态
     */
    public ExecutorStatus getExecutorStatus() {
        return ExecutorStatus.builder()
                .activeThreads(executor.getActiveCount())
                .poolSize(executor.getPoolSize())
                .maxPoolSize(executor.getMaximumPoolSize())
                .queueSize(executor.getQueue().size())
                .queueCapacity(queueCapacity)
                .completedTasks(executor.getCompletedTaskCount())
                .activeJobs(activeJobs.get())
                .availableResourcePermits(resourceSemaphore.availablePermits())
                .build();
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
    }
}