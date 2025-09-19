import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import AuditResultDisplay from '../AuditResultDisplay.vue'

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

// Mock useAuditResults composable
const mockUseAuditResults = {
  state: {
    taskId: 'task-123',
    results: [
      {
        id: 'result-1',
        documentType: '保险条款',
        fileName: 'policy_terms.pdf',
        ruleId: 'CC001',
        ruleName: '产品基本信息检核',
        severity: 'high',
        status: 'failed',
        message: '缺少必要的产品代码信息',
        details: '在第3页未找到产品代码字段',
        lineNumber: 15,
        suggestion: '请在第3页添加产品代码信息'
      },
      {
        id: 'result-2',
        documentType: '保险条款',
        fileName: 'policy_terms.pdf',
        ruleId: 'CC002',
        ruleName: '保险条款检核',
        severity: 'medium',
        status: 'warning',
        message: '条款格式不规范',
        details: '第5页条款编号格式不一致',
        lineNumber: 25,
        suggestion: '统一条款编号格式'
      }
    ],
    loading: false,
    total: 2
  },
  overallStatistics: {
    totalDocuments: 5,
    passedDocuments: 3,
    failedDocuments: 1,
    warningDocuments: 1,
    totalIssues: 12,
    highSeverityIssues: 3,
    mediumSeverityIssues: 6,
    lowSeverityIssues: 3
  },
  documentTypeStats: [
    { type: '保险条款', total: 2, passed: 1, failed: 1, warnings: 1 },
    { type: '产品说明书', total: 2, passed: 2, failed: 0, warnings: 0 },
    { type: '费率表', total: 1, passed: 0, failed: 0, warnings: 1 }
  ],
  severityStats: {
    high: 3,
    medium: 6,
    low: 3
  },
  filterForm: {
    documentType: '',
    severity: '',
    status: '',
    keyword: ''
  },
  loadResults: vi.fn(),
  exportResults: vi.fn(),
  applyFilters: vi.fn(),
  resetFilters: vi.fn(),
  viewDetail: vi.fn()
}

vi.mock('@/composables/audit/useAuditResults', () => ({
  useAuditResults: () => mockUseAuditResults
}))

