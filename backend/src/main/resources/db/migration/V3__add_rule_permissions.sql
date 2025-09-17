-- =====================================================
-- 规则管理权限完整定义
-- =====================================================

-- 插入规则管理相关权限
INSERT INTO permissions (id, permission_name, description, resource_path, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
-- 规则基础权限
('perm_rule_view', 'RULE_VIEW', '查看规则信息', '/api/v1/rules/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_create', 'RULE_CREATE', '创建规则', '/api/v1/rules', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_edit', 'RULE_EDIT', '编辑规则', '/api/v1/rules/**', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_delete', 'RULE_DELETE', '删除规则', '/api/v1/rules/**', 'system', NOW(), 'system', NOW(), 0, 0),

-- 规则状态管理权限
('perm_rule_audit', 'RULE_AUDIT', '规则审核', '/api/v1/rules/*/audit', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_approve', 'RULE_APPROVE', '规则审批', '/api/v1/rules/*/approve', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_publish', 'RULE_PUBLISH', '规则发布', '/api/v1/rules/*/publish', 'system', NOW(), 'system', NOW(), 0, 0),

-- 规则导入导出权限
('perm_rule_import', 'RULE_IMPORT', '规则导入', '/api/v1/rules/import', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_export', 'RULE_EXPORT', '规则导出', '/api/v1/rules/export', 'system', NOW(), 'system', NOW(), 0, 0),

-- 规则高级操作权限
('perm_rule_copy', 'RULE_COPY', '复制规则', '/api/v1/rules/*/copy', 'system', NOW(), 'system', NOW(), 0, 0),
('perm_rule_batch', 'RULE_BATCH', '批量操作规则', '/api/v1/rules/batch/**', 'system', NOW(), 'system', NOW(), 0, 0),

-- OA系统集成权限
('perm_rule_oa_submit', 'RULE_OA_SUBMIT', '提交OA审核', '/api/v1/rules/oa/submit', 'system', NOW(), 'system', NOW(), 0, 0),

-- 规则统计权限
('perm_rule_statistics', 'RULE_STATISTICS', '规则统计', '/api/v1/rules/statistics', 'system', NOW(), 'system', NOW(), 0, 0);

-- 创建规则管理相关角色
INSERT INTO roles (id, role_code, role_name, description, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('role_rule_admin', 'RULE_ADMIN', 'RULE_ADMIN', '规则管理员 - 拥有规则管理的所有权限', 'system', NOW(), 'system', NOW(), 0, 0),
('role_rule_auditor', 'RULE_AUDITOR', 'RULE_AUDITOR', '规则审核员 - 可以查看和审核规则', 'system', NOW(), 'system', NOW(), 0, 0),
('role_rule_viewer', 'RULE_VIEWER', 'RULE_VIEWER', '规则查看员 - 只能查看规则信息', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为规则管理员角色分配所有规则相关权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version)
SELECT
    CONCAT('rp_rule_admin_', LPAD(ROW_NUMBER() OVER (ORDER BY p.id), 3, '0')) as id,
    'role_rule_admin' as role_id,
    p.id as permission_id,
    'system' as created_by,
    NOW() as created_at,
    'system' as updated_by,
    NOW() as updated_at,
    0 as is_deleted,
    0 as version
FROM permissions p
WHERE p.permission_name LIKE 'RULE_%' AND p.is_deleted = 0;

-- 为规则审核员角色分配审核相关权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('rp_rule_auditor_001', 'role_rule_auditor', 'perm_rule_view', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_rule_auditor_002', 'role_rule_auditor', 'perm_rule_audit', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_rule_auditor_003', 'role_rule_auditor', 'perm_rule_approve', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_rule_auditor_004', 'role_rule_auditor', 'perm_rule_statistics', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_rule_auditor_005', 'role_rule_auditor', 'perm_rule_export', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为规则查看员角色分配只读权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('rp_rule_viewer_001', 'role_rule_viewer', 'perm_rule_view', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_rule_viewer_002', 'role_rule_viewer', 'perm_rule_statistics', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_rule_viewer_003', 'role_rule_viewer', 'perm_rule_export', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为现有的管理员角色添加规则管理权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version)
SELECT
    CONCAT('rp_admin_rule_', LPAD(ROW_NUMBER() OVER (ORDER BY p.id), 3, '0')) as id,
    'role_admin_001' as role_id,
    p.id as permission_id,
    'system' as created_by,
    NOW() as created_at,
    'system' as updated_by,
    NOW() as updated_at,
    0 as is_deleted,
    0 as version
FROM permissions p
WHERE p.permission_name LIKE 'RULE_%' AND p.is_deleted = 0;

-- 为现有的审核用户角色添加规则审核权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('rp_audit_rule_001', 'role_audit_001', 'perm_rule_view', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_audit_rule_002', 'role_audit_001', 'perm_rule_audit', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_audit_rule_003', 'role_audit_001', 'perm_rule_approve', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_audit_rule_004', 'role_audit_001', 'perm_rule_statistics', 'system', NOW(), 'system', NOW(), 0, 0);

-- 为现有的业务用户角色添加基础规则权限
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, is_deleted, version) VALUES
('rp_business_rule_001', 'role_business_001', 'perm_rule_view', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_002', 'role_business_001', 'perm_rule_create', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_003', 'role_business_001', 'perm_rule_edit', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_004', 'role_business_001', 'perm_rule_delete', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_005', 'role_business_001', 'perm_rule_import', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_006', 'role_business_001', 'perm_rule_export', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_007', 'role_business_001', 'perm_rule_copy', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_008', 'role_business_001', 'perm_rule_batch', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_009', 'role_business_001', 'perm_rule_oa_submit', 'system', NOW(), 'system', NOW(), 0, 0),
('rp_business_rule_010', 'role_business_001', 'perm_rule_statistics', 'system', NOW(), 'system', NOW(), 0, 0);