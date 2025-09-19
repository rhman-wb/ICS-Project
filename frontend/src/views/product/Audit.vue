<template>
  <div class="product-audit-page">
    <div class="page-header">
      <h2>产品检核</h2>
      <p class="page-description">选择检核规则并启动产品智能检核</p>
    </div>

    <div class="audit-content">
      <!-- 步骤指示器 -->
      <a-steps :current="currentStep" class="audit-steps">
        <a-step title="选择规则" description="选择检核规则" />
        <a-step title="执行检核" description="启动检核任务" />
        <a-step title="查看结果" description="查看检核结果" />
      </a-steps>

      <!-- 规则选择区域 -->
      <div v-show="currentStep === 0" class="rule-selection-section">
        <a-card title="选择检核规则" :bordered="false">
          <!-- 规则列表 -->
          <a-table
            :columns="ruleColumns"
            :data-source="rules"
            :loading="loading"
            :pagination="{
              current: current,
              total: total,
              pageSize: size,
              showSizeChanger: true,
              showQuickJumper: true
            }"
            :row-selection="{
              selectedRowKeys: selectedRules,
              onChange: onSelectChange
            }"
            @change="handleTableChange"
            class="rule-table"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'effectiveStatus'">
                <a-tag :color="record.effectiveStatus === 'active' ? 'green' : 'red'">
                  {{ record.effectiveStatus === 'active' ? '有效' : '无效' }}
                </a-tag>
              </template>
            </template>
          </a-table>

          <!-- 操作按钮 -->
          <div class="action-section">
            <a-space>
              <a-button @click="handleGoBack">返回</a-button>
              <a-button type="primary" @click="startAudit" :disabled="selectedRules.length === 0">
                开始检核 (已选 {{ selectedRules.length }} 条规则)
              </a-button>
            </a-space>
          </div>
        </a-card>
      </div>

      <!-- 检核进度区域 -->
      <div v-show="currentStep === 1" class="progress-section">
        <a-card title="检核进度" :bordered="false">
          <div v-if="currentTaskId" class="progress-content">
            <p>检核任务ID: {{ currentTaskId }}</p>
            <p>正在执行检核，请稍候...</p>
            <a-button type="primary" @click="handleProgressComplete">
              模拟完成检核
            </a-button>
          </div>
        </a-card>
      </div>

      <!-- 结果展示区域 -->
      <div v-show="currentStep === 2" class="result-section">
        <a-card title="检核结果" :bordered="false">
          <div class="result-actions">
            <a-space>
              <a-button @click="goToProductManagement">返回产品列表</a-button>
              <a-button type="primary" @click="viewDetailResults">
                查看详细结果
              </a-button>
            </a-space>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useAuditRules } from '@/composables/audit/useAuditRules'
import { auditApi } from '@/api/modules/audit'

const router = useRouter()
const route = useRoute()

// 当前步骤
const currentStep = ref(0)
const currentTaskId = ref('')

// 产品ID（从路由参数获取）
const productId = route.params.id as string

// 使用检核规则Composable
const {
  state: { rules, loading, total, current, size, selectedRules },
  searchRules,
  handlePageChange,
  selectRules,
  clearSelection,
  validateSelection
} = useAuditRules({ autoLoad: true })

// 表格列定义
const ruleColumns = [
  {
    title: '规则编号',
    dataIndex: 'code',
    key: 'code',
    width: 100
  },
  {
    title: '规则名称',
    dataIndex: 'name',
    key: 'name',
    ellipsis: true
  },
  {
    title: '规则来源',
    dataIndex: 'source',
    key: 'source',
    width: 150,
    ellipsis: true
  },
  {
    title: '管理部门',
    dataIndex: 'managementDepartment',
    key: 'managementDepartment',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'effectiveStatus',
    key: 'effectiveStatus',
    width: 80
  }
]

// 选择变化处理
const onSelectChange = (selectedRowKeys: string[]) => {
  selectRules(selectedRowKeys)
}

// 表格变化处理
const handleTableChange = (pagination: any) => {
  handlePageChange(pagination.current, pagination.pageSize)
}

// 开始检核
const startAudit = async () => {
  if (!validateSelection()) {
    return
  }

  try {
    const response = await auditApi.createAuditTask([productId], selectedRules.value)
    if (response.success && response.data) {
      currentTaskId.value = response.data.taskId
      currentStep.value = 1
      message.success('检核任务创建成功，正在执行...')
    } else {
      message.error(response.message || '创建检核任务失败')
    }
  } catch (error) {
    console.error('Start audit error:', error)
    message.error('创建检核任务失败')
  }
}

// 进度完成处理
const handleProgressComplete = () => {
  currentStep.value = 2
  message.success('检核任务完成！')
}

// 查看详细结果
const viewDetailResults = () => {
  router.push({
    name: 'AuditResults',
    params: { id: productId },
    query: { taskId: currentTaskId.value }
  })
}

const handleGoBack = () => {
  router.back()
}

const goToProductManagement = () => {
  router.push('/product-management')
}

onMounted(() => {
  clearSelection()
})
</script>

<style scoped>
.product-audit-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #262626;
}

.page-description {
  margin: 0;
  color: #8c8c8c;
}

.audit-content {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.audit-steps {
  margin-bottom: 32px;
}

.rule-table {
  margin-bottom: 24px;
}

.action-section {
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.result-actions {
  text-align: center;
  padding: 32px 0;
}

.progress-section,
.result-section {
  min-height: 400px;
}

.progress-content {
  text-align: center;
  padding: 32px 0;
}
</style>