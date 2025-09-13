<template>
  <div class="product-list-component">
    <!-- 筛选查询模块 -->
    <div class="filter-section">
      <a-card title="筛选查询" class="filter-card">
        <a-form
          :model="filterForm"
          layout="horizontal"
          class="filter-form"
          @finish="handleSearch"
        >
          <a-row :gutter="16">
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="文件名" name="fileName">
                <a-input
                  v-model:value="filterForm.fileName"
                  placeholder="支持产品名称关键字搜索"
                  allow-clear
                  @press-enter="handleSearch"
                />
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="报送类型" name="reportType">
                <a-select
                  v-model:value="filterForm.reportType"
                  placeholder="请选择报送类型"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="新产品">新产品</a-select-option>
                  <a-select-option value="修订产品">修订产品</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="开发类型" name="developmentType">
                <a-select
                  v-model:value="filterForm.developmentType"
                  placeholder="请选择开发类型"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="自主开发">自主开发</a-select-option>
                  <a-select-option value="合作开发">合作开发</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="产品类别" name="productCategory">
                <a-select
                  v-model:value="filterForm.productCategory"
                  placeholder="请选择产品类别"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="种植险">种植险</a-select-option>
                  <a-select-option value="养殖险">养殖险</a-select-option>
                  <a-select-option value="不区分">不区分</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="主附险" name="primaryAdditional">
                <a-select
                  v-model:value="filterForm.primaryAdditional"
                  placeholder="请选择主附险"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="主险">主险</a-select-option>
                  <a-select-option value="附险">附险</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="修订类型" name="revisionType">
                <a-select
                  v-model:value="filterForm.revisionType"
                  placeholder="请选择修订类型"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="条款修订">条款修订</a-select-option>
                  <a-select-option value="费率修订">费率修订</a-select-option>
                  <a-select-option value="条款费率修订">条款费率修订</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="经营区域" name="operatingRegion">
                <a-input
                  v-model:value="filterForm.operatingRegion"
                  placeholder="支持地区名称输入"
                  allow-clear
                  @press-enter="handleSearch"
                />
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="年度" name="year">
                <a-select
                  v-model:value="filterForm.year"
                  placeholder="请选择年度"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="2024">2024</a-select-option>
                  <a-select-option value="2023">2023</a-select-option>
                  <a-select-option value="2022">2022</a-select-option>
                  <a-select-option value="2021">2021</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row>
            <a-col :span="24" style="text-align: right">
              <a-space>
                <a-button @click="handleReset">
                  <template #icon>
                    <ReloadOutlined />
                  </template>
                  重置
                </a-button>
                <a-button type="primary" html-type="submit">
                  <template #icon>
                    <SearchOutlined />
                  </template>
                  查询
                </a-button>
              </a-space>
            </a-col>
          </a-row>
        </a-form>
      </a-card>
    </div>

    <!-- 产品列表表格 -->
    <div class="table-section">
      <a-card class="table-card">
        <!-- 表格工具栏 -->
        <div class="table-toolbar">
          <div class="toolbar-left">
            <a-space>
              <span class="result-count">
                共找到 {{ pagination.total }} 个产品
              </span>
              <a-divider type="vertical" />
              <a-button
                v-if="hasSelected"
                type="link"
                @click="clearSelection"
              >
                取消选择
              </a-button>
            </a-space>
          </div>
          <div class="toolbar-right">
            <a-space>
              <a-tooltip title="刷新数据">
                <a-button
                  shape="circle"
                  :icon="h(ReloadOutlined)"
                  :loading="tableLoading"
                  @click="refreshData"
                />
              </a-tooltip>
              <a-tooltip title="列设置">
                <a-button
                  shape="circle"
                  :icon="h(SettingOutlined)"
                  @click="showColumnSettings = true"
                />
              </a-tooltip>
            </a-space>
          </div>
        </div>

        <!-- 批量操作栏 -->
        <div class="batch-operations" v-if="hasSelected">
          <a-space>
            <span class="selected-info">
              已选择 {{ selectedRowKeys.length }} 项
            </span>
            <a-button type="link" @click="handleBatchAudit">
              <template #icon>
                <AuditOutlined />
              </template>
              批量检核
            </a-button>
            <a-button type="link" @click="handleBatchExport">
              <template #icon>
                <ExportOutlined />
              </template>
              批量导出
            </a-button>
            <a-button type="link" danger @click="handleBatchDelete">
              <template #icon>
                <DeleteOutlined />
              </template>
              批量删除
            </a-button>
          </a-space>
        </div>

        <!-- 产品列表表格 -->
        <a-table
          :columns="visibleColumns"
          :data-source="productList"
          :pagination="pagination"
          :loading="tableLoading"
          :row-selection="rowSelection"
          :scroll="{ x: 1400 }"
          class="product-table"
          row-key="id"
          @change="handleTableChange"
          @expand="handleExpand"
        >
          <!-- 自定义列渲染 -->
          <template #bodyCell="{ column, record, index }">
            <!-- 产品名称 -->
            <template v-if="column.key === 'productName'">
              <div class="product-name-cell">
                <a-button
                  type="link"
                  class="product-name-link"
                  @click="handleViewDetail(record)"
                >
                  <a-tooltip :title="record.productName">
                    {{ record.productName }}
                  </a-tooltip>
                </a-button>
                <div class="product-meta">
                  <a-tag size="small" :color="getProductTypeColor(record.productType)">
                    {{ record.productTypeDescription }}
                  </a-tag>
                </div>
              </div>
            </template>

            <!-- 报送类型 -->
            <template v-if="column.key === 'reportType'">
              <a-tag :color="getReportTypeColor(record.reportType)">
                {{ record.reportType }}
              </a-tag>
            </template>

            <!-- 开发方式 -->
            <template v-if="column.key === 'developmentMethod'">
              <a-tag>{{ record.developmentMethod }}</a-tag>
            </template>

            <!-- 主附险 -->
            <template v-if="column.key === 'primaryAdditional'">
              <a-tag :color="record.primaryAdditional === '主险' ? 'blue' : 'green'">
                {{ record.primaryAdditional }}
              </a-tag>
            </template>

            <!-- 产品状态 -->
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                {{ record.statusDescription }}
              </a-tag>
            </template>

            <!-- 检核状态 -->
            <template v-if="column.key === 'auditStatus'">
              <a-tag :color="getAuditStatusColor(record.auditStatus)">
                <template #icon>
                  <component :is="getAuditStatusIcon(record.auditStatus)" />
                </template>
                {{ record.auditStatus }}
              </a-tag>
            </template>

            <!-- 文档数量 -->
            <template v-if="column.key === 'documentCount'">
              <a-badge
                :count="record.documentCount"
                :number-style="{ backgroundColor: record.documentCount > 0 ? '#52c41a' : '#d9d9d9' }"
              />
            </template>

            <!-- 更新时间 -->
            <template v-if="column.key === 'updatedAt'">
              <a-tooltip :title="record.updatedAt">
                {{ formatTime(record.updatedAt) }}
              </a-tooltip>
            </template>

            <!-- 操作列 -->
            <template v-if="column.key === 'operation'">
              <a-space>
                <a-tooltip title="查看详情">
                  <a-button
                    type="link"
                    size="small"
                    :icon="h(EyeOutlined)"
                    @click="handleViewDetail(record)"
                  />
                </a-tooltip>
                <a-tooltip title="查看检核报告">
                  <a-button
                    type="link"
                    size="small"
                    :icon="h(FileTextOutlined)"
                    @click="handleViewAuditReport(record)"
                  />
                </a-tooltip>
                <a-tooltip title="智能检核">
                  <a-button
                    type="link"
                    size="small"
                    :icon="h(AuditOutlined)"
                    @click="handleAudit(record)"
                  />
                </a-tooltip>
                <a-dropdown>
                  <a-button
                    type="link"
                    size="small"
                    :icon="h(MoreOutlined)"
                  />
                  <template #overlay>
                    <a-menu @click="handleMenuClick($event, record)">
                      <a-menu-item key="edit">
                        <EditOutlined />
                        编辑
                      </a-menu-item>
                      <a-menu-item key="download">
                        <DownloadOutlined />
                        下载
                      </a-menu-item>
                      <a-menu-divider />
                      <a-menu-item key="delete" class="danger-menu-item">
                        <DeleteOutlined />
                        删除
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </a-space>
            </template>
          </template>

          <!-- 展开行内容 -->
          <template #expandedRowRender="{ record }">
            <div class="expanded-content">
              <a-descriptions :column="2" size="small">
                <a-descriptions-item label="产品性质">
                  {{ record.productNature }}
                </a-descriptions-item>
                <a-descriptions-item label="原产品名称">
                  {{ record.originalProductName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="主险情况">
                  {{ record.primarySituation || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="经营范围">
                  {{ record.operatingScope || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="创建人">
                  {{ record.createdBy }}
                </a-descriptions-item>
                <a-descriptions-item label="创建时间">
                  {{ record.createdAt }}
                </a-descriptions-item>
              </a-descriptions>
            </div>
          </template>

          <!-- 空状态 -->
          <template #emptyText>
            <a-empty
              description="暂无产品数据"
              :image="Empty.PRESENTED_IMAGE_SIMPLE"
            >
              <a-button type="primary" @click="handleReset">
                重置筛选条件
              </a-button>
            </a-empty>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 列设置抽屉 -->
    <a-drawer
      v-model:open="showColumnSettings"
      title="列设置"
      placement="right"
      width="300"
    >
      <a-checkbox-group
        v-model:value="selectedColumns"
        class="column-settings"
        @change="handleColumnChange"
      >
        <div
          v-for="column in allColumns"
          :key="column.key"
          class="column-item"
        >
          <a-checkbox :value="column.key">
            {{ column.title }}
          </a-checkbox>
        </div>
      </a-checkbox-group>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { message, Empty } from 'ant-design-vue'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  SettingOutlined,
  AuditOutlined,
  ExportOutlined,
  DeleteOutlined,
  EyeOutlined,
  FileTextOutlined,
  EditOutlined,
  DownloadOutlined,
  MoreOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined
} from '@ant-design/icons-vue'
import { productApi, type ProductInfo, type ProductQueryParams } from '@/api/modules/product'

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

// 计算属性
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

// 行选择配置
const rowSelection: TableProps['rowSelection'] = {
  selectedRowKeys,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
}

// 所有可用列配置
const allColumns: TableColumnsType = [
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 300,
    ellipsis: true,
    fixed: 'left'
  },
  {
    title: '报送类型',
    dataIndex: 'reportType',
    key: 'reportType',
    width: 100,
    align: 'center'
  },
  {
    title: '开发方式',
    dataIndex: 'developmentMethod',
    key: 'developmentMethod',
    width: 100,
    align: 'center'
  },
  {
    title: '产品类别',
    dataIndex: 'productCategory',
    key: 'productCategory',
    width: 100,
    align: 'center'
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
    title: '年度',
    dataIndex: 'year',
    key: 'year',
    width: 80,
    align: 'center'
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
    align: 'center'
  },
  {
    title: '操作',
    key: 'operation',
    width: 120,
    fixed: 'right',
    align: 'center'
  }
]

// 选中的列
const selectedColumns = ref<string[]>([
  'productName',
  'reportType',
  'developmentMethod',
  'productCategory',
  'primaryAdditional',
  'auditStatus',
  'documentCount',
  'updatedAt',
  'operation'
])

// 可见的列
const visibleColumns = computed(() => {
  return allColumns.filter(column => selectedColumns.value.includes(column.key as string))
})

// 方法
const loadProductList = async () => {
  tableLoading.value = true
  try {
    const params: ProductQueryParams = {
      page: pagination.current,
      size: pagination.pageSize,
      ...filterForm
    }

    const response = await productApi.getProductList(params)

    if (response.success && response.data) {
      productList.value = response.data.records
      pagination.total = response.data.total
      pagination.current = response.data.current
    } else {
      message.error(response.message || '获取产品列表失败')
    }
  } catch (error) {
    console.error('获取产品列表失败:', error)
    message.error('获取产品列表失败，请稍后重试')
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
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

const handleTableChange: TableProps['onChange'] = (pag) => {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  loadProductList()
}

const refreshData = () => {
  loadProductList()
  emit('refresh')
}

const clearSelection = () => {
  selectedRowKeys.value = []
}

const handleColumnChange = () => {
  // 列设置变化时的处理
}

const handleViewDetail = (record: ProductInfo) => {
  emit('viewDetail', record)
}

const handleExpand = (expanded: boolean, record: ProductInfo) => {
  if (expanded) {
    emit('selectProduct', record)
  }
}

// Mock 操作方法
const handleViewAuditReport = (record: ProductInfo) => {
  message.info(`查看 ${record.productName} 的检核报告`)
}

const handleAudit = (record: ProductInfo) => {
  message.info(`对 ${record.productName} 执行智能检核`)
}

const handleMenuClick = ({ key }: { key: string }, record: ProductInfo) => {
  switch (key) {
    case 'edit':
      message.info(`编辑 ${record.productName}`)
      break
    case 'download':
      message.info(`下载 ${record.productName} 相关文档`)
      break
    case 'delete':
      message.warning(`删除 ${record.productName}`)
      break
  }
}

const handleBatchAudit = () => {
  message.info(`对选中的 ${selectedRowKeys.value.length} 个产品执行批量检核`)
}

const handleBatchExport = () => {
  message.info(`导出选中的 ${selectedRowKeys.value.length} 个产品`)
}

const handleBatchDelete = () => {
  message.warning(`删除选中的 ${selectedRowKeys.value.length} 个产品`)
}

// 工具方法
const getReportTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    '新产品': 'green',
    '修订产品': 'blue'
  }
  return colorMap[type] || 'default'
}

const getProductTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'AGRICULTURAL': 'orange',
    'FILING': 'purple'
  }
  return colorMap[type] || 'default'
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'DRAFT': 'default',
    'SUBMITTED': 'processing',
    'APPROVED': 'success',
    'REJECTED': 'error'
  }
  return colorMap[status] || 'default'
}

const getAuditStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    '未上传文档': 'default',
    '待审核': 'orange',
    '审核中': 'blue',
    '已完成': 'green',
    '审核失败': 'red'
  }
  return colorMap[status] || 'default'
}

