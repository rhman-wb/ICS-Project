-- =====================================================
-- Rule management permissions and roles
-- =====================================================

-- Upsert rule API permissions
INSERT INTO permissions (
    id,
    name,
    code,
    type,
    parent_id,
    path,
    component,
    icon,
    sort_order,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    is_deleted,
    version
)
VALUES
    ('perm_rule_view', 'RULE_VIEW', 'RULE_VIEW', 'API', NULL, '/api/v1/rules/**', NULL, NULL, 10, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_create', 'RULE_CREATE', 'RULE_CREATE', 'API', NULL, '/api/v1/rules', NULL, NULL, 20, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_edit', 'RULE_EDIT', 'RULE_EDIT', 'API', NULL, '/api/v1/rules/**', NULL, NULL, 30, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_delete', 'RULE_DELETE', 'RULE_DELETE', 'API', NULL, '/api/v1/rules/**', NULL, NULL, 40, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_audit', 'RULE_AUDIT', 'RULE_AUDIT', 'API', NULL, '/api/v1/rules/*/audit', NULL, NULL, 50, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_approve', 'RULE_APPROVE', 'RULE_APPROVE', 'API', NULL, '/api/v1/rules/*/approve', NULL, NULL, 60, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_publish', 'RULE_PUBLISH', 'RULE_PUBLISH', 'API', NULL, '/api/v1/rules/*/publish', NULL, NULL, 70, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_import', 'RULE_IMPORT', 'RULE_IMPORT', 'API', NULL, '/api/v1/rules/import', NULL, NULL, 80, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_export', 'RULE_EXPORT', 'RULE_EXPORT', 'API', NULL, '/api/v1/rules/export', NULL, NULL, 90, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_copy', 'RULE_COPY', 'RULE_COPY', 'API', NULL, '/api/v1/rules/*/copy', NULL, NULL, 100, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_batch', 'RULE_BATCH', 'RULE_BATCH', 'API', NULL, '/api/v1/rules/batch/**', NULL, NULL, 110, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_oa_submit', 'RULE_OA_SUBMIT', 'RULE_OA_SUBMIT', 'API', NULL, '/api/v1/rules/oa/submit', NULL, NULL, 120, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_rule_statistics', 'RULE_STATISTICS', 'RULE_STATISTICS', 'API', NULL, '/api/v1/rules/statistics', NULL, NULL, 130, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
    path = CASE WHEN COALESCE(permissions.path, '') = '' THEN VALUES(path) ELSE permissions.path END,
    status = VALUES(status),
    updated_by = VALUES(updated_by),
    updated_at = VALUES(updated_at),
    is_deleted = VALUES(is_deleted),
    version = VALUES(version);

-- Upsert rule-focused roles
INSERT INTO roles (
    id,
    name,
    code,
    description,
    status,
    created_by,
    created_at,
    updated_by,
    updated_at,
    is_deleted,
    version
)
VALUES
    ('role_rule_admin', 'RULE_ADMIN', 'RULE_ADMIN', 'Rule administrator - full access to rule management.', 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('role_rule_auditor', 'RULE_AUDITOR', 'RULE_AUDITOR', 'Rule auditor - can review and approve rules.', 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('role_rule_viewer', 'RULE_VIEWER', 'RULE_VIEWER', 'Rule viewer - read-only access to rules.', 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    status = VALUES(status),
    updated_by = VALUES(updated_by),
    updated_at = VALUES(updated_at),
    is_deleted = VALUES(is_deleted),
    version = VALUES(version);

-- Grant all rule permissions to the dedicated rule admin role
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT
    CONCAT('rp_rule_admin_', LPAD(ROW_NUMBER() OVER (ORDER BY p.code), 3, '0')) AS id,
    'role_rule_admin' AS role_id,
    p.id AS permission_id,
    'system' AS created_by,
    NOW() AS created_at
FROM permissions p
WHERE p.code LIKE 'RULE_%'
  AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_rule_admin'
        AND rp.permission_id = p.id
  );

-- Assign targeted rule permissions to the rule auditor role
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT
    CONCAT('rp_rule_auditor_', tp.seq) AS id,
    'role_rule_auditor' AS role_id,
    p.id AS permission_id,
    'system' AS created_by,
    NOW() AS created_at
FROM (
    SELECT 'RULE_VIEW' AS code, '001' AS seq UNION ALL
    SELECT 'RULE_AUDIT', '002' UNION ALL
    SELECT 'RULE_APPROVE', '003' UNION ALL
    SELECT 'RULE_STATISTICS', '004' UNION ALL
    SELECT 'RULE_EXPORT', '005'
) AS tp
JOIN permissions p ON p.code = tp.code
WHERE p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_rule_auditor'
        AND rp.permission_id = p.id
  );

-- Assign read-only permissions to the rule viewer role
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT
    CONCAT('rp_rule_viewer_', tp.seq) AS id,
    'role_rule_viewer' AS role_id,
    p.id AS permission_id,
    'system' AS created_by,
    NOW() AS created_at
FROM (
    SELECT 'RULE_VIEW' AS code, '001' AS seq UNION ALL
    SELECT 'RULE_STATISTICS', '002' UNION ALL
    SELECT 'RULE_EXPORT', '003'
) AS tp
JOIN permissions p ON p.code = tp.code
WHERE p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_rule_viewer'
        AND rp.permission_id = p.id
  );

-- Ensure existing admin role retains all rule permissions
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT
    CONCAT('rp_admin_rule_', LPAD(ROW_NUMBER() OVER (ORDER BY p.code), 3, '0')) AS id,
    'role_admin_001' AS role_id,
    p.id AS permission_id,
    'system' AS created_by,
    NOW() AS created_at
FROM permissions p
WHERE p.code LIKE 'RULE_%'
  AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_admin_001'
        AND rp.permission_id = p.id
  );

-- Ensure existing audit role gains rule oversight permissions
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT
    CONCAT('rp_audit_rule_', tp.seq) AS id,
    'role_audit_001' AS role_id,
    p.id AS permission_id,
    'system' AS created_by,
    NOW() AS created_at
FROM (
    SELECT 'RULE_VIEW' AS code, '001' AS seq UNION ALL
    SELECT 'RULE_AUDIT', '002' UNION ALL
    SELECT 'RULE_APPROVE', '003' UNION ALL
    SELECT 'RULE_STATISTICS', '004'
) AS tp
JOIN permissions p ON p.code = tp.code
WHERE p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_audit_001'
        AND rp.permission_id = p.id
  );

-- Ensure existing business role gains rule operational permissions
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT
    CONCAT('rp_business_rule_', tp.seq) AS id,
    'role_business_001' AS role_id,
    p.id AS permission_id,
    'system' AS created_by,
    NOW() AS created_at
FROM (
    SELECT 'RULE_VIEW' AS code, '001' AS seq UNION ALL
    SELECT 'RULE_CREATE', '002' UNION ALL
    SELECT 'RULE_EDIT', '003' UNION ALL
    SELECT 'RULE_DELETE', '004' UNION ALL
    SELECT 'RULE_IMPORT', '005' UNION ALL
    SELECT 'RULE_EXPORT', '006' UNION ALL
    SELECT 'RULE_COPY', '007' UNION ALL
    SELECT 'RULE_BATCH', '008' UNION ALL
    SELECT 'RULE_OA_SUBMIT', '009' UNION ALL
    SELECT 'RULE_STATISTICS', '010'
) AS tp
JOIN permissions p ON p.code = tp.code
WHERE p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_business_001'
        AND rp.permission_id = p.id
  );
