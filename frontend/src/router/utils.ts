import type { RouteRecordRaw, RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '@/stores/modules/auth'

/**
 * 过滤路由，根据用户权限显示可访问的路由
 * @param routes 路由配置
 * @param authStore 认证store
 * @returns 过滤后的路由
 */
export function filterRoutes(routes: RouteRecordRaw[], authStore: ReturnType<typeof useAuthStore>): RouteRecordRaw[] {
  return routes.filter(route => {
    // 检查路由是否需要隐藏
    if (route.meta?.hidden) {
      return false
    }
    
    // 检查是否需要认证
    if (route.meta?.requiresAuth && !authStore.isLoggedIn) {
      return false
    }
    
    // 检查权限
    if (route.meta?.permissions && Array.isArray(route.meta.permissions)) {
      const hasPermission = route.meta.permissions.some(permission => 
        authStore.checkPermission(permission)
      )
      if (!hasPermission) {
        return false
      }
    }
    
    // 检查角色
    if (route.meta?.roles && Array.isArray(route.meta.roles)) {
      const hasRole = authStore.checkRole(route.meta.roles)
      if (!hasRole) {
        return false
      }
    }
    
    // 递归过滤子路由
    if (route.children && route.children.length > 0) {
      route.children = filterRoutes(route.children, authStore)
      // 如果所有子路由都被过滤掉，则隐藏父路由
      if (route.children.length === 0 && route.redirect) {
        return false
      }
    }
    
    return true
  })
}

/**
 * 生成面包屑导航
 * @param route 当前路由
 * @returns 面包屑数组
 */
export function generateBreadcrumbs(route: RouteLocationNormalized): Array<{ name: string; path?: string; title: string }> {
  const breadcrumbs: Array<{ name: string; path?: string; title: string }> = []
  
  // 获取匹配的路由记录
  const matched = route.matched.filter(item => item.meta?.title)
  
  matched.forEach((item, index) => {
    const isLast = index === matched.length - 1
    breadcrumbs.push({
      name: item.name as string,
      path: isLast ? undefined : item.path,
      title: item.meta?.title as string
    })
  })
  
  return breadcrumbs
}

/**
 * 获取路由的完整标题
 * @param route 路由对象
 * @returns 完整标题
 */
export function getRouteTitle(route: RouteLocationNormalized): string {
  const title = route.meta?.title as string
  if (title) {
    return `${title} - 智能保险产品检核系统`
  }
  return '智能保险产品检核系统'
}

/**
 * 检查路由是否为活跃状态
 * @param routePath 路由路径
 * @param currentPath 当前路径
 * @returns 是否活跃
 */
export function isRouteActive(routePath: string, currentPath: string): boolean {
  if (routePath === currentPath) {
    return true
  }
  
  // 检查是否为子路由
  if (currentPath.startsWith(routePath + '/')) {
    return true
  }
  
  return false
}

/**
 * 获取默认路由
 * @param authStore 认证store
 * @returns 默认路由路径
 */
export function getDefaultRoute(authStore: ReturnType<typeof useAuthStore>): string {
  if (!authStore.isLoggedIn) {
    return '/login'
  }
  
  // 根据用户角色返回不同的默认路由
  if (authStore.checkRole('admin')) {
    return '/admin/users'
  }
  
  return '/dashboard'
}

/**
 * 路由元信息类型守卫
 * @param route 路由对象
 * @returns 类型安全的元信息
 */
export function getRouteMeta(route: RouteLocationNormalized) {
  return {
    title: route.meta?.title as string | undefined,
    requiresAuth: route.meta?.requiresAuth as boolean | undefined,
    permissions: route.meta?.permissions as string[] | undefined,
    roles: route.meta?.roles as string[] | undefined,
    icon: route.meta?.icon as string | undefined,
    hidden: route.meta?.hidden as boolean | undefined,
    keepAlive: route.meta?.keepAlive as boolean | undefined
  }
}

/**
 * 构建菜单数据
 * @param routes 路由配置
 * @param authStore 认证store
 * @returns 菜单数据
 */
export function buildMenuData(routes: RouteRecordRaw[], authStore: ReturnType<typeof useAuthStore>) {
  const menuData: Array<{
    key: string
    title: string
    icon?: string
    path?: string
    children?: any[]
  }> = []
  
  const filteredRoutes = filterRoutes(routes, authStore)
  
  filteredRoutes.forEach(route => {
    if (route.meta?.hidden) {
      return
    }
    
    const menuItem = {
      key: route.name as string,
      title: route.meta?.title as string,
      icon: route.meta?.icon,
      path: route.path,
      children: undefined as any
    }
    
    // 处理子路由
    if (route.children && route.children.length > 0) {
      const childrenMenu = buildMenuData(route.children, authStore)
      if (childrenMenu.length > 0) {
        menuItem.children = childrenMenu
      }
    }
    
    if (menuItem.title) {
      menuData.push(menuItem)
    }
  })
  
  return menuData
}

/**
 * 路由跳转工具
 * @param router Vue Router实例
 * @param path 目标路径
 * @param options 跳转选项
 */
export async function navigateTo(
  router: any,
  path: string,
  options: {
    replace?: boolean
    query?: Record<string, any>
    params?: Record<string, any>
  } = {}
) {
  const { replace = false, query, params } = options
  
  const routeConfig: any = { path }
  
  if (query) {
    routeConfig.query = query
  }
  
  if (params) {
    routeConfig.params = params
  }
  
  if (replace) {
    await router.replace(routeConfig)
  } else {
    await router.push(routeConfig)
  }
}

/**
 * 检查路由是否存在
 * @param router Vue Router实例
 * @param routeName 路由名称
 * @returns 是否存在
 */
export function routeExists(router: any, routeName: string): boolean {
  try {
    const route = router.resolve({ name: routeName })
    return route.name === routeName
  } catch {
    return false
  }
}