<template>
  <div class="rules-management">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <a-button
          type="link"
          class="back-button"
          @click="handleBack"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title">规则管理</h1>
      </div>
      <div class="header-right">
        <a-button @click="handleRefresh">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
      </div>
    </div>

    <!-- 顶部工具栏 -->
    <RulesToolbar
      :selected-count="selectedCount"
      :total-count="totalCount"
      @import="handleImport"
      @add="handleAdd"
      @all-rules="handleAllRules"
      @select-all="handleSelectAll"
      @batch-delete="handleBatchDelete"
      @single-rule-create="handleSingleRuleCreate"
      @focus="handleFocus"
      @template-batch-import="handleTemplateBatchImport"
      @submit-oa="handleSubmitOA"
    />

    <!-- 筛选区域 -->
    <RulesFilterBar
      :filters="filters"
      @search="handleSearch"
      @reset="handleResetFilters"
    />

    <!-- 已选条件 -->
    <SelectedChips
      :selected-filters="selectedFilters"
      @remove-filter="removeFilter"
      @clear-all="clearAllFilters"
    />

    <!-- 规则列表表格 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="rulesData"
        :row-selection="{
          selectedRowKeys: selectedRules,
          onChange: onSelectChange,
          preserveSelectedRowKeys: true
        }"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'ruleName'">
            <a @click="handleEdit(record)">{{ record.ruleName }}</a>
          </template>
          <template v-else-if="column.key === 'auditStatus'">
            <a-tag :color="getStatusColor(record.auditStatus)">
              {{ getStatusText(record.auditStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'effectiveStatus'">
            <a-tag :color="getStatusColor(record.effectiveStatus)">
              {{ getStatusText(record.effectiveStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'followed'">
            <a-button
              type="link"
              @click="handleToggleFollow(record)"
            >
              <StarFilled v-if="record.followed" style="color: #faad14;" />
              <StarOutlined v-else />
            </a-button>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-dropdown>
              <a-button type="link">
                操作 <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu>
                  <PermissionWrapper permissions="RULE_EDIT">
                    <a-menu-item @click="handleEdit(record)">
                      <EditOutlined />
                      编辑
                    </a-menu-item>
                  </PermissionWrapper>
                  <a-menu-item @click="handleToggleFollow(record)">
                    <StarOutlined />
                    {{ record.followed ? '取消关注' : '关注' }}
                  </a-menu-item>
                  <PermissionWrapper permissions="RULE_COPY">
                    <a-menu-item @click="$router.push(`/rules/copy/${record.id}`)">
                      <CopyOutlined />
                      复制
                    </a-menu-item>
                  </PermissionWrapper>
                  <a-menu-divider />
                  <PermissionWrapper permissions="RULE_DELETE">
                    <a-menu-item @click="handleDelete(record)" class="danger-item">
                      <DeleteOutlined />
                      删除
                    </a-menu-item>
                  </PermissionWrapper>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 导入规则对话框 -->
    <ImportModal
      v-model:visible="importModalVisible"
      @success="handleImportSuccess"
    />

    <!-- 提交OA审核对话框 -->
    <SubmitOAModal
      v-model:visible="submitOAModalVisible"
      :selected-rules="selectedRules"
      @success="handleSubmitOASuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  ArrowLeftOutlined,
  ReloadOutlined,
  StarFilled,
  StarOutlined,
  DownOutlined,
  EditOutlined,
  CopyOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRulesList } from '@/composables/rules/useRulesList'
import RulesToolbar from '@/components/rules/RulesToolbar.vue'
import RulesFilterBar from '@/components/rules/RulesFilterBar.vue'
import SelectedChips from '@/components/rules/SelectedChips.vue'
import ImportModal from '@/components/rules/ImportModal.vue'
import SubmitOAModal from '@/components/rules/SubmitOAModal.vue'
import PermissionWrapper from '@/components/common/PermissionWrapper.vue'
import { rulesApi } from '@/api/modules/rules'

// 使用composable
const {
  loading,
  rulesData,
  selectedRules,
  pagination,
  filters,
  selectedFilters,
  selectedCount,
  totalCount,
  fetchRulesList,
  handleSearch,
  handleResetFilters,
  removeFilter,
  clearAllFilters,
  handleTableChange,
  handleSelectAll,
  handleBatchDelete,
  handleAdd,
  handleSingleRuleCreate,
  handleBack,
  handleRefresh,
  handleAllRules,
  handleFocus,
  handleEdit,
  handleDelete,
  handleToggleFollow
} = useRulesList()

// 弹窗状态
const importModalVisible = ref(false)
const submitOAModalVisible = ref(false)

// 表格列定义
const columns = [
  {
    title: '',
    key: 'selection',
    width: 50,
    align: 'center'
  },
  {
    title: '规则编号',
    dataIndex: 'ruleNumber',
    key: 'ruleNumber',
    width: 120
  },
  {
    title: '规则描述',
    dataIndex: 'ruleDescription',
    key: 'ruleDescription',
    width: 250
  },
  {
    title: '规则来源',
    dataIndex: 'ruleSource',
    key: 'ruleSource',
    width: 120
  },
  {
    title: '规则管理部门',
    dataIndex: 'managementDepartment',
    key: 'managementDepartment',
    width: 150
  },
  {
    title: '适用险种',
    dataIndex: 'applicableInsurance',
    key: 'applicableInsurance',
    width: 120
  },
  {
    title: '适用要件',
    dataIndex: 'applicableRequirements',
    key: 'applicableRequirements',
    width: 150
  },
  {
    title: '适用章节',
    dataIndex: 'applicableChapter',
    key: 'applicableChapter',
    width: 120
  },
  {
    title: '审核状态',
    dataIndex: 'auditStatus',
    key: 'auditStatus',
    width: 120
  },
  {
    title: '有效状态',
    dataIndex: 'effectiveStatus',
    key: 'effectiveStatus',
    width: 120
  },
  {
    title: '最后更新时间',
    dataIndex: 'lastUpdatedAt',
    key: 'lastUpdatedAt',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right'
  }
]

// 选择改变
const onSelectChange = (selectedRowKeys: string[]) => {
  selectedRules.value = selectedRowKeys
}

// 状态颜色
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'PENDING_TECH_EVALUATION': 'orange',
    'PENDING_OA_SUBMISSION': 'blue',
    'SUBMITTED_TO_OA': 'cyan',
    'OA_APPROVED': 'green',
    'OA_REJECTED': 'red',
    'TECH_APPROVED': 'green',
    'TECH_REJECTED': 'red',
    'EFFECTIVE': 'green',
    'INACTIVE': 'red',
    'PENDING_DEPLOYMENT': 'orange',
    'OFFLINE': 'default',
    'TESTING': 'blue'
  }
  return colorMap[status] || 'default'
}

// 状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'PENDING_TECH_EVALUATION': '待技术评估',
    'PENDING_OA_SUBMISSION': '待提交OA',
    'SUBMITTED_TO_OA': '已提交OA',
    'OA_APPROVED': 'OA已通过',
    'OA_REJECTED': 'OA驳回',
    'TECH_APPROVED': '技术评估通过',
    'TECH_REJECTED': '技术评估驳回',
    'EFFECTIVE': '有效',
    'INACTIVE': '无效',
    'PENDING_DEPLOYMENT': '待上线',
    'OFFLINE': '已下线',
    'TESTING': '测试中'
  }
  return textMap[status] || status
}

// 导入
const handleImport = () => {
  importModalVisible.value = true
}

// 模板批量导入
const handleTemplateBatchImport = async () => {
  try {
    const blob = await rulesApi.downloadTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '规则导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    message.error('下载模板失败')
  }
}

// 提交OA
const handleSubmitOA = () => {
  if (selectedRules.value.length === 0) {
    message.warning('请先选择要提交的规则')
    return
  }
  submitOAModalVisible.value = true
}

// 导入成功
const handleImportSuccess = () => {
  fetchRulesList()
  message.success('导入成功')
}

// 提交OA成功
const handleSubmitOASuccess = () => {
  fetchRulesList()
  selectedRules.value = []
  message.success('提交OA审核成功')
}
</script>

<style scoped>
.rules-management {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-button {
  padding: 0;
  color: #666;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  gap: 8px;
}

.table-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.danger-item {
  color: #ff4d4f;
}

.danger-item:hover {
  background-color: #fff2f0;
}
</style>