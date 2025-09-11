import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/modules/auth'
import { handleLoginRedirect } from '@/router/guards'

// Mock ant-design-vue message
vi.mock('ant-design-vue', () => ({
  message: {
    warning: vi.fn(),
    error: vi.fn()
  }
}))

describe('Router Guards', () => {
  let router: any
  let pinia: any
  let authStore: any

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    authStore = useAuthStore()
    
    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/',
          redirect: '/dashboard'
        },
        {
          path: '/login',
          name: 'Login',
          component: () => Promise.resolve({ default: {} }),
          meta: { requiresAuth: false }
        },
        {
          path: '/dashboard',
          name: 'Dashboard',
          component: () => Promise.resolve({ default: {} }),
          meta: { requiresAuth: true, title: '工作台' }
        },
        {
          path: '/admin',
          name: 'Admin',
          component: () => Promise.resolve({ default: {} }),
          meta: { requiresAuth: true, roles: ['admin'] }
        },
        {
          path: '/user/profile',
          name: 'UserProfile',
          component: () => Promise.resolve({ default: {} }),
          meta: { requiresAuth: true, permissions: ['user:profile:view'] }
        }
      ]
    })
    
    // Clear all mocks
    vi.clearAllMocks()
  })

  describe('Auth Store Integration', () => {
    it('should work with auth store methods', () => {
      expect(authStore).toBeDefined()
      expect(typeof authStore.checkPermission).toBe('function')
      expect(typeof authStore.checkRole).toBe('function')
    })
  })

  describe('Login Redirect Handler', () => {
    it('should redirect to dashboard by default', () => {
      const mockPush = vi.fn()
      const mockRouter = { push: mockPush }
      
      handleLoginRedirect(mockRouter)
      
      expect(mockPush).toHaveBeenCalledWith('/dashboard')
    })

    it('should redirect to specified path', () => {
      const mockPush = vi.fn()
      const mockRouter = { push: mockPush }
      
      handleLoginRedirect(mockRouter, '/user/profile')
      
      expect(mockPush).toHaveBeenCalledWith('/user/profile')
    })

    it('should reject unsafe redirect paths', () => {
      const mockPush = vi.fn()
      const mockRouter = { push: mockPush }
      
      // Test external URL
      handleLoginRedirect(mockRouter, 'https://evil.com')
      expect(mockPush).toHaveBeenCalledWith('/dashboard')
      
      // Test relative path without leading slash
      handleLoginRedirect(mockRouter, 'relative/path')
      expect(mockPush).toHaveBeenCalledWith('/dashboard')
    })

    it('should accept safe redirect paths', () => {
      const mockPush = vi.fn()
      const mockRouter = { push: mockPush }
      
      handleLoginRedirect(mockRouter, '/safe/path')
      expect(mockPush).toHaveBeenCalledWith('/safe/path')
    })
  })
})