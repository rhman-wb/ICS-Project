<template>
  <div class="summary-totals">
    <!-- 时间选择标签 -->
    <div class="time-ranges">
      <div 
        v-for="range in ranges" 
        :key="range"
        :class="['range-item', { active: activeRange === range }]"
        @click="handleRangeClick(range)"
      >
        {{ range }}
      </div>
    </div>

    <!-- 汇总数据卡片 -->
    <div class="metrics-grid">
      <div 
        v-for="metric in metrics" 
        :key="metric.label"
        class="metric-card"
      >
        <div class="metric-value">{{ formatNumber(metric.value) }}</div>
        <div class="metric-label">{{ metric.label }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 定义组件属性
interface Props {
  ranges: string[]
  activeRange: string
  metrics: Array<{
    label: string
    value: number
  }>
}

// 定义组件事件
interface Emits {
  (e: 'changeRange', range: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 处理时间范围点击
const handleRangeClick = (range: string) => {
  emit('changeRange', range)
}

// 格式化数字显示
const formatNumber = (num: number): string => {
  return num.toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.summary-totals {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 时间选择标签
.time-ranges {
  display: flex;
  gap: 8px;
  padding: 4px;
  background: #f5f5f5;
  border-radius: 6px;

  .range-item {
    flex: 1;
    padding: 8px 16px;
    text-align: center;
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    color: #666666;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s ease;
    font-weight: 500;

    &:hover {
      color: #333333;
      background: rgba(255, 255, 255, 0.5);
    }

    &.active {
      color: #ffffff;
      background: #40a9ff;
      box-shadow: 0 2px 4px rgba(64, 169, 255, 0.2);
    }
  }
}

// 汇总数据网格
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

// 指标卡片
.metric-card {
  background: #ffffff;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transform: translateY(-1px);
  }
}

// 指标数值
.metric-value {
  font-size: 28px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 8px;
  font-family: 'Microsoft YaHei', sans-serif;
  line-height: 1.2;
}

// 指标标签
.metric-label {
  font-size: 14px;
  color: #666666;
  font-family: 'Microsoft YaHei', sans-serif;
  line-height: 1.4;
}

// 响应式设计
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .time-ranges {
    flex-wrap: wrap;
    
    .range-item {
      flex: 1 1 calc(50% - 4px);
      min-width: 80px;
      font-size: 13px;
      padding: 6px 12px;
    }
  }
  
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .metric-card {
    padding: 16px;
  }
  
  .metric-value {
    font-size: 24px;
  }
  
  .metric-label {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .time-ranges {
    .range-item {
      flex: 1 1 100%;
      font-size: 12px;
      padding: 6px 8px;
    }
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .metric-value {
    font-size: 20px;
  }
}
</style>