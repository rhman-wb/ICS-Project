import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { message } from 'ant-design-vue'
import LoginForm from '@/views/auth/Login.vue'
import { useAuthStore } from '@/stores/modules/auth'

// Mock ant-design-vue message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue')
  return {
    ...actual,
    message: {
      error: vi.fn(),
      success: vi.fn(),
      loading: vi.fn()
    }
  }
})

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush
    }),
    useRoute: () => ({
      query: {}
    })
  }
})

describe('LoginForm', () => {
  let wrapper: VueWrapper
  let authStore: ReturnType<typeof useAuthStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    
    wrapper = mount(LoginForm, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'router-link': true,
          'a-form': {
            template: '<form @submit.prevent="$emit(\'finish\', $attrs.model)"><slot /></form>'
          },
          'a-form-item': {
            template: '<div><slot /></div>'
          },
          'a-input': {
            template: '<input v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-input-password': {
            template: '<input type="password" v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-button': {
            template: '<button :disabled="disabled" :loading="loading" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'loading']
          },
          'a-alert': {
            template: '<div class="error-alert"><slot /></div>'
          }
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form correctly', () => {
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('displays system title correctly', () => {
    const title = wrapper.find('.system-title')
    expect(title.exists()).toBe(true)
    expect(title.text()).toContain('保险产品智能检核系统')
  })

  it('updates form data when user types', async () => {
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    expect(wrapper.vm.loginForm.username).toBe('testuser')
    expect(wrapper.vm.loginForm.password).toBe('password123')
  })

  it('disables login button when form is invalid', async () => {
    const loginButton = wrapper.find('button')
    
    // Initially should be disabled (empty form)
    expect(loginButton.attributes('disabled')).toBeDefined()

    // Fill only username
    const usernameInput = wrapper.find('input[type="text"]')
    await usernameInput.setValue('testuser')
    await wrapper.vm.$nextTick()
    
    // Should still be disabled
    expect(loginButton.attributes('disabled')).toBeDefined()

    // Fill both username and password
    const passwordInput = wrapper.find('input[type="password"]')
    await passwordInput.setValue('password123')
    await wrapper.vm.$nextTick()
    
    // Should be enabled
    expect(loginButton.attributes('disabled')).toBeUndefined()
  })

  it('shows loading state during login', async () => {
    // Mock login to return a pending promise
    const loginPromise = new Promise(resolve => setTimeout(resolve, 100))
    vi.spyOn(authStore, 'login').mockReturnValue(loginPromise)

    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')
    const loginButton = wrapper.find('button')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')
    
    // Trigger login
    await loginButton.trigger('click')
    await wrapper.vm.$nextTick()

    // Should show loading state
    expect(loginButton.attributes('loading')).toBeDefined()
    expect(loginButton.attributes('disabled')).toBeDefined()
  })

  it('calls login action when form is submitted', async () => {
    const loginSpy = vi.spyOn(authStore, 'login').mockResolvedValue({
      success: true,
      data: {
        accessToken: 'test-token',
        refreshToken: 'test-refresh-token',
        username: 'testuser',
        realName: 'Test User'
      }
    })

    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    // Submit form
    await wrapper.find('form').trigger('submit')

    expect(loginSpy).toHaveBeenCalledWith({
      username: 'testuser',
      password: 'password123'
    })
  })

  it('redirects to dashboard on successful login', async () => {
    vi.spyOn(authStore, 'login').mockResolvedValue({
      success: true,
      data: {
        accessToken: 'test-token',
        refreshToken: 'test-refresh-token',
        username: 'testuser',
        realName: 'Test User'
      }
    })

    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    await wrapper.find('form').trigger('submit')
    await wrapper.vm.$nextTick()

    expect(mockPush).toHaveBeenCalledWith('/dashboard')
  })

  it('shows error message on login failure', async () => {
    const errorMessage = '用户名或密码错误'
    vi.spyOn(authStore, 'login').mockRejectedValue(new Error(errorMessage))

    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('wrongpassword')

    await wrapper.find('form').trigger('submit')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.errorMessage).toBe(errorMessage)
    expect(wrapper.find('.error-alert').exists()).toBe(true)
  })

  it('clears error message when user starts typing', async () => {
    // Set initial error
    wrapper.vm.errorMessage = '用户名或密码错误'
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-alert').exists()).toBe(true)

    // Start typing in username field
    const usernameInput = wrapper.find('input[type="text"]')
    await usernameInput.setValue('t')

    expect(wrapper.vm.errorMessage).toBe('')
    expect(wrapper.find('.error-alert').exists()).toBe(false)
  })

  it('validates required fields', async () => {
    const loginButton = wrapper.find('button')

    // Try to submit empty form
    await loginButton.trigger('click')
    await wrapper.vm.$nextTick()

    // Should show validation errors
    expect(wrapper.vm.errorMessage).toContain('请填写')
  })

  it('handles network errors gracefully', async () => {
    vi.spyOn(authStore, 'login').mockRejectedValue(new Error('Network Error'))

    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    await wrapper.find('form').trigger('submit')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.errorMessage).toBe('Network Error')
    expect(message.error).toHaveBeenCalledWith('登录失败，请稍后重试')
  })

  it('supports keyboard navigation', async () => {
    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    // Focus username input
    await usernameInput.trigger('focus')
    expect(document.activeElement).toBe(usernameInput.element)

    // Tab to password input
    await usernameInput.trigger('keydown', { key: 'Tab' })
    // Note: Actual tab navigation would require more complex setup

    // Enter in password field should submit form
    vi.spyOn(authStore, 'login').mockResolvedValue({
      success: true,
      data: {
        accessToken: 'test-token',
        refreshToken: 'test-refresh-token',
        username: 'testuser',
        realName: 'Test User'
      }
    })

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')
    await passwordInput.trigger('keydown', { key: 'Enter' })

    expect(authStore.login).toHaveBeenCalled()
  })

  it('shows different error messages for different error types', async () => {
    const testCases = [
      { error: 'ACCOUNT_LOCKED', expectedMessage: '账户已被锁定' },
      { error: 'ACCOUNT_DISABLED', expectedMessage: '账户已被停用' },
      { error: 'BAD_CREDENTIALS', expectedMessage: '用户名或密码错误' },
      { error: 'UNKNOWN_ERROR', expectedMessage: '登录失败，请稍后重试' }
    ]

    for (const testCase of testCases) {
      vi.spyOn(authStore, 'login').mockRejectedValue(new Error(testCase.error))

      const usernameInput = wrapper.find('input[type="text"]')
      const passwordInput = wrapper.find('input[type="password"]')

      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')

      await wrapper.find('form').trigger('submit')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.getErrorMessage(testCase.error)).toBe(testCase.expectedMessage)
    }
  })

  it('remembers redirect URL from query params', async () => {
    // Mock route with redirect query
    const mockRoute = {
      query: { redirect: '/admin/users' }
    }
    
    // Remount with redirect query
    wrapper = mount(LoginForm, {
      global: {
        plugins: [createPinia()],
        mocks: {
          $route: mockRoute
        },
        stubs: {
          'router-link': true,
          'a-form': {
            template: '<form @submit.prevent="$emit(\'finish\', $attrs.model)"><slot /></form>'
          },
          'a-form-item': {
            template: '<div><slot /></div>'
          },
          'a-input': {
            template: '<input v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-input-password': {
            template: '<input type="password" v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
          },
          'a-button': {
            template: '<button :disabled="disabled" :loading="loading" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'loading']
          }
        }
      }
    })

    vi.spyOn(authStore, 'login').mockResolvedValue({
      success: true,
      data: {
        accessToken: 'test-token',
        refreshToken: 'test-refresh-token',
        username: 'testuser',
        realName: 'Test User'
      }
    })

    const usernameInput = wrapper.find('input[type="text"]')
    const passwordInput = wrapper.find('input[type="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    await wrapper.find('form').trigger('submit')
    await wrapper.vm.$nextTick()

    expect(mockPush).toHaveBeenCalledWith('/admin/users')
  })
})