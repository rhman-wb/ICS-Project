<template>
  <div class="template-parser">
    <a-card title="模板文件解析" class="parser-card">
      <a-alert
        message="上传填写好的模板文件"
        description="系统将自动解析模板内容并填充到表单中，请确保模板格式正确且数据完整。"
        type="info"
        show-icon
        class="parser-alert"
      />

      <!-- File Upload Area -->
      <div class="upload-area">
        <a-upload-dragger
          v-model:file-list="fileList"
          :before-upload="handleBeforeUpload"
          :custom-request="handleCustomRequest"
          :max-count="1"
          accept=".xlsx,.xls"
          @change="handleFileChange"
        >
          <p class="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p class="ant-upload-hint">
            支持 .xlsx 和 .xls 格式，文件大小不超过10MB
          </p>
        </a-upload-dragger>
      </div>

      <!-- Parsing Progress -->
      <div v-if="parsing" class="parsing-progress">
        <a-spin>
          <template #indicator>
            <LoadingOutlined style="font-size: 24px" spin />
          </template>
        </a-spin>
        <p class="progress-text">正在解析模板文件...</p>
      </div>

      <!-- Parse Result -->
      <div v-if="parseResult && !parsing" class="parse-result">
        <!-- Success Result -->
        <div v-if="parseResult.success" class="result-success">
          <a-result
            status="success"
            title="解析成功"
            :sub-title="`成功解析 ${parseResult.metadata?.fileName}，共处理 ${parseResult.metadata?.rowsProcessed} 行数据`"
          >
            <template #extra>
              <a-space>
                <a-button type="primary" @click="handleAutoFill">
                  <template #icon>
                    <CheckOutlined />
                  </template>
                  自动填充表单
                </a-button>
                <a-button @click="handleReset">
                  <template #icon>
                    <ReloadOutlined />
                  </template>
                  重新上传
                </a-button>
              </a-space>
            </template>
          </a-result>

          <!-- Parse Metadata -->
          <div class="parse-metadata">
            <a-descriptions :column="2" bordered size="small">
              <a-descriptions-item label="文件名">
                {{ parseResult.metadata?.fileName }}
              </a-descriptions-item>
              <a-descriptions-item label="文件大小">
                {{ formatFileSize(parseResult.metadata?.fileSize || 0) }}
              </a-descriptions-item>
              <a-descriptions-item label="解析时间">
                {{ formatParseTime(parseResult.metadata?.parseTime || 0) }}
              </a-descriptions-item>
              <a-descriptions-item label="处理行数">
                {{ parseResult.metadata?.rowsProcessed }}
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <!-- Warnings -->
          <div v-if="parseResult.warnings && parseResult.warnings.length > 0" class="parse-warnings">
            <h4>
              <WarningOutlined /> 注意事项 ({{ parseResult.warnings.length }})
            </h4>
            <a-list
              size="small"
              :data-source="parseResult.warnings"
              :pagination="false"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta>
                    <template #title>
                      <span class="warning-field">{{ item.field }}</span>
                    </template>
                    <template #description>
                      <div class="warning-content">
                        <div>{{ item.message }}</div>
                        <div v-if="item.suggestion" class="warning-suggestion">
                          建议：{{ item.suggestion }}
                        </div>
                      </div>
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </template>
            </a-list>
          </div>
        </div>

        <!-- Error Result -->
        <div v-else class="result-error">
          <a-result
            status="error"
            title="解析失败"
            :sub-title="`解析 ${parseResult.metadata?.fileName} 时发生错误`"
          >
            <template #extra>
              <a-button type="primary" @click="handleReset">
                <template #icon>
                  <ReloadOutlined />
                  </template>
                重新上传
              </a-button>
            </template>
          </a-result>

          <!-- Errors List -->
          <div class="parse-errors">
            <h4>
              <CloseCircleOutlined /> 错误列表 ({{ parseResult.errors?.length || 0 }})
            </h4>
            <a-list
              size="small"
              :data-source="parseResult.errors"
              :pagination="false"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-alert
                    :message="`字段：${item.field}`"
                    :description="item.message"
                    type="error"
                    show-icon
                    closable
                  />
                </a-list-item>
              </template>
            </a-list>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadProps } from 'ant-design-vue'
