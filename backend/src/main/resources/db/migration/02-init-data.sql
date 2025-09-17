-- 保险产品智能检核系统初始数据脚本
-- 创建时间: 2024-01-01
-- 版本: 1.0.0

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 插入系统角色
INSERT INTO `roles` (`id`, `name`, `code`, `description`, `status`, `created_by`, `created_at`) VALUES
('role_admin_001', '系统管理员', 'ADMIN', '系统管理员，拥有所有权限', 'ACTIVE', 'system', NOW()),
('role_business_001', '业务用户', 'BUSINESS_USER', '业务用户，可以上传产品和查看检核结果', 'ACTIVE', 'system', NOW()),
('role_audit_001', '审核用户', 'AUDIT_USER', '审核用户，可以配置规则和审核产品', 'ACTIVE', 'system', NOW());

-- 插入系统权限
INSERT INTO `permissions` (`id`, `name`, `code`, `type`, `parent_id`, `path`, `component`, `icon`, `sort_order`, `status`, `created_by`, `created_at`) VALUES
-- 主菜单
('perm_dashboard_001', '工作台', 'DASHBOARD', 'MENU', NULL, '/dashboard', 'Dashboard', 'dashboard', 1, 'ACTIVE', 'system', NOW()),
('perm_user_mgmt_001', '用户管理', 'USER_MANAGEMENT', 'MENU', NULL, '/user', 'UserManagement', 'user', 2, 'ACTIVE', 'system', NOW()),
('perm_product_mgmt_001', '产品管理', 'PRODUCT_MANAGEMENT', 'MENU', NULL, '/product', 'ProductManagement', 'product', 3, 'ACTIVE', 'system', NOW()),
('perm_rule_mgmt_001', '规则管理', 'RULE_MANAGEMENT', 'MENU', NULL, '/rule', 'RuleManagement', 'rule', 4, 'ACTIVE', 'system', NOW()),
('perm_audit_mgmt_001', '检核管理', 'AUDIT_MANAGEMENT', 'MENU', NULL, '/audit', 'AuditManagement', 'audit', 5, 'ACTIVE', 'system', NOW()),
('perm_system_mgmt_001', '系统管理', 'SYSTEM_MANAGEMENT', 'MENU', NULL, '/system', 'SystemManagement', 'setting', 6, 'ACTIVE', 'system', NOW()),

-- 用户管理权限
('perm_user_view_001', '查看用户', 'USER_VIEW', 'BUTTON', 'perm_user_mgmt_001', NULL, NULL, NULL, 1, 'ACTIVE', 'system', NOW()),
('perm_user_create_001', '创建用户', 'USER_CREATE', 'BUTTON', 'perm_user_mgmt_001', NULL, NULL, NULL, 2, 'ACTIVE', 'system', NOW()),
('perm_user_update_001', '更新用户', 'USER_UPDATE', 'BUTTON', 'perm_user_mgmt_001', NULL, NULL, NULL, 3, 'ACTIVE', 'system', NOW()),
('perm_user_delete_001', '删除用户', 'USER_DELETE', 'BUTTON', 'perm_user_mgmt_001', NULL, NULL, NULL, 4, 'ACTIVE', 'system', NOW()),
('perm_user_reset_pwd_001', '重置密码', 'USER_RESET_PASSWORD', 'BUTTON', 'perm_user_mgmt_001', NULL, NULL, NULL, 5, 'ACTIVE', 'system', NOW()),

-- 产品管理权限
('perm_product_view_001', '查看产品', 'PRODUCT_VIEW', 'BUTTON', 'perm_product_mgmt_001', NULL, NULL, NULL, 1, 'ACTIVE', 'system', NOW()),
('perm_product_upload_001', '上传产品', 'PRODUCT_UPLOAD', 'BUTTON', 'perm_product_mgmt_001', NULL, NULL, NULL, 2, 'ACTIVE', 'system', NOW()),
('perm_product_download_001', '下载产品', 'PRODUCT_DOWNLOAD', 'BUTTON', 'perm_product_mgmt_001', NULL, NULL, NULL, 3, 'ACTIVE', 'system', NOW()),
('perm_product_delete_001', '删除产品', 'PRODUCT_DELETE', 'BUTTON', 'perm_product_mgmt_001', NULL, NULL, NULL, 4, 'ACTIVE', 'system', NOW()),

