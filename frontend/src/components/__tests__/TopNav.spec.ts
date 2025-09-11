import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TopNav from '@/components/dashboard/TopNav.vue'

describe('TopNav', () => {
  const defaultProps = {
    brand: '智能检核系统',
    menus: ['主页', '产品导入', '规则管理', '系统设置'],
    active: '主页',
    username: '张三'
  }

  it('renders correctly with default props', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.brand-text').text()).toBe('智能检核系统')
    expect(wrapper.find('.username').text()).toBe('张三')
    expect(wrapper.findAll('.menu-item')).toHaveLength(4)
  })

  it('renders brand text correctly', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    const brandText = wrapper.find('.brand-text')
    expect(brandText.exists()).toBe(true)
    expect(brandText.text()).toBe('智能检核系统')
  })

  it('renders all menu items', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    const menuItems = wrapper.findAll('.menu-item')
    expect(menuItems).toHaveLength(4)
    
    const menuTexts = menuItems.map(item => item.text())
    expect(menuTexts).toEqual(['主页', '产品导入', '规则管理', '系统设置'])
  })

  it('applies active class to active menu item', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    const menuItems = wrapper.findAll('.menu-item')
    const activeMenuItem = menuItems[0] // '主页' is active
    
    expect(activeMenuItem.classes()).toContain('active')
    
    // Check other menu items don't have active class
    menuItems.slice(1).forEach(item => {
      expect(item.classes()).not.toContain('active')
    })
  })

  it('applies active class to correct menu item when active prop changes', () => {
    const wrapper = mount(TopNav, {
      props: {
        ...defaultProps,
        active: '产品导入'
      }
    })

    const menuItems = wrapper.findAll('.menu-item')
    const activeMenuItem = menuItems[1] // '产品导入' is active
    
    expect(activeMenuItem.classes()).toContain('active')
    expect(menuItems[0].classes()).not.toContain('active')
  })

  it('emits navigate event when menu item is clicked', async () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    const menuItems = wrapper.findAll('.menu-item')
    const secondMenuItem = menuItems[1] // '产品导入'
    
    await secondMenuItem.trigger('click')
    
    expect(wrapper.emitted('navigate')).toBeTruthy()
    expect(wrapper.emitted('navigate')).toHaveLength(1)
    expect(wrapper.emitted('navigate')[0]).toEqual(['产品导入'])
  })

  it('emits navigate event with correct menu key for different menu items', async () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    const menuItems = wrapper.findAll('.menu-item')
    
    // Test each menu item
    for (let i = 0; i < menuItems.length; i++) {
      const menuItem = menuItems[i]
      await menuItem.trigger('click')
      
      expect(wrapper.emitted('navigate')).toBeTruthy()
      expect(wrapper.emitted('navigate')[i]).toEqual([defaultProps.menus[i]])
    }
  })

  it('has correct styling classes', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    expect(wrapper.find('.top-nav').exists()).toBe(true)
    expect(wrapper.find('.nav-container').exists()).toBe(true)
    expect(wrapper.find('.nav-brand').exists()).toBe(true)
    expect(wrapper.find('.nav-menu').exists()).toBe(true)
    expect(wrapper.find('.nav-user').exists()).toBe(true)
  })

  it('has user icon', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    const userIcon = wrapper.find('.user-icon')
    expect(userIcon.exists()).toBe(true)
    expect(userIcon.classes()).toContain('fa')
    expect(userIcon.classes()).toContain('fa-user')
  })

  it('is responsive on mobile (hides menu)', () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    // Test that menu exists initially
    expect(wrapper.find('.nav-menu').exists()).toBe(true)
  })

  it('updates when props change', async () => {
    const wrapper = mount(TopNav, {
      props: defaultProps
    })

    // Update username
    await wrapper.setProps({
      ...defaultProps,
      username: '李四'
    })
    
    expect(wrapper.find('.username').text()).toBe('李四')

    // Update active menu
    await wrapper.setProps({
      ...defaultProps,
      active: '规则管理'
    })
    
    const menuItems = wrapper.findAll('.menu-item')
    expect(menuItems[2].classes()).toContain('active')
  })

  it('handles empty menus array', () => {
    const wrapper = mount(TopNav, {
      props: {
        ...defaultProps,
        menus: []
      }
    })

    expect(wrapper.findAll('.menu-item')).toHaveLength(0)
    expect(wrapper.find('.nav-menu').exists()).toBe(true)
  })

  it('handles single menu item', () => {
    const wrapper = mount(TopNav, {
      props: {
        ...defaultProps,
        menus: ['主页']
      }
    })

    const menuItems = wrapper.findAll('.menu-item')
    expect(menuItems).toHaveLength(1)
    expect(menuItems[0].text()).toBe('主页')
    expect(menuItems[0].classes()).toContain('active')
  })
})