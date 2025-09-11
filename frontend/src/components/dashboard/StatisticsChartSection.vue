<template>
  <div class="statistics-chart-section">
    <div class="section-header">
      <h3 class="section-title">数量统计占比</h3>
    </div>
    
    <div class="chart-container">
      <!-- 选项卡切换 -->
      <a-tabs 
        v-model:activeKey="activeTab" 
        @change="handleTabChange"
        class="chart-tabs"
      >
        <a-tab-pane key="department" tab="产品管理部门" />
        <a-tab-pane key="productType" tab="产品类型" />
        <a-tab-pane key="ruleType" tab="规则类型" />
      </a-tabs>
      
      <!-- 图表和列表容器 -->
      <div class="chart-content">
        <div class="chart-wrapper">
          <!-- ECharts 饼图 -->
          <div 
            ref="chartRef" 
            class="pie-chart"
            :style="{ width: '300px', height: '300px' }"
          ></div>
        </div>
        
        <!-- 类型列表 -->
        <div class="type-list">
          <div 
            v-for="item in currentData.chartData" 
            :key="item.name"
            class="type-item"
          >
            <div class="type-indicator">
              <span 
                class="color-dot" 
                :style="{ backgroundColor: item.color }"
              ></span>
              <span class="type-name">{{ item.name }}</span>
            </div>
            <div class="type-stats">
              <span class="percentage">{{ item.percentage }}</span>
              <span class="count">({{ item.value }})</span>
            </div>
          </div>
          
          <!-- 总计 -->
          <div class="total-item">
            <div class="total-label">总计</div>
            <div class="total-value">{{ currentData.total }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// 组件名称定义
defineOptions({
  name: 'StatisticsChartSection'
})

// 图表数据接口
interface ChartDataItem {
  name: string
  value: number
  percentage: string
  color: string
}

interface StatisticsData {
  chartData: ChartDataItem[]
  total: number
}

// 响应式数据
const activeTab = ref<string>('productType')
const chartRef = ref<HTMLElement>()
const loading = ref(false)
const statisticsData = ref<Record<string, StatisticsData>>({})

// ECharts 实例
let chartInstance: echarts.ECharts | null = null

// 当前数据
const currentData = computed(() => {
  return statisticsData.value[activeTab.value] || { chartData: [], total: 0 }
})

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  // 销毁已存在的实例
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  // 创建新实例
  chartInstance = echarts.init(chartRef.value)
  
  // 设置图表配置
  updateChart()
}

// 更新图表
const updateChart = () => {
  if (!chartInstance || !currentData.value.chartData.length) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      show: false // 隐藏图例，使用自定义列表
    },
    series: [
      {
        name: getSeriesName(),
        type: 'pie',
        radius: ['40%', '70%'], // 环形饼图
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false // 隐藏标签，使用自定义列表
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: false
        },
        data: currentData.value.chartData.map(item => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: item.color
          }
        }))
      }
    ]
  }
  
  chartInstance.setOption(option, true)
}

// 获取系列名称
const getSeriesName = () => {
  const nameMap = {
    department: '产品管理部门',
    productType: '产品类型',
    ruleType: '规则类型'
  }
  return nameMap[activeTab.value as keyof typeof nameMap] || '统计数据'
}

// 处理选项卡切换
const handleTabChange = async (key: string) => {
  activeTab.value = key
  await loadStatisticsData(key)
  await nextTick()
  updateChart()
}

// 加载统计数据
const loadStatisticsData = async (type: string) => {
  try {
    loading.value = true
    const data = await DashboardMockService.getStatisticsData(type)
    statisticsData.value[type] = data
  } catch (error) {
    console.error('Failed to load statistics data:', error)
  } finally {
    loading.value = false
  }
}

// 窗口大小变化处理
const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

// 生命周期
onMounted(async () => {
  // 加载初始数据
  await loadStatisticsData(activeTab.value)
  
  // 初始化图表
  await nextTick()
  initChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清理图表实例
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.statistics-chart-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .section-header {
    margin-bottom: 24px;
    
    .section-title {
      font-size: 20px;
      font-weight: 500;
      color: #262626;
      margin: 0;
    }
  }
  
  .chart-container {
    .chart-tabs {
      margin-bottom: 24px;
      
      :deep(.ant-tabs-nav) {
        margin-bottom: 0;
      }
      
      :deep(.ant-tabs-tab) {
        font-size: 14px;
        
        &.ant-tabs-tab-active {
          .ant-tabs-tab-btn {
            color: #1890ff;
            font-weight: 500;
          }
        }
      }
      
      :deep(.ant-tabs-ink-bar) {
        background: #1890ff;
      }
    }
    
    .chart-content {
      display: flex;
      align-items: flex-start;
      gap: 32px;
      
      .chart-wrapper {
        flex-shrink: 0;
        
        .pie-chart {
          border-radius: 8px;
        }
      }
      
      .type-list {
        flex: 1;
        min-width: 200px;
        
        .type-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          
          &:last-child {
            border-bottom: none;
          }
          
          .type-indicator {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .color-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              flex-shrink: 0;
            }
            
            .type-name {
              font-size: 14px;
              color: #262626;
              font-weight: 400;
            }
          }
          
          .type-stats {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .percentage {
              font-size: 14px;
              font-weight: 500;
              color: #262626;
            }
            
            .count {
              font-size: 12px;
              color: #8c8c8c;
            }
          }
        }
        
        .total-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px 0 0;
          margin-top: 16px;
          border-top: 2px solid #f0f0f0;
          
          .total-label {
            font-size: 14px;
            font-weight: 500;
            color: #262626;
          }
          
          .total-value {
            font-size: 16px;
            font-weight: 600;
            color: #1890ff;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .statistics-chart-section {
    padding: 16px;
    
    .chart-content {
      flex-direction: column;
      gap: 24px;
      
      .chart-wrapper {
        align-self: center;
        
        .pie-chart {
          width: 280px !important;
          height: 280px !important;
        }
      }
      
      .type-list {
        min-width: auto;
      }
    }
  }
}

@media (max-width: 480px) {
  .statistics-chart-section {
    .chart-wrapper {
      .pie-chart {
        width: 240px !important;
        height: 240px !important;
      }
    }
  }
}
</style>