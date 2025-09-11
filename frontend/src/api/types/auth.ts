// 登录请求接口
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应接口
export interface LoginResponse {
  token: string
  refreshToken: string
  user: {
    id: string
    username: string
    realName: string
    email?: string
    phone?: string
    roles: string[]
  }
}

// 刷新令牌请求接口
export interface RefreshTokenRequest {
  refreshToken: string
}

// 用户信息接口
export interface UserInfo {
  id: string
  username: string
  realName: string
  email?: string
  phone?: string
  roles: string[]
  permissions: string[]
  lastLoginTime?: string
  lastLoginIp?: string
}

// 修改密码请求接口
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 用户配置文件更新请求接口
export interface UpdateProfileRequest {
  realName?: string
  email?: string
  phone?: string
}

// 登录历史记录接口
export interface LoginHistory {
  id: string
  loginTime: string
  loginIp: string
  userAgent: string
  success: boolean
  failureReason?: string
}

// 会话信息接口
export interface SessionInfo {
  sessionId: string
  userId: string
  loginTime: string
  lastActiveTime: string
  ipAddress: string
  userAgent: string
  expiresAt: string
}

// 安全设置接口
export interface SecuritySettings {
  twoFactorEnabled: boolean
  loginNotificationEnabled: boolean
  sessionTimeout: number
  allowMultipleSessions: boolean
}