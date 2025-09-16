-- =====================================================
-- 保险产品智能检核系统 - 规则配置管理数据库表结构
-- Version: V4__rules_tables.sql
-- Description: 创建规则管理相关表结构，包括主表和四类规则子表
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 规则主表 (rule)
-- =====================================================
CREATE TABLE IF NOT EXISTS `rule` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_number` varchar(50) NOT NULL COMMENT '规则编号',
  `rule_name` varchar(200) NOT NULL COMMENT '规则名称',
  `rule_description` text COMMENT '规则描述',
  `rule_type` varchar(20) NOT NULL COMMENT '规则类型：SINGLE/DOUBLE/FORMAT/ADVANCED',
  `rule_source` varchar(20) NOT NULL COMMENT '规则来源：SYSTEM/CUSTOM/IMPORTED/TEMPLATE',
  `manage_department` varchar(100) COMMENT '规则管理部门',
  `applicable_insurance` varchar(100) COMMENT '适用险种',
  `applicable_requirements` varchar(200) COMMENT '适用要件',
  `applicable_chapter` varchar(100) COMMENT '适用章节',
  `business_area` varchar(200) COMMENT '适用经营区域',
  `audit_status` varchar(30) NOT NULL DEFAULT 'PENDING_TECH_EVALUATION' COMMENT '审核状态',
  `effective_status` varchar(20) NOT NULL DEFAULT 'PENDING_DEPLOYMENT' COMMENT '有效状态',
  `rule_content` json COMMENT '规则内容JSON',
  `rule_config` json COMMENT '规则配置JSON',
  `effective_time` datetime COMMENT '生效时间',
  `expiry_time` datetime COMMENT '失效时间',
  `last_updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `submitted_by` varchar(32) COMMENT '提交人ID',
  `submitted_at` datetime COMMENT '提交时间',
  `audited_by` varchar(32) COMMENT '审核人ID',
  `audited_at` datetime COMMENT '审核时间',
  `audit_comments` text COMMENT '审核意见',
  `is_followed` tinyint(1) DEFAULT 0 COMMENT '是否关注',
  `priority` int DEFAULT 1 COMMENT '优先级',
  `sort_order` int DEFAULT 1 COMMENT '排序号',
  `tags` json COMMENT '标签JSON数组',
  `remarks` text COMMENT '备注',
  `created_by` varchar(32) COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_number` (`rule_number`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_rule_source` (`rule_source`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_effective_status` (`effective_status`),
  KEY `idx_last_updated_at` (`last_updated_at`),
  KEY `idx_manage_department` (`manage_department`),
  KEY `idx_applicable_insurance` (`applicable_insurance`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则主表';

-- =====================================================
-- 2. 单句规则表 (rule_single)
-- =====================================================
CREATE TABLE IF NOT EXISTS `rule_single` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_id` varchar(32) NOT NULL COMMENT '关联规则ID',
  `rule_description` text NOT NULL COMMENT '规则描述',
  `match_chapter` varchar(100) NOT NULL COMMENT '匹配章节',
  `match_type` varchar(50) NOT NULL COMMENT '匹配类型',
  `match_content` text NOT NULL COMMENT '匹配内容',
  `trigger_condition` varchar(100) NOT NULL COMMENT '触发条件',
  `alert_type` varchar(50) NOT NULL COMMENT '提示类型',
  `alert_content` text NOT NULL COMMENT '提示内容',
  `applicable_requirements` varchar(200) NOT NULL COMMENT '适用要件',
  `applicable_insurance` varchar(100) COMMENT '适用险种',
  `applicable_business_area` varchar(200) COMMENT '适用经营区域',
  `specific_local_area` varchar(200) COMMENT '具体地方区域',
  `product_nature` varchar(100) COMMENT '产品性质',
  `rule_management_department` varchar(100) COMMENT '规则管理部门',
  `rule_source` varchar(100) COMMENT '规则来源',
  `created_by` varchar(32) COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_match_type` (`match_type`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_rule_single_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单句规则表';

-- =====================================================
-- 3. 双句规则表 (rule_double)
-- =====================================================
CREATE TABLE IF NOT EXISTS `rule_double` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_id` varchar(32) NOT NULL COMMENT '关联规则ID',
  `rule_description` text NOT NULL COMMENT '规则描述',
  `match_chapter_a` varchar(100) NOT NULL COMMENT '匹配章节A',
  `match_type_a` varchar(50) NOT NULL COMMENT '匹配类型A',
  `match_content_a` text NOT NULL COMMENT '匹配内容A',
  `match_chapter_b` varchar(100) NOT NULL COMMENT '匹配章节B',
  `match_type_b` varchar(50) NOT NULL COMMENT '匹配类型B',
  `match_content_b` text NOT NULL COMMENT '匹配内容B',
  `trigger_condition` varchar(100) NOT NULL COMMENT '触发条件',
  `alert_type` varchar(50) NOT NULL COMMENT '提示类型',
  `alert_content` text NOT NULL COMMENT '提示内容',
  `applicable_requirements` varchar(200) NOT NULL COMMENT '适用要件',
  `applicable_insurance` varchar(100) COMMENT '适用险种',
  `applicable_business_area` varchar(200) COMMENT '适用经营区域',
  `specific_local_area` varchar(200) COMMENT '具体地方区域',
  `product_nature` varchar(100) COMMENT '产品性质',
  `rule_management_department` varchar(100) COMMENT '规则管理部门',
  `rule_source` varchar(100) COMMENT '规则来源',
  `created_by` varchar(32) COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_match_type_a` (`match_type_a`),
  KEY `idx_match_type_b` (`match_type_b`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_rule_double_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='双句规则表';

-- =====================================================
-- 4. 格式规则表 (rule_format)
-- =====================================================
CREATE TABLE IF NOT EXISTS `rule_format` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_id` varchar(32) NOT NULL COMMENT '关联规则ID',
  `rule_description` text NOT NULL COMMENT '规则描述',
  `match_position` varchar(200) NOT NULL COMMENT '匹配位置',
  `match_content` text NOT NULL COMMENT '匹配内容',
  `paragraph_format_requirements` text NOT NULL COMMENT '段落格式要求',
  `font_format_requirements` text NOT NULL COMMENT '字体格式要求',
  `text_format_requirements` text NOT NULL COMMENT '行文格式要求',
  `alert_type` varchar(50) NOT NULL COMMENT '提示类型',
  `alert_content` text NOT NULL COMMENT '提示内容',
  `applicable_requirements` varchar(200) NOT NULL COMMENT '适用要件',
  `applicable_insurance` varchar(100) COMMENT '适用险种',
  `applicable_business_area` varchar(200) COMMENT '适用经营区域',
  `specific_local_area` varchar(200) COMMENT '具体地方区域',
  `product_nature` varchar(100) COMMENT '产品性质',
  `rule_management_department` varchar(100) COMMENT '规则管理部门',
  `rule_source` varchar(100) COMMENT '规则来源',
  `created_by` varchar(32) COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_rule_format_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='格式规则表';

-- =====================================================
-- 5. 高级规则表 (rule_advanced)
-- =====================================================
CREATE TABLE IF NOT EXISTS `rule_advanced` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_id` varchar(32) NOT NULL COMMENT '关联规则ID',
  `rule_description` text NOT NULL COMMENT '规则描述',
  `alert_type` varchar(50) NOT NULL COMMENT '提示类型',
  `alert_content` text NOT NULL COMMENT '提示内容',
  `applicable_requirements` varchar(200) NOT NULL COMMENT '适用要件',
  `applicable_insurance` varchar(100) COMMENT '适用险种',
  `applicable_business_area` varchar(200) COMMENT '适用经营区域',
  `specific_local_area` varchar(200) COMMENT '具体地方区域',
  `product_nature` varchar(100) COMMENT '产品性质',
  `rule_management_department` varchar(100) COMMENT '规则管理部门',
  `rule_source` varchar(100) COMMENT '规则来源',
  `advanced_rule_config` json COMMENT '高级规则配置JSON',
  `execution_script` text COMMENT '执行脚本或表达式',
  `rule_weight` int DEFAULT 100 COMMENT '规则权重',
  `llm_enabled` tinyint(1) DEFAULT 0 COMMENT '是否启用LLM',
  `llm_model_config` json COMMENT 'LLM模型配置JSON',
  `created_by` varchar(32) COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) COMMENT '更新人ID',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `version` int NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_rule_weight` (`rule_weight`),
  KEY `idx_llm_enabled` (`llm_enabled`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_rule_advanced_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='高级规则表';

-- =====================================================
-- 6. 规则操作日志表 (rule_operation_log)
-- =====================================================
CREATE TABLE IF NOT EXISTS `rule_operation_log` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `rule_id` varchar(32) NOT NULL COMMENT '规则ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/SUBMIT/AUDIT/IMPORT',
  `operation_description` varchar(500) COMMENT '操作描述',
  `old_data` json COMMENT '变更前数据',
  `new_data` json COMMENT '变更后数据',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) COMMENT '操作人姓名',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip_address` varchar(50) COMMENT 'IP地址',
  `user_agent` varchar(500) COMMENT '用户代理',
  `operation_result` varchar(20) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
  `error_message` text COMMENT '错误信息',
  `request_id` varchar(50) COMMENT '请求ID',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_operation_result` (`operation_result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则操作日志表';

-- =====================================================
-- 7. 插入初始化数据
-- =====================================================

-- 插入示例规则数据
INSERT INTO `rule` (
  `id`, `rule_number`, `rule_name`, `rule_description`, `rule_type`, `rule_source`,
  `manage_department`, `applicable_insurance`, `applicable_requirements`,
  `applicable_chapter`, `business_area`, `audit_status`, `effective_status`,
  `created_by`, `updated_by`
) VALUES (
  'R001', 'RULE-001', '保险金额限制规则', '保险金额不能超过投保人年收入的10倍',
  'SINGLE', 'SYSTEM', '技术部门', '人寿保险', '投保条件',
  '第一章', '全国', 'PENDING_TECH_EVALUATION', 'EFFECTIVE',
  'SYSTEM', 'SYSTEM'
);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 执行完成提示
-- =====================================================
-- Migration V4__rules_tables.sql executed successfully
-- Created tables: rule, rule_single, rule_double, rule_format, rule_advanced, rule_operation_log
-- Created indexes for performance optimization on key fields
-- =====================================================