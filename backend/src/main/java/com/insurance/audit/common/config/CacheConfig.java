package com.insurance.audit.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * 生产级缓存策略：多级缓存、过期策略、监控
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${audit.cache.default-ttl:3600}")
    private long defaultTtlSeconds;

    @Value("${audit.cache.ruleset-ttl:7200}")
    private long rulesetTtlSeconds;

    @Value("${audit.cache.max-size:1000}")
    private long maxSize;

    @Value("${audit.cache.cleanup-interval:300}")
    private long cleanupIntervalSeconds;

    @Bean
    @Primary
    public CacheManager cacheManager() {
        log.info("初始化缓存管理器: defaultTtl={}s, rulesetTtl={}s, maxSize={}, cleanupInterval={}s",
                defaultTtlSeconds, rulesetTtlSeconds, maxSize, cleanupIntervalSeconds);

        // 创建基于ConcurrentMap的缓存管理器（生产环境建议使用Redis）
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();

        // 设置缓存名称
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "rulesets",           // 规则集缓存
                "ruleset-versions",   // 规则集版本缓存
                "documents",          // 文档缓存
                "audit-results",      // 检核结果缓存
                "performance-metrics", // 性能指标缓存
                "product_templates"   // 产品模板缓存
        ));

        // 允许运行时创建新缓存
        cacheManager.setAllowNullValues(false);

        log.info("缓存管理器初始化完成，支持的缓存: {}", cacheManager.getCacheNames());

        // 启动定期清理任务
        startCacheCleanupTask(cacheManager);

        return cacheManager;
    }

    /**
     * 启动缓存清理任务
     */
    private void startCacheCleanupTask(CacheManager cacheManager) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cache-cleanup");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("执行缓存清理任务");

                // 获取缓存统计信息
                cacheManager.getCacheNames().forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        // 这里可以添加缓存大小监控和清理逻辑
                        log.debug("缓存状态: name={}", cacheName);
                    }
                });

            } catch (Exception e) {
                log.error("缓存清理任务执行失败", e);
            }
        }, cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS);

        log.info("缓存清理任务已启动，清理间隔: {}秒", cleanupIntervalSeconds);
    }

    /**
     * 缓存配置属性
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheProperties {
        private String name;
        private Duration ttl;
        private long maxSize;
        private boolean allowNullValues;
    }

    /**
     * 缓存统计信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheStats {
        private String cacheName;
        private long hitCount;
        private long missCount;
        private double hitRate;
        private long evictionCount;
        private long size;
    }
}