# 前端开发规范

## 技术栈

### 核心框架
- **前端框架**: Vue 3.4.x (Composition API)
- **构建工具**: Vite 5.x
- **包管理器**: pnpm 8.x
- **TypeScript**: 5.x
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia 2.x
- **路由管理**: Vue Router 4.x

### 开发工具
- **IDE**: VS Code + Volar
- **代码格式化**: Prettier + ESLint
- **代码检查**: ESLint + TypeScript ESLint
- **样式预处理**: Sass/SCSS
- **测试框架**: Vitest + Vue Test Utils
- **E2E测试**: Playwright
- **版本控制**: Git + GitFlow

### 工程化工具
- **构建优化**: Vite Bundle Analyzer
- **代码质量**: SonarQube
- **CI/CD**: GitHub Actions / Jenkins
- **容器化**: Docker + Nginx
- **监控**: Sentry (错误监控)

## 项目结构

### 标准目录结构

```
frontend/
├── public/                           # 静态资源
│   ├── favicon.ico
│   ├── logo.png
│   └── index.html
├── src/
│   ├── main.ts                       # 应用入口
│   ├── App.vue                       # 根组件
│   ├── assets/                       # 静态资源
│   │   ├── images/                   # 图片资源
│   │   ├── icons/                    # 图标资源
│   │   └── styles/                   # 全局样式
│   │       ├── index.scss            # 样式入口
│   │       ├── variables.scss        # 变量定义
│   │       ├── mixins.scss           # 混入函数
│   │       └── reset.scss            # 样式重置
│   ├── components/                   # 通用组件
│   │   ├── common/                   # 基础组件
│   │   │   ├── BaseButton/
│   │   │   ├── BaseInput/
│   │   │   ├── BaseTable/
│   │   │   └── BaseModal/
│   │   ├── business/                 # 业务组件
│   │   │   ├── DocumentUpload/
│   │   │   ├── AuditResult/
│   │   │   └── RuleConfig/
│   │   └── layout/                   # 布局组件
│   │       ├── AppHeader/
│   │       ├── AppSidebar/
│   │       └── AppFooter/
│   ├── views/                        # 页面组件
│   │   ├── auth/                     # 认证相关
│   │   │   ├── Login.vue
│   │   │   └── Register.vue
│   │   ├── dashboard/                # 工作台
│   │   │   └── Dashboard.vue
│   │   ├── document/                 # 文档管理
│   │   │   ├── DocumentList.vue
│   │   │   ├── DocumentDetail.vue
│   │   │   └── DocumentUpload.vue
│   │   ├── audit/                    # 检核管理
│   │   │   ├── AuditTask.vue
│   │   │   ├── AuditResult.vue
│   │   │   └── AuditHistory.vue
│   │   ├── rule/                     # 规则管理
│   │   │   ├── RuleList.vue
│   │   │   ├── RuleEditor.vue
│   │   │   └── RuleTest.vue
│   │   └── system/                   # 系统管理
│   │       ├── UserManagement.vue
│   │       └── SystemConfig.vue│
   ├── router/                       # 路由配置
│   │   ├── index.ts                  # 路由入口
│   │   ├── modules/                  # 路由模块
│   │   │   ├── auth.ts
│   │   │   ├── document.ts
│   │   │   ├── audit.ts
│   │   │   └── system.ts
│   │   └── guards/                   # 路由守卫
│   │       ├── auth.ts
│   │       └── permission.ts
│   ├── stores/                       # 状态管理
│   │   ├── index.ts                  # Store入口
│   │   ├── modules/                  # Store模块
│   │   │   ├── auth.ts               # 认证状态
│   │   │   ├── user.ts               # 用户信息
│   │   │   ├── document.ts           # 文档状态
│   │   │   ├── audit.ts              # 检核状态
│   │   │   └── app.ts                # 应用状态
│   │   └── types/                    # Store类型定义
│   ├── api/                          # API接口
│   │   ├── index.ts                  # API入口
│   │   ├── request.ts                # 请求封装
│   │   ├── types/                    # API类型定义
│   │   │   ├── common.ts             # 通用类型
│   │   │   ├── auth.ts               # 认证类型
│   │   │   ├── document.ts           # 文档类型
│   │   │   ├── audit.ts              # 检核类型
│   │   │   └── user.ts               # 用户类型
│   │   └── modules/                  # API模块
│   │       ├── auth.ts               # 认证接口
│   │       ├── document.ts           # 文档接口
│   │       ├── audit.ts              # 检核接口
│   │       ├── rule.ts               # 规则接口
│   │       └── user.ts               # 用户接口
│   ├── utils/                        # 工具函数
│   │   ├── index.ts                  # 工具入口
│   │   ├── request.ts                # 请求工具
│   │   ├── storage.ts                # 存储工具
│   │   ├── auth.ts                   # 认证工具
│   │   ├── format.ts                 # 格式化工具
│   │   ├── validate.ts               # 验证工具
│   │   ├── file.ts                   # 文件工具
│   │   └── constants.ts              # 常量定义
│   ├── composables/                  # 组合式函数
│   │   ├── useAuth.ts                # 认证逻辑
│   │   ├── useTable.ts               # 表格逻辑
│   │   ├── useForm.ts                # 表单逻辑
│   │   ├── useUpload.ts              # 上传逻辑
│   │   └── usePermission.ts          # 权限逻辑
│   ├── directives/                   # 自定义指令
│   │   ├── index.ts                  # 指令入口
│   │   ├── permission.ts             # 权限指令
│   │   └── loading.ts                # 加载指令
│   ├── plugins/                      # 插件配置
│   │   ├── index.ts                  # 插件入口
│   │   ├── antd.ts                   # Ant Design配置
│   │   └── i18n.ts                   # 国际化配置
│   └── types/                        # 全局类型定义
│       ├── index.ts                  # 类型入口
│       ├── global.d.ts               # 全局类型
│       ├── env.d.ts                  # 环境变量类型
│       └── vue.d.ts                  # Vue类型扩展
├── tests/                            # 测试文件
│   ├── unit/                         # 单元测试
│   ├── e2e/                          # E2E测试
│   └── __mocks__/                    # Mock文件
├── docs/                             # 项目文档
├── .env                              # 环境变量
├── .env.development                  # 开发环境变量
├── .env.production                   # 生产环境变量
├── .eslintrc.js                      # ESLint配置
├── .prettierrc                       # Prettier配置
├── tsconfig.json                     # TypeScript配置
├── vite.config.ts                    # Vite配置
├── package.json                      # 项目配置
└── README.md                         # 项目说明
```## 编码规范


