import request from '@/api'
import type { ApiResponse, PageResponse } from '@/api/types/common'

// 规则接口类型定义
export interface RuleQueryParams {
  page?: number
  size?: number
  ruleSource?: string
  applicableInsurance?: string
  manageDepartment?: string
  auditStatus?: string
  effectiveStatus?: string
  applicableChapter?: string
  businessArea?: string
  keyword?: string
  startTime?: string
  endTime?: string
  ruleType?: string
  followed?: boolean
  sortField?: string
  sortDirection?: string
}

export interface RuleInfo {
  id: string
  ruleNumber: string
  ruleName: string
  ruleDescription: string
  ruleType: string
  ruleSource: string
  manageDepartment: string
  applicableInsurance: string
  applicableRequirements: string
  applicableChapter: string
  businessArea: string
  auditStatus: string
  effectiveStatus: string
  ruleContent?: string
  ruleConfig?: string
  effectiveTime?: string
  expiryTime?: string
  lastUpdatedAt: string
  submittedBy?: string
  submittedAt?: string
  auditedBy?: string
  auditedAt?: string
  auditComments?: string
  followed: boolean
  priority: number
  sortOrder: number
  tags?: string
  remarks?: string
  createdBy: string
  createdAt: string
  updatedBy: string
  updatedAt: string
  version: number
  subRuleData?: any
}

export interface CreateRuleRequest {
  ruleName: string
  ruleDescription: string
  ruleType: string
  ruleSource: string
  manageDepartment?: string
  applicableInsurance?: string
  applicableRequirements?: string
  applicableChapter?: string
  businessArea?: string
  ruleContent?: string
  ruleConfig?: string
  effectiveTime?: string
  expiryTime?: string
  priority?: number
  sortOrder?: number
  tags?: string
  remarks?: string
  subRuleData?: any
}

export interface UpdateRuleRequest {
  ruleName?: string
  ruleDescription?: string
  ruleType?: string
  ruleSource?: string
  manageDepartment?: string
  applicableInsurance?: string
  applicableRequirements?: string
  applicableChapter?: string
  businessArea?: string
  auditStatus?: string
  effectiveStatus?: string
  ruleContent?: string
  ruleConfig?: string
  effectiveTime?: string
  expiryTime?: string
  auditComments?: string
  followed?: boolean
  priority?: number
  sortOrder?: number
  tags?: string
  remarks?: string
  subRuleData?: any
}

export interface BatchDeleteRequest {
  ids: string[]
}

export interface BatchOperationResponse {
  totalRequested: number
  successCount: number
  failureCount: number
  success: boolean
  successIds: string[]
  failureIds: string[]
  failureDetails?: Record<string, string>
  batchId?: string
  operationType?: string
  processingTimeMs?: number
  resultSummary?: string
}

export interface RuleStatistics {
  totalRules: number
  rulesByType: Record<string, number>
  rulesByStatus: Record<string, number>
  rulesBySource: Record<string, number>
  recentlyCreated: number
  recentlyUpdated: number
}

export interface BatchFollowRequest {
  ids: string[]
  followed: boolean
}

export interface BatchStatusUpdateRequest {
  ids: string[]
  auditStatus?: string
  effectiveStatus?: string
}

export interface ImportPreviewRequest {
  file: File
}

export interface ImportPreviewResponse {
  success: boolean
  totalRecords: number
  validRecords: number
  errorRecords: number
  previewData: RuleInfo[]
  errorDetails: Record<string, string>
  batchId?: string
}

export interface SubmitOARequest {
  ruleIds: string[]
  submittedBy: string
  priority: string
  expectedCompletionDate?: string
  comments: string
  ccUsers?: string[]
  notifyEmail?: boolean
}

