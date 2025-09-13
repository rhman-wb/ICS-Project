<template>
  <a-table
    :columns="visibleColumns"
    :data-source="dataSource"
    :pagination="pagination"
    :loading="loading"
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
        <a-button type="primary" @click="handleResetFilters">
          重置筛选条件
        </a-button>
      </a-empty>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { Empty } from 'ant-design-vue'
import {
  EyeOutlined,
  FileTextOutlined,
  AuditOutlined,
  MoreOutlined,
  EditOutlined,
  DownloadOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import { formatTime } from '@/utils/date'

interface Product {
  id: string
  productName: string
  reportType: string
  developmentMethod: string
  primaryAdditional: string
  productType: string
  productTypeDescription: string
  status: string
  statusDescription: string
  auditStatus: string
  documentCount: number
  updatedAt: string
  createdAt: string
  createdBy: string
  productNature?: string
  originalProductName?: string
  primarySituation?: string
  operatingScope?: string
}

interface Props {
  dataSource: Product[]
  columns: TableColumnType[]
  visibleColumnKeys: string[]
  pagination: any
  loading?: boolean
  selectedRowKeys: string[]
}

interface Emits {
  (e: 'change', pagination: any, filters: any, sorter: any): void
  (e: 'expand', expanded: boolean, record: Product): void
  (e: 'selectionChange', selectedKeys: string[], selectedRows: Product[]): void
  (e: 'viewDetail', record: Product): void
  (e: 'viewAuditReport', record: Product): void
  (e: 'audit', record: Product): void
  (e: 'edit', record: Product): void
  (e: 'download', record: Product): void
  (e: 'delete', record: Product): void
  (e: 'resetFilters'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

// 计算可见列
const visibleColumns = computed(() => {
  return props.columns.filter(col => props.visibleColumnKeys.includes(col.key as string))
})

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: props.selectedRowKeys,
  onChange: (selectedRowKeys: string[], selectedRows: Product[]) => {
    emit('selectionChange', selectedRowKeys, selectedRows)
  }
}))

// 事件处理
const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  emit('change', pagination, filters, sorter)
}

const handleExpand = (expanded: boolean, record: Product) => {
  emit('expand', expanded, record)
}

const handleViewDetail = (record: Product) => {
  emit('viewDetail', record)
}

const handleViewAuditReport = (record: Product) => {
  emit('viewAuditReport', record)
}

const handleAudit = (record: Product) => {
  emit('audit', record)
}

const handleMenuClick = ({ key }: { key: string }, record: Product) => {
  switch (key) {
    case 'edit':
      emit('edit', record)
      break
    case 'download':
      emit('download', record)
      break
    case 'delete':
      emit('delete', record)
      break
  }
}

const handleResetFilters = () => {
  emit('resetFilters')
}

// 状态颜色和图标
const getProductTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'AGRICULTURAL': 'green',
    'FILING': 'blue'
  }
  return colorMap[type] || 'default'
}

const getReportTypeColor = (type: string) => {
  return type === '新产品' ? 'blue' : 'orange'
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
    '未检核': 'default',
    '检核中': 'processing',
    '检核完成': 'success',
    '检核失败': 'error'
  }
  return colorMap[status] || 'default'
}

const getAuditStatusIcon = (status: string) => {
  const iconMap: Record<string, any> = {
    '未检核': ClockCircleOutlined,
    '检核中': ClockCircleOutlined,
    '检核完成': CheckCircleOutlined,
    '检核失败': ExclamationCircleOutlined
  }
  return iconMap[status] || ClockCircleOutlined
}
</script>

<style scoped>
.product-table {
  background: white;
}

.product-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name-link {
  text-align: left;
  padding: 0;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-meta {
  display: flex;
  gap: 4px;
}

.expanded-content {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 6px;
  margin: 8px 0;
}

.danger-menu-item {
  color: #ff4d4f !important;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: top;
}

:deep(.ant-table-tbody > tr.ant-table-row-selected > td) {
  background-color: #e6f7ff;
}

:deep(.ant-badge-count) {
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  font-size: 12px;
}
</style>