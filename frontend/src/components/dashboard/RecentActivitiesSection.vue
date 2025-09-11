<template>
  <div class="recent-activities-section">
    <div class="section-header">
      <h3 class="section-title">近期动态</h3>
    </div>
    
    <div class="activities-content">
      <a-spin :spinning="loading" tip="加载中...">
        <a-timeline v-if="activities.length > 0" class="activities-timeline">
          <a-timeline-item
            v-for="activity in displayedActivities"
            :key="activity.id"
            class="activity-item"
          >
            <template #dot>
              <div class="activity-dot" :class="`activity-dot--${activity.type}`">
                <component :is="getActivityIcon(activity.type)" />
              </div>
            </template>
            
            <div class="activity-content">
              <div class="activity-text">
                <span class="activity-user">{{ activity.user }}</span>
                <span class="activity-action">{{ getActivityAction(activity) }}</span>
                <a-button
                  v-if="activity.productName"
                  type="link"
                  size="small"
                  class="product-link"
                  @click="handleProductClick(activity.productId)"
                >
                  {{ activity.productName }}
                </a-button>
              </div>
              <div class="activity-time">
                {{ getRelativeTime(activity.time) }}
              </div>
            </div>
          </a-timeline-item>
        </a-timeline>
        
        <a-empty v-else description="暂无动态数据" />
      </a-spin>
      
      <!-- 查看更多按钮 -->
      <div v-if="hasMore && !showAll" class="view-more-section">
        <a-button 
          type="link" 
          class="view-more-btn"
          @click="handleViewMore"
        >
          查看更多
          <DownOutlined />
        </a-button>
      </div>
      
      <!-- 收起按钮 -->
      <div v-if="showAll && activities.length > defaultLimit" class="view-more-section">
        <a-button 
          type="link" 
          class="view-more-btn"
          @click="handleCollapse"
        >
          收起
          <UpOutlined />
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  FileTextOutlined,
  SettingOutlined,
  CheckCircleOutlined,
  UploadOutlined,
  DownOutlined,
  UpOutlined
} from '@ant-design/icons-vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// 配置dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// 组件名称定义
defineOptions({
  name: 'RecentActivitiesSection'
})

// 活动数据接口
interface Activity {
  id: string
  type: string
  user: string
  content: string
  productName?: string
  productId?: string
  ruleCount?: number
  time: Date
  timestamp: number
}

// 响应式数据
const loading = ref(false)
const activities = ref<Activity[]>([])
const showAll = ref(false)
const hasMore = ref(false)
const defaultLimit = 5

// 计算属性
const displayedActivities = computed(() => {
  return showAll.value ? activities.value : activities.value.slice(0, defaultLimit)
})

// 获取活动图标
const getActivityIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    upload: UploadOutlined,
    rule_update: SettingOutlined,
    rule_approve: CheckCircleOutlined,
    rule_config: SettingOutlined,
    default: FileTextOutlined
  }
  return iconMap[type] || iconMap.default
}

// 获取活动动作描述
const getActivityAction = (activity: Activity) => {
  switch (activity.type) {
    case 'upload':
      return activity.content
    case 'rule_update':
      return `更新了${activity.ruleCount}条规则`
    case 'rule_approve':
      return `审核通过了${activity.ruleCount}条规则`
    case 'rule_config':
      return `配置了${activity.ruleCount}条规则`
    default:
      return activity.content
  }
}

// 获取相对时间
const getRelativeTime = (time: Date) => {
  const now = dayjs()
  const activityTime = dayjs(time)
  const diffInSeconds = now.diff(activityTime, 'second')
  const diffInMinutes = now.diff(activityTime, 'minute')
  const diffInHours = now.diff(activityTime, 'hour')
  const diffInDays = now.diff(activityTime, 'day')

  if (diffInSeconds < 60) {
    return '几秒前'
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`
  } else if (diffInHours < 24) {
    return `${diffInHours}小时前`
  } else if (diffInDays < 7) {
    return `${diffInDays}天前`
  } else {
    return activityTime.format('MM-DD HH:mm')
  }
}

// 处理产品点击
const handleProductClick = (productId?: string) => {
  if (productId) {
    message.info(`跳转到产品详情: ${productId}`)
    // TODO: 实际项目中这里应该跳转到产品详情页面
    // router.push(`/product/${productId}`)
  }
}

// 处理查看更多
const handleViewMore = () => {
  showAll.value = true
}

// 处理收起
const handleCollapse = () => {
  showAll.value = false
}

// 获取近期动态数据
const fetchRecentActivities = async () => {
  try {
    loading.value = true
    const response = await DashboardMockService.getRecentActivities(15)
    activities.value = response.activities
    hasMore.value = response.hasMore
  } catch (error) {
    console.error('获取近期动态失败:', error)
    message.error('获取近期动态失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  fetchRecentActivities()
})

// 暴露给父组件的方法
defineExpose({
  refresh: fetchRecentActivities
})
</script>

<style lang="scss" scoped>
.recent-activities-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;

  .section-header {
    margin-bottom: 20px;

    .section-title {
      font-size: 20px;
      font-weight: 500;
      color: #262626;
      margin: 0;
    }
  }

  .activities-content {
    flex: 1;
    display: flex;
    flex-direction: column;

    .activities-timeline {
      flex: 1;

      :deep(.ant-timeline-item) {
        padding-bottom: 16px;

        &:last-child {
          padding-bottom: 0;
        }
      }

      :deep(.ant-timeline-item-tail) {
        border-left: 2px solid #f0f0f0;
      }

      :deep(.ant-timeline-item-head) {
        background: transparent;
        border: none;
        width: auto;
        height: auto;
      }

      .activity-item {
        .activity-dot {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          color: #fff;

          &--upload {
            background-color: #1890ff;
          }

          &--rule_update {
            background-color: #52c41a;
          }

          &--rule_approve {
            background-color: #722ed1;
          }

          &--rule_config {
            background-color: #faad14;
          }
        }

        .activity-content {
          margin-left: 16px;
          margin-top: -2px;

          .activity-text {
            font-size: 14px;
            line-height: 1.5;
            color: #262626;
            margin-bottom: 4px;

            .activity-user {
              font-weight: 500;
              color: #262626;
            }

            .activity-action {
              margin: 0 4px;
              color: #595959;
            }

            .product-link {
              padding: 0;
              height: auto;
              font-size: 14px;
              color: #1890ff;

              &:hover {
                color: #40a9ff;
              }
            }
          }

          .activity-time {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }
    }

    .view-more-section {
      text-align: center;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;

      .view-more-btn {
        color: #1890ff;
        font-size: 14px;
        padding: 0;
        height: auto;

        &:hover {
          color: #40a9ff;
        }

        .anticon {
          margin-left: 4px;
          font-size: 12px;
        }
      }
    }
  }

  // 空状态样式
  :deep(.ant-empty) {
    margin: 40px 0;

    .ant-empty-description {
      color: #8c8c8c;
    }
  }

  // 加载状态样式
  :deep(.ant-spin-container) {
    min-height: 200px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .recent-activities-section {
    padding: 16px;

    .section-header {
      margin-bottom: 16px;

      .section-title {
        font-size: 18px;
      }
    }

    .activities-content {
      .activities-timeline {
        .activity-item {
          .activity-content {
            margin-left: 12px;

            .activity-text {
              font-size: 13px;
            }

            .activity-time {
              font-size: 11px;
            }
          }
        }
      }
    }
  }
}
</style>