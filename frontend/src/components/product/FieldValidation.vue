<template>
  <div class="field-validation">
    <!-- Validation Error Display -->
    <transition name="slide-fade">
      <div v-if="hasErrors" class="validation-errors">
        <a-alert
          v-for="(error, index) in errors"
          :key="index"
          :message="error"
          type="error"
          show-icon
          closable
          class="error-item"
          @close="handleErrorClose(index)"
        >
          <template #icon>
            <ExclamationCircleOutlined />
          </template>
        </a-alert>
      </div>
    </transition>

    <!-- Validation Warning Display -->
    <transition name="slide-fade">
      <div v-if="hasWarnings" class="validation-warnings">
        <a-alert
          v-for="(warning, index) in warnings"
          :key="index"
          :message="warning"
          type="warning"
          show-icon
          closable
          class="warning-item"
          @close="handleWarningClose(index)"
        >
          <template #icon>
            <WarningOutlined />
          </template>
        </a-alert>
      </div>
    </transition>

    <!-- Validation Success Display -->
    <transition name="slide-fade">
      <div v-if="showSuccess && isValid && !hasErrors && !hasWarnings" class="validation-success">
        <a-alert
          message="验证通过"
          type="success"
          show-icon
          class="success-item"
        >
          <template #icon>
            <CheckCircleOutlined />
          </template>
        </a-alert>
      </div>
    </transition>

    <!-- Real-time Field Validation Feedback -->
    <div v-if="inline && fieldName" class="inline-validation">
      <span v-if="hasErrors" class="inline-error">
        <ExclamationCircleOutlined class="icon" />
        <span class="message">{{ errors[0] }}</span>
      </span>
      <span v-else-if="hasWarnings" class="inline-warning">
        <WarningOutlined class="icon" />
        <span class="message">{{ warnings[0] }}</span>
      </span>
      <span v-else-if="showSuccess && isValid" class="inline-success">
        <CheckCircleOutlined class="icon" />
        <span class="message">验证通过</span>
      </span>
    </div>

    <!-- Field-level Help Text -->
    <div v-if="helpText" class="field-help">
      <a-tooltip :title="helpText">
        <InfoCircleOutlined class="help-icon" />
      </a-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import {
  ExclamationCircleOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import type {
  ValidationResult,
  ValidationRule,
  ValidationRuleType
} from '@/types/product/template'
/**
 * Normalize validation rule type to enum
 * 将验证规则类型标准化为枚举值
 */
const normalizeRuleType = (type: string | ValidationRuleType): ValidationRuleType => {
  if (Object.values(ValidationRuleType).includes(type as ValidationRuleType)) {
    return type as ValidationRuleType
  }
  // 默认返回custom类型
  return ValidationRuleType.CUSTOM
}

// Component props
interface Props {
  /** Field name being validated */
  fieldName?: string
  /** Field value to validate */
  modelValue?: any
  /** Validation rules to apply */
  rules?: ValidationRule[]
  /** Whether to show inline validation (vs. block display) */
  inline?: boolean
  /** Whether to show success state */
  showSuccess?: boolean
  /** Help text for the field */
  helpText?: string
  /** Whether to validate immediately on mount */
  validateOnMount?: boolean
  /** Debounce delay for validation (ms) */
  debounce?: number
  /** Form data for context-dependent validation */
  formData?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  inline: false,
  showSuccess: false,
  validateOnMount: false,
  debounce: 300
})

// Component emits
const emit = defineEmits<{
  (e: 'update:validationResult', result: ValidationResult): void
  (e: 'validate', result: ValidationResult): void
}>()

// Component state
const errors = ref<string[]>([])
const warnings = ref<string[]>([])
const isValid = ref(true)
const validating = ref(false)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

// Computed properties
const hasErrors = computed(() => errors.value.length > 0)
const hasWarnings = computed(() => warnings.value.length > 0)
const validationResult = computed<ValidationResult>(() => ({
  valid: isValid.value,
  errors: errors.value,
  warnings: warnings.value
}))

// Watch for value changes and validate
watch(
  () => props.modelValue,
  (newValue) => {
    if (debounceTimer) {
      clearTimeout(debounceTimer)
    }

    debounceTimer = setTimeout(() => {
      validateField(newValue)
    }, props.debounce)
  },
  { deep: true }
)

// Watch for rules changes
watch(
  () => props.rules,
  () => {
    if (props.modelValue !== undefined && props.modelValue !== null) {
      validateField(props.modelValue)
    }
  },
  { deep: true }
)

// Validation functions
const validateField = async (value: any) => {
  if (!props.rules || props.rules.length === 0) {
    // No rules, consider valid
    setValidationState(true, [], [])
    return
  }

  validating.value = true
  const fieldErrors: string[] = []
  const fieldWarnings: string[] = []

  try {
    for (const rule of props.rules) {
      const result = await validateRule(rule, value)
      if (!result.valid) {
        fieldErrors.push(result.message)
      }
    }

    const valid = fieldErrors.length === 0
    setValidationState(valid, fieldErrors, fieldWarnings)
  } catch (error) {
    console.error('Validation error:', error)
    fieldErrors.push('验证过程中发生错误')
    setValidationState(false, fieldErrors, fieldWarnings)
  } finally {
    validating.value = false
  }
}

