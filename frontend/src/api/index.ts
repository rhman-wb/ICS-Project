import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import { getToken } from '@/utils/auth'
import router from '@/router'
import { ApiError } from '@/utils/apiError'

// 计算基础地址：开发环境固定走 '/api'（通过 Vite 代理到后端），避免跨域；生产再读环境变量
const computedBaseURL = (() => {
  if (import.meta.env.DEV) return '/api'
  const envBase = import.meta.env.VITE_API_BASE_URL
  return (envBase && envBase.trim().length > 0) ? envBase : '/api'
})()

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: computedBaseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 是否正在刷新token
let isRefreshing = false
// 失败队列
let failedQueue: Array<{
  resolve: (value?: any) => void
  reject: (reason?: any) => void
}> = []

// 处理队列
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  
  failedQueue = []
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()
    
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 统一处理业务错误
    if (data && typeof data === 'object' && 'success' in data && !data.success) {
      const businessError = ApiError.businessError(
        data.code ?? 'BUSINESS_ERROR',
        data.message || '请求失败',
        data
      )
      // 友好提示
      message.error(businessError.message)
      // 使用结构化错误对象，避免被误判为“网络错误”
      return Promise.reject(businessError)
    }
    
    return data
  },
  async (error) => {
    const originalRequest = error.config
    const { response } = error
    
    // 处理HTTP错误
    if (response) {
      switch (response.status) {
        case 401:
          // token过期，尝试刷新
          if (!originalRequest._retry && !originalRequest.url?.includes('/auth/refresh')) {
            if (isRefreshing) {
              // 如果正在刷新token，将请求加入队列
              return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject })
              }).then(token => {
                if (token) {
                  originalRequest.headers.Authorization = `Bearer ${token}`
                  return request(originalRequest)
                } else {
                  return Promise.reject(new Error('Token refresh failed'))
                }
              }).catch(err => {
                return Promise.reject(err)
              })
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
              // 动态导入auth store以避免循环依赖
              const { useAuthStore } = await import('@/stores/modules/auth')
              const authStore = useAuthStore()
              
              const success = await authStore.refreshTokenAction()
              
              if (success) {
                const newToken = getToken()
                processQueue(null, newToken)
                
                if (newToken) {
                  originalRequest.headers.Authorization = `Bearer ${newToken}`
                  return request(originalRequest)
                } else {
                  processQueue(new Error('No token after refresh'), null)
                  return Promise.reject(new Error('No token after refresh'))
                }
              } else {
                processQueue(error, null)
                return Promise.reject(error)
              }
            } catch (refreshError) {
              processQueue(refreshError, null)
              
              // 只有在非登录页面时才显示错误消息和跳转
              if (!window.location.pathname.includes('/login')) {
                message.error('登录已过期，请重新登录')
                
                // 延迟跳转，避免在组件销毁时执行路由操作
                setTimeout(() => {
                  router.push('/login')
                }, 100)
              }
              
              return Promise.reject(refreshError)
            } finally {
              isRefreshing = false
            }
          } else {
            // 如果是刷新token的请求失败，或者已经重试过，直接跳转登录页
            if (!window.location.pathname.includes('/login')) {
              message.error('登录已过期，请重新登录')
              
              setTimeout(() => {
                router.push('/login')
              }, 100)
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
      // 网络错误，尝试重试
      if (!originalRequest._retryCount) {
        originalRequest._retryCount = 0
      }
      
      if (originalRequest._retryCount < RETRY_CONFIG.maxRetries && RETRY_CONFIG.retryCondition(error)) {
        originalRequest._retryCount++
        
        console.warn(`Network error, retrying... (${originalRequest._retryCount}/${RETRY_CONFIG.maxRetries})`)
        
        // 指数退避延迟
        const delay = RETRY_CONFIG.retryDelay * Math.pow(2, originalRequest._retryCount - 1)
        
        return new Promise((resolve) => {
          setTimeout(() => {
            resolve(request(originalRequest))
          }, delay)
        })
      } else {
        message.error('网络连接失败，请检查网络后重试')
      }
    }
    
    return Promise.reject(error)
  }
)

// 生成请求ID
function generateRequestId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 11)}`
}

// 重试配置
const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000,
  retryCondition: (error: any) => {
    // 网络错误或5xx服务器错误时重试
    return !error.response || (error.response.status >= 500 && error.response.status < 600)
  }
}

// 重试函数
const retryRequest = async (config: any, retryCount = 0): Promise<any> => {
  try {
    return await request(config)
  } catch (error) {
    if (retryCount < RETRY_CONFIG.maxRetries && RETRY_CONFIG.retryCondition(error)) {
      console.warn(`Request failed, retrying... (${retryCount + 1}/${RETRY_CONFIG.maxRetries})`)
      
      // 指数退避延迟
      const delay = RETRY_CONFIG.retryDelay * Math.pow(2, retryCount)
      await new Promise(resolve => setTimeout(resolve, delay))
      
      return retryRequest(config, retryCount + 1)
    }
    throw error
  }
}

export default request
