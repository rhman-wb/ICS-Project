<template>
  <div class="my-favorites">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <!-- 搜索输入框 -->
      <div class="search-input">
        <i class="fa fa-search search-icon"></i>
        <input 
          type="text" 
          :placeholder="filters.keywordPlaceholder"
          v-model="searchKeyword"
          @input="handleSearch"
        />
      </div>
      
      <!-- 筛选选择器 -->
      <div class="filter-selects">
        <div class="filter-select">
          <select v-model="timeFilter" @change="handleTimeFilterChange">
            <option value="">{{ filters.timeLabel }}</option>
            <option value="today">本日</option>
            <option value="week">本周</option>
            <option value="month">本月</option>
          </select>
        </div>
        
        <div class="filter-select">
          <select v-model="typeFilter" @change="handleTypeFilterChange">
            <option value="">{{ filters.typeLabel }}</option>
            <option value="审核">审核</option>
            <option value="配置">配置</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="task-list-container">
      <div class="task-list">
        <div 
          v-for="item in filteredItems" 
          :key="item.id || item.name"
          class="task-item"
        >
          <!-- 任务图标 -->
          <div class="task-icon">
            <i :class="['fa', item.icon]"></i>
          </div>
          
          <!-- 任务名称（可点击链接） -->
          <div class="task-name">
            <a 
              href="#" 
              class="task-link"
              @click.prevent="handleTaskClick(item)"
            >
              {{ item.name }}
            </a>
          </div>
          
          <!-- 任务类型 -->
          <div class="task-type">{{ item.type }}</div>
          
          <!-- 提交人 -->
          <div class="task-submitter">{{ item.submitter }}</div>
          
          <!-- 提交时间 -->
          <div class="task-time">{{ item.submittedAt }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
// 定义组件属性
interface Props {
  items: Array<{
    name: string
    type: '审核' | '配置'
    submitter: string
    submittedAt: string
    icon: string
    id?: string
  }>
  filters: {
    keywordPlaceholder: string
    timeLabel: string
    typeLabel: string
  }
}

// 定义组件事件
interface Emits {
  (e: 'select', item: any): void
  (e: 'search', keyword: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const searchKeyword = ref('')
const timeFilter = ref('')
const typeFilter = ref('')

// 过滤后的任务列表
const filteredItems = computed(() => {
  let result = props.items

  // 按搜索关键词过滤
  if (searchKeyword.value.trim()) {
    result = result.filter(item => 
      item.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }

  // 按任务类型过滤
  if (typeFilter.value) {
    result = result.filter(item => item.type === typeFilter.value)
  }

  return result
})

// 事件处理函数
const handleSearch = () => {
  emit('search', searchKeyword.value)
}

const handleTimeFilterChange = () => {
  // MVP 阶段仅处理外观，不实现真实过滤
  console.log('Time filter changed:', timeFilter.value)
}

const handleTypeFilterChange = () => {
  // MVP 阶段仅处理外观，不实现真实过滤
  console.log('Type filter changed:', typeFilter.value)
}

const handleTaskClick = (item: any) => {
  emit('select', item)
}
</script>

<style scoped lang="scss">
.my-favorites {
  display: flex;
  flex-direction: column;
  height: 100%;
}

// 搜索和筛选区域
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  align-items: center;
}

// 搜索输入框
.search-input {
  flex: 1;
  position: relative;
  
  .search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #999999;
    font-size: 14px;
  }
  
  input {
    width: 100%;
    height: 36px;
    padding: 0 12px 0 36px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    
    &::placeholder {
      color: #999999;
    }
    
    &:focus {
      outline: none;
      border-color: #40a9ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }
}

// 筛选选择器
.filter-selects {
  display: flex;
  gap: 8px;
}

.filter-select {
  select {
    height: 36px;
    min-width: 100px;
    padding: 0 8px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    background-color: #ffffff;
    cursor: pointer;
    
    &:focus {
      outline: none;
      border-color: #40a9ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
    }
  }
}

// 任务列表容器
.task-list-container {
  flex: 1;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background: #ffffff;
}

.task-list {
  height: 100%;
  overflow-y: auto;
  
  // 自定义滚动条
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f5f5f5;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb:hover {
    background: #bfbfbf;
  }
}

// 任务项
.task-item {
  display: grid;
  grid-template-columns: 40px 1fr 80px 100px 100px;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background-color: #fafafa;
  }
}

// 任务图标
.task-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  
  i {
    font-size: 16px;
    color: #666666;
  }
}

// 任务名称
.task-name {
  .task-link {
    color: #0079FE;
    text-decoration: none;
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 任务类型
.task-type {
  font-size: 14px;
  color: #666666;
  font-family: 'Microsoft YaHei', sans-serif;
}

// 提交人
.task-submitter {
  font-size: 14px;
  color: #666666;
  font-family: 'Microsoft YaHei', sans-serif;
}

// 提交时间
.task-time {
  font-size: 14px;
  color: #999999;
  font-family: 'Microsoft YaHei', sans-serif;
}

// 响应式设计
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-selects {
    justify-content: space-between;
  }
  
  .task-item {
    grid-template-columns: 32px 1fr 70px 80px 80px;
    padding: 10px 12px;
  }
  
  .task-name .task-link,
  .task-type,
  .task-submitter,
  .task-time {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .task-item {
    grid-template-columns: 32px 1fr;
    gap: 8px;
    
    .task-type,
    .task-submitter,
    .task-time {
      display: none;
    }
  }
}
</style>