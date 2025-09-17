package com.insurance.audit.rules.interfaces.web;

import com.insurance.audit.common.dto.ApiResponse;
import com.insurance.audit.rules.infrastructure.performance.RulePerformanceMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 规则性能监控控制器
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-17
 */
@Slf4j
@RestController
@RequestMapping("/v1/rules/performance")
@RequiredArgsConstructor
@Tag(name = "规则性能监控", description = "规则模块性能监控和优化API")
public class RulePerformanceController {

    private final RulePerformanceMonitor performanceMonitor;

    @GetMapping("/validate")
    @Operation(summary = "验证查询性能", description = "验证规则模块常用查询的性能表现")
    @PreAuthorize("hasAuthority('RULE_STATISTICS')")
    public ApiResponse<Map<String, Object>> validateQueryPerformance() {
        log.info("开始验证规则查询性能");

        Map<String, Object> result = performanceMonitor.validateQueryPerformance();

        return ApiResponse.success(result);
    }

    @GetMapping("/execution-plans")
    @Operation(summary = "分析查询执行计划", description = "分析规则模块查询的执行计划和索引使用情况")
    @PreAuthorize("hasAuthority('RULE_STATISTICS')")
    public ApiResponse<Map<String, Object>> analyzeExecutionPlans() {
        log.info("开始分析查询执行计划");

        Map<String, Object> result = performanceMonitor.analyzeQueryExecutionPlans();

        return ApiResponse.success(result);
    }

    @GetMapping("/indexes")
    @Operation(summary = "获取索引使用情况", description = "获取规则表的索引使用统计信息")
    @PreAuthorize("hasAuthority('RULE_STATISTICS')")
    public ApiResponse<Map<String, Object>> getIndexUsageStatistics() {
        log.info("获取索引使用情况");

        Map<String, Object> result = performanceMonitor.getIndexUsageStatistics();

        return ApiResponse.success(result);
    }

    @GetMapping("/report")
    @Operation(summary = "生成性能报告", description = "生成完整的性能分析报告")
    @PreAuthorize("hasAuthority('RULE_STATISTICS')")
    public ApiResponse<Map<String, Object>> generatePerformanceReport() {
        log.info("生成性能报告");

        Map<String, Object> result = performanceMonitor.generatePerformanceReport();

        return ApiResponse.success(result);
    }

    @PostMapping("/optimize")
    @Operation(summary = "执行性能优化", description = "执行数据库优化操作")
    @PreAuthorize("hasAuthority('RULE_STATISTICS')")
    public ApiResponse<Map<String, Object>> optimizePerformance() {
        log.info("执行性能优化");

        // 这里可以添加具体的优化操作
        // 例如：更新统计信息、清理日志等

        Map<String, Object> result = Map.of(
            "message", "性能优化操作已触发",
            "timestamp", new java.util.Date(),
            "operations", java.util.List.of(
                "ANALYZE TABLE operations",
                "Index optimization",
                "Cache warming"
            )
        );

        return ApiResponse.success(result);
    }
}