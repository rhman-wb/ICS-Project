<template>
  <div class="enhanced-product-form">
    <a-card title="产品信息登记" class="form-card">
      <template #extra>
        <a-space>
          <a-button
            type="default"
            @click="saveDraft"
            :loading="saving"
            :icon="h(SaveOutlined)"
          >
            保存草稿
          </a-button>

          <ProductFormReset
            ref="formResetRef"
            buttonText="重置表单"
            buttonType="default"
            danger
            @beforeReset="handleBeforeReset"
            @afterReset="handleAfterReset"
            @resetCancelled="handleResetCancelled"
            @resetError="handleResetError"
          />

          <a-button
            type="primary"
            @click="submitForm"
            :loading="submitting"
            :disabled="!isFormValid"
            :icon="h(CheckOutlined)"
          >
            提交审核
          </a-button>
        </a-space>
      </template>

      <!-- 表单步骤指示器 -->
      <div class="form-steps" v-if="showSteps">
        <a-steps :current="currentStep" size="small" class="steps-nav">
          <a-step title="基本信息" description="产品基础信息" />
          <a-step title="文档上传" description="要件文档上传" />
          <a-step title="信息确认" description="信息核对确认" />
        </a-steps>
      </div>

      <!-- 表单内容 -->
      <div class="form-content">
        <!-- 步骤1: 基本信息 -->
        <div v-show="currentStep === 0" class="form-step">
          <ProductFormTemplate
            ref="productFormRef"
            v-model:productType="formData.productType"
            v-model:formData="formData"
            @formValidChange="handleFormValidChange"
          />
        </div>

        <!-- 步骤2: 文档上传 -->
        <div v-show="currentStep === 1" class="form-step">
          <DocumentUploadWithValidation
            ref="documentUploadRef"
            :productId="formData.productId || tempProductId"
            :autoValidate="true"
            @uploadComplete="handleUploadComplete"
            @validationComplete="handleValidationComplete"
          />
        </div>

        <!-- 步骤3: 信息确认 -->
        <div v-show="currentStep === 2" class="form-step">
          <div class="form-review">
            <h3>请确认以下信息：</h3>

            <!-- 产品信息确认 -->
            <a-card title="产品信息" size="small" class="review-card">
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="产品名称">
                  {{ formData.productName }}
                </a-descriptions-item>
                <a-descriptions-item label="产品类型">
                  {{ getProductTypeText(formData.productType) }}
                </a-descriptions-item>
                <a-descriptions-item label="报送类型">
                  {{ formData.reportType }}
                </a-descriptions-item>
                <a-descriptions-item label="经营区域">
                  {{ formData.operatingRegion }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 文档上传确认 -->
            <a-card title="文档上传情况" size="small" class="review-card">
              <div v-if="validationResult">
                <div class="validation-summary">
                  <a-row :gutter="16">
                    <a-col :span="8">
                      <a-statistic
                        title="已上传"
                        :value="validationResult.summary.uploadedDocuments"
                        :suffix="`/ ${validationResult.summary.requiredDocuments}`"
                      />
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="完整性"
                        :value="validationResult.summary.completenessPercentage"
                        suffix="%"
                        :value-style="getCompletenessStyle()"
                      />
                    </a-col>
                    <a-col :span="8">
                      <a-statistic
                        title="错误数"
                        :value="validationResult.summary.totalErrors"
                        :value-style="{ color: validationResult.summary.totalErrors > 0 ? '#ff4d4f' : '#52c41a' }"
                      />
                    </a-col>
                  </a-row>
                </div>

                <!-- 错误提示 -->
                <div v-if="validationResult.summary.totalErrors > 0" class="validation-errors">
                  <a-alert
                    message="发现校验错误"
                    :description="`请修正 ${validationResult.summary.totalErrors} 个错误后再提交`"
                    type="error"
                    show-icon
                  />
                </div>
              </div>
              <div v-else>
                <a-empty description="暂无文档校验信息" />
              </div>
            </a-card>
          </div>
        </div>

        <!-- 表单操作按钮 -->
        <div class="form-actions">
          <a-space>
            <a-button
              v-if="currentStep > 0"
              @click="prevStep"
              :icon="h(LeftOutlined)"
            >
              上一步
            </a-button>

            <a-button
              v-if="currentStep < 2"
              type="primary"
              @click="nextStep"
              :disabled="!canProceedToNext()"
              :icon="h(RightOutlined)"
            >
              下一步
            </a-button>
          </a-space>
        </div>
      </div>
    </a-card>

    <!-- 重置确认提示 -->
    <div v-if="showResetFeedback" class="reset-feedback">
      <a-alert
        message="表单已重置"
        description="所有数据已清空，您可以重新填写产品信息"
        type="info"
        show-icon
        closable
        @close="showResetFeedback = false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, h } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SaveOutlined,
  CheckOutlined,
  LeftOutlined,
  RightOutlined
} from '@ant-design/icons-vue'
import ProductFormTemplate from './ProductFormTemplate.vue'
import DocumentUploadWithValidation from './DocumentUploadWithValidation.vue'
import ProductFormReset from './ProductFormReset.vue'

