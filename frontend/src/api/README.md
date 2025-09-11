# API 集成使用指南

## 概述

本项目的API集成提供了完整的认证和权限管理功能，包括自动令牌刷新、错误处理、重试机制和加载状态管理。

## 主要特性

### 1. 自动令牌刷新
- 当访问令牌过期时，自动使用刷新令牌获取新的访问令牌
- 支持请求队列，避免并发刷新
- 刷新失败时自动跳转到登录页

### 2. 错误处理和重试
- 网络错误时自动重试（指数退避策略）
- 统一的错误处理和用户友好的错误提示
- 支持不同类型错误的特殊处理

### 3. 加载状态管理
- 每个API调用都有对应的加载状态
- 可以在UI组件中实时获取加载状态
- 支持全局加载状态监控

## 使用方法

### 基本API调用

```typescript
import { authApi } from '@/api/modules/auth'

// 登录
try {
  const response = await authApi.login({
    username: 'user@example.com',
    password: 'password123'
  })
  
  if (response.success) {
    console.log('登录成功:', response.data)
  }
} catch (error) {
  console.error('登录失败:', error.message)
}
```

### 使用加载状态

```vue
<template>
  <a-button 
    :loading="loginLoading" 
    @click="handleLogin"
  >
    {{ loginLoading ? '登录中...' : '登录' }}
  </a-button>
</template>

<script setup lang="ts">
import { useApiLoading } from '@/composables/useApiLoading'

const { loginLoading } = useApiLoading()

const handleLogin = async () => {
  // API调用会自动管理加载状态
  await authApi.login(loginData)
}
</script>
```

### 错误处理

```typescript
import { ApiError, ApiErrorHandler } from '@/utils/apiError'

try {
  await authApi.login(loginData)
} catch (error) {
  if (error instanceof ApiError) {
    // 处理特定的API错误
    switch (error.type) {
      case ApiErrorType.AUTH_ERROR:
        // 处理认证错误
        break
      case ApiErrorType.NETWORK_ERROR:
        // 处理网络错误
        break
    }
  }
  
  // 显示错误消息
  ApiErrorHandler.showError(error)
}
```

## API 接口列表

### 认证相关

| 方法 | 端点 | 描述 |
|------|------|------|
| `authApi.login()` | `POST /api/v1/auth/login` | 用户登录 |
| `authApi.logout()` | `POST /api/v1/auth/logout` | 用户登出 |
| `authApi.refreshToken()` | `POST /api/v1/auth/refresh` | 刷新令牌 |
| `authApi.getUserInfo()` | `GET /api/v1/auth/user-info` | 获取用户信息 |
| `authApi.getUserPermissions()` | `GET /api/v1/auth/permissions` | 获取用户权限 |
| `authApi.changePassword()` | `POST /api/v1/auth/change-password` | 修改密码 |
| `authApi.checkLoginStatus()` | `GET /api/v1/auth/check` | 检查登录状态 |

## 配置

### 环境变量

```env
# API基础URL
VITE_API_BASE_URL=/api
```

### 请求配置

- **超时时间**: 30秒
- **重试次数**: 最多3次
- **重试延迟**: 指数退避（1s, 2s, 4s）
- **自动刷新**: 支持

## 类型定义

### 请求类型

```typescript
interface LoginRequest {
  username: string
  password: string
}

interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}
```

### 响应类型

```typescript
interface ApiResponse<T> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp: number
}

interface LoginResponse {
  token: string
  refreshToken: string
  user: {
    id: string
    username: string
    realName: string
    email?: string
    roles: string[]
  }
}
```

## 最佳实践

### 1. 错误处理
- 始终使用 try-catch 包装API调用
- 使用 ApiErrorHandler 统一处理错误
- 为用户提供友好的错误提示

### 2. 加载状态
- 在UI中显示加载状态，提升用户体验
- 使用 useApiLoading 组合式函数管理状态
- 避免重复提交请求

### 3. 令牌管理
- 不需要手动管理令牌刷新
- 系统会自动处理令牌过期和刷新
- 刷新失败时会自动跳转登录页

### 4. 测试
- 为API调用编写单元测试
- 使用 Mock 数据进行测试
- 测试错误场景和边界情况

## 故障排除

### 常见问题

1. **网络错误**
   - 检查网络连接
   - 确认API服务器状态
   - 查看浏览器控制台错误

2. **认证失败**
   - 检查用户名和密码
   - 确认账户未被锁定
   - 检查令牌是否过期

3. **权限不足**
   - 确认用户角色和权限
   - 检查API端点权限配置
   - 联系管理员分配权限

### 调试技巧

1. 开启浏览器开发者工具
2. 查看网络请求和响应
3. 检查控制台错误日志
4. 使用API测试工具验证接口

## 更新日志

### v1.0.0
- 实现基础认证API
- 添加自动令牌刷新
- 实现错误处理和重试
- 添加加载状态管理
- 完善类型定义