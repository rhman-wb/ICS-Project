<template>
  <a-modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    width="80%"
    :footer="null"
    :closable="!uploading"
    :maskClosable="!uploading"
    @cancel="handleCancel"
    class="document-upload-dialog"
  >
    <div class="upload-dialog-content">
      <!-- 上传提示区域 -->
      <div class="upload-tips">
        <div class="tips-header">
          <InfoCircleOutlined class="tips-icon" />
          <span>上传要求</span>
        </div>
        <ul class="tips-list">
          <li>支持格式：{{ getSupportedFormatsText() }}</li>
          <li>单个文件大小限制：{{ maxFileSizeMB }}MB</li>
          <li>可同时上传多个文件</li>
          <li>支持拖拽上传</li>
        </ul>
      </div>

      <!-- 主上传区域 -->
      <div class="main-upload-area">
        <a-upload-dragger
          v-model:fileList="fileList"
          :name="'files'"
          :multiple="allowMultiple"
          :accept="acceptedFileTypes"
          :before-upload="beforeUpload"
          @change="handleUploadChange"
          @drop="handleDrop"
          :disabled="uploading"
          class="upload-dragger"
        >
          <div class="upload-content">
            <p class="ant-upload-drag-icon">
              <CloudUploadOutlined />
            </p>
            <p class="ant-upload-text">
              <span v-if="!uploading">点击或拖拽文件到此区域上传</span>
              <span v-else>正在上传中...</span>
            </p>
            <p class="ant-upload-hint">
              支持 {{ getSupportedFormatsText() }}，单个文件不超过 {{ maxFileSizeMB }}MB
            </p>
          </div>
        </a-upload-dragger>
      </div>

      <!-- 文件列表区域 -->
      <div v-if="fileList.length > 0" class="file-list-section">
        <div class="section-header">
          <span class="file-count">已选择文件 ({{ fileList.length }})</span>
          <div class="section-actions">
            <a-button
              size="small"
              @click="clearAllFiles"
              :disabled="uploading"
            >
              <template #icon>
                <DeleteOutlined />
              </template>
              清空全部
            </a-button>
          </div>
        </div>

        <div class="file-list">
          <div
            v-for="(file, index) in fileList"
            :key="file.uid || index"
            class="file-item"
            :class="{ 'uploading': file.status === 'uploading', 'error': file.status === 'error' }"
          >
            <div class="file-info">
              <div class="file-icon-name">
                <span class="file-icon">
                  <FileTextOutlined v-if="isDocumentFile(file)" />
                  <FileExcelOutlined v-else-if="isExcelFile(file)" />
                  <FilePdfOutlined v-else-if="isPdfFile(file)" />
                  <FileOutlined v-else />
                </span>
                <span class="file-name" :title="file.name">{{ file.name }}</span>
              </div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(file.size || 0) }}</span>
                <span class="file-status" :class="file.status">
                  {{ getFileStatusText(file.status) }}
                </span>
              </div>
            </div>

            <!-- 上传进度条 -->
            <div v-if="file.status === 'uploading' && file.percent !== undefined" class="upload-progress">
              <a-progress
                :percent="file.percent"
                :show-info="false"
                size="small"
                :stroke-color="{ '0%': '#108ee9', '100%': '#87d068' }"
              />
              <span class="progress-text">{{ file.percent }}%</span>
            </div>

            <!-- 文件操作 -->
            <div class="file-actions">
              <a-button
                v-if="file.status !== 'uploading'"
                size="small"
                type="text"
                danger
                @click="removeFile(index)"
                :disabled="uploading"
              >
                <template #icon>
                  <DeleteOutlined />
                </template>
              </a-button>
              <a-button
                v-if="file.status === 'error'"
                size="small"
                type="text"
                @click="retryUpload(index)"
                :disabled="uploading"
              >
                <template #icon>
                  <ReloadOutlined />
                </template>
                重试
              </a-button>
            </div>

            <!-- 错误信息 -->
            <div v-if="file.status === 'error' && file.error" class="error-message">
              <ExclamationCircleOutlined />
              <span>{{ file.error }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 上传统计 -->
      <div v-if="uploadStats.total > 0" class="upload-stats">
        <div class="stats-item">
          <span class="stats-label">总计：</span>
          <span class="stats-value">{{ uploadStats.total }}</span>
        </div>
        <div class="stats-item success">
          <CheckCircleOutlined />
          <span class="stats-label">成功：</span>
          <span class="stats-value">{{ uploadStats.success }}</span>
        </div>
        <div v-if="uploadStats.error > 0" class="stats-item error">
          <ExclamationCircleOutlined />
          <span class="stats-label">失败：</span>
          <span class="stats-value">{{ uploadStats.error }}</span>
        </div>
        <div v-if="uploadStats.uploading > 0" class="stats-item uploading">
          <LoadingOutlined />
          <span class="stats-label">上传中：</span>
          <span class="stats-value">{{ uploadStats.uploading }}</span>
        </div>
      </div>

      <!-- 底部操作区域 -->
      <div class="dialog-footer">
        <div class="footer-left">
          <a-space>
            <a-button @click="selectFiles" :disabled="uploading">
              <template #icon>
                <FolderOpenOutlined />
              </template>
              选择文件
            </a-button>
            <a-upload
              v-show="false"
              ref="hiddenUpload"
              :multiple="allowMultiple"
              :accept="acceptedFileTypes"
              :before-upload="beforeUpload"
              @change="handleFileSelect"
            >
            </a-upload>
          </a-space>
        </div>
        <div class="footer-right">
          <a-space>
            <a-button @click="handleCancel" :disabled="uploading">
              取消
            </a-button>
            <a-button
              type="primary"
              @click="startUpload"
              :loading="uploading"
              :disabled="fileList.length === 0 || allFilesUploaded"
            >
              <template #icon v-if="!uploading">
                <UploadOutlined />
              </template>
              {{ uploading ? '上传中...' : '开始上传' }}
            </a-button>
          </a-space>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  CloudUploadOutlined,
  InfoCircleOutlined,
  FileTextOutlined,
  FileExcelOutlined,
  FilePdfOutlined,
  FileOutlined,
  DeleteOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  LoadingOutlined,
  FolderOpenOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'
import type { UploadProps, UploadFile, UploadChangeParam } from 'ant-design-vue'

interface Props {
  visible?: boolean
  title?: string
  productId?: string
  documentType?: string
  maxFiles?: number
  maxFileSize?: number
  allowMultiple?: boolean
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'upload-success', files: any[]): void
  (e: 'upload-error', error: string): void
  (e: 'files-change', files: any[]): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  title: '文档上传',
  maxFiles: 10,
  maxFileSize: 50 * 1024 * 1024, // 50MB
  allowMultiple: true
})

