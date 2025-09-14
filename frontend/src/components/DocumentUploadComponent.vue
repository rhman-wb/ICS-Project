<template>
  <div class="document-upload-component">
    <!-- 文档上传区域 -->
    <a-card title="要件文档上传" class="upload-card">
      <div class="upload-container">
        <!-- 四种要件类型的上传区域 -->
        <div
          v-for="documentType in documentTypes"
          :key="documentType.key"
          class="document-type-section"
        >
          <div class="section-header">
            <div class="section-title">
              <span class="title-icon" :class="documentType.iconClass">{{ documentType.icon }}</span>
              <span class="title-text">{{ documentType.name }}</span>
              <a-tag v-if="documentType.required" color="red" size="small">必需</a-tag>
              <a-tag v-else color="blue" size="small">可选</a-tag>
            </div>
            <div class="section-desc">{{ documentType.description }}</div>
          </div>

          <!-- 文件路径配置 -->
          <div class="path-config">
            <a-input-group compact>
              <a-input
                v-model:value="documentType.customPath"
                :placeholder="`自定义${documentType.name}路径（可选）`"
                class="path-input"
                allow-clear
              />
              <a-button @click="selectFolder(documentType)" class="folder-btn">
                <template #icon>
                  <FolderOutlined />
                </template>
              </a-button>
            </a-input-group>
          </div>

          <!-- 文件上传区域 -->
          <div class="upload-area">
            <a-upload-dragger
              :name="documentType.key"
              :multiple="documentType.allowMultiple"
              :accept="acceptedFileTypes"
              :file-list="documentType.fileList"
              :before-upload="(file) => beforeUpload(file, documentType)"
              @change="(info) => handleUploadChange(info, documentType)"
              @drop="(e) => handleDrop(e, documentType)"
              class="upload-dragger"
            >
              <div class="upload-content">
                <p class="ant-upload-drag-icon">
                  <InboxOutlined />
                </p>
                <p class="ant-upload-text">
                  点击或拖拽{{ documentType.name }}到此区域上传
                </p>
                <p class="ant-upload-hint">
                  支持 {{ getSupportedFormatsText() }}，单个文件不超过 {{ maxFileSizeMB }}MB
                </p>
              </div>
            </a-upload-dragger>
          </div>

          <!-- 已上传文件列表 -->
          <div v-if="documentType.fileList.length > 0" class="uploaded-files">
            <div class="files-header">
              <span>已上传文件 ({{ documentType.fileList.length }})</span>
              <a-button
                v-if="documentType.fileList.length > 1"
                size="small"
                type="link"
                @click="clearFiles(documentType)"
              >
                清空全部
              </a-button>
            </div>
            <div class="files-list">
              <div
                v-for="(file, index) in documentType.fileList"
                :key="file.uid || index"
                class="file-item"
              >
                <div class="file-info">
                  <span class="file-icon">
                    <FileTextOutlined v-if="isDocumentFile(file)" />
                    <FileExcelOutlined v-else-if="isExcelFile(file)" />
                    <FilePdfOutlined v-else-if="isPdfFile(file)" />
                    <FileOutlined v-else />
                  </span>
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                </div>
                <div class="file-actions">
                  <a-button
                    size="small"
                    type="text"
                    @click="previewFile(file)"
                    v-if="canPreview(file)"
                  >
                    预览
                  </a-button>
                  <a-button
                    size="small"
                    type="text"
                    danger
                    @click="removeFile(documentType, index)"
                  >
                    删除
                  </a-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 验证状态 -->
          <div v-if="documentType.validationMessage" class="validation-message">
            <a-alert
              :message="documentType.validationMessage"
              :type="documentType.validationStatus"
              show-icon
              closable
              @close="clearValidation(documentType)"
            />
          </div>
        </div>

        <!-- 全局操作按钮 -->
        <div class="global-actions">
          <a-space size="middle">
            <a-button @click="validateAllDocuments" :loading="validating">
              <template #icon>
                <CheckCircleOutlined />
              </template>
              验证所有文档
            </a-button>
            <a-button type="primary" @click="uploadAllDocuments" :loading="uploading">
              <template #icon>
                <UploadOutlined />
              </template>
              上传所有文档
            </a-button>
            <a-button @click="resetAll" :disabled="uploading">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置所有
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- 文件预览模态框 -->
    <a-modal
      v-model:open="previewVisible"
      title="文件预览"
      width="80%"
      :footer="null"
      centered
    >
      <div class="file-preview">
        <iframe
          v-if="previewFile && isPdfFile(previewFile)"
          :src="previewUrl"
          width="100%"
          height="500px"
          frameborder="0"
        />
        <div v-else class="preview-unavailable">
          <FileOutlined class="preview-icon" />
          <p>该文件类型暂不支持预览</p>
          <p>文件名：{{ previewFile?.name }}</p>
          <p>文件大小：{{ previewFile ? formatFileSize(previewFile.size) : '' }}</p>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  InboxOutlined,
  FolderOutlined,
  FileTextOutlined,
  FileExcelOutlined,
  FilePdfOutlined,
  FileOutlined,
  CheckCircleOutlined,
  UploadOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import type { UploadProps, UploadFile } from 'ant-design-vue'

