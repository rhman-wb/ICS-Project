// 产品管理相关类型定义

// 产品状态枚举
export type ProductStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED'

// 文档类型枚举
export type DocumentType = 'TERMS' | 'FEASIBILITY_REPORT' | 'ACTUARIAL_REPORT' | 'RATE_TABLE' | 'REGISTRATION_FORM'

// 审核严重程度枚举
export type AuditSeverity = 'error' | 'warning' | 'info'

// 产品基础信息接口
export interface Product {
  id: string
  productName: string
  reportType: string
  productNature?: string
  year?: number
  revisionType?: string
  developmentMethod?: string
  primaryAdditional?: string
  productCategory?: string
  operatingRegion?: string
  status: ProductStatus
  documents: Document[]
  createdAt: string
  updatedAt: string
  createdBy?: string
  auditStatus?: string
}

// 文档信息接口
export interface Document {
  id: string
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  documentType: DocumentType
  productId: string
  auditResults?: AuditResult[]
  uploadedAt: string
}

// 审核结果接口
export interface AuditResult {
  id: string
  ruleId: string
  ruleType: string
  applicableChapter: string
  ruleSource: string
  originalContent: string
  suggestion: string
  severity: AuditSeverity
  documentId: string
}

// 产品筛选条件接口
export interface ProductFilters {
  fileName?: string
  reportType?: string
  developmentType?: string
  productCategory?: string
  primaryAdditional?: string
  revisionType?: string
  operatingRegion?: string
  year?: string
  auditStatus?: string
  dateRange?: [string, string]
}

// 分页配置接口
export interface PaginationConfig {
  current: number
  pageSize: number
  total: number
  showSizeChanger?: boolean
  showQuickJumper?: boolean
}

// 产品创建请求接口
export interface CreateProductRequest {
  productInfo: ProductInfo
  documents: DocumentUploadInfo[]
}

// 产品信息接口
export interface ProductInfo {
  productName: string
  reportType: string
  productNature?: string
  year?: number
  revisionType?: string
  developmentMethod?: string
  primaryAdditional?: string
  productCategory?: string
  operatingRegion?: string
  // 农险产品特有字段
  originalProductNameAndNumber?: string
  mainInsuranceInfo?: string
  // 备案产品特有字段
  developmentType?: string
  originalProductNameAndCode?: string
  businessScope?: string
  demonstrativeClauseName?: string
  operatingRegion1?: string
  operatingRegion2?: string
  salesPromotionName?: string
}

// 文档上传信息接口
export interface DocumentUploadInfo {
  documentType: DocumentType
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
}

// 上传的文件接口
export interface UploadedFile {
  id: string
  name: string
  size: number
  type: string
  url: string
  uploadTime: string
}

// 产品更新请求接口
export interface UpdateProductRequest {
  productInfo: ProductInfo
  documents?: DocumentUploadInfo[]
}

// 产品列表响应接口
export interface ProductListResponse {
  data: Product[]
  total: number
  page: number
  size: number
}

// 批量操作请求接口
export interface BatchOperationRequest {
  action: 'delete' | 'audit' | 'export' | 'follow'
  productIds: string[]
}

// 文档章节接口
export interface Chapter {
  id: string
  name: string
  level: number
  startPosition?: number
  endPosition?: number
}

// 文档内容响应接口
export interface DocumentContentResponse {
  content: string
  chapters: Chapter[]
  documentType: DocumentType
  productInfo: Product
}

// 表单验证规则接口
export interface FormValidationRules {
  [key: string]: Array<{
    required?: boolean
    message?: string
    min?: number
    max?: number
    pattern?: RegExp
    validator?: (rule: any, value: any) => Promise<void>
  }>
}