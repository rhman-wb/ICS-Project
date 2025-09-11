<template>
  <div class="forbidden-page">
    <div class="forbidden-content">
      <div class="forbidden-icon">
        <ExclamationCircleOutlined />
      </div>
      
      <h1 class="forbidden-title">403</h1>
      <h2 class="forbidden-subtitle">访问被拒绝</h2>
      
      <p class="forbidden-description">
        抱歉，您没有权限访问此页面。请联系管理员获取相应权限。
      </p>
      
      <div class="forbidden-details" v-if="showDetails">
        <p><strong>当前用户：</strong>{{ userInfo?.realName || userInfo?.username || '未知' }}</p>
        <p><strong>用户角色：</strong>{{ userRoles }}</p>
        <p><strong>请求路径：</strong>{{ currentPath }}</p>
      </div>
      
      <div class="forbidden-actions">
        <a-button type="primary" @click="goBack">
          <ArrowLeftOutlined />
          返回上一页
        </a-button>
        
        <a-button @click="goHome">
          <HomeOutlined />
          返回首页
        </a-button>
        
        <a-button @click="logout">
          <LogoutOutlined />
          重新登录
        </a-button>
      </div>
      
      <div class="forbidden-help">
        <p>如果您认为这是一个错误，请：</p>
        <ul>
          <li>检查您的账户权限设置</li>
          <li>联系系统管理员</li>
          <li>或发送邮件至 <a href="mailto:admin@example.com">admin@example.com</a></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  ExclamationCircleOutlined, 
  ArrowLeftOutlined, 
  HomeOutlined, 
  LogoutOutlined 
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/modules/auth'

interface Props {
  showDetails?: boolean
}

withDefaults(defineProps<Props>(), {
  showDetails: true
})

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)
const currentPath = computed(() => route.fullPath)

const userRoles = computed(() => {
  if (!userInfo.value?.roles?.length) {
    return '无角色'
  }
  return userInfo.value.roles.join(', ')
})

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    goHome()
  }
}

const goHome = () => {
  router.push('/dashboard/home')
}

const logout = async () => {
  await authStore.logout()
}

onMounted(() => {
  // 记录403访问日志
  console.warn('403 Forbidden access attempt:', {
    user: userInfo.value?.username,
    roles: userInfo.value?.roles,
    path: currentPath.value,
    timestamp: new Date().toISOString()
  })
})
</script>

<style lang="scss" scoped>
.forbidden-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.forbidden-content {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 48px;
  text-align: center;
  max-width: 600px;
  width: 100%;
}

.forbidden-icon {
  font-size: 72px;
  color: #f5222d;
  margin-bottom: 24px;
}

.forbidden-title {
  font-size: 72px;
  font-weight: 700;
  color: #262626;
  margin: 0 0 16px 0;
  line-height: 1;
}

.forbidden-subtitle {
  font-size: 24px;
  font-weight: 500;
  color: #595959;
  margin: 0 0 24px 0;
}

.forbidden-description {
  font-size: 16px;
  color: #8c8c8c;
  line-height: 1.6;
  margin-bottom: 32px;
}

.forbidden-details {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 32px;
  text-align: left;
  
  p {
    margin: 8px 0;
    font-size: 14px;
    color: #595959;
    
    strong {
      color: #262626;
    }
  }
}

.forbidden-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 32px;
  
  .ant-btn {
    min-width: 120px;
  }
}

.forbidden-help {
  text-align: left;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  padding: 20px;
  
  p {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #52c41a;
    font-weight: 500;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin: 8px 0;
      font-size: 14px;
      color: #595959;
      
      a {
        color: #1890ff;
        text-decoration: none;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .forbidden-content {
    padding: 32px 24px;
  }
  
  .forbidden-title {
    font-size: 56px;
  }
  
  .forbidden-subtitle {
    font-size: 20px;
  }
  
  .forbidden-actions {
    flex-direction: column;
    align-items: center;
    
    .ant-btn {
      width: 100%;
      max-width: 200px;
    }
  }
}
</style>