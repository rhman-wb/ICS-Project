import request from '@/api'
import type { ApiResponse } from '@/api/types/common'

export interface DocumentResponseDto {
  id?: string
  fileName: string
  filePath: string
  fileSize: number
  formattedFileSize?: string
  fileType: string
  documentType?: string | null
  productId: string
  uploadStatus: string
  uploadStatusDescription?: string
  auditStatus?: string
  auditStatusDescription?: string
  createdAt?: string
  updatedAt?: string
}

export interface SupportedFormatsDto {
  documentTypes: string[]
  fileTypes: string[]
  allowedUploadTypes: string[]
  maxFileSize: number
  parseFormats: string[]
  parsableFormats: string[]
}

export const documentsApi = {
  upload(
    file: File,
    documentType: string,
    productId: string,
    description?: string,
    onUploadProgress?: (e: ProgressEvent) => void
  ) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('documentType', documentType)
    formData.append('productId', productId)
    if (description) formData.append('description', description)

    return request.post<ApiResponse<DocumentResponseDto>>('/v1/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress
    })
  },

  batchUpload(files: File[], productId: string) {
    const formData = new FormData()
    files.forEach(f => formData.append('files', f))
    formData.append('productId', productId)
    return request.post<ApiResponse<DocumentResponseDto[]>>('/v1/documents/batch-upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  parseRegistrationForm(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<ApiResponse<any>>('/v1/documents/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  getSupportedFormats() {
    return request.get<ApiResponse<SupportedFormatsDto>>('/v1/documents/supported-formats')
  }
}
