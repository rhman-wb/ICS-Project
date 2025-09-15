import { vi } from 'vitest'
import { mockApiResponse, mockPageResponse } from './test-utils'

/**
 * 产品测试工具函数
 */

/**
 * 创建测试产品数据
 */
export function createTestProduct(overrides: any = {}) {
  return {
    id: 'product-test-001',
    fileName: '测试农险产品信息登记表.xlsx',
    productName: '测试农险产品',
    reportingType: 'POLICY',
    developmentType: 'INDEPENDENT',
    productCategory: 'AGRICULTURAL',
    status: 'DRAFT',
    createdBy: 'testuser',
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
    documentCount: 4,
    auditStatus: 'PENDING',
    ...overrides
  }
}

/**
 * 创建产品列表数据
 */
export function createTestProductList(count = 5) {
  return Array.from({ length: count }, (_, index) =>
    createTestProduct({
      id: `product-test-${index.toString().padStart(3, '0')}`,
      fileName: `测试产品${index + 1}.xlsx`,
      productName: `测试产品${index + 1}`,
      status: index % 2 === 0 ? 'DRAFT' : 'SUBMITTED'
    })
  )
}

/**
 * 创建测试文档数据
 */
export function createTestDocument(overrides: any = {}) {
  return {
    id: 'doc-test-001',
    productId: 'product-test-001',
    fileName: '测试条款.pdf',
    fileType: 'TERMS',
    fileSize: 1024000,
    uploadStatus: 'SUCCESS',
    validationStatus: 'VALID',
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
    ...overrides
  }
}

/**
 * 创建文档列表数据
 */
export function createTestDocumentList(count = 4) {
  const fileTypes = ['TERMS', 'FEASIBILITY', 'ACTUARIAL', 'RATE_TABLE']
  const fileNames = ['条款.pdf', '可行性报告.pdf', '精算报告.pdf', '费率表.xlsx']

  return Array.from({ length: count }, (_, index) =>
    createTestDocument({
      id: `doc-test-${index.toString().padStart(3, '0')}`,
      fileName: `测试${fileNames[index]}`,
      fileType: fileTypes[index]
    })
  )
}

/**
 * 创建农险产品表单数据
 */
export function createAgriculturalFormData(overrides: any = {}) {
  return {
    reportingType: 'POLICY',
    productNature: 'GOVERNMENT_SUBSIDY',
    year: '2024',
    productName: '测试农险产品',
    insuranceSubject: '农作物',
    policyTerm: '1年',
    coverageScope: '全国',
    riskCategory: 'NATURAL_DISASTER',
    premiumRate: '5%',
    maxLiability: '100万元',
    deductible: '1000元',
    claimSettlement: '定损理赔',
    remark: '测试备注',
    ...overrides
  }
}

/**
 * 创建备案产品表单数据
 */
export function createFilingFormData(overrides: any = {}) {
  return {
    developmentType: 'INDEPENDENT',
    templateName: '测试示范条款',
    operatingRegion: '全国',
    businessLine: 'PROPERTY',
    productType: 'TRADITIONAL',
    salesChannel: 'DIRECT',
    targetCustomer: '个人客户',
    riskLevel: 'LOW',
    premiumScale: '500万元',
    marketAnalysis: '市场前景良好',
    competitiveAdvantage: '产品创新',
    remark: '测试备注',
    ...overrides
  }
}

/**
 * 创建审核结果数据
 */
export function createTestAuditResult(overrides: any = {}) {
  return {
    id: 'audit-test-001',
    documentId: 'doc-test-001',
    ruleId: 'rule-001',
    ruleTitle: '条款完整性检查',
    chapter: '第一章 保险责任',
    originalContent: '原始内容',
    suggestion: '修改建议',
    severity: 'WARNING',
    status: 'PENDING',
    createdAt: '2024-01-01T00:00:00',
    ...overrides
  }
}

/**
 * Mock 产品API响应
 */
export function mockProductApi() {
  const productList = createTestProductList()

  return {
    // 获取产品列表
    getProductList: vi.fn(() =>
      Promise.resolve(mockPageResponse(productList, 50, 1, 20))
    ),

    // 获取产品详情
    getProductDetail: vi.fn((id: string) =>
      Promise.resolve(mockApiResponse(createTestProduct({ id })))
    ),

    // 创建产品
    createProduct: vi.fn((data: any) =>
      Promise.resolve(mockApiResponse(createTestProduct({ ...data, id: 'new-product-001' })))
    ),

    // 更新产品
    updateProduct: vi.fn((id: string, data: any) =>
      Promise.resolve(mockApiResponse(createTestProduct({ id, ...data })))
    ),

    // 删除产品
    deleteProduct: vi.fn((id: string) =>
      Promise.resolve(mockApiResponse({ deleted: true }))
    ),

    // 审核产品
    auditProduct: vi.fn((id: string) =>
      Promise.resolve(mockApiResponse({ auditId: 'audit-001' }))
    )
  }
}

