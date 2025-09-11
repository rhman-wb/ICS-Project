<template>
  <div class="forgot-password-container">
    <a-card title="忘记密码" :bordered="false">
      <p>忘记密码页面 - 开发中</p>
      <a-button @click="goToLogin">返回登录</a-button>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

defineOptions({
  name: 'ForgotPassword'
})

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.forgot-password-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 24px;
}
</style>