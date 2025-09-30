# 产品模板管理 API 文档

## 概述

本文档描述产品模板增强功能相关的所有 API 端点。包括模板下载、字段配置、验证规则等功能。

**Base URL**: `/api/v1/products/templates`

**认证方式**: Bearer Token (JWT)

---

## API 端点列表

### 1. 获取所有启用的模板列表

**端点**: `GET /api/v1/products/templates`

**描述**: 获取所有已启用的产品模板列表

**请求头**:
- `Authorization`: Bearer {access_token}
- `Content-Type`: application/json

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": "template-id-1",
      "templateType": "FILING",
      "templateName": "备案产品自主注册信息登记表",
      "templateVersion": "1.0.0",
      "description": "备案产品模板",
      "enabled": true,
      "fileSize": 102400,
      "mimeType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "sortOrder": 1
    }
  ]
}
```

---

### 2. 下载模板文件

**端点**: `POST /api/v1/products/templates/download`

**描述**: 根据模板类型下载对应的Excel模板文件

**请求体**:
```json
{
  "templateType": "FILING"  // 或 "AGRICULTURAL"
}
```

**响应**:
- 成功: 返回文件流 (application/octet-stream)
- 失败: 返回错误JSON

**响应头**:
- `Content-Disposition`: attachment; filename="template.xlsx"
- `Content-Type`: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

---

### 3. 获取模板字段配置

**端点**: `GET /api/v1/products/templates/fields`

**描述**: 获取指定模板类型的字段配置列表

**请求参数**:
- `templateType` (required): 模板类型 (FILING/AGRICULTURAL)

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "fieldName": "productName",
      "fieldLabel": "产品名称",
      "fieldType": "text",
      "required": true,
      "maxLength": 200,
      "placeholder": "请输入产品名称",
      "helpText": "产品的完整名称",
      "defaultValue": null,
      "options": null,
      "dependsOn": null,
      "showWhen": null
    },
    {
      "fieldName": "productCode",
      "fieldLabel": "产品代码",
      "fieldType": "text",
      "required": true,
      "maxLength": 50,
      "pattern": "^[A-Z0-9]{6,20}$",
      "placeholder": "请输入产品代码",
      "helpText": "6-20位大写字母和数字"
    }
  ]
}
```

---

### 4. 获取模板验证规则

**端点**: `GET /api/v1/products/templates/validation-rules`

**描述**: 获取指定模板类型的字段验证规则

