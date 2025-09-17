package com.insurance.audit.rules.infrastructure.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 规则模块性能监控和优化工具
 *
 * @author System
 * @version 1.0.0
 * @since 2024-09-17
 */
@Slf4j
@Component
public class RulePerformanceMonitor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    /**
     * 验证常用查询的性能
     *
     * @return 性能验证结果
     */
    public Map<String, Object> validateQueryPerformance() {
        Map<String, Object> results = new HashMap<>();

        log.info("开始验证规则模块查询性能...");

        // 1. 验证分页列表查询性能
        long listQueryTime = measureQueryTime(() -> {
            return jdbcTemplate.queryForList(
                "SELECT * FROM rule " +
                "WHERE is_deleted = 0 " +
                "  AND audit_status IN ('PENDING_TECH_EVALUATION', 'APPROVED') " +
                "  AND effective_status = 'EFFECTIVE' " +
                "ORDER BY last_updated_at DESC " +
                "LIMIT 20 OFFSET 0"
            );
        });

        // 2. 验证筛选查询性能
        long filterQueryTime = measureQueryTime(() -> {
            return jdbcTemplate.queryForList(
                "SELECT * FROM rule " +
                "WHERE is_deleted = 0 " +
                "  AND rule_type = 'SINGLE' " +
                "  AND manage_department = '技术部门' " +
                "ORDER BY priority DESC, created_at DESC " +
                "LIMIT 20 OFFSET 0"
            );
        });

        // 3. 验证统计查询性能
        long statsQueryTime = measureQueryTime(() -> {
            return jdbcTemplate.queryForList(
                "SELECT audit_status, COUNT(*) as count " +
                "FROM rule " +
                "WHERE is_deleted = 0 " +
                "GROUP BY audit_status"
            );
        });

        // 4. 验证搜索查询性能
        long searchQueryTime = measureQueryTime(() -> {
            return jdbcTemplate.queryForList(
                "SELECT * FROM rule " +
                "WHERE is_deleted = 0 " +
                "  AND (rule_name LIKE '%保险%' OR rule_description LIKE '%保险%') " +
                "ORDER BY last_updated_at DESC " +
                "LIMIT 20 OFFSET 0"
            );
        });

        // 5. 验证联表查询性能（规则详情）
        long joinQueryTime = measureQueryTime(() -> {
            return jdbcTemplate.queryForList(
                "SELECT r.*, rs.rule_description as single_description " +
                "FROM rule r " +
                "LEFT JOIN rule_single rs ON r.id = rs.rule_id " +
                "WHERE r.is_deleted = 0 " +
                "  AND r.rule_type = 'SINGLE' " +
                "LIMIT 10"
            );
        });

        results.put("listQueryTime", listQueryTime);
        results.put("filterQueryTime", filterQueryTime);
        results.put("statsQueryTime", statsQueryTime);
        results.put("searchQueryTime", searchQueryTime);
        results.put("joinQueryTime", joinQueryTime);

        // 性能评估
        results.put("listQueryPass", listQueryTime < 3000); // 3秒
        results.put("filterQueryPass", filterQueryTime < 3000);
        results.put("statsQueryPass", statsQueryTime < 1000); // 1秒
        results.put("searchQueryPass", searchQueryTime < 5000); // 5秒
        results.put("joinQueryPass", joinQueryTime < 2000); // 2秒

        // 总体评估
        boolean overallPass = listQueryTime < 3000 &&
                            filterQueryTime < 3000 &&
                            statsQueryTime < 1000 &&
                            searchQueryTime < 5000 &&
                            joinQueryTime < 2000;

        results.put("overallPass", overallPass);
        results.put("timestamp", new Date());

        log.info("性能验证完成 - 列表查询: {}ms, 筛选查询: {}ms, 统计查询: {}ms, 搜索查询: {}ms, 联表查询: {}ms",
                listQueryTime, filterQueryTime, statsQueryTime, searchQueryTime, joinQueryTime);

        return results;
    }

    /**
     * 分析查询执行计划
     *
     * @return 执行计划分析结果
     */
    public Map<String, Object> analyzeQueryExecutionPlans() {
        Map<String, Object> results = new HashMap<>();

        log.info("开始分析查询执行计划...");

        // 常用查询的执行计划
        String[] queries = {
            "SELECT * FROM rule WHERE is_deleted = 0 AND audit_status = 'APPROVED' ORDER BY last_updated_at DESC LIMIT 20",
            "SELECT * FROM rule WHERE is_deleted = 0 AND rule_type = 'SINGLE' AND manage_department = '技术部门' LIMIT 20",
            "SELECT audit_status, COUNT(*) FROM rule WHERE is_deleted = 0 GROUP BY audit_status",
            "SELECT * FROM rule WHERE is_deleted = 0 AND rule_name LIKE '%保险%' LIMIT 20"
        };

        List<Map<String, Object>> plans = new ArrayList<>();

        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            try {
                List<Map<String, Object>> plan = analyzeQueryPlan(query);
                Map<String, Object> queryResult = new HashMap<>();
                queryResult.put("queryIndex", i + 1);
                queryResult.put("query", query);
                queryResult.put("executionPlan", plan);
                queryResult.put("hasIndexUsage", checkIndexUsage(plan));
                plans.add(queryResult);
            } catch (Exception e) {
                log.error("分析查询执行计划失败: {}", query, e);
            }
        }

        results.put("queryPlans", plans);
        results.put("timestamp", new Date());

        return results;
    }

    /**
     * 获取索引使用情况
     *
     * @return 索引使用统计
     */
    public Map<String, Object> getIndexUsageStatistics() {
        Map<String, Object> results = new HashMap<>();

        try {
            // 获取表的索引信息
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX " +
                "FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "  AND TABLE_NAME LIKE 'rule%' " +
                "ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX"
            );

            results.put("indexes", indexes);

            // 获取表大小信息
            List<Map<String, Object>> tableSizes = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, " +
                "       ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'TABLE_SIZE_MB', " +
                "       TABLE_ROWS " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "  AND TABLE_NAME LIKE 'rule%' " +
                "ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC"
            );

            results.put("tableSizes", tableSizes);

        } catch (Exception e) {
            log.error("获取索引使用情况失败", e);
            results.put("error", e.getMessage());
        }

        results.put("timestamp", new Date());

        return results;
    }

    /**
     * 测量查询执行时间
     *
     * @param queryAction 查询操作
     * @return 执行时间（毫秒）
     */
    private long measureQueryTime(Runnable queryAction) {
        long startTime = System.currentTimeMillis();
        try {
            queryAction.run();
        } catch (Exception e) {
            log.warn("查询执行失败", e);
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 分析单个查询的执行计划
     *
     * @param query SQL查询
     * @return 执行计划
     */
    private List<Map<String, Object>> analyzeQueryPlan(String query) {
        String explainQuery = "EXPLAIN " + query;
        return jdbcTemplate.queryForList(explainQuery);
    }

    /**
     * 检查执行计划中是否使用了索引
     *
     * @param executionPlan 执行计划
     * @return 是否使用索引
     */
    private boolean checkIndexUsage(List<Map<String, Object>> executionPlan) {
        for (Map<String, Object> row : executionPlan) {
            Object key = row.get("key");
            if (key != null && !key.toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成性能报告
     *
     * @return 性能报告
     */
    public Map<String, Object> generatePerformanceReport() {
        Map<String, Object> report = new HashMap<>();

        try {
            // 查询性能验证
            Map<String, Object> queryPerformance = validateQueryPerformance();
            report.put("queryPerformance", queryPerformance);

            // 执行计划分析
            Map<String, Object> executionPlans = analyzeQueryExecutionPlans();
            report.put("executionPlans", executionPlans);

            // 索引使用情况
            Map<String, Object> indexUsage = getIndexUsageStatistics();
            report.put("indexUsage", indexUsage);

            // 生成建议
            List<String> recommendations = generateRecommendations(queryPerformance, executionPlans);
            report.put("recommendations", recommendations);

            report.put("reportTime", new Date());
            report.put("status", "SUCCESS");

            log.info("性能报告生成完成");

        } catch (Exception e) {
            log.error("生成性能报告失败", e);
            report.put("status", "ERROR");
            report.put("error", e.getMessage());
        }

        return report;
    }

    /**
     * 根据性能测试结果生成优化建议
     *
     * @param queryPerformance 查询性能结果
     * @param executionPlans 执行计划结果
     * @return 优化建议列表
     */
    private List<String> generateRecommendations(Map<String, Object> queryPerformance,
                                                Map<String, Object> executionPlans) {
        List<String> recommendations = new ArrayList<>();

        // 检查查询性能
        if (!(Boolean) queryPerformance.get("listQueryPass")) {
            recommendations.add("列表查询性能不达标，建议检查 idx_rule_composite_status 索引是否生效");
        }

        if (!(Boolean) queryPerformance.get("filterQueryPass")) {
            recommendations.add("筛选查询性能不达标，建议检查 idx_rule_composite_type 和 idx_rule_composite_dept 索引");
        }

        if (!(Boolean) queryPerformance.get("searchQueryPass")) {
            recommendations.add("搜索查询性能不达标，建议考虑使用全文索引或 Elasticsearch");
        }

        // 检查执行计划
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> plans = (List<Map<String, Object>>) executionPlans.get("queryPlans");
        if (plans != null) {
            for (Map<String, Object> plan : plans) {
                if (!(Boolean) plan.get("hasIndexUsage")) {
                    recommendations.add("查询 " + plan.get("queryIndex") + " 未使用索引，需要优化");
                }
            }
        }

        // 通用建议
        if (recommendations.isEmpty()) {
            recommendations.add("查询性能良好，建议定期监控和维护");
        }

        recommendations.add("建议定期执行 ANALYZE TABLE 更新统计信息");
        recommendations.add("建议配置 MySQL 慢查询日志监控性能");

        return recommendations;
    }
}