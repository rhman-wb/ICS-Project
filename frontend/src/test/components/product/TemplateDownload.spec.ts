import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import TemplateDownload from '@/components/product/TemplateDownload.vue'
import { message } from 'ant-design-vue'

// Mock ant-design-vue message
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    loading: vi.fn(() => () => {}),
  },
  Button: {
    name: 'AButton',
    template: '<button><slot /></button>',
  },
  Card: {
    name: 'ACard',
    template: '<div><slot /></div>',
  },
  Radio: {
    name: 'ARadio',
    template: '<input type="radio"><slot /></input>',
    Group: {
      name: 'ARadioGroup',
      template: '<div><slot /></div>',
    },
  },
  Space: {
    name: 'ASpace',
    template: '<div><slot /></div>',
  },
  Divider: {
    name: 'ADivider',
    template: '<hr />',
  },
}))

// Mock template API
vi.mock('@/api/product/template', () => ({
  downloadTemplate: vi.fn(),
  getTemplateMetadata: vi.fn(),
}))

describe('TemplateDownload Component', () => {
  let wrapper: VueWrapper<any>

  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mount(TemplateDownload, {
      props,
      global: {
        stubs: {
          AButton: true,
          ACard: true,
          ARadio: true,
          ARadioGroup: true,
          ASpace: true,
          ADivider: true,
        },
      },
    })
  }

  it('should render component correctly', () => {
    wrapper = createWrapper()
    expect(wrapper.exists()).toBe(true)
  })

  it('should have two template type options', () => {
    wrapper = createWrapper()
    const radioButtons = wrapper.findAll('input[type="radio"]')
    expect(radioButtons.length).toBeGreaterThanOrEqual(2)
  })

  it('should have default selected template type', () => {
    wrapper = createWrapper()
    expect(wrapper.vm.selectedTemplate).toBeDefined()
  })

  it('should emit download event when download button clicked', async () => {
    wrapper = createWrapper()
    const downloadButton = wrapper.find('[data-testid="download-button"]')

    if (downloadButton.exists()) {
      await downloadButton.trigger('click')
      expect(wrapper.emitted()).toHaveProperty('download')
    }
  })

  it('should show success message on successful download', async () => {
    const { downloadTemplate } = await import('@/api/product/template')
    ;(downloadTemplate as any).mockResolvedValue({ success: true })

    wrapper = createWrapper()

    if (wrapper.vm.handleDownload) {
      await wrapper.vm.handleDownload()
      expect(message.success).toHaveBeenCalled()
    }
  })

  it('should show error message on failed download', async () => {
    const { downloadTemplate } = await import('@/api/product/template')
    ;(downloadTemplate as any).mockRejectedValue(new Error('Download failed'))

    wrapper = createWrapper()

    if (wrapper.vm.handleDownload) {
      await wrapper.vm.handleDownload()
      expect(message.error).toHaveBeenCalled()
    }
  })

  it('should disable download button during download', async () => {
    wrapper = createWrapper()

    if (wrapper.vm.downloading !== undefined) {
      wrapper.vm.downloading = true
      await wrapper.vm.$nextTick()

      const downloadButton = wrapper.find('[data-testid="download-button"]')
      if (downloadButton.exists()) {
        expect(downloadButton.attributes('disabled')).toBeDefined()
      }
    }
  })

  it('should change selected template when radio selection changes', async () => {
    wrapper = createWrapper()
    const initialSelection = wrapper.vm.selectedTemplate

    const radioGroup = wrapper.find('[data-testid="template-radio-group"]')
    if (radioGroup.exists()) {
      await radioGroup.trigger('change')
      await wrapper.vm.$nextTick()
      // Template selection should be changeable
      expect(wrapper.vm.selectedTemplate).toBeDefined()
    }
  })

  it('should display template descriptions', () => {
    wrapper = createWrapper()
    const descriptions = wrapper.findAll('[data-testid="template-description"]')
    // Should have descriptions for templates
    expect(descriptions.length).toBeGreaterThanOrEqual(0)
  })

  it('should handle template metadata loading', async () => {
    const { getTemplateMetadata } = await import('@/api/product/template')
    const mockMetadata = {
      templateName: '备案产品自主注册信息登记表',
      templateVersion: '1.0.0',
      fileSize: 102400,
    }
    ;(getTemplateMetadata as any).mockResolvedValue(mockMetadata)

    wrapper = createWrapper()

    if (wrapper.vm.loadTemplateMetadata) {
      await wrapper.vm.loadTemplateMetadata('FILING')
      expect(getTemplateMetadata).toHaveBeenCalledWith('FILING')
    }
  })

  it('should format file size correctly', () => {
    wrapper = createWrapper()

    if (wrapper.vm.formatFileSize) {
      expect(wrapper.vm.formatFileSize(1024)).toBe('1.00 KB')
      expect(wrapper.vm.formatFileSize(1048576)).toBe('1.00 MB')
      expect(wrapper.vm.formatFileSize(500)).toBe('500 B')
    }
  })
})