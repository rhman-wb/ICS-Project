<template>
  <div class="audit-history-manager">
    <!-- 标题和操作区域 -->
    <div class="history-header">
      <div class="header-info">
        <h2 class="section-title">检核历史记录</h2>
        <div class="history-summary" v-if="state.total > 0">
          <a-space>
            <span>共</span>
            <a-tag color="blue" style="font-weight: 600;">{{ state.total }}</a-tag>
            <span>条记录</span>
          </a-space>
        </div>
      </div>
      <div class="header-actions">
        <a-space>
          <a-button :icon="h(ReloadOutlined)" :loading="loading" @click="refreshHistory">
            刷新
          </a-button>
          <a-button
            v-if="selectedRowKeys.length > 0"
            danger
            :icon="h(DeleteOutlined)"
            @click="handleBatchDelete"
          >
            批量删除
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <a-card>
        <a-form layout="inline" class="search-form">
          <a-form-item label="产品名称">
            <a-input
              v-model:value="searchForm.productName"
              placeholder="请输入产品名称"
              allow-clear
              style="width: 200px"
              @press-enter="handleSearch"
            />
          </a-form-item>
          <a-form-item label="执行状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              allow-clear
              style="width: 120px"
            >
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="completed">已完成</a-select-option>
              <a-select-option value="failed">执行失败</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="执行时间">
            <a-range-picker
              v-model:value="searchForm.dateRange"
              style="width: 240px"
              @change="handleDateRangeChange"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" :loading="loading" @click="handleSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>

    <!-- 历史记录表格 -->
    <div class="history-table-section">
      <a-card>
        <template #title>
          <div class="table-header">
            <span>历史记录列表</span>
            <div class="table-actions">
              <a-space>
                <a-checkbox
                  v-if="state.records.length > 0"
                  :indeterminate="indeterminate"
                  :checked="checkAll"
                  @change="handleSelectAll"
                >
                  全选
                </a-checkbox>
                <span class="selection-info" v-if="selectedRowKeys.length > 0">
                  已选择 {{ selectedRowKeys.length }} 项
                </span>
              </a-space>
            </div>
          </div>
        </template>

        <a-table
          :dataSource="state.records"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          :row-selection="rowSelection"
          row-key="id"
          size="middle"
          :scroll="{ x: 1000 }"
          @change="handleTableChange"
        >
          <!-- 自定义列渲染 -->
          <template #bodyCell="{ column, record }">
            <!-- 产品名称 -->
            <template v-if="column.key === 'productName'">
              <a-tooltip :title="record.productName">
                <span class="product-name">{{ record.productName }}</span>
              </a-tooltip>
            </template>

            <!-- 执行状态 -->
            <template v-if="column.key === 'status'">
              <a-tag :color="getStatusColor(record.status)">
                <template #icon>
                  <component :is="getStatusIcon(record.status)" />
                </template>
                {{ getStatusText(record.status) }}
              </a-tag>
            </template>

            <!-- 问题数量 -->
            <template v-if="column.key === 'totalErrors'">
              <a-badge
                :count="record.totalErrors"
                :number-style="{ backgroundColor: record.totalErrors > 0 ? '#f5222d' : '#52c41a' }"
              />
            </template>

            <!-- 执行时间 -->
            <template v-if="column.key === 'executionTime'">
              <a-tooltip :title="formatDateTime(record.executionTime)">
                <span>{{ formatTime(record.executionTime) }}</span>
              </a-tooltip>
            </template>

            <!-- 操作 -->
            <template v-if="column.key === 'operation'">
              <a-space>
                <a-tooltip title="查看详情">
                  <a-button
                    type="link"
                    size="small"
                    :icon="h(EyeOutlined)"
                    @click="handleViewHistory(record)"
                  />
                </a-tooltip>
                <a-tooltip title="下载报告" v-if="record.reportDownloadUrl">
                  <a-button
                    type="link"
                    size="small"
                    :icon="h(DownloadOutlined)"
                    @click="handleDownloadReport(record)"
                  />
                </a-tooltip>
                <a-popconfirm
                  title="确定删除此记录吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDeleteRecord(record)"
                >
                  <a-tooltip title="删除记录">
                    <a-button
                      type="link"
                      size="small"
                      danger
                      :icon="h(DeleteOutlined)"
                    />
                  </a-tooltip>
                </a-popconfirm>
              </a-space>
            </template>
          </template>

          <!-- 空状态 -->
          <template #emptyText>
            <a-empty description="暂无历史记录">
              <a-button type="primary" @click="handleReset">
                重置筛选条件
              </a-button>
            </a-empty>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="检核记录详情"
      width="700px"
      :footer="null"
      @cancel="closeDetailModal"
    >
      <div v-if="selectedRecord" class="history-detail">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="任务ID" :span="2">
            <a-tag color="blue">{{ selectedRecord.taskId }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="产品名称">
            {{ selectedRecord.productName }}
          </a-descriptions-item>
          <a-descriptions-item label="执行状态">
            <a-tag :color="getStatusColor(selectedRecord.status)">
              <template #icon>
                <component :is="getStatusIcon(selectedRecord.status)" />
              </template>
              {{ getStatusText(selectedRecord.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="执行时间">
            {{ formatDateTime(selectedRecord.executionTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="检出问题">
            <a-badge
              :count="selectedRecord.totalErrors"
              :number-style="{ backgroundColor: selectedRecord.totalErrors > 0 ? '#f5222d' : '#52c41a' }"
            />
          </a-descriptions-item>
          <a-descriptions-item label="报告下载" :span="2" v-if="selectedRecord.reportDownloadUrl">
            <a-button
              type="link"
              :icon="h(DownloadOutlined)"
              @click="handleDownloadReport(selectedRecord)"
            >
              下载检核报告
            </a-button>
          </a-descriptions-item>
        </a-descriptions>

        <div class="detail-actions">
          <a-space>
            <a-button @click="closeDetailModal">关闭</a-button>
            <a-button
              v-if="selectedRecord.reportDownloadUrl"
              type="primary"
              :icon="h(DownloadOutlined)"
              @click="handleDownloadReport(selectedRecord)"
            >
              下载报告
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  ReloadOutlined,
  SearchOutlined,
  DeleteOutlined,
  EyeOutlined,
  DownloadOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { auditApi } from '@/api/modules/audit'
import type {
  AuditHistoryManagerProps,
  AuditHistoryManagerEvents,
  AuditHistory,
  AuditHistoryQueryParams,
  AuditHistoryState
} from '@/api/types/audit'

// Props 定义
interface Props extends AuditHistoryManagerProps {}

// Events 定义
interface Emits extends AuditHistoryManagerEvents {}

const props = withDefaults(defineProps<Props>(), {
  showActions: true
})

const emit = defineEmits<Emits>()

// 响应式状态
const state = reactive<AuditHistoryState>({
  records: [],
  queryParams: {
    page: 1,
    size: 10,
    productId: props.productId
  },
  loading: false,
  total: 0,
  current: 1,
  size: 10
})

// 搜索表单
const searchForm = reactive({
  productName: '',
  status: '',
  dateRange: null as any
})

// 选择状态
const selectedRowKeys = ref<string[]>([])
const selectedRecords = ref<AuditHistory[]>([])

// 模态框状态
const detailModalVisible = ref(false)
const selectedRecord = ref<AuditHistory | null>(null)

// 计算属性
const loading = computed(() => state.loading)

const pagination = computed(() => ({
  current: state.current,
  pageSize: state.size,
  total: state.total,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total: number, range: [number, number]) =>
    `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`
}))

const checkAll = computed(() => {
  return selectedRowKeys.value.length === state.records.length && state.records.length > 0
})

const indeterminate = computed(() => {
  return selectedRowKeys.value.length > 0 && selectedRowKeys.value.length < state.records.length
})

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[], rows: AuditHistory[]) => {
    selectedRowKeys.value = keys
    selectedRecords.value = rows
  }
}))

// 表格列配置
const columns = [
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    ellipsis: true,
    width: 200
  },
  {
    title: '执行状态',
    dataIndex: 'status',
    key: 'status',
    width: 120
  },
  {
    title: '检出问题',
    dataIndex: 'totalErrors',
    key: 'totalErrors',
    width: 100,
    sorter: true
  },
  {
    title: '执行时间',
    dataIndex: 'executionTime',
    key: 'executionTime',
    width: 150,
    sorter: true
  },
  {
    title: '操作',
    key: 'operation',
    width: 150,
    fixed: 'right' as const
  }
]

// 数据获取方法
const loadHistory = async (params?: Partial<AuditHistoryQueryParams>): Promise<void> => {
  try {
    state.loading = true

    const queryParams = {
      ...state.queryParams,
      ...params
    }

    const response = await auditApi.getAuditHistory(queryParams)

    if (response.success && response.data) {
      state.records = response.data.records
      state.total = response.data.total
      state.current = response.data.current
      state.size = response.data.size

      Object.assign(state.queryParams, queryParams)
    } else {
      message.error(response.message || '获取历史记录失败')
    }
  } catch (error: any) {
    console.error('Load history error:', error)
    message.error('获取历史记录失败：' + (error.message || '未知错误'))

    state.records = []
    state.total = 0
  } finally {
    state.loading = false
  }
}

const refreshHistory = (): Promise<void> => {
  return loadHistory({ page: 1 })
}

// 搜索和筛选
const handleSearch = (): void => {
  const params: Partial<AuditHistoryQueryParams> = {
    page: 1
  }

  if (searchForm.productName) {
    params.productName = searchForm.productName.trim()
  }

  if (searchForm.status) {
    params.status = searchForm.status
  }

  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startTime = searchForm.dateRange[0].format('YYYY-MM-DD')
    params.endTime = searchForm.dateRange[1].format('YYYY-MM-DD')
  }

  emit('search', params)
  loadHistory(params)
}