/**
 * Mock 文档API响应
 */
export function mockDocumentApi() {
  const documentList = createTestDocumentList()

  return {
    // 上传文档
    uploadDocument: vi.fn((file: File, productId: string) =>
      Promise.resolve(mockApiResponse(createTestDocument({
        productId,
        fileName: file.name,
        fileSize: file.size
      })))
    ),

    // 获取文档列表
    getDocumentList: vi.fn((productId: string) =>
      Promise.resolve(mockApiResponse(documentList.filter(doc => doc.productId === productId)))
    ),

    // 验证文档
    validateDocument: vi.fn((documentId: string) =>
      Promise.resolve(mockApiResponse({
        valid: true,
        errors: [],
        warnings: []
      }))
    ),

    // 删除文档
    deleteDocument: vi.fn((documentId: string) =>
      Promise.resolve(mockApiResponse({ deleted: true }))
    ),

    // 下载文档
    downloadDocument: vi.fn((documentId: string) =>
      Promise.resolve(new Blob(['test content'], { type: 'application/pdf' }))
    )
  }
}

/**
 * Mock 审核API响应
 */
export function mockAuditApi() {
  return {
    // 获取审核结果
    getAuditResults: vi.fn((documentId: string) =>
      Promise.resolve(mockApiResponse([
        createTestAuditResult({ documentId }),
        createTestAuditResult({
          documentId,
          id: 'audit-test-002',
          ruleTitle: '条款规范性检查',
          severity: 'ERROR'
        })
      ]))
    ),

    // 开始审核
    startAudit: vi.fn((productId: string) =>
      Promise.resolve(mockApiResponse({ auditId: 'audit-task-001' }))
    ),

    // 获取审核状态
    getAuditStatus: vi.fn((auditId: string) =>
      Promise.resolve(mockApiResponse({
        status: 'COMPLETED',
        progress: 100,
        totalRules: 50,
        passedRules: 45,
        failedRules: 5
      }))
    )
  }
}

/**
 * 创建Ant Design Vue组件存根（产品专用）
 */
