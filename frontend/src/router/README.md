# 前端路由配置和守卫系统

本文档描述了智能保险产品检核系统前端路由配置和守卫系统的实现。

## 目录结构

```
src/router/
├── index.ts              # 主路由配置文件
├── guards/               # 路由守卫
│   ├── index.ts         # 守卫入口文件
│   ├── auth.ts          # 认证守卫
│   └── permission.ts    # 权限守卫
├── modules/             # 路由模块
│   ├── auth.ts          # 认证相关路由
│   ├── dashboard.ts     # 工作台路由
│   ├── user.ts          # 用户管理路由
│   ├── admin.ts         # 管理员路由
│   └── error.ts         # 错误页面路由
├── utils.ts             # 路由工具函数
└── README.md            # 本文档
```

## 主要功能

### 1. 路由配置 (index.ts)

- ✅ 使用 Vue Router 4.x 配置路由表
- ✅ 实现路由懒加载，使用 `() => import()` 语法进行代码分割
- ✅ 配置 history 模式，设置 base 路径
- ✅ 实现滚动行为配置
- ✅ 扩展路由元信息类型定义

#### 路由元信息类型

```typescript
interface RouteMeta {
  title?: string          // 页面标题
  requiresAuth?: boolean  // 是否需要认证
  permissions?: string[]  // 需要的权限列表
  roles?: string[]        // 需要的角色列表
  icon?: string          // 图标
  hidden?: boolean       // 是否在菜单中隐藏
  keepAlive?: boolean    // 是否缓存组件
}
```

### 2. 认证守卫 (guards/auth.ts)

- ✅ 检查认证状态，未登录用户重定向到登录页
- ✅ 支持 redirect 参数，登录成功后跳转回原页面
- ✅ 实现登录成功后的页面跳转逻辑
- ✅ 防止开放重定向攻击

#### 主要功能

```typescript
// 设置认证守卫
setupAuthGuard(router)

// 处理登录重定向
handleLoginRedirect(router, redirectPath)

// 检查路由权限
hasRoutePermission(route, authStore)
```

### 3. 权限守卫 (guards/permission.ts)

- ✅ 实现路由权限验证，检查用户是否有访问特定页面的权限
- ✅ 支持基于权限和角色的访问控制
- ✅ 提供动态权限检查函数

#### 主要功能

```typescript
// 设置权限守卫
setupPermissionGuard(router)

// 动态权限检查
checkDynamicPermission(permissions)
checkDynamicRole(roles)
```

### 4. 页面标题管理

- ✅ 根据路由 meta.title 动态更新 document.title
- ✅ 统一页面标题格式：`${title} - 智能保险产品检核系统`

### 5. 路由模块化

路由按功能模块组织，便于维护：

- **auth.ts**: 登录、注册、忘记密码等认证相关路由
- **dashboard.ts**: 工作台主页路由
- **user.ts**: 用户管理相关路由
- **admin.ts**: 管理员功能路由
- **error.ts**: 错误页面路由（403、404、500）

### 6. 路由工具函数 (utils.ts)

提供丰富的路由工具函数：

```typescript
// 过滤路由（根据权限）
filterRoutes(routes, authStore)

// 生成面包屑导航
generateBreadcrumbs(route)

// 获取路由标题
getRouteTitle(route)

// 检查路由是否活跃
isRouteActive(routePath, currentPath)

// 构建菜单数据
buildMenuData(routes, authStore)

// 路由跳转工具
navigateTo(router, path, options)
```

## 使用示例

### 1. 基本路由配置

```typescript
// 在 main.ts 中使用
import router from '@/router'
import { useAuthStore } from '@/stores/modules/auth'

const app = createApp(App)
app.use(router)

// 初始化认证状态
const authStore = useAuthStore()
await authStore.initAuth()
```

### 2. 在组件中使用路由

```vue
<template>
  <div>
    <router-link to="/dashboard">工作台</router-link>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { handleLoginRedirect } from '@/router/guards/auth'

const router = useRouter()
const route = useRoute()

// 处理登录重定向
const handleLogin = () => {
  const redirect = route.query.redirect as string
  handleLoginRedirect(router, redirect)
}
</script>
```