const handleReset = (): void => {
  Object.assign(searchForm, {
    productName: '',
    status: '',
    dateRange: null
  })

  const params: Partial<AuditHistoryQueryParams> = {
    page: 1,
    productId: props.productId
  }

  loadHistory(params)
}

const handleDateRangeChange = (): void => {
  // 日期范围变化时自动搜索
  if (searchForm.dateRange) {
    handleSearch()
  }
}

// 表格操作
const handleTableChange = (pag: any, filters: any, sorter: any): void => {
  const params: Partial<AuditHistoryQueryParams> = {
    page: pag.current,
    size: pag.pageSize
  }

  if (sorter.field && sorter.order) {
    params.sortField = sorter.field
    params.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }

  loadHistory(params)
}

const handleSelectAll = (e: any): void => {
  if (e.target.checked) {
    selectedRowKeys.value = state.records.map(record => record.id)
    selectedRecords.value = [...state.records]
  } else {
    selectedRowKeys.value = []
    selectedRecords.value = []
  }
}

// 记录操作
const handleViewHistory = (record: AuditHistory): void => {
  selectedRecord.value = record
  detailModalVisible.value = true
  emit('view-history', record.id)
}

const handleDownloadReport = (record: AuditHistory): void => {
  if (!record.reportDownloadUrl) {
    message.warning('该记录没有可下载的报告')
    return
  }

  emit('download-report', record.reportDownloadUrl)

  // 触发下载
  const link = document.createElement('a')
  link.href = record.reportDownloadUrl
  link.download = `检核报告_${record.productName}_${formatTime(record.executionTime)}.pdf`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  message.success('开始下载检核报告')
}

