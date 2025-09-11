import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import SummaryDataSection from '@/components/dashboard/SummaryDataSection.vue'

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      error: vi.fn(),
      success: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('SummaryDataSection', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(SummaryDataSection, {
      global: {
        stubs: {
          'a-tabs': true,
          'a-tab-pane': true,
          'a-row': true,
          'a-col': true,
          'a-card': true
        }
      }
    })
  })

  it('renders correctly', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.summary-data-section').exists()).toBe(true)
  })

  it('displays section title', () => {
    expect(wrapper.find('.section-title').text()).toBe('汇总数据')
  })

  it('has correct initial state', () => {
    expect(wrapper.vm.activeTimeFilter).toBe('total')
    expect(wrapper.vm.summaryData).toHaveLength(4)
    // Note: loading might be true initially due to onMounted fetchSummaryData call
  })

  it('has correct summary data structure', () => {
    const expectedData = [
      { key: 'products', label: '产品数量', value: 0 },
      { key: 'documents', label: '要件数量', value: 0 },
      { key: 'rules', label: '规则数量', value: 0 },
      { key: 'errors', label: '已检核错误数量', value: 0 }
    ]
    
    expectedData.forEach((expected, index) => {
      expect(wrapper.vm.summaryData[index].key).toBe(expected.key)
      expect(wrapper.vm.summaryData[index].label).toBe(expected.label)
    })
  })

  it('formats numbers correctly', () => {
    expect(wrapper.vm.formatNumber(123)).toBe('123')
    expect(wrapper.vm.formatNumber(1234)).toBe('1.2k')
    expect(wrapper.vm.formatNumber(12345)).toBe('1.2万')
  })

  it('handles time filter change', async () => {
    // Test the method directly
    wrapper.vm.handleTimeFilterChange('today')
    
    expect(wrapper.vm.activeTimeFilter).toBe('today')
    // The fetchSummaryData will be called due to the watch effect
  })
})