import type { Router } from 'vue-router'
import { setupAuthGuard } from './auth'
import { setupPermissionGuard } from './permission'

/**
 * 设置所有路由守卫
 * @param router Vue Router实例
 */
export function setupRouterGuards(router: Router) {
  // 设置认证守卫
  setupAuthGuard(router)
  
  // 设置权限守卫
  setupPermissionGuard(router)
  
  // 设置页面标题守卫
  router.afterEach((to) => {
    const title = to.meta.title as string
    if (title) {
      document.title = `${title} - 智能保险产品检核系统`
    } else {
      document.title = '智能保险产品检核系统'
    }
  })
  
  // 设置页面加载进度条（可选）
  router.beforeEach((_to, _from, next) => {
    // 可以在这里添加页面加载进度条的开始逻辑
    // 例如：NProgress.start()
    next()
  })
  
  router.afterEach(() => {
    // 可以在这里添加页面加载进度条的结束逻辑
    // 例如：NProgress.done()
  })
}

// 导出守卫函数供其他地方使用
export { setupAuthGuard } from './auth'
export { setupPermissionGuard } from './permission'
export { handleLoginRedirect, hasRoutePermission } from './auth'
export { checkDynamicPermission, checkDynamicRole } from './permission'