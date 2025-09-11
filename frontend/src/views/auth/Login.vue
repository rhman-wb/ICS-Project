<template>
  <div class="login-container">
    <div class="login-panel">
      <div class="system-header">
        <div class="logo-container">
          <img src="/favicon.ico" alt="Logo" class="logo" />
        </div>
        <h1 class="system-title">保险产品智能检核系统</h1>
      </div>
      
      <div class="login-form">
        <!-- 错误提示区域 -->
        <transition name="error-fade" appear>
          <div v-if="errorState.visible" class="error-alert-container">
            <a-alert
              :message="errorState.message"
              type="error"
              show-icon
              closable
              @close="clearError"
              class="error-alert"
            >
              <template #icon>
                <ExclamationCircleOutlined class="error-icon" />
              </template>
            </a-alert>
          </div>
        </transition>
        
        <a-form
          ref="formRef"
          :model="loginForm"
          @finish="handleLogin"
          @keyup.enter="handleEnterSubmit"
        >
          <a-form-item
            name="username"
            :rules="usernameRules"
          >
            <a-input
              ref="usernameInputRef"
              v-model:value="loginForm.username"
              placeholder="请输入用户账号"
              size="large"
              allow-clear
              maxlength="20"
              :show-count="false"
              tabindex="1"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
              @input="handleInputChange"
            >
              <template #prefix>
                <UserOutlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>
          
          <a-form-item
            name="password"
            :rules="passwordRules"
          >
            <a-input-password
              ref="passwordInputRef"
              v-model:value="loginForm.password"
              placeholder="请输入登录密码"
              size="large"
              allow-clear
              maxlength="20"
              :show-count="false"
              tabindex="2"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
              @input="handleInputChange"
              @keyup.enter="handleEnterSubmit"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>
          
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="buttonState.loading"
              :disabled="isButtonDisabled"
              tabindex="3"
              class="login-button"
              @click="handleButtonClick"
            >
              {{ buttonText }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
    
    <div class="footer">
      <p>&copy; {{ currentYear }} 保险产品智能检核系统. All rights reserved.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { handleLoginRedirect } from '@/router/guards/auth'
import { ApiError, ApiErrorType } from '@/utils/apiError'
import { UserOutlined, LockOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import type { FormInstance } from 'ant-design-vue'

defineOptions({
  name: 'Login'
})
// 路由实例（顶层初始化，避免在异步回调中获取失败）
const router = useRouter()
const route = useRoute()

interface LoginForm {
  username: string
  password: string
}

// 错误类型枚举
enum ErrorType {
  VALIDATION = 'validation',
  AUTH_FAILED = 'auth',
  NETWORK_ERROR = 'network',
  SERVER_ERROR = 'server',
  ACCOUNT_LOCKED = 'locked',
  UNKNOWN = 'unknown'
}

interface ErrorState {
  visible: boolean
  message: string
  type: ErrorType
}

const loginForm = ref<LoginForm>({
  username: '',
  password: ''
})

const loginLoading = ref(false)
const formRef = ref<FormInstance>()
const usernameInputRef = ref()
const passwordInputRef = ref()

// 错误状态管理
const errorState = ref<ErrorState>({
  visible: false,
  message: '',
  type: ErrorType.VALIDATION
})

// 错误自动消失定时器
let errorTimer: NodeJS.Timeout | null = null

// 获取当前年份
const currentYear = new Date().getFullYear()

// 错误信息映射
const errorMessages: Record<ErrorType, Record<string, string>> = {
  [ErrorType.VALIDATION]: {
    username_empty: '请填写用户账号',
    password_empty: '请填写登录密码',
    username_invalid: '用户账号格式不正确',
    password_invalid: '密码格式不正确'
  },
  [ErrorType.AUTH_FAILED]: {
    invalid_credentials: '用户名或密码不正确',
    user_not_found: '用户不存在',
    password_incorrect: '密码错误'
  },
  [ErrorType.ACCOUNT_LOCKED]: {
    account_locked: '账户已被锁定，请30分钟后重试',
    too_many_attempts: '登录失败次数过多，账户已被锁定'
  },
  [ErrorType.NETWORK_ERROR]: {
    network_timeout: '网络连接超时，请检查网络后重试',
    network_error: '网络连接异常，请检查网络后重试',
    connection_failed: '无法连接到服务器，请稍后重试'
  },
  [ErrorType.SERVER_ERROR]: {
    internal_error: '服务器内部错误，请稍后重试',
    service_unavailable: '服务暂时不可用，请稍后重试',
    maintenance: '系统正在维护中，请稍后重试'
  },
  [ErrorType.UNKNOWN]: {
    unknown_error: '未知错误，请稍后重试'
  }
}

// 登录按钮状态管理
const buttonState = ref({
  loading: false,
  disabled: false,
  lastClickTime: 0
})

const usernameRules: Rule[] = [
  { required: true, message: '请输入用户账号', trigger: 'blur' },
  { min: 2, max: 20, message: '用户账号长度为2-20个字符', trigger: 'blur' },
  { 
    pattern: /^[a-zA-Z0-9_-]+$/, 
    message: '用户账号只能包含字母、数字、下划线和连字符', 
    trigger: 'blur' 
  }
]

const passwordRules: Rule[] = [
  { required: true, message: '请输入登录密码', trigger: 'blur' },
  { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
]

// 表单验证状态
const isFormValid = computed(() => {
  return loginForm.value.username.trim().length > 0 && 
         loginForm.value.password.length > 0
})

// 按钮禁用状态逻辑
const isButtonDisabled = computed(() => {
  return !isFormValid.value || buttonState.value.loading || buttonState.value.disabled
})

// 按钮文字动态变化
const buttonText = computed(() => {
  return buttonState.value.loading ? '登录中...' : '登录'
})

// 防重复点击保护机制
const canClick = computed(() => {
  const now = Date.now()
  const timeSinceLastClick = now - buttonState.value.lastClickTime
  return timeSinceLastClick > 1000 // 1秒内只能点击一次
})

// 按钮状态重置方法
const resetButtonState = () => {
  buttonState.value.loading = false
  buttonState.value.disabled = false
  loginLoading.value = false
}

// 设置按钮加载状态
const setButtonLoading = (loading: boolean) => {
  buttonState.value.loading = loading
  buttonState.value.disabled = loading
  loginLoading.value = loading
}

// 显示错误提示
const showError = (type: ErrorType, key: string, customMessage?: string) => {
  // 清除之前的定时器
  if (errorTimer) {
    clearTimeout(errorTimer)
    errorTimer = null
  }
  
  // 获取错误信息
  const errorMessage = customMessage || 
    errorMessages[type]?.[key] || 
    errorMessages[ErrorType.UNKNOWN].unknown_error
  
  // 设置错误状态
  errorState.value = {
    visible: true,
    message: errorMessage,
    type
  }
  
  // 同时显示全局错误提示（用于网络和服务器错误）
  if (type === ErrorType.NETWORK_ERROR || type === ErrorType.SERVER_ERROR) {
    message.error(errorMessage)
  }
  
  // 设置自动消失定时器（5秒后自动消失）
  errorTimer = setTimeout(() => {
    clearError()
  }, 5000)
}

// 清除错误提示
const clearError = () => {
  errorState.value.visible = false
  errorState.value.message = ''
  
  if (errorTimer) {
    clearTimeout(errorTimer)
    errorTimer = null
  }
}

// 处理不同类型的错误
const handleLoginError = (error: any) => {
  console.error('Login error:', error)
  
  // 优先处理结构化 ApiError
  if (error instanceof ApiError) {
    // 业务错误码（后端 ErrorCode 枚举）
    if (typeof error.code === 'number') {
      switch (error.code) {
        case 1003: // INVALID_CREDENTIALS
          showError(ErrorType.AUTH_FAILED, 'invalid_credentials')
          return
        case 1005: // USER_LOCKED
          showError(ErrorType.ACCOUNT_LOCKED, 'account_locked')
          return
        case 400:
          showError(ErrorType.VALIDATION, 'username_invalid')
          return
        case 401:
          showError(ErrorType.AUTH_FAILED, 'invalid_credentials')
          return
      }
    }
    // 网络/超时/服务器错误
    if (error.type === ApiErrorType.NETWORK_ERROR || error.type === ApiErrorType.TIMEOUT_ERROR) {
      showError(ErrorType.NETWORK_ERROR, 'network_error')
      return
    }
    if (error.type === ApiErrorType.SERVER_ERROR) {
      showError(ErrorType.SERVER_ERROR, 'internal_error')
      return
    }
    // 其他业务错误
    showError(ErrorType.UNKNOWN, 'unknown_error', error.message)
    return
  }

  // 非结构化错误的兜底处理
  if (error?.message) {
    if (error.message.includes('timeout') || error.message.includes('网络')) {
      showError(ErrorType.NETWORK_ERROR, 'network_error')
      return
    }
    if (error.message.includes('500') || error.message.includes('服务器')) {
      showError(ErrorType.SERVER_ERROR, 'internal_error')
      return
    }
    showError(ErrorType.UNKNOWN, 'unknown_error', error.message)
  } else {
    showError(ErrorType.UNKNOWN, 'unknown_error')
  }
}

// 验证表单并显示验证错误
const validateFormAndShowErrors = (): boolean => {
  const { username, password } = loginForm.value
  
  // 检查用户名
  if (!username || username.trim().length === 0) {
    showError(ErrorType.VALIDATION, 'username_empty')
    return false
  }
  
  // 检查密码
  if (!password || password.length === 0) {
    showError(ErrorType.VALIDATION, 'password_empty')
    return false
  }
  
  // 检查用户名格式
  if (username.length < 2 || username.length > 20) {
    showError(ErrorType.VALIDATION, 'username_invalid', '用户账号长度为2-20个字符')
    return false
  }
  
  // 检查密码格式
  if (password.length < 6 || password.length > 20) {
    showError(ErrorType.VALIDATION, 'password_invalid', '密码长度为6-20个字符')
    return false
  }
  
  return true
}

// 处理输入框焦点事件
const handleInputFocus = (event: FocusEvent) => {
  const target = event.target as HTMLInputElement
  const inputWrapper = target.closest('.ant-input-affix-wrapper')
  if (inputWrapper) {
    inputWrapper.classList.add('input-focused')
  }
  
  // 用户开始输入时清除错误提示
  if (errorState.value.visible) {
    clearError()
  }
}

// 处理输入变化事件
const handleInputChange = () => {
  // 用户开始输入时清除错误提示
  if (errorState.value.visible) {
    clearError()
  }
}

const handleInputBlur = (event: FocusEvent) => {
  const target = event.target as HTMLInputElement
  const inputWrapper = target.closest('.ant-input-affix-wrapper')
  if (inputWrapper) {
    inputWrapper.classList.remove('input-focused')
  }
}

// 处理按钮点击事件
const handleButtonClick = (event: Event) => {
  // 防重复点击保护
  if (!canClick.value) {
    event.preventDefault()
    event.stopPropagation()
    console.log('Button click ignored due to rate limiting')
    return
  }
  
  // 更新最后点击时间
  buttonState.value.lastClickTime = Date.now()
  
  // 如果按钮被禁用，阻止提交
  if (isButtonDisabled.value) {
    event.preventDefault()
    event.stopPropagation()
    return
  }
}

// 处理Enter键提交
const handleEnterSubmit = async () => {
  if (!isFormValid.value || buttonState.value.loading) {
    return
  }
  
  // 检查防重复点击
  if (!canClick.value) {
    console.log('Enter submit ignored due to rate limiting')
    return
  }
  
  try {
    await formRef.value?.validate()
    await handleLogin(loginForm.value)
  } catch (error) {
    console.log('Form validation failed:', error)
    handleLoginError(error)
  }
}

const handleLogin = async (values: LoginForm) => {
  try {
    // 清除之前的错误提示
    clearError()
    
    // 验证表单
    if (!validateFormAndShowErrors()) {
      return
    }
    
    // 设置按钮加载状态
    setButtonLoading(true)
    
    console.log('Login form values:', values)
    
    // 使用auth store进行登录
    const { useAuthStore } = await import('@/stores/modules/auth')
    const authStore = useAuthStore()
    
    await authStore.login({
      username: values.username.trim(),
      password: values.password
    })
    
    console.log('Login successful')
    message.success('登录成功')
    
    // 登录成功后跳转（支持 redirect 参数）
    const redirect = route?.query?.redirect as string
    handleLoginRedirect(router, redirect)
    
  } catch (error) {
    console.error('Login failed:', error)
    handleLoginError(error)
  } finally {
    // 重置按钮状态
    resetButtonState()
  }
}

// 监听表单值变化，自动清除错误
watch(
  () => [loginForm.value.username, loginForm.value.password],
  () => {
    if (errorState.value.visible && errorState.value.type === ErrorType.VALIDATION) {
      clearError()
    }
  },
  { deep: true }
)

// 组件挂载后自动聚焦到用户名输入框
onMounted(async () => {
  await nextTick()
  if (usernameInputRef.value) {
    usernameInputRef.value.focus()
  }
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (errorTimer) {
    clearTimeout(errorTimer)
    errorTimer = null
  }
})
</script>

<style lang="scss" scoped>
@use "@/assets/styles/globals.scss" as *;

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #764ba2 100%);
  @include flex-center;
  flex-direction: column;
  position: relative;
  padding: var(--spacing-lg);
}

.login-panel {
  background: #fff;
  border-radius: var(--border-radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: var(--spacing-xl);
  width: 400px;
  max-width: 100%;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }
}

.system-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.logo-container {
  margin-bottom: var(--spacing-md);
}

.logo {
  width: 64px;
  height: 64px;
  object-fit: contain;
}

.system-title {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.2;
}

.login-form {
  // 错误提示区域
  .error-alert-container {
    margin-bottom: var(--spacing-lg);
  }
  
  .error-alert {
    border-radius: var(--border-radius-base);
    border: 1px solid #ffccc7;
    background-color: #fff2f0;
    
    :deep(.ant-alert-message) {
      color: var(--color-error);
      font-size: var(--font-size-base);
      font-weight: 400;
    }
    
    :deep(.ant-alert-icon) {
      color: var(--color-error);
      font-size: 16px;
      margin-top: 1px;
    }
    
    .error-icon {
      color: var(--color-error);
    }
    
    :deep(.ant-alert-close-icon) {
      color: var(--color-text-disabled);
      transition: color 0.3s ease;
      
      &:hover {
        color: var(--color-error);
      }
    }
    
    // 错误提示动画效果
    animation: errorShake 0.5s ease-in-out;
  }
  
  // 表单项间距
  :deep(.ant-form-item) {
    margin-bottom: var(--spacing-lg);
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  // 输入框样式增强
  :deep(.ant-input-affix-wrapper) {
    border: 1px solid var(--color-border);
    border-radius: var(--border-radius-base);
    transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
    
    &:hover {
      border-color: var(--color-primary-hover);
    }
    
    &:focus-within,
    &.ant-input-affix-wrapper-focused {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      outline: none;
    }
    
    // 自定义焦点状态类
    &.input-focused {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }
  
  // 普通输入框样式
  :deep(.ant-input) {
    border: none;
    box-shadow: none;
    font-size: var(--font-size-base);
    
    &:focus {
      border: none;
      box-shadow: none;
    }
    
    &::placeholder {
      color: var(--color-text-disabled);
      font-size: var(--font-size-base);
    }
  }
  
  // 密码输入框特殊处理
  :deep(.ant-input-password) {
    .ant-input {
      border: none;
      box-shadow: none;
      
      &:focus {
        border: none;
        box-shadow: none;
      }
    }
  }
  
  // 图标样式
  .input-icon {
    color: var(--color-text-secondary);
    font-size: 16px;
    transition: color 0.3s ease;
  }
  
  :deep(.ant-input-affix-wrapper:focus-within) .input-icon,
  :deep(.ant-input-affix-wrapper.input-focused) .input-icon {
    color: var(--color-primary);
  }
  
  // 清除按钮样式
  :deep(.ant-input-clear-icon) {
    color: var(--color-text-disabled);
    transition: color 0.3s ease;
    
    &:hover {
      color: var(--color-text-secondary);
    }
  }
  
  // 密码显示/隐藏按钮
  :deep(.ant-input-password-icon) {
    color: var(--color-text-disabled);
    transition: color 0.3s ease;
    
    &:hover {
      color: var(--color-text-secondary);
    }
  }
  
  // 登录按钮样式
  .login-button {
    &:deep(.ant-btn-primary) {
      background-color: var(--color-primary);
      border-color: var(--color-primary);
      font-weight: 500;
      height: 40px;
      font-size: var(--font-size-base);
      transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
      position: relative;
      overflow: hidden;
      
      // 悬停效果
      &:hover:not(:disabled):not(.ant-btn-loading) {
        background-color: var(--color-primary-hover);
        border-color: var(--color-primary-hover);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
      }
      
      // 点击效果
      &:active:not(:disabled):not(.ant-btn-loading) {
        background-color: var(--color-primary-active);
        border-color: var(--color-primary-active);
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
      }
      
      // 禁用状态
      &:disabled {
        background-color: var(--color-text-disabled);
        border-color: var(--color-text-disabled);
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
        
        &:hover {
          background-color: var(--color-text-disabled);
          border-color: var(--color-text-disabled);
          transform: none;
          box-shadow: none;
        }
      }
      
      // 加载状态
      &.ant-btn-loading {
        background-color: var(--color-primary);
        border-color: var(--color-primary);
        cursor: wait;
        
        &:hover {
          background-color: var(--color-primary);
          border-color: var(--color-primary);
          transform: none;
          box-shadow: none;
        }
      }
      
      // 按钮内容动画
      .ant-btn-loading-icon {
        margin-right: 8px;
      }
      
      // 点击波纹效果
      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 0;
        height: 0;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.3);
        transform: translate(-50%, -50%);
        transition: width 0.6s, height 0.6s;
      }
      
      &:active:not(:disabled):not(.ant-btn-loading)::before {
        width: 300px;
        height: 300px;
      }
    }
  }
  
  // 增强的按钮状态样式
  :deep(.ant-btn-primary) {
    // 焦点状态
    &:focus-visible {
      outline: 2px solid var(--color-primary);
      outline-offset: 2px;
    }
    
    // 文字选择禁用
    user-select: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
  }
  
  // 表单验证错误状态
  :deep(.ant-form-item-has-error) {
    .ant-input-affix-wrapper {
      border-color: var(--color-error);
      
      &:hover,
      &:focus-within,
      &.input-focused {
        border-color: var(--color-error);
        box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.2);
      }
    }
  }
  
  // 错误提示文字
  :deep(.ant-form-item-explain-error) {
    font-size: var(--font-size-sm);
    color: var(--color-error);
    margin-top: var(--spacing-xs);
  }
}

.footer {
  position: absolute;
  bottom: var(--spacing-lg);
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  
  p {
    color: rgba(255, 255, 255, 0.8);
    font-size: var(--font-size-sm);
    margin: 0;
  }
}

// 响应式设计
@include respond-to(xs) {
  .login-container {
    padding: var(--spacing-md);
  }
  
  .login-panel {
    padding: var(--spacing-lg);
    width: 100%;
    max-width: 320px;
  }
  
  .system-title {
    font-size: var(--font-size-xl);
  }
  
  .logo {
    width: 48px;
    height: 48px;
  }
  
  .footer {
    bottom: var(--spacing-md);
    
    p {
      font-size: var(--font-size-xs);
    }
  }
  
  .login-form {
    :deep(.ant-form-item) {
      margin-bottom: var(--spacing-md);
    }
  }
}

@include respond-to(sm) {
  .login-panel {
    width: 360px;
  }
}

@include respond-to(md) {
  .login-panel {
    width: 400px;
    padding: var(--spacing-xl);
  }
  
  .system-title {
    font-size: var(--font-size-xxl);
  }
  
  .logo {
    width: 64px;
    height: 64px;
  }
}

@include respond-to(lg) {
  .login-container {
    padding: var(--spacing-xl);
  }
  
  .login-panel {
    width: 420px;
  }
}

// 平板设备适配
@media (min-width: 768px) and (max-width: 1024px) {
  .login-panel {
    width: 380px;
  }
}

// 大屏幕设备
@media (min-width: 1200px) {
  .login-panel {
    width: 440px;
  }
}

// 键盘导航支持
:deep(.ant-input-affix-wrapper:focus-within),
:deep(.ant-btn:focus-visible) {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

// 高对比度模式支持
@media (prefers-contrast: high) {
  .login-form {
    :deep(.ant-input-affix-wrapper) {
      border-width: 2px;
      
      &:focus-within,
      &.input-focused {
        border-width: 2px;
        box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.3);
      }
    }
  }
}

// 减少动画模式支持
@media (prefers-reduced-motion: reduce) {
  .login-panel,
  .login-form :deep(.ant-input-affix-wrapper),
  .login-form :deep(.ant-btn-primary),
  .login-form .login-button :deep(.ant-btn-primary) {
    transition: none;
    
    &::before {
      transition: none;
    }
  }
}

// 按钮交互增强
.login-form .login-button {
  // 按钮容器样式
  position: relative;
  
  // 按钮点击反馈
  &:active {
    transform: scale(0.98);
  }
  
  // 键盘导航支持
  :deep(.ant-btn-primary:focus-visible) {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
  }
  
  // 触摸设备优化
  @media (hover: none) and (pointer: coarse) {
    :deep(.ant-btn-primary) {
      &:hover {
        transform: none;
        box-shadow: none;
      }
      
      &:active:not(:disabled):not(.ant-btn-loading) {
        transform: scale(0.95);
        background-color: var(--color-primary-active);
      }
    }
  }
}

// 错误提示淡入淡出动画
.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.error-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.error-fade-enter-to,
.error-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

// 错误提示震动动画
@keyframes errorShake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-2px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(2px);
  }
}

