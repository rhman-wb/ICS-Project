import { describe, it, expect } from 'vitest'
import {
  createTestProduct,
  createTestProductList,
  createAgriculturalFormData,
  createFilingFormData,
  createMockFile,
  mockProductApi,
  mockDocumentApi,
  mockAuditApi
} from '@/test/utils/product-test-utils'

describe('Product Test Utils', () => {
  describe('Test Data Creation', () => {
    it('应该创建有效的测试产品数据', () => {
      const product = createTestProduct()

      expect(product).toHaveProperty('id')
      expect(product).toHaveProperty('fileName')
      expect(product).toHaveProperty('productName')
      expect(product).toHaveProperty('status')
      expect(product.id).toBe('product-test-001')
    })

    it('应该创建产品列表数据', () => {
      const productList = createTestProductList(3)

      expect(productList).toHaveLength(3)
      expect(productList[0]).toHaveProperty('id', 'product-test-000')
      expect(productList[1]).toHaveProperty('id', 'product-test-001')
      expect(productList[2]).toHaveProperty('id', 'product-test-002')
    })

    it('应该创建农险表单数据', () => {
      const formData = createAgriculturalFormData()

      expect(formData).toHaveProperty('reportingType')
      expect(formData).toHaveProperty('productName')
      expect(formData).toHaveProperty('year')
      expect(formData.productName).toBe('测试农险产品')
    })

    it('应该创建备案表单数据', () => {
      const formData = createFilingFormData()

      expect(formData).toHaveProperty('developmentType')
      expect(formData).toHaveProperty('templateName')
      expect(formData).toHaveProperty('operatingRegion')
      expect(formData.templateName).toBe('测试示范条款')
    })

    it('应该创建Mock文件', () => {
      const file = createMockFile('test.pdf', 1024, 'application/pdf')

      expect(file.name).toBe('test.pdf')
      expect(file.size).toBe(1024)
      expect(file.type).toBe('application/pdf')
    })
  })

  describe('API Mocks', () => {
    it('应该创建产品API mock', () => {
      const api = mockProductApi()

      expect(api).toHaveProperty('getProductList')
      expect(api).toHaveProperty('getProductDetail')
      expect(api).toHaveProperty('createProduct')
      expect(api).toHaveProperty('updateProduct')
      expect(api).toHaveProperty('deleteProduct')
      expect(api).toHaveProperty('auditProduct')

      expect(typeof api.getProductList).toBe('function')
      expect(typeof api.createProduct).toBe('function')
    })

    it('应该创建文档API mock', () => {
      const api = mockDocumentApi()

      expect(api).toHaveProperty('uploadDocument')
      expect(api).toHaveProperty('getDocumentList')
      expect(api).toHaveProperty('validateDocument')
      expect(api).toHaveProperty('deleteDocument')
      expect(api).toHaveProperty('downloadDocument')

      expect(typeof api.uploadDocument).toBe('function')
      expect(typeof api.validateDocument).toBe('function')
    })

    it('应该创建审核API mock', () => {
      const api = mockAuditApi()

      expect(api).toHaveProperty('getAuditResults')
      expect(api).toHaveProperty('startAudit')
      expect(api).toHaveProperty('getAuditStatus')

      expect(typeof api.getAuditResults).toBe('function')
      expect(typeof api.startAudit).toBe('function')
    })
  })

  describe('Mock API Behavior', () => {
    it('应该返回Promise的产品列表', async () => {
      const api = mockProductApi()
      const result = await api.getProductList()

      expect(result).toHaveProperty('success', true)
      expect(result).toHaveProperty('data')
      expect(result.data).toHaveProperty('records')
      expect(Array.isArray(result.data.records)).toBe(true)
    })

    it('应该处理产品创建', async () => {
      const api = mockProductApi()
      const productData = createAgriculturalFormData()
      const result = await api.createProduct(productData)

      expect(result).toHaveProperty('success', true)
      expect(result).toHaveProperty('data')
      expect(result.data).toHaveProperty('id', 'new-product-001')
    })

    it('应该处理文档上传', async () => {
      const api = mockDocumentApi()
      const file = createMockFile('test.pdf')
      const result = await api.uploadDocument(file, 'product-001')

      expect(result).toHaveProperty('success', true)
      expect(result).toHaveProperty('data')
      expect(result.data).toHaveProperty('fileName', 'test.pdf')
    })
  })
})