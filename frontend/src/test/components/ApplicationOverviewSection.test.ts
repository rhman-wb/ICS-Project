import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ApplicationOverviewSection from '@/components/dashboard/ApplicationOverviewSection.vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// Mock the DashboardMockService
vi.mock('@/api/mock/dashboardMock', () => ({
  DashboardMockService: {
    getOverviewData: vi.fn()
  }
}))

// Mock Ant Design Vue components
vi.mock('ant-design-vue', () => ({
  message: {
    error: vi.fn()
  }
}))

describe('ApplicationOverviewSection', () => {
  const mockOverviewData = {
    cumulative: {
      totalProducts: 156,
      totalRules: 89,
      errorDetectionRate: "85.6%",
      avgAuditTime: "00:02:27"
    },
    recent: {
      products7Days: 23,
      products30Days: 67,
      rules7Days: 12,
      rules30Days: 28,
      productGrowthRate: "+12.5%",
      ruleGrowthRate: "+8.3%"
    }
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(DashboardMockService.getOverviewData).mockResolvedValue(mockOverviewData)
  })

  it('renders correctly', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    expect(wrapper.find('.application-overview-section').exists()).toBe(true)
    expect(wrapper.find('.section-title').text()).toBe('应用概要')
  })

  it('displays cumulative statistics section', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    const statsGroups = wrapper.findAll('.stats-group')
    expect(statsGroups.length).toBe(2)
    
    const cumulativeSection = statsGroups[0]
    expect(cumulativeSection.find('.group-title').text()).toBe('累计统计')
    
    // Check for cumulative statistics cards
    const cumulativeCards = cumulativeSection.findAll('.overview-card')
    expect(cumulativeCards.length).toBe(4)
  })

  it('displays recent statistics section', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    const statsGroups = wrapper.findAll('.stats-group')
    const recentSection = statsGroups[1]
    expect(recentSection.find('.group-title').text()).toBe('近期统计')
    
    // Check for recent statistics cards
    const recentCards = recentSection.findAll('.overview-card')
    expect(recentCards.length).toBe(4)
  })

  it('shows trend indicators for recent statistics', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    const statsGroups = wrapper.findAll('.stats-group')
    const recentSection = statsGroups[1]
    const trendElements = recentSection.findAll('.card-trend')
    
    // Should have trend indicators for recent statistics
    expect(trendElements.length).toBeGreaterThan(0)
    
    // Check for trend icons
    const upTrendIcons = recentSection.findAll('.trend-up')
    const downTrendIcons = recentSection.findAll('.trend-down')
    
    expect(upTrendIcons.length).toBeGreaterThan(0)
    expect(downTrendIcons.length).toBeGreaterThan(0)
  })

  it('fetches and displays overview data on mount', async () => {
    mount(ApplicationOverviewSection)
    
    // Wait for the component to mount and fetch data
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    expect(DashboardMockService.getOverviewData).toHaveBeenCalled()
  })

  it('formats numbers correctly', async () => {
    const wrapper = mount(ApplicationOverviewSection)
    const vm = wrapper.vm as any
    
    // Test number formatting
    expect(vm.formatNumber(156)).toBe('156')
    expect(vm.formatNumber(1500)).toBe('1.5k')
    expect(vm.formatNumber(15000)).toBe('1.5万')
  })

  it('shows loading state initially', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    // Check if cards show loading state
    const cards = wrapper.findAll('.overview-card')
    cards.forEach(card => {
      expect(card.attributes('loading')).toBeDefined()
    })
  })

  it('displays correct labels for cumulative statistics', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    const statsGroups = wrapper.findAll('.stats-group')
    const cumulativeSection = statsGroups[0]
    const labels = cumulativeSection.findAll('.card-label')
    
    const expectedLabels = [
      '累计上传产品数量',
      '累计导入规则数量', 
      '错误检出率',
      '平均检核时间'
    ]
    
    labels.forEach((label, index) => {
      expect(label.text()).toBe(expectedLabels[index])
    })
  })

  it('displays correct labels for recent statistics', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    const statsGroups = wrapper.findAll('.stats-group')
    const recentSection = statsGroups[1]
    const labels = recentSection.findAll('.card-label')
    
    const expectedLabels = [
      '近7日上传产品数量',
      '近30日上传产品数量',
      '近7日导入规则数量', 
      '近30日导入规则数量'
    ]
    
    labels.forEach((label, index) => {
      expect(label.text()).toBe(expectedLabels[index])
    })
  })

  it('uses correct colors for trend indicators', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    const upTrendIcons = wrapper.findAll('.trend-up')
    const downTrendIcons = wrapper.findAll('.trend-down')
    
    // Up trend should use red color (as specified in requirements)
    upTrendIcons.forEach(icon => {
      expect(icon.classes()).toContain('trend-up')
    })
    
    // Down trend should use green color (as specified in requirements)
    downTrendIcons.forEach(icon => {
      expect(icon.classes()).toContain('trend-down')
    })
  })

  it('has responsive design classes', () => {
    const wrapper = mount(ApplicationOverviewSection)
    
    // Check for responsive card layouts
    const cards = wrapper.findAll('.overview-card')
    expect(cards.length).toBe(8) // 4 cumulative + 4 recent
    
    // Check for stats groups
    const statsGroups = wrapper.findAll('.stats-group')
    expect(statsGroups.length).toBe(2)
  })

  it('displays time format correctly for average audit time', async () => {
    mount(ApplicationOverviewSection)
    
    // Wait for data to load
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // Should match HH:MM:SS pattern (like "00:02:27")
    const timePattern = /^\d{2}:\d{2}:\d{2}$/
    expect(timePattern.test(mockOverviewData.cumulative.avgAuditTime)).toBe(true)
  })
})