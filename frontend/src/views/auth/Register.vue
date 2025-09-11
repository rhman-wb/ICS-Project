<template>
  <div class="register-container">
    <a-card title="用户注册" :bordered="false">
      <p>用户注册页面 - 开发中</p>
      <a-button @click="goToLogin">返回登录</a-button>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

defineOptions({
  name: 'Register'
})

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 24px;
}
</style>