const emit = defineEmits<Emits>()

// 响应式状态
const dialogVisible = ref(props.visible)
const uploading = ref(false)
const fileList = ref<UploadFile[]>([])
const hiddenUpload = ref()

// 支持的文件类型
const acceptedFileTypes = '.doc,.docx,.pdf,.xls,.xlsx'
const maxFileSizeMB = computed(() => Math.round(props.maxFileSize / (1024 * 1024)))

// 对话框标题
const dialogTitle = computed(() => {
  if (props.documentType) {
    const typeMap = {
      'TERMS': '条款文档上传',
      'FEASIBILITY_REPORT': '可行性报告上传',
      'ACTUARIAL_REPORT': '精算报告上传',
      'RATE_TABLE': '费率表上传',
      'REGISTRATION_FORM': '产品信息登记表上传'
    }
    return typeMap[props.documentType] || props.title
  }
  return props.title
})

// 上传统计
const uploadStats = computed(() => {
  const stats = {
    total: fileList.value.length,
    success: 0,
    error: 0,
    uploading: 0
  }

  fileList.value.forEach(file => {
    switch (file.status) {
      case 'done':
        stats.success++
        break
      case 'error':
        stats.error++
        break
      case 'uploading':
        stats.uploading++
        break
    }
  })

  return stats
})

// 是否所有文件都已上传完成
const allFilesUploaded = computed(() => {
  return fileList.value.length > 0 && fileList.value.every(file =>
    file.status === 'done' || file.status === 'error'
  )
})

// 监听visible变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

watch(dialogVisible, (newVal) => {
  if (!newVal) {
    emit('update:visible', false)
  }
})

// 获取支持格式文本
const getSupportedFormatsText = () => {
  return 'DOC, DOCX, PDF, XLS, XLSX'
}

// 文件类型检查
const isAllowedFileType = (file: UploadFile): boolean => {
  const allowedExtensions = ['.doc', '.docx', '.pdf', '.xls', '.xlsx']
  const fileName = file.name.toLowerCase()
  return allowedExtensions.some(ext => fileName.endsWith(ext))
}

const isDocumentFile = (file: UploadFile): boolean => {
  const fileName = file.name.toLowerCase()
  return fileName.endsWith('.doc') || fileName.endsWith('.docx')
}

