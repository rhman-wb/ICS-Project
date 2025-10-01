import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import TemplateDownload from '@/components/product/TemplateDownload.vue'
import { message } from 'ant-design-vue'

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
      loading: vi.fn(() => () => {})
    }
  }
})

// Mock template API with object export
vi.mock('@/api/product/template', () => ({
  templateApi: {
    downloadTemplate: vi.fn()
  },
  templateUtils: {
    getTemplateFileName: vi.fn((type) =>
      type === 'backup'
        ? '附件3_备案产品自主注册信息登记表.xlsx'
        : '附件5_农险产品信息登记表.xls'
    ),
    downloadBlob: vi.fn()
  }
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
          FileExcelOutlined: true,
          DownloadOutlined: true,
          InfoCircleOutlined: true,
          ACard: {
            template: '<div class="a-card"><slot name="default" /></div>'
          },
          AAlert: true,
          ARow: {
            template: '<div class="a-row"><slot /></div>'
          },
          ACol: {
            template: '<div class="a-col"><slot /></div>'
          },
          AButton: {
            template: '<button :disabled="disabled"><slot /></button>',
            props: ['disabled', 'loading']
          },
          AModal: {
            template:
              '<div v-if="open" class="a-modal"><slot /><slot name="footer" /></div>',
            props: ['open', 'footer']
          },
          AProgress: true,
          ASpace: {
            template: '<div class="a-space"><slot /></div>'
          },
          ABadge: true,
          ADivider: true,
          ATypographyTitle: true,
          ATypographyParagraph: true
        }
      }
    })
  }

  it('应该正确渲染组件', () => {
    wrapper = createWrapper()
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.template-download').exists()).toBe(true)
  })

  it('应该显示两个模板卡片', () => {
    wrapper = createWrapper()
    const cards = wrapper.findAll('.template-card')
    expect(cards.length).toBe(2)
  })

  it('应该支持 v-model:template-type 双向绑定', async () => {
    wrapper = createWrapper({ templateType: 'backup' })
    expect(wrapper.vm.activeTemplate).toBe('backup')
  })

  it('点击卡片时应该选择模板并触发 update:template-type 事件', async () => {
    wrapper = createWrapper()
    const backupCard = wrapper.findAll('.template-card')[0]
    await backupCard.trigger('click')

    expect(wrapper.vm.activeTemplate).toBe('backup')
    expect(wrapper.emitted('update:template-type')).toBeTruthy()
    expect(wrapper.emitted('update:template-type')![0]).toEqual(['backup'])
  })

  it('选中的卡片应该有 active 样式类', async () => {
    wrapper = createWrapper()
    const backupCard = wrapper.findAll('.template-card')[0]
    await backupCard.trigger('click')
    await wrapper.vm.$nextTick()

    expect(backupCard.classes()).toContain('template-card-active')
  })

  it('点击下载按钮时应该调用下载逻辑', async () => {
    const { templateApi, templateUtils } = await import('@/api/product/template')
    ;(templateApi.downloadTemplate as any).mockResolvedValue(new Blob(['test']))

    wrapper = createWrapper()
    const downloadButton = wrapper.findAll('.template-card button')[0]
    await downloadButton.trigger('click')

    // Wait for download to complete
    await new Promise((resolve) => setTimeout(resolve, 100))

    expect(templateApi.downloadTemplate).toHaveBeenCalledWith('backup')
  })

  it('下载成功时应该使用工具函数下载文件', async () => {
    const { templateApi, templateUtils } = await import('@/api/product/template')
    const mockBlob = new Blob(['test'])
    ;(templateApi.downloadTemplate as any).mockResolvedValue(mockBlob)

    wrapper = createWrapper()
    await wrapper.vm.handleDownload('backup')

    // Wait for success timeout
    await new Promise((resolve) => setTimeout(resolve, 1100))

    expect(templateUtils.getTemplateFileName).toHaveBeenCalledWith('backup')
    expect(templateUtils.downloadBlob).toHaveBeenCalledWith(
      mockBlob,
      '附件3_备案产品自主注册信息登记表.xlsx'
    )
    expect(message.success).toHaveBeenCalled()
  })

  it('下载失败时应该显示错误信息和重试按钮', async () => {
    const { templateApi } = await import('@/api/product/template')
    ;(templateApi.downloadTemplate as any).mockRejectedValue(new Error('Network error'))

    wrapper = createWrapper()
    await wrapper.vm.handleDownload('backup')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.downloadStatus).toBe('exception')
    expect(message.error).toHaveBeenCalled()
    expect(wrapper.vm.showProgressModal).toBe(true)
  })

  it('点击重试按钮应该重新下载', async () => {
    const { templateApi } = await import('@/api/product/template')
    ;(templateApi.downloadTemplate as any).mockRejectedValue(new Error('Network error'))

    wrapper = createWrapper()
    wrapper.vm.activeTemplate = 'backup'
    await wrapper.vm.handleDownload('backup')
    await wrapper.vm.$nextTick()

    // Clear previous calls
    vi.clearAllMocks()
    ;(templateApi.downloadTemplate as any).mockResolvedValue(new Blob(['test']))

    // Trigger retry
    await wrapper.vm.handleRetry()

    expect(wrapper.vm.downloadProgress).toBe(0)
    expect(wrapper.vm.downloadStatus).toBe('normal')
    expect(templateApi.downloadTemplate).toHaveBeenCalledWith('backup')
  })

  it('下载中时应该显示进度弹窗', async () => {
    const { templateApi } = await import('@/api/product/template')
    ;(templateApi.downloadTemplate as any).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(new Blob(['test'])), 500))
    )

    wrapper = createWrapper()
    const downloadPromise = wrapper.vm.handleDownload('backup')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.showProgressModal).toBe(true)
    expect(wrapper.vm.downloading).toBe(true)

    await downloadPromise
  })

  it('下载时按钮应该显示 loading 状态', async () => {
    const { templateApi } = await import('@/api/product/template')
    ;(templateApi.downloadTemplate as any).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(new Blob(['test'])), 500))
    )

    wrapper = createWrapper()
    wrapper.vm.activeTemplate = 'backup'
    const downloadPromise = wrapper.vm.handleDownload('backup')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.downloading).toBe(true)

    await downloadPromise
  })

  it('进度文本应该根据状态正确显示', () => {
    wrapper = createWrapper()

    wrapper.vm.downloadProgress = 0
    wrapper.vm.downloadStatus = 'normal'
    expect(wrapper.vm.progressText).toBe('准备下载...')

    wrapper.vm.downloadProgress = 50
    wrapper.vm.downloadStatus = 'normal'
    expect(wrapper.vm.progressText).toBe('下载中... 50%')

    wrapper.vm.downloadProgress = 100
    wrapper.vm.downloadStatus = 'success'
    expect(wrapper.vm.progressText).toBe('下载完成!')

    wrapper.vm.downloadStatus = 'exception'
    expect(wrapper.vm.progressText).toBe('下载失败,请重试')
  })

  it('应该正确显示使用说明', () => {
    wrapper = createWrapper()
    const usageGuide = wrapper.find('.usage-guide')
    expect(usageGuide.exists()).toBe(true)
  })

  it('备案产品模板应该显示30个字段', () => {
    wrapper = createWrapper()
    const backupCard = wrapper.findAll('.template-card')[0]
    expect(backupCard.html()).toContain('30')
  })

  it('农险产品模板应该显示25个字段', () => {
    wrapper = createWrapper()
    const agriculturalCard = wrapper.findAll('.template-card')[1]
    expect(agriculturalCard.html()).toContain('25')
  })
})
