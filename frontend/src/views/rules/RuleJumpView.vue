<template>
  <div class="rule-jump">
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
        <h1 class="page-title">检核跳转</h1>
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

    <!-- 操作说明 -->
    <div class="description-section">
      <a-alert
        message="请选择需要进行检核的规则"
        description="您可以通过筛选条件快速找到目标规则，选择后点击"确定"开始检核流程。"
        type="info"
        show-icon
        style="margin-bottom: 24px"
      />
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <a-card title="筛选条件">
        <div class="filter-form">
          <a-row :gutter="16">
            <a-col :span="6">
              <div class="filter-item">
                <label>规则来源：</label>
                <a-select v-model:value="filters.ruleSource" placeholder="请选择规则来源" allowClear>
                  <a-select-option value="SYSTEM">系统规则</a-select-option>
                  <a-select-option value="CUSTOM">自定义规则</a-select-option>
                  <a-select-option value="IMPORTED">导入规则</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>规则类型：</label>
                <a-select v-model:value="filters.ruleType" placeholder="请选择规则类型" allowClear>
                  <a-select-option value="SINGLE">单句规则</a-select-option>
                  <a-select-option value="DOUBLE">双句规则</a-select-option>
                  <a-select-option value="FORMAT">格式规则</a-select-option>
                  <a-select-option value="ADVANCED">高级规则</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>审核状态：</label>
                <a-select v-model:value="filters.auditStatus" placeholder="请选择审核状态" allowClear>
                  <a-select-option value="PENDING_TECH_EVALUATION">待技术评估</a-select-option>
                  <a-select-option value="TECH_APPROVED">技术评估通过</a-select-option>
                  <a-select-option value="OA_APPROVED">OA审核通过</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>有效状态：</label>
                <a-select v-model:value="filters.effectiveStatus" placeholder="请选择有效状态" allowClear>
                  <a-select-option value="EFFECTIVE">有效</a-select-option>
                  <a-select-option value="PENDING_DEPLOYMENT">待开发上线</a-select-option>
                </a-select>
              </div>
            </a-col>
          </a-row>
          <a-row :gutter="16" style="margin-top: 16px;">
            <a-col :span="6">
              <div class="filter-item">
                <label>适用险种：</label>
                <a-select v-model:value="filters.applicableInsurance" placeholder="请选择适用险种" allowClear>
                  <a-select-option value="人寿保险">人寿保险</a-select-option>
                  <a-select-option value="财产保险">财产保险</a-select-option>
                  <a-select-option value="健康保险">健康保险</a-select-option>
                  <a-select-option value="意外保险">意外保险</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>经营区域：</label>
                <a-select v-model:value="filters.businessArea" placeholder="请选择经营区域" allowClear>
                  <a-select-option value="全国">全国</a-select-option>
                  <a-select-option value="华北">华北</a-select-option>
                  <a-select-option value="华南">华南</a-select-option>
                  <a-select-option value="华东">华东</a-select-option>
                  <a-select-option value="华西">华西</a-select-option>
                </a-select>
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-item">
                <label>关键词：</label>
                <a-input
                  v-model:value="filters.keyword"
                  placeholder="请输入规则描述关键词"
                  allowClear
                />
              </div>
            </a-col>
            <a-col :span="6">
              <div class="filter-actions">
                <a-space>
                  <a-button type="primary" @click="handleSearch">
                    <template #icon>
                      <SearchOutlined />
                    </template>
                    搜索
                  </a-button>
                  <a-button @click="handleResetFilters">重置</a-button>
                </a-space>
              </div>
            </a-col>
          </a-row>
        </div>

        <!-- 已选条件标签 -->
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

    <!-- 选中规则列表 -->
    <div class="selected-rules-section">
      <a-card>
        <template #title>
          <div class="selected-title">
            <span>选中规则列表</span>
            <a-tag color="blue" style="margin-left: 8px;">
              已选择 {{ selectedRules.length }} 条规则
            </a-tag>
          </div>
        </template>
        <template #extra>
          <a-space>
            <a-button size="small" @click="handleSelectAll" :disabled="rulesData.length === 0">
              {{ selectedRules.length === rulesData.length && rulesData.length > 0 ? '取消全选' : '全选' }}
            </a-button>
            <a-button size="small" @click="handleClearSelection" :disabled="selectedRules.length === 0">
              清空选择
            </a-button>
          </a-space>
        </template>

        <!-- 选中规则展示 -->
        <div class="selected-rules-display" v-if="selectedRules.length > 0">
          <div class="selected-rules-grid">
            <div
              v-for="rule in selectedRulesData"
              :key="rule.id"
              class="selected-rule-card"
            >
              <div class="rule-card-header">
                <span class="rule-number">{{ rule.ruleNumber }}</span>
                <a-button
                  type="link"
                  size="small"
                  danger
                  @click="handleRemoveSelectedRule(rule.id)"
                >
                  <template #icon>
                    <CloseOutlined />
                  </template>
                </a-button>
              </div>
              <div class="rule-card-content">
                <div class="rule-description">{{ rule.ruleDescription }}</div>
                <div class="rule-meta">
                  <a-tag size="small" :color="getRuleTypeColor(rule.ruleType)">
                    {{ getRuleTypeText(rule.ruleType) }}
                  </a-tag>
                  <a-tag size="small" :color="getAuditStatusColor(rule.auditStatus)">
                    {{ getAuditStatusText(rule.auditStatus) }}
                  </a-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-selection">
          <a-empty description="暂未选择任何规则">
            <template #image>
              <UnorderedListOutlined style="font-size: 64px; color: #d9d9d9;" />
            </template>
          </a-empty>
        </div>
      </a-card>
    </div>

    <!-- 可选规则列表 -->
    <div class="available-rules-section">
      <a-card title="可选规则">
        <a-table
          :columns="columns"
          :data-source="rulesData"
          :pagination="pagination"
          :row-selection="rowSelection"
          :loading="loading"
          @change="handleTableChange"
          size="small"
          bordered
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'ruleType'">
              <a-tag :color="getRuleTypeColor(record.ruleType)">
                {{ getRuleTypeText(record.ruleType) }}
              </a-tag>
            </template>
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
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <a-space size="large">
        <a-button size="large" @click="handleCancel">取消</a-button>
        <a-button
          type="primary"
          size="large"
          @click="handleConfirm"
          :disabled="selectedRules.length === 0"
        >
          确定 ({{ selectedRules.length }})
        </a-button>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { TableColumnsType, TableProps } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  ReloadOutlined,
  SearchOutlined,
  CloseOutlined,
  UnorderedListOutlined
} from '@ant-design/icons-vue'
import { rulesApi, type RuleQueryParams, type RuleInfo } from '@/api/modules/rules'
import { useApiLoading } from '@/composables/useApiLoading'

