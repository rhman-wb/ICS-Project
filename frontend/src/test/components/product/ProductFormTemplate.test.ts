import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import ProductFormTemplate from '@/components/ProductFormTemplate.vue'
import {
  createAgriculturalFormData,
  createFilingFormData,
  createProductAntdStubs
} from '@/test/utils/product-test-utils'
import { mountComponent, flushPromises } from '@/test/utils/test-utils'

// Mock Ant Design Vue components
const antdStubs = createProductAntdStubs()

// Mock Ant Design Icons
vi.mock('@ant-design/icons-vue', () => ({
  FileTextOutlined: { template: '<span class="file-text-icon" />' },
  AuditOutlined: { template: '<span class="audit-icon" />' },
  ExclamationCircleOutlined: { template: '<span class="exclamation-icon" />' }
}))

// Mock Ant Design Vue message and Modal
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  },
  Modal: {
    confirm: vi.fn((config) => {
      if (config.onOk) {
        config.onOk()
      }
    })
  }
}))

// Mock子组件
const MockAgriculturalProductForm = {
  template: '<div class="agricultural-form">Agricultural Form</div>',
  props: ['initialData', 'loading'],
  emits: ['submit', 'reset'],
  methods: {
    validate: vi.fn(() => Promise.resolve(true)),
    resetForm: vi.fn()
  }
}

const MockFilingProductForm = {
  template: '<div class="filing-form">Filing Form</div>',
  props: ['initialData', 'loading'],
  emits: ['submit', 'reset'],
  methods: {
    validate: vi.fn(() => Promise.resolve(true)),
    resetForm: vi.fn()
  }
}

