<template>
  <div class="dynamic-form">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="computedRules"
      :layout="formLayout"
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
      @finish="handleSubmit"
      @finishFailed="handleSubmitFailed"
    >
      <!-- Render field groups if configured -->
      <template v-if="hasFieldGroups">
        <a-collapse
          v-model:activeKey="activeGroups"
          :bordered="false"
          class="form-groups"
        >
          <a-collapse-panel
            v-for="group in fieldGroups"
            :key="group.id"
            :header="group.label"
          >
            <a-row :gutter="[16, 0]">
              <template v-for="field in getGroupFields(group.id)" :key="field.name">
                <a-col
                  v-show="isFieldVisible(field)"
                  :xs="24"
                  :sm="12"
                  :md="colSpan"
                >
                  <component
                    :is="getFieldComponent(field)"
                    v-bind="getFieldProps(field)"
                    v-model:value="formData[field.name]"
                    @change="handleFieldChange(field.name, $event)"
                  />
                </a-col>
              </template>
            </a-row>
          </a-collapse-panel>
        </a-collapse>
      </template>

      <!-- Render fields without groups -->
      <template v-else>
        <a-row :gutter="[16, 0]">
          <template v-for="field in visibleFields" :key="field.name">
            <a-col
              v-show="isFieldVisible(field)"
              :xs="24"
              :sm="12"
              :md="colSpan"
            >
              <a-form-item
                :name="field.name"
                :label="field.label"
                :required="isFieldRequired(field)"
                :help="field.helpText"
              >
                <component
                  :is="getFieldComponent(field)"
                  v-bind="getFieldProps(field)"
                  v-model:value="formData[field.name]"
                  @change="handleFieldChange(field.name, $event)"
                />

                <!-- Inline field validation -->
                <FieldValidation
                  v-if="enableValidation"
                  :field-name="field.name"
                  :model-value="formData[field.name]"
                  :rules="field.validationRules"
                  :form-data="formData"
                  inline
                  :show-success="showValidationSuccess"
                  @validate="handleFieldValidation(field.name, $event)"
                />
              </a-form-item>
            </a-col>
          </template>
        </a-row>
      </template>

      <!-- Form actions -->
      <a-form-item v-if="showActions" :wrapper-col="actionWrapperCol">
        <a-space>
          <a-button
            type="primary"
            html-type="submit"
            :loading="submitting"
            :disabled="!isFormValid"
          >
            {{ submitText }}
          </a-button>
          <a-button @click="handleReset">
            {{ resetText }}
          </a-button>
          <a-button v-if="showCancel" @click="handleCancel">
            {{ cancelText }}
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, h } from 'vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import FieldValidation from './FieldValidation.vue'
import type {
  FieldConfig,
  FieldType,
  FieldDependency,
  ValidationResult,
  TemplateConfig,
  ProductFormData
} from '@/types/product/template'

// Component props
interface Props {
  /** Template configuration */
  config?: TemplateConfig
  /** Field configurations */
  fields?: FieldConfig[]
  /** Initial form data */
  modelValue?: Partial<ProductFormData>
  /** Form layout */
  formLayout?: 'horizontal' | 'vertical' | 'inline'
  /** Label column span */
  labelCol?: { span: number }
  /** Wrapper column span */
  wrapperCol?: { span: number }
  /** Field column span */
  colSpan?: number
  /** Whether to show form actions */
  showActions?: boolean
  /** Submit button text */
  submitText?: string
  /** Reset button text */
  resetText?: string
  /** Cancel button text */
  cancelText?: string
  /** Whether to show cancel button */
  showCancel?: boolean
  /** Whether form is submitting */
  submitting?: boolean
  /** Whether to enable real-time validation */
  enableValidation?: boolean
  /** Whether to show validation success */
  showValidationSuccess?: boolean
  /** Whether to validate on mount */
  validateOnMount?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  formLayout: 'horizontal',
  labelCol: () => ({ span: 8 }),
  wrapperCol: () => ({ span: 16 }),
  colSpan: 12,
  showActions: true,
  submitText: '提交',
  resetText: '重置',
  cancelText: '取消',
  showCancel: false,
  submitting: false,
  enableValidation: true,
  showValidationSuccess: false,
  validateOnMount: false
})

// Component emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: Partial<ProductFormData>): void
  (e: 'submit', value: Partial<ProductFormData>): void
  (e: 'cancel'): void
  (e: 'fieldChange', fieldName: string, value: any): void
  (e: 'validate', result: Record<string, ValidationResult>): void
}>()

