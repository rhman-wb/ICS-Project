import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import DocumentValidation from '@/components/DocumentValidation.vue'
import { createProductAntdStubs } from '@/test/utils/product-test-utils'
import { mountComponent, flushPromises } from '@/test/utils/test-utils'

// Mock Ant Design Vue components
const antdStubs = createProductAntdStubs()

// Mock Ant Design Icons
vi.mock('@ant-design/icons-vue', () => ({
  ReloadOutlined: { template: '<span class="reload-icon" />' },
  ExclamationCircleOutlined: { template: '<span class="exclamation-icon" />' },
  WarningOutlined: { template: '<span class="warning-icon" />' },
  CheckCircleOutlined: { template: '<span class="check-icon" />' }
}))

// Mock Ant Design Vue message
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
}))

// Mock DocumentValidationService
const mockValidationService = {
  validateProductDocuments: vi.fn()
}

vi.mock('@/api/documentValidation', () => ({
  default: mockValidationService
}))

// 测试数据
const createValidationResult = (isValid = true, errors = [], warnings = []) => ({
  isValid,
  errors,
  warnings,
  summary: {
    totalDocuments: 4,
    requiredDocuments: 3,
    uploadedDocuments: isValid ? 4 : 2,
    completenessPercentage: isValid ? 100 : 50
  }
})

const createValidationError = (overrides = {}) => ({
  errorCode: 'DOC_001',
  documentType: 'TERMS',
  severity: 'HIGH',
  message: '条款文档缺失必要章节',
  suggestion: '请确保条款包含保险责任、责任免除等必要章节',
  ...overrides
})

const createValidationWarning = (overrides = {}) => ({
  warningCode: 'WARN_001',
  documentType: 'RATE_TABLE',
  message: '费率表格式建议优化',
  recommendation: '建议使用标准Excel模板格式',
  ...overrides
})

