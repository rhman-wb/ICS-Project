-- =====================================================
-- 保险产品智能检核系统 - 产品文档管理数据库表结构
-- Version: V3__product_tables.sql
-- Description: 创建产品管理相关表结构
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 产品表 (products)
-- =====================================================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` varchar(32) NOT NULL COMMENT '产品ID',
  `product_name` varchar(200) NOT NULL COMMENT '产品名称',
  `report_type` varchar(50) NOT NULL COMMENT '报送类型：新产品、修订产品',
  `product_nature` varchar(50) DEFAULT NULL COMMENT '产品性质',
  `year` int DEFAULT NULL COMMENT '年度',
  `revision_type` varchar(50) DEFAULT NULL COMMENT '修订类型：条款修订、费率修订、条款费率修订',
  `original_product_name` varchar(200) DEFAULT NULL COMMENT '原产品名称和编号/备案编号',
  `development_method` varchar(50) DEFAULT NULL COMMENT '开发方式/开发类型：自主开发、合作开发',
  `primary_additional` varchar(50) DEFAULT NULL COMMENT '主附险：主险、附险',
  `primary_situation` varchar(200) DEFAULT NULL COMMENT '主险情况',
  `product_category` varchar(50) DEFAULT NULL COMMENT '产品类别：种植险、养殖险、不区分等',
  `operating_region` varchar(200) DEFAULT NULL COMMENT '经营区域',
  `operating_scope` varchar(200) DEFAULT NULL COMMENT '经营范围（备案产品）',
  `demonstration_clause_name` varchar(200) DEFAULT NULL COMMENT '示范条款名称（备案产品）',
  `operating_region1` varchar(200) DEFAULT NULL COMMENT '经营区域1（备案产品）',
  `operating_region2` varchar(200) DEFAULT NULL COMMENT '经营区域2（备案产品）',
  `sales_promotion_name` varchar(200) DEFAULT NULL COMMENT '销售推广名称（备案产品）',
  `product_type` varchar(50) NOT NULL DEFAULT 'AGRICULTURAL' COMMENT '产品类型：AGRICULTURAL-农险产品，FILING-备案产品',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '产品状态：DRAFT-草稿，SUBMITTED-已提交，APPROVED-已审批，REJECTED-已拒绝',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_product_type` (`product_type`),
  KEY `idx_status` (`status`),
  KEY `idx_year` (`year`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- =====================================================
-- 2. 文档表 (documents)
-- =====================================================
DROP TABLE IF EXISTS `documents`;
CREATE TABLE `documents` (
  `id` varchar(32) NOT NULL COMMENT '文档ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型：DOCX、PDF、XLS、XLSX等',
  `document_type` varchar(50) NOT NULL COMMENT '要件类型：REGISTRATION-产品信息登记表，TERMS-条款，FEASIBILITY_REPORT-可行性报告，ACTUARIAL_REPORT-精算报告，RATE_TABLE-费率表',
  `product_id` varchar(32) NOT NULL COMMENT '关联产品ID',
  `upload_status` varchar(20) NOT NULL DEFAULT 'UPLOADED' COMMENT '上传状态：UPLOADED-已上传，PROCESSING-处理中，PROCESSED-已处理，FAILED-失败',
  `audit_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核，PROCESSING-审核中，COMPLETED-审核完成，FAILED-审核失败',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_document_type` (`document_type`),
  KEY `idx_upload_status` (`upload_status`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_file_name` (`file_name`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_documents_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- =====================================================
-- 3. 审核结果表 (audit_results)
-- =====================================================
DROP TABLE IF EXISTS `audit_results`;
CREATE TABLE `audit_results` (
  `id` varchar(32) NOT NULL COMMENT '审核结果ID',
  `document_id` varchar(32) NOT NULL COMMENT '关联文档ID',
  `rule_id` varchar(50) NOT NULL COMMENT '规则ID：如CC001、CC002等',
  `rule_type` varchar(50) NOT NULL COMMENT '规则类型：格式规则、内容规则等',
  `applicable_chapter` varchar(100) DEFAULT NULL COMMENT '适用章节：总则、保险责任、责任免除等',
  `rule_source` varchar(500) DEFAULT NULL COMMENT '规则来源：法规依据',
  `original_content` text COMMENT '原文内容：触发规则的原始文档内容',
  `suggestion` text COMMENT '修改建议：具体的修改建议和说明',
  `severity` varchar(20) NOT NULL DEFAULT 'INFO' COMMENT '严重程度：ERROR-错误，WARNING-警告，INFO-信息',
  `position_start` int DEFAULT NULL COMMENT '在文档中的起始位置',
  `position_end` int DEFAULT NULL COMMENT '在文档中的结束位置',
  `chapter_number` varchar(50) DEFAULT NULL COMMENT '章节编号',
  `page_number` int DEFAULT NULL COMMENT '页码',
  `line_number` int DEFAULT NULL COMMENT '行号',
  `audit_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_severity` (`severity`),
  KEY `idx_applicable_chapter` (`applicable_chapter`),
  KEY `idx_audit_time` (`audit_time`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_audit_results_document_id` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核结果表';

-- =====================================================
-- 4. 产品操作日志表 (product_operation_logs)
-- =====================================================
DROP TABLE IF EXISTS `product_operation_logs`;
CREATE TABLE `product_operation_logs` (
  `id` varchar(32) NOT NULL COMMENT '日志ID',
  `product_id` varchar(32) NOT NULL COMMENT '产品ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：CREATE-创建，UPDATE-更新，DELETE-删除，SUBMIT-提交，AUDIT-审核',
  `operation_desc` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `before_data` json DEFAULT NULL COMMENT '操作前数据（JSON格式）',
  `after_data` json DEFAULT NULL COMMENT '操作后数据（JSON格式）',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
  `operation_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `client_ip` varchar(45) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(1000) DEFAULT NULL COMMENT '用户代理',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operation_time` (`operation_time`),
  CONSTRAINT `fk_product_operation_logs_product_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品操作日志表';

-- =====================================================
-- 5. 文档章节表 (document_chapters)
-- =====================================================
DROP TABLE IF EXISTS `document_chapters`;
CREATE TABLE `document_chapters` (
  `id` varchar(32) NOT NULL COMMENT '章节ID',
  `document_id` varchar(32) NOT NULL COMMENT '关联文档ID',
  `chapter_name` varchar(100) NOT NULL COMMENT '章节名称',
  `chapter_number` varchar(50) DEFAULT NULL COMMENT '章节编号',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父章节ID',
  `level` int NOT NULL DEFAULT '1' COMMENT '章节级别：1-一级章节，2-二级章节等',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `content_start` int DEFAULT NULL COMMENT '内容起始位置',
  `content_end` int DEFAULT NULL COMMENT '内容结束位置',
  `page_start` int DEFAULT NULL COMMENT '起始页码',
  `page_end` int DEFAULT NULL COMMENT '结束页码',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_chapter_name` (`chapter_name`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_document_chapters_document_id` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_document_chapters_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `document_chapters` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档章节表';

-- =====================================================
-- 6. 批量操作记录表 (batch_operations)
-- =====================================================
DROP TABLE IF EXISTS `batch_operations`;
CREATE TABLE `batch_operations` (
  `id` varchar(32) NOT NULL COMMENT '批量操作ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：BATCH_DELETE-批量删除，BATCH_AUDIT-批量审核，BATCH_EXPORT-批量导出',
  `target_type` varchar(50) NOT NULL COMMENT '目标类型：PRODUCT-产品，DOCUMENT-文档',
  `target_ids` json NOT NULL COMMENT '目标ID列表（JSON数组）',
  `operation_status` varchar(20) NOT NULL DEFAULT 'PROCESSING' COMMENT '操作状态：PROCESSING-处理中，COMPLETED-已完成，FAILED-失败，CANCELLED-已取消',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '总数量',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功数量',
  `failed_count` int NOT NULL DEFAULT '0' COMMENT '失败数量',
  `error_message` text COMMENT '错误信息',
  `result_file_path` varchar(500) DEFAULT NULL COMMENT '结果文件路径（如导出文件）',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) NOT NULL COMMENT '操作人姓名',
  `start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_operation_status` (`operation_status`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批量操作记录表';

-- =====================================================
-- 7. 创建索引优化查询性能
-- =====================================================

-- 产品表复合索引
CREATE INDEX `idx_products_status_type_year` ON `products` (`status`, `product_type`, `year`);
CREATE INDEX `idx_products_report_category_region` ON `products` (`report_type`, `product_category`, `operating_region`(50));
CREATE INDEX `idx_products_created_time_status` ON `products` (`created_at`, `status`);

-- 文档表复合索引
CREATE INDEX `idx_documents_product_type_status` ON `documents` (`product_id`, `document_type`, `audit_status`);
CREATE INDEX `idx_documents_upload_audit_status` ON `documents` (`upload_status`, `audit_status`);
CREATE INDEX `idx_documents_created_product` ON `documents` (`created_at`, `product_id`);

-- 审核结果表复合索引
CREATE INDEX `idx_audit_results_document_rule` ON `audit_results` (`document_id`, `rule_id`);
CREATE INDEX `idx_audit_results_severity_chapter` ON `audit_results` (`severity`, `applicable_chapter`(50));
CREATE INDEX `idx_audit_results_audit_time_severity` ON `audit_results` (`audit_time`, `severity`);

-- 全文索引（如果支持）
-- ALTER TABLE `products` ADD FULLTEXT(`product_name`, `original_product_name`);
-- ALTER TABLE `documents` ADD FULLTEXT(`file_name`);

-- =====================================================
-- 8. 插入初始化数据
-- =====================================================

-- 插入示例产品数据（用于开发测试）
INSERT INTO `products` (`id`, `product_name`, `report_type`, `product_nature`, `year`, `revision_type`, `development_method`, `primary_additional`, `product_category`, `operating_region`, `product_type`, `status`, `created_by`, `created_at`) VALUES
('product_001', '中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险', '修订产品', '政策性农险', 2022, '条款修订', '自主开发', '主险', '种植险', '西藏自治区', 'AGRICULTURAL', 'SUBMITTED', 'user_admin_001', NOW()),
('product_002', '中国人寿财产保险股份有限公司安徽省水稻种植保险', '新产品', '政策性农险', 2023, NULL, '自主开发', '主险', '种植险', '安徽', 'AGRICULTURAL', 'DRAFT', 'user_admin_001', NOW()),
('product_003', '人保财险机动车辆保险条款（2020版）', '备案产品', '商业险', 2023, '条款费率修订', '自主开发', '主险', '不区分', '全国', 'FILING', 'APPROVED', 'user_admin_001', NOW());

-- 插入示例文档数据
INSERT INTO `documents` (`id`, `file_name`, `file_path`, `file_size`, `file_type`, `document_type`, `product_id`, `upload_status`, `audit_status`, `created_by`, `created_at`) VALUES
('doc_001', '中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险条款.docx', '/uploads/2024/09/product_001/terms.docx', 2048576, 'DOCX', 'TERMS', 'product_001', 'PROCESSED', 'COMPLETED', 'user_admin_001', NOW()),
('doc_002', '中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险费率表.xlsx', '/uploads/2024/09/product_001/rate_table.xlsx', 1024768, 'XLSX', 'RATE_TABLE', 'product_001', 'PROCESSED', 'COMPLETED', 'user_admin_001', NOW()),
('doc_003', '中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险可行性报告.pdf', '/uploads/2024/09/product_001/feasibility.pdf', 5242880, 'PDF', 'FEASIBILITY_REPORT', 'product_001', 'PROCESSED', 'PENDING', 'user_admin_001', NOW()),
('doc_004', '安徽省水稻种植保险产品信息登记表.xlsx', '/uploads/2024/09/product_002/registration.xlsx', 512000, 'XLSX', 'REGISTRATION', 'product_002', 'UPLOADED', 'PENDING', 'user_admin_001', NOW());

-- 插入示例审核结果数据
INSERT INTO `audit_results` (`id`, `document_id`, `rule_id`, `rule_type`, `applicable_chapter`, `rule_source`, `original_content`, `suggestion`, `severity`, `audit_time`, `created_by`, `created_at`) VALUES
('audit_001', 'doc_001', 'CC001', '格式规则', '总则', '行政监管措施决定书（京银保监发〔2022〕36号）', '第一条 保险责任', '标题格式应为：第一条【保险责任】，需要在标题中添加中括号', 'WARNING', NOW(), 'system', NOW()),
('audit_002', 'doc_001', 'CC002', '格式规则', '标题', '保险产品管理办法', '中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险条款', '标题格式不规范，应包含完整的公司名称和产品类型标识', 'ERROR', NOW(), 'system', NOW()),
('audit_003', 'doc_001', 'CC003', '内容规则', '保险责任', '农业保险条例', '本保险承保羊只死亡风险', '保险责任描述不够详细，应明确承保范围、免赔条件和赔付标准', 'INFO', NOW(), 'system', NOW());

-- 插入示例章节数据
INSERT INTO `document_chapters` (`id`, `document_id`, `chapter_name`, `chapter_number`, `parent_id`, `level`, `sort_order`, `content_start`, `content_end`, `created_at`) VALUES
('chapter_001', 'doc_001', '总则', '第一条', NULL, 1, 1, 0, 500, NOW()),
('chapter_002', 'doc_001', '保险责任', '第二条', NULL, 1, 2, 500, 1200, NOW()),
('chapter_003', 'doc_001', '责任免除', '第三条', NULL, 1, 3, 1200, 2000, NOW()),
('chapter_004', 'doc_001', '保险金额免赔额和给付比例', '第四条', NULL, 1, 4, 2000, 2800, NOW()),
('chapter_005', 'doc_001', '保险期间', '第五条', NULL, 1, 5, 2800, 3200, NOW()),
('chapter_006', 'doc_001', '投保人被保险人义务', '第六条', NULL, 1, 6, 3200, 4000, NOW());

-- =====================================================
-- 9. 恢复外键检查
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 脚本执行完成
-- =====================================================