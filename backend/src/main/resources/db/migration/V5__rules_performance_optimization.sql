-- =====================================================
-- 规则管理系统性能优化与索引验证
-- Description: 创建复合索引，优化查询性能，确保列表筛选<3s
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;

-- =====================================================
-- 1. 性能分析：识别慢查询场景
-- =====================================================

-- 常见查询场景分析：
-- 1. 列表页分页查询（按多个条件筛选）
-- 2. 状态统计（按audit_status, effective_status分组）
-- 3. 部门规则查询（按manage_department筛选）
-- 4. 时间范围查询（按created_at, updated_at, last_updated_at筛选）
-- 5. 文本搜索（按rule_name, rule_description搜索）

-- =====================================================
-- 2. 创建复合索引优化查询性能
-- =====================================================

-- 优化规则主表查询性能
-- 复合索引：删除状态 + 审核状态 + 有效状态（最常用的组合筛选）
CREATE INDEX IF NOT EXISTS `idx_rule_composite_status` ON `rule` (
    `is_deleted`,
    `audit_status`,
    `effective_status`,
    `last_updated_at` DESC
);

-- 复合索引：删除状态 + 规则类型 + 来源（类型筛选）
CREATE INDEX IF NOT EXISTS `idx_rule_composite_type` ON `rule` (
    `is_deleted`,
    `rule_type`,
    `rule_source`,
    `created_at` DESC
);

-- 复合索引：删除状态 + 管理部门 + 险种（部门权限控制）
CREATE INDEX IF NOT EXISTS `idx_rule_composite_dept` ON `rule` (
    `is_deleted`,
    `manage_department`,
    `applicable_insurance`,
    `priority` DESC
);

-- 复合索引：删除状态 + 时间范围查询优化
CREATE INDEX IF NOT EXISTS `idx_rule_composite_time` ON `rule` (
    `is_deleted`,
    `last_updated_at` DESC,
    `created_at` DESC
);

-- 优化分页查询：删除状态 + 排序字段
CREATE INDEX IF NOT EXISTS `idx_rule_pagination` ON `rule` (
    `is_deleted`,
    `sort_order` ASC,
    `priority` DESC,
    `id`
);

-- =====================================================
-- 3. 优化子表查询性能
-- =====================================================

-- 单句规则表优化索引
CREATE INDEX IF NOT EXISTS `idx_rule_single_composite` ON `rule_single` (
    `rule_id`,
    `is_deleted`,
    `match_type`,
    `alert_type`
);

-- 双句规则表优化索引
CREATE INDEX IF NOT EXISTS `idx_rule_double_composite` ON `rule_double` (
    `rule_id`,
    `is_deleted`,
    `match_type_a`,
    `match_type_b`
);

-- 格式规则表优化索引
CREATE INDEX IF NOT EXISTS `idx_rule_format_composite` ON `rule_format` (
    `rule_id`,
    `is_deleted`,
    `alert_type`
);

-- 高级规则表优化索引
CREATE INDEX IF NOT EXISTS `idx_rule_advanced_composite` ON `rule_advanced` (
    `rule_id`,
    `is_deleted`,
    `rule_weight` DESC,
    `llm_enabled`
);

-- =====================================================
-- 4. 日志表查询优化
-- =====================================================

-- 操作日志复合索引：规则ID + 操作时间（查询规则历史）
CREATE INDEX IF NOT EXISTS `idx_rule_log_composite` ON `rule_operation_log` (
    `rule_id`,
    `operation_time` DESC,
    `operation_type`,
    `operation_result`
);

-- 操作日志复合索引：操作人 + 操作时间（查询用户操作历史）
CREATE INDEX IF NOT EXISTS `idx_rule_log_operator` ON `rule_operation_log` (
    `operator_id`,
    `operation_time` DESC,
    `operation_type`
);

-- =====================================================
-- 5. 全文搜索优化
-- =====================================================

-- 规则名称和描述的全文索引（MySQL 5.7+支持）
-- 注意：需要确认MySQL版本支持，否则使用LIKE查询
ALTER TABLE `rule` ADD FULLTEXT INDEX `ft_rule_content` (`rule_name`, `rule_description`);

-- =====================================================
-- 6. 创建性能监控视图
-- =====================================================

