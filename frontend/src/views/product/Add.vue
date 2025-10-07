<template>
  <div class="product-add-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-button type="link" class="back-button" @click="handleBack">
        <template #icon>
          <ArrowLeftOutlined />
        </template>
        返回
      </a-button>
      <h1 class="page-title">产品添加 （手动-农险）</h1>
    </div>

    <!-- 表单容器 -->
    <div class="page-content">
      <a-card class="form-card" :bordered="false">
        <!-- 必填项提示 -->
        <div class="required-notice">
          <span class="required-star">*</span>
          <span class="required-text">为必填项</span>
        </div>

        <a-form
          ref="formRef"
          :model="formData"
          :label-col="{ span: 6 }"
          :wrapper-col="{ span: 18 }"
          layout="horizontal"
        >
          <!-- 新增产品标题 -->
          <div class="section-title">新增产品</div>

          <!-- 产品内容模块 -->
          <ProductFormContent
            v-model:form-data="formData"
            v-model:template-type="templateType"
            @parse-success="handleParseSuccess"
            @validation-change="handleValidationChange"
          />

          <!-- 条款模块 -->
          <div class="section-title">条款</div>
          <ProductFormDocuments
            v-model:documents="formData.documents"
            section="clause"
            @validation-change="handleDocumentValidation"
          />

          <!-- 可行性报告模块 -->
          <div class="section-title">可行性报告</div>
          <ProductFormDocuments
            v-model:documents="formData.documents"
            section="feasibilityReport"
            @validation-change="handleDocumentValidation"
          />

          <!-- 表单操作按钮 -->
          <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
            <a-space :size="16">
              <a-button
                type="primary"
                size="large"
                :loading="submitting"
                :disabled="!isFormValid"
                @click="handleSubmit"
              >
                提交
              </a-button>
              <a-button size="large" @click="handleReset">
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'
import ProductFormContent from '@/components/product/ProductFormContent.vue'
import ProductFormDocuments from '@/components/product/ProductFormDocuments.vue'
import type { ProductFormData, TemplateType } from '@/types/product/template'
import { createProduct } from '@/api/product/product'
import logger from '@/utils/logger'

const router = useRouter()

// 表单引用
const formRef = ref()

// 模板类型
const templateType = ref<TemplateType>('AGRICULTURAL')

// 表单数据
const formData = reactive<ProductFormData>({
  // 产品内容字段
  templateSelection: '',
  registrationTablePath: '',
  productName: '',
  submissionType: '',
  revisionType: '',
  productCategory: '',
  productNature: '',
  developmentMethod: '',
  mainSupplementary: '',
  mainInsuranceStatus: '',
  operatingArea: '',
  year: '',
  originalProductName: '',
  originalProductCode: '',

  // 文档字段
  documents: {
    clause: {
      path: '',
      file: null
    },
    feasibilityReport: {
      path: '',
      file: null
    },
    actuarialReport: {
      path: '',
      file: null
    },
    rateTable: {
      path: '',
      file: null
    }
  }
} as ProductFormData)

// 表单验证状态
const isFormValid = ref(false)
const fieldValidations = ref<Record<string, boolean>>({})
const documentValidations = ref<Record<string, boolean>>({})
const submitting = ref(false)

// 计算表单整体有效性
const computedFormValid = computed(() => {
  const allFieldsValid = Object.values(fieldValidations.value).every(v => v === true)
  const allDocumentsValid = Object.values(documentValidations.value).every(v => v === true)
  return allFieldsValid && allDocumentsValid
})

// 事件处理

/**
 * 返回产品管理页面
 */
const handleBack = () => {
  router.push('/product-management')
}

/**
 * 处理文档解析成功
 */
const handleParseSuccess = (parsedData: any) => {
  // 自动填充解析的数据到表单
  Object.assign(formData, parsedData)
  message.success('产品信息登记表解析成功，数据已自动填充')
  logger.info('Product registration table parsed successfully', parsedData)
}

/**
 * 处理字段验证变化
 */
const handleValidationChange = (validations: Record<string, boolean>) => {
  fieldValidations.value = validations
  updateFormValidity()
}

/**
 * 处理文档验证变化
 */
const handleDocumentValidation = (validations: Record<string, boolean>) => {
  Object.assign(documentValidations.value, validations)
  updateFormValidity()
}

/**
 * 更新表单有效性
 */
const updateFormValidity = () => {
  isFormValid.value = computedFormValid.value
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!isFormValid.value) {
    message.warning('请完善必填项和上传必要文档')
    return
  }

  try {
    submitting.value = true
    message.loading('正在提交产品信息...', 0)

    // 准备提交数据
    const submitData = {
      ...formData,
      templateType: templateType.value
    }

    // 调用API提交
    const result = await createProduct(submitData)

    message.destroy()
    message.success('产品信息提交成功')

    logger.info('Product created successfully', result)

    // 跳转到成功页面
    setTimeout(() => {
      router.push({
        path: '/product-management/success',
        query: {
          productId: result.data?.id
        }
      })
    }, 1000)
  } catch (error: any) {
    message.destroy()
    message.error(`提交失败：${error.message || '未知错误'}`)
    logger.error('Failed to create product', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  // 显示确认对话框
  if (Object.keys(formData).some(key => formData[key as keyof ProductFormData])) {
    // 有数据时提示确认
    const confirmed = window.confirm('确定要重置表单吗？所有已填写的数据将被清空。')
    if (!confirmed) {
      return
    }
  }

  // 重置表单数据
  Object.keys(formData).forEach(key => {
    if (key === 'documents') {
      formData.documents = {
        clause: { path: '', file: null },
        feasibilityReport: { path: '', file: null },
        actuarialReport: { path: '', file: null },
        rateTable: { path: '', file: null }
      }
    } else {
      (formData as any)[key] = ''
    }
  })

  // 重置验证状态
  fieldValidations.value = {}
  documentValidations.value = {}
  isFormValid.value = false

  // 重置表单验证
  formRef.value?.resetFields()

  message.info('表单已重置')
}
</script>

<style scoped lang="scss">
.product-add-page {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;

    .back-button {
      padding: 4px 8px;
      margin-right: 12px;
      color: #1890ff;
      font-size: 14px;

      &:hover {
        background: #f0f9ff;
      }
    }

    .page-title {
      font-size: 20px;
      font-weight: 600;
      color: #262626;
      margin: 0;
    }
  }

  .page-content {
    max-width: 1200px;
    margin: 0 auto;

    .form-card {
      background: #ffffff;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .required-notice {
        padding: 16px 24px;
        background: #fafafa;
        border-bottom: 1px solid #f0f0f0;
        margin: -24px -24px 24px -24px;

        .required-star {
          color: #ff4d4f;
          font-size: 14px;
          margin-right: 4px;
        }

        .required-text {
          color: #8c8c8c;
          font-size: 14px;
        }
      }

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
        padding: 16px 0;
        border-bottom: 2px solid #1890ff;
        margin-bottom: 24px;
      }

      :deep(.ant-form-item) {
        margin-bottom: 24px;
      }

      :deep(.ant-form-item-label > label) {
        font-weight: 500;
        color: #595959;
      }

      :deep(.ant-form-item-required::before) {
        color: #ff4d4f !important;
      }
    }
  }
}
</style>
