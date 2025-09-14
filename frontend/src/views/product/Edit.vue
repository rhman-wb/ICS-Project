<template>
  <div class="product-edit-placeholder">
    <a-result
      status="info"
      title="产品编辑页面"
      sub-title="此功能正在开发中..."
    >
      <template #extra>
        <a-space>
          <a-button @click="handleGoBack">返回</a-button>
          <a-button type="primary" @click="goToProductManagement">返回产品列表</a-button>
        </a-space>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const handleGoBack = () => {
  router.back()
}

const goToProductManagement = () => {
  router.push('/product-management')
}
</script>

<style scoped>
.product-edit-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f5f5;
}
</style>