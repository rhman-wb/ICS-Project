<template>
  <div class="user-management">
    <a-card title="用户管理" :bordered="false">
      <p>用户管理页面 - 开发中</p>
      <p>仅管理员可访问</p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'UserManagement'
})
</script>

<style lang="scss" scoped>
.user-management {
  padding: 24px;
}
</style>