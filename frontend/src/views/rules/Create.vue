<template>
  <div class="placeholder-page">
    <div class="placeholder-content">
      <h1 class="placeholder-title">创建规则</h1>
      <p class="placeholder-description">此功能正在建设中...</p>
      <div class="placeholder-actions">
        <router-link to="/" class="back-link">
          <i class="fa fa-arrow-left"></i>
          返回主页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Rules Creation Placeholder Page
// MVP阶段 - 占位页面，不实现业务逻辑
</script>

<style scoped lang="scss">
.placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f5f5;
  font-family: 'Microsoft YaHei', sans-serif;
}

.placeholder-content {
  text-align: center;
  padding: 60px 40px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.placeholder-title {
  font-size: 32px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 16px;
}

.placeholder-description {
  font-size: 16px;
  color: #666666;
  margin-bottom: 32px;
}

.placeholder-actions {
  display: flex;
  justify-content: center;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #40a9ff;
  color: #ffffff;
  text-decoration: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;

  &:hover {
    background: #1890ff;
    transform: translateY(-1px);
  }

  i {
    font-size: 14px;
  }
}
</style>