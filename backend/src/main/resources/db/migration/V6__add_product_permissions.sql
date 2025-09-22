-- =====================================================
-- Product management permissions and role assignments
-- =====================================================

-- Upsert product API permissions (idempotent)
INSERT INTO permissions (
    id, name, code, type, parent_id, path, component, icon, sort_order, status,
    created_by, created_at, updated_by, updated_at, is_deleted, version
) VALUES
    ('perm_product_view', 'PRODUCT_VIEW', 'PRODUCT_VIEW', 'API', NULL, '/api/v1/products/**', NULL, NULL, 10, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_product_create', 'PRODUCT_CREATE', 'PRODUCT_CREATE', 'API', NULL, '/api/v1/products', NULL, NULL, 20, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0),
    ('perm_product_submit', 'PRODUCT_SUBMIT', 'PRODUCT_SUBMIT', 'API', NULL, '/api/v1/products/*/submit', NULL, NULL, 30, 'ACTIVE', 'system', NOW(), 'system', NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
    path = CASE WHEN COALESCE(permissions.path, '') = '' THEN VALUES(path) ELSE permissions.path END,
    status = VALUES(status),
    updated_by = VALUES(updated_by),
    updated_at = VALUES(updated_at),
    is_deleted = VALUES(is_deleted),
    version = VALUES(version);

-- Grant product permissions to admin role
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT CONCAT('rp_admin_product_', LPAD(ROW_NUMBER() OVER (ORDER BY p.code), 3, '0')),
       'role_admin_001', p.id, 'system', NOW()
FROM permissions p
WHERE p.code IN ('PRODUCT_VIEW','PRODUCT_CREATE','PRODUCT_SUBMIT')
  AND p.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id='role_admin_001' AND rp.permission_id = p.id
  );

-- Also grant basic view permission to business user role
INSERT INTO role_permissions (id, role_id, permission_id, created_by, created_at)
SELECT CONCAT('rp_business_product_', LPAD(ROW_NUMBER() OVER (ORDER BY p.code), 3, '0')),
       'role_business_001', p.id, 'system', NOW()
FROM permissions p
WHERE p.code IN ('PRODUCT_VIEW')
  AND p.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id='role_business_001' AND rp.permission_id = p.id
  );