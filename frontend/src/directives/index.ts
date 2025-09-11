import type { App } from 'vue'
import permission from './permission'

/**
 * 注册全局指令
 */
export function setupDirectives(app: App) {
  app.directive('permission', permission)
}

export { permission }