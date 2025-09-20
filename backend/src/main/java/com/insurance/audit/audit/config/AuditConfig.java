package com.insurance.audit.audit.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 审计模块配置
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableCaching
public class AuditConfig {

    /**
     * HTTP客户端配置
     */
    @Bean
    public RestTemplate auditRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 可以在这里配置超时、拦截器等
        // TODO: 添加超时配置
        // TODO: 添加请求/响应拦截器用于日志记录

        return restTemplate;
    }
}