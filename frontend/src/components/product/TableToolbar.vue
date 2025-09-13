<template>
  <div class="table-toolbar">
    <div class="toolbar-left">
      <a-space>
        <span class="result-count">
          共找到 {{ total }} 个产品
        </span>
        <a-divider type="vertical" />
        <a-button
          v-if="hasSelected"
          type="link"
          @click="handleClearSelection"
        >
          取消选择
        </a-button>
      </a-space>
    </div>
    <div class="toolbar-right">
      <a-space>
        <a-tooltip title="刷新数据">
          <a-button
            shape="circle"
            :icon="h(ReloadOutlined)"
            :loading="loading"
            @click="handleRefresh"
          />
        </a-tooltip>
        <a-tooltip title="列设置">
          <a-button
            shape="circle"
            :icon="h(SettingOutlined)"
            @click="handleColumnSettings"
          />
        </a-tooltip>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { ReloadOutlined, SettingOutlined } from '@ant-design/icons-vue'

interface Props {
  total: number
  selectedCount: number
  loading?: boolean
}

interface Emits {
  (e: 'refresh'): void
  (e: 'clearSelection'): void
  (e: 'columnSettings'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

const hasSelected = computed(() => props.selectedCount > 0)

const handleRefresh = () => {
  emit('refresh')
}

const handleClearSelection = () => {
  emit('clearSelection')
}

const handleColumnSettings = () => {
  emit('columnSettings')
}
</script>

<style scoped>
.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.result-count {
  color: #666;
  font-size: 14px;
}

.ant-btn-circle {
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .table-toolbar {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }
}
</style>