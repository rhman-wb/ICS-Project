/**
 * 检核结果展示Composable
 * 实现检核结果展示的业务逻辑
 */

import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { auditApi } from '@/api/modules/audit'
import type {
  AuditResult,
  AuditStatistics,
  AuditResultsState,
  AuditResultQueryParams,
  ResultStatistics,
  DocumentTypeMap,
  RuleTypeMap,
  SeverityMap
} from '@/api/types/audit'

export interface UseAuditResultsOptions {
  taskId: string
  autoLoad?: boolean
  defaultParams?: Partial<AuditResultQueryParams>
}

export function useAuditResults(options: UseAuditResultsOptions) {
  const { taskId, autoLoad = true, defaultParams = {} } = options

  // ==================== 响应式状态 ====================

  const state = reactive<AuditResultsState>({
    results: [],
    statistics: [],
    queryParams: {
      taskId,
      page: 1,
      size: 10,
      ...defaultParams
    },
    loading: false,
    total: 0,
    current: 1,
    size: 10
  })

  // 统计数据状态
  const overallStatistics = ref<ResultStatistics | null>(null)
  const statisticsLoading = ref(false)

  // 导出相关状态
  const exporting = ref(false)
  const exportProgress = ref(0)

  // 筛选状态
  const activeDocumentType = ref<string>('all')
  const activeRuleType = ref<string>('')
  const activeSeverity = ref<string>('')

  // ==================== 计算属性 ====================

  // 加载状态
  const loading = computed(() => state.loading)

  // 分页信息
  const pagination = computed(() => ({
    current: state.current,
    pageSize: state.size,
    total: state.total,
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: ['10', '20', '50', '100'],
    showTotal: (total: number, range: [number, number]) =>
      `第 ${range[0]}-${range[1]} 条，共 ${total} 个问题`
  }))

  // 文档类型统计
  const documentTypeStats = computed(() => {
    if (!overallStatistics.value) return []

    const stats = overallStatistics.value.byDocumentType
    return [
      { type: 'terms', label: '条款', count: stats.terms, color: '#1890ff' },
      { type: 'feasibility', label: '可行性报告', count: stats.feasibility, color: '#52c41a' },
      { type: 'comparison', label: '前后修改对比表', count: stats.comparison, color: '#faad14' }
    ]
  })

  // 规则类型统计
  const ruleTypeStats = computed(() => {
    if (!overallStatistics.value) return []

    const stats = overallStatistics.value.byRuleType
    return [
      { type: 'single', label: '单句规则', count: stats.single },
      { type: 'double', label: '双句规则', count: stats.double },
      { type: 'format', label: '格式规则', count: stats.format },
      { type: 'advanced', label: '高级规则', count: stats.advanced }
    ]
  })

  // 严重程度统计
  const severityStats = computed(() => {
    if (!overallStatistics.value) return []

    const stats = overallStatistics.value.bySeverity
    return [
      { type: 'high', label: '高', count: stats.high, color: '#f5222d' },
      { type: 'medium', label: '中', count: stats.medium, color: '#faad14' },
      { type: 'low', label: '低', count: stats.low, color: '#52c41a' }
    ]
  })

  // 按文档类型分组的统计表格数据
  const statisticsTableData = computed(() => {
    return state.statistics.map(stat => ({
      documentType: stat.documentType,
      documentTypeName: getDocumentTypeName(stat.documentType),
      singleRules: stat.ruleTypeStats.single,
      doubleRules: stat.ruleTypeStats.double,
      formatRules: stat.ruleTypeStats.format,
      advancedRules: stat.ruleTypeStats.advanced,
      totalCount: stat.totalCount
    }))
  })

  // 当前筛选的结果总数
  const filteredTotal = computed((): number => {
    if (activeDocumentType.value === 'all') {
      return overallStatistics.value?.totalResults || 0
    }

    const stats = overallStatistics.value?.byDocumentType
    if (!stats) return 0

    switch (activeDocumentType.value) {
      case 'terms': return stats.terms
      case 'feasibility': return stats.feasibility
      case 'comparison': return stats.comparison
      default: return 0
    }
  })

  // ==================== 数据获取方法 ====================

  /**
   * 获取检核结果列表
   */
  const loadResults = async (params?: Partial<AuditResultQueryParams>): Promise<void> => {
    try {
      state.loading = true

      // 合并查询参数
      const queryParams = {
        ...state.queryParams,
        ...params
      }

      const response = await auditApi.getAuditResults(queryParams)

      if (response.success && response.data) {
        state.results = response.data.records
        state.statistics = response.data.statistics
        state.total = response.data.total
        state.current = response.data.current
        state.size = response.data.size

        // 更新查询参数
        Object.assign(state.queryParams, queryParams)
      } else {
        message.error(response.message || '获取检核结果失败')
      }
    } catch (error: any) {
      console.error('Load results error:', error)
      message.error('获取检核结果失败：' + (error.message || '未知错误'))

      // 重置数据
      state.results = []
      state.statistics = []
      state.total = 0
    } finally {
      state.loading = false
    }
  }

  /**
   * 获取整体统计数据
   */
  const loadStatistics = async (): Promise<void> => {
    try {
      statisticsLoading.value = true

      const response = await auditApi.getResultStatistics(taskId)

      if (response.success && response.data) {
        overallStatistics.value = response.data
      } else {
        message.error(response.message || '获取统计数据失败')
      }
    } catch (error: any) {
      console.error('Load statistics error:', error)
      message.error('获取统计数据失败：' + (error.message || '未知错误'))
    } finally {
      statisticsLoading.value = false
    }
  }

  /**
   * 刷新数据
   */
  const refreshData = async (): Promise<void> => {
    await Promise.all([
      loadResults({ page: 1 }),
      loadStatistics()
    ])
  }

  // ==================== 筛选方法 ====================

  /**
   * 按文档类型筛选
   */
  const filterByDocumentType = async (documentType: string): Promise<void> => {
    activeDocumentType.value = documentType

    const filterParams: Partial<AuditResultQueryParams> = {
      page: 1,
      documentType: documentType === 'all' ? undefined : documentType as any
    }

    await loadResults(filterParams)
  }

  /**
   * 按规则类型筛选
   */
  const filterByRuleType = async (ruleType: string): Promise<void> => {
    activeRuleType.value = ruleType

    const filterParams: Partial<AuditResultQueryParams> = {
      page: 1,
      ruleType: ruleType || undefined
    }

    await loadResults(filterParams)
  }

  /**
   * 按严重程度筛选
   */
  const filterBySeverity = async (severity: string): Promise<void> => {
    activeSeverity.value = severity

    const filterParams: Partial<AuditResultQueryParams> = {
      page: 1,
      severity: severity || undefined
    }

    await loadResults(filterParams)
  }

  /**
   * 重置所有筛选
   */
  const resetFilters = async (): Promise<void> => {
    activeDocumentType.value = 'all'
    activeRuleType.value = ''
    activeSeverity.value = ''

    await loadResults({
      page: 1,
      documentType: undefined,
      ruleType: undefined,
      severity: undefined
    })
  }

  /**
   * 多条件筛选
   */
  const applyFilters = async (): Promise<void> => {
    const filterParams: Partial<AuditResultQueryParams> = {
      page: 1,
      documentType: activeDocumentType.value === 'all' ? undefined : activeDocumentType.value as any,
      ruleType: activeRuleType.value || undefined,
      severity: activeSeverity.value || undefined
    }

    await loadResults(filterParams)
  }

  // ==================== 分页方法 ====================

  /**
   * 分页变化处理
   */
  const handlePageChange = async (page: number, pageSize: number): Promise<void> => {
    await loadResults({ page, size: pageSize })
  }

  /**
   * 页大小变化处理
   */
  const handlePageSizeChange = async (current: number, size: number): Promise<void> => {
    await loadResults({ page: 1, size })
  }

  // ==================== 导出功能 ====================

  /**
   * 导出检核结果
   */
  const exportResults = async (format: 'excel' | 'pdf' = 'excel'): Promise<void> => {
    try {
      exporting.value = true
      exportProgress.value = 0

      // 模拟进度更新
      const progressTimer = setInterval(() => {
        if (exportProgress.value < 90) {
          exportProgress.value += Math.random() * 20
        }
      }, 300)

      const response = await auditApi.exportAuditResults(taskId, format)

      clearInterval(progressTimer)
      exportProgress.value = 100

      if (response.success && response.data) {
        message.success('导出成功')

        // 触发文件下载
        const link = document.createElement('a')
        link.href = response.data.downloadUrl
        link.download = response.data.fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        return response.data.downloadUrl
      } else {
        message.error(response.message || '导出失败')
      }
    } catch (error: any) {
      console.error('Export results error:', error)
      message.error('导出失败：' + (error.message || '未知错误'))
    } finally {
      exporting.value = false
      exportProgress.value = 0
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 获取文档类型名称
   */
  const getDocumentTypeName = (type: string): string => {
    const map: Record<string, string> = {
      terms: '条款',
      feasibility: '可行性报告',
      comparison: '前后修改对比表'
    }
    return map[type] || type
  }

  /**
   * 获取规则类型名称
   */
  const getRuleTypeName = (type: string): string => {
    const map: Record<string, string> = {
      single: '单句规则',
      double: '双句规则',
      format: '格式规则',
      advanced: '高级规则'
    }
    return map[type] || type
  }

  /**
   * 获取严重程度名称
   */
  const getSeverityName = (severity: string): string => {
    const map: Record<string, string> = {
      high: '高',
      medium: '中',
      low: '低'
    }
    return map[severity] || severity
  }

  /**
   * 获取严重程度颜色
   */
  const getSeverityColor = (severity: string): string => {
    const map: Record<string, string> = {
      high: '#f5222d',
      medium: '#faad14',
      low: '#52c41a'
    }
    return map[severity] || '#d9d9d9'
  }

  /**
   * 根据ID获取结果详情
   */
  const getResultById = (resultId: string): AuditResult | undefined => {
    return state.results.find(result => result.id === resultId)
  }

  /**
   * 格式化页码显示
   */
  const formatPageNumber = (pageNumber: string): string => {
    return pageNumber || '未知页'
  }

  // ==================== 生命周期 ====================

  onMounted(() => {
    if (autoLoad) {
      Promise.all([
        loadResults(),
        loadStatistics()
      ]).catch(error => {
        console.error('Initial load error:', error)
      })
    }
  })

  // ==================== 返回接口 ====================

  return {
    // 状态
    state,
    overallStatistics,
    statisticsLoading,
    exporting,
    exportProgress,
    activeDocumentType,
    activeRuleType,
    activeSeverity,

    // 计算属性
    loading,
    pagination,
    documentTypeStats,
    ruleTypeStats,
    severityStats,
    statisticsTableData,
    filteredTotal,

    // 数据获取
    loadResults,
    loadStatistics,
    refreshData,

    // 筛选功能
    filterByDocumentType,
    filterByRuleType,
    filterBySeverity,
    resetFilters,
    applyFilters,

    // 分页功能
    handlePageChange,
    handlePageSizeChange,

    // 导出功能
    exportResults,

    // 工具方法
    getDocumentTypeName,
    getRuleTypeName,
    getSeverityName,
    getSeverityColor,
    getResultById,
    formatPageNumber
  }
}