interface DocumentType {
  key: string
  name: string
  description: string
  icon: string
  iconClass: string
  required: boolean
  allowMultiple: boolean
  customPath: string
  fileList: UploadFile[]
  validationMessage?: string
  validationStatus?: 'success' | 'info' | 'warning' | 'error'
}

interface Props {
  productId?: string
  maxFiles?: number
  maxFileSize?: number
  disabled?: boolean
}

interface Emits {
  (e: 'upload-success', files: any[]): void
  (e: 'upload-error', error: string): void
  (e: 'files-change', files: any[]): void
  (e: 'validate', result: any): void
}

const props = withDefaults(defineProps<Props>(), {
  maxFiles: 10,
  maxFileSize: 50 * 1024 * 1024, // 50MB
  disabled: false
})

const emit = defineEmits<Emits>()

// 响应式状态
const uploading = ref(false)
const validating = ref(false)
const previewVisible = ref(false)
const previewFile = ref<UploadFile | null>(null)
const previewUrl = ref('')

// 支持的文件类型
const acceptedFileTypes = '.doc,.docx,.pdf,.xls,.xlsx'
const maxFileSizeMB = computed(() => Math.round(props.maxFileSize / (1024 * 1024)))

// 四种要件类型配置
const documentTypes = reactive<DocumentType[]>([
  {
    key: 'TERMS',
    name: '条款',
    description: '保险条款文档，通常为Word或PDF格式',
    icon: '📄',
    iconClass: 'terms-icon',
    required: true,
    allowMultiple: false,
    customPath: '',
    fileList: []
  },
  {
    key: 'FEASIBILITY_REPORT',
    name: '可行性报告',
    description: '产品可行性分析报告',
    icon: '📊',
    iconClass: 'report-icon',
    required: true,
    allowMultiple: false,
    customPath: '',
    fileList: []
  },
  {
    key: 'ACTUARIAL_REPORT',
    name: '精算报告',
    description: '精算分析和计算报告',
    icon: '🧮',
    iconClass: 'actuarial-icon',
    required: true,
    allowMultiple: false,
    customPath: '',
    fileList: []
  },
  {
    key: 'RATE_TABLE',
    name: '费率表',
    description: '费率计算表格，通常为Excel格式',
    icon: '📈',
    iconClass: 'rate-icon',
    required: false,
    allowMultiple: true,
    customPath: '',
    fileList: []
  }
])

// 获取支持格式文本
const getSupportedFormatsText = () => {
  return 'DOC, DOCX, PDF, XLS, XLSX'
}

// 文件上传前验证
const beforeUpload = (file: UploadFile, docType: DocumentType) => {
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

  // 单文件类型数量限制
  if (!docType.allowMultiple && docType.fileList.length >= 1) {
    message.warning(`${docType.name} 只能上传一个文件，请先删除现有文件`)
    return false
  }

  return false // 阻止自动上传，我们手动处理
}

// 处理上传变化
const handleUploadChange: UploadProps['onChange'] = (info, docType: DocumentType) => {
  const { file, fileList } = info

  if (file.status === 'uploading') {
    // 更新文件列表
    docType.fileList = [...fileList]
  }

  // 触发文件变化事件
  emitFilesChange()
}

// 处理拖拽上传
const handleDrop = (e: DragEvent, docType: DocumentType) => {
  console.log('文件拖拽到', docType.name, e.dataTransfer?.files)
}

