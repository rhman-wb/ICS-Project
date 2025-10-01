/**
 * 产品模板增强功能的类型定义
 * 与后端 DTO 和需求保持一致
 */

// ========================================
// 模板类型和枚举
// ========================================

/**
 * 模板类型标识符
 */
export type TemplateType = 'backup' | 'agricultural'

/**
 * 模板信息
 */
export interface TemplateInfo {
  /** 模板标识符 */
  id: string
  /** 模板类型 */
  type: TemplateType
  /** 模板显示名称 */
  name: string
  /** 模板描述 */
  description: string
  /** 模板文件格式 */
  fileFormat: 'xlsx' | 'xls'
  /** 字段数量 */
  fieldCount: number
  /** 模板版本 */
  version: string
  /** 模板在服务器上的文件路径 */
  filePath?: string
  /** 模板是否启用 */
  enabled: boolean
  /** 创建时间戳 */
  createdAt?: string
  /** 最后更新时间戳 */
  updatedAt?: string
}

// ========================================
// 字段配置类型
// ========================================

/**
 * 字段类型枚举
 */
export enum FieldType {
  TEXT = 'text',
  NUMBER = 'number',
  DATE = 'date',
  SELECT = 'select',
  MULTI_SELECT = 'multi_select',
  TEXTAREA = 'textarea',
  RADIO = 'radio',
  CHECKBOX = 'checkbox'
}

/**
 * Field configuration for dynamic form rendering
 */
export interface FieldConfig {
  /** Field name/key */
  name: string
  /** Field label for display */
  label: string
  /** Field type */
  type: FieldType
  /** Whether field is required */
  required: boolean
  /** Conditional required based on other field values */
  conditionalRequired?: {
    dependsOn: string
    values: any[]
  }
  /** Field placeholder text */
  placeholder?: string
  /** Default value */
  defaultValue?: any
  /** Options for select/radio fields */
  options?: FieldOption[]
  /** Validation rules */
  validationRules?: ValidationRule[]
  /** Field dependencies for dynamic display */
  dependencies?: FieldDependency[]
  /** Field display order */
  order: number
  /** Maximum length for text fields */
  maxLength?: number
  /** Minimum value for number fields */
  min?: number
  /** Maximum value for number fields */
  max?: number
  /** Help text or description */
  helpText?: string
  /** Whether field is disabled */
  disabled?: boolean
  /** Whether field is visible */
  visible?: boolean
  /** Field group for organization */
  group?: string
}

/**
 * Field option for select/radio fields
 */
export interface FieldOption {
  /** Option value */
  value: string | number
  /** Option display label */
  label: string
  /** Whether option is disabled */
  disabled?: boolean
}

/**
 * Field dependency configuration
 */
export interface FieldDependency {
  /** Field name this depends on */
  dependsOn: string
  /** Condition type */
  condition: 'equals' | 'notEquals' | 'in' | 'notIn'
  /** Values to check against */
  values: any[]
  /** Action to take when condition is met */
  action: 'show' | 'hide' | 'enable' | 'disable' | 'require'
}

// ========================================
// Validation Types
// ========================================

/**
 * Validation rule type enum
 */
export enum ValidationRuleType {
  REQUIRED = 'required',
  MIN_LENGTH = 'minLength',
  MAX_LENGTH = 'maxLength',
  MIN_VALUE = 'minValue',
  MAX_VALUE = 'maxValue',
  PATTERN = 'pattern',
  CUSTOM = 'custom',
  PHONE = 'phone',
  EMAIL = 'email',
  URL = 'url',
  NUMERIC = 'numeric',
  ALPHA = 'alpha',
  ALPHANUMERIC = 'alphanumeric'
}

/**
 * Validation rule configuration
 */
export interface ValidationRule {
  /** Rule type */
  type: ValidationRuleType
  /** Rule value/parameter */
  value?: any
  /** Error message to display */
  message: string
  /** Custom validation function name */
  validator?: string
}

/**
 * Field validation result
 */
export interface ValidationResult {
  /** Whether validation passed */
  valid: boolean
  /** Error messages if validation failed */
  errors: string[]
  /** Warning messages */
  warnings?: string[]
}

/**
 * Form validation result
 */
export interface FormValidationResult {
  /** Whether entire form is valid */
  valid: boolean
  /** Field-level validation results */
  fieldResults: Record<string, ValidationResult>
  /** Global form errors */
  globalErrors?: string[]
}

// ========================================
// Template Configuration
// ========================================

/**
 * Complete template configuration
 */
export interface TemplateConfig {
  /** Template information */
  templateInfo: TemplateInfo
  /** Field configurations */
  fields: FieldConfig[]
  /** Validation rules by field name */
  validationRules: Record<string, ValidationRule[]>
  /** Field groups for organization */
  fieldGroups?: FieldGroup[]
  /** Template metadata */
  metadata?: Record<string, any>
}

/**
 * Field group for form organization
 */
export interface FieldGroup {
  /** Group identifier */
  id: string
  /** Group label */
  label: string
  /** Field names in this group */
  fields: string[]
  /** Group display order */
  order: number
  /** Whether group is collapsible */
  collapsible?: boolean
  /** Whether group is initially collapsed */
  collapsed?: boolean
}

// ========================================
// Product Form Data Types
// ========================================

/**
 * Base product form data
 */
export interface BaseProductFormData {
  /** Product name */
  productName: string
  /** Report type */
  reportType: string
  /** Year */
  year?: number
  /** Template type used */
  templateType: TemplateType
}

/**
 * Backup product (备案产品) form data - 30 fields
 */
