<template>
  <div class="selected-filters" v-if="selectedFilters.length > 0">
    <span class="filter-label">已选条件：</span>
    <a-tag
      v-for="filter in selectedFilters"
      :key="filter.key"
      closable
      @close="$emit('removeFilter', filter.key)"
    >
      {{ filter.label }}
    </a-tag>
    <a-button type="link" size="small" @click="$emit('clearAll')">清除全部</a-button>
  </div>
</template>

<script setup lang="ts">
export interface SelectedFilter {
  key: string
  label: string
}

defineProps<{
  selectedFilters: SelectedFilter[]
}>()

defineEmits<{
  removeFilter: [key: string]
  clearAll: []
}>()
</script>

<style scoped>
.selected-filters {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}
</style>