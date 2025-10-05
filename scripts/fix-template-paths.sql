-- =====================================================
-- 快速修复模板文件路径（去掉前导斜杠）
-- =====================================================

USE insurance_audit;

-- 更新备案产品模板路径（去掉前导斜杠）
UPDATE product_templates
SET
    template_file_path = 'templates/附件3_备案产品自主注册信息登记表.xlsx',
    updated_by = 'system',
    updated_at = NOW()
WHERE
    template_type = 'FILING'
    AND is_deleted = 0;

-- 更新农险产品模板路径（去掉前导斜杠）
UPDATE product_templates
SET
    template_file_path = 'templates/附件5_农险产品信息登记表.xls',
    updated_by = 'system',
    updated_at = NOW()
WHERE
    template_type = 'AGRICULTURAL'
    AND is_deleted = 0;

-- 验证更新结果
SELECT
    template_type,
    template_name,
    template_file_path,
    CONCAT('uploads/', template_file_path) AS full_path
FROM
    product_templates
WHERE
    is_deleted = 0;
