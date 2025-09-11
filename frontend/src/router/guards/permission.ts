import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'
import { message } from 'ant-design-vue'

/**
 * 设置权限路由守卫
 * @param router Vue Router实例
 */
export function setupPermissionGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()
    
    // 只对需要认证的路由进行权限检查
    if (!to.meta.requiresAuth) {
      next()
      return
    }
    
    // 如果用户未登录，由认证守卫处理
    if (!authStore.isLoggedIn) {
      next()
      return
    }
    
    // 检查页面级权限
    if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
      const hasPermission = to.meta.permissions.some((permission: string) => 
        authStore.checkPermission(permission)
      )
      
      if (!hasPermission) {
        console.warn(`User lacks permission for route: ${to.path}`, {
          requiredPermissions: to.meta.permissions,
          userPermissions: authStore.permissions
        })
        
        message.error('没有权限访问该页面')
        
        // 跳转到403页面
        next({ 
          name: 'Forbidden',
          query: { 
            redirect: to.fullPath,
            reason: 'permission'
          }
        })
        return
      }
    }
    
    // 检查角色权限
    if (to.meta.roles && Array.isArray(to.meta.roles)) {
      const hasRole = authStore.checkRole(to.meta.roles)
      
      if (!hasRole) {
        console.warn(`User lacks role for route: ${to.path}`, {
          requiredRoles: to.meta.roles,
          userRoles: authStore.userInfo?.roles
        })
        
        message.error('没有权限访问该页面')
        
        // 跳转到403页面
        next({ 
          name: 'Forbidden',
          query: { 
            redirect: to.fullPath,
            reason: 'role'
          }
        })
        return
      }
    }
    
    next()
  })
}

/**
 * 动态权限检查函数
 * @param permissions 需要的权限列表
 * @returns 是否有权限
 */
export function checkDynamicPermission(permissions: string | string[]): boolean {
  const authStore = useAuthStore()
  
  if (!authStore.isLoggedIn) {
    return false
  }
  
  return authStore.checkPermission(permissions)
}

/**
 * 动态角色检查函数
 * @param roles 需要的角色列表
 * @returns 是否有角色
 */
export function checkDynamicRole(roles: string | string[]): boolean {
  const authStore = useAuthStore()
  
  if (!authStore.isLoggedIn) {
    return false
  }
  
  return authStore.checkRole(roles)
}