import { message } from 'ant-design-vue'

// API错误类型枚举
export enum ApiErrorType {
  NETWORK_ERROR = 'NETWORK_ERROR',
  TIMEOUT_ERROR = 'TIMEOUT_ERROR',
  AUTH_ERROR = 'AUTH_ERROR',
  PERMISSION_ERROR = 'PERMISSION_ERROR',
  VALIDATION_ERROR = 'VALIDATION_ERROR',
  SERVER_ERROR = 'SERVER_ERROR',
  BUSINESS_ERROR = 'BUSINESS_ERROR',
  UNKNOWN_ERROR = 'UNKNOWN_ERROR'
}

// API错误信息接口
export interface ApiErrorInfo {
  type: ApiErrorType
  code: string | number
  message: string
  details?: any
  timestamp: number
}

// API错误处理类
export class ApiError extends Error {
  public readonly type: ApiErrorType
  public readonly code: string | number
  public readonly details?: any
  public readonly timestamp: number

  constructor(type: ApiErrorType, code: string | number, message: string, details?: any) {
    super(message)
    this.name = 'ApiError'
    this.type = type
    this.code = code
    this.details = details
    this.timestamp = Date.now()
  }

  // 创建错误信息对象
  toErrorInfo(): ApiErrorInfo {
    return {
      type: this.type,
      code: this.code,
      message: this.message,
      details: this.details,
      timestamp: this.timestamp
    }
  }

  // 静态工厂方法
  static networkError(message = '网络连接失败'): ApiError {
    return new ApiError(ApiErrorType.NETWORK_ERROR, 'NETWORK_ERROR', message)
  }

  static timeoutError(message = '请求超时'): ApiError {
    return new ApiError(ApiErrorType.TIMEOUT_ERROR, 'TIMEOUT_ERROR', message)
  }

  static authError(message = '认证失败'): ApiError {
    return new ApiError(ApiErrorType.AUTH_ERROR, 401, message)
  }

  static permissionError(message = '权限不足'): ApiError {
    return new ApiError(ApiErrorType.PERMISSION_ERROR, 403, message)
  }

  static validationError(message = '参数验证失败', details?: any): ApiError {
    return new ApiError(ApiErrorType.VALIDATION_ERROR, 400, message, details)
  }

  static serverError(message = '服务器内部错误'): ApiError {
    return new ApiError(ApiErrorType.SERVER_ERROR, 500, message)
  }

  static businessError(code: string | number, message: string, details?: any): ApiError {
    return new ApiError(ApiErrorType.BUSINESS_ERROR, code, message, details)
  }

  static unknownError(message = '未知错误', details?: any): ApiError {
    return new ApiError(ApiErrorType.UNKNOWN_ERROR, 'UNKNOWN_ERROR', message, details)
  }
}

// 错误处理器
export class ApiErrorHandler {
  // 处理axios错误
  static handleAxiosError(error: any): ApiError {
    if (!error.response) {
      // 网络错误
      if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        return ApiError.timeoutError('请求超时，请稍后重试')
      }
      return ApiError.networkError('网络连接失败，请检查网络后重试')
    }

    const { status, data } = error.response

    switch (status) {
      case 400:
        return ApiError.validationError(data?.message || '请求参数错误', data)
      case 401:
        return ApiError.authError(data?.message || '认证失败，请重新登录')
      case 403:
        return ApiError.permissionError(data?.message || '权限不足，无法访问该资源')
      case 404:
        return ApiError.businessError(404, data?.message || '请求的资源不存在')
      case 422:
        return ApiError.validationError(data?.message || '数据验证失败', data)
      case 429:
        return ApiError.businessError(429, data?.message || '请求过于频繁，请稍后重试')
      case 500:
        return ApiError.serverError(data?.message || '服务器内部错误')
      case 502:
      case 503:
      case 504:
        return ApiError.serverError('服务暂时不可用，请稍后重试')
      default:
        return ApiError.businessError(status, data?.message || `请求失败 (${status})`, data)
    }
  }

  // 显示错误消息
  static showError(error: ApiError | Error, showMessage = true): void {
    let errorMessage: string

    if (error instanceof ApiError) {
      errorMessage = error.message
      
      // 记录错误日志
      console.error('API Error:', {
        type: error.type,
        code: error.code,
        message: error.message,
        details: error.details,
        timestamp: new Date(error.timestamp).toISOString()
      })
    } else {
      errorMessage = error.message || '操作失败'
      console.error('Unknown Error:', error)
    }

    // 显示用户友好的错误消息
    if (showMessage) {
      message.error(errorMessage)
    }
  }

  // 处理业务错误
  static handleBusinessError(response: any): ApiError | null {
    if (response && typeof response === 'object' && 'success' in response && !response.success) {
      return ApiError.businessError(
        response.code || 'BUSINESS_ERROR',
        response.message || '业务处理失败',
        response.data
      )
    }
    return null
  }
}

// 错误重试配置
export interface RetryConfig {
  maxRetries: number
  retryDelay: number
  retryCondition: (error: ApiError) => boolean
}

// 默认重试配置
export const DEFAULT_RETRY_CONFIG: RetryConfig = {
  maxRetries: 3,
  retryDelay: 1000,
  retryCondition: (error: ApiError) => {
    // 网络错误、超时错误、服务器错误时重试
    return [
      ApiErrorType.NETWORK_ERROR,
      ApiErrorType.TIMEOUT_ERROR,
      ApiErrorType.SERVER_ERROR
    ].includes(error.type)
  }
}

// 重试执行器
export class RetryExecutor {
  static async execute<T>(
    operation: () => Promise<T>,
    config: RetryConfig = DEFAULT_RETRY_CONFIG
  ): Promise<T> {
    let lastError: ApiError | null = null

    for (let attempt = 0; attempt <= config.maxRetries; attempt++) {
      try {
        return await operation()
      } catch (error: any) {
        const apiError = error instanceof ApiError 
          ? error 
          : ApiErrorHandler.handleAxiosError(error)

        lastError = apiError

        // 如果是最后一次尝试，或者不满足重试条件，直接抛出错误
        if (attempt === config.maxRetries || !config.retryCondition(apiError)) {
          throw apiError
        }

        // 等待后重试
        const delay = config.retryDelay * Math.pow(2, attempt) // 指数退避
        console.warn(`Operation failed, retrying in ${delay}ms... (${attempt + 1}/${config.maxRetries})`)
        
        await new Promise(resolve => setTimeout(resolve, delay))
      }
    }

    throw lastError || ApiError.unknownError('重试执行失败')
  }
}

// 导出便捷函数
export const handleApiError = ApiErrorHandler.handleAxiosError
export const showApiError = ApiErrorHandler.showError
export const retryApiCall = RetryExecutor.execute