export function createProductAntdStubs() {
  return {
    // 基础组件
    'a-card': {
      template: '<div class="ant-card"><div v-if="$attrs.title" class="ant-card-head">{{ $attrs.title }}</div><div class="ant-card-body"><slot /></div></div>',
      props: ['title', 'size', 'bordered']
    },
    'a-form': {
      template: '<form @submit.prevent="$emit(\'finish\', $attrs.model)" class="ant-form"><slot /></form>',
      props: ['model', 'rules', 'layout']
    },
    'a-form-item': {
      template: '<div class="ant-form-item" :class="{ \'has-error\': validateStatus === \'error\' }"><div v-if="label" class="ant-form-item-label">{{ label }}</div><div class="ant-form-item-control"><slot /></div></div>',
      props: ['label', 'name', 'rules', 'validateStatus', 'help']
    },
    'a-input': {
      template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" v-bind="$attrs" class="ant-input" />',
      props: ['modelValue', 'placeholder', 'size', 'disabled'],
      emits: ['update:modelValue']
    },
    'a-select': {
      template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)" v-bind="$attrs" class="ant-select"><slot /></select>',
      props: ['modelValue', 'placeholder', 'size', 'disabled', 'allowClear'],
      emits: ['update:modelValue']
    },
    'a-select-option': {
      template: '<option :value="value"><slot /></option>',
      props: ['value', 'label', 'disabled']
    },
    'a-radio-group': {
      template: '<div class="ant-radio-group"><slot /></div>',
      props: ['value', 'size', 'disabled'],
      emits: ['change']
    },
    'a-radio-button': {
      template: '<label class="ant-radio-button-wrapper" :class="{ \'ant-radio-button-wrapper-checked\': checked }"><input type="radio" :value="value" :checked="checked" @change="$emit(\'change\', $event)" /><span><slot /></span></label>',
      props: ['value', 'checked'],
      emits: ['change']
    },
    'a-button': {
      template: '<button :disabled="disabled || loading" :class="[\'ant-btn\', type ? \'ant-btn-\' + type : \'\', { \'ant-btn-loading\': loading }]" @click="$emit(\'click\')" v-bind="$attrs"><slot /></button>',
      props: ['disabled', 'loading', 'type', 'size', 'htmlType'],
      emits: ['click']
    },
    'a-table': {
      template: '<div class="ant-table" :class="{ \'ant-table-loading\': loading }"><table><slot /></table></div>',
      props: ['columns', 'dataSource', 'loading', 'pagination', 'rowSelection', 'scroll'],
      emits: ['change', 'expand', 'select', 'selectAll']
    },
    'a-upload': {
      template: '<div class="ant-upload"><slot /></div>',
      props: ['fileList', 'multiple', 'accept', 'beforeUpload', 'customRequest'],
      emits: ['change', 'remove']
    },
    'a-upload-dragger': {
      template: '<div class="ant-upload-drag"><slot /></div>',
      props: ['multiple', 'accept', 'beforeUpload'],
      emits: ['change', 'drop']
    },
    'a-modal': {
      template: '<div v-if="visible" class="ant-modal"><div class="ant-modal-content"><div v-if="title" class="ant-modal-header">{{ title }}</div><div class="ant-modal-body"><slot /></div><div class="ant-modal-footer"><slot name="footer" /></div></div></div>',
      props: ['visible', 'title', 'width', 'centered', 'maskClosable'],
      emits: ['ok', 'cancel', 'update:visible']
    },
    'a-alert': {
      template: '<div class="ant-alert" :class="\'ant-alert-\' + type"><div class="ant-alert-message">{{ message }}</div><div v-if="description" class="ant-alert-description">{{ description }}</div></div>',
      props: ['type', 'message', 'description', 'showIcon', 'closable']
    },
    'a-spin': {
      template: '<div class="ant-spin" :class="{ \'ant-spin-spinning\': spinning }"><slot /></div>',
      props: ['spinning', 'size', 'tip']
    },
    'a-progress': {
      template: '<div class="ant-progress"><div class="ant-progress-inner" :style="{ width: percent + \'%\' }"></div></div>',
      props: ['percent', 'status', 'showInfo']
    },
    'a-tabs': {
      template: '<div class="ant-tabs"><div class="ant-tabs-content"><slot /></div></div>',
      props: ['activeKey', 'type', 'size'],
      emits: ['change']
    },
    'a-tab-pane': {
      template: '<div class="ant-tab-pane"><slot /></div>',
      props: ['key', 'tab', 'disabled']
    },
    'a-divider': {
      template: '<div class="ant-divider"><slot /></div>',
      props: ['type', 'orientation', 'dashed']
    },
    'a-space': {
      template: '<div class="ant-space"><slot /></div>',
      props: ['size', 'direction', 'align', 'wrap']
    },
    'a-breadcrumb': {
      template: '<nav class="ant-breadcrumb"><slot /></nav>',
      props: ['separator']
    },
    'a-breadcrumb-item': {
      template: '<span class="ant-breadcrumb-item"><slot /></span>'
    },
    'a-tooltip': {
      template: '<span class="ant-tooltip"><slot /></span>',
      props: ['title', 'placement', 'trigger']
    },
    'a-tag': {
      template: '<span class="ant-tag" :class="\'ant-tag-\' + color"><slot /></span>',
      props: ['color', 'closable'],
      emits: ['close']
    },
    'a-checkbox': {
      template: '<label class="ant-checkbox-wrapper"><input type="checkbox" :checked="checked" @change="$emit(\'change\', $event)" /><span><slot /></span></label>',
      props: ['checked', 'value', 'disabled'],
      emits: ['change']
    },
    'a-checkbox-group': {
      template: '<div class="ant-checkbox-group"><slot /></div>',
      props: ['value', 'options', 'disabled'],
      emits: ['change']
    }
  }
}

/**
 * 模拟文件对象
 */
export function createMockFile(name = 'test.pdf', size = 1024, type = 'application/pdf') {
  const file = new File(['test content'], name, { type })
  Object.defineProperty(file, 'size', { value: size })
  return file
}

/**
 * 模拟文件列表
 */
export function createMockFileList(count = 1) {
  const files = ['条款.pdf', '可行性报告.pdf', '精算报告.pdf', '费率表.xlsx']
  const types = ['application/pdf', 'application/pdf', 'application/pdf', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']

  return Array.from({ length: Math.min(count, files.length) }, (_, index) =>
    createMockFile(files[index], 1024 * (index + 1), types[index])
  )
}

/**
 * 等待上传完成
 */
export async function waitForUpload(wrapper: any, timeout = 3000) {
  const startTime = Date.now()

  while (Date.now() - startTime < timeout) {
    if (!wrapper.find('.ant-upload-list-item-uploading').exists()) {
      return
    }
    await new Promise(resolve => setTimeout(resolve, 100))
  }

  throw new Error('Upload did not complete within timeout')
}