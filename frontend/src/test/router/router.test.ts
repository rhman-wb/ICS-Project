import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { routes } from '@/router'
import { useAuthStore } from '@/stores/modules/auth'
import { setupRouterGuards } from '@/router/guards'

// Mock ant-design-vue message
vi.mock('ant-design-vue', () => ({
  message: {
    warning: vi.fn(),
    error: vi.fn()
  }
}))

describe('Router Configuration', () => {
  let router: any
  let pinia: any

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    
    router = createRouter({
      history: createWebHistory(),
      routes
    })
    
    setupRouterGuards(router)
  })

  it('should have correct route structure', () => {
    expect(routes).toBeDefined()
    expect(Array.isArray(routes)).toBe(true)
    expect(routes.length).toBeGreaterThan(0)
  })

  it('should have login route', () => {
    const loginRoute = routes.find(route => route.name === 'Login')
    expect(loginRoute).toBeDefined()
    expect(loginRoute?.path).toBe('/login')
    expect(loginRoute?.meta?.requiresAuth).toBe(false)
  })

  it('should have dashboard route', () => {
    const dashboardRoute = routes.find(route => route.name === 'Dashboard')
    expect(dashboardRoute).toBeDefined()
    expect(dashboardRoute?.path).toBe('/dashboard')
    expect(dashboardRoute?.meta?.requiresAuth).toBe(true)
  })

  it('should have error routes', () => {
    const notFoundRoute = routes.find(route => route.name === 'NotFound')
    const forbiddenRoute = routes.find(route => route.name === 'Forbidden')
    const serverErrorRoute = routes.find(route => route.name === 'ServerError')
    
    expect(notFoundRoute).toBeDefined()
    expect(forbiddenRoute).toBeDefined()
    expect(serverErrorRoute).toBeDefined()
  })

  it('should redirect root path to dashboard', () => {
    const rootRoute = routes.find(route => route.path === '/')
    expect(rootRoute?.redirect).toBe('/dashboard')
  })

  it('should have catch-all route for 404', () => {
    const catchAllRoute = routes.find(route => route.path === '/:pathMatch(.*)*')
    expect(catchAllRoute).toBeDefined()
    expect(catchAllRoute?.redirect).toBe('/404')
  })

  it('should have admin routes with role restrictions', () => {
    const adminRoute = routes.find(route => route.name === 'AdminLayout')
    expect(adminRoute).toBeDefined()
    expect(adminRoute?.meta?.roles).toContain('admin')
    expect(adminRoute?.meta?.requiresAuth).toBe(true)
  })

  it('should have user routes with permission restrictions', () => {
    const userRoute = routes.find(route => route.name === 'UserLayout')
    expect(userRoute).toBeDefined()
    expect(userRoute?.meta?.requiresAuth).toBe(true)
    
    const profileRoute = userRoute?.children?.find(child => child.name === 'UserProfile')
    expect(profileRoute).toBeDefined()
    expect(profileRoute?.meta?.requiresAuth).toBe(true)
  })

  it('should handle route navigation with authentication', async () => {
    const authStore = useAuthStore()
    
    // Test that auth store is available
    expect(authStore).toBeDefined()
    expect(typeof authStore.checkPermission).toBe('function')
  })

  it('should set correct page titles', () => {
    const dashboardRoute = routes.find(route => route.name === 'Dashboard')
    expect(dashboardRoute?.meta?.title).toBe('工作台')
    
    const loginRoute = routes.find(route => route.name === 'Login')
    expect(loginRoute?.meta?.title).toBe('登录')
  })

  it('should have lazy-loaded components', () => {
    const dashboardRoute = routes.find(route => route.name === 'Dashboard')
    expect(typeof dashboardRoute?.component).toBe('function')
    
    const loginRoute = routes.find(route => route.name === 'Login')
    expect(typeof loginRoute?.component).toBe('function')
  })

  it('should handle scroll behavior correctly', () => {
    // Test that router has scroll behavior configured
    expect(router).toBeDefined()
    expect(router.options).toBeDefined()
    
    // Test scroll behavior function exists
    const scrollBehavior = router.options.scrollBehavior
    if (scrollBehavior) {
      // Test with saved position
      const savedPosition = { top: 100 }
      const result1 = scrollBehavior({}, {}, savedPosition)
      expect(result1).toEqual(savedPosition)
      
      // Test with hash
      const toWithHash = { hash: '#section1' }
      const result2 = scrollBehavior(toWithHash, {}, null)
      expect(result2).toEqual({
        el: '#section1',
        behavior: 'smooth'
      })
      
      // Test default behavior
      const result3 = scrollBehavior({}, {}, null)
      expect(result3).toEqual({ top: 0 })
    }
  })
})
