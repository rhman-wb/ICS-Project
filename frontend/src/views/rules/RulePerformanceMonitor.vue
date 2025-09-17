<template>
  <div class="performance-monitor">
    <a-card title="规则模块性能监控" :loading="loading">
      <template #extra>
        <a-space>
          <a-button @click="refreshData" :loading="loading">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
          <a-button type="primary" @click="generateReport" :loading="reportLoading">
            <template #icon>
              <FileTextOutlined />
            </template>
            生成报告
          </a-button>
        </a-space>
      </template>

      <!-- 性能概览 -->
      <div class="performance-overview" v-if="performanceData">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic
              title="列表查询时间"
              :value="performanceData.listQueryTime"
              suffix="ms"
              :value-style="getTimeStyle(performanceData.listQueryTime, 3000)"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="筛选查询时间"
              :value="performanceData.filterQueryTime"
              suffix="ms"
              :value-style="getTimeStyle(performanceData.filterQueryTime, 3000)"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="统计查询时间"
              :value="performanceData.statsQueryTime"
              suffix="ms"
              :value-style="getTimeStyle(performanceData.statsQueryTime, 1000)"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="搜索查询时间"
              :value="performanceData.searchQueryTime"
              suffix="ms"
              :value-style="getTimeStyle(performanceData.searchQueryTime, 5000)"
            />
          </a-col>
        </a-row>

        <!-- 性能状态指示器 -->
        <div class="performance-status">
          <a-row :gutter="16" style="margin-top: 16px;">
            <a-col :span="6">
              <div class="status-item">
                <span class="status-label">列表查询:</span>
                <a-tag :color="performanceData.listQueryPass ? 'green' : 'red'">
                  {{ performanceData.listQueryPass ? '通过' : '不达标' }}
                </a-tag>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="status-item">
                <span class="status-label">筛选查询:</span>
                <a-tag :color="performanceData.filterQueryPass ? 'green' : 'red'">
                  {{ performanceData.filterQueryPass ? '通过' : '不达标' }}
                </a-tag>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="status-item">
                <span class="status-label">统计查询:</span>
                <a-tag :color="performanceData.statsQueryPass ? 'green' : 'red'">
                  {{ performanceData.statsQueryPass ? '通过' : '不达标' }}
                </a-tag>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="status-item">
                <span class="status-label">搜索查询:</span>
                <a-tag :color="performanceData.searchQueryPass ? 'green' : 'red'">
                  {{ performanceData.searchQueryPass ? '通过' : '不达标' }}
                </a-tag>
              </div>
            </a-col>
          </a-row>
        </div>

        <!-- 总体状态 -->
        <div class="overall-status" style="margin-top: 16px;">
          <a-alert
            :type="performanceData.overallPass ? 'success' : 'warning'"
            :message="performanceData.overallPass ? '系统性能良好' : '系统性能需要优化'"
            :description="getStatusDescription()"
            show-icon
          />
        </div>
      </div>

      <!-- 索引使用情况 -->
      <div class="index-usage" v-if="indexData" style="margin-top: 24px;">
        <h3>索引使用情况</h3>
        <a-table
          :dataSource="indexData.indexes"
          :columns="indexColumns"
          size="small"
          :pagination="{ pageSize: 10 }"
          rowKey="INDEX_NAME"
        />
      </div>

      <!-- 表大小统计 -->
      <div class="table-sizes" v-if="indexData && indexData.tableSizes" style="margin-top: 16px;">
        <h3>表大小统计</h3>
        <a-table
          :dataSource="indexData.tableSizes"
          :columns="tableSizeColumns"
          size="small"
          :pagination="false"
          rowKey="TABLE_NAME"
        />
      </div>

      <!-- 性能报告对话框 -->
      <a-modal
        v-model:open="reportModalVisible"
        title="性能分析报告"
        width="900px"
        :footer="null"
      >
        <div v-if="reportData">
          <a-descriptions title="报告概览" :column="2" bordered>
            <a-descriptions-item label="生成时间">
              {{ formatDate(reportData.reportTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="报告状态">
              <a-tag :color="reportData.status === 'SUCCESS' ? 'green' : 'red'">
                {{ reportData.status }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>

          <!-- 优化建议 -->
          <div v-if="reportData.recommendations" style="margin-top: 16px;">
            <h4>优化建议</h4>
            <a-list
              :dataSource="reportData.recommendations"
              size="small"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-typography-text type="secondary">
                    <BulbOutlined style="margin-right: 8px;" />
                    {{ item }}
                  </a-typography-text>
                </a-list-item>
              </template>
            </a-list>
          </div>
        </div>

        <template #footer>
          <a-button @click="reportModalVisible = false">关闭</a-button>
          <a-button type="primary" @click="downloadReport">下载报告</a-button>
        </template>
      </a-modal>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  ReloadOutlined,
  FileTextOutlined,
  BulbOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { rulesApi } from '@/api/modules/rules'

// 响应式数据
const loading = ref(false)
const reportLoading = ref(false)
const reportModalVisible = ref(false)
const performanceData = ref<any>(null)
const indexData = ref<any>(null)
const reportData = ref<any>(null)

// 表格列定义
const indexColumns = [
  {
    title: '表名',
    dataIndex: 'TABLE_NAME',
    key: 'TABLE_NAME'
  },
  {
    title: '索引名',
    dataIndex: 'INDEX_NAME',
    key: 'INDEX_NAME'
  },
  {
    title: '列名',
    dataIndex: 'COLUMN_NAME',
    key: 'COLUMN_NAME'
  },
  {
    title: '位置',
    dataIndex: 'SEQ_IN_INDEX',
    key: 'SEQ_IN_INDEX'
  }
]

const tableSizeColumns = [
  {
    title: '表名',
    dataIndex: 'TABLE_NAME',
    key: 'TABLE_NAME'
  },
  {
    title: '大小(MB)',
    dataIndex: 'TABLE_SIZE_MB',
    key: 'TABLE_SIZE_MB',
    sorter: (a: any, b: any) => a.TABLE_SIZE_MB - b.TABLE_SIZE_MB
  },
  {
    title: '行数',
    dataIndex: 'TABLE_ROWS',
    key: 'TABLE_ROWS',
    sorter: (a: any, b: any) => a.TABLE_ROWS - b.TABLE_ROWS
  }
]

// 获取性能数据
const fetchPerformanceData = async () => {
  try {
    const response = await rulesApi.validateQueryPerformance()
    performanceData.value = response.data
  } catch (error) {
    console.error('获取性能数据失败:', error)
    message.error('获取性能数据失败')
  }
}

// 获取索引数据
const fetchIndexData = async () => {
  try {
    const response = await rulesApi.getIndexUsageStatistics()
    indexData.value = response.data
  } catch (error) {
    console.error('获取索引数据失败:', error)
    message.error('获取索引数据失败')
  }
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchPerformanceData(),
      fetchIndexData()
    ])
    message.success('数据刷新成功')
  } finally {
    loading.value = false
  }
}

// 生成报告
const generateReport = async () => {
  reportLoading.value = true
  try {
    const response = await rulesApi.generatePerformanceReport()
    reportData.value = response.data
    reportModalVisible.value = true
    message.success('性能报告生成成功')
  } catch (error) {
    console.error('生成报告失败:', error)
    message.error('生成报告失败')
  } finally {
    reportLoading.value = false
  }
}

// 下载报告
const downloadReport = () => {
  const content = JSON.stringify(reportData.value, null, 2)
  const blob = new Blob([content], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `performance-report-${Date.now()}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 获取时间样式
const getTimeStyle = (time: number, threshold: number) => {
  return {
    color: time <= threshold ? '#52c41a' : '#ff4d4f'
  }
}

// 获取状态描述
const getStatusDescription = () => {
  if (!performanceData.value) return ''

  if (performanceData.value.overallPass) {
    return '所有查询都在性能目标范围内'
  } else {
    const issues = []
    if (!performanceData.value.listQueryPass) issues.push('列表查询')
    if (!performanceData.value.filterQueryPass) issues.push('筛选查询')
    if (!performanceData.value.statsQueryPass) issues.push('统计查询')
    if (!performanceData.value.searchQueryPass) issues.push('搜索查询')

    return `${issues.join('、')}性能不达标，建议进行优化`
  }
}

// 格式化日期
const formatDate = (date: string | Date) => {
  return new Date(date).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  refreshData()
})
</script>

<style scoped>
.performance-monitor {
  padding: 24px;
}

.performance-overview .ant-statistic {
  text-align: center;
}

.status-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.status-label {
  font-size: 14px;
  color: #666;
}

.overall-status {
  text-align: center;
}

.performance-overview,
.index-usage,
.table-sizes {
  margin-bottom: 24px;
}

h3 {
  margin-bottom: 16px;
  color: #262626;
  font-weight: 600;
}
</style>