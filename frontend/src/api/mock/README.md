# Dashboard Mock Data Service

工作台Mock数据服务，为前端开发阶段提供模拟数据支持。

## 功能特性

### 1. 汇总数据服务 (getSummaryData)
- 支持不同时间维度的数据筛选（总计、今日、本周、本月）
- 包含产品数量、要件数量、规则数量、已检核错误数量
- 数据具有随机波动，模拟真实业务场景
- 模拟API延迟（200-500ms）

### 2. 应用概要服务 (getOverviewData)
- 提供累计统计和近期统计数据
- 动态计算增长率和检出率
- 格式化时间显示（HH:MM:SS）
- 模拟API延迟（100-300ms）

### 3. 关注任务服务 (getFocusTasks)
- 支持关键词搜索和类型筛选
- 支持时间范围筛选（本周、本月）
- 支持分页查询
- 包含丰富的任务类型和状态
- 模拟API延迟（150-450ms）

### 4. 近期动态服务 (getRecentActivities)
- 生成多种类型的操作动态
- 自动格式化相对时间显示
- 按时间倒序排列
- 支持数量限制和分页
- 模拟API延迟（100-350ms）

### 5. 统计图表服务 (getStatisticsData)
- 支持多种统计类型（产品类型、部门、规则类型）
- 动态计算百分比，确保总和为100%
- 提供颜色配置用于图表展示
- 数据具有随机波动
- 模拟API延迟（100-300ms）

## 使用示例

```typescript
import { DashboardMockService } from '@/api/mock/dashboardMock'

// 获取汇总数据
const summaryData = await DashboardMockService.getSummaryData('total')

// 获取应用概要
const overviewData = await DashboardMockService.getOverviewData()

// 获取关注任务（支持搜索筛选）
const focusTasks = await DashboardMockService.getFocusTasks({
  keyword: '财产',
  typeFilter: '产品检核',
  timeFilter: '本周',
  page: 1,
  size: 10
})

// 获取近期动态
const activities = await DashboardMockService.getRecentActivities(8)

// 获取统计图表数据
const statistics = await DashboardMockService.getStatisticsData('productType')

// 批量获取工作台数据
const batchData = await DashboardMockService.getBatchDashboardData('total')
```

## 工具方法

### 选项获取
```typescript
// 获取任务类型选项
const taskTypes = await DashboardMockService.getTaskTypeOptions()

// 获取时间筛选选项
const timeFilters = await DashboardMockService.getTimeFilterOptions()

// 获取统计类型选项
const statisticsTypes = await DashboardMockService.getStatisticsTypeOptions()
```

### 网络错误模拟
```typescript
// 模拟5%的网络错误率
const hasError = DashboardMockService.simulateNetworkError(0.05)
```

## 数据类型

所有数据类型定义位于 `@/api/types/dashboard.ts`：

- `SummaryData` - 汇总数据
- `OverviewData` - 应用概要数据
- `FocusTask` - 关注任务
- `FocusTaskParams` - 任务查询参数
- `FocusTaskResponse` - 任务查询响应
- `RecentActivity` - 近期动态
- `RecentActivitiesResponse` - 动态查询响应
- `StatisticsData` - 统计图表数据
- `ChartDataItem` - 图表数据项

## 特性说明

### 数据真实性
- 使用合理的随机数据生成
- 保持数据之间的逻辑一致性
- 模拟真实的业务场景和数据分布

### 性能优化
- 合理的API延迟模拟
- 支持批量数据获取
- 内存友好的数据生成

### 扩展性
- 易于添加新的数据类型
- 支持自定义筛选条件
- 模块化的服务设计

## 测试覆盖

完整的单元测试覆盖，包括：
- 数据结构验证
- 筛选功能测试
- 分页功能测试
- 时间格式化测试
- 错误处理测试

运行测试：
```bash
npm test -- src/test/api/dashboardMock.test.ts --run
```