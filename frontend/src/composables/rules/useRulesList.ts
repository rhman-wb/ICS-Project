import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { rulesApi, type RuleQueryParams, type RuleInfo } from '@/api/modules/rules'
import { createLoadingRef } from '@/utils/loading'

export interface FilterValues {
  ruleSource?: string
  applicableInsurance?: string
  manageDepartment?: string
  auditStatus?: string
  effectiveStatus?: string
  applicableChapter?: string
  businessArea?: string
  keyword?: string
  startTime?: any
  endTime?: any
}

export interface SelectedFilter {
  key: string
  label: string
}

export function useRulesList() {
  const router = useRouter()
  const loading = createLoadingRef('rules-list')

  // 数据状态
  const rulesData = ref<RuleInfo[]>([])
  const selectedRules = ref<string[]>([])
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条数据`
  })

  // 筛选状态
  const filters = reactive<FilterValues>({
    ruleSource: undefined,
    applicableInsurance: undefined,
    manageDepartment: undefined,
    auditStatus: undefined,
    effectiveStatus: undefined,
    applicableChapter: undefined,
    businessArea: undefined,
    keyword: undefined,
    startTime: undefined,
    endTime: undefined
  })

  // 已选条件
  const selectedFilters = computed<SelectedFilter[]>(() => {
    const result: SelectedFilter[] = []

    if (filters.ruleSource) {
      result.push({ key: 'ruleSource', label: `规则来源: ${filters.ruleSource}` })
    }
    if (filters.applicableInsurance) {
      result.push({ key: 'applicableInsurance', label: `适用险种: ${filters.applicableInsurance}` })
    }
    if (filters.manageDepartment) {
      result.push({ key: 'manageDepartment', label: `管理部门: ${filters.manageDepartment}` })
    }
    if (filters.auditStatus) {
      result.push({ key: 'auditStatus', label: `审核状态: ${filters.auditStatus}` })
    }
    if (filters.effectiveStatus) {
      result.push({ key: 'effectiveStatus', label: `有效状态: ${filters.effectiveStatus}` })
    }
    if (filters.applicableChapter) {
      result.push({ key: 'applicableChapter', label: `适用章节: ${filters.applicableChapter}` })
    }
    if (filters.businessArea) {
      result.push({ key: 'businessArea', label: `经营区域: ${filters.businessArea}` })
    }
    if (filters.keyword) {
      result.push({ key: 'keyword', label: `关键词: ${filters.keyword}` })
    }

    return result
  })

  // 计算属性
  const selectedCount = computed(() => selectedRules.value.length)
  const totalCount = computed(() => rulesData.value.length)

  // 获取规则列表
  const fetchRulesList = async () => {
    try {
      loading.value = true

      // 格式化时间参数
      const formatTime = (time: any) => {
        if (!time) return undefined
        // 如果是dayjs对象，格式化为HH:mm:ss格式
        if (dayjs.isDayjs(time)) {
          return time.format('HH:mm:ss')
        }
        // 如果是其他格式，尝试转换
        if (time instanceof Date) {
          return dayjs(time).format('HH:mm:ss')
        }
        // 如果已经是字符串，直接返回
        return time
      }

      const params: RuleQueryParams = {
        page: pagination.current,
        size: pagination.pageSize,
        ...filters,
        // 格式化时间字段
        startTime: formatTime(filters.startTime),
        endTime: formatTime(filters.endTime)
      }

      const response = await rulesApi.getRuleList(params)

      if (response.data) {
        rulesData.value = response.data.records
        pagination.total = response.data.total
      }
    } catch (error) {
      console.error('获取规则列表失败:', error)
      message.error('获取规则列表失败')
    } finally {
      loading.value = false
    }
  }

  // 搜索
  const handleSearch = () => {
    pagination.current = 1
    selectedRules.value = []
    fetchRulesList()
  }

  // 重置筛选
  const handleResetFilters = () => {
    Object.keys(filters).forEach(key => {
      filters[key] = undefined
    })
    pagination.current = 1
    selectedRules.value = []
    fetchRulesList()
  }

  // 移除单个筛选条件
  const removeFilter = (key: string) => {
    filters[key] = undefined
    handleSearch()
  }

  // 清除所有筛选条件
  const clearAllFilters = () => {
    handleResetFilters()
  }

  // 分页改变
  const handleTableChange = (pag: any) => {
    pagination.current = pag.current
    pagination.pageSize = pag.pageSize
    fetchRulesList()
  }

  // 全选/取消全选
  const handleSelectAll = () => {
    if (selectedRules.value.length === rulesData.value.length) {
      selectedRules.value = []
    } else {
      selectedRules.value = rulesData.value.map(rule => rule.id)
    }
  }

  // 批量删除
  const handleBatchDelete = async () => {
    if (selectedRules.value.length === 0) {
      message.warning('请先选择要删除的规则')
      return
    }

    try {
      const response = await rulesApi.batchDeleteRules({ ids: selectedRules.value })

      if (response.success) {
        message.success('批量删除成功')
        selectedRules.value = []
        fetchRulesList()
      }
    } catch (error) {
      message.error('批量删除失败')
    }
  }

  // 导航操作
  const handleAdd = () => {
    router.push('/rules/new')
  }

  const handleSingleRuleCreate = () => {
    router.push('/rules/new?type=single')
  }

  const handleBack = () => {
    router.back()
  }

  const handleRefresh = () => {
    fetchRulesList()
  }

  const handleAllRules = () => {
    handleResetFilters()
  }

  // 关注操作
  const handleFocus = async () => {
    if (selectedRules.value.length === 0) {
      message.warning('请先选择要关注的规则')
      return
    }

    try {
      const response = await rulesApi.batchToggleFollow({
        ids: selectedRules.value,
        followed: true
      })

      if (response.success) {
        message.success('关注成功')
        fetchRulesList()
      }
    } catch (error) {
      message.error('关注失败')
    }
  }

  // 编辑规则
  const handleEdit = (record: RuleInfo) => {
    router.push(`/rules/edit/${record.id}`)
  }

  // 删除单个规则
  const handleDelete = async (record: RuleInfo) => {
    try {
      const response = await rulesApi.deleteRule(record.id)

      if (response.success) {
        message.success('删除成功')
        fetchRulesList()
      }
    } catch (error) {
      message.error('删除失败')
    }
  }

  // 切换关注状态
  const handleToggleFollow = async (record: RuleInfo) => {
    try {
      const response = await rulesApi.toggleRuleFollow(record.id, !record.followed)

      if (response.success) {
        message.success(record.followed ? '取消关注成功' : '关注成功')
        fetchRulesList()
      }
    } catch (error) {
      message.error('操作失败')
    }
  }

  // 初始化
  onMounted(() => {
    fetchRulesList()
  })

  return {
    // 状态
    loading,
    rulesData,
    selectedRules,
    pagination,
    filters,
    selectedFilters,
    selectedCount,
    totalCount,

    // 方法
    fetchRulesList,
    handleSearch,
    handleResetFilters,
    removeFilter,
    clearAllFilters,
    handleTableChange,
    handleSelectAll,
    handleBatchDelete,
    handleAdd,
    handleSingleRuleCreate,
    handleBack,
    handleRefresh,
    handleAllRules,
    handleFocus,
    handleEdit,
    handleDelete,
    handleToggleFollow
  }
}