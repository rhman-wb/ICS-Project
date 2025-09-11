package com.insurance.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试应用 - 最简单的Spring Boot应用
 * 用于快速验证Spring Boot基础功能
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication(scanBasePackages = "com.insurance.audit.test")
@RestController
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
    
    @GetMapping("/test")
    public String test() {
        return "Hello World! Spring Boot is working!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}