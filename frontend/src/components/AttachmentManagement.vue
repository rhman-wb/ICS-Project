<template>
  <div class="attachment-management">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <a-space>
          <a-button
            type="primary"
            :icon="h(UploadOutlined)"
            @click="showUploadModal = true"
          >
            上传文档
          </a-button>
          <a-button
            :icon="h(DownloadOutlined)"
            @click="handleBatchDownload"
            :disabled="selectedDocuments.length === 0"
          >
            批量下载 ({{ selectedDocuments.length }})
          </a-button>
          <a-button
            :icon="h(DeleteOutlined)"
            danger
            @click="handleBatchDelete"
            :disabled="selectedDocuments.length === 0"
          >
            批量删除
          </a-button>
        </a-space>
      </div>
      <div class="toolbar-right">
        <a-space>
          <!-- 文档类型筛选 -->
          <a-select
            v-model:value="filterDocumentType"
            placeholder="文档类型"
            style="width: 150px"
            allowClear
            @change="handleFilterChange"
          >
            <a-select-option value="TERMS">保险条款</a-select-option>
            <a-select-option value="FEASIBILITY_REPORT">可行性报告</a-select-option>
            <a-select-option value="ACTUARIAL_REPORT">精算报告</a-select-option>
            <a-select-option value="RATE_TABLE">费率表</a-select-option>
            <a-select-option value="REGISTRATION_FORM">登记表</a-select-option>
          </a-select>
          <!-- 审核状态筛选 -->
          <a-select
            v-model:value="filterAuditStatus"
            placeholder="审核状态"
            style="width: 120px"
            allowClear
            @change="handleFilterChange"
          >
            <a-select-option value="pending">未检核</a-select-option>
            <a-select-option value="processing">检核中</a-select-option>
            <a-select-option value="passed">检核通过</a-select-option>
            <a-select-option value="failed">检核失败</a-select-option>
          </a-select>
          <!-- 刷新按钮 -->
          <a-button
            :icon="h(ReloadOutlined)"
            @click="handleRefresh"
            :loading="loading"
          >
            刷新
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 附件列表表格 -->
    <a-table
      :columns="columns"
      :data-source="filteredDocuments"
      :loading="loading"
      :pagination="paginationConfig"
      :row-selection="rowSelectionConfig"
      :scroll="{ x: 1200 }"
      row-key="id"
      class="attachment-table"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, index }">
        <!-- 文档名称 -->
        <template v-if="column.key === 'fileName'">
          <div class="file-name-cell">
            <component
              :is="getFileIcon(record.fileType)"
              class="file-icon"
            />
            <div class="file-info">
              <a-tooltip :title="record.fileName">
                <div class="file-name">{{ record.fileName }}</div>
              </a-tooltip>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(record.fileSize) }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- 文档类型 -->
        <template v-if="column.key === 'documentType'">
          <a-tag :color="getDocumentTypeColor(record.documentType)">
            {{ getDocumentTypeName(record.documentType) }}
          </a-tag>
        </template>

        <!-- 上传时间 -->
        <template v-if="column.key === 'uploadedAt'">
          <div class="upload-time">
            <div>{{ formatDate(record.uploadedAt) }}</div>
            <div class="upload-time-detail">{{ formatTime(record.uploadedAt) }}</div>
          </div>
        </template>

        <!-- 审核状态 -->
        <template v-if="column.key === 'auditStatus'">
          <div class="audit-status-cell">
            <a-tag
              :color="getDocumentAuditStatusColor(record.auditResults)"
              class="audit-tag"
            >
              <template #icon>
                <component :is="getAuditStatusIcon(record.auditResults)" />
              </template>
              {{ getDocumentAuditStatus(record.auditResults) }}
            </a-tag>
            <div v-if="record.auditResults && record.auditResults.length > 0" class="audit-summary">
              <span class="error-count" v-if="getErrorCount(record.auditResults) > 0">
                错误: {{ getErrorCount(record.auditResults) }}
              </span>
              <span class="warning-count" v-if="getWarningCount(record.auditResults) > 0">
                警告: {{ getWarningCount(record.auditResults) }}
              </span>
            </div>
          </div>
        </template>

        <!-- 操作 -->
        <template v-if="column.key === 'operation'">
          <a-space>
            <!-- 预览 -->
            <a-tooltip title="预览文档">
              <a-button
                type="link"
                size="small"
                :icon="h(EyeOutlined)"
                @click="handlePreviewDocument(record)"
              />
            </a-tooltip>
            <!-- 下载原文档 -->
            <a-tooltip title="下载原文档">
              <a-button
                type="link"
                size="small"
                :icon="h(DownloadOutlined)"
                @click="handleDownloadDocument(record, 'original')"
              />
            </a-tooltip>
            <!-- 下载检核后文档 -->
            <a-tooltip
              title="下载检核后文档"
              v-if="record.auditResults && record.auditResults.length > 0"
            >
              <a-button
                type="link"
                size="small"
                :icon="h(FileTextOutlined)"
                @click="handleDownloadDocument(record, 'audited')"
              />
            </a-tooltip>
            <!-- 重新上传 -->
            <a-tooltip title="重新上传">
              <a-button
                type="link"
                size="small"
                :icon="h(UploadOutlined)"
                @click="handleReplaceDocument(record)"
              />
            </a-tooltip>
            <!-- 查看审核结果 -->
            <a-tooltip title="查看审核结果">
              <a-button
                type="link"
                size="small"
                :icon="h(AuditOutlined)"
                @click="handleViewAuditResults(record)"
                :disabled="!record.auditResults || record.auditResults.length === 0"
              />
            </a-tooltip>
            <!-- 更多操作 -->
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
                    编辑信息
                  </a-menu-item>
                  <a-menu-item key="history">
                    <HistoryOutlined />
                    查看历史
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

      <!-- 空状态 -->
      <template #emptyText>
        <a-empty
          description="暂无文档数据"
          :image="Empty.PRESENTED_IMAGE_SIMPLE"
        >
          <a-button type="primary" @click="showUploadModal = true">
            上传第一个文档
          </a-button>
        </a-empty>
      </template>
    </a-table>

    <!-- 文档上传模态框 -->
    <a-modal
      v-model:open="showUploadModal"
      title="上传要件文档"
      :width="700"
      @ok="handleUploadConfirm"
      @cancel="showUploadModal = false"
    >
      <DocumentUploadComponent
        ref="uploadComponentRef"
        :product-id="productId"
        @upload-success="handleUploadSuccess"
      />
    </a-modal>

    <!-- 文档预览模态框 -->
    <a-modal
      v-model:open="showPreviewModal"
      :title="`预览: ${previewDocument?.fileName}`"
      :width="1000"
      :footer="null"
      class="preview-modal"
    >
      <div v-if="previewDocument" class="preview-content">
        <!-- 这里根据文件类型显示不同的预览组件 -->
        <div class="preview-placeholder">
          <a-result
            status="info"
            title="文档预览"
            :sub-title="`文件: ${previewDocument.fileName}`"
          >
            <template #extra>
              <a-space>
                <a-button @click="showPreviewModal = false">关闭</a-button>
                <a-button type="primary" @click="handleDownloadDocument(previewDocument, 'original')">
                  下载文档
                </a-button>
              </a-space>
            </template>
          </a-result>
        </div>
      </div>
    </a-modal>

    <!-- 审核结果模态框 -->
    <a-modal
      v-model:open="showAuditModal"
      :title="`审核结果: ${auditDocument?.fileName}`"
      :width="1200"
      :footer="null"
      class="audit-modal"
    >
      <div v-if="auditDocument && auditDocument.auditResults" class="audit-content">
        <a-table
          :columns="auditColumns"
          :data-source="auditDocument.auditResults"
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'severity'">
              <a-tag :color="getSeverityColor(record.severity)">
                {{ getSeverityText(record.severity) }}
              </a-tag>
            </template>
          </template>
        </a-table>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { Empty } from 'ant-design-vue'