// 错误状态下输入框样式增强
.login-form {
  :deep(.ant-form-item-has-error) {
    .ant-input-affix-wrapper {
      border-color: var(--color-error);
      animation: inputErrorPulse 0.3s ease-in-out;
      
      &:hover,
      &:focus-within,
      &.input-focused {
        border-color: var(--color-error);
        box-shadow: 0 0 0 2px rgba(245, 34, 45, 0.2);
      }
      
      .input-icon {
        color: var(--color-error);
      }
    }
  }
}

// 输入框错误状态脉冲动画
@keyframes inputErrorPulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 34, 45, 0.4);
  }
  70% {
    box-shadow: 0 0 0 4px rgba(245, 34, 45, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 34, 45, 0);
  }
}

// 高对比度模式下的错误提示
@media (prefers-contrast: high) {
  .login-form {
    .error-alert {
      border-width: 2px;
      border-color: var(--color-error);
      
      :deep(.ant-alert-message) {
        font-weight: 600;
      }
    }
    
    :deep(.ant-form-item-has-error) {
      .ant-input-affix-wrapper {
        border-width: 2px;
        
        &:focus-within,
        &.input-focused {
          border-width: 2px;
          box-shadow: 0 0 0 3px rgba(245, 34, 45, 0.3);
        }
      }
    }
  }
}

// 减少动画模式下禁用动画
@media (prefers-reduced-motion: reduce) {
  .error-fade-enter-active,
  .error-fade-leave-active {
    transition: none;
  }
  
  .error-fade-enter-from,
  .error-fade-leave-to {
    transform: none;
  }
  
  .login-form .error-alert {
    animation: none;
  }
  
  .login-form :deep(.ant-form-item-has-error) .ant-input-affix-wrapper {
    animation: none;
  }
}

// 触摸设备优化
@media (hover: none) and (pointer: coarse) {
  .login-form .error-alert {
    :deep(.ant-alert-close-icon) {
      padding: 8px;
      margin: -8px;
    }
  }
}
</style>