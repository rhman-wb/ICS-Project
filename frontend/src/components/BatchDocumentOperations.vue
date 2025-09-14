<template>
  <div class="batch-document-operations">
    <!-- 批量操作工具栏 -->
    <div class="batch-toolbar">
      <div class="selection-info">
        <a-checkbox
          :checked="isAllSelected"
          :indeterminate="isIndeterminate"
          @change="handleSelectAll"
        >
          全选
        </a-checkbox>
        <span class="selected-count" v-if="selectedDocuments.length > 0">
          已选择 {{ selectedDocuments.length }} 个文档
        </span>
      </div>

      <div class="batch-actions">
        <a-space>
          <!-- 批量下载原文档 -->
          <a-button
            type="primary"
            :icon="h(DownloadOutlined)"
            @click="handleBatchDownload('original')"
            :disabled="selectedDocuments.length === 0"
            :loading="downloadingOriginal"
          >
            批量下载原文档
          </a-button>

          <!-- 批量下载检核后文档 -->
          <a-button
            :icon="h(FileTextOutlined)"
            @click="handleBatchDownload('audited')"
            :disabled="selectedDocuments.length === 0 || !hasAuditedDocuments"
            :loading="downloadingAudited"
          >
            批量下载检核后文档
          </a-button>

          <!-- 批量审核 -->
          <a-button
            :icon="h(AuditOutlined)"
            @click="handleBatchAudit"
            :disabled="selectedDocuments.length === 0"
            :loading="auditing"
          >
            批量审核
          </a-button>

          <!-- 更多操作 -->
          <a-dropdown>
            <a-button
              :icon="h(MoreOutlined)"
              :disabled="selectedDocuments.length === 0"
            >
              更多操作
              <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu @click="handleMenuClick">
                <a-menu-item key="export">
                  <ExportOutlined />
                  导出列表
                </a-menu-item>
                <a-menu-item key="compress">
                  <CompressOutlined />
                  打包下载
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="delete" class="danger-menu-item">
                  <DeleteOutlined />
                  批量删除
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>

          <!-- 清空选择 -->
          <a-button
            v-if="selectedDocuments.length > 0"
            type="text"
            @click="handleClearSelection"
          >
            清空选择
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 下载进度模态框 -->
    <a-modal
      v-model:open="showDownloadModal"
      title="批量下载"
      :width="600"
      :closable="false"
      :mask-closable="false"
      :footer="downloadCompleted ? ['确定'] : null"
      @ok="handleDownloadModalClose"
    >
      <div class="download-progress">
        <div class="progress-header">
          <span>正在下载 {{ downloadType === 'original' ? '原' : '检核后' }}文档</span>
          <span class="progress-text">{{ downloadProgress.completed }}/{{ downloadProgress.total }}</span>
        </div>

        <a-progress
          :percent="downloadProgressPercent"
          :status="downloadCompleted ? 'success' : 'active'"
        />

        <div class="download-list">
          <div
            v-for="item in downloadItems"
            :key="item.id"
            class="download-item"
            :class="{
              'completed': item.status === 'completed',
              'error': item.status === 'error',
              'downloading': item.status === 'downloading'
            }"
          >
            <div class="item-info">
              <component :is="getFileIcon(item.fileType)" class="file-icon" />
              <span class="file-name">{{ item.fileName }}</span>
            </div>
            <div class="item-status">
              <a-spin v-if="item.status === 'downloading'" size="small" />
              <CheckCircleOutlined v-else-if="item.status === 'completed'" class="success-icon" />
              <ExclamationCircleOutlined v-else-if="item.status === 'error'" class="error-icon" />
              <ClockCircleOutlined v-else class="pending-icon" />
            </div>
          </div>
        </div>

        <div v-if="downloadErrors.length > 0" class="error-summary">
          <a-alert
            type="warning"
            :message="`${downloadErrors.length} 个文档下载失败`"
            :description="downloadErrors.join(', ')"
            show-icon
          />
        </div>
      </div>
    </a-modal>

    <!-- 批量审核进度模态框 -->
    <a-modal
      v-model:open="showAuditModal"
      title="批量审核"
      :width="700"
      :closable="false"
      :mask-closable="false"
      :footer="auditCompleted ? ['确定'] : null"
      @ok="handleAuditModalClose"
    >
      <div class="audit-progress">
        <div class="progress-header">
          <span>正在审核文档</span>
          <span class="progress-text">{{ auditProgress.completed }}/{{ auditProgress.total }}</span>
        </div>

        <a-progress
          :percent="auditProgressPercent"
          :status="auditCompleted ? 'success' : 'active'"
        />

        <div class="audit-list">
          <div
            v-for="item in auditItems"
            :key="item.id"
            class="audit-item"
            :class="{
              'completed': item.status === 'completed',
              'error': item.status === 'error',
              'auditing': item.status === 'auditing'
            }"
          >
            <div class="item-info">
              <component :is="getFileIcon(item.fileType)" class="file-icon" />
              <span class="file-name">{{ item.fileName }}</span>
            </div>
            <div class="item-result" v-if="item.status === 'completed' && item.result">
              <a-tag v-if="item.result.errorCount > 0" color="error">
                错误: {{ item.result.errorCount }}
              </a-tag>
              <a-tag v-if="item.result.warningCount > 0" color="warning">
                警告: {{ item.result.warningCount }}
              </a-tag>
              <a-tag v-if="item.result.errorCount === 0 && item.result.warningCount === 0" color="success">
                通过
              </a-tag>
            </div>
            <div class="item-status">
              <a-spin v-if="item.status === 'auditing'" size="small" />
              <CheckCircleOutlined v-else-if="item.status === 'completed'" class="success-icon" />
              <ExclamationCircleOutlined v-else-if="item.status === 'error'" class="error-icon" />
              <ClockCircleOutlined v-else class="pending-icon" />
            </div>
          </div>
        </div>

        <div v-if="auditErrors.length > 0" class="error-summary">
          <a-alert
            type="error"
            :message="`${auditErrors.length} 个文档审核失败`"
            :description="auditErrors.join(', ')"
            show-icon
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  DownloadOutlined,
  FileTextOutlined,
  AuditOutlined,
  MoreOutlined,
  DownOutlined,
  ExportOutlined,
  CompressOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  FilePdfOutlined,
  FileWordOutlined,
  FileExcelOutlined,
  FileOutlined
} from '@ant-design/icons-vue'
import type { Document } from '@/types/product'

