<template>
  <div class="quick-start">
    <div class="quick-start-grid">
      <div 
        v-for="item in items" 
        :key="item.label"
        :class="['quick-start-item', { 'with-badge': item.badge }]"
        @click="handleItemClick(item)"
      >
        <!-- 按钮文本 -->
        <span class="item-text">{{ item.label }}</span>
        
        <!-- 徽章 -->
        <div v-if="item.badge" class="item-badge">
          {{ item.badge }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

// 定义组件属性
interface Props {
  items: Array<{
    label: string
    route: string
    badge?: number
  }>
}

// 定义组件事件
interface Emits {
  (e: 'click', item: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const router = useRouter()

// 处理按钮点击
const handleItemClick = (item: any) => {
  emit('click', item)

  // 特殊处理下载模板
  if (item.route === '/template/download') {
    handleDownload()
    return
  }

  // 更健壮的导航匹配：优先按已知功能名跳转命名路由
  const label = (item.label || '').replace(/\s+/g, '')
  const route = item.route as string | undefined

  if (route === '/product/import' || label.includes('导入产品')) {
    router.push({ name: 'ProductImport' })
    return
  }

  // 兜底按提供的 route 跳转
  if (route) {
    router.push(route)
  }
}

// 处理下载模板
const handleDownload = () => {
  // 创建一个临时链接来触发下载
  const link = document.createElement('a')
  link.href = '/template.xlsx' // 假设模板文件在 public 目录下
  link.download = 'template.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  // 或者显示下载开始提示
  // message.success('下载开始')
}
</script>

<style scoped lang="scss">
.quick-start {
  width: 100%;
}

.quick-start-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}

.quick-start-item {
  position: relative;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  color: #ffffff;
  font-size: 14px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: center;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  }
  
  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  // 不同按钮的渐变色
  &:nth-child(1) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  &:nth-child(2) {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &:nth-child(3) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &:nth-child(4) {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
  
  &:nth-child(5) {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  }
  
  &:nth-child(6) {
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  }
}

.item-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}

// 徽章样式
.item-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 20px;
  height: 20px;
  background: #ff4d4f;
  color: #ffffff;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.3);
  border: 2px solid #ffffff;
  z-index: 1;
}

// 响应式设计
@media (max-width: 768px) {
  .quick-start-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .quick-start-item {
    height: 44px;
    font-size: 13px;
  }
  
  .item-badge {
    min-width: 18px;
    height: 18px;
    font-size: 11px;
    top: -5px;
    right: -5px;
  }
}

@media (max-width: 480px) {
  .quick-start-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .quick-start-item {
    height: 40px;
    font-size: 13px;
    padding: 0 12px;
  }
}
</style>
