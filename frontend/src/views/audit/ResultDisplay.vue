<template>
  <div class="result-display-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-page-header
        :ghost="false"
        title="检核结果"
        sub-title="产品检核结果详情"
        @back="handleBack"
      >
        <template #extra>
          <a-space>
            <a-button @click="handleViewHistory">历史记录</a-button>
            <a-button @click="handleBack">返回</a-button>
          </a-space>
        </template>
      </a-page-header>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">
      <AuditResultDisplay
        v-if="taskId"
        :task-id="taskId"
        :show-export="true"
        @export-results="handleExportResults"
        @back-to-list="handleBackToList"
        @view-detail="handleViewDetail"
        @filter-change="handleFilterChange"
      />

      <!-- 空状态 -->
      <div v-else class="empty-container">
        <a-empty description="未找到检核任务">
          <a-button type="primary" @click="handleBackToList">
            返回产品列表
          </a-button>
        </a-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import AuditResultDisplay from '@/components/audit/AuditResultDisplay.vue'
import type { AuditResultQueryParams } from '@/api/types/audit'

// 路由相关
const router = useRouter()
const route = useRoute()

// 页面状态
const taskId = computed(() => route.query.taskId as string)

// 事件处理
const handleBack = () => {
  router.back()
}

const handleBackToList = () => {
  router.push('/product')
}

const handleViewHistory = () => {
  router.push('/audit/history')
}

const handleExportResults = (format: 'excel' | 'pdf') => {
  message.success(`开始导出${format.toUpperCase()}格式的检核结果`)
}

const handleViewDetail = (resultId: string) => {
  console.log('View result detail:', resultId)
}

const handleFilterChange = (params: AuditResultQueryParams) => {
  console.log('Filter change:', params)
}

// 页面初始化
onMounted(() => {
  if (!taskId.value) {
    message.error('缺少任务ID参数')
    router.push('/product')
  }
})
</script>

<style scoped>
.result-display-page {
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

.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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