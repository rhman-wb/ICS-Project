-- =====================================================
-- 保险产品智能检核系统 - 产品模板增强数据库迁移
-- Version: V7__Add_Template_Support.sql
-- Description: 添加产品模板支持，扩展产品表字段
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 扩展产品表字段以支持新模板
-- =====================================================

-- 添加缺失的文档数量字段
ALTER TABLE `products` ADD COLUMN `document_count` int DEFAULT '0' COMMENT '文档数量' AFTER `status`;

-- 添加备案产品模板字段
ALTER TABLE `products` ADD COLUMN `development_type` varchar(100) DEFAULT NULL COMMENT '开发类型（备案产品）' AFTER `document_count`;
ALTER TABLE `products` ADD COLUMN `demonstration_clause` varchar(200) DEFAULT NULL COMMENT '示范条款名称（备案产品）' AFTER `development_type`;
ALTER TABLE `products` ADD COLUMN `business_scope` varchar(100) DEFAULT NULL COMMENT '经营范围（备案产品）' AFTER `demonstration_clause`;
ALTER TABLE `products` ADD COLUMN `business_area1` varchar(100) DEFAULT NULL COMMENT '经营区域1（备案产品）' AFTER `business_scope`;
ALTER TABLE `products` ADD COLUMN `business_area2` varchar(200) DEFAULT NULL COMMENT '经营区域2（备案产品）' AFTER `business_area1`;
ALTER TABLE `products` ADD COLUMN `product_property` varchar(100) DEFAULT NULL COMMENT '产品属性（备案产品）' AFTER `business_area2`;
ALTER TABLE `products` ADD COLUMN `basic_rate` varchar(50) DEFAULT NULL COMMENT '基础费率（备案产品）' AFTER `product_property`;
ALTER TABLE `products` ADD COLUMN `basic_rate_table` varchar(200) DEFAULT NULL COMMENT '基础费率表（备案产品）' AFTER `basic_rate`;
ALTER TABLE `products` ADD COLUMN `has_electronic_policy` varchar(10) DEFAULT NULL COMMENT '是否有电子保单（备案产品）' AFTER `basic_rate_table`;
ALTER TABLE `products` ADD COLUMN `has_national_encryption` varchar(10) DEFAULT NULL COMMENT '是否是国产加密算法（备案产品）' AFTER `has_electronic_policy`;
ALTER TABLE `products` ADD COLUMN `insurance_period` text COMMENT '保险期间（备案产品）' AFTER `has_national_encryption`;
ALTER TABLE `products` ADD COLUMN `insurance_responsibility` text COMMENT '保险责任（备案产品）' AFTER `insurance_period`;
ALTER TABLE `products` ADD COLUMN `sales_area` varchar(200) DEFAULT NULL COMMENT '销售区域（备案产品）' AFTER `insurance_responsibility`;
ALTER TABLE `products` ADD COLUMN `sales_channel` varchar(200) DEFAULT NULL COMMENT '销售渠道（备案产品）' AFTER `sales_area`;

-- 添加农险产品模板字段
ALTER TABLE `products` ADD COLUMN `revision_count` int DEFAULT NULL COMMENT '修订次数（农险产品）' AFTER `sales_channel`;
ALTER TABLE `products` ADD COLUMN `insurance_target` varchar(100) DEFAULT NULL COMMENT '保险标的（农险产品）' AFTER `revision_count`;
ALTER TABLE `products` ADD COLUMN `is_operated` varchar(10) DEFAULT NULL COMMENT '是否开办（农险产品）' AFTER `insurance_target`;
ALTER TABLE `products` ADD COLUMN `operation_date` varchar(50) DEFAULT NULL COMMENT '开办日期（农险产品）' AFTER `is_operated`;
ALTER TABLE `products` ADD COLUMN `rate_floating_range` varchar(100) DEFAULT NULL COMMENT '费率浮动区间（农险产品）' AFTER `operation_date`;
ALTER TABLE `products` ADD COLUMN `rate_floating_coefficient` varchar(100) DEFAULT NULL COMMENT '费率浮动系数（农险产品）' AFTER `rate_floating_range`;
ALTER TABLE `products` ADD COLUMN `absolute_deductible` varchar(100) DEFAULT NULL COMMENT '绝对免赔率（额）（农险产品）' AFTER `rate_floating_coefficient`;
ALTER TABLE `products` ADD COLUMN `relative_deductible` varchar(100) DEFAULT NULL COMMENT '相对免赔率（额）（农险产品）' AFTER `absolute_deductible`;
ALTER TABLE `products` ADD COLUMN `remarks` text COMMENT '备注栏（农险产品）' AFTER `relative_deductible`;

