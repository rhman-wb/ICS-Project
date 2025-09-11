<template>
  <div class="my-focus-section">
    <div class="section-header">
      <h2 class="section-title">我的关注</h2>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section">
        <div class="search-row">
          <div class="search-input-wrapper input-focus-effect">
            <a-input
              v-model:value="searchKeyword"
              placeholder="请输入任务名称"
              allow-clear
              class="search-input"
              @focus="handleSearchFocus"
              @blur="handleSearchBlur"
            >
              <template #prefix>
                <SearchOutlined class="search-icon" />
              </template>
            </a-input>
          </div>
          
          <transition name="fade">
            <a-button 
              v-if="hasActiveFilters"
              type="link" 
              @click="clearAllFilters"
              class="clear-filters-btn btn-hover-effect"
            >
              清除筛选
            </a-button>
          </transition>
        </div>
        
        <div class="filter-row">
          <div class="filter-select-wrapper input-focus-effect">
            <a-select
              v-model:value="timeFilter"
              placeholder="按时间筛选"
              class="filter-select"
              :options="timeFilterOptions"
            />
          </div>
          
          <div class="filter-select-wrapper input-focus-effect">
            <a-select
              v-model:value="typeFilter"
              placeholder="按任务类型筛选"
              class="filter-select"
              :options="typeFilterOptions"
            />
          </div>
        </div>
      </div>
    </div>
    
    <div class="section-content">
      <!-- 管理员专用功能 -->
      <PermissionWrapper 
        roles="admin" 
        show-fallback
        fallback-text="仅管理员可查看高级统计信息"
        class="admin-section"
      >
        <div class="admin-stats">
          <a-statistic title="待审核任务" :value="pendingApprovalCount" />
          <a-statistic title="异常任务" :value="errorTaskCount" />
        </div>
      </PermissionWrapper>
      
      <!-- 骨架屏 -->
      <div v-if="loading" class="skeleton-container">
        <a-skeleton 
          v-for="i in 5" 
          :key="i" 
          active 
          :paragraph="{ rows: 2 }" 
          class="skeleton-loading"
        />
      </div>
      
      <!-- 任务列表 -->
      <transition-group v-else name="list-item" tag="div" class="task-list-container">
        <a-list
          :data-source="filteredTaskList"
          class="task-list"
          :pagination="false"
          :virtual="filteredTaskList.length > 20"
          :height="400"
          key="task-list"
        >
          <template #renderItem="{ item, index }">
            <a-list-item 
              class="task-item card-hover-effect" 
              @click="handleTaskClick(item)"
              :style="{ animationDelay: `${index * 50}ms` }"
            >
              <div class="task-content">
                <div class="task-main">
                  <div class="task-icon">
                    <component :is="getTaskIcon(item.icon)" />
                  </div>
                  <div class="task-info">
                    <div class="task-name">
                      <a href="javascript:void(0)" @click.stop="handleTaskClick(item)">
                        {{ item.name }}
                      </a>
                    </div>
                    <div class="task-meta">
                      <span class="task-submitter">{{ item.submitter }}</span>
                      <span class="task-time">{{ item.submitTime }}</span>
                    </div>
                  </div>
                </div>
                <div class="task-status">
                  <a-tag :color="getStatusColor(item.status)">
                    {{ item.status }}
                  </a-tag>
                </div>
              </div>
            </a-list-item>
          </template>
        
        <template #loadMore>
          <div v-if="hasMore" class="load-more">
            <a-button 
              type="link" 
              @click="loadMore"
              :loading="loadingMore"
            >
              查看更多任务
            </a-button>
          </div>
        </template>
        </a-list>
      </transition-group>
      
      <!-- 空状态 -->
      <a-empty 
        v-if="!loading && filteredTaskList.length === 0 && taskList.length === 0"
        description="暂无关注的任务"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
      />
      
      <!-- 搜索无结果状态 -->
      <a-empty 
        v-else-if="!loading && filteredTaskList.length === 0 && taskList.length > 0"
        description="未找到符合条件的任务"
        :image="Empty.PRESENTED_IMAGE_SIMPLE"
      >
        <template #description>
          <span>未找到符合条件的任务</span>
          <br>
          <a-button type="link" @click="clearAllFilters" style="padding: 0;">
            清除筛选条件
          </a-button>
        </template>
      </a-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { message, Empty } from 'ant-design-vue'
import { 
  FileTextOutlined,
  SettingOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'
import PermissionWrapper from '@/components/common/PermissionWrapper.vue'

// 组件名称定义
defineOptions({
  name: 'MyFocusSection'
})

// 任务接口定义
interface FocusTask {
  id: string
  name: string
  submitter: string
  submitTime: string
  type: string
  status: string
  icon: string
}

// 任务状态类型
type TaskStatus = '进行中' | '待审核' | '已完成' | '已拒绝' | '已暂停'

// 任务类型
type TaskType = '产品检核' | '规则配置' | '合规检查' | '风险评估' | '条款审核' | '费率核算'

// 响应式数据
const loading = ref(false)
const loadingMore = ref(false)
const taskList = ref<FocusTask[]>([])
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)

