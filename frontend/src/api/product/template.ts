/**
 * Template API Service
 * Handles all template-related API operations including download, configuration, validation, and parsing
 */

import request from '@/api'
import type { ApiResponse } from '@/api/types/common'
import type {
  TemplateType,
  TemplateInfo,
  TemplateConfig,
  TemplateConfigRequest,
  TemplateConfigResponse,
  TemplateDownloadRequest,
  FieldValidationRequest,
  FieldValidationResponse,
  FormValidationRequest,
  FormValidationResponse,
  TemplateParseRequest,
  TemplateParseResult,
  ProductFormData
} from '@/types/product/template'

/**
 * Template API endpoints
 */
const TEMPLATE_API = {
  BASE: '/v1/products/templates',
  LIST: '/v1/products/templates',
  DOWNLOAD: '/v1/products/templates/download',
  CONFIG: '/v1/products/templates/config',
  VALIDATE_FIELD: '/v1/products/templates/validate/field',
  VALIDATE_FORM: '/v1/products/templates/validate/form',
  PARSE: '/v1/products/templates/parse'
}

/**
 * Template API Service
 */
export const templateApi = {
  /**
   * Get list of available templates
   * @returns Promise with list of available templates
   */
  getTemplateList(): Promise<ApiResponse<TemplateInfo[]>> {
    return request({
      url: TEMPLATE_API.LIST,
      method: 'GET'
    })
  },

  /**
   * Download template file
   * @param templateType - Type of template to download ('backup' or 'agricultural')
   * @returns Promise with file blob
   */
  async downloadTemplate(templateType: TemplateType): Promise<Blob> {
    const response = await request({
      url: TEMPLATE_API.DOWNLOAD,
      method: 'GET',
      params: { templateType },
      responseType: 'blob'
    })

    // For blob responses, return the blob directly
    if (response instanceof Blob) {
      return response
    }

    // If response is wrapped in ApiResponse, extract the blob
    return response
  },

  /**
   * Get template configuration
   * @param templateType - Type of template
   * @returns Promise with template configuration
   */
  getTemplateConfig(templateType: TemplateType): Promise<ApiResponse<TemplateConfig>> {
    return request({
      url: TEMPLATE_API.CONFIG,
      method: 'GET',
      params: { templateType }
    })
  },

  /**
   * Update template configuration (admin only)
   * @param templateType - Type of template
   * @param config - Updated configuration
   * @returns Promise with updated configuration
   */
  updateTemplateConfig(
    templateType: TemplateType,
    config: TemplateConfig
  ): Promise<ApiResponse<TemplateConfig>> {
    return request({
      url: TEMPLATE_API.CONFIG,
      method: 'PUT',
      params: { templateType },
      data: config
    })
  },

  /**
   * Validate a single field
   * @param requestData - Field validation request
   * @returns Promise with validation result
   */
  validateField(
    requestData: FieldValidationRequest
  ): Promise<ApiResponse<FieldValidationResponse>> {
    return request({
      url: TEMPLATE_API.VALIDATE_FIELD,
      method: 'POST',
      data: requestData
    })
  },

  /**
   * Validate entire form
   * @param requestData - Form validation request
   * @returns Promise with validation result
   */
  validateForm(
    requestData: FormValidationRequest
  ): Promise<ApiResponse<FormValidationResponse>> {
    return request({
      url: TEMPLATE_API.VALIDATE_FORM,
      method: 'POST',
      data: requestData
    })
  },

  /**
   * Parse template file and extract data
   * @param file - Excel file to parse
   * @param templateType - Expected template type
   * @returns Promise with parsed data
   */
  async parseTemplateFile(
    file: File,
    templateType: TemplateType
  ): Promise<ApiResponse<TemplateParseResult>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('templateType', templateType)

    return request({
      url: TEMPLATE_API.PARSE,
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 60000 // 60 seconds for file upload and parsing
    })
  },

  /**
   * Submit product form data
   * @param formData - Product form data
   * @returns Promise with created product info
   */
  submitProductForm(formData: ProductFormData): Promise<ApiResponse<any>> {
    return request({
      url: '/v1/products',
      method: 'POST',
      data: formData
    })
  },

  /**
   * Update product form data
   * @param productId - Product ID
   * @param formData - Updated product form data
   * @returns Promise with updated product info
   */
  updateProductForm(
    productId: string,
    formData: Partial<ProductFormData>
  ): Promise<ApiResponse<any>> {
    return request({
      url: `/v1/products/${productId}`,
      method: 'PUT',
      data: formData
    })
  }
}

/**
 * Template utility functions
 */
export const templateUtils = {
  /**
   * Get template display name
   * @param templateType - Template type
   * @returns Display name in Chinese
   */
  getTemplateDisplayName(templateType: TemplateType): string {
    const names: Record<TemplateType, string> = {
      backup: '备案产品自主注册信息登记表',
      agricultural: '农险产品信息登记表'
    }
    return names[templateType] || templateType
  },

  /**
   * Get template file extension
   * @param templateType - Template type
   * @returns File extension
   */
  getTemplateFileExtension(templateType: TemplateType): 'xlsx' | 'xls' {
    return templateType === 'backup' ? 'xlsx' : 'xls'
  },

  /**
   * Get template file name
   * @param templateType - Template type
   * @returns Complete file name with extension
   */
  getTemplateFileName(templateType: TemplateType): string {
    const fileNames: Record<TemplateType, string> = {
      backup: '附件3_备案产品自主注册信息登记表.xlsx',
      agricultural: '附件5_农险产品信息登记表.xls'
    }
    return fileNames[templateType]
  },

  /**
   * Validate file type matches template type
   * @param file - File to validate
   * @param templateType - Expected template type
   * @returns True if file type is valid
   */
  validateFileType(file: File, templateType: TemplateType): boolean {
    const extension = this.getTemplateFileExtension(templateType)
    const fileName = file.name.toLowerCase()

    if (extension === 'xlsx') {
      return fileName.endsWith('.xlsx')
    } else {
      return fileName.endsWith('.xls')
    }
  },

  /**
   * Get template field count
   * @param templateType - Template type
   * @returns Number of fields in template
   */
  getTemplateFieldCount(templateType: TemplateType): number {
    return templateType === 'backup' ? 30 : 25
  },

  /**
   * Download blob as file
   * @param blob - Blob data
   * @param fileName - File name for download
   */
  downloadBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
}

/**
 * Export default template API
 */
export default templateApi