import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import DocumentUploadComponent from '@/components/DocumentUploadComponent.vue'
import {
  createMockFile,
  createMockFileList,
  createProductAntdStubs,
  waitForUpload
} from '@/test/utils/product-test-utils'
import { mountComponent, flushPromises } from '@/test/utils/test-utils'

// Mock Ant Design Vue components
const antdStubs = createProductAntdStubs()

// Mock Ant Design Icons
vi.mock('@ant-design/icons-vue', () => ({
  InboxOutlined: { template: '<span class="inbox-icon" />' },
  FolderOutlined: { template: '<span class="folder-icon" />' },
  FileTextOutlined: { template: '<span class="file-text-icon" />' },
  FileExcelOutlined: { template: '<span class="file-excel-icon" />' },
  FilePdfOutlined: { template: '<span class="file-pdf-icon" />' },
  FileOutlined: { template: '<span class="file-icon" />' },
  CheckCircleOutlined: { template: '<span class="check-icon" />' },
  UploadOutlined: { template: '<span class="upload-icon" />' },
  ReloadOutlined: { template: '<span class="reload-icon" />' }
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

describe('DocumentUploadComponent', () => {
  let wrapper: any

  beforeEach(() => {
    const pinia = createPinia()
    setActivePinia(pinia)

    // Mock URL.createObjectURL
    global.URL.createObjectURL = vi.fn(() => 'mocked-url')
    global.URL.revokeObjectURL = vi.fn()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mountComponent(DocumentUploadComponent, {
      props,
      global: {
        stubs: {
          ...antdStubs,
          'a-upload-dragger': {
            template: '<div class="ant-upload-dragger"><slot /></div>',
            props: ['name', 'multiple', 'accept', 'fileList', 'beforeUpload'],
            emits: ['change', 'drop']
          }
        }
      }
    })
  }

  describe('Component Initialization', () => {
    it('应该正确渲染基本结构', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.document-upload-component').exists()).toBe(true)
      expect(wrapper.find('.upload-card').exists()).toBe(true)
      expect(wrapper.find('.upload-container').exists()).toBe(true)
      expect(wrapper.find('.global-actions').exists()).toBe(true)
    })

    it('应该显示四种文档类型', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const documentSections = wrapper.findAll('.document-type-section')
      expect(documentSections).toHaveLength(4)

      const expectedTypes = ['条款', '可行性报告', '精算报告', '费率表']
      documentSections.forEach((section, index) => {
        expect(section.text()).toContain(expectedTypes[index])
      })
    })

    it('应该正确设置必需和可选标签', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const requiredTags = wrapper.findAll('.ant-tag[color="red"]')
      const optionalTags = wrapper.findAll('.ant-tag[color="blue"]')

      expect(requiredTags).toHaveLength(3) // 条款、可行性报告、精算报告
      expect(optionalTags).toHaveLength(1) // 费率表
    })

    it('应该使用默认props值', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const vm = wrapper.vm
      expect(vm.maxFiles).toBe(10)
      expect(vm.maxFileSize).toBe(50 * 1024 * 1024)
      expect(vm.disabled).toBe(false)
    })

    it('应该使用自定义props值', async () => {
      wrapper = createWrapper({
        maxFiles: 5,
        maxFileSize: 10 * 1024 * 1024,
        disabled: true,
        productId: 'test-product-001'
      })
      await flushPromises()

      const vm = wrapper.vm
      expect(vm.maxFiles).toBe(5)
      expect(vm.maxFileSize).toBe(10 * 1024 * 1024)
      expect(vm.disabled).toBe(true)
      expect(vm.productId).toBe('test-product-001')
    })
  })

  describe('File Type Validation', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该正确识别允许的文件类型', () => {
      const allowedFiles = [
        createMockFile('test.doc'),
        createMockFile('test.docx'),
        createMockFile('test.pdf'),
        createMockFile('test.xls'),
        createMockFile('test.xlsx')
      ]

      allowedFiles.forEach(file => {
        expect(wrapper.vm.isAllowedFileType(file)).toBe(true)
      })
    })

    it('应该拒绝不支持的文件类型', () => {
      const disallowedFiles = [
        createMockFile('test.txt'),
        createMockFile('test.jpg'),
        createMockFile('test.png'),
        createMockFile('test.zip')
      ]

      disallowedFiles.forEach(file => {
        expect(wrapper.vm.isAllowedFileType(file)).toBe(false)
      })
    })

    it('应该正确识别不同文件类型', () => {
      const docFile = createMockFile('test.docx')
      const excelFile = createMockFile('test.xlsx')
      const pdfFile = createMockFile('test.pdf')

      expect(wrapper.vm.isDocumentFile(docFile)).toBe(true)
      expect(wrapper.vm.isExcelFile(excelFile)).toBe(true)
      expect(wrapper.vm.isPdfFile(pdfFile)).toBe(true)
      expect(wrapper.vm.canPreview(pdfFile)).toBe(true)
      expect(wrapper.vm.canPreview(docFile)).toBe(false)
    })
  })

  describe('File Upload Validation', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ maxFileSize: 1024 * 1024 }) // 1MB限制
      await flushPromises()
    })

    it('应该在文件过大时拒绝上传', async () => {
      const { message } = await import('ant-design-vue')
      const largeFile = createMockFile('large.pdf', 2 * 1024 * 1024) // 2MB
      const termsType = wrapper.vm.documentTypes[0]

      const result = wrapper.vm.beforeUpload(largeFile, termsType)

      expect(result).toBe(false)
      expect(message.error).toHaveBeenCalledWith(
        expect.stringContaining('大小超过 1MB 限制')
      )
    })

    it('应该在文件类型不支持时拒绝上传', async () => {
      const { message } = await import('ant-design-vue')
      const invalidFile = createMockFile('test.txt', 1024, 'text/plain')
      const termsType = wrapper.vm.documentTypes[0]

      const result = wrapper.vm.beforeUpload(invalidFile, termsType)

      expect(result).toBe(false)
      expect(message.error).toHaveBeenCalledWith(
        expect.stringContaining('不支持的文件类型')
      )
    })

    it('应该在单文件类型超出限制时拒绝上传', async () => {
      const { message } = await import('ant-design-vue')
      const file = createMockFile('test.pdf', 1024)
      const termsType = wrapper.vm.documentTypes[0] // 不允许多文件

      // 先添加一个文件
      termsType.fileList = [{ uid: '1', name: 'existing.pdf', size: 1024 }]

      const result = wrapper.vm.beforeUpload(file, termsType)

      expect(result).toBe(false)
      expect(message.warning).toHaveBeenCalledWith(
        expect.stringContaining('只能上传一个文件')
      )
    })

    it('应该允许多文件类型上传多个文件', async () => {
      const file = createMockFile('test.xlsx', 1024)
      const rateTableType = wrapper.vm.documentTypes[3] // 允许多文件

      // 先添加一个文件
      rateTableType.fileList = [{ uid: '1', name: 'existing.xlsx', size: 1024 }]

      const result = wrapper.vm.beforeUpload(file, rateTableType)

      expect(result).toBe(false) // 仍然返回false因为手动处理上传
      expect(vi.mocked(await import('ant-design-vue')).message.warning).not.toHaveBeenCalled()
    })
  })

  describe('File Operations', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该正确处理上传变化', async () => {
      const file = createMockFile('test.pdf')
      const termsType = wrapper.vm.documentTypes[0]
      const info = {
        file: { ...file, status: 'uploading' },
        fileList: [{ ...file, status: 'uploading' }]
      }

      await wrapper.vm.handleUploadChange(info, termsType)

      expect(termsType.fileList).toHaveLength(1)
      expect(wrapper.emitted('files-change')).toBeTruthy()
    })

    it('应该正确删除文件', async () => {
      const { message } = await import('ant-design-vue')
      const termsType = wrapper.vm.documentTypes[0]
      termsType.fileList = [
        { uid: '1', name: 'test1.pdf', size: 1024 },
        { uid: '2', name: 'test2.pdf', size: 2048 }
      ]

      await wrapper.vm.removeFile(termsType, 0)

      expect(termsType.fileList).toHaveLength(1)
      expect(termsType.fileList[0].name).toBe('test2.pdf')
      expect(message.success).toHaveBeenCalledWith('文件删除成功')
      expect(wrapper.emitted('files-change')).toBeTruthy()
    })

    it('应该正确清空文件', async () => {
      const { message } = await import('ant-design-vue')
      const termsType = wrapper.vm.documentTypes[0]
      termsType.fileList = [
        { uid: '1', name: 'test1.pdf', size: 1024 },
        { uid: '2', name: 'test2.pdf', size: 2048 }
      ]

      await wrapper.vm.clearFiles(termsType)

      expect(termsType.fileList).toHaveLength(0)
      expect(message.success).toHaveBeenCalledWith('条款 文件已清空')
      expect(wrapper.emitted('files-change')).toBeTruthy()
    })

    it('应该正确格式化文件大小', () => {
      expect(wrapper.vm.formatFileSize(512)).toBe('512 B')
      expect(wrapper.vm.formatFileSize(1536)).toBe('1.5 KB')
      expect(wrapper.vm.formatFileSize(2 * 1024 * 1024)).toBe('2.0 MB')
    })
  })

  describe('File Preview', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该为PDF文件打开预览', async () => {
      const pdfFile = { name: 'test.pdf', url: 'http://example.com/test.pdf' }

      await wrapper.vm.previewFile(pdfFile)

      expect(wrapper.vm.previewVisible).toBe(true)
      expect(wrapper.vm.previewFile).toEqual(pdfFile)
      expect(wrapper.vm.previewUrl).toBe(pdfFile.url)
    })

    it('应该为非PDF文件显示警告', async () => {
      const { message } = await import('ant-design-vue')
      const docFile = { name: 'test.docx', url: 'http://example.com/test.docx' }

      await wrapper.vm.previewFile(docFile)

      expect(wrapper.vm.previewVisible).toBe(false)
      expect(message.warning).toHaveBeenCalledWith('该文件类型暂不支持预览')
    })

    it('应该为没有URL的文件显示警告', async () => {
      const { message } = await import('ant-design-vue')
      const pdfFile = { name: 'test.pdf' } // 没有url

      await wrapper.vm.previewFile(pdfFile)

      expect(wrapper.vm.previewVisible).toBe(false)
      expect(message.warning).toHaveBeenCalledWith('该文件类型暂不支持预览')
    })
  })

  describe('Document Validation', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该验证必需文档', async () => {
      const { message } = await import('ant-design-vue')

      await wrapper.vm.validateAllDocuments()

      expect(wrapper.vm.validating).toBe(false)
      expect(message.error).toHaveBeenCalledWith('文档验证失败，请检查必需文档')
      expect(wrapper.emitted('validate')).toBeTruthy()

      const validationResult = wrapper.emitted('validate')[0][0]
      expect(validationResult.success).toBe(false)
      expect(validationResult.message).toContain('验证失败')
    })

    it('应该在有必需文档时验证通过', async () => {
      const { message } = await import('ant-design-vue')

      // 为所有必需文档添加文件
      wrapper.vm.documentTypes.forEach(docType => {
        if (docType.required) {
          docType.fileList = [{ uid: '1', name: `${docType.name}.pdf`, size: 1024 }]
        }
      })

      await wrapper.vm.validateAllDocuments()

      expect(message.success).toHaveBeenCalledWith('所有文档验证通过')
      expect(wrapper.emitted('validate')).toBeTruthy()

      const validationResult = wrapper.emitted('validate')[0][0]
      expect(validationResult.success).toBe(true)
    })

    it('应该设置正确的验证状态', async () => {
      // 只为条款添加文件
      wrapper.vm.documentTypes[0].fileList = [{ uid: '1', name: 'terms.pdf', size: 1024 }]

      await wrapper.vm.validateAllDocuments()

      expect(wrapper.vm.documentTypes[0].validationStatus).toBe('success')
      expect(wrapper.vm.documentTypes[1].validationStatus).toBe('error')
      expect(wrapper.vm.documentTypes[2].validationStatus).toBe('error')
    })
  })

  describe('Document Upload', () => {
    beforeEach(async () => {
      wrapper = createWrapper({ productId: 'test-product-001' })
      await flushPromises()
    })

    it('应该在没有productId时显示错误', async () => {
      const { message } = await import('ant-design-vue')
      wrapper = createWrapper() // 没有productId
      await flushPromises()

      await wrapper.vm.uploadAllDocuments()

      expect(message.error).toHaveBeenCalledWith('请先选择产品')
    })

    it('应该在没有文件时显示警告', async () => {
      const { message } = await import('ant-design-vue')

      await wrapper.vm.uploadAllDocuments()

      expect(message.warning).toHaveBeenCalledWith('没有文件需要上传')
    })

    it('应该正确上传文件', async () => {
      const { message } = await import('ant-design-vue')

      // 添加一些文件
      wrapper.vm.documentTypes[0].fileList = [
        { uid: '1', name: 'terms.pdf', size: 1024, originFileObj: createMockFile('terms.pdf') }
      ]
      wrapper.vm.documentTypes[3].fileList = [
        { uid: '2', name: 'rate.xlsx', size: 2048, originFileObj: createMockFile('rate.xlsx') }
      ]

      await wrapper.vm.uploadAllDocuments()

      expect(wrapper.vm.uploading).toBe(false)
      expect(message.success).toHaveBeenCalledWith('成功上传 2 个文件')
      expect(wrapper.emitted('upload-success')).toBeTruthy()

      const uploadedFiles = wrapper.emitted('upload-success')[0][0]
      expect(uploadedFiles).toHaveLength(2)
    })

    it('应该处理上传错误', async () => {
      const { message } = await import('ant-design-vue')

      // Mock一个会抛出错误的情况
      wrapper.vm.documentTypes[0].fileList = [
        { uid: '1', name: 'terms.pdf', size: 1024, originFileObj: null } // 没有originFileObj会导致错误
      ]

      // 模拟Promise.reject
      const originalSetTimeout = global.setTimeout
      global.setTimeout = vi.fn(() => {
        throw new Error('Upload failed')
      })

      await wrapper.vm.uploadAllDocuments()

      expect(wrapper.vm.uploading).toBe(false)
      expect(wrapper.emitted('upload-error')).toBeTruthy()

      global.setTimeout = originalSetTimeout
    })
  })

  describe('Global Operations', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该正确重置所有文档', async () => {
      const { message } = await import('ant-design-vue')

      // 添加一些文件和路径
      wrapper.vm.documentTypes.forEach(docType => {
        docType.fileList = [{ uid: '1', name: 'test.pdf', size: 1024 }]
        docType.customPath = '/custom/path'
        docType.validationMessage = 'Test message'
        docType.validationStatus = 'success'
      })

      await wrapper.vm.resetAll()

      wrapper.vm.documentTypes.forEach(docType => {
        expect(docType.fileList).toHaveLength(0)
        expect(docType.customPath).toBe('')
        expect(docType.validationMessage).toBeUndefined()
        expect(docType.validationStatus).toBeUndefined()
      })

      expect(message.info).toHaveBeenCalledWith('已重置所有文档')
      expect(wrapper.emitted('files-change')).toBeTruthy()
    })

    it('应该正确处理文件夹选择', async () => {
      const { message } = await import('ant-design-vue')
      const termsType = wrapper.vm.documentTypes[0]

      await wrapper.vm.selectFolder(termsType)

      expect(message.info).toHaveBeenCalledWith('文件夹选择功能需要在Electron环境中实现')
    })

    it('应该正确清除验证信息', () => {
      const termsType = wrapper.vm.documentTypes[0]
      termsType.validationMessage = 'Test message'
      termsType.validationStatus = 'error'

      wrapper.vm.clearValidation(termsType)

      expect(termsType.validationMessage).toBeUndefined()
      expect(termsType.validationStatus).toBeUndefined()
    })
  })

  describe('Component Exposure', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该暴露validateAllDocuments方法', () => {
      expect(typeof wrapper.vm.validateAllDocuments).toBe('function')
    })

    it('应该暴露uploadAllDocuments方法', () => {
      expect(typeof wrapper.vm.uploadAllDocuments).toBe('function')
    })

    it('应该暴露resetAll方法', () => {
      expect(typeof wrapper.vm.resetAll).toBe('function')
    })

    it('应该正确计算文件总数', () => {
      wrapper.vm.documentTypes[0].fileList = [{ uid: '1' }, { uid: '2' }]
      wrapper.vm.documentTypes[1].fileList = [{ uid: '3' }]

      expect(wrapper.vm.getFileCount()).toBe(3)
    })

    it('应该返回文档类型配置', () => {
      const documentTypes = wrapper.vm.getDocumentTypes()
      expect(documentTypes).toHaveLength(4)
      expect(documentTypes[0].key).toBe('TERMS')
    })
  })

  describe('UI Interactions', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该正确处理验证按钮点击', async () => {
      const validateButton = wrapper.find('button').filter(btn =>
        btn.text().includes('验证所有文档')
      )

      await validateButton.trigger('click')

      expect(wrapper.emitted('validate')).toBeTruthy()
    })

    it('应该正确处理上传按钮点击', async () => {
      wrapper = createWrapper({ productId: 'test-product' })
      await flushPromises()

      // 添加一个文件
      wrapper.vm.documentTypes[0].fileList = [
        { uid: '1', name: 'test.pdf', originFileObj: createMockFile('test.pdf') }
      ]

      const uploadButton = wrapper.find('button[type="primary"]')
      await uploadButton.trigger('click')

      expect(wrapper.emitted('upload-success')).toBeTruthy()
    })

    it('应该正确处理重置按钮点击', async () => {
      const resetButton = wrapper.find('button').filter(btn =>
        btn.text().includes('重置所有')
      )

      await resetButton.trigger('click')

      expect(wrapper.emitted('files-change')).toBeTruthy()
    })

    it('应该在禁用状态下禁用重置按钮', async () => {
      wrapper.vm.uploading = true
      await nextTick()

      const resetButton = wrapper.find('button').filter(btn =>
        btn.text().includes('重置所有')
      )

      expect(resetButton.attributes('disabled')).toBeDefined()
    })
  })

  describe('Event Emissions', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该在文件变化时触发files-change事件', () => {
      wrapper.vm.emitFilesChange()

      expect(wrapper.emitted('files-change')).toBeTruthy()
      expect(wrapper.emitted('files-change')[0][0]).toEqual([])
    })

    it('应该包含正确的文件信息', () => {
      wrapper.vm.documentTypes[0].fileList = [
        { uid: '1', name: 'test.pdf', size: 1024 }
      ]
      wrapper.vm.documentTypes[0].customPath = '/custom/path'

      wrapper.vm.emitFilesChange()

      const emittedFiles = wrapper.emitted('files-change')[0][0]
      expect(emittedFiles).toHaveLength(1)
      expect(emittedFiles[0]).toMatchObject({
        uid: '1',
        name: 'test.pdf',
        size: 1024,
        documentType: 'TERMS',
        customPath: '/custom/path'
      })
    })
  })

  describe('Error Handling', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理文件操作中的边界情况', () => {
      const termsType = wrapper.vm.documentTypes[0]

      // 尝试删除不存在的文件索引
      expect(() => wrapper.vm.removeFile(termsType, 999)).not.toThrow()

      // 处理空文件列表的清空操作
      expect(() => wrapper.vm.clearFiles(termsType)).not.toThrow()
    })

    it('应该处理预览文件的边界情况', async () => {
      // 尝试预览null文件
      expect(() => wrapper.vm.previewFile(null)).not.toThrow()

      // 尝试预览没有名称的文件
      expect(() => wrapper.vm.previewFile({})).not.toThrow()
    })
  })
})