### 命名规范

#### 文件命名
- **组件文件**: PascalCase，如 `UserProfile.vue`、`DocumentList.vue`
- **页面文件**: PascalCase，如 `Dashboard.vue`、`Login.vue`
- **工具文件**: camelCase，如 `request.ts`、`formatDate.ts`
- **类型文件**: camelCase，如 `user.ts`、`document.ts`
- **常量文件**: camelCase，如 `constants.ts`、`enums.ts`

#### 变量命名
```typescript
// 正确的变量命名
const userName = 'admin'                    // 用户名
const userList = []                         // 用户列表
const isLoading = false                     // 布尔值以is开头
const hasPermission = true                  // 布尔值以has开头
const canEdit = false                       // 布尔值以can开头
const totalCount = 100                      // 总数量
const createdAt = new Date()                // 创建时间

// 避免的命名方式
const s = 'string'                          // 过于简短
const list = []                             // 缺少具体含义
const flag = true                           // 含义不明确
const num = 10                              // 含义不明确
```

#### 函数命名
```typescript
// 事件处理函数
const handleSubmit = () => {}               // 处理提交
const handleClick = () => {}                // 处理点击
const handleChange = () => {}               // 处理变更

// 业务逻辑函数
const fetchUserList = () => {}              // 获取用户列表
const createDocument = () => {}             // 创建文档
const updateUserInfo = () => {}             // 更新用户信息
const deleteRecord = () => {}               // 删除记录

// 工具函数
const formatDate = () => {}                 // 格式化日期
const validateEmail = () => {}              // 验证邮箱
const generateId = () => {}                 // 生成ID
```

#### 组件命名
```vue
<!-- 正确的组件命名 -->
<template>
  <div class="user-profile">
    <BaseButton @click="handleSave">保存</BaseButton>
    <DocumentUpload v-model="fileList" />
    <AuditResultTable :data="auditResults" />
  </div>
</template>

<script setup lang="ts">
// 组件名使用PascalCase
defineOptions({
  name: 'UserProfile'
})
</script>
```

### TypeScript规范

#### 类型定义
```typescript
// 接口定义 - 使用PascalCase
interface User {
  id: string
  name: string
  email: string
  role: UserRole
  createdAt: Date
  updatedAt: Date
}

// 类型别名 - 使用PascalCase
type UserRole = 'admin' | 'user' | 'guest'
type ApiResponse<T> = {
  code: number
  message: string
  data: T
  success: boolean
}

// 枚举定义 - 使用PascalCase
enum DocumentStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

// 常量定义 - 使用UPPER_SNAKE_CASE
const API_BASE_URL = 'https://api.example.com'
const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
const DEFAULT_PAGE_SIZE = 20
```

#### 函数类型定义
```typescript
// 函数参数和返回值类型
interface CreateUserParams {
  name: string
  email: string
  role: UserRole
}

interface UserService {
  createUser(params: CreateUserParams): Promise<User>
  getUserById(id: string): Promise<User | null>
  updateUser(id: string, data: Partial<User>): Promise<User>
  deleteUser(id: string): Promise<void>
}

// 事件处理函数类型
type ClickHandler = (event: MouseEvent) => void
type ChangeHandler<T> = (value: T) => void
type SubmitHandler<T> = (data: T) => Promise<void>
```

### Vue 3 Composition API规范

#### 组件结构
```vue
<template>
  <div class="document-list">
    <!-- 搜索区域 -->
    <div class="search-section">
      <a-input
        v-model:value="searchForm.keyword"
        placeholder="请输入文档名称"
        @press-enter="handleSearch"
      />
      <a-button type="primary" @click="handleSearch">搜索</a-button>
    </div>

    <!-- 表格区域 -->
    <a-table
      :columns="columns"
      :data-source="documentList"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import { useDocumentStore } from '@/stores/modules/document'
import { documentApi } from '@/api/modules/document'
import type { Document, DocumentQuery } from '@/api/types/document'

// 组件名称定义
defineOptions({
  name: 'DocumentList'
})

// Props定义
interface Props {
  readonly?: boolean
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  showActions: true
})

// Emits定义
interface Emits {
  select: [document: Document]
  delete: [id: string]
}

const emit = defineEmits<Emits>()

// 状态管理
const documentStore = useDocumentStore()

// 响应式数据
const loading = ref(false)
const documentList = ref<Document[]>([])
const searchForm = reactive<DocumentQuery>({
  keyword: '',
  status: undefined,
  page: 1,
  size: 20
})

// 计算属性
const pagination = computed(() => ({
  current: searchForm.page,
  pageSize: searchForm.size,
  total: documentStore.total,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
}))

// 表格列定义
const columns: TableColumnsType<Document> = [
  {
    title: '文档名称',
    dataIndex: 'name',
    key: 'name',
    ellipsis: true
  },
  {
    title: '文档类型',
    dataIndex: 'type',
    key: 'type',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180
  }
]

// 方法定义
const fetchDocumentList = async () => {
  try {
    loading.value = true
    const response = await documentApi.getDocumentList(searchForm)
    documentList.value = response.data.records
    documentStore.setTotal(response.data.total)
  } catch (error) {
    message.error('获取文档列表失败')
    console.error('Fetch document list error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.page = 1
  fetchDocumentList()
}

const handleTableChange: TableProps['onChange'] = (pagination) => {
  if (pagination) {
    searchForm.page = pagination.current || 1
    searchForm.size = pagination.pageSize || 20
    fetchDocumentList()
  }
}

// 生命周期
onMounted(() => {
  fetchDocumentList()
})

// 暴露给父组件的方法
defineExpose({
  refresh: fetchDocumentList
})
</script>

<style lang="scss" scoped>
.document-list {
  .search-section {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
    align-items: center;
  }
}
</style>
```###
 状态管理规范 (Pinia)

