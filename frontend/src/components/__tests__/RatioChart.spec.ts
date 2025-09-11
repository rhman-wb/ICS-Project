import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RatioChart from '@/components/dashboard/RatioChart.vue'

// Mock ECharts
vi.mock('echarts', () => ({
  default: vi.fn(() => ({
    setOption: vi.fn(),
    dispose: vi.fn(),
    resize: vi.fn()
  }))
}))

describe('RatioChart', () => {
  const mockTabs = [
    { key: 'category', label: '产品分类' },
    { key: 'brand', label: '品牌分布' },
    { key: 'region', label: '地区分布' }
  ]

  const mockProductTypes = [
    { key: 'electronics', name: '电子产品', value: 35, color: '#5470c6' },
    { key: 'clothing', name: '服装配饰', value: 25, color: '#91cc75' },
    { key: 'food', name: '食品饮料', value: 20, color: '#fac858' },
    { key: 'books', name: '图书文具', value: 12, color: '#ee6666' },
    { key: 'other', name: '其他', value: 8, color: '#73c0de' }
  ]

  it('renders correctly with tabs and product types', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.ratio-chart').exists()).toBe(true)
  })

  it('renders all tab buttons', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const tabButtons = wrapper.findAll('.tab-btn')
    expect(tabButtons).toHaveLength(3)
    
    const tabLabels = tabButtons.map(btn => btn.text())
    expect(tabLabels).toEqual(['产品分类', '品牌分布', '地区分布'])
  })

  it('applies active class to correct tab', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const tabButtons = wrapper.findAll('.tab-btn')
    expect(tabButtons[0].classes()).toContain('active')
    expect(tabButtons[1].classes()).not.toContain('active')
    expect(tabButtons[2].classes()).not.toContain('active')
  })

  it('applies active class to different tab when active prop changes', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'brand',
        productTypes: mockProductTypes
      }
    })

    const tabButtons = wrapper.findAll('.tab-btn')
    expect(tabButtons[0].classes()).not.toContain('active')
    expect(tabButtons[1].classes()).toContain('active')
    expect(tabButtons[2].classes()).not.toContain('active')
  })

  it('renders all product type items in list', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const typeItems = wrapper.findAll('.type-item')
    expect(typeItems).toHaveLength(5)
  })

  it('displays correct product type names', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const typeNames = wrapper.findAll('.type-name')
    expect(typeNames).toHaveLength(5)
    
    const expectedNames = ['电子产品', '服装配饰', '食品饮料', '图书文具', '其他']
    const actualNames = typeNames.map(name => name.text())
    
    expect(actualNames).toEqual(expectedNames)
  })

  it('displays correct product type values', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const typeValues = wrapper.findAll('.type-value')
    expect(typeValues).toHaveLength(5)
    
    const expectedValues = ['35', '25', '20', '12', '8']
    const actualValues = typeValues.map(value => value.text())
    
    expect(actualValues).toEqual(expectedValues)
  })

  it('displays correct product type percentages', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const typePercentages = wrapper.findAll('.type-percentage')
    expect(typePercentages).toHaveLength(5)
    
    const expectedPercentages = ['35%', '25%', '20%', '12%', '8%']
    const actualPercentages = typePercentages.map(percentage => percentage.text())
    
    expect(actualPercentages).toEqual(expectedPercentages)
  })

  it('displays correct color indicators for each product type', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const colorIndicators = wrapper.findAll('.color-indicator')
    expect(colorIndicators).toHaveLength(5)
    
    // Check color indicators have correct background colors
    expect(colorIndicators[0].attributes('style')).toContain('background-color: #5470c6')
    expect(colorIndicators[1].attributes('style')).toContain('background-color: #91cc75')
    expect(colorIndicators[2].attributes('style')).toContain('background-color: #fac858')
    expect(colorIndicators[3].attributes('style')).toContain('background-color: #ee6666')
    expect(colorIndicators[4].attributes('style')).toContain('background-color: #73c0de')
  })

  it('has chart container', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const chartContainer = wrapper.find('.chart-container')
    expect(chartContainer.exists()).toBe(true)
  })

  it('has chart fallback image', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const fallbackImage = wrapper.find('.chart-fallback')
    expect(fallbackImage.exists()).toBe(true)
    expect(fallbackImage.attributes('src')).toBe('/fallback-chart.png')
    expect(fallbackImage.attributes('alt')).toBe('图表占位')
  })

  it('has correct layout structure', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    expect(wrapper.find('.chart-tabs').exists()).toBe(true)
    expect(wrapper.find('.chart-content').exists()).toBe(true)
    expect(wrapper.find('.type-list').exists()).toBe(true)
  })

  it('emits changeTab event when tab is clicked', async () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const tabButtons = wrapper.findAll('.tab-btn')
    await tabButtons[1].trigger('click') // Click '品牌分布'
    
    expect(wrapper.emitted('changeTab')).toBeTruthy()
    expect(wrapper.emitted('changeTab')).toHaveLength(1)
    expect(wrapper.emitted('changeTab')[0]).toEqual(['brand'])
  })

  it('emits changeTab event with correct tab key for different tabs', async () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const tabButtons = wrapper.findAll('.tab-btn')
    
    // Test each tab
    for (let i = 0; i < tabButtons.length; i++) {
      await tabButtons[i].trigger('click')
      
      expect(wrapper.emitted('changeTab')).toBeTruthy()
      expect(wrapper.emitted('changeTab')[i]).toEqual([mockTabs[i].key])
    }
  })

  it('handles empty product types array', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: []
      }
    })

    expect(wrapper.findAll('.type-item')).toHaveLength(0)
    expect(wrapper.find('.type-list').exists()).toBe(true)
  })

  it('handles single product type', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: [mockProductTypes[0]]
      }
    })

    const typeItems = wrapper.findAll('.type-item')
    expect(typeItems).toHaveLength(1)
    expect(typeItems[0].find('.type-name').text()).toBe('电子产品')
    expect(typeItems[0].find('.type-value').text()).toBe('35')
    expect(typeItems[0].find('.type-percentage').text()).toBe('35%')
  })

  it('updates when product types change', async () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: [mockProductTypes[0]]
      }
    })

    expect(wrapper.findAll('.type-item')).toHaveLength(1)
    
    // Add more product types
    await wrapper.setProps({
      tabs: mockTabs,
      active: 'category',
      productTypes: mockProductTypes
    })
    
    expect(wrapper.findAll('.type-item')).toHaveLength(5)
  })

  it('handles missing color property gracefully', () => {
    const productTypesWithoutColor = mockProductTypes.map(type => {
      const { color, ...rest } = type
      return rest
    })

    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: productTypesWithoutColor
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.findAll('.type-item')).toHaveLength(5)
  })

  it('handles percentage calculation correctly', () => {
    const customProductTypes = [
      { key: 'a', name: 'Type A', value: 10, color: '#5470c6' },
      { key: 'b', name: 'Type B', value: 20, color: '#91cc75' },
      { key: 'c', name: 'Type C', value: 30, color: '#fac858' }
    ]

    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: customProductTypes
      }
    })

    const typePercentages = wrapper.findAll('.type-percentage')
    const expectedPercentages = ['17%', '33%', '50%'] // 10/60, 20/60, 30/60
    const actualPercentages = typePercentages.map(percentage => percentage.text())
    
    expect(actualPercentages).toEqual(expectedPercentages)
  })

  it('has correct styling classes', () => {
    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: mockProductTypes
      }
    })

    const typeItems = wrapper.findAll('.type-item')
    typeItems.forEach(item => {
      expect(item.classes()).toContain('type-item')
      expect(item.find('.type-info').exists()).toBe(true)
      expect(item.find('.type-details').exists()).toBe(true)
    })
  })

  it('handles zero values correctly', () => {
    const zeroProductTypes = [
      { key: 'a', name: 'Type A', value: 0, color: '#5470c6' },
      { key: 'b', name: 'Type B', value: 0, color: '#91cc75' }
    ]

    const wrapper = mount(RatioChart, {
      props: {
        tabs: mockTabs,
        active: 'category',
        productTypes: zeroProductTypes
      }
    })

    const typeValues = wrapper.findAll('.type-value')
    const typePercentages = wrapper.findAll('.type-percentage')
    
    expect(typeValues[0].text()).toBe('0')
    expect(typeValues[1].text()).toBe('0')
    expect(typePercentages[0].text()).toBe('0%')
    expect(typePercentages[1].text()).toBe('0%')
  })
})