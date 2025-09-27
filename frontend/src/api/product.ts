import request from './index'

// 浜у搧鐩稿叧鐨勬帴鍙ｇ被鍨嬪畾涔?
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
 * 鍒涘缓浜у搧
 */
export const createProduct = (data: ProductCreateRequest): Promise<ApiResponse<ProductResponse>> => {
  return request.post('/v1/products', data)
    
}

/**
 * 鑾峰彇浜у搧璇︽儏
 */
export const getProduct = (productId: string): Promise<ApiResponse<ProductResponse>> => {
  return request.get(`/v1/products/${productId}`)
    
}

/**
 * 鎻愪氦浜у搧
 */
export const submitProduct = (productId: string): Promise<ApiResponse<ProductResponse>> => {
  return request.put(`/v1/products/${productId}/submit`)
    
}

/**
 * 鑾峰彇浜у搧鍒楄〃
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
 * 鏇存柊浜у搧淇℃伅
 */
export const updateProduct = (productId: string, data: Partial<ProductCreateRequest>): Promise<ApiResponse<ProductResponse>> => {
  return request.put(`/v1/products/${productId}`, data)
    
}

/**
 * 鍒犻櫎浜у搧
 */
export const deleteProduct = (productId: string): Promise<ApiResponse<void>> => {
  return request.delete(`/v1/products/${productId}`)
    
}
