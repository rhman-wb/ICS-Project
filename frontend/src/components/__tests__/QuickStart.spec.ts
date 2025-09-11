import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory, Router } from 'vue-router'
import QuickStart from '@/components/dashboard/QuickStart.vue'

// Create a mock router for testing
let router: Router

const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/product/import', name: 'ProductImport', component: { template: '<div>Import</div>' } },
      { path: '/rules/create', name: 'RulesCreate', component: { template: '<div>Create</div>' } },
      { path: '/template/download', name: 'TemplateDownload', component: { template: '<div>Download</div>' } },
      { path: '/', name: 'Home', component: { template: '<div>Home</div>' } }
    ]
  })
}

describe('QuickStart', () => {
  beforeEach(() => {
    router = createMockRouter()
  })

  const mockItems = [
    {
      id: 'import',
      name: '导入产品',
      description: '批量导入产品数据',
      icon: 'fa-upload',
      route: '/product/import',
      badge: null
    },
    {
      id: 'create',
      name: '创建规则',
      description: '创建检测规则',
      icon: 'fa-plus',
      route: '/rules/create',
      badge: null
    },
    {
      id: 'download',
      name: '下载模板',
      description: '下载导入模板',
      icon: 'fa-download',
      route: '/template/download',
      badge: null
    },
    {
      id: 'batch',
      name: '批量导入',
      description: '批量导入产品',
      icon: 'fa fa-upload',
      route: '/product/import/batch',
      badge: '新'
    },
    {
      id: 'detail',
      name: '产品详情',
      description: '查看产品详情',
      icon: 'fa fa-info-circle',
      route: '/product/detail/1',
      badge: null
    },
    {
      id: 'settings',
      name: '系统设置',
      description: '系统配置管理',
      icon: 'fa fa-cog',
      route: '/settings',
      badge: '3'
    }
  ]

  it('renders correctly with items', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.quick-start').exists()).toBe(true)
  })

  it('renders all 6 quick start items', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    expect(quickItems).toHaveLength(6)
  })

  it('displays correct item names', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const itemNames = wrapper.findAll('.item-name')
    expect(itemNames).toHaveLength(6)
    
    const expectedNames = ['导入产品', '创建规则', '下载模板', '批量导入', '产品详情', '系统设置']
    const actualNames = itemNames.map(name => name.text())
    
    expect(actualNames).toEqual(expectedNames)
  })

  it('displays correct item descriptions', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const itemDescriptions = wrapper.findAll('.item-description')
    expect(itemDescriptions).toHaveLength(6)
    
    const expectedDescriptions = [
      '批量导入产品数据',
      '创建检测规则',
      '下载导入模板',
      '批量导入产品',
      '查看产品详情',
      '系统配置管理'
    ]
    const actualDescriptions = itemDescriptions.map(desc => desc.text())
    
    expect(actualDescriptions).toEqual(expectedDescriptions)
  })

  it('displays correct icons for each item', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const itemIcons = wrapper.findAll('.item-icon')
    expect(itemIcons).toHaveLength(6)
    
    const expectedIcons = ['fa-upload', 'fa-plus', 'fa-download', 'fa fa-upload', 'fa fa-info-circle', 'fa fa-cog']
    const actualIcons = itemIcons.map(icon => icon.classes())
    
    expectedIcons.forEach((expectedIcon, index) => {
      expect(actualIcons[index]).toContain(expectedIcon)
    })
  })

  it('displays badges correctly', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const badges = wrapper.findAll('.badge')
    expect(badges).toHaveLength(2) // Only '新' and '3' badges should be visible
    
    expect(badges[0].text()).toBe('新')
    expect(badges[1].text()).toBe('3')
  })

  it('does not display badge for items without badge', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    
    // Items without badges should not have badge elements
    expect(quickItems[0].find('.badge').exists()).toBe(false) // import
    expect(quickItems[1].find('.badge').exists()).toBe(false) // create
    expect(quickItems[2].find('.badge').exists()).toBe(false) // download
  })

  it('displays badges in correct positions', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    
    // '批量导入' (index 3) should have '新' badge
    expect(quickItems[3].find('.badge').text()).toBe('新')
    
    // '系统设置' (index 5) should have '3' badge
    expect(quickItems[5].find('.badge').text()).toBe('3')
  })

  it('has correct grid layout', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickGrid = wrapper.find('.quick-grid')
    expect(quickGrid.exists()).toBe(true)
  })

  it('has correct item structure', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    quickItems.forEach(item => {
      expect(item.find('.item-content').exists()).toBe(true)
      expect(item.find('.item-icon').exists()).toBe(true)
      expect(item.find('.item-text').exists()).toBe(true)
      expect(item.find('.item-name').exists()).toBe(true)
      expect(item.find('.item-description').exists()).toBe(true)
    })
  })

  it('is clickable for items with routes', async () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    const pushSpy = vi.spyOn(router, 'push')
    
    await quickItems[0].trigger('click') // Click '导入产品'
    
    expect(pushSpy).toHaveBeenCalledWith('/product/import')
  })

  it('handles items without routes gracefully', async () => {
    const itemsWithoutRoute = [
      {
        id: 'no-route',
        name: 'No Route',
        description: 'Item without route',
        icon: 'fa-question',
        route: null,
        badge: null
      }
    ]

    const wrapper = mount(QuickStart, {
      props: {
        items: itemsWithoutRoute
      },
      global: {
        plugins: [router]
      }
    })

    const quickItem = wrapper.find('.quick-item')
    const pushSpy = vi.spyOn(router, 'push')
    
    await quickItem.trigger('click')
    
    // Should not call router.push
    expect(pushSpy).not.toHaveBeenCalled()
  })

  it('emits click event when item is clicked', async () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    
    await quickItems[0].trigger('click') // Click '导入产品'
    
    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')).toHaveLength(1)
    expect(wrapper.emitted('click')[0]).toEqual([mockItems[0]])
  })

  it('emits click event with correct item data', async () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    
    // Test each item
    for (let i = 0; i < quickItems.length; i++) {
      await quickItems[i].trigger('click')
      
      expect(wrapper.emitted('click')).toBeTruthy()
      expect(wrapper.emitted('click')[i]).toEqual([mockItems[i]])
    }
  })

  it('handles empty items array', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: []
      },
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.findAll('.quick-item')).toHaveLength(0)
    expect(wrapper.find('.quick-grid').exists()).toBe(true)
  })

  it('handles single item', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: [mockItems[0]]
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    expect(quickItems).toHaveLength(1)
    expect(quickItems[0].find('.item-name').text()).toBe('导入产品')
  })

  it('updates when items change', async () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: [mockItems[0]]
      },
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.findAll('.quick-item')).toHaveLength(1)
    
    // Add more items
    await wrapper.setProps({
      items: mockItems.slice(0, 3)
    })
    
    expect(wrapper.findAll('.quick-item')).toHaveLength(3)
  })

  it('handles badge with empty string', () => {
    const itemsWithEmptyBadge = [
      {
        id: 'empty-badge',
        name: 'Empty Badge',
        description: 'Badge with empty string',
        icon: 'fa-question',
        route: '/test',
        badge: ''
      }
    ]

    const wrapper = mount(QuickStart, {
      props: {
        items: itemsWithEmptyBadge
      },
      global: {
        plugins: [router]
      }
    })

    const quickItem = wrapper.find('.quick-item')
    expect(quickItem.find('.badge').exists()).toBe(true)
    expect(quickItem.find('.badge').text()).toBe('')
  })

  it('handles badge with zero', () => {
    const itemsWithZeroBadge = [
      {
        id: 'zero-badge',
        name: 'Zero Badge',
        description: 'Badge with zero',
        icon: 'fa-question',
        route: '/test',
        badge: '0'
      }
    ]

    const wrapper = mount(QuickStart, {
      props: {
        items: itemsWithZeroBadge
      },
      global: {
        plugins: [router]
      }
    })

    const quickItem = wrapper.find('.quick-item')
    expect(quickItem.find('.badge').exists()).toBe(true)
    expect(quickItem.find('.badge').text()).toBe('0')
  })

  it('has correct styling classes', () => {
    const wrapper = mount(QuickStart, {
      props: {
        items: mockItems
      },
      global: {
        plugins: [router]
      }
    })

    const quickItems = wrapper.findAll('.quick-item')
    quickItems.forEach(item => {
      expect(item.classes()).toContain('quick-item')
      expect(item.find('.item-content').classes()).toContain('item-content')
    })
  })

  it('handles missing item properties gracefully', () => {
    const incompleteItems = [
      {
        id: 'incomplete',
        name: 'Incomplete Item'
        // missing other properties
      }
    ]

    const wrapper = mount(QuickStart, {
      props: {
        items: incompleteItems
      },
      global: {
        plugins: [router]
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.item-name').text()).toBe('Incomplete Item')
  })
})