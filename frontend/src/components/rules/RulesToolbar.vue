<template>
  <div class="toolbar">
    <div class="toolbar-left">
      <a-button
        v-permission="'RULE_IMPORT'"
        type="primary"
        @click="$emit('import')"
      >
        <template #icon>
          <ImportOutlined />
        </template>
        导入
      </a-button>
      <a-button
        v-permission="'RULE_CREATE'"
        type="primary"
        @click="$emit('add')"
      >
        <template #icon>
          <PlusOutlined />
        </template>
        添加
      </a-button>
      <a-button @click="$emit('allRules')">全部规则</a-button>
      <a-divider type="vertical" />
      <a-button @click="$emit('selectAll')">
        <template #icon>
          <CheckOutlined />
        </template>
        {{ selectedCount === totalCount && totalCount > 0 ? '取消全选' : '全选' }}
      </a-button>
      <span class="selection-info" v-if="selectedCount > 0">
        已选择 {{ selectedCount }} 项
      </span>
    </div>
    <div class="toolbar-right">
      <a-button
        v-permission="'RULE_DELETE'"
        danger
        :disabled="selectedCount === 0"
        @click="$emit('batchDelete')"
      >
        <template #icon>
          <DeleteOutlined />
        </template>
        删除
      </a-button>
      <a-button
        v-permission="'RULE_CREATE'"
        @click="$emit('singleRuleCreate')"
      >
        单条规则创建
      </a-button>
      <a-button
        :disabled="selectedCount === 0"
        @click="$emit('focus')"
      >
        <template #icon>
          <StarOutlined />
        </template>
        关注
      </a-button>
      <a-button
        v-permission="'RULE_IMPORT'"
        @click="$emit('templateBatchImport')"
      >
        <template #icon>
          <DownloadOutlined />
        </template>
        模板批量导入
      </a-button>
      <a-button
        v-permission="'RULE_OA_SUBMIT'"
        type="primary"
        :disabled="selectedCount === 0"
        @click="$emit('submitOA')"
      >
        提交OA审核
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ImportOutlined,
  PlusOutlined,
  CheckOutlined,
  DeleteOutlined,
  StarOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'

defineProps<{
  selectedCount: number
  totalCount: number
}>()

defineEmits<{
  import: []
  add: []
  allRules: []
  selectAll: []
  batchDelete: []
  singleRuleCreate: []
  focus: []
  templateBatchImport: []
  submitOA: []
}>()
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  gap: 16px;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selection-info {
  color: #1890ff;
  font-size: 14px;
  margin-left: 8px;
}
</style>