/**
 * 工作台相关数据类型定义
 */

// 时间筛选类型
export type TimeFilterType = 'total' | 'today' | 'week' | 'month'

// 汇总数据接口
export interface SummaryData {
  products: number
  documents: number
  rules: number
  errors: number
}

// 应用概要数据接口
export interface OverviewData {
  cumulative: {
    totalProducts: number
    totalRules: number
    errorDetectionRate: string
    avgAuditTime: string
  }
  recent: {
    products7Days: number
    products30Days: number
    rules7Days: number
    rules30Days: number
    productGrowthRate: string
    ruleGrowthRate: string
  }
}

// 关注任务接口
export interface FocusTask {
  id: string
  name: string
  submitter: string
  submitTime: string
  type: string
  status: string
  icon: string
}

// 关注任务查询参数
export interface FocusTaskParams {
  keyword?: string
  timeFilter?: string
  typeFilter?: string
  page?: number
  size?: number
}

// 关注任务响应
export interface FocusTaskResponse {
  records: FocusTask[]
  total: number
  current: number
  size: number
}

// 近期动态接口
export interface RecentActivity {
  id: string
  type: 'upload' | 'rule_update' | 'rule_approve' | 'rule_config'
  user: string
  content: string
  productName?: string
  productId?: string
  ruleCount?: number
  time: Date
  timestamp: number
  relativeTime?: string
}

// 近期动态响应
export interface RecentActivitiesResponse {
  activities: RecentActivity[]
  total: number
  hasMore: boolean
}

// 统计图表数据项
export interface ChartDataItem {
  name: string
  value: number
  percentage: string
  color: string
}

// 统计图表数据
export interface StatisticsData {
  chartData: ChartDataItem[]
  total: number
}

// 统计图表类型
export type StatisticsType = 'productType' | 'department' | 'ruleType'