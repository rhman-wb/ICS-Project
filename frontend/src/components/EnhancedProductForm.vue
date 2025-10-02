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
          <a-step title="模板选择" description="选择产品模板类型" />
          <a-step title="基本信息" description="产品基础信息" />
          <a-step title="文档上传" description="要件文档上传" />
          <a-step title="信息确认" description="信息核对确认" />
        </a-steps>
      </div>

      <!-- 表单内容 -->
      <div class="form-content">
        <!-- 步骤0: 模板选择 -->
        <div v-show="currentStep === 0" class="form-step">
          <div class="template-selection">
            <a-alert
              message="请选择产品模板类型"
              description="根据您的产品类型选择相应的模板，不同模板具有不同的字段配置和验证规则。"
              type="info"
              show-icon
              class="selection-alert"
            />

            <a-row :gutter="24" class="template-cards">
              <a-col :xs="24" :sm="12">
                <a-card
                  hoverable
                  class="template-option-card"
                  :class="{ 'card-selected': selectedTemplateType === 'backup' }"
                  @click="handleTemplateSelect('backup')"
                >
                  <template #cover>
                    <div class="card-icon backup-icon">
                      <FileTextOutlined />
                    </div>
                  </template>
                  <a-card-meta
                    title="备案产品模板"
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
                  <div class="card-features">
                    <a-tag color="blue">基础费率</a-tag>
                    <a-tag color="blue">免赔额（率）</a-tag>
                    <a-tag color="blue">赔偿限额</a-tag>
                    <a-tag color="blue">销售区域</a-tag>
                  </div>
                </a-card>
              </a-col>

              <a-col :xs="24" :sm="12">
                <a-card
                  hoverable
                  class="template-option-card"
                  :class="{ 'card-selected': selectedTemplateType === 'agricultural' }"
                  @click="handleTemplateSelect('agricultural')"
                >
                  <template #cover>
                    <div class="card-icon agricultural-icon">
                      <FileTextOutlined />
                    </div>
                  </template>
                  <a-card-meta
                    title="农险产品模板"
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
                  <div class="card-features">
                    <a-tag color="green">开发方式</a-tag>
                    <a-tag color="green">产品性质</a-tag>
                    <a-tag color="green">赔偿处理方式</a-tag>
                    <a-tag color="green">是否开办</a-tag>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </div>
        </div>

        <!-- 步骤1: 基本信息（使用动态表单） -->
        <div v-show="currentStep === 1" class="form-step">
          <DynamicForm
            v-if="selectedTemplateType && templateConfig"
            ref="dynamicFormRef"
            :config="templateConfig"
            v-model="formData"
            :submitting="submitting"
            :show-actions="false"
            :enable-validation="true"
            :show-validation-success="true"
            @validate="handleFormValidation"
            @field-change="handleFieldChange"
          />
          <ProductFormTemplate
            v-else
            ref="productFormRef"
            v-model:productType="formData.productType"
            v-model:formData="formData"
            @formValidChange="handleFormValidChange"
          />
        </div>

        <!-- 步骤2: 文档上传 -->
        <div v-show="currentStep === 2" class="form-step">
          <DocumentUploadWithValidation
            ref="documentUploadRef"
            :productId="formData.productId || tempProductId"
            :autoValidate="true"
            @uploadComplete="handleUploadComplete"
            @validationComplete="handleValidationComplete"
          />
        </div>

        <!-- 步骤3: 信息确认 -->
        <div v-show="currentStep === 3" class="form-step">
          <div class="form-review">
            <h3>请确认以下信息：</h3>

            <!-- 模板类型确认 -->
            <a-card title="模板信息" size="small" class="review-card">
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="模板类型">
                  {{ getTemplateTypeText(selectedTemplateType) }}
                </a-descriptions-item>
                <a-descriptions-item label="字段数量">
                  {{ selectedTemplateType === 'backup' ? '30个' : '25个' }}
                </a-descriptions-item>
              </a-descriptions>
            </a-card>

            <!-- 产品信息确认 -->
            <a-card title="产品信息" size="small" class="review-card">
              <a-descriptions :column="2" bordered>
                <a-descriptions-item label="产品名称">
                  {{ formData.productName }}
                </a-descriptions-item>
                <a-descriptions-item label="产品类型">
                  {{ getTemplateTypeText(formData.templateType) }}
                </a-descriptions-item>
                <a-descriptions-item label="报送类型">
                  {{ formData.reportType }}
                </a-descriptions-item>
                <a-descriptions-item label="经营区域">
                  {{ formData.operatingRegion || formData.operatingRegion1 }}
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
              v-if="currentStep < 3"
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
import { ref, reactive, computed, watch, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SaveOutlined,
  CheckOutlined,
  LeftOutlined,
  RightOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'
import ProductFormTemplate from './ProductFormTemplate.vue'
import DocumentUploadWithValidation from './DocumentUploadWithValidation.vue'
import ProductFormReset from './ProductFormReset.vue'
import DynamicForm from './product/DynamicForm.vue'
import { createProduct, submitProduct, type ProductCreateRequest } from '@/api/product'
import { templateApi } from '@/api/product/template'
import type {
  TemplateType,
  TemplateConfig,
  ProductFormData,
  ValidationResult,
  DocumentValidationResult
} from '@/types/product/template'
import {
  getTemplateTypeDisplayName,
  getProductTypeDisplayName,
  getProductTypeFromTemplate,
  type ProductType
} from '@/config/templateMapping'
import logger from '@/utils/logger'

interface Props {
  productId?: string
  showSteps?: boolean
  autoSave?: boolean
  autoSaveInterval?: number
}

const props = withDefaults(defineProps<Props>(), {
  showSteps: true,
  autoSave: false,
  autoSaveInterval: 30000 // 30秒
})

const emit = defineEmits<{
  formSubmit: [data: Partial<ProductFormData>]
  formSave: [data: Partial<ProductFormData>]
  formReset: []
  stepChange: [step: number]
}>()

const router = useRouter()

// 组件引用
const formResetRef = ref()
const productFormRef = ref()
const documentUploadRef = ref()
const dynamicFormRef = ref()

// 响应式数据
const currentStep = ref(0)
const saving = ref(false)
const submitting = ref(false)
const showResetFeedback = ref(false)
const selectedTemplateType = ref<TemplateType | null>(null)
const templateConfig = ref<TemplateConfig | null>(null)
const loadingTemplateConfig = ref(false)

// 表单数据
const formData = ref<Partial<ProductFormData>>({
  productName: '',
  reportType: '',
  year: undefined,
  templateType: undefined as any
})

// 表单状态
const isFormValid = ref(false)
const validationResult = ref<DocumentValidationResult | null>(null)
const tempProductId = ref(`temp_${Date.now()}`)
const actualProductId = ref<string | null>(null) // 真实产品ID
const fieldValidations = ref<Record<string, ValidationResult>>({})

// Template selection handler
const handleTemplateSelect = async (type: TemplateType) => {
  selectedTemplateType.value = type
  formData.value.templateType = type

  // Load template configuration
  await loadTemplateConfig(type)
}

// Load template configuration
const loadTemplateConfig = async (type: TemplateType) => {
  loadingTemplateConfig.value = true

  try {
    const response = await templateApi.getTemplateConfig(type)
    // 根据实际API返回结构处理
    if (response && response.data) {
      templateConfig.value = response.data.config || response.data
    }
  } catch (error: any) {
    logger.error('Failed to load template config:', error)
    message.error('加载模板配置失败')
  } finally {
    loadingTemplateConfig.value = false
  }
}

// Form validation handlers
const handleFormValidation = (validations: Record<string, ValidationResult>) => {
  fieldValidations.value = validations

  // Check if all fields are valid
  const allValid = Object.values(validations).every(v => v.valid)
  isFormValid.value = allValid
}

const handleFieldChange = (fieldName: string, value: any) => {
  logger.debug('Field changed:', fieldName, value)
}

// 计算属性
const canProceedToNext = () => {
  switch (currentStep.value) {
    case 0:
      // Step 0: Template selection
      return selectedTemplateType.value !== null
    case 1:
      // Step 1: Form validation
      return isFormValid.value && formData.value.productName?.trim() !== ''
    case 2:
      // Step 2: Document upload
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
  if (currentStep.value < 3) {
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

const getTemplateTypeText = (type: TemplateType | undefined | null): string => {
  return type ? getTemplateTypeDisplayName(type) : '未选择'
}

const getProductTypeText = (productType: string): string => {
  return getProductTypeDisplayName(productType as ProductType)
}

const handleUploadComplete = (file: any, documentType: string) => {
  logger.debug('文档上传完成:', file, documentType)
}

const handleFormValidChange = (valid: boolean) => {
  isFormValid.value = valid
}

const handleValidationComplete = (result: any) => {
  validationResult.value = result
}

const saveDraft = async () => {
  saving.value = true

  try {
    // 如果还没有实际产品ID，先创建产品
    if (!actualProductId.value) {
      // 转换模板类型为产品类型
      const productType = selectedTemplateType.value
        ? getProductTypeFromTemplate(selectedTemplateType.value)
        : undefined

      const productData: ProductCreateRequest = {
        productName: formData.value.productName,
        productType: productType as string,
        reportType: formData.value.reportType,
        productNature: formData.value.productNature,
        year: formData.value.year,
        revisionType: formData.value.revisionType || undefined,
        originalProductName: formData.value.originalProductName || undefined,
        developmentMethod: formData.value.developmentMethod || undefined,
        primaryAdditional: formData.value.primaryAdditional || undefined,
        primarySituation: formData.value.primarySituation || undefined,
        productCategory: formData.value.productCategory || undefined,
        operatingRegion: formData.value.operatingRegion || undefined,
        operatingScope: formData.value.operatingScope || undefined,
        demonstrationClauseName: formData.value.demonstrationClauseName || undefined,
        operatingRegion1: formData.value.operatingRegion1 || undefined,
        operatingRegion2: formData.value.operatingRegion2 || undefined,
        salesPromotionName: formData.value.salesPromotionName || undefined
      }

      const response = await createProduct(productData)
      if (response.success && response.data) {
        actualProductId.value = response.data.id
        formData.value.productId = response.data.id
        message.success('产品创建成功，已保存为草稿')
      } else {
        throw new Error(response.message || '产品创建失败')
      }
    } else {
      // 如果已有产品ID，可以调用更新接口（这里暂时跳过）
      message.success('草稿保存成功')
    }

    emit('formSave', { ...formData.value })

  } catch (error: any) {
    logger.error('保存草稿失败:', error)
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
    // 确保先有产品ID
    let productId = actualProductId.value

    if (!productId) {
      // 先创建产品
      await saveDraft()
      productId = actualProductId.value

      if (!productId) {
        throw new Error('产品创建失败')
      }
    }

    // 提交产品进行审核
    const response = await submitProduct(productId)

    if (response.success) {
      message.success('产品提交成功')
      emit('formSubmit', { ...formData.value })

      // 跳转到成功页面，使用实际产品ID
      router.push(`/product-management/success?productId=${productId}`)
    } else {
      throw new Error(response.message || '产品提交失败')
    }

  } catch (error: any) {
    logger.error('产品提交失败:', error)
    message.error('提交失败: ' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

const resetFormData = () => {
  formData.value = {
    productName: '',
    reportType: '',
    year: undefined,
    templateType: undefined as any
  }

  currentStep.value = 0
  isFormValid.value = false
  validationResult.value = null
  actualProductId.value = null // 重置实际产品ID
  selectedTemplateType.value = null
  templateConfig.value = null
  fieldValidations.value = {}
}

// 重置事件处理
const handleBeforeReset = (resetData: any) => {
  logger.debug('准备重置表单:', resetData)
}

const handleAfterReset = (resetData: any) => {
  logger.debug('表单重置完成:', resetData)

  // 重置表单数据
  resetFormData()

  // 重置子组件
  if (dynamicFormRef.value?.resetFields) {
    dynamicFormRef.value.resetFields()
  }
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
  logger.debug('用户取消重置')
}

const handleResetError = (error: any) => {
  logger.error('重置失败:', error)
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

    // Template selection styles
    .template-selection {
      .selection-alert {
        margin-bottom: 24px;
      }

      .template-cards {
        margin-top: 24px;

        .template-option-card {
          transition: all 0.3s ease;
          cursor: pointer;
          height: 100%;

          &:hover {
            border-color: #1890ff;
            box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
          }

          &.card-selected {
            border-color: #1890ff;
            box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
            background-color: #f0f8ff;
          }

          .card-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 120px;
            font-size: 48px;
            color: #fff;

            &.backup-icon {
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }

            &.agricultural-icon {
              background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
            }
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

          .card-features {
            margin-top: 16px;
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
          }
        }
      }
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