import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory, Router } from 'vue-router'
import { vi } from 'vitest'
import type { Component } from 'vue'

/**
 * 测试工具函数
 */

/**
 * 创建测试路由器
 */
export function createTestRouter(routes: any[] = []): Router {
  const defaultRoutes = [
    { path: '/', redirect: '/dashboard/home' },
    { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
    { path: '/dashboard', name: 'Dashboard', component: { template: '<div>Dashboard</div>' } },
    { path: '/forbidden', name: 'Forbidden', component: { template: '<div>Forbidden</div>' } },
    ...routes
  ]

  return createRouter({
    history: createWebHistory(),
    routes: defaultRoutes
  })
}

/**
 * 挂载组件的辅助函数
 */
export function mountComponent(component: Component, options: any = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  
  const router = createTestRouter(options.routes)
  
  const defaultOptions = {
    global: {
      plugins: [pinia, router],
      stubs: {
        'router-link': true,
        'router-view': true,
        ...options.stubs
      },
      mocks: {
        $t: (key: string) => key,
        $route: {
          path: '/',
          query: {},
          params: {},
          ...options.route
        },
        $router: {
          push: vi.fn(),
          replace: vi.fn(),
          go: vi.fn(),
          back: vi.fn(),
          forward: vi.fn(),
          ...options.router
        },
        ...options.mocks
      }
    },
    ...options
  }

  return mount(component, defaultOptions)
}

/**
 * 等待异步操作完成
 */
export async function flushPromises(): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, 0))
}

/**
 * 模拟用户输入
 */
export async function typeInInput(wrapper: VueWrapper, selector: string, value: string) {
  const input = wrapper.find(selector)
  await input.setValue(value)
  await input.trigger('input')
  await wrapper.vm.$nextTick()
}

/**
 * 模拟用户点击
 */
export async function clickElement(wrapper: VueWrapper, selector: string) {
  const element = wrapper.find(selector)
  await element.trigger('click')
  await wrapper.vm.$nextTick()
}

/**
 * 等待元素出现
 */
export async function waitForElement(wrapper: VueWrapper, selector: string, timeout = 1000) {
  const startTime = Date.now()
  
  while (Date.now() - startTime < timeout) {
    if (wrapper.find(selector).exists()) {
      return wrapper.find(selector)
    }
    await new Promise(resolve => setTimeout(resolve, 10))
  }
  
  throw new Error(`Element ${selector} not found within ${timeout}ms`)
}

/**
 * 模拟API响应
 */
export function mockApiResponse(data: any, success = true) {
  return {
    success,
    data,
    message: success ? '操作成功' : '操作失败',
    code: success ? 200 : 400,
    timestamp: Date.now()
  }
}

/**
 * 模拟分页响应
 */
export function mockPageResponse(records: any[], total = records.length, current = 1, size = 20) {
  return mockApiResponse({
    records,
    total,
    current,
    size,
    pages: Math.ceil(total / size)
  })
}

/**
 * 模拟错误响应
 */
export function mockErrorResponse(message = '操作失败', code = 400) {
  return {
    success: false,
    data: null,
    message,
    code,
    timestamp: Date.now()
  }
}

/**
 * 创建测试用户数据
 */
export function createTestUser(overrides: any = {}) {
  return {
    id: 'user-test',
    username: 'testuser',
    realName: '测试用户',
    email: 'test@example.com',
    phone: '13800138000',
    status: 'ACTIVE',
    roles: ['USER'],
    permissions: ['USER_VIEW', 'DASHBOARD_VIEW'],
    createdAt: '2024-01-01T00:00:00',
    updatedAt: '2024-01-01T00:00:00',
    ...overrides
  }
}

/**
 * 创建测试登录响应
 */
export function createTestLoginResponse(user = createTestUser()) {
  return mockApiResponse({
    accessToken: 'test-access-token',
    refreshToken: 'test-refresh-token',
    username: user.username,
    realName: user.realName,
    roles: user.roles,
    permissions: user.permissions,
    expiresIn: 3600
  })
}

/**
 * 模拟本地存储
 */
export function mockLocalStorage() {
  const store: Record<string, string> = {}
  
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key]
    }),
    clear: vi.fn(() => {
      Object.keys(store).forEach(key => delete store[key])
    }),
    key: vi.fn((index: number) => Object.keys(store)[index] || null),
    get length() {
      return Object.keys(store).length
    }
  }
}

/**
 * 模拟会话存储
 */
export function mockSessionStorage() {
  return mockLocalStorage()
}

/**
 * 创建测试环境的Ant Design Vue组件存根
 */
export function createAntdStubs() {
  return {
    'a-form': {
      template: '<form @submit.prevent="$emit(\'finish\', $attrs.model)"><slot /></form>'
    },
    'a-form-item': {
      template: '<div class="ant-form-item"><slot /></div>'
    },
    'a-input': {
      template: `
        <input 
          :value="modelValue" 
          @input="$emit('update:modelValue', $event.target.value)"
          v-bind="$attrs"
        />
      `,
      props: ['modelValue'],
      emits: ['update:modelValue']
    },
    'a-input-password': {
      template: `
        <input 
          type="password"
          :value="modelValue" 
          @input="$emit('update:modelValue', $event.target.value)"
          v-bind="$attrs"
        />
      `,
      props: ['modelValue'],
      emits: ['update:modelValue']
    },
    'a-button': {
      template: `
        <button 
          :disabled="disabled || loading" 
          :class="{ 'ant-btn-loading': loading }"
          @click="$emit('click')"
          v-bind="$attrs"
        >
          <slot />
        </button>
      `,
      props: ['disabled', 'loading', 'type', 'size']
    },
    'a-alert': {
      template: '<div class="ant-alert"><slot /></div>'
    },
    'a-card': {
      template: '<div class="ant-card"><slot /></div>'
    },
    'a-table': {
      template: '<div class="ant-table"><slot /></div>',
      props: ['columns', 'dataSource', 'loading', 'pagination']
    },
    'a-menu': {
      template: '<div class="ant-menu"><slot /></div>',
      props: ['selectedKeys', 'mode', 'theme']
    },
    'a-menu-item': {
      template: '<div class="ant-menu-item"><slot /></div>',
      props: ['key']
    },
    'a-dropdown': {
      template: '<div class="ant-dropdown"><slot /></div>'
    },
    'a-tabs': {
      template: '<div class="ant-tabs"><slot /></div>',
      props: ['activeKey']
    },
    'a-tab-pane': {
      template: '<div class="ant-tab-pane"><slot /></div>',
      props: ['key', 'tab']
    }
  }
}

/**
 * 断言辅助函数
 */
export const assertions = {
  /**
   * 断言元素可见
   */
  toBeVisible(wrapper: VueWrapper, selector: string) {
    const element = wrapper.find(selector)
    expect(element.exists()).toBe(true)
    expect(element.isVisible()).toBe(true)
  },

  /**
   * 断言元素不可见
   */
  toBeHidden(wrapper: VueWrapper, selector: string) {
    const element = wrapper.find(selector)
    expect(element.exists()).toBe(false) || expect(element.isVisible()).toBe(false)
  },

  /**
   * 断言元素包含文本
   */
  toContainText(wrapper: VueWrapper, selector: string, text: string) {
    const element = wrapper.find(selector)
    expect(element.exists()).toBe(true)
    expect(element.text()).toContain(text)
  },

  /**
   * 断言输入框有值
   */
  toHaveValue(wrapper: VueWrapper, selector: string, value: string) {
    const element = wrapper.find(selector)
    expect(element.exists()).toBe(true)
    expect((element.element as HTMLInputElement).value).toBe(value)
  }
}