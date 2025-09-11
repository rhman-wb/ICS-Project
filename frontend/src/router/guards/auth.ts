import type { Router, NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { message } from 'ant-design-vue'

/**
 * 设置认证路由守卫
 * @param router Vue Router实例
 */
export function setupAuthGuard(router: Router) {
  router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const authStore = useAuthStore()
    
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
      if (!authStore.isLoggedIn) {
        // 未登录用户重定向到登录页
        message.warning('请先登录')
        next({
          name: 'Login',
          query: { 
            redirect: to.fullPath // 支持redirect参数，登录成功后跳转回原页面
          }
        })
        return
      }
      
      // 检查用户信息是否存在
      if (!authStore.userInfo) {
        try {
          // 尝试获取用户信息
          await authStore.fetchUserInfo()
        } catch (error) {
          console.error('Failed to fetch user info:', error)
          // 获取用户信息失败，清除认证状态并跳转到登录页
          authStore.clearAuthData()
          message.error('用户信息获取失败，请重新登录')
          next({
            name: 'Login',
            query: { redirect: to.fullPath }
          })
          return
        }
      }
      
      // 检查页面权限
      if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
        const hasPermission = to.meta.permissions.some((permission: string) => 
          authStore.checkPermission(permission)
        )
        
        if (!hasPermission) {
          message.error('没有权限访问该页面')
          // 权限不足，跳转到首页或上一页
          if (from.name && from.name !== 'Login') {
            next(false) // 阻止导航，停留在当前页面
          } else {
            next({ name: 'Dashboard' })
          }
          return
        }
      }
      
      // 检查角色权限
      if (to.meta.roles && Array.isArray(to.meta.roles)) {
        const hasRole = authStore.checkRole(to.meta.roles)
        
        if (!hasRole) {
          message.error('没有权限访问该页面')
          if (from.name && from.name !== 'Login') {
            next(false)
          } else {
            next({ name: 'Dashboard' })
          }
          return
        }
      }
    } else {
      // 不需要认证的页面处理
      if (to.name === 'Login' && authStore.isLoggedIn) {
        // 已登录用户访问登录页，处理redirect参数
        const redirectPath = (to.query.redirect as string) || '/dashboard'
        next(redirectPath)
        return
      }
    }
    
    next()
  })
}

/**
 * 处理登录成功后的页面跳转逻辑
 * @param router Vue Router实例
 * @param redirectPath 重定向路径
 */
export function handleLoginRedirect(router: Router, redirectPath?: string) {
  const targetPath = redirectPath || '/dashboard'
  
  // 验证重定向路径是否安全
  if (redirectPath && !isValidRedirectPath(redirectPath)) {
    console.warn('Invalid redirect path:', redirectPath)
    router.push('/dashboard')
    return
  }
  
  router.push(targetPath)
}

/**
 * 验证重定向路径是否安全
 * @param path 路径
 * @returns 是否为安全路径
 */
function isValidRedirectPath(path: string): boolean {
  // 防止开放重定向攻击
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return false
  }
  
  // 确保路径以 / 开头
  if (!path.startsWith('/')) {
    return false
  }
  
  // 可以添加更多安全检查
  return true
}

/**
 * 检查用户是否有访问路由的权限
 * @param route 路由对象
 * @param authStore 认证store
 * @returns 是否有权限
 */
export function hasRoutePermission(route: RouteLocationNormalized, authStore: ReturnType<typeof useAuthStore>): boolean {
  // 检查权限
  if (route.meta.permissions && Array.isArray(route.meta.permissions)) {
    return route.meta.permissions.some((permission: string) => 
      authStore.checkPermission(permission)
    )
  }
  
  // 检查角色
  if (route.meta.roles && Array.isArray(route.meta.roles)) {
    return authStore.checkRole(route.meta.roles)
  }
  
  // 如果没有特定权限要求，则允许访问
  return true
}