<template>
  <div class="rule-success">
    <div class="success-content">
      <a-result
        status="success"
        title="规则创建成功！"
        sub-title="您的规则已成功创建并保存到系统中。"
      >
        <template #extra>
          <a-space>
            <a-button type="primary" @click="handleGoToList">
              返回规则列表
            </a-button>
            <a-button @click="handleCreateNew">
              继续创建规则
            </a-button>
          </a-space>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回规则列表
const handleGoToList = () => {
  router.push('/rules')
}

// 继续创建新规则
const handleCreateNew = () => {
  router.push('/rules/new')
}
</script>

<style scoped lang="scss">
.rule-success {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;

  .success-content {
    background: white;
    border-radius: 6px;
    padding: 48px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    max-width: 600px;
    width: 100%;
  }
}
</style>