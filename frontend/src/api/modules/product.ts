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
  }
}