#### Store结构
```typescript
// stores/modules/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginParams, LoginResponse } from '@/api/types/auth'
import { authApi } from '@/api/modules/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态定义
  const token = ref<string>(getToken() || '')
  const userInfo = ref<User | null>(null)
  const permissions = ref<string[]>([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const hasPermission = computed(() => (permission: string) => {
    return permissions.value.includes(permission)
  })

  // 操作方法
  const login = async (params: LoginParams): Promise<void> => {
    try {
      const response = await authApi.login(params)
      const { token: newToken, user } = response.data
      
      token.value = newToken
      userInfo.value = user
      setToken(newToken)
      
      // 获取用户权限
      await fetchUserPermissions()
    } catch (error) {
      throw new Error('登录失败')
    }
  }

  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      permissions.value = []
      removeToken()
    }
  }

  const fetchUserInfo = async (): Promise<void> => {
    if (!token.value) return
    
    try {
      const response = await authApi.getUserInfo()
      userInfo.value = response.data
    } catch (error) {
      console.error('Fetch user info error:', error)
      await logout()
    }
  }

  const fetchUserPermissions = async (): Promise<void> => {
    if (!token.value) return
    
    try {
      const response = await authApi.getUserPermissions()
      permissions.value = response.data
    } catch (error) {
      console.error('Fetch permissions error:', error)
    }
  }

  return {
    // 状态
    token,
    userInfo,
    permissions,
    // 计算属性
    isLoggedIn,
    hasPermission,
    // 方法
    login,
    logout,
    fetchUserInfo,
    fetchUserPermissions
  }
})
```

#### Store使用规范
```vue
<script setup lang="ts">
import { useAuthStore } from '@/stores/modules/auth'
import { useDocumentStore } from '@/stores/modules/document'

// 在组件中使用Store
const authStore = useAuthStore()
const documentStore = useDocumentStore()

// 访问状态
const isLoggedIn = authStore.isLoggedIn
const userInfo = authStore.userInfo

// 调用方法
const handleLogin = async (params: LoginParams) => {
  try {
    await authStore.login(params)
    // 登录成功后的逻辑
  } catch (error) {
    // 错误处理
  }
}
</script>
```

### API接口规范

#### 请求封装
```typescript
// api/request.ts
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/modules/auth'
import type { ApiResponse } from '@/api/types/common'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    
    // 添加认证token
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()
    
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<any>>) => {
    const { data } = response
    
    // 统一处理业务错误
    if (!data.success) {
      message.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
    
    return response
  },
  (error) => {
    const { response } = error
    
    // 处理HTTP错误
    if (response) {
      switch (response.status) {
        case 401:
          message.error('登录已过期，请重新登录')
          const authStore = useAuthStore()
          authStore.logout()
          break
        case 403:
          message.error('没有权限访问该资源')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(response.data?.message || '请求失败')
      }
    } else {
      message.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

// 生成请求ID
function generateRequestId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

export default request
```

#### API模块定义
```typescript
// api/modules/document.ts
import request from '@/api/request'
import type { 
  Document, 
  DocumentQuery, 
  CreateDocumentParams, 
  UpdateDocumentParams,
  DocumentListResponse 
} from '@/api/types/document'
import type { ApiResponse, PageResponse } from '@/api/types/common'

export const documentApi = {
  // 获取文档列表
  getDocumentList(params: DocumentQuery): Promise<ApiResponse<PageResponse<Document>>> {
    return request.get('/api/v1/documents', { params })
  },

  // 获取文档详情
  getDocumentById(id: string): Promise<ApiResponse<Document>> {
    return request.get(`/api/v1/documents/${id}`)
  },

  // 创建文档
  createDocument(data: CreateDocumentParams): Promise<ApiResponse<Document>> {
    return request.post('/api/v1/documents', data)
  },

  // 更新文档
  updateDocument(id: string, data: UpdateDocumentParams): Promise<ApiResponse<Document>> {
    return request.put(`/api/v1/documents/${id}`, data)
  },

  // 删除文档
  deleteDocument(id: string): Promise<ApiResponse<void>> {
    return request.delete(`/api/v1/documents/${id}`)
  },

  // 上传文档
  uploadDocument(file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<Document>> {
    const formData = new FormData()
    formData.append('file', file)
    
    return request.post('/api/v1/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      }
    })
  }
}
```

#### 类型定义
```typescript
// api/types/common.ts
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp: number
}

export interface PageResponse<T = any> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface PageRequest {
  page: number
  size: number
}

// api/types/document.ts
export interface Document {
  id: string
  name: string
  type: DocumentType
  status: DocumentStatus
  size: number
  url: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

export enum DocumentType {
  TERMS = 'TERMS',           // 条款
  ACTUARIAL = 'ACTUARIAL',   // 精算报告
  RATE = 'RATE'              // 费率表
}

export enum DocumentStatus {
  DRAFT = 'DRAFT',           // 草稿
  PENDING = 'PENDING',       // 待审核
  APPROVED = 'APPROVED',     // 已审核
  REJECTED = 'REJECTED'      // 已拒绝
}

export interface DocumentQuery extends PageRequest {
  keyword?: string
  type?: DocumentType
  status?: DocumentStatus
  createdBy?: string
  startDate?: string
  endDate?: string
}

export interface CreateDocumentParams {
  name: string
  type: DocumentType
  file: File
  description?: string
}

export interface UpdateDocumentParams {
  name?: string
  description?: string
  status?: DocumentStatus
}
```#
## 路由管理规范

#### 路由配置
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuards } from './guards'

// 导入路由模块
import authRoutes from './modules/auth'
import documentRoutes from './modules/document'
import auditRoutes from './modules/audit'
import systemRoutes from './modules/system'

// 基础路由
const baseRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue'),
    meta: {
      title: '工作台',
      requiresAuth: true,
      icon: 'dashboard'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: {
      title: '页面不存在'
    }
  }
]

// 合并所有路由
const routes: RouteRecordRaw[] = [
  ...baseRoutes,
  ...authRoutes,
  ...documentRoutes,
  ...auditRoutes,
  ...systemRoutes
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 设置路由守卫
setupRouterGuards(router)

export default router
```

#### 路由模块
```typescript
// router/modules/document.ts
import type { RouteRecordRaw } from 'vue-router'

const documentRoutes: RouteRecordRaw[] = [
  {
    path: '/document',
    name: 'DocumentLayout',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/document/list',
    meta: {
      title: '文档管理',
      requiresAuth: true,
      icon: 'file-text'
    },
    children: [
      {
        path: 'list',
        name: 'DocumentList',
        component: () => import('@/views/document/DocumentList.vue'),
        meta: {
          title: '文档列表',
          requiresAuth: true,
          permissions: ['document:view']
        }
      },
      {
        path: 'upload',
        name: 'DocumentUpload',
        component: () => import('@/views/document/DocumentUpload.vue'),
        meta: {
          title: '文档上传',
          requiresAuth: true,
          permissions: ['document:create']
        }
      },
      {
        path: 'detail/:id',
        name: 'DocumentDetail',
        component: () => import('@/views/document/DocumentDetail.vue'),
        meta: {
          title: '文档详情',
          requiresAuth: true,
          permissions: ['document:view']
        }
      }
    ]
  }
]

export default documentRoutes
```

#### 路由守卫
```typescript
// router/guards/auth.ts
import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { message } from 'ant-design-vue'

export function setupAuthGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
      if (!authStore.isLoggedIn) {
        message.warning('请先登录')
        next({
          name: 'Login',
          query: { redirect: to.fullPath }
        })
        return
      }
      
      // 检查用户信息
      if (!authStore.userInfo) {
        try {
          await authStore.fetchUserInfo()
        } catch (error) {
          next({ name: 'Login' })
          return
        }
      }
    }
    
    next()
  })
}

