import request from '@/api/index'

// 文档校验结果类型定义
export interface ValidationError {
  errorType: string
  errorCode: string
  message: string
  documentType: string
  documentId?: string
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  suggestion: string
}

export interface ValidationWarning {
  warningType: string
  warningCode: string
  message: string
  documentType: string
  documentId?: string
  recommendation: string
}

export interface ValidationSummary {
  totalDocuments: number
  requiredDocuments: number
  uploadedDocuments: number
  validDocuments: number
  totalErrors: number
  totalWarnings: number
  completenessPercentage: number
}

export interface DocumentValidationResult {
  isValid: boolean
  productId: string
  errors: ValidationError[]
  warnings: ValidationWarning[]
  summary: ValidationSummary
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
  code?: string
}

/**
 * 文档校验 API 服务
 */
export class DocumentValidationService {
  /**
   * 校验产品的所有文档
   */
  static async validateProductDocuments(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate/${productId}`)
  }

  /**
   * 校验单个文档
   */
  static async validateSingleDocument(documentId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-single/${documentId}`)
  }

  /**
   * 校验文档完整性
   */
  static async validateDocumentCompleteness(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-completeness/${productId}`)
  }

  /**
   * 校验文档名称一致性
   */
  static async validateDocumentNameConsistency(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-consistency/${productId}`)
  }

  /**
   * 校验文档格式
   */
  static async validateDocumentFormat(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-format/${productId}`)
  }
}

export default DocumentValidationService