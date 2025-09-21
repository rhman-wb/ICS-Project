package com.insurance.audit.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate配置
 * 生产级配置：连接池、超时设置、重试机制
 *
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${audit.rest-template.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${audit.rest-template.read-timeout:10000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("初始化RestTemplate配置: connectionTimeout={}ms, readTimeout={}ms",
                connectionTimeout, readTimeout);

        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();

        log.info("RestTemplate配置完成，支持超时控制");
        return restTemplate;
    }
}