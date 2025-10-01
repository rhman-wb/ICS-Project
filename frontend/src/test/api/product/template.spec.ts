import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import request from '@/api'
import { templateApi, templateUtils } from '@/api/product/template'
import type { TemplateType } from '@/types/product/template'

describe('Template API', () => {
  let mock: MockAdapter

  beforeEach(() => {
    mock = new MockAdapter(request)
  })

  afterEach(() => {
    mock.reset()
  })

  describe('templateApi.downloadTemplate', () => {
    it('应该成功下载备案产品模板', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.ms-excel' })
      mock.onGet('/v1/products/templates/download').reply(200, mockBlob, {
        'content-type': 'application/vnd.ms-excel'
      })

      const result = await templateApi.downloadTemplate('backup')
      expect(result).toBeInstanceOf(Blob)
    })

    it('应该成功下载农险产品模板', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/vnd.ms-excel' })
      mock.onGet('/v1/products/templates/download').reply(200, mockBlob)

      const result = await templateApi.downloadTemplate('agricultural')
      expect(result).toBeInstanceOf(Blob)
    })

    it('下载失败时应该抛出错误', async () => {
      mock.onGet('/v1/products/templates/download').networkError()

      await expect(templateApi.downloadTemplate('backup')).rejects.toThrow()
    })

    it('下载超时时应该抛出错误', async () => {
      mock.onGet('/v1/products/templates/download').timeout()

      await expect(templateApi.downloadTemplate('backup')).rejects.toThrow()
    })
  })

  describe('templateApi.getTemplateConfig', () => {
    it('应该成功获取模板配置', async () => {
      const mockConfig = {
        templateInfo: {
          id: '1',
          type: 'backup',
          name: '备案产品模板',
          description: '测试描述',
          fileFormat: 'xlsx',
          fieldCount: 30,
          version: '1.0.0',
          enabled: true
        },
        fields: [],
        validationRules: {}
      }

      mock.onGet('/v1/products/templates/config').reply(200, {
        code: 200,
        data: mockConfig
      })

      const result = await templateApi.getTemplateConfig('backup')
      expect(result.data).toEqual(mockConfig)
    })

    it('获取配置失败时应该抛出错误', async () => {
      mock.onGet('/v1/products/templates/config').reply(500, {
        code: 500,
        message: '服务器错误'
      })

      await expect(templateApi.getTemplateConfig('backup')).rejects.toThrow()
    })
  })

  describe('templateApi.validateField', () => {
    it('应该成功验证字段', async () => {
      const mockResponse = {
        result: {
          valid: true,
          errors: []
        }
      }

      mock.onPost('/v1/products/templates/validate/field').reply(200, {
        code: 200,
        data: mockResponse
      })

      const result = await templateApi.validateField({
        templateType: 'backup',
        fieldName: 'productName',
        fieldValue: '测试产品'
      })

      expect(result.data.result.valid).toBe(true)
    })

    it('字段验证失败时应该返回错误', async () => {
      const mockResponse = {
        result: {
          valid: false,
          errors: ['产品名称不能为空']
        }
      }

      mock.onPost('/v1/products/templates/validate/field').reply(200, {
        code: 200,
        data: mockResponse
      })

      const result = await templateApi.validateField({
        templateType: 'backup',
        fieldName: 'productName',
        fieldValue: ''
      })

      expect(result.data.result.valid).toBe(false)
      expect(result.data.result.errors).toHaveLength(1)
    })
  })

  describe('templateApi.validateForm', () => {
    it('应该成功验证整个表单', async () => {
      const mockResponse = {
        result: {
          valid: true,
          fieldResults: {}
        }
      }

      mock.onPost('/v1/products/templates/validate/form').reply(200, {
        code: 200,
        data: mockResponse
      })

      const result = await templateApi.validateForm({
        templateType: 'backup',
        formData: {
          templateType: 'backup',
          productName: '测试产品',
          reportType: '年度报告',
          year: 2024,
          developmentType: '自主开发',
          productCategory: '财产险',
          primaryAdditional: '主险'
        }
      })

      expect(result.data.result.valid).toBe(true)
    })
  })

  describe('templateApi.parseTemplateFile', () => {
    it('应该成功解析模板文件', async () => {
      const mockResponse = {
        success: true,
        data: {
          productName: '测试产品',
          productCategory: '财产险'
        },
        errors: [],
        warnings: []
      }

      mock.onPost('/v1/products/templates/parse').reply(200, {
        code: 200,
        data: mockResponse
      })

      const file = new File(['test'], 'template.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const result = await templateApi.parseTemplateFile(file, 'backup')
      expect(result.data.success).toBe(true)
      expect(result.data.data?.productName).toBe('测试产品')
    })

    it('解析文件超时应该抛出错误', async () => {
      mock.onPost('/v1/products/templates/parse').timeout()

      const file = new File(['test'], 'template.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      await expect(templateApi.parseTemplateFile(file, 'backup')).rejects.toThrow()
    })
  })

  describe('templateApi.submitProductForm', () => {
    it('应该成功提交产品表单', async () => {
      const mockResponse = {
        id: '123',
        productName: '测试产品'
      }

      mock.onPost('/v1/products').reply(200, {
        code: 200,
        data: mockResponse
      })

      const formData = {
        templateType: 'backup' as TemplateType,
        productName: '测试产品',
        reportType: '年度报告',
        year: 2024,
        developmentType: '自主开发',
        productCategory: '财产险',
        primaryAdditional: '主险'
      }

      const result = await templateApi.submitProductForm(formData)
      expect(result.data.id).toBe('123')
    })
  })

  describe('templateApi.updateProductForm', () => {
    it('应该成功更新产品表单', async () => {
      const mockResponse = {
        id: '123',
        productName: '更新的产品'
      }

      mock.onPut('/v1/products/123').reply(200, {
        code: 200,
        data: mockResponse
      })

      const result = await templateApi.updateProductForm('123', {
        productName: '更新的产品'
      })

      expect(result.data.productName).toBe('更新的产品')
    })
  })
})

