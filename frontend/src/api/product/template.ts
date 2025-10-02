/**
 * 模板 API 服务
 * 处理所有模板相关的 API 操作，包括下载、配置、验证和解析
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
 * 模板 API 端点
 * 与后端实现保持一致
 */
const TEMPLATE_API = {
  BASE: '/v1/products/templates',
  LIST: '/v1/products/templates',
  // 使用路径参数的端点需要在调用时替换{templateType}
  CONFIG: '/v1/products/templates/{templateType}',
  DOWNLOAD_INFO: '/v1/products/templates/{templateType}/download',
  DOWNLOAD_FILE: '/v1/products/templates/{templateType}/file',
  FIELDS: '/v1/products/templates/{templateType}/fields',
  VALIDATION_RULES: '/v1/products/templates/{templateType}/validation-rules',
  VALIDATE_FIELDS: '/v1/products/validate-fields',
  PARSE: '/v1/products/parse-template'
}

/**
 * 模板常量配置
 * 集中管理模板相关的常量，避免硬编码
 */
export const TEMPLATE_CONSTANTS = {
  /** 字段数量 */
  FIELD_COUNTS: {
    FILING: 30,
    AGRICULTURAL: 25
  },
  /** 文件名 */
  FILE_NAMES: {
    FILING: '附件3_备案产品自主注册信息登记表.xlsx',
    AGRICULTURAL: '附件5_农险产品信息登记表.xls'
  },
  /** 显示名称 */
  DISPLAY_NAMES: {
    FILING: '备案产品自主注册信息登记表',
    AGRICULTURAL: '农险产品信息登记表'
  },
  /** 文件扩展名 */
  FILE_EXTENSIONS: {
    FILING: 'xlsx' as const,
    AGRICULTURAL: 'xls' as const
  }
}

/**
 * 模板 API 服务
 */
export const templateApi = {
  /**
   * 获取可用模板列表
   * @returns 返回可用模板列表的 Promise
   */
  getTemplateList(): Promise<ApiResponse<TemplateInfo[]>> {
    return request({
      url: TEMPLATE_API.LIST,
      method: 'GET'
    })
  },

  /**
   * 下载模板文件
   * @param templateType - 模板类型（'FILING' 或 'AGRICULTURAL'）
   * @returns 返回文件 Blob 的 Promise
   */
  async downloadTemplate(templateType: TemplateType): Promise<Blob> {
    // 先获取下载信息
    const downloadInfo = await request({
      url: TEMPLATE_API.DOWNLOAD_INFO.replace('{templateType}', templateType),
      method: 'GET'
    })

    // 然后下载文件
    const response = await request({
      url: TEMPLATE_API.DOWNLOAD_FILE.replace('{templateType}', templateType),
      method: 'GET',
      responseType: 'blob'
    })

    // 对于 blob 响应，直接返回 blob
    if (response instanceof Blob) {
      return response
    }

    // 如果响应被包装在 ApiResponse 中，提取 blob
    return response
  },

  /**
   * 获取模板配置
   * @param templateType - 模板类型
   * @returns 返回模板配置的 Promise
   */
  getTemplateConfig(templateType: TemplateType): Promise<ApiResponse<TemplateConfig>> {
    return request({
      url: TEMPLATE_API.CONFIG.replace('{templateType}', templateType),
      method: 'GET'
    })
  },

  /**
   * 更新模板配置（仅管理员）
   * @param templateType - 模板类型
   * @param config - 更新的配置
   * @returns 返回更新后的配置的 Promise
   */
  updateTemplateConfig(
    templateType: TemplateType,
    config: TemplateConfig
  ): Promise<ApiResponse<TemplateConfig>> {
    return request({
      url: TEMPLATE_API.CONFIG.replace('{templateType}', templateType),
      method: 'PUT',
      data: config
    })
  },

  /**
   * 验证单个字段
   * @param requestData - 字段验证请求
   * @returns 返证结果的 Promise
   */
  validateField(
    requestData: FieldValidationRequest
  ): Promise<ApiResponse<FieldValidationResponse>> {
    return request({
      url: TEMPLATE_API.VALIDATE_FIELDS,
      method: 'POST',
      data: requestData
    })
  },

  /**
   * 验证整个表单
   * @param requestData - 表单验证请求
   * @returns 返回验证结果的 Promise
   */
  validateForm(
    requestData: FormValidationRequest
  ): Promise<ApiResponse<FormValidationResponse>> {
    return request({
      url: TEMPLATE_API.VALIDATE_FIELDS,
      method: 'POST',
      data: requestData
    })
  },

  /**
   * 解析模板文件并提取数据
   * @param file - 要解析的 Excel 文件
   * @param templateType - 预期的模板类型
   * @returns 返回解析后的数据的 Promise
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
      timeout: 60000 // 文件上传和解析 60 秒超时
    })
  },

  /**
   * 提交产品表单数据
   * @param formData - 产品表单数据
   * @returns 返回创建的产品信息的 Promise
   */
  submitProductForm(formData: ProductFormData): Promise<ApiResponse<any>> {
    return request({
      url: '/v1/products',
      method: 'POST',
      data: formData
    })
  },

  /**
   * 更新产品表单数据
   * @param productId - 产品 ID
   * @param formData - 更新的产品表单数据
   * @returns 返回更新后的产品信息的 Promise
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

// 命名导出，用于向后兼容和更容易的 mock
export const {
  getTemplateList,
  downloadTemplate,
  getTemplateConfig,
  updateTemplateConfig,
  validateField,
  validateForm,
  parseTemplateFile,
  submitProductForm,
  updateProductForm
} = templateApi

/**
 * 模板工具函数
 */
export const templateUtils = {
  /**
   * 获取模板显示名称
   * @param templateType - 模板类型
   * @returns 中文显示名称
   */
  getTemplateDisplayName(templateType: TemplateType): string {
    return TEMPLATE_CONSTANTS.DISPLAY_NAMES[templateType] || templateType
  },

  /**
   * 获取模板文件扩展名
   * @param templateType - 模板类型
   * @returns 文件扩展名
   */
  getTemplateFileExtension(templateType: TemplateType): 'xlsx' | 'xls' {
    return TEMPLATE_CONSTANTS.FILE_EXTENSIONS[templateType]
  },

  /**
   * 获取模板文件名
   * @param templateType - 模板类型
   * @returns 包含扩展名的完整文件名
   */
  getTemplateFileName(templateType: TemplateType): string {
    return TEMPLATE_CONSTANTS.FILE_NAMES[templateType]
  },

  /**
   * 验证文件类型是否与模板类型匹配
   * @param file - 要验证的文件
   * @param templateType - 预期的模板类型
   * @returns 如果文件类型有效则返回 true
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
   * 获取模板字段数量
   * @param templateType - 模板类型
   * @returns 模板中的字段数量
   */
  getTemplateFieldCount(templateType: TemplateType): number {
    return TEMPLATE_CONSTANTS.FIELD_COUNTS[templateType]
  },

  /**
   * 下载 Blob 为文件
   * @param blob - Blob 数据
   * @param fileName - 下载的文件名
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
 * 导出默认模板 API
 */
export default templateApi