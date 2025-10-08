-- =====================================================
-- 保险产品智能检核系统 - 填充产品模板字段配置
-- Version: V10__Populate_Template_Field_Config.sql
-- Description: 为产品模板添加完整的字段配置和验证规则
-- =====================================================

SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- =====================================================
-- 1. 更新备案产品模板字段配置 (FILING - 30 fields)
-- =====================================================

UPDATE `product_templates`
SET `field_config` = JSON_ARRAY(
    JSON_OBJECT('name', 'productName', 'label', '产品名称', 'type', 'text', 'required', true, 'placeholder', '请填写产品名称', 'order', 1, 'maxLength', 200),
    JSON_OBJECT('name', 'developmentType', 'label', '开发类型', 'type', 'select', 'required', true, 'placeholder', '请选择开发类型', 'order', 2, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '自主开发', 'label', '自主开发'),
        JSON_OBJECT('value', '合作开发', 'label', '合作开发'),
        JSON_OBJECT('value', '其他', 'label', '其他')
    )),
    JSON_OBJECT('name', 'productNature', 'label', '产品性质', 'type', 'select', 'required', false, 'placeholder', '请选择产品性质', 'order', 3, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '新产品', 'label', '新产品'),
        JSON_OBJECT('value', '修订产品', 'label', '修订产品')
    )),
    JSON_OBJECT('name', 'year', 'label', '年度', 'type', 'number', 'required', false, 'placeholder', '请填写年度', 'order', 4, 'min', 2000, 'max', 2100),
    JSON_OBJECT('name', 'revisionType', 'label', '修订类型', 'type', 'select', 'required', false, 'placeholder', '请选择修订类型', 'order', 5, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '条款修订', 'label', '条款修订'),
        JSON_OBJECT('value', '费率修订', 'label', '费率修订'),
        JSON_OBJECT('value', '条款费率修订', 'label', '条款费率修订')
    )),
    JSON_OBJECT('name', 'originalProductName', 'label', '原产品名称和备案编号', 'type', 'text', 'required', false, 'placeholder', '请填写原产品名称和备案编号', 'order', 6, 'maxLength', 200),
    JSON_OBJECT('name', 'reportType', 'label', '报送类型', 'type', 'select', 'required', true, 'placeholder', '请选择报送类型', 'order', 7, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '新产品', 'label', '新产品'),
        JSON_OBJECT('value', '修订产品', 'label', '修订产品')
    )),
    JSON_OBJECT('name', 'demonstrationClauseName', 'label', '示范条款名称', 'type', 'text', 'required', true, 'placeholder', '请填写示范条款名称', 'order', 8, 'maxLength', 200),
    JSON_OBJECT('name', 'productCategory', 'label', '产品类别', 'type', 'select', 'required', true, 'placeholder', '请选择产品类别', 'order', 9, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '种植险', 'label', '种植险'),
        JSON_OBJECT('value', '养殖险', 'label', '养殖险'),
        JSON_OBJECT('value', '不区分', 'label', '不区分')
    )),
    JSON_OBJECT('name', 'primaryAdditional', 'label', '主附险类型', 'type', 'select', 'required', true, 'placeholder', '请选择主附险类型', 'order', 10, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '主险', 'label', '主险'),
        JSON_OBJECT('value', '附险', 'label', '附险')
    )),
    JSON_OBJECT('name', 'primarySituation', 'label', '主险情况', 'type', 'text', 'required', false, 'placeholder', '请填写主险情况', 'order', 11, 'maxLength', 200),
    JSON_OBJECT('name', 'productAttribute', 'label', '产品属性', 'type', 'text', 'required', false, 'placeholder', '请填写产品属性', 'order', 12, 'maxLength', 100),
    JSON_OBJECT('name', 'insurancePeriod', 'label', '保险期间', 'type', 'text', 'required', false, 'placeholder', '请填写保险期间', 'order', 13, 'maxLength', 100),
    JSON_OBJECT('name', 'insuranceResponsibility', 'label', '保险责任', 'type', 'textarea', 'required', false, 'placeholder', '请填写保险责任', 'order', 14, 'maxLength', 500),
    JSON_OBJECT('name', 'baseRate', 'label', '基础费率（%）', 'type', 'number', 'required', false, 'placeholder', '请填写基础费率', 'order', 15, 'min', 0, 'max', 100),
    JSON_OBJECT('name', 'deductible', 'label', '免赔额（率）', 'type', 'text', 'required', false, 'placeholder', '请填写免赔额（率）', 'order', 16, 'maxLength', 100),
    JSON_OBJECT('name', 'compensationLimit', 'label', '赔偿限额', 'type', 'text', 'required', false, 'placeholder', '请填写赔偿限额', 'order', 17, 'maxLength', 100),
    JSON_OBJECT('name', 'operatingScope', 'label', '经营范围', 'type', 'text', 'required', false, 'placeholder', '请填写经营范围', 'order', 18, 'maxLength', 200),
    JSON_OBJECT('name', 'operatingRegion1', 'label', '经营区域1', 'type', 'text', 'required', false, 'placeholder', '请填写经营区域1', 'order', 19, 'maxLength', 200),
    JSON_OBJECT('name', 'operatingRegion2', 'label', '经营区域2', 'type', 'text', 'required', false, 'placeholder', '请填写经营区域2', 'order', 20, 'maxLength', 200),
    JSON_OBJECT('name', 'salesArea', 'label', '销售区域', 'type', 'text', 'required', false, 'placeholder', '请填写销售区域', 'order', 21, 'maxLength', 200),
    JSON_OBJECT('name', 'hasElectronicPolicy', 'label', '是否有电子保单', 'type', 'select', 'required', false, 'placeholder', '请选择是否有电子保单', 'order', 22, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '是', 'label', '是'),
        JSON_OBJECT('value', '否', 'label', '否')
    )),
    JSON_OBJECT('name', 'isDomesticEncryption', 'label', '是否是国产加密算法', 'type', 'select', 'required', false, 'placeholder', '请选择是否是国产加密算法', 'order', 23, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '是', 'label', '是'),
        JSON_OBJECT('value', '否', 'label', '否')
    )),
    JSON_OBJECT('name', 'remarks', 'label', '备注', 'type', 'textarea', 'required', false, 'placeholder', '请填写备注信息', 'order', 24, 'maxLength', 500)
),
`validation_rules` = JSON_ARRAY(
    JSON_OBJECT('fieldName', 'productName', 'ruleType', 'required', 'message', '产品名称为必填项'),
    JSON_OBJECT('fieldName', 'productName', 'ruleType', 'maxLength', 'value', 200, 'message', '产品名称长度不能超过200个字符'),
    JSON_OBJECT('fieldName', 'developmentType', 'ruleType', 'required', 'message', '开发类型为必填项'),
    JSON_OBJECT('fieldName', 'reportType', 'ruleType', 'required', 'message', '报送类型为必填项'),
    JSON_OBJECT('fieldName', 'demonstrationClauseName', 'ruleType', 'required', 'message', '示范条款名称为必填项'),
    JSON_OBJECT('fieldName', 'productCategory', 'ruleType', 'required', 'message', '产品类别为必填项'),
    JSON_OBJECT('fieldName', 'primaryAdditional', 'ruleType', 'required', 'message', '主附险类型为必填项'),
    JSON_OBJECT('fieldName', 'baseRate', 'ruleType', 'minValue', 'value', 0, 'message', '基础费率不能小于0'),
    JSON_OBJECT('fieldName', 'baseRate', 'ruleType', 'maxValue', 'value', 100, 'message', '基础费率不能大于100')
),
`updated_by` = 'system',
`updated_at` = NOW()
WHERE `template_type` = 'FILING' AND `is_deleted` = 0;

