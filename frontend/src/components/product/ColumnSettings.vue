<template>
  <a-drawer
    :open="visible"
    title="列设置"
    placement="right"
    width="300"
    @close="handleClose"
  >
    <div class="column-settings">
      <div class="settings-header">
        <a-space>
          <a-button size="small" @click="handleSelectAll">全选</a-button>
          <a-button size="small" @click="handleDeselectAll">取消全选</a-button>
          <a-button size="small" @click="handleReset">重置</a-button>
        </a-space>
      </div>
      
      <a-divider />
      
      <a-checkbox-group
        v-model:value="selectedColumns"
        class="column-list"
        @change="handleChange"
      >
        <div class="column-group">
          <div class="group-title">基础信息</div>
          <a-checkbox
            v-for="column in basicColumns"
            :key="column.key"
            :value="column.key"
            :disabled="column.required"
            class="column-item"
          >
            {{ column.title }}
            <a-tag v-if="column.required" size="small" color="red">必选</a-tag>
          </a-checkbox>
        </div>
        
        <div class="column-group">
          <div class="group-title">产品属性</div>
          <a-checkbox
            v-for="column in attributeColumns"
            :key="column.key"
            :value="column.key"
            class="column-item"
          >
            {{ column.title }}
          </a-checkbox>
        </div>
        
        <div class="column-group">
          <div class="group-title">状态信息</div>
          <a-checkbox
            v-for="column in statusColumns"
            :key="column.key"
            :value="column.key"
            class="column-item"
          >
            {{ column.title }}
          </a-checkbox>
        </div>
        
        <div class="column-group">
          <div class="group-title">其他</div>
          <a-checkbox
            v-for="column in otherColumns"
            :key="column.key"
            :value="column.key"
            :disabled="column.required"
            class="column-item"
          >
            {{ column.title }}
            <a-tag v-if="column.required" size="small" color="red">必选</a-tag>
          </a-checkbox>
        </div>
      </a-checkbox-group>
    </div>
    
    <template #footer>
      <a-space>
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" @click="handleConfirm">确定</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface ColumnConfig {
  key: string
  title: string
  required?: boolean
  group: 'basic' | 'attribute' | 'status' | 'other'
}

interface Props {
  visible: boolean
  selectedKeys: string[]
  columns: ColumnConfig[]
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'update:selectedKeys', keys: string[]): void
  (e: 'confirm', keys: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const selectedColumns = ref<string[]>([...props.selectedKeys])
const defaultColumns = ['productName', 'reportType', 'status', 'updatedAt', 'operation']

// 根据分组过滤列
const basicColumns = computed(() => 
  props.columns.filter(col => col.group === 'basic')
)

const attributeColumns = computed(() => 
  props.columns.filter(col => col.group === 'attribute')
)

const statusColumns = computed(() => 
  props.columns.filter(col => col.group === 'status')
)

const otherColumns = computed(() => 
  props.columns.filter(col => col.group === 'other')
)

const allColumnKeys = computed(() => 
  props.columns.map(col => col.key)
)

// 监听props变化
watch(
  () => props.selectedKeys,
  (newKeys) => {
    selectedColumns.value = [...newKeys]
  }
)

watch(
  () => props.visible,
  (newVisible) => {
    if (newVisible) {
      selectedColumns.value = [...props.selectedKeys]
    }
  }
)

// 事件处理
const handleClose = () => {
  emit('update:visible', false)
}

const handleChange = (checkedValues: string[]) => {
  selectedColumns.value = checkedValues
}

const handleSelectAll = () => {
  selectedColumns.value = [...allColumnKeys.value]
}

const handleDeselectAll = () => {
  const requiredColumns = props.columns
    .filter(col => col.required)
    .map(col => col.key)
  selectedColumns.value = requiredColumns
}

const handleReset = () => {
  selectedColumns.value = [...defaultColumns]
}

const handleConfirm = () => {
  emit('update:selectedKeys', selectedColumns.value)
  emit('confirm', selectedColumns.value)
  handleClose()
}
</script>

<style scoped>
.column-settings {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.settings-header {
  padding-bottom: 16px;
}

.column-list {
  flex: 1;
  overflow-y: auto;
}

.column-group {
  margin-bottom: 24px;
}

.group-title {
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}

.column-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.column-item:last-child {
  border-bottom: none;
}

:deep(.ant-checkbox-group) {
  width: 100%;
}

:deep(.ant-checkbox-wrapper) {
  display: block;
  width: 100%;
  margin-left: 0;
  margin-right: 0;
}

:deep(.ant-checkbox-wrapper-disabled) {
  opacity: 0.6;
}

:deep(.ant-tag) {
  margin-left: auto;
}
</style>