const validateRule = async (
  rule: ValidationRule,
  value: any
): Promise<{ valid: boolean; message: string }> => {
  const normalizedType = normalizeRuleType(rule.type)
  const { value: ruleValue, message } = rule

  switch (normalizedType) {
    case 'required':
      return {
        valid: validateRequired(value),
        message: message || '此字段为必填项'
      }

    case 'minLength':
      return {
        valid: validateMinLength(value, ruleValue),
        message: message || `最小长度为 ${ruleValue} 个字符`
      }

    case 'maxLength':
      return {
        valid: validateMaxLength(value, ruleValue),
        message: message || `最大长度为 ${ruleValue} 个字符`
      }

    case 'minValue':
      return {
        valid: validateMinValue(value, ruleValue),
        message: message || `最小值为 ${ruleValue}`
      }

    case 'maxValue':
      return {
        valid: validateMaxValue(value, ruleValue),
        message: message || `最大值为 ${ruleValue}`
      }

    case 'pattern':
      return {
        valid: validatePattern(value, ruleValue),
        message: message || '格式不正确'
      }

    case 'phone':
      return {
        valid: validatePhone(value),
        message: message || '请输入有效的手机号码'
      }

    case 'email':
      return {
        valid: validateEmail(value),
        message: message || '请输入有效的邮箱地址'
      }

    case 'url':
      return {
        valid: validateUrl(value),
        message: message || '请输入有效的URL地址'
      }

    case 'numeric':
      return {
        valid: validateNumeric(value),
        message: message || '请输入数字'
      }

    case 'alpha':
      return {
        valid: validateAlpha(value),
        message: message || '只能包含字母'
      }

    case 'alphanumeric':
      return {
        valid: validateAlphanumeric(value),
        message: message || '只能包含字母和数字'
      }

    case 'custom':
      // Custom validation would require a validator function
      // For now, return valid
      return { valid: true, message: '' }

    default:
      return { valid: true, message: '' }
  }
}

// Individual validation functions
const validateRequired = (value: any): boolean => {
  if (value === null || value === undefined) return false
  if (typeof value === 'string') return value.trim().length > 0
  if (Array.isArray(value)) return value.length > 0
  return true
}

const validateMinLength = (value: any, minLength: number): boolean => {
  if (!value) return true // Optional field
  return String(value).length >= minLength
}

const validateMaxLength = (value: any, maxLength: number): boolean => {
  if (!value) return true // Optional field
  return String(value).length <= maxLength
}

const validateMinValue = (value: any, minValue: number): boolean => {
  if (value === null || value === undefined || value === '') return true
  return Number(value) >= minValue
}

const validateMaxValue = (value: any, maxValue: number): boolean => {
  if (value === null || value === undefined || value === '') return true
  return Number(value) <= maxValue
}

const validatePattern = (value: any, pattern: string): boolean => {
  if (!value) return true // Optional field
  const regex = new RegExp(pattern)
  return regex.test(String(value))
}

const validatePhone = (value: any): boolean => {
  if (!value) return true // Optional field
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(String(value))
}

const validateEmail = (value: any): boolean => {
  if (!value) return true // Optional field
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(String(value))
}

const validateUrl = (value: any): boolean => {
  if (!value) return true // Optional field
  try {
    new URL(String(value))
    return true
  } catch {
    return false
  }
}

const validateNumeric = (value: any): boolean => {
  if (value === null || value === undefined || value === '') return true
  return !isNaN(Number(value))
}

const validateAlpha = (value: any): boolean => {
  if (!value) return true // Optional field
  const alphaRegex = /^[a-zA-Z]+$/
  return alphaRegex.test(String(value))
}

const validateAlphanumeric = (value: any): boolean => {
  if (!value) return true // Optional field
  const alphanumericRegex = /^[a-zA-Z0-9]+$/
  return alphanumericRegex.test(String(value))
}

// Set validation state
const setValidationState = (
  valid: boolean,
  errorMessages: string[],
  warningMessages: string[]
) => {
  isValid.value = valid
  errors.value = errorMessages
  warnings.value = warningMessages

  const result = validationResult.value
  emit('update:validationResult', result)
  emit('validate', result)
}

// Error/warning close handlers
const handleErrorClose = (index: number) => {
  errors.value.splice(index, 1)
}

const handleWarningClose = (index: number) => {
  warnings.value.splice(index, 1)
}

// Public API - validate method
const validate = async () => {
  await validateField(props.modelValue)
  return validationResult.value
}

// Public API - reset method
const reset = () => {
  setValidationState(true, [], [])
}

// Expose public methods
defineExpose({
  validate,
  reset,
  isValid: computed(() => isValid.value),
  errors: computed(() => errors.value),
  warnings: computed(() => warnings.value)
})

// Lifecycle
onMounted(() => {
  if (props.validateOnMount && props.modelValue !== undefined) {
    validateField(props.modelValue)
  }
})
</script>

<style scoped lang="scss">
.field-validation {
  position: relative;

  // Block validation display
  .validation-errors,
  .validation-warnings,
  .validation-success {
    margin-bottom: 12px;

    .error-item,
    .warning-item,
    .success-item {
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  // Inline validation display
  .inline-validation {
    display: inline-flex;
    align-items: center;
    margin-left: 8px;
    font-size: 14px;

    .icon {
      margin-right: 4px;
    }

    .inline-error {
      color: #ff4d4f;
      display: flex;
      align-items: center;
    }

    .inline-warning {
      color: #faad14;
      display: flex;
      align-items: center;
    }

    .inline-success {
      color: #52c41a;
      display: flex;
      align-items: center;
    }

    .message {
      font-size: 13px;
    }
  }

  // Field help icon
  .field-help {
    display: inline-block;
    margin-left: 4px;

    .help-icon {
      color: #8c8c8c;
      cursor: help;
      font-size: 14px;

      &:hover {
        color: #1890ff;
      }
    }
  }
}

// Animations
.slide-fade-enter-active {
  transition: all 0.3s ease;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

// Responsive design
@media (max-width: 768px) {
  .field-validation {
    .inline-validation {
      display: block;
      margin-left: 0;
      margin-top: 4px;

      .message {
        font-size: 12px;
      }
    }
  }
}
</style>