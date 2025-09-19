<template>
  <div class="audit-process-monitor">
    <!-- 标题区域 -->
    <div class="monitor-header">
      <h2 class="section-title">检核过程监控</h2>
      <div class="header-actions">
        <a-space>
          <a-button
            :icon="h(ReloadOutlined)"
            :loading="manualRefreshing"
            @click="refreshProgress"
          >
            刷新
          </a-button>
          <a-button
            v-if="canStart"
            type="primary"
            :icon="h(PlayCircleOutlined)"
            :loading="state.loading"
            @click="handleStart"
          >
            开始检核
          </a-button>
          <a-button
            v-if="canStop"
            danger
            :icon="h(PauseCircleOutlined)"
            :loading="state.loading"
            @click="handleStop"
          >
            停止检核
          </a-button>
          <a-button
            v-if="canRestart"
            type="primary"
            :icon="h(RedoOutlined)"
            :loading="state.loading"
            @click="handleRestart"
          >
            重新开始
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="state.error" class="error-section">
      <a-alert
        :message="state.error"
        type="error"
        showIcon
        closable
        @close="clearError"
      />
    </div>

    <!-- 加载状态 -->
    <div v-if="state.loading && !state.task" class="loading-section">
      <a-skeleton active :paragraph="{ rows: 4 }" />
    </div>

    <!-- 主要内容 -->
    <div v-else-if="state.task" class="monitor-content">
      <!-- 状态概览卡片 -->
      <a-row :gutter="[16, 16]" class="status-overview">
        <a-col :xl="6" :lg="6" :md="12" :sm="24">
          <a-card class="status-card" :class="`status-${state.task.status}`">
            <div class="card-content">
              <div class="card-icon">
                <component :is="getStatusIcon(state.task.status)" />
              </div>
              <div class="card-info">
                <div class="card-value">{{ statusText }}</div>
                <div class="card-label">执行状态</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xl="6" :lg="6" :md="12" :sm="24">
          <a-card class="status-card">
            <div class="card-content">
              <div class="card-icon progress-icon">
                <progress-circle
                  :percent="progressPercent"
                  :strokeColor="getProgressColor"
                  :size="32"
                />
              </div>
              <div class="card-info">
                <div class="card-value">{{ progressPercent }}%</div>
                <div class="card-label">完成进度</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xl="6" :lg="6" :md="12" :sm="24">
          <a-card class="status-card">
            <div class="card-content">
              <div class="card-icon">
                <CheckCircleOutlined />
              </div>
              <div class="card-info">
                <div class="card-value">{{ state.task.completedProducts }}/{{ state.task.totalProducts }}</div>
                <div class="card-label">产品检核</div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xl="6" :lg="6" :md="12" :sm="24">
          <a-card class="status-card">
            <div class="card-content">
              <div class="card-icon error-icon">
                <ExclamationCircleOutlined />
              </div>
              <div class="card-info">
                <div class="card-value">{{ state.task.totalErrors }}</div>
                <div class="card-label">检出问题</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 进度详情卡片 -->
      <a-card class="progress-detail-card" title="检核进度详情">
        <div class="progress-section">
          <!-- 进度条 -->
          <div class="progress-bar-section">
            <div class="progress-info">
              <span class="progress-label">{{ progressText }}</span>
              <span class="progress-percent">{{ progressPercent }}%</span>
            </div>
            <a-progress
              :percent="progressPercent"
              :strokeColor="getProgressColor"
              :status="getProgressStatus()"
              strokeLinecap="round"
              :showInfo="false"
            />
          </div>

          <!-- 时间信息 -->
          <div class="time-info-section">
            <a-row :gutter="16">
              <a-col :span="8">
                <div class="time-item">
                  <span class="time-label">开始时间：</span>
                  <span class="time-value">{{ formatTime(state.task.createdTime) }}</span>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="time-item">
                  <span class="time-label">执行时长：</span>
                  <span class="time-value">{{ formatExecutionTime }}</span>
                </div>
              </a-col>
              <a-col :span="8">
                <div class="time-item">
                  <span class="time-label">预计剩余：</span>
                  <span class="time-value">{{ estimatedTimeLeft || '计算中...' }}</span>
                </div>
              </a-col>
            </a-row>
          </div>

          <!-- 完成时间（如果已完成） -->
          <div v-if="state.task.completedTime" class="completion-info">
            <a-divider />
            <div class="completion-time">
              <span class="completion-label">完成时间：</span>
              <span class="completion-value">{{ formatTime(state.task.completedTime) }}</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 实时日志（模拟） -->
      <a-card class="log-card" title="执行日志">
        <template #extra>
          <a-switch
            v-model:checked="autoScrollLog"
            size="small"
            checkedChildren="自动滚动"
            unCheckedChildren="手动滚动"
          />
        </template>
        <div class="log-container" ref="logContainerRef">
          <div v-for="log in logs" :key="log.id" class="log-item" :class="`log-${log.level}`">
            <span class="log-time">{{ formatLogTime(log.timestamp) }}</span>
            <span class="log-level">{{ log.level.toUpperCase() }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
          <div v-if="logs.length === 0" class="no-logs">
            暂无执行日志
          </div>
        </div>
      </a-card>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <a-empty description="未找到检核任务信息" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  RedoOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
  LoadingOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import { useAuditProgress } from '@/composables/audit/useAuditProgress'
import type { AuditProcessMonitorProps, AuditProcessMonitorEvents } from '@/api/types/audit'

// 自定义进度圆环组件
const ProgressCircle = {
  props: {
    percent: { type: Number, default: 0 },
    strokeColor: { type: String, default: '#1890ff' },
    size: { type: Number, default: 24 }
  },
  setup(props: any) {
    const radius = computed(() => (props.size - 4) / 2)
    const circumference = computed(() => 2 * Math.PI * radius.value)
    const offset = computed(() => circumference.value - (props.percent / 100) * circumference.value)

    return { radius, circumference, offset }
  },
  template: `
    <svg :width="size" :height="size" class="progress-circle">
      <circle
        cx="50%"
        cy="50%"
        :r="radius"
        fill="none"
        stroke="#f0f0f0"
        stroke-width="2"
      />
      <circle
        cx="50%"
        cy="50%"
        :r="radius"
        fill="none"
        :stroke="strokeColor"
        stroke-width="2"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="offset"
        transform="rotate(-90 50% 50%)"
        style="transition: stroke-dashoffset 0.3s ease"
      />
    </svg>
  `
}

// Props 定义
interface Props extends AuditProcessMonitorProps {}

// Events 定义
interface Emits extends AuditProcessMonitorEvents {}

const props = withDefaults(defineProps<Props>(), {
  autoRefresh: true,
  refreshInterval: 2000
})

const emit = defineEmits<Emits>()

// 使用检核进度 Composable
const {
  state,
  manualRefreshing,
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
  refreshProgress,
  startTask,
  stopTask,
  restartTask
} = useAuditProgress({
  taskId: props.taskId,
  interval: props.refreshInterval,
  autoStart: props.autoRefresh
})

// 本地状态
const logContainerRef = ref<HTMLElement>()
const autoScrollLog = ref(true)

// 模拟日志数据
const logs = ref<Array<{
  id: string
  timestamp: Date
  level: 'info' | 'warn' | 'error' | 'success'
  message: string
}>>([])

// 事件处理函数
const clearError = () => {
  state.error = null
}

const handleStart = async () => {
  const success = await startTask()
  if (success) {
    addLog('info', '检核任务已启动')
    emit('process-complete', state.task!)
  }
}

const handleStop = async () => {
  const success = await stopTask()
  if (success) {
    addLog('warn', '检核任务已停止')
    emit('stop-task', props.taskId)
  }
}

const handleRestart = async () => {
  const newTaskId = await restartTask()
  if (newTaskId) {
    addLog('info', '检核任务已重新启动')
    emit('restart-task', newTaskId)
  }
}

// 工具函数
const getStatusIcon = (status: string) => {
  const iconMap: Record<string, any> = {
    pending: ClockCircleOutlined,
    running: LoadingOutlined,
    completed: CheckCircleOutlined,
    failed: StopOutlined
  }
  return iconMap[status] || ClockCircleOutlined
}

const getProgressStatus = (): 'normal' | 'exception' | 'success' => {
  if (isFailed.value) return 'exception'
  if (isCompleted.value) return 'success'
  return 'normal'
}

const formatTime = (time: Date | string): string => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const formatLogTime = (time: Date): string => {
  return time.toLocaleTimeString('zh-CN')
}

const addLog = (level: 'info' | 'warn' | 'error' | 'success', message: string) => {
  logs.value.push({
    id: Date.now().toString(),
    timestamp: new Date(),
    level,
    message
  })

  // 限制日志数量
  if (logs.value.length > 100) {
    logs.value = logs.value.slice(-100)
  }

  // 自动滚动到底部
  if (autoScrollLog.value) {
    nextTick(() => {
      if (logContainerRef.value) {
        logContainerRef.value.scrollTop = logContainerRef.value.scrollHeight
      }
    })
  }
}

// 监听任务状态变化，生成对应日志
watch(() => state.task?.status, (newStatus, oldStatus) => {
  if (!newStatus || !oldStatus) return

  if (oldStatus === 'pending' && newStatus === 'running') {
    addLog('info', '开始执行检核任务')
  } else if (oldStatus === 'running' && newStatus === 'completed') {
    addLog('success', '检核任务执行完成')
    emit('process-complete', state.task!)
  } else if (oldStatus === 'running' && newStatus === 'failed') {
    addLog('error', '检核任务执行失败')
    emit('process-error', '检核任务执行失败')
  }
})

// 监听进度变化，生成进度日志
watch(() => state.task?.completedProducts, (newCompleted, oldCompleted) => {
  if (newCompleted && oldCompleted && newCompleted > oldCompleted) {
    addLog('info', `完成产品检核：${newCompleted}/${state.task?.totalProducts}`)
  }
})

// 监听错误数量变化
watch(() => state.task?.totalErrors, (newErrors, oldErrors) => {
  if (newErrors && oldErrors && newErrors > oldErrors) {
    const increment = newErrors - oldErrors
    addLog('warn', `检出新问题 ${increment} 个，累计 ${newErrors} 个`)
  }
})

// 定义组件引用
defineExpose({
  refreshProgress,
  clearError
})
</script>

<style scoped>
.audit-process-monitor {
  padding: 0;
}

.monitor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.error-section {
  margin-bottom: 16px;
}

.loading-section {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.monitor-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-overview {
  margin-bottom: 0;
}

.status-card {
  height: 120px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.status-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.status-card.status-running {
  border-left: 4px solid #1890ff;
}

.status-card.status-completed {
  border-left: 4px solid #52c41a;
}

.status-card.status-failed {
  border-left: 4px solid #f5222d;
}

.status-card.status-pending {
  border-left: 4px solid #faad14;
}

.status-card :deep(.ant-card-body) {
  padding: 20px;
  height: 100%;
  display: flex;
  align-items: center;
}

.card-content {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 16px;
}

.card-icon {
  font-size: 32px;
  color: #1890ff;
  flex-shrink: 0;
}

.card-icon.progress-icon {
  font-size: 24px;
}

.card-icon.error-icon {
  color: #f5222d;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
  line-height: 1.2;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #8c8c8c;
  line-height: 1.4;
}

.progress-detail-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.progress-section {
  padding: 8px 0;
}

.progress-bar-section {
  margin-bottom: 24px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  color: #595959;
}

.progress-percent {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.time-info-section {
  margin-bottom: 16px;
}

.time-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-label {
  font-size: 12px;
  color: #8c8c8c;
}

.time-value {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.completion-info {
  margin-top: 16px;
}

.completion-time {
  text-align: center;
  padding: 8px 0;
}

.completion-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-right: 8px;
}

.completion-value {
  font-size: 14px;
  color: #52c41a;
  font-weight: 500;
}

.log-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.log-container {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px;
  background: #fafafa;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.log-item {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
  line-height: 1.5;
  word-break: break-all;
}

.log-time {
  color: #8c8c8c;
  flex-shrink: 0;
  width: 80px;
}

.log-level {
  font-weight: 600;
  flex-shrink: 0;
  width: 60px;
}

.log-message {
  flex: 1;
  color: #262626;
}

.log-info .log-level {
  color: #1890ff;
}

.log-warn .log-level {
  color: #faad14;
}

.log-error .log-level {
  color: #f5222d;
}

.log-success .log-level {
  color: #52c41a;
}

.no-logs {
  text-align: center;
  color: #8c8c8c;
  font-style: italic;
  padding: 40px 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.progress-circle {
  display: block;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .monitor-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .section-title {
    text-align: center;
  }

  .status-card {
    height: 100px;
  }

  .status-card :deep(.ant-card-body) {
    padding: 16px;
  }

  .card-value {
    font-size: 20px;
  }

  .card-icon {
    font-size: 24px;
  }

  .time-info-section .ant-col {
    margin-bottom: 16px;
  }

  .log-container {
    height: 200px;
  }

  .log-time {
    width: 60px;
  }

  .log-level {
    width: 50px;
  }
}

@media (max-width: 576px) {
  .monitor-header {
    padding: 12px 16px;
  }

  .section-title {
    font-size: 18px;
  }

  .status-card {
    height: 80px;
  }

  .card-content {
    gap: 12px;
  }

  .card-value {
    font-size: 18px;
  }

  .card-icon {
    font-size: 20px;
  }
}
</style>