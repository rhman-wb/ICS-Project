<template>
  <div class="app-overview">
    <div class="stats-grid">
      <!-- 累计上传产品数量 -->
      <div class="stat-card">
        <div class="stat-value">{{ formatNumber(overview.totalProducts) }}</div>
        <div class="stat-label">累计上传产品数量</div>
      </div>

      <!-- 近7日上传产品数量 -->
      <div class="stat-card">
        <div class="stat-value-with-trend">
          <span class="value">{{ formatNumber(overview.last7DaysProducts.value) }}</span>
          <span 
            :class="['trend', overview.last7DaysProducts.trend]"
          >
            <i :class="[
              'fa', 
              overview.last7DaysProducts.trend === 'up' ? 'fa-arrow-up' : 'fa-arrow-down'
            ]"></i>
            {{ Math.abs(overview.last7DaysProducts.yoy) }}%
          </span>
        </div>
        <div class="stat-label">近7日上传产品数量</div>
      </div>

      <!-- 近30日上传产品数量 -->
      <div class="stat-card">
        <div class="stat-value-with-trend">
          <span class="value">{{ formatNumber(overview.last30DaysProducts.value) }}</span>
          <span 
            :class="['trend', overview.last30DaysProducts.trend]"
          >
            <i :class="[
              'fa', 
              overview.last30DaysProducts.trend === 'up' ? 'fa-arrow-up' : 'fa-arrow-down'
            ]"></i>
            {{ Math.abs(overview.last30DaysProducts.yoy) }}%
          </span>
        </div>
        <div class="stat-label">近30日上传产品数量</div>
      </div>

      <!-- 错误检出率 -->
      <div class="stat-card">
        <div class="stat-value">{{ overview.errorRate }}</div>
        <div class="stat-label">错误检出率</div>
      </div>

      <!-- 累计导入规则数量 -->
      <div class="stat-card">
        <div class="stat-value">{{ formatNumber(overview.totalRules) }}</div>
        <div class="stat-label">累计导入规则数量</div>
      </div>

      <!-- 近7日导入规则数量 -->
      <div class="stat-card">
        <div class="stat-value-with-trend">
          <span class="value">{{ formatNumber(overview.last7DaysRules.value) }}</span>
          <span 
            :class="['trend', overview.last7DaysRules.trend]"
          >
            <i :class="[
              'fa', 
              overview.last7DaysRules.trend === 'up' ? 'fa-arrow-up' : 'fa-arrow-down'
            ]"></i>
            {{ Math.abs(overview.last7DaysRules.yoy) }}%
          </span>
        </div>
        <div class="stat-label">近7日导入规则数量</div>
      </div>

      <!-- 近30日导入规则数量 -->
      <div class="stat-card">
        <div class="stat-value-with-trend">
          <span class="value">{{ formatNumber(overview.last30DaysRules.value) }}</span>
          <span 
            :class="['trend', overview.last30DaysRules.trend]"
          >
            <i :class="[
              'fa', 
              overview.last30DaysRules.trend === 'up' ? 'fa-arrow-up' : 'fa-arrow-down'
            ]"></i>
            {{ Math.abs(overview.last30DaysRules.yoy) }}%
          </span>
        </div>
        <div class="stat-label">近30日导入规则数量</div>
      </div>

      <!-- 平均检核时间 -->
      <div class="stat-card">
        <div class="stat-value">{{ overview.avgAuditTime }}</div>
        <div class="stat-label">平均检核时间</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 定义组件属性
interface Props {
  overview: {
    totalProducts: number
    last7DaysProducts: {
      value: number
      yoy: number
      trend: 'up' | 'down'
    }
    last30DaysProducts: {
      value: number
      yoy: number
      trend: 'up' | 'down'
    }
    errorRate: string
    totalRules: number
    last7DaysRules: {
      value: number
      yoy: number
      trend: 'up' | 'down'
    }
    last30DaysRules: {
      value: number
      yoy: number
      trend: 'up' | 'down'
    }
    avgAuditTime: string
  }
}

defineProps<Props>()

// 格式化数字显示
const formatNumber = (num: number): string => {
  return num.toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.app-overview {
  width: 100%;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  width: 100%;
}

.stat-card {
  background: #ffffff;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
  
  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transform: translateY(-1px);
  }
}

// 统计数值样式
.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 8px;
  font-family: 'Microsoft YaHei', sans-serif;
}

// 带趋势的数值样式
.stat-value-with-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;
  
  .value {
    font-size: 20px;
    font-weight: 600;
    color: #333333;
    font-family: 'Microsoft YaHei', sans-serif;
  }
  
  .trend {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 500;
    padding: 2px 6px;
    border-radius: 12px;
    
    i {
      font-size: 10px;
    }
    
    &.up {
      color: #F95E5A;
      background-color: rgba(249, 94, 90, 0.1);
    }
    
    &.down {
      color: #4BD863;
      background-color: rgba(75, 216, 99, 0.1);
    }
  }
}

// 统计标签样式
.stat-label {
  font-size: 14px;
  color: #666666;
  font-family: 'Microsoft YaHei', sans-serif;
  line-height: 1.4;
}

// 响应式设计
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    padding: 12px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .stat-value-with-trend .value {
    font-size: 18px;
  }
  
  .stat-label {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>