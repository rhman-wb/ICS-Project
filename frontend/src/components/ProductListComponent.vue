<template>
  <div class="product-list-component">
    <!-- 筛选查询模块 -->
    <ProductFilter
      v-if="!hideFilter"
      v-model="filterForm"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 产品列表表格 -->
    <div class="table-section">
      <a-card class="table-card">
        <!-- 表格工具栏 -->
        <TableToolbar
          :total="pagination.total"
          :selected-count="selectedRowKeys.length"
          :loading="tableLoading"
          @refresh="refreshData"
          @clear-selection="clearSelection"
          @column-settings="showColumnSettings = true"
        />

        <!-- 批量操作栏 -->
        <BatchOperations
          :selected-keys="selectedRowKeys"
          @batch-audit="handleBatchAudit"
          @batch-export="handleBatchExport"
          @batch-delete="handleBatchDelete"
        />

        <!-- 产品列表表格 -->
        <ProductTable
          :data-source="productList"
          :columns="allColumns"
          :visible-column-keys="selectedColumns"
          :pagination="pagination"
          :loading="tableLoading"
          :selected-row-keys="selectedRowKeys"
          @change="handleTableChange"
          @expand="handleExpand"
          @selection-change="handleSelectionChange"
          @view-detail="handleViewDetail"
          @view-audit-report="handleViewAuditReport"
          @audit="handleAudit"
          @edit="handleEdit"
          @download="handleDownload"
          @delete="handleDelete"
          @reset-filters="handleReset"
        />
      </a-card>
    </div>

    <!-- 列设置抽屉 -->
    <ColumnSettings
      v-model:visible="showColumnSettings"
      v-model:selected-keys="selectedColumns"
      :columns="columnConfigs"
      @confirm="handleColumnSettingsConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import type { TableColumnsType } from 'ant-design-vue'
import ProductFilter from './product/ProductFilter.vue'
import TableToolbar from './product/TableToolbar.vue'
import BatchOperations from './product/BatchOperations.vue'
import ProductTable from './product/ProductTable.vue'
import ColumnSettings from './product/ColumnSettings.vue'
import { productApi, type ProductInfo, type ProductQueryParams } from '@/api/modules/product'
import { getToken, isTokenExpired } from '@/utils/auth'
import { useAuthStore } from '@/stores/modules/auth'

// 组件名称
defineOptions({
  name: 'ProductListComponent'
})

// Props
interface Props {
  hideFilter?: boolean
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  hideFilter: false,
  pageSize: 10
})

// Emits
const emit = defineEmits<{
  selectProduct: [product: ProductInfo]
  viewDetail: [product: ProductInfo]
  refresh: []
}>()

// 路由和认证
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const tableLoading = ref(false)
const productList = ref<ProductInfo[]>([])
const selectedRowKeys = ref<string[]>([])
const showColumnSettings = ref(false)

// 筛选表单
const filterForm = reactive<ProductQueryParams>({
  fileName: '',
  reportType: '',
  developmentType: '',
  productCategory: '',
  primaryAdditional: '',
  revisionType: '',
  operatingRegion: '',
  year: ''
})

// 排序配置
const sortConfig = reactive({
  sortField: '',
  sortOrder: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: props.pageSize,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number, range: [number, number]) =>
    `第 ${range[0]}-${range[1]} 项，共 ${total} 项`
})

