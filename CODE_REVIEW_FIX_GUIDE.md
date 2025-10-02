# 代码审查修复实施指南

本文档提供详细的分步修复指令,用于解决git提交 `f4021b10c0ce1e5586a3ed06d3009afae86b7dc1` 中发现的代码问题。

## 修复进度跟踪

- [x] 阶段1.1: 统一TemplateParser事件接口 - **已完成**
- [ ] 阶段1.2: 统一DynamicForm v-model绑定
- [ ] 阶段1.3: 修正EnhancedProductForm API和ref使用
- [ ] 阶段1.4: 修正中文乱码
- [ ] 阶段2.1: 定义DocumentValidationResult类型
- [ ] 阶段2.2: 明确模板类型与产品类型映射
- [ ] 阶段2.3: 完成模板解析最小可用实现
- [ ] 阶段3.1: 类型系统一致化
- [ ] 阶段3.2: 代码清理

---

## ✅ 阶段1.1: 统一TemplateParser事件接口 (已完成)

**修改文件**: `frontend/src/components/product/TemplateParser.vue`

### 修改内容
1. 事件定义修改为:
```typescript
const emit = defineEmits<{
  (e: 'parse-success', result: TemplateParseResult): void
  (e: 'parse-error', error: { message: string; errors?: any[] }): void
  (e: 'auto-fill', data: Partial<ProductFormData>): void
}>()
```

2. 事件发射修改:
- 成功时: `emit('parse-success', result)`
- 失败时: `emit('parse-error', { message: ..., errors: ... })`
- 自动填充: `emit('auto-fill', filledData)`

**验证**:
- ✓ 事件名称与Import.vue监听一致
- ✓ 错误事件增加详细错误信息

---

## 🔴 阶段1.2: 统一DynamicForm v-model绑定

**修改文件**: `frontend/src/views/product/Import.vue`

### 修改步骤

#### 步骤1: 修改DynamicForm组件绑定 (行137-143)

**查找代码**:
```vue
<DynamicForm
  ref="dynamicFormRef"
  v-model:formData="productFormData"
  :fields="formFields"
  :template-type="selectedTemplateType"
  :enable-validation="true"
  :show-actions="false"
  @field-change="handleFieldChange"
  @validation-change="handleValidationChange"
/>
```

**替换为**:
```vue
<DynamicForm
  ref="dynamicFormRef"
  v-model="productFormData"
  :fields="formFields"
  :template-type="selectedTemplateType"
  :enable-validation="true"
  :show-actions="false"
  @field-change="handleFieldChange"
  @validate="handleValidate"
/>
```

**变更说明**:
- 将 `v-model:formData` 改为标准 `v-model`
- 将 `@validation-change` 改为 `@validate`

#### 步骤2: 修改事件处理函数 (行362-364)

**查找代码**:
```typescript
const handleValidationChange = (valid: boolean) => {
  isFormValid.value = valid
}
```

**替换为**:
```typescript
const handleValidate = (validations: Record<string, any>) => {
  // Check if all fields are valid
  const allValid = Object.values(validations).every((v: any) => v.valid)
  isFormValid.value = allValid
}
```

**变更说明**:
- 函数名称从 `handleValidationChange` 改为 `handleValidate`
- 参数从简单的 `boolean` 改为 `Record<string, any>`
- 逻辑改为检查所有字段的验证结果

### 验证检查
- [ ] DynamicForm使用标准v-model
- [ ] 事件监听器使用@validate
- [ ] 处理函数正确处理验证结果对象

---

## 🔴 阶段1.3: 修正EnhancedProductForm API和ref使用

**修改文件**: `frontend/src/components/EnhancedProductForm.vue`

### 问题分析
1. **API返回结构误读**: 行377处理response.config,但实际应为response.data
2. **ref使用错误**: 多处直接访问formData而非formData.value

### 修改步骤

#### 步骤1: 修正API配置加载 (行372-386)

**查找代码** (约行376-378):
```typescript
const response = await templateApi.getTemplateConfig(type)
if (response && response.config) {
  templateConfig.value = response.config
}
```

**替换为**:
```typescript
const response = await templateApi.getTemplateConfig(type)
// 根据实际API返回结构处理
if (response && response.data) {
  templateConfig.value = response.data.config || response.data
}
```