describe('Template Utils', () => {
  describe('getTemplateDisplayName', () => {
    it('应该返回备案产品的中文名称', () => {
      const name = templateUtils.getTemplateDisplayName('backup')
      expect(name).toBe('备案产品自主注册信息登记表')
    })

    it('应该返回农险产品的中文名称', () => {
      const name = templateUtils.getTemplateDisplayName('agricultural')
      expect(name).toBe('农险产品信息登记表')
    })
  })

  describe('getTemplateFileExtension', () => {
    it('备案产品应该返回 xlsx 扩展名', () => {
      const ext = templateUtils.getTemplateFileExtension('backup')
      expect(ext).toBe('xlsx')
    })

    it('农险产品应该返回 xls 扩展名', () => {
      const ext = templateUtils.getTemplateFileExtension('agricultural')
      expect(ext).toBe('xls')
    })
  })

  describe('getTemplateFileName', () => {
    it('应该返回备案产品的完整文件名', () => {
      const filename = templateUtils.getTemplateFileName('backup')
      expect(filename).toBe('附件3_备案产品自主注册信息登记表.xlsx')
    })

    it('应该返回农险产品的完整文件名', () => {
      const filename = templateUtils.getTemplateFileName('agricultural')
      expect(filename).toBe('附件5_农险产品信息登记表.xls')
    })
  })

  describe('validateFileType', () => {
    it('备案产品模板应该接受 xlsx 文件', () => {
      const file = new File(['test'], 'template.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const isValid = templateUtils.validateFileType(file, 'backup')
      expect(isValid).toBe(true)
    })

    it('备案产品模板应该拒绝 xls 文件', () => {
      const file = new File(['test'], 'template.xls', {
        type: 'application/vnd.ms-excel'
      })

      const isValid = templateUtils.validateFileType(file, 'backup')
      expect(isValid).toBe(false)
    })

    it('农险产品模板应该接受 xls 文件', () => {
      const file = new File(['test'], 'template.xls', {
        type: 'application/vnd.ms-excel'
      })

      const isValid = templateUtils.validateFileType(file, 'agricultural')
      expect(isValid).toBe(true)
    })

    it('农险产品模板应该拒绝 xlsx 文件', () => {
      const file = new File(['test'], 'template.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const isValid = templateUtils.validateFileType(file, 'agricultural')
      expect(isValid).toBe(false)
    })
  })

  describe('getTemplateFieldCount', () => {
    it('备案产品应该返回 30 个字段', () => {
      const count = templateUtils.getTemplateFieldCount('backup')
      expect(count).toBe(30)
    })

    it('农险产品应该返回 25 个字段', () => {
      const count = templateUtils.getTemplateFieldCount('agricultural')
      expect(count).toBe(25)
    })
  })

  describe('downloadBlob', () => {
    it('应该创建下载链接并触发下载', () => {
      // Mock DOM methods
      const createObjectURLMock = vi.fn(() => 'blob:mock-url')
      const revokeObjectURLMock = vi.fn()
      window.URL.createObjectURL = createObjectURLMock
      window.URL.revokeObjectURL = revokeObjectURLMock

      const appendChildMock = vi.spyOn(document.body, 'appendChild')
      const removeChildMock = vi.spyOn(document.body, 'removeChild')

      // Create a spy for the click method
      const clickSpy = vi.fn()
      HTMLAnchorElement.prototype.click = clickSpy

      const blob = new Blob(['test'])
      const filename = 'test.xlsx'

      templateUtils.downloadBlob(blob, filename)

      expect(createObjectURLMock).toHaveBeenCalledWith(blob)
      expect(appendChildMock).toHaveBeenCalled()
      expect(clickSpy).toHaveBeenCalled()
      expect(removeChildMock).toHaveBeenCalled()
      expect(revokeObjectURLMock).toHaveBeenCalledWith('blob:mock-url')

      // Restore mocks
      appendChildMock.mockRestore()
      removeChildMock.mockRestore()
    })
  })
})