// 列配置
const allColumns: TableColumnsType = [
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 300,
    ellipsis: true,
    fixed: 'left',
    sorter: true
  },
  {
    title: '模板类型',
    dataIndex: 'templateType',
    key: 'templateType',
    width: 120,
    align: 'center',
    sorter: true,
    customRender: ({ text }) => {
      if (!text) return '-'
      return text === 'backup' ? '备案产品' : text === 'agricultural' ? '农险产品' : text
    }
  },
  {
    title: '产品代码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 150,
    align: 'center',
    ellipsis: true
  },
  {
    title: '保险类别',
    dataIndex: 'insuranceCategory',
    key: 'insuranceCategory',
    width: 120,
    align: 'center',
    ellipsis: true
  },
  {
    title: '报送类型',
    dataIndex: 'reportType',
    key: 'reportType',
    width: 100,
    align: 'center',
    sorter: true
  },
  {
    title: '开发方式',
    dataIndex: 'developmentMethod',
    key: 'developmentMethod',
    width: 100,
    align: 'center',
    sorter: true
  },
  {
    title: '产品类别',
    dataIndex: 'productCategory',
    key: 'productCategory',
    width: 100,
    align: 'center',
    sorter: true
  },
  {
    title: '主附险',
    dataIndex: 'primaryAdditional',
    key: 'primaryAdditional',
    width: 80,
    align: 'center'
  },
  {
    title: '修订类型',
    dataIndex: 'revisionType',
    key: 'revisionType',
    width: 120,
    align: 'center'
  },
  {
    title: '经营区域',
    dataIndex: 'operatingRegion',
    key: 'operatingRegion',
    width: 120,
    align: 'center',
    ellipsis: true
  },
  {
    title: '保险期限',
    dataIndex: 'insurancePeriod',
    key: 'insurancePeriod',
    width: 120,
    align: 'center',
    ellipsis: true
  },
  {
    title: '保险金额',
    dataIndex: 'coverageAmount',
    key: 'coverageAmount',
    width: 120,
    align: 'right',
    customRender: ({ text }) => {
      if (!text) return '-'
      return typeof text === 'number' ? text.toLocaleString('zh-CN') : text
    }
  },
  {
    title: '年度',
    dataIndex: 'year',
    key: 'year',
    width: 80,
    align: 'center',
    sorter: true
  },
  {
    title: '产品状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center'
  },
  {
    title: '检核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 120,
    align: 'center'
  },
  {
    title: '文档数',
    dataIndex: 'documentCount',
    key: 'documentCount',
    width: 80,
    align: 'center'
  },
  {
    title: '更新时间',
    dataIndex: 'updatedAt',
    key: 'updatedAt',
    width: 120,
    align: 'center',
    sorter: true
  },
  {
    title: '操作',
    key: 'operation',
    width: 180,
    align: 'center',
    fixed: 'right'
  }
]

// 列设置配置
const columnConfigs = [
  { key: 'productName', title: '产品名称', required: true, group: 'basic' },
  { key: 'templateType', title: '模板类型', group: 'basic' },
  { key: 'productCode', title: '产品代码', group: 'basic' },
  { key: 'insuranceCategory', title: '保险类别', group: 'basic' },
  { key: 'reportType', title: '报送类型', group: 'basic' },
  { key: 'developmentMethod', title: '开发方式', group: 'attribute' },
  { key: 'productCategory', title: '产品类别', group: 'attribute' },
  { key: 'primaryAdditional', title: '主附险', group: 'attribute' },
  { key: 'revisionType', title: '修订类型', group: 'attribute' },
  { key: 'operatingRegion', title: '经营区域', group: 'attribute' },
  { key: 'insurancePeriod', title: '保险期限', group: 'attribute' },
  { key: 'coverageAmount', title: '保险金额', group: 'attribute' },
  { key: 'year', title: '年度', group: 'time' },
  { key: 'status', title: '产品状态', group: 'status' },
  { key: 'auditStatus', title: '检核状态', group: 'status' },
  { key: 'documentCount', title: '文档数', group: 'status' },
  { key: 'updatedAt', title: '更新时间', group: 'time' },
  { key: 'operation', title: '操作', required: true, group: 'other' }
]

// 默认显示的列
const selectedColumns = ref([
  'productName',
  'templateType',
  'productCode',
  'insuranceCategory',
  'reportType',
  'developmentMethod',
  'productCategory',
  'status',
  'auditStatus',
  'documentCount',
  'updatedAt',
  'operation'
])

// 事件处理
const handleSearch = (filters: ProductQueryParams) => {
  Object.assign(filterForm, filters)
  pagination.current = 1
  loadProductList()
}

const handleReset = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key as keyof ProductQueryParams] = ''
  })
  pagination.current = 1
  loadProductList()
}

const handleTableChange = (paginationInfo: any, filters: any, sorter: any) => {
  // 更新分页
  pagination.current = paginationInfo.current
  pagination.pageSize = paginationInfo.pageSize
  
  // 更新排序
  if (sorter.field) {
    sortConfig.sortField = sorter.field
    sortConfig.sortOrder = sorter.order === 'ascend' ? 'ASC' : 'DESC'
  } else {
    sortConfig.sortField = ''
    sortConfig.sortOrder = ''
  }
  
  loadProductList()
}

