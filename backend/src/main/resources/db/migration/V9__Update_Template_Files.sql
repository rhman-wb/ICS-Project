-- =====================================================
-- 保险产品智能检核系统 - 更新产品模板文件路径
-- Version: V9__Update_Template_Files.sql
-- Description: 更新产品模板文件路径为新的官方模板文件
-- =====================================================

SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- =====================================================
-- 1. 更新模板文件路径为新的官方模板
-- =====================================================

-- 更新备案产品模板文件路径和名称
UPDATE `product_templates`
SET
    `template_name` = '附件3_备案产品自主注册信息登记表',
    `template_file_path` = 'templates/附件3_备案产品自主注册信息登记表.xlsx',
    `template_version` = '1.0.1',
    `description` = '官方备案产品自主注册信息登记表模板（附件3），包含30个标准字段',
    `mime_type` = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    `updated_by` = 'system',
    `updated_at` = NOW()
WHERE `template_type` = 'FILING' AND `is_deleted` = 0;

-- 更新农险产品模板文件路径和名称
UPDATE `product_templates`
SET
    `template_name` = '附件5_农险产品信息登记表',
    `template_file_path` = 'templates/附件5_农险产品信息登记表.xls',
    `template_version` = '1.0.1',
    `description` = '官方农险产品信息登记表模板（附件5），包含25个标准字段',
    `mime_type` = 'application/vnd.ms-excel',
    `updated_by` = 'system',
    `updated_at` = NOW()
WHERE `template_type` = 'AGRICULTURAL' AND `is_deleted` = 0;

-- =====================================================
-- 2. 验证更新结果
-- =====================================================

SELECT
    `id`,
    `template_type`,
    `template_name`,
    `template_file_path`,
    `template_version`,
    `description`,
    `enabled`
FROM `product_templates`
WHERE `is_deleted` = 0
ORDER BY `sort_order`;

-- =====================================================
-- 脚本执行完成
-- 模板文件路径已更新为新的官方模板
-- =====================================================

SET SQL_SAFE_UPDATES = 1;
