<template>
  <div class="process-monitor-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-page-header
        :ghost="false"
        title="智能检核"
        sub-title="检核过程监控"
        @back="handleBack"
      >
        <template #extra>
          <a-space>
            <a-button @click="handleViewResults" :disabled="!isCompleted">
              查看结果
            </a-button>
            <a-button @click="handleBack">返回</a-button>
          </a-space>
        </template>
      </a-page-header>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">
      <AuditProcessMonitor
        v-if="taskId"
        :task-id="taskId"
        :auto-refresh="true"
        :refresh-interval="2000"
        @process-complete="handleProcessComplete"
        @process-error="handleProcessError"
        @stop-task="handleStopTask"
        @restart-task="handleRestartTask"
      />

      <!-- 加载状态 -->
      <div v-else class="loading-container">
        <a-spin size="large">
          <div class="loading-text">正在创建检核任务...</div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import AuditProcessMonitor from '@/components/audit/AuditProcessMonitor.vue'
import { auditApi } from '@/api/modules/audit'
import type { AuditTask } from '@/api/types/audit'

// 路由相关
const router = useRouter()
const route = useRoute()

// 页面状态
const taskId = ref<string>('')
const currentTask = ref<AuditTask | null>(null)
const creating = ref(false)

// 从路由参数获取数据
const productIds = computed(() => {
  const ids = route.query.productIds
  if (typeof ids === 'string') {
    return ids.split(',')
  }
  return Array.isArray(ids) ? ids : []
})

const ruleIds = computed(() => {
  const ids = route.query.ruleIds
  if (typeof ids === 'string') {
    return ids.split(',')
  }
  return Array.isArray(ids) ? ids : []
})

// 计算任务是否完成
const isCompleted = computed(() => {
  return currentTask.value?.status === 'completed'
})

// 事件处理
const handleBack = () => {
  router.push('/product')
}

const handleProcessComplete = (task: AuditTask) => {
  currentTask.value = task
  message.success('检核任务执行完成')
}

const handleProcessError = (error: string) => {
  message.error(`检核任务执行失败：${error}`)
}

const handleStopTask = (stoppedTaskId: string) => {
  message.info('检核任务已停止')
}

const handleRestartTask = (newTaskId: string) => {
  taskId.value = newTaskId
  message.info('检核任务已重新启动')
}

const handleViewResults = () => {
  if (!taskId.value) {
    message.warning('未找到检核任务')
    return
  }

  router.push({
    path: '/audit/results',
    query: {
      taskId: taskId.value
    }
  })
}

// 创建检核任务
const createAuditTask = async () => {
  if (productIds.value.length === 0 || ruleIds.value.length === 0) {
    message.error('缺少必要的参数')
    router.push('/audit/rule-selection')
    return
  }

  try {
    creating.value = true

    const response = await auditApi.createAuditTask({
      productIds: productIds.value,
      ruleIds: ruleIds.value
    })

    if (response.success && response.data) {
      taskId.value = response.data.taskId
      message.success('检核任务创建成功')
    } else {
      message.error(response.message || '创建检核任务失败')
      router.push('/audit/rule-selection')
    }
  } catch (error: any) {
    console.error('Create audit task error:', error)
    message.error('创建检核任务失败：' + (error.message || '未知错误'))
    router.push('/audit/rule-selection')
  } finally {
    creating.value = false
  }
}

// 页面初始化
onMounted(async () => {
  // 检查是否有现有的taskId参数
  const existingTaskId = route.query.taskId as string
  if (existingTaskId) {
    taskId.value = existingTaskId
  } else {
    // 创建新的检核任务
    await createAuditTask()
  }
})
</script>

<style scoped>
.process-monitor-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-header {
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.page-content {
  flex: 1;
  padding: 24px;
  background: #f5f5f5;
  overflow-y: auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
  color: #595959;
}

:deep(.ant-page-header) {
  padding: 16px 24px;
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px;
  }

  :deep(.ant-page-header) {
    padding: 12px 16px;
  }
}
</style>