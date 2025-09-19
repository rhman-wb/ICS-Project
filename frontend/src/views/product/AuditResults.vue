<template>
  <div class="audit-results-page">
    <div class="page-header">
      <h2>检核结果</h2>
      <p class="page-description">查看产品检核详细结果和统计信息</p>
    </div>

    <div class="results-content">
      <!-- 统计概览 -->
      <div class="statistics-section">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-card>
              <a-statistic
                title="检核错误总数"
                :value="overallStatistics?.totalResults || 0"
                valueStyle="color: #cf1322"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic
                title="高风险问题"
                :value="overallStatistics?.highSeverityCount || 0"
                valueStyle="color: #f50"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic
                title="中风险问题"
                :value="overallStatistics?.mediumSeverityCount || 0"
                valueStyle="color: #fa8c16"
              />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card>
              <a-statistic
                title="低风险问题"
                :value="overallStatistics?.lowSeverityCount || 0"
                valueStyle="color: #fadb14"
              />
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 筛选区域 -->
      <a-card title="筛选条件" class="filter-card">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-select
              v-model:value="activeDocumentType"
              placeholder="文档类型"
              style="width: 100%"
              @change="filterByDocumentType"
            >
              <a-select-option value="all">全部文档</a-select-option>
              <a-select-option value="terms">保险条款</a-select-option>
              <a-select-option value="feasibility">可行性报告</a-select-option>
              <a-select-option value="comparison">比较说明</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="activeSeverity"
              placeholder="风险等级"
              style="width: 100%"
              @change="filterBySeverity"
            >
              <a-select-option value="">全部等级</a-select-option>
              <a-select-option value="high">高风险</a-select-option>
              <a-select-option value="medium">中风险</a-select-option>
              <a-select-option value="low">低风险</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-button type="primary" @click="resetFilters">重置筛选</a-button>
          </a-col>
          <a-col :span="6">
            <a-button @click="exportResults('excel')" :loading="exporting">
              导出Excel
            </a-button>
          </a-col>
        </a-row>
      </a-card>

      <!-- 结果列表 -->
      <a-card title="检核结果详情" class="results-table-card">
        <a-table
          :columns="resultColumns"
          :data-source="results"
          :loading="loading"
          :pagination="{
            current: current,
            total: total,
            pageSize: size,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`
          }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'severity'">
              <a-tag :color="getSeverityColor(record.severity)">
                {{ getSeverityText(record.severity) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'documentType'">
              <a-tag>{{ getDocumentTypeName(record.documentType) }}</a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button type="link" size="small" @click="viewDetail(record)">
                查看详情
              </a-button>
            </template>
          </template>
        </a-table>
      </a-card>

      <!-- 操作按钮 -->
      <div class="action-section">
        <a-space>
          <a-button @click="handleGoBack">返回</a-button>
          <a-button type="primary" @click="goToProductManagement">返回产品列表</a-button>
        </a-space>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useAuditResults } from '@/composables/audit/useAuditResults'

const router = useRouter()
const route = useRoute()

// 从路由参数获取taskId
const taskId = (route.query.taskId as string) || 'default_task_id'

// 使用检核结果Composable
const {
  state: { results, loading, total, current, size },
  overallStatistics,
  activeDocumentType,
  activeSeverity,
  exporting,
  filterByDocumentType,
  filterBySeverity,
  resetFilters,
  exportResults,
  handlePageChange,
  handlePageSizeChange,
  getDocumentTypeName
} = useAuditResults({ taskId, autoLoad: true })

// 表格列定义
const resultColumns = [
  {
    title: '规则编号',
    dataIndex: 'ruleCode',
    key: 'ruleCode',
    width: 100
  },
  {
    title: '检查建议',
    dataIndex: 'suggestion',
    key: 'suggestion',
    ellipsis: true
  },
  {
    title: '文档类型',
    dataIndex: 'documentType',
    key: 'documentType',
    width: 120
  },
  {
    title: '风险等级',
    dataIndex: 'severity',
    key: 'severity',
    width: 100
  },
  {
    title: '页码',
    dataIndex: 'pageNumber',
    key: 'pageNumber',
    width: 80
  },
  {
    title: '管理部门',
    dataIndex: 'managementDepartment',
    key: 'managementDepartment',
    width: 120
  },
  {
    title: '操作',
    key: 'action',
    width: 100
  }
]

// 获取风险等级颜色
const getSeverityColor = (severity: string): string => {
  const colorMap: Record<string, string> = {
    high: 'red',
    medium: 'orange',
    low: 'yellow'
  }
  return colorMap[severity] || 'default'
}

// 获取风险等级文本
const getSeverityText = (severity: string): string => {
  const textMap: Record<string, string> = {
    high: '高风险',
    medium: '中风险',
    low: '低风险'
  }
  return textMap[severity] || severity
}

// 表格变化处理
const handleTableChange = (pagination: any) => {
  handlePageChange(pagination.current, pagination.pageSize)
}

// 查看详情
const viewDetail = (record: any) => {
  message.info(`查看规则 ${record.ruleCode} 的详细信息`)
}

const handleGoBack = () => {
  router.back()
}

const goToProductManagement = () => {
  router.push('/product-management')
}

onMounted(() => {
  // 页面加载完成
})
</script>

<style scoped>
.audit-results-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #262626;
}

.page-description {
  margin: 0;
  color: #8c8c8c;
}

.results-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.statistics-section {
  margin-bottom: 24px;
}

.filter-card {
  margin-bottom: 24px;
}

.results-table-card {
  margin-bottom: 24px;
}

.action-section {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>