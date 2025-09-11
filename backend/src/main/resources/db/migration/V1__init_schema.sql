-- =====================================================
-- 保险产品智能检核系统 - 用户认证与权限管理数据库初始化脚本
-- Version: V1__init_schema.sql
-- Description: 创建用户、角色、权限相关表结构
-- =====================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 用户表 (users)
-- =====================================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` varchar(32) NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：ACTIVE-激活，INACTIVE-未激活，LOCKED-锁定',
  `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(45) DEFAULT NULL COMMENT '最后登录IP',
  `login_attempts` int NOT NULL DEFAULT '0' COMMENT '登录尝试次数',
  `locked_until` timestamp NULL DEFAULT NULL COMMENT '锁定截止时间',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 角色表 (roles)
-- =====================================================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` varchar(32) NOT NULL COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '角色状态：ACTIVE-激活，INACTIVE-未激活',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_role_name` (`role_name`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =====================================================
-- 3. 权限表 (permissions)
-- =====================================================
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id` varchar(32) NOT NULL COMMENT '权限ID',
  `permission_name` varchar(100) NOT NULL COMMENT '权限名称',
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
  `resource_type` varchar(50) DEFAULT NULL COMMENT '资源类型：MENU-菜单，BUTTON-按钮，API-接口',
  `resource_path` varchar(200) DEFAULT NULL COMMENT '资源路径',
  `description` varchar(200) DEFAULT NULL COMMENT '权限描述',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父权限ID',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '权限状态：ACTIVE-激活，INACTIVE-未激活',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_permission_name` (`permission_name`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =====================================================
-- 4. 用户角色关联表 (user_roles)
-- =====================================================
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `id` varchar(32) NOT NULL COMMENT '关联ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `role_id` varchar(32) NOT NULL COMMENT '角色ID',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =====================================================
-- 5. 角色权限关联表 (role_permissions)
-- =====================================================
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
  `id` varchar(32) NOT NULL COMMENT '关联ID',
  `role_id` varchar(32) NOT NULL COMMENT '角色ID',
  `permission_id` varchar(32) NOT NULL COMMENT '权限ID',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`),
  CONSTRAINT `fk_role_permissions_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_permissions_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================================
-- 6. 操作日志表 (operation_logs)
-- =====================================================
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` varchar(32) NOT NULL COMMENT '日志ID',
  `user_id` varchar(32) DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：LOGIN-登录，LOGOUT-退出，CREATE-创建，UPDATE-更新，DELETE-删除',
  `operation_module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `operation_desc` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法：GET，POST，PUT，DELETE',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `client_ip` varchar(45) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(1000) DEFAULT NULL COMMENT '用户代理',
  `execution_time` bigint DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` varchar(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作状态：SUCCESS-成功，FAILURE-失败',
  `error_message` text COMMENT '错误信息',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_module` (`operation_module`),
  KEY `idx_client_ip` (`client_ip`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =====================================================
-- 7. 初始化基础数据
-- =====================================================

-- 插入默认角色
INSERT INTO `roles` (`id`, `role_name`, `role_code`, `description`, `status`, `created_by`, `created_at`) VALUES
('role_admin_001', '系统管理员', 'ADMIN', '系统管理员，拥有所有权限', 'ACTIVE', 'system', NOW()),
('role_business_001', '业务用户', 'BUSINESS_USER', '业务用户，可进行产品上传和检核操作', 'ACTIVE', 'system', NOW()),
('role_audit_001', '审核用户', 'AUDIT_USER', '审核用户，可进行规则审核和检核结果审核', 'ACTIVE', 'system', NOW());

-- 插入默认权限
INSERT INTO `permissions` (`id`, `permission_name`, `permission_code`, `resource_type`, `resource_path`, `description`, `status`, `created_by`, `created_at`) VALUES
-- 系统管理权限
('perm_system_001', '系统管理', 'system:manage', 'MENU', '/system', '系统管理菜单', 'ACTIVE', 'system', NOW()),
('perm_user_001', '用户管理', 'user:manage', 'MENU', '/system/user', '用户管理菜单', 'ACTIVE', 'system', NOW()),
('perm_user_002', '用户查看', 'user:view', 'API', '/api/v1/users/**', '用户查看权限', 'ACTIVE', 'system', NOW()),
('perm_user_003', '用户创建', 'user:create', 'API', '/api/v1/users', '用户创建权限', 'ACTIVE', 'system', NOW()),
('perm_user_004', '用户更新', 'user:update', 'API', '/api/v1/users/**', '用户更新权限', 'ACTIVE', 'system', NOW()),
('perm_user_005', '用户删除', 'user:delete', 'API', '/api/v1/users/**', '用户删除权限', 'ACTIVE', 'system', NOW()),

-- 角色权限管理
('perm_role_001', '角色管理', 'role:manage', 'MENU', '/system/role', '角色管理菜单', 'ACTIVE', 'system', NOW()),
('perm_role_002', '角色查看', 'role:view', 'API', '/api/v1/roles/**', '角色查看权限', 'ACTIVE', 'system', NOW()),
('perm_role_003', '角色创建', 'role:create', 'API', '/api/v1/roles', '角色创建权限', 'ACTIVE', 'system', NOW()),
('perm_role_004', '角色更新', 'role:update', 'API', '/api/v1/roles/**', '角色更新权限', 'ACTIVE', 'system', NOW()),
('perm_role_005', '角色删除', 'role:delete', 'API', '/api/v1/roles/**', '角色删除权限', 'ACTIVE', 'system', NOW()),

-- 工作台权限
('perm_dashboard_001', '工作台', 'dashboard:view', 'MENU', '/dashboard', '工作台菜单', 'ACTIVE', 'system', NOW()),

-- 产品管理权限
('perm_product_001', '产品管理', 'product:manage', 'MENU', '/product', '产品管理菜单', 'ACTIVE', 'system', NOW()),
('perm_product_002', '产品查看', 'product:view', 'API', '/api/v1/products/**', '产品查看权限', 'ACTIVE', 'system', NOW()),
('perm_product_003', '产品上传', 'product:upload', 'API', '/api/v1/products/upload', '产品上传权限', 'ACTIVE', 'system', NOW()),

-- 规则管理权限
('perm_rule_001', '规则管理', 'rule:manage', 'MENU', '/rule', '规则管理菜单', 'ACTIVE', 'system', NOW()),
('perm_rule_002', '规则查看', 'rule:view', 'API', '/api/v1/rules/**', '规则查看权限', 'ACTIVE', 'system', NOW()),
('perm_rule_003', '规则创建', 'rule:create', 'API', '/api/v1/rules', '规则创建权限', 'ACTIVE', 'system', NOW()),
('perm_rule_004', '规则审核', 'rule:audit', 'API', '/api/v1/rules/audit/**', '规则审核权限', 'ACTIVE', 'system', NOW());

-- 设置权限层级关系
UPDATE `permissions` SET `parent_id` = 'perm_system_001' WHERE `permission_code` IN ('user:manage', 'role:manage');
UPDATE `permissions` SET `parent_id` = 'perm_user_001' WHERE `permission_code` IN ('user:view', 'user:create', 'user:update', 'user:delete');
UPDATE `permissions` SET `parent_id` = 'perm_role_001' WHERE `permission_code` IN ('role:view', 'role:create', 'role:update', 'role:delete');
UPDATE `permissions` SET `parent_id` = 'perm_product_001' WHERE `permission_code` IN ('product:view', 'product:upload');
UPDATE `permissions` SET `parent_id` = 'perm_rule_001' WHERE `permission_code` IN ('rule:view', 'rule:create', 'rule:audit');

-- 分配角色权限
-- 管理员拥有所有权限
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_by`, `created_at`)
SELECT 
    CONCAT('rp_admin_', LPAD(ROW_NUMBER() OVER (ORDER BY p.id), 3, '0')) as id,
    'role_admin_001' as role_id,
    p.id as permission_id,
    'system' as created_by,
    NOW() as created_at
FROM `permissions` p WHERE p.status = 'ACTIVE';

-- 业务用户权限（工作台、产品管理）
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_by`, `created_at`) VALUES
('rp_business_001', 'role_business_001', 'perm_dashboard_001', 'system', NOW()),
('rp_business_002', 'role_business_001', 'perm_product_001', 'system', NOW()),
('rp_business_003', 'role_business_001', 'perm_product_002', 'system', NOW()),
('rp_business_004', 'role_business_001', 'perm_product_003', 'system', NOW());

-- 审核用户权限（工作台、规则管理）
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_by`, `created_at`) VALUES
('rp_audit_001', 'role_audit_001', 'perm_dashboard_001', 'system', NOW()),
('rp_audit_002', 'role_audit_001', 'perm_rule_001', 'system', NOW()),
('rp_audit_003', 'role_audit_001', 'perm_rule_002', 'system', NOW()),
('rp_audit_004', 'role_audit_001', 'perm_rule_004', 'system', NOW());

-- 创建默认管理员用户（密码：admin123，使用BCrypt加密）
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `email`, `status`, `created_by`, `created_at`) VALUES
('user_admin_001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaYMountjdMSS', '系统管理员', 'admin@insurance-audit.com', 'ACTIVE', 'system', NOW());

-- 分配管理员角色
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`, `created_by`, `created_at`) VALUES
('ur_admin_001', 'user_admin_001', 'role_admin_001', 'system', NOW());

-- =====================================================
-- 8. 恢复外键检查
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 脚本执行完成
-- =====================================================