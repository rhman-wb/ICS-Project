# Template File Path Fix - Critical Issue

## Problem Identified

The database has template paths with a **leading slash** (`/templates/...`), but the FileStorageService expects **relative paths** (`templates/...`).

### Current State (WRONG):
```
Database: /templates/附件3_备案产品自主注册信息登记表.xlsx
FileStorageService combines: uploads + /templates/... = uploads/templates/... (INCORRECT)
Actual file location: backend/uploads/templates/附件3_备案产品自主注册信息登记表.xlsx
```

### Required State (CORRECT):
```
Database: templates/附件3_备案产品自主注册信息登记表.xlsx
FileStorageService combines: uploads + templates/... = uploads/templates/... (CORRECT)
File location matches: backend/uploads/templates/附件3_备案产品自主注册信息登记表.xlsx
```

## Quick Fix - Run This SQL Now!

**Option 1: Using MySQL Command Line**

```bash
mysql -u insurance_user -p insurance_audit < D:/ClaudeCodeProject/ICS-Project/scripts/fix-template-paths.sql
```

**Option 2: Using MySQL Workbench or GUI**

```sql
USE insurance_audit;

-- Update FILING template path (remove leading slash)
UPDATE product_templates
SET
    template_file_path = 'templates/附件3_备案产品自主注册信息登记表.xlsx',
    updated_by = 'system',
    updated_at = NOW()
WHERE
    template_type = 'FILING'
    AND is_deleted = 0;

-- Update AGRICULTURAL template path (remove leading slash)
UPDATE product_templates
SET
    template_file_path = 'templates/附件5_农险产品信息登记表.xls',
    updated_by = 'system',
    updated_at = NOW()
WHERE
    template_type = 'AGRICULTURAL'
    AND is_deleted = 0;

-- Verify the fix
SELECT
    template_type,
    template_name,
    template_file_path,
    CONCAT('uploads/', template_file_path) AS full_path
FROM
    product_templates
WHERE
    is_deleted = 0;
```

## Expected Result After Fix

```
+---------------+---------------------------------------+-------------------------------------------------------+------------------------------------------------------------------+
| template_type | template_name                         | template_file_path                                    | full_path                                                        |
+---------------+---------------------------------------+-------------------------------------------------------+------------------------------------------------------------------+
| FILING        | 附件3_备案产品自主注册信息登记表      | templates/附件3_备案产品自主注册信息登记表.xlsx       | uploads/templates/附件3_备案产品自主注册信息登记表.xlsx          |
| AGRICULTURAL  | 附件5_农险产品信息登记表              | templates/附件5_农险产品信息登记表.xls                | uploads/templates/附件5_农险产品信息登记表.xls                   |
+---------------+---------------------------------------+-------------------------------------------------------+------------------------------------------------------------------+
```

## Testing After Fix

### 1. Verify Files Exist

```bash
ls -lh D:/ClaudeCodeProject/ICS-Project/backend/uploads/templates/
```

Should show:
```
-rw-r--r-- 1 user 197121 83K 附件3_备案产品自主注册信息登记表.xlsx
-rw-r--r-- 1 user 197121 33K 附件5_农险产品信息登记表.xls
```

### 2. Restart Backend (NOT NEEDED if only database changed)

The cache will pick up the new paths automatically on next request.

### 3. Test Download

1. Navigate to: http://localhost:3000/product-management
2. Click "导入产品"
3. Click "下载模板 (.xlsx)" for FILING template
   - ✅ Should download successfully
   - ✅ File name: `附件3_备案产品自主注册信息登记表.xlsx`
4. Click "下载模板 (.xls)" for AGRICULTURAL template
   - ✅ Should download successfully
   - ✅ File name: `附件5_农险产品信息登记表.xls`

## Root Cause Analysis

### Why This Happened

The original V7 migration script created paths with leading slashes:
```sql
-- V7__Add_Template_Support.sql (INCORRECT)
INSERT INTO `product_templates` (..., `template_file_path`, ...) VALUES
('template_filing_001', 'FILING', '备案产品自主注册信息登记表', '/templates/filing_product_template.xlsx', ...);
```

### How FileStorageService Works

```java
// FileStorageServiceImpl.java
public String getFullPath(String filePath) {
    Path base = Paths.get(uploadDir).toAbsolutePath().normalize();  // "uploads"
    Path fullPath = base.resolve(filePath).normalize();              // "uploads" + filePath
    return fullPath.toString();
}
```

When `filePath` starts with `/`, `Paths.resolve()` treats it as an absolute path and ignores the base directory!

### The Fix

Remove leading slashes from all template file paths in the database:
- ❌ `/templates/file.xlsx`
- ✅ `templates/file.xlsx`

## Files Updated

1. **`backend/src/main/resources/db/migration/V9__Update_Template_Files.sql`**
   - Fixed to use relative paths without leading slash

2. **`scripts/verify-and-update-templates.sql`**
   - Fixed to use relative paths without leading slash

3. **`scripts/fix-template-paths.sql`** (NEW)
   - Quick fix script to correct existing database records

## Prevention for Future

### Rule for Template Paths

**Always use relative paths in database:**
- ✅ `templates/file.xlsx`
- ✅ `documents/2024/file.pdf`
- ✅ `uploads/images/logo.png`

**Never use absolute paths:**
- ❌ `/templates/file.xlsx`
- ❌ `/var/uploads/file.xlsx`
- ❌ `C:\uploads\file.xlsx`

### Verification Before Inserting

```sql
-- Good practice: Verify path format before insert
SELECT
    CASE
        WHEN template_file_path LIKE '/%' THEN 'ERROR: Absolute path'
        WHEN template_file_path LIKE '%//%' THEN 'ERROR: Double slash'
        ELSE 'OK'
    END AS path_check,
    template_file_path
FROM product_templates;
```

## Complete Checklist

- [x] Fixed V9 migration script
- [x] Fixed verification script
- [x] Created quick fix script
- [ ] **Run fix-template-paths.sql (DO THIS NOW!)**
- [ ] Verify database shows correct paths
- [ ] Test template downloads
- [ ] Confirm both templates download successfully

## Summary

**The Issue:** Leading slash in file paths breaks Path.resolve()
**The Fix:** Remove leading slash from database paths
**Action Required:** Run `scripts/fix-template-paths.sql` immediately
**Expected Time:** 30 seconds to fix
**Testing:** Download both templates to verify

Run the SQL fix script now, then test! 🚀