const isExcelFile = (file: UploadFile): boolean => {
  const fileName = file.name.toLowerCase()
  return fileName.endsWith('.xls') || fileName.endsWith('.xlsx')
}

const isPdfFile = (file: UploadFile): boolean => {
  return file.name.toLowerCase().endsWith('.pdf')
}

// 格式化文件大小
const formatFileSize = (size: number): string => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

// 获取文件状态文本
const getFileStatusText = (status?: string): string => {
  switch (status) {
    case 'uploading':
      return '上传中'
    case 'done':
      return '上传成功'
    case 'error':
      return '上传失败'
    default:
      return '待上传'
  }
}

// 文件上传前验证
const beforeUpload = (file: UploadFile) => {
  // 文件大小验证
  if (file.size && file.size > props.maxFileSize) {
    message.error(`文件 ${file.name} 大小超过 ${maxFileSizeMB.value}MB 限制`)
    return false
  }

  // 文件类型验证
  if (!isAllowedFileType(file)) {
    message.error(`不支持的文件类型: ${file.name}`)
    return false
  }

  // 文件数量限制
  if (fileList.value.length >= props.maxFiles) {
    message.error(`最多只能上传 ${props.maxFiles} 个文件`)
    return false
  }

  // 阻止自动上传，我们手动控制
  return false
}

// 处理文件选择变化
const handleUploadChange: UploadProps['onChange'] = (info: UploadChangeParam) => {
  fileList.value = [...info.fileList]
  emitFilesChange()
}

// 处理文件选择（隐藏上传）
const handleFileSelect = (info: UploadChangeParam) => {
  // 将新选择的文件添加到当前列表
  const newFiles = info.fileList.filter(newFile =>
    !fileList.value.some(existingFile => existingFile.uid === newFile.uid)
  )

  fileList.value = [...fileList.value, ...newFiles]
  emitFilesChange()
}

// 处理拖拽上传
const handleDrop = (e: DragEvent) => {
  console.log('文件拖拽上传', e.dataTransfer?.files)
}

// 选择文件
const selectFiles = () => {
  nextTick(() => {
    const input = hiddenUpload.value?.$el?.querySelector('input')
    input?.click()
  })
}

