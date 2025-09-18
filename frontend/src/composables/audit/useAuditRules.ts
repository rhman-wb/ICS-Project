/**
 * 检核规则选择Composable
 * 实现规则选择页面的业务逻辑
 */

import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { auditApi } from '@/api/modules/audit'
import type {
  AuditRule,
  AuditRulesState,
  RuleFilterOptions,
  AuditRuleQueryParams,
  RuleSelectionForm
} from '@/api/types/audit'

export interface UseAuditRulesOptions {
  autoLoad?: boolean
  defaultParams?: Partial<AuditRuleQueryParams>
}

export function useAuditRules(options: UseAuditRulesOptions = {}) {
  const { autoLoad = true, defaultParams = {} } = options

  // ==================== 响应式状态 ====================

  const state = reactive<AuditRulesState>({
    rules: [],
    selectedRules: [],
    filterOptions: null,
    queryParams: {
      page: 1,
      size: 10,
      ruleSource: '',
      insuranceTypes: [],
      managementDepartment: '',
      applicableChapter: '',
      effectiveStatus: '',
      auditStatus: '',
      keyword: '',
      ...defaultParams
    },
    loading: false,
    total: 0,
    current: 1,
    size: 10
  })

  // 表单状态
  const filterForm = reactive<RuleSelectionForm>({
    ruleSource: '',
    insuranceTypes: [],
    managementDepartment: '',
    applicableChapter: '',
    effectiveStatus: 'active', // 默认显示有效规则
    auditStatus: 'approved', // 默认显示已审核规则
    startTime: '',
    endTime: '',
    keyword: ''
  })

  // 加载状态
  const loading = computed(() => state.loading)
  const filterLoading = ref(false)

  // ==================== 计算属性 ====================

  // 已选择的规则数量
  const selectedCount = computed(() => state.selectedRules.length)

  // 是否有选中的规则
  const hasSelected = computed(() => selectedCount.value > 0)

  // 分页信息
  const pagination = computed(() => ({
    current: state.current,
    pageSize: state.size,
    total: state.total,
    showSizeChanger: true,
    showQuickJumper: true,
    pageSizeOptions: ['10', '20', '50', '100'],
    showTotal: (total: number, range: [number, number]) =>
      `第 ${range[0]}-${range[1]} 条，共 ${total} 条规则`
  }))

  // ==================== 数据获取方法 ====================

  /**
   * 获取筛选选项
   */
  const loadFilterOptions = async (): Promise<void> => {
    try {
      filterLoading.value = true
      const response = await auditApi.getRuleFilterOptions()

      if (response.success && response.data) {
        state.filterOptions = response.data
      } else {
        message.error(response.message || '获取筛选选项失败')
      }
    } catch (error: any) {
      console.error('Load filter options error:', error)
      message.error('获取筛选选项失败：' + (error.message || '未知错误'))
    } finally {
      filterLoading.value = false
    }
  }

  /**
   * 获取规则列表
   */
  const loadRules = async (params?: Partial<AuditRuleQueryParams>): Promise<void> => {
    try {
      state.loading = true

      // 合并查询参数
      const queryParams = {
        ...state.queryParams,
        ...params
      }

      const response = await auditApi.getAuditRules(queryParams)

      if (response.success && response.data) {
        state.rules = response.data.records
        state.total = response.data.total
        state.current = response.data.current
        state.size = response.data.size

        // 更新查询参数
        Object.assign(state.queryParams, queryParams)
      } else {
        message.error(response.message || '获取检核规则失败')
      }
    } catch (error: any) {
      console.error('Load rules error:', error)
      message.error('获取检核规则失败：' + (error.message || '未知错误'))

      // 重置数据
      state.rules = []
      state.total = 0
    } finally {
      state.loading = false
    }
  }

  /**
   * 刷新规则列表
   */
  const refreshRules = (): Promise<void> => {
    return loadRules({ page: 1 })
  }

  // ==================== 筛选方法 ====================

  /**
   * 搜索规则
   */
  const searchRules = (): Promise<void> => {
    // 将表单数据转换为查询参数
    const searchParams = {
      page: 1,
      ruleSource: filterForm.ruleSource,
      insuranceTypes: filterForm.insuranceTypes.length > 0 ? filterForm.insuranceTypes : undefined,
      managementDepartment: filterForm.managementDepartment,
      applicableChapter: filterForm.applicableChapter,
      effectiveStatus: filterForm.effectiveStatus,
      auditStatus: filterForm.auditStatus,
      startTime: filterForm.startTime,
      endTime: filterForm.endTime,
      keyword: filterForm.keyword?.trim()
    }

    return loadRules(searchParams)
  }

  /**
   * 重置筛选条件
   */
  const resetFilter = (): Promise<void> => {
    // 重置表单
    Object.assign(filterForm, {
      ruleSource: '',
      insuranceTypes: [],
      managementDepartment: '',
      applicableChapter: '',
      effectiveStatus: 'active',
      auditStatus: 'approved',
      startTime: '',
      endTime: '',
      keyword: ''
    })

    // 重置查询参数并重新加载
    return loadRules({
      page: 1,
      ruleSource: '',
      insuranceTypes: undefined,
      managementDepartment: '',
      applicableChapter: '',
      effectiveStatus: 'active',
      auditStatus: 'approved',
      startTime: '',
      endTime: '',
      keyword: ''
    })
  }

  /**
   * 快速筛选 - 按险种
   */
  const filterByInsuranceType = (insuranceType: string): Promise<void> => {
    filterForm.insuranceTypes = [insuranceType]
    return searchRules()
  }

  /**
   * 快速筛选 - 按管理部门
   */
  const filterByDepartment = (department: string): Promise<void> => {
    filterForm.managementDepartment = department
    return searchRules()
  }

  /**
   * 快速筛选 - 按章节
   */
  const filterByChapter = (chapter: string): Promise<void> => {
    filterForm.applicableChapter = chapter
    return searchRules()
  }

  // ==================== 选择方法 ====================

  /**
   * 选择/取消选择规则
   */
  const toggleRuleSelection = (ruleId: string): void => {
    const index = state.selectedRules.indexOf(ruleId)
    if (index > -1) {
      state.selectedRules.splice(index, 1)
    } else {
      state.selectedRules.push(ruleId)
    }
  }

  /**
   * 批量选择规则
   */
  const selectRules = (ruleIds: string[]): void => {
    ruleIds.forEach(id => {
      if (!state.selectedRules.includes(id)) {
        state.selectedRules.push(id)
      }
    })
  }

  /**
   * 全选当前页规则
   */
  const selectAllCurrentPage = (): void => {
    const currentPageRuleIds = state.rules.map(rule => rule.id)
    selectRules(currentPageRuleIds)
  }

  /**
   * 取消全选当前页
   */
  const deselectAllCurrentPage = (): void => {
    const currentPageRuleIds = state.rules.map(rule => rule.id)
    state.selectedRules = state.selectedRules.filter(id => !currentPageRuleIds.includes(id))
  }

  /**
   * 清空所有选择
   */
  const clearSelection = (): void => {
    state.selectedRules = []
  }

  /**
   * 检查规则是否被选中
   */
  const isRuleSelected = (ruleId: string): boolean => {
    return state.selectedRules.includes(ruleId)
  }

  /**
   * 检查当前页是否全选
   */
  const isCurrentPageAllSelected = computed((): boolean => {
    if (state.rules.length === 0) return false
    return state.rules.every(rule => state.selectedRules.includes(rule.id))
  })

  /**
   * 检查当前页是否部分选中
   */
  const isCurrentPageIndeterminate = computed((): boolean => {
    if (state.rules.length === 0) return false
    const selectedInCurrentPage = state.rules.filter(rule => state.selectedRules.includes(rule.id))
    return selectedInCurrentPage.length > 0 && selectedInCurrentPage.length < state.rules.length
  })

  // ==================== 分页方法 ====================

  /**
   * 分页变化处理
   */
  const handlePageChange = (page: number, pageSize: number): Promise<void> => {
    return loadRules({ page, size: pageSize })
  }

  /**
   * 页大小变化处理
   */
  const handlePageSizeChange = (current: number, size: number): Promise<void> => {
    return loadRules({ page: 1, size })
  }

  // ==================== 工具方法 ====================

  /**
   * 根据规则ID获取规则详情
   */
  const getRuleById = (ruleId: string): AuditRule | undefined => {
    return state.rules.find(rule => rule.id === ruleId)
  }

  /**
   * 获取选中的规则详情列表
   */
  const getSelectedRules = (): AuditRule[] => {
    return state.selectedRules
      .map(id => getRuleById(id))
      .filter((rule): rule is AuditRule => rule !== undefined)
  }

  /**
   * 验证是否可以进行下一步
   */
  const validateSelection = (): boolean => {
    if (state.selectedRules.length === 0) {
      message.warning('请至少选择一条检核规则')
      return false
    }
    return true
  }

  // ==================== 生命周期 ====================

  onMounted(() => {
    if (autoLoad) {
      Promise.all([
        loadFilterOptions(),
        loadRules()
      ]).catch(error => {
        console.error('Initial load error:', error)
      })
    }
  })

  // ==================== 返回接口 ====================

  return {
    // 状态
    state,
    filterForm,
    loading,
    filterLoading,

    // 计算属性
    selectedCount,
    hasSelected,
    pagination,
    isCurrentPageAllSelected,
    isCurrentPageIndeterminate,

    // 数据获取
    loadFilterOptions,
    loadRules,
    refreshRules,

    // 筛选功能
    searchRules,
    resetFilter,
    filterByInsuranceType,
    filterByDepartment,
    filterByChapter,

    // 选择功能
    toggleRuleSelection,
    selectRules,
    selectAllCurrentPage,
    deselectAllCurrentPage,
    clearSelection,
    isRuleSelected,

    // 分页功能
    handlePageChange,
    handlePageSizeChange,

    // 工具方法
    getRuleById,
    getSelectedRules,
    validateSelection
  }
}