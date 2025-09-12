<template>
  <div class="application-overview-section">
    <!-- 标题 -->
    <h2 class="section-title">应用概要</h2>
    
    <!-- 统计数据布局 -->
    <a-row :gutter="[16, 16]" class="overview-cards">
      <!-- 累计统计 -->
      <a-col :xl="12" :lg="12" :md="24" :sm="24">
        <div class="stats-group">
          <h3 class="group-title">累计统计</h3>
          <a-row :gutter="[16, 16]">
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ formatNumber(overviewData.cumulative.totalProducts) }}
                  </div>
                  <div class="card-label">累计上传产品数量</div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ formatNumber(overviewData.cumulative.totalRules) }}
                  </div>
                  <div class="card-label">累计导入规则数量</div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ overviewData.cumulative.errorDetectionRate }}
                  </div>
                  <div class="card-label">错误检出率</div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ overviewData.cumulative.avgAuditTime }}
                  </div>
                  <div class="card-label">平均检核时间</div>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>
      </a-col>
      
      <!-- 近期统计 -->
      <a-col :xl="12" :lg="12" :md="24" :sm="24">
        <div class="stats-group">
          <h3 class="group-title">近期统计</h3>
          <a-row :gutter="[16, 16]">
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ formatNumber(overviewData.recent.products7Days) }}
                  </div>
                  <div class="card-label">近7日上传产品数量</div>
                  <div class="card-trend">
                    <ArrowUpOutlined class="trend-icon trend-up" />
                    <span class="trend-text trend-up">{{ overviewData.recent.productGrowthRate }}</span>
                  </div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ formatNumber(overviewData.recent.products30Days) }}
                  </div>
                  <div class="card-label">近30日上传产品数量</div>
                  <div class="card-trend">
                    <ArrowUpOutlined class="trend-icon trend-up" />
                    <span class="trend-text trend-up">+15.8%</span>
                  </div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ formatNumber(overviewData.recent.rules7Days) }}
                  </div>
                  <div class="card-label">近7日导入规则数量</div>
                  <div class="card-trend">
                    <ArrowUpOutlined class="trend-icon trend-up" />
                    <span class="trend-text trend-up">{{ overviewData.recent.ruleGrowthRate }}</span>
                  </div>
                </div>
              </a-card>
            </a-col>
            <a-col :span="12">
              <a-card :loading="loading" class="overview-card">
                <div class="card-content">
                  <div class="card-value">
                    {{ formatNumber(overviewData.recent.rules30Days) }}
                  </div>
                  <div class="card-label">近30日导入规则数量</div>
                  <div class="card-trend">
                    <ArrowDownOutlined class="trend-icon trend-down" />
                    <span class="trend-text trend-down">-3.2%</span>
                  </div>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons-vue'
import { DashboardMockService } from '@/api/mock/dashboardMock'

// 组件名称定义
defineOptions({
  name: 'ApplicationOverviewSection'
})

// 应用概要数据类型
interface OverviewData {
  cumulative: {
    totalProducts: number
    totalRules: number
    errorDetectionRate: string
    avgAuditTime: string
  }
  recent: {
    products7Days: number
    products30Days: number
    rules7Days: number
    rules30Days: number
    productGrowthRate: string
    ruleGrowthRate: string
  }
}

// 响应式数据
const loading = ref(false)

// 应用概要数据
const overviewData = reactive<OverviewData>({
  cumulative: {
    totalProducts: 0,
    totalRules: 0,
    errorDetectionRate: '0%',
    avgAuditTime: '00:00:00'
  },
  recent: {
    products7Days: 0,
    products30Days: 0,
    rules7Days: 0,
    rules30Days: 0,
    productGrowthRate: '0%',
    ruleGrowthRate: '0%'
  }
})

// 获取应用概要数据
const fetchOverviewData = async () => {
  try {
    loading.value = true
    const data = await DashboardMockService.getOverviewData()
    
    // 使用动画效果更新数据
    animateData(data)
    
  } catch (error) {
    console.error('获取应用概要数据失败:', error)
    message.error('获取应用概要数据失败')
  } finally {
    loading.value = false
  }
}