import {
  UploadOutlined,
  DownloadOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EyeOutlined,
  FileTextOutlined,
  AuditOutlined,
  MoreOutlined,
  EditOutlined,
  HistoryOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileExcelOutlined,
  FileOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import type { TableColumnType } from 'ant-design-vue'
import type { Document, AuditResult } from '@/types/product'
import { formatDateTime, formatDate, formatTime, formatFileSize } from '@/utils/format'
import DocumentUploadComponent from '@/components/DocumentUploadComponent.vue'

interface Props {
  productId: string
  documents?: Document[]
  loading?: boolean
}

interface Emits {
  (e: 'refresh'): void
  (e: 'upload-success'): void
  (e: 'document-updated', document: Document): void
  (e: 'document-deleted', documentId: string): void
}

const props = withDefaults(defineProps<Props>(), {
  documents: () => [],
  loading: false
})

const emit = defineEmits<Emits>()

// 响应式数据
const selectedDocuments = ref<string[]>([])
const filterDocumentType = ref<string>()
const filterAuditStatus = ref<string>()
const showUploadModal = ref(false)
const showPreviewModal = ref(false)
const showAuditModal = ref(false)
const previewDocument = ref<Document | null>(null)
const auditDocument = ref<Document | null>(null)

// 组件引用
const uploadComponentRef = ref()

// 分页配置
const paginationConfig = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表格列配置
const columns: TableColumnType[] = [
  {
    title: '文档名称',
    dataIndex: 'fileName',
    key: 'fileName',
    width: 250,
    ellipsis: true
  },
  {
    title: '文档类型',
    dataIndex: 'documentType',
    key: 'documentType',
    width: 120,
    align: 'center'
  },
  {
    title: '上传时间',
    dataIndex: 'uploadedAt',
    key: 'uploadedAt',
    width: 150,
    align: 'center',
    sorter: true
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 150,
    align: 'center'
  },
  {
    title: '操作',
    key: 'operation',
    width: 200,
    align: 'center',
    fixed: 'right'
  }
]

// 审核结果表格列配置
const auditColumns: TableColumnType[] = [
  {
    title: '规则ID',
    dataIndex: 'ruleId',
    key: 'ruleId',
    width: 120
  },
  {
    title: '适用章节',
    dataIndex: 'applicableChapter',
    key: 'applicableChapter',
    width: 150
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    key: 'severity',
    width: 100,
    align: 'center'
  },
  {
    title: '原文内容',
    dataIndex: 'originalContent',
    key: 'originalContent',
    width: 200,
    ellipsis: true
  },
  {
    title: '修改建议',
    dataIndex: 'suggestion',
    key: 'suggestion',
    ellipsis: true
  }
]

// 计算属性
const filteredDocuments = computed(() => {
  let filtered = props.documents

  if (filterDocumentType.value) {
    filtered = filtered.filter(doc => doc.documentType === filterDocumentType.value)
  }

  if (filterAuditStatus.value) {
    filtered = filtered.filter(doc => {
      const status = getDocumentAuditStatus(doc.auditResults).toLowerCase()
      return status.includes(filterAuditStatus.value!)
    })
  }

  return filtered
})

// 行选择配置
const rowSelectionConfig = computed(() => ({
  selectedRowKeys: selectedDocuments.value,
  onChange: (selectedRowKeys: string[]) => {
    selectedDocuments.value = selectedRowKeys
  }
}))

// 生命周期
onMounted(() => {
  updatePaginationTotal()
})

// 更新分页总数
const updatePaginationTotal = () => {
  paginationConfig.value.total = filteredDocuments.value.length
}

// 事件处理
const handleFilterChange = () => {
  updatePaginationTotal()
}

const handleRefresh = () => {
  emit('refresh')
}

const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  paginationConfig.value = { ...paginationConfig.value, ...pagination }
}