**说明**: 需要先检查后端templateApi的实际返回结构,选择合适的访问路径

#### 步骤2: 修正formData ref访问 (多处)

**需要修改的位置**:

1. **行477-508 saveDraft函数**:

查找所有 `formData.xxx` 并替换为 `formData.value.xxx`:

```typescript
// 修改前
const productData: ProductCreateRequest = {
  productName: formData.productName,
  productType: formData.productType,
  // ... 其他字段
}

// 修改后
const productData: ProductCreateRequest = {
  productName: formData.value.productName,
  productType: formData.value.productType,
  // ... 其他字段
}
```

2. **行509 emit事件**:

```typescript
// 修改前
emit('formSave', { ...formData })

// 修改后
emit('formSave', { ...formData.value })
```

3. **行551 submitForm emit事件**:

```typescript
// 修改前
emit('formSubmit', { ...formData })

// 修改后
emit('formSubmit', { ...formData.value })
```

### 完整搜索替换
建议使用编辑器的查找替换功能,在EnhancedProductForm.vue中:
- 查找: `formData\.([a-zA-Z]+)` (正则)
- 替换为: `formData.value.$1`
- **注意**: 排除 `formData.value =` 赋值语句

### 验证检查
- [ ] templateConfig加载正确处理API响应
- [ ] 所有formData访问都使用.value
- [ ] emit事件传递formData.value而非formData

---

## 🔴 阶段1.4: 修正中文乱码

**影响文件**:
- `frontend/src/components/product/TemplateParser.vue`
- `frontend/src/utils/templateParsing.ts`
- 其他包含乱码的文件

### 修改策略
1. 全局搜索 `�?` 字符
2. 根据上下文推断正确的中文
3. 确保文件保存为 UTF-8 无BOM 格式

### 已知乱码位置及修复

#### TemplateParser.vue

| 行号 | 乱码文本 | 正确文本 |
|------|---------|---------|
| 约25 | 上�?...文件到此区域 | 上传文件到此区域 |

**修复方法**: 使用编辑器查找`�`并手动修正

#### templateParsing.ts

| 行号 | 乱码文本 | 正确文本 |
|------|---------|---------|
| 约86 | 最�?0MB | 最大10MB |

### 批量修复命令 (参考)

在项目根目录执行:
```bash
# 查找所有包含乱码的文件
grep -r "�" frontend/src/components/product/
grep -r "�" frontend/src/utils/

# 或使用ripgrep
rg "�" frontend/src/
```

### 验证检查
- [ ] 所有 `�?` 或 `�` 字符已修正
- [ ] 文件编码为 UTF-8 无BOM
- [ ] 中文显示正常

---

## 🟡 阶段2.1: 定义DocumentValidationResult类型

**修改文件**:
1. `frontend/src/types/product/template.ts` (新增类型)
2. `frontend/src/components/EnhancedProductForm.vue` (使用类型)

### 步骤1: 在types/product/template.ts添加类型定义

**位置**: 文件末尾或与其他接口一起

**添加内容**:
```typescript
/**
 * Document validation result interface
 * 文档校验结果接口
 */
export interface DocumentValidationResult {
  /** Whether all documents are valid */
  isValid: boolean

  /** Validation summary */
  summary: {
    /** Number of uploaded documents */
    uploadedDocuments: number
    /** Number of required documents */
    requiredDocuments: number
    /** Completeness percentage (0-100) */
    completenessPercentage: number
    /** Total error count */
    totalErrors: number
  }

  /** List of validation errors */
  errors: Array<{
    /** Document or field name */
    field: string
    /** Error message */
    message: string
    /** Error severity level */
    severity: 'error' | 'warning' | 'info'
  }>
}
```

### 步骤2: 在EnhancedProductForm.vue中使用类型

**查找代码** (约行348):
```typescript
const validationResult = ref<any>(null)
```

**替换为**:
```typescript
import type { DocumentValidationResult } from '@/types/product/template'

const validationResult = ref<DocumentValidationResult | null>(null)
```

### 步骤3: 更新相关类型引用

确保在文件顶部导入类型:
```typescript
import type {
  TemplateType,
  TemplateConfig,
  ProductFormData,
  ValidationResult,
  DocumentValidationResult  // 新增
} from '@/types/product/template'
```

