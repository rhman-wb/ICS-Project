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
    <div class="toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="handleImport">
          <template #icon>
            <ImportOutlined />
          </template>
          导入
        </a-button>
        <a-button type="primary" @click="handleAdd">
          <template #icon>
            <PlusOutlined />
          </template>
          添加
        </a-button>
        <a-button @click="handleAllRules">全部规则</a-button>
        <a-divider type="vertical" />
        <a-button @click="handleSelectAll">
          <template #icon>
            <CheckOutlined />
          </template>
          {{ selectedRules.length === rulesData.length && rulesData.length > 0 ? '取消全选' : '全选' }}
        </a-button>
        <span class="selection-info" v-if="selectedRules.length > 0">
          已选择 {{ selectedRules.length }} 项
        </span>
      </div>
      <div class="toolbar-right">
        <a-button
          danger
          :disabled="selectedRules.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon>
            <DeleteOutlined />
          </template>
          删除
        </a-button>
        <a-button @click="handleSingleRuleCreate">单条规则创建</a-button>
        <a-button
          :disabled="selectedRules.length === 0"
          @click="handleFocus"
        >
          <template #icon>
            <StarOutlined />
          </template>
          关注
        </a-button>
        <a-button @click="handleTemplateBatchImport">
          <template #icon>
            <DownloadOutlined />
          </template>
          模板批量导入
        </a-button>
        <a-button
          type="primary"
          :disabled="selectedRules.length === 0"
          @click="handleSubmitOA"
        >
          提交OA审核
        </a-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <a-card>
        <div class="filter-form">
          <a-row :gutter="16">
            <a-col :span="6">
              <div class="filter-item">
                <label>规则来源：</label>
                <a-select v-model:value="filters.ruleSource" placeholder="请选择规则来源" allowClear>
                  <a-select-option value="system">系统规则</a-select-option>
                  <a-select-option value="custom">自定义规则</a-select-option>
                  <a-select-option value="imported">导入规则</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>适用险种：</label>
                <a-select v-model:value="filters.applicableInsurance" placeholder="请选择适用险种" allowClear>
                  <a-select-option value="life">人寿保险</a-select-option>
                  <a-select-option value="property">财产保险</a-select-option>
                  <a-select-option value="health">健康保险</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>规则管理部门：</label>
                <a-select v-model:value="filters.manageDepartment" placeholder="请选择管理部门" allowClear>
                  <a-select-option value="tech">技术部门</a-select-option>
                  <a-select-option value="business">业务部门</a-select-option>
                  <a-select-option value="risk">风控部门</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>审核状态：</label>
                <a-select v-model:value="filters.auditStatus" placeholder="请选择审核状态" allowClear>
                  <a-select-option value="pending-tech">待技术评估</a-select-option>
                  <a-select-option value="pending-oa">待提交OA审核</a-select-option>
                  <a-select-option value="submitted-oa">已提交OA审核</a-select-option>
                  <a-select-option value="oa-approved">OA审核通过</a-select-option>
                </a-select>
              </div>
            </a-col>
          </a-row>
          <a-row :gutter="16" style="margin-top: 16px;">
            <a-col :span="6">
              <div class="filter-item">
                <label>有效状态：</label>
                <a-select v-model:value="filters.effectiveStatus" placeholder="请选择有效状态" allowClear>
                  <a-select-option value="effective">有效</a-select-option>
                  <a-select-option value="inactive">无效</a-select-option>
                  <a-select-option value="pending-deploy">待开发上线</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>适用章节：</label>
                <a-select v-model:value="filters.applicableChapter" placeholder="请选择适用章节" allowClear>
                  <a-select-option value="chapter1">第一章</a-select-option>
                  <a-select-option value="chapter2">第二章</a-select-option>
                  <a-select-option value="chapter3">第三章</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>适用经营区域：</label>
                <a-select v-model:value="filters.businessArea" placeholder="请选择经营区域" allowClear>
                  <a-select-option value="nationwide">全国</a-select-option>
                  <a-select-option value="north">华北</a-select-option>
                  <a-select-option value="south">华南</a-select-option>
                  <a-select-option value="east">华东</a-select-option>
                  <a-select-option value="west">华西</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>输入查询：</label>
                <a-input v-model:value="filters.keyword" placeholder="请输入关键词" allowClear />
              </div>
            </a-col>
          </a-row>
          <a-row :gutter="16" style="margin-top: 16px;">
            <a-col :span="6">
              <div class="filter-item">
                <label>起始时间：</label>
                <a-time-picker v-model:value="filters.startTime" placeholder="09:12:21" format="HH:mm:ss" />
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>终止时间：</label>
                <a-time-picker v-model:value="filters.endTime" placeholder="09:12:21" format="HH:mm:ss" />
              </div>
            </a-col>
            <a-col :span="12">
              <div class="filter-actions">
                <a-button type="primary" @click="handleSearch">
                  <template #icon>
                    <SearchOutlined />
                  </template>
                  搜索
                </a-button>
                <a-button @click="handleResetFilters">重置</a-button>
              </div>
            </a-col>
          </a-row>
        </div>

        <!-- 已选条件 -->
        <div class="selected-filters" v-if="selectedFilters.length > 0">
          <span class="filter-label">已选条件：</span>
          <a-tag
            v-for="filter in selectedFilters"
            :key="filter.key"
            closable
            @close="removeFilter(filter.key)"
          >
            {{ filter.label }}
          </a-tag>
          <a-button type="link" size="small" @click="clearAllFilters">清除全部</a-button>
        </div>
      </a-card>
    </div>

    <!-- 规则列表表格 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="rulesData"
        :pagination="pagination"
        :row-selection="rowSelection"
        :loading="loading"
        @change="handleTableChange"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'auditStatus'">
            <a-tag :color="getAuditStatusColor(record.auditStatus)">
              {{ getAuditStatusText(record.auditStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'effectiveStatus'">
            <a-tag :color="getEffectiveStatusColor(record.effectiveStatus)">
              {{ getEffectiveStatusText(record.effectiveStatus) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'followed'">
            <a-button
              type="link"
              size="small"
              @click="handleToggleFollow(record)"
            >
              <StarFilled v-if="record.followed" style="color: #faad14;" />
              <StarOutlined v-else />
            </a-button>
          </template>
          <template v-if="column.key === 'operation'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleAudit(record)">审核</a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleToggleFollow(record)">
                      {{ record.followed ? '取消关注' : '关注' }}
                    </a-menu-item>
                    <a-menu-item @click="$router.push(`/rules/copy/${record.id}`)">
                      复制
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item danger @click="handleSingleDelete(record)">
                      删除
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多
                  <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 导入规则对话框 -->
    <a-modal
      v-model:open="importVisible"
      title="导入规则"
      width="800px"
      :footer="null"
    >
      <div class="import-content">
        <a-steps :current="importPreviewData ? 1 : 0" size="small">
          <a-step title="选择文件" />
          <a-step title="预览数据" />
          <a-step title="确认导入" />
        </a-steps>

        <div class="import-upload" v-if="!importPreviewData">
          <a-upload-dragger
            :before-upload="handleImportUpload"
            :show-upload-list="false"
            accept=".xlsx,.xls"
          >
            <p class="ant-upload-drag-icon">
              <UploadOutlined />
            </p>
            <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p class="ant-upload-hint">
              支持 Excel 格式文件 (.xlsx, .xls)，请使用标准模板格式
            </p>
          </a-upload-dragger>
          <div class="upload-actions" style="margin-top: 16px; text-align: center;">
            <a-space>
              <a-button @click="handleCancelImport">取消</a-button>
              <a-button type="primary" @click="handleImportPreview" :disabled="!importFile">
                预览数据
              </a-button>
            </a-space>
          </div>
        </div>

        <div class="import-preview" v-if="importPreviewData">
          <a-descriptions bordered size="small" :column="2">
            <a-descriptions-item label="总记录数">{{ importPreviewData.totalRecords }}</a-descriptions-item>
            <a-descriptions-item label="有效记录数">{{ importPreviewData.validRecords }}</a-descriptions-item>
            <a-descriptions-item label="错误记录数">{{ importPreviewData.errorRecords }}</a-descriptions-item>
            <a-descriptions-item label="预期导入">{{ importPreviewData.validRecords }}</a-descriptions-item>
          </a-descriptions>

          <div style="margin-top: 16px;" v-if="importPreviewData.errorRecords > 0">
            <a-alert
              message="存在错误记录"
              description="请检查以下错误信息，只有有效记录会被导入"
              type="warning"
              show-icon
            />
          </div>

          <div class="preview-actions" style="margin-top: 16px; text-align: center;">
            <a-space>
              <a-button @click="handleCancelImport">取消</a-button>
              <a-button @click="importPreviewData = null">重新选择</a-button>
              <a-button
                type="primary"
                @click="handleConfirmImport"
                :disabled="importPreviewData.validRecords === 0"
              >
                确认导入 ({{ importPreviewData.validRecords }} 条)
              </a-button>
            </a-space>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 提交OA审核对话框 -->
    <a-modal
      v-model:open="submitOAVisible"
      title="提交OA审核"
      width="600px"
      @ok="handleConfirmSubmitOA"
      @cancel="handleCancelSubmitOA"
    >
      <div class="submit-oa-content">
        <a-alert
          :message="`即将提交 ${selectedRules.length} 条规则到OA系统进行审核`"
          type="info"
          show-icon
          style="margin-bottom: 16px;"
        />

        <a-form layout="vertical">
          <a-form-item label="备注说明（可选）">
            <a-textarea
              v-model:value="submitOAComments"
              placeholder="请输入提交说明或备注信息"
              :rows="4"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </a-form>

        <a-alert
          message="注意事项"
          description="提交后规则状态将变更为"已提交OA审核"，在OA审核完成前无法进行编辑操作。"
          type="warning"
          show-icon
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { TableColumnsType, TableProps, UploadProps } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ReloadOutlined,
  ImportOutlined,
  PlusOutlined,
  DeleteOutlined,
  StarOutlined,
  SearchOutlined,
  StarFilled,
  UploadOutlined,
  DownloadOutlined,
  CheckOutlined,
  ExclamationCircleOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import { rulesApi, type RuleQueryParams, type RuleInfo, type ImportPreviewResponse } from '@/api/modules/rules'
import { useApiLoading } from '@/composables/useApiLoading'

// 路由
const router = useRouter()

// API加载状态
const { createLoadingRef } = useApiLoading()
const loading = createLoadingRef('rules-list')

// 组件状态
const selectedRules = ref<string[]>([])
const rulesData = ref<RuleInfo[]>([])
const importVisible = ref(false)
const importPreviewData = ref<ImportPreviewResponse | null>(null)
const importFile = ref<File | null>(null)
const submitOAVisible = ref(false)
const submitOAComments = ref('')

// 筛选条件
const filters = reactive<RuleQueryParams>({
  page: 1,
  size: 10,
  ruleSource: undefined,
  applicableInsurance: undefined,
  manageDepartment: undefined,
  auditStatus: undefined,
  effectiveStatus: undefined,
  applicableChapter: undefined,
  businessArea: undefined,
  keyword: '',
  startTime: undefined,
  endTime: undefined,
  sortField: 'lastUpdatedAt',
  sortDirection: 'DESC'
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表格列定义
const columns: TableColumnsType = [
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
    width: 200
  },
  {
    title: '规则来源',
    dataIndex: 'ruleSource',
    key: 'ruleSource',
    width: 100
  },
  {
    title: '规则管理部门',
    dataIndex: 'manageDepartment',
    key: 'manageDepartment',
    width: 120
  },
  {
    title: '适用险种',
    dataIndex: 'applicableInsurance',
    key: 'applicableInsurance',
    width: 100
  },
  {
    title: '适用要件',
    dataIndex: 'applicableRequirements',
    key: 'applicableRequirements',
    width: 120
  },
  {
    title: '适用章节',
    dataIndex: 'applicableChapter',
    key: 'applicableChapter',
    width: 100
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
    width: 100
  },
  {
    title: '关注',
    dataIndex: 'followed',
    key: 'followed',
    width: 80,
    align: 'center'
  },
  {
    title: '最后更新时间',
    dataIndex: 'lastUpdatedAt',
    key: 'lastUpdatedAt',
    width: 150
  },
  {
    title: '操作',
    key: 'operation',
    width: 150,
    fixed: 'right'
  }
]

// 行选择配置
const rowSelection: TableProps['rowSelection'] = {
  selectedRowKeys: selectedRules,
  onChange: (selectedRowKeys: string[]) => {
    selectedRules.value = selectedRowKeys
  },
  onSelectAll: (selected: boolean, selectedRows: RuleInfo[], changeRows: RuleInfo[]) => {
    if (selected) {
      selectedRules.value = rulesData.value.map(rule => rule.id)
    } else {
      selectedRules.value = []
    }
  },
  onSelect: (record: RuleInfo, selected: boolean) => {
    if (selected) {
      selectedRules.value.push(record.id)
    } else {
      selectedRules.value = selectedRules.value.filter(id => id !== record.id)
    }
  }
}

// 已选条件计算
const selectedFilters = computed(() => {
  const result = []
  if (filters.ruleSource) result.push({ key: 'ruleSource', label: `规则来源: ${getRuleSourceText(filters.ruleSource)}` })
  if (filters.applicableInsurance) result.push({ key: 'applicableInsurance', label: `适用险种: ${filters.applicableInsurance}` })
  if (filters.manageDepartment) result.push({ key: 'manageDepartment', label: `管理部门: ${filters.manageDepartment}` })
  if (filters.auditStatus) result.push({ key: 'auditStatus', label: `审核状态: ${getAuditStatusText(filters.auditStatus)}` })
  if (filters.effectiveStatus) result.push({ key: 'effectiveStatus', label: `有效状态: ${getEffectiveStatusText(filters.effectiveStatus)}` })
  if (filters.applicableChapter) result.push({ key: 'applicableChapter', label: `适用章节: ${filters.applicableChapter}` })
  if (filters.businessArea) result.push({ key: 'businessArea', label: `经营区域: ${filters.businessArea}` })
  if (filters.keyword) result.push({ key: 'keyword', label: `关键词: ${filters.keyword}` })
  return result
})

// 获取规则列表
const fetchRulesList = async () => {
  try {
    loading.value = true
    const response = await rulesApi.getRuleList({
      ...filters,
      page: pagination.current,
      size: pagination.pageSize
    })

    if (response.success) {
      rulesData.value = response.data.records
      pagination.total = response.data.total
      pagination.current = response.data.current
      pagination.pageSize = response.data.size
    } else {
      message.error(response.message || '获取规则列表失败')
    }
  } catch (error) {
    console.error('获取规则列表失败:', error)
    message.error('获取规则列表失败')
  } finally {
    loading.value = false
  }
}

// 事件处理方法
const handleBack = () => {
  router.push('/dashboard/home')
}

const handleRefresh = () => {
  fetchRulesList()
  message.success('规则列表已刷新')
}

const handleImport = () => {
  importVisible.value = true
}

const handleAdd = () => {
  router.push('/rules/new')
}

const handleAllRules = () => {
  handleResetFilters()
  message.info('显示全部规则')
}

const handleBatchDelete = async () => {
  if (selectedRules.value.length === 0) {
    message.warning('请选择要删除的规则')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRules.value.length} 条规则吗？此操作不可恢复。`,
    icon: ExclamationCircleOutlined(),
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const response = await rulesApi.batchDeleteRules({ ids: selectedRules.value })
        if (response.success) {
          message.success(`成功删除 ${response.data.successCount} 条规则`)
          if (response.data.failureCount > 0) {
            message.warning(`${response.data.failureCount} 条规则删除失败`)
          }
          selectedRules.value = []
          await fetchRulesList()
        } else {
          message.error(response.message || '批量删除失败')
        }
      } catch (error) {
        console.error('批量删除失败:', error)
        message.error('批量删除失败')
      }
    }
  })
}

const handleSingleRuleCreate = () => {
  router.push('/rules/new?type=single')
}

const handleFocus = async () => {
  if (selectedRules.value.length === 0) {
    message.warning('请选择要关注的规则')
    return
  }

  try {
    const response = await rulesApi.batchToggleFollow({
      ids: selectedRules.value,
      followed: true
    })
    if (response.success) {
      message.success(`成功关注 ${response.data.successCount} 条规则`)
      selectedRules.value = []
      await fetchRulesList()
    } else {
      message.error(response.message || '批量关注失败')
    }
  } catch (error) {
    console.error('批量关注失败:', error)
    message.error('批量关注失败')
  }
}

const handleTemplateBatchImport = async () => {
  try {
    const blob = await rulesApi.downloadTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '规则导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    message.error('下载模板失败')
  }
}

const handleSubmitOA = async () => {
  if (selectedRules.value.length === 0) {
    message.warning('请选择要提交OA审核的规则')
    return
  }
  submitOAVisible.value = true
}

const handleSearch = () => {
  pagination.current = 1
  fetchRulesList()
}

const handleResetFilters = () => {
  Object.keys(filters).forEach(key => {
    if (key === 'keyword') {
      ;(filters as any)[key] = ''
    } else if (key === 'page') {
      ;(filters as any)[key] = 1
    } else if (key === 'size') {
      ;(filters as any)[key] = 10
    } else if (key === 'sortField') {
      ;(filters as any)[key] = 'lastUpdatedAt'
    } else if (key === 'sortDirection') {
      ;(filters as any)[key] = 'DESC'
    } else {
      ;(filters as any)[key] = undefined
    }
  })
  pagination.current = 1
  fetchRulesList()
  message.info('筛选条件已重置')
}

const removeFilter = (key: string) => {
  if (key === 'keyword') {
    ;(filters as any)[key] = ''
  } else {
    ;(filters as any)[key] = undefined
  }
  handleSearch()
}

const clearAllFilters = () => {
  handleResetFilters()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchRulesList()
}

const handleEdit = (record: RuleInfo) => {
  router.push(`/rules/edit/${record.id}`)
}

const handleAudit = (record: RuleInfo) => {
  message.info(`审核规则: ${record.ruleNumber}`)
}

// 导入相关处理
const handleImportUpload: UploadProps['beforeUpload'] = (file) => {
  importFile.value = file
  return false
}

const handleImportPreview = async () => {
  if (!importFile.value) {
    message.warning('请先选择文件')
    return
  }

  try {
    const response = await rulesApi.importPreview(importFile.value)
    if (response.success) {
      importPreviewData.value = response.data
      message.success('文件预览成功')
    } else {
      message.error(response.message || '文件预览失败')
    }
  } catch (error) {
    console.error('文件预览失败:', error)
    message.error('文件预览失败')
  }
}

const handleConfirmImport = async () => {
  if (!importPreviewData.value) {
    message.warning('请先预览文件')
    return
  }

  try {
    const response = await rulesApi.confirmImport(importPreviewData.value.batchId!)
    if (response.success) {
      message.success(`成功导入 ${response.data.successCount} 条规则`)
      if (response.data.failureCount > 0) {
        message.warning(`${response.data.failureCount} 条规则导入失败`)
      }
      importVisible.value = false
      importPreviewData.value = null
      importFile.value = null
      await fetchRulesList()
    } else {
      message.error(response.message || '导入失败')
    }
  } catch (error) {
    console.error('导入失败:', error)
    message.error('导入失败')
  }
}

const handleCancelImport = () => {
  importVisible.value = false
  importPreviewData.value = null
  importFile.value = null
}

// OA提交相关处理
const handleConfirmSubmitOA = async () => {
  try {
    const response = await rulesApi.batchSubmitOA({
      ids: selectedRules.value,
      comments: submitOAComments.value || undefined
    })
    if (response.success) {
      message.success(`成功提交 ${response.data.successCount} 条规则到OA审核`)
      if (response.data.failureCount > 0) {
        message.warning(`${response.data.failureCount} 条规则提交失败`)
      }
      submitOAVisible.value = false
      submitOAComments.value = ''
      selectedRules.value = []
      await fetchRulesList()
    } else {
      message.error(response.message || '提交OA审核失败')
    }
  } catch (error) {
    console.error('提交OA审核失败:', error)
    message.error('提交OA审核失败')
  }
}

const handleCancelSubmitOA = () => {
  submitOAVisible.value = false
  submitOAComments.value = ''
}

// 批量操作 - 全选/反选
const handleSelectAll = () => {
  if (selectedRules.value.length === rulesData.value.length) {
    selectedRules.value = []
    message.info('已取消全选')
  } else {
    selectedRules.value = rulesData.value.map(rule => rule.id)
    message.info(`已选中 ${selectedRules.value.length} 条规则`)
  }
}

// 单项关注/取消关注
const handleToggleFollow = async (record: RuleInfo) => {
  try {
    const response = await rulesApi.toggleRuleFollow(record.id, !record.followed)
    if (response.success) {
      message.success(record.followed ? '已取消关注' : '已添加关注')
      await fetchRulesList()
    } else {
      message.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    message.error('操作失败')
  }
}

// 单项删除
const handleSingleDelete = async (record: RuleInfo) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除规则"${record.ruleNumber}"吗？此操作不可恢复。`,
    icon: ExclamationCircleOutlined(),
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      try {
        const response = await rulesApi.deleteRule(record.id)
        if (response.success) {
          message.success('规则删除成功')
          await fetchRulesList()
        } else {
          message.error(response.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

// 状态颜色和文本处理
const getAuditStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'PENDING_TECH_EVALUATION': 'orange',
    'PENDING_OA_SUBMISSION': 'blue',
    'SUBMITTED_TO_OA': 'cyan',
    'OA_APPROVED': 'green',
    'OA_REJECTED': 'red',
    'TECH_APPROVED': 'green',
    'TECH_REJECTED': 'red'
  }
  return colorMap[status] || 'default'
}

const getAuditStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'PENDING_TECH_EVALUATION': '待技术评估',
    'PENDING_OA_SUBMISSION': '待提交OA审核',
    'SUBMITTED_TO_OA': '已提交OA审核',
    'OA_APPROVED': 'OA审核通过',
    'OA_REJECTED': 'OA审核驳回',
    'TECH_APPROVED': '技术评估通过',
    'TECH_REJECTED': '技术评估驳回'
  }
  return textMap[status] || status
}

const getEffectiveStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'EFFECTIVE': 'green',
    'INACTIVE': 'red',
    'PENDING_DEPLOYMENT': 'orange',
    'OFFLINE': 'gray',
    'TESTING': 'blue'
  }
  return colorMap[status] || 'default'
}

const getEffectiveStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'EFFECTIVE': '有效',
    'INACTIVE': '无效',
    'PENDING_DEPLOYMENT': '待开发上线',
    'OFFLINE': '已下线',
    'TESTING': '测试中'
  }
  return textMap[status] || status
}

const getRuleSourceText = (source: string) => {
  const textMap: Record<string, string> = {
    'SYSTEM': '系统规则',
    'CUSTOM': '自定义规则',
    'IMPORTED': '导入规则',
    'TEMPLATE': '模板规则'
  }
  return textMap[source] || source
}

// 监听筛选条件变化
watch([() => filters.ruleSource, () => filters.applicableInsurance, () => filters.manageDepartment,
       () => filters.auditStatus, () => filters.effectiveStatus, () => filters.applicableChapter,
       () => filters.businessArea], () => {
  // 当筛选条件变化时，重置到第一页
  pagination.current = 1
})

// 组件挂载时获取数据
onMounted(() => {
  fetchRulesList()
})
</script>

<style scoped lang="scss">
.rules-management {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;

      .back-button {
        padding: 4px 8px;
        margin-right: 12px;
        color: #1890ff;

        &:hover {
          background: #f0f9ff;
        }
      }

      .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #262626;
        margin: 0;
      }
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: white;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    .toolbar-left, .toolbar-right {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .selection-info {
      color: #1890ff;
      font-size: 14px;
      margin-left: 8px;
    }
  }

  .filter-section {
    margin-bottom: 16px;

    .filter-form {
      .filter-item {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        label {
          width: 120px;
          text-align: right;
          margin-right: 8px;
          font-weight: 500;
        }

        .ant-select, .ant-input, .ant-time-picker {
          flex: 1;
        }
      }

      .filter-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
        align-items: center;
        height: 32px;
      }
    }

    .selected-filters {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;

      .filter-label {
        margin-right: 8px;
        font-weight: 500;
      }

      .ant-tag {
        margin-right: 8px;
        margin-bottom: 4px;
      }
    }
  }

  .table-section {
    background: white;
    border-radius: 6px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }

  // 导入对话框样式
  .import-content {
    .ant-steps {
      margin-bottom: 24px;
    }

    .import-upload {
      .ant-upload-drag {
        margin-bottom: 16px;
      }
    }

    .import-preview {
      .ant-descriptions {
        margin-bottom: 16px;
      }
    }
  }

  // 提交OA对话框样式
  .submit-oa-content {
    .ant-alert {
      margin-bottom: 16px;
    }
  }
}
</style>