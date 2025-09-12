package com.insurance.audit.product.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 产品管理模块配置
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-09-12
 */
@Slf4j
@Configuration
public class ProductModuleConfig {

    public ProductModuleConfig() {
        log.info("产品管理模块配置初始化完成");
    }
}