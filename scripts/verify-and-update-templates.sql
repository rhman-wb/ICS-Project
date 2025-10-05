-- =====================================================
-- 验证数据库模板配置
-- =====================================================

-- 1. 检查 product_templates 表是否存在
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME
FROM
    INFORMATION_SCHEMA.TABLES
WHERE
    TABLE_SCHEMA = 'insurance_audit'
    AND TABLE_NAME = 'product_templates';

-- 2. 查看现有模板记录
SELECT
    id,
    template_type,
    template_name,
    template_file_path,
    template_version,
    enabled,
    created_at,
    updated_at
FROM
    product_templates
WHERE
    is_deleted = 0
ORDER BY
    sort_order;

-- 3. 如果记录不存在或路径错误，执行以下更新
-- 更新备案产品模板
UPDATE product_templates
SET
    template_name = '附件3_备案产品自主注册信息登记表',
    template_file_path = 'templates/附件3_备案产品自主注册信息登记表.xlsx',
    template_version = '1.0.1',
    description = '官方备案产品自主注册信息登记表模板（附件3），包含30个标准字段',
    mime_type = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    updated_by = 'system',
    updated_at = NOW()
WHERE
    template_type = 'FILING'
    AND is_deleted = 0;

-- 更新农险产品模板
UPDATE product_templates
SET
    template_name = '附件5_农险产品信息登记表',
    template_file_path = 'templates/附件5_农险产品信息登记表.xls',
    template_version = '1.0.1',
    description = '官方农险产品信息登记表模板（附件5），包含25个标准字段',
    mime_type = 'application/vnd.ms-excel',
    updated_by = 'system',
    updated_at = NOW()
WHERE
    template_type = 'AGRICULTURAL'
    AND is_deleted = 0;

-- 4. 验证更新结果
SELECT
    id,
    template_type,
    template_name,
    template_file_path,
    template_version,
    mime_type,
    enabled
FROM
    product_templates
WHERE
    is_deleted = 0
ORDER BY
    sort_order;

-- 5. 如果表不存在，需要先执行 V7__Add_Template_Support.sql 创建表
-- 然后执行此脚本更新模板路径
