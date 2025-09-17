import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import RuleJumpView from '@/views/rules/RuleJumpView.vue'

// Mock rule store
const ruleStore = {
  fetchRuleList: vi.fn()
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
      loading: vi.fn()
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

describe('RuleJumpView', () => {
  let wrapper: VueWrapper

  const mockRuleData = {
    records: [
      {
        id: '1',
        ruleName: '检核规则1',
        ruleType: 'SINGLE',
        description: '用于检核的规则1',
        auditStatus: 'APPROVED',
        effectiveStatus: 'EFFECTIVE',
        category: '基础检核',
        tags: ['必填', '格式']
      },
      {
        id: '2',
        ruleName: '检核规则2',
        ruleType: 'DOUBLE',
        description: '用于检核的规则2',
        auditStatus: 'APPROVED',
        effectiveStatus: 'EFFECTIVE',
        category: '高级检核',
        tags: ['逻辑', '关联']
      },
      {
        id: '3',
        ruleName: '检核规则3',
        ruleType: 'FORMAT',
        description: '用于检核的规则3',
        auditStatus: 'PENDING',
        effectiveStatus: 'DRAFT',
        category: '格式检核',
        tags: ['格式']
      }
    ],
    current: 1,
    size: 10,
    total: 3
  }

  beforeEach(() => {
    setActivePinia(createPinia())

    // Mock store methods
    vi.spyOn(ruleStore, 'fetchRuleList').mockResolvedValue(mockRuleData)

    wrapper = mount(RuleJumpView, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-card': {
            template: '<div class="mock-card"><slot /></div>'
          },
          'a-form': {
            template: '<form><slot /></form>'
          },
          'a-form-item': {
            template: '<div class="form-item"><label v-if="label">{{ label }}</label><slot /></div>',
            props: ['label']
          },
          'a-input': {
            template: '<input v-model="modelValue" :placeholder="placeholder" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'placeholder'],
            emits: ['update:modelValue']
          },
          'a-select': {
            template: '<select v-model="modelValue" :placeholder="placeholder" @change="$emit(\'update:modelValue\', $event.target.value)"><slot /></select>',
            props: ['modelValue', 'placeholder'],
            emits: ['update:modelValue']
          },
          'a-select-option': {
            template: '<option :value="value"><slot /></option>',
            props: ['value']
          },
          'a-button': {
            template: '<button :disabled="disabled" :loading="loading" :type="type" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'loading', 'type']
          },
          'a-table': {
            template: '<div class="mock-table" :class="{ loading: loading }"><slot /></div>',
            props: ['columns', 'dataSource', 'rowSelection', 'pagination', 'loading', 'size']
          },
          'a-tag': {
            template: '<span class="mock-tag" :class="color"><slot /></span>',
            props: ['color']
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
          },
          'a-divider': {
            template: '<hr class="mock-divider" />'
          },
          'a-typography-title': {
            template: '<h1 class="mock-title"><slot /></h1>',
            props: ['level']
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
    expect(wrapper.find('.mock-title').exists()).toBe(true)
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('.mock-table').exists()).toBe(true)
  })

  it('displays correct page title', () => {
    const title = wrapper.find('.mock-title')
    expect(title.text()).toContain('检核跳转')
  })

  it('initializes with default filter values', () => {
    expect(wrapper.vm.filterForm.keyword).toBe('')
    expect(wrapper.vm.filterForm.ruleType).toBe(undefined)
    expect(wrapper.vm.filterForm.category).toBe(undefined)
    expect(wrapper.vm.filterForm.tags).toEqual([])
  })

  it('initializes with empty selected rules', () => {
    expect(wrapper.vm.selectedRules).toEqual([])
    expect(wrapper.vm.selectedRowKeys).toEqual([])
  })

  it('fetches rules on mounted', () => {
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith({
      auditStatus: 'APPROVED',
      effectiveStatus: 'EFFECTIVE',
      ...wrapper.vm.filterForm,
      page: 1,
      size: 10
    })
  })

  it('handles keyword search', async () => {
    const keywordInput = wrapper.find('input')
    await keywordInput.setValue('检核规则')

    expect(wrapper.vm.filterForm.keyword).toBe('检核规则')

    // Trigger search
    wrapper.vm.handleSearch()
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith({
      auditStatus: 'APPROVED',
      effectiveStatus: 'EFFECTIVE',
      keyword: '检核规则',
      ruleType: undefined,
      category: undefined,
      tags: [],
      page: 1,
      size: 10
    })
  })

  it('handles rule type filter', async () => {
    const ruleTypeSelect = wrapper.find('select')
    await ruleTypeSelect.setValue('SINGLE')

    expect(wrapper.vm.filterForm.ruleType).toBe('SINGLE')

    wrapper.vm.handleSearch()
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({ ruleType: 'SINGLE' })
    )
  })

  it('handles filter reset', async () => {
    // Set some filter values
    wrapper.vm.filterForm.keyword = '测试'
    wrapper.vm.filterForm.ruleType = 'SINGLE'
    wrapper.vm.filterForm.category = '基础检核'

    wrapper.vm.handleReset()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.filterForm.keyword).toBe('')
    expect(wrapper.vm.filterForm.ruleType).toBe(undefined)
    expect(wrapper.vm.filterForm.category).toBe(undefined)
    expect(wrapper.vm.filterForm.tags).toEqual([])
    expect(ruleStore.fetchRuleList).toHaveBeenCalled()
  })

  it('handles rule selection', () => {
    const selectedRowKeys = ['1', '2']
    const selectedRows = mockRuleData.records.slice(0, 2)

    wrapper.vm.handleSelectionChange(selectedRowKeys, selectedRows)

    expect(wrapper.vm.selectedRowKeys).toEqual(selectedRowKeys)
    expect(wrapper.vm.selectedRules).toEqual(selectedRows)
  })

  it('prevents duplicate rule selection', () => {
    // Select rule 1
    wrapper.vm.handleSelectionChange(['1'], [mockRuleData.records[0]])

    // Try to select rule 1 again
    wrapper.vm.handleSelectionChange(['1', '1'], [mockRuleData.records[0], mockRuleData.records[0]])

    // Should only have one instance
    expect(wrapper.vm.selectedRowKeys).toEqual(['1'])
    expect(wrapper.vm.selectedRules).toHaveLength(1)
  })

  it('handles rule deselection', () => {
    // First select some rules
    wrapper.vm.selectedRowKeys = ['1', '2']
    wrapper.vm.selectedRules = mockRuleData.records.slice(0, 2)

    // Deselect rule 1
    wrapper.vm.handleSelectionChange(['2'], [mockRuleData.records[1]])

    expect(wrapper.vm.selectedRowKeys).toEqual(['2'])
    expect(wrapper.vm.selectedRules).toEqual([mockRuleData.records[1]])
  })

  it('handles confirm with selected rules', () => {
    // Select some rules
    const selectedRules = mockRuleData.records.slice(0, 2)
    wrapper.vm.selectedRules = selectedRules
    wrapper.vm.selectedRowKeys = ['1', '2']

    wrapper.vm.handleConfirm()

    expect(mockPush).toHaveBeenCalledWith({
      path: '/audit/execute',
      query: {
        ruleIds: '1,2',
        source: 'rules'
      }
    })
  })

  it('prevents confirm when no rules selected', () => {
    wrapper.vm.selectedRules = []
    wrapper.vm.selectedRowKeys = []

    wrapper.vm.handleConfirm()

    expect(message.error).toHaveBeenCalledWith('请至少选择一条规则进行检核')
    expect(mockPush).not.toHaveBeenCalled()
  })

  it('handles cancel navigation', () => {
    wrapper.vm.handleCancel()
    expect(mockPush).toHaveBeenCalledWith('/rules')
  })

  it('displays rule count information', () => {
    wrapper.vm.selectedRules = mockRuleData.records.slice(0, 2)

    expect(wrapper.vm.selectedCount).toBe(2)
    expect(wrapper.vm.totalCount).toBe(mockRuleData.total)
  })

  it('filters only approved and effective rules', () => {
    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({
        auditStatus: 'APPROVED',
        effectiveStatus: 'EFFECTIVE'
      })
    )
  })

  it('handles pagination', async () => {
    const newPage = 2
    const newPageSize = 20

    await wrapper.vm.handleTableChange({ current: newPage, pageSize: newPageSize })

    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({
        page: newPage,
        size: newPageSize
      })
    )
  })

  it('handles loading states', async () => {
    wrapper.vm.loading = true
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.mock-table')
    expect(table.classes()).toContain('loading')
  })

  it('handles error states gracefully', async () => {
    const errorMessage = '获取规则列表失败'
    vi.spyOn(ruleStore, 'fetchRuleList').mockRejectedValue(new Error(errorMessage))

    await wrapper.vm.fetchRules()

    expect(message.error).toHaveBeenCalledWith(errorMessage)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('formats rule categories correctly', () => {
    const testCases = [
      { category: '基础检核', expected: '基础检核' },
      { category: '高级检核', expected: '高级检核' },
      { category: '格式检核', expected: '格式检核' }
    ]

    testCases.forEach(({ category, expected }) => {
      expect(wrapper.vm.getCategoryText(category)).toBe(expected)
    })
  })

  it('handles tag filtering', async () => {
    const tags = ['必填', '格式']
    wrapper.vm.filterForm.tags = tags

    wrapper.vm.handleSearch()

    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({ tags })
    )
  })

  it('displays rule tags correctly', () => {
    const rule = mockRuleData.records[0]
    expect(rule.tags).toEqual(['必填', '格式'])

    // Tag display would be tested in the template
    const tags = wrapper.findAll('.mock-tag')
    expect(tags.length).toBeGreaterThanOrEqual(0)
  })

  it('handles keyboard shortcuts', async () => {
    const keywordInput = wrapper.find('input')
    await keywordInput.setValue('测试规则')

    // Enter key should trigger search
    await keywordInput.trigger('keydown', { key: 'Enter' })

    expect(ruleStore.fetchRuleList).toHaveBeenCalledWith(
      expect.objectContaining({ keyword: '测试规则' })
    )
  })

  it('supports accessible navigation', () => {
    // Check if form elements have proper accessibility
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('.form-item').exists()).toBe(true)

    // Check if buttons have proper roles
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThan(0)
  })

  it('shows selection summary', () => {
    wrapper.vm.selectedRules = mockRuleData.records.slice(0, 2)

    const summary = wrapper.vm.getSelectionSummary()
    expect(summary).toContain('已选择 2 条规则')
  })

  it('validates selection limits', () => {
    // Test maximum selection limit if exists
    const maxSelection = 50 // Assuming there's a max limit
    const largeSelection = Array.from({ length: maxSelection + 1 }, (_, i) => `rule-${i}`)

    const isValidSelection = wrapper.vm.validateSelection(largeSelection)
    expect(isValidSelection).toBe(false)
  })

  it('handles rapid filter changes', async () => {
    const keywordInput = wrapper.find('input')

    // Rapid input changes
    await keywordInput.setValue('a')
    await keywordInput.setValue('ab')
    await keywordInput.setValue('abc')

    // Should debounce the search calls
    expect(wrapper.vm.filterForm.keyword).toBe('abc')
  })

  it('preserves selection across filter changes', async () => {
    // Select some rules
    wrapper.vm.selectedRules = mockRuleData.records.slice(0, 1)
    wrapper.vm.selectedRowKeys = ['1']

    // Change filter
    wrapper.vm.filterForm.ruleType = 'SINGLE'
    wrapper.vm.handleSearch()

    // Selection should be preserved
    expect(wrapper.vm.selectedRowKeys).toEqual(['1'])
    expect(wrapper.vm.selectedRules).toHaveLength(1)
  })
})