### 验证检查
- [ ] DocumentValidationResult类型已定义
- [ ] EnhancedProductForm.vue正确导入类型
- [ ] validationResult使用明确类型而非any

---

## 🟡 阶段2.2: 明确模板类型与产品类型映射

**新建文件**: `frontend/src/config/templateMapping.ts`

### 步骤1: 创建映射配置文件

**文件路径**: `frontend/src/config/templateMapping.ts`

**文件内容**:
```typescript
/**
 * Template to Product Type Mapping Configuration
 * 模板类型与产品类型映射配置
 */

/**
 * Template type to product type mapping
 * 模板类型到产品类型的映射关系
 */
export const TEMPLATE_TO_PRODUCT_TYPE_MAP = {
  'backup': 'FILING',
  'agricultural': 'AGRICULTURAL'
} as const

/**
 * Template type definition
 */
export type TemplateType = keyof typeof TEMPLATE_TO_PRODUCT_TYPE_MAP

/**
 * Product type definition
 */
export type ProductType = typeof TEMPLATE_TO_PRODUCT_TYPE_MAP[TemplateType]

/**
 * Convert template type to product type
 * @param templateType - Template type
 * @returns Product type
 */
export const getProductTypeFromTemplate = (templateType: TemplateType): ProductType => {
  return TEMPLATE_TO_PRODUCT_TYPE_MAP[templateType]
}

/**
 * Convert product type to template type
 * @param productType - Product type
 * @returns Template type
 */
export const getTemplateTypeFromProduct = (productType: ProductType): TemplateType => {
  const entries = Object.entries(TEMPLATE_TO_PRODUCT_TYPE_MAP)
  const found = entries.find(([_, value]) => value === productType)
  return found?.[0] as TemplateType || 'backup'
}

/**
 * Get display name for template type
 * @param templateType - Template type
 * @returns Display name in Chinese
 */
export const getTemplateTypeDisplayName = (templateType: TemplateType): string => {
  const displayNames: Record<TemplateType, string> = {
    'backup': '备案产品',
    'agricultural': '农险产品'
  }
  return displayNames[templateType] || templateType
}

/**
 * Get display name for product type
 * @param productType - Product type
 * @returns Display name in Chinese
 */
export const getProductTypeDisplayName = (productType: ProductType): string => {
  const displayNames: Record<ProductType, string> = {
    'FILING': '备案产品',
    'AGRICULTURAL': '农险产品'
  }
  return displayNames[productType] || productType
}
```

### 步骤2: 在EnhancedProductForm.vue中使用映射

**修改行450-456的函数**:

```typescript
// 修改前
const getTemplateTypeText = (type: TemplateType | undefined | null): string => {
  const typeMap: Record<string, string> = {
    'backup': '备案产品',
    'agricultural': '农险产品'
  }
  return type ? typeMap[type] || type : '未选择'
}

const getProductTypeText = (productType: string): string => {
  const typeMap: Record<string, string> = {
    'AGRICULTURAL': '农险产品',
    'FILING': '备案产品'
  }
  return typeMap[productType] || productType
}

// 修改后
import {
  getTemplateTypeDisplayName,
  getProductTypeDisplayName,
  getProductTypeFromTemplate
} from '@/config/templateMapping'

const getTemplateTypeText = (type: TemplateType | undefined | null): string => {
  return type ? getTemplateTypeDisplayName(type) : '未选择'
}

const getProductTypeText = (productType: string): string => {
  return getProductTypeDisplayName(productType as ProductType)
}
```

### 步骤3: 保存/提交时使用映射转换

在saveDraft和submitForm函数中:

```typescript
// 转换模板类型为产品类型
const productType = selectedTemplateType.value
  ? getProductTypeFromTemplate(selectedTemplateType.value)
  : undefined

const productData: ProductCreateRequest = {
  // ... 其他字段
  productType: productType,  // 使用转换后的类型
  templateType: selectedTemplateType.value  // 保留原始模板类型
}
```

### 验证检查
- [ ] templateMapping.ts文件已创建
- [ ] 映射函数正确实现
- [ ] EnhancedProductForm使用映射函数
- [ ] 保存/提交时正确转换类型

---

## 🟡 阶段2.3: 完成模板解析最小可用实现

**修改文件**: `frontend/src/utils/templateParsing.ts`

### 当前问题
- parseTemplateFile返回mock数据
- 未实际读取Excel文件内容
- 恒定报缺失必填字段错误

