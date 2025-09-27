import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import { getToken } from '@/utils/auth'
import router from '@/router'
import { ApiError } from '@/utils/apiError'

// Compute baseURL: dev uses '/api' (Vite proxy), production reads env
const computedBaseURL = (() => {
  if (import.meta.env.DEV) return '/api'
  const envBase = import.meta.env.VITE_API_BASE_URL
  return (envBase && envBase.trim().length > 0) ? envBase : '/api'
})()

// Create axios instance
const request: AxiosInstance = axios.create({
  baseURL: computedBaseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Refresh token state
let isRefreshing = false
let failedQueue: Array<{ resolve: (v?: any) => void; reject: (r?: any) => void }> = []

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  failedQueue = []
}

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) config.headers.Authorization = `Bearer ${token}`
    // Request ID for tracing
    config.headers['X-Request-ID'] = generateRequestId()
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    // Business error normalization
    if (data && typeof data === 'object' && 'success' in data && !data.success) {
      const businessError = ApiError.businessError(
        data.code ?? 'BUSINESS_ERROR',
        data.message || '请求失败',
        data
      )
      message.error(businessError.message)
      return Promise.reject(businessError)
    }
    return data
  },
  async (error) => {
    const originalRequest = error.config || {}
    const { response } = error

    if (response) {
      switch (response.status) {
        case 401:
          if (!originalRequest._retry && !originalRequest.url?.includes('/auth/refresh')) {
            if (isRefreshing) {
              return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject })
              }).then((token) => {
                if (token) {
                  originalRequest.headers = originalRequest.headers || {}
                  originalRequest.headers.Authorization = `Bearer ${token}`
                  return request(originalRequest)
                }
                return Promise.reject(new Error('Token refresh failed'))
              })
            }

            originalRequest._retry = true
            isRefreshing = true
            try {
              // Lazy import to avoid circular deps
              const { useAuthStore } = await import('@/stores/modules/auth')
              const authStore = useAuthStore()
              const success = await authStore.refreshTokenAction()
              if (success) {
                const newToken = getToken()
                processQueue(null, newToken)
                if (newToken) {
                  originalRequest.headers = originalRequest.headers || {}
                  originalRequest.headers.Authorization = `Bearer ${newToken}`
                  return request(originalRequest)
                }
                processQueue(new Error('No token after refresh'), null)
                return Promise.reject(new Error('No token after refresh'))
              }
              processQueue(error, null)
              return Promise.reject(error)
            } catch (refreshError) {
              processQueue(refreshError, null)
              if (!window.location.pathname.includes('/login')) {
                message.error('登录已过期，请重新登录')
                setTimeout(() => router.push('/login'), 100)
              }
              return Promise.reject(refreshError)
            } finally {
              isRefreshing = false
            }
          } else {
            if (!window.location.pathname.includes('/login')) {
              message.error('登录已过期，请重新登录')
              setTimeout(() => router.push('/login'), 100)
            }
          }
          break
        case 403:
          message.error('没有权限访问该资源')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(response.data?.message || '请求失败')
      }
    } else {
      // Network error with retry
      originalRequest._retryCount = originalRequest._retryCount || 0
      if (originalRequest._retryCount < RETRY_CONFIG.maxRetries && RETRY_CONFIG.retryCondition(error)) {
        originalRequest._retryCount++
        const delay = RETRY_CONFIG.retryDelay * Math.pow(2, originalRequest._retryCount - 1)
        return new Promise((resolve) => setTimeout(() => resolve(request(originalRequest)), delay))
      }
      message.error('网络连接失败，请检查网络后重试')
    }

    return Promise.reject(error)
  }
)

// Utils
function generateRequestId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 11)}`
}

const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000,
  retryCondition: (error: any) => {
    return !error.response || (error.response.status >= 500 && error.response.status < 600)
  }
}

const retryRequest = async (config: any, retryCount = 0): Promise<any> => {
  try {
    return await request(config)
  } catch (error) {
    if (retryCount < RETRY_CONFIG.maxRetries && RETRY_CONFIG.retryCondition(error)) {
      const delay = RETRY_CONFIG.retryDelay * Math.pow(2, retryCount)
      await new Promise((r) => setTimeout(r, delay))
      return retryRequest(config, retryCount + 1)
    }
    throw error
  }
}

export default request