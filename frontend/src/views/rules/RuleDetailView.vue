<template>
  <div class="rule-detail">
    <div class="page-header">
      <div class="header-left">
        <a-button type="link" class="back-button" @click="goBack">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title">规则详情</h1>
      </div>
      <div class="header-right">
        <a-button type="primary" @click="goEdit">编辑</a-button>
      </div>
    </div>

    <a-card :loading="loading" title="基本信息">
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="规则ID">{{ ruleId }}</a-descriptions-item>
        <a-descriptions-item label="名称">{{ detail.name || '-' }}</a-descriptions-item>
        <a-descriptions-item label="类型">{{ detail.type || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detail.createdAt || '-' }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const ruleId = route.params.id as string
const detail = ref<Record<string, any>>({})

onMounted(async () => {
  loading.value = true
  try {
    // TODO: 接入真实接口：rulesApi.getDetail(ruleId)
    detail.value = {
      name: `示例规则 ${ruleId}`,
      type: 'single',
      createdAt: new Date().toLocaleString(),
      description: '这是占位的规则详情页面，用于打通路由与视图。'
    }
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.push('/rules')
}

const goEdit = () => {
  router.push(`/rules/edit/${ruleId}`)
}
</script>

<style scoped>
.rule-detail {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.back-button {
  padding: 0;
}
</style>