-- 规则管理权限
('perm_rule_view_001', '查看规则', 'RULE_VIEW', 'BUTTON', 'perm_rule_mgmt_001', NULL, NULL, NULL, 1, 'ACTIVE', 'system', NOW()),
('perm_rule_create_001', '创建规则', 'RULE_CREATE', 'BUTTON', 'perm_rule_mgmt_001', NULL, NULL, NULL, 2, 'ACTIVE', 'system', NOW()),
('perm_rule_update_001', '更新规则', 'RULE_UPDATE', 'BUTTON', 'perm_rule_mgmt_001', NULL, NULL, NULL, 3, 'ACTIVE', 'system', NOW()),
('perm_rule_delete_001', '删除规则', 'RULE_DELETE', 'BUTTON', 'perm_rule_mgmt_001', NULL, NULL, NULL, 4, 'ACTIVE', 'system', NOW()),
('perm_rule_test_001', '测试规则', 'RULE_TEST', 'BUTTON', 'perm_rule_mgmt_001', NULL, NULL, NULL, 5, 'ACTIVE', 'system', NOW()),

-- 检核管理权限
('perm_audit_view_001', '查看检核', 'AUDIT_VIEW', 'BUTTON', 'perm_audit_mgmt_001', NULL, NULL, NULL, 1, 'ACTIVE', 'system', NOW()),
('perm_audit_execute_001', '执行检核', 'AUDIT_EXECUTE', 'BUTTON', 'perm_audit_mgmt_001', NULL, NULL, NULL, 2, 'ACTIVE', 'system', NOW()),
('perm_audit_result_001', '查看结果', 'AUDIT_RESULT', 'BUTTON', 'perm_audit_mgmt_001', NULL, NULL, NULL, 3, 'ACTIVE', 'system', NOW()),
('perm_audit_export_001', '导出结果', 'AUDIT_EXPORT', 'BUTTON', 'perm_audit_mgmt_001', NULL, NULL, NULL, 4, 'ACTIVE', 'system', NOW()),

-- 系统管理权限
('perm_system_config_001', '系统配置', 'SYSTEM_CONFIG', 'BUTTON', 'perm_system_mgmt_001', NULL, NULL, NULL, 1, 'ACTIVE', 'system', NOW()),
('perm_operation_log_001', '操作日志', 'OPERATION_LOG', 'BUTTON', 'perm_system_mgmt_001', NULL, NULL, NULL, 2, 'ACTIVE', 'system', NOW()),
('perm_system_monitor_001', '系统监控', 'SYSTEM_MONITOR', 'BUTTON', 'perm_system_mgmt_001', NULL, NULL, NULL, 3, 'ACTIVE', 'system', NOW());

-- 分配管理员权限（所有权限）
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_by`, `created_at`)
SELECT 
    CONCAT('rp_admin_', LPAD(ROW_NUMBER() OVER (ORDER BY id), 3, '0')),
    'role_admin_001',
    id,
    'system',
    NOW()
FROM `permissions`;

-- 分配业务用户权限
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_by`, `created_at`) VALUES
('rp_business_001', 'role_business_001', 'perm_dashboard_001', 'system', NOW()),
('rp_business_002', 'role_business_001', 'perm_product_mgmt_001', 'system', NOW()),
('rp_business_003', 'role_business_001', 'perm_product_view_001', 'system', NOW()),
('rp_business_004', 'role_business_001', 'perm_product_upload_001', 'system', NOW()),
('rp_business_005', 'role_business_001', 'perm_product_download_001', 'system', NOW()),
('rp_business_006', 'role_business_001', 'perm_audit_mgmt_001', 'system', NOW()),
('rp_business_007', 'role_business_001', 'perm_audit_view_001', 'system', NOW()),
('rp_business_008', 'role_business_001', 'perm_audit_result_001', 'system', NOW()),
('rp_business_009', 'role_business_001', 'perm_audit_export_001', 'system', NOW());