// router/guards/permission.ts
export function setupPermissionGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    
    // 检查页面权限
    if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
      const hasPermission = to.meta.permissions.some(permission => 
        authStore.hasPermission(permission)
      )
      
      if (!hasPermission) {
        message.error('没有权限访问该页面')
        next({ name: 'Dashboard' })
        return
      }
    }
    
    next()
  })
}

// router/guards/index.ts
import type { Router } from 'vue-router'
import { setupAuthGuard } from './auth'
import { setupPermissionGuard } from './permission'

export function setupRouterGuards(router: Router) {
  setupAuthGuard(router)
  setupPermissionGuard(router)
  
  // 设置页面标题
  router.afterEach((to) => {
    const title = to.meta.title as string
    if (title) {
      document.title = `${title} - 智能保险产品检核系统`
    }
  })
}
```

### 组合式函数规范

#### 通用Composables
```typescript
// composables/useTable.ts
import { ref, reactive, computed } from 'vue'
import type { TableProps } from 'ant-design-vue'

interface UseTableOptions<T = any> {
  fetchData: (params: any) => Promise<{ records: T[]; total: number }>
  defaultParams?: Record<string, any>
  immediate?: boolean
}

export function useTable<T = any>(options: UseTableOptions<T>) {
  const { fetchData, defaultParams = {}, immediate = true } = options
  
  const loading = ref(false)
  const dataSource = ref<T[]>([])
  const total = ref(0)
  
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条记录`
  })
  
  const queryParams = reactive({
    page: 1,
    size: 20,
    ...defaultParams
  })
  
  const fetchTableData = async (params?: Record<string, any>) => {
    try {
      loading.value = true
      
      const mergedParams = { ...queryParams, ...params }
      const response = await fetchData(mergedParams)
      
      dataSource.value = response.records
      total.value = response.total
      pagination.total = response.total
      pagination.current = mergedParams.page
      pagination.pageSize = mergedParams.size
    } catch (error) {
      console.error('Fetch table data error:', error)
    } finally {
      loading.value = false
    }
  }
  
  const handleTableChange: TableProps['onChange'] = (paginationInfo) => {
    if (paginationInfo) {
      queryParams.page = paginationInfo.current || 1
      queryParams.size = paginationInfo.pageSize || 20
      fetchTableData()
    }
  }
  
  const refresh = () => {
    fetchTableData()
  }
  
  const reset = () => {
    Object.assign(queryParams, { page: 1, size: 20, ...defaultParams })
    fetchTableData()
  }
  
  // 立即执行
  if (immediate) {
    fetchTableData()
  }
  
  return {
    loading,
    dataSource,
    pagination,
    queryParams,
    fetchTableData,
    handleTableChange,
    refresh,
    reset
  }
}

// composables/useForm.ts
import { ref, reactive } from 'vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'

interface UseFormOptions<T = any> {
  initialValues?: Partial<T>
  rules?: Record<string, Rule[]>
  onSubmit?: (values: T) => Promise<void> | void
}

export function useForm<T extends Record<string, any>>(options: UseFormOptions<T> = {}) {
  const { initialValues = {}, rules = {}, onSubmit } = options
  
  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const formData = reactive<T>({ ...initialValues } as T)
  
  const validate = async (): Promise<T> => {
    if (!formRef.value) {
      throw new Error('Form instance not found')
    }
    
    try {
      await formRef.value.validate()
      return formData
    } catch (error) {
      throw new Error('Form validation failed')
    }
  }
  
  const submit = async () => {
    if (!onSubmit) return
    
    try {
      loading.value = true
      const values = await validate()
      await onSubmit(values)
    } catch (error) {
      console.error('Form submit error:', error)
      throw error
    } finally {
      loading.value = false
    }
  }
  
  const reset = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialValues)
  }
  
  const clearValidate = () => {
    formRef.value?.clearValidate()
  }
  
  return {
    formRef,
    formData,
    loading,
    rules,
    validate,
    submit,
    reset,
    clearValidate
  }
}

// composables/useUpload.ts
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadFile, UploadProps } from 'ant-design-vue'

interface UseUploadOptions {
  accept?: string
  maxSize?: number // MB
  maxCount?: number
  onSuccess?: (file: UploadFile, response: any) => void
  onError?: (error: Error, file: UploadFile) => void
}

export function useUpload(options: UseUploadOptions = {}) {
  const {
    accept = '.pdf,.doc,.docx,.xls,.xlsx',
    maxSize = 10,
    maxCount = 1,
    onSuccess,
    onError
  } = options
  
  const fileList = ref<UploadFile[]>([])
  const uploading = ref(false)
  
  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    // 检查文件类型
    const isValidType = accept.split(',').some(type => 
      file.name.toLowerCase().endsWith(type.trim().replace('.', ''))
    )
    
    if (!isValidType) {
      message.error(`只支持 ${accept} 格式的文件`)
      return false
    }
    
    // 检查文件大小
    const isValidSize = file.size / 1024 / 1024 < maxSize
    if (!isValidSize) {
      message.error(`文件大小不能超过 ${maxSize}MB`)
      return false
    }
    
    // 检查文件数量
    if (fileList.value.length >= maxCount) {
      message.error(`最多只能上传 ${maxCount} 个文件`)
      return false
    }
    
    return true
  }
  
  const handleChange: UploadProps['onChange'] = (info) => {
    fileList.value = [...info.fileList]
    
    if (info.file.status === 'uploading') {
      uploading.value = true
    }
    
    if (info.file.status === 'done') {
      uploading.value = false
      message.success(`${info.file.name} 上传成功`)
      onSuccess?.(info.file, info.file.response)
    }
    
    if (info.file.status === 'error') {
      uploading.value = false
      message.error(`${info.file.name} 上传失败`)
      onError?.(new Error('Upload failed'), info.file)
    }
  }
  
  const removeFile = (file: UploadFile) => {
    const index = fileList.value.indexOf(file)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
  
  const clearFiles = () => {
    fileList.value = []
  }
  
  return {
    fileList,
    uploading,
    beforeUpload,
    handleChange,
    removeFile,
    clearFiles
  }
}
```#
# 样式规范

### SCSS/CSS规范

#### 样式组织结构
```scss
// assets/styles/index.scss - 样式入口文件
@import './variables.scss';
@import './mixins.scss';
@import './reset.scss';
@import './common.scss';
@import './components.scss';
@import './utilities.scss';

// assets/styles/variables.scss - 变量定义
:root {
  // 主色调 - 与UI设计规范保持一致
  --color-primary: #1890ff;
  --color-primary-hover: #40a9ff;
  --color-primary-active: #096dd9;
  
  // 功能色
  --color-success: #52c41a;
  --color-warning: #faad14;
  --color-error: #f5222d;
  --color-info: #1890ff;
  
  // 中性色
  --color-text-primary: #262626;
  --color-text-secondary: #595959;
  --color-text-disabled: #bfbfbf;
  --color-border: #d9d9d9;
  --color-background: #fafafa;
  
  // 间距
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  
  // 字体
  --font-size-xs: 10px;
  --font-size-sm: 12px;
  --font-size-base: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 20px;
  --font-size-xxl: 24px;
  
  // 圆角
  --border-radius-sm: 4px;
  --border-radius-base: 6px;
  --border-radius-lg: 8px;
  
  // 阴影
  --box-shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.03);
  --box-shadow-base: 0 2px 8px rgba(0, 0, 0, 0.06);
  --box-shadow-lg: 0 4px 12px rgba(0, 0, 0, 0.15);
}

// assets/styles/mixins.scss - 混入函数
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@mixin text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@mixin multi-line-ellipsis($lines: 2) {
  display: -webkit-box;
  -webkit-line-clamp: $lines;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

@mixin button-variant($bg-color, $text-color: #fff) {
  background-color: $bg-color;
  color: $text-color;
  border: 1px solid $bg-color;
  
  &:hover {
    background-color: lighten($bg-color, 10%);
    border-color: lighten($bg-color, 10%);
  }
  
  &:active {
    background-color: darken($bg-color, 5%);
    border-color: darken($bg-color, 5%);
  }
}

// 响应式断点
@mixin respond-to($breakpoint) {
  @if $breakpoint == xs {
    @media (max-width: 575px) { @content; }
  }
  @if $breakpoint == sm {
    @media (min-width: 576px) { @content; }
  }
  @if $breakpoint == md {
    @media (min-width: 768px) { @content; }
  }
  @if $breakpoint == lg {
    @media (min-width: 992px) { @content; }
  }
  @if $breakpoint == xl {
    @media (min-width: 1200px) { @content; }
  }
  @if $breakpoint == xxl {
    @media (min-width: 1600px) { @content; }
  }
}
```

#### 组件样式规范
```vue
<template>
  <div class="document-card">
    <div class="document-card__header">
      <h3 class="document-card__title">{{ document.name }}</h3>
      <div class="document-card__actions">
        <a-button size="small" @click="handleEdit">编辑</a-button>
        <a-button size="small" danger @click="handleDelete">删除</a-button>
      </div>
    </div>
    <div class="document-card__content">
      <p class="document-card__description">{{ document.description }}</p>
      <div class="document-card__meta">
        <span class="document-card__type">{{ document.type }}</span>
        <span class="document-card__date">{{ formatDate(document.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.document-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow-base);
  padding: var(--spacing-lg);
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: var(--box-shadow-lg);
    transform: translateY(-2px);
  }
  
  &__header {
    @include flex-between;
    margin-bottom: var(--spacing-md);
  }
  
  &__title {
    font-size: var(--font-size-lg);
    font-weight: 500;
    color: var(--color-text-primary);
    margin: 0;
    @include text-ellipsis;
  }
  
  &__actions {
    display: flex;
    gap: var(--spacing-sm);
  }
  
  &__content {
    .document-card__description {
      color: var(--color-text-secondary);
      margin-bottom: var(--spacing-md);
      @include multi-line-ellipsis(2);
    }
  }
  
  &__meta {
    @include flex-between;
    font-size: var(--font-size-sm);
    color: var(--color-text-disabled);
  }
  
  &__type {
    padding: 2px 8px;
    background: var(--color-primary);
    color: #fff;
    border-radius: var(--border-radius-sm);
    font-size: var(--font-size-xs);
  }
  
  // 响应式适配
  @include respond-to(xs) {
    padding: var(--spacing-md);
    
    .document-card__header {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--spacing-sm);
    }
    
    .document-card__actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
```

### CSS类命名规范

#### BEM命名方法
```scss
// 块（Block）- 组件名
.document-list { }

// 元素（Element）- 组件内的元素
.document-list__header { }
.document-list__content { }
.document-list__item { }

// 修饰符（Modifier）- 状态或变体
.document-list--loading { }
.document-list__item--active { }
.document-list__item--disabled { }

// 示例
.search-form {
  &__input {
    width: 100%;
    
    &--error {
      border-color: var(--color-error);
    }
  }
  
  &__button {
    margin-left: var(--spacing-sm);
    
    &--primary {
      @include button-variant(var(--color-primary));
    }
  }
  
  &--compact {
    .search-form__input {
      height: 32px;
    }
  }
}
```

## 工程化配置

### Vite配置
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { visualizer } from 'rollup-plugin-visualizer'

export default defineConfig({
  plugins: [
    vue(),
    // SVG图标插件
    createSvgIconsPlugin({
      iconDirs: [resolve(process.cwd(), 'src/assets/icons')],
      symbolId: 'icon-[dir]-[name]'
    }),
    // 打包分析插件
    visualizer({
      filename: 'dist/stats.html',
      open: true,
      gzipSize: true
    })
  ],
  
  // 路径别名
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@views': resolve(__dirname, 'src/views'),
      '@utils': resolve(__dirname, 'src/utils'),
      '@api': resolve(__dirname, 'src/api'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@assets': resolve(__dirname, 'src/assets')
    }
  },
  
  // 开发服务器配置
  server: {
    host: '0.0.0.0',
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  
  // 构建配置
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          antd: ['ant-design-vue'],
          utils: ['axios', 'dayjs', 'lodash-es']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  },
  
  // CSS配置
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `
          @import "@/assets/styles/variables.scss";
          @import "@/assets/styles/mixins.scss";
        `
      }
    }
  },
  
  // 环境变量
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version)
  }
})
```

### TypeScript配置
```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    
    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    
    /* Linting */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    
    /* Path mapping */
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"],
      "@components/*": ["src/components/*"],
      "@views/*": ["src/views/*"],
      "@utils/*": ["src/utils/*"],
      "@api/*": ["src/api/*"],
      "@stores/*": ["src/stores/*"],
      "@assets/*": ["src/assets/*"]
    },
    
    /* Vue specific */
    "types": ["vite/client", "node"]
  },
  "include": [
    "src/**/*.ts",
    "src/**/*.d.ts",
    "src/**/*.tsx",
    "src/**/*.vue"
  ],
  "exclude": [
    "node_modules",
    "dist"
  ],
  "references": [
    {
      "path": "./tsconfig.node.json"
    }
  ]
}
```

### ESLint配置
```javascript
// .eslintrc.js
module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    '@typescript-eslint/recommended',
    'plugin:vue/vue3-essential',
    'plugin:vue/vue3-strongly-recommended',
    'plugin:vue/vue3-recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-prettier'
  ],
  parser: 'vue-eslint-parser',
  parserOptions: {
    ecmaVersion: 'latest',
    parser: '@typescript-eslint/parser',
    sourceType: 'module'
  },
  plugins: [
    '@typescript-eslint',
    'vue'
  ],
  rules: {
    // Vue规则
    'vue/multi-word-component-names': 'off',
    'vue/no-v-html': 'off',
    'vue/require-default-prop': 'off',
    'vue/require-explicit-emits': 'error',
    'vue/component-definition-name-casing': ['error', 'PascalCase'],
    'vue/component-name-in-template-casing': ['error', 'PascalCase'],
    'vue/custom-event-name-casing': ['error', 'camelCase'],
    'vue/define-macros-order': ['error', {
      order: ['defineOptions', 'defineProps', 'defineEmits', 'defineSlots']
    }],
    'vue/no-empty-component-block': 'error',
    'vue/prefer-separate-static-class': 'error',
    
    // TypeScript规则
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    '@typescript-eslint/no-explicit-any': 'warn',
    '@typescript-eslint/explicit-function-return-type': 'off',
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    '@typescript-eslint/no-non-null-assertion': 'warn',
    
    // 通用规则
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'prefer-const': 'error',
    'no-var': 'error',
    'object-shorthand': 'error',
    'prefer-template': 'error'
  },
  overrides: [
    {
      files: ['*.vue'],
      rules: {
        '@typescript-eslint/no-unused-vars': 'off'
      }
    }
  ]
}
```

### Prettier配置
```json
// .prettierrc
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none",
  "printWidth": 100,
  "bracketSpacing": true,
  "bracketSameLine": false,
  "arrowParens": "avoid",
  "vueIndentScriptAndStyle": false,
  "endOfLine": "lf"
}
```## 测
试规范

### 单元测试
```typescript
// tests/unit/components/DocumentCard.test.ts
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import DocumentCard from '@/components/business/DocumentCard.vue'
import type { Document } from '@/api/types/document'

