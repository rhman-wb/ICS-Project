<template>
  <a-modal
    v-model:open="visible"
    title="导入规则"
    width="600px"
    :footer="null"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <div class="import-modal">
      <!-- 步骤指示器 -->
      <a-steps :current="currentStep" size="small" class="mb-6">
        <a-step title="选择文件" />
        <a-step title="上传导入" />
        <a-step title="导入结果" />
      </a-steps>

      <!-- 步骤1: 选择文件 -->
      <div v-if="currentStep === 0" class="step-content">
        <div class="upload-area">
          <a-upload-dragger
            v-model:fileList="fileList"
            :before-upload="beforeUpload"
            :accept=".xlsx,.xls"
            :multiple="false"
            :show-upload-list="false"
          >
            <div class="upload-content">
              <InboxOutlined class="upload-icon" />
              <p class="upload-text">点击或拖拽文件到此区域上传</p>
              <p class="upload-hint">支持扩展名：.xlsx .xls</p>
            </div>
          </a-upload-dragger>

          <div v-if="selectedFile" class="selected-file">
            <FileExcelOutlined />
            <span class="file-name">{{ selectedFile.name }}</span>
            <a-button type="link" @click="removeFile">
              <DeleteOutlined />
            </a-button>
          </div>
        </div>

        <div class="template-section">
          <a-divider>模板下载</a-divider>
          <div class="template-info">
            <DownloadOutlined class="template-icon" />
            <div class="template-text">
              <div>请先下载导入模板，按照模板格式填写规则信息</div>
              <a-button type="link" @click="downloadTemplate">下载模板</a-button>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <a-button @click="handleCancel">取消</a-button>
          <a-button
            type="primary"
            :disabled="!selectedFile"
            @click="nextStep"
          >
            下一步
          </a-button>
        </div>
      </div>

      <!-- 步骤2: 上传导入 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="import-info">
          <h4>导入信息确认</h4>
          <div class="file-info">
            <FileExcelOutlined />
            <span>{{ selectedFile?.name }}</span>
          </div>
        </div>

        <div class="import-options">
          <a-form layout="vertical">
            <a-form-item label="导入模式">
              <a-radio-group v-model:value="importMode">
                <a-radio value="append">追加模式（保留现有数据）</a-radio>
                <a-radio value="replace">替换模式（清空后导入）</a-radio>
              </a-radio-group>
            </a-form-item>

            <a-form-item label="数据校验">
              <a-checkbox v-model:checked="validateData">
                启用数据校验（推荐）
              </a-checkbox>
            </a-form-item>
          </a-form>
        </div>

        <div class="step-actions">
          <a-button @click="prevStep">上一步</a-button>
          <a-button
            type="primary"
            :loading="uploading"
            @click="startImport"
          >
            开始导入
          </a-button>
        </div>
      </div>

      <!-- 步骤3: 导入结果 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="import-result">
          <div v-if="uploading" class="importing">
            <a-spin size="large" />
            <div class="import-progress">
              <div>正在导入中...</div>
              <a-progress :percent="importProgress" />
            </div>
          </div>

          <div v-else class="import-summary">
            <div class="result-header">
              <CheckCircleOutlined
                v-if="importResult?.success"
                class="success-icon"
              />
              <ExclamationCircleOutlined
                v-else
                class="error-icon"
              />
              <h4>导入{{ importResult?.success ? '成功' : '失败' }}</h4>
            </div>

            <div class="result-stats" v-if="importResult">
              <div class="stat-item">
                <span class="label">总计：</span>
                <span class="value">{{ importResult.total }}条</span>
              </div>
              <div class="stat-item">
                <span class="label">成功：</span>
                <span class="value success">{{ importResult.success_count }}条</span>
              </div>
              <div class="stat-item" v-if="importResult.failed_count > 0">
                <span class="label">失败：</span>
                <span class="value error">{{ importResult.failed_count }}条</span>
              </div>
            </div>

            <div v-if="importResult?.errors?.length" class="error-details">
              <h5>错误详情</h5>
              <div class="error-list">
                <div
                  v-for="(error, index) in importResult.errors"
                  :key="index"
                  class="error-item"
                >
                  <span class="error-row">第{{ error.row }}行：</span>
                  <span class="error-message">{{ error.message }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="step-actions">
          <a-button v-if="!importResult?.success" @click="resetImport">
            重新导入
          </a-button>
          <a-button
            type="primary"
            @click="handleSuccess"
          >
            {{ importResult?.success ? '完成' : '关闭' }}
          </a-button>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  InboxOutlined,
  FileExcelOutlined,
  DeleteOutlined,
  DownloadOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import type { UploadFile } from 'ant-design-vue'
import { rulesApi } from '@/api/modules/rules'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success', data: any): void
}

interface ImportResult {
  success: boolean
  total: number
  success_count: number
  failed_count: number
  errors?: Array<{
    row: number
    message: string
  }>
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
})

