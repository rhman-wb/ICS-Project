<template>
  <div class="product-form-reset">
    <!-- 重置按钮 -->
    <a-button
      v-if="!hideButton"
      :type="buttonType"
      :size="buttonSize"
      :danger="danger"
      :loading="resetting"
      :icon="h(ReloadOutlined)"
      @click="showResetConfirm"
      :class="buttonClass"
    >
      {{ buttonText }}
    </a-button>

    <!-- 确认对话框 -->
    <a-modal
      v-model:open="confirmVisible"
      title="确认重置表单"
      :width="480"
      :centered="true"
      @ok="performReset"
      @cancel="cancelReset"
      :confirmLoading="resetting"
      ok-text="确认重置"
      cancel-text="取消"
      :ok-button-props="{ danger: true }"
    >
      <div class="reset-confirm-content">
        <div class="warning-icon">
          <exclamation-circle-outlined style="color: #ff4d4f; font-size: 24px;" />
        </div>

        <div class="warning-message">
          <h4>确认重置表单数据？</h4>
          <p>此操作将清空以下内容：</p>

          <ul class="reset-items">
            <li v-for="item in resetItems" :key="item.key" class="reset-item">
              <span class="item-label">{{ item.label }}</span>
              <span class="item-description">{{ item.description }}</span>
            </li>
          </ul>

          <div class="warning-note">
            <warning-outlined style="color: #faad14; margin-right: 4px;" />
            <span>重置后的数据无法恢复，请谨慎操作！</span>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 重置成功反馈 -->
    <div v-if="showSuccessFeedback" class="reset-feedback">
      <a-alert
        message="表单重置成功"
        description="所有表单字段已恢复到初始状态"
        type="success"
        show-icon
        closable
        @close="showSuccessFeedback = false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  ExclamationCircleOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'

interface Props {
  // 按钮相关
  buttonText?: string
  buttonType?: 'default' | 'primary' | 'link' | 'text'
  buttonSize?: 'small' | 'middle' | 'large'
  buttonClass?: string
  danger?: boolean
  hideButton?: boolean

  // 重置内容配置
  resetItems?: ResetItem[]

  // 行为配置
  confirmText?: string
  successMessage?: string
  autoShowSuccess?: boolean
}

interface ResetItem {
  key: string
  label: string
  description: string
}

interface ResetData {
  productForm?: any
  documentUploads?: any[]
  validationResults?: any
  formState?: any
}

const props = withDefaults(defineProps<Props>(), {
  buttonText: '重置表单',
  buttonType: 'default',
  buttonSize: 'middle',
  buttonClass: '',
  danger: true,
  hideButton: false,
  confirmText: '确认重置表单数据？',
  successMessage: '表单重置成功',
  autoShowSuccess: true,
  resetItems: () => [
    {
      key: 'productInfo',
      label: '产品信息',
      description: '产品名称、类型、报送信息等基础字段'
    },
    {
      key: 'documentUploads',
      label: '文档上传',
      description: '已上传的条款、报告、费率表等文档'
    },
    {
      key: 'validationResults',
      label: '校验结果',
      description: '文档校验错误和警告信息'
    },
    {
      key: 'formState',
      label: '表单状态',
      description: '编辑状态、临时保存的数据等'
    }
  ]
})

const emit = defineEmits<{
  beforeReset: [data: ResetData]
  afterReset: [data: ResetData]
  resetCancelled: []
  resetError: [error: any]
}>()

// 响应式数据
const confirmVisible = ref(false)
const resetting = ref(false)
const showSuccessFeedback = ref(false)

// 方法
const showResetConfirm = () => {
  confirmVisible.value = true
}

const cancelReset = () => {
  confirmVisible.value = false
  emit('resetCancelled')
}

const performReset = async () => {
  resetting.value = true

  try {
    // 准备重置数据
    const resetData: ResetData = {
      productForm: null,
      documentUploads: [],
      validationResults: null,
      formState: null
    }

    // 触发重置前事件
    emit('beforeReset', resetData)

    // 执行重置逻辑
    await executeReset(resetData)

    // 关闭对话框
    confirmVisible.value = false

    // 显示成功反馈
    if (props.autoShowSuccess) {
      showSuccessFeedback.value = true
      message.success(props.successMessage)
    }

    // 触发重置后事件
    emit('afterReset', resetData)

  } catch (error: any) {
    console.error('表单重置失败:', error)
    message.error('重置失败: ' + (error.message || '未知错误'))
    emit('resetError', error)
  } finally {
    resetting.value = false
  }
}

