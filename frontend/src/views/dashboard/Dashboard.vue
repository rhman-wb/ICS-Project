<template>
  <a-layout class="dashboard-layout">
    <!-- 顶部导航栏 -->
    <a-layout-header class="dashboard-header">
      <div class="header-left">
        <span class="system-brand">产品检核系统</span>
      </div>
      
      <div class="header-nav">
        <a-menu
          v-model:selectedKeys="selectedMenuKeys"
          mode="horizontal"
          theme="dark"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </div>
      
      <div class="header-right">
        <a-dropdown>
          <span class="user-info">
            <UserOutlined />
            {{ currentUser?.realName || currentUser?.username || '用户' }}
            <DownOutlined />
          </span>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="profile">
                <UserOutlined />
                个人设置
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    
    <!-- 主内容区域 -->
    <a-layout-content class="dashboard-content">
      <div class="content-wrapper">
        <!-- 汇总数据区域 -->
        <SummaryDataSection />
        
        <!-- 应用概要区域 -->
        <ApplicationOverviewSection />
        
        <!-- 工作台内容区域 -->
        <div class="dashboard-grid">
          <!-- 左侧区域 -->
          <div class="grid-left">
            <!-- 我的关注区域 -->
            <div class="grid-item focus-section">
              <MyFocusSection />
            </div>
          </div>
          
          <!-- 右侧区域 -->
          <div class="grid-right">
            <!-- 快速开始区域 -->
            <div class="grid-item quick-start-section">
              <QuickStartSection />
            </div>
            
            <!-- 数量统计占比区域 -->
            <div class="grid-item statistics-chart-section">
              <StatisticsChartSection />
            </div>
            
            <!-- 近期动态区域 -->
            <div class="grid-item recent-activities-section">
              <RecentActivitiesSection />
            </div>
          </div>
        </div>
      </div>
    </a-layout-content>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  UserOutlined, 
  DownOutlined, 
  LogoutOutlined,
  HomeOutlined,
  FileTextOutlined,
  SettingOutlined,
  AppstoreOutlined
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/modules/auth'
import SummaryDataSection from '@/components/dashboard/SummaryDataSection.vue'
import ApplicationOverviewSection from '@/components/dashboard/ApplicationOverviewSection.vue'
import MyFocusSection from '@/components/dashboard/MyFocusSection.vue'
import QuickStartSection from '@/components/dashboard/QuickStartSection.vue'
import StatisticsChartSection from '@/components/dashboard/StatisticsChartSection.vue'
import RecentActivitiesSection from '@/components/dashboard/RecentActivitiesSection.vue'

// 组件名称定义
defineOptions({
  name: 'Dashboard'
})

// 路由和状态管理
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const selectedMenuKeys = ref<string[]>(['dashboard'])

// 所有菜单项配置（包含权限信息）
const allMenuItems = [
  {
    key: 'dashboard',
    label: '主页',
    icon: () => h(HomeOutlined),
    permissions: [], // 主页无需特殊权限
    roles: []
  },
  {
    key: 'product',
    label: '产品管理',
    icon: () => h(AppstoreOutlined),
    permissions: ['product:view'],
    roles: ['admin', 'business_user']
  },
  {
    key: 'rule',
    label: '规则管理',
    icon: () => h(FileTextOutlined),
    permissions: ['rule:view'],
    roles: ['admin', 'audit_user']
  },
  {
    key: 'settings',
    label: '设置',
    icon: () => h(SettingOutlined),
    permissions: ['system:config'],
    roles: ['admin']
  }
]

// 根据权限过滤菜单项
const menuItems = computed(() => {
  return allMenuItems.filter(item => {
    // 检查权限
    if (item.permissions.length > 0) {
      const hasPermission = item.permissions.some(permission => 
        authStore.hasPermission(permission)
      )
      if (!hasPermission) return false
    }
    
    // 检查角色
    if (item.roles.length > 0) {
      const hasRole = item.roles.some(role => 
        authStore.hasRole(role)
      )
      if (!hasRole) return false
    }
    
    return true
  }).map(item => ({
    key: item.key,
    label: item.label,
    icon: item.icon
  }))
})

// 计算属性
const currentUser = computed(() => authStore.userInfo)


// 方法定义
const handleMenuClick = ({ key }: { key: string }) => {
  selectedMenuKeys.value = [key]
  
  // 根据菜单项跳转到对应路由
  switch (key) {
    case 'dashboard':
      router.push('/dashboard/home')
      break
    case 'product':
      // 暂时显示提示，后续实现产品管理页面
      message.info('产品管理功能正在开发中')
      break
    case 'rule':
      // 暂时显示提示，后续实现规则管理页面
      message.info('规则管理功能正在开发中')
      break
    case 'settings':
      // 暂时显示提示，后续实现设置页面
      message.info('设置功能正在开发中')
      break
    default:
      break
  }
}