### 修改策略
使用`xlsx`库实现基础Excel解析功能

### 步骤1: 安装依赖

```bash
cd frontend
npm install xlsx
npm install --save-dev @types/xlsx
```

### 步骤2: 修改parseTemplateFile函数

**查找代码** (约行134-221):

**替换为**:
```typescript
import * as XLSX from 'xlsx'

/**
 * Parse template file with actual Excel reading
 */
export async function parseTemplateFile(
  file: File,
  templateType: TemplateType
): Promise<TemplateParseResult> {
  const startTime = Date.now()
  const errors: ParseError[] = []
  const warnings: ParseWarning[] = []

  try {
    // Validate file format first
    const formatErrors = validateFileFormat(file, templateType)
    if (formatErrors.length > 0) {
      return {
        success: false,
        errors: formatErrors,
        metadata: {
          fileName: file.name,
          fileSize: file.size,
          parseTime: Date.now() - startTime,
          rowsProcessed: 0
        }
      }
    }

    // Read file as ArrayBuffer
    const arrayBuffer = await file.arrayBuffer()

    // Parse Excel file
    const workbook = XLSX.read(arrayBuffer, { type: 'array' })

    // Get first worksheet
    const firstSheetName = workbook.SheetNames[0]
    const worksheet = workbook.Sheets[firstSheetName]

    // Convert to JSON
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

    // Extract form data based on template type
    const formData = extractFormDataFromSheet(jsonData, templateType)

    // Validate extracted data
    const validation = validateParsedData(formData, templateType)
    errors.push(...validation.errors)
    warnings.push(...validation.warnings)

    const success = errors.filter(e => e.severity === 'error').length === 0

    return {
      success,
      data: formData,
      errors,
      warnings,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        parseTime: Date.now() - startTime,
        rowsProcessed: jsonData.length
      }
    }
  } catch (error: any) {
    console.error('Template parsing error:', error)

    errors.push({
      field: 'file',
      message: `文件解析失败: ${error.message || '未知错误'}`,
      severity: 'critical'
    })

    return {
      success: false,
      errors,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        parseTime: Date.now() - startTime,
        rowsProcessed: 0
      }
    }
  }
}

/**
 * Extract form data from Excel sheet data
 * 从Excel表格数据中提取表单数据
 */
function extractFormDataFromSheet(
  jsonData: any[][],
  templateType: TemplateType
): Partial<ProductFormData> {
  const formData: Partial<ProductFormData> = {
    templateType: templateType,
    year: new Date().getFullYear()
  }

  // 跳过标题行,从第二行开始读取数据
  if (jsonData.length < 2) {
    return formData
  }

  const dataRow = jsonData[1]  // 第二行是实际数据
  const mappings = getFieldMappings(templateType)

  // 根据字段映射提取数据
  mappings.forEach((mapping, index) => {
    const cellValue = dataRow[index]

    if (cellValue !== undefined && cellValue !== null && cellValue !== '') {
      const fieldKey = mapping.formField as keyof ProductFormData

      // 应用转换函数(如果有)
      if (mapping.transform) {
        formData[fieldKey] = mapping.transform(cellValue) as any
      } else {
        formData[fieldKey] = cellValue as any
      }
    }
  })

  return formData
}
```

### 步骤3: 更新字段映射

确保`BACKUP_TEMPLATE_MAPPINGS`和`AGRICULTURAL_TEMPLATE_MAPPINGS`的excelColumn使用数字索引而非字母:

```typescript
// 修改为使用索引
const BACKUP_TEMPLATE_MAPPINGS: FieldMapping[] = [
  { excelColumn: '0', formField: 'productName', required: true },
  { excelColumn: '1', formField: 'developmentType', required: true },
  // ... 其他映射
]
```

或者保持字母列名,在extractFormDataFromSheet中转换:

```typescript
// 列字母转索引
function columnToIndex(column: string): number {
  let index = 0
  for (let i = 0; i < column.length; i++) {
    index = index * 26 + (column.charCodeAt(i) - 'A'.charCodeAt(0) + 1)
  }
  return index - 1
}
```

### 验证检查
- [ ] xlsx依赖已安装
- [ ] parseTemplateFile实际读取Excel文件
- [ ] 字段映射正确提取数据
- [ ] 至少支持产品名称、开发类型、产品类别等关键字段

