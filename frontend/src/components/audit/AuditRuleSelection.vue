<template>
  <div class="audit-rule-selection">
    <!-- 步骤指示器 -->
    <div class="steps-container">
      <a-steps :current="0" size="small">
        <a-step title="选择规则" description="选择要执行的检核规则" />
        <a-step title="执行检核" description="执行产品检核任务" />
        <a-step title="查看结果" description="查看检核结果详情" />
      </a-steps>
    </div>

    <!-- 规则筛选区域 -->
    <div class="filter-section">
      <a-card title="规则筛选" class="filter-card">
        <a-form
          :model="filterForm"
          layout="horizontal"
          class="filter-form"
          @finish="handleSearch"
        >
          <a-row :gutter="16">
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="规则来源" name="ruleSource">
                <a-select
                  v-model:value="filterForm.ruleSource"
                  placeholder="请选择规则来源"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="SYSTEM">系统规则</a-select-option>
                  <a-select-option value="CUSTOM">自定义规则</a-select-option>
                  <a-select-option value="IMPORTED">导入规则</a-select-option>
                  <a-select-option value="TEMPLATE">模板规则</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="适用险种" name="insuranceTypes">
                <a-select
                  v-model:value="filterForm.insuranceTypes"
                  mode="multiple"
                  placeholder="请选择适用险种"
                  allow-clear
                >
                  <a-select-option value="life">人寿保险</a-select-option>
                  <a-select-option value="property">财产保险</a-select-option>
                  <a-select-option value="health">健康保险</a-select-option>
                  <a-select-option value="accident">意外保险</a-select-option>
                  <a-select-option value="auto">车险</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="管理部门" name="managementDepartment">
                <a-select
                  v-model:value="filterForm.managementDepartment"
                  placeholder="请选择管理部门"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="tech">技术部门</a-select-option>
                  <a-select-option value="business">业务部门</a-select-option>
                  <a-select-option value="risk">风控部门</a-select-option>
                  <a-select-option value="product">产品部门</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="适用章节" name="applicableChapter">
                <a-select
                  v-model:value="filterForm.applicableChapter"
                  placeholder="请选择适用章节"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="chapter1">第一章</a-select-option>
                  <a-select-option value="chapter2">第二章</a-select-option>
                  <a-select-option value="chapter3">第三章</a-select-option>
                  <a-select-option value="chapter4">第四章</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="16">
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="有效状态" name="effectiveStatus">
                <a-select
                  v-model:value="filterForm.effectiveStatus"
                  placeholder="请选择有效状态"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="active">有效</a-select-option>
                  <a-select-option value="inactive">无效</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="审核状态" name="auditStatus">
                <a-select
                  v-model:value="filterForm.auditStatus"
                  placeholder="请选择审核状态"
                  allow-clear
                >
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="approved">已审核</a-select-option>
                  <a-select-option value="pending">待审核</a-select-option>
                  <a-select-option value="rejected">已驳回</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24">
              <a-form-item label="关键词" name="keyword">
                <a-input
                  v-model:value="filterForm.keyword"
                  placeholder="请输入规则名称或编号"
                  allow-clear
                  @press-enter="handleSearch"
                />
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="8" :md="12" :sm="24" class="search-actions">
              <a-space>
                <a-button @click="handleReset">
                  <template #icon>
                    <ReloadOutlined />
                  </template>
                  重置
                </a-button>
                <a-button type="primary" html-type="submit" :loading="loading">
                  <template #icon>
                    <SearchOutlined />
                  </template>
                  搜索
                </a-button>
              </a-space>
            </a-col>
          </a-row>
        </a-form>
      </a-card>
    </div>

    <!-- 规则列表区域 -->
    <div class="rules-section">
      <a-card class="rules-card">
        <template #title>
          <div class="rules-header">
            <span>检核规则列表</span>
            <div class="rules-stats">
              <a-space>
                <span>共 {{ pagination.total }} 条规则</span>
                <a-divider type="vertical" />
                <span>已选择 {{ selectedCount }} 条</span>
              </a-space>
            </div>
          </div>
        </template>

        <template #extra>
          <a-space>
            <a-checkbox
              :checked="isCurrentPageAllSelected"
              :indeterminate="isCurrentPageIndeterminate"
              @change="handleSelectAllChange"
            >
              全选当前页
            </a-checkbox>
            <a-button
              v-if="hasSelected"
              size="small"
              @click="clearSelection"
            >
              清空选择
            </a-button>
          </a-space>
        </template>

        <!-- 规则表格 -->
        <a-table
          :dataSource="state.rules"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          row-key="id"
          size="middle"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'selection'">
              <a-checkbox
                :checked="isRuleSelected(record.id)"
                @change="() => toggleRuleSelection(record.id)"
              />
            </template>
            <template v-else-if="column.key === 'code'">
              <a-tag :color="getRuleCodeColor(record.code)">{{ record.code }}</a-tag>
            </template>
            <template v-else-if="column.key === 'name'">
              <a-tooltip :title="record.description">
                <span class="rule-name">{{ record.name }}</span>
              </a-tooltip>
            </template>
            <template v-else-if="column.key === 'applicableInsuranceType'">
              <a-space>
                <a-tag
                  v-for="type in record.applicableInsuranceType"
                  :key="type"
                  size="small"
                >
                  {{ getInsuranceTypeLabel(type) }}
                </a-tag>
              </a-space>
            </template>
            <template v-else-if="column.key === 'ruleType'">
              <a-tag :color="getRuleTypeColor(record.ruleType)">
                {{ getRuleTypeLabel(record.ruleType) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'effectiveStatus'">
              <a-tag :color="record.effectiveStatus === 'active' ? 'green' : 'red'">
                {{ record.effectiveStatus === 'active' ? '有效' : '无效' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'auditStatus'">
              <a-tag :color="getAuditStatusColor(record.auditStatus)">
                {{ getAuditStatusLabel(record.auditStatus) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'effectiveStartTime'">
              {{ formatDate(record.effectiveStartTime) }}
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 操作按钮区域 -->
    <div class="actions-section">
      <a-card>
        <div class="actions-content">
          <div class="selection-summary">
            <a-space>
              <span>已选择规则：</span>
              <a-tag v-if="hasSelected" color="blue">{{ selectedCount }} 条</a-tag>
              <span v-else class="no-selection">未选择任何规则</span>
            </a-space>
          </div>
          <div class="action-buttons">
            <a-space>
              <a-button @click="handleCancel">取消</a-button>
              <a-button
                type="primary"
                :disabled="!hasSelected"
                @click="handleConfirm"
              >
                确认选择
              </a-button>
            </a-space>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { useAuditRules } from '@/composables/audit/useAuditRules'
import type {
  AuditRuleSelectionProps,
  AuditRuleSelectionEvents,
  AuditRuleQueryParams
} from '@/api/types/audit'

// Props 定义
interface Props extends AuditRuleSelectionProps {}

// Events 定义
interface Emits extends AuditRuleSelectionEvents {}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  loading: false
})

const emit = defineEmits<Emits>()

// 使用检核规则 Composable
const {
  state,
  filterForm,
  loading,
  selectedCount,
  hasSelected,
  pagination,
  isCurrentPageAllSelected,
  isCurrentPageIndeterminate,
  searchRules,
  resetFilter,
  toggleRuleSelection,
  selectAllCurrentPage,
  deselectAllCurrentPage,
  clearSelection,
  isRuleSelected,
  getSelectedRules,
  validateSelection,
  handlePageChange,
  handlePageSizeChange
} = useAuditRules({
  autoLoad: true,
  defaultParams: {
    effectiveStatus: 'active',
    auditStatus: 'approved'
  }
})

// 表格列配置
const columns = [
  {
    title: '选择',
    key: 'selection',
    width: 60,
    fixed: 'left' as const
  },
  {
    title: '规则编号',
    dataIndex: 'code',
    key: 'code',
    width: 100,
    sorter: true
  },
  {
    title: '规则名称',
    dataIndex: 'name',
    key: 'name',
    ellipsis: true
  },
  {
    title: '适用险种',
    dataIndex: 'applicableInsuranceType',
    key: 'applicableInsuranceType',
    width: 150
  },
  {
    title: '规则类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 100
  },
  {
    title: '管理部门',
    dataIndex: 'managementDepartment',
    key: 'managementDepartment',
    width: 120
  },
  {
    title: '适用章节',
    dataIndex: 'applicableChapter',
    key: 'applicableChapter',
    width: 100
  },
  {
    title: '有效状态',
    dataIndex: 'effectiveStatus',
    key: 'effectiveStatus',
    width: 100
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 100
  },
  {
    title: '生效时间',
    dataIndex: 'effectiveStartTime',
    key: 'effectiveStartTime',
    width: 120
  }
]

// 事件处理函数
const handleSearch = () => {
  emit('search', { ...filterForm, page: 1 })
  searchRules()
}

const handleReset = () => {
  emit('reset')
  resetFilter()
}

const handleSelectAllChange = (e: any) => {
  if (e.target.checked) {
    selectAllCurrentPage()
  } else {
    deselectAllCurrentPage()
  }
}

const handleTableChange = (pag: any, filters: any, sorter: any) => {
  const params: AuditRuleQueryParams = {
    page: pag.current,
    size: pag.pageSize
  }

  // 处理排序
  if (sorter.field && sorter.order) {
    params.sortField = sorter.field
    params.sortOrder = sorter.order === 'ascend' ? 'asc' : 'desc'
  }

  handlePageChange(params.page!, params.size!)
}

const handleCancel = () => {
  emit('cancel')
}

const handleConfirm = () => {
  if (validateSelection()) {
    const selectedRules = getSelectedRules()
    const selectedRuleIds = selectedRules.map(rule => rule.id)
    emit('rule-selected', selectedRuleIds)
  }
}

// 工具函数
const getRuleCodeColor = (code: string): string => {
  const num = parseInt(code.replace('CC', ''))
  if (num <= 5) return 'blue'
  if (num <= 10) return 'green'
  if (num <= 15) return 'orange'
  return 'red'
}

const getRuleTypeColor = (type: string): string => {
  const colorMap: Record<string, string> = {
    single: 'blue',
    double: 'green',
    format: 'orange',
    advanced: 'red'
  }
  return colorMap[type] || 'default'
}

const getRuleTypeLabel = (type: string): string => {
  const labelMap: Record<string, string> = {
    single: '单句规则',
    double: '双句规则',
    format: '格式规则',
    advanced: '高级规则'
  }
  return labelMap[type] || type
}

const getInsuranceTypeLabel = (type: string): string => {
  const labelMap: Record<string, string> = {
    life: '人寿',
    property: '财产',
    health: '健康',
    accident: '意外',
    auto: '车险'
  }
  return labelMap[type] || type
}

const getAuditStatusColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    approved: 'green',
    pending: 'orange',
    rejected: 'red'
  }
  return colorMap[status] || 'default'
}

const getAuditStatusLabel = (status: string): string => {
  const labelMap: Record<string, string> = {
    approved: '已审核',
    pending: '待审核',
    rejected: '已驳回'
  }
  return labelMap[status] || status
}

const formatDate = (date: Date | string): string => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.audit-rule-selection {
  padding: 0;
}

.steps-container {
  margin-bottom: 24px;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-section {
  margin-bottom: 16px;
}

.filter-card {
  border-radius: 8px;
}

.filter-form {
  margin-bottom: 0;
}

.filter-form .ant-form-item {
  margin-bottom: 16px;
}

.filter-form .ant-form-item-label {
  width: 80px;
  flex: 0 0 80px;
}

.search-actions {
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding-top: 30px;
}

.rules-section {
  margin-bottom: 16px;
}

.rules-card {
  border-radius: 8px;
}

.rules-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.rules-stats {
  font-size: 14px;
  color: #666;
  font-weight: normal;
}

.rule-name {
  cursor: pointer;
  color: #1890ff;
}

.rule-name:hover {
  text-decoration: underline;
}

.actions-section {
  position: sticky;
  bottom: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.actions-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.selection-summary {
  flex: 1;
}

.no-selection {
  color: #999;
}

.action-buttons {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .filter-form .ant-form-item-label {
    width: 60px;
    flex: 0 0 60px;
  }

  .search-actions {
    justify-content: flex-start;
  }

  .rules-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .actions-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .action-buttons {
    display: flex;
    justify-content: flex-end;
  }
}
</style>