// 开始上传
const startUpload = async () => {
  if (!props.productId) {
    message.error('请先选择产品')
    return
  }

  const filesToUpload = fileList.value.filter(file =>
    file.status !== 'done' && file.status !== 'uploading'
  )

  if (filesToUpload.length === 0) {
    message.warning('没有需要上传的文件')
    return
  }

  uploading.value = true

  try {
    // 逐个上传文件
    for (const file of filesToUpload) {
      if (file.originFileObj) {
        await uploadSingleFile(file)
      }
    }

    // 检查上传结果
    const successFiles = fileList.value.filter(file => file.status === 'done')
    const errorFiles = fileList.value.filter(file => file.status === 'error')

    if (successFiles.length > 0) {
      emit('upload-success', successFiles)
      message.success(`成功上传 ${successFiles.length} 个文件`)
    }

    if (errorFiles.length > 0) {
      message.error(`${errorFiles.length} 个文件上传失败`)
    }

  } catch (error: any) {
    console.error('上传过程出现异常', error)
    emit('upload-error', error.message || '上传失败')
    message.error('上传失败: ' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 上传单个文件
const uploadSingleFile = async (file: UploadFile): Promise<void> => {
  return new Promise((resolve, reject) => {
    // 设置上传状态
    file.status = 'uploading'
    file.percent = 0

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (file.percent !== undefined && file.percent < 90) {
        file.percent += Math.random() * 20
        if (file.percent > 90) file.percent = 90
      }
    }, 200)

    // TODO: 实际的上传逻辑
    // 这里应该调用API服务上传文件
    const formData = new FormData()
    formData.append('file', file.originFileObj as File)
    formData.append('productId', props.productId || '')
    formData.append('documentType', props.documentType || 'GENERAL')

    // 模拟上传请求
    setTimeout(() => {
      clearInterval(progressInterval)

      // 模拟上传成功/失败
      const success = Math.random() > 0.1 // 90% 成功率

      if (success) {
        file.status = 'done'
        file.percent = 100
        file.response = { id: Date.now(), url: '/mock/file/url' }
        resolve()
      } else {
        file.status = 'error'
        file.error = '网络错误，上传失败'
        reject(new Error('上传失败'))
      }
    }, 1000 + Math.random() * 2000)
  })
}

// 重试上传
const retryUpload = async (index: number) => {
  const file = fileList.value[index]
  if (file && file.originFileObj) {
    file.status = undefined
    file.percent = undefined
    file.error = undefined
    await uploadSingleFile(file)
  }
}

// 删除文件
const removeFile = (index: number) => {
  fileList.value.splice(index, 1)
  emitFilesChange()
  message.success('文件删除成功')
}

// 清空所有文件
const clearAllFiles = () => {
  fileList.value = []
  emitFilesChange()
  message.success('已清空所有文件')
}

// 触发文件变化事件
const emitFilesChange = () => {
  const files = fileList.value.map(file => ({
    ...file,
    documentType: props.documentType,
    productId: props.productId
  }))
  emit('files-change', files)
}

// 处理取消
const handleCancel = () => {
  if (uploading.value) {
    message.warning('正在上传中，无法关闭')
    return
  }

  // 清理状态
  fileList.value = []
  uploading.value = false

  emit('cancel')
  dialogVisible.value = false
}

// 暴露组件方法
defineExpose({
  clearFiles: clearAllFiles,
  getFileList: () => fileList.value,
  startUpload
})
</script>

<style scoped>
.document-upload-dialog :deep(.ant-modal-content) {
  padding: 0;
}

.document-upload-dialog :deep(.ant-modal-header) {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f6f8fa 0%, #e8f4fd 100%);
}

.document-upload-dialog :deep(.ant-modal-title) {
  color: #1890ff;
  font-weight: 600;
}

.upload-dialog-content {
  padding: 24px;
  max-height: 70vh;
  overflow-y: auto;
}

/* 上传提示区域 */
.upload-tips {
  margin-bottom: 24px;
  padding: 16px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #52c41a;
  font-weight: 500;
}

.tips-icon {
  font-size: 16px;
}

.tips-list {
  margin: 0;
  padding-left: 24px;
  color: #666;
  font-size: 14px;
}

.tips-list li {
  margin-bottom: 4px;
}

/* 主上传区域 */
.main-upload-area {
  margin-bottom: 24px;
}

.upload-dragger :deep(.ant-upload-drag) {
  background: #fafafa;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px 20px;
  transition: all 0.3s ease;
}

.upload-dragger :deep(.ant-upload-drag:hover) {
  border-color: #40a9ff;
  background: #f0f8ff;
}

.upload-dragger :deep(.ant-upload-drag.ant-upload-drag-hover) {
  border-color: #40a9ff;
  background: #f0f8ff;
}

.upload-content {
  text-align: center;
}

.upload-content :deep(.ant-upload-drag-icon) {
  font-size: 48px;
  color: #40a9ff;
  margin-bottom: 16px;
}

.upload-content :deep(.ant-upload-text) {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.upload-content :deep(.ant-upload-hint) {
  font-size: 14px;
  color: #999;
}

/* 文件列表区域 */
.file-list-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.file-count {
  font-weight: 500;
  color: #262626;
}

.file-list {
  max-height: 300px;
  overflow-y: auto;
}

.file-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.file-item.uploading {
  border-color: #40a9ff;
  background: #f0f8ff;
}

.file-item.error {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.file-icon-name {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.file-icon {
  color: #1890ff;
  font-size: 16px;
  flex-shrink: 0;
}

.file-name {
  font-weight: 500;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.file-size {
  color: #8c8c8c;
  font-size: 12px;
}

.file-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.file-status.uploading {
  background: #e6f7ff;
  color: #1890ff;
}

.file-status.done {
  background: #f6ffed;
  color: #52c41a;
}

.file-status.error {
  background: #fff2f0;
  color: #ff4d4f;
}

.upload-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.upload-progress :deep(.ant-progress) {
  flex: 1;
}

.progress-text {
  color: #666;
  font-size: 12px;
  width: 35px;
  text-align: right;
}

.file-actions {
  display: flex;
  gap: 4px;
  justify-content: flex-end;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

/* 上传统计 */
.upload-stats {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #f0f2f5;
  border-radius: 6px;
  margin-bottom: 24px;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.stats-item.success {
  color: #52c41a;
}

.stats-item.error {
  color: #ff4d4f;
}

.stats-item.uploading {
  color: #1890ff;
}

.stats-label {
  color: #666;
}

.stats-value {
  font-weight: 600;
}

/* 底部操作区域 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .upload-dialog-content {
    padding: 16px;
  }

  .upload-content :deep(.ant-upload-drag) {
    padding: 30px 16px;
  }

  .upload-stats {
    flex-wrap: wrap;
    gap: 16px;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 16px;
  }

  .footer-left,
  .footer-right {
    width: 100%;
  }

  .footer-right {
    display: flex;
    justify-content: flex-end;
  }
}
</style>