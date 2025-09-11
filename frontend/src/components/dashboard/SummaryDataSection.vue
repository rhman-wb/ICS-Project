<template>
  <div class="summary-data-section">
    <!-- 标题 -->
    <h2 class="section-title">汇总数据</h2>
    
    <!-- 时间筛选选项卡 -->
    <a-tabs 
      v-model:activeKey="activeTimeFilter" 
      class="time-filter-tabs"
      @change="handleTimeFilterChange"
    >
      <a-tab-pane key="total" tab="总计" />
      <a-tab-pane key="today" tab="本日" />
      <a-tab-pane key="week" tab="本周" />
      <a-tab-pane key="month" tab="本月" />
    </a-tabs>
    
    <!-- 数据卡片布局 -->
    <div class="summary-cards" :class="{ 'data-updating': loading }">
      <!-- 骨架屏 -->
      <div v-if="loading" class="skeleton-container">
        <a-row :gutter="[16, 16]">
          <a-col :xl="6" :lg="6" :md="12" :sm="24" v-for="i in 4" :key="i">
            <a-skeleton-button 
              active 
              size="large" 
              :style="{ width: '100%', height: '120px', borderRadius: '8px' }"
              class="skeleton-loading"
            />
          </a-col>
        </a-row>
      </div>
      
      <!-- 数据卡片 -->
      <transition-group v-else name="list-item" tag="div">
        <a-row :gutter="[16, 16]" key="data-row">
          <a-col 
            :xl="6" :lg="6" :md="12" :sm="24" 
            v-for="(item, index) in summaryData" 
            :key="item.key"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <a-card 
              class="summary-card card-hover-effect btn-hover-effect"
              :class="{ 'card-animate': !loading }"
            >
              <div class="card-content">
                <div class="card-value" :class="`value-${item.key}`">
                  <transition name="fade" mode="out-in">
                    <span :key="item.value">{{ formatNumber(item.value) }}</span>
                  </transition>
                </div>
                <div class="card-label">
                  {{ item.label }}
                </div>
                <div class="card-icon" :class="`icon-${item.key}`">
                  <component :is="getCardIcon(item.key)" />
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </transition-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { 
  FileTextOutlined, 
  FolderOutlined, 
  SettingOutlined, 
  ExclamationCircleOutlined 
} from '@ant-design/icons-vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// 时间筛选类型
type TimeFilterType = 'total' | 'today' | 'week' | 'month'

// 组件名称定义
defineOptions({
  name: 'SummaryDataSection'
})

// 时间筛选类型已从mock服务导入

// 汇总数据类型
interface SummaryDataItem {
  key: string
  label: string
  value: number
}

// 响应式数据
const loading = ref(false)
const activeTimeFilter = ref<TimeFilterType>('total')

// 汇总数据
const summaryData = reactive<SummaryDataItem[]>([
  { key: 'products', label: '产品数量', value: 0 },
  { key: 'documents', label: '要件数量', value: 0 },
  { key: 'rules', label: '规则数量', value: 0 },
  { key: 'errors', label: '已检核错误数量', value: 0 }
])

// 使用集中的Mock数据服务

// 获取汇总数据
const fetchSummaryData = async (timeFilter: TimeFilterType) => {
  try {
    loading.value = true
    const data = await DashboardMockService.getSummaryData(timeFilter)
    
    // 使用动画效果更新数据
    summaryData.forEach(item => {
      const value = (data as any)[item.key] || 0
      animateValue(item, value)
    })
    
  } catch (error) {
    console.error('获取汇总数据失败:', error)
    message.error('获取汇总数据失败')
  } finally {
    loading.value = false
  }
}

// 数值动画效果
const animateValue = (item: SummaryDataItem, targetValue: number) => {
  const startValue = item.value
  const duration = 800 // 动画持续时间
  const startTime = Date.now()
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用缓动函数
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    item.value = Math.round(startValue + (targetValue - startValue) * easeOutQuart)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  
  requestAnimationFrame(animate)
}

// 格式化数字显示
const formatNumber = (value: number): string => {
  if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万'
  } else if (value >= 1000) {
    return (value / 1000).toFixed(1) + 'k'
  }
  return value.toString()
}

// 获取卡片图标
const getCardIcon = (key: string) => {
  const iconMap: Record<string, any> = {
    products: FileTextOutlined,
    documents: FolderOutlined,
    rules: SettingOutlined,
    errors: ExclamationCircleOutlined
  }
  return iconMap[key] || FileTextOutlined
}

