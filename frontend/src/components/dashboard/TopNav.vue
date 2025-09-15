<template>
  <nav class="top-nav" :style="{ backgroundColor: '#001529' }">
    <div class="nav-container">
      <!-- 左侧品牌 -->
      <div class="nav-brand">
        <span class="brand-text">{{ brand }}</span>
      </div>

      <!-- 中间菜单 -->
      <div class="nav-menu">
        <div 
          v-for="menu in menus" 
          :key="menu"
          :class="['menu-item', { active: active === menu }]"
          @click="handleMenuClick(menu)"
        >
          {{ menu }}
        </div>
      </div>

      <!-- 右侧用户信息 -->
      <div class="nav-user">
        <i class="fa fa-user user-icon"></i>
        <span class="username">{{ username }}</span>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
// 定义组件属性
interface Props {
  brand: string
  menus: string[]
  active: string
  username: string
}

// 定义组件事件
interface Emits {
  (e: 'navigate', menuKey: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 处理菜单点击
const handleMenuClick = (menuKey: string) => {
  // 规整菜单键，避免尾随空格或不可见字符导致匹配失败
  const normalized = (menuKey || '').trim()
  emit('navigate', normalized)
}
</script>

<style scoped lang="scss">
.top-nav {
  width: 100%;
  height: 64px;
  /* Use sticky so it participates in layout and won't need spacer gaps */
  position: sticky;
  top: 0;
  left: 0;
  z-index: 1050; /* keep above page content */
  background-color: #001529;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.nav-container {
  max-width: 1440px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

// 品牌样式
.nav-brand {
  flex-shrink: 0;
  
  .brand-text {
    color: #ffffff;
    font-size: 18px;
    font-weight: 600;
    font-family: 'Microsoft YaHei', sans-serif;
    text-decoration: none;
  }
}

// 菜单样式
.nav-menu {
  display: flex;
  gap: 8px;
  flex: 1;
  justify-content: center;
  
  .menu-item {
    padding: 8px 16px;
    color: rgba(255, 255, 255, 0.65);
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.3s ease;
    text-decoration: none;
    white-space: nowrap;
    
    &:hover {
      color: #ffffff;
      background-color: rgba(255, 255, 255, 0.08);
    }
    
    &.active {
      color: #ffffff;
      background-color: rgba(255, 255, 255, 0.16);
      font-weight: 500;
    }
  }
}

// 用户信息样式
.nav-user {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  
  .user-icon {
    color: rgba(255, 255, 255, 0.65);
    font-size: 16px;
  }
  
  .username {
    color: #ffffff;
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    font-weight: 500;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
  }
  
  .nav-menu {
    gap: 4px;
    
    .menu-item {
      padding: 8px 12px;
      font-size: 13px;
    }
  }
  
  .nav-brand .brand-text {
    font-size: 16px;
  }
  
  .nav-user .username {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .nav-menu {
    display: none; // 在小屏幕上隐藏菜单，可以考虑使用汉堡菜单
  }
}
</style>