import {
  InboxOutlined,
  LoadingOutlined,
  CheckOutlined,
  ReloadOutlined,
  WarningOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import {
  parseTemplateFile,
  autoFillForm,
  formatFileSize,
  formatParseTime
} from '@/utils/templateParsing'
import type {
  TemplateType,
  TemplateParseResult,
  ProductFormData
} from '@/types/product/template'

// Component props
interface Props {
  /** Template type */
  templateType: TemplateType
  /** Current form data */
  formData?: Partial<ProductFormData>
}

const props = defineProps<Props>()

// Component emits
const emit = defineEmits<{
  (e: 'parse-success', result: TemplateParseResult): void
  (e: 'parse-error', error: { message: string; errors?: any[] }): void
  (e: 'auto-fill', data: Partial<ProductFormData>): void
}>()

// Component state
const fileList = ref<any[]>([])
const parsing = ref(false)
const parseResult = ref<TemplateParseResult | null>(null)

// File upload handlers
const handleBeforeUpload = (file: File) => {
  // Validate file type
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    message.error('只支持上传 .xlsx 或 .xls 格式的文件')
    return false
  }

  // Validate file size (max 10MB)
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB')
    return false
  }

  return false // Prevent automatic upload
}

const handleCustomRequest = async ({ file }: any) => {
  parsing.value = true
  parseResult.value = null

  try {
    // Parse template file
    const result = await parseTemplateFile(file, props.templateType)
    parseResult.value = result

    if (result.success) {
      // Emit parse-success event
      emit('parse-success', result)
      message.success('模板解析成功')
    } else {
      // Emit parse-error event
      emit('parse-error', {
        message: '模板解析失败，请检查错误信息',
        errors: result.errors
      })
      message.error('模板解析失败，请检查错误信息')
    }
  } catch (error: any) {
    console.error('Parse error:', error)
    const errorMessage = `解析失败: ${error.message || '未知错误'}`

    // Emit parse-error event for exceptions
    emit('parse-error', {
      message: errorMessage,
      errors: [{ field: 'file', message: error.message || '未知错误' }]
    })
    message.error(errorMessage)
  } finally {
    parsing.value = false
  }
}

const handleFileChange: UploadProps['onChange'] = (info) => {
  const status = info.file.status
  if (status === 'done') {
    message.success(`${info.file.name} 文件上传成功`)
  } else if (status === 'error') {
    message.error(`${info.file.name} 文件上传失败`)
  }
}

// Auto-fill handler
const handleAutoFill = () => {
  if (!parseResult.value || !parseResult.value.success || !parseResult.value.data) {
    message.error('没有可用的解析数据')
    return
  }

  const filledData = autoFillForm(props.formData || {}, parseResult.value.data)
  emit('auto-fill', filledData)
  message.success('表单已自动填充')
}

// Reset handler
const handleReset = () => {
  fileList.value = []
  parseResult.value = null
  parsing.value = false
}

// Expose public methods
defineExpose({
  reset: handleReset,
  parseResult
})
</script>

<style scoped lang="scss">
.template-parser {
  .parser-card {
    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .parser-alert {
    margin-bottom: 24px;
  }

  .upload-area {
    margin-bottom: 24px;

    :deep(.ant-upload-drag) {
      background-color: #fafafa;
      border: 2px dashed #d9d9d9;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        border-color: #1890ff;
        background-color: #f0f8ff;
      }

      .ant-upload-drag-icon {
        margin-bottom: 16px;
        font-size: 48px;
        color: #1890ff;
      }

      .ant-upload-text {
        margin-bottom: 8px;
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }

      .ant-upload-hint {
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }

  .parsing-progress {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 48px 0;

    .progress-text {
      margin-top: 16px;
      font-size: 14px;
      color: #595959;
    }
  }

  .parse-result {
    margin-top: 24px;

    .result-success,
    .result-error {
      :deep(.ant-result) {
        padding: 32px 16px;
      }
    }

    .parse-metadata {
      margin-top: 24px;
      padding: 16px;
      background-color: #fafafa;
      border-radius: 4px;
    }

    .parse-warnings,
    .parse-errors {
      margin-top: 24px;

      h4 {
        margin-bottom: 16px;
        font-size: 15px;
        font-weight: 600;
        color: #333;

        .anticon {
          margin-right: 8px;
          color: #faad14;
        }
      }

      .warning-field {
        font-weight: 600;
        color: #faad14;
      }

      .warning-content {
        .warning-suggestion {
          margin-top: 4px;
          padding: 8px;
          background-color: #fffbe6;
          border-radius: 4px;
          font-size: 13px;
          color: #595959;
        }
      }

      :deep(.ant-list-item) {
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }
      }
    }

    .parse-errors {
      h4 {
        .anticon {
          color: #ff4d4f;
        }
      }

      :deep(.ant-alert) {
        margin-bottom: 12px;
      }
    }
  }
}

// Responsive design
@media (max-width: 768px) {
  .template-parser {
    .parse-result {
      .parse-metadata {
        :deep(.ant-descriptions) {
          .ant-descriptions-item {
            padding-bottom: 12px;
          }
        }
      }
    }
  }
}
</style>