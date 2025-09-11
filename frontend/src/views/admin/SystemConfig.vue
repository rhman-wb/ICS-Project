<template>
  <div class="system-config">
    <a-card title="系统配置" :bordered="false">
      <p>系统配置页面 - 开发中</p>
      <p>仅管理员可访问</p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'SystemConfig'
})
</script>

<style lang="scss" scoped>
.system-config {
  padding: 24px;
}
</style>