-- 分配审核用户权限
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_by`, `created_at`) VALUES
('rp_audit_001', 'role_audit_001', 'perm_dashboard_001', 'system', NOW()),
('rp_audit_002', 'role_audit_001', 'perm_product_mgmt_001', 'system', NOW()),
('rp_audit_003', 'role_audit_001', 'perm_product_view_001', 'system', NOW()),
('rp_audit_004', 'role_audit_001', 'perm_rule_mgmt_001', 'system', NOW()),
('rp_audit_005', 'role_audit_001', 'perm_rule_view_001', 'system', NOW()),
('rp_audit_006', 'role_audit_001', 'perm_rule_create_001', 'system', NOW()),
('rp_audit_007', 'role_audit_001', 'perm_rule_update_001', 'system', NOW()),
('rp_audit_008', 'role_audit_001', 'perm_rule_delete_001', 'system', NOW()),
('rp_audit_009', 'role_audit_001', 'perm_rule_test_001', 'system', NOW()),
('rp_audit_010', 'role_audit_001', 'perm_audit_mgmt_001', 'system', NOW()),
('rp_audit_011', 'role_audit_001', 'perm_audit_view_001', 'system', NOW()),
('rp_audit_012', 'role_audit_001', 'perm_audit_execute_001', 'system', NOW()),
('rp_audit_013', 'role_audit_001', 'perm_audit_result_001', 'system', NOW()),
('rp_audit_014', 'role_audit_001', 'perm_audit_export_001', 'system', NOW());

-- 创建默认管理员用户
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`, `created_by`, `created_at`) VALUES
('user_admin_001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaLO.GegzRbKi', '系统管理员', 'admin@insurance-audit.com', '13800138000', 'ACTIVE', 'system', NOW());

-- 创建测试用户
-- 密码: password123 (BCrypt加密后的值)
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `status`, `created_by`, `created_at`) VALUES
('user_business_001', 'business', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '业务用户', 'business@insurance-audit.com', '13800138001', 'ACTIVE', 'user_admin_001', NOW()),
('user_audit_001', 'auditor', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', '审核用户', 'auditor@insurance-audit.com', '13800138002', 'ACTIVE', 'user_admin_001', NOW());

-- 分配用户角色
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`, `created_by`, `created_at`) VALUES
('ur_admin_001', 'user_admin_001', 'role_admin_001', 'system', NOW()),
('ur_business_001', 'user_business_001', 'role_business_001', 'user_admin_001', NOW()),
('ur_audit_001', 'user_audit_001', 'role_audit_001', 'user_admin_001', NOW());

-- 插入系统配置
INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `config_type`, `description`, `is_system`, `created_by`, `created_at`) VALUES
('config_001', 'system.name', '保险产品智能检核系统', 'STRING', '系统名称', 1, 'system', NOW()),
('config_002', 'system.version', '1.0.0', 'STRING', '系统版本', 1, 'system', NOW()),
('config_003', 'system.company', '保险科技有限公司', 'STRING', '公司名称', 1, 'system', NOW()),
('config_004', 'login.max_attempts', '5', 'NUMBER', '最大登录尝试次数', 1, 'system', NOW()),
('config_005', 'login.lock_duration', '1800', 'NUMBER', '账户锁定时长(秒)', 1, 'system', NOW()),
('config_006', 'file.max_size', '104857600', 'NUMBER', '文件最大上传大小(字节)', 1, 'system', NOW()),
('config_007', 'file.allowed_types', 'pdf,doc,docx,xls,xlsx,jpg,jpeg,png', 'STRING', '允许上传的文件类型', 1, 'system', NOW()),
('config_008', 'jwt.access_token_expiration', '3600', 'NUMBER', 'JWT访问令牌过期时间(秒)', 1, 'system', NOW()),
('config_009', 'jwt.refresh_token_expiration', '604800', 'NUMBER', 'JWT刷新令牌过期时间(秒)', 1, 'system', NOW()),
('config_010', 'password.min_length', '8', 'NUMBER', '密码最小长度', 1, 'system', NOW()),
('config_011', 'password.require_uppercase', 'true', 'BOOLEAN', '密码是否需要大写字母', 1, 'system', NOW()),
('config_012', 'password.require_lowercase', 'true', 'BOOLEAN', '密码是否需要小写字母', 1, 'system', NOW()),
('config_013', 'password.require_digit', 'true', 'BOOLEAN', '密码是否需要数字', 1, 'system', NOW()),
('config_014', 'password.require_special', 'true', 'BOOLEAN', '密码是否需要特殊字符', 1, 'system', NOW());

SET FOREIGN_KEY_CHECKS = 1;