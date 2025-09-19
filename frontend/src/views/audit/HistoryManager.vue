<template>
  <div class="history-manager-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-page-header
        :ghost="false"
        title="检核历史"
        sub-title="历史记录管理"
        @back="handleBack"
      >
        <template #extra>
          <a-space>
            <a-button type="primary" @click="handleStartNewAudit">
              新建检核
            </a-button>
            <a-button @click="handleBack">返回</a-button>
          </a-space>
        </template>
      </a-page-header>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">
      <AuditHistoryManager
        :product-id="productId"
        :show-actions="true"
        @view-history="handleViewHistory"
        @delete-record="handleDeleteRecord"
        @download-report="handleDownloadReport"
        @search="handleSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import AuditHistoryManager from '@/components/audit/AuditHistoryManager.vue'
import type { AuditHistoryQueryParams } from '@/api/types/audit'

// 路由相关
const router = useRouter()
const route = useRoute()

// 页面状态
const productId = computed(() => route.query.productId as string)

// 事件处理
const handleBack = () => {
  router.back()
}

const handleStartNewAudit = () => {
  router.push('/audit/rule-selection')
}

const handleViewHistory = (historyId: string) => {
  console.log('View history:', historyId)
  message.info(`查看历史记录：${historyId}`)
}

const handleDeleteRecord = (historyId: string) => {
  console.log('Delete record:', historyId)
  message.success('删除历史记录成功')
}

const handleDownloadReport = (downloadUrl: string) => {
  console.log('Download report:', downloadUrl)
  message.success('开始下载检核报告')
}

const handleSearch = (params: AuditHistoryQueryParams) => {
  console.log('Search history:', params)
}
</script>

<style scoped>
.history-manager-page {
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