// 路由
const router = useRouter()

// API加载状态
const { createLoadingRef } = useApiLoading()
const loading = createLoadingRef('rule-jump-list')

// 组件状态
const selectedRules = ref<string[]>([])
const rulesData = ref<RuleInfo[]>([])

// 筛选条件
const filters = reactive<RuleQueryParams>({
  page: 1,
  size: 10,
  ruleSource: undefined,
  ruleType: undefined,
  auditStatus: undefined,
  effectiveStatus: undefined,
  applicableInsurance: undefined,
  businessArea: undefined,
  keyword: '',
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
    width: 250,
    ellipsis: true
  },
  {
    title: '规则类型',
    dataIndex: 'ruleType',
    key: 'ruleType',
    width: 100
  },
  {
    title: '规则来源',
    dataIndex: 'ruleSource',
    key: 'ruleSource',
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
    dataIndex: 'lastUpdatedAt',
    key: 'lastUpdatedAt',
    width: 150
  }
]

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
  if (filters.ruleSource) result.push({ key: 'ruleSource', label: `规则来源: ${getRuleSourceText(filters.ruleSource)}` })
  if (filters.ruleType) result.push({ key: 'ruleType', label: `规则类型: ${getRuleTypeText(filters.ruleType)}` })
  if (filters.auditStatus) result.push({ key: 'auditStatus', label: `审核状态: ${getAuditStatusText(filters.auditStatus)}` })
  if (filters.effectiveStatus) result.push({ key: 'effectiveStatus', label: `有效状态: ${getEffectiveStatusText(filters.effectiveStatus)}` })
  if (filters.applicableInsurance) result.push({ key: 'applicableInsurance', label: `适用险种: ${filters.applicableInsurance}` })
  if (filters.businessArea) result.push({ key: 'businessArea', label: `经营区域: ${filters.businessArea}` })
  if (filters.keyword) result.push({ key: 'keyword', label: `关键词: ${filters.keyword}` })
  return result
})