describe('ProductFormTemplate', () => {
  let wrapper: any

  beforeEach(() => {
    const pinia = createPinia()
    setActivePinia(pinia)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mountComponent(ProductFormTemplate, {
      props,
      global: {
        stubs: {
          ...antdStubs,
          'AgriculturalProductForm': MockAgriculturalProductForm,
          'FilingProductForm': MockFilingProductForm
        }
      }
    })
  }

  describe('Component Initialization', () => {
    it('应该正确渲染基本结构', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.product-form-template').exists()).toBe(true)
      expect(wrapper.find('.template-selector').exists()).toBe(true)
      expect(wrapper.find('.template-options').exists()).toBe(true)
    })

    it('应该显示两个模板选项', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const options = wrapper.findAll('.template-option')
      expect(options).toHaveLength(2)

      const agriculturalOption = wrapper.find('.template-option.agricultural')
      const filingOption = wrapper.find('.template-option.filing')

      expect(agriculturalOption.exists()).toBe(true)
      expect(filingOption.exists()).toBe(true)
      expect(agriculturalOption.text()).toContain('农险产品信息登记表')
      expect(filingOption.text()).toContain('备案产品自主注册信息登记表')
    })

    it('应该使用初始模板', async () => {
      wrapper = createWrapper({ initialTemplate: 'agricultural' })
      await flushPromises()

      expect(wrapper.vm.selectedTemplate).toBe('agricultural')
      expect(wrapper.find('.form-container').exists()).toBe(true)
      expect(wrapper.find('.agricultural-form').exists()).toBe(true)
    })

    it('应该使用初始数据', async () => {
      const initialData = {
        agricultural: createAgriculturalFormData(),
        filing: createFilingFormData()
      }

      wrapper = createWrapper({ initialData })
      await flushPromises()

      expect(wrapper.vm.formData.agricultural).toEqual(initialData.agricultural)
      expect(wrapper.vm.formData.filing).toEqual(initialData.filing)
    })
  })

  describe('Template Selection', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该在没有表单数据时直接切换模板', async () => {
      const radioGroup = wrapper.find('.ant-radio-group')

      // 模拟选择农险模板
      await radioGroup.vm.$emit('change', { target: { value: 'agricultural' } })
      await nextTick()

      expect(wrapper.vm.selectedTemplate).toBe('agricultural')
      expect(wrapper.find('.agricultural-form').exists()).toBe(true)
      expect(wrapper.emitted('template-change')).toBeTruthy()
      expect(wrapper.emitted('template-change')[0]).toEqual(['agricultural'])
    })

    it('应该在有表单数据时显示确认弹窗', async () => {
      // 先选择农险模板并添加数据
      wrapper.vm.selectedTemplate = 'agricultural'
      wrapper.vm.formData.agricultural = createAgriculturalFormData()
      await nextTick()

      const radioGroup = wrapper.find('.ant-radio-group')

      // 尝试切换到备案模板
      await radioGroup.vm.$emit('change', { target: { value: 'filing' } })
      await nextTick()

      expect(wrapper.vm.switchConfirmVisible).toBe(true)
      expect(wrapper.vm.pendingTemplate).toBe('filing')
      expect(wrapper.vm.selectedTemplate).toBe('agricultural') // 应该保持原值
      expect(wrapper.find('.ant-modal').props('visible')).toBe(true)
    })

    it('应该正确显示模板显示名称', () => {
      expect(wrapper.vm.getTemplateDisplayName('agricultural')).toBe('农险产品信息登记表')
      expect(wrapper.vm.getTemplateDisplayName('filing')).toBe('备案产品自主注册信息登记表')
      expect(wrapper.vm.getTemplateDisplayName(null)).toBe('')
    })
  })

  describe('Template Switch Confirmation', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()

      // 设置初始状态：有数据的农险模板
      wrapper.vm.selectedTemplate = 'agricultural'
      wrapper.vm.formData.agricultural = createAgriculturalFormData()
      wrapper.vm.pendingTemplate = 'filing'
      wrapper.vm.switchConfirmVisible = true
      await nextTick()
    })

    it('应该在确认时切换模板并清空数据', async () => {
      const { message } = await import('ant-design-vue')

      await wrapper.vm.confirmTemplateSwitch()
      await nextTick()

      expect(wrapper.vm.selectedTemplate).toBe('filing')
      expect(wrapper.vm.pendingTemplate).toBe(null)
      expect(wrapper.vm.switchConfirmVisible).toBe(false)
      expect(Object.keys(wrapper.vm.formData.agricultural)).toHaveLength(0)
      expect(message.success).toHaveBeenCalledWith('已切换到备案产品自主注册信息登记表')
      expect(wrapper.emitted('template-change')).toBeTruthy()
    })

    it('应该在取消时重置状态', async () => {
      await wrapper.vm.cancelTemplateSwitch()
      await nextTick()

      expect(wrapper.vm.pendingTemplate).toBe(null)
      expect(wrapper.vm.switchConfirmVisible).toBe(false)
      expect(wrapper.vm.selectedTemplate).toBe('agricultural') // 保持原值
    })

    it('应该在模态框的取消按钮点击时取消切换', async () => {
      const modal = wrapper.find('.ant-modal')
      await modal.vm.$emit('cancel')

      expect(wrapper.vm.pendingTemplate).toBe(null)
      expect(wrapper.vm.switchConfirmVisible).toBe(false)
    })

    it('应该在模态框的确认按钮点击时确认切换', async () => {
      const modal = wrapper.find('.ant-modal')
      await modal.vm.$emit('ok')

      expect(wrapper.vm.selectedTemplate).toBe('filing')
      expect(wrapper.vm.switchConfirmVisible).toBe(false)
    })
  })

  describe('Form Data Detection', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该在没有表单数据时返回false', () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      expect(wrapper.vm.hasFormData).toBe(false)
    })

    it('应该在有表单数据时返回true', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      wrapper.vm.formData.agricultural = { productName: '测试产品' }
      await nextTick()

      expect(wrapper.vm.hasFormData).toBe(true)
    })

    it('应该忽略空值', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      wrapper.vm.formData.agricultural = {
        productName: '',
        remark: null,
        year: undefined
      }
      await nextTick()

      expect(wrapper.vm.hasFormData).toBe(false)
    })

    it('应该在没有选中模板时返回false', () => {
      wrapper.vm.selectedTemplate = null
      wrapper.vm.formData.agricultural = { productName: '测试产品' }

      expect(wrapper.vm.hasFormData).toBe(false)
    })
  })

  describe('Form Submission', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理农险产品表单提交', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      const formData = createAgriculturalFormData()
      const agriculturalForm = wrapper.findComponent({ name: 'AgriculturalProductForm' })

      await agriculturalForm.vm.$emit('submit', formData)

      expect(wrapper.vm.loading).toBe(true)

      // 等待异步操作完成
      await new Promise(resolve => setTimeout(resolve, 1100))

      expect(wrapper.vm.loading).toBe(false)
      expect(wrapper.emitted('submit')).toBeTruthy()
      expect(wrapper.emitted('submit')[0]).toEqual([{
        type: 'agricultural',
        formData: { ...formData, productType: 'AGRICULTURAL' }
      }])
    })

    it('应该处理备案产品表单提交', async () => {
      wrapper.vm.selectedTemplate = 'filing'
      await nextTick()

      const formData = createFilingFormData()
      const filingForm = wrapper.findComponent({ name: 'FilingProductForm' })

      await filingForm.vm.$emit('submit', formData)

      expect(wrapper.vm.loading).toBe(true)

      // 等待异步操作完成
      await new Promise(resolve => setTimeout(resolve, 1100))

      expect(wrapper.vm.loading).toBe(false)
      expect(wrapper.emitted('submit')).toBeTruthy()
      expect(wrapper.emitted('submit')[0]).toEqual([{
        type: 'filing',
        formData: { ...formData, productType: 'FILING' }
      }])
    })

    it('应该在autoSave时显示自动保存消息', async () => {
      const { message } = await import('ant-design-vue')
      wrapper = createWrapper({ autoSave: true })
      await flushPromises()

      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      const formData = createAgriculturalFormData()
      await wrapper.vm.handleAgriculturalSubmit(formData)

      // 等待异步操作完成
      await new Promise(resolve => setTimeout(resolve, 1100))

      expect(message.success).toHaveBeenCalledWith('农险产品信息已自动保存')
    })
  })

  describe('Form Reset', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理农险表单重置', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      wrapper.vm.formData.agricultural = createAgriculturalFormData()
      await nextTick()

      const agriculturalForm = wrapper.findComponent({ name: 'AgriculturalProductForm' })
      await agriculturalForm.vm.$emit('reset')

      expect(Object.keys(wrapper.vm.formData.agricultural)).toHaveLength(0)
      expect(wrapper.emitted('form-reset')).toBeTruthy()
      expect(wrapper.emitted('form-reset')[0]).toEqual(['agricultural'])
    })

    it('应该处理备案表单重置', async () => {
      wrapper.vm.selectedTemplate = 'filing'
      wrapper.vm.formData.filing = createFilingFormData()
      await nextTick()

      const filingForm = wrapper.findComponent({ name: 'FilingProductForm' })
      await filingForm.vm.$emit('reset')

      expect(Object.keys(wrapper.vm.formData.filing)).toHaveLength(0)
      expect(wrapper.emitted('form-reset')).toBeTruthy()
      expect(wrapper.emitted('form-reset')[0]).toEqual(['filing'])
    })

    it('应该在无效模板时不执行重置', async () => {
      await wrapper.vm.handleFormReset(null)

      expect(wrapper.emitted('form-reset')).toBeFalsy()
    })
  })

  describe('Form References and Validation', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该获取农险表单引用', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      const formRef = wrapper.vm.getCurrentFormRef()
      expect(formRef).toBeDefined()
      expect(formRef.validate).toBeDefined()
    })

    it('应该获取备案表单引用', async () => {
      wrapper.vm.selectedTemplate = 'filing'
      await nextTick()

      const formRef = wrapper.vm.getCurrentFormRef()
      expect(formRef).toBeDefined()
      expect(formRef.validate).toBeDefined()
    })

    it('应该在无表单时返回undefined', () => {
      wrapper.vm.selectedTemplate = null

      const formRef = wrapper.vm.getCurrentFormRef()
      expect(formRef).toBeUndefined()
    })

    it('应该验证当前表单', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      const result = await wrapper.vm.validateCurrentForm()
      expect(result).toBe(true)
    })

    it('应该在没有表单实例时抛出错误', async () => {
      wrapper.vm.selectedTemplate = null

      await expect(wrapper.vm.validateCurrentForm()).rejects.toThrow('没有可用的表单实例')
    })

    it('应该在表单验证失败时抛出错误', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      // Mock表单验证失败
      const formRef = wrapper.vm.getCurrentFormRef()
      formRef.validate = vi.fn(() => Promise.reject(new Error('Validation failed')))

      await expect(wrapper.vm.validateCurrentForm()).rejects.toThrow('表单验证失败')
    })
  })

  describe('Component Exposure', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该暴露selectedTemplate', () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      expect(wrapper.vm.selectedTemplate).toBe('agricultural')
    })

    it('应该暴露formData', () => {
      const testData = createAgriculturalFormData()
      wrapper.vm.formData.agricultural = testData
      expect(wrapper.vm.formData.agricultural).toEqual(testData)
    })

    it('应该暴露switchTemplate方法', () => {
      wrapper.vm.switchTemplate('filing')
      expect(wrapper.vm.selectedTemplate).toBe('filing')
    })

    it('应该暴露getCurrentFormData方法', () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      const testData = createAgriculturalFormData()
      wrapper.vm.formData.agricultural = testData

      expect(wrapper.vm.getCurrentFormData()).toEqual(testData)
    })

    it('应该在无选中模板时返回null', () => {
      wrapper.vm.selectedTemplate = null
      expect(wrapper.vm.getCurrentFormData()).toBe(null)
    })

    it('应该暴露resetCurrentForm方法', () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      wrapper.vm.formData.agricultural = createAgriculturalFormData()

      wrapper.vm.resetCurrentForm()

      expect(Object.keys(wrapper.vm.formData.agricultural)).toHaveLength(0)
    })
  })

  describe('Props Watching', () => {
    it('应该监听初始数据变化', async () => {
      const initialData = {
        agricultural: createAgriculturalFormData(),
        filing: createFilingFormData()
      }

      wrapper = createWrapper({ initialData })
      await flushPromises()

      // 更新props
      const newData = {
        agricultural: { ...createAgriculturalFormData(), productName: '更新后的产品' },
        filing: { ...createFilingFormData(), templateName: '更新后的模板' }
      }

      await wrapper.setProps({ initialData: newData })
      await nextTick()

      expect(wrapper.vm.formData.agricultural.productName).toBe('更新后的产品')
      expect(wrapper.vm.formData.filing.templateName).toBe('更新后的模板')
    })
  })

  describe('Dynamic Form Rendering', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该在未选择模板时不显示表单', () => {
      expect(wrapper.find('.form-container').exists()).toBe(false)
    })

    it('应该在选择农险模板时显示农险表单', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      expect(wrapper.find('.form-container').exists()).toBe(true)
      expect(wrapper.find('.agricultural-form').exists()).toBe(true)
      expect(wrapper.find('.filing-form').exists()).toBe(false)
    })

    it('应该在选择备案模板时显示备案表单', async () => {
      wrapper.vm.selectedTemplate = 'filing'
      await nextTick()

      expect(wrapper.find('.form-container').exists()).toBe(true)
      expect(wrapper.find('.filing-form').exists()).toBe(true)
      expect(wrapper.find('.agricultural-form').exists()).toBe(false)
    })

    it('应该正确传递props到子表单', async () => {
      const initialData = { agricultural: createAgriculturalFormData() }
      wrapper = createWrapper({ initialData })
      wrapper.vm.selectedTemplate = 'agricultural'
      await nextTick()

      const agriculturalForm = wrapper.findComponent({ name: 'AgriculturalProductForm' })
      expect(agriculturalForm.props('initialData')).toEqual(initialData.agricultural)
      expect(agriculturalForm.props('loading')).toBe(wrapper.vm.loading)
    })
  })

  describe('Error Handling', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理模板切换时的错误', async () => {
      // 模拟错误情况
      wrapper.vm.pendingTemplate = null

      // 调用确认切换，但没有待切换的模板
      await wrapper.vm.confirmTemplateSwitch()

      // 应该不会抛出错误，也不会改变状态
      expect(wrapper.vm.selectedTemplate).toBe(null)
    })

    it('应该处理表单数据更新错误', async () => {
      wrapper.vm.selectedTemplate = 'agricultural'

      // 模拟formData为只读
      Object.freeze(wrapper.vm.formData.agricultural)

      // 尝试更新数据时应该不会抛出错误
      expect(() => {
        wrapper.vm.handleAgriculturalSubmit(createAgriculturalFormData())
      }).not.toThrow()
    })
  })
})