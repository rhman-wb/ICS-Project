<template>
  <div class="recent-activities">
    <!-- 动态列表 -->
    <div class="activities-list">
      <div 
        v-for="(activity, index) in activities" 
        :key="index"
        class="activity-item"
      >
        <!-- 时间线左侧装饰 -->
        <div class="activity-timeline">
          <div class="timeline-dot"></div>
          <div v-if="index !== activities.length - 1" class="timeline-line"></div>
        </div>

        <!-- 动态内容 -->
        <div class="activity-content">
          <div class="activity-text">
            <!-- 渲染动态文本，支持链接 -->
            <span v-html="formatActivityText(activity)"></span>
          </div>
          <div class="activity-time">{{ activity.time }}</div>
        </div>
      </div>
    </div>

    <!-- 查看更多 -->
    <div class="view-more">
      <a href="#" class="view-more-link" @click.prevent="handleViewMore">
        查看更多
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

// 定义组件属性
interface Props {
  activities: Array<{
    type: string
    text: string
    time: string
    link?: string
  }>
}

// 定义组件事件
interface Emits {
  (e: 'open', link: string): void
  (e: 'viewMore'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const router = useRouter()

// 格式化动态文本，将产品名转换为链接
const formatActivityText = (activity: any) => {
  let text = activity.text
  
  if (activity.link) {
    // 提取产品名称（假设产品名称在"上传了"、"更新了"等后面）
    const productMatch = text.match(/(?:上传了|更新了|审核通过了|配置了)(.+?)(?:条规则|$)/)
    if (productMatch) {
      const productName = productMatch[1].trim()
      const linkText = `<a href="#" class="activity-link" data-link="${activity.link}">${productName}</a>`
      text = text.replace(productName, linkText)
    }
  }
  
  return text
}

// 处理动态内容点击
const handleActivityClick = (event: Event) => {
  const target = event.target as HTMLElement
  if (target.classList.contains('activity-link')) {
    event.preventDefault()
    const link = target.getAttribute('data-link')
    if (link) {
      emit('open', link)
      router.push(link)
    }
  }
}

// 处理查看更多
const handleViewMore = () => {
  emit('viewMore')
}
</script>

<style scoped lang="scss">
.recent-activities {
  display: flex;
  flex-direction: column;
  height: 100%;
}

// 动态列表
.activities-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  
  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f5f5f5;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 2px;
  }
  
  &::-webkit-scrollbar-thumb:hover {
    background: #bfbfbf;
  }
}

// 动态项
.activity-item {
  display: flex;
  margin-bottom: 16px;
  position: relative;
  
  &:last-child {
    margin-bottom: 0;
  }
}

// 时间线
.activity-timeline {
  flex-shrink: 0;
  margin-right: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.timeline-dot {
  width: 8px;
  height: 8px;
  background: #40a9ff;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 2px rgba(64, 169, 255, 0.2);
  position: relative;
  z-index: 2;
}

.timeline-line {
  position: absolute;
  top: 8px;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: calc(100% + 8px);
  background: linear-gradient(to bottom, #40a9ff, transparent);
  opacity: 0.3;
}

// 动态内容
.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 14px;
  color: #333333;
  font-family: 'Microsoft YaHei', sans-serif;
  line-height: 1.5;
  margin-bottom: 4px;
  
  :deep(.activity-link) {
    color: #0079FE;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.activity-time {
  font-size: 12px;
  color: #999999;
  font-family: 'Microsoft YaHei', sans-serif;
}

// 查看更多
.view-more {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
}

.view-more-link {
  color: #0079FE;
  text-decoration: none;
  font-size: 14px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-weight: 500;
  
  &:hover {
    text-decoration: underline;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .activity-item {
    margin-bottom: 12px;
  }
  
  .activity-text {
    font-size: 13px;
  }
  
  .activity-time {
    font-size: 11px;
  }
  
  .view-more {
    margin-top: 12px;
    padding-top: 12px;
  }
  
  .view-more-link {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .activity-timeline {
    margin-right: 8px;
  }
  
  .timeline-dot {
    width: 6px;
    height: 6px;
  }
  
  .activity-content {
    font-size: 12px;
  }
}
</style>