const mockDocument: Document = {
  id: '1',
  name: '测试文档',
  type: 'TERMS',
  status: 'APPROVED',
  size: 1024,
  url: 'http://example.com/doc.pdf',
  createdBy: 'user1',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z'
}

describe('DocumentCard', () => {
  const createWrapper = (props = {}) => {
    return mount(DocumentCard, {
      props: {
        document: mockDocument,
        ...props
      },
      global: {
        plugins: [createPinia()]
      }
    })
  }

  it('renders document information correctly', () => {
    const wrapper = createWrapper()
    
    expect(wrapper.find('.document-card__title').text()).toBe('测试文档')
    expect(wrapper.find('.document-card__type').text()).toBe('TERMS')
  })

  it('emits edit event when edit button is clicked', async () => {
    const wrapper = createWrapper()
    
    await wrapper.find('[data-testid="edit-button"]').trigger('click')
    
    expect(wrapper.emitted('edit')).toBeTruthy()
    expect(wrapper.emitted('edit')?.[0]).toEqual([mockDocument])
  })

  it('emits delete event when delete button is clicked', async () => {
    const wrapper = createWrapper()
    
    await wrapper.find('[data-testid="delete-button"]').trigger('click')
    
    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')?.[0]).toEqual([mockDocument.id])
  })

  it('shows readonly state when readonly prop is true', () => {
    const wrapper = createWrapper({ readonly: true })
    
    expect(wrapper.find('[data-testid="edit-button"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="delete-button"]').exists()).toBe(false)
  })
})

