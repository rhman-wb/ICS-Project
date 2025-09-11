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
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  }),
  useRoute: () => ({
    query: {}
  })
}))

describe('LoginForm', () => {
  let wrapper: VueWrapper<any>
  let authStore: any

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    
    // Mock auth store methods
    authStore.login = vi.fn()
    
    wrapper = mount(LoginForm, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'router-link': true
        }
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
    vi.clearAllMocks()
  })

  describe('组件渲染', () => {
    it('应该正确渲染登录表单', () => {
      expect(wrapper.find('.login-container').exists()).toBe(true)
      expect(wrapper.find('.login-panel').exists()).toBe(true)
      expect(wrapper.find('.system-header').exists()).toBe(true)
      expect(wrapper.find('.login-form').exists()).toBe(true)
    })

    it('应该显示系统标题', () => {
      const title = wrapper.find('.system-title')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('保险产品智能检核系统')
    })

    it('应该显示用户名输入框', () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      expect(usernameInput.exists()).toBe(true)
    })

    it('应该显示密码输入框', () => {
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      expect(passwordInput.exists()).toBe(true)
      expect(passwordInput.attributes('type')).toBe('password')
    })

    it('应该显示登录按钮', () => {
      const loginButton = wrapper.find('button[type="submit"]')
      expect(loginButton.exists()).toBe(true)
      expect(loginButton.text()).toBe('登录')
    })

    it('应该显示版权信息', () => {
      const footer = wrapper.find('.footer')
      expect(footer.exists()).toBe(true)
      expect(footer.text()).toContain('保险产品智能检核系统')
    })
  })

  describe('表单验证', () => {
    it('用户名为空时登录按钮应该被禁用', async () => {
      const loginButton = wrapper.find('button[type="submit"]')
      expect(loginButton.attributes('disabled')).toBeDefined()
    })

    it('密码为空时登录按钮应该被禁用', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      await usernameInput.setValue('testuser')
      
      const loginButton = wrapper.find('button[type="submit"]')
      expect(loginButton.attributes('disabled')).toBeDefined()
    })

    it('用户名和密码都填写后登录按钮应该可用', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      const loginButton = wrapper.find('button[type="submit"]')
      expect(loginButton.attributes('disabled')).toBeUndefined()
    })

    it('用户名长度超过限制时应该显示错误', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const longUsername = 'a'.repeat(21) // 超过20字符限制
      
      await usernameInput.setValue(longUsername)
      await usernameInput.trigger('blur')
      
      // 等待验证完成
      await wrapper.vm.$nextTick()
      
      const errorMessage = wrapper.find('.ant-form-item-explain-error')
      expect(errorMessage.exists()).toBe(true)
    })

    it('密码长度超过限制时应该显示错误', async () => {
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const longPassword = 'a'.repeat(21) // 超过20字符限制
      
      await passwordInput.setValue(longPassword)
      await passwordInput.trigger('blur')
      
      // 等待验证完成
      await wrapper.vm.$nextTick()
      
      const errorMessage = wrapper.find('.ant-form-item-explain-error')
      expect(errorMessage.exists()).toBe(true)
    })
  })

  describe('用户交互', () => {
    it('应该支持清除用户名输入', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      
      await usernameInput.setValue('testuser')
      expect(usernameInput.element.value).toBe('testuser')
      
      // 查找清除按钮并点击
      const clearButton = wrapper.find('.ant-input-clear-icon')
      if (clearButton.exists()) {
        await clearButton.trigger('click')
        expect(usernameInput.element.value).toBe('')
      }
    })

    it('应该支持清除密码输入', async () => {
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      
      await passwordInput.setValue('password123')
      expect(passwordInput.element.value).toBe('password123')
      
      // 查找清除按钮并点击
      const clearButton = wrapper.find('.ant-input-clear-icon')
      if (clearButton.exists()) {
        await clearButton.trigger('click')
        expect(passwordInput.element.value).toBe('')
      }
    })

    it('应该支持Tab键切换焦点', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      
      await usernameInput.trigger('focus')
      await usernameInput.trigger('keydown', { key: 'Tab' })
      
      // 验证焦点是否切换到密码输入框
      expect(document.activeElement).toBe(passwordInput.element)
    })

    it('应该支持Enter键提交表单', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      // Mock login method
      authStore.login.mockResolvedValue({
        token: 'mock-token',
        user: { username: 'testuser' }
      })
      
      await passwordInput.trigger('keydown', { key: 'Enter' })
      
      expect(authStore.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123'
      })
    })
  })

  describe('登录功能', () => {
    it('登录成功时应该跳转到工作台', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const loginButton = wrapper.find('button[type="submit"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      // Mock successful login
      authStore.login.mockResolvedValue({
        token: 'mock-token',
        user: { username: 'testuser', realName: '测试用户' }
      })
      
      await loginButton.trigger('click')
      
      // 等待异步操作完成
      await wrapper.vm.$nextTick()
      
      expect(authStore.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123'
      })
      
      expect(mockPush).toHaveBeenCalledWith('/dashboard/home')
    })

    it('登录失败时应该显示错误信息', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const loginButton = wrapper.find('button[type="submit"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('wrongpassword')
      
      // Mock failed login
      const errorMessage = '用户名或密码不正确'
      authStore.login.mockRejectedValue(new Error(errorMessage))
      
      await loginButton.trigger('click')
      
      // 等待异步操作完成
      await wrapper.vm.$nextTick()
      
      expect(authStore.login).toHaveBeenCalled()
      expect(message.error).toHaveBeenCalledWith(errorMessage)
    })

    it('登录过程中按钮应该显示加载状态', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const loginButton = wrapper.find('button[type="submit"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      // Mock slow login
      let resolveLogin: (value: any) => void
      const loginPromise = new Promise(resolve => {
        resolveLogin = resolve
      })
      authStore.login.mockReturnValue(loginPromise)
      
      await loginButton.trigger('click')
      
      // 检查加载状态
      expect(loginButton.classes()).toContain('ant-btn-loading')
      expect(loginButton.text()).toBe('登录中...')
      expect(loginButton.attributes('disabled')).toBeDefined()
      
      // 完成登录
      resolveLogin!({ token: 'mock-token', user: { username: 'testuser' } })
      await wrapper.vm.$nextTick()
      
      // 检查加载状态已清除
      expect(loginButton.classes()).not.toContain('ant-btn-loading')
      expect(loginButton.text()).toBe('登录')
    })

    it('登录过程中应该防止重复提交', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const loginButton = wrapper.find('button[type="submit"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      // Mock slow login
      let resolveLogin: (value: any) => void
      const loginPromise = new Promise(resolve => {
        resolveLogin = resolve
      })
      authStore.login.mockReturnValue(loginPromise)
      
      // 快速点击两次
      await loginButton.trigger('click')
      await loginButton.trigger('click')
      
      // 应该只调用一次
      expect(authStore.login).toHaveBeenCalledTimes(1)
      
      // 完成登录
      resolveLogin!({ token: 'mock-token', user: { username: 'testuser' } })
      await wrapper.vm.$nextTick()
    })
  })

  describe('错误处理', () => {
    it('应该显示网络错误提示', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const loginButton = wrapper.find('button[type="submit"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      // Mock network error
      authStore.login.mockRejectedValue(new Error('Network Error'))
      
      await loginButton.trigger('click')
      await wrapper.vm.$nextTick()
      
      expect(message.error).toHaveBeenCalledWith('网络连接失败，请检查网络后重试')
    })

    it('应该显示服务器错误提示', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      const loginButton = wrapper.find('button[type="submit"]')
      
      await usernameInput.setValue('testuser')
      await passwordInput.setValue('password123')
      
      // Mock server error
      authStore.login.mockRejectedValue(new Error('Internal Server Error'))
      
      await loginButton.trigger('click')
      await wrapper.vm.$nextTick()
      
      expect(message.error).toHaveBeenCalledWith('服务器内部错误，请稍后重试')
    })

    it('用户开始输入时应该清除错误提示', async () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      
      // 先设置一个错误状态
      await wrapper.setData({ errorMessage: '用户名或密码不正确' })
      
      // 开始输入
      await usernameInput.setValue('t')
      await usernameInput.trigger('input')
      
      // 错误信息应该被清除
      expect(wrapper.vm.errorMessage).toBe('')
    })
  })

  describe('响应式设计', () => {
    it('在小屏幕设备上应该适配布局', async () => {
      // 模拟小屏幕
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 375
      })
      
      window.dispatchEvent(new Event('resize'))
      await wrapper.vm.$nextTick()
      
      const loginPanel = wrapper.find('.login-panel')
      expect(loginPanel.exists()).toBe(true)
      
      // 在小屏幕上，登录面板应该有适当的样式调整
      const computedStyle = window.getComputedStyle(loginPanel.element)
      expect(computedStyle.width).toBe('90%')
    })
  })

  describe('无障碍访问', () => {
    it('输入框应该有正确的标签', () => {
      const usernameInput = wrapper.find('input[placeholder="请输入用户账号"]')
      const passwordInput = wrapper.find('input[placeholder="请输入登录密码"]')
      
      expect(usernameInput.attributes('aria-label')).toBe('用户账号')
      expect(passwordInput.attributes('aria-label')).toBe('登录密码')
    })

    it('登录按钮应该有正确的角色', () => {
      const loginButton = wrapper.find('button[type="submit"]')
      expect(loginButton.attributes('role')).toBe('button')
    })

    it('错误信息应该有正确的角色', async () => {
      await wrapper.setData({ errorMessage: '用户名或密码不正确' })
      
      const errorAlert = wrapper.find('.error-alert')
      expect(errorAlert.attributes('role')).toBe('alert')
      expect(errorAlert.attributes('aria-live')).toBe('polite')
    })
  })
})