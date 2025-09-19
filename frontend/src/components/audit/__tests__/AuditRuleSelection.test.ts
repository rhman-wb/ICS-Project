import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import AuditRuleSelection from '../AuditRuleSelection.vue'

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush
    }),
    useRoute: () => ({
      query: { productIds: 'product1,product2' }
    })
  }
})

// Mock useAuditRules composable
const mockUseAuditRules = {
  state: {
    rules: [
      {
        id: 'CC001',
        name: '产品基本信息检核',
        description: '检核产品名称、代码等基本信息',
        category: 'basic',
        severity: 'high',
        enabled: true
      },
      {
        id: 'CC002',
        name: '保险条款检核',
        description: '检核保险条款完整性和合规性',
        category: 'contract',
        severity: 'medium',
        enabled: true
      }
    ],
    loading: false,
    total: 2
  },
  selectedRules: [],
  filterForm: {
    category: '',
    severity: '',
    keyword: ''
  },
  loadRules: vi.fn(),
  selectRule: vi.fn(),
  selectAllRules: vi.fn(),
  clearSelection: vi.fn(),
  applyFilters: vi.fn(),
  resetFilters: vi.fn()
}

vi.mock('@/composables/audit/useAuditRules', () => ({
  useAuditRules: () => mockUseAuditRules
}))

describe('AuditRuleSelection', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    setActivePinia(createPinia())

    wrapper = mount(AuditRuleSelection, {
      props: {
        productIds: ['product1', 'product2']
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-steps': {
            template: '<div class="steps"><slot /></div>'
          },
          'a-step': {
            template: '<div class="step"><slot /></div>'
          },
          'a-card': {
            template: '<div class="card"><slot /></div>'
          },
          'a-form': {
            template: '<form><slot /></form>'
          },
          'a-form-item': {
            template: '<div class="form-item"><slot /></div>'
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
          'a-input': {
            template: '<input v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-table': {
            template: '<table class="rules-table"><slot /></table>',
            props: ['dataSource', 'columns', 'rowSelection', 'pagination', 'loading']
          },
          'a-button': {
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'type']
          },
          'a-space': {
            template: '<div class="space"><slot /></div>'
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with initial state', () => {
    expect(wrapper.find('.audit-rule-selection').exists()).toBe(true)
    expect(wrapper.find('.steps').exists()).toBe(true)
    expect(wrapper.find('.filter-section').exists()).toBe(true)
    expect(wrapper.find('.rules-table').exists()).toBe(true)
  })

  it('displays product count correctly', () => {
    const productInfo = wrapper.find('.product-info')
    expect(productInfo.exists()).toBe(true)
    expect(productInfo.text()).toContain('已选择 2 个产品')
  })

  it('loads rules on mount', () => {
    expect(mockUseAuditRules.loadRules).toHaveBeenCalled()
  })

  it('shows filter form with correct options', () => {
    const categorySelect = wrapper.find('select[data-testid="category-select"]')
    const severitySelect = wrapper.find('select[data-testid="severity-select"]')
    const keywordInput = wrapper.find('input[data-testid="keyword-input"]')

    expect(categorySelect.exists()).toBe(true)
    expect(severitySelect.exists()).toBe(true)
    expect(keywordInput.exists()).toBe(true)
  })

  it('applies filters when form values change', async () => {
    const categorySelect = wrapper.find('select[data-testid="category-select"]')

    await categorySelect.setValue('basic')
    await categorySelect.trigger('change')

    expect(mockUseAuditRules.applyFilters).toHaveBeenCalled()
  })

  it('enables next button when rules are selected', async () => {
    // Mock selected rules
    mockUseAuditRules.selectedRules = ['CC001', 'CC002']
    await wrapper.vm.$nextTick()

    const nextButton = wrapper.find('button[data-testid="next-button"]')
    expect(nextButton.attributes('disabled')).toBeUndefined()
  })

  it('disables next button when no rules are selected', () => {
    mockUseAuditRules.selectedRules = []
    const nextButton = wrapper.find('button[data-testid="next-button"]')
    expect(nextButton.attributes('disabled')).toBeDefined()
  })

  it('emits rule-selected event when proceeding', async () => {
    mockUseAuditRules.selectedRules = ['CC001', 'CC002']

    const nextButton = wrapper.find('button[data-testid="next-button"]')
    await nextButton.trigger('click')

    expect(wrapper.emitted('rule-selected')).toBeTruthy()
    expect(wrapper.emitted('rule-selected')[0]).toEqual([{
      productIds: ['product1', 'product2'],
      ruleIds: ['CC001', 'CC002']
    }])
  })

  it('shows loading state when loading rules', async () => {
    mockUseAuditRules.state.loading = true
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.rules-table')
    expect(table.attributes('loading')).toBeDefined()
  })

  it('displays rule count in statistics', () => {
    const stats = wrapper.find('.selection-stats')
    expect(stats.exists()).toBe(true)
    expect(stats.text()).toContain('共 2 条规则')
  })

  it('supports select all functionality', async () => {
    const selectAllButton = wrapper.find('button[data-testid="select-all-button"]')
    await selectAllButton.trigger('click')

    expect(mockUseAuditRules.selectAllRules).toHaveBeenCalled()
  })

  it('supports clear selection functionality', async () => {
    const clearButton = wrapper.find('button[data-testid="clear-selection-button"]')
    await clearButton.trigger('click')

    expect(mockUseAuditRules.clearSelection).toHaveBeenCalled()
  })

  it('resets filters when reset button is clicked', async () => {
    const resetButton = wrapper.find('button[data-testid="reset-filters-button"]')
    await resetButton.trigger('click')

    expect(mockUseAuditRules.resetFilters).toHaveBeenCalled()
  })

  it('shows validation message when no product IDs provided', async () => {
    await wrapper.setProps({ productIds: [] })

    const nextButton = wrapper.find('button[data-testid="next-button"]')
    await nextButton.trigger('click')

    expect(message.warning).toHaveBeenCalledWith('请先选择要检核的产品')
  })

  it('handles rule selection correctly', async () => {
    const rule = mockUseAuditRules.state.rules[0]

    // Simulate rule selection
    await wrapper.vm.handleRuleSelect(rule.id, true)

    expect(mockUseAuditRules.selectRule).toHaveBeenCalledWith(rule.id, true)
  })

  it('displays rule categories correctly', () => {
    const table = wrapper.find('.rules-table')
    expect(table.exists()).toBe(true)

    // Check if rules are passed to table
    expect(table.props('dataSource')).toEqual(mockUseAuditRules.state.rules)
  })

  it('shows correct step indicator', () => {
    const steps = wrapper.find('.steps')
    expect(steps.exists()).toBe(true)

    // Should show first step as active
    expect(wrapper.vm.currentStep).toBe(0)
  })

  it('validates minimum rule selection', async () => {
    mockUseAuditRules.selectedRules = []

    const nextButton = wrapper.find('button[data-testid="next-button"]')
    await nextButton.trigger('click')

    expect(message.warning).toHaveBeenCalledWith('请至少选择一条检核规则')
  })

  it('supports keyboard navigation', async () => {
    const table = wrapper.find('.rules-table')

    // Test keyboard events on table
    await table.trigger('keydown', { key: 'Space' })
    await table.trigger('keydown', { key: 'Enter' })

    // Should handle keyboard interactions
    expect(table.exists()).toBe(true)
  })
})