// 数据动画效果
const animateData = (targetData: any) => {
  const duration = 800 // 动画持续时间
  const startTime = Date.now()
  
  // 保存初始值
  const startValues = {
    totalProducts: overviewData.cumulative.totalProducts,
    totalRules: overviewData.cumulative.totalRules,
    products7Days: overviewData.recent.products7Days,
    products30Days: overviewData.recent.products30Days,
    rules7Days: overviewData.recent.rules7Days,
    rules30Days: overviewData.recent.rules30Days
  }
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用缓动函数
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    
    // 动画数值类型的数据
    overviewData.cumulative.totalProducts = Math.round(
      startValues.totalProducts + (targetData.cumulative.totalProducts - startValues.totalProducts) * easeOutQuart
    )
    overviewData.cumulative.totalRules = Math.round(
      startValues.totalRules + (targetData.cumulative.totalRules - startValues.totalRules) * easeOutQuart
    )
    overviewData.recent.products7Days = Math.round(
      startValues.products7Days + (targetData.recent.products7Days - startValues.products7Days) * easeOutQuart
    )
    overviewData.recent.products30Days = Math.round(
      startValues.products30Days + (targetData.recent.products30Days - startValues.products30Days) * easeOutQuart
    )
    overviewData.recent.rules7Days = Math.round(
      startValues.rules7Days + (targetData.recent.rules7Days - startValues.rules7Days) * easeOutQuart
    )
    overviewData.recent.rules30Days = Math.round(
      startValues.rules30Days + (targetData.recent.rules30Days - startValues.rules30Days) * easeOutQuart
    )
    
    if (progress >= 1) {
      // 动画完成，设置字符串类型的数据
      overviewData.cumulative.errorDetectionRate = targetData.cumulative.errorDetectionRate
      overviewData.cumulative.avgAuditTime = targetData.cumulative.avgAuditTime
      overviewData.recent.productGrowthRate = targetData.recent.productGrowthRate
      overviewData.recent.ruleGrowthRate = targetData.recent.ruleGrowthRate
    } else {
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

// 组件挂载时获取数据
onMounted(() => {
  fetchOverviewData()
})
</script>

<style lang="scss" scoped>
.application-overview-section {
  margin-bottom: var(--spacing-xl);
  
  .section-title {
    font-size: var(--font-size-xl);
    font-weight: 500;
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-lg);
    // 顶部留白，避免被固定导航遮挡
    margin-top: var(--spacing-md);
    scroll-margin-top: calc(var(--header-height) + var(--spacing-md));
  }
  
  .overview-cards {
    .stats-group {
      .group-title {
        font-size: var(--font-size-lg);
        font-weight: 500;
        color: var(--color-text-primary);
        margin-bottom: var(--spacing-md);
        margin-top: 0;
        text-align: center;
        padding-bottom: var(--spacing-sm);
        border-bottom: 2px solid var(--color-primary);
      }
      
      .overview-card {
        height: 120px;
        border-radius: var(--border-radius-lg);
        box-shadow: var(--box-shadow-base);
        transition: all 0.3s ease;
        
        &:hover {
          box-shadow: var(--box-shadow-lg);
          transform: translateY(-2px);
        }
        
        :deep(.ant-card-body) {
          padding: var(--spacing-md);
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        
        .card-content {
          text-align: center;
          width: 100%;
          
          .card-value {
            font-size: 20px;
            font-weight: 600;
            color: var(--color-text-primary);
            line-height: 1.2;
            margin-bottom: var(--spacing-xs);
            transition: all 0.3s ease;
          }
          
          .card-label {
            font-size: 12px;
            color: var(--color-text-secondary);
            font-weight: 400;
            line-height: 1.4;
            margin-bottom: var(--spacing-xs);
          }
          
          .card-trend {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: var(--spacing-xs);
            
            .trend-icon {
              font-size: 12px;
              
              &.trend-up {
                color: #f5222d;
              }
              
              &.trend-down {
                color: #52c41a;
              }
            }
            
            .trend-text {
              font-size: 12px;
              font-weight: 500;
              
              &.trend-up {
                color: #f5222d;
              }
              
              &.trend-down {
                color: #52c41a;
              }
            }
          }
        }
      }
    }
  }
}

// 响应式适配
@media (max-width: 1200px) {
  .application-overview-section {
    .overview-cards {
      .stats-group {
        .overview-card {
          height: 110px;
          
          .card-content {
            .card-value {
              font-size: 18px;
            }
            
            .card-label {
              font-size: 11px;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .application-overview-section {
    .overview-cards {
      .stats-group {
        margin-bottom: var(--spacing-lg);
        
        .overview-card {
          height: 100px;
          
          :deep(.ant-card-body) {
            padding: var(--spacing-sm);
          }
          
          .card-content {
            .card-value {
              font-size: 16px;
            }
            
            .card-label {
              font-size: 10px;
            }
            
            .card-trend {
              .trend-icon,
              .trend-text {
                font-size: 10px;
              }
            }
          }
        }
      }
    }
  }
}

@media (max-width: 576px) {
  .application-overview-section {
    .section-title {
      font-size: var(--font-size-lg);
    }
    
    .overview-cards {
      .stats-group {
        .group-title {
          font-size: var(--font-size-base);
        }
        
        .overview-card {
          height: 90px;
          
          .card-content {
            .card-value {
              font-size: 14px;
            }
            
            .card-label {
              font-size: 9px;
            }
          }
        }
      }
    }
  }
}
</style>