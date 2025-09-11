-- 初始化基础数据

-- 插入默认角色
INSERT INTO roles (id, role_name, role_type, description, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('role_admin_001', 'ADMIN', 'ADMIN', '系统管理员', 'system', NOW(), 'system', NOW(), 0, 0),
('role_business_001', 'BUSINESS_USER', 'BUSINESS_USER', '业务用户', 'system', NOW(), 'system', NOW(), 0, 0),
('role_audit_001', 'AUDIT_USER', 'AUDIT_USER', '审核用户', 'system', NOW(), 'system', NOW(), 0, 0);

-- 插入默认权限
INSERT INTO permissions (id, permission_name, description, resource_path, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
-- 用户管理权限
('perm_user_view', 'user:view', '查看用户信息', '/api/v1/users/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_user_create', 'user:create', '创建用户', '/api/v1/users', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_user_update', 'user:update', '更新用户信息', '/api/v1/users/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_user_delete', 'user:delete', '删除用户', '/api/v1/users/**', 'system', NOW(), 'system', NOW(), 0, 0),

-- 角色管理权限
('perm_role_view', 'role:view', '查看角色信息', '/api/v1/roles/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_role_create', 'role:create', '创建角色', '/api/v1/roles', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_role_update', 'role:update', '更新角色信息', '/api/v1/roles/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_role_delete', 'role:delete', '删除角色', '/api/v1/roles/**', 'system', NOW(), 'system', NOW(), 0, 0),

-- 权限管理权限
('perm_permission_view', 'permission:view', '查看权限信息', '/api/v1/permissions/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_permission_create', 'permission:create', '创建权限', '/api/v1/permissions', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_permission_update', 'permission:update', '更新权限信息', '/api/v1/permissions/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_permission_delete', 'permission:delete', '删除权限', '/api/v1/permissions/**', 'system', NOW(), 'system', NOW(), 0, 0),

-- 系统管理权限
('perm_system_config', 'system:config', '系统配置管理', '/api/v1/system/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_system_monitor', 'system:monitor', '系统监控', '/api/v1/monitor/**', 'system', NOW(), 'system', NOW(), 0, 0),

-- 个人信息权限
('perm_profile_view', 'profile:view', '查看个人信息', '/api/v1/profile', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_profile_update', 'profile:update', '更新个人信息', '/api/v1/profile', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version)
SELECT 
    CONCAT('rp_admin_', ROW_NUMBER() OVER (ORDER BY p.id)) as id,
    'role_admin_001' as role_id,
    p.id as permission_id,
    'system' as created_by,
    NOW() as created_at,
    'system' as updated_by,
    NOW() as updated_at,
    0 as is_deleted,
    0 as version
FROM permissions p WHERE p.is_deleted = 0;

-- 为业务用户角色分配基础权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('rp_business_001', 'role_business_001', 'perm_profile_view', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_002', 'role_business_001', 'perm_profile_update', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为审核用户角色分配审核权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('rp_audit_001', 'role_audit_001', 'perm_profile_view', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_audit_002', 'role_audit_001', 'perm_profile_update', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_audit_003', 'role_audit_001', 'perm_user_view', 'system', NOW(), 'system', NOW(), 0, 0);

-- 插入默认管理员用户（密码：admin123，使用BCrypt加密）
INSERT INTO users (id, username, password, real_name, email, phone, status, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('user_admin_001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '系统管理员', 'admin@example.com', '13800138000', 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为默认管理员分配管理员角色
INSERT INTO user_roles (id, user_id, role_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('ur_admin_001', 'user_admin_001', 'role_admin_001', 'system', NOW(), 'system', NOW(), 0, 0);