export interface BackupProductFormData extends BaseProductFormData {
  templateType: 'backup'
  // 基本信息
  developmentType: string // 开发类型
  revisionType?: string // 修订类型
  originalProductName?: string // 原产品名称和备案编号
  demonstrationClauseName?: string // 示范条款名称
  productCategory: string // 产品类别
  primaryAdditional: string // 主附险类型
  primarySituation?: string // 主险情况
  productAttribute?: string // 产品属性

  // 保险信息
  insurancePeriod?: string // 保险期间
  insuranceResponsibility?: string // 保险责任
  baseRate?: number // 基础费率（%）
  deductible?: string // 免赔额（率）
  compensationLimit?: string // 赔偿限额

  // 经营信息
  operatingScope?: string // 经营范围
  operatingRegion1?: string // 经营区域1
  operatingRegion2?: string // 经营区域2
  salesArea?: string // 销售区域

  // 其他信息
  hasElectronicPolicy?: string // 是否有电子保单
  isDomesticEncryption?: string // 是否是国产加密算法
  remarks?: string // 备注

  // 附件文件ID列表
  attachmentFileIds?: string[]
}

/**
 * Agricultural product (农险产品) form data - 25 fields
 */
export interface AgriculturalProductFormData extends BaseProductFormData {
  templateType: 'agricultural'
  // 基本信息
  developmentMethod: string // 开发方式
  revisionType?: string // 修订类型
  originalProductInfo?: string // 原产品名称和编号
  revisionCount?: number // 修订次数
  primaryAdditional: string // 主附险
  primarySituation?: string // 主险情况
  productCategory: string // 产品类别
  productNature: string // 产品性质

  // 保险信息
  insurancePeriod?: string // 保险期间
  insuranceResponsibility?: string // 保险责任
  baseRate?: number // 基础费率（%）
  compensationMethod?: string // 赔偿处理方式

  // 经营信息
  operatingRegion?: string // 经营区域
  isOperated?: string // 是否开办

  // 其他信息
  hasElectronicPolicy?: string // 是否有电子保单
  isDomesticCrypto?: string // 是否是国产密码算法
  remarks?: string // 备注

  // 附件文件ID列表
  attachmentFileIds?: string[]
}

/**
 * Union type for all product form data
 */
export type ProductFormData = BackupProductFormData | AgriculturalProductFormData

// ========================================
// Template Parsing Types
// ========================================

/**
 * Template file parsing request
 */
export interface TemplateParseRequest {
  /** File to parse */
  file: File
  /** Expected template type */
  templateType: TemplateType
}

/**
 * Template parsing result
 */
export interface TemplateParseResult {
  /** Whether parsing succeeded */
  success: boolean
  /** Parsed form data */
  data?: Partial<ProductFormData>
  /** Parsing errors */
  errors?: ParseError[]
  /** Parsing warnings */
  warnings?: ParseWarning[]
  /** Metadata about parsing */
  metadata?: {
    fileName: string
    fileSize: number
    parseTime: number
    rowsProcessed: number
  }
}

/**
 * Parse error information
 */
export interface ParseError {
  /** Field name where error occurred */
  field: string
  /** Row number in Excel file */
  row?: number
  /** Column identifier */
  column?: string
  /** Error message */
  message: string
  /** Error severity */
  severity: 'error' | 'critical'
}

/**
 * Parse warning information
 */
export interface ParseWarning {
  /** Field name where warning occurred */
  field: string
  /** Row number in Excel file */
  row?: number
  /** Column identifier */
  column?: string
  /** Warning message */
  message: string
  /** Suggested fix */
  suggestion?: string
}

// ========================================
// API Request/Response Types
// ========================================

/**
 * Template download request
 */
export interface TemplateDownloadRequest {
  /** Template type to download */
  templateType: TemplateType
}

/**
 * Template configuration request
 */
export interface TemplateConfigRequest {
  /** Template type */
  templateType: TemplateType
}

/**
 * Template configuration response
 */
export interface TemplateConfigResponse {
  /** Template configuration */
  config: TemplateConfig
}

/**
 * Field validation request
 */
export interface FieldValidationRequest {
  /** Template type */
  templateType: TemplateType
  /** Field name to validate */
  fieldName: string
  /** Field value to validate */
  fieldValue: any
  /** Complete form data for context */
  formData?: Partial<ProductFormData>
}

/**
 * Field validation response
 */
export interface FieldValidationResponse {
  /** Validation result */
  result: ValidationResult
}

/**
 * Form validation request
 */
export interface FormValidationRequest {
  /** Template type */
  templateType: TemplateType
  /** Complete form data */
  formData: ProductFormData
}

/**
 * Form validation response
 */
export interface FormValidationResponse {
  /** Validation result */
  result: FormValidationResult
}

// ========================================
// UI State Types
// ========================================

/**
 * Template download state
 */
export interface TemplateDownloadState {
  /** Currently active template */
  activeTemplate: TemplateType | null
  /** Whether download is in progress */
  downloading: boolean
  /** Download progress percentage */
  downloadProgress: number
  /** Download status */
  downloadStatus: 'idle' | 'downloading' | 'success' | 'error'
  /** Error message if download failed */
  errorMessage?: string
}

/**
 * Form state
 */
export interface FormState {
  /** Current form data */
  formData: Partial<ProductFormData>
  /** Field validation states */
  fieldValidations: Record<string, ValidationResult>
  /** Whether form is submitting */
  submitting: boolean
  /** Whether form has unsaved changes */
  dirty: boolean
  /** Current template type */
  templateType: TemplateType | null
}