import request from './index'

// 产品相关的接口类型定义
export interface ProductCreateRequest {
  productName: string
  productType: string
  reportType: string
  productNature: string
  year?: number | null
  revisionType?: string
  originalProductName?: string
  developmentMethod?: string
  primaryAdditional?: string
  primarySituation?: string
  productCategory?: string
  operatingRegion?: string
  operatingScope?: string
  demonstrationClauseName?: string
  operatingRegion1?: string
  operatingRegion2?: string
  salesPromotionName?: string
}

export interface ProductResponse {
  id: string
  productName: string
  productType: string
  reportType: string
  productNature: string
  year?: number | null
  revisionType?: string
  originalProductName?: string
  developmentMethod?: string
  primaryAdditional?: string
  primarySituation?: string
  productCategory?: string
  operatingRegion?: string
  operatingScope?: string
  demonstrationClauseName?: string
  operatingRegion1?: string
  operatingRegion2?: string
  salesPromotionName?: string
  status?: string
  createdAt?: string
  updatedAt?: string
  documentCount?: number
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string
  code?: string
  timestamp?: string
}

/**
 * 创建产品
 */
export const createProduct = (data: ProductCreateRequest): Promise<ApiResponse<ProductResponse>> => {
  return request.post('/v1/products', data)
}

/**
 * 获取产品详情
 */
export const getProduct = (productId: string): Promise<ApiResponse<ProductResponse>> => {
  return request.get(`/v1/products/${productId}`)
}

/**
 * 提交产品
 */
export const submitProduct = (productId: string): Promise<ApiResponse<ProductResponse>> => {
  return request.put(`/v1/products/${productId}/submit`)
}

/**
 * 获取产品列表
 */
export const getProductList = (params?: {
  page?: number
  size?: number
  productType?: string
  status?: string
  keyword?: string
}): Promise<ApiResponse<{
  content: ProductResponse[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}>> => {
  return request.get('/v1/products', { params })
}

/**
 * 更新产品信息
 */
export const updateProduct = (productId: string, data: Partial<ProductCreateRequest>): Promise<ApiResponse<ProductResponse>> => {
  return request.put(`/v1/products/${productId}`, data)
}

/**
 * 删除产品
 */
export const deleteProduct = (productId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/v1/products/${productId}`)
}