**请求参数**:
- `templateType` (required): 模板类型

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "validator": "productName",
      "type": "required",
      "message": "产品名称不能为空",
      "value": null
    },
    {
      "validator": "productCode",
      "type": "pattern",
      "value": "^[A-Z0-9]{6,20}$",
      "message": "产品代码格式不正确，应为6-20位大写字母和数字"
    },
    {
      "validator": "premium",
      "type": "range",
      "value": "0,999999999",
      "message": "保费金额必须在有效范围内"
    }
  ]
}
```

---

### 5. 验证产品字段

**端点**: `POST /api/v1/products/templates/validate-fields`

**描述**: 根据模板规则验证产品字段值

**请求体**:
```json
{
  "productId": "product-id-optional",
  "templateType": "FILING",
  "fieldValues": {
    "productName": "测试产品",
    "productCode": "TEST123456",
    "registrationNumber": "REG20240930001"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证完成",
  "data": {
    "valid": false,
    "errors": [
      {
        "fieldName": "insuranceCompany",
        "message": "保险公司名称不能为空",
        "errorCode": "required",
        "rejectedValue": null
      }
    ],
    "validatedFields": {
      "productName": "测试产品",
      "productCode": "TEST123456"
    }
  }
}
```

---

### 6. 解析模板文件

**端点**: `POST /api/v1/products/templates/parse`

**描述**: 上传并解析填好的模板Excel文件，提取产品数据

**请求**: Multipart Form Data
- `file` (required): Excel文件 (.xlsx 或 .xls)
- `templateType` (required): 模板类型

**响应**:
```json
{
  "code": 200,
  "message": "解析成功",
  "data": {
    "success": true,
    "parsedData": {
      "productName": "从模板解析的产品",
      "productCode": "PARSED001",
      "registrationNumber": "REG20240930002",
      "insuranceCompany": "ABC保险公司"
    },
    "warnings": [
      "某些可选字段未填写"
    ],
    "errors": []
  }
}
```

---

### 7. 获取模板元数据

**端点**: `GET /api/v1/products/templates/metadata`

**描述**: 获取模板的元数据信息（不包含大字段配置）

**请求参数**:
- `templateType` (required): 模板类型

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": "template-id",
    "templateType": "FILING",
    "templateName": "备案产品自主注册信息登记表",
    "templateVersion": "1.0.0",
    "description": "用于备案产品信息登记",
    "enabled": true,
    "fileSize": 102400,
    "mimeType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "sortOrder": 1,
    "createdAt": "2024-09-30T10:00:00Z",
    "updatedAt": "2024-09-30T10:00:00Z"
  }
}
```

---

## 数据模型

### TemplateType 枚举

```typescript
enum TemplateType {
  FILING = "FILING",           // 备案产品
  AGRICULTURAL = "AGRICULTURAL" // 农险产品
}
```

### FieldType 枚举

```typescript
enum FieldType {
  TEXT = "text",               // 文本输入
  TEXTAREA = "textarea",       // 多行文本
  NUMBER = "number",           // 数字输入
  DATE = "date",               // 日期选择
  SELECT = "select",           // 下拉选择
  CHECKBOX = "checkbox",       // 复选框
  RADIO = "radio"              // 单选按钮
}
```

### ValidationType 枚举

```typescript
enum ValidationType {
  REQUIRED = "required",       // 必填验证
  MINLENGTH = "minlength",     // 最小长度
  MAXLENGTH = "maxlength",     // 最大长度
  PATTERN = "pattern",         // 正则表达式
  RANGE = "range",             // 数值范围
  CUSTOM = "custom"            // 自定义验证
}
```

---

## 错误码

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 200 | 成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未授权访问 | 401 |
| 403 | 禁止访问 | 403 |
| 404 | 资源不存在 | 404 |
| 40001 | 无效的模板类型 | 400 |
| 40002 | 模板未找到 | 404 |
| 40003 | 模板已禁用 | 400 |
| 40004 | 文件解析失败 | 400 |
| 40005 | 字段验证失败 | 400 |
| 50001 | 服务器内部错误 | 500 |

---

## 使用示例

### 示例 1: 下载备案产品模板

```bash
curl -X POST \
  'http://localhost:8080/api/v1/products/templates/download' \
  -H 'Authorization: Bearer your-access-token' \
  -H 'Content-Type: application/json' \
  -d '{"templateType": "FILING"}' \
  --output filing-template.xlsx
```

### 示例 2: 获取字段配置

```bash
curl -X GET \
  'http://localhost:8080/api/v1/products/templates/fields?templateType=FILING' \
  -H 'Authorization: Bearer your-access-token'
```

### 示例 3: 验证产品字段

```bash
curl -X POST \
  'http://localhost:8080/api/v1/products/templates/validate-fields' \
  -H 'Authorization: Bearer your-access-token' \
  -H 'Content-Type: application/json' \
  -d '{
    "templateType": "FILING",
    "fieldValues": {
      "productName": "测试产品",
      "productCode": "TEST123456"
    }
  }'
```

---

## 注意事项

1. 所有API请求都需要在请求头中包含有效的JWT令牌
2. 模板文件大小限制为10MB
3. 字段验证是实时的，建议在用户输入时进行验证
4. 模板类型区分大小写，必须使用大写形式（FILING, AGRICULTURAL）
5. 文件下载接口返回的是二进制流，需要正确处理Content-Disposition头
6. 字段依赖关系会影响验证逻辑，需要按照依赖顺序进行验证

---

## 版本历史

- **v1.0.0** (2024-09-30): 初始版本
  - 支持备案产品和农险产品两种模板类型
  - 完整的字段配置和验证功能
  - 模板文件解析功能

---

## 联系支持

如有问题或建议，请联系开发团队或提交Issue。