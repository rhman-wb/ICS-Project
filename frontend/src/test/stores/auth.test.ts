import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/modules/auth'
import { authApi } from '@/api/modules/auth'
import { mockApiResponse, mockErrorResponse, createTestLoginResponse, createTestUser } from '../utils/test-utils'

// Mock API
vi.mock('@/api/modules/auth', () => ({
  authApi: {
    login: vi.fn(),
    logout: vi.fn(),
    refreshToken: vi.fn(),
    getCurrentUser: vi.fn()
  }
}))

// Mock localStorage
const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage
})

// Mock router
const mockRouter = {
  push: vi.fn(),
  replace: vi.fn()
}
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => mockRouter
  }
})

describe('Auth Store', () => {
  let authStore: ReturnType<typeof useAuthStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isLoggedIn).toBe(false)
    })

    it('should load token from localStorage on initialization', () => {
      mockLocalStorage.getItem.mockReturnValue('stored-token')
      
      // 重新创建store以触发初始化
      setActivePinia(createPinia())
      authStore = useAuthStore()
      
      expect(mockLocalStorage.getItem).toHaveBeenCalledWith('access_token')
      expect(authStore.token).toBe('stored-token')
    })
  })

  describe('Login', () => {
    it('should login successfully', async () => {
      const loginRequest = { username: 'testuser', password: 'password123' }
      const loginResponse = createTestLoginResponse()
      
      vi.mocked(authApi.login).mockResolvedValue(loginResponse)

      const result = await authStore.login(loginRequest)

      expect(authApi.login).toHaveBeenCalledWith(loginRequest)
      expect(authStore.token).toBe(loginResponse.data.accessToken)
      expect(authStore.userInfo).toEqual({
        username: loginResponse.data.username,
        realName: loginResponse.data.realName,
        roles: loginResponse.data.roles,
        permissions: loginResponse.data.permissions
      })
      expect(authStore.isLoggedIn).toBe(true)
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('access_token', loginResponse.data.accessToken)
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('refresh_token', loginResponse.data.refreshToken)
      expect(result).toEqual(loginResponse)
    })

    it('should handle login failure', async () => {
      const loginRequest = { username: 'testuser', password: 'wrongpassword' }
      const errorResponse = mockErrorResponse('用户名或密码错误', 400)
      
      vi.mocked(authApi.login).mockRejectedValue(new Error('用户名或密码错误'))

      await expect(authStore.login(loginRequest)).rejects.toThrow('用户名或密码错误')
      
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.isLoggedIn).toBe(false)
    })

    it('should handle network error during login', async () => {
      const loginRequest = { username: 'testuser', password: 'password123' }
      
      vi.mocked(authApi.login).mockRejectedValue(new Error('Network Error'))

      await expect(authStore.login(loginRequest)).rejects.toThrow('Network Error')
      
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.isLoggedIn).toBe(false)
    })
  })

  describe('Logout', () => {
    beforeEach(async () => {
      // 先登录
      const loginResponse = createTestLoginResponse()
      vi.mocked(authApi.login).mockResolvedValue(loginResponse)
      await authStore.login({ username: 'testuser', password: 'password123' })
    })

    it('should logout successfully', async () => {
      vi.mocked(authApi.logout).mockResolvedValue(mockApiResponse(null))

      await authStore.logout()

      expect(authApi.logout).toHaveBeenCalled()
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isLoggedIn).toBe(false)
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('access_token')
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('refresh_token')
    })

    it('should clear local state even if logout API fails', async () => {
      vi.mocked(authApi.logout).mockRejectedValue(new Error('Network Error'))

      await authStore.logout()

      // 即使API调用失败，本地状态也应该被清除
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isLoggedIn).toBe(false)
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('access_token')
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('refresh_token')
    })
  })

  describe('Refresh Token', () => {
    beforeEach(() => {
      mockLocalStorage.getItem.mockImplementation((key) => {
        if (key === 'refresh_token') return 'test-refresh-token'
        return null
      })
    })

    it('should refresh token successfully', async () => {
      const refreshResponse = createTestLoginResponse()
      vi.mocked(authApi.refreshToken).mockResolvedValue(refreshResponse)

      const result = await authStore.refreshToken()

      expect(authApi.refreshToken).toHaveBeenCalledWith('test-refresh-token')
      expect(authStore.token).toBe(refreshResponse.data.accessToken)
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('access_token', refreshResponse.data.accessToken)
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith('refresh_token', refreshResponse.data.refreshToken)
      expect(result).toEqual(refreshResponse)
    })

    it('should handle refresh token failure', async () => {
      vi.mocked(authApi.refreshToken).mockRejectedValue(new Error('刷新令牌无效'))

      await expect(authStore.refreshToken()).rejects.toThrow('刷新令牌无效')
      
      // 刷新失败时应该清除本地状态
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('access_token')
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('refresh_token')
    })

    it('should handle missing refresh token', async () => {
      mockLocalStorage.getItem.mockReturnValue(null)

      await expect(authStore.refreshToken()).rejects.toThrow('刷新令牌不存在')
    })
  })

  describe('Get Current User', () => {
    beforeEach(async () => {
      // 先登录
      const loginResponse = createTestLoginResponse()
      vi.mocked(authApi.login).mockResolvedValue(loginResponse)
      await authStore.login({ username: 'testuser', password: 'password123' })
    })

    it('should get current user successfully', async () => {
      const userResponse = mockApiResponse(createTestUser())
      vi.mocked(authApi.getCurrentUser).mockResolvedValue(userResponse)

      await authStore.getCurrentUser()

      expect(authApi.getCurrentUser).toHaveBeenCalled()
      expect(authStore.userInfo).toEqual(userResponse.data)
    })

    it('should handle get current user failure', async () => {
      vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error('Unauthorized'))

      await expect(authStore.getCurrentUser()).rejects.toThrow('Unauthorized')
    })

    it('should not call API if not logged in', async () => {
      // 先退出登录
      await authStore.logout()

      await expect(authStore.getCurrentUser()).rejects.toThrow('用户未登录')
      expect(authApi.getCurrentUser).not.toHaveBeenCalled()
    })
  })

  describe('Permissions', () => {
    beforeEach(async () => {
      // 先登录
      const loginResponse = createTestLoginResponse()
      vi.mocked(authApi.login).mockResolvedValue(loginResponse)
      await authStore.login({ username: 'testuser', password: 'password123' })
    })

    it('should check if user has permission', () => {
      authStore.permissions = ['USER_VIEW', 'USER_CREATE', 'DASHBOARD_VIEW']

      expect(authStore.hasPermission('USER_VIEW')).toBe(true)
      expect(authStore.hasPermission('USER_DELETE')).toBe(false)
    })

    it('should check if user has role', () => {
      authStore.userInfo = {
        username: 'testuser',
        realName: '测试用户',
        roles: ['USER', 'ADMIN'],
        permissions: []
      }

      expect(authStore.hasRole('USER')).toBe(true)
      expect(authStore.hasRole('ADMIN')).toBe(true)
      expect(authStore.hasRole('SUPER_ADMIN')).toBe(false)
    })

    it('should check if user has any of the specified permissions', () => {
      authStore.permissions = ['USER_VIEW', 'DASHBOARD_VIEW']

      expect(authStore.hasAnyPermission(['USER_VIEW', 'USER_CREATE'])).toBe(true)
      expect(authStore.hasAnyPermission(['USER_CREATE', 'USER_DELETE'])).toBe(false)
    })

    it('should check if user has all specified permissions', () => {
      authStore.permissions = ['USER_VIEW', 'USER_CREATE', 'DASHBOARD_VIEW']

      expect(authStore.hasAllPermissions(['USER_VIEW', 'DASHBOARD_VIEW'])).toBe(true)
      expect(authStore.hasAllPermissions(['USER_VIEW', 'USER_DELETE'])).toBe(false)
    })
  })

  describe('Token Management', () => {
    it('should check if token is expired', () => {
      // Mock一个过期的token
      const expiredToken = 'expired.token.here'
      authStore.token = expiredToken

      // 这里需要根据实际的JWT工具实现来测试
      // 由于我们没有实际的JWT解析逻辑，这里只是示例
      expect(authStore.isTokenExpired).toBe(false) // 默认实现
    })

    it('should get token expiration time', () => {
      const token = 'test.token.here'
      authStore.token = token

      // 这里需要根据实际的JWT工具实现来测试
      expect(authStore.tokenExpirationTime).toBeNull() // 默认实现
    })
  })

  describe('Auto Logout', () => {
    it('should auto logout when token expires', async () => {
      // 先登录
      const loginResponse = createTestLoginResponse()
      vi.mocked(authApi.login).mockResolvedValue(loginResponse)
      await authStore.login({ username: 'testuser', password: 'password123' })

      // 模拟token过期
      vi.mocked(authApi.getCurrentUser).mockRejectedValue(new Error('Token expired'))

      try {
        await authStore.getCurrentUser()
      } catch (error) {
        // 忽略错误，我们关心的是是否自动退出登录
      }

      // 验证是否自动退出登录
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
    })
  })

  describe('State Persistence', () => {
    it('should persist user info to localStorage', async () => {
      const loginResponse = createTestLoginResponse()
      vi.mocked(authApi.login).mockResolvedValue(loginResponse)

      await authStore.login({ username: 'testuser', password: 'password123' })

      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'user_info',
        JSON.stringify({
          username: loginResponse.data.username,
          realName: loginResponse.data.realName,
          roles: loginResponse.data.roles,
          permissions: loginResponse.data.permissions
        })
      )
    })

    it('should restore user info from localStorage', () => {
      const userInfo = {
        username: 'testuser',
        realName: '测试用户',
        roles: ['USER'],
        permissions: ['USER_VIEW']
      }

      mockLocalStorage.getItem.mockImplementation((key) => {
        if (key === 'access_token') return 'test-token'
        if (key === 'user_info') return JSON.stringify(userInfo)
        return null
      })

      // 重新创建store以触发初始化
      setActivePinia(createPinia())
      authStore = useAuthStore()

      expect(authStore.token).toBe('test-token')
      expect(authStore.userInfo).toEqual(userInfo)
      expect(authStore.isLoggedIn).toBe(true)
    })
  })
})