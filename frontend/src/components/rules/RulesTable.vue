<template>
  <div class="rules-table-container" data-testid="rules-table">
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="paginationConfig"
      :row-selection="rowSelection"
      :scroll="{ x: 1500 }"
      row-key="id"
      size="middle"
      @change="handleTableChange"
    >
      <!-- 空状态 -->
      <template #emptyText>
        <div class="empty-state">
          <a-empty
            description="暂无规则数据"
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
          />
        </div>
      </template>

      <!-- 自定义渲染选择列 -->
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.key === 'selection'">
          <a-checkbox
            :checked="selectedRowKeys.includes(record.id)"
            @change="(e) => handleSelectChange(e.target.checked, record)"
          />
        </template>

        <template v-else-if="column.key === 'auditStatus'">
          <a-tag :color="getAuditStatusColor(record.auditStatus)">
            {{ getAuditStatusText(record.auditStatus) }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'effectiveStatus'">
          <a-tag :color="getEffectiveStatusColor(record.effectiveStatus)">
            {{ getEffectiveStatusText(record.effectiveStatus) }}
          </a-tag>
        </template>

        <template v-else-if="column.key === 'actions'">
          <div class="table-actions">
            <!-- 关注按钮 -->
            <a-button
              type="text"
              size="small"
              :icon="record.isFollowed ? h(StarFilled) : h(StarOutlined)"
              @click="handleToggleFollow(record)"
            />

            <!-- 更多操作 -->
            <a-dropdown :trigger="['click']">
              <a-button type="text" size="small">
                更多
                <DownOutlined />
              </a-button>
              <template #overlay>
                <a-menu @click="({ key }) => handleMenuClick(key, record)">
                  <a-menu-item key="edit">
                    <EditOutlined />
                    编辑
                  </a-menu-item>
                  <a-menu-item key="copy">
                    <CopyOutlined />
                    复制
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="delete" class="danger-item">
                    <DeleteOutlined />
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { h, computed, type PropType } from 'vue'
import {
  Table as ATable,
  Tag as ATag,
  Button as AButton,
  Checkbox as ACheckbox,
  Dropdown as ADropdown,
  Menu as AMenu,
  MenuItem as AMenuItem,
  MenuDivider as AMenuDivider,
  Empty
} from 'ant-design-vue'
import {
  StarFilled,
  StarOutlined,
  DownOutlined,
  EditOutlined,
  CopyOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { useRulesStatusMap } from '@/composables/rules/useRulesStatusMap'
import type { Rule } from '@/types/rules'
import type { TableColumnType } from 'ant-design-vue'

const {
  getAuditStatusText,
  getAuditStatusColor,
  getEffectiveStatusText,
  getEffectiveStatusColor
} = useRulesStatusMap()

interface Props {
  dataSource: Rule[]
  loading: boolean
  selectedRowKeys: string[]
  pagination: {
    current: number
    pageSize: number
    total: number
    showSizeChanger: boolean
    showQuickJumper: boolean
    pageSizeOptions: string[]
  }
}

interface Emits {
  (e: 'selectChange', checked: boolean, record: Rule): void
  (e: 'selectAll', checked: boolean): void
  (e: 'tableChange', pagination: any, filters: any, sorter: any): void
  (e: 'toggleFollow', record: Rule): void
  (e: 'edit', record: Rule): void
  (e: 'copy', record: Rule): void
  (e: 'delete', record: Rule): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表格列定义
const columns: TableColumnType<Rule>[] = [
  {
    title: '',
    key: 'selection',
    width: 50,
    fixed: 'left'
  },
  {
    title: '规则编号',
    dataIndex: 'ruleNumber',
    key: 'ruleNumber',
    width: 120,
    fixed: 'left',
    ellipsis: true
  },
  {
    title: '规则描述',
    dataIndex: 'ruleDescription',
    key: 'ruleDescription',
    width: 200,
    ellipsis: true
  },
  {
    title: '规则来源',
    dataIndex: 'ruleSource',
    key: 'ruleSource',
    width: 120,
    ellipsis: true
  },
  {
    title: '规则管理部门',
    dataIndex: 'ruleDept',
    key: 'ruleDept',
    width: 140,
    ellipsis: true
  },
  {
    title: '适用险种',
    dataIndex: 'insuranceType',
    key: 'insuranceType',
    width: 120,
    ellipsis: true
  },
  {
    title: '适用要件',
    dataIndex: 'applicableRequirement',
    key: 'applicableRequirement',
    width: 120,
    ellipsis: true
  },
  {
    title: '适用章节',
    dataIndex: 'chapter',
    key: 'chapter',
    width: 120,
    ellipsis: true
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
    width: 180,
    sorter: true
  },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right'
  }
]

// 分页配置
const paginationConfig = computed(() => ({
  ...props.pagination,
  showTotal: (total: number, range: [number, number]) =>
    `第 ${range[0]}-${range[1]} 条 / 共 ${total} 条`
}))

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: props.selectedRowKeys,
  onSelect: (record: Rule, selected: boolean) => {
    emit('selectChange', selected, record)
  },
  onSelectAll: (selected: boolean, selectedRows: Rule[], changeRows: Rule[]) => {
    emit('selectAll', selected)
  }
}))

// 处理表格变化
const handleTableChange = (pagination: any, filters: any, sorter: any) => {
  emit('tableChange', pagination, filters, sorter)
}

// 处理选择变化
const handleSelectChange = (checked: boolean, record: Rule) => {
  emit('selectChange', checked, record)
}

// 处理关注切换
const handleToggleFollow = (record: Rule) => {
  emit('toggleFollow', record)
}

// 处理菜单点击
const handleMenuClick = (key: string, record: Rule) => {
  switch (key) {
    case 'edit':
      emit('edit', record)
      break
    case 'copy':
      emit('copy', record)
      break
    case 'delete':
      emit('delete', record)
      break
  }
}
</script>

<style scoped>
.rules-table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.table-actions {
  display: flex;
  gap: 4px;
  justify-content: center;
  align-items: center;
}

.empty-state {
  padding: 32px;
}

:deep(.danger-item) {
  color: #ff4d4f !important;
}

:deep(.danger-item:hover) {
  background-color: #fff2f0 !important;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-weight: 600;
}
</style>