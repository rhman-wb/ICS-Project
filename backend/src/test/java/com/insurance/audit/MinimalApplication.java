package com.insurance.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 最小化启动类 - 用于排查问题
 * 排除了Security、Redis等组件，便于调试基础功能
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
public class MinimalApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MinimalApplication.class, args);
    }
}