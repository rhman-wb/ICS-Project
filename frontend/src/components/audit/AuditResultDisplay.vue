<template>
  <div class="audit-result-display">
    <!-- 标题和操作区域 -->
    <div class="result-header">
      <div class="header-info">
        <h2 class="section-title">检核结果</h2>
        <div class="result-summary" v-if="overallStatistics">
          <a-space>
            <span>共检出</span>
            <a-tag color="red" style="font-weight: 600;">{{ overallStatistics.totalResults }}</a-tag>
            <span>个问题</span>
          </a-space>
        </div>
      </div>
      <div class="header-actions">
        <a-space>
          <a-button :icon="h(ReloadOutlined)" :loading="loading" @click="refreshData">
            刷新
          </a-button>
          <a-button
            v-if="showExport"
            type="primary"
            :icon="h(DownloadOutlined)"
            :loading="exporting"
            @click="handleExportExcel"
          >
            导出Excel
          </a-button>
          <a-button
            v-if="showExport"
            :icon="h(FilePdfOutlined)"
            :loading="exporting"
            @click="handleExportPdf"
          >
            导出PDF
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 导出进度 -->
    <div v-if="exporting" class="export-progress-section">
      <a-progress
        :percent="Math.round(exportProgress)"
        status="active"
        :show-info="true"
      />
      <p class="progress-text">正在导出检核结果，请稍候...</p>
    </div>

    <!-- 统计概览卡片 -->
    <div v-if="overallStatistics" class="statistics-overview">
      <a-row :gutter="[16, 16]">
        <!-- 按严重程度统计 -->
        <a-col :xl="8" :lg="8" :md="24" :sm="24">
          <a-card title="严重程度分布" class="stats-card">
            <div class="severity-stats">
              <div
                v-for="item in severityStats"
                :key="item.type"
                class="severity-item"
                @click="handleSeverityFilter(item.type)"
                :class="{ active: activeSeverity === item.type }"
              >
                <div class="severity-info">
                  <div class="severity-color" :style="{ backgroundColor: item.color }"></div>
                  <span class="severity-label">{{ item.label }}</span>
                </div>
                <div class="severity-count">{{ item.count }}</div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 按文档类型统计 -->
        <a-col :xl="8" :lg="8" :md="24" :sm="24">
          <a-card title="文档类型分布" class="stats-card">
            <div class="document-stats">
              <div
                v-for="item in documentTypeStats"
                :key="item.type"
                class="document-item"
                @click="handleDocumentTypeFilter(item.type)"
                :class="{ active: activeDocumentType === item.type }"
              >
                <div class="document-info">
                  <div class="document-color" :style="{ backgroundColor: item.color }"></div>
                  <span class="document-label">{{ item.label }}</span>
                </div>
                <div class="document-count">{{ item.count }}</div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 按规则类型统计 -->
        <a-col :xl="8" :lg="8" :md="24" :sm="24">
          <a-card title="规则类型分布" class="stats-card">
            <div class="rule-type-stats">
              <div
                v-for="item in ruleTypeStats"
                :key="item.type"
                class="rule-type-item"
                @click="handleRuleTypeFilter(item.type)"
                :class="{ active: activeRuleType === item.type }"
              >
                <div class="rule-type-info">
                  <span class="rule-type-label">{{ item.label }}</span>
                </div>
                <div class="rule-type-count">{{ item.count }}</div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <a-card>
        <a-row :gutter="16" align="middle">
          <a-col :flex="1">
            <a-space wrap>
              <span class="filter-label">文档类型：</span>
              <a-radio-group
                v-model:value="activeDocumentType"
                button-style="solid"
                size="small"
                @change="handleDocumentTypeChange"
              >
                <a-radio-button value="all">全部</a-radio-button>
                <a-radio-button value="terms">条款</a-radio-button>
                <a-radio-button value="feasibility">可行性报告</a-radio-button>
                <a-radio-button value="comparison">对比表</a-radio-button>
              </a-radio-group>

              <a-divider type="vertical" />

              <span class="filter-label">规则类型：</span>
              <a-select
                v-model:value="activeRuleType"
                placeholder="选择规则类型"
                allow-clear
                style="width: 120px"
                size="small"
                @change="handleRuleTypeChange"
              >
                <a-select-option value="single">单句规则</a-select-option>
                <a-select-option value="double">双句规则</a-select-option>
                <a-select-option value="format">格式规则</a-select-option>
                <a-select-option value="advanced">高级规则</a-select-option>
              </a-select>

              <span class="filter-label">严重程度：</span>
              <a-select
                v-model:value="activeSeverity"
                placeholder="选择严重程度"
                allow-clear
                style="width: 100px"
                size="small"
                @change="handleSeverityChange"
              >
                <a-select-option value="high">高</a-select-option>
                <a-select-option value="medium">中</a-select-option>
                <a-select-option value="low">低</a-select-option>
              </a-select>
            </a-space>
          </a-col>
          <a-col :flex="none">
            <a-space>
              <a-button size="small" @click="handleResetFilters">
                重置筛选
              </a-button>
              <span class="filter-result">
                显示 {{ filteredTotal }} 个结果
              </span>
            </a-space>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 结果详情表格 -->
    <div class="results-table-section">
      <a-card>
        <template #title>
          <div class="table-header">
            <span>检核结果详情</span>
            <a-tag v-if="state.total > 0" color="blue">
              共 {{ state.total }} 条记录
            </a-tag>
          </div>
        </template>

        <a-table
          :dataSource="state.results"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          row-key="id"
          size="middle"
          :scroll="{ x: 1200 }"
          @change="handleTableChange"
        >
          <!-- 自定义列渲染 -->
          <template #bodyCell="{ column, record }">
            <!-- 规则编号 -->
            <template v-if="column.key === 'ruleCode'">
              <a-tag color="blue">{{ record.ruleCode }}</a-tag>
            </template>

            <!-- 文档类型 -->
            <template v-if="column.key === 'documentType'">
              <a-tag :color="getDocumentTypeColor(record.documentType)">
                {{ getDocumentTypeName(record.documentType) }}
              </a-tag>
            </template>

            <!-- 规则类型 -->
            <template v-if="column.key === 'ruleType'">
              <a-tag>{{ getRuleTypeName(record.ruleType) }}</a-tag>
            </template>

            <!-- 严重程度 -->
            <template v-if="column.key === 'severity'">
              <a-tag :color="getSeverityColor(record.severity)">
                {{ getSeverityName(record.severity) }}
              </a-tag>
            </template>

            <!-- 问题描述 -->
            <template v-if="column.key === 'suggestion'">
              <a-tooltip :title="record.suggestion">
                <div class="suggestion-content">{{ record.suggestion }}</div>
              </a-tooltip>
            </template>

            <!-- 原文内容 -->
            <template v-if="column.key === 'originalContent'">
              <a-tooltip :title="record.originalContent">
                <div class="content-preview">
                  {{ record.originalContent }}
                </div>
              </a-tooltip>
            </template>

            <!-- 页码 -->
            <template v-if="column.key === 'pageNumber'">
              <span>{{ formatPageNumber(record.pageNumber) }}</span>
            </template>

            <!-- 操作 -->
            <template v-if="column.key === 'operation'">
              <a-space>
                <a-button
                  type="link"
                  size="small"
                  :icon="h(EyeOutlined)"
                  @click="handleViewDetail(record)"
                >
                  查看
                </a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 结果详情模态框 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="问题详情"
      width="800px"
      :footer="null"
      @cancel="closeDetailModal"
    >
      <div v-if="selectedResult" class="result-detail">
        <a-descriptions :column="1" bordered>
          <a-descriptions-item label="规则编号">
            <a-tag color="blue">{{ selectedResult.ruleCode }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="文档类型">
            <a-tag :color="getDocumentTypeColor(selectedResult.documentType)">
              {{ getDocumentTypeName(selectedResult.documentType) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="规则类型">
            <a-tag>{{ getRuleTypeName(selectedResult.ruleType) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="严重程度">
            <a-tag :color="getSeverityColor(selectedResult.severity)">
              {{ getSeverityName(selectedResult.severity) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="适用章节">
            {{ selectedResult.applicableChapter }}
          </a-descriptions-item>
          <a-descriptions-item label="管理部门">
            {{ selectedResult.managementDepartment }}
          </a-descriptions-item>
          <a-descriptions-item label="页码">
            {{ formatPageNumber(selectedResult.pageNumber) }}
          </a-descriptions-item>
          <a-descriptions-item label="问题描述">
            <div class="problem-description">{{ selectedResult.suggestion }}</div>
          </a-descriptions-item>
          <a-descriptions-item label="原文内容">
            <div class="original-content">{{ selectedResult.originalContent }}</div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  DownloadOutlined,
  FilePdfOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import { useAuditResults } from '@/composables/audit/useAuditResults'
import type {
  AuditResultDisplayProps,
  AuditResultDisplayEvents,
  AuditResult,
  AuditResultQueryParams
} from '@/api/types/audit'

// Props 定义
interface Props extends AuditResultDisplayProps {}

// Events 定义
interface Emits extends AuditResultDisplayEvents {}

const props = withDefaults(defineProps<Props>(), {
  showExport: true
})

const emit = defineEmits<Emits>()

// 使用检核结果 Composable
const {
  state,
  overallStatistics,
  loading,
  pagination,
  documentTypeStats,
  ruleTypeStats,
  severityStats,
  filteredTotal,
  exporting,
  exportProgress,
  activeDocumentType,
  activeRuleType,
  activeSeverity,
  refreshData,
  filterByDocumentType,
  filterByRuleType,
  filterBySeverity,
  resetFilters,
  exportResults,
  getDocumentTypeName,
  getRuleTypeName,
  getSeverityName,
  getSeverityColor,
  formatPageNumber,
  handlePageChange,
  handlePageSizeChange
} = useAuditResults({
  taskId: props.taskId,
  autoLoad: true,
  defaultParams: props.resultData ? {} : undefined
})

// 本地状态
const detailModalVisible = ref(false)
const selectedResult = ref<AuditResult | null>(null)

// 表格列配置
const columns = [
  {
    title: '规则编号',
    dataIndex: 'ruleCode',
    key: 'ruleCode',
    width: 100,
    fixed: 'left' as const
  },
  {
    title: '文档类型',
    dataIndex: 'documentType',
    key: 'documentType',
    width: 120
  },
  {
    title: '规则类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 100
  },
  {
    title: '严重程度',
    dataIndex: 'severity',
    key: 'severity',
    width: 100
  },
  {
    title: '问题描述',
    dataIndex: 'suggestion',
    key: 'suggestion',
    ellipsis: true,
    width: 250
  },
  {
    title: '原文内容',
    dataIndex: 'originalContent',
    key: 'originalContent',
    ellipsis: true,
    width: 200
  },
  {
    title: '页码',
    dataIndex: 'pageNumber',
    key: 'pageNumber',
    width: 80
  },
  {
    title: '适用章节',
    dataIndex: 'applicableChapter',
    key: 'applicableChapter',
    width: 100
  },
  {
    title: '操作',
    key: 'operation',
    width: 80,
    fixed: 'right' as const
  }
]

// 事件处理函数
const handleExportExcel = async () => {
  try {
    const downloadUrl = await exportResults('excel')
    if (downloadUrl) {
      emit('export-results', 'excel')
    }
  } catch (error) {
    console.error('Export excel error:', error)
  }
}

const handleExportPdf = async () => {
  try {
    const downloadUrl = await exportResults('pdf')
    if (downloadUrl) {
      emit('export-results', 'pdf')
    }
  } catch (error) {
    console.error('Export pdf error:', error)
  }
}

const handleDocumentTypeFilter = (type: string) => {
  if (activeDocumentType.value === type) {
    activeDocumentType.value = 'all'
    filterByDocumentType('all')
  } else {
    filterByDocumentType(type)
  }
}

const handleDocumentTypeChange = (e: any) => {
  filterByDocumentType(e.target.value)
  emit('filter-change', { documentType: e.target.value } as AuditResultQueryParams)
}

const handleRuleTypeFilter = (type: string) => {
  if (activeRuleType.value === type) {
    activeRuleType.value = ''
    filterByRuleType('')
  } else {
    filterByRuleType(type)
  }
}

const handleRuleTypeChange = (value: string) => {
  filterByRuleType(value || '')
  emit('filter-change', { ruleType: value } as AuditResultQueryParams)
}

const handleSeverityFilter = (severity: string) => {
  if (activeSeverity.value === severity) {
    activeSeverity.value = ''
    filterBySeverity('')
  } else {
    filterBySeverity(severity)
  }
}

const handleSeverityChange = (value: string) => {
  filterBySeverity(value || '')
  emit('filter-change', { severity: value } as AuditResultQueryParams)
}

const handleResetFilters = () => {
  resetFilters()
  emit('filter-change', {} as AuditResultQueryParams)
}

const handleTableChange = (pag: any, filters: any, sorter: any) => {
  handlePageChange(pag.current, pag.pageSize)
}

const handleViewDetail = (record: AuditResult) => {
  selectedResult.value = record
  detailModalVisible.value = true
  emit('view-detail', record.id)
}

const closeDetailModal = () => {
  detailModalVisible.value = false
  selectedResult.value = null
}

// 工具函数
const getDocumentTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    terms: '#1890ff',
    feasibility: '#52c41a',
    comparison: '#faad14'
  }
  return colorMap[type] || 'default'
}
</script>

<style scoped>
.audit-result-display {
  padding: 0;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.result-summary {
  font-size: 14px;
  color: #595959;
}

.export-progress-section {
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.progress-text {
  text-align: center;
  margin-top: 8px;
  margin-bottom: 0;
  color: #595959;
}

.statistics-overview {
  margin-bottom: 16px;
}

.stats-card {
  height: 200px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stats-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stats-card :deep(.ant-card-body) {
  padding: 20px;
  height: 140px;
  display: flex;
  flex-direction: column;
}

.severity-stats,
.document-stats,
.rule-type-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.severity-item,
.document-item,
.rule-type-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.severity-item:hover,
.document-item:hover,
.rule-type-item:hover {
  background: #f5f5f5;
}

.severity-item.active,
.document-item.active,
.rule-type-item.active {
  background: #e6f7ff;
  border: 1px solid #1890ff;
}

.severity-info,
.document-info,
.rule-type-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.severity-color,
.document-color {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.severity-label,
.document-label,
.rule-type-label {
  font-size: 14px;
  color: #262626;
}

.severity-count,
.document-count,
.rule-type-count {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.filter-toolbar {
  margin-bottom: 16px;
}

.filter-label {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

.filter-result {
  font-size: 14px;
  color: #595959;
}

.results-table-section {
  margin-bottom: 16px;
}

.table-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.suggestion-content,
.content-preview {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-detail {
  margin-top: 16px;
}

.problem-description,
.original-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .result-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-info {
    justify-content: center;
    text-align: center;
  }

  .header-actions {
    display: flex;
    justify-content: center;
  }

  .filter-toolbar .ant-row {
    flex-direction: column;
    gap: 16px;
  }

  .stats-card {
    height: auto;
  }

  .stats-card :deep(.ant-card-body) {
    height: auto;
  }
}

@media (max-width: 576px) {
  .result-header {
    padding: 12px 16px;
  }

  .section-title {
    font-size: 18px;
  }

  .statistics-overview .ant-col {
    margin-bottom: 16px;
  }
}
</style>