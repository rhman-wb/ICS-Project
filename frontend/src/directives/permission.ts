import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'

/**
 * 权限验证指令
 * 用法：
 * v-permission="'user:create'" - 检查单个权限
 * v-permission="['user:create', 'user:update']" - 检查多个权限（OR关系）
 * v-permission.all="['user:create', 'user:update']" - 检查多个权限（AND关系）
 * v-permission:role="'admin'" - 检查角色
 * v-permission:role="['admin', 'manager']" - 检查多个角色
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding)
  }
}

function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const authStore = useAuthStore()
  const { value, modifiers, arg } = binding
  
  if (!value) {
    console.warn('v-permission directive requires a value')
    return
  }

  let hasPermission = false

  if (arg === 'role') {
    // 角色检查
    if (Array.isArray(value)) {
      hasPermission = value.some(role => authStore.hasRole(role))
    } else {
      hasPermission = authStore.hasRole(value)
    }
  } else {
    // 权限检查
    if (Array.isArray(value)) {
      if (modifiers.all) {
        // AND关系：需要拥有所有权限
        hasPermission = value.every(permission => authStore.hasPermission(permission))
      } else {
        // OR关系：拥有任一权限即可
        hasPermission = value.some(permission => authStore.hasPermission(permission))
      }
    } else {
      hasPermission = authStore.hasPermission(value)
    }
  }

  if (!hasPermission) {
    // 移除元素或隐藏元素
    if (modifiers.hide) {
      el.style.display = 'none'
    } else {
      el.remove()
    }
  } else {
    // 确保元素可见
    if (el.style.display === 'none') {
      el.style.display = ''
    }
  }
}

export default permission