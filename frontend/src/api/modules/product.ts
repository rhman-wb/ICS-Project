import request from '@/api'
import type { ApiResponse, PageResponse } from '@/api/types/common'

// 产品接口类型定义
export interface ProductQueryParams {
  page?: number
  size?: number
  fileName?: string
  reportType?: string
  developmentType?: string
  productCategory?: string
  primaryAdditional?: string
  revisionType?: string
  operatingRegion?: string
  year?: string
  productType?: string
  status?: string
  sortField?: string
  sortOrder?: string
}

export interface ProductInfo {
  id: string
  productName: string
  reportType: string
  productNature: string
  year: number
  revisionType?: string
  originalProductName?: string
  developmentMethod: string
  primaryAdditional: string
  primarySituation?: string
  productCategory: string
  operatingRegion: string
  operatingScope?: string
  demonstrationClauseName?: string
  operatingRegion1?: string
  operatingRegion2?: string
  salesPromotionName?: string
  productType: string
  productTypeDescription: string
  status: string
  statusDescription: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  documentCount: number
  auditStatus: string
}

// 产品API服务
export const productApi = {
  /**
   * 获取产品列表
   */
  getProductList(params: ProductQueryParams): Promise<ApiResponse<PageResponse<ProductInfo>>> {
    return request({
      url: '/v1/products',
      method: 'GET',
      params
    })
  },

  /**
   * 根据ID获取产品详情
   */
  getProductById(id: string): Promise<ApiResponse<ProductInfo>> {
    return request({
      url: `/v1/products/${id}`,
      method: 'GET'
    })
  },

  /**
   * 创建产品
   */
  createProduct(data: Partial<ProductInfo>): Promise<ApiResponse<ProductInfo>> {
    return request({
      url: '/v1/products',
      method: 'POST',
      data
    })
  },

  /**
   * 更新产品
   */
  updateProduct(id: string, data: Partial<ProductInfo>): Promise<ApiResponse<ProductInfo>> {
    return request({
      url: `/v1/products/${id}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除产品
   */
  deleteProduct(id: string): Promise<ApiResponse<boolean>> {
    return request({
      url: `/v1/products/${id}`,
      method: 'DELETE'
    })
  },

  /**
   * 解析模板文件（批量导入第一步：文件解析和数据验证）
   */
  parseTemplate(file: File, templateType: string, validateData: boolean = true): Promise<ApiResponse<{
    success: boolean
    message: string
    parsedData: Record<string, any>
    parsedFieldCount: number
    validationResult?: {
      valid: boolean
      message: string
      validatedFieldCount: number
      passedFieldCount: number
      failedFieldCount: number
      fieldValidations: Array<{
        fieldName: string
        valid: boolean
        value: any
        errorMessage: string
      }>
    }
  }>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('templateType', templateType)
    formData.append('validateData', validateData.toString())

    return request({
      url: '/v1/products/parse-template',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 批量导入产品（批量导入第二步：导入已解析的数据）
   */
  batchImportProducts(products: Array<Partial<ProductInfo>>, skipDuplicates: boolean = true): Promise<ApiResponse<{
    total: number
    success: number
    failed: number
    skipped: number
    errors: Array<{
      index: number
      productName: string
      error: string
    }>
  }>> {
    return request({
      url: '/v1/products/batch-import',
      method: 'POST',
      data: {
        products,
        skipDuplicates
      }
    })
  }
}
