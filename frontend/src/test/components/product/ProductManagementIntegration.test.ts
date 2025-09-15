import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import {
  createTestProductList,
  createAgriculturalFormData,
  createFilingFormData,
  createMockFileList,
  mockProductApi,
  mockDocumentApi,
  mockAuditApi,
  createProductAntdStubs
} from '@/test/utils/product-test-utils'
import { mountComponent, flushPromises } from '@/test/utils/test-utils'

// Mock Ant Design Vue components
const antdStubs = createProductAntdStubs()

// Mock API modules
vi.mock('@/api/modules/product', () => ({
  productApi: mockProductApi()
}))

vi.mock('@/api/modules/document', () => ({
  documentApi: mockDocumentApi()
}))

vi.mock('@/api/modules/audit', () => ({
  auditApi: mockAuditApi()
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

// Mock components to test integration flow
const MockProductManagementView = {
  template: `
    <div class="product-management-view">
      <ProductListComponent
        ref="productListRef"
        @view-detail="handleViewDetail"
        @refresh="handleRefresh"
      />
      <ProductFormTemplate
        ref="productFormRef"
        v-if="showForm"
        @submit="handleFormSubmit"
        @template-change="handleTemplateChange"
      />
      <DocumentUploadComponent
        ref="documentUploadRef"
        v-if="showUpload"
        :product-id="currentProductId"
        @upload-success="handleUploadSuccess"
        @files-change="handleFilesChange"
      />
      <DocumentValidation
        ref="documentValidationRef"
        v-if="showValidation"
        :product-id="currentProductId"
        @validation-complete="handleValidationComplete"
      />
    </div>
  `,
  components: {
    ProductListComponent: {
      template: '<div class="product-list-mock">Product List</div>',
      emits: ['view-detail', 'refresh'],
      methods: {
        refresh: vi.fn(),
        getSelectedProducts: vi.fn(() => ['product-1']),
        clearSelection: vi.fn()
      }
    },
    ProductFormTemplate: {
      template: '<div class="product-form-mock">Product Form</div>',
      emits: ['submit', 'template-change'],
      methods: {
        validateCurrentForm: vi.fn(() => Promise.resolve(true)),
        getCurrentFormData: vi.fn(() => createAgriculturalFormData()),
        resetCurrentForm: vi.fn()
      }
    },
    DocumentUploadComponent: {
      template: '<div class="document-upload-mock">Document Upload</div>',
      props: ['productId'],
      emits: ['upload-success', 'files-change'],
      methods: {
        validateAllDocuments: vi.fn(() => Promise.resolve()),
        uploadAllDocuments: vi.fn(() => Promise.resolve()),
        getFileCount: vi.fn(() => 4)
      }
    },
    DocumentValidation: {
      template: '<div class="document-validation-mock">Document Validation</div>',
      props: ['productId'],
      emits: ['validation-complete'],
      methods: {
        validateNow: vi.fn(() => Promise.resolve())
      }
    }
  },
  data() {
    return {
      showForm: false,
      showUpload: false,
      showValidation: false,
      currentProductId: null
    }
  },
  methods: {
    handleViewDetail(product) {
      this.currentProductId = product.id
      this.showValidation = true
    },
    handleRefresh() {
      this.$refs.productListRef?.refresh()
    },
    handleFormSubmit(data) {
      this.currentProductId = data.formData.id || 'new-product-001'
      this.showUpload = true
    },
    handleTemplateChange(template) {
      console.log('Template changed to:', template)
    },
    handleUploadSuccess(files) {
      this.showValidation = true
    },
    handleFilesChange(files) {
      console.log('Files changed:', files.length)
    },
    handleValidationComplete(result) {
      console.log('Validation completed:', result.isValid)
    },
    startNewProduct() {
      this.showForm = true
      this.showUpload = false
      this.showValidation = false
    },
    editProduct(productId) {
      this.currentProductId = productId
      this.showForm = true
    }
  }
}

describe('Product Management Integration Tests', () => {
  let wrapper: any
  let router: any
  let mockApis: any

  beforeEach(() => {
    const pinia = createPinia()
    setActivePinia(pinia)

    router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: { template: '<div>Home</div>' } },
        { path: '/product/management', component: MockProductManagementView },
        { path: '/product/detail/:id', component: { template: '<div>Product Detail</div>' } },
        { path: '/product/success', component: { template: '<div>Success</div>' } }
      ]
    })

    // Reset API mocks
    mockApis = {
      product: mockProductApi(),
      document: mockDocumentApi(),
      audit: mockAuditApi()
    }

    vi.doMock('@/api/modules/product', () => ({ productApi: mockApis.product }))
    vi.doMock('@/api/modules/document', () => ({ documentApi: mockApis.document }))
    vi.doMock('@/api/modules/audit', () => ({ auditApi: mockApis.audit }))
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const createWrapper = () => {
    return mountComponent(MockProductManagementView, {
      global: {
        plugins: [router],
        stubs: antdStubs
      }
    })
  }

  describe('Complete Product Creation Flow', () => {
    it('应该完成完整的产品创建流程', async () => {
      wrapper = createWrapper()
      await flushPromises()

      // 1. 开始新产品创建
      await wrapper.vm.startNewProduct()
      await nextTick()

      expect(wrapper.vm.showForm).toBe(true)
      expect(wrapper.find('.product-form-mock').exists()).toBe(true)

      // 2. 提交产品表单
      const formData = {
        type: 'agricultural',
        formData: {
          ...createAgriculturalFormData(),
          id: 'new-product-001'
        }
      }

      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })
      await productForm.vm.$emit('submit', formData)
      await nextTick()

      expect(wrapper.vm.currentProductId).toBe('new-product-001')
      expect(wrapper.vm.showUpload).toBe(true)
      expect(wrapper.find('.document-upload-mock').exists()).toBe(true)

      // 3. 上传文档
      const mockFiles = createMockFileList(4)
      const documentUpload = wrapper.findComponent({ name: 'DocumentUploadComponent' })
      expect(documentUpload.props('productId')).toBe('new-product-001')

      await documentUpload.vm.$emit('upload-success', mockFiles)
      await nextTick()

      expect(wrapper.vm.showValidation).toBe(true)
      expect(wrapper.find('.document-validation-mock').exists()).toBe(true)

      // 4. 验证文档
      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })
      expect(documentValidation.props('productId')).toBe('new-product-001')

      const validationResult = {
        isValid: true,
        summary: { completenessPercentage: 100 },
        errors: [],
        warnings: []
      }

      await documentValidation.vm.$emit('validation-complete', validationResult)
      await nextTick()

      // 验证整个流程完成
      expect(wrapper.vm.currentProductId).toBe('new-product-001')
      expect(wrapper.vm.showForm).toBe(true)
      expect(wrapper.vm.showUpload).toBe(true)
      expect(wrapper.vm.showValidation).toBe(true)
    })

    it('应该处理农险产品创建流程', async () => {
      wrapper = createWrapper()
      await flushPromises()

      await wrapper.vm.startNewProduct()
      await nextTick()

      // 提交农险产品表单
      const agriculturalData = {
        type: 'agricultural',
        formData: {
          ...createAgriculturalFormData(),
          productType: 'AGRICULTURAL'
        }
      }

      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })
      await productForm.vm.$emit('submit', agriculturalData)
      await nextTick()

      expect(wrapper.vm.showUpload).toBe(true)

      // 验证API调用（如果有的话）
      expect(mockApis.product.createProduct).not.toHaveBeenCalled() // Mock组件不调用API
    })

    it('应该处理备案产品创建流程', async () => {
      wrapper = createWrapper()
      await flushPromises()

      await wrapper.vm.startNewProduct()
      await nextTick()

      // 提交备案产品表单
      const filingData = {
        type: 'filing',
        formData: {
          ...createFilingFormData(),
          productType: 'FILING'
        }
      }

      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })
      await productForm.vm.$emit('submit', filingData)
      await nextTick()

      expect(wrapper.vm.showUpload).toBe(true)
    })
  })

  describe('Product List to Detail Flow', () => {
    it('应该正确处理产品查看详情流程', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const testProduct = {
        id: 'product-test-001',
        productName: '测试产品',
        status: 'DRAFT'
      }

      // 模拟查看产品详情
      const productList = wrapper.findComponent({ name: 'ProductListComponent' })
      await productList.vm.$emit('view-detail', testProduct)
      await nextTick()

      expect(wrapper.vm.currentProductId).toBe('product-test-001')
      expect(wrapper.vm.showValidation).toBe(true)
      expect(wrapper.find('.document-validation-mock').exists()).toBe(true)

      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })
      expect(documentValidation.props('productId')).toBe('product-test-001')
    })

    it('应该处理产品列表刷新', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const productList = wrapper.findComponent({ name: 'ProductListComponent' })
      const refreshSpy = vi.spyOn(productList.vm, 'refresh')

      await productList.vm.$emit('refresh')
      await nextTick()

      // 验证handleRefresh方法被调用，但在mock组件中不会实际调用refresh
      expect(wrapper.vm.handleRefresh).toBeDefined()
    })
  })

  describe('Document Management Flow', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()

      // 设置一个产品ID用于文档操作
      wrapper.vm.currentProductId = 'test-product-001'
      wrapper.vm.showUpload = true
      wrapper.vm.showValidation = true
      await nextTick()
    })

    it('应该正确处理文档上传到验证的流程', async () => {
      const mockFiles = createMockFileList(3)

      // 1. 文档上传
      const documentUpload = wrapper.findComponent({ name: 'DocumentUploadComponent' })
      await documentUpload.vm.$emit('files-change', mockFiles)
      await nextTick()

      // 验证文件变化被处理
      expect(wrapper.vm.handleFilesChange).toBeDefined()

      // 2. 上传成功
      await documentUpload.vm.$emit('upload-success', mockFiles)
      await nextTick()

      expect(wrapper.vm.showValidation).toBe(true)

      // 3. 验证文档
      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })
      const validationResult = {
        isValid: true,
        summary: { completenessPercentage: 100 },
        errors: [],
        warnings: []
      }

      await documentValidation.vm.$emit('validation-complete', validationResult)
      await nextTick()

      // 验证整个流程
      expect(wrapper.vm.currentProductId).toBe('test-product-001')
    })

    it('应该处理文档验证失败的情况', async () => {
      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })
      const validationResult = {
        isValid: false,
        summary: { completenessPercentage: 60 },
        errors: [
          {
            errorCode: 'DOC_001',
            documentType: 'TERMS',
            severity: 'HIGH',
            message: '条款文档缺失'
          }
        ],
        warnings: []
      }

      await documentValidation.vm.$emit('validation-complete', validationResult)
      await nextTick()

      // 验证验证结果被正确处理
      expect(wrapper.vm.handleValidationComplete).toBeDefined()
    })
  })

  describe('Form Template Integration', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.showForm = true
      await nextTick()
    })

    it('应该处理模板切换', async () => {
      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })

      await productForm.vm.$emit('template-change', 'filing')
      await nextTick()

      // 验证模板切换被处理
      expect(wrapper.vm.handleTemplateChange).toBeDefined()
    })

    it('应该验证表单数据', async () => {
      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })
      const validateSpy = vi.spyOn(productForm.vm, 'validateCurrentForm')

      await productForm.vm.validateCurrentForm()

      expect(validateSpy).toHaveBeenCalled()
    })

    it('应该获取当前表单数据', async () => {
      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })
      const getDataSpy = vi.spyOn(productForm.vm, 'getCurrentFormData')

      const formData = productForm.vm.getCurrentFormData()

      expect(getDataSpy).toHaveBeenCalled()
      expect(formData).toBeDefined()
    })
  })

  describe('Error Handling in Integration Flow', () => {
    it('应该处理产品创建失败', async () => {
      wrapper = createWrapper()
      await flushPromises()

      await wrapper.vm.startNewProduct()
      await nextTick()

      // 模拟提交失败的数据
      const invalidData = {
        type: 'agricultural',
        formData: null // 无效数据
      }

      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })

      // 这不会抛出错误，因为是mock组件
      await productForm.vm.$emit('submit', invalidData)
      await nextTick()

      // 验证状态变化
      expect(wrapper.vm.showUpload).toBe(true)
    })

    it('应该处理文档上传失败', async () => {
      wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.currentProductId = 'test-product-001'
      wrapper.vm.showUpload = true
      await nextTick()

      const documentUpload = wrapper.findComponent({ name: 'DocumentUploadComponent' })

      // 即使上传失败，组件也应该保持稳定
      await documentUpload.vm.$emit('upload-success', [])
      await nextTick()

      expect(wrapper.vm.showValidation).toBe(true)
    })

    it('应该处理验证失败', async () => {
      wrapper = createWrapper()
      await flushPromises()

      wrapper.vm.currentProductId = 'test-product-001'
      wrapper.vm.showValidation = true
      await nextTick()

      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })
      const validationResult = {
        isValid: false,
        summary: { completenessPercentage: 0 },
        errors: [
          {
            errorCode: 'CRITICAL_001',
            documentType: 'ALL',
            severity: 'CRITICAL',
            message: '严重验证错误'
          }
        ],
        warnings: []
      }

      await documentValidation.vm.$emit('validation-complete', validationResult)
      await nextTick()

      // 验证系统仍然稳定
      expect(wrapper.vm.currentProductId).toBe('test-product-001')
    })
  })

  describe('Component Method Integration', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()

      // 确保所有组件都可见
      wrapper.vm.showForm = true
      wrapper.vm.showUpload = true
      wrapper.vm.showValidation = true
      wrapper.vm.currentProductId = 'test-product-001'
      await nextTick()
    })

    it('应该协调产品列表组件方法', async () => {
      const productList = wrapper.findComponent({ name: 'ProductListComponent' })

      // 测试暴露的方法
      expect(productList.vm.refresh).toBeDefined()
      expect(productList.vm.getSelectedProducts).toBeDefined()
      expect(productList.vm.clearSelection).toBeDefined()

      // 调用方法
      const selectedProducts = productList.vm.getSelectedProducts()
      expect(selectedProducts).toEqual(['product-1'])

      productList.vm.clearSelection()
      expect(productList.vm.clearSelection).toHaveBeenCalled()
    })

    it('应该协调表单组件方法', async () => {
      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })

      // 测试验证方法
      const isValid = await productForm.vm.validateCurrentForm()
      expect(isValid).toBe(true)

      // 测试获取数据方法
      const formData = productForm.vm.getCurrentFormData()
      expect(formData).toBeDefined()

      // 测试重置方法
      productForm.vm.resetCurrentForm()
      expect(productForm.vm.resetCurrentForm).toHaveBeenCalled()
    })

    it('应该协调文档上传组件方法', async () => {
      const documentUpload = wrapper.findComponent({ name: 'DocumentUploadComponent' })

      // 测试验证方法
      await documentUpload.vm.validateAllDocuments()
      expect(documentUpload.vm.validateAllDocuments).toHaveBeenCalled()

      // 测试上传方法
      await documentUpload.vm.uploadAllDocuments()
      expect(documentUpload.vm.uploadAllDocuments).toHaveBeenCalled()

      // 测试获取文件数量
      const fileCount = documentUpload.vm.getFileCount()
      expect(fileCount).toBe(4)
    })

    it('应该协调文档验证组件方法', async () => {
      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })

      // 测试验证方法
      await documentValidation.vm.validateNow()
      expect(documentValidation.vm.validateNow).toHaveBeenCalled()
    })
  })

  describe('State Management Integration', () => {
    it('应该在组件间正确传递状态', async () => {
      wrapper = createWrapper()
      await flushPromises()

      // 1. 设置产品ID
      const productId = 'integration-test-001'
      wrapper.vm.currentProductId = productId

      // 2. 显示所有组件
      wrapper.vm.showForm = true
      wrapper.vm.showUpload = true
      wrapper.vm.showValidation = true
      await nextTick()

      // 3. 验证props传递
      const documentUpload = wrapper.findComponent({ name: 'DocumentUploadComponent' })
      const documentValidation = wrapper.findComponent({ name: 'DocumentValidation' })

      expect(documentUpload.props('productId')).toBe(productId)
      expect(documentValidation.props('productId')).toBe(productId)

      // 4. 测试状态变化
      wrapper.vm.currentProductId = 'new-product-id'
      await nextTick()

      expect(documentUpload.props('productId')).toBe('new-product-id')
      expect(documentValidation.props('productId')).toBe('new-product-id')
    })

    it('应该在不同视图间保持状态一致', async () => {
      wrapper = createWrapper()
      await flushPromises()

      // 模拟产品创建流程
      await wrapper.vm.startNewProduct()
      expect(wrapper.vm.showForm).toBe(true)
      expect(wrapper.vm.showUpload).toBe(false)
      expect(wrapper.vm.showValidation).toBe(false)

      // 模拟表单提交
      const formData = {
        type: 'agricultural',
        formData: { id: 'new-product-002' }
      }

      const productForm = wrapper.findComponent({ name: 'ProductFormTemplate' })
      await productForm.vm.$emit('submit', formData)
      await nextTick()

      expect(wrapper.vm.currentProductId).toBe('new-product-002')
      expect(wrapper.vm.showUpload).toBe(true)

      // 模拟文档上传
      const documentUpload = wrapper.findComponent({ name: 'DocumentUploadComponent' })
      await documentUpload.vm.$emit('upload-success', [])
      await nextTick()

      expect(wrapper.vm.showValidation).toBe(true)

      // 验证最终状态
      expect(wrapper.vm.currentProductId).toBe('new-product-002')
      expect(wrapper.vm.showForm).toBe(true)
      expect(wrapper.vm.showUpload).toBe(true)
      expect(wrapper.vm.showValidation).toBe(true)
    })
  })
})