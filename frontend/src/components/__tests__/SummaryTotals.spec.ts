import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SummaryTotals from '@/components/dashboard/SummaryTotals.vue'

describe('SummaryTotals', () => {
  const mockRanges = [
    { key: 'today', label: '今日' },
    { key: 'week', label: '本周' },
    { key: 'month', label: '本月' },
    { key: 'year', label: '本年' }
  ]

  const mockMetrics = {
    today: {
      totalChecks: 1250,
      completedChecks: 1180,
      successRate: 94.4,
      averageTime: 2.3
    },
    week: {
      totalChecks: 8750,
      completedChecks: 8200,
      successRate: 93.7,
      averageTime: 2.5
    },
    month: {
      totalChecks: 35200,
      completedChecks: 33100,
      successRate: 94.0,
      averageTime: 2.4
    },
    year: {
      totalChecks: 423600,
      completedChecks: 398200,
      successRate: 94.0,
      averageTime: 2.3
    }
  }

  it('renders correctly with ranges and metrics', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.summary-totals').exists()).toBe(true)
  })

  it('renders all range buttons', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    expect(rangeButtons).toHaveLength(4)
    
    const rangeLabels = rangeButtons.map(btn => btn.text())
    expect(rangeLabels).toEqual(['今日', '本周', '本月', '本年'])
  })

  it('applies active class to correct range button', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    expect(rangeButtons[0].classes()).toContain('active')
    expect(rangeButtons[1].classes()).not.toContain('active')
    expect(rangeButtons[2].classes()).not.toContain('active')
    expect(rangeButtons[3].classes()).not.toContain('active')
  })

  it('applies active class to different range button when activeRange changes', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'week',
        metrics: mockMetrics
      }
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    expect(rangeButtons[0].classes()).not.toContain('active')
    expect(rangeButtons[1].classes()).toContain('active')
    expect(rangeButtons[2].classes()).not.toContain('active')
    expect(rangeButtons[3].classes()).not.toContain('active')
  })

  it('displays correct metrics for active range', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues).toHaveLength(4)
    
    const expectedValues = ['1,250', '1,180', '94.4%', '2.3s']
    const actualValues = metricValues.map(value => value.text())
    
    expect(actualValues).toEqual(expectedValues)
  })

  it('displays correct metric labels', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const metricLabels = wrapper.findAll('.metric-label')
    expect(metricLabels).toHaveLength(4)
    
    const expectedLabels = ['检测总数', '完成检测', '成功率', '平均耗时']
    const actualLabels = metricLabels.map(label => label.text())
    
    expect(actualLabels).toEqual(expectedLabels)
  })

  it('displays metrics with correct formatting', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    
    // Check number formatting (thousands separator)
    expect(metricValues[0].text()).toBe('1,250') // totalChecks
    expect(metricValues[1].text()).toBe('1,180') // completedChecks
    
    // Check percentage formatting
    expect(metricValues[2].text()).toBe('94.4%') // successRate
    
    // Check time formatting
    expect(metricValues[3].text()).toBe('2.3s') // averageTime
  })

  it('updates metrics when activeRange changes', async () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    // Check initial metrics (today)
    expect(wrapper.findAll('.metric-value')[0].text()).toBe('1,250')
    
    // Change to week range
    await wrapper.setProps({
      ranges: mockRanges,
      activeRange: 'week',
      metrics: mockMetrics
    })
    
    // Check updated metrics
    const weekMetrics = wrapper.findAll('.metric-value')
    expect(weekMetrics[0].text()).toBe('8,750')
    expect(weekMetrics[1].text()).toBe('8,200')
    expect(weekMetrics[2].text()).toBe('93.7%')
    expect(weekMetrics[3].text()).toBe('2.5s')
  })

  it('displays month range metrics correctly', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'month',
        metrics: mockMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues[0].text()).toBe('35,200')
    expect(metricValues[1].text()).toBe('33,100')
    expect(metricValues[2].text()).toBe('94.0%')
    expect(metricValues[3].text()).toBe('2.4s')
  })

  it('displays year range metrics correctly', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'year',
        metrics: mockMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues[0].text()).toBe('423,600')
    expect(metricValues[1].text()).toBe('398,200')
    expect(metricValues[2].text()).toBe('94.0%')
    expect(metricValues[3].text()).toBe('2.3s')
  })

  it('emits changeRange event when range button is clicked', async () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    await rangeButtons[1].trigger('click') // Click '本周'
    
    expect(wrapper.emitted('changeRange')).toBeTruthy()
    expect(wrapper.emitted('changeRange')).toHaveLength(1)
    expect(wrapper.emitted('changeRange')[0]).toEqual(['week'])
  })

  it('emits changeRange event with correct range key for different buttons', async () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    
    // Test each range button
    for (let i = 0; i < rangeButtons.length; i++) {
      await rangeButtons[i].trigger('click')
      
      expect(wrapper.emitted('changeRange')).toBeTruthy()
      expect(wrapper.emitted('changeRange')[i]).toEqual([mockRanges[i].key])
    }
  })

  it('has correct grid layout for metrics', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const metricsGrid = wrapper.find('.metrics-grid')
    expect(metricsGrid.exists()).toBe(true)
  })

  it('has correct metric card structure', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const metricCards = wrapper.findAll('.metric-card')
    expect(metricCards).toHaveLength(4)
    
    metricCards.forEach(card => {
      expect(card.find('.metric-value').exists()).toBe(true)
      expect(card.find('.metric-label').exists()).toBe(true)
    })
  })

  it('has correct range selector structure', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const rangeSelector = wrapper.find('.range-selector')
    expect(rangeSelector.exists()).toBe(true)
    expect(rangeSelector.find('.range-buttons').exists()).toBe(true)
  })

  it('handles empty ranges array', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: [],
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    expect(wrapper.findAll('.range-btn')).toHaveLength(0)
    expect(wrapper.find('.range-buttons').exists()).toBe(true)
  })

  it('handles single range', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: [mockRanges[0]],
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    expect(rangeButtons).toHaveLength(1)
    expect(rangeButtons[0].text()).toBe('今日')
    expect(rangeButtons[0].classes()).toContain('active')
  })

  it('handles metrics with zero values', () => {
    const zeroMetrics = {
      today: {
        totalChecks: 0,
        completedChecks: 0,
        successRate: 0,
        averageTime: 0
      }
    }

    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: zeroMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues[0].text()).toBe('0')
    expect(metricValues[1].text()).toBe('0')
    expect(metricValues[2].text()).toBe('0%')
    expect(metricValues[3].text()).toBe('0s')
  })

  it('handles metrics with decimal values', () => {
    const decimalMetrics = {
      today: {
        totalChecks: 1234.5,
        completedChecks: 1180.2,
        successRate: 95.67,
        averageTime: 2.45
      }
    }

    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: decimalMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues[0].text()).toBe('1,234.5')
    expect(metricValues[1].text()).toBe('1,180.2')
    expect(metricValues[2].text()).toBe('95.7%')
    expect(metricValues[3].text()).toBe('2.5s')
  })

  it('handles metrics with large numbers', () => {
    const largeMetrics = {
      today: {
        totalChecks: 9999999,
        completedChecks: 8888888,
        successRate: 88.88,
        averageTime: 99.99
      }
    }

    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: largeMetrics
      }
    })

    const metricValues = wrapper.findAll('.metric-value')
    expect(metricValues[0].text()).toBe('9,999,999')
    expect(metricValues[1].text()).toBe('8,888,888')
    expect(metricValues[2].text()).toBe('88.9%')
    expect(metricValues[3].text()).toBe('100.0s')
  })

  it('handles missing metric properties gracefully', () => {
    const incompleteMetrics = {
      today: {
        totalChecks: 1250
        // missing other properties
      }
    }

    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: incompleteMetrics
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.findAll('.metric-card')).toHaveLength(4)
  })

  it('has correct styling classes', () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: mockRanges,
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    const metricCards = wrapper.findAll('.metric-card')
    metricCards.forEach(card => {
      expect(card.classes()).toContain('metric-card')
    })

    const rangeButtons = wrapper.findAll('.range-btn')
    rangeButtons.forEach(btn => {
      expect(btn.classes()).toContain('range-btn')
    })
  })

  it('updates when ranges change', async () => {
    const wrapper = mount(SummaryTotals, {
      props: {
        ranges: [mockRanges[0]],
        activeRange: 'today',
        metrics: mockMetrics
      }
    })

    expect(wrapper.findAll('.range-btn')).toHaveLength(1)
    
    // Add more ranges
    await wrapper.setProps({
      ranges: mockRanges,
      activeRange: 'today',
      metrics: mockMetrics
    })
    
    expect(wrapper.findAll('.range-btn')).toHaveLength(4)
  })
})