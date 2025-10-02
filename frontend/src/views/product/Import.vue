<template>
  <div class="product-import-page">
    <a-page-header
      title="产品导入"
      sub-title="使用模板导入产品信息"
      @back="handleBack"
    >
      <template #extra>
        <a-space>
          <a-button @click="handleBack">
            <template #icon>
              <ArrowLeftOutlined />
            </template>
            返回
          </a-button>
          <a-button type="primary" :disabled="!canSubmit" :loading="submitting" @click="handleSubmit">
            <template #icon>
              <SaveOutlined />
            </template>
            提交产品信息
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <div class="page-content">
      <!-- Steps Navigator -->
      <a-card class="steps-card">
        <a-steps :current="currentStep" @change="handleStepChange">
          <a-step title="下载模板" description="选择并下载产品模板" />
          <a-step title="填写模板" description="填写产品信息（可选）" />
          <a-step title="上传解析" description="上传模板文件解析" />
          <a-step title="表单录入" description="完善产品信息" />
          <a-step title="提交审核" description="提交产品信息" />
        </a-steps>
      </a-card>

      <!-- Step Content -->
      <div class="step-content">
        <!-- Step 1: Download Template -->
        <div v-show="currentStep === 0" class="step-panel">
          <TemplateDownload
            v-model:template-type="selectedTemplateType"
            @download-success="handleDownloadSuccess"
          />
          <div class="step-actions">
            <a-button type="primary" size="large" @click="nextStep">
              下一步
              <template #icon>
                <ArrowRightOutlined />
              </template>
            </a-button>
          </div>
        </div>

        <!-- Step 2: Fill Template (Optional) -->
        <div v-show="currentStep === 1" class="step-panel">
          <a-card title="填写模板说明" class="instruction-card">
            <a-alert
              message="此步骤为可选项"
              description="您可以直接在下载的Excel模板中填写产品信息，填写完成后进入下一步上传；或者跳过此步骤，直接进入表单录入。"
              type="info"
              show-icon
              class="step-alert"
            />
            <div class="instruction-content">
              <h3>模板填写指南：</h3>
              <ol>
                <li>打开下载的Excel模板文件</li>
                <li>按照模板中的说明和示例填写产品信息</li>
                <li>确保所有必填字段都已填写完整</li>
                <li>保存文件后，点击"下一步"上传</li>
              </ol>
              <a-divider />
              <p class="tip-text">
                <InfoCircleOutlined /> 提示：如果您不熟悉Excel操作，可以直接跳过此步骤，使用系统表单录入。
              </p>
            </div>
          </a-card>
          <div class="step-actions">
            <a-space>
              <a-button @click="prevStep">
                <template #icon>
                  <ArrowLeftOutlined />
                </template>
                上一步
              </a-button>
              <a-button @click="skipTemplateUpload">
                跳过模板上传
              </a-button>
              <a-button type="primary" @click="nextStep">
                下一步
                <template #icon>
                  <ArrowRightOutlined />
                </template>
              </a-button>
            </a-space>
          </div>
        </div>

        <!-- Step 3: Upload and Parse Template -->
        <div v-show="currentStep === 2" class="step-panel">
          <TemplateParser
            :template-type="selectedTemplateType"
            @parse-success="handleParseSuccess"
            @parse-error="handleParseError"
          />
          <div class="step-actions">
            <a-space>
              <a-button @click="prevStep">
                <template #icon>
                  <ArrowLeftOutlined />
                </template>
                上一步
              </a-button>
              <a-button @click="skipTemplateUpload">
                跳过模板上传
              </a-button>
              <a-button
                type="primary"
                :disabled="!parsedData"
                @click="nextStep"
              >
                下一步
                <template #icon>
                  <ArrowRightOutlined />
                </template>
              </a-button>
            </a-space>
          </div>
        </div>

        <!-- Step 4: Form Entry -->
        <div v-show="currentStep === 3" class="step-panel">
          <DynamicForm
            ref="dynamicFormRef"
            v-model="productFormData"
            :fields="formFields"
            :template-type="selectedTemplateType"
            :enable-validation="true"
            :show-actions="false"
            @field-change="handleFieldChange"
            @validate="handleValidationChange"
          />
          <div class="step-actions">
            <a-space>
              <a-button @click="prevStep">
                <template #icon>
                  <ArrowLeftOutlined />
                </template>
                上一步
              </a-button>
              <a-button @click="handleSaveDraft">
                <template #icon>
                  <SaveOutlined />
                </template>
                保存草稿
              </a-button>
              <a-button
                type="primary"
                :disabled="!isFormValid"
                @click="nextStep"
              >
                下一步
                <template #icon>
                  <ArrowRightOutlined />
                </template>
              </a-button>
            </a-space>
          </div>
        </div>

        <!-- Step 5: Review and Submit -->
        <div v-show="currentStep === 4" class="step-panel">
          <a-card title="产品信息确认" class="review-card">
            <a-alert
              message="提交前请仔细检查"
              description="请仔细核对以下产品信息，确认无误后提交审核。提交后将无法直接修改，需要重新提交。"
              type="warning"
              show-icon
              class="review-alert"
            />

            <!-- Product Information Summary -->
            <div class="product-summary">
              <a-descriptions
                :column="{ xs: 1, sm: 2, md: 3 }"
                bordered
                size="small"
              >
                <a-descriptions-item label="模板类型">
                  {{ getTemplateTypeName(selectedTemplateType) }}
                </a-descriptions-item>
                <a-descriptions-item label="产品名称">
                  {{ productFormData.productName || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="产品代码">
                  {{ productFormData.productCode || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="保险类别">
                  {{ productFormData.insuranceCategory || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="保险期限">
                  {{ productFormData.insurancePeriod || '-' }}
                </a-descriptions-item>
                <a-descriptions-item label="创建时间">
                  {{ formatDateTime(new Date()) }}
                </a-descriptions-item>
              </a-descriptions>

              <a-divider />

              <!-- Detailed Information -->
              <a-collapse v-model:activeKey="activeReviewPanels" ghost>
                <a-collapse-panel key="basic" header="基本信息">
                  <a-descriptions :column="2" bordered size="small">
                    <template v-for="(value, key) in getBasicInfo()" :key="key">
                      <a-descriptions-item :label="getFieldLabel(key)">
                        {{ value || '-' }}
                      </a-descriptions-item>
                    </template>
                  </a-descriptions>
                </a-collapse-panel>
                <a-collapse-panel key="details" header="详细信息">
                  <a-descriptions :column="2" bordered size="small">
                    <template v-for="(value, key) in getDetailInfo()" :key="key">
                      <a-descriptions-item :label="getFieldLabel(key)">
                        {{ value || '-' }}
                      </a-descriptions-item>
                    </template>
                  </a-descriptions>
                </a-collapse-panel>
              </a-collapse>
            </div>
          </a-card>

          <div class="step-actions">
            <a-space>
              <a-button @click="prevStep">
                <template #icon>
                  <ArrowLeftOutlined />
                </template>
                上一步
              </a-button>
              <a-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
                <template #icon>
                  <CheckOutlined />
                </template>
                提交产品信息
              </a-button>
            </a-space>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  SaveOutlined,
  CheckOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import TemplateDownload from '@/components/product/TemplateDownload.vue'
import TemplateParser from '@/components/product/TemplateParser.vue'
import DynamicForm from '@/components/product/DynamicForm.vue'
import type {  ProductFormData, TemplateFieldConfig, ParseResult , ValidationResult } from '@/types/product/template'
import { getTemplateFieldConfig } from '@/api/product/template'
import { createProduct } from '@/api/product/product'
import logger from '@/utils/logger'

const router = useRouter()

// Step management
const currentStep = ref(0)
const selectedTemplateType = ref<'backup' | 'agricultural'>('backup')

// Form data
const productFormData = ref<ProductFormData>({} as ProductFormData)
const parsedData = ref<ParseResult | null>(null)
const formFields = ref<TemplateFieldConfig[]>([])
const isFormValid = ref(false)

// UI state
const submitting = ref(false)
const activeReviewPanels = ref(['basic', 'details'])
const dynamicFormRef = ref()

// Computed properties
const canSubmit = computed(() => {
  return currentStep.value === 4 && isFormValid.value && !submitting.value
})

// Lifecycle hooks
onMounted(() => {
  loadTemplateConfig()
})

// Methods
const loadTemplateConfig = async () => {
  try {
    const config = await getTemplateFieldConfig(selectedTemplateType.value)
    formFields.value = config.fields
  } catch (error) {
    logger.error('Failed to load template config:', error)
    message.error('加载模板配置失败')
  }
}

const handleBack = () => {
  router.push('/product-management')
}

const handleStepChange = (step: number) => {
  if (step < currentStep.value) {
    currentStep.value = step
  } else if (step === currentStep.value + 1) {
    nextStep()
  }
}

const nextStep = () => {
  if (currentStep.value < 4) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const skipTemplateUpload = () => {
  currentStep.value = 3
  message.info('已跳过模板上传，请直接在表单中填写产品信息')
}

const handleDownloadSuccess = () => {
  message.success('模板下载成功，请填写后上传或直接进入表单录入')
}

const handleParseSuccess = (result: ParseResult) => {
  parsedData.value = result
  productFormData.value = { ...productFormData.value, ...result.data }
  message.success('模板解析成功，数据已自动填充到表单')
}

const handleParseError = (error: any) => {
  message.error(`模板解析失败：${error.message || '未知错误'}`)
}

const handleFieldChange = (fieldName: string, value: any) => {
  logger.debug('Field changed:', fieldName, value)
}

const handleValidationChange = (results: Record<string, ValidationResult>) => {
  // 计算表单整体校验结果：所有字段全部通过才为 true
  isFormValid.value = Object.values(results).every(r => r.valid)
}

const handleSaveDraft = async () => {
  try {
    message.loading('正在保存草稿...', 0)
    // TODO: Implement draft save functionality
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.destroy()
    message.success('草稿保存成功')
  } catch (error) {
    message.destroy()
    message.error('草稿保存失败')
  }
}

const handleSubmit = async () => {
  if (!isFormValid.value) {
    message.warning('请先完善必填字段')
    return
  }

  try {
    submitting.value = true
    message.loading('正在提交产品信息...', 0)

    // Validate form
    if (dynamicFormRef.value) {
      await dynamicFormRef.value.validate()
    }

    // Submit product data
    await createProduct({
      ...productFormData.value,
      templateType: selectedTemplateType.value
    })

    message.destroy()
    message.success('产品信息提交成功')

    // Navigate to product list
    setTimeout(() => {
      router.push('/product-management')
    }, 1500)
  } catch (error: any) {
    message.destroy()
    message.error(`提交失败：${error.message || '未知错误'}`)
  } finally {
    submitting.value = false
  }
}

const getTemplateTypeName = (type: string) => {
  return type === 'backup' ? '备案产品' : '农险产品'
}

const formatDateTime = (date: Date) => {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
}

const getFieldLabel = (fieldName: string) => {
  const field = formFields.value.find(f => f.name === fieldName)
  return field?.label || fieldName
}

const getBasicInfo = () => {
  const basicFields = ['productName', 'productCode', 'insuranceCategory', 'insurancePeriod', 'coverageAmount']
  const info: Record<string, any> = {}
  basicFields.forEach(field => {
    if (productFormData.value[field] !== undefined) {
      info[field] = productFormData.value[field]
    }
  })
  return info
}

const getDetailInfo = () => {
  const basicFields = ['productName', 'productCode', 'insuranceCategory', 'insurancePeriod', 'coverageAmount']
  const info: Record<string, any> = {}
  Object.keys(productFormData.value).forEach(field => {
    if (!basicFields.includes(field) && productFormData.value[field] !== undefined) {
      info[field] = productFormData.value[field]
    }
  })
  return info
}
</script>

<style scoped lang="scss">
.product-import-page {
  min-height: 100vh;
  background: #f0f2f5;

  .page-content {
    padding: 24px;
    max-width: 1400px;
    margin: 0 auto;
  }

  .steps-card {
    margin-bottom: 24px;
  }

  .step-content {
    .step-panel {
      min-height: 400px;
    }

    .step-actions {
      margin-top: 24px;
      text-align: center;
      padding: 24px 0;
      border-top: 1px solid #f0f0f0;
    }
  }

  .instruction-card {
    .step-alert {
      margin-bottom: 24px;
    }

    .instruction-content {
      h3 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 16px;
        color: #333;
      }

      ol {
        padding-left: 24px;

        li {
          margin-bottom: 12px;
          font-size: 14px;
          line-height: 1.8;
          color: #666;
        }
      }

      .tip-text {
        font-size: 14px;
        color: #1890ff;
        margin-top: 16px;

        .anticon {
          margin-right: 8px;
        }
      }
    }
  }

  .review-card {
    .review-alert {
      margin-bottom: 24px;
    }

    .product-summary {
      margin-top: 24px;
    }
  }
}
</style>