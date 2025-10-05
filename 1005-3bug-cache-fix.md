# Template Download Cache Error Fix

## Error Description

When clicking the template download buttons, the following error occurs:

```
Cannot find cache named 'product_templates' for Builder[public
com.insurance.audit.product.domain.entity.ProductTemplate
com.insurance.audit.product.application.service.impl.TemplateServiceImpl.getTemplateMetadata(java.lang.String)]
caches=[product_templates] | key="metadata_" + #templateType' | keyGenerator="" | cacheManager="" |
cacheResolver="" | condition="" | unless='#result == null' | sync='false'
```

## Root Cause

The `CacheConfig.java` file did not include `"product_templates"` in the list of configured cache names, but `TemplateServiceImpl.java` uses `@Cacheable(value = "product_templates")` annotation.

## Fix Applied

### File: `backend/src/main/java/com/insurance/audit/common/config/CacheConfig.java`

**Line 52-59:**

**Before:**
```java
cacheManager.setCacheNames(java.util.Arrays.asList(
        "rulesets",           // 规则集缓存
        "ruleset-versions",   // 规则集版本缓存
        "documents",          // 文档缓存
        "audit-results",      // 检核结果缓存
        "performance-metrics" // 性能指标缓存
));
```

**After:**
```java
cacheManager.setCacheNames(java.util.Arrays.asList(
        "rulesets",           // 规则集缓存
        "ruleset-versions",   // 规则集版本缓存
        "documents",          // 文档缓存
        "audit-results",      // 检核结果缓存
        "performance-metrics", // 性能指标缓存
        "product_templates"   // 产品模板缓存
));
```

## Database Verification Required

Before testing, ensure the database templates are configured correctly:

### Step 1: Run Verification Script

```bash
mysql -u insurance_user -p insurance_audit < scripts/verify-and-update-templates.sql
```

This will:
1. Check if `product_templates` table exists
2. Display current template records
3. Update template paths to new files (if needed)
4. Verify the updates

### Step 2: Expected Database State

After running the verification script, you should see:

```sql
mysql> SELECT id, template_type, template_name, template_file_path, template_version FROM product_templates WHERE is_deleted = 0;

+---------------------------+---------------+---------------------------------------+--------------------------------------------------------+------------------+
| id                        | template_type | template_name                         | template_file_path                                     | template_version |
+---------------------------+---------------+---------------------------------------+--------------------------------------------------------+------------------+
| template_filing_001       | FILING        | 附件3_备案产品自主注册信息登记表      | /templates/附件3_备案产品自主注册信息登记表.xlsx       | 1.0.1            |
| template_agricultural_001 | AGRICULTURAL  | 附件5_农险产品信息登记表              | /templates/附件5_农险产品信息登记表.xls                | 1.0.1            |
+---------------------------+---------------+---------------------------------------+--------------------------------------------------------+------------------+
```

### Step 3: Verify Template Files

Ensure template files exist:

```bash
ls -lh backend/uploads/templates/
```

Expected output:
```
-rw-r--r-- 1 user group 83K 附件3_备案产品自主注册信息登记表.xlsx
-rw-r--r-- 1 user group 33K 附件5_农险产品信息登记表.xls
```

## Testing Steps

### 1. Restart Backend Service

```bash
cd backend
mvn clean spring-boot:run
```

**Verify** in logs:
```
初始化缓存管理器: defaultTtl=3600s, rulesetTtl=7200s, maxSize=1000, cleanupInterval=300s
缓存管理器初始化完成，支持的缓存: [rulesets, ruleset-versions, documents, audit-results, performance-metrics, product_templates]
```

### 2. Test Frontend

1. Navigate to: http://localhost:3000/product-management
2. Click "导入产品" button
3. Modal should display with two template cards
4. Click "下载模板 (.xlsx)" on FILING template
   - Should download: `附件3_备案产品自主注册信息登记表.xlsx`
   - No errors in browser console
5. Click "下载模板 (.xls)" on AGRICULTURAL template
   - Should download: `附件5_农险产品信息登记表.xls`
   - No errors in browser console

## Complete File Checklist

### Modified Files:
- ✅ `backend/src/main/java/com/insurance/audit/common/config/CacheConfig.java` - Added product_templates cache
- ✅ `frontend/src/components/ProductOperationsComponent.vue` - Added TemplateDownload component
- ✅ `frontend/src/components/product/TemplateDownload.vue` - Fixed template type values
- ✅ `frontend/src/views/product/Import.vue` - Fixed template type values
- ✅ `backend/src/main/resources/application-dev.yml` - Added file upload configuration

### Created Files:
- ✅ `backend/src/main/resources/db/migration/V9__Update_Template_Files.sql` - Database migration
- ✅ `scripts/deploy-templates.sh` - Linux deployment script
- ✅ `scripts/deploy-templates.bat` - Windows deployment script
- ✅ `scripts/verify-and-update-templates.sql` - Database verification script
- ✅ `1005-3bug-fix-report.md` - Initial fix report
- ✅ Template files in `backend/uploads/templates/`

## Common Issues and Solutions

### Issue 1: Cache Error Persists

**Solution:** Restart the backend service completely:
```bash
# Stop the service
Ctrl+C

# Clean and rebuild
mvn clean install

# Start again
mvn spring-boot:run
```

### Issue 2: Templates Not Found

**Error:** `模板文件不存在`

**Solution:** Verify file paths and permissions:
```bash
# Check files exist
ls -la backend/uploads/templates/

# Check permissions
chmod 755 backend/uploads/templates/
chmod 644 backend/uploads/templates/*.xls*
```

### Issue 3: Database Not Updated

**Error:** Old template names still showing

**Solution:** Manually run database update:
```sql
-- Connect to database
mysql -u insurance_user -p insurance_audit

-- Run update
source backend/src/main/resources/db/migration/V9__Update_Template_Files.sql;

-- Verify
SELECT * FROM product_templates WHERE is_deleted = 0;
```

### Issue 4: Download Returns 404

**Symptoms:** Download button triggers download but file not found

**Checklist:**
1. ✅ Backend service running on port 8080
2. ✅ Database templates updated with correct paths
3. ✅ Template files copied to `backend/uploads/templates/`
4. ✅ `app.file.upload-dir` configured in `application-dev.yml`
5. ✅ No typos in Chinese file names

## Summary of All Fixes

### Issue 1: Frontend Template Type Mismatch
- **Fixed:** TemplateDownload.vue and Import.vue to use 'FILING' and 'AGRICULTURAL'

### Issue 2: Modal Using Hardcoded Download
- **Fixed:** ProductOperationsComponent.vue to use TemplateDownload component

### Issue 3: Cache Not Configured
- **Fixed:** CacheConfig.java to include 'product_templates' cache

### Issue 4: Database Template Paths
- **Fixed:** Created V9 migration and verification script

### Issue 5: Template Files Not Deployed
- **Fixed:** Created deployment scripts and copied files

## Final Verification

Run all checks:

```bash
# 1. Check backend logs for cache initialization
grep "product_templates" backend/logs/application.log

# 2. Check database
mysql -u insurance_user -p -e "USE insurance_audit; SELECT * FROM product_templates WHERE is_deleted=0;"

# 3. Check files
ls -lh backend/uploads/templates/

# 4. Test download
curl -X GET "http://localhost:8080/api/v1/products/templates/FILING/file" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output test-filing.xlsx

curl -X GET "http://localhost:8080/api/v1/products/templates/AGRICULTURAL/file" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output test-agricultural.xls
```

All fixes are now complete! 🎉