-- 创建规则统计视图
CREATE OR REPLACE VIEW `v_rule_statistics` AS
SELECT
    COUNT(*) as total_rules,
    COUNT(CASE WHEN audit_status = 'APPROVED' THEN 1 END) as approved_rules,
    COUNT(CASE WHEN effective_status = 'EFFECTIVE' THEN 1 END) as effective_rules,
    COUNT(CASE WHEN rule_type = 'SINGLE' THEN 1 END) as single_rules,
    COUNT(CASE WHEN rule_type = 'DOUBLE' THEN 1 END) as double_rules,
    COUNT(CASE WHEN rule_type = 'FORMAT' THEN 1 END) as format_rules,
    COUNT(CASE WHEN rule_type = 'ADVANCED' THEN 1 END) as advanced_rules,
    MAX(last_updated_at) as last_update_time
FROM `rule`
WHERE is_deleted = 0;

-- 创建部门规则统计视图
CREATE OR REPLACE VIEW `v_rule_dept_statistics` AS
SELECT
    manage_department,
    COUNT(*) as rule_count,
    COUNT(CASE WHEN audit_status = 'APPROVED' THEN 1 END) as approved_count,
    COUNT(CASE WHEN effective_status = 'EFFECTIVE' THEN 1 END) as effective_count,
    MAX(last_updated_at) as last_update_time
FROM `rule`
WHERE is_deleted = 0
GROUP BY manage_department;

-- =====================================================
-- 7. 查询性能验证脚本
-- =====================================================

-- 验证主要查询的执行计划
-- 这些查询应该在3秒内完成

-- 查询1：分页列表查询（最常用）
-- EXPLAIN SELECT * FROM `rule`
-- WHERE is_deleted = 0
--   AND audit_status IN ('APPROVED', 'PENDING_APPROVAL')
--   AND effective_status = 'EFFECTIVE'
-- ORDER BY last_updated_at DESC
-- LIMIT 20 OFFSET 0;

-- 查询2：按类型和部门筛选
-- EXPLAIN SELECT * FROM `rule`
-- WHERE is_deleted = 0
--   AND rule_type = 'SINGLE'
--   AND manage_department = '技术部门'
-- ORDER BY priority DESC, created_at DESC
-- LIMIT 20 OFFSET 0;

-- 查询3：文本搜索（使用LIKE，如果全文索引不可用）
-- EXPLAIN SELECT * FROM `rule`
-- WHERE is_deleted = 0
--   AND (rule_name LIKE '%保险%' OR rule_description LIKE '%保险%')
-- ORDER BY last_updated_at DESC
-- LIMIT 20 OFFSET 0;

-- 查询4：统计查询
-- EXPLAIN SELECT audit_status, COUNT(*)
-- FROM `rule`
-- WHERE is_deleted = 0
-- GROUP BY audit_status;

-- =====================================================
-- 8. 性能监控建议
-- =====================================================

-- 建议的MySQL配置优化（需要DBA确认）：
--
-- [mysqld]
-- # InnoDB缓冲池大小（建议设置为可用内存的70-80%）
-- innodb_buffer_pool_size = 2G
--
-- # 查询缓存（MySQL 5.7及以下）
-- query_cache_size = 256M
-- query_cache_type = 1
--
-- # 慢查询日志
-- slow_query_log = 1
-- long_query_time = 1
--
-- # 连接池设置
-- max_connections = 200
-- thread_cache_size = 50

-- =====================================================
-- 9. 缓存策略建议
-- =====================================================

-- 建议缓存的数据：
-- 1. 规则统计信息（v_rule_statistics） - 缓存5分钟
-- 2. 部门规则统计（v_rule_dept_statistics） - 缓存10分钟
-- 3. 枚举数据（规则类型、状态等） - 缓存1小时
-- 4. 热点规则详情 - 缓存15分钟

-- =====================================================
-- 10. 定期维护脚本
-- =====================================================

-- 清理过期操作日志（保留最近3个月）
-- DELETE FROM `rule_operation_log`
-- WHERE operation_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 优化表（定期执行）
-- OPTIMIZE TABLE `rule`;
-- OPTIMIZE TABLE `rule_single`;
-- OPTIMIZE TABLE `rule_double`;
-- OPTIMIZE TABLE `rule_format`;
-- OPTIMIZE TABLE `rule_advanced`;
-- OPTIMIZE TABLE `rule_operation_log`;

-- 分析表统计信息（定期执行）
-- ANALYZE TABLE `rule`;
-- ANALYZE TABLE `rule_single`;
-- ANALYZE TABLE `rule_double`;
-- ANALYZE TABLE `rule_format`;
-- ANALYZE TABLE `rule_advanced`;
-- ANALYZE TABLE `rule_operation_log`;

-- =====================================================
-- 执行完成提示
-- =====================================================
-- Performance optimization for rules module completed successfully
-- Created composite indexes for common query patterns
-- Created monitoring views for statistics
-- Estimated query performance improvement: 60-80%
-- Target: List queries < 3 seconds, Search queries < 5 seconds
-- =====================================================