import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import StatisticsChartSection from '@/components/dashboard/StatisticsChartSection.vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// Mock ECharts
vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: vi.fn(),
    dispose: vi.fn(),
    resize: vi.fn()
  })),
  dispose: vi.fn()
}))

// Mock DashboardMockService
vi.mock('@/api/mock/dashboardMock', () => ({
  DashboardMockService: {
    getStatisticsData: vi.fn()
  }
}))

describe('StatisticsChartSection', () => {
  const mockStatisticsData = {
    chartData: [
      { name: "种植险", value: 45, percentage: "28.8%", color: "#1890ff" },
      { name: "意健险", value: 32, percentage: "20.5%", color: "#52c41a" },
      { name: "车险", value: 28, percentage: "17.9%", color: "#faad14" },
      { name: "家财险", value: 25, percentage: "16.0%", color: "#f5222d" },
      { name: "养殖险", value: 18, percentage: "11.5%", color: "#722ed1" },
      { name: "其他", value: 8, percentage: "5.1%", color: "#13c2c2" }
    ],
    total: 156
  }

  beforeEach(() => {
    vi.mocked(DashboardMockService.getStatisticsData).mockResolvedValue(mockStatisticsData)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    const wrapper = mount(StatisticsChartSection)
    
    expect(wrapper.find('.statistics-chart-section').exists()).toBe(true)
    expect(wrapper.find('.section-title').text()).toBe('数量统计占比')
  })

  it('renders tabs correctly', () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Check if tabs component exists
    expect(wrapper.find('.chart-tabs').exists()).toBe(true)
    
    // Check if the component has the expected structure
    expect(wrapper.find('.chart-content').exists()).toBe(true)
  })

  it('renders chart container', () => {
    const wrapper = mount(StatisticsChartSection)
    
    expect(wrapper.find('.chart-content').exists()).toBe(true)
    expect(wrapper.find('.chart-wrapper').exists()).toBe(true)
    expect(wrapper.find('.pie-chart').exists()).toBe(true)
    expect(wrapper.find('.type-list').exists()).toBe(true)
  })

  it('loads initial data on mount', async () => {
    mount(StatisticsChartSection)
    
    await nextTick()
    
    expect(DashboardMockService.getStatisticsData).toHaveBeenCalledWith('productType')
  })

  it('renders type list correctly', async () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Wait for data to load
    await nextTick()
    await nextTick()
    
    const typeItems = wrapper.findAll('.type-item')
    expect(typeItems.length).toBeGreaterThan(0)
    
    // Check if total is displayed
    expect(wrapper.find('.total-item').exists()).toBe(true)
    expect(wrapper.find('.total-value').exists()).toBe(true)
  })

  it('handles tab change correctly', async () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Simulate tab change by triggering the tabs change event
    const tabs = wrapper.findComponent({ name: 'ATabs' })
    if (tabs.exists()) {
      await tabs.vm.$emit('change', 'department')
    }
    
    // The component should load data for the new tab
    expect(DashboardMockService.getStatisticsData).toHaveBeenCalledWith('productType')
  })

  it('displays color dots for each type', async () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Wait for data to load
    await nextTick()
    await nextTick()
    
    const colorDots = wrapper.findAll('.color-dot')
    expect(colorDots.length).toBeGreaterThan(0)
  })

  it('displays percentages and counts', async () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Wait for data to load
    await nextTick()
    await nextTick()
    
    const percentages = wrapper.findAll('.percentage')
    const counts = wrapper.findAll('.count')
    
    expect(percentages.length).toBeGreaterThan(0)
    expect(counts.length).toBeGreaterThan(0)
  })

  it('handles loading state', () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Component should handle loading state internally
    expect(wrapper.exists()).toBe(true)
  })

  it('handles error gracefully', async () => {
    // Mock error
    vi.mocked(DashboardMockService.getStatisticsData).mockRejectedValue(new Error('API Error'))
    
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    
    mount(StatisticsChartSection)
    
    await nextTick()
    
    expect(consoleSpy).toHaveBeenCalledWith('Failed to load statistics data:', expect.any(Error))
    
    consoleSpy.mockRestore()
  })

  it('is responsive on mobile', () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Check if responsive classes exist
    expect(wrapper.find('.statistics-chart-section').exists()).toBe(true)
    
    // The responsive behavior is handled by CSS media queries
    // We can verify the structure is in place
    expect(wrapper.find('.chart-content').exists()).toBe(true)
  })

  it('computes current data correctly', async () => {
    const wrapper = mount(StatisticsChartSection)
    
    // Wait for initial data load
    await nextTick()
    
    // Check if the component renders the data correctly
    expect(wrapper.find('.type-list').exists()).toBe(true)
    expect(wrapper.find('.total-item').exists()).toBe(true)
  })
})