/**
 * 检核模块TypeScript类型定义
 * 提供检核功能的类型安全支持
 */

// 基础类型导入
import type { ApiResponse, PageRequest, PageResponse } from './common'

// ==================== 检核任务相关类型 ====================

export interface AuditTask {
  id: string
  productIds: string[]
  ruleIds: string[]
  status: 'pending' | 'running' | 'completed' | 'failed'
  createdBy: string
  createdTime: Date
  completedTime?: Date
  totalProducts: number
  completedProducts: number
  totalErrors: number
  progressPercent: number
}

export interface CreateAuditTaskRequest {
  productIds: string[]
  ruleIds: string[]
}

export interface CreateAuditTaskResponse {
  taskId: string
}

// ==================== 检核规则相关类型 ====================

export interface AuditRule {
  id: string
  code: string // CC001-CC017
  name: string
  source: string
  applicableInsuranceType: string[]
  applicableChapter: string
  managementDepartment: string
  ruleType: 'single' | 'double' | 'format' | 'advanced'
  effectiveStatus: 'active' | 'inactive'
  auditStatus: 'approved' | 'pending' | 'rejected'
  effectiveStartTime: Date
  effectiveEndTime?: Date
  description: string
}

export interface AuditRuleQueryParams extends PageRequest {
  ruleSource?: string
  insuranceTypes?: string[]
  managementDepartment?: string
  applicableDocumentTypes?: string[]
  applicableChapter?: string
  effectiveStatus?: string
  auditStatus?: string
  startTime?: string
  endTime?: string
  keyword?: string
}

export interface RuleFilterOptions {
  ruleSources: string[]
  insuranceTypes: string[]
  managementDepartments: string[]
  applicableChapters: string[]
  effectiveStatuses: { label: string; value: string }[]
  auditStatuses: { label: string; value: string }[]
}

// ==================== 检核结果相关类型 ====================

export interface AuditResult {
  id: string
  taskId: string
  productId: string
  documentType: 'terms' | 'feasibility' | 'comparison'
  ruleId: string
  ruleCode: string
  suggestion: string
  applicableChapter: string
  ruleSource: string
  managementDepartment: string
  ruleType: string
  originalContent: string
  pageNumber: string
  highlightPosition?: HighlightPosition
  severity: 'high' | 'medium' | 'low'
  createdTime: Date
}

export interface HighlightPosition {
  start: number
  end: number
  page: number
}

export interface AuditResultQueryParams extends PageRequest {
  taskId: string
  documentType?: 'terms' | 'feasibility' | 'comparison'
  ruleType?: 'single' | 'double' | 'format' | 'advanced'
  severity?: 'high' | 'medium' | 'low'
}

export interface AuditResultsResponse extends PageResponse<AuditResult> {
  statistics: AuditStatistics[]
}

// ==================== 统计数据相关类型 ====================

export interface AuditStatistics {
  id: string
  taskId: string
  documentType: 'terms' | 'feasibility' | 'comparison'
  ruleTypeStats: {
    single: number
    double: number
    format: number
    advanced: number
  }
  totalCount: number
}

export interface ResultStatistics {
  totalResults: number
  byDocumentType: {
    terms: number
    feasibility: number
    comparison: number
  }
  byRuleType: {
    single: number
    double: number
    format: number
    advanced: number
  }
  bySeverity: {
    high: number
    medium: number
    low: number
  }
}

// ==================== 历史记录相关类型 ====================

export interface AuditHistory {
  id: string
  taskId: string
  productId: string
  productName: string
  executionTime: Date
  totalErrors: number
  status: 'completed' | 'failed'
  reportDownloadUrl?: string
}

export interface AuditHistoryQueryParams extends PageRequest {
  productId?: string
  startTime?: string
  endTime?: string
  status?: string
}

// ==================== 原文内容相关类型 ====================

export interface ResultContent {
  content: string
  highlightRanges: HighlightRange[]
}

export interface HighlightRange {
  start: number
  end: number
  ruleCode: string
  severity: string
}

// ==================== 导出相关类型 ====================

export interface ExportRequest {
  taskId: string
  format: 'excel' | 'pdf'
}

export interface ExportResponse {
  downloadUrl: string
  fileName: string
}

// ==================== 任务操作相关类型 ====================

export interface TaskOperationResponse {
  success: boolean
  message?: string
}

export interface RestartTaskResponse {
  taskId: string
}

// ==================== Composable相关类型 ====================

export interface UseAuditRulesOptions {
  autoLoad?: boolean
  defaultParams?: Partial<AuditRuleQueryParams>
}

