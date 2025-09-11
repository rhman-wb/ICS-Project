package com.insurance.audit.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/v1/health")
public class HealthController {
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Autowired(required = false)
    private BuildProperties buildProperties;
    
    /**
     * 基础健康检查
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "user-auth-system");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * 详细健康检查
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "user-auth-system");
        
        // 应用信息
        Map<String, Object> app = new HashMap<>();
        if (buildProperties != null) {
            app.put("name", buildProperties.getName());
            app.put("version", buildProperties.getVersion());
            app.put("time", buildProperties.getTime());
        } else {
            app.put("name", "user-auth-system");
            app.put("version", "1.0.0-dev");
            app.put("time", LocalDateTime.now());
        }
        health.put("app", app);
        
        // 数据库连接检查
        Map<String, Object> database = new HashMap<>();
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                database.put("status", "UP");
                database.put("database", connection.getMetaData().getDatabaseProductName());
                database.put("version", connection.getMetaData().getDatabaseProductVersion());
                database.put("url", connection.getMetaData().getURL());
            } catch (Exception e) {
                database.put("status", "DOWN");
                database.put("error", e.getMessage());
            }
        } else {
            database.put("status", "NOT_CONFIGURED");
        }
        health.put("database", database);
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        system.put("processors", runtime.availableProcessors());
        system.put("memory", Map.of(
            "total", runtime.totalMemory(),
            "free", runtime.freeMemory(),
            "max", runtime.maxMemory(),
            "used", runtime.totalMemory() - runtime.freeMemory()
        ));
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        health.put("system", system);
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * 就绪检查
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> readiness = new HashMap<>();
        boolean isReady = true;
        
        // 检查数据库连接
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                readiness.put("database", "UP");
            } catch (Exception e) {
                readiness.put("database", "DOWN");
                readiness.put("databaseError", e.getMessage());
                isReady = false;
            }
        } else {
            readiness.put("database", "NOT_CONFIGURED");
        }
        
        readiness.put("status", isReady ? "READY" : "NOT_READY");
        readiness.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(readiness);
    }
    
    /**
     * 存活检查
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now());
        liveness.put("uptime", System.currentTimeMillis());
        
        return ResponseEntity.ok(liveness);
    }
}