// tests/unit/composables/useTable.test.ts
import { describe, it, expect, vi } from 'vitest'
import { useTable } from '@/composables/useTable'

describe('useTable', () => {
  it('should initialize with default values', () => {
    const mockFetchData = vi.fn().mockResolvedValue({
      records: [],
      total: 0
    })

    const { loading, dataSource, pagination } = useTable({
      fetchData: mockFetchData,
      immediate: false
    })

    expect(loading.value).toBe(false)
    expect(dataSource.value).toEqual([])
    expect(pagination.current).toBe(1)
    expect(pagination.pageSize).toBe(20)
  })

  it('should fetch data on initialization when immediate is true', async () => {
    const mockData = {
      records: [{ id: 1, name: 'test' }],
      total: 1
    }
    const mockFetchData = vi.fn().mockResolvedValue(mockData)

    const { dataSource, pagination } = useTable({
      fetchData: mockFetchData,
      immediate: true
    })

    // 等待异步操作完成
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(mockFetchData).toHaveBeenCalledWith({
      page: 1,
      size: 20
    })
    expect(dataSource.value).toEqual(mockData.records)
    expect(pagination.total).toBe(mockData.total)
  })
})
```

### E2E测试
```typescript
// tests/e2e/document.spec.ts
import { test, expect } from '@playwright/test'

