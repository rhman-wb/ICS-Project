<template>
  <div class="product-management">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <a-button 
          type="link" 
          class="back-button"
          @click="handleBack"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title">产品管理</h1>
      </div>
      <div class="header-right">
        <a-button 
          type="primary"
          @click="handleImportProduct"
        >
          <template #icon>
            <PlusOutlined />
          </template>
          导入产品
        </a-button>
      </div>
    </div>

    <!-- 筛选查询模块 -->
    <div class="filter-section">
      <a-card title="筛选查询" class="filter-card">
        <a-form
          :model="filterForm"
          layout="horizontal"
          class="filter-form"
        >
          <a-row :gutter="16">
            <a-col :span="6">
              <a-form-item label="文件名">
                <a-input
                  v-model:value="filterForm.fileName"
                  placeholder="支持产品名称关键字搜索"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="报送类型">
                <a-select
                  v-model:value="filterForm.reportType"
                  placeholder="请选择报送类型"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="new">新产品</a-select-option>
                  <a-select-option value="modified">修订产品</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="开发类型">
                <a-select
                  v-model:value="filterForm.developmentType"
                  placeholder="请选择开发类型"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="independent">自主开发</a-select-option>
                  <a-select-option value="cooperative">合作开发</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="产品类别">
                <a-select
                  v-model:value="filterForm.productCategory"
                  placeholder="请选择产品类别"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="planting">种植险</a-select-option>
                  <a-select-option value="breeding">养殖险</a-select-option>
                  <a-select-option value="undifferentiated">不区分</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :span="6">
              <a-form-item label="主附险">
                <a-select
                  v-model:value="filterForm.primaryAdditional"
                  placeholder="请选择主附险"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="primary">主险</a-select-option>
                  <a-select-option value="additional">附险</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="修订类型">
                <a-select
                  v-model:value="filterForm.revisionType"
                  placeholder="请选择修订类型"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="terms">条款修订</a-select-option>
                  <a-select-option value="rate">费率修订</a-select-option>
                  <a-select-option value="both">条款费率修订</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="经营区域">
                <a-input
                  v-model:value="filterForm.operatingRegion"
                  placeholder="支持地区名称输入"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="年度">
                <a-select
                  v-model:value="filterForm.year"
                  placeholder="请选择年度"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="2024">2024</a-select-option>
                  <a-select-option value="2023">2023</a-select-option>
                  <a-select-option value="2022">2022</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row>
            <a-col :span="24" style="text-align: right">
              <a-space>
                <a-button @click="handleReset">重置</a-button>
                <a-button type="primary" @click="handleSearch">
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

    <!-- 产品列表区域 -->
    <div class="table-section">
      <a-card class="table-card">
        <!-- 批量操作栏 -->
        <div class="batch-operations" v-if="hasSelected">
          <a-space>
            <span>已选择 {{ selectedRowKeys.length }} 项</span>
            <a-button type="link" @click="handleBatchAudit">
              <template #icon>
                <AuditOutlined />
              </template>
              智能检核
            </a-button>
            <a-button type="link" @click="handleBatchFollow">
              <template #icon>
                <HeartOutlined />
              </template>
              关注
            </a-button>
            <a-button type="link" danger @click="handleBatchDelete">
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </a-space>
        </div>

        <!-- 产品列表表格 -->
        <a-table
          :columns="columns"
          :data-source="productList"
          :pagination="pagination"
          :loading="tableLoading"
          :row-selection="rowSelection"
          :scroll="{ x: 1200 }"
          class="product-table"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'fileName'">
              <a-button
                type="link"
                class="file-name-link"
                @click="handleViewDetail(record)"
              >
                {{ record.fileName }}
              </a-button>
            </template>
            <template v-if="column.key === 'reportType'">
              <a-tag :color="getReportTypeColor(record.reportType)">
                {{ record.reportType }}
              </a-tag>
            </template>
            <template v-if="column.key === 'updateTime'">
              {{ formatTime(record.updateTime) }}
            </template>
            <template v-if="column.key === 'auditStatus'">
              <a-tag :color="getAuditStatusColor(record.auditStatus)">
                {{ record.auditStatus }}
              </a-tag>
            </template>
            <template v-if="column.key === 'operation'">
              <a-space>
                <a-button type="link" size="small" @click="handleViewAuditReport(record)">
                  查看检核报告
                </a-button>
                <a-button type="link" size="small" @click="handleAudit(record)">
                  智能检核
                </a-button>
                <a-button type="link" size="small" @click="handleFollow(record)">
                  关注
                </a-button>
                <a-button type="link" size="small" danger @click="handleDelete(record)">
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
          
          <!-- 空状态 -->
          <template #emptyText>
            <a-empty 
              description="暂无数据，请使用筛选查询功能"
              :image="Empty.PRESENTED_IMAGE_SIMPLE"
            />
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Empty } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  PlusOutlined,
  SearchOutlined,
  AuditOutlined,
  HeartOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'