// Component refs
const formRef = ref<FormInstance>()

// Component state
const formData = ref<Partial<ProductFormData>>(props.modelValue || {})
const fieldValidations = ref<Record<string, ValidationResult>>({})
const activeGroups = ref<string[]>([])

// Computed properties
const allFields = computed<FieldConfig[]>(() => {
  if (props.config?.fields) {
    return props.config.fields
  }
  return props.fields || []
})

const fieldGroups = computed(() => {
  return props.config?.fieldGroups || []
})

const hasFieldGroups = computed(() => {
  return fieldGroups.value.length > 0
})

const visibleFields = computed(() => {
  return allFields.value
    .filter(field => isFieldVisible(field))
    .sort((a, b) => a.order - b.order)
})

const computedRules = computed<Record<string, Rule[]>>(() => {
  const rules: Record<string, Rule[]> = {}

  allFields.value.forEach(field => {
    const fieldRules: Rule[] = []

    // Required rule
    if (isFieldRequired(field)) {
      fieldRules.push({
        required: true,
        message: `${field.label}为必填项`,
        trigger: ['change', 'blur']
      })
    }

    // Add custom validation rules
    if (field.validationRules && field.validationRules.length > 0) {
      field.validationRules.forEach(rule => {
        fieldRules.push({
          validator: async (_rule, value) => {
            if (!value && !field.required) {
              return Promise.resolve()
            }
            // Validation logic is handled by FieldValidation component
            return Promise.resolve()
          },
          message: rule.message,
          trigger: ['change', 'blur']
        })
      })
    }

    if (fieldRules.length > 0) {
      rules[field.name] = fieldRules
    }
  })

  return rules
})

const actionWrapperCol = computed(() => {
  if (props.formLayout === 'horizontal') {
    return {
      offset: props.labelCol.span,
      span: props.wrapperCol.span
    }
  }
  return { span: 24 }
})

const isFormValid = computed(() => {
  if (!props.enableValidation) return true

  const validationResults = Object.values(fieldValidations.value)
  if (validationResults.length === 0) return true

  return validationResults.every(result => result.valid)
})

// Watch for model value changes
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue) {
      formData.value = { ...newValue }
    }
  },
  { deep: true }
)

// Watch for form data changes
watch(
  formData,
  (newValue) => {
    emit('update:modelValue', newValue)
  },
  { deep: true }
)

// Field visibility logic
const isFieldVisible = (field: FieldConfig): boolean => {
  if (field.visible === false) {
    return false
  }

  if (!field.dependencies || field.dependencies.length === 0) {
    return true
  }

  // Check all dependencies
  return field.dependencies.every(dep => evaluateDependency(dep))
}

const evaluateDependency = (dependency: FieldDependency): boolean => {
  const { dependsOn, condition, values, action } = dependency
  const fieldValue = formData.value[dependsOn]

  let conditionMet = false

  switch (condition) {
    case 'equals':
      conditionMet = values.includes(fieldValue)
      break
    case 'notEquals':
      conditionMet = !values.includes(fieldValue)
      break
    case 'in':
      conditionMet = values.includes(fieldValue)
      break
    case 'notIn':
      conditionMet = !values.includes(fieldValue)
      break
  }

  // Determine visibility based on action
  if (action === 'show') {
    return conditionMet
  } else if (action === 'hide') {
    return !conditionMet
  }

  return true
}

// Field required logic
const isFieldRequired = (field: FieldConfig): boolean => {
  if (field.required) {
    return true
  }

  // Check conditional required
  if (field.conditionalRequired) {
    const { dependsOn, values } = field.conditionalRequired
    const fieldValue = formData.value[dependsOn]
    return values.includes(fieldValue)
  }

  return false
}

// Get fields for a specific group
const getGroupFields = (groupId: string) => {
  const group = fieldGroups.value.find(g => g.id === groupId)
  if (!group) return []

  return allFields.value
    .filter(field => group.fields.includes(field.name))
    .sort((a, b) => a.order - b.order)
}

// Get field component based on field type
const getFieldComponent = (field: FieldConfig) => {
  switch (field.type) {
    case 'text':
      return 'a-input'
    case 'number':
      return 'a-input-number'
    case 'textarea':
      return 'a-textarea'
    case 'select':
      return 'a-select'
    case 'multi_select':
      return 'a-select'
    case 'date':
      return 'a-date-picker'
    case 'radio':
      return 'a-radio-group'
    case 'checkbox':
      return 'a-checkbox-group'
    default:
      return 'a-input'
  }
}

