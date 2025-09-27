import request from '@/api/index'

// 鏂囨。鏍￠獙缁撴灉绫诲瀷瀹氫箟
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
 * 鏂囨。鏍￠獙API鏈嶅姟
 */
export class DocumentValidationService {

  /**
   * 鏍￠獙浜у搧鐨勬墍鏈夋枃妗?
   */
  static async validateProductDocuments(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate/${productId}`)
      
  }

  /**
   * 鏍￠獙鍗曚釜鏂囨。
   */
  static async validateSingleDocument(documentId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-single/${documentId}`)
      
  }

  /**
   * 鏍￠獙鏂囨。瀹屾暣鎬?
   */
  static async validateDocumentCompleteness(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-completeness/${productId}`)
      
  }

  /**
   * 鏍￠獙鏂囨。鍚嶇О涓€鑷存€?
   */
  static async validateDocumentNameConsistency(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-consistency/${productId}`)
      
  }

  /**
   * 鏍￠獙鏂囨。鏍煎紡
   */
  static async validateDocumentFormat(productId: string): Promise<ApiResponse<DocumentValidationResult>> {
    return request.get(`/v1/documents/validate-format/${productId}`)
      
  }
}

export default DocumentValidationService
