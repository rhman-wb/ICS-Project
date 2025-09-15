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
        <a-button @click="handleFocus">
          <template #icon>
            <StarOutlined />
          </template>
          关注
        </a-button>
        <a-button @click="handleTemplateBatchImport">模板批量导入</a-button>
        <a-button type="primary" @click="handleSubmitOA">提交OA审核</a-button>
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
          <template v-if="column.key === 'operation'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleAudit(record)">审核</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ReloadOutlined,
  ImportOutlined,
  PlusOutlined,
  DeleteOutlined,
  StarOutlined,
  SearchOutlined
} from '@ant-design/icons-vue'

// 路由
const router = useRouter()

// 组件状态
const loading = ref(false)
const selectedRules = ref<string[]>([])

// 筛选条件
const filters = reactive({
  ruleSource: undefined,
  applicableInsurance: undefined,
  manageDepartment: undefined,
  auditStatus: undefined,
  effectiveStatus: undefined,
  applicableChapter: undefined,
  businessArea: undefined,
  keyword: '',
  startTime: undefined,
  endTime: undefined
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
    title: '最后更新时间',
    dataIndex: 'lastUpdateTime',
    key: 'lastUpdateTime',
    width: 150
  },
  {
    title: '操作',
    key: 'operation',
    width: 120,
    fixed: 'right'
  }
]

// 模拟数据
const rulesData = ref([
  {
    key: '1',
    ruleNumber: 'R001',
    ruleDescription: '保险金额不能超过投保人年收入的10倍',
    ruleSource: '系统规则',
    manageDepartment: '技术部门',
    applicableInsurance: '人寿保险',
    applicableRequirements: '投保条件',
    applicableChapter: '第一章',
    auditStatus: 'pending-tech',
    effectiveStatus: 'effective',
    lastUpdateTime: '2024-09-16 09:12:21'
  }
])

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 行选择配置
const rowSelection: TableProps['rowSelection'] = {
  selectedRowKeys: selectedRules,
  onChange: (selectedRowKeys: string[]) => {
    selectedRules.value = selectedRowKeys
  }
}

// 已选条件计算
const selectedFilters = computed(() => {
  const result = []
  if (filters.ruleSource) result.push({ key: 'ruleSource', label: `规则来源: ${filters.ruleSource}` })
  if (filters.applicableInsurance) result.push({ key: 'applicableInsurance', label: `适用险种: ${filters.applicableInsurance}` })
  if (filters.manageDepartment) result.push({ key: 'manageDepartment', label: `管理部门: ${filters.manageDepartment}` })
  if (filters.auditStatus) result.push({ key: 'auditStatus', label: `审核状态: ${filters.auditStatus}` })
  if (filters.effectiveStatus) result.push({ key: 'effectiveStatus', label: `有效状态: ${filters.effectiveStatus}` })
  if (filters.applicableChapter) result.push({ key: 'applicableChapter', label: `适用章节: ${filters.applicableChapter}` })
  if (filters.businessArea) result.push({ key: 'businessArea', label: `经营区域: ${filters.businessArea}` })
  if (filters.keyword) result.push({ key: 'keyword', label: `关键词: ${filters.keyword}` })
  return result
})

// 事件处理方法
const handleBack = () => {
  router.push('/dashboard/home')
}

const handleRefresh = () => {
  message.success('规则列表已刷新')
}

const handleImport = () => {
  message.info('导入功能开发中')
}

const handleAdd = () => {
  router.push('/rules/new')
}

const handleAllRules = () => {
  handleResetFilters()
  message.info('显示全部规则')
}

const handleBatchDelete = () => {
  if (selectedRules.value.length === 0) {
    message.warning('请选择要删除的规则')
    return
  }
  message.info(`批量删除 ${selectedRules.value.length} 条规则`)
}

const handleSingleRuleCreate = () => {
  router.push('/rules/new/single')
}

const handleFocus = () => {
  message.info('关注功能开发中')
}

const handleTemplateBatchImport = () => {
  message.info('模板批量导入功能开发中')
}

const handleSubmitOA = () => {
  message.info('提交OA审核功能开发中')
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    message.success('搜索完成')
  }, 1000)
}

const handleResetFilters = () => {
  Object.keys(filters).forEach(key => {
    ;(filters as any)[key] = key === 'keyword' ? '' : undefined
  })
  message.info('筛选条件已重置')
}

const removeFilter = (key: string) => {
  ;(filters as any)[key] = key === 'keyword' ? '' : undefined
}

const clearAllFilters = () => {
  handleResetFilters()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
}

const handleEdit = (record: any) => {
  router.push(`/rules/edit/${record.key}`)
}

const handleAudit = (record: any) => {
  message.info(`审核规则: ${record.ruleNumber}`)
}

// 状态颜色和文本处理
const getAuditStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'pending-tech': 'orange',
    'pending-oa': 'blue',
    'submitted-oa': 'cyan',
    'oa-approved': 'green'
  }
  return colorMap[status] || 'default'
}

const getAuditStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'pending-tech': '待技术评估',
    'pending-oa': '待提交OA审核',
    'submitted-oa': '已提交OA审核',
    'oa-approved': 'OA审核通过'
  }
  return textMap[status] || status
}

const getEffectiveStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'effective': 'green',
    'inactive': 'red',
    'pending-deploy': 'orange'
  }
  return colorMap[status] || 'default'
}

const getEffectiveStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'effective': '有效',
    'inactive': '无效',
    'pending-deploy': '待开发上线'
  }
  return textMap[status] || status
}
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
}
</style>