const executeReset = async (resetData: ResetData) => {
  // 模拟重置过程
  await new Promise(resolve => setTimeout(resolve, 1000))

  // 重置各个组件的状态
  resetProductForm(resetData)
  resetDocumentUploads(resetData)
  resetValidationResults(resetData)
  resetFormState(resetData)
}

const resetProductForm = (resetData: ResetData) => {
  // 重置产品表单数据
  resetData.productForm = {
    productName: '',
    productType: '',
    reportType: '',
    productNature: '',
    year: null,
    revisionType: '',
    originalProductName: '',
    developmentMethod: '',
    primaryAdditional: '',
    primarySituation: '',
    productCategory: '',
    operatingRegion: '',
    operatingScope: '',
    demonstrationClauseName: '',
    operatingRegion1: '',
    operatingRegion2: '',
    salesPromotionName: ''
  }
}

const resetDocumentUploads = (resetData: ResetData) => {
  // 重置文档上传状态
  resetData.documentUploads = []
}

const resetValidationResults = (resetData: ResetData) => {
  // 重置校验结果
  resetData.validationResults = {
    isValid: true,
    errors: [],
    warnings: [],
    summary: {
      totalDocuments: 0,
      requiredDocuments: 5,
      uploadedDocuments: 0,
      validDocuments: 0,
      totalErrors: 0,
      totalWarnings: 0,
      completenessPercentage: 0
    }
  }
}

const resetFormState = (resetData: ResetData) => {
  // 重置表单状态
  resetData.formState = {
    currentStep: 1,
    isDirty: false,
    isSubmitting: false,
    lastSaveTime: null
  }
}

// 暴露方法给父组件
const reset = async () => {
  await performReset()
}

const showConfirm = () => {
  showResetConfirm()
}

defineExpose({
  reset,
  showConfirm,
  resetting
})
</script>

<style scoped lang="scss">
.product-form-reset {
  .reset-confirm-content {
    display: flex;
    padding: 16px 0;

    .warning-icon {
      flex-shrink: 0;
      margin-right: 16px;
      margin-top: 4px;
    }

    .warning-message {
      flex: 1;

      h4 {
        margin: 0 0 12px 0;
        color: #333;
        font-size: 16px;
        font-weight: 600;
      }

      p {
        margin: 0 0 12px 0;
        color: #666;
      }

      .reset-items {
        margin: 16px 0;
        padding: 0;
        list-style: none;

        .reset-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 8px 12px;
          margin-bottom: 8px;
          background-color: #fafafa;
          border-radius: 4px;
          border-left: 3px solid #ff4d4f;

          &:last-child {
            margin-bottom: 0;
          }

          .item-label {
            font-weight: 500;
            color: #333;
            min-width: 80px;
            margin-right: 12px;
          }

          .item-description {
            color: #666;
            font-size: 13px;
            flex: 1;
            line-height: 1.4;
          }
        }
      }

      .warning-note {
        display: flex;
        align-items: center;
        padding: 8px 12px;
        background-color: #fffbe6;
        border-radius: 4px;
        color: #faad14;
        font-size: 13px;
        margin-top: 16px;

        .anticon {
          flex-shrink: 0;
        }

        span {
          margin-left: 4px;
        }
      }
    }
  }

  .reset-feedback {
    margin-top: 16px;

    .ant-alert {
      border-radius: 6px;
    }
  }

  // 按钮样式变体
  :deep(.ant-btn) {
    &.reset-button-danger {
      &:hover {
        background-color: #ff7875;
        border-color: #ff7875;
      }
    }

    &.reset-button-secondary {
      color: #666;
      background-color: #f5f5f5;
      border-color: #d9d9d9;

      &:hover {
        color: #ff4d4f;
        background-color: #fff2f0;
        border-color: #ffccc7;
      }
    }

    &.reset-button-text {
      color: #ff4d4f;

      &:hover {
        background-color: #fff2f0;
      }
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .product-form-reset {
    .reset-confirm-content {
      flex-direction: column;

      .warning-icon {
        margin-right: 0;
        margin-bottom: 12px;
        text-align: center;
      }

      .warning-message {
        .reset-items {
          .reset-item {
            flex-direction: column;
            align-items: flex-start;

            .item-label {
              margin-bottom: 4px;
              min-width: auto;
            }
          }
        }
      }
    }
  }
}
</style>