// 路由
const router = useRouter()

// 筛选表单数据
const filterForm = reactive({
  fileName: '',
  reportType: '',
  developmentType: '',
  productCategory: '',
  primaryAdditional: '',
  revisionType: '',
  operatingRegion: '',
  year: ''
})

// 产品列表数据
const productList = ref<any[]>([])
const tableLoading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条数据`
})

// 行选择
const selectedRowKeys = ref<string[]>([])
const hasSelected = computed(() => selectedRowKeys.value.length > 0)

const rowSelection = {
  selectedRowKeys,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  }
}

// 表格列配置
const columns: TableColumnsType = [
  {
    title: '文件名',
    dataIndex: 'fileName',
    key: 'fileName',
    width: 300,
    ellipsis: true
  },
  {
    title: '报送类型',
    dataIndex: 'reportType',
    key: 'reportType',
    width: 100,
    align: 'center'
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 120,
    align: 'center'
  },
  {
    title: '开发类型',
    dataIndex: 'developmentType',
    key: 'developmentType',
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
    align: 'center'
  },
  {
    title: '年度',
    dataIndex: 'year',
    key: 'year',
    width: 80,
    align: 'center'
  },
  {
    title: '检核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 100,
    align: 'center'
  },
  {
    title: '操作',
    key: 'operation',
    width: 300,
    fixed: 'right',
    align: 'center'
  }
]

// 事件处理方法
const handleBack = () => {
  router.push('/dashboard/home')
}

const handleImportProduct = () => {
  router.push('/product/import')
}

const handleSearch = () => {
  pagination.current = 1
  loadProductList()
}

const handleReset = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = ''
  })
  pagination.current = 1
  loadProductList()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadProductList()
}

const handleViewDetail = (record: any) => {
  router.push(`/product/detail/${record.id}`)
}

// Mock 操作方法（MVP阶段）
const handleViewAuditReport = (record: any) => {
  message.info('查看检核报告功能正在开发中（使用mock数据演示）')
}

const handleAudit = (record: any) => {
  message.info('智能检核功能正在开发中（使用mock数据演示）')
}

const handleFollow = (record: any) => {
  message.info('关注功能正在开发中（使用mock数据演示）')
}

const handleDelete = (record: any) => {
  message.info('删除功能正在开发中（使用mock数据演示）')
}

const handleBatchAudit = () => {
  message.info('批量智能检核功能正在开发中（使用mock数据演示）')
}

const handleBatchFollow = () => {
  message.info('批量关注功能正在开发中（使用mock数据演示）')
}

const handleBatchDelete = () => {
  message.info('批量删除功能正在开发中（使用mock数据演示）')
}

// 工具方法
const getReportTypeColor = (type: string) => {
  const colorMap = {
    '新产品': 'green',
    '修订产品': 'blue'
  }
  return colorMap[type] || 'default'
}

const getAuditStatusColor = (status: string) => {
  const colorMap = {
    '待检核': 'orange',
    '检核中': 'blue',
    '已检核': 'green',
    '检核失败': 'red'
  }
  return colorMap[status] || 'default'
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

// 加载产品列表数据
const loadProductList = async () => {
  tableLoading.value = true
  try {
    // TODO: 调用实际API获取数据
    // 目前使用 mock 数据
    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟API调用
    
    // Mock 数据
    productList.value = []
    pagination.total = 0
    
    message.info('产品列表功能需要连接后端API')
  } catch (error) {
    console.error('加载产品列表失败:', error)
    message.error('加载产品列表失败')
  } finally {
    tableLoading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadProductList()
})
</script>

<style scoped lang="scss">
.product-management {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;

      .back-button {
        padding: 4px 8px;
        margin-right: 12px;
        color: #1890ff;
        
        &:hover {
          background: #f0f9ff;
        }
      }

      .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #262626;
        margin: 0;
      }
    }
  }

  .filter-section {
    margin-bottom: 24px;

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
      .batch-operations {
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        margin-bottom: 16px;

        .ant-btn-link {
          color: #1890ff;
          
          &.ant-btn-dangerous {
            color: #ff4d4f;
          }
        }
      }

      .product-table {
        .file-name-link {
          color: #0079FE;
          text-decoration: underline;
          padding: 0;

          &:hover {
            color: #1890ff;
          }
        }

        :deep(.ant-table-thead) {
          > tr > th {
            background: #fafafa;
            font-weight: 600;
          }
        }

        :deep(.ant-table-tbody) {
          > tr {
            &:hover > td {
              background: #f5f9fe;
            }
          }
        }
      }
    }
  }
}
</style>