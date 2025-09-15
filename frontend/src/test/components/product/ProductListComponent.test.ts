import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import ProductListComponent from '@/components/ProductListComponent.vue'
import {
  createTestProductList,
  mockProductApi,
  createProductAntdStubs,
  createTestProduct
} from '@/test/utils/product-test-utils'
import { mountComponent, flushPromises } from '@/test/utils/test-utils'

// Mock Ant Design Vue components
const antdStubs = createProductAntdStubs()

// Mock API module
const mockApi = mockProductApi()
vi.mock('@/api/modules/product', () => ({
  productApi: mockApi
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

describe('ProductListComponent', () => {
  let wrapper: any
  let mockApi: any

  beforeEach(() => {
    const pinia = createPinia()
    setActivePinia(pinia)

    // Reset API mocks
    mockApi = mockProductApi()
    vi.doMock('@/api/modules/product', () => ({
      productApi: mockApi
    }))
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mountComponent(ProductListComponent, {
      props,
      global: {
        stubs: {
          ...antdStubs,
          'ProductFilter': {
            template: '<div class="product-filter"><slot /></div>',
            emits: ['search', 'reset']
          },
          'TableToolbar': {
            template: '<div class="table-toolbar"><slot /></div>',
            props: ['total', 'selectedCount', 'loading'],
            emits: ['refresh', 'clearSelection', 'columnSettings']
          },
          'BatchOperations': {
            template: '<div class="batch-operations"><slot /></div>',
            props: ['selectedKeys'],
            emits: ['batchAudit', 'batchExport', 'batchDelete']
          },
          'ProductTable': {
            template: '<div class="product-table"><slot /></div>',
            props: ['dataSource', 'columns', 'visibleColumnKeys', 'pagination', 'loading', 'selectedRowKeys'],
            emits: ['change', 'expand', 'selectionChange', 'viewDetail', 'viewAuditReport', 'audit', 'edit', 'download', 'delete', 'resetFilters']
          },
          'ColumnSettings': {
            template: '<div class="column-settings" v-if="visible"><slot /></div>',
            props: ['visible', 'selectedKeys', 'columns'],
            emits: ['confirm', 'update:visible', 'update:selectedKeys']
          }
        }
      }
    })
  }

  describe('Component Initialization', () => {
    it('应该正确渲染基本结构', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.product-list-component').exists()).toBe(true)
      expect(wrapper.find('.product-filter').exists()).toBe(true)
      expect(wrapper.find('.table-section').exists()).toBe(true)
      expect(wrapper.find('.product-table').exists()).toBe(true)
    })

    it('应该在hideFilter为true时隐藏筛选器', async () => {
      wrapper = createWrapper({ hideFilter: true })
      await flushPromises()

      expect(wrapper.find('.product-filter').exists()).toBe(false)
      expect(wrapper.find('.product-table').exists()).toBe(true)
    })

    it('应该使用自定义pageSize', async () => {
      wrapper = createWrapper({ pageSize: 20 })
      await flushPromises()

      const vm = wrapper.vm
      expect(vm.pagination.pageSize).toBe(20)
    })

    it('应该在挂载时加载产品列表', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(mockApi.getProductList).toHaveBeenCalledTimes(1)
      expect(wrapper.vm.productList).toHaveLength(5) // 默认创建5个测试产品
    })
  })

  describe('Filter Functionality', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理搜索事件', async () => {
      const filterData = {
        fileName: '测试产品',
        reportType: 'POLICY',
        productCategory: 'AGRICULTURAL'
      }

      const productFilter = wrapper.findComponent({ name: 'ProductFilter' })
      await productFilter.vm.$emit('search', filterData)
      await flushPromises()

      expect(wrapper.vm.filterForm.fileName).toBe('测试产品')
      expect(wrapper.vm.filterForm.reportType).toBe('POLICY')
      expect(wrapper.vm.pagination.current).toBe(1)
      expect(mockApi.getProductList).toHaveBeenCalledWith(
        expect.objectContaining(filterData)
      )
    })

    it('应该处理重置事件', async () => {
      // 先设置一些筛选条件
      wrapper.vm.filterForm.fileName = '测试'
      wrapper.vm.filterForm.reportType = 'POLICY'
      wrapper.vm.pagination.current = 3

      const productFilter = wrapper.findComponent({ name: 'ProductFilter' })
      await productFilter.vm.$emit('reset')
      await flushPromises()

      expect(wrapper.vm.filterForm.fileName).toBe('')
      expect(wrapper.vm.filterForm.reportType).toBe('')
      expect(wrapper.vm.pagination.current).toBe(1)
    })
  })

  describe('Table Functionality', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理表格变化事件（分页）', async () => {
      const paginationInfo = { current: 2, pageSize: 20 }
      const productTable = wrapper.findComponent({ name: 'ProductTable' })

      await productTable.vm.$emit('change', paginationInfo, {}, {})
      await flushPromises()

      expect(wrapper.vm.pagination.current).toBe(2)
      expect(wrapper.vm.pagination.pageSize).toBe(20)
      expect(mockApi.getProductList).toHaveBeenCalledWith(
        expect.objectContaining({
          page: 2,
          size: 20
        })
      )
    })

    it('应该处理表格排序', async () => {
      const sorter = { field: 'productName', order: 'ascend' }
      const productTable = wrapper.findComponent({ name: 'ProductTable' })

      await productTable.vm.$emit('change', {}, {}, sorter)
      await flushPromises()

      expect(wrapper.vm.sortConfig.sortField).toBe('productName')
      expect(wrapper.vm.sortConfig.sortOrder).toBe('ASC')
      expect(mockApi.getProductList).toHaveBeenCalledWith(
        expect.objectContaining({
          sortField: 'productName',
          sortOrder: 'ASC'
        })
      )
    })

    it('应该处理行选择变化', async () => {
      const selectedKeys = ['product-1', 'product-2']
      const selectedRows = [createTestProduct({ id: 'product-1' }), createTestProduct({ id: 'product-2' })]

      const productTable = wrapper.findComponent({ name: 'ProductTable' })
      await productTable.vm.$emit('selectionChange', selectedKeys, selectedRows)

      expect(wrapper.vm.selectedRowKeys).toEqual(selectedKeys)
    })

    it('应该处理查看详情事件', async () => {
      const product = createTestProduct()
      const productTable = wrapper.findComponent({ name: 'ProductTable' })

      await productTable.vm.$emit('viewDetail', product)

      expect(wrapper.emitted('viewDetail')).toBeTruthy()
      expect(wrapper.emitted('viewDetail')[0]).toEqual([product])
    })
  })

  describe('Toolbar Functionality', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理刷新事件', async () => {
      const initialCallCount = mockApi.getProductList.mock.calls.length

      const tableToolbar = wrapper.findComponent({ name: 'TableToolbar' })
      await tableToolbar.vm.$emit('refresh')
      await flushPromises()

      expect(mockApi.getProductList.mock.calls.length).toBe(initialCallCount + 1)
      expect(wrapper.emitted('refresh')).toBeTruthy()
    })

    it('应该处理清除选择事件', async () => {
      wrapper.vm.selectedRowKeys = ['product-1', 'product-2']

      const tableToolbar = wrapper.findComponent({ name: 'TableToolbar' })
      await tableToolbar.vm.$emit('clearSelection')

      expect(wrapper.vm.selectedRowKeys).toEqual([])
    })

    it('应该处理列设置事件', async () => {
      const tableToolbar = wrapper.findComponent({ name: 'TableToolbar' })
      await tableToolbar.vm.$emit('columnSettings')

      expect(wrapper.vm.showColumnSettings).toBe(true)
    })
  })

  describe('Batch Operations', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
      wrapper.vm.selectedRowKeys = ['product-1', 'product-2']
    })

    it('应该处理批量审核事件', async () => {
      const { message } = await import('ant-design-vue')
      const batchOperations = wrapper.findComponent({ name: 'BatchOperations' })

      await batchOperations.vm.$emit('batchAudit', wrapper.vm.selectedRowKeys)

      expect(message.info).toHaveBeenCalledWith('批量检核 2 个产品')
    })

    it('应该处理批量导出事件', async () => {
      const { message } = await import('ant-design-vue')
      const batchOperations = wrapper.findComponent({ name: 'BatchOperations' })

      await batchOperations.vm.$emit('batchExport', wrapper.vm.selectedRowKeys)

      expect(message.info).toHaveBeenCalledWith('批量导出 2 个产品')
    })

    it('应该处理批量删除事件', async () => {
      const { message, Modal } = await import('ant-design-vue')
      const batchOperations = wrapper.findComponent({ name: 'BatchOperations' })

      await batchOperations.vm.$emit('batchDelete', wrapper.vm.selectedRowKeys)

      expect(Modal.confirm).toHaveBeenCalledWith(
        expect.objectContaining({
          title: '批量删除确认',
          content: '确定要删除选中的 2 个产品吗？'
        })
      )
      expect(message.success).toHaveBeenCalledWith('批量删除成功')
      expect(wrapper.vm.selectedRowKeys).toEqual([])
    })
  })

  describe('Product Operations', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理审核报告查看', async () => {
      const { message } = await import('ant-design-vue')
      const product = createTestProduct({ productName: '测试产品' })

      await wrapper.vm.handleViewAuditReport(product)

      expect(message.info).toHaveBeenCalledWith('查看检核报告：测试产品')
    })

    it('应该处理产品审核', async () => {
      const { message } = await import('ant-design-vue')
      const product = createTestProduct({ productName: '测试产品' })

      await wrapper.vm.handleAudit(product)

      expect(message.info).toHaveBeenCalledWith('开始检核：测试产品')
    })

    it('应该处理产品编辑', async () => {
      const { message } = await import('ant-design-vue')
      const product = createTestProduct({ productName: '测试产品' })

      await wrapper.vm.handleEdit(product)

      expect(message.info).toHaveBeenCalledWith('编辑产品：测试产品')
    })

    it('应该处理产品下载', async () => {
      const { message } = await import('ant-design-vue')
      const product = createTestProduct({ productName: '测试产品' })

      await wrapper.vm.handleDownload(product)

      expect(message.info).toHaveBeenCalledWith('下载产品：测试产品')
    })

    it('应该处理产品删除', async () => {
      const { message, Modal } = await import('ant-design-vue')
      const product = createTestProduct({ productName: '测试产品' })

      await wrapper.vm.handleDelete(product)

      expect(Modal.confirm).toHaveBeenCalledWith(
        expect.objectContaining({
          title: '确认删除',
          content: '确定要删除产品"测试产品"吗？'
        })
      )
      expect(message.success).toHaveBeenCalledWith('删除成功')
    })
  })

  describe('Column Settings', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该处理列设置确认', async () => {
      const { message } = await import('ant-design-vue')
      const newColumns = ['productName', 'status', 'operation']

      await wrapper.vm.handleColumnSettingsConfirm(newColumns)

      expect(wrapper.vm.selectedColumns).toEqual(newColumns)
      expect(message.success).toHaveBeenCalledWith('列设置已更新')
    })

    it('应该显示和隐藏列设置抽屉', async () => {
      expect(wrapper.vm.showColumnSettings).toBe(false)

      wrapper.vm.showColumnSettings = true
      await nextTick()

      expect(wrapper.findComponent({ name: 'ColumnSettings' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'ColumnSettings' }).props('visible')).toBe(true)
    })
  })

  describe('API Error Handling', () => {
    it('应该处理API加载失败', async () => {
      const { message } = await import('ant-design-vue')
      mockApi.getProductList.mockRejectedValueOnce(new Error('Network error'))

      wrapper = createWrapper()
      await flushPromises()

      expect(message.error).toHaveBeenCalledWith('获取产品列表失败')
      expect(wrapper.vm.tableLoading).toBe(false)
    })

    it('应该处理API返回错误响应', async () => {
      const { message } = await import('ant-design-vue')
      mockApi.getProductList.mockResolvedValueOnce({
        success: false,
        message: '参数错误',
        data: null
      })

      wrapper = createWrapper()
      await flushPromises()

      expect(message.error).toHaveBeenCalledWith('参数错误')
    })
  })

  describe('Component Exposure', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该暴露refresh方法', () => {
      expect(typeof wrapper.vm.refresh).toBe('function')

      const initialCallCount = mockApi.getProductList.mock.calls.length
      wrapper.vm.refresh()

      expect(mockApi.getProductList.mock.calls.length).toBe(initialCallCount + 1)
    })

    it('应该暴露getSelectedProducts方法', () => {
      wrapper.vm.selectedRowKeys = ['product-1', 'product-2']

      expect(wrapper.vm.getSelectedProducts()).toEqual(['product-1', 'product-2'])
    })

    it('应该暴露clearSelection方法', () => {
      wrapper.vm.selectedRowKeys = ['product-1', 'product-2']
      wrapper.vm.clearSelection()

      expect(wrapper.vm.selectedRowKeys).toEqual([])
    })
  })

  describe('Loading States', () => {
    it('应该在加载时显示loading状态', async () => {
      // 模拟慢速API响应
      mockApi.getProductList.mockImplementationOnce(() =>
        new Promise(resolve => setTimeout(() => resolve({
          success: true,
          data: { records: [], total: 0 }
        }), 100))
      )

      wrapper = createWrapper()

      // 检查loading状态
      expect(wrapper.vm.tableLoading).toBe(true)

      await flushPromises()

      // 检查loading完成后状态
      expect(wrapper.vm.tableLoading).toBe(false)
    })
  })

  describe('Data Flow', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await flushPromises()
    })

    it('应该正确传递props到子组件', () => {
      const productFilter = wrapper.findComponent({ name: 'ProductFilter' })
      const tableToolbar = wrapper.findComponent({ name: 'TableToolbar' })
      const productTable = wrapper.findComponent({ name: 'ProductTable' })

      // 检查ProductTable的props
      expect(productTable.props('dataSource')).toEqual(wrapper.vm.productList)
      expect(productTable.props('columns')).toEqual(wrapper.vm.allColumns)
      expect(productTable.props('pagination')).toEqual(wrapper.vm.pagination)
      expect(productTable.props('loading')).toBe(wrapper.vm.tableLoading)
      expect(productTable.props('selectedRowKeys')).toEqual(wrapper.vm.selectedRowKeys)

      // 检查TableToolbar的props
      expect(tableToolbar.props('total')).toBe(wrapper.vm.pagination.total)
      expect(tableToolbar.props('selectedCount')).toBe(wrapper.vm.selectedRowKeys.length)
      expect(tableToolbar.props('loading')).toBe(wrapper.vm.tableLoading)
    })

    it('应该正确处理v-model绑定', async () => {
      // 测试filterForm的双向绑定
      const productFilter = wrapper.findComponent({ name: 'ProductFilter' })
      expect(productFilter.props('modelValue')).toEqual(wrapper.vm.filterForm)

      // 测试列设置的双向绑定
      wrapper.vm.showColumnSettings = true
      await nextTick()

      const columnSettings = wrapper.findComponent({ name: 'ColumnSettings' })
      expect(columnSettings.props('visible')).toBe(true)
      expect(columnSettings.props('selectedKeys')).toEqual(wrapper.vm.selectedColumns)
    })
  })
})