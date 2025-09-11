import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { message } from 'ant-design-vue'
import QuickStartSection from '@/components/dashboard/QuickStartSection.vue'

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      info: vi.fn(),
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    }
  }
})

describe('QuickStartSection', () => {
  const createWrapper = (props = {}) => {
    return mount(QuickStartSection, {
      props
    })
  }

  it('renders correctly', () => {
    const wrapper = createWrapper()
    
    expect(wrapper.find('.quick-start-section').exists()).toBe(true)
    expect(wrapper.find('.section-title').text()).toBe('快速开始 / 便捷导航')
  })

  it('displays all action buttons', () => {
    const wrapper = createWrapper()
    
    // 检查是否包含所有按钮文本
    const text = wrapper.text()
    const buttonTexts = ['导入产品', '创建规则', '批量导入', '下载模板', '操作四', '操作五']
    buttonTexts.forEach(buttonText => {
      expect(text).toContain(buttonText)
    })
  })

  it('shows badge counts for relevant buttons', () => {
    const wrapper = createWrapper()
    
    // 检查徽章数字是否正确显示（通过HTML内容检查）
    const html = wrapper.html()
    expect(html).toContain('24') // pendingImports
    expect(html).toContain('8')  // pendingRules
    expect(html).toContain('12') // batchTasks
    expect(html).toContain('5')  // otherTasks
  })

  it('handles import product button click', async () => {
    const wrapper = createWrapper()
    
    // 查找并点击导入产品按钮
    const importButton = wrapper.find('.quick-action-btn')
    await importButton.trigger('click')
    
    expect(message.info).toHaveBeenCalledWith('跳转到产品导入页面')
  })

  it('handles create rule button click', async () => {
    const wrapper = createWrapper()
    
    // 查找创建规则按钮（第二个按钮）
    const buttons = wrapper.findAll('.quick-action-btn')
    if (buttons.length > 1) {
      await buttons[1].trigger('click')
      expect(message.info).toHaveBeenCalledWith('跳转到规则创建页面')
    }
  })

  it('handles batch import button click', async () => {
    const wrapper = createWrapper()
    
    // 查找批量导入按钮（第三个按钮）
    const buttons = wrapper.findAll('.quick-action-btn')
    if (buttons.length > 2) {
      await buttons[2].trigger('click')
      expect(message.info).toHaveBeenCalledWith('跳转到批量导入页面')
    }
  })

  it('handles download template button click', async () => {
    const wrapper = createWrapper()
    
    // 查找下载模板按钮（第四个按钮）
    const buttons = wrapper.findAll('.quick-action-btn')
    if (buttons.length > 3) {
      await buttons[3].trigger('click')
      expect(message.success).toHaveBeenCalledWith('模板下载已开始')
    }
  })

  it('handles operation 4 button click', async () => {
    const wrapper = createWrapper()
    
    // 查找操作四按钮（第五个按钮）
    const buttons = wrapper.findAll('.quick-action-btn')
    if (buttons.length > 4) {
      await buttons[4].trigger('click')
      expect(message.info).toHaveBeenCalledWith('执行操作四')
    }
  })

  it('handles operation 5 button click', async () => {
    const wrapper = createWrapper()
    
    // 查找操作五按钮（第六个按钮）
    const buttons = wrapper.findAll('.quick-action-btn')
    if (buttons.length > 5) {
      await buttons[5].trigger('click')
      expect(message.info).toHaveBeenCalledWith('执行操作五')
    }
  })

  it('has correct component name', () => {
    const wrapper = createWrapper()
    
    expect(wrapper.vm.$options.name).toBe('QuickStartSection')
  })

  it('maintains responsive layout structure', () => {
    const wrapper = createWrapper()
    
    // 检查是否有正确的响应式布局结构
    expect(wrapper.find('.quick-start-content').exists()).toBe(true)
    expect(wrapper.find('.quick-action-card').exists()).toBe(true)
  })

  it('includes placeholder cards for layout balance', () => {
    const wrapper = createWrapper()
    
    // 检查是否有占位卡片保持布局平衡
    const text = wrapper.text()
    expect(text).toContain('操作四')
    expect(text).toContain('操作五')
  })
})