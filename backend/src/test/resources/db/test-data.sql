-- 测试数据
-- 清理数据
DELETE FROM role_permissions WHERE 1=1;
DELETE FROM user_roles WHERE 1=1;
DELETE FROM permissions WHERE 1=1;
DELETE FROM roles WHERE 1=1;
DELETE FROM users WHERE 1=1;

-- 插入测试用户
INSERT INTO users (id, username, password, real_name, email, phone, avatar, status, login_failure_count, created_at, updated_at, is_deleted, version) VALUES
('test-user-id-001', 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjxSOpE6.jVgKlW', '测试用户', 'test@example.com', '13800138000', 'https://example.com/avatar.jpg', 'ACTIVE', 0, NOW(), NOW(), 0, 0),
('admin-user-id', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjxSOpE6.jVgKlW', '管理员', 'admin@example.com', '13800138001', 'https://example.com/admin-avatar.jpg', 'ACTIVE', 0, NOW(), NOW(), 0, 0),
('locked-user-id', 'lockeduser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjxSOpE6.jVgKlW', '锁定用户', 'locked@example.com', '13800138002', NULL, 'LOCKED', 5, NOW(), NOW(), 0, 0),
('inactive-user-id', 'inactiveuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjxSOpE6.jVgKlW', '停用用户', 'inactive@example.com', '13800138003', NULL, 'INACTIVE', 0, NOW(), NOW(), 0, 0);

-- 插入测试角色
INSERT INTO roles (id, name, code, description, status, created_at, updated_at, is_deleted, version) VALUES
('admin-role-id', '管理员', 'ADMIN', '系统管理员', 'ACTIVE', NOW(), NOW(), 0, 0),
('user-role-id', '普通用户', 'USER', '普通用户', 'ACTIVE', NOW(), NOW(), 0, 0),
('test-role-id', '测试角色', 'TEST', '测试角色', 'ACTIVE', NOW(), NOW(), 0, 0),
('audit-role-id', '审核用户', 'AUDIT', '审核用户', 'ACTIVE', NOW(), NOW(), 0, 0);

-- 插入测试权限
INSERT INTO permissions (id, name, code, type, parent_id, path, component, icon, sort_order, status, created_at, updated_at, is_deleted, version) VALUES
-- 菜单权限
('perm-user-menu', '用户管理', 'user:menu', 'MENU', NULL, '/user', 'UserManagement', 'user', 1, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-role-menu', '角色管理', 'role:menu', 'MENU', NULL, '/role', 'RoleManagement', 'team', 2, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-permission-menu', '权限管理', 'permission:menu', 'MENU', NULL, '/permission', 'PermissionManagement', 'lock', 3, 'ACTIVE', NOW(), NOW(), 0, 0),

-- 按钮权限
('perm-user-view', '用户查看', 'user:view', 'BUTTON', 'perm-user-menu', NULL, NULL, NULL, 1, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-user-create', '用户新增', 'user:create', 'BUTTON', 'perm-user-menu', NULL, NULL, NULL, 2, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-user-edit', '用户编辑', 'user:edit', 'BUTTON', 'perm-user-menu', NULL, NULL, NULL, 3, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-user-delete', '用户删除', 'user:delete', 'BUTTON', 'perm-user-menu', NULL, NULL, NULL, 4, 'ACTIVE', NOW(), NOW(), 0, 0),

('perm-role-view', '角色查看', 'role:view', 'BUTTON', 'perm-role-menu', NULL, NULL, NULL, 1, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-role-create', '角色新增', 'role:create', 'BUTTON', 'perm-role-menu', NULL, NULL, NULL, 2, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-role-edit', '角色编辑', 'role:edit', 'BUTTON', 'perm-role-menu', NULL, NULL, NULL, 3, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-role-delete', '角色删除', 'role:delete', 'BUTTON', 'perm-role-menu', NULL, NULL, NULL, 4, 'ACTIVE', NOW(), NOW(), 0, 0),

-- API权限
('perm-user-api', '用户API', 'user:api', 'API', NULL, '/api/users/*', NULL, NULL, 1, 'ACTIVE', NOW(), NOW(), 0, 0),
('perm-role-api', '角色API', 'role:api', 'API', NULL, '/api/roles/*', NULL, NULL, 2, 'ACTIVE', NOW(), NOW(), 0, 0),

-- 测试权限
('test-permission-id', '测试权限', 'test:view', 'BUTTON', NULL, NULL, NULL, NULL, 1, 'ACTIVE', NOW(), NOW(), 0, 0);

-- 插入用户角色关联
INSERT INTO user_roles (id, user_id, role_id, created_at, updated_at, is_deleted, version) VALUES
('ur-001', 'test-user-id-001', 'user-role-id', NOW(), NOW(), 0, 0),
('ur-002', 'test-user-id-001', 'test-role-id', NOW(), NOW(), 0, 0),
('ur-003', 'admin-user-id', 'admin-role-id', NOW(), NOW(), 0, 0),
('ur-004', 'admin-user-id', 'user-role-id', NOW(), NOW(), 0, 0),
('ur-005', 'locked-user-id', 'user-role-id', NOW(), NOW(), 0, 0),
('ur-006', 'inactive-user-id', 'user-role-id', NOW(), NOW(), 0, 0);

-- 插入角色权限关联
INSERT INTO role_permissions (id, role_id, permission_id, created_at, updated_at, is_deleted, version) VALUES
-- 管理员拥有所有权限
('rp-001', 'admin-role-id', 'perm-user-menu', NOW(), NOW(), 0, 0),
('rp-002', 'admin-role-id', 'perm-user-view', NOW(), NOW(), 0, 0),
('rp-003', 'admin-role-id', 'perm-user-create', NOW(), NOW(), 0, 0),
('rp-004', 'admin-role-id', 'perm-user-edit', NOW(), NOW(), 0, 0),
('rp-005', 'admin-role-id', 'perm-user-delete', NOW(), NOW(), 0, 0),
('rp-006', 'admin-role-id', 'perm-role-menu', NOW(), NOW(), 0, 0),
('rp-007', 'admin-role-id', 'perm-role-view', NOW(), NOW(), 0, 0),
('rp-008', 'admin-role-id', 'perm-role-create', NOW(), NOW(), 0, 0),
('rp-009', 'admin-role-id', 'perm-role-edit', NOW(), NOW(), 0, 0),
('rp-010', 'admin-role-id', 'perm-role-delete', NOW(), NOW(), 0, 0),
('rp-011', 'admin-role-id', 'perm-permission-menu', NOW(), NOW(), 0, 0),
('rp-012', 'admin-role-id', 'perm-user-api', NOW(), NOW(), 0, 0),
('rp-013', 'admin-role-id', 'perm-role-api', NOW(), NOW(), 0, 0),

-- 普通用户只有查看权限
('rp-014', 'user-role-id', 'perm-user-menu', NOW(), NOW(), 0, 0),
('rp-015', 'user-role-id', 'perm-user-view', NOW(), NOW(), 0, 0),
('rp-016', 'user-role-id', 'perm-role-menu', NOW(), NOW(), 0, 0),
('rp-017', 'user-role-id', 'perm-role-view', NOW(), NOW(), 0, 0),

-- 测试角色拥有测试权限
('rp-018', 'test-role-id', 'test-permission-id', NOW(), NOW(), 0, 0),

-- 审核用户拥有审核相关权限
('rp-019', 'audit-role-id', 'perm-user-view', NOW(), NOW(), 0, 0),
('rp-020', 'audit-role-id', 'perm-role-view', NOW(), NOW(), 0, 0);