// 管理员统计数据
const pendingApprovalCount = ref(15)
const errorTaskCount = ref(3)

// 搜索和筛选相关数据
const searchKeyword = ref('')
const timeFilter = ref<string>('全部时间')
const typeFilter = ref<string>('全部任务')

// 筛选选项
const timeFilterOptions = [
  { label: '全部时间', value: '全部时间' },
  { label: '本周', value: '本周' },
  { label: '本月', value: '本月' }
]

const typeFilterOptions = [
  { label: '全部任务', value: '全部任务' },
  { label: '产品检核', value: '产品检核' },
  { label: '规则配置', value: '规则配置' },
  { label: '合规检查', value: '合规检查' },
  { label: '风险评估', value: '风险评估' },
  { label: '条款审核', value: '条款审核' },
  { label: '费率核算', value: '费率核算' }
]

// 图标映射
const iconMap = {
  'file-text': FileTextOutlined,
  'setting': SettingOutlined,
  'check-circle': CheckCircleOutlined,
  'clock-circle': ClockCircleOutlined,
  'exclamation-circle': ExclamationCircleOutlined
}

// 计算属性
const filteredTaskList = computed(() => {
  let filtered = [...taskList.value]
  
  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(task => 
      task.name.toLowerCase().includes(keyword) || 
      task.submitter.toLowerCase().includes(keyword)
    )
  }
  
  // 任务类型筛选
  if (typeFilter.value && typeFilter.value !== '全部任务') {
    filtered = filtered.filter(task => task.type === typeFilter.value)
  }
  
  // 时间筛选（这里简化处理，实际项目中需要根据具体时间逻辑）
  if (timeFilter.value && timeFilter.value !== '全部时间') {
    // 这里可以根据实际需求实现时间筛选逻辑
    // 目前保持所有数据，实际项目中需要根据submitTime字段进行筛选
  }
  
  return filtered
})

// 检查是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return searchKeyword.value.trim() !== '' || 
         timeFilter.value !== '全部时间' || 
         typeFilter.value !== '全部任务'
})

// 方法定义
const getTaskIcon = (iconName: string) => {
  return iconMap[iconName as keyof typeof iconMap] || FileTextOutlined
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    '进行中': 'processing',
    '待审核': 'warning',
    '已完成': 'success',
    '已拒绝': 'error',
    '已暂停': 'default'
  }
  return colorMap[status] || 'default'
}

const handleTaskClick = (task: FocusTask) => {
  // 暂时跳转到占位页面，显示任务信息
  message.info(`点击了任务：${task.name}`)
  
  // TODO: 后续实现具体的任务详情页面跳转
  // router.push(`/task/${task.id}`)
}

// 搜索框焦点处理
const handleSearchFocus = (event: FocusEvent) => {
  const wrapper = (event.target as HTMLElement).closest('.search-input-wrapper')
  if (wrapper) {
    wrapper.classList.add('focused')
  }
}

const handleSearchBlur = (event: FocusEvent) => {
  const wrapper = (event.target as HTMLElement).closest('.search-input-wrapper')
  if (wrapper) {
    wrapper.classList.remove('focused')
  }
}

const fetchTasks = async (append = false) => {
  try {
    if (!append) {
      loading.value = true
    } else {
      loadingMore.value = true
    }
    
    // 构建查询参数
    const params = {
      keyword: searchKeyword.value.trim() || undefined,
      timeFilter: timeFilter.value !== '全部时间' ? timeFilter.value : undefined,
      typeFilter: typeFilter.value !== '全部任务' ? typeFilter.value : undefined
    }
    
    // 获取Mock数据
    const response = await DashboardMockService.getFocusTasks(params)
    const allTasks: FocusTask[] = response.records
    
    // 模拟分页
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    const pageData = allTasks.slice(startIndex, endIndex)
    
    if (append) {
      taskList.value = [...taskList.value, ...pageData]
    } else {
      taskList.value = pageData
    }
    
    // 检查是否还有更多数据
    hasMore.value = endIndex < allTasks.length
    
  } catch (error) {
    console.error('Failed to fetch focus tasks:', error)
    message.error('获取关注任务失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const clearAllFilters = () => {
  searchKeyword.value = ''
  timeFilter.value = '全部时间'
  typeFilter.value = '全部任务'
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  
  currentPage.value++
  await fetchTasks(true)
}

// 防抖定时器
let searchTimer: NodeJS.Timeout | null = null

// 监听搜索关键词变化（防抖）
watch(searchKeyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchTasks()
  }, 300)
})

// 监听筛选条件变化（立即执行）
watch([timeFilter, typeFilter], () => {
  currentPage.value = 1
  fetchTasks()
})

// 生命周期
onMounted(() => {
  fetchTasks()
})
</script>

