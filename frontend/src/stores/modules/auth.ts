import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import { authApi } from '@/api/modules/auth'
import router from '@/router'

// 存储键名常量
const STORAGE_KEYS = {
  TOKEN: 'auth_token',
  REFRESH_TOKEN: 'auth_refresh_token',
  USER_INFO: 'auth_user_info',
  PERMISSIONS: 'auth_permissions'
} as const

// 用户信息接口
export interface User {
  id: string
  username: string
  realName: string
  email?: string
  phone?: string
  roles: string[]
}

// 登录参数接口
export interface LoginParams {
  username: string
  password: string
}

export const useAuthStore = defineStore('auth', () => {
  // 状态定义
  const token = ref<string>('')
  const refreshToken = ref<string>('')
  const userInfo = ref<User | null>(null)
  const permissions = ref<string[]>([])
  const loading = ref<boolean>(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  
  const hasPermission = computed(() => (permission: string) => {
    if (!permissions.value.length) return false
    return permissions.value.includes(permission)
  })

  const hasRole = computed(() => (role: string) => {
    if (!userInfo.value?.roles.length) return false
    return userInfo.value.roles.includes(role)
  })

  const hasAnyRole = computed(() => (roles: string[]) => {
    if (!userInfo.value?.roles.length) return false
    return roles.some(role => userInfo.value!.roles.includes(role))
  })

  const hasAllRoles = computed(() => (roles: string[]) => {
    if (!userInfo.value?.roles.length) return false
    return roles.every(role => userInfo.value!.roles.includes(role))
  })

  // 检查是否为管理员
  const isAdmin = computed(() => hasRole.value('admin'))
  
  // 检查是否为业务用户
  const isBusinessUser = computed(() => hasRole.value('business_user'))
  
  // 检查是否为审核用户
  const isAuditUser = computed(() => hasRole.value('audit_user'))

  // 检查多个权限（AND关系）
  const hasAllPermissions = computed(() => (perms: string[]) => {
    if (!permissions.value.length) return false
    return perms.every(perm => permissions.value.includes(perm))
  })

  // 检查多个权限（OR关系）
  const hasAnyPermission = computed(() => (perms: string[]) => {
    if (!permissions.value.length) return false
    return perms.some(perm => permissions.value.includes(perm))
  })

  // 存储令牌到本地存储
  const setTokens = (accessToken: string, refreshTokenValue?: string) => {
    token.value = accessToken
    localStorage.setItem(STORAGE_KEYS.TOKEN, accessToken)
    
    if (refreshTokenValue) {
      refreshToken.value = refreshTokenValue
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshTokenValue)
    }
  }

  // 存储用户信息到本地存储
  const setUserInfo = (user: User) => {
    userInfo.value = user
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(user))
  }

  // 存储权限信息到本地存储
  const setPermissions = (perms: string[]) => {
    permissions.value = perms
    localStorage.setItem(STORAGE_KEYS.PERMISSIONS, JSON.stringify(perms))
  }

  // 清除所有认证信息
  const clearAuthData = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    permissions.value = []
    
    // 清除本地存储
    localStorage.removeItem(STORAGE_KEYS.TOKEN)
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
    localStorage.removeItem(STORAGE_KEYS.USER_INFO)
    localStorage.removeItem(STORAGE_KEYS.PERMISSIONS)
    
    // 清除会话存储
    sessionStorage.removeItem(STORAGE_KEYS.TOKEN)
    sessionStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)
    sessionStorage.removeItem(STORAGE_KEYS.USER_INFO)
    sessionStorage.removeItem(STORAGE_KEYS.PERMISSIONS)
  }

  // 登录操作
  const login = async (params: LoginParams): Promise<void> => {
    try {
      loading.value = true
      
      const response = await authApi.login(params)
      
      if (!response.success || !response.data) {
        throw new Error(response.message || '登录失败')
      }
      
      const { token: accessToken, refreshToken: refreshTokenValue, user } = response.data
      
      // 验证响应数据完整性
      if (!accessToken || !user || !user.id) {
        throw new Error('登录响应数据不完整')
      }
      
      // 存储令牌
      setTokens(accessToken, refreshTokenValue)
      
      // 存储用户信息
      setUserInfo({
        id: user.id,
        username: user.username,
        realName: user.realName,
        email: user.email,
        phone: user.phone,
        roles: user.roles || []
      })
      
      // 获取用户权限
      await fetchUserPermissions()
      
      console.log('Login completed successfully:', {
        userId: user.id,
        username: user.username,
        roles: user.roles
      })
      
    } catch (error: any) {
      console.error('Login failed:', error.message)
      clearAuthData()
      throw error
    } finally {
      loading.value = false
    }
  }

  // 登出操作
  const logout = async (): Promise<void> => {
    const currentUserId = userInfo.value?.id
    
    try {
      loading.value = true
      
      // 调用登出API
      if (token.value) {
        const response = await authApi.logout()
        
        if (response.success) {
          console.log('Logout API call successful')
        }
      }
    } catch (error: any) {
      console.error('Logout API error:', error.message)
      // 登出API失败不影响本地清理
    } finally {
      // 无论API调用是否成功，都清除本地数据
      clearAuthData()
      loading.value = false
      
      console.log('Logout completed for user:', currentUserId)
      
      // 跳转到登录页，避免在当前页面已经是登录页时重复跳转
      if (router.currentRoute.value.path !== '/login') {
        await router.push('/login')
      }
    }
  }

  // 刷新令牌
  const refreshTokenAction = async (): Promise<boolean> => {
    try {
      if (!refreshToken.value) {
        console.warn('No refresh token available for refresh')
        return false
      }
      
      const response = await authApi.refreshToken(refreshToken.value)
      
      if (!response.success || !response.data?.token) {
        console.error('Invalid refresh token response:', response)
        return false
      }
      
      const { token: newToken, refreshToken: newRefreshToken } = response.data
      
      // 更新访问令牌，如果返回了新的refresh token也一起更新
      setTokens(newToken, newRefreshToken || refreshToken.value)
      
      console.log('Token refresh successful')
      return true
      
    } catch (error: any) {
      console.error('Token refresh failed:', error.message)
      
      // 刷新失败，清除所有数据
      clearAuthData()
      
      // 只有在非登录页面时才跳转
      if (router.currentRoute.value.path !== '/login') {
        await router.push('/login')
      }
      
      return false
    }
  }

  // 获取用户信息
  const fetchUserInfo = async (): Promise<void> => {
    try {
      if (!token.value) return
      
      const response = await authApi.getUserInfo()
      const userData = response.data
      
      setUserInfo({
        id: userData.id,
        username: userData.username,
        realName: userData.realName,
        email: userData.email,
        phone: userData.phone,
        roles: userData.roles
      })
      
      // 同时更新权限信息
      setPermissions(userData.permissions || [])
      
    } catch (error) {
      console.error('Fetch user info error:', error)
      throw error
    }
  }

  // 获取用户权限
  const fetchUserPermissions = async (): Promise<void> => {
    try {
      if (!token.value) return
      
      const response = await authApi.getUserPermissions()
      setPermissions(response.data)
      
    } catch (error) {
      console.error('Fetch permissions error:', error)
      // 权限获取失败不影响登录，设置为空数组
      setPermissions([])
    }
  }

  // 初始化认证状态
  const initAuth = async (): Promise<void> => {
    try {
      // 从localStorage恢复数据
      const savedToken = localStorage.getItem(STORAGE_KEYS.TOKEN)
      const savedRefreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
      const savedUserInfo = localStorage.getItem(STORAGE_KEYS.USER_INFO)
      const savedPermissions = localStorage.getItem(STORAGE_KEYS.PERMISSIONS)
      
      if (savedToken && savedUserInfo) {
        token.value = savedToken
        refreshToken.value = savedRefreshToken || ''
        userInfo.value = JSON.parse(savedUserInfo)
        permissions.value = savedPermissions ? JSON.parse(savedPermissions) : []
        
        // 验证token是否仍然有效，如果无效则尝试刷新
        try {
          await fetchUserInfo()
        } catch (error) {
          // 如果获取用户信息失败，尝试刷新token
          if (refreshToken.value) {
            const refreshSuccess = await refreshTokenAction()
            if (refreshSuccess) {
              await fetchUserInfo()
            }
          } else {
            // 没有refresh token，清除数据
            clearAuthData()
          }
        }
      }
    } catch (error) {
      console.error('Init auth error:', error)
      clearAuthData()
    }
  }

  // 检查权限（支持多种权限检查方式）
  const checkPermission = (permission: string | string[]): boolean => {
    if (!permissions.value.length) return false
    
    if (Array.isArray(permission)) {
      // 检查是否拥有任意一个权限
      return permission.some(perm => permissions.value.includes(perm))
    } else {
      return permissions.value.includes(permission)
    }
  }

  // 检查角色
  const checkRole = (role: string | string[]): boolean => {
    if (!userInfo.value?.roles.length) return false
    
    if (Array.isArray(role)) {
      // 检查是否拥有任意一个角色
      return role.some(r => userInfo.value!.roles.includes(r))
    } else {
      return userInfo.value.roles.includes(role)
    }
  }

  // 更新用户信息
  const updateUserInfo = (updates: Partial<User>): void => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...updates }
      localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(userInfo.value))
    }
  }

  return {
    // 状态
    token: readonly(token),
    refreshToken: readonly(refreshToken),
    userInfo: readonly(userInfo),
    permissions: readonly(permissions),
    loading: readonly(loading),
    
    // 计算属性
    isLoggedIn,
    hasPermission,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    isAdmin,
    isBusinessUser,
    isAuditUser,
    hasAllPermissions,
    hasAnyPermission,
    
    // 方法
    login,
    logout,
    refreshTokenAction,
    fetchUserInfo,
    fetchUserPermissions,
    initAuth,
    checkPermission,
    checkRole,
    updateUserInfo,
    clearAuthData
  }
})