const getAuditStatusIcon = (status: string) => {
  const iconMap: Record<string, any> = {
    '未上传文档': ExclamationCircleOutlined,
    '待审核': ClockCircleOutlined,
    '审核中': LoadingOutlined,
    '已完成': CheckCircleOutlined,
    '审核失败': CloseCircleOutlined
  }
  return iconMap[status] || ExclamationCircleOutlined
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const oneDay = 24 * 60 * 60 * 1000

  if (diff < oneDay) {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } else if (diff < 7 * oneDay) {
    const days = Math.floor(diff / oneDay)
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit'
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadProductList()
})

// 暴露方法供父组件调用
defineExpose({
  refresh: refreshData,
  clearSelection,
  getSelectedProducts: () => productList.value.filter(item => selectedRowKeys.value.includes(item.id))
})
</script>

<style scoped lang="scss">
.product-list-component {
  .filter-section {
    margin-bottom: 16px;

    .filter-card {
      .filter-form {
        .ant-form-item {
          margin-bottom: 16px;
        }
      }
    }
  }

  .table-section {
    .table-card {
      .table-toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 16px;
        border-bottom: 1px solid #f0f0f0;
        margin-bottom: 16px;

        .toolbar-left {
          .result-count {
            color: #666;
            font-size: 14px;
          }
        }

        .toolbar-right {
          .ant-btn {
            color: #666;

            &:hover {
              color: #1890ff;
            }
          }
        }
      }

      .batch-operations {
        padding: 12px 16px;
        background: #f6f8fa;
        border-radius: 6px;
        margin-bottom: 16px;

        .selected-info {
          color: #1890ff;
          font-weight: 500;
        }

        .ant-btn-link {
          color: #1890ff;

          &.ant-btn-dangerous {
            color: #ff4d4f;
          }
        }
      }

      .product-table {
        .product-name-cell {
          .product-name-link {
            color: #1890ff;
            padding: 0;
            margin-bottom: 4px;
            display: block;
            text-align: left;

            &:hover {
              color: #40a9ff;
            }
          }

          .product-meta {
            .ant-tag {
              margin: 0;
              font-size: 12px;
            }
          }
        }

        .expanded-content {
          padding: 16px;
          background: #fafafa;
          border-radius: 6px;
        }

        :deep(.ant-table-thead) {
          > tr > th {
            background: #fafafa;
            font-weight: 600;
            border-bottom: 2px solid #f0f0f0;
          }
        }

        :deep(.ant-table-tbody) {
          > tr {
            &:hover > td {
              background: #f5f9fe;
            }

            &.ant-table-row-selected > td {
              background: #e6f7ff;
            }
          }
        }

        :deep(.ant-dropdown-menu) {
          .danger-menu-item {
            color: #ff4d4f;

            &:hover {
              background: #fff1f0;
              color: #ff4d4f;
            }
          }
        }
      }
    }
  }

  .column-settings {
    .column-item {
      display: block;
      margin-bottom: 8px;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .product-list-component {
    .filter-section {
      .filter-form {
        .ant-col {
          margin-bottom: 16px;
        }
      }
    }

    .table-section {
      .table-card {
        .table-toolbar {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;

          .toolbar-left,
          .toolbar-right {
            width: 100%;
          }

          .toolbar-right {
            text-align: right;
          }
        }

        .batch-operations {
          .ant-space {
            width: 100%;
            flex-wrap: wrap;
          }
        }
      }
    }
  }
}
</style>