interface DownloadItem {
  id: string
  fileName: string
  fileType: string
  status: 'pending' | 'downloading' | 'completed' | 'error'
}

interface AuditItem {
  id: string
  fileName: string
  fileType: string
  status: 'pending' | 'auditing' | 'completed' | 'error'
  result?: {
    errorCount: number
    warningCount: number
  }
}

interface Props {
  documents: Document[]
  selectedDocuments: string[]
}

interface Emits {
  (e: 'update:selectedDocuments', value: string[]): void
  (e: 'download-completed', downloadType: 'original' | 'audited', documents: Document[]): void
  (e: 'audit-completed', documents: Document[]): void
  (e: 'delete-documents', documentIds: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const showDownloadModal = ref(false)
const showAuditModal = ref(false)
const downloadingOriginal = ref(false)
const downloadingAudited = ref(false)
const auditing = ref(false)
const downloadCompleted = ref(false)
const auditCompleted = ref(false)
const downloadType = ref<'original' | 'audited'>('original')

const downloadProgress = ref({ completed: 0, total: 0 })
const auditProgress = ref({ completed: 0, total: 0 })
const downloadItems = ref<DownloadItem[]>([])
const auditItems = ref<AuditItem[]>([])
const downloadErrors = ref<string[]>([])
const auditErrors = ref<string[]>([])

// 计算属性
const selectedDocuments = computed({
  get: () => props.selectedDocuments,
  set: (value: string[]) => emit('update:selectedDocuments', value)
})

const isAllSelected = computed(() => {
  return props.documents.length > 0 && props.selectedDocuments.length === props.documents.length
})

const isIndeterminate = computed(() => {
  return props.selectedDocuments.length > 0 && props.selectedDocuments.length < props.documents.length
})

const hasAuditedDocuments = computed(() => {
  return props.selectedDocuments.some(id => {
    const document = props.documents.find(doc => doc.id === id)
    return document?.auditResults && document.auditResults.length > 0
  })
})

const downloadProgressPercent = computed(() => {
  if (downloadProgress.value.total === 0) return 0
  return Math.round((downloadProgress.value.completed / downloadProgress.value.total) * 100)
})

const auditProgressPercent = computed(() => {
  if (auditProgress.value.total === 0) return 0
  return Math.round((auditProgress.value.completed / auditProgress.value.total) * 100)
})

// 监听选择变化
watch(
  () => props.selectedDocuments,
  (newValue) => {
    if (newValue.length === 0) {
      // 清空所有进度状态
      resetProgressState()
    }
  }
)

// 事件处理
const handleSelectAll = (e: any) => {
  if (e.target.checked) {
    selectedDocuments.value = props.documents.map(doc => doc.id)
  } else {
    selectedDocuments.value = []
  }
}

const handleClearSelection = () => {
  selectedDocuments.value = []
}

const handleBatchDownload = async (type: 'original' | 'audited') => {
  if (props.selectedDocuments.length === 0) {
    message.warning('请先选择要下载的文档')
    return
  }

  try {
    downloadType.value = type
    showDownloadModal.value = true
    downloadCompleted.value = false
    downloadErrors.value = []

    if (type === 'original') {
      downloadingOriginal.value = true
    } else {
      downloadingAudited.value = true
    }

    // 初始化下载项目
    const selectedDocs = props.documents.filter(doc => props.selectedDocuments.includes(doc.id))
    downloadItems.value = selectedDocs.map(doc => ({
      id: doc.id,
      fileName: doc.fileName,
      fileType: doc.fileType,
      status: 'pending' as const
    }))

    downloadProgress.value = { completed: 0, total: selectedDocs.length }

    // 模拟批量下载过程
    for (let i = 0; i < downloadItems.value.length; i++) {
      const item = downloadItems.value[i]
      item.status = 'downloading'

      try {
        // 模拟下载延时
        await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000))

        // TODO: 实际下载逻辑
        await downloadDocument(item.id, type)

        item.status = 'completed'
        downloadProgress.value.completed++
      } catch (error: any) {
        item.status = 'error'
        downloadErrors.value.push(item.fileName)
        downloadProgress.value.completed++
      }
    }

    downloadCompleted.value = true
    message.success(`批量下载完成，成功下载 ${downloadProgress.value.completed - downloadErrors.value.length} 个文档`)

    emit('download-completed', type, selectedDocs)
  } catch (error: any) {
    message.error('批量下载失败: ' + error.message)
  } finally {
    if (type === 'original') {
      downloadingOriginal.value = false
    } else {
      downloadingAudited.value = false
    }
  }
}