const handleSelectionChange = (keys: string[], rows: ProductInfo[]) => {
  selectedRowKeys.value = keys
}

const clearSelection = () => {
  selectedRowKeys.value = []
}

const refreshData = () => {
  loadProductList()
  emit('refresh')
}

const handleExpand = (expanded: boolean, record: ProductInfo) => {
  // 展开行处理逻辑
}

// 业务操作
const handleViewDetail = (record: ProductInfo) => {
  emit('viewDetail', record)
}

const handleViewAuditReport = (record: ProductInfo) => {
  message.info(`查看检核报告：${record.productName}`)
}

const handleAudit = (record: ProductInfo) => {
  message.info(`开始检核：${record.productName}`)
}

const handleEdit = (record: ProductInfo) => {
  message.info(`编辑产品：${record.productName}`)
}

const handleDownload = (record: ProductInfo) => {
  message.info(`下载产品：${record.productName}`)
}

const handleDelete = (record: ProductInfo) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除产品"${record.productName}"吗？`,
    onOk() {
      message.success('删除成功')
      loadProductList()
    }
  })
}

const handleBatchAudit = (keys: string[]) => {
  message.info(`批量检核 ${keys.length} 个产品`)
}

const handleBatchExport = (keys: string[]) => {
  message.info(`批量导出 ${keys.length} 个产品`)
}

const handleBatchDelete = (keys: string[]) => {
  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${keys.length} 个产品吗？`,
    onOk() {
      message.success('批量删除成功')
      selectedRowKeys.value = []
      loadProductList()
    }
  })
}

const handleColumnSettingsConfirm = (keys: string[]) => {
  selectedColumns.value = keys
  message.success('列设置已更新')
}

// 数据加载
const loadProductList = async () => {
  try {
    tableLoading.value = true

    // 检查认证状态
    const token = getToken()
    if (!token) {
      message.error('登录状态已失效，请重新登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      return
    }

    // 检查token是否过期
    if (isTokenExpired(token)) {
      console.log('Token expired, attempting refresh...')
      const refreshSuccess = await authStore.refreshTokenAction()

      if (!refreshSuccess) {
        message.error('登录已过期，请重新登录')
        setTimeout(() => {
          router.push('/login')
        }, 1500)
        return
      }
    }

    const params = {
      ...filterForm,
      ...sortConfig,
      page: pagination.current,
      size: pagination.pageSize
    }

    const response = await productApi.getProductList(params)

    if (response.success) {
      productList.value = response.data.records
      pagination.total = response.data.total

      // 如果成功获取到数据，清除之前的错误状态
      if (response.data.records.length > 0) {
        console.log(`Successfully loaded ${response.data.records.length} products`)
      } else {
        message.info('当前没有符合条件的产品数据')
      }
    } else {
      message.error(response.message || '获取产品列表失败')
    }
  } catch (error: any) {
    console.error('Load product list error:', error)

    // 处理不同类型的错误
    if (error?.response?.status === 401) {
      message.error('身份验证失败，请重新登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else if (error?.response?.status === 403) {
      message.error('没有权限访问产品管理功能，请联系管理员')
    } else if (error?.response?.status === 404) {
      message.error('产品管理服务暂时不可用，请稍后重试')
    } else if (error?.code === 'NETWORK_ERROR' || !error?.response) {
      message.error('网络连接失败，请检查网络后重试')
    } else {
      // 通用错误处理
      const errorMessage = error?.response?.data?.message || error?.message || '获取产品列表失败'
      message.error(errorMessage)
    }
  } finally {
    tableLoading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadProductList()
})

// 暴露方法给父组件
defineExpose({
  refresh: loadProductList,
  getSelectedProducts: () => selectedRowKeys.value,
  clearSelection
})
</script>

<style scoped>
.product-list-component {
  background: #f5f5f5;
  min-height: 100%;
  padding: 16px;
}

.table-section {
  margin-top: 16px;
}

.table-card {
  border-radius: 8px;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .product-list-component {
    padding: 8px;
  }
  
  .table-section {
    margin-top: 8px;
  }
}
</style>
