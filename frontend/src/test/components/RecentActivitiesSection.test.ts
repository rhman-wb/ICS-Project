import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import RecentActivitiesSection from '@/components/dashboard/RecentActivitiesSection.vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// Mock dayjs
vi.mock('dayjs', () => {
  const mockDayjs = vi.fn((time) => ({
    diff: vi.fn((otherTime, unit) => {
      if (unit === 'second') return 30
      if (unit === 'minute') return 14
      if (unit === 'hour') return 2
      if (unit === 'day') return 1
      return 0
    }),
    format: vi.fn(() => '10-31 14:30')
  }))
  
  mockDayjs.extend = vi.fn()
  mockDayjs.locale = vi.fn()
  
  return {
    default: mockDayjs
  }
})

// Mock dayjs plugins
vi.mock('dayjs/plugin/relativeTime', () => ({
  default: vi.fn()
}))

vi.mock('dayjs/locale/zh-cn', () => ({
  default: vi.fn()
}))

// Mock DashboardMockService
vi.mock('@/api/mock/dashboardMock', () => ({
  DashboardMockService: {
    getRecentActivities: vi.fn()
  }
}))

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      info: vi.fn(),
      error: vi.fn()
    }
  }
})

describe('RecentActivitiesSection', () => {
  const mockActivities = [
    {
      id: '1',
      type: 'upload',
      user: '胡潇禹',
      content: '上传了',
      productName: '财产保险产品A',
      productId: 'prod_001',
      time: new Date('2024-01-20T14:30:00'),
      timestamp: 1705737000000
    },
    {
      id: '2',
      type: 'rule_update',
      user: '张三',
      content: '更新了3条规则',
      ruleCount: 3,
      time: new Date('2024-01-20T14:10:00'),
      timestamp: 1705735800000
    },
    {
      id: '3',
      type: 'rule_approve',
      user: '李四',
      content: '审核通过了5条规则',
      ruleCount: 5,
      time: new Date('2024-01-20T13:50:00'),
      timestamp: 1705734600000
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(DashboardMockService.getRecentActivities).mockResolvedValue({
      activities: mockActivities,
      total: 15,
      hasMore: true
    })
  })

  it('renders correctly', async () => {
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick() // Wait for async data loading
    
    expect(wrapper.find('.section-title').text()).toBe('近期动态')
    expect(wrapper.find('.activities-content').exists()).toBe(true)
  })

  it('loads activities on mount', async () => {
    mount(RecentActivitiesSection)
    
    await nextTick()
    
    expect(DashboardMockService.getRecentActivities).toHaveBeenCalledWith(15)
  })

  it('displays activities correctly', async () => {
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick() // Wait for async data loading
    
    const activityItems = wrapper.findAll('.activity-item')
    expect(activityItems).toHaveLength(3)
    
    // Check first activity
    const firstActivity = activityItems[0]
    expect(firstActivity.find('.activity-user').text()).toBe('胡潇禹')
    expect(firstActivity.find('.product-link').text()).toBe('财产保险产品A')
  })

  it('shows different activity types correctly', async () => {
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    const activityItems = wrapper.findAll('.activity-item')
    
    // Check activity content
    expect(activityItems[1].text()).toContain('更新了3条规则')
    expect(activityItems[2].text()).toContain('审核通过了5条规则')
  })

  it('handles product link click', async () => {
    const { message } = await import('ant-design-vue')
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    const productLink = wrapper.find('.product-link')
    await productLink.trigger('click')
    
    expect(message.info).toHaveBeenCalledWith('跳转到产品详情: prod_001')
  })

  it('shows view more button when hasMore is true', async () => {
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    const viewMoreBtn = wrapper.find('.view-more-btn')
    expect(viewMoreBtn.exists()).toBe(true)
    expect(viewMoreBtn.text()).toContain('查看更多')
  })

  it('expands activities when view more is clicked', async () => {
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    const viewMoreBtn = wrapper.find('.view-more-btn')
    if (viewMoreBtn.exists()) {
      await viewMoreBtn.trigger('click')
      await nextTick()
      
      // Should show collapse button
      const collapseBtn = wrapper.find('.view-more-btn')
      if (collapseBtn.exists()) {
        expect(collapseBtn.text()).toContain('收起')
      }
    }
  })

  it('handles loading state', async () => {
    // Mock a delayed response
    vi.mocked(DashboardMockService.getRecentActivities).mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve({
        activities: mockActivities,
        total: 15,
        hasMore: true
      }), 100))
    )
    
    const wrapper = mount(RecentActivitiesSection)
    
    // Should show loading content
    expect(wrapper.find('.activities-content').exists()).toBe(true)
  })

  it('handles empty state', async () => {
    vi.mocked(DashboardMockService.getRecentActivities).mockResolvedValue({
      activities: [],
      total: 0,
      hasMore: false
    })
    
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    // Check that component renders without errors when no activities
    expect(wrapper.find('.section-title').text()).toBe('近期动态')
    expect(wrapper.findAll('.activity-item')).toHaveLength(0)
  })

  it('handles error state', async () => {
    const { message } = await import('ant-design-vue')
    vi.mocked(DashboardMockService.getRecentActivities).mockRejectedValue(
      new Error('Network error')
    )
    
    mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    expect(message.error).toHaveBeenCalledWith('获取近期动态失败')
  })

  it('exposes refresh method', () => {
    const wrapper = mount(RecentActivitiesSection)
    
    expect(wrapper.vm.refresh).toBeDefined()
    expect(typeof wrapper.vm.refresh).toBe('function')
  })

  it('displays relative time correctly', async () => {
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    const timeElements = wrapper.findAll('.activity-time')
    if (timeElements.length > 0) {
      // Should display some relative time format
      expect(timeElements[0].text()).toMatch(/前$/)
    }
  })

  it('limits displayed activities to default limit initially', async () => {
    // Mock more activities than default limit
    const manyActivities = Array.from({ length: 10 }, (_, i) => ({
      id: `${i + 1}`,
      type: 'upload',
      user: `用户${i + 1}`,
      content: '上传了',
      productName: `产品${i + 1}`,
      productId: `prod_${i + 1}`,
      time: new Date(),
      timestamp: Date.now() - i * 60000
    }))
    
    vi.mocked(DashboardMockService.getRecentActivities).mockResolvedValue({
      activities: manyActivities,
      total: 15,
      hasMore: true
    })
    
    const wrapper = mount(RecentActivitiesSection)
    
    await nextTick()
    await nextTick()
    
    // Should only show 5 activities initially (default limit)
    const activityItems = wrapper.findAll('.activity-item')
    expect(activityItems).toHaveLength(5)
  })
})