import { ref, computed, type Ref } from 'vue'
import { getLoadingState } from '@/api/modules/auth'

// 全局加载状态管理
const globalLoadingStates = ref<Record<string, boolean>>({})

// API加载状态组合式函数
export function useApiLoading() {
  // 获取特定API的加载状态
  const getApiLoadingState = (apiKey: string): boolean => {
    return getLoadingState(apiKey) || globalLoadingStates.value[apiKey] || false
  }

  // 设置API加载状态
  const setApiLoadingState = (apiKey: string, loading: boolean): void => {
    globalLoadingStates.value[apiKey] = loading
  }

  // 创建响应式的加载状态
  const createLoadingRef = (apiKey: string): Ref<boolean> => {
    const loadingRef = ref(false)
    
    // 监听加载状态变化
    const updateLoading = () => {
      loadingRef.value = getApiLoadingState(apiKey)
    }
    
    // 定期更新状态（简单实现，实际项目中可以使用更高效的方式）
    const interval = setInterval(updateLoading, 100)
    
    // 清理函数
    const cleanup = () => {
      clearInterval(interval)
    }
    
    return loadingRef
  }

  // 认证相关的加载状态
  const loginLoading = computed(() => getApiLoadingState('login'))
  const logoutLoading = computed(() => getApiLoadingState('logout'))
  const refreshTokenLoading = computed(() => getApiLoadingState('refreshToken'))
  const getUserInfoLoading = computed(() => getApiLoadingState('getUserInfo'))
  const getUserPermissionsLoading = computed(() => getApiLoadingState('getUserPermissions'))
  const changePasswordLoading = computed(() => getApiLoadingState('changePassword'))

  // 任何认证API正在加载
  const anyAuthLoading = computed(() => 
    loginLoading.value || 
    logoutLoading.value || 
    refreshTokenLoading.value || 
    getUserInfoLoading.value || 
    getUserPermissionsLoading.value ||
    changePasswordLoading.value
  )

  return {
    // 通用方法
    getApiLoadingState,
    setApiLoadingState,
    createLoadingRef,
    
    // 认证相关加载状态
    loginLoading,
    logoutLoading,
    refreshTokenLoading,
    getUserInfoLoading,
    getUserPermissionsLoading,
    changePasswordLoading,
    anyAuthLoading
  }
}

// 全局加载状态管理器
export class ApiLoadingManager {
  private static instance: ApiLoadingManager
  private loadingStates = new Map<string, boolean>()
  private listeners = new Map<string, Set<(loading: boolean) => void>>()

  static getInstance(): ApiLoadingManager {
    if (!this.instance) {
      this.instance = new ApiLoadingManager()
    }
    return this.instance
  }

  // 设置加载状态
  setLoading(key: string, loading: boolean): void {
    this.loadingStates.set(key, loading)
    this.notifyListeners(key, loading)
  }

  // 获取加载状态
  getLoading(key: string): boolean {
    return this.loadingStates.get(key) || false
  }

  // 添加状态监听器
  addListener(key: string, listener: (loading: boolean) => void): void {
    if (!this.listeners.has(key)) {
      this.listeners.set(key, new Set())
    }
    this.listeners.get(key)!.add(listener)
  }

  // 移除状态监听器
  removeListener(key: string, listener: (loading: boolean) => void): void {
    const listeners = this.listeners.get(key)
    if (listeners) {
      listeners.delete(listener)
      if (listeners.size === 0) {
        this.listeners.delete(key)
      }
    }
  }

  // 通知监听器
  private notifyListeners(key: string, loading: boolean): void {
    const listeners = this.listeners.get(key)
    if (listeners) {
      listeners.forEach(listener => listener(loading))
    }
  }

  // 清除所有状态
  clear(): void {
    this.loadingStates.clear()
    this.listeners.clear()
  }
}

// 导出单例实例
export const apiLoadingManager = ApiLoadingManager.getInstance()