// 已选规则数据
const selectedRulesData = computed(() => {
  return rulesData.value.filter(rule => selectedRules.value.includes(rule.id))
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
  router.push('/rules')
}

const handleRefresh = () => {
  fetchRulesList()
  message.success('规则列表已刷新')
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

const handleSelectAll = () => {
  if (selectedRules.value.length === rulesData.value.length) {
    selectedRules.value = []
    message.info('已取消全选')
  } else {
    selectedRules.value = rulesData.value.map(rule => rule.id)
    message.info(`已选中 ${selectedRules.value.length} 条规则`)
  }
}

const handleClearSelection = () => {
  selectedRules.value = []
  message.info('已清空选择')
}

const handleRemoveSelectedRule = (ruleId: string) => {
  selectedRules.value = selectedRules.value.filter(id => id !== ruleId)
  message.info('已移除选择')
}

const handleCancel = () => {
  router.push('/rules')
}

const handleConfirm = () => {
  if (selectedRules.value.length === 0) {
    message.warning('请至少选择一条规则')
    return
  }

  // 这里应该跳转到实际的检核页面，目前先模拟
  message.success(`已选择 ${selectedRules.value.length} 条规则，即将开始检核流程`)

  // 可以在这里添加跳转到检核系统的逻辑
  // router.push('/audit/verification', {
  //   query: { ruleIds: selectedRules.value.join(',') }
  // })

  // 暂时跳转回规则列表
  setTimeout(() => {
    router.push('/rules')
  }, 1500)
}

// 状态颜色和文本处理
const getRuleTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'SINGLE': 'blue',
    'DOUBLE': 'green',
    'FORMAT': 'orange',
    'ADVANCED': 'purple'
  }
  return colorMap[type] || 'default'
}

const getRuleTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'SINGLE': '单句规则',
    'DOUBLE': '双句规则',
    'FORMAT': '格式规则',
    'ADVANCED': '高级规则'
  }
  return textMap[type] || type
}

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
watch([() => filters.ruleSource, () => filters.ruleType, () => filters.auditStatus,
       () => filters.effectiveStatus, () => filters.applicableInsurance, () => filters.businessArea], () => {
  pagination.current = 1
})

// 组件挂载时获取数据
onMounted(() => {
  fetchRulesList()
})
</script>

<style scoped lang="scss">
.rule-jump {
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

  .description-section {
    margin-bottom: 24px;
  }

  .filter-section {
    margin-bottom: 24px;

    .filter-form {
      .filter-item {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        label {
          width: 100px;
          text-align: right;
          margin-right: 8px;
          font-weight: 500;
        }

        .ant-select, .ant-input {
          flex: 1;
        }
      }

      .filter-actions {
        display: flex;
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

  .selected-rules-section {
    margin-bottom: 24px;

    .selected-title {
      display: flex;
      align-items: center;
    }

    .selected-rules-display {
      .selected-rules-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 16px;
      }

      .selected-rule-card {
        border: 1px solid #d9d9d9;
        border-radius: 6px;
        padding: 12px;
        background: #fafafa;
        transition: all 0.3s;

        &:hover {
          border-color: #1890ff;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
        }

        .rule-card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .rule-number {
            font-weight: 600;
            color: #1890ff;
          }
        }

        .rule-card-content {
          .rule-description {
            font-size: 14px;
            color: #262626;
            margin-bottom: 8px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }

          .rule-meta {
            display: flex;
            gap: 4px;
          }
        }
      }
    }

    .empty-selection {
      text-align: center;
      padding: 40px 0;
    }
  }

  .available-rules-section {
    margin-bottom: 24px;
  }

  .action-buttons {
    text-align: center;
    padding: 24px 0;
    border-top: 1px solid #f0f0f0;
    background: white;
    position: sticky;
    bottom: 0;
    margin: 0 -24px -24px;
    z-index: 10;
  }
}
</style>