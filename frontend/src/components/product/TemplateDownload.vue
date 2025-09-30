<template>
  <div class="template-download">
    <a-card title="下载导入模板" class="download-card">
      <a-alert
        message="模板选择指导"
        description="请根据您的产品类型选择相应的模板：备案产品使用「备案产品自主注册信息登记表」，农险产品使用「农险产品信息登记表」。"
        type="info"
        show-icon
        class="guide-alert"
      />

      <a-row :gutter="24" class="template-options">
        <a-col :xs="24" :sm="12">
          <a-card
            hoverable
            class="template-card"
            :class="{ 'template-card-active': activeTemplate === 'backup' }"
            @click="handleTemplateSelect('backup')"
          >
            <template #cover>
              <div class="template-icon">
                <FileExcelOutlined />
              </div>
            </template>
            <a-card-meta
              title="备案产品自主注册信息登记表"
              description="适用于一般财产险、责任险、信用保险等备案产品"
            >
              <template #avatar>
                <a-badge
                  :count="30"
                  :number-style="{ backgroundColor: '#52c41a' }"
                  title="包含30个标准字段"
                />
              </template>
            </a-card-meta>
            <div class="template-actions">
              <a-button
                type="primary"
                :loading="downloading && activeTemplate === 'backup'"
                :disabled="downloading && activeTemplate !== 'backup'"
                @click.stop="handleDownload('backup')"
              >
                <template #icon>
                  <DownloadOutlined />
                </template>
                下载模板 (.xlsx)
              </a-button>
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :sm="12">
          <a-card
            hoverable
            class="template-card"
            :class="{ 'template-card-active': activeTemplate === 'agricultural' }"
            @click="handleTemplateSelect('agricultural')"
          >
            <template #cover>
              <div class="template-icon">
                <FileExcelOutlined />
              </div>
            </template>
            <a-card-meta
              title="农险产品信息登记表"
              description="适用于种植险、养殖险等农业保险产品"
            >
              <template #avatar>
                <a-badge
                  :count="25"
                  :number-style="{ backgroundColor: '#1890ff' }"
                  title="包含25个标准字段"
                />
              </template>
            </a-card-meta>
            <div class="template-actions">
              <a-button
                type="primary"
                :loading="downloading && activeTemplate === 'agricultural'"
                :disabled="downloading && activeTemplate !== 'agricultural'"
                @click.stop="handleDownload('agricultural')"
              >
                <template #icon>
                  <DownloadOutlined />
                </template>
                下载模板 (.xls)
              </a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-divider />

      <div class="usage-guide">
        <a-typography-title :level="5">
          <InfoCircleOutlined /> 使用说明
        </a-typography-title>
        <a-typography-paragraph>
          <ul>
            <li>下载模板后，请按照模板中的字段说明和填写规则填写产品信息</li>
            <li>所有带红色星号标记的字段为必填项，请确保填写完整</li>
            <li>下拉选项字段请严格按照模板中提供的选项填写，避免手动输入</li>
            <li>填写完成后，可通过下方的"上传模板文件"功能导入系统</li>
            <li>系统会自动解析模板内容并进行数据验证</li>
          </ul>
        </a-typography-paragraph>
      </div>
    </a-card>

    <!-- Download Progress Modal -->
    <a-modal
      v-model:open="showProgressModal"
      title="下载进度"
      :footer="null"
      :closable="false"
      :maskClosable="false"
      width="400px"
    >
      <div class="download-progress">
        <a-progress
          :percent="downloadProgress"
          :status="downloadStatus"
        />
        <p class="progress-text">{{ progressText }}</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileExcelOutlined,
  DownloadOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import { templateApi } from '@/api/product/template'
import type { TemplateType } from '@/types/product/template'

// Component state
const activeTemplate = ref<TemplateType | null>(null)
const downloading = ref(false)
const showProgressModal = ref(false)
const downloadProgress = ref(0)
const downloadStatus = ref<'normal' | 'success' | 'exception'>('normal')

// Computed properties
const progressText = computed(() => {
  if (downloadProgress.value === 0) {
    return '准备下载...'
  } else if (downloadProgress.value === 100 && downloadStatus.value === 'success') {
    return '下载完成!'
  } else if (downloadStatus.value === 'exception') {
    return '下载失败，请重试'
  } else {
    return `下载中... ${downloadProgress.value}%`
  }
})

// Template selection handler
const handleTemplateSelect = (type: TemplateType) => {
  activeTemplate.value = type
}

// Download handler
const handleDownload = async (type: TemplateType) => {
  try {
    downloading.value = true
    showProgressModal.value = true
    downloadProgress.value = 0
    downloadStatus.value = 'normal'

    // Simulate progress
    const progressInterval = setInterval(() => {
      if (downloadProgress.value < 90) {
        downloadProgress.value += 10
      }
    }, 100)

    // Call API to download template
    const response = await templateApi.downloadTemplate(type)

    // Stop progress simulation
    clearInterval(progressInterval)
    downloadProgress.value = 100
    downloadStatus.value = 'success'

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url

    // Set filename based on template type
    const filename =
      type === 'backup'
        ? '附件3_备案产品自主注册信息登记表.xlsx'
        : '附件5_农险产品信息登记表.xls'
    link.setAttribute('download', filename)

    // Trigger download
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    // Clean up
    window.URL.revokeObjectURL(url)

    // Show success message
    setTimeout(() => {
      showProgressModal.value = false
      message.success(`${filename} 下载成功`)
      downloadProgress.value = 0
    }, 1000)
  } catch (error: any) {
    console.error('Template download failed:', error)
    downloadStatus.value = 'exception'

    // Show error message
    const errorMessage = error.response?.data?.message || error.message || '模板下载失败'
    message.error(errorMessage)

    // Reset after delay
    setTimeout(() => {
      showProgressModal.value = false
      downloadProgress.value = 0
      downloadStatus.value = 'normal'
    }, 2000)
  } finally {
    downloading.value = false
  }
}

// Retry download handler
const handleRetry = () => {
  if (activeTemplate.value) {
    handleDownload(activeTemplate.value)
  }
}
</script>

<style scoped lang="scss">
.template-download {
  .download-card {
    :deep(.ant-card-body) {
      padding: 24px;
    }
  }

  .guide-alert {
    margin-bottom: 24px;
  }

  .template-options {
    margin-bottom: 24px;

    .template-card {
      transition: all 0.3s ease;
      cursor: pointer;
      height: 100%;

      &:hover {
        border-color: #1890ff;
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
      }

      &-active {
        border-color: #1890ff;
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
      }

      .template-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 120px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        font-size: 48px;
        color: #fff;
      }

      :deep(.ant-card-meta-title) {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 8px;
      }

      :deep(.ant-card-meta-description) {
        font-size: 14px;
        color: #8c8c8c;
      }

      .template-actions {
        margin-top: 16px;

        .ant-btn {
          width: 100%;
        }
      }
    }
  }

  .usage-guide {
    padding: 16px;
    background-color: #fafafa;
    border-radius: 4px;

    :deep(.ant-typography) {
      margin-bottom: 12px;

      .anticon {
        margin-right: 8px;
        color: #1890ff;
      }
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        margin-bottom: 8px;
        color: #595959;
        line-height: 1.6;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }

  .download-progress {
    padding: 20px 0;

    .progress-text {
      margin-top: 16px;
      text-align: center;
      font-size: 14px;
      color: #595959;
    }
  }
}

// Responsive design
@media (max-width: 768px) {
  .template-download {
    .template-options {
      .ant-col {
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>