// 规则API服务
export const rulesApi = {
  /**
   * 获取规则列表
   */
  getRuleList(params: RuleQueryParams): Promise<ApiResponse<PageResponse<RuleInfo>>> {
    return request({
      url: '/v1/rules',
      method: 'GET',
      params
    })
  },

  /**
   * 根据ID获取规则详情
   */
  getRuleById(id: string): Promise<ApiResponse<RuleInfo>> {
    return request({
      url: `/v1/rules/${id}`,
      method: 'GET'
    })
  },

  /**
   * 创建规则
   */
  createRule(data: CreateRuleRequest): Promise<ApiResponse<RuleInfo>> {
    return request({
      url: '/v1/rules',
      method: 'POST',
      data
    })
  },

  /**
   * 更新规则
   */
  updateRule(id: string, data: UpdateRuleRequest): Promise<ApiResponse<RuleInfo>> {
    return request({
      url: `/v1/rules/${id}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除规则
   */
  deleteRule(id: string): Promise<ApiResponse<void>> {
    return request({
      url: `/v1/rules/${id}`,
      method: 'DELETE'
    })
  },

  /**
   * 批量删除规则
   */
  batchDeleteRules(data: BatchDeleteRequest): Promise<ApiResponse<BatchOperationResponse>> {
    return request({
      url: '/v1/rules/batch',
      method: 'DELETE',
      data
    })
  },

  /**
   * 获取所有规则（无分页）
   */
  getAllRules(): Promise<ApiResponse<RuleInfo[]>> {
    return request({
      url: '/v1/rules/all',
      method: 'GET'
    })
  },

  /**
   * 搜索规则
   */
  searchRules(keyword: string): Promise<ApiResponse<RuleInfo[]>> {
    return request({
      url: '/v1/rules/search',
      method: 'GET',
      params: { keyword }
    })
  },

  /**
   * 关注/取消关注规则
   */
  toggleRuleFollow(id: string, followed: boolean): Promise<ApiResponse<RuleInfo>> {
    return request({
      url: `/v1/rules/${id}/follow`,
      method: 'POST',
      params: { followed }
    })
  },

  /**
   * 复制规则
   */
  copyRule(id: string): Promise<ApiResponse<RuleInfo>> {
    return request({
      url: `/v1/rules/${id}/copy`,
      method: 'POST'
    })
  },

  /**
   * 获取规则统计信息
   */
  getRuleStatistics(): Promise<ApiResponse<RuleStatistics>> {
    return request({
      url: '/v1/rules/statistics',
      method: 'GET'
    })
  },

  /**
   * 批量关注/取消关注规则
   */
  batchToggleFollow(data: BatchFollowRequest): Promise<ApiResponse<BatchOperationResponse>> {
    return request({
      url: '/v1/rules/batch/follow',
      method: 'POST',
      data
    })
  },

  /**
   * 批量更新状态
   */
  batchUpdateStatus(data: BatchStatusUpdateRequest): Promise<ApiResponse<BatchOperationResponse>> {
    return request({
      url: '/v1/rules/batch/status',
      method: 'PATCH',
      data
    })
  },

  /**
   * 批量提交OA审核
   */
  batchSubmitOA(data: SubmitOARequest): Promise<ApiResponse<BatchOperationResponse>> {
    return request({
      url: '/v1/rules/batch/submit-oa',
      method: 'POST',
      data
    })
  },

  /**
   * 导入预览
   */
  importPreview(file: File): Promise<ApiResponse<ImportPreviewResponse>> {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/v1/rules/import/preview',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 确认导入
   */
  confirmImport(importId: string): Promise<ApiResponse<BatchOperationResponse>> {
    return request({
      url: `/v1/rules/import/${importId}/confirm`,
      method: 'POST'
    })
  },

  /**
   * 导入规则
   */
  importRules(formData: FormData): Promise<ApiResponse<any>> {
    return request({
      url: '/v1/rules/import',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 下载导入模板
   */
  downloadImportTemplate(): Promise<any> {
    return request({
      url: '/v1/rules/template/download',
      method: 'GET',
      responseType: 'blob'
    })
  },

  /**
   * 下载导入模板
   */
  downloadTemplate(): Promise<Blob> {
    return request({
      url: '/v1/rules/template/download',
      method: 'GET',
      responseType: 'blob'
    })
  }
}