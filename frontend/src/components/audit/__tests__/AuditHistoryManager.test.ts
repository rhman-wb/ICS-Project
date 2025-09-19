import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import AuditHistoryManager from '../AuditHistoryManager.vue'

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

// Mock audit API
const mockAuditApi = {
  getAuditHistory: vi.fn(),
  deleteAuditRecord: vi.fn(),
  downloadAuditReport: vi.fn()
}

vi.mock('@/api/modules/audit', () => mockAuditApi)

describe('AuditHistoryManager', () => {
  let wrapper: VueWrapper

  const mockHistoryData = [
    {
      id: 'history-1',
      taskId: 'task-1',
      productName: '终身寿险产品A',
      productId: 'product-1',
      ruleCount: 15,
      status: 'completed',
      startTime: '2024-01-01T10:00:00Z',
      endTime: '2024-01-01T11:30:00Z',
      duration: 5400, // 90 minutes in seconds
      totalIssues: 8,
      highSeverityIssues: 2,
      mediumSeverityIssues: 4,
      lowSeverityIssues: 2,
      operator: '张三',
      reportUrl: '/reports/audit-report-1.pdf'
    },
    {
      id: 'history-2',
      taskId: 'task-2',
      productName: '意外险产品B',
      productId: 'product-2',
      ruleCount: 12,
      status: 'failed',
      startTime: '2024-01-02T14:00:00Z',
      endTime: '2024-01-02T14:15:00Z',
      duration: 900, // 15 minutes in seconds
      totalIssues: 0,
      highSeverityIssues: 0,
      mediumSeverityIssues: 0,
      lowSeverityIssues: 0,
      operator: '李四',
      reportUrl: null,
      errorMessage: '检核过程中发生系统错误'
    }
  ]

  beforeEach(() => {
    setActivePinia(createPinia())

    mockAuditApi.getAuditHistory.mockResolvedValue({
      data: mockHistoryData,
      total: 2,
      page: 1,
      pageSize: 10
    })

    wrapper = mount(AuditHistoryManager, {
      props: {
        productId: 'product-1',
        showActions: true
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          'a-table': {
            template: '<table class="history-table" :data-loading="loading"><slot /></table>',
            props: ['dataSource', 'columns', 'pagination', 'loading', 'rowKey']
          },
          'a-form': {
            template: '<form><slot /></form>'
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
          'a-date-picker': {
            template: '<input type="date" v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-range-picker': {
            template: '<div class="range-picker"><input type="date" /><input type="date" /></div>',
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
          'a-card': {
            template: '<div class="card"><slot /></div>'
          },
          'a-tag': {
            template: '<span class="tag" :data-color="color"><slot /></span>',
            props: ['color']
          },
          'a-popconfirm': {
            template: '<div class="popconfirm" @click="$emit(\'confirm\')"><slot /></div>',
            props: ['title']
          },
          'a-tooltip': {
            template: '<div class="tooltip"><slot /></div>',
            props: ['title']
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with history data', async () => {
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.audit-history-manager').exists()).toBe(true)
    expect(wrapper.find('.search-form').exists()).toBe(true)
    expect(wrapper.find('.history-table').exists()).toBe(true)
  })

  it('loads history data on mount', () => {
    expect(mockAuditApi.getAuditHistory).toHaveBeenCalled()
  })

  it('displays search form with correct fields', () => {
    const searchForm = wrapper.find('.search-form')
    expect(searchForm.exists()).toBe(true)

    const productNameInput = wrapper.find('input[data-testid="product-name-input"]')
    const statusSelect = wrapper.find('select[data-testid="status-select"]')
    const dateRangePicker = wrapper.find('.range-picker')

    expect(productNameInput.exists()).toBe(true)
    expect(statusSelect.exists()).toBe(true)
    expect(dateRangePicker.exists()).toBe(true)
  })

  it('filters history by product name', async () => {
    const productNameInput = wrapper.find('input[data-testid="product-name-input"]')

    await productNameInput.setValue('终身寿险')
    await productNameInput.trigger('input')

    const searchButton = wrapper.find('button[data-testid="search-button"]')
    await searchButton.trigger('click')

    expect(mockAuditApi.getAuditHistory).toHaveBeenCalledWith(
      expect.objectContaining({
        productName: '终身寿险'
      })
    )
  })

  it('filters history by status', async () => {
    const statusSelect = wrapper.find('select[data-testid="status-select"]')

    await statusSelect.setValue('completed')
    await statusSelect.trigger('change')

    const searchButton = wrapper.find('button[data-testid="search-button"]')
    await searchButton.trigger('click')

    expect(mockAuditApi.getAuditHistory).toHaveBeenCalledWith(
      expect.objectContaining({
        status: 'completed'
      })
    )
  })

  it('resets search form when reset button is clicked', async () => {
    // Fill form with values
    const productNameInput = wrapper.find('input[data-testid="product-name-input"]')
    await productNameInput.setValue('test product')

    const resetButton = wrapper.find('button[data-testid="reset-button"]')
    await resetButton.trigger('click')

    expect(wrapper.vm.searchForm.productName).toBe('')
    expect(wrapper.vm.searchForm.status).toBe('')
  })

  it('displays history records in table', async () => {
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.history-table')
    expect(table.exists()).toBe(true)

    // Check if history data is passed to table
    expect(table.props('dataSource')).toEqual(mockHistoryData)
  })

  it('shows correct status tags for different record states', async () => {
    await wrapper.vm.$nextTick()

    const statusTags = wrapper.findAll('.tag')

    // Should show different colors for different statuses
    const completedTag = statusTags.find(tag =>
      tag.attributes('data-color') === 'green'
    )
    const failedTag = statusTags.find(tag =>
      tag.attributes('data-color') === 'red'
    )

    expect(completedTag).toBeTruthy()
    expect(failedTag).toBeTruthy()
  })

  it('handles view history action', async () => {
    const historyRecord = mockHistoryData[0]

    await wrapper.vm.handleViewHistory(historyRecord.id)

    expect(wrapper.emitted('view-history')).toBeTruthy()
    expect(wrapper.emitted('view-history')[0]).toEqual([historyRecord.id])
  })

  it('handles delete record action', async () => {
    mockAuditApi.deleteAuditRecord.mockResolvedValue({ success: true })

    const historyRecord = mockHistoryData[0]
    const deleteButton = wrapper.find('button[data-testid="delete-button"]')

    await deleteButton.trigger('click')

    // Simulate confirmation
    const popconfirm = wrapper.find('.popconfirm')
    await popconfirm.trigger('confirm')

    expect(mockAuditApi.deleteAuditRecord).toHaveBeenCalledWith(historyRecord.id)
    expect(message.success).toHaveBeenCalledWith('删除成功')
  })

  it('handles download report action', async () => {
    const historyRecord = mockHistoryData[0]

    await wrapper.vm.handleDownloadReport(historyRecord.reportUrl)

    expect(wrapper.emitted('download-report')).toBeTruthy()
    expect(wrapper.emitted('download-report')[0]).toEqual([historyRecord.reportUrl])
  })

  it('shows loading state when fetching data', async () => {
    wrapper.vm.loading = true
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.history-table')
    expect(table.attributes('data-loading')).toBe('true')
  })

  it('displays pagination when there are many records', async () => {
    // Mock large dataset
    mockAuditApi.getAuditHistory.mockResolvedValue({
      data: mockHistoryData,
      total: 100,
      page: 1,
      pageSize: 10
    })

    await wrapper.vm.loadHistory()
    await wrapper.vm.$nextTick()

    const table = wrapper.find('.history-table')
    expect(table.props('pagination')).toBeTruthy()
  })

  it('handles pagination change', async () => {
    await wrapper.vm.handlePageChange(2, 20)

    expect(mockAuditApi.getAuditHistory).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 2,
        pageSize: 20
      })
    )
  })

  it('shows empty state when no history records', async () => {
    mockAuditApi.getAuditHistory.mockResolvedValue({
      data: [],
      total: 0,
      page: 1,
      pageSize: 10
    })

    await wrapper.vm.loadHistory()
    await wrapper.vm.$nextTick()

    const emptyState = wrapper.find('.empty-history')
    expect(emptyState.exists()).toBe(true)
    expect(emptyState.text()).toContain('暂无检核历史记录')
  })

  it('formats duration correctly', () => {
    const duration = wrapper.vm.formatDuration(5400) // 90 minutes
    expect(duration).toBe('1小时30分钟')

    const shortDuration = wrapper.vm.formatDuration(900) // 15 minutes
    expect(shortDuration).toBe('15分钟')
  })

  it('formats date correctly', () => {
    const date = wrapper.vm.formatDate('2024-01-01T10:00:00Z')
    expect(date).toMatch(/2024-01-01 \d{2}:\d{2}/)
  })

  it('disables actions for failed records appropriately', async () => {
    await wrapper.vm.$nextTick()

    // Failed records should not have download button
    const failedRecord = mockHistoryData[1]
    expect(failedRecord.status).toBe('failed')
    expect(failedRecord.reportUrl).toBeNull()

    // Download button should be disabled or hidden for failed records
    const downloadButtons = wrapper.findAll('button[data-testid="download-button"]')
    expect(downloadButtons.length).toBeLessThan(mockHistoryData.length)
  })

  it('shows error message for failed records', async () => {
    await wrapper.vm.$nextTick()

    const failedRecord = mockHistoryData[1]
    expect(wrapper.text()).toContain(failedRecord.errorMessage)
  })

  it('emits search event when performing search', async () => {
    const searchForm = {
      productName: '终身寿险',
      status: 'completed',
      dateRange: ['2024-01-01', '2024-01-31']
    }

    await wrapper.vm.handleSearch(searchForm)

    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual([searchForm])
  })

  it('supports batch operations when multiple records selected', async () => {
    // Mock row selection
    wrapper.vm.selectedRowKeys = ['history-1', 'history-2']
    await wrapper.vm.$nextTick()

    const batchDeleteButton = wrapper.find('button[data-testid="batch-delete-button"]')
    expect(batchDeleteButton.exists()).toBe(true)
    expect(batchDeleteButton.attributes('disabled')).toBeUndefined()
  })

  it('handles API errors gracefully', async () => {
    mockAuditApi.getAuditHistory.mockRejectedValue(new Error('Network error'))

    await wrapper.vm.loadHistory()

    expect(message.error).toHaveBeenCalledWith('加载历史记录失败')
  })
})