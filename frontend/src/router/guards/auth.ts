import type { Router, NavigationGuardNext, RouteLocationNormalized } from "vue-router"
import { useAuthStore } from "@/stores/modules/auth"
import { message } from "ant-design-vue"

/**
 * 路由认证与权限守卫
 */
export function setupAuthGuard(router: Router) {
  router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const authStore = useAuthStore()

    // 需要鉴权的页面
    if (to.meta.requiresAuth) {
      // 未登录，跳转到登录页（不弹全局提示，避免遮挡）
      if (!authStore.isLoggedIn) {
        next({ name: "Login", query: { redirect: to.fullPath } })
        return
      }

      // 已登录但本地无用户信息，尝试拉取
      if (!authStore.userInfo) {
        try {
          await authStore.fetchUserInfo()
        } catch (error) {
          console.error("Failed to fetch user info:", error)
          authStore.clearAuthData()
          message.error("用户信息获取失败，请重新登录")
          next({ name: "Login", query: { redirect: to.fullPath } })
          return
        }
      }

      // 基于权限点校验
      if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
        const hasPermission = to.meta.permissions.some((permission: string) =>
          authStore.checkPermission(permission)
        )
        if (!hasPermission) {
          message.error("没有权限访问该页面")
          if (from.name && from.name !== "Login") {
            next(false)
          } else {
            next({ name: "Dashboard" })
          }
          return
        }
      }

      // 基于角色校验
      if (to.meta.roles && Array.isArray(to.meta.roles)) {
        const hasRole = authStore.checkRole(to.meta.roles)
        if (!hasRole) {
          message.error("没有权限访问该页面")
          if (from.name && from.name !== "Login") {
            next(false)
          } else {
            next({ name: "Dashboard" })
          }
          return
        }
      }
    } else {
      // 对无需鉴权的页面进行登录态处理
      if (to.name === "Login" && authStore.isLoggedIn) {
        const redirectPath = (to.query.redirect as string) || "/dashboard/home"
        next(redirectPath)
        return
      }
    }

    next()
  })
}

/**
 * 登录成功后的跳转统一处理
 */
export function handleLoginRedirect(router: Router, redirectPath?: string) {
  const targetPath = redirectPath || "/dashboard/home"
  if (redirectPath && !isValidRedirectPath(redirectPath)) {
    console.warn("Invalid redirect path:", redirectPath)
    router.push("/dashboard/home")
    return
  }
  router.push(targetPath)
}

function isValidRedirectPath(path: string): boolean {
  // 禁止外链重定向
  if (path.startsWith("http://") || path.startsWith("https://")) return false
  // 必须以/开头
  if (!path.startsWith("/")) return false
  return true
}

export function hasRoutePermission(
  route: RouteLocationNormalized,
  authStore: ReturnType<typeof useAuthStore>
): boolean {
  if (route.meta.permissions && Array.isArray(route.meta.permissions)) {
    return route.meta.permissions.some((permission: string) => authStore.checkPermission(permission))
  }
  if (route.meta.roles && Array.isArray(route.meta.roles)) {
    return authStore.checkRole(route.meta.roles)
  }
  return true
}
