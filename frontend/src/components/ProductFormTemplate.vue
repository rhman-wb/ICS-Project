<template>
  <div class="product-form-template">
    <!-- 产品类型选择器 -->
    <a-card title="产品类型选择" class="template-selector">
      <a-radio-group
        v-model:value="selectedTemplate"
        size="large"
        @change="handleTemplateChange"
        class="template-options"
      >
        <a-radio-button value="agricultural" class="template-option agricultural">
          <FileTextOutlined />
          <span class="option-text">农险产品信息登记表</span>
          <div class="option-desc">适用于政策性农险、商业性农险等产品登记</div>
        </a-radio-button>
        <a-radio-button value="filing" class="template-option filing">
          <AuditOutlined />
          <span class="option-text">备案产品自主注册信息登记表</span>
          <div class="option-desc">适用于备案产品的自主注册信息登记</div>
        </a-radio-button>
      </a-radio-group>
    </a-card>

    <!-- 动态表单区域 -->
    <div class="form-container" v-if="selectedTemplate">
      <a-card class="form-wrapper">
        <!-- 表单切换动画容器 -->
        <transition name="form-slide" mode="out-in">
          <!-- 农险产品表单 -->
          <AgriculturalProductForm
            v-if="selectedTemplate === 'agricultural'"
            key="agricultural"
            :initial-data="formData.agricultural"
            :loading="loading"
            @submit="handleAgriculturalSubmit"
            @reset="handleFormReset('agricultural')"
            ref="agriculturalFormRef"
          />

          <!-- 备案产品表单 -->
          <FilingProductForm
            v-else-if="selectedTemplate === 'filing'"
            key="filing"
            :initial-data="formData.filing"
            :loading="loading"
            @submit="handleFilingSubmit"
            @reset="handleFormReset('filing')"
            ref="filingFormRef"
          />
        </transition>
      </a-card>
    </div>

    <!-- 模板切换确认弹窗 -->
    <a-modal
      v-model:open="switchConfirmVisible"
      title="切换产品类型模板"
      :confirm-loading="loading"
      @ok="confirmTemplateSwitch"
      @cancel="cancelTemplateSwitch"
    >
      <div class="switch-warning">
        <ExclamationCircleOutlined class="warning-icon" />
        <div class="warning-content">
          <p><strong>注意：</strong>切换产品类型模板将清空当前已填写的表单数据！</p>
          <p>是否确认切换到<strong>{{ getTemplateDisplayName(pendingTemplate) }}</strong>？</p>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  FileTextOutlined,
  AuditOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import AgriculturalProductForm from './AgriculturalProductForm.vue'
import FilingProductForm from './FilingProductForm.vue'
import type { ProductInfo } from '@/types/product'

type TemplateType = 'agricultural' | 'filing' | null

interface Props {
  initialTemplate?: TemplateType
  initialData?: {
    agricultural?: Partial<ProductInfo>
    filing?: Partial<ProductInfo>
  }
  autoSave?: boolean
}

interface Emits {
  (e: 'submit', data: { type: TemplateType; formData: ProductInfo }): void
  (e: 'template-change', template: TemplateType): void
  (e: 'form-reset', template: TemplateType): void
}

const props = withDefaults(defineProps<Props>(), {
  initialTemplate: null,
  autoSave: false,
  initialData: () => ({})
})

const emit = defineEmits<Emits>()

// 表单引用
const agriculturalFormRef = ref<InstanceType<typeof AgriculturalProductForm>>()
const filingFormRef = ref<InstanceType<typeof FilingProductForm>>()

// 当前选中的模板
const selectedTemplate = ref<TemplateType>(props.initialTemplate)
const pendingTemplate = ref<TemplateType>(null)
const loading = ref(false)

// 模板切换确认弹窗
const switchConfirmVisible = ref(false)

// 表单数据存储
const formData = reactive({
  agricultural: { ...props.initialData.agricultural } as Partial<ProductInfo>,
  filing: { ...props.initialData.filing } as Partial<ProductInfo>
})

// 模板显示名称映射
const getTemplateDisplayName = (template: TemplateType): string => {
  switch (template) {
    case 'agricultural':
      return '农险产品信息登记表'
    case 'filing':
      return '备案产品自主注册信息登记表'
    default:
      return ''
  }
}

// 检查当前表单是否有数据
const hasFormData = computed(() => {
  if (!selectedTemplate.value) return false

  const currentData = formData[selectedTemplate.value]
  return Object.values(currentData).some(value =>
    value !== undefined && value !== null && value !== ''
  )
})

// 处理模板切换
const handleTemplateChange = (e: any) => {
  const newTemplate = e.target.value as TemplateType

  // 如果当前表单有数据，显示确认弹窗
  if (hasFormData.value && selectedTemplate.value !== newTemplate) {
    pendingTemplate.value = newTemplate
    selectedTemplate.value = selectedTemplate.value // 恢复到原来的值
    switchConfirmVisible.value = true
  } else {
    // 没有数据或相同模板，直接切换
    selectedTemplate.value = newTemplate
    emit('template-change', newTemplate)
  }
}

// 确认模板切换
const confirmTemplateSwitch = () => {
  if (!pendingTemplate.value) return

  // 清空当前表单数据
  if (selectedTemplate.value) {
    Object.keys(formData[selectedTemplate.value]).forEach(key => {
      delete (formData[selectedTemplate.value] as any)[key]
    })
  }

  selectedTemplate.value = pendingTemplate.value
  pendingTemplate.value = null
  switchConfirmVisible.value = false

  message.success(`已切换到${getTemplateDisplayName(selectedTemplate.value)}`)
  emit('template-change', selectedTemplate.value)
}