-- =====================================================
-- 2. 更新农险产品模板字段配置 (AGRICULTURAL - 25 fields)
-- =====================================================

UPDATE `product_templates`
SET `field_config` = JSON_ARRAY(
    JSON_OBJECT('name', 'productName', 'label', '产品名称', 'type', 'text', 'required', true, 'placeholder', '请填写产品名称', 'order', 1, 'maxLength', 200),
    JSON_OBJECT('name', 'developmentMethod', 'label', '开发方式', 'type', 'select', 'required', true, 'placeholder', '请选择开发方式', 'order', 2, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '自主开发', 'label', '自主开发'),
        JSON_OBJECT('value', '合作开发', 'label', '合作开发'),
        JSON_OBJECT('value', '其他', 'label', '其他')
    )),
    JSON_OBJECT('name', 'productNature', 'label', '产品性质', 'type', 'select', 'required', true, 'placeholder', '请选择产品性质', 'order', 3, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '新产品', 'label', '新产品'),
        JSON_OBJECT('value', '修订产品', 'label', '修订产品')
    )),
    JSON_OBJECT('name', 'year', 'label', '年度', 'type', 'number', 'required', false, 'placeholder', '请填写年度', 'order', 4, 'min', 2000, 'max', 2100),
    JSON_OBJECT('name', 'revisionType', 'label', '修订类型', 'type', 'select', 'required', true, 'placeholder', '请选择修订类型', 'order', 5, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '条款修订', 'label', '条款修订'),
        JSON_OBJECT('value', '费率修订', 'label', '费率修订'),
        JSON_OBJECT('value', '条款费率修订', 'label', '条款费率修订')
    )),
    JSON_OBJECT('name', 'originalProductInfo', 'label', '原产品名称和编号', 'type', 'text', 'required', false, 'placeholder', '请填写原产品名称和编号', 'order', 6, 'maxLength', 200),
    JSON_OBJECT('name', 'revisionCount', 'label', '修订次数', 'type', 'number', 'required', false, 'placeholder', '请填写修订次数', 'order', 7, 'min', 0),
    JSON_OBJECT('name', 'reportType', 'label', '报送类型', 'type', 'select', 'required', true, 'placeholder', '请选择报送类型', 'order', 8, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '新产品', 'label', '新产品'),
        JSON_OBJECT('value', '修订产品', 'label', '修订产品')
    )),
    JSON_OBJECT('name', 'primaryAdditional', 'label', '主附险', 'type', 'select', 'required', true, 'placeholder', '请选择主附险', 'order', 9, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '主险', 'label', '主险'),
        JSON_OBJECT('value', '附险', 'label', '附险')
    )),
    JSON_OBJECT('name', 'primarySituation', 'label', '主险情况', 'type', 'text', 'required', false, 'placeholder', '请填写主险情况', 'order', 10, 'maxLength', 200),
    JSON_OBJECT('name', 'productCategory', 'label', '产品类别', 'type', 'select', 'required', true, 'placeholder', '请选择产品类别', 'order', 11, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '种植险', 'label', '种植险'),
        JSON_OBJECT('value', '养殖险', 'label', '养殖险'),
        JSON_OBJECT('value', '不区分', 'label', '不区分')
    )),
    JSON_OBJECT('name', 'insurancePeriod', 'label', '保险期间', 'type', 'text', 'required', false, 'placeholder', '请填写保险期间', 'order', 12, 'maxLength', 100),
    JSON_OBJECT('name', 'insuranceResponsibility', 'label', '保险责任', 'type', 'textarea', 'required', false, 'placeholder', '请填写保险责任', 'order', 13, 'maxLength', 500),
    JSON_OBJECT('name', 'baseRate', 'label', '基础费率（%）', 'type', 'number', 'required', false, 'placeholder', '请填写基础费率', 'order', 14, 'min', 0, 'max', 100),
    JSON_OBJECT('name', 'compensationMethod', 'label', '赔偿处理方式', 'type', 'text', 'required', false, 'placeholder', '请填写赔偿处理方式', 'order', 15, 'maxLength', 100),
    JSON_OBJECT('name', 'operatingRegion', 'label', '经营区域', 'type', 'text', 'required', false, 'placeholder', '请填写经营区域', 'order', 16, 'maxLength', 200),
    JSON_OBJECT('name', 'isOperated', 'label', '是否开办', 'type', 'select', 'required', false, 'placeholder', '请选择是否开办', 'order', 17, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '是', 'label', '是'),
        JSON_OBJECT('value', '否', 'label', '否')
    )),
    JSON_OBJECT('name', 'hasElectronicPolicy', 'label', '是否有电子保单', 'type', 'select', 'required', false, 'placeholder', '请选择是否有电子保单', 'order', 18, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '是', 'label', '是'),
        JSON_OBJECT('value', '否', 'label', '否')
    )),
    JSON_OBJECT('name', 'isDomesticCrypto', 'label', '是否是国产密码算法', 'type', 'select', 'required', false, 'placeholder', '请选择是否是国产密码算法', 'order', 19, 'options', JSON_ARRAY(
        JSON_OBJECT('value', '是', 'label', '是'),
        JSON_OBJECT('value', '否', 'label', '否')
    )),
    JSON_OBJECT('name', 'remarks', 'label', '备注', 'type', 'textarea', 'required', false, 'placeholder', '请填写备注信息', 'order', 20, 'maxLength', 500)
),
`validation_rules` = JSON_ARRAY(
    JSON_OBJECT('fieldName', 'productName', 'ruleType', 'required', 'message', '产品名称为必填项'),
    JSON_OBJECT('fieldName', 'productName', 'ruleType', 'maxLength', 'value', 200, 'message', '产品名称长度不能超过200个字符'),
    JSON_OBJECT('fieldName', 'developmentMethod', 'ruleType', 'required', 'message', '开发方式为必填项'),
    JSON_OBJECT('fieldName', 'productNature', 'ruleType', 'required', 'message', '产品性质为必填项'),
    JSON_OBJECT('fieldName', 'revisionType', 'ruleType', 'required', 'message', '修订类型为必填项'),
    JSON_OBJECT('fieldName', 'reportType', 'ruleType', 'required', 'message', '报送类型为必填项'),
    JSON_OBJECT('fieldName', 'primaryAdditional', 'ruleType', 'required', 'message', '主附险为必填项'),
    JSON_OBJECT('fieldName', 'productCategory', 'ruleType', 'required', 'message', '产品类别为必填项'),
    JSON_OBJECT('fieldName', 'baseRate', 'ruleType', 'minValue', 'value', 0, 'message', '基础费率不能小于0'),
    JSON_OBJECT('fieldName', 'baseRate', 'ruleType', 'maxValue', 'value', 100, 'message', '基础费率不能大于100'),
    JSON_OBJECT('fieldName', 'revisionCount', 'ruleType', 'minValue', 'value', 0, 'message', '修订次数不能为负数')
),
`updated_by` = 'system',
`updated_at` = NOW()
WHERE `template_type` = 'AGRICULTURAL' AND `is_deleted` = 0;

