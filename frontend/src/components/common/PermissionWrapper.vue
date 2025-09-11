<template>
  <div v-if="hasAccess" :class="wrapperClass">
    <slot />
  </div>
  <div v-else-if="showFallback" :class="fallbackClass">
    <slot name="fallback">
      <div class="permission-denied">
        <ExclamationCircleOutlined class="permission-denied__icon" />
        <span class="permission-denied__text">{{ fallbackText }}</span>
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/modules/auth'

interface Props {
  // 权限列表
  permissions?: string | string[]
  // 角色列表
  roles?: string | string[]
  // 权限检查模式：'some' 表示拥有任一权限即可，'every' 表示需要拥有所有权限
  mode?: 'some' | 'every'
  // 是否显示无权限时的占位内容
  showFallback?: boolean
  // 无权限时的提示文字
  fallbackText?: string
  // 包装器样式类
  wrapperClass?: string
  // 占位内容样式类
  fallbackClass?: string
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'some',
  showFallback: false,
  fallbackText: '暂无权限访问',
  wrapperClass: '',
  fallbackClass: ''
})

const authStore = useAuthStore()

// 检查权限
const hasPermissions = computed(() => {
  if (!props.permissions) return true
  
  const permissions = Array.isArray(props.permissions) ? props.permissions : [props.permissions]
  
  if (props.mode === 'every') {
    return permissions.every(permission => authStore.hasPermission(permission))
  } else {
    return permissions.some(permission => authStore.hasPermission(permission))
  }
})

// 检查角色
const hasRoles = computed(() => {
  if (!props.roles) return true
  
  const roles = Array.isArray(props.roles) ? props.roles : [props.roles]
  
  if (props.mode === 'every') {
    return roles.every(role => authStore.hasRole(role))
  } else {
    return roles.some(role => authStore.hasRole(role))
  }
})

// 是否有访问权限
const hasAccess = computed(() => {
  return hasPermissions.value && hasRoles.value
})
</script>

<style lang="scss" scoped>
.permission-denied {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #8c8c8c;
  font-size: 14px;
  
  &__icon {
    margin-right: 8px;
    font-size: 16px;
  }
  
  &__text {
    line-height: 1.5;
  }
}
</style>