<template>
  <div class="document-upload-with-validation">
    <a-card title="要件文档上传" class="upload-card">
      <template #extra>
        <a-button
          type="primary"
          size="small"
          :loading="validating"
          @click="validateDocuments"
          :icon="h(CheckCircleOutlined)"
        >
          校验文档
        </a-button>
      </template>

      <!-- 上传区域 -->
      <div class="upload-sections">
        <div
          v-for="docType in documentTypes"
          :key="docType.type"
          class="upload-section"
          :class="{
            'has-error': hasError(docType.type),
            'has-warning': hasWarning(docType.type),
            'is-valid': isValid(docType.type)
          }"
        >
          <div class="section-header">
            <h4 class="section-title">
              {{ docType.name }}
              <span class="required-mark" v-if="docType.required">*</span>
            </h4>

            <!-- 状态指示器 -->
            <div class="status-indicator">
              <a-badge
                v-if="hasError(docType.type)"
                status="error"
                :text="`${getErrorCount(docType.type)} 个错误`"
              />
              <a-badge
                v-else-if="hasWarning(docType.type)"
                status="warning"
                :text="`${getWarningCount(docType.type)} 个警告`"
              />
              <a-badge
                v-else-if="isValid(docType.type)"
                status="success"
                text="已通过校验"
              />
              <a-badge
                v-else-if="hasDocument(docType.type)"
                status="processing"
                text="待校验"
              />
              <a-badge
                v-else
                status="default"
                text="未上传"
              />
            </div>
          </div>

          <!-- 文件上传组件 -->
          <a-upload-dragger
            :name="docType.type"
            :multiple="false"
            :action="`/api/v1/documents/upload`"
            :data="{
              documentType: docType.type,
              productId: productId
            }"
            :headers="{
              Authorization: `Bearer ${getToken()}`
            }"
            :beforeUpload="beforeUpload"
            @change="(info) => handleUploadChange(info, docType.type)"
            class="upload-area"
          >
            <div class="upload-content">
              <p class="ant-upload-drag-icon">
                <inbox-outlined v-if="!hasDocument(docType.type)" />
                <file-text-outlined v-else style="color: #1890ff;" />
              </p>
              <p class="ant-upload-text">
                {{ hasDocument(docType.type) ? '替换文档' : '点击或拖拽上传文档' }}
              </p>
              <p class="ant-upload-hint">
                支持 {{ docType.supportedFormats.join(', ') }} 格式，大小不超过 {{ docType.maxSize }}
              </p>
            </div>
          </a-upload-dragger>

          <!-- 已上传文件信息 -->
          <div class="uploaded-file-info" v-if="uploadedFiles[docType.type]">
            <div class="file-item">
              <div class="file-info">
                <file-text-outlined style="color: #1890ff; margin-right: 8px;" />
                <span class="file-name">{{ uploadedFiles[docType.type].fileName }}</span>
                <span class="file-size">({{ formatFileSize(uploadedFiles[docType.type].fileSize) }})</span>
              </div>
              <div class="file-actions">
                <a-button
                  type="link"
                  size="small"
                  @click="removeFile(docType.type)"
                  danger
                >
                  删除
                </a-button>
              </div>
            </div>
          </div>

          <!-- 校验错误信息 -->
          <div class="validation-messages" v-if="getValidationMessages(docType.type).length > 0">
            <div
              v-for="message in getValidationMessages(docType.type)"
              :key="`${message.type}-${message.code}`"
              class="validation-message"
              :class="`validation-${message.type}`"
            >
              <div class="message-content">
                <exclamation-circle-outlined v-if="message.type === 'error'" />
                <warning-outlined v-else />
                <span class="message-text">{{ message.text }}</span>
              </div>
              <div class="message-suggestion" v-if="message.suggestion">
                <span class="suggestion-label">建议：</span>
                <span class="suggestion-text">{{ message.suggestion }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 整体校验结果摘要 -->
      <div class="validation-summary" v-if="validationResult">
        <a-divider />
        <div class="summary-content">
          <div class="summary-stats">
            <a-row :gutter="16">
              <a-col :span="6">
                <a-statistic
                  title="文档完整性"
                  :value="validationResult.summary.completenessPercentage"
                  suffix="%"
                  :value-style="getCompletenessStyle()"
                />
              </a-col>
              <a-col :span="6">
                <a-statistic
                  title="已上传"
                  :value="validationResult.summary.uploadedDocuments"
                  :suffix="`/ ${validationResult.summary.requiredDocuments}`"
                  :value-style="{ color: '#1890ff' }"
                />
              </a-col>
              <a-col :span="6">
                <a-statistic
                  title="错误"
                  :value="validationResult.summary.totalErrors"
                  :value-style="{ color: validationResult.summary.totalErrors > 0 ? '#ff4d4f' : '#52c41a' }"
                />
              </a-col>
              <a-col :span="6">
                <a-statistic
                  title="警告"
                  :value="validationResult.summary.totalWarnings"
                  :value-style="{ color: validationResult.summary.totalWarnings > 0 ? '#faad14' : '#52c41a' }"
                />
              </a-col>
            </a-row>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import { message, type UploadChangeParam } from 'ant-design-vue'
import {
  InboxOutlined,
  FileTextOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import DocumentValidationService, {
  type DocumentValidationResult,
  type ValidationError,
  type ValidationWarning
} from '@/api/documentValidation'
import { getToken } from '@/utils/auth'

interface Props {
  productId: string
  autoValidate?: boolean
}

interface DocumentType {
  type: string
  name: string
  required: boolean
  supportedFormats: string[]
  maxSize: string
}

interface UploadedFile {
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
}

interface ValidationMessage {
  type: 'error' | 'warning'
  code: string
  text: string
  suggestion?: string
}

const props = withDefaults(defineProps<Props>(), {
  autoValidate: true
})

const emit = defineEmits<{
  uploadComplete: [file: UploadedFile, documentType: string]
  uploadError: [error: any, documentType: string]
  validationComplete: [result: DocumentValidationResult]
}>()

// 响应式数据
const validating = ref(false)
const validationResult = ref<DocumentValidationResult | null>(null)
const uploadedFiles = ref<Record<string, UploadedFile>>({})

// 文档类型定义
const documentTypes: DocumentType[] = [
  {
    type: 'REGISTRATION',
    name: '产品信息登记表',
    required: true,
    supportedFormats: ['XLS', 'XLSX'],
    maxSize: '20MB'
  },
  {
    type: 'TERMS',
    name: '条款',
    required: true,
    supportedFormats: ['DOC', 'DOCX', 'PDF'],
    maxSize: '20MB'
  },
  {
    type: 'FEASIBILITY_REPORT',
    name: '可行性报告',
    required: true,
    supportedFormats: ['DOC', 'DOCX', 'PDF'],
    maxSize: '20MB'
  },
  {
    type: 'ACTUARIAL_REPORT',
    name: '精算报告',
    required: true,
    supportedFormats: ['DOC', 'DOCX', 'PDF', 'XLS', 'XLSX'],
    maxSize: '20MB'
  },
  {
    type: 'RATE_TABLE',
    name: '费率表',
    required: true,
    supportedFormats: ['XLS', 'XLSX'],
    maxSize: '20MB'
  }
]

// 计算属性
const validationMessages = computed(() => {
  if (!validationResult.value) return {}

  const messages: Record<string, ValidationMessage[]> = {}

  // 处理错误信息
  validationResult.value.errors.forEach(error => {
    const docType = error.documentType
    if (!messages[docType]) messages[docType] = []

    messages[docType].push({
      type: 'error',
      code: error.errorCode,
      text: error.message,
      suggestion: error.suggestion
    })
  })

  // 处理警告信息
  validationResult.value.warnings.forEach(warning => {
    const docType = warning.documentType
    if (!messages[docType]) messages[docType] = []

    messages[docType].push({
      type: 'warning',
      code: warning.warningCode,
      text: warning.message,
      suggestion: warning.recommendation
    })
  })

  return messages
})

// 方法
const beforeUpload = (file: File) => {
  // 文件大小检查
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) {
    message.error('文件大小不能超过 20MB!')
    return false
  }

  return true
}

const handleUploadChange = (info: UploadChangeParam, documentType: string) => {
  const { status, response } = info.file

  if (status === 'done') {
    if (response && response.success) {
      // 上传成功
      const fileData: UploadedFile = {
        fileName: response.data.fileName,
        filePath: response.data.filePath,
        fileSize: response.data.fileSize,
        fileType: response.data.fileType
      }

      uploadedFiles.value[documentType] = fileData
      emit('uploadComplete', fileData, documentType)

      message.success(`${getDocumentTypeName(documentType)}上传成功`)

      // 自动校验
      if (props.autoValidate) {
        setTimeout(() => validateDocuments(), 1000)
      }
    } else {
      message.error(response?.message || `${getDocumentTypeName(documentType)}上传失败`)
      emit('uploadError', new Error(response?.message), documentType)
    }
  } else if (status === 'error') {
    message.error(`${getDocumentTypeName(documentType)}上传失败`)
    emit('uploadError', new Error('Upload failed'), documentType)
  }
}

const removeFile = (documentType: string) => {
  delete uploadedFiles.value[documentType]

  // 重新校验
  if (props.autoValidate) {
    setTimeout(() => validateDocuments(), 500)
  }
}

const validateDocuments = async () => {
  if (!props.productId) {
    message.error('产品ID不能为空')
    return
  }

  validating.value = true

  try {
    const response = await DocumentValidationService.validateProductDocuments(props.productId)

    if (response.success) {
      validationResult.value = response.data
      emit('validationComplete', response.data)

      if (response.data.isValid) {
        message.success('文档校验通过')
      } else {
        message.warning(`发现 ${response.data.errors.length} 个错误`)
      }
    } else {
      message.error(response.message || '文档校验失败')
    }
  } catch (error: any) {
    console.error('文档校验异常:', error)
    message.error(error.message || '文档校验异常')
  } finally {
    validating.value = false
  }
}

const hasDocument = (documentType: string): boolean => {
  return !!uploadedFiles.value[documentType]
}

const hasError = (documentType: string): boolean => {
  return (validationMessages.value[documentType] || [])
    .some(msg => msg.type === 'error')
}

const hasWarning = (documentType: string): boolean => {
  return (validationMessages.value[documentType] || [])
    .some(msg => msg.type === 'warning')
}

const isValid = (documentType: string): boolean => {
  return hasDocument(documentType) && !hasError(documentType)
}

const getErrorCount = (documentType: string): number => {
  return (validationMessages.value[documentType] || [])
    .filter(msg => msg.type === 'error').length
}

const getWarningCount = (documentType: string): number => {
  return (validationMessages.value[documentType] || [])
    .filter(msg => msg.type === 'warning').length
}

const getValidationMessages = (documentType: string): ValidationMessage[] => {
  return validationMessages.value[documentType] || []
}

const getDocumentTypeName = (documentType: string): string => {
  const docType = documentTypes.find(dt => dt.type === documentType)
  return docType?.name || documentType
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes'

  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getCompletenessStyle = () => {
  if (!validationResult.value) return { color: '#666' }

  const percentage = validationResult.value.summary.completenessPercentage
  if (percentage >= 100) return { color: '#52c41a' }
  if (percentage >= 80) return { color: '#faad14' }
  return { color: '#ff4d4f' }
}

// 监听产品ID变化
watch(() => props.productId, (newProductId) => {
  if (newProductId && props.autoValidate) {
    validateDocuments()
  }
}, { immediate: true })

// 暴露方法给父组件
defineExpose({
  validateDocuments,
  uploadedFiles,
  validationResult
})
</script>

<style scoped lang="scss">
.document-upload-with-validation {
  .upload-card {
    border-radius: 8px;
  }

  .upload-sections {
    .upload-section {
      margin-bottom: 24px;
      padding: 16px;
      border-radius: 8px;
      border: 1px solid #e8e8e8;
      transition: all 0.3s;

      &.has-error {
        border-color: #ff4d4f;
        background-color: #fff2f0;
      }

      &.has-warning {
        border-color: #faad14;
        background-color: #fffbe6;
      }

      &.is-valid {
        border-color: #52c41a;
        background-color: #f6ffed;
      }

      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .section-title {
          margin: 0;
          font-size: 16px;
          font-weight: 500;

          .required-mark {
            color: #ff4d4f;
            margin-left: 4px;
          }
        }

        .status-indicator {
          .ant-badge {
            font-size: 12px;
          }
        }
      }

      .upload-area {
        margin-bottom: 12px;

        :deep(.ant-upload-drag) {
          border-radius: 6px;
          border: 1px dashed #d9d9d9;
          background-color: #fafafa;

          &:hover {
            border-color: #1890ff;
          }
        }

        .upload-content {
          padding: 20px 0;

          .ant-upload-drag-icon {
            font-size: 48px;
            margin-bottom: 16px;

            .anticon {
              color: #1890ff;
            }
          }

          .ant-upload-text {
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
          }

          .ant-upload-hint {
            font-size: 12px;
            color: #999;
          }
        }
      }

      .uploaded-file-info {
        margin-bottom: 12px;
        padding: 8px;
        background-color: #f0f0f0;
        border-radius: 4px;

        .file-item {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .file-info {
            display: flex;
            align-items: center;

            .file-name {
              font-weight: 500;
              margin-right: 8px;
            }

            .file-size {
              color: #999;
              font-size: 12px;
            }
          }
        }
      }

      .validation-messages {
        .validation-message {
          padding: 8px 12px;
          border-radius: 4px;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          &.validation-error {
            background-color: #fff2f0;
            border: 1px solid #ffccc7;

            .message-content {
              color: #ff4d4f;
            }
          }

          &.validation-warning {
            background-color: #fffbe6;
            border: 1px solid #ffe7ba;

            .message-content {
              color: #faad14;
            }
          }

          .message-content {
            display: flex;
            align-items: flex-start;
            margin-bottom: 4px;

            .anticon {
              margin-right: 8px;
              margin-top: 2px;
            }

            .message-text {
              font-weight: 500;
            }
          }

          .message-suggestion {
            font-size: 12px;
            color: #666;
            margin-left: 24px;

            .suggestion-label {
              font-weight: 500;
              margin-right: 4px;
            }
          }
        }
      }
    }
  }

  .validation-summary {
    .summary-content {
      .summary-stats {
        .ant-statistic {
          text-align: center;
        }
      }
    }
  }
}
</style>