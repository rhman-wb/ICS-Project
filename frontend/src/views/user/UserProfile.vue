<template>
  <div class="user-profile">
    <a-card title="个人资料" :bordered="false">
      <p>个人资料页面 - 开发中</p>
      <p>当前用户: {{ userInfo?.realName || userInfo?.username }}</p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'

defineOptions({
  name: 'UserProfile'
})

const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)
</script>

<style lang="scss" scoped>
.user-profile {
  padding: 24px;
}
</style>