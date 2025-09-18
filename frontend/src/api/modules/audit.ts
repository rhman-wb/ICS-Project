/**
 * 检核模块API接口
 * 集成Mock数据，预留真实API接口
 */

import request from '@/api'
import type { ApiResponse, PageResponse } from '@/api/types/common'
import type {
  AuditTask,
  AuditRule,
  AuditResult,
  AuditStatistics,
  AuditHistory
} from '@/api/types/audit'

// Mock服务导入
import { auditMockService } from '@/api/mock/audit/index'
import { auditRulesMockService } from '@/api/mock/audit/auditRules'
import { auditResultsMockService } from '@/api/mock/audit/auditResults'

// 环境变量控制是否使用Mock数据
const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// 请求加载状态管理
const loadingStates = new Map<string, boolean>()

// 获取加载状态
export const getLoadingState = (key: string): boolean => {
  return loadingStates.get(key) || false
}

// 设置加载状态
const setLoadingState = (key: string, loading: boolean): void => {
  loadingStates.set(key, loading)
}

// 包装API调用以管理加载状态
const withLoading = async <T>(
  key: string,
  apiCall: () => Promise<T>
): Promise<T> => {
  try {
    setLoadingState(key, true)
    const result = await apiCall()
    return result
  } finally {
    setLoadingState(key, false)
  }
}