describe('AuditResultDisplay', () => {
  let wrapper: VueWrapper

  beforeEach(() => {
    setActivePinia(createPinia())

    wrapper = mount(AuditResultDisplay, {
      props: {
        taskId: 'task-123',
        showExport: true
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-card': {
            template: '<div class="card"><slot /></div>'
          },
          'a-statistic': {
            template: '<div class="statistic" :data-value="value" :data-title="title"><slot /></div>',
            props: ['value', 'title', 'valueStyle']
          },
          'a-table': {
            template: '<table class="results-table" :data-loading="loading"><slot /></table>',
            props: ['dataSource', 'columns', 'pagination', 'loading', 'rowKey']
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
          'a-button': {
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'type', 'danger']
          },
          'a-space': {
            template: '<div class="space"><slot /></div>'
          },
          'a-row': {
            template: '<div class="row"><slot /></div>'
          },
          'a-col': {
            template: '<div class="col"><slot /></div>'
          },
          'a-tag': {
            template: '<span class="tag" :data-color="color"><slot /></span>',
            props: ['color']
          },
          'a-tabs': {
            template: '<div class="tabs"><slot /></div>'
          },
          'a-tab-pane': {
            template: '<div class="tab-pane" :data-key="key"><slot /></div>',
            props: ['key', 'tab']
          },
          'a-dropdown': {
            template: '<div class="dropdown"><slot /></div>'
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with result data', () => {
    expect(wrapper.find('.audit-result-display').exists()).toBe(true)
    expect(wrapper.find('.statistics-section').exists()).toBe(true)
    expect(wrapper.find('.results-table').exists()).toBe(true)
  })

  it('displays overall statistics correctly', () => {
    const statistics = wrapper.findAll('.statistic')

    // Should show total documents, passed, failed, etc.
    const totalDocsStats = statistics.find(stat =>
      stat.attributes('data-value') === '5'
    )
    expect(totalDocsStats).toBeTruthy()

    const passedDocsStats = statistics.find(stat =>
      stat.attributes('data-value') === '3'
    )
    expect(passedDocsStats).toBeTruthy()
  })

  it('shows document type breakdown', () => {
    const documentTypeSection = wrapper.find('.document-type-stats')
    expect(documentTypeSection.exists()).toBe(true)

    // Should display stats for different document types
    expect(documentTypeSection.text()).toContain('保险条款')
    expect(documentTypeSection.text()).toContain('产品说明书')
    expect(documentTypeSection.text()).toContain('费率表')
  })

  it('displays severity distribution', () => {
    const severitySection = wrapper.find('.severity-stats')
    expect(severitySection.exists()).toBe(true)

    // Should show high, medium, low severity counts
    expect(severitySection.text()).toContain('3') // high severity
    expect(severitySection.text()).toContain('6') // medium severity
    expect(severitySection.text()).toContain('3') // low severity
  })

  it('loads results on mount', () => {
    expect(mockUseAuditResults.loadResults).toHaveBeenCalledWith('task-123')
  })

  it('shows filter form with correct options', () => {
    const filterForm = wrapper.find('.filter-form')
    expect(filterForm.exists()).toBe(true)

    const documentTypeSelect = wrapper.find('select[data-testid="document-type-filter"]')
    const severitySelect = wrapper.find('select[data-testid="severity-filter"]')
    const statusSelect = wrapper.find('select[data-testid="status-filter"]')
    const keywordInput = wrapper.find('input[data-testid="keyword-filter"]')

    expect(documentTypeSelect.exists()).toBe(true)
    expect(severitySelect.exists()).toBe(true)
    expect(statusSelect.exists()).toBe(true)
    expect(keywordInput.exists()).toBe(true)
  })

  it('applies filters when form values change', async () => {
    const severitySelect = wrapper.find('select[data-testid="severity-filter"]')

    await severitySelect.setValue('high')
    await severitySelect.trigger('change')

    expect(mockUseAuditResults.applyFilters).toHaveBeenCalled()
  })

  it('resets filters when reset button is clicked', async () => {
    const resetButton = wrapper.find('button[data-testid="reset-filters-button"]')
    await resetButton.trigger('click')

    expect(mockUseAuditResults.resetFilters).toHaveBeenCalled()
  })

  it('shows export functionality when enabled', () => {
    const exportButton = wrapper.find('button[data-testid="export-button"]')
    expect(exportButton.exists()).toBe(true)
  })

  it('handles export action correctly', async () => {
    const exportButton = wrapper.find('button[data-testid="export-button"]')
    await exportButton.trigger('click')

    // Should show export options dropdown or call export directly
    expect(mockUseAuditResults.exportResults).toHaveBeenCalled()
  })

  it('displays results in table format', () => {
    const table = wrapper.find('.results-table')
    expect(table.exists()).toBe(true)

    // Check if results are passed to table
    expect(table.props('dataSource')).toEqual(mockUseAuditResults.state.results)
  })

  it('shows loading state when loading results', async () => {
    mockUseAuditResults.state.loading = true
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.results-table')
    expect(table.attributes('data-loading')).toBe('true')
  })

  it('handles view detail action', async () => {
    const result = mockUseAuditResults.state.results[0]

    // Simulate clicking on a result row
    await wrapper.vm.handleViewDetail(result)

    expect(mockUseAuditResults.viewDetail).toHaveBeenCalledWith(result.id)
    expect(wrapper.emitted('view-detail')).toBeTruthy()
  })

  it('displays severity tags with correct colors', () => {
    const severityTags = wrapper.findAll('.tag')

    // Should have tags for different severities
    const highSeverityTag = severityTags.find(tag =>
      tag.attributes('data-color') === 'red'
    )
    const mediumSeverityTag = severityTags.find(tag =>
      tag.attributes('data-color') === 'orange'
    )

    expect(highSeverityTag).toBeTruthy()
    expect(mediumSeverityTag).toBeTruthy()
  })

  it('shows result count information', () => {
    const resultCount = wrapper.find('.result-count')
    expect(resultCount.exists()).toBe(true)
    expect(resultCount.text()).toContain('共 2 条结果')
  })

  it('emits export-results event when exporting', async () => {
    const exportButton = wrapper.find('button[data-testid="export-button"]')
    await exportButton.trigger('click')

    expect(wrapper.emitted('export-results')).toBeTruthy()
    expect(wrapper.emitted('export-results')[0]).toEqual(['excel'])
  })

  it('emits filter-change event when filters are applied', async () => {
    const documentTypeSelect = wrapper.find('select[data-testid="document-type-filter"]')

    await documentTypeSelect.setValue('保险条款')
    await documentTypeSelect.trigger('change')

    expect(wrapper.emitted('filter-change')).toBeTruthy()
  })

  it('handles empty results state', async () => {
    mockUseAuditResults.state.results = []
    mockUseAuditResults.state.total = 0
    await wrapper.vm.$nextTick()

    const emptyState = wrapper.find('.empty-results')
    expect(emptyState.exists()).toBe(true)
    expect(emptyState.text()).toContain('暂无检核结果')
  })

  it('displays result details correctly', () => {
    const firstResult = mockUseAuditResults.state.results[0]

    // Should display file name, rule name, message, etc.
    expect(wrapper.text()).toContain(firstResult.fileName)
    expect(wrapper.text()).toContain(firstResult.ruleName)
    expect(wrapper.text()).toContain(firstResult.message)
  })

  it('supports pagination when many results', async () => {
    // Mock large result set
    mockUseAuditResults.state.total = 100
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.results-table')
    expect(table.props('pagination')).toBeTruthy()
  })

  it('handles tab switching between views', async () => {
    const tabs = wrapper.find('.tabs')
    expect(tabs.exists()).toBe(true)

    // Should have tabs for different views (overview, details, etc.)
    const tabPanes = wrapper.findAll('.tab-pane')
    expect(tabPanes.length).toBeGreaterThan(0)
  })
})