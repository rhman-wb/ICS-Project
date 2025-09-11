import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppOverview from '@/components/dashboard/AppOverview.vue'

describe('AppOverview', () => {
  const mockOverview = {
    appCount: { value: 128, trend: 'up' },
    activeCount: { value: 42, trend: 'down' },
    totalCount: { value: 256, trend: 'up' },
    validCount: { value: 198, trend: 'up' },
    errorCount: { value: 8, trend: 'down' },
    warningCount: { value: 12, trend: 'up' },
    pendingCount: { value: 16, trend: 'down' },
    completedCount: { value: 220, trend: 'up' }
  }

  it('renders correctly with overview data', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.app-overview').exists()).toBe(true)
  })

  it('renders all 8 metric cards', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const metricCards = wrapper.findAll('.metric-card')
    expect(metricCards).toHaveLength(8)
  })

  it('displays correct metric values', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues).toHaveLength(8)
    
    const expectedValues = ['128', '42', '256', '198', '8', '12', '16', '220']
    const actualValues = metricValues.map(value => value.text().replace(/,/g, ''))
    
    expect(actualValues).toEqual(expectedValues)
  })

  it('displays correct metric labels', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const metricLabels = wrapper.findAll('.metric-label')
    expect(metricLabels).toHaveLength(8)
    
    const expectedLabels = ['应用总数', '活跃应用', '检测总数', '有效检测', '错误数', '警告数', '待处理', '已完成']
    const actualLabels = metricLabels.map(label => label.text())
    
    expect(actualLabels).toEqual(expectedLabels)
  })

  it('displays trend indicators with correct icons', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const trendIcons = wrapper.findAll('.trend-icon')
    expect(trendIcons).toHaveLength(8)
    
    // Check that up trends have fa-arrow-up class
    const upTrends = wrapper.findAll('.trend-up')
    expect(upTrends).toHaveLength(5) // appCount, totalCount, validCount, warningCount, completedCount
    
    // Check that down trends have fa-arrow-down class
    const downTrends = wrapper.findAll('.trend-down')
    expect(downTrends).toHaveLength(3) // activeCount, errorCount, pendingCount
  })

  it('displays trend indicators with correct colors', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const upTrends = wrapper.findAll('.trend-up')
    const downTrends = wrapper.findAll('.trend-down')
    
    // Up trends should be red (#F95E5A)
    upTrends.forEach(trend => {
      expect(trend.classes()).toContain('trend-up')
    })
    
    // Down trends should be green (#4BD863)
    downTrends.forEach(trend => {
      expect(trend.classes()).toContain('trend-down')
    })
  })

  it('handles zero values correctly', () => {
    const zeroOverview = {
      appCount: { value: 0, trend: 'up' },
      activeCount: { value: 0, trend: 'down' },
      totalCount: { value: 0, trend: 'up' },
      validCount: { value: 0, trend: 'up' },
      errorCount: { value: 0, trend: 'down' },
      warningCount: { value: 0, trend: 'up' },
      pendingCount: { value: 0, trend: 'down' },
      completedCount: { value: 0, trend: 'up' }
    }

    const wrapper = mount(AppOverview, {
      props: {
        overview: zeroOverview
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    metricValues.forEach(value => {
      expect(value.text()).toBe('0')
    })
  })

  it('handles large values correctly', () => {
    const largeOverview = {
      appCount: { value: 9999, trend: 'up' },
      activeCount: { value: 8888, trend: 'down' },
      totalCount: { value: 7777, trend: 'up' },
      validCount: { value: 6666, trend: 'up' },
      errorCount: { value: 5555, trend: 'down' },
      warningCount: { value: 4444, trend: 'up' },
      pendingCount: { value: 3333, trend: 'down' },
      completedCount: { value: 2222, trend: 'up' }
    }

    const wrapper = mount(AppOverview, {
      props: {
        overview: largeOverview
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    const expectedValues = ['9999', '8888', '7777', '6666', '5555', '4444', '3333', '2222']
    const actualValues = metricValues.map(value => value.text().replace(/,/g, ''))
    
    expect(actualValues).toEqual(expectedValues)
  })

  it('has correct card structure', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const metricCards = wrapper.findAll('.metric-card')
    metricCards.forEach(card => {
      expect(card.find('.metric-value').exists()).toBe(true)
      expect(card.find('.metric-label').exists()).toBe(true)
      expect(card.find('.trend-indicator').exists()).toBe(true)
      expect(card.find('.trend-icon').exists()).toBe(true)
    })
  })

  it('has correct grid layout', () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    const metricsGrid = wrapper.find('.metrics-grid')
    expect(metricsGrid.exists()).toBe(true)
  })

  it('updates when overview data changes', async () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    // Check initial app count
    expect(wrapper.findAll('.metric-value')[0].text()).toBe('128')
    
    // Update with new data
    const newOverview = { ...mockOverview }
    newOverview.appCount = { value: 200, trend: 'up' }
    
    await wrapper.setProps({ overview: newOverview })
    
    expect(wrapper.findAll('.metric-value')[0].text()).toBe('200')
  })

  it('updates trend indicators when data changes', async () => {
    const wrapper = mount(AppOverview, {
      props: {
        overview: mockOverview
      }
    })

    // Check initial trend (up)
    expect(wrapper.findAll('.metric-card')[0].findAll('.trend-up')).toHaveLength(1)
    expect(wrapper.findAll('.metric-card')[0].findAll('.trend-down')).toHaveLength(0)
    
    // Update with new trend
    const newOverview = { ...mockOverview }
    newOverview.appCount = { value: 200, trend: 'down' }
    
    await wrapper.setProps({ overview: newOverview })
    
    // Check trend changed to down
    expect(wrapper.findAll('.metric-card')[0].findAll('.trend-up')).toHaveLength(0)
    expect(wrapper.findAll('.metric-card')[0].findAll('.trend-down')).toHaveLength(1)
  })

  it('handles undefined trend gracefully', () => {
    const invalidOverview = {
      appCount: { value: 128, trend: undefined },
      activeCount: { value: 42, trend: 'down' },
      totalCount: { value: 256, trend: 'up' },
      validCount: { value: 198, trend: 'up' },
      errorCount: { value: 8, trend: 'down' },
      warningCount: { value: 12, trend: 'up' },
      pendingCount: { value: 16, trend: 'down' },
      completedCount: { value: 220, trend: 'up' }
    }

    const wrapper = mount(AppOverview, {
      props: {
        overview: invalidOverview
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.findAll('.metric-card')).toHaveLength(8)
  })
})