// 选择文件夹
const selectFolder = (docType: DocumentType) => {
  // 在实际实现中，可能需要使用electron的dialog API
  message.info('文件夹选择功能需要在Electron环境中实现')
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

const canPreview = (file: UploadFile): boolean => {
  return isPdfFile(file)
}

// 格式化文件大小
const formatFileSize = (size: number): string => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

// 预览文件
const previewFile = (file: UploadFile) => {
  if (isPdfFile(file) && file.url) {
    previewFile.value = file
    previewUrl.value = file.url
    previewVisible.value = true
  } else {
    message.warning('该文件类型暂不支持预览')
  }
}

// 删除文件
const removeFile = (docType: DocumentType, index: number) => {
  docType.fileList.splice(index, 1)
  clearValidation(docType)
  emitFilesChange()
  message.success('文件删除成功')
}

// 清空文件
const clearFiles = (docType: DocumentType) => {
  docType.fileList = []
  clearValidation(docType)
  emitFilesChange()
  message.success(`${docType.name} 文件已清空`)
}

// 清除验证信息
const clearValidation = (docType: DocumentType) => {
  docType.validationMessage = undefined
  docType.validationStatus = undefined
}

// 验证所有文档
const validateAllDocuments = async () => {
  validating.value = true

  try {
    let hasErrors = false

    // 验证必需文档
    for (const docType of documentTypes) {
      if (docType.required && docType.fileList.length === 0) {
        docType.validationMessage = `${docType.name} 是必需的，请上传相关文档`
        docType.validationStatus = 'error'
        hasErrors = true
      } else if (docType.fileList.length > 0) {
        docType.validationMessage = `${docType.name} 验证通过`
        docType.validationStatus = 'success'
      }
    }

    const result = {
      success: !hasErrors,
      message: hasErrors ? '文档验证失败，请检查必需文档' : '所有文档验证通过',
      details: documentTypes.map(dt => ({
        type: dt.key,
        name: dt.name,
        fileCount: dt.fileList.length,
        status: dt.validationStatus
      }))
    }

    emit('validate', result)

    if (hasErrors) {
      message.error('文档验证失败，请检查必需文档')
    } else {
      message.success('所有文档验证通过')
    }

  } finally {
    validating.value = false
  }
}

// 上传所有文档
const uploadAllDocuments = async () => {
  if (!props.productId) {
    message.error('请先选择产品')
    return
  }

  uploading.value = true

  try {
    // 收集所有文件
    const allFiles = documentTypes.flatMap(dt =>
      dt.fileList.map(file => ({
        file: file.originFileObj,
        documentType: dt.key,
        customPath: dt.customPath
      }))
    )

    if (allFiles.length === 0) {
      message.warning('没有文件需要上传')
      return
    }

    // TODO: 实际的上传逻辑
    // 这里应该调用API服务
    console.log('上传文件:', allFiles)

    // 模拟上传延迟
    await new Promise(resolve => setTimeout(resolve, 2000))

    emit('upload-success', allFiles)
    message.success(`成功上传 ${allFiles.length} 个文件`)

  } catch (error: any) {
    const errorMsg = error.message || '上传失败'
    emit('upload-error', errorMsg)
    message.error(errorMsg)
  } finally {
    uploading.value = false
  }
}

// 重置所有
const resetAll = () => {
  documentTypes.forEach(docType => {
    docType.fileList = []
    docType.customPath = ''
    clearValidation(docType)
  })
  emitFilesChange()
  message.info('已重置所有文档')
}

// 触发文件变化事件
const emitFilesChange = () => {
  const allFiles = documentTypes.flatMap(dt =>
    dt.fileList.map(file => ({
      ...file,
      documentType: dt.key,
      customPath: dt.customPath
    }))
  )
  emit('files-change', allFiles)
}

// 暴露组件方法
defineExpose({
  validateAllDocuments,
  uploadAllDocuments,
  resetAll,
  getFileCount: () => documentTypes.reduce((total, dt) => total + dt.fileList.length, 0),
  getDocumentTypes: () => documentTypes
})
</script>

<style scoped>
.document-upload-component {
  max-width: 1200px;
  margin: 0 auto;
}

.upload-card :deep(.ant-card-head) {
  background: linear-gradient(135deg, #f6f8fa 0%, #e8f4fd 100%);
  border-bottom: 2px solid #1890ff;
}

.upload-card :deep(.ant-card-head-title) {
  color: #1890ff;
  font-weight: 600;
  font-size: 18px;
}

.upload-container {
  padding: 16px 0;
}

.document-type-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.title-icon {
  font-size: 20px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.section-desc {
  color: #8c8c8c;
  font-size: 13px;
  margin-left: 28px;
}

.path-config {
  margin-bottom: 16px;
}

.path-input {
  flex: 1;
}

.folder-btn {
  width: 40px;
}

.upload-area {
  margin-bottom: 16px;
}

.upload-dragger :deep(.ant-upload-drag) {
  background: #ffffff;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.upload-dragger :deep(.ant-upload-drag:hover) {
  border-color: #40a9ff;
  background: #f0f8ff;
}

.upload-content {
  padding: 20px 12px;
}

.uploaded-files {
  background: white;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e8e8e8;
}

.files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
  color: #262626;
}

.files-list {
  space: 8px 0;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  margin-bottom: 8px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.file-icon {
  color: #1890ff;
  font-size: 16px;
}

.file-name {
  flex: 1;
  font-weight: 500;
  color: #262626;
}

.file-size {
  color: #8c8c8c;
  font-size: 12px;
  margin-left: auto;
  margin-right: 12px;
}

.file-actions {
  display: flex;
  gap: 4px;
}

.validation-message {
  margin-top: 12px;
}

.global-actions {
  margin-top: 32px;
  text-align: center;
  padding: 24px;
  background: #f0f2f5;
  border-radius: 8px;
}

.file-preview {
  min-height: 400px;
}

.preview-unavailable {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #8c8c8c;
}

.preview-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .document-type-section {
    padding: 16px;
    margin-bottom: 24px;
  }

  .upload-content {
    padding: 16px 8px;
  }

  .file-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .file-actions {
    align-self: flex-end;
  }

  .global-actions {
    padding: 16px;
  }
}
</style>