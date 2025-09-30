-- =====================================================
-- 保险产品智能检核系统 - 删除重复字段并统一布尔类型
-- Version: V8__Remove_Duplicate_Fields.sql
-- Description: 1) 删除products表中与新增字段重复的旧字段
--              2) 将布尔语义字段从varchar统一改为tinyint(1)
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 删除products表中的重复字段
-- =====================================================

-- 删除重复的经营范围字段 (保留business_scope)
ALTER TABLE `products` DROP COLUMN IF EXISTS `operating_scope`;

-- 删除重复的示范条款名称字段 (保留demonstration_clause)
ALTER TABLE `products` DROP COLUMN IF EXISTS `demonstration_clause_name`;

-- 删除重复的经营区域字段 (保留business_area1和business_area2)
ALTER TABLE `products` DROP COLUMN IF EXISTS `operating_region1`;
ALTER TABLE `products` DROP COLUMN IF EXISTS `operating_region2`;

-- =====================================================
-- 2. 统一布尔字段类型为tinyint(1)
-- =====================================================

-- 修改has_electronic_policy字段类型
-- 先转换数据: "是"→1, "否"→0, NULL→NULL
UPDATE `products` SET `has_electronic_policy` =
    CASE
        WHEN `has_electronic_policy` = '是' THEN '1'
        WHEN `has_electronic_policy` = '否' THEN '0'
        ELSE NULL
    END
WHERE `has_electronic_policy` IN ('是', '否');

-- 修改字段类型
ALTER TABLE `products` MODIFY COLUMN `has_electronic_policy` tinyint(1) DEFAULT NULL COMMENT '是否有电子保单（备案产品）';

-- 修改has_national_encryption字段类型
-- 先转换数据
UPDATE `products` SET `has_national_encryption` =
    CASE
        WHEN `has_national_encryption` = '是' THEN '1'
        WHEN `has_national_encryption` = '否' THEN '0'
        ELSE NULL
    END
WHERE `has_national_encryption` IN ('是', '否');

-- 修改字段类型
ALTER TABLE `products` MODIFY COLUMN `has_national_encryption` tinyint(1) DEFAULT NULL COMMENT '是否是国产加密算法（备案产品）';

-- 修改is_operated字段类型
-- 先转换数据
UPDATE `products` SET `is_operated` =
    CASE
        WHEN `is_operated` = '是' THEN '1'
        WHEN `is_operated` = '否' THEN '0'
        ELSE NULL
    END
WHERE `is_operated` IN ('是', '否');

-- 修改字段类型
ALTER TABLE `products` MODIFY COLUMN `is_operated` tinyint(1) DEFAULT NULL COMMENT '是否开办（农险产品）';

-- =====================================================
-- 3. 验证操作结果
-- =====================================================

-- 验证旧字段是否已删除
SELECT COUNT(*) as remaining_old_fields FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'products'
AND COLUMN_NAME IN ('operating_scope', 'demonstration_clause_name', 'operating_region1', 'operating_region2');

-- 验证布尔字段类型是否已更新
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'products'
AND COLUMN_NAME IN ('has_electronic_policy', 'has_national_encryption', 'is_operated');

-- =====================================================
-- 4. 恢复外键检查
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 脚本执行完成
-- 1. 已删除与新字段重复的旧字段
-- 2. 已将布尔语义字段统一为tinyint(1)类型
-- =====================================================