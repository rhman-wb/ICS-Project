import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { authApi, getLoadingState } from '@/api/modules/auth'
import type { LoginRequest, ChangePasswordRequest } from '@/api/types/auth'

// Mock axios
vi.mock('@/api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn()
  }
}))

// Mock utils
vi.mock('@/utils/apiError', () => ({
  ApiErrorHandler: {
    handleAxiosError: vi.fn()
  },
  ApiError: {
    authError: vi.fn(),
    networkError: vi.fn()
  },
  retryApiCall: vi.fn()
}))

describe('Auth API', () => {
  let mockRequest: any

  beforeEach(async () => {
    vi.clearAllMocks()
    mockRequest = vi.mocked((await import('@/api')).default)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('login', () => {
    it('should login successfully', async () => {
      const loginData: LoginRequest = {
        username: 'testuser',
        password: 'password123'
      }

      const mockResponse = {
        data: {
          code: 200,
          message: '登录成功',
          success: true,
          data: {
            token: 'mock-token',
            refreshToken: 'mock-refresh-token',
            user: {
              id: '1',
              username: 'testuser',
              realName: '测试用户',
              email: 'test@example.com',
              roles: ['USER']
            }
          },
          timestamp: Date.now()
        }
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      const result = await authApi.login(loginData)

      expect(mockRequest.post).toHaveBeenCalledWith('/v1/auth/login', loginData)
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle login failure', async () => {
      const loginData: LoginRequest = {
        username: 'testuser',
        password: 'wrongpassword'
      }

      const mockError = {
        response: {
          status: 401,
          data: {
            message: '用户名或密码不正确'
          }
        }
      }

      mockRequest.post.mockRejectedValue(mockError)

      await expect(authApi.login(loginData)).rejects.toThrow('用户名或密码不正确')
    })

    it('should handle network error', async () => {
      const loginData: LoginRequest = {
        username: 'testuser',
        password: 'password123'
      }

      const mockError = new Error('Network Error')
      mockRequest.post.mockRejectedValue(mockError)

      await expect(authApi.login(loginData)).rejects.toThrow('网络连接失败，请检查网络后重试')
    })
  })

  describe('logout', () => {
    it('should logout successfully', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: '登出成功',
          success: true,
          data: undefined,
          timestamp: Date.now()
        }
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      const result = await authApi.logout()

      expect(mockRequest.post).toHaveBeenCalledWith('/v1/auth/logout')
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle logout API failure gracefully', async () => {
      const mockError = new Error('Server Error')
      mockRequest.post.mockRejectedValue(mockError)

      // Should not throw error, but return success response for local cleanup
      const result = await authApi.logout()

      expect(result.success).toBe(true)
      expect(result.message).toBe('登出成功')
    })
  })

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const refreshToken = 'mock-refresh-token'
      const mockResponse = {
        data: {
          code: 200,
          message: '令牌刷新成功',
          success: true,
          data: {
            token: 'new-mock-token',
            refreshToken: 'new-refresh-token'
          },
          timestamp: Date.now()
        }
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      const result = await authApi.refreshToken(refreshToken)

      expect(mockRequest.post).toHaveBeenCalledWith('/v1/auth/refresh', { refreshToken })
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle refresh token failure', async () => {
      const refreshToken = 'invalid-refresh-token'
      const mockError = {
        response: {
          status: 401,
          data: {
            message: '刷新令牌已过期'
          }
        }
      }

      mockRequest.post.mockRejectedValue(mockError)

      await expect(authApi.refreshToken(refreshToken)).rejects.toThrow('刷新令牌已过期，请重新登录')
    })
  })

  describe('getUserInfo', () => {
    it('should get user info successfully', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: '获取成功',
          success: true,
          data: {
            id: '1',
            username: 'testuser',
            realName: '测试用户',
            email: 'test@example.com',
            phone: '13800138000',
            roles: ['USER'],
            permissions: ['read', 'write']
          },
          timestamp: Date.now()
        }
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      const result = await authApi.getUserInfo()

      expect(mockRequest.get).toHaveBeenCalledWith('/v1/users/profile')
      expect(result).toEqual(mockResponse.data)
    })
  })

  describe('getUserPermissions', () => {
    it('should get user permissions successfully', async () => {
      const mockResponse = {
        data: {
          code: 200,
          message: '获取成功',
          success: true,
          data: ['read', 'write', 'delete'],
          timestamp: Date.now()
        }
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      const result = await authApi.getUserPermissions()

      // 现在通过 `/v1/users/profile` 获取并提取权限
      expect(mockRequest.get).toHaveBeenCalledWith('/v1/users/profile')
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle permissions failure gracefully', async () => {
      const mockError = new Error('Server Error')
      mockRequest.get.mockRejectedValue(mockError)

      // Should not throw error, but return empty permissions array
      const result = await authApi.getUserPermissions()

      expect(result.success).toBe(true)
      expect(result.data).toEqual([])
    })
  })

  describe('changePassword', () => {
    it('should change password successfully', async () => {
      const passwordData: ChangePasswordRequest = {
        oldPassword: 'oldpass123',
        newPassword: 'newpass123',
        confirmPassword: 'newpass123'
      }

      const mockResponse = {
        data: {
          code: 200,
          message: '密码修改成功',
          success: true,
          data: undefined,
          timestamp: Date.now()
        }
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      const result = await authApi.changePassword(passwordData)

      expect(mockRequest.post).toHaveBeenCalledWith('/api/v1/auth/change-password', passwordData)
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle wrong old password', async () => {
      const passwordData: ChangePasswordRequest = {
        oldPassword: 'wrongpass',
        newPassword: 'newpass123',
        confirmPassword: 'newpass123'
      }

      const mockError = {
        response: {
          status: 400,
          data: {
            message: '原密码不正确'
          }
        }
      }

      mockRequest.post.mockRejectedValue(mockError)

      await expect(authApi.changePassword(passwordData)).rejects.toThrow('原密码不正确')
    })
  })

  describe('loading states', () => {
    it('should manage loading states correctly', () => {
      // Initially should be false
      expect(getLoadingState('login')).toBe(false)
      expect(getLoadingState('logout')).toBe(false)
      expect(getLoadingState('refreshToken')).toBe(false)
    })
  })
})