test.describe('Document Management', () => {
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login')
    await page.fill('[data-testid="username-input"]', 'admin')
    await page.fill('[data-testid="password-input"]', 'password')
    await page.click('[data-testid="login-button"]')
    
    // 等待跳转到工作台
    await expect(page).toHaveURL('/dashboard')
  })

  test('should display document list', async ({ page }) => {
    await page.goto('/document/list')
    
    // 检查页面标题
    await expect(page.locator('h1')).toContainText('文档列表')
    
    // 检查表格是否存在
    await expect(page.locator('.ant-table')).toBeVisible()
    
    // 检查搜索框
    await expect(page.locator('[placeholder="请输入文档名称"]')).toBeVisible()
  })

  test('should upload document successfully', async ({ page }) => {
    await page.goto('/document/upload')
    
    // 上传文件
    const fileInput = page.locator('input[type="file"]')
    await fileInput.setInputFiles('tests/fixtures/test-document.pdf')
    
    // 填写文档信息
    await page.fill('[data-testid="document-name"]', '测试文档')
    await page.selectOption('[data-testid="document-type"]', 'TERMS')
    
    // 提交表单
    await page.click('[data-testid="submit-button"]')
    
    // 检查成功消息
    await expect(page.locator('.ant-message-success')).toBeVisible()
    
    // 检查是否跳转到文档列表
    await expect(page).toHaveURL('/document/list')
  })

  test('should search documents', async ({ page }) => {
    await page.goto('/document/list')
    
    // 输入搜索关键词
    await page.fill('[placeholder="请输入文档名称"]', '测试')
    await page.press('[placeholder="请输入文档名称"]', 'Enter')
    
    // 等待搜索结果
    await page.waitForLoadState('networkidle')
    
    // 检查搜索结果
    const rows = page.locator('.ant-table-tbody tr')
    await expect(rows.first()).toBeVisible()
  })
})
```

### 测试配置
```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['tests/setup.ts']
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})

// tests/setup.ts
import { config } from '@vue/test-utils'
import { createPinia } from 'pinia'

// 全局配置
config.global.plugins = [createPinia()]

// Mock全局对象
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn()
  }))
})
```

## 性能优化

### 代码分割
```typescript
// 路由懒加载
const routes = [
  {
    path: '/document',
    component: () => import('@/views/document/DocumentList.vue')
  },
  {
    path: '/audit',
    component: () => import('@/views/audit/AuditTask.vue')
  }
]

// 组件懒加载
import { defineAsyncComponent } from 'vue'

const AsyncComponent = defineAsyncComponent({
  loader: () => import('@/components/HeavyComponent.vue'),
  loadingComponent: () => import('@/components/Loading.vue'),
  errorComponent: () => import('@/components/Error.vue'),
  delay: 200,
  timeout: 3000
})
```

### 缓存策略
```typescript
// utils/cache.ts
interface CacheItem<T> {
  data: T
  timestamp: number
  ttl: number
}

class Cache {
  private storage = new Map<string, CacheItem<any>>()

  set<T>(key: string, data: T, ttl = 5 * 60 * 1000): void {
    this.storage.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  get<T>(key: string): T | null {
    const item = this.storage.get(key)
    
    if (!item) return null
    
    if (Date.now() - item.timestamp > item.ttl) {
      this.storage.delete(key)
      return null
    }
    
    return item.data
  }

  clear(): void {
    this.storage.clear()
  }
}

export const cache = new Cache()

// API缓存装饰器
export function withCache<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  getCacheKey: (...args: Parameters<T>) => string,
  ttl = 5 * 60 * 1000
): T {
  return (async (...args: Parameters<T>) => {
    const cacheKey = getCacheKey(...args)
    const cached = cache.get(cacheKey)
    
    if (cached) {
      return cached
    }
    
    const result = await fn(...args)
    cache.set(cacheKey, result, ttl)
    
    return result
  }) as T
}
```

### 虚拟滚动
```vue
<!-- components/VirtualList.vue -->
<template>
  <div class="virtual-list" @scroll="handleScroll" ref="containerRef">
    <div class="virtual-list__phantom" :style="{ height: totalHeight + 'px' }"></div>
    <div class="virtual-list__content" :style="{ transform: `translateY(${offset}px)` }">
      <div
        v-for="item in visibleItems"
        :key="item.id"
        class="virtual-list__item"
        :style="{ height: itemHeight + 'px' }"
      >
        <slot :item="item" :index="item.index"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Props {
  items: any[]
  itemHeight: number
  containerHeight: number
}

const props = defineProps<Props>()

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

const totalHeight = computed(() => props.items.length * props.itemHeight)
const visibleCount = computed(() => Math.ceil(props.containerHeight / props.itemHeight) + 2)
const startIndex = computed(() => Math.floor(scrollTop.value / props.itemHeight))
const endIndex = computed(() => Math.min(startIndex.value + visibleCount.value, props.items.length))
const offset = computed(() => startIndex.value * props.itemHeight)

const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value).map((item, index) => ({
    ...item,
    index: startIndex.value + index
  }))
})

const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
}

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.style.height = props.containerHeight + 'px'
  }
})
</script>

<style lang="scss" scoped>
.virtual-list {
  position: relative;
  overflow-y: auto;
  
  &__phantom {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: -1;
  }
  
  &__content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
  }
  
  &__item {
    border-bottom: 1px solid var(--color-border);
  }
}
</style>
```

## 安全规范

### XSS防护
```typescript
// utils/security.ts
export function sanitizeHtml(html: string): string {
  const div = document.createElement('div')
  div.textContent = html
  return div.innerHTML
}

export function escapeHtml(text: string): string {
  const div = document.createElement('div')
  div.appendChild(document.createTextNode(text))
  return div.innerHTML
}

// 在模板中使用
// 避免使用 v-html，如必须使用则先进行清理
<div v-html="sanitizeHtml(userContent)"></div>
```

### CSRF防护
```typescript
// utils/csrf.ts
export function getCSRFToken(): string {
  const meta = document.querySelector('meta[name="csrf-token"]')
  return meta?.getAttribute('content') || ''
}

