/**
 * 认证相关工具函数
 */

// 存储键名常量
export const AUTH_STORAGE_KEYS = {
  TOKEN: 'auth_token',
  REFRESH_TOKEN: 'auth_refresh_token',
  USER_INFO: 'auth_user_info',
  PERMISSIONS: 'auth_permissions'
} as const

/**
 * 获取存储的token
 */
export const getToken = (): string | null => {
  return localStorage.getItem(AUTH_STORAGE_KEYS.TOKEN) || sessionStorage.getItem(AUTH_STORAGE_KEYS.TOKEN)
}

/**
 * 设置token到存储
 */
export const setToken = (token: string, useSessionStorage = false): void => {
  if (useSessionStorage) {
    sessionStorage.setItem(AUTH_STORAGE_KEYS.TOKEN, token)
    localStorage.removeItem(AUTH_STORAGE_KEYS.TOKEN)
  } else {
    localStorage.setItem(AUTH_STORAGE_KEYS.TOKEN, token)
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.TOKEN)
  }
}

/**
 * 移除token
 */
export const removeToken = (): void => {
  localStorage.removeItem(AUTH_STORAGE_KEYS.TOKEN)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.TOKEN)
}

/**
 * 获取refresh token
 */
export const getRefreshToken = (): string | null => {
  return localStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN) || sessionStorage.getItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
}

/**
 * 设置refresh token
 */
export const setRefreshToken = (refreshToken: string, useSessionStorage = false): void => {
  if (useSessionStorage) {
    sessionStorage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
    localStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
  } else {
    localStorage.setItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
  }
}

/**
 * 移除refresh token
 */
export const removeRefreshToken = (): void => {
  localStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.REFRESH_TOKEN)
}

/**
 * 获取用户信息
 */
export const getUserInfo = (): any | null => {
  const userInfoStr = localStorage.getItem(AUTH_STORAGE_KEYS.USER_INFO) || sessionStorage.getItem(AUTH_STORAGE_KEYS.USER_INFO)
  if (userInfoStr) {
    try {
      return JSON.parse(userInfoStr)
    } catch (error) {
      console.error('Parse user info error:', error)
      return null
    }
  }
  return null
}

/**
 * 设置用户信息
 */
export const setUserInfo = (userInfo: any, useSessionStorage = false): void => {
  const userInfoStr = JSON.stringify(userInfo)
  if (useSessionStorage) {
    sessionStorage.setItem(AUTH_STORAGE_KEYS.USER_INFO, userInfoStr)
    localStorage.removeItem(AUTH_STORAGE_KEYS.USER_INFO)
  } else {
    localStorage.setItem(AUTH_STORAGE_KEYS.USER_INFO, userInfoStr)
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.USER_INFO)
  }
}

/**
 * 移除用户信息
 */
export const removeUserInfo = (): void => {
  localStorage.removeItem(AUTH_STORAGE_KEYS.USER_INFO)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.USER_INFO)
}

/**
 * 获取权限列表
 */
export const getPermissions = (): string[] => {
  const permissionsStr = localStorage.getItem(AUTH_STORAGE_KEYS.PERMISSIONS) || sessionStorage.getItem(AUTH_STORAGE_KEYS.PERMISSIONS)
  if (permissionsStr) {
    try {
      return JSON.parse(permissionsStr)
    } catch (error) {
      console.error('Parse permissions error:', error)
      return []
    }
  }
  return []
}

/**
 * 设置权限列表
 */
export const setPermissions = (permissions: string[], useSessionStorage = false): void => {
  const permissionsStr = JSON.stringify(permissions)
  if (useSessionStorage) {
    sessionStorage.setItem(AUTH_STORAGE_KEYS.PERMISSIONS, permissionsStr)
    localStorage.removeItem(AUTH_STORAGE_KEYS.PERMISSIONS)
  } else {
    localStorage.setItem(AUTH_STORAGE_KEYS.PERMISSIONS, permissionsStr)
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.PERMISSIONS)
  }
}

/**
 * 移除权限列表
 */
export const removePermissions = (): void => {
  localStorage.removeItem(AUTH_STORAGE_KEYS.PERMISSIONS)
  sessionStorage.removeItem(AUTH_STORAGE_KEYS.PERMISSIONS)
}

/**
 * 清除所有认证相关数据
 */
export const clearAuthData = (): void => {
  removeToken()
  removeRefreshToken()
  removeUserInfo()
  removePermissions()
}

/**
 * 检查token是否过期
 */
export const isTokenExpired = (token: string): boolean => {
  if (!token) return true
  
  try {
    // 解析JWT token
    const payload = JSON.parse(atob(token.split('.')[1]))
    const currentTime = Math.floor(Date.now() / 1000)
    
    // 检查是否过期
    return payload.exp < currentTime
  } catch (error) {
    console.error('Parse token error:', error)
    return true
  }
}

/**
 * 获取token剩余有效时间（秒）
 */
export const getTokenRemainingTime = (token: string): number => {
  if (!token) return 0
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const currentTime = Math.floor(Date.now() / 1000)
    
    return Math.max(0, payload.exp - currentTime)
  } catch (error) {
    console.error('Parse token error:', error)
    return 0
  }
}

/**
 * 从token中获取用户信息
 */
export const getUserInfoFromToken = (token: string): any | null => {
  if (!token) return null
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      id: payload.sub || payload.userId,
      username: payload.username,
      realName: payload.realName,
      roles: payload.roles || [],
      permissions: payload.permissions || []
    }
  } catch (error) {
    console.error('Parse token error:', error)
    return null
  }
}