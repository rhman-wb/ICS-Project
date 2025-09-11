import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuards } from './guards'

// 导入路由模块
import authRoutes from './modules/auth'
import dashboardRoutes from './modules/dashboard'
import userRoutes from './modules/user'
import adminRoutes from './modules/admin'
import errorRoutes from './modules/error'

// 扩展路由元信息类型
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    permissions?: string[]
    roles?: string[]
    icon?: string
    hidden?: boolean
    keepAlive?: boolean
  }
}

// 基础路由配置
const baseRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard/home'
  }
]

// 合并所有路由
const routes: RouteRecordRaw[] = [
  ...baseRoutes,
  ...authRoutes,
  ...dashboardRoutes,
  ...userRoutes,
  ...adminRoutes,
  ...errorRoutes,
  // 捕获所有未匹配的路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, _from, savedPosition) {
    // 如果有保存的滚动位置，恢复到该位置
    if (savedPosition) {
      return savedPosition
    }
    
    // 如果路由有hash，滚动到对应元素
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }
    
    // 默认滚动到顶部
    return { top: 0 }
  }
})

// 设置路由守卫
setupRouterGuards(router)

export default router

// 导出路由配置供其他地方使用
export { routes }