const handleUploadConfirm = () => {
  uploadComponentRef.value?.submit()
}

const handleUploadSuccess = () => {
  showUploadModal.value = false
  message.success('文档上传成功')
  emit('upload-success')
}

const handlePreviewDocument = (record: Document) => {
  previewDocument.value = record
  showPreviewModal.value = true
}

const handleDownloadDocument = async (record: Document, type: 'original' | 'audited') => {
  try {
    // TODO: 实现文档下载功能
    const downloadType = type === 'audited' ? '检核后' : '原'
    message.success(`开始下载${downloadType}文档: ${record.fileName}`)
  } catch (error: any) {
    message.error('下载失败: ' + error.message)
  }
}

const handleReplaceDocument = (record: Document) => {
  // TODO: 实现文档替换功能
  message.info(`重新上传文档: ${record.fileName}`)
}

const handleViewAuditResults = (record: Document) => {
  auditDocument.value = record
  showAuditModal.value = true
}

const handleBatchDownload = async () => {
  if (selectedDocuments.value.length === 0) {
    message.warning('请先选择要下载的文档')
    return
  }

  try {
    // TODO: 实现批量下载功能
    message.success(`开始批量下载 ${selectedDocuments.value.length} 个文档`)
    selectedDocuments.value = []
  } catch (error: any) {
    message.error('批量下载失败: ' + error.message)
  }
}