const emit = defineEmits<Emits>()

// 响应式数据
const currentStep = ref(0)
const fileList = ref<UploadFile[]>([])
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const importProgress = ref(0)
const importMode = ref('append')
const validateData = ref(true)
const importResult = ref<ImportResult | null>(null)

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 监听visible变化，重置状态
watch(visible, (newVal) => {
  if (newVal) {
    resetModal()
  }
})

// 文件上传前处理
const beforeUpload = (file: File) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel'

  if (!isExcel) {
    message.error('只能上传Excel文件！')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过10MB！')
    return false
  }

  selectedFile.value = file
  return false // 阻止自动上传
}

// 移除文件
const removeFile = () => {
  selectedFile.value = null
  fileList.value = []
}

// 下载模板
const downloadTemplate = async () => {
  try {
    const response = await rulesApi.downloadImportTemplate()
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = '规则导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('模板下载成功')
  } catch (error) {
    message.error('模板下载失败')
  }
}

// 步骤控制
const nextStep = () => {
  if (currentStep.value < 2) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 开始导入
const startImport = async () => {
  if (!selectedFile.value) {
    message.error('请选择要导入的文件')
    return
  }

  uploading.value = true
  importProgress.value = 0
  currentStep.value = 2

  try {
    // 模拟进度更新
    const progressInterval = setInterval(() => {
      if (importProgress.value < 90) {
        importProgress.value += Math.random() * 10
      }
    }, 500)

    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('mode', importMode.value)
    formData.append('validate', validateData.value.toString())

    const response = await rulesApi.importRules(formData)

    clearInterval(progressInterval)
    importProgress.value = 100

    setTimeout(() => {
      uploading.value = false
      importResult.value = response.data
    }, 500)

  } catch (error: any) {
    uploading.value = false
    importResult.value = {
      success: false,
      total: 0,
      success_count: 0,
      failed_count: 0,
      errors: [{ row: 0, message: error.message || '导入失败' }]
    }
  }
}

// 重置导入
const resetImport = () => {
  currentStep.value = 0
  importResult.value = null
  importProgress.value = 0
}

// 重置整个模态框
const resetModal = () => {
  currentStep.value = 0
  selectedFile.value = null
  fileList.value = []
  uploading.value = false
  importProgress.value = 0
  importMode.value = 'append'
  validateData.value = true
  importResult.value = null
}

// 处理成功
const handleSuccess = () => {
  if (importResult.value?.success) {
    emit('success', importResult.value)
  }
  handleCancel()
}

// 处理取消
const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
.import-modal {
  .step-content {
    min-height: 300px;
    padding: 20px 0;
  }

  .upload-area {
    .upload-content {
      padding: 40px 20px;
      text-align: center;

      .upload-icon {
        font-size: 48px;
        color: #1890ff;
        margin-bottom: 16px;
      }

      .upload-text {
        font-size: 16px;
        margin-bottom: 8px;
      }

      .upload-hint {
        color: #8c8c8c;
        font-size: 14px;
      }
    }

    .selected-file {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f6f8fa;
      border-radius: 6px;
      margin-top: 16px;

      .file-name {
        flex: 1;
        font-weight: 500;
      }
    }
  }

  .template-section {
    margin-top: 24px;

    .template-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #f0f9ff;
      border-radius: 6px;

      .template-icon {
        font-size: 24px;
        color: #1890ff;
      }

      .template-text {
        flex: 1;
      }
    }
  }

  .import-info {
    h4 {
      margin-bottom: 16px;
    }

    .file-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f6f8fa;
      border-radius: 6px;
    }
  }

  .import-options {
    margin: 24px 0;
  }

  .import-result {
    .importing {
      text-align: center;
      padding: 40px 20px;

      .import-progress {
        margin-top: 20px;
      }
    }

    .import-summary {
      .result-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 20px;

        .success-icon {
          font-size: 24px;
          color: #52c41a;
        }

        .error-icon {
          font-size: 24px;
          color: #ff4d4f;
        }
      }

      .result-stats {
        display: flex;
        flex-direction: column;
        gap: 8px;
        margin-bottom: 20px;

        .stat-item {
          display: flex;
          justify-content: space-between;

          .value.success {
            color: #52c41a;
          }

          .value.error {
            color: #ff4d4f;
          }
        }
      }

      .error-details {
        h5 {
          margin-bottom: 12px;
          color: #ff4d4f;
        }

        .error-list {
          max-height: 200px;
          overflow-y: auto;
          border: 1px solid #f0f0f0;
          border-radius: 6px;
          padding: 12px;

          .error-item {
            display: flex;
            gap: 8px;
            margin-bottom: 8px;
            font-size: 14px;

            .error-row {
              font-weight: 500;
              min-width: 80px;
            }

            .error-message {
              color: #ff4d4f;
            }
          }
        }
      }
    }
  }

  .step-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>