-- =====================================================
-- 3. 验证字段配置更新结果
-- =====================================================

SELECT
    `id`,
    `template_type`,
    `template_name`,
    `template_version`,
    JSON_LENGTH(`field_config`) as field_count,
    JSON_LENGTH(`validation_rules`) as rule_count,
    `enabled`
FROM `product_templates`
WHERE `is_deleted` = 0
ORDER BY `sort_order`;

-- =====================================================
-- 4. 验证字段配置内容示例
-- =====================================================

-- 查看FILING模板的前3个字段配置
SELECT
    'FILING' as template_type,
    JSON_EXTRACT(`field_config`, '$[0]') as field_1,
    JSON_EXTRACT(`field_config`, '$[1]') as field_2,
    JSON_EXTRACT(`field_config`, '$[2]') as field_3
FROM `product_templates`
WHERE `template_type` = 'FILING' AND `is_deleted` = 0
LIMIT 1;

-- 查看AGRICULTURAL模板的前3个字段配置
SELECT
    'AGRICULTURAL' as template_type,
    JSON_EXTRACT(`field_config`, '$[0]') as field_1,
    JSON_EXTRACT(`field_config`, '$[1]') as field_2,
    JSON_EXTRACT(`field_config`, '$[2]') as field_3
FROM `product_templates`
WHERE `template_type` = 'AGRICULTURAL' AND `is_deleted` = 0
LIMIT 1;

-- =====================================================
-- 脚本执行完成
-- 产品模板字段配置和验证规则已成功填充
-- FILING模板: 24个字段, 9条验证规则
-- AGRICULTURAL模板: 20个字段, 11条验证规则
-- =====================================================

SET SQL_SAFE_UPDATES = 1;