### 3. 权限控制

```vue
<template>
  <div>
    <!-- 基于权限显示内容 -->
    <a-button v-if="hasPermission('user:create')" @click="createUser">
      创建用户
    </a-button>
    
    <!-- 基于角色显示内容 -->
    <admin-panel v-if="hasRole('admin')" />
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/modules/auth'
import { checkDynamicPermission, checkDynamicRole } from '@/router/guards/permission'

const authStore = useAuthStore()

const hasPermission = (permission: string) => {
  return checkDynamicPermission(permission)
}

const hasRole = (role: string) => {
  return checkDynamicRole(role)
}
</script>
```

### 4. 菜单生成

```vue
<template>
  <a-menu :items="menuItems" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import { buildMenuData } from '@/router/utils'
import { routes } from '@/router'

const authStore = useAuthStore()

const menuItems = computed(() => {
  return buildMenuData(routes, authStore)
})
</script>
```

## 安全特性

### 1. 认证保护

- 所有需要认证的路由都会检查用户登录状态
- 未登录用户自动重定向到登录页
- 支持登录后回到原页面

### 2. 权限控制

- 基于权限的细粒度访问控制
- 基于角色的粗粒度访问控制
- 权限不足时显示友好提示

### 3. 安全重定向

- 防止开放重定向攻击
- 验证重定向URL的安全性
- 只允许内部路径重定向

### 4. 错误处理

- 完善的错误页面（403、404、500）
- 友好的错误提示信息
- 错误状态的正确处理

## 测试

路由系统包含完整的单元测试：

```bash
# 运行路由测试
npm run test -- --run src/test/router

# 运行特定测试文件
npm run test -- --run src/test/router/router.test.ts
npm run test -- --run src/test/router/guards.test.ts
```

测试覆盖：
- ✅ 路由配置正确性
- ✅ 认证守卫功能
- ✅ 权限守卫功能
- ✅ 重定向逻辑
- ✅ 滚动行为
- ✅ 懒加载组件

## 性能优化

### 1. 代码分割

- 所有路由组件都使用懒加载
- 按模块分割代码，减少初始包大小
- 支持组件级别的缓存（keepAlive）

### 2. 路由缓存

- 支持路由级别的组件缓存
- 通过 meta.keepAlive 控制缓存策略

### 3. 权限缓存

- 权限信息缓存在 Pinia store 中
- 避免重复的权限检查请求

## 扩展指南

### 1. 添加新路由

1. 在对应的模块文件中添加路由配置
2. 创建对应的 Vue 组件
3. 配置路由元信息（权限、角色等）
4. 更新测试用例

### 2. 添加新权限

1. 在后端定义新权限
2. 在路由 meta 中配置权限要求
3. 在组件中使用权限检查函数
4. 更新权限相关测试

### 3. 自定义守卫

```typescript
// 创建自定义守卫
export function setupCustomGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    // 自定义逻辑
    next()
  })
}

// 在 guards/index.ts 中注册
import { setupCustomGuard } from './custom'

export function setupRouterGuards(router: Router) {
  setupAuthGuard(router)
  setupPermissionGuard(router)
  setupCustomGuard(router) // 添加自定义守卫
}
```

## 最佳实践

1. **路由命名**: 使用 PascalCase 命名路由
2. **路径设计**: 使用 kebab-case 命名路径
3. **权限粒度**: 合理设计权限粒度，避免过细或过粗
4. **错误处理**: 提供友好的错误页面和提示
5. **性能考虑**: 合理使用懒加载和缓存
6. **测试覆盖**: 为所有路由功能编写测试用例

## 故障排除

### 1. 路由不生效

- 检查路由配置是否正确
- 确认组件路径是否存在
- 检查路由守卫是否阻止了导航

### 2. 权限检查失败

- 确认用户已登录
- 检查权限配置是否正确
- 验证后端权限数据是否正确

### 3. 重定向循环

- 检查路由重定向配置
- 确认守卫逻辑没有冲突
- 验证默认路由设置

通过以上配置和实现，前端路由系统提供了完整的认证、权限控制和导航功能，满足了任务要求的所有功能点。