// 处理时间筛选变化
const handleTimeFilterChange = (key: string) => {
  activeTimeFilter.value = key as TimeFilterType
  fetchSummaryData(activeTimeFilter.value)
}

// 监听时间筛选变化
watch(activeTimeFilter, (newFilter) => {
  fetchSummaryData(newFilter)
})

// 组件挂载时获取数据
onMounted(() => {
  fetchSummaryData(activeTimeFilter.value)
})
</script>

<style lang="scss" scoped>
.summary-data-section {
  margin-bottom: var(--spacing-xl);
  
  .section-title {
    font-size: var(--font-size-xl);
    font-weight: 500;
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-lg);
    margin-top: 0;
  }
  
  .time-filter-tabs {
    margin-bottom: var(--spacing-lg);
    
    :deep(.ant-tabs-nav) {
      margin-bottom: 0;
    }
    
    :deep(.ant-tabs-tab) {
      font-size: var(--font-size-base);
      
      &.ant-tabs-tab-active {
        .ant-tabs-tab-btn {
          color: var(--color-primary);
          font-weight: 500;
        }
      }
    }
    
    :deep(.ant-tabs-ink-bar) {
      background: var(--color-primary);
    }
  }
  
  .summary-cards {
    .skeleton-container {
      .ant-skeleton-button {
        border-radius: var(--border-radius-lg);
      }
    }
    
    .summary-card {
      height: 120px;
      border-radius: var(--border-radius-lg);
      box-shadow: var(--box-shadow-base);
      transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
      cursor: pointer;
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        transition: left var(--animation-duration-base) var(--animation-timing-ease-out);
      }
      
      &:hover {
        box-shadow: var(--box-shadow-xl);
        transform: translateY(-4px);
        
        &::before {
          left: 100%;
        }
        
        .card-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }
      
      &:active {
        transform: translateY(-2px);
        transition-duration: var(--animation-duration-fast);
      }
      
      &.card-animate {
        animation: cardFadeIn var(--animation-duration-slow) var(--animation-timing-ease-out);
      }
      
      :deep(.ant-card-body) {
        padding: var(--spacing-lg);
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        z-index: 1;
      }
      
      .card-content {
        text-align: center;
        width: 100%;
        position: relative;
        
        .card-value {
          font-size: 28px;
          font-weight: 600;
          color: var(--color-text-primary);
          line-height: 1.2;
          margin-bottom: var(--spacing-sm);
          transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
          
          &.value-products {
            color: var(--color-primary);
          }
          
          &.value-documents {
            color: #52c41a;
          }
          
          &.value-rules {
            color: #faad14;
          }
          
          &.value-errors {
            color: #f5222d;
          }
          
          span {
            display: inline-block;
          }
        }
        
        .card-label {
          font-size: 14px;
          color: var(--color-text-secondary);
          font-weight: 400;
          line-height: 1.4;
          margin-bottom: var(--spacing-sm);
        }
        
        .card-icon {
          position: absolute;
          top: 8px;
          right: 8px;
          font-size: 20px;
          opacity: 0.3;
          transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
          
          &.icon-products {
            color: var(--color-primary);
          }
          
          &.icon-documents {
            color: #52c41a;
          }
          
          &.icon-rules {
            color: #faad14;
          }
          
          &.icon-errors {
            color: #f5222d;
          }
        }
      }
    }
  }
}

// 数值变化淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: all var(--animation-duration-fast) var(--animation-timing-ease-in-out);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

// 列表项动画
.list-item-enter-active {
  transition: all var(--animation-duration-base) var(--animation-timing-ease-out);
}

.list-item-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

// 卡片淡入动画
@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// 响应式适配
@media (max-width: 768px) {
  .summary-data-section {
    .summary-cards {
      .summary-card {
        height: 100px;
        
        :deep(.ant-card-body) {
          padding: var(--spacing-md);
        }
        
        .card-content {
          .card-value {
            font-size: 24px;
          }
          
          .card-label {
            font-size: 12px;
          }
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .summary-data-section {
    .section-title {
      font-size: var(--font-size-lg);
    }
    
    .summary-cards {
      .summary-card {
        height: 90px;
        
        .card-content {
          .card-value {
            font-size: 22px;
          }
        }
      }
    }
  }
}
</style>