---

## 🟢 阶段3.1: 类型系统一致化

**修改文件**: `frontend/src/types/product/template.ts`

### 步骤1: 统一枚举定义

**在文件中添加**:

```typescript
/**
 * Field type enumeration
 * 字段类型枚举
 */
export enum FieldType {
  TEXT = 'text',
  NUMBER = 'number',
  TEXTAREA = 'textarea',
  SELECT = 'select',
  MULTI_SELECT = 'multi_select',
  DATE = 'date',
  RADIO = 'radio',
  CHECKBOX = 'checkbox'
}

/**
 * Validation rule type enumeration
 * 验证规则类型枚举
 */
export enum ValidationRuleType {
  REQUIRED = 'required',
  MIN_LENGTH = 'minLength',
  MAX_LENGTH = 'maxLength',
  MIN_VALUE = 'minValue',
  MAX_VALUE = 'maxValue',
  PATTERN = 'pattern',
  PHONE = 'phone',
  EMAIL = 'email',
  URL = 'url',
  NUMERIC = 'numeric',
  ALPHA = 'alpha',
  ALPHANUMERIC = 'alphanumeric',
  CUSTOM = 'custom'
}
```

### 步骤2: 更新FieldConfig接口

```typescript
/**
 * Field configuration interface
 * 字段配置接口
 */
export interface FieldConfig {
  name: string
  label: string
  type: FieldType | string  // 兼容字符串配置
  required?: boolean
  disabled?: boolean
  visible?: boolean
  placeholder?: string
  helpText?: string
  maxLength?: number
  min?: number
  max?: number
  options?: Array<{
    label: string
    value: string | number
  }>
  validationRules?: ValidationRule[]
  dependencies?: FieldDependency[]
  conditionalRequired?: {
    dependsOn: string
    values: any[]
  }
  order: number
}

/**
 * Validation rule interface
 * 验证规则接口
 */
export interface ValidationRule {
  type: ValidationRuleType | string  // 兼容字符串配置
  value?: any
  message: string
  validator?: string
}
```

### 步骤3: 在组件中标准化类型

在`DynamicForm.vue`和`FieldValidation.vue`中添加类型转换函数:

```typescript
/**
 * Normalize field type to enum
 */
const normalizeFieldType = (type: string | FieldType): FieldType => {
  if (Object.values(FieldType).includes(type as FieldType)) {
    return type as FieldType
  }
  // 默认返回text类型
  return FieldType.TEXT
}

/**
 * Normalize validation rule type to enum
 */
const normalizeRuleType = (type: string | ValidationRuleType): ValidationRuleType => {
  if (Object.values(ValidationRuleType).includes(type as ValidationRuleType)) {
    return type as ValidationRuleType
  }
  // 默认返回custom类型
  return ValidationRuleType.CUSTOM
}
```

### 验证检查
- [ ] FieldType和ValidationRuleType枚举已定义
- [ ] 接口支持枚举和字符串两种方式
- [ ] 组件内部使用枚举进行类型判断

---

## 🟢 阶段3.2: 代码清理

### 任务1: 封装日志工具

**新建文件**: `frontend/src/utils/logger.ts`

**文件内容**:
```typescript
/**
 * Logger utility for consistent logging across the application
 * 应用程序统一日志工具
 */

type LogLevel = 'log' | 'info' | 'warn' | 'error' | 'debug'

class Logger {
  private isDevelopment: boolean

  constructor() {
    this.isDevelopment = import.meta.env.DEV
  }

  /**
   * Log message (only in development)
   */
  log(...args: any[]): void {
    if (this.isDevelopment) {
      console.log('[LOG]', ...args)
    }
  }

  /**
   * Info message (only in development)
   */
  info(...args: any[]): void {
    if (this.isDevelopment) {
      console.info('[INFO]', ...args)
    }
  }

  /**
   * Warning message (always logged)
   */
  warn(...args: any[]): void {
    console.warn('[WARN]', ...args)
  }

  /**
   * Error message (always logged)
   */
  error(...args: any[]): void {
    console.error('[ERROR]', ...args)
  }

  /**
   * Debug message (only in development)
   */
  debug(...args: any[]): void {
    if (this.isDevelopment) {
      console.debug('[DEBUG]', ...args)
    }
  }
}

export const logger = new Logger()

// 默认导出
export default logger
```

