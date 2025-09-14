<template>
  <div class="document-validation">
    <!-- 校验状态概览 -->
    <div class="validation-overview" v-if="validationResult">
      <a-card :title="overviewTitle" size="small" class="validation-card">
        <template #extra>
          <a-button
            type="primary"
            size="small"
            :loading="validating"
            @click="performValidation"
            :icon="h(ReloadOutlined)"
          >
            重新校验
          </a-button>
        </template>

        <!-- 校验结果统计 -->
        <div class="validation-summary">
          <a-row :gutter="16">
            <a-col :span="6">
              <a-statistic
                title="文档总数"
                :value="validationResult.summary.totalDocuments"
                :value-style="{ color: '#1890ff' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="必需文档"
                :value="validationResult.summary.requiredDocuments"
                :value-style="{ color: '#722ed1' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="已上传"
                :value="validationResult.summary.uploadedDocuments"
                :value-style="{ color: '#52c41a' }"
              />
            </a-col>
            <a-col :span="6">
              <a-statistic
                title="完整性"
                :value="validationResult.summary.completenessPercentage"
                suffix="%"
                :value-style="getCompletenessColor()"
              />
            </a-col>
          </a-row>
        </div>
      </a-card>
    </div>

    <!-- 校验错误信息 -->
    <div class="validation-errors" v-if="validationResult && validationResult.errors.length > 0">
      <a-card title="校验错误" size="small" class="error-card">
        <template #extra>
          <a-badge :count="validationResult.errors.length" status="error" />
        </template>

        <div class="error-list">
          <div
            v-for="error in validationResult.errors"
            :key="`${error.errorCode}-${error.documentType}`"
            class="error-item"
          >
            <div class="error-header">
              <a-tag
                :color="getSeverityColor(error.severity)"
                class="severity-tag"
              >
                {{ getSeverityText(error.severity) }}
              </a-tag>
              <span class="document-type-tag">
                {{ getDocumentTypeText(error.documentType) }}
              </span>
              <span class="error-code">{{ error.errorCode }}</span>
            </div>

            <div class="error-content">
              <div class="error-message">
                <exclamation-circle-outlined style="color: #ff4d4f; margin-right: 8px;" />
                {{ error.message }}
              </div>

              <div class="error-suggestion" v-if="error.suggestion">
                <div class="suggestion-label">建议解决方案：</div>
                <div class="suggestion-content">{{ error.suggestion }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 校验警告信息 -->
    <div class="validation-warnings" v-if="validationResult && validationResult.warnings.length > 0">
      <a-card title="校验警告" size="small" class="warning-card">
        <template #extra>
          <a-badge :count="validationResult.warnings.length" status="warning" />
        </template>

        <div class="warning-list">
          <div
            v-for="warning in validationResult.warnings"
            :key="`${warning.warningCode}-${warning.documentType}`"
            class="warning-item"
          >
            <div class="warning-header">
              <a-tag color="orange" class="warning-tag">警告</a-tag>
              <span class="document-type-tag">
                {{ getDocumentTypeText(warning.documentType) }}
              </span>
              <span class="warning-code">{{ warning.warningCode }}</span>
            </div>

            <div class="warning-content">
              <div class="warning-message">
                <warning-outlined style="color: #faad14; margin-right: 8px;" />
                {{ warning.message }}
              </div>

              <div class="warning-recommendation" v-if="warning.recommendation">
                <div class="recommendation-label">建议操作：</div>
                <div class="recommendation-content">{{ warning.recommendation }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 校验成功状态 -->
    <div class="validation-success" v-if="validationResult && validationResult.isValid">
      <a-result
        status="success"
        title="文档校验通过"
        sub-title="所有文档均符合要求，可以继续进行下一步操作。"
      >
        <template #icon>
          <check-circle-outlined style="color: #52c41a;" />
        </template>
      </a-result>
    </div>

    <!-- 加载状态 -->
    <div class="validation-loading" v-if="validating && !validationResult">
      <a-card>
        <div style="text-align: center; padding: 40px 0;">
          <a-spin size="large" />
          <div style="margin-top: 16px; color: #666;">正在校验文档...</div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  ExclamationCircleOutlined,
  WarningOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import DocumentValidationService, {
  type DocumentValidationResult,
  type ValidationError,
  type ValidationWarning
} from '@/api/documentValidation'

interface Props {
  productId: string
  autoValidate?: boolean
  documentTypeFilter?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  autoValidate: true,
  documentTypeFilter: () => []
})

const emit = defineEmits<{
  validationComplete: [result: DocumentValidationResult]
  validationError: [error: any]
}>()

// 响应式数据
const validating = ref(false)
const validationResult = ref<DocumentValidationResult | null>(null)

// 计算属性
const overviewTitle = computed(() => {
  if (!validationResult.value) return '文档校验'

  return validationResult.value.isValid ? '文档校验通过' : '文档校验未通过'
})

// 文档类型映射
const documentTypeMap: Record<string, string> = {
  'REGISTRATION': '产品信息登记表',
  'TERMS': '条款',
  'FEASIBILITY_REPORT': '可行性报告',
  'ACTUARIAL_REPORT': '精算报告',
  'RATE_TABLE': '费率表'
}

// 严重级别映射
const severityMap: Record<string, string> = {
  'LOW': '低',
  'MEDIUM': '中',
  'HIGH': '高',
  'CRITICAL': '严重'
}

// 方法
const performValidation = async () => {
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

      // 如果有错误，显示提示
      if (!response.data.isValid && response.data.errors.length > 0) {
        message.warning(`发现 ${response.data.errors.length} 个校验错误，请查看详情并修正`)
      } else if (response.data.warnings.length > 0) {
        message.info(`发现 ${response.data.warnings.length} 个校验警告`)
      } else {
        message.success('文档校验通过')
      }
    } else {
      message.error(response.message || '文档校验失败')
      emit('validationError', new Error(response.message))
    }
  } catch (error: any) {
    console.error('文档校验异常:', error)
    message.error(error.message || '文档校验异常')
    emit('validationError', error)
  } finally {
    validating.value = false
  }
}

