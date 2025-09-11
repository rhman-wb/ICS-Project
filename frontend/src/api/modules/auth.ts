import request from '@/api'
import axios from 'axios'
import type { ApiResponse } from '@/api/types/common'
import type { 
  LoginRequest, 
  LoginResponse, 
  UserInfo, 
  RefreshTokenRequest,
  ChangePasswordRequest 
} from '@/api/types/auth'
// 保留统一错误展示工具的导入以便后续扩展（当前未直接使用）
// import { ApiErrorHandler, ApiError, retryApiCall } from '@/utils/apiError'

// 请求加载状态管理
const loadingStates = new Map<string, boolean>()

// 获取加载状态
export const getLoadingState = (key: string): boolean => {
  return loadingStates.get(key) || false
}

// 设置加载状态
const setLoadingState = (key: string, loading: boolean): void => {
  loadingStates.set(key, loading)
}

// 包装API调用以管理加载状态
const withLoading = async <T>(
  key: string, 
  apiCall: () => Promise<T>
): Promise<T> => {
  try {
    setLoadingState(key, true)
    const result = await apiCall()
    return result
  } finally {
    setLoadingState(key, false)
  }
}

export const authApi = {
  // 用户登录
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return withLoading('login', async () => {
      try {
        const response = await request.post('/v1/auth/login', data)
        const resp = response.data

        // 兼容后端 LoginResponse 字段（accessToken/refreshToken 等）
        if (resp?.success && resp?.data) {
          const d = resp.data as any
          // 如果后端返回 accessToken，则转换为前端使用的 token 结构
          if (d.accessToken && !d.token) {
            resp.data = {
              token: d.accessToken,
              refreshToken: d.refreshToken,
              user: {
                id: d.userId,
                username: d.username,
                realName: d.realName,
                email: d.email,
                phone: d.phone,
                roles: d.roles || []
              }
            } as any
          }

          console.log('Login successful:', {
            username: data.username,
            timestamp: new Date().toISOString()
          })
        }

        return resp
      } catch (error: any) {
        // 使用统一错误类型，避免被展示为“网络错误”
        if (error?.code === 1003 || error?.response?.status === 401) {
          throw new Error('用户名或密码不正确')
        }
        if (error?.code === 1005 || error?.response?.status === 423) {
          throw new Error('账户已被锁定，请稍后重试')
        }
        if (error?.response?.status === 429) {
          throw new Error('登录尝试过于频繁，请稍后重试')
        }
        // 无 response 视为网络错误：启用本机后端直连兜底重试
        if (!error.response && !error.code) {
          const fallbackBases = [
            'http://127.0.0.1:8080/api',
            'http://localhost:8080/api'
          ]
          for (const base of fallbackBases) {
            try {
              const retryResp = await axios.post(`${base}/v1/auth/login`, data, {
                headers: { 'Content-Type': 'application/json' },
                timeout: 10000
              })
              return retryResp.data
            } catch (e: any) {
              // 若 fallback 返回了认证类错误，直接抛出业务提示
              if (e?.response?.status === 401) {
                throw new Error('用户名或密码不正确')
              }
              if (e?.response?.status) {
                // 有 HTTP 状态码，说明网络已打通，交由上层统一处理
                throw e
              }
            }
          }
          // 均失败
          throw new Error('网络连接失败，请检查网络后重试')
        }
        throw error
      }
    })
  },

  // 用户登出
  async logout(): Promise<ApiResponse<void>> {
    return withLoading('logout', async () => {
      try {
        const response = await request.post('/v1/auth/logout')
        
        console.log('Logout successful:', {
          timestamp: new Date().toISOString()
        })
        
        return response.data
      } catch (error: any) {
        // 登出失败不影响客户端清理
        console.warn('Logout API failed, but continuing with local cleanup:', error.message)
        
        // 返回成功响应以确保本地状态清理
        return {
          code: 200,
          message: '登出成功',
          data: undefined,
          success: true,
          timestamp: Date.now()
        } as ApiResponse<void>
      }
    })
  },

  // 刷新token
  async refreshToken(refreshToken: string): Promise<ApiResponse<{ token: string; refreshToken?: string }>> {
    return withLoading('refreshToken', async () => {
      try {
        const requestData: RefreshTokenRequest = { refreshToken }
        const response = await request.post('/v1/auth/refresh', requestData)
        
        console.log('Token refresh successful')
        
        return response.data
      } catch (error: any) {
        console.error('Token refresh failed:', error.message)
        
        // 刷新失败时的特殊处理
        if (error.response?.status === 401 || error.response?.status === 403) {
          throw new Error('刷新令牌已过期，请重新登录')
        }
        
        throw error
      }
    })
  },

  // 获取用户信息
  async getUserInfo(): Promise<ApiResponse<UserInfo>> {
    return withLoading('getUserInfo', async () => {
      try {
        // 后端当前实现为 `/v1/users/profile` 获取当前用户信息（axios baseURL 为 /api）
        const response = await request.get('/v1/users/profile')
        return response.data
      } catch (error: any) {
        console.error('Get user info failed:', error.message)
        
        if (error.response?.status === 401) {
          throw new Error('用户信息获取失败，请重新登录')
        }
        
        throw error
      }
    })
  },

  // 获取用户权限
  async getUserPermissions(): Promise<ApiResponse<string[]>> {
    return withLoading('getUserPermissions', async () => {
      try {
        // 与后端统一：权限通常在用户信息中返回，若单独获取可按需要调整
        const response = await request.get('/v1/users/profile')
        const resp = response.data as any
        if (resp?.success && resp?.data) {
          return {
            code: 200,
            message: '权限获取成功',
            success: true,
            timestamp: Date.now(),
            data: resp.data.permissions || []
          } as ApiResponse<string[]>
        }
        return response.data
      } catch (error: any) {
        console.error('Get user permissions failed:', error.message)
        
        // 权限获取失败时返回空权限数组，不阻断用户使用
        return {
          code: 200,
          message: '权限获取成功',
          data: [],
          success: true,
          timestamp: Date.now()
        } as ApiResponse<string[]>
      }
    })
  },

  // 修改密码
  async changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>> {
    return withLoading('changePassword', async () => {
      try {
        const response = await request.post('/api/v1/auth/change-password', data)
        
        console.log('Password change successful')
        
        return response.data
      } catch (error: any) {
        if (error.response?.status === 400) {
          throw new Error('原密码不正确')
        } else if (error.response?.status === 422) {
          throw new Error('新密码不符合安全要求')
        }
        
        throw error
      }
    })
  },

  // 检查登录状态
  async checkLoginStatus(): Promise<ApiResponse<{ valid: boolean }>> {
    return withLoading('checkLoginStatus', async () => {
      try {
        const response = await request.get('/v1/auth/check')
        return response.data
      } catch (error: any) {
        // 检查失败认为登录状态无效
        return {
          code: 200,
          message: '检查完成',
          data: { valid: false },
          success: true,
          timestamp: Date.now()
        } as ApiResponse<{ valid: boolean }>
      }
    })
  }
}

// getLoadingState is already exported above