// Get field component props
const getFieldProps = (field: FieldConfig) => {
  const baseProps: any = {
    placeholder: field.placeholder || `请输入${field.label}`,
    disabled: field.disabled || false
  }

  switch (field.type) {
    case 'text':
      return {
        ...baseProps,
        maxLength: field.maxLength,
        showCount: !!field.maxLength
      }

    case 'number':
      return {
        ...baseProps,
        min: field.min,
        max: field.max,
        style: { width: '100%' }
      }

    case 'textarea':
      return {
        ...baseProps,
        maxLength: field.maxLength,
        showCount: !!field.maxLength,
        rows: 4
      }

    case 'select':
      return {
        ...baseProps,
        options: field.options,
        allowClear: true
      }

    case 'multi_select':
      return {
        ...baseProps,
        mode: 'multiple',
        options: field.options,
        allowClear: true
      }

    case 'date':
      return {
        ...baseProps,
        style: { width: '100%' },
        format: 'YYYY-MM-DD'
      }

    case 'radio':
      return {
        options: field.options
      }

    case 'checkbox':
      return {
        options: field.options
      }

    default:
      return baseProps
  }
}

// Event handlers
const handleFieldChange = (fieldName: string, value: any) => {
  formData.value[fieldName] = value
  emit('fieldChange', fieldName, value)

  // Trigger validation for dependent fields
  triggerDependentFieldValidation(fieldName)
}

const handleFieldValidation = (fieldName: string, result: ValidationResult) => {
  fieldValidations.value[fieldName] = result
  emit('validate', fieldValidations.value)
}

const triggerDependentFieldValidation = (fieldName: string) => {
  // Find fields that depend on this field
  const dependentFields = allFields.value.filter(field =>
    field.dependencies?.some(dep => dep.dependsOn === fieldName)
  )

  // Re-validate dependent fields
  dependentFields.forEach(field => {
    if (formData.value[field.name] !== undefined) {
      // Trigger re-validation
      formRef.value?.validateFields([field.name])
    }
  })
}

const handleSubmit = async () => {
  try {
    const values = await formRef.value?.validate()
    emit('submit', values || formData.value)
  } catch (error) {
    console.error('Form validation failed:', error)
  }
}

const handleSubmitFailed = (errorInfo: any) => {
  console.error('Submit failed:', errorInfo)
}

const handleReset = () => {
  formRef.value?.resetFields()
  fieldValidations.value = {}
}

const handleCancel = () => {
  emit('cancel')
}

// Public API
const validate = async () => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

const resetFields = () => {
  formRef.value?.resetFields()
  fieldValidations.value = {}
}

const setFieldsValue = (values: Partial<ProductFormData>) => {
  formData.value = { ...formData.value, ...values }
}

const getFieldsValue = () => {
  return formData.value
}

// Expose public methods
defineExpose({
  validate,
  resetFields,
  setFieldsValue,
  getFieldsValue,
  formRef
})

// Lifecycle
onMounted(() => {
  // Initialize active groups (expand all by default)
  if (hasFieldGroups.value) {
    activeGroups.value = fieldGroups.value
      .filter(group => !group.collapsed)
      .map(group => group.id)
  }

  // Validate on mount if enabled
  if (props.validateOnMount) {
    formRef.value?.validate().catch(() => {
      // Ignore validation errors on mount
    })
  }
})
</script>

<style scoped lang="scss">
.dynamic-form {
  .form-groups {
    background-color: #fafafa;

    :deep(.ant-collapse-item) {
      border-bottom: 1px solid #d9d9d9;

      &:last-child {
        border-bottom: none;
      }
    }

    :deep(.ant-collapse-header) {
      padding: 16px;
      font-weight: 600;
      font-size: 15px;
      background-color: #fff;
    }

    :deep(.ant-collapse-content-box) {
      padding: 24px 16px;
      background-color: #fafafa;
    }
  }

  :deep(.ant-form-item) {
    margin-bottom: 20px;

    .ant-form-item-label {
      label {
        font-weight: 500;
      }
    }
  }

  :deep(.ant-input),
  :deep(.ant-input-number),
  :deep(.ant-select),
  :deep(.ant-picker) {
    width: 100%;
  }

  :deep(.ant-form-item-explain-error) {
    font-size: 13px;
  }
}

// Responsive design
@media (max-width: 768px) {
  .dynamic-form {
    :deep(.ant-form) {
      .ant-form-item-label {
        padding-bottom: 4px;
      }
    }
  }
}
</style>