export const auditApi = {

  // ==================== 检核任务相关 ====================

  /**
   * 创建检核任务
   */
  async createAuditTask(data: {
    productIds: string[]
    ruleIds: string[]
  }): Promise<ApiResponse<{ taskId: string }>> {
    return withLoading('createAuditTask', async () => {
      if (USE_MOCK) {
        const result = await auditMockService.createAuditTask(data.productIds, data.ruleIds)
        return {
          code: 200,
          message: '检核任务创建成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.post('/v1/audit/tasks', data)
        return response.data
      }
    })
  },

  /**
   * 获取检核任务进度
   */
  async getTaskProgress(taskId: string): Promise<ApiResponse<AuditTask>> {
    return withLoading('getTaskProgress', async () => {
      if (USE_MOCK) {
        const result = await auditMockService.getTaskProgress(taskId)
        return {
          code: 200,
          message: '获取任务进度成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get(`/v1/audit/tasks/${taskId}/progress`)
        return response.data
      }
    })
  },

  /**
   * 启动检核任务
   */
  async startAuditTask(taskId: string): Promise<ApiResponse<{ success: boolean }>> {
    return withLoading('startAuditTask', async () => {
      if (USE_MOCK) {
        const result = await auditMockService.startAuditTask(taskId)
        return {
          code: 200,
          message: '检核任务启动成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.post(`/v1/audit/tasks/${taskId}/start`)
        return response.data
      }
    })
  },

  /**
   * 停止检核任务
   */
  async stopAuditTask(taskId: string): Promise<ApiResponse<{ success: boolean }>> {
    return withLoading('stopAuditTask', async () => {
      if (USE_MOCK) {
        const result = await auditMockService.stopAuditTask(taskId)
        return {
          code: 200,
          message: '检核任务停止成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.post(`/v1/audit/tasks/${taskId}/stop`)
        return response.data
      }
    })
  },

  /**
   * 重新开始检核任务
   */
  async restartAuditTask(taskId: string): Promise<ApiResponse<{ taskId: string }>> {
    return withLoading('restartAuditTask', async () => {
      if (USE_MOCK) {
        const result = await auditMockService.restartAuditTask(taskId)
        return {
          code: 200,
          message: '检核任务重新启动成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.post(`/v1/audit/tasks/${taskId}/restart`)
        return response.data
      }
    })
  },

  // ==================== 检核规则相关 ====================

  /**
   * 获取检核规则列表
   */
  async getAuditRules(params: {
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
    page?: number
    size?: number
  } = {}): Promise<ApiResponse<PageResponse<AuditRule>>> {
    return withLoading('getAuditRules', async () => {
      if (USE_MOCK) {
        const result = await auditRulesMockService.getAuditRules(params)
        return {
          code: 200,
          message: '获取检核规则成功',
          data: {
            ...result,
            pages: Math.ceil(result.total / result.size)
          },
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get('/v1/audit/rules', { params })
        return response.data
      }
    })
  },

  /**
   * 获取规则筛选选项
   */
  async getRuleFilterOptions(): Promise<ApiResponse<{
    ruleSources: string[]
    insuranceTypes: string[]
    managementDepartments: string[]
    applicableChapters: string[]
    effectiveStatuses: { label: string; value: string }[]
    auditStatuses: { label: string; value: string }[]
  }>> {
    return withLoading('getRuleFilterOptions', async () => {
      if (USE_MOCK) {
        const result = await auditRulesMockService.getRuleFilterOptions()
        return {
          code: 200,
          message: '获取筛选选项成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get('/v1/audit/rules/filter-options')
        return response.data
      }
    })
  },

  /**
   * 根据ID获取规则详情
   */
  async getRuleById(ruleId: string): Promise<ApiResponse<AuditRule | null>> {
    return withLoading('getRuleById', async () => {
      if (USE_MOCK) {
        const result = await auditRulesMockService.getRuleById(ruleId)
        return {
          code: 200,
          message: result ? '获取规则详情成功' : '规则不存在',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get(`/v1/audit/rules/${ruleId}`)
        return response.data
      }
    })
  },

  /**
   * 批量获取规则详情
   */
  async getRulesByIds(ruleIds: string[]): Promise<ApiResponse<AuditRule[]>> {
    return withLoading('getRulesByIds', async () => {
      if (USE_MOCK) {
        const result = await auditRulesMockService.getRulesByIds(ruleIds)
        return {
          code: 200,
          message: '批量获取规则成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.post('/v1/audit/rules/batch', { ruleIds })
        return response.data
      }
    })
  },

  // ==================== 检核结果相关 ====================

  /**
   * 获取检核结果列表
   */
  async getAuditResults(params: {
    taskId: string
    documentType?: 'terms' | 'feasibility' | 'comparison'
    ruleType?: 'single' | 'double' | 'format' | 'advanced'
    severity?: 'high' | 'medium' | 'low'
    page?: number
    size?: number
  }): Promise<ApiResponse<PageResponse<AuditResult> & { statistics: AuditStatistics[] }>> {
    return withLoading('getAuditResults', async () => {
      if (USE_MOCK) {
        const result = await auditResultsMockService.getAuditResults(params)
        return {
          code: 200,
          message: '获取检核结果成功',
          data: {
            ...result,
            pages: Math.ceil(result.total / result.size)
          },
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get(`/v1/audit/results/${params.taskId}`, {
          params: { ...params, taskId: undefined }
        })
        return response.data
      }
    })
  },

  /**
   * 获取检核结果统计
   */
  async getResultStatistics(taskId: string): Promise<ApiResponse<{
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
  }>> {
    return withLoading('getResultStatistics', async () => {
      if (USE_MOCK) {
        const result = await auditResultsMockService.getResultStatistics(taskId)
        return {
          code: 200,
          message: '获取结果统计成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get(`/v1/audit/results/${taskId}/statistics`)
        return response.data
      }
    })
  },

  /**
   * 获取单个检核结果详情
   */
  async getResultDetail(resultId: string): Promise<ApiResponse<AuditResult | null>> {
    return withLoading('getResultDetail', async () => {
      if (USE_MOCK) {
        const result = await auditResultsMockService.getResultDetail(resultId)
        return {
          code: 200,
          message: result ? '获取结果详情成功' : '结果不存在',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get(`/v1/audit/results/detail/${resultId}`)
        return response.data
      }
    })
  },

  /**
   * 获取检核结果原文内容
   */
  async getResultContent(resultId: string): Promise<ApiResponse<{
    content: string
    highlightRanges: Array<{
      start: number
      end: number
      ruleCode: string
      severity: string
    }>
  }>> {
    return withLoading('getResultContent', async () => {
      if (USE_MOCK) {
        const result = await auditResultsMockService.getResultContent(resultId)
        return {
          code: 200,
          message: '获取原文内容成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get(`/v1/audit/results/${resultId}/content`)
        return response.data
      }
    })
  },

  // ==================== 导出功能相关 ====================

  /**
   * 导出检核结果
   */
  async exportAuditResults(taskId: string, format: 'excel' | 'pdf' = 'excel'): Promise<ApiResponse<{
    downloadUrl: string
    fileName: string
  }>> {
    return withLoading('exportAuditResults', async () => {
      if (USE_MOCK) {
        const result = await auditMockService.exportAuditResults(taskId, format)
        return {
          code: 200,
          message: '导出文件生成成功',
          data: result,
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.post(`/v1/audit/results/${taskId}/export`, { format })
        return response.data
      }
    })
  },

  // ==================== 历史记录相关 ====================

  /**
   * 获取检核历史记录
   */
  async getAuditHistory(params: {
    productId?: string
    startTime?: string
    endTime?: string
    status?: string
    page?: number
    size?: number
  } = {}): Promise<ApiResponse<PageResponse<AuditHistory>>> {
    return withLoading('getAuditHistory', async () => {
      if (USE_MOCK) {
        // Mock历史记录数据
        const mockHistory: AuditHistory[] = [
          {
            id: 'history_001',
            taskId: 'task_001',
            productId: 'prod_001',
            productName: '中国人寿财产保险股份有限公司安徽省地方财政补贴型鸭养殖保险',
            executionTime: new Date(Date.now() - 86400000), // 1天前
            totalErrors: 35,
            status: 'completed',
            reportDownloadUrl: '/api/mock/reports/report_001.xlsx'
          },
          {
            id: 'history_002',
            taskId: 'task_002',
            productId: 'prod_002',
            productName: '中国人寿财产保险股份有限公司安徽省地方财政补贴型能繁母猪养殖保险',
            executionTime: new Date(Date.now() - 172800000), // 2天前
            totalErrors: 28,
            status: 'completed',
            reportDownloadUrl: '/api/mock/reports/report_002.xlsx'
          }
        ]

        const filtered = mockHistory.filter(h =>
          !params.productId || h.productId === params.productId
        )

        const page = params.page || 1
        const size = params.size || 10
        const start = (page - 1) * size
        const end = start + size

        return {
          code: 200,
          message: '获取历史记录成功',
          data: {
            records: filtered.slice(start, end),
            total: filtered.length,
            current: page,
            size: size,
            pages: Math.ceil(filtered.length / size)
          },
          success: true,
          timestamp: Date.now()
        }
      } else {
        const response = await request.get('/v1/audit/history', { params })
        return response.data
      }
    })
  }
}

// 导出加载状态获取函数
export { getLoadingState as getAuditLoadingState }