const handleBatchDelete = () => {
  if (selectedDocuments.value.length === 0) {
    message.warning('请先选择要删除的文档')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedDocuments.value.length} 个文档吗？此操作不可恢复。`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        // TODO: 实现批量删除功能
        message.success(`已删除 ${selectedDocuments.value.length} 个文档`)
        selectedDocuments.value = []
        emit('refresh')
      } catch (error: any) {
        message.error('批量删除失败: ' + error.message)
      }
    }
  })
}

const handleMenuClick = ({ key }: { key: string }, record: Document) => {
  switch (key) {
    case 'edit':
      message.info(`编辑文档信息: ${record.fileName}`)
      break
    case 'history':
      message.info(`查看文档历史: ${record.fileName}`)
      break
    case 'delete':
      Modal.confirm({
        title: '确认删除',
        content: `确定要删除文档 "${record.fileName}" 吗？此操作不可恢复。`,
        okText: '确定',
        okType: 'danger',
        cancelText: '取消',
        onOk: async () => {
          try {
            // TODO: 实现删除功能
            message.success(`已删除文档: ${record.fileName}`)
            emit('document-deleted', record.id)
          } catch (error: any) {
            message.error('删除失败: ' + error.message)
          }
        }
      })
      break
  }
}

// 工具函数
const getFileIcon = (fileType: string) => {
  const iconMap: Record<string, any> = {
    'pdf': FilePdfOutlined,
    'doc': FileWordOutlined,
    'docx': FileWordOutlined,
    'xls': FileExcelOutlined,
    'xlsx': FileExcelOutlined
  }
  return iconMap[fileType] || FileOutlined
}

const getDocumentTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'TERMS': 'blue',
    'FEASIBILITY_REPORT': 'green',
    'ACTUARIAL_REPORT': 'orange',
    'RATE_TABLE': 'purple',
    'REGISTRATION_FORM': 'cyan'
  }
  return colorMap[type] || 'default'
}

const getDocumentTypeName = (type: string) => {
  const nameMap: Record<string, string> = {
    'TERMS': '保险条款',
    'FEASIBILITY_REPORT': '可行性报告',
    'ACTUARIAL_REPORT': '精算报告',
    'RATE_TABLE': '费率表',
    'REGISTRATION_FORM': '登记表'
  }
  return nameMap[type] || type
}

const getDocumentAuditStatusColor = (auditResults?: AuditResult[]) => {
  if (!auditResults || auditResults.length === 0) return 'default'

  const hasError = auditResults.some(result => result.severity === 'error')
  const hasWarning = auditResults.some(result => result.severity === 'warning')

  if (hasError) return 'error'
  if (hasWarning) return 'warning'
  return 'success'
}

const getDocumentAuditStatus = (auditResults?: AuditResult[]) => {
  if (!auditResults || auditResults.length === 0) return '未检核'

  const hasError = auditResults.some(result => result.severity === 'error')
  const hasWarning = auditResults.some(result => result.severity === 'warning')

  if (hasError) return '检核失败'
  if (hasWarning) return '存在问题'
  return '检核通过'
}

const getAuditStatusIcon = (auditResults?: AuditResult[]) => {
  if (!auditResults || auditResults.length === 0) return ClockCircleOutlined

  const hasError = auditResults.some(result => result.severity === 'error')
  const hasWarning = auditResults.some(result => result.severity === 'warning')

  if (hasError) return CloseCircleOutlined
  if (hasWarning) return ExclamationCircleOutlined
  return CheckCircleOutlined
}

const getErrorCount = (auditResults: AuditResult[]) => {
  return auditResults.filter(result => result.severity === 'error').length
}

const getWarningCount = (auditResults: AuditResult[]) => {
  return auditResults.filter(result => result.severity === 'warning').length
}

const getSeverityColor = (severity: string) => {
  const colorMap: Record<string, string> = {
    'error': 'error',
    'warning': 'warning',
    'info': 'info'
  }
  return colorMap[severity] || 'default'
}

const getSeverityText = (severity: string) => {
  const textMap: Record<string, string> = {
    'error': '错误',
    'warning': '警告',
    'info': '信息'
  }
  return textMap[severity] || severity
}
</script>

<style scoped lang="scss">
.attachment-management {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: white;
    border-radius: 8px 8px 0 0;
    border-bottom: 1px solid #f0f0f0;

    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
    }
  }

  .attachment-table {
    background: white;
    border-radius: 0 0 8px 8px;

    .file-name-cell {
      display: flex;
      align-items: center;
      gap: 8px;

      .file-icon {
        flex-shrink: 0;
        font-size: 18px;
        color: #1890ff;
      }

      .file-info {
        min-width: 0;
        flex: 1;

        .file-name {
          font-weight: 500;
          color: #262626;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .file-meta {
          margin-top: 2px;

          .file-size {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }
    }

    .upload-time {
      text-align: center;

      .upload-time-detail {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 2px;
      }
    }

    .audit-status-cell {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .audit-tag {
        margin: 0;
      }

      .audit-summary {
        display: flex;
        gap: 8px;
        font-size: 11px;

        .error-count {
          color: #ff4d4f;
        }

        .warning-count {
          color: #faad14;
        }
      }
    }
  }

  .preview-modal {
    .preview-content {
      min-height: 400px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .preview-placeholder {
      text-align: center;
      color: #8c8c8c;
    }
  }

  .audit-modal {
    .audit-content {
      max-height: 500px;
      overflow-y: auto;
    }
  }

  .danger-menu-item {
    color: #ff4d4f !important;
  }
}

// 响应式布局
@media (max-width: 768px) {
  .attachment-management {
    .toolbar {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .toolbar-left,
      .toolbar-right {
        justify-content: center;
      }
    }

    .attachment-table {
      :deep(.ant-table-tbody > tr > td) {
        padding: 8px 4px;
      }
    }
  }
}
</style>