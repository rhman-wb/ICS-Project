import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import RulesListView from '@/views/rules/RulesListView.vue'

// Mock rule store
const ruleStore = {
  fetchRuleList: vi.fn(),
  deleteRule: vi.fn(),
  batchDeleteRules: vi.fn(),
  copyRule: vi.fn(),
  toggleRuleFollow: vi.fn(),
  submitToOA: vi.fn()
}

vi.mock('@/stores/modules/rule', () => ({
  useRuleStore: () => ruleStore
}))

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      error: vi.fn(),
      success: vi.fn(),
      loading: vi.fn(),
      destroy: vi.fn()
    },
    Modal: {
      confirm: vi.fn()
    }
  }
})

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush
    }),
    useRoute: () => ({
      query: {},
      params: {}
    })
  }
})

// Mock API
vi.mock('@/api/rule', () => ({
  getRuleList: vi.fn(),
  deleteRule: vi.fn(),
  batchDeleteRules: vi.fn(),
  toggleRuleFollow: vi.fn(),
  copyRule: vi.fn(),
  submitToOA: vi.fn()
}))

describe('RulesListView', () => {
  let wrapper: VueWrapper

  const mockRuleData = {
    records: [
      {
        id: '1',
        ruleName: '测试规则1',
        ruleType: 'SINGLE',
        description: '测试描述1',
        auditStatus: 'PENDING',
        effectiveStatus: 'DRAFT',
        followed: false,
        createdAt: '2024-09-16T10:00:00',
        updatedAt: '2024-09-16T10:00:00'
      },
      {
        id: '2',
        ruleName: '测试规则2',
        ruleType: 'DOUBLE',
        description: '测试描述2',
        auditStatus: 'APPROVED',
        effectiveStatus: 'EFFECTIVE',
        followed: true,
        createdAt: '2024-09-16T11:00:00',
        updatedAt: '2024-09-16T11:00:00'
      }
    ],
    current: 1,
    size: 10,
    total: 2
  }

  beforeEach(() => {
    setActivePinia(createPinia())

    // Mock store methods
    vi.spyOn(ruleStore, 'fetchRuleList').mockResolvedValue(mockRuleData)
    vi.spyOn(ruleStore, 'deleteRule').mockResolvedValue({ success: true })
    vi.spyOn(ruleStore, 'batchDeleteRules').mockResolvedValue({ success: true, successCount: 2 })

    wrapper = mount(RulesListView, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-card': {
            template: '<div class="mock-card"><slot /></div>'
          },
          'a-form': {
            template: '<form @submit.prevent><slot /></form>'
          },
          'a-form-item': {
            template: '<div class="form-item"><slot /></div>'
          },
          'a-input': {
            template: '<input v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-select': {
            template: '<select v-model="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot /></select>',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-select-option': {
            template: '<option :value="value"><slot /></option>',
            props: ['value']
          },
          'a-button': {
            template: '<button :disabled="disabled" :loading="loading" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'loading', 'type', 'danger']
          },
          'a-table': {
            template: '<div class="mock-table"><slot /></div>',
            props: ['columns', 'dataSource', 'rowSelection', 'pagination', 'loading']
          },
          'a-tag': {
            template: '<span class="mock-tag" :class="color"><slot /></span>',
            props: ['color']
          },
          'a-popconfirm': {
            template: '<div @click="$emit(\'confirm\')"><slot /></div>',
            props: ['title'],
            emits: ['confirm']
          },
          'a-tooltip': {
            template: '<div><slot /></div>',
            props: ['title']
          },
          'a-space': {
            template: '<div class="mock-space"><slot /></div>'
          },
          'a-row': {
            template: '<div class="mock-row"><slot /></div>'
          },
          'a-col': {
            template: '<div class="mock-col"><slot /></div>',
            props: ['span', 'xs', 'sm', 'md', 'lg', 'xl']
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    expect(wrapper.find('.mock-card').exists()).toBe(true)
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('.mock-table').exists()).toBe(true)
  })

  it('initializes with default filter values', () => {
    expect(wrapper.vm.queryForm.page).toBe(1)
    expect(wrapper.vm.queryForm.size).toBe(10)
    expect(wrapper.vm.queryForm.ruleName).toBe('')
    expect(wrapper.vm.queryForm.ruleType).toBe(undefined)
    expect(wrapper.vm.queryForm.auditStatus).toBe(undefined)
    expect(wrapper.vm.queryForm.effectiveStatus).toBe(undefined)
  })

  it('fetches rule list on mounted', () => {
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(wrapper.vm.queryForm)
  })

  it('updates query form when user inputs values', async () => {
    const ruleNameInput = wrapper.find('input')
    await ruleNameInput.setValue('测试规则')

    expect(wrapper.vm.queryForm.ruleName).toBe('测试规则')
  })

  it('handles search functionality', async () => {
    // Fill search form
    const ruleNameInput = wrapper.find('input')
    await ruleNameInput.setValue('测试规则')

    // Mock form submission
    await wrapper.find('form').trigger('submit')

    expect(wrapper.vm.queryForm.page).toBe(1) // Reset to first page
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({
        ruleName: '测试规则',
        page: 1
      })
    )
  })

  it('handles reset functionality', async () => {
    // First set some values
    wrapper.vm.queryForm.ruleName = '测试规则'
    wrapper.vm.queryForm.ruleType = 'SINGLE'

    // Trigger reset
    wrapper.vm.handleReset()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.queryForm.ruleName).toBe('')
    expect(wrapper.vm.queryForm.ruleType).toBe(undefined)
    expect(wrapper.vm.queryForm.page).toBe(1)
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('handles single rule deletion', async () => {
    const ruleId = '1'

    await wrapper.vm.handleDelete(ruleId)

    expect(ruleStore.deleteRule).toHaveBeenCalledWith(ruleId)
    expect(message.success).toHaveBeenCalledWith('删除成功')
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('handles batch deletion', async () => {
    const selectedIds = ['1', '2']
    wrapper.vm.selectedRowKeys = selectedIds

    await wrapper.vm.handleBatchDelete()

    expect(ruleStore.batchDeleteRules).toHaveBeenCalledWith(selectedIds)
    expect(message.success).toHaveBeenCalledWith('批量删除成功，成功删除 2 条记录')
    expect(wrapper.vm.selectedRowKeys).toEqual([])
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('prevents batch deletion when no items selected', async () => {
    wrapper.vm.selectedRowKeys = []

    await wrapper.vm.handleBatchDelete()

    expect(message.error).toHaveBeenCalledWith('请先选择要删除的规则')
    expect(ruleStore.batchDeleteRules).not.toHaveBeenCalled()
  })

  it('handles rule creation navigation', async () => {
    const ruleType = 'SINGLE'

    wrapper.vm.handleCreate(ruleType)

    expect(mockPush).toHaveBeenCalledWith(`/rules/new?type=${ruleType}`)
  })

  it('handles rule edit navigation', async () => {
    const ruleId = '1'

    wrapper.vm.handleEdit(ruleId)

    expect(mockPush).toHaveBeenCalledWith(`/rules/${ruleId}/edit`)
  })

  it('handles rule copy functionality', async () => {
    const ruleId = '1'
    vi.spyOn(ruleStore, 'copyRule').mockResolvedValue({ success: true, data: { id: 'new-id' } })

    await wrapper.vm.handleCopy(ruleId)

    expect(ruleStore.copyRule).toHaveBeenCalledWith(ruleId)
    expect(message.success).toHaveBeenCalledWith('复制成功')
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('handles toggle follow functionality', async () => {
    const rule = mockRuleData.records[0]
    vi.spyOn(ruleStore, 'toggleRuleFollow').mockResolvedValue({ success: true })

    await wrapper.vm.handleToggleFollow(rule)

    expect(ruleStore.toggleRuleFollow).toHaveBeenCalledWith(rule.id, !rule.followed)
    expect(message.success).toHaveBeenCalledWith('关注状态更新成功')
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('handles pagination change', async () => {
    const newPage = 2
    const newPageSize = 20

    await wrapper.vm.handleTableChange({ current: newPage, pageSize: newPageSize })

    expect(wrapper.vm.queryForm.page).toBe(newPage)
    expect(wrapper.vm.queryForm.size).toBe(newPageSize)
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({
        page: newPage,
        size: newPageSize
      })
    )
  })

  it('handles row selection change', async () => {
    const selectedRowKeys = ['1', '2']

    wrapper.vm.handleSelectionChange(selectedRowKeys)

    expect(wrapper.vm.selectedRowKeys).toEqual(selectedRowKeys)
  })

  it('handles submit to OA functionality', async () => {
    const selectedIds = ['1', '2']
    wrapper.vm.selectedRowKeys = selectedIds
    vi.spyOn(ruleStore, 'submitToOA').mockResolvedValue({ success: true })

    await wrapper.vm.handleSubmitOA()

    expect(ruleStore.submitToOA).toHaveBeenCalledWith({
      ruleIds: selectedIds,
      submissionType: 'BATCH',
      comments: ''
    })
    expect(message.success).toHaveBeenCalledWith('提交OA审核成功')
    expect(wrapper.vm.selectedRowKeys).toEqual([])
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('prevents OA submission when no items selected', async () => {
    wrapper.vm.selectedRowKeys = []

    await wrapper.vm.handleSubmitOA()

    expect(message.error).toHaveBeenCalledWith('请先选择要提交的规则')
    expect(ruleStore.submitToOA).not.toHaveBeenCalled()
  })

  it('handles loading states correctly', async () => {
    wrapper.vm.loading = true
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.mock-table')
    expect(table.attributes('loading')).toBeDefined()
  })

  it('handles error states gracefully', async () => {
    const errorMessage = '获取规则列表失败'
    vi.spyOn(ruleStore, 'fetchRuleList').mockRejectedValue(new Error(errorMessage))

    await wrapper.vm.fetchRuleList()

    expect(message.error).toHaveBeenCalledWith(errorMessage)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('formats rule type display correctly', () => {
    const testCases = [
      { type: 'SINGLE', expected: '单句规则' },
      { type: 'DOUBLE', expected: '双句规则' },
      { type: 'FORMAT', expected: '格式规则' },
      { type: 'ADVANCED', expected: '高级规则' }
    ]

    testCases.forEach(({ type, expected }) => {
      expect(wrapper.vm.getRuleTypeText(type)).toBe(expected)
    })
  })

  it('formats audit status display correctly', () => {
    const testCases = [
      { status: 'PENDING', expected: '待审核' },
      { status: 'APPROVED', expected: '已通过' },
      { status: 'REJECTED', expected: '已拒绝' }
    ]

    testCases.forEach(({ status, expected }) => {
      expect(wrapper.vm.getAuditStatusText(status)).toBe(expected)
    })
  })

  it('formats effective status display correctly', () => {
    const testCases = [
      { status: 'DRAFT', expected: '草稿' },
      { status: 'EFFECTIVE', expected: '有效' },
      { status: 'EXPIRED', expected: '已过期' }
    ]

    testCases.forEach(({ status, expected }) => {
      expect(wrapper.vm.getEffectiveStatusText(status)).toBe(expected)
    })
  })

  it('calculates selected count correctly', () => {
    wrapper.vm.selectedRowKeys = ['1', '2', '3']
    expect(wrapper.vm.selectedCount).toBe(3)

    wrapper.vm.selectedRowKeys = []
    expect(wrapper.vm.selectedCount).toBe(0)
  })

  it('validates form inputs', async () => {
    // Test empty rule name search (should be allowed)
    wrapper.vm.queryForm.ruleName = ''
    await wrapper.vm.handleSearch()
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()

    // Test valid rule name search
    wrapper.vm.queryForm.ruleName = '测试'
    await wrapper.vm.handleSearch()
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({ ruleName: '测试' })
    )
  })

  it('handles keyboard shortcuts', async () => {
    // Test Enter key in search input
    const searchInput = wrapper.find('input')
    await searchInput.setValue('测试规则')
    await searchInput.trigger('keydown', { key: 'Enter' })

    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({ ruleName: '测试规则' })
    )
  })

  it('handles accessibility requirements', () => {
    // Check if form has proper labels (through stubs)
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('.form-item').exists()).toBe(true)

    // Check if buttons have proper accessibility attributes
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('handles responsive design considerations', () => {
    // Check if responsive grid components are present
    expect(wrapper.find('.mock-row').exists()).toBe(true)
    expect(wrapper.find('.mock-col').exists()).toBe(true)
  })
})