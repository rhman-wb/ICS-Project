<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="not-found-icon">
        <FileSearchOutlined />
      </div>
      
      <h1 class="not-found-title">404</h1>
      <h2 class="not-found-subtitle">页面不存在</h2>
      
      <p class="not-found-description">
        抱歉，您访问的页面不存在或已被移除。
      </p>
      
      <div class="not-found-actions">
        <a-button type="primary" @click="goHome">
          <HomeOutlined />
          返回首页
        </a-button>
        
        <a-button @click="goBack">
          <ArrowLeftOutlined />
          返回上一页
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { 
  FileSearchOutlined, 
  HomeOutlined, 
  ArrowLeftOutlined 
} from '@ant-design/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard/home')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    goHome()
  }
}
</script>

<style lang="scss" scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.not-found-content {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 48px;
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.not-found-icon {
  font-size: 72px;
  color: #faad14;
  margin-bottom: 24px;
}

.not-found-title {
  font-size: 72px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 16px 0;
  line-height: 1;
}

.not-found-subtitle {
  font-size: 24px;
  font-weight: 500;
  color: #595959;
  margin: 0 0 24px 0;
}

.not-found-description {
  font-size: 16px;
  color: #8c8c8c;
  line-height: 1.6;
  margin-bottom: 32px;
}

.not-found-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  
  .ant-btn {
    min-width: 120px;
  }
}

@media (max-width: 768px) {
  .not-found-content {
    padding: 32px 24px;
  }
  
  .not-found-title {
    font-size: 56px;
  }
  
  .not-found-subtitle {
    font-size: 20px;
  }
  
  .not-found-actions {
    flex-direction: column;
    align-items: center;
    
    .ant-btn {
      width: 100%;
      max-width: 200px;
    }
  }
}
</style>