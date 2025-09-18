/**
 * 检核进度监控Composable
 * 实现检核过程监控的业务逻辑
 */

import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { auditApi } from '@/api/modules/audit'
import type { AuditTask, AuditProgressState } from '@/api/types/audit'

export interface UseAuditProgressOptions {
  taskId: string
  interval?: number
  autoStart?: boolean
}

export function useAuditProgress(options: UseAuditProgressOptions) {
  const { taskId, interval = 2000, autoStart = true } = options

  // ==================== 响应式状态 ====================

  const state = reactive<AuditProgressState>({
    task: null,
    loading: false,
    error: null,
    isRunning: false
  })

  // 定时器引用
  const intervalId = ref<NodeJS.Timeout | null>(null)

  // 手动刷新控制
  const manualRefreshing = ref(false)

  // ==================== 计算属性 ====================

  // 进度百分比
  const progressPercent = computed((): number => {
    return state.task?.progressPercent || 0
  })

  // 是否完成
  const isCompleted = computed((): boolean => {
    return state.task?.status === 'completed'
  })

  // 是否失败
  const isFailed = computed((): boolean => {
    return state.task?.status === 'failed'
  })

  // 是否正在执行
  const isRunning = computed((): boolean => {
    return state.task?.status === 'running'
  })

  // 是否可以启动
  const canStart = computed((): boolean => {
    return state.task?.status === 'pending' && !state.loading
  })

  // 是否可以停止
  const canStop = computed((): boolean => {
    return state.task?.status === 'running' && !state.loading
  })

  // 是否可以重新开始
  const canRestart = computed((): boolean => {
    return (state.task?.status === 'completed' || state.task?.status === 'failed') && !state.loading
  })

  // 进度状态文本
  const statusText = computed((): string => {
    if (!state.task) return '获取状态中...'

    switch (state.task.status) {
      case 'pending':
        return '等待开始'
      case 'running':
        return '检核进行中...'
      case 'completed':
        return '检核完成'
      case 'failed':
        return '检核失败'
      default:
        return '未知状态'
    }
  })

  // 进度文本
  const progressText = computed((): string => {
    if (!state.task) return ''

    const { completedProducts, totalProducts, totalErrors } = state.task
    return `已检核 ${completedProducts}/${totalProducts} 个产品，检出 ${totalErrors} 个问题`
  })

  // 预估剩余时间
  const estimatedTimeLeft = computed((): string => {
    if (!state.task || !isRunning.value) return ''

    const { completedProducts, totalProducts, createdTime } = state.task
    if (completedProducts === 0) return '计算中...'

    const elapsed = Date.now() - new Date(createdTime).getTime()
    const avgTimePerProduct = elapsed / completedProducts
    const remainingProducts = totalProducts - completedProducts
    const estimatedTimeLeftMs = remainingProducts * avgTimePerProduct

    const minutes = Math.ceil(estimatedTimeLeftMs / 60000)
    if (minutes <= 1) return '即将完成'
    if (minutes < 60) return `约 ${minutes} 分钟`

    const hours = Math.floor(minutes / 60)
    const remainingMinutes = minutes % 60
    return `约 ${hours} 小时 ${remainingMinutes} 分钟`
  })

  // ==================== 数据获取方法 ====================

  /**
   * 获取任务进度
   */
  const fetchProgress = async (showLoading = false): Promise<void> => {
    try {
      if (showLoading) {
        state.loading = true
      }

      const response = await auditApi.getTaskProgress(taskId)

      if (response.success && response.data) {
        const previousStatus = state.task?.status
        state.task = response.data
        state.error = null
        state.isRunning = response.data.status === 'running'

        // 状态变更通知
        if (previousStatus && previousStatus !== response.data.status) {
          handleStatusChange(previousStatus, response.data.status)
        }
      } else {
        state.error = response.message || '获取进度失败'
        message.error(state.error)
      }
    } catch (error: any) {
      console.error('Fetch progress error:', error)
      state.error = error.message || '获取进度失败'

      // 避免频繁显示同样的错误提示
      if (!state.error.includes('网络')) {
        message.error(state.error)
      }
    } finally {
      if (showLoading) {
        state.loading = false
      }
      manualRefreshing.value = false
    }
  }

  /**
   * 手动刷新进度
   */
  const refreshProgress = async (): Promise<void> => {
    if (manualRefreshing.value) return

    manualRefreshing.value = true
    await fetchProgress(true)
  }

  // ==================== 任务控制方法 ====================

  /**
   * 启动检核任务
   */
  const startTask = async (): Promise<boolean> => {
    if (!canStart.value) return false

    try {
      state.loading = true
      const response = await auditApi.startAuditTask(taskId)

      if (response.success) {
        message.success('检核任务已启动')
        await fetchProgress()
        startPolling()
        return true
      } else {
        message.error(response.message || '启动任务失败')
        return false
      }
    } catch (error: any) {
      console.error('Start task error:', error)
      message.error('启动任务失败：' + (error.message || '未知错误'))
      return false
    } finally {
      state.loading = false
    }
  }

  /**
   * 停止检核任务
   */
  const stopTask = async (): Promise<boolean> => {
    if (!canStop.value) return false

    try {
      state.loading = true
      const response = await auditApi.stopAuditTask(taskId)

      if (response.success) {
        message.success('检核任务已停止')
        await fetchProgress()
        stopPolling()
        return true
      } else {
        message.error(response.message || '停止任务失败')
        return false
      }
    } catch (error: any) {
      console.error('Stop task error:', error)
      message.error('停止任务失败：' + (error.message || '未知错误'))
      return false
    } finally {
      state.loading = false
    }
  }

  /**
   * 重新开始检核任务
   */
  const restartTask = async (): Promise<string | null> => {
    if (!canRestart.value) return null

    try {
      state.loading = true
      const response = await auditApi.restartAuditTask(taskId)

      if (response.success && response.data) {
        message.success('检核任务已重新启动')
        // 注意：重新启动会产生新的taskId
        return response.data.taskId
      } else {
        message.error(response.message || '重新启动失败')
        return null
      }
    } catch (error: any) {
      console.error('Restart task error:', error)
      message.error('重新启动失败：' + (error.message || '未知错误'))
      return null
    } finally {
      state.loading = false
    }
  }

  // ==================== 轮询控制 ====================

  /**
   * 开始轮询
   */
  const startPolling = (): void => {
    if (intervalId.value) return // 避免重复启动

    intervalId.value = setInterval(async () => {
      if (state.task && (state.task.status === 'running')) {
        await fetchProgress()
      } else {
        // 任务完成或失败时停止轮询
        stopPolling()
      }
    }, interval)
  }

  /**
   * 停止轮询
   */
  const stopPolling = (): void => {
    if (intervalId.value) {
      clearInterval(intervalId.value)
      intervalId.value = null
    }
  }

  // ==================== 状态变更处理 ====================

  /**
   * 处理状态变更
   */
  const handleStatusChange = (
    previousStatus: string,
    currentStatus: string
  ): void => {
    // 从运行中变为完成
    if (previousStatus === 'running' && currentStatus === 'completed') {
      message.success('检核任务已完成')
      stopPolling()
    }
    // 从运行中变为失败
    else if (previousStatus === 'running' && currentStatus === 'failed') {
      message.error('检核任务执行失败')
      stopPolling()
    }
    // 从待执行变为运行中
    else if (previousStatus === 'pending' && currentStatus === 'running') {
      startPolling()
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 格式化执行时间
   */
  const formatExecutionTime = computed((): string => {
    if (!state.task?.createdTime) return ''

    const start = new Date(state.task.createdTime)
    const end = state.task.completedTime ? new Date(state.task.completedTime) : new Date()
    const duration = end.getTime() - start.getTime()

    const hours = Math.floor(duration / 3600000)
    const minutes = Math.floor((duration % 3600000) / 60000)
    const seconds = Math.floor((duration % 60000) / 1000)

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
    } else {
      return `${minutes}:${seconds.toString().padStart(2, '0')}`
    }
  })

  /**
   * 获取进度条颜色
   */
  const getProgressColor = computed((): string => {
    if (!state.task) return '#1890ff'

    switch (state.task.status) {
      case 'completed':
        return '#52c41a' // 绿色
      case 'failed':
        return '#f5222d' // 红色
      case 'running':
        return '#1890ff' // 蓝色
      default:
        return '#d9d9d9' // 灰色
    }
  })

  // ==================== 生命周期 ====================

  onMounted(async () => {
    // 初始加载
    await fetchProgress(true)

    // 自动开始轮询（如果任务正在运行）
    if (autoStart && state.task?.status === 'running') {
      startPolling()
    }
  })

  onUnmounted(() => {
    stopPolling()
  })

  // ==================== 返回接口 ====================

  return {
    // 状态
    state,
    manualRefreshing,

    // 计算属性
    progressPercent,
    isCompleted,
    isFailed,
    isRunning,
    canStart,
    canStop,
    canRestart,
    statusText,
    progressText,
    estimatedTimeLeft,
    formatExecutionTime,
    getProgressColor,

    // 数据获取
    fetchProgress,
    refreshProgress,

    // 任务控制
    startTask,
    stopTask,
    restartTask,

    // 轮询控制
    startPolling,
    stopPolling
  }
}