const handleBatchAudit = async () => {
  if (props.selectedDocuments.length === 0) {
    message.warning('请先选择要审核的文档')
    return
  }

  try {
    auditing.value = true
    showAuditModal.value = true
    auditCompleted.value = false
    auditErrors.value = []

    // 初始化审核项目
    const selectedDocs = props.documents.filter(doc => props.selectedDocuments.includes(doc.id))
    auditItems.value = selectedDocs.map(doc => ({
      id: doc.id,
      fileName: doc.fileName,
      fileType: doc.fileType,
      status: 'pending' as const
    }))

    auditProgress.value = { completed: 0, total: selectedDocs.length }

    // 模拟批量审核过程
    for (let i = 0; i < auditItems.value.length; i++) {
      const item = auditItems.value[i]
      item.status = 'auditing'

      try {
        // 模拟审核延时
        await new Promise(resolve => setTimeout(resolve, 2000 + Math.random() * 3000))

        // TODO: 实际审核逻辑
        const auditResult = await auditDocument(item.id)

        item.status = 'completed'
        item.result = auditResult
        auditProgress.value.completed++
      } catch (error: any) {
        item.status = 'error'
        auditErrors.value.push(item.fileName)
        auditProgress.value.completed++
      }
    }

    auditCompleted.value = true
    message.success(`批量审核完成，成功审核 ${auditProgress.value.completed - auditErrors.value.length} 个文档`)

    emit('audit-completed', selectedDocs)
  } catch (error: any) {
    message.error('批量审核失败: ' + error.message)
  } finally {
    auditing.value = false
  }
}

const handleMenuClick = ({ key }: { key: string }) => {
  switch (key) {
    case 'export':
      handleExportList()
      break
    case 'compress':
      handleCompressDownload()
      break
    case 'delete':
      handleBatchDelete()
      break
  }
}

const handleExportList = () => {
  // TODO: 实现导出功能
  message.info('导出功能开发中...')
}

