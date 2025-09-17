<template>
  <div class="rules-management" data-testid="rules-management">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header" data-testid="page-header">
      <div class="header-left">
        <a-button
          type="link"
          class="back-button"
          data-testid="back-button"
          @click="handleBack"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title" data-testid="page-title">规则管理</h1>
      </div>
      <div class="header-right">
        <a-button data-testid="refresh-button" @click="handleRefresh">
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
    <RulesTable
      :data-source="rulesData"
      :loading="loading"
      :selected-row-keys="selectedRules"
      :pagination="pagination"
      @select-change="onSelectChange"
      @table-change="handleTableChange"
      @toggle-follow="handleToggleFollow"
      @edit="handleEdit"
      @copy="handleCopy"
      @delete="handleDelete"
    />

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
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useRulesList } from '@/composables/rules/useRulesList'
import RulesToolbar from '@/components/rules/RulesToolbar.vue'
import RulesFilterBar from '@/components/rules/RulesFilterBar.vue'
import SelectedChips from '@/components/rules/SelectedChips.vue'
import RulesTable from '@/components/rules/RulesTable.vue'
import ImportModal from '@/components/rules/ImportModal.vue'
import SubmitOAModal from '@/components/rules/SubmitOAModal.vue'
import { rulesApi } from '@/api/modules/rules'

const router = useRouter()

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

// 选择改变
const onSelectChange = (selectedRowKeys: string[]) => {
  selectedRules.value = selectedRowKeys
}

// 复制操作
const handleCopy = (record: any) => {
  router.push(`/rules/copy/${record.id}`)
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
</style>