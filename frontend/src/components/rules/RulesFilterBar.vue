<template>
  <div class="filter-section">
    <a-card>
      <div class="filter-form">
        <a-row :gutter="16">
          <a-col :span="6">
            <div class="filter-item">
              <label>规则来源：</label>
              <a-select v-model:value="filters.ruleSource" placeholder="请选择规则来源" allowClear>
                <a-select-option value="SYSTEM">系统规则</a-select-option>
                <a-select-option value="CUSTOM">自定义规则</a-select-option>
                <a-select-option value="IMPORTED">导入规则</a-select-option>
                <a-select-option value="TEMPLATE">模板规则</a-select-option>
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
                <a-select-option value="PENDING_TECH_EVALUATION">待技术评估</a-select-option>
                <a-select-option value="PENDING_OA_SUBMISSION">待提交OA审核</a-select-option>
                <a-select-option value="SUBMITTED_TO_OA">已提交OA审核</a-select-option>
                <a-select-option value="OA_APPROVED">OA审核通过</a-select-option>
                <a-select-option value="OA_REJECTED">OA审核驳回</a-select-option>
                <a-select-option value="TECH_APPROVED">技术评估通过</a-select-option>
                <a-select-option value="TECH_REJECTED">技术评估驳回</a-select-option>
              </a-select>
            </div>
          </a-col>
        </a-row>
        <a-row :gutter="16" style="margin-top: 16px;">
          <a-col :span="6">
            <div class="filter-item">
              <label>有效状态：</label>
              <a-select v-model:value="filters.effectiveStatus" placeholder="请选择有效状态" allowClear>
                <a-select-option value="EFFECTIVE">有效</a-select-option>
                <a-select-option value="INACTIVE">无效</a-select-option>
                <a-select-option value="PENDING_DEPLOYMENT">待开发上线</a-select-option>
                <a-select-option value="OFFLINE">已下线</a-select-option>
                <a-select-option value="TESTING">测试中</a-select-option>
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
              <a-button type="primary" @click="$emit('search')">
                <template #icon>
                  <SearchOutlined />
                </template>
                搜索
              </a-button>
              <a-button @click="$emit('reset')">重置</a-button>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { SearchOutlined } from '@ant-design/icons-vue'

export interface FilterValues {
  ruleSource?: string
  applicableInsurance?: string
  manageDepartment?: string
  auditStatus?: string
  effectiveStatus?: string
  applicableChapter?: string
  businessArea?: string
  keyword?: string
  startTime?: any
  endTime?: any
}

defineProps<{
  filters: FilterValues
}>()

defineEmits<{
  search: []
  reset: []
}>()
</script>

<style scoped>
.filter-section {
  margin-bottom: 16px;
}

.filter-form {
  width: 100%;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-item label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  height: 100%;
  padding-top: 24px;
}
</style>