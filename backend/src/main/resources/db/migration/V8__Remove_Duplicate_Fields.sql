-- =====================================================
-- 保险产品智能检核系统 - 删除重复字段并统一布尔类型
-- Version: V8__Remove_Duplicate_Fields.sql
-- Description: 1) 删除products表中与新增字段重复的旧字段
--              2) 将布尔语义字段从varchar统一改为tinyint(1)
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- =====================================================
-- 1. 删除products表中的重复字段
-- =====================================================

-- 创建存储过程用于安全删除列（如果列存在才删除）
DELIMITER $$

DROP PROCEDURE IF EXISTS drop_column_if_exists$$
CREATE PROCEDURE drop_column_if_exists(
    IN tableName VARCHAR(128),
    IN columnName VARCHAR(128)
)
MODIFIES SQL DATA
SQL SECURITY INVOKER
COMMENT '安全删除表列（如果列存在）'
BEGIN
    DECLARE column_count INT;
    
    -- 检查列是否存在
    SELECT COUNT(*) INTO column_count
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = tableName
    AND COLUMN_NAME = columnName;
    
    -- 如果列存在，则删除
    IF column_count > 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', tableName, '` DROP COLUMN `', columnName, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- 删除重复的经营范围字段 (保留business_scope)
CALL drop_column_if_exists('products', 'operating_scope');

-- 删除重复的示范条款名称字段 (保留demonstration_clause)
CALL drop_column_if_exists('products', 'demonstration_clause_name');

-- 删除重复的经营区域字段 (保留business_area1和business_area2)
CALL drop_column_if_exists('products', 'operating_region1');
CALL drop_column_if_exists('products', 'operating_region2');

-- 清理存储过程
DROP PROCEDURE IF EXISTS drop_column_if_exists;

-- =====================================================
-- 2. 统一布尔字段类型为tinyint(1)
-- =====================================================

-- 处理has_electronic_policy字段
-- 先添加临时列
ALTER TABLE `products` ADD COLUMN `has_electronic_policy_temp` tinyint(1) DEFAULT NULL AFTER `has_electronic_policy`;

-- 转换数据到临时列: "是"→1, "否"→0, NULL→NULL
UPDATE `products` SET `has_electronic_policy_temp` =
    CASE
        WHEN `has_electronic_policy` = '是' THEN 1
        WHEN `has_electronic_policy` = '否' THEN 0
        WHEN `has_electronic_policy` = '1' THEN 1
        WHEN `has_electronic_policy` = '0' THEN 0
        ELSE NULL
    END;

-- 删除原列，重命名临时列
ALTER TABLE `products` DROP COLUMN `has_electronic_policy`;
ALTER TABLE `products` CHANGE COLUMN `has_electronic_policy_temp` `has_electronic_policy` tinyint(1) DEFAULT NULL COMMENT '是否有电子保单（备案产品）';

-- 处理has_national_encryption字段
ALTER TABLE `products` ADD COLUMN `has_national_encryption_temp` tinyint(1) DEFAULT NULL AFTER `has_national_encryption`;

UPDATE `products` SET `has_national_encryption_temp` =
    CASE
        WHEN `has_national_encryption` = '是' THEN 1
        WHEN `has_national_encryption` = '否' THEN 0
        WHEN `has_national_encryption` = '1' THEN 1
        WHEN `has_national_encryption` = '0' THEN 0
        ELSE NULL
    END;

ALTER TABLE `products` DROP COLUMN `has_national_encryption`;
ALTER TABLE `products` CHANGE COLUMN `has_national_encryption_temp` `has_national_encryption` tinyint(1) DEFAULT NULL COMMENT '是否是国产加密算法（备案产品）';

-- 处理is_operated字段
ALTER TABLE `products` ADD COLUMN `is_operated_temp` tinyint(1) DEFAULT NULL AFTER `is_operated`;

UPDATE `products` SET `is_operated_temp` =
    CASE
        WHEN `is_operated` = '是' THEN 1
        WHEN `is_operated` = '否' THEN 0
        WHEN `is_operated` = '1' THEN 1
        WHEN `is_operated` = '0' THEN 0
        ELSE NULL
    END;

ALTER TABLE `products` DROP COLUMN `is_operated`;
ALTER TABLE `products` CHANGE COLUMN `is_operated_temp` `is_operated` tinyint(1) DEFAULT NULL COMMENT '是否开办（农险产品）';

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
-- 4. 恢复设置
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- =====================================================
-- 脚本执行完成
-- 1. 已删除与新字段重复的旧字段
-- 2. 已将布尔语义字段统一为tinyint(1)类型
-- =====================================================