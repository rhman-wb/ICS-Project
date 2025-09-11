import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/modules/auth'
import { authApi } from '@/api/modules/auth'

// Mock API
vi.mock('@/api/modules/auth', () => ({
  authApi: {
    login: vi.fn(),
    logout: vi.fn(),
    refreshToken: vi.fn(),
    getUserInfo: vi.fn()
  }
}))

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('Auth Store', () => {
  let authStore: ReturnType<typeof useAuthStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      expect(authStore.token).toBe('')
      expect(authStore.refreshToken).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isLoggedIn).toBe(false)
    })

    it('从localStorage恢复token时应该更新登录状态', () => {
      localStorageMock.getItem.mockReturnValue('stored-token')
      
      // 重新创建store以触发初始化
      setActivePinia(createPinia())
      authStore = useAuthStore()
      
      expect(authStore.token).toBe('stored-token')
      expect(authStore.isLoggedIn).toBe(true)
    })
  })

  describe('登录功能', () => {
    it('登录成功时应该更新状态', async () => {
      const mockLoginResponse = {
        data: {
          token: 'new-token',
          refreshToken: 'new-refresh-token',
          user: {
            id: 'user-123',
            username: 'testuser',
            realName: '测试用户',
            email: 'test@example.com'
          }
        }
      }

      authApi.login.mockResolvedValue(mockLoginResponse)

      const loginParams = {
        username: 'testuser',
        password: 'password123'
      }

      await authStore.login(loginParams)

      expect(authStore.token).toBe('new-token')
      expect(authStore.refreshToken).toBe('new-refresh-token')
      expect(authStore.userInfo).toEqual(mockLoginResponse.data.user)
      expect(authStore.isLoggedIn).toBe(true)

      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'new-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('refreshToken', 'new-refresh-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(mockLoginResponse.data.user))
    })

    it('登录失败时应该抛出错误', async () => {
      const errorMessage = '用户名或密码不正确'
      authApi.login.mockRejectedValue(new Error(errorMessage))

      const loginParams = {
        username: 'testuser',
        password: 'wrongpassword'
      }

      await expect(authStore.login(loginParams)).rejects.toThrow(errorMessage)

      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.isLoggedIn).toBe(false)
    })

    it('登录成功后应该获取用户权限', async () => {
      const mockLoginResponse = {
        data: {
          token: 'new-token',
          refreshToken: 'new-refresh-token',
          user: {
            id: 'user-123',
            username: 'testuser',
            realName: '测试用户',
            email: 'test@example.com'
          }
        }
      }

      const mockPermissions = ['user:view', 'user:create']

      authApi.login.mockResolvedValue(mockLoginResponse)
      authApi.getUserInfo.mockResolvedValue({
        data: {
          ...mockLoginResponse.data.user,
          permissions: mockPermissions
        }
      })

      await authStore.login({
        username: 'testuser',
        password: 'password123'
      })

      expect(authStore.permissions).toEqual(mockPermissions)
    })
  })

  describe('退出登录功能', () => {
    beforeEach(async () => {
      // 先设置登录状态
      authStore.token = 'existing-token'
      authStore.refreshToken = 'existing-refresh-token'
      authStore.userInfo = {
        id: 'user-123',
        username: 'testuser',
        realName: '测试用户',
        email: 'test@example.com'
      }
      authStore.permissions = ['user:view']
    })

    it('退出登录成功时应该清除状态', async () => {
      authApi.logout.mockResolvedValue({ data: null })

      await authStore.logout()

      expect(authStore.token).toBe('')
      expect(authStore.refreshToken).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isLoggedIn).toBe(false)

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('userInfo')
    })

    it('退出登录失败时也应该清除本地状态', async () => {
      authApi.logout.mockRejectedValue(new Error('Network error'))

      await authStore.logout()

      expect(authStore.token).toBe('')
      expect(authStore.refreshToken).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(authStore.permissions).toEqual([])
      expect(authStore.isLoggedIn).toBe(false)
    })

    it('退出登录后应该跳转到登录页', async () => {
      authApi.logout.mockResolvedValue({ data: null })

      await authStore.logout()

      expect(mockPush).toHaveBeenCalledWith('/login')
    })
  })

  describe('刷新令牌功能', () => {
    beforeEach(() => {
      authStore.refreshToken = 'existing-refresh-token'
    })

    it('刷新令牌成功时应该更新token', async () => {
      const mockRefreshResponse = {
        data: {
          token: 'new-access-token'
        }
      }

      authApi.refreshToken.mockResolvedValue(mockRefreshResponse)

      await authStore.refreshAccessToken()

      expect(authStore.token).toBe('new-access-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'new-access-token')
    })

    it('刷新令牌失败时应该退出登录', async () => {
      authApi.refreshToken.mockRejectedValue(new Error('Invalid refresh token'))

      await authStore.refreshAccessToken()

      expect(authStore.token).toBe('')
      expect(authStore.refreshToken).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(mockPush).toHaveBeenCalledWith('/login')
    })

    it('没有刷新令牌时应该直接退出登录', async () => {
      authStore.refreshToken = ''

      await authStore.refreshAccessToken()

      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(mockPush).toHaveBeenCalledWith('/login')
    })
  })

  describe('权限检查功能', () => {
    beforeEach(() => {
      authStore.permissions = ['user:view', 'user:create', 'admin:config']
    })

    it('应该正确检查单个权限', () => {
      expect(authStore.hasPermission('user:view')).toBe(true)
      expect(authStore.hasPermission('user:delete')).toBe(false)
    })

    it('应该正确检查多个权限（AND逻辑）', () => {
      expect(authStore.hasAllPermissions(['user:view', 'user:create'])).toBe(true)
      expect(authStore.hasAllPermissions(['user:view', 'user:delete'])).toBe(false)
    })

    it('应该正确检查多个权限（OR逻辑）', () => {
      expect(authStore.hasAnyPermission(['user:view', 'user:delete'])).toBe(true)
      expect(authStore.hasAnyPermission(['user:delete', 'user:update'])).toBe(false)
    })

    it('没有权限时应该返回false', () => {
      authStore.permissions = []
      
      expect(authStore.hasPermission('user:view')).toBe(false)
      expect(authStore.hasAllPermissions(['user:view'])).toBe(false)
      expect(authStore.hasAnyPermission(['user:view'])).toBe(false)
    })
  })

  describe('角色检查功能', () => {
    beforeEach(() => {
      authStore.userInfo = {
        id: 'user-123',
        username: 'testuser',
        realName: '测试用户',
        email: 'test@example.com',
        roles: ['USER', 'ADMIN']
      }
    })

    it('应该正确检查单个角色', () => {
      expect(authStore.hasRole('USER')).toBe(true)
      expect(authStore.hasRole('ADMIN')).toBe(true)
      expect(authStore.hasRole('AUDITOR')).toBe(false)
    })

    it('应该正确检查多个角色（AND逻辑）', () => {
      expect(authStore.hasAllRoles(['USER', 'ADMIN'])).toBe(true)
      expect(authStore.hasAllRoles(['USER', 'AUDITOR'])).toBe(false)
    })

    it('应该正确检查多个角色（OR逻辑）', () => {
      expect(authStore.hasAnyRole(['USER', 'AUDITOR'])).toBe(true)
      expect(authStore.hasAnyRole(['AUDITOR', 'GUEST'])).toBe(false)
    })

    it('没有用户信息时应该返回false', () => {
      authStore.userInfo = null
      
      expect(authStore.hasRole('USER')).toBe(false)
      expect(authStore.hasAllRoles(['USER'])).toBe(false)
      expect(authStore.hasAnyRole(['USER'])).toBe(false)
    })
  })

  describe('用户信息获取', () => {
    it('获取用户信息成功时应该更新状态', async () => {
      const mockUserInfo = {
        id: 'user-123',
        username: 'testuser',
        realName: '测试用户',
        email: 'test@example.com',
        roles: ['USER'],
        permissions: ['user:view']
      }

      authApi.getUserInfo.mockResolvedValue({ data: mockUserInfo })

      await authStore.fetchUserInfo()

      expect(authStore.userInfo).toEqual(mockUserInfo)
      expect(authStore.permissions).toEqual(['user:view'])
      expect(localStorageMock.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(mockUserInfo))
    })

    it('获取用户信息失败时应该退出登录', async () => {
      authApi.getUserInfo.mockRejectedValue(new Error('Unauthorized'))

      await authStore.fetchUserInfo()

      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
      expect(mockPush).toHaveBeenCalledWith('/login')
    })
  })

  describe('状态持久化', () => {
    it('应该正确保存状态到localStorage', async () => {
      const mockLoginResponse = {
        data: {
          token: 'new-token',
          refreshToken: 'new-refresh-token',
          user: {
            id: 'user-123',
            username: 'testuser',
            realName: '测试用户',
            email: 'test@example.com'
          }
        }
      }

      authApi.login.mockResolvedValue(mockLoginResponse)

      await authStore.login({
        username: 'testuser',
        password: 'password123'
      })

      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', 'new-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('refreshToken', 'new-refresh-token')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('userInfo', JSON.stringify(mockLoginResponse.data.user))
    })

    it('应该正确从localStorage清除状态', async () => {
      authApi.logout.mockResolvedValue({ data: null })

      await authStore.logout()

      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('refreshToken')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('userInfo')
    })
  })
})