<style lang="scss" scoped>
.my-focus-section {
  background: #ffffff;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow-base);
  overflow: hidden;
  
  .section-header {
    padding: var(--spacing-lg) var(--spacing-lg) 0;
    border-bottom: 1px solid #f0f0f0;
    
    .section-title {
      font-size: var(--font-size-xl);
      font-weight: 500;
      color: var(--color-text-primary);
      margin: 0 0 var(--spacing-lg) 0;
    }
    
    .search-filter-section {
      margin-bottom: var(--spacing-lg);
      
      .search-row {
        display: flex;
        align-items: center;
        gap: var(--spacing-md);
        margin-bottom: var(--spacing-md);
        
        .search-input {
          flex: 1;
          max-width: 300px;
        }
        
        .clear-filters-btn {
          color: var(--color-primary);
          padding: 0;
          height: auto;
          
          &:hover {
            color: var(--color-primary-hover);
          }
        }
      }
      
      .filter-row {
        display: flex;
        align-items: center;
        gap: var(--spacing-md);
        
        .filter-select {
          min-width: 120px;
        }
      }
    }
  }
  
  .section-content {
    .admin-section {
      margin-bottom: var(--spacing-lg);
      
      .admin-stats {
        display: flex;
        gap: var(--spacing-lg);
        padding: var(--spacing-md);
        background: #f6ffed;
        border: 1px solid #b7eb8f;
        border-radius: 6px;
        
        :deep(.ant-statistic) {
          .ant-statistic-title {
            color: #52c41a;
            font-size: 12px;
          }
          
          .ant-statistic-content {
            color: #389e0d;
            font-size: 18px;
            font-weight: 600;
          }
        }
      }
    }
    
    .task-list {
      :deep(.ant-list-item) {
        padding: 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
      }
      
      .task-item {
        padding: var(--spacing-md) var(--spacing-lg);
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background: #f5f5f5;
        }
        
        .task-content {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;
          
          .task-main {
            display: flex;
            align-items: center;
            flex: 1;
            min-width: 0;
            
            .task-icon {
              margin-right: var(--spacing-md);
              color: var(--color-primary);
              font-size: var(--font-size-lg);
              flex-shrink: 0;
            }
            
            .task-info {
              flex: 1;
              min-width: 0;
              
              .task-name {
                margin-bottom: var(--spacing-xs);
                
                a {
                  color: var(--color-primary);
                  text-decoration: none;
                  font-weight: 500;
                  font-size: var(--font-size-base);
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  
                  &:hover {
                    color: var(--color-primary-hover);
                    text-decoration: underline;
                  }
                }
              }
              
              .task-meta {
                display: flex;
                align-items: center;
                gap: var(--spacing-md);
                font-size: var(--font-size-sm);
                color: var(--color-text-secondary);
                
                .task-submitter {
                  &::before {
                    content: "提交人：";
                  }
                }
                
                .task-time {
                  &::before {
                    content: "时间：";
                  }
                }
              }
            }
          }
          
          .task-status {
            flex-shrink: 0;
            margin-left: var(--spacing-md);
          }
        }
      }
    }
    
    .load-more {
      text-align: center;
      padding: var(--spacing-md);
      border-top: 1px solid #f0f0f0;
      
      .ant-btn-link {
        color: var(--color-primary);
        
        &:hover {
          color: var(--color-primary-hover);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .my-focus-section {
    .section-header {
      padding: var(--spacing-md) var(--spacing-md) 0;
      
      .section-title {
        font-size: var(--font-size-lg);
        margin-bottom: var(--spacing-md);
      }
      
      .search-filter-section {
        .search-row {
          .search-input {
            max-width: none;
          }
        }
        
        .filter-row {
          flex-wrap: wrap;
          
          .filter-select {
            min-width: 100px;
            flex: 1;
          }
        }
      }
    }
    
    .section-content {
      .task-list {
        .task-item {
          padding: var(--spacing-md);
          
          .task-content {
            .task-main {
              .task-icon {
                margin-right: var(--spacing-sm);
                font-size: var(--font-size-base);
              }
              
              .task-info {
                .task-name {
                  a {
                    font-size: var(--font-size-sm);
                  }
                }
                
                .task-meta {
                  flex-direction: column;
                  align-items: flex-start;
                  gap: var(--spacing-xs);
                  font-size: var(--font-size-xs);
                }
              }
            }
            
            .task-status {
              margin-left: var(--spacing-sm);
              
              :deep(.ant-tag) {
                font-size: var(--font-size-xs);
                padding: 2px 6px;
              }
            }
          }
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .my-focus-section {
    .section-header {
      .search-filter-section {
        .search-row {
          flex-direction: column;
          align-items: stretch;
          gap: var(--spacing-sm);
          
          .clear-filters-btn {
            align-self: flex-end;
          }
        }
        
        .filter-row {
          flex-direction: column;
          gap: var(--spacing-sm);
          
          .filter-select {
            width: 100%;
          }
        }
      }
    }
    
    .section-content {
      .task-list {
        .task-item {
          .task-content {
            flex-direction: column;
            align-items: flex-start;
            gap: var(--spacing-sm);
            
            .task-main {
              width: 100%;
            }
            
            .task-status {
              margin-left: 0;
              align-self: flex-end;
            }
          }
        }
      }
    }
  }
}
</style>