export interface UseAuditProgressOptions {
  taskId: string
  interval?: number
  autoStart?: boolean
}

export interface UseAuditResultsOptions {
  taskId: string
  autoLoad?: boolean
  defaultParams?: Partial<AuditResultQueryParams>
}

export interface UseAuditHistoryOptions {
  productId?: string
  autoLoad?: boolean
  defaultParams?: Partial<AuditHistoryQueryParams>
}

// ==================== 状态相关类型 ====================

export interface AuditRulesState {
  rules: AuditRule[]
  selectedRules: string[]
  filterOptions: RuleFilterOptions | null
  queryParams: AuditRuleQueryParams
  loading: boolean
  total: number
  current: number
  size: number
}

export interface AuditProgressState {
  task: AuditTask | null
  loading: boolean
  error: string | null
  isRunning: boolean
}

export interface AuditResultsState {
  results: AuditResult[]
  statistics: AuditStatistics[]
  queryParams: AuditResultQueryParams
  loading: boolean
  total: number
  current: number
  size: number
}

export interface AuditHistoryState {
  records: AuditHistory[]
  queryParams: AuditHistoryQueryParams
  loading: boolean
  total: number
  current: number
  size: number
}

// ==================== 表单相关类型 ====================

export interface RuleSelectionForm {
  ruleSource: string
  insuranceTypes: string[]
  managementDepartment: string
  applicableChapter: string
  effectiveStatus: string
  auditStatus: string
  startTime: string
  endTime: string
  keyword: string
}

export interface RuleSelectionFormRules {
  [key: string]: Array<{
    required?: boolean
    message: string
    trigger?: string | string[]
  }>
}

// ==================== 事件相关类型 ====================

export interface AuditRuleSelectionEvents {
  'rule-selected': (ruleIds: string[]) => void
  'cancel': () => void
  'search': (params: AuditRuleQueryParams) => void
  'reset': () => void
}

export interface AuditProcessMonitorEvents {
  'process-complete': (task: AuditTask) => void
  'process-error': (error: string) => void
  'stop-task': (taskId: string) => void
  'restart-task': (taskId: string) => void
}

export interface AuditResultDisplayEvents {
  'export-results': (format: 'excel' | 'pdf') => void
  'back-to-list': () => void
  'view-detail': (resultId: string) => void
  'filter-change': (params: AuditResultQueryParams) => void
}

export interface AuditHistoryManagerEvents {
  'view-history': (historyId: string) => void
  'delete-record': (historyId: string) => void
  'download-report': (downloadUrl: string) => void
  'search': (params: AuditHistoryQueryParams) => void
}

// ==================== 组件Props类型 ====================

export interface AuditRuleSelectionProps {
  productIds: string[]
  visible?: boolean
  loading?: boolean
}

export interface AuditProcessMonitorProps {
  taskId: string
  autoRefresh?: boolean
  refreshInterval?: number
}

export interface AuditResultDisplayProps {
  taskId: string
  resultData?: AuditResult[]
  showExport?: boolean
}

export interface AuditHistoryManagerProps {
  productId?: string
  showActions?: boolean
}

// ==================== 文档类型映射 ====================

export const DocumentTypeMap = {
  terms: '条款',
  feasibility: '可行性报告',
  comparison: '前后修改对比表'
} as const

export const RuleTypeMap = {
  single: '单句规则',
  double: '双句规则',
  format: '格式规则',
  advanced: '高级规则'
} as const

export const SeverityMap = {
  high: '高',
  medium: '中',
  low: '低'
} as const

export const TaskStatusMap = {
  pending: '待执行',
  running: '执行中',
  completed: '已完成',
  failed: '执行失败'
} as const

export const HistoryStatusMap = {
  completed: '已完成',
  failed: '执行失败'
} as const

// ==================== 颜色配置类型 ====================

export interface SeverityColors {
  high: string
  medium: string
  low: string
}

export interface StatusColors {
  pending: string
  running: string
  completed: string
  failed: string
}

// ==================== API响应类型别名 ====================

export type AuditTaskResponse = ApiResponse<AuditTask>
export type AuditRulesResponse = ApiResponse<PageResponse<AuditRule>>
export type AuditResultResponse = ApiResponse<AuditResultsResponse>
export type AuditHistoryResponse = ApiResponse<PageResponse<AuditHistory>>
export type CreateTaskResponse = ApiResponse<CreateAuditTaskResponse>
export type ExportResultResponse = ApiResponse<ExportResponse>
export type TaskProgressResponse = ApiResponse<AuditTask>
export type ResultStatisticsResponse = ApiResponse<ResultStatistics>
export type ResultContentResponse = ApiResponse<ResultContent>