describe('DocumentValidation', () => {
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
    return mountComponent(DocumentValidation, {
      props: { productId: 'test-product-001', ...props },
      global: {
        stubs: {
          ...antdStubs,
          'a-statistic': {
            template: '<div class="ant-statistic"><div class="title">{{ title }}</div><div class="value" :style="valueStyle">{{ value }}{{ suffix }}</div></div>',
            props: ['title', 'value', 'suffix', 'valueStyle']
          },
          'a-result': {
            template: '<div class="ant-result"><div class="result-icon"><slot name="icon" /></div><div class="result-title">{{ title }}</div><div class="result-subtitle">{{ subTitle }}</div></div>',
            props: ['status', 'title', 'subTitle']
          },
          'a-badge': {
            template: '<span class="ant-badge"><slot />{{ count > 0 ? count : "" }}</span>',
            props: ['count', 'status']
          },
          'a-spin': {
            template: '<div class="ant-spin" :class="{ spinning: true }"><slot /></div>',
            props: ['size']
          }
        }
      }
    })
  }

  describe('Component Initialization', () => {
    it('应该正确渲染基本结构', async () => {
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult()
      })

      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.document-validation').exists()).toBe(true)
    })

    it('应该在autoValidate为true时自动执行校验', async () => {
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult()
      })

      wrapper = createWrapper({ autoValidate: true })
      await flushPromises()

      expect(mockValidationService.validateProductDocuments).toHaveBeenCalledWith('test-product-001')
    })

    it('应该在autoValidate为false时不自动执行校验', async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()

      expect(mockValidationService.validateProductDocuments).not.toHaveBeenCalled()
    })

    it('应该在没有productId时不执行校验', async () => {
      wrapper = createWrapper({ productId: '', autoValidate: true })
      await flushPromises()

      expect(mockValidationService.validateProductDocuments).not.toHaveBeenCalled()
    })
  })

  describe('Validation Success State', () => {
    beforeEach(async () => {
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult(true)
      })

      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该显示校验概览', () => {
      expect(wrapper.find('.validation-overview').exists()).toBe(true)
      expect(wrapper.find('.validation-card').exists()).toBe(true)
    })

    it('应该显示正确的概览标题', () => {
      const title = wrapper.vm.overviewTitle
      expect(title).toBe('文档校验通过')
    })

    it('应该显示校验统计信息', () => {
      const statistics = wrapper.findAll('.ant-statistic')
      expect(statistics).toHaveLength(4)

      // 检查统计数据
      expect(statistics[0].props('title')).toBe('文档总数')
      expect(statistics[0].props('value')).toBe(4)

      expect(statistics[1].props('title')).toBe('必需文档')
      expect(statistics[1].props('value')).toBe(3)

      expect(statistics[2].props('title')).toBe('已上传')
      expect(statistics[2].props('value')).toBe(4)

      expect(statistics[3].props('title')).toBe('完整性')
      expect(statistics[3].props('value')).toBe(100)
      expect(statistics[3].props('suffix')).toBe('%')
    })

    it('应该显示成功结果', () => {
      expect(wrapper.find('.validation-success').exists()).toBe(true)
      expect(wrapper.find('.ant-result').exists()).toBe(true)
      expect(wrapper.find('.ant-result').props('title')).toBe('文档校验通过')
    })

    it('应该不显示错误和警告', () => {
      expect(wrapper.find('.validation-errors').exists()).toBe(false)
      expect(wrapper.find('.validation-warnings').exists()).toBe(false)
    })
  })

  describe('Validation Error State', () => {
    beforeEach(async () => {
      const errors = [
        createValidationError(),
        createValidationError({
          errorCode: 'DOC_002',
          documentType: 'FEASIBILITY_REPORT',
          severity: 'CRITICAL',
          message: '可行性报告格式不正确'
        })
      ]

      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult(false, errors)
      })

      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该显示错误列表', () => {
      expect(wrapper.find('.validation-errors').exists()).toBe(true)
      expect(wrapper.find('.error-card').exists()).toBe(true)
    })

    it('应该显示错误数量徽章', () => {
      const badge = wrapper.find('.validation-errors .ant-badge')
      expect(badge.props('count')).toBe(2)
      expect(badge.props('status')).toBe('error')
    })

    it('应该显示所有错误项', () => {
      const errorItems = wrapper.findAll('.error-item')
      expect(errorItems).toHaveLength(2)
    })

    it('应该正确显示错误信息', () => {
      const firstError = wrapper.find('.error-item')

      // 检查严重级别标签
      const severityTag = firstError.find('.severity-tag')
      expect(severityTag.props('color')).toBe('red')

      // 检查文档类型
      const typeTag = firstError.find('.document-type-tag')
      expect(typeTag.text()).toBe('条款')

      // 检查错误代码
      const errorCode = firstError.find('.error-code')
      expect(errorCode.text()).toBe('DOC_001')

      // 检查错误消息
      const message = firstError.find('.error-message')
      expect(message.text()).toContain('条款文档缺失必要章节')

      // 检查建议
      const suggestion = firstError.find('.suggestion-content')
      expect(suggestion.text()).toBe('请确保条款包含保险责任、责任免除等必要章节')
    })

    it('应该显示正确的概览标题', () => {
      expect(wrapper.vm.overviewTitle).toBe('文档校验未通过')
    })
  })

  describe('Validation Warning State', () => {
    beforeEach(async () => {
      const warnings = [
        createValidationWarning(),
        createValidationWarning({
          warningCode: 'WARN_002',
          documentType: 'ACTUARIAL_REPORT',
          message: '精算报告数据精度建议提高'
        })
      ]

      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult(true, [], warnings)
      })

      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该显示警告列表', () => {
      expect(wrapper.find('.validation-warnings').exists()).toBe(true)
      expect(wrapper.find('.warning-card').exists()).toBe(true)
    })

    it('应该显示警告数量徽章', () => {
      const badge = wrapper.find('.validation-warnings .ant-badge')
      expect(badge.props('count')).toBe(2)
      expect(badge.props('status')).toBe('warning')
    })

    it('应该显示所有警告项', () => {
      const warningItems = wrapper.findAll('.warning-item')
      expect(warningItems).toHaveLength(2)
    })

    it('应该正确显示警告信息', () => {
      const firstWarning = wrapper.find('.warning-item')

      // 检查警告标签
      const warningTag = firstWarning.find('.warning-tag')
      expect(warningTag.props('color')).toBe('orange')
      expect(warningTag.text()).toBe('警告')

      // 检查文档类型
      const typeTag = firstWarning.find('.document-type-tag')
      expect(typeTag.text()).toBe('费率表')

      // 检查警告代码
      const warningCode = firstWarning.find('.warning-code')
      expect(warningCode.text()).toBe('WARN_001')

      // 检查警告消息
      const message = firstWarning.find('.warning-message')
      expect(message.text()).toContain('费率表格式建议优化')

      // 检查建议
      const recommendation = firstWarning.find('.recommendation-content')
      expect(recommendation.text()).toBe('建议使用标准Excel模板格式')
    })
  })

  describe('Loading State', () => {
    it('应该在校验时显示加载状态', async () => {
      // 模拟长时间的异步操作
      mockValidationService.validateProductDocuments.mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve({
          success: true,
          data: createValidationResult()
        }), 100))
      )

      wrapper = createWrapper()

      // 立即检查loading状态
      expect(wrapper.vm.validating).toBe(true)
      expect(wrapper.find('.validation-loading').exists()).toBe(true)
      expect(wrapper.find('.ant-spin').exists()).toBe(true)

      await flushPromises()

      // 等待完成后检查状态
      expect(wrapper.vm.validating).toBe(false)
      expect(wrapper.find('.validation-loading').exists()).toBe(false)
    })
  })

  describe('Utility Methods', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()
    })

    it('应该正确映射文档类型', () => {
      expect(wrapper.vm.getDocumentTypeText('TERMS')).toBe('条款')
      expect(wrapper.vm.getDocumentTypeText('FEASIBILITY_REPORT')).toBe('可行性报告')
      expect(wrapper.vm.getDocumentTypeText('ACTUARIAL_REPORT')).toBe('精算报告')
      expect(wrapper.vm.getDocumentTypeText('RATE_TABLE')).toBe('费率表')
      expect(wrapper.vm.getDocumentTypeText('UNKNOWN')).toBe('UNKNOWN')
    })

    it('应该正确映射严重级别', () => {
      expect(wrapper.vm.getSeverityText('LOW')).toBe('低')
      expect(wrapper.vm.getSeverityText('MEDIUM')).toBe('中')
      expect(wrapper.vm.getSeverityText('HIGH')).toBe('高')
      expect(wrapper.vm.getSeverityText('CRITICAL')).toBe('严重')
      expect(wrapper.vm.getSeverityText('UNKNOWN')).toBe('UNKNOWN')
    })

    it('应该正确映射严重级别颜色', () => {
      expect(wrapper.vm.getSeverityColor('LOW')).toBe('blue')
      expect(wrapper.vm.getSeverityColor('MEDIUM')).toBe('orange')
      expect(wrapper.vm.getSeverityColor('HIGH')).toBe('red')
      expect(wrapper.vm.getSeverityColor('CRITICAL')).toBe('magenta')
      expect(wrapper.vm.getSeverityColor('UNKNOWN')).toBe('default')
    })

    it('应该正确计算完整性颜色', () => {
      // 测试100%完整性
      wrapper.vm.validationResult = createValidationResult(true)
      expect(wrapper.vm.getCompletenessColor()).toEqual({ color: '#52c41a' })

      // 测试80%以上完整性
      wrapper.vm.validationResult = createValidationResult(false)
      wrapper.vm.validationResult.summary.completenessPercentage = 90
      expect(wrapper.vm.getCompletenessColor()).toEqual({ color: '#faad14' })

      // 测试80%以下完整性
      wrapper.vm.validationResult.summary.completenessPercentage = 60
      expect(wrapper.vm.getCompletenessColor()).toEqual({ color: '#ff4d4f' })

      // 测试无结果
      wrapper.vm.validationResult = null
      expect(wrapper.vm.getCompletenessColor()).toEqual({ color: '#666' })
    })
  })

  describe('Manual Validation', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()
    })

    it('应该通过重新校验按钮执行校验', async () => {
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult()
      })

      await wrapper.vm.performValidation()
      await nextTick()

      expect(mockValidationService.validateProductDocuments).toHaveBeenCalledWith('test-product-001')
      expect(wrapper.vm.validationResult).toBeTruthy()
    })

    it('应该通过暴露的validateNow方法执行校验', async () => {
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult()
      })

      await wrapper.vm.validateNow()
      await nextTick()

      expect(mockValidationService.validateProductDocuments).toHaveBeenCalledWith('test-product-001')
    })
  })

  describe('Error Handling', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()
    })

    it('应该处理API调用失败', async () => {
      const { message } = await import('ant-design-vue')
      mockValidationService.validateProductDocuments.mockRejectedValue(
        new Error('Network error')
      )

      await wrapper.vm.performValidation()

      expect(wrapper.vm.validating).toBe(false)
      expect(message.error).toHaveBeenCalledWith('Network error')
      expect(wrapper.emitted('validationError')).toBeTruthy()
    })

    it('应该处理API返回错误响应', async () => {
      const { message } = await import('ant-design-vue')
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: false,
        message: '产品不存在'
      })

      await wrapper.vm.performValidation()

      expect(message.error).toHaveBeenCalledWith('产品不存在')
      expect(wrapper.emitted('validationError')).toBeTruthy()
    })

    it('应该在productId为空时显示错误', async () => {
      const { message } = await import('ant-design-vue')
      wrapper = createWrapper({ productId: '', autoValidate: false })
      await flushPromises()

      await wrapper.vm.performValidation()

      expect(message.error).toHaveBeenCalledWith('产品ID不能为空')
      expect(mockValidationService.validateProductDocuments).not.toHaveBeenCalled()
    })
  })

  describe('Event Emissions', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()
    })

    it('应该在校验完成时触发validationComplete事件', async () => {
      const result = createValidationResult()
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: result
      })

      await wrapper.vm.performValidation()

      expect(wrapper.emitted('validationComplete')).toBeTruthy()
      expect(wrapper.emitted('validationComplete')[0]).toEqual([result])
    })

    it('应该在校验错误时触发validationError事件', async () => {
      const error = new Error('Validation failed')
      mockValidationService.validateProductDocuments.mockRejectedValue(error)

      await wrapper.vm.performValidation()

      expect(wrapper.emitted('validationError')).toBeTruthy()
      expect(wrapper.emitted('validationError')[0][0]).toBeInstanceOf(Error)
    })
  })

  describe('Message Display', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()
    })

    it('应该在有错误时显示警告消息', async () => {
      const { message } = await import('ant-design-vue')
      const errors = [createValidationError(), createValidationError()]
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult(false, errors)
      })

      await wrapper.vm.performValidation()

      expect(message.warning).toHaveBeenCalledWith('发现 2 个校验错误，请查看详情并修正')
    })

    it('应该在只有警告时显示信息消息', async () => {
      const { message } = await import('ant-design-vue')
      const warnings = [createValidationWarning()]
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult(true, [], warnings)
      })

      await wrapper.vm.performValidation()

      expect(message.info).toHaveBeenCalledWith('发现 1 个校验警告')
    })

    it('应该在校验通过时显示成功消息', async () => {
      const { message } = await import('ant-design-vue')
      mockValidationService.validateProductDocuments.mockResolvedValue({
        success: true,
        data: createValidationResult(true)
      })

      await wrapper.vm.performValidation()

      expect(message.success).toHaveBeenCalledWith('文档校验通过')
    })
  })

  describe('Component Exposure', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ autoValidate: false })
      await flushPromises()
    })

    it('应该暴露validateNow方法', () => {
      expect(typeof wrapper.vm.validateNow).toBe('function')
    })

    it('应该暴露validationResult数据', () => {
      expect(wrapper.vm.validationResult).toBeDefined()
    })

    it('应该暴露validating状态', () => {
      expect(typeof wrapper.vm.validating).toBe('boolean')
    })
  })

  describe('Props Behavior', () => {
    it('应该使用documentTypeFilter过滤结果', async () => {
      // 注意：当前组件实现中没有使用documentTypeFilter，
      // 这个测试是为了确保props被正确接收
      wrapper = createWrapper({
        documentTypeFilter: ['TERMS', 'RATE_TABLE']
      })

      expect(wrapper.props('documentTypeFilter')).toEqual(['TERMS', 'RATE_TABLE'])
    })

    it('应该使用默认的documentTypeFilter', async () => {
      wrapper = createWrapper()

      expect(wrapper.props('documentTypeFilter')).toEqual([])
    })
  })
})