const handleUserMenuClick = async ({ key }: { key: string }) => {
  switch (key) {
    case 'profile':\n      router.push('/user/profile')\n      break
    case 'logout':\n      await authStore.logout()\n      message.success('退出登录成功')\n      break
    default:
      break
  }
}

// 根据当前路由设置选中的菜单项
const updateSelectedKeys = () => {
  const path = route.path
  if (path === '/dashboard' || path === '/dashboard/home') {
    selectedMenuKeys.value = ['dashboard']
  } else if (path.startsWith('/product')) {
    selectedMenuKeys.value = ['product']
  } else if (path.startsWith('/rule')) {
    selectedMenuKeys.value = ['rule']
  } else if (path.startsWith('/settings')) {
    selectedMenuKeys.value = ['settings']
  }
}

// 生命周期
onMounted(() => {
  updateSelectedKeys()
})

// 监听路由变化
watch(() => route.path, () => {
  updateSelectedKeys()
})
</script>

<style lang="scss" scoped>
.dashboard-layout {
  min-height: 100vh;
  
  .dashboard-header {
    background: #001529;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 var(--spacing-lg);
    height: 64px;
    
    .header-left {
      .system-brand {
        color: #ffffff;
        font-size: var(--font-size-lg);
        font-weight: 500;
        white-space: nowrap;
      }
    }
    
    .header-nav {
      flex: 1;
      display: flex;
      justify-content: center;
      
      :deep(.ant-menu) {
        background: transparent;
        border-bottom: none;
        line-height: 64px;
        
        .ant-menu-item {
          color: rgba(255, 255, 255, 0.65);
          border-bottom: 2px solid transparent;
          
          &:hover {
            color: #ffffff;
            background: rgba(255, 255, 255, 0.1);
          }
          
          &.ant-menu-item-selected {
            color: #ffffff;
            background: var(--color-primary);
            border-bottom-color: var(--color-primary);
          }
        }
      }
    }
    
    .header-right {
      .user-info {
        color: #ffffff;
        cursor: pointer;
        padding: var(--spacing-sm) var(--spacing-md);
        border-radius: var(--border-radius-base);
        transition: background-color 0.3s ease;
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        
        &:hover {
          background: rgba(255, 255, 255, 0.1);
        }
        
        .anticon {
          font-size: var(--font-size-base);
        }
      }
    }
  }
  
  .dashboard-content {
    background: var(--color-background);
    min-height: calc(100vh - 64px);
    
    .content-wrapper {
      max-width: 1200px;
      margin: 0 auto;
      padding: var(--spacing-lg);
      
      .dashboard-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: var(--spacing-lg);
        margin-top: var(--spacing-lg);
        
        .grid-left,
        .grid-right {
          display: flex;
          flex-direction: column;
          gap: var(--spacing-lg);
        }
        
        .grid-item {
          &.focus-section {
            min-height: 500px;
          }
          
          &.quick-start-section {
            min-height: 300px;
          }
          
          &.statistics-chart-section {
            min-height: 400px;
          }
          
          &.recent-activities-section {
            min-height: 400px;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard-layout {
    .dashboard-header {
      padding: 0 var(--spacing-md);
      
      .header-left {
        .system-brand {
          font-size: var(--font-size-base);
        }
      }
      
      .header-nav {
        :deep(.ant-menu) {
          .ant-menu-item {
            padding: 0 var(--spacing-sm);
            
            .ant-menu-title-content {
              display: none;
            }
          }
        }
      }
      
      .header-right {
        .user-info {
          padding: var(--spacing-xs) var(--spacing-sm);
          font-size: var(--font-size-sm);
        }
      }
    }
    
    .dashboard-content {
      .content-wrapper {
        padding: var(--spacing-md);
        
        .dashboard-grid {
          grid-template-columns: 1fr;
          gap: var(--spacing-md);
          
          .grid-left,
          .grid-right {
            gap: var(--spacing-md);
          }
          
          .grid-item {
            &.focus-section,
            &.quick-start-section,
            &.statistics-chart-section,
            &.recent-activities-section {
              min-height: 300px;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .dashboard-layout {
    .dashboard-header {
      flex-direction: column;
      height: auto;
      padding: var(--spacing-sm);
      
      .header-left,
      .header-nav,
      .header-right {
        width: 100%;
        justify-content: center;
      }
      
      .header-nav {
        margin: var(--spacing-sm) 0;
        
        :deep(.ant-menu) {
          justify-content: center;
        }
      }
    }
    
    .dashboard-content {
      min-height: calc(100vh - 120px);
      
      .content-wrapper {
        padding: var(--spacing-sm);
        
        .dashboard-grid {
          margin-top: var(--spacing-md);
          gap: var(--spacing-sm);
        }
      }
    }
  }
}
</style>