const handleDeleteRecord = async (record: AuditHistory): Promise<void> => {
  try {
    const response = await auditApi.deleteAuditHistory(record.id)

    if (response.success) {
      message.success('删除成功')
      emit('delete-record', record.id)
      await refreshHistory()
    } else {
      message.error(response.message || '删除失败')
    }
  } catch (error: any) {
    console.error('Delete record error:', error)
    message.error('删除失败：' + (error.message || '未知错误'))
  }
}

const handleBatchDelete = (): void => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要删除的记录')
    return
  }

  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const promises = selectedRowKeys.value.map(id => auditApi.deleteAuditHistory(id))
        await Promise.all(promises)

        message.success(`成功删除 ${selectedRowKeys.value.length} 条记录`)
        selectedRowKeys.value = []
        selectedRecords.value = []
        await refreshHistory()
      } catch (error: any) {
        console.error('Batch delete error:', error)
        message.error('批量删除失败：' + (error.message || '未知错误'))
      }
    }
  })
}

const closeDetailModal = (): void => {
  detailModalVisible.value = false
  selectedRecord.value = null
}

// 工具函数
const getStatusColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    completed: 'success',
    failed: 'error'
  }
  return colorMap[status] || 'default'
}

const getStatusIcon = (status: string): any => {
  const iconMap: Record<string, any> = {
    completed: CheckCircleOutlined,
    failed: ExclamationCircleOutlined
  }
  return iconMap[status] || CheckCircleOutlined
}

const getStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    completed: '已完成',
    failed: '执行失败'
  }
  return textMap[status] || status
}

const formatTime = (time: Date | string): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (time: Date | string): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.audit-history-manager {
  padding: 0;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.history-summary {
  font-size: 14px;
  color: #595959;
}

.filter-section {
  margin-bottom: 16px;
}

.search-form {
  width: 100%;
}

.search-form .ant-form-item {
  margin-bottom: 0;
}

.history-table-section {
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selection-info {
  font-size: 14px;
  color: #595959;
}

.product-name {
  cursor: pointer;
  color: #1890ff;
}

.product-name:hover {
  text-decoration: underline;
}

.history-detail {
  margin-top: 16px;
}

.detail-actions {
  margin-top: 24px;
  text-align: right;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .history-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-info {
    justify-content: center;
    text-align: center;
  }

  .header-actions {
    display: flex;
    justify-content: center;
  }

  .search-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .search-form .ant-form-item {
    width: 100%;
  }

  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

@media (max-width: 576px) {
  .history-header {
    padding: 12px 16px;
  }

  .section-title {
    font-size: 18px;
  }
}
</style>