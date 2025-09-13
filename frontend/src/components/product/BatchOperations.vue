<template>
  <div class="batch-operations" v-if="visible">
    <a-space>
      <span class="selected-info">
        已选择 {{ selectedCount }} 项
      </span>
      <a-button type="link" @click="handleBatchAudit">
        <template #icon>
          <AuditOutlined />
        </template>
        批量检核
      </a-button>
      <a-button type="link" @click="handleBatchExport">
        <template #icon>
          <ExportOutlined />
        </template>
        批量导出
      </a-button>
      <a-button type="link" danger @click="handleBatchDelete">
        <template #icon>
          <DeleteOutlined />
        </template>
        批量删除
      </a-button>
    </a-space>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { AuditOutlined, ExportOutlined, DeleteOutlined } from '@ant-design/icons-vue'

interface Props {
  selectedKeys: string[]
}

interface Emits {
  (e: 'batchAudit', keys: string[]): void
  (e: 'batchExport', keys: string[]): void
  (e: 'batchDelete', keys: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed(() => props.selectedKeys.length > 0)
const selectedCount = computed(() => props.selectedKeys.length)

const handleBatchAudit = () => {
  emit('batchAudit', [...props.selectedKeys])
}

const handleBatchExport = () => {
  emit('batchExport', [...props.selectedKeys])
}

const handleBatchDelete = () => {
  emit('batchDelete', [...props.selectedKeys])
}
</script>

<style scoped>
.batch-operations {
  padding: 12px 16px;
  background-color: #f6f6f6;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  margin-bottom: 16px;
}

.selected-info {
  color: #1890ff;
  font-weight: 500;
}

.ant-btn-link {
  padding: 0 8px;
}
</style>