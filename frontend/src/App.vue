<template>
  <a-config-provider :theme="themeConfig">
    <div id="app">
      <router-view v-slot="{ Component, route }">
        <transition name="page-transition" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/modules/auth'
import themeConfig from '@/config/theme'

defineOptions({
  name: 'App'
})

const authStore = useAuthStore()

onMounted(() => {
  // 初始化认证状态
  authStore.initAuth()
})
</script>

<style lang="scss">
#app {
  min-height: 100vh;
}
</style>