interface Props {
  productId?: string
  showSteps?: boolean
  autoSave?: boolean
  autoSaveInterval?: number
}

interface FormData {
  productId?: string
  productName: string
  productType: string
  reportType: string
  productNature: string
  year: number | null
  revisionType: string
  originalProductName: string
  developmentMethod: string
  primaryAdditional: string
  primarySituation: string
  productCategory: string
  operatingRegion: string
  operatingScope: string
  demonstrationClauseName: string
  operatingRegion1: string
  operatingRegion2: string
  salesPromotionName: string
}

const props = withDefaults(defineProps<Props>(), {
  showSteps: true,
  autoSave: false,
  autoSaveInterval: 30000 // 30秒
})

const emit = defineEmits<{
  formSubmit: [data: FormData]
  formSave: [data: FormData]
  formReset: []
  stepChange: [step: number]
}>()

const router = useRouter()

// 组件引用
const formResetRef = ref()
const productFormRef = ref()
const documentUploadRef = ref()

// 响应式数据
const currentStep = ref(0)
const saving = ref(false)
const submitting = ref(false)
const showResetFeedback = ref(false)

// 表单数据
const formData = reactive<FormData>({
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
})

// 表单状态
const isFormValid = ref(false)
const validationResult = ref<any>(null)
const tempProductId = ref(`temp_${Date.now()}`)

// 计算属性
const canProceedToNext = () => {
  switch (currentStep.value) {
    case 0:
      return isFormValid.value && formData.productName.trim() !== ''
    case 1:
      return validationResult.value?.isValid || false
    default:
      return true
  }
}

const getCompletenessStyle = () => {
  if (!validationResult.value) return { color: '#666' }

  const percentage = validationResult.value.summary.completenessPercentage
  if (percentage >= 100) return { color: '#52c41a' }
  if (percentage >= 80) return { color: '#faad14' }
  return { color: '#ff4d4f' }
}

// 方法
const nextStep = () => {
  if (currentStep.value < 2) {
    currentStep.value++
    emit('stepChange', currentStep.value)
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    emit('stepChange', currentStep.value)
  }
}

const handleFormValidChange = (valid: boolean) => {
  isFormValid.value = valid
}

const handleUploadComplete = (file: any, documentType: string) => {
  console.log('文档上传完成:', file, documentType)
}

const handleValidationComplete = (result: any) => {
  validationResult.value = result
}

