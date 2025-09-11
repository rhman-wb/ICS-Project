<template>
  <div class="ratio-chart">
    <!-- 选项卡 -->
    <div class="chart-tabs">
      <div 
        v-for="tab in tabs" 
        :key="tab"
        :class="['tab-item', { active: active === tab }]"
        @click="handleTabClick(tab)"
      >
        {{ tab }}
      </div>
    </div>

    <!-- 图表内容区域 -->
    <div class="chart-content">
      <!-- 饼图区域 -->
      <div class="chart-container">
        <div ref="chartRef" class="chart"></div>
      </div>

      <!-- 类型列表 -->
      <div class="type-list">
        <div 
          v-for="type in productTypes" 
          :key="type.label"
          class="type-item"
        >
          <div class="type-color" :style="{ backgroundColor: type.color }"></div>
          <div class="type-info">
            <div class="type-label">{{ type.label }}</div>
            <div class="type-stats">
              <span class="type-percent">{{ type.percent }}%</span>
              <span class="type-count">{{ type.count }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import fallbackChart from '@/assets/images/ratio-chart.png'

// 定义组件属性
interface Props {
  tabs: string[]
  active: string
  productTypes: Array<{
    label: string
    percent: number
    count: number
    color: string
  }>
}

// 定义组件事件
interface Emits {
  (e: 'changeTab', tab: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 图表实例和DOM引用
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  // 销毁现有实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  // 创建新实例
  chartInstance = echarts.init(chartRef.value)

  // 准备图表数据
  const chartData = props.productTypes.map(type => ({
    name: type.label,
    value: type.count,
    itemStyle: {
      color: type.color
    }
  }))

  // 图表配置
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '产品类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            formatter: '{b}\n{d}%'
          }
        },
        labelLine: {
          show: false
        },
        data: chartData
      }
    ]
  }

  // 设置配置
  chartInstance.setOption(option)
}

// 处理选项卡点击
const handleTabClick = (tab: string) => {
  emit('changeTab', tab)
}

// 响应式处理
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 生命周期钩子
onMounted(async () => {
  await nextTick()
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
  }
  window.removeEventListener('resize', handleResize)
})

// 监听数据变化
watch(() => props.productTypes, () => {
  nextTick(() => {
    initChart()
  })
}, { deep: true })
</script>

<style scoped lang="scss">
.ratio-chart {
  display: flex;
  flex-direction: column;
  height: 100%;
}

// 选项卡样式
.chart-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 8px;

  .tab-item {
    padding: 8px 16px;
    font-size: 14px;
    font-family: 'Microsoft YaHei', sans-serif;
    color: #666666;
    cursor: pointer;
    border-radius: 4px 4px 0 0;
    transition: all 0.2s ease;

    &:hover {
      color: #40a9ff;
      background-color: rgba(24, 144, 255, 0.05);
    }

    &.active {
      color: #40a9ff;
      background-color: rgba(24, 144, 255, 0.1);
      font-weight: 500;
    }
  }
}

// 图表内容区域
.chart-content {
  flex: 1;
  display: flex;
  gap: 24px;
  min-height: 300px;
}

// 饼图容器
.chart-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  .chart {
    width: 100%;
    height: 280px;
  }
}

// 类型列表
.type-list {
  flex: 0 0 200px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #fafafa;
  }
}

// 类型颜色标识
.type-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  flex-shrink: 0;
}

// 类型信息
.type-info {
  flex: 1;
  min-width: 0;
}

.type-label {
  font-size: 14px;
  color: #333333;
  font-family: 'Microsoft YaHei', sans-serif;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.type-percent {
  font-size: 12px;
  color: #666666;
  font-weight: 500;
}

.type-count {
  font-size: 12px;
  color: #999999;
}

// 响应式设计
@media (max-width: 768px) {
  .chart-content {
    flex-direction: column;
    gap: 16px;
  }

  .chart-container {
    .chart {
      height: 240px;
    }
  }

  .type-list {
    flex: none;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;

    .type-item {
      flex: 1 1 calc(50% - 4px);
      min-width: 120px;
    }
  }
}

@media (max-width: 480px) {
  .chart-tabs {
    gap: 4px;

    .tab-item {
      padding: 6px 12px;
      font-size: 13px;
    }
  }

  .type-list {
    .type-item {
      flex: 1 1 100%;
      min-width: auto;
    }
  }
}
</style>