### 任务2: 替换console.log

在所有组件文件中:

**查找**: `console.log(`
**替换为**: `logger.log(`

**查找**: `console.error(`
**替换为**: `logger.error(`

确保文件顶部导入:
```typescript
import { logger } from '@/utils/logger'
```

### 任务3: 清理未使用的导入

使用ESLint自动修复:

```bash
cd frontend
npm run lint -- --fix
```

或手动检查每个文件,移除未使用的import语句。

### 验证检查
- [ ] logger.ts文件已创建
- [ ] 所有console.log替换为logger.log
- [ ] 所有console.error替换为logger.error
- [ ] 未使用的导入已清理

---

## 测试验证清单

完成所有修复后,执行以下验证:

### 功能测试
- [ ] 模板解析成功触发parse-success事件
- [ ] 模板解析失败触发parse-error事件
- [ ] DynamicForm数据双向绑定正常
- [ ] 表单验证状态正确传递
- [ ] 模板类型与产品类型转换正确
- [ ] Excel文件成功解析并填充数据

### 代码质量
- [ ] TypeScript编译无错误: `npm run type-check`
- [ ] ESLint检查通过: `npm run lint`
- [ ] 无中文乱码
- [ ] 无未使用的import
- [ ] 生产环境无console.log输出

### 单元测试
- [ ] DynamicForm.spec.ts测试通过
- [ ] 其他相关组件测试通过

### E2E测试
- [ ] 产品导入完整流程可用
- [ ] 模板解析流程正常
- [ ] 表单提交流程正常

---

## 常见问题排查

### 问题1: 文件被linter频繁修改
**解决方案**:
1. 暂时禁用自动保存时的格式化
2. 批量修改后一次性运行`npm run lint -- --fix`

### 问题2: TypeScript类型错误
**解决方案**:
1. 确保所有新增类型已正确导入
2. 运行`npm run type-check`查看详细错误
3. 检查interface定义是否完整

### 问题3: Excel解析失败
**解决方案**:
1. 确认xlsx库已正确安装
2. 检查文件格式验证逻辑
3. 添加详细的error logging
4. 验证字段映射配置

---

## 修复提交建议

### 提交策略
建议分阶段提交,每个阶段单独commit:

```bash
# 阶段1: 事件和绑定修复
git add frontend/src/components/product/TemplateParser.vue
git add frontend/src/views/product/Import.vue
git commit -m "fix: 统一组件事件接口和v-model绑定

- TemplateParser使用parse-success/parse-error事件
- DynamicForm使用标准v-model绑定
- Import页面适配新事件接口"

# 阶段2: API和类型修复
git add frontend/src/components/EnhancedProductForm.vue
git add frontend/src/types/product/template.ts
git add frontend/src/config/templateMapping.ts
git commit -m "fix: 修正API访问和类型定义

- 修正EnhancedProductForm的ref使用
- 新增DocumentValidationResult类型
- 添加模板类型映射配置"

# 阶段3: 模板解析实现
git add frontend/src/utils/templateParsing.ts
git add frontend/package.json
git commit -m "feat: 实现Excel模板解析功能

- 集成xlsx库实现实际文件解析
- 支持备案产品和农险产品模板
- 添加字段映射和数据提取逻辑"

# 阶段4: 代码质量优化
git add frontend/src/utils/logger.ts
git add "frontend/src/**/*.vue"
git add "frontend/src/**/*.ts"
git commit -m "refactor: 代码清理和质量优化

- 添加统一日志工具
- 清理未使用的导入
- 修正中文乱码
- 统一类型系统"
```

---

## 附录: 快速修复脚本

### 批量查找乱码
```bash
#!/bin/bash
# 查找所有包含乱码的文件
echo "=== 查找乱码文件 ==="
rg "�" frontend/src/ -l

echo -e "\n=== 显示乱码内容 ==="
rg "�" frontend/src/ -C 2
```

### 批量类型检查
```bash
#!/bin/bash
cd frontend
echo "=== 运行TypeScript类型检查 ==="
npm run type-check

echo -e "\n=== 运行ESLint检查 ==="
npm run lint

echo -e "\n=== 运行单元测试 ==="
npm test
```

---

**文档版本**: 1.0
**最后更新**: 2025-10-01
**状态**: 等待实施
