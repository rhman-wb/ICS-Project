import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory, Router } from 'vue-router'
import RecentActivities from '@/components/dashboard/RecentActivities.vue'

// Create a mock router for testing
let router: Router

const createMockRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/product/1', name: 'ProductDetail', component: { template: '<div>Product Detail</div>' } },
      { path: '/rules/1', name: 'RuleDetail', component: { template: '<div>Rule Detail</div>' } },
      { path: '/', name: 'Home', component: { template: '<div>Home</div>' } }
    ]
  })
}

describe('RecentActivities', () => {
  beforeEach(() => {
    router = createMockRouter()
  })

  const mockActivities = [
    {
      id: '1',
      type: 'product',
      title: '新产品导入完成',
      description: '成功导入 256 个新产品',
      time: '2 小时前',
      link: '/product/1',
      icon: 'fa-plus-circle',
      status: 'success'
    },
    {
      id: '2',
      type: 'rule',
      title: '检测规则更新',
      description: '更新了 15 个检测规则',
      time: '5 小时前',
      link: '/rules/1',
      icon: 'fa-edit',
      status: 'warning'
    },
    {
      id: '3',
      type: 'system',
      title: '系统维护完成',
      description: '系统例行维护已完成',
      time: '1 天前',
      link: null,
      icon: 'fa-cog',
      status: 'info'
    },
    {
      id: '4',
      type: 'error',
      title: '数据同步失败',
      description: '部分产品数据同步失败',
      time: '2 天前',
      link: '/error/1',
      icon: 'fa-exclamation-triangle',
      status: 'error'
    }
  ]

  it('renders correctly with activities', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.recent-activities').exists()).toBe(true)
  })

  it('renders all activity items', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    expect(activityItems).toHaveLength(4)
  })

  it('displays correct activity titles', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityTitles = wrapper.findAll('.activity-title')
    expect(activityTitles).toHaveLength(4)
    
    const expectedTitles = ['新产品导入完成', '检测规则更新', '系统维护完成', '数据同步失败']
    const actualTitles = activityTitles.map(title => title.text())
    
    expect(actualTitles).toEqual(expectedTitles)
  })

  it('displays correct activity descriptions', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityDescriptions = wrapper.findAll('.activity-description')
    expect(activityDescriptions).toHaveLength(4)
    
    const expectedDescriptions = [
      '成功导入 256 个新产品',
      '更新了 15 个检测规则',
      '系统例行维护已完成',
      '部分产品数据同步失败'
    ]
    const actualDescriptions = activityDescriptions.map(desc => desc.text())
    
    expect(actualDescriptions).toEqual(expectedDescriptions)
  })

  it('displays correct activity times', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityTimes = wrapper.findAll('.activity-time')
    expect(activityTimes).toHaveLength(4)
    
    const expectedTimes = ['2 小时前', '5 小时前', '1 天前', '2 天前']
    const actualTimes = activityTimes.map(time => time.text())
    
    expect(actualTimes).toEqual(expectedTimes)
  })

  it('displays correct icons for each activity', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityIcons = wrapper.findAll('.activity-icon')
    expect(activityIcons).toHaveLength(4)
    
    const expectedIcons = ['fa-plus-circle', 'fa-edit', 'fa-cog', 'fa-exclamation-triangle']
    const actualIcons = activityIcons.map(icon => icon.classes())
    
    expectedIcons.forEach((expectedIcon, index) => {
      expect(actualIcons[index]).toContain(expectedIcon)
    })
  })

  it('displays correct status indicators', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    
    expect(activityItems[0].classes()).toContain('status-success')
    expect(activityItems[1].classes()).toContain('status-warning')
    expect(activityItems[2].classes()).toContain('status-info')
    expect(activityItems[3].classes()).toContain('status-error')
  })

  it('has clickable items with links', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    
    // Items with links should be clickable
    expect(activityItems[0].classes()).toContain('clickable')
    expect(activityItems[1].classes()).toContain('clickable')
    expect(activityItems[3].classes()).toContain('clickable')
    
    // Item without link should not be clickable
    expect(activityItems[2].classes()).not.toContain('clickable')
  })

  it('navigates to correct route when clickable item is clicked', async () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    const pushSpy = vi.spyOn(router, 'push')
    
    await activityItems[0].trigger('click') // Click '新产品导入完成'
    
    expect(pushSpy).toHaveBeenCalledWith('/product/1')
  })

  it('does not navigate when non-clickable item is clicked', async () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    const pushSpy = vi.spyOn(router, 'push')
    
    await activityItems[2].trigger('click') // Click '系统维护完成' (no link)
    
    expect(pushSpy).not.toHaveBeenCalled()
  })

  it('emits open event when clickable item is clicked', async () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      },
      global: {
        plugins: [router]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    
    await activityItems[0].trigger('click') // Click '新产品导入完成'
    
    expect(wrapper.emitted('open')).toBeTruthy()
    expect(wrapper.emitted('open')).toHaveLength(1)
    expect(wrapper.emitted('open')[0]).toEqual(['/product/1'])
  })

  it('emits open event with correct link for different items', async () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    
    // Test clickable items
    const clickableItems = [0, 1, 3] // indices of items with links
    clickableItems.forEach((index, i) => {
      activityItems[index].trigger('click')
      expect(wrapper.emitted('open')[i]).toEqual([mockActivities[index].link])
    })
  })

  it('emits viewMore event when view more button is clicked', async () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    const viewMoreBtn = wrapper.find('.view-more-btn')
    await viewMoreBtn.trigger('click')
    
    expect(wrapper.emitted('viewMore')).toBeTruthy()
    expect(wrapper.emitted('viewMore')).toHaveLength(1)
  })

  it('has view more button', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    const viewMoreBtn = wrapper.find('.view-more-btn')
    expect(viewMoreBtn.exists()).toBe(true)
    expect(viewMoreBtn.text()).toBe('查看更多')
  })

  it('has correct activity structure', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    activityItems.forEach(item => {
      expect(item.find('.activity-icon').exists()).toBe(true)
      expect(item.find('.activity-content').exists()).toBe(true)
      expect(item.find('.activity-title').exists()).toBe(true)
      expect(item.find('.activity-description').exists()).toBe(true)
      expect(item.find('.activity-time').exists()).toBe(true)
    })
  })

  it('has correct status colors', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    
    // Check status classes
    expect(activityItems[0].classes()).toContain('status-success')
    expect(activityItems[1].classes()).toContain('status-warning')
    expect(activityItems[2].classes()).toContain('status-info')
    expect(activityItems[3].classes()).toContain('status-error')
  })

  it('handles empty activities array', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: []
      }
    })

    expect(wrapper.findAll('.activity-item')).toHaveLength(0)
    expect(wrapper.find('.activities-list').exists()).toBe(true)
  })

  it('handles single activity', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: [mockActivities[0]]
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    expect(activityItems).toHaveLength(1)
    expect(activityItems[0].find('.activity-title').text()).toBe('新产品导入完成')
  })

  it('updates when activities change', async () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: [mockActivities[0]]
      }
    })

    expect(wrapper.findAll('.activity-item')).toHaveLength(1)
    
    // Add more activities
    await wrapper.setProps({
      activities: mockActivities.slice(0, 2)
    })
    
    expect(wrapper.findAll('.activity-item')).toHaveLength(2)
  })

  it('handles activities without links', () => {
    const activitiesWithoutLinks = mockActivities.map(activity => ({
      ...activity,
      link: null
    }))

    const wrapper = mount(RecentActivities, {
      props: {
        activities: activitiesWithoutLinks
      }
    })

    const activityItems = wrapper.findAll('.activity-item')
    activityItems.forEach(item => {
      expect(item.classes()).not.toContain('clickable')
    })
  })

  it('handles activities without status', () => {
    const activitiesWithoutStatus = mockActivities.map(activity => {
      const { status, ...rest } = activity
      return rest
    })

    const wrapper = mount(RecentActivities, {
      props: {
        activities: activitiesWithoutStatus
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.findAll('.activity-item')).toHaveLength(4)
  })

  it('handles missing activity properties gracefully', () => {
    const incompleteActivities = [
      {
        id: '1',
        title: 'Incomplete Activity'
        // missing other properties
      }
    ]

    const wrapper = mount(RecentActivities, {
      props: {
        activities: incompleteActivities
      }
    })

    // Should still render without crashing
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.activity-title').text()).toBe('Incomplete Activity')
  })

  it('has correct container structure', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    expect(wrapper.find('.activities-header').exists()).toBe(true)
    expect(wrapper.find('.activities-list').exists()).toBe(true)
    expect(wrapper.find('.activities-footer').exists()).toBe(true)
  })

  it('has scrollable content area', () => {
    const wrapper = mount(RecentActivities, {
      props: {
        activities: mockActivities
      }
    })

    const activitiesList = wrapper.find('.activities-list')
    expect(activitiesList.exists()).toBe(true)
  })
})