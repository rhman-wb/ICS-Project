# 智能检核模块文档

## 目录
- [模块概述](#模块概述)
- [功能特性](#功能特性)
- [架构设计](#架构设计)
- [使用指南](#使用指南)
- [API文档](#api文档)
- [组件说明](#组件说明)
- [测试指南](#测试指南)
- [部署指南](#部署指南)
- [故障排除](#故障排除)

## 模块概述

智能检核模块是保险产品智能检核系统的核心功能模块，提供全面的产品文档检核能力。该模块支持多种文档格式的智能分析，基于预定义规则进行自动化检核，并生成详细的检核报告。

### 主要功能
- 📋 **规则选择**: 灵活的检核规则配置和选择
- ⚡ **实时监控**: 检核过程的实时进度监控
- 📊 **结果展示**: 详细的检核结果分析和可视化
- 📈 **历史管理**: 完整的检核历史记录管理
- 📤 **报告导出**: 多格式检核报告导出功能

### 技术栈
- **前端**: Vue 3 + TypeScript + Ant Design Vue
- **状态管理**: Pinia
- **路由**: Vue Router
- **测试**: Vitest + Playwright
- **构建工具**: Vite

## 功能特性

### 1. 检核规则选择
- 支持按类别、严重程度、关键词筛选规则
- 提供规则详细信息展示
- 支持批量规则选择和全选功能
- 显示已选择产品和规则的统计信息

### 2. 检核进度监控
- 实时显示检核进度和状态
- 提供详细的文件处理统计信息
- 实时日志显示，支持不同级别的日志展示
- 支持检核过程的暂停、恢复、停止操作

### 3. 检核结果展示
- 全面的统计信息展示（通过率、失败率等）
- 按文档类型和严重程度的分类统计
- 支持结果筛选和搜索
- 详细的问题描述和修复建议
- 多格式报告导出（Excel、PDF）

### 4. 历史记录管理
- 完整的检核历史记录
- 支持按产品、状态、时间范围筛选
- 历史报告下载功能
- 历史记录的删除和管理

## 架构设计

### 组件架构
```
audit/
├── components/
│   ├── AuditRuleSelection.vue      # 规则选择组件
│   ├── AuditProcessMonitor.vue     # 进度监控组件
│   ├── AuditResultDisplay.vue      # 结果展示组件
│   └── AuditHistoryManager.vue     # 历史管理组件
├── views/
│   ├── RuleSelection.vue           # 规则选择页面
│   ├── ProcessMonitor.vue          # 进度监控页面
│   ├── ResultDisplay.vue           # 结果展示页面
│   └── HistoryManager.vue          # 历史管理页面
├── composables/
│   ├── useAuditRules.ts           # 规则管理逻辑
│   ├── useAuditProgress.ts        # 进度监控逻辑
│   └── useAuditResults.ts         # 结果处理逻辑
└── api/
    └── modules/
        └── audit.ts               # API接口定义
```

### 数据流设计
1. **规则选择阶段**: 用户选择产品和检核规则
2. **检核执行阶段**: 后端执行检核任务，前端监控进度
3. **结果展示阶段**: 展示检核结果和统计信息
4. **历史管理阶段**: 管理和查看历史检核记录

### 状态管理
使用Pinia进行状态管理，主要包括：
- 当前检核任务状态
- 选择的产品和规则
- 检核进度和日志
- 检核结果数据

## 使用指南

### 快速开始

#### 1. 从产品管理页面启动检核
```javascript
// 在产品管理页面选择产品后
const selectedProducts = ['product-1', 'product-2']

// 点击批量检核按钮，自动跳转到规则选择页面
router.push({
  path: '/audit/rule-selection',
  query: {
    productIds: selectedProducts.join(','),
    fromPage: 'product-management'
  }
})
```

#### 2. 选择检核规则
```vue
<template>
  <AuditRuleSelection
    :productIds="productIds"
    @rule-selected="handleRuleSelected"
  />
</template>

<script setup>
const handleRuleSelected = ({ productIds, ruleIds }) => {
  // 跳转到进度监控页面
  router.push({
    path: '/audit/process-monitor',
    query: {
      productIds: productIds.join(','),
      ruleIds: ruleIds.join(',')
    }
  })
}
</script>
```

#### 3. 监控检核进度
```vue
<template>
  <AuditProcessMonitor
    :taskId="taskId"
    @pause="handlePause"
    @stop="handleStop"
    @completed="handleCompleted"
  />
</template>

<script setup>
const handleCompleted = (taskId) => {
  // 检核完成，跳转到结果页面
  router.push(`/audit/results?taskId=${taskId}`)
}
</script>
```

#### 4. 查看检核结果
```vue
<template>
  <AuditResultDisplay
    :taskId="taskId"
    :showExport="true"
    @export-results="handleExport"
    @view-detail="handleViewDetail"
  />
</template>

<script setup>
const handleExport = (format) => {
  // 导出检核结果
  exportAuditResults(taskId, format)
}
</script>
```

### 高级用法

#### 自定义检核规则
```javascript
// 使用 useAuditRules 组合式函数
const {
  state,
  selectedRules,
  filterForm,
  loadRules,
  selectRule,
  applyFilters
} = useAuditRules()

// 加载规则
await loadRules()

// 应用筛选条件
filterForm.category = 'basic'
filterForm.severity = 'high'
applyFilters()

// 选择规则
selectRule('rule-001', true)
```

#### 实时进度监控
```javascript
// 使用 useAuditProgress 组合式函数
const {
  state,
  progressPercent,
  isCompleted,
  statusText,
  pauseAudit,
  resumeAudit
} = useAuditProgress({ taskId: 'task-123' })

// 监控进度变化
watch(() => state.progress, (newProgress) => {
  console.log(`当前进度: ${newProgress}%`)
})

// 暂停检核
await pauseAudit()

// 恢复检核
await resumeAudit()
```

#### 结果数据处理
```javascript
// 使用 useAuditResults 组合式函数
const {
  state,
  overallStatistics,
  documentTypeStats,
  severityStats,
  exportResults
} = useAuditResults({ taskId: 'task-123' })

// 导出Excel格式结果
await exportResults('excel')

// 导出PDF格式结果
await exportResults('pdf')
```

## API文档

### 检核规则API

#### GET /api/audit/rules
获取检核规则列表

**请求参数:**
```typescript
interface RuleQueryParams {
  category?: string          // 规则类别
  severity?: string         // 严重程度
  keyword?: string          // 关键词搜索
  page?: number            // 页码
  pageSize?: number        // 每页数量
}
```

**响应数据:**
```typescript
interface RuleListResponse {
  data: AuditRule[]
  total: number
  page: number
  pageSize: number
}

interface AuditRule {
  id: string
  name: string
  description: string
  category: string
  severity: 'high' | 'medium' | 'low'
  enabled: boolean
  tags: string[]
}
```

### 检核任务API

#### POST /api/audit/tasks
创建检核任务

**请求参数:**
```typescript
interface CreateTaskRequest {
  productIds: string[]     // 产品ID列表
  ruleIds: string[]       // 规则ID列表
  priority?: 'high' | 'medium' | 'low'
}
```

**响应数据:**
```typescript
interface CreateTaskResponse {
  taskId: string
  status: 'created' | 'queued' | 'running'
  estimatedDuration: number
}
```

#### GET /api/audit/tasks/:taskId/progress
获取检核进度

**响应数据:**
```typescript
interface ProgressResponse {
  taskId: string
  status: 'running' | 'paused' | 'completed' | 'failed'
  progress: number         // 0-100
  totalSteps: number
  currentStep: number
  startTime: string
  estimatedEndTime?: string
  logs: AuditLog[]
  statistics: TaskStatistics
}

interface AuditLog {
  id: string
  timestamp: string
  level: 'info' | 'success' | 'warning' | 'error'
  message: string
}

interface TaskStatistics {
  totalFiles: number
  processedFiles: number
  passedFiles: number
  failedFiles: number
  errors: number
  warnings: number
}
```

#### POST /api/audit/tasks/:taskId/control
控制检核任务

**请求参数:**
```typescript
interface TaskControlRequest {
  action: 'pause' | 'resume' | 'stop' | 'restart'
}
```

### 检核结果API

#### GET /api/audit/tasks/:taskId/results
获取检核结果

**请求参数:**
```typescript
interface ResultQueryParams {
  documentType?: string    // 文档类型
  severity?: string       // 严重程度
  status?: string         // 状态
  keyword?: string        // 关键词搜索
  page?: number          // 页码
  pageSize?: number      // 每页数量
}
```

**响应数据:**
```typescript
interface ResultListResponse {
  data: AuditResult[]
  total: number
  statistics: ResultStatistics
}

interface AuditResult {
  id: string
  documentType: string
  fileName: string
  ruleId: string
  ruleName: string
  severity: 'high' | 'medium' | 'low'
  status: 'passed' | 'failed' | 'warning'
  message: string
  details: string
  lineNumber?: number
  suggestion?: string
}

interface ResultStatistics {
  totalDocuments: number
  passedDocuments: number
  failedDocuments: number
  warningDocuments: number
  totalIssues: number
  highSeverityIssues: number
  mediumSeverityIssues: number
  lowSeverityIssues: number
}
```

#### POST /api/audit/tasks/:taskId/export
导出检核结果

**请求参数:**
```typescript
interface ExportRequest {
  format: 'excel' | 'pdf'
  filters?: ResultQueryParams
}
```

**响应**: 文件下载

### 历史记录API

#### GET /api/audit/history
获取检核历史

**请求参数:**
```typescript
interface HistoryQueryParams {
  productName?: string     // 产品名称
  status?: string         // 状态
  startDate?: string      // 开始日期
  endDate?: string        // 结束日期
  page?: number          // 页码
  pageSize?: number      // 每页数量
}
```

**响应数据:**
```typescript
interface HistoryListResponse {
  data: AuditHistory[]
  total: number
}

interface AuditHistory {
  id: string
  taskId: string
  productName: string
  productId: string
  ruleCount: number
  status: 'completed' | 'failed' | 'cancelled'
  startTime: string
  endTime?: string
  duration?: number
  totalIssues: number
  highSeverityIssues: number
  mediumSeverityIssues: number
  lowSeverityIssues: number
  operator: string
  reportUrl?: string
  errorMessage?: string
}
```

#### DELETE /api/audit/history/:id
删除历史记录

**响应数据:**
```typescript
interface DeleteResponse {
  success: boolean
  message: string
}
```

## 组件说明

### AuditRuleSelection 组件

检核规则选择组件，提供规则筛选、选择和确认功能。

**Props:**
```typescript
interface Props {
  productIds: string[]     // 选中的产品ID列表
}
```

**Events:**
```typescript
interface Events {
  'rule-selected': (data: {
    productIds: string[]
    ruleIds: string[]
  }) => void
}
```

**Slots:**
```vue
<template>
  <AuditRuleSelection :productIds="productIds">
    <!-- 自定义规则卡片头部 -->
    <template #rule-card-header="{ rule }">
      <CustomRuleHeader :rule="rule" />
    </template>

    <!-- 自定义操作按钮 -->
    <template #actions>
      <CustomActionButtons />
    </template>
  </AuditRuleSelection>
</template>
```

### AuditProcessMonitor 组件

检核进度监控组件，提供实时进度显示和任务控制功能。

**Props:**
```typescript
interface Props {
  taskId: string          // 检核任务ID
  autoRefresh?: boolean   // 是否自动刷新，默认 true
  refreshInterval?: number // 刷新间隔，默认 3000ms
}
```

**Events:**
```typescript
interface Events {
  'pause': (taskId: string) => void
  'resume': (taskId: string) => void
  'stop': (taskId: string) => void
  'restart': (taskId: string) => void
  'completed': (taskId: string) => void
  'failed': (taskId: string, error: string) => void
}
```

### AuditResultDisplay 组件

检核结果展示组件，提供结果查看、筛选和导出功能。

**Props:**
```typescript
interface Props {
  taskId: string          // 检核任务ID
  showExport?: boolean    // 是否显示导出功能，默认 true
  showFilters?: boolean   // 是否显示筛选功能，默认 true
}
```

**Events:**
```typescript
interface Events {
  'export-results': (format: 'excel' | 'pdf') => void
  'view-detail': (resultId: string) => void
  'filter-change': (filters: ResultQueryParams) => void
}
```

### AuditHistoryManager 组件

检核历史管理组件，提供历史记录查看、搜索和管理功能。

**Props:**
```typescript
interface Props {
  productId?: string      // 可选的产品ID筛选
  showActions?: boolean   // 是否显示操作按钮，默认 true
}
```

**Events:**
```typescript
interface Events {
  'view-history': (historyId: string) => void
  'download-report': (reportUrl: string) => void
  'delete-history': (historyId: string) => void
  'search': (filters: HistoryQueryParams) => void
}
```

## 测试指南

### 单元测试

所有组件都提供了完整的单元测试，位于 `components/audit/__tests__/` 目录。

**运行单元测试:**
```bash
# 运行所有单元测试
npm test

# 运行特定组件测试
npm test AuditRuleSelection

# 运行测试并生成覆盖率报告
npm run test:coverage
```

**测试覆盖范围:**
- 组件渲染测试
- 用户交互测试
- 事件触发测试
- 边界条件测试
- 错误处理测试

### E2E测试

E2E测试位于 `tests/e2e/audit.e2e.spec.ts`，覆盖完整的检核流程。

**运行E2E测试:**
```bash
# 运行所有E2E测试
npm run test:e2e

# 运行带UI的E2E测试
npm run test:e2e:ui

# 运行特定测试套件
npm run test:e2e -- --grep "检核功能端到端测试"
```

**测试场景:**
- 完整检核流程测试
- 产品选择和规则配置测试
- 进度监控和任务控制测试
- 结果查看和导出测试
- 历史记录管理测试
- 错误处理和边界情况测试
- 响应式设计测试

### 性能测试

**前端性能测试:**
```bash
# 运行性能测试
npm run test:performance

# 分析包大小
npm run analyze
```

**API性能测试:**
- 检核任务创建性能
- 进度查询响应时间
- 结果数据加载性能
- 导出功能性能

## 部署指南

### 前端部署

**构建生产版本:**
```bash
# 安装依赖
npm install

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

**环境配置:**
```javascript
// .env.production
VITE_API_BASE_URL=https://api.example.com
VITE_APP_TITLE=保险产品智能检核系统
VITE_AUDIT_EXPORT_MAX_SIZE=10000
```

### 后端部署

**配置文件:**
```yaml
# application.yml
audit:
  max-concurrent-tasks: 10
  task-timeout: 3600000  # 1小时
  result-retention-days: 30
  export:
    max-records: 10000
    formats: [excel, pdf]
```

### Docker部署

**Dockerfile:**
```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**docker-compose.yml:**
```yaml
version: '3.8'
services:
  audit-frontend:
    build: .
    ports:
      - "8080:80"
    environment:
      - VITE_API_BASE_URL=http://api:8081
    depends_on:
      - audit-backend

  audit-backend:
    image: audit-api:latest
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
```

## 故障排除

### 常见问题

#### 1. 检核任务无法启动
**症状**: 点击开始检核后任务一直处于创建状态

**可能原因**:
- 后端服务未启动或无法访问
- 产品ID或规则ID不存在
- 用户权限不足

**解决方案**:
```javascript
// 检查API连接状态
const checkApiHealth = async () => {
  try {
    const response = await fetch('/api/health')
    if (!response.ok) {
      throw new Error('API服务不可用')
    }
  } catch (error) {
    console.error('API连接失败:', error)
  }
}

// 验证产品和规则ID
const validateIds = async (productIds, ruleIds) => {
  const validationResult = await validateAuditParams({
    productIds,
    ruleIds
  })

  if (!validationResult.valid) {
    throw new Error(validationResult.message)
  }
}
```

#### 2. 进度监控页面无响应
**症状**: 进度条不更新，日志不刷新

**可能原因**:
- WebSocket连接断开
- 任务ID无效
- 浏览器性能问题

**解决方案**:
```javascript
// 重新建立连接
const reconnectProgress = () => {
  if (progressConnection) {
    progressConnection.close()
  }

  progressConnection = new WebSocket(`/ws/audit/progress/${taskId}`)
  progressConnection.onmessage = handleProgressUpdate
  progressConnection.onerror = handleConnectionError
}

// 降级到轮询模式
const fallbackToPolling = () => {
  progressInterval = setInterval(async () => {
    try {
      const progress = await getAuditProgress(taskId)
      updateProgress(progress)
    } catch (error) {
      console.error('获取进度失败:', error)
    }
  }, 5000)
}
```

#### 3. 结果导出失败
**症状**: 导出按钮无响应或下载文件损坏

**可能原因**:
- 结果数据量过大
- 服务器内存不足
- 网络超时

**解决方案**:
```javascript
// 分批导出大量数据
const exportLargeDataset = async (taskId, format) => {
  const totalCount = await getResultCount(taskId)
  const batchSize = 1000
  const batches = Math.ceil(totalCount / batchSize)

  for (let i = 0; i < batches; i++) {
    const batch = await exportAuditResults(taskId, format, {
      offset: i * batchSize,
      limit: batchSize
    })

    // 处理批次数据
    processBatch(batch, i)
  }
}

// 设置超时和重试
const exportWithRetry = async (taskId, format, retries = 3) => {
  for (let i = 0; i < retries; i++) {
    try {
      return await exportAuditResults(taskId, format, {
        timeout: 60000 // 60秒超时
      })
    } catch (error) {
      if (i === retries - 1) throw error
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)))
    }
  }
}
```

### 调试技巧

#### 1. 开启调试模式
```javascript
// 在浏览器控制台中启用调试
localStorage.setItem('audit-debug', 'true')

// 查看详细日志
console.log('当前检核状态:', auditStore.currentTask)
console.log('选择的规则:', auditStore.selectedRules)
```

#### 2. 性能分析
```javascript
// 监控组件渲染性能
import { performance } from 'perf_hooks'

const measureRender = (componentName) => {
  performance.mark(`${componentName}-start`)

  return () => {
    performance.mark(`${componentName}-end`)
    performance.measure(
      componentName,
      `${componentName}-start`,
      `${componentName}-end`
    )
  }
}
```

#### 3. 网络请求调试
```javascript
// API请求拦截器
axios.interceptors.request.use(config => {
  if (localStorage.getItem('audit-debug')) {
    console.log('API请求:', config)
  }
  return config
})

axios.interceptors.response.use(
  response => {
    if (localStorage.getItem('audit-debug')) {
      console.log('API响应:', response)
    }
    return response
  },
  error => {
    console.error('API错误:', error)
    return Promise.reject(error)
  }
)
```

### 监控和日志

#### 1. 错误监控
```javascript
// 全局错误处理
window.addEventListener('unhandledrejection', event => {
  console.error('未捕获的Promise错误:', event.reason)

  // 发送错误报告
  reportError({
    type: 'promise-rejection',
    message: event.reason.message,
    stack: event.reason.stack
  })
})

// Vue错误处理
app.config.errorHandler = (error, vm, info) => {
  console.error('Vue组件错误:', error, info)

  reportError({
    type: 'vue-error',
    message: error.message,
    stack: error.stack,
    component: vm?.$options.name,
    info
  })
}
```

#### 2. 性能监控
```javascript
// 页面加载性能
window.addEventListener('load', () => {
  const perfData = performance.getEntriesByType('navigation')[0]

  reportPerformance({
    loadTime: perfData.loadEventEnd - perfData.loadEventStart,
    domContentLoaded: perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart,
    firstPaint: performance.getEntriesByType('paint').find(p => p.name === 'first-paint')?.startTime
  })
})
```

## 更新日志

### v1.0.0 (2024-01-19)
- ✨ 新增检核规则选择功能
- ✨ 新增检核进度实时监控
- ✨ 新增检核结果展示和导出
- ✨ 新增检核历史记录管理
- 🧪 完整的单元测试和E2E测试覆盖
- 📚 完整的API文档和使用指南

---

如有问题或建议，请联系开发团队或提交Issue。