const getCompletenessColor = () => {
  if (!validationResult.value) return { color: '#666' }

  const percentage = validationResult.value.summary.completenessPercentage
  if (percentage >= 100) return { color: '#52c41a' }
  if (percentage >= 80) return { color: '#faad14' }
  return { color: '#ff4d4f' }
}

const getSeverityColor = (severity: string): string => {
  const colorMap: Record<string, string> = {
    'LOW': 'blue',
    'MEDIUM': 'orange',
    'HIGH': 'red',
    'CRITICAL': 'magenta'
  }
  return colorMap[severity] || 'default'
}

const getSeverityText = (severity: string): string => {
  return severityMap[severity] || severity
}

const getDocumentTypeText = (documentType: string): string => {
  return documentTypeMap[documentType] || documentType
}

// 暴露方法给父组件
const validateNow = () => {
  performValidation()
}

// 生命周期
onMounted(() => {
  if (props.autoValidate && props.productId) {
    performValidation()
  }
})

// 暴露给父组件的方法
defineExpose({
  validateNow,
  validationResult,
  validating
})
</script>

<style scoped lang="scss">
.document-validation {
  .validation-overview {
    margin-bottom: 16px;

    .validation-card {
      border-radius: 8px;
    }

    .validation-summary {
      .ant-statistic {
        text-align: center;
      }
    }
  }

  .validation-errors {
    margin-bottom: 16px;

    .error-card {
      border: 1px solid #ffccc7;
      border-radius: 8px;

      :deep(.ant-card-head) {
        background-color: #fff2f0;
        border-bottom: 1px solid #ffccc7;
      }
    }

    .error-list {
      .error-item {
        padding: 12px;
        border-radius: 6px;
        background-color: #fff2f0;
        border: 1px solid #ffccc7;
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }

        .error-header {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          .severity-tag {
            font-weight: 500;
            margin-right: 8px;
          }

          .document-type-tag {
            background-color: #f0f0f0;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            color: #666;
            margin-right: 8px;
          }

          .error-code {
            font-family: monospace;
            color: #999;
            font-size: 12px;
          }
        }

        .error-content {
          .error-message {
            display: flex;
            align-items: flex-start;
            margin-bottom: 8px;
            font-weight: 500;
            color: #ff4d4f;
          }

          .error-suggestion {
            .suggestion-label {
              font-weight: 500;
              color: #666;
              margin-bottom: 4px;
            }

            .suggestion-content {
              color: #666;
              font-size: 13px;
              line-height: 1.5;
            }
          }
        }
      }
    }
  }

  .validation-warnings {
    margin-bottom: 16px;

    .warning-card {
      border: 1px solid #ffe7ba;
      border-radius: 8px;

      :deep(.ant-card-head) {
        background-color: #fffbe6;
        border-bottom: 1px solid #ffe7ba;
      }
    }

    .warning-list {
      .warning-item {
        padding: 12px;
        border-radius: 6px;
        background-color: #fffbe6;
        border: 1px solid #ffe7ba;
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }

        .warning-header {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          .warning-tag {
            font-weight: 500;
            margin-right: 8px;
          }

          .document-type-tag {
            background-color: #f0f0f0;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            color: #666;
            margin-right: 8px;
          }

          .warning-code {
            font-family: monospace;
            color: #999;
            font-size: 12px;
          }
        }

        .warning-content {
          .warning-message {
            display: flex;
            align-items: flex-start;
            margin-bottom: 8px;
            font-weight: 500;
            color: #faad14;
          }

          .warning-recommendation {
            .recommendation-label {
              font-weight: 500;
              color: #666;
              margin-bottom: 4px;
            }

            .recommendation-content {
              color: #666;
              font-size: 13px;
              line-height: 1.5;
            }
          }
        }
      }
    }
  }

  .validation-success {
    .ant-result {
      padding: 40px 20px;
    }
  }

  .validation-loading {
    .ant-card {
      border-radius: 8px;
    }
  }
}
</style>