const saveDraft = async () => {
  saving.value = true

  try {
    // 模拟保存API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    message.success('草稿保存成功')
    emit('formSave', { ...formData })

  } catch (error: any) {
    console.error('保存草稿失败:', error)
    message.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const submitForm = async () => {
  if (!isFormValid.value) {
    message.error('请先完善表单信息')
    return
  }

  if (!validationResult.value?.isValid) {
    message.error('请先完成文档上传并通过校验')
    return
  }

  submitting.value = true

  try {
    // 模拟提交API调用
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success('产品提交成功')
    emit('formSubmit', { ...formData })

    // 跳转到成功页面
    router.push(`/product-management/success?productId=${formData.productId || tempProductId.value}`)

  } catch (error: any) {
    console.error('产品提交失败:', error)
    message.error('提交失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

const resetFormData = () => {
  Object.keys(formData).forEach(key => {
    if (key === 'year') {
      formData[key as keyof FormData] = null as any
    } else {
      formData[key as keyof FormData] = '' as any
    }
  })

  currentStep.value = 0
  isFormValid.value = false
  validationResult.value = null
}

// 重置事件处理
const handleBeforeReset = (resetData: any) => {
  console.log('准备重置表单:', resetData)
}

const handleAfterReset = (resetData: any) => {
  console.log('表单重置完成:', resetData)

  // 重置表单数据
  resetFormData()

  // 重置子组件
  if (productFormRef.value?.reset) {
    productFormRef.value.reset()
  }
  if (documentUploadRef.value?.reset) {
    documentUploadRef.value.reset()
  }

  // 显示重置反馈
  showResetFeedback.value = true

  emit('formReset')
}

const handleResetCancelled = () => {
  console.log('用户取消重置')
}

const handleResetError = (error: any) => {
  console.error('重置失败:', error)
}

const getProductTypeText = (productType: string): string => {
  const typeMap: Record<string, string> = {
    'AGRICULTURAL': '农险产品',
    'FILING': '备案产品'
  }
  return typeMap[productType] || productType
}

// 暴露方法
const reset = () => {
  if (formResetRef.value) {
    formResetRef.value.showConfirm()
  }
}

defineExpose({
  reset,
  formData,
  currentStep,
  isFormValid
})
</script>

<style scoped lang="scss">
.enhanced-product-form {
  .form-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    :deep(.ant-card-head-title) {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .form-steps {
    margin-bottom: 32px;

    .steps-nav {
      :deep(.ant-steps-item-title) {
        font-size: 14px;
      }

      :deep(.ant-steps-item-description) {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .form-content {
    .form-step {
      min-height: 400px;
    }

    .form-review {
      h3 {
        margin-bottom: 24px;
        color: #333;
        font-size: 16px;
        font-weight: 600;
      }

      .review-card {
        margin-bottom: 24px;
        border-radius: 6px;

        :deep(.ant-card-head-title) {
          font-size: 14px;
          font-weight: 500;
        }

        .validation-summary {
          margin-bottom: 16px;

          .ant-statistic {
            text-align: center;

            :deep(.ant-statistic-title) {
              font-size: 12px;
              color: #666;
            }

            :deep(.ant-statistic-content) {
              font-size: 20px;
              font-weight: 600;
            }
          }
        }

        .validation-errors {
          .ant-alert {
            border-radius: 4px;
          }
        }
      }
    }

    .form-actions {
      margin-top: 32px;
      text-align: center;
      padding-top: 24px;
      border-top: 1px solid #e8e8e8;

      .ant-btn {
        min-width: 100px;
        height: 36px;
      }
    }
  }

  .reset-feedback {
    margin-top: 16px;

    .ant-alert {
      border-radius: 6px;
    }
  }
}

@media (max-width: 768px) {
  .enhanced-product-form {
    .form-card {
      :deep(.ant-card-head) {
        .ant-card-extra {
          .ant-space {
            flex-direction: column;
            width: 100%;

            .ant-btn {
              width: 100%;
            }
          }
        }
      }
    }

    .form-content {
      .form-review {
        .review-card {
          .validation-summary {
            .ant-row {
              .ant-col {
                margin-bottom: 16px;
              }
            }
          }
        }
      }
    }
  }
}
</style>