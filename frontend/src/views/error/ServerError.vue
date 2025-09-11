<template>
  <div class="server-error-page">
    <div class="server-error-content">
      <div class="server-error-icon">
        <ExclamationTriangleOutlined />
      </div>
      
      <h1 class="server-error-title">500</h1>
      <h2 class="server-error-subtitle">服务器错误</h2>
      
      <p class="server-error-description">
        抱歉，服务器遇到了一些问题。我们正在努力修复中。
      </p>
      
      <div class="server-error-actions">
        <a-button type="primary" @click="refresh">
          <ReloadOutlined />
          刷新页面
        </a-button>
        
        <a-button @click="goHome">
          <HomeOutlined />
          返回首页
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { 
  ExclamationTriangleOutlined, 
  ReloadOutlined, 
  HomeOutlined 
} from '@ant-design/icons-vue'

const router = useRouter()

const refresh = () => {
  window.location.reload()
}

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.server-error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.server-error-content {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 48px;
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.server-error-icon {
  font-size: 72px;
  color: #ff4d4f;
  margin-bottom: 24px;
}

.server-error-title {
  font-size: 72px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 16px 0;
  line-height: 1;
}

.server-error-subtitle {
  font-size: 24px;
  font-weight: 500;
  color: #595959;
  margin: 0 0 24px 0;
}

.server-error-description {
  font-size: 16px;
  color: #8c8c8c;
  line-height: 1.6;
  margin-bottom: 32px;
}

.server-error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  
  .ant-btn {
    min-width: 120px;
  }
}

@media (max-width: 768px) {
  .server-error-content {
    padding: 32px 24px;
  }
  
  .server-error-title {
    font-size: 56px;
  }
  
  .server-error-subtitle {
    font-size: 20px;
  }
  
  .server-error-actions {
    flex-direction: column;
    align-items: center;
    
    .ant-btn {
      width: 100%;
      max-width: 200px;
    }
  }
}
</style>