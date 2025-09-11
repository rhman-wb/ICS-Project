import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MyFavorites from '@/components/dashboard/MyFavorites.vue'

describe('MyFavorites', () => {
  const mockItems = [
    {
      id: '1',
      name: '销售数据分析',
      type: '审核',
      submitter: '张三',
      submittedAt: '2024-01-15',
      icon: 'fa-chart-line'
    },
    {
      id: '2',
      name: '库存监控看板',
      type: '配置',
      submitter: '李四',
      submittedAt: '2024-01-14',
      icon: 'fa-dashboard'
    },
    {
      id: '3',
      name: '客户满意度调查',
      type: '审核',
      submitter: '王五',
      submittedAt: '2024-01-13',
      icon: 'fa-survey'
    }
  ]

  const mockFilters = {
    keywordPlaceholder: '搜索任务',
    timeLabel: '时间筛选',
    typeLabel: '类型筛选'
  }

  it('renders correctly with items and filters', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.my-favorites').exists()).toBe(true)
  })

  it('renders filter section with search and selects', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    expect(wrapper.find('.filter-section').exists()).toBe(true)
    expect(wrapper.find('.search-input').exists()).toBe(true)
    expect(wrapper.find('.filter-selects').exists()).toBe(true)
  })

  it('renders all task items', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const taskItems = wrapper.findAll('.task-item')
    expect(taskItems).toHaveLength(3)
  })

  it('displays correct item names', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const itemNames = wrapper.findAll('.item-name')
    expect(itemNames).toHaveLength(3)
    
    const expectedNames = ['销售数据分析', '库存监控看板', '客户满意度调查']
    const actualNames = itemNames.map(name => name.text())
    
    expect(actualNames).toEqual(expectedNames)
  })

  it('displays correct item descriptions', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const itemDescriptions = wrapper.findAll('.item-description')
    expect(itemDescriptions).toHaveLength(3)
    
    const expectedDescriptions = [
      '月度销售数据统计与分析报告',
      '实时库存数量与预警监控看板',
      '客户满意度问卷调查结果统计'
    ]
    const actualDescriptions = itemDescriptions.map(desc => desc.text())
    
    expect(actualDescriptions).toEqual(expectedDescriptions)
  })

  it('displays correct item status with styling', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const statusBadges = wrapper.findAll('.status-badge')
    expect(statusBadges).toHaveLength(3)
    
    // Check status text
    const statusTexts = statusBadges.map(badge => badge.text())
    expect(statusTexts).toEqual(['进行中', '待处理', '已完成'])
    
    // Check status classes
    expect(statusBadges[0].classes()).toContain('status-active')
    expect(statusBadges[1].classes()).toContain('status-pending')
    expect(statusBadges[2].classes()).toContain('status-completed')
  })

  it('displays correct last updated dates', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const updateDates = wrapper.findAll('.update-date')
    expect(updateDates).toHaveLength(3)
    
    const expectedDates = ['2024-01-15', '2024-01-14', '2024-01-13']
    const actualDates = updateDates.map(date => date.text())
    
    expect(actualDates).toEqual(expectedDates)
  })

  it('has correct item link structure', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const itemLinks = wrapper.findAll('.item-link')
    expect(itemLinks).toHaveLength(3)
    
    itemLinks.forEach(link => {
      expect(link.classes()).toContain('item-link')
      expect(link.find('.item-content').exists()).toBe(true)
    })
  })

  it('has search input', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const searchInput = wrapper.find('.search-input')
    expect(searchInput.exists()).toBe(true)
    expect(searchInput.attributes('placeholder')).toBe('搜索我的关注')
  })

  it('applies active class to filter buttons', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const filterButtons = wrapper.findAll('.filter-btn')
    expect(filterButtons[0].classes()).toContain('active')
    
    filterButtons.slice(1).forEach(btn => {
      expect(btn.classes()).not.toContain('active')
    })
  })

  it('emits search event when search input changes', async () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const searchInput = wrapper.find('.search-input')
    await searchInput.setValue('销售数据')
    
    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')).toHaveLength(1)
    expect(wrapper.emitted('search')[0]).toEqual(['销售数据'])
  })

  it('emits search event with empty string when cleared', async () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const searchInput = wrapper.find('.search-input')
    await searchInput.setValue('')
    
    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')).toHaveLength(1)
    expect(wrapper.emitted('search')[0]).toEqual([''])
  })

  it('emits select event when item is clicked', async () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const itemLinks = wrapper.findAll('.item-link')
    await itemLinks[0].trigger('click')
    
    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')).toHaveLength(1)
    expect(wrapper.emitted('select')[0]).toEqual([mockItems[0]])
  })

  it('emits select event with correct item data', async () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const itemLinks = wrapper.findAll('.item-link')
    
    // Test each item
    for (let i = 0; i < itemLinks.length; i++) {
      await itemLinks[i].trigger('click')
      
      expect(wrapper.emitted('select')).toBeTruthy()
      expect(wrapper.emitted('select')[i]).toEqual([mockItems[i]])
    }
  })

  it('handles empty items array', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: [],
        filters: mockFilters
      }
    })

    expect(wrapper.findAll('.list-item')).toHaveLength(0)
    expect(wrapper.find('.item-list').exists()).toBe(true)
  })

  it('handles single item', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: [mockItems[0]],
        filters: mockFilters
      }
    })

    const listItems = wrapper.findAll('.list-item')
    expect(listItems).toHaveLength(1)
    expect(listItems[0].find('.item-name').text()).toBe('销售数据分析')
  })

  it('updates when items change', async () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: [mockItems[0]],
        filters: mockFilters
      }
    })

    expect(wrapper.findAll('.list-item')).toHaveLength(1)
    
    // Add more items
    await wrapper.setProps({
      items: mockItems,
      filters: mockFilters
    })
    
    expect(wrapper.findAll('.list-item')).toHaveLength(3)
  })

  it('handles missing item properties gracefully', () => {
    const incompleteItems = [
      {
        id: '1',
        name: '测试项目',
        // missing type, status, lastUpdated, description
      }
    ]

    const wrapper = mount(MyFavorites, {
      props: {
        items: incompleteItems,
        filters: mockFilters
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.item-name').text()).toBe('测试项目')
  })

  it('has correct container structure', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    expect(wrapper.find('.favorites-header').exists()).toBe(true)
    expect(wrapper.find('.filters-container').exists()).toBe(true)
    expect(wrapper.find('.item-list').exists()).toBe(true)
    expect(wrapper.find('.search-container').exists()).toBe(true)
  })

  it('has scrollable content area', () => {
    const wrapper = mount(MyFavorites, {
      props: {
        items: mockItems,
        filters: mockFilters
      }
    })

    const itemList = wrapper.find('.item-list')
    expect(itemList.exists()).toBe(true)
  })
})