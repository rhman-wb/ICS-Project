<template>
  <div class="rule-selection-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-page-header
        :ghost="false"
        title="智能检核"
        sub-title="选择检核规则"
        @back="handleBack"
      >
        <template #extra>
          <a-space>
            <a-button @click="handleCancel">取消</a-button>
            <a-button type="primary" :disabled="!canProceed" @click="handleNext">
              下一步
            </a-button>
          </a-space>
        </template>
      </a-page-header>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">
      <AuditRuleSelection
        :product-ids="productIds"
        :visible="true"
        :loading="pageLoading"
        @rule-selected="handleRuleSelected"
        @cancel="handleCancel"
        @search="handleSearch"
        @reset="handleReset"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import AuditRuleSelection from '@/components/audit/AuditRuleSelection.vue'
import type { AuditRuleQueryParams } from '@/api/types/audit'

// 路由相关
const router = useRouter()
const route = useRoute()

// 页面状态
const pageLoading = ref(false)
const selectedRuleIds = ref<string[]>([])

// 从路由参数获取产品ID
const productIds = computed(() => {
  const ids = route.query.productIds
  if (typeof ids === 'string') {
    return ids.split(',')
  }
  return Array.isArray(ids) ? ids : []
})

// 计算是否可以继续
const canProceed = computed(() => {
  return selectedRuleIds.value.length > 0
})

// 事件处理
const handleBack = () => {
  router.back()
}

const handleCancel = () => {
  router.push('/product')
}

const handleRuleSelected = (ruleIds: string[]) => {
  selectedRuleIds.value = ruleIds
  message.success(`已选择 ${ruleIds.length} 条检核规则`)
}

const handleNext = () => {
  if (!canProceed.value) {
    message.warning('请先选择检核规则')
    return
  }

  // 跳转到检核过程监控页面
  router.push({
    path: '/audit/process-monitor',
    query: {
      productIds: productIds.value.join(','),
      ruleIds: selectedRuleIds.value.join(',')
    }
  })
}

const handleSearch = (params: AuditRuleQueryParams) => {
  console.log('Search rules:', params)
}

const handleReset = () => {
  console.log('Reset rules filter')
}

// 页面初始化
onMounted(() => {
  if (productIds.value.length === 0) {
    message.error('请先选择要检核的产品')
    router.push('/product')
  }
})
</script>

<style scoped>
.rule-selection-page {
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