const handleCompressDownload = async () => {
  if (props.selectedDocuments.length === 0) {
    message.warning('请先选择要打包的文档')
    return
  }

  try {
    // TODO: 实现打包下载功能
    message.success(`开始打包下载 ${props.selectedDocuments.length} 个文档`)
  } catch (error: any) {
    message.error('打包下载失败: ' + error.message)
  }
}

const handleBatchDelete = () => {
  if (props.selectedDocuments.length === 0) {
    message.warning('请先选择要删除的文档')
    return
  }

  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${props.selectedDocuments.length} 个文档吗？此操作不可恢复。`,
    okText: '确定删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        // TODO: 实现批量删除功能
        emit('delete-documents', [...props.selectedDocuments])
        selectedDocuments.value = []
        message.success(`已删除 ${props.selectedDocuments.length} 个文档`)
      } catch (error: any) {
        message.error('批量删除失败: ' + error.message)
      }
    }
  })
}

const handleDownloadModalClose = () => {
  showDownloadModal.value = false
  resetProgressState()
}

const handleAuditModalClose = () => {
  showAuditModal.value = false
  resetProgressState()
}

// 工具函数
const resetProgressState = () => {
  downloadProgress.value = { completed: 0, total: 0 }
  auditProgress.value = { completed: 0, total: 0 }
  downloadItems.value = []
  auditItems.value = []
  downloadErrors.value = []
  auditErrors.value = []
  downloadCompleted.value = false
  auditCompleted.value = false
}

const downloadDocument = async (documentId: string, type: 'original' | 'audited') => {
  // TODO: 实际的下载实现
  return new Promise((resolve, reject) => {
    // 模拟随机失败
    if (Math.random() < 0.1) {
      reject(new Error('下载失败'))
    } else {
      resolve(true)
    }
  })
}

const auditDocument = async (documentId: string) => {
  // TODO: 实际的审核实现
  return new Promise<{ errorCount: number; warningCount: number }>((resolve, reject) => {
    // 模拟随机失败
    if (Math.random() < 0.05) {
      reject(new Error('审核失败'))
    } else {
      // 模拟审核结果
      resolve({
        errorCount: Math.floor(Math.random() * 3),
        warningCount: Math.floor(Math.random() * 5)
      })
    }
  })
}

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
</script>

<style scoped lang="scss">
.batch-document-operations {
  .batch-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    margin-bottom: 16px;

    .selection-info {
      display: flex;
      align-items: center;
      gap: 16px;

      .selected-count {
        color: #1890ff;
        font-weight: 500;
      }
    }

    .batch-actions {
      flex-shrink: 0;
    }
  }

  .download-progress,
  .audit-progress {
    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .progress-text {
        font-weight: 500;
        color: #1890ff;
      }
    }

    .download-list,
    .audit-list {
      max-height: 300px;
      overflow-y: auto;
      margin: 16px 0;
      border: 1px solid #f0f0f0;
      border-radius: 6px;
    }

    .download-item,
    .audit-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid #f0f0f0;
      transition: background-color 0.3s;

      &:last-child {
        border-bottom: none;
      }

      &.downloading,
      &.auditing {
        background-color: #e6f7ff;
      }

      &.completed {
        background-color: #f6ffed;
      }

      &.error {
        background-color: #fff2f0;
      }

      .item-info {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;
        min-width: 0;

        .file-icon {
          flex-shrink: 0;
          font-size: 16px;
          color: #1890ff;
        }

        .file-name {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .item-result {
        display: flex;
        gap: 4px;
        margin: 0 16px;
      }

      .item-status {
        flex-shrink: 0;

        .success-icon {
          color: #52c41a;
        }

        .error-icon {
          color: #ff4d4f;
        }

        .pending-icon {
          color: #d9d9d9;
        }
      }
    }

    .error-summary {
      margin-top: 16px;
    }
  }

  .danger-menu-item {
    color: #ff4d4f !important;
  }
}

// 响应式布局
@media (max-width: 768px) {
  .batch-document-operations {
    .batch-toolbar {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .batch-actions {
        align-self: stretch;

        :deep(.ant-space) {
          width: 100%;
          justify-content: center;
        }
      }
    }

    .download-item,
    .audit-item {
      .item-info {
        .file-name {
          font-size: 14px;
        }
      }

      .item-result {
        margin: 0 8px;
      }
    }
  }
}
</style>