// 取消模板切换
const cancelTemplateSwitch = () => {
  pendingTemplate.value = null
  switchConfirmVisible.value = false
}

// 处理农险产品表单提交
const handleAgriculturalSubmit = (data: ProductInfo) => {
  loading.value = true

  // 更新存储的表单数据
  Object.assign(formData.agricultural, data)

  // 设置产品类型
  const submitData = {
    ...data,
    productType: 'AGRICULTURAL'
  }

  setTimeout(() => {
    loading.value = false
    emit('submit', { type: 'agricultural', formData: submitData })

    if (props.autoSave) {
      message.success('农险产品信息已自动保存')
    }
  }, 1000)
}

// 处理备案产品表单提交
const handleFilingSubmit = (data: ProductInfo) => {
  loading.value = true

  // 更新存储的表单数据
  Object.assign(formData.filing, data)

  // 设置产品类型
  const submitData = {
    ...data,
    productType: 'FILING'
  }

  setTimeout(() => {
    loading.value = false
    emit('submit', { type: 'filing', formData: submitData })

    if (props.autoSave) {
      message.success('备案产品信息已自动保存')
    }
  }, 1000)
}

// 处理表单重置
const handleFormReset = (template: TemplateType) => {
  if (!template) return

  // 清空对应的表单数据
  Object.keys(formData[template]).forEach(key => {
    delete (formData[template] as any)[key]
  })

  emit('form-reset', template)
}

// 获取当前表单引用
const getCurrentFormRef = () => {
  return selectedTemplate.value === 'agricultural'
    ? agriculturalFormRef.value
    : filingFormRef.value
}

// 验证当前表单
const validateCurrentForm = async () => {
  const formRef = getCurrentFormRef()
  if (!formRef) {
    throw new Error('没有可用的表单实例')
  }

  try {
    return await formRef.validate()
  } catch (error) {
    throw new Error('表单验证失败')
  }
}

// 监听初始数据变化
watch(() => props.initialData, (newData) => {
  if (newData.agricultural) {
    Object.assign(formData.agricultural, newData.agricultural)
  }
  if (newData.filing) {
    Object.assign(formData.filing, newData.filing)
  }
}, { deep: true })

// 暴露组件方法
defineExpose({
  selectedTemplate,
  formData,
  validateCurrentForm,
  switchTemplate: (template: TemplateType) => {
    selectedTemplate.value = template
  },
  getCurrentFormData: () => {
    return selectedTemplate.value ? formData[selectedTemplate.value] : null
  },
  resetCurrentForm: () => {
    if (selectedTemplate.value) {
      handleFormReset(selectedTemplate.value)
    }
  }
})
</script>

<style scoped>
.product-form-template {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.template-selector {
  margin-bottom: 24px;
}

.template-selector :deep(.ant-card-head) {
  background: linear-gradient(135deg, #f6f8fa 0%, #e8f4fd 100%);
  border-bottom: 2px solid #722ed1;
}

.template-selector :deep(.ant-card-head-title) {
  color: #722ed1;
  font-weight: 600;
  font-size: 16px;
}

.template-options {
  width: 100%;
  display: flex;
  gap: 16px;
}

.template-option {
  flex: 1;
  height: auto !important;
  padding: 20px !important;
  border-radius: 8px !important;
  text-align: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.template-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.template-option:hover::before {
  left: 100%;
}

.template-option.agricultural {
  border-color: #1890ff !important;
}

.template-option.agricultural:hover,
.template-option.agricultural.ant-radio-button-wrapper-checked {
  background: linear-gradient(135deg, #e6f7ff 0%, #91d5ff 50%, #1890ff 100%) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.template-option.filing {
  border-color: #52c41a !important;
}

.template-option.filing:hover,
.template-option.filing.ant-radio-button-wrapper-checked {
  background: linear-gradient(135deg, #f6ffed 0%, #b7eb8f 50%, #52c41a 100%) !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.template-option .anticon {
  font-size: 24px;
  margin-bottom: 8px;
  display: block;
}

.option-text {
  display: block;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  line-height: 1.2;
}

.option-desc {
  font-size: 12px;
  opacity: 0.8;
  line-height: 1.3;
  margin-top: 4px;
}

.form-container {
  margin-top: 24px;
}

.form-wrapper {
  background: #fafafa;
  border-radius: 12px;
  overflow: hidden;
}

/* 表单切换动画 */
.form-slide-enter-active,
.form-slide-leave-active {
  transition: all 0.3s ease;
}

.form-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.form-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.form-slide-enter-to,
.form-slide-leave-from {
  opacity: 1;
  transform: translateX(0);
}

/* 切换确认弹窗样式 */
.switch-warning {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
}

.warning-icon {
  color: #faad14;
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 2px;
}

.warning-content {
  flex: 1;
}

.warning-content p {
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.warning-content p:last-child {
  margin-bottom: 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .product-form-template {
    padding: 16px;
  }

  .template-options {
    flex-direction: column;
    gap: 12px;
  }

  .template-option {
    padding: 16px !important;
  }

  .option-text {
    font-size: 14px;
  }

  .option-desc {
    font-size: 11px;
  }
}

@media (max-width: 576px) {
  .template-option .anticon {
    font-size: 20px;
    margin-bottom: 6px;
  }

  .option-text {
    font-size: 13px;
  }

  .option-desc {
    font-size: 10px;
  }
}
</style>