-- 添加通用模板字段
ALTER TABLE `products` ADD COLUMN `template_type` varchar(50) DEFAULT NULL COMMENT '模板类型标识' AFTER `remarks`;
ALTER TABLE `products` ADD COLUMN `original_product_code` varchar(200) DEFAULT NULL COMMENT '原产品名称和编号/备案编号（修订产品）' AFTER `template_type`;
ALTER TABLE `products` ADD COLUMN `uses_demonstration_clause` tinyint(1) DEFAULT '0' COMMENT '是否使用示范条款' AFTER `original_product_code`;
ALTER TABLE `products` ADD COLUMN `is_policy_insurance` tinyint(1) DEFAULT '0' COMMENT '是否为政策性保险' AFTER `uses_demonstration_clause`;
ALTER TABLE `products` ADD COLUMN `underwriting_method` varchar(50) DEFAULT NULL COMMENT '承保方式' AFTER `is_policy_insurance`;
ALTER TABLE `products` ADD COLUMN `claim_method` varchar(50) DEFAULT NULL COMMENT '赔付方式' AFTER `underwriting_method`;

-- =====================================================
-- 2. 创建产品模板表
-- =====================================================
DROP TABLE IF EXISTS `product_templates`;
CREATE TABLE `product_templates` (
  `id` varchar(32) NOT NULL COMMENT '模板ID',
  `template_type` varchar(50) NOT NULL COMMENT '模板类型：FILING-备案产品，AGRICULTURAL-农险产品',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_file_path` varchar(500) NOT NULL COMMENT '模板文件路径',
  `field_config` json DEFAULT NULL COMMENT '字段配置JSON',
  `validation_rules` json DEFAULT NULL COMMENT '验证规则JSON',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用，1-启用',
  `template_version` varchar(20) NOT NULL COMMENT '模板版本',
  `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_type_version` (`template_type`, `template_version`, `is_deleted`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_template_name` (`template_name`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品模板表';

-- =====================================================
-- 3. 为产品表新增字段创建索引
-- =====================================================

-- 模板相关索引
CREATE INDEX `idx_products_template_type` ON `products` (`template_type`);
CREATE INDEX `idx_products_development_type` ON `products` (`development_type`);
CREATE INDEX `idx_products_business_scope` ON `products` (`business_scope`);
CREATE INDEX `idx_products_is_operated` ON `products` (`is_operated`);
CREATE INDEX `idx_products_template_type_status` ON `products` (`template_type`, `status`);

-- 复合索引优化查询
CREATE INDEX `idx_products_template_year_status` ON `products` (`template_type`, `year`, `status`);
CREATE INDEX `idx_products_type_nature_region` ON `products` (`product_type`, `product_nature`, `operating_region`(50));

-- =====================================================
-- 4. 插入模板初始化数据
-- =====================================================

-- 插入备案产品模板
INSERT INTO `product_templates` (`id`, `template_type`, `template_name`, `template_file_path`, `template_version`, `description`, `enabled`, `sort_order`, `created_by`, `created_at`) VALUES
('template_filing_001', 'FILING', '备案产品自主注册信息登记表', '/templates/filing_product_template.xlsx', '1.0.0', '用于备案产品信息登记的标准模板，包含所有必填字段和验证规则', 1, 1, 'system', NOW());

-- 插入农险产品模板
INSERT INTO `product_templates` (`id`, `template_type`, `template_name`, `template_file_path`, `template_version`, `description`, `enabled`, `sort_order`, `created_by`, `created_at`) VALUES
('template_agricultural_001', 'AGRICULTURAL', '农险产品信息登记表', '/templates/agricultural_product_template.xls', '1.0.0', '用于农险产品信息登记的标准模板，包含农险特有字段和验证规则', 1, 2, 'system', NOW());

-- =====================================================
-- 5. 更新现有产品数据设置默认模板类型
-- =====================================================

-- 根据现有product_type字段设置template_type
UPDATE `products` SET `template_type` = 'FILING' WHERE `product_type` = 'FILING' AND `template_type` IS NULL;
UPDATE `products` SET `template_type` = 'AGRICULTURAL' WHERE `product_type` = 'AGRICULTURAL' AND `template_type` IS NULL;

-- 设置默认的文档数量（基于关联的documents表）
UPDATE `products` p SET `document_count` = (
    SELECT COUNT(*) FROM `documents` d
    WHERE d.`product_id` = p.`id` AND d.`is_deleted` = 0
) WHERE `document_count` = 0;

-- =====================================================
-- 6. 数据完整性验证
-- =====================================================

-- 验证产品表新字段是否添加成功
SELECT COUNT(*) as new_fields_count FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'products'
AND COLUMN_NAME IN ('development_type', 'template_type', 'document_count');

-- 验证产品模板表是否创建成功
SELECT COUNT(*) as template_records FROM `product_templates` WHERE `is_deleted` = 0;

-- =====================================================
-- 7. 创建触发器维护数据一致性
-- =====================================================

-- 创建触发器：当documents表有变更时自动更新products表的document_count
DELIMITER $$

DROP TRIGGER IF EXISTS `tr_documents_update_count_insert`$$
CREATE TRIGGER `tr_documents_update_count_insert`
AFTER INSERT ON `documents`
FOR EACH ROW
BEGIN
    UPDATE `products` SET `document_count` = (
        SELECT COUNT(*) FROM `documents`
        WHERE `product_id` = NEW.`product_id` AND `is_deleted` = 0
    ) WHERE `id` = NEW.`product_id`;
END$$

DROP TRIGGER IF EXISTS `tr_documents_update_count_update`$$
CREATE TRIGGER `tr_documents_update_count_update`
AFTER UPDATE ON `documents`
FOR EACH ROW
BEGIN
    -- 更新旧产品的文档计数
    IF OLD.`product_id` IS NOT NULL THEN
        UPDATE `products` SET `document_count` = (
            SELECT COUNT(*) FROM `documents`
            WHERE `product_id` = OLD.`product_id` AND `is_deleted` = 0
        ) WHERE `id` = OLD.`product_id`;
    END IF;

    -- 更新新产品的文档计数(如果product_id发生变化或is_deleted状态变化)
    IF NEW.`product_id` IS NOT NULL AND
       (NEW.`product_id` != OLD.`product_id` OR NEW.`is_deleted` != OLD.`is_deleted`) THEN
        UPDATE `products` SET `document_count` = (
            SELECT COUNT(*) FROM `documents`
            WHERE `product_id` = NEW.`product_id` AND `is_deleted` = 0
        ) WHERE `id` = NEW.`product_id`;
    END IF;
END$$

DROP TRIGGER IF EXISTS `tr_documents_update_count_delete`$$
CREATE TRIGGER `tr_documents_update_count_delete`
AFTER DELETE ON `documents`
FOR EACH ROW
BEGIN
    UPDATE `products` SET `document_count` = (
        SELECT COUNT(*) FROM `documents`
        WHERE `product_id` = OLD.`product_id` AND `is_deleted` = 0
    ) WHERE `id` = OLD.`product_id`;
END$$

DELIMITER ;

-- =====================================================
-- 8. 恢复外键检查
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 脚本执行完成
-- 数据库已成功升级以支持产品模板增强功能
-- =====================================================