// 在请求中添加CSRF token
request.interceptors.request.use(config => {
  const token = getCSRFToken()
  if (token) {
    config.headers['X-CSRF-Token'] = token
  }
  return config
})
```

### 敏感信息处理
```typescript
// utils/sensitive.ts
export function maskPhone(phone: string): string {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

export function maskEmail(email: string): string {
  const [username, domain] = email.split('@')
  const maskedUsername = username.length > 2 
    ? username.slice(0, 2) + '*'.repeat(username.length - 2)
    : username
  return `${maskedUsername}@${domain}`
}

export function maskIdCard(idCard: string): string {
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
}
```##
 部署规范

### Docker配置
```dockerfile
# Dockerfile
# 多阶段构建
FROM node:18-alpine AS builder

WORKDIR /app

# 复制package文件
COPY package*.json pnpm-lock.yaml ./

# 安装pnpm
RUN npm install -g pnpm

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制源代码
COPY . .

# 构建应用
RUN pnpm build

# 生产环境镜像
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
```

### Nginx配置
```nginx
# nginx.conf
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # 日志格式
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
    
    access_log /var/log/nginx/access.log main;
    
    # 基础配置
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    
    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;
    
    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;
        
        # 安全头
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        add_header Referrer-Policy "strict-origin-when-cross-origin" always;
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
        
        # HTML文件不缓存
        location ~* \.html$ {
            expires -1;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
        
        # API代理
        location /api/ {
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
        
        # SPA路由支持
        location / {
            try_files $uri $uri/ /index.html;
        }
        
        # 健康检查
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }
}
```

### CI/CD配置
```yaml
# .github/workflows/deploy.yml
name: Deploy Frontend

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'pnpm'
      
      - name: Install pnpm
        run: npm install -g pnpm
      
      - name: Install dependencies
        run: pnpm install --frozen-lockfile
      
      - name: Run linting
        run: pnpm lint
      
      - name: Run type checking
        run: pnpm type-check
      
      - name: Run unit tests
        run: pnpm test:unit
      
      - name: Run E2E tests
        run: pnpm test:e2e
      
      - name: Build application
        run: pnpm build
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Login to Docker Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ secrets.DOCKER_REGISTRY }}
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.DOCKER_REGISTRY }}/frontend:latest
            ${{ secrets.DOCKER_REGISTRY }}/frontend:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - name: Deploy to production
        uses: appleboy/ssh-action@v0.1.5
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ${{ secrets.PROD_USER }}
          key: ${{ secrets.PROD_SSH_KEY }}
          script: |
            docker pull ${{ secrets.DOCKER_REGISTRY }}/frontend:latest
            docker stop frontend || true
            docker rm frontend || true
            docker run -d \
              --name frontend \
              --restart unless-stopped \
              -p 80:80 \
              ${{ secrets.DOCKER_REGISTRY }}/frontend:latest
```

## 监控与错误处理

### 错误监控
```typescript
// plugins/sentry.ts
import * as Sentry from '@sentry/vue'
import type { App } from 'vue'
import router from '@/router'

export function setupSentry(app: App) {
  Sentry.init({
    app,
    dsn: import.meta.env.VITE_SENTRY_DSN,
    environment: import.meta.env.MODE,
    integrations: [
      new Sentry.BrowserTracing({
        routingInstrumentation: Sentry.vueRouterInstrumentation(router)
      })
    ],
    tracesSampleRate: 0.1,
    beforeSend(event) {
      // 过滤敏感信息
      if (event.request?.url?.includes('/api/auth')) {
        delete event.request.data
      }
      return event
    }
  })
}

// 全局错误处理
app.config.errorHandler = (error, instance, info) => {
  console.error('Global error:', error, info)
  Sentry.captureException(error, {
    contexts: {
      vue: {
        componentName: instance?.$options.name,
        propsData: instance?.$props,
        info
      }
    }
  })
}
```

### 性能监控
```typescript
// utils/performance.ts
export class PerformanceMonitor {
  private static instance: PerformanceMonitor
  
  static getInstance(): PerformanceMonitor {
    if (!this.instance) {
      this.instance = new PerformanceMonitor()
    }
    return this.instance
  }
  
  // 监控页面加载性能
  measurePageLoad(): void {
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
      
      const metrics = {
        dns: navigation.domainLookupEnd - navigation.domainLookupStart,
        tcp: navigation.connectEnd - navigation.connectStart,
        request: navigation.responseStart - navigation.requestStart,
        response: navigation.responseEnd - navigation.responseStart,
        dom: navigation.domContentLoadedEventEnd - navigation.responseEnd,
        load: navigation.loadEventEnd - navigation.loadEventStart,
        total: navigation.loadEventEnd - navigation.navigationStart
      }
      
      console.log('Page Load Metrics:', metrics)
      
      // 发送到监控服务
      this.sendMetrics('page_load', metrics)
    })
  }
  
  // 监控API请求性能
  measureApiRequest(url: string, startTime: number, endTime: number): void {
    const duration = endTime - startTime
    
    this.sendMetrics('api_request', {
      url,
      duration,
      timestamp: Date.now()
    })
  }
  
  // 监控组件渲染性能
  measureComponentRender(componentName: string, renderTime: number): void {
    this.sendMetrics('component_render', {
      component: componentName,
      renderTime,
      timestamp: Date.now()
    })
  }
  
  private sendMetrics(type: string, data: any): void {
    // 发送到监控服务
    if (navigator.sendBeacon) {
      navigator.sendBeacon('/api/metrics', JSON.stringify({ type, data }))
    }
  }
}

// 在main.ts中初始化
const performanceMonitor = PerformanceMonitor.getInstance()
performanceMonitor.measurePageLoad()
```

## 最佳实践总结

### 开发流程
1. **需求分析**: 仔细分析UI设计稿和交互需求
2. **技术选型**: 基于项目需求选择合适的技术栈
3. **架构设计**: 设计合理的目录结构和模块划分
4. **编码实现**: 遵循编码规范，注重代码质量
5. **测试验证**: 编写单元测试和E2E测试
6. **性能优化**: 关注首屏加载和运行时性能
7. **部署上线**: 使用CI/CD自动化部署

### 代码质量
- 使用TypeScript提供类型安全
- 遵循ESLint和Prettier规范
- 编写有意义的测试用例
- 进行代码审查(Code Review)
- 使用SonarQube进行代码质量检查

### 性能优化
- 合理使用代码分割和懒加载
- 实现虚拟滚动处理大量数据
- 使用缓存策略减少重复请求
- 优化图片和静态资源
- 监控和分析性能指标

### 安全防护
- 防范XSS和CSRF攻击
- 敏感信息脱敏处理
- 使用HTTPS传输
- 实现访问控制和权限验证
- 定期进行安全审计

### 用户体验
- 遵循UI设计规范
- 提供友好的错误提示
- 实现响应式设计
- 支持无障碍访问
- 优化加载和交互体验

---

本前端开发规范与后端开发规范保持一致，确保前后端协调发展，共同构建高质量的智能保险产品检核系统。所有开发人员应严格遵循本规范，确保代码质量和项目的可维护性。