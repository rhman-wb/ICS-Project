<template>
  <a-modal
    v-model:open="visible"
    title="提交OA审核"
    width="700px"
    :maskClosable="false"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <template #footer>
      <a-button @click="handleCancel">取消</a-button>
      <a-button
        type="primary"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        提交审核
      </a-button>
    </template>

    <div class="submit-oa-modal">
      <!-- 选中规则展示 -->
      <div class="selected-rules-section">
        <h4>已选择规则 ({{ selectedRules.length }}条)</h4>
        <div class="rules-list">
          <div
            v-for="rule in selectedRules"
            :key="rule.id"
            class="rule-item"
          >
            <div class="rule-info">
              <div class="rule-number">{{ rule.ruleNumber }}</div>
              <div class="rule-description">{{ rule.ruleDescription }}</div>
              <div class="rule-meta">
                <a-tag :color="getStatusColor(rule.auditStatus)">
                  {{ rule.auditStatus }}
                </a-tag>
                <span class="rule-source">{{ rule.ruleSource }}</span>
              </div>
            </div>
            <a-button
              type="text"
              size="small"
              @click="removeRule(rule.id)"
            >
              <DeleteOutlined />
            </a-button>
          </div>
        </div>

        <div v-if="selectedRules.length === 0" class="empty-rules">
          <Empty description="请先在列表中选择要提交的规则" />
        </div>
      </div>

      <!-- 提交表单 -->
      <div class="submit-form-section">
        <a-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          layout="vertical"
        >
          <a-form-item
            label="提交人"
            name="submittedBy"
          >
            <a-input
              v-model:value="formData.submittedBy"
              placeholder="请输入提交人姓名"
            />
          </a-form-item>

          <a-form-item
            label="审核优先级"
            name="priority"
          >
            <a-select
              v-model:value="formData.priority"
              placeholder="请选择审核优先级"
            >
              <a-select-option value="high">高优先级</a-select-option>
              <a-select-option value="medium">中优先级</a-select-option>
              <a-select-option value="low">低优先级</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item
            label="预期完成时间"
            name="expectedCompletionDate"
          >
            <a-date-picker
              v-model:value="formData.expectedCompletionDate"
              style="width: 100%"
              placeholder="请选择预期完成时间"
              :disabled-date="disabledDate"
            />
          </a-form-item>

          <a-form-item
            label="提交说明"
            name="comments"
          >
            <a-textarea
              v-model:value="formData.comments"
              placeholder="请输入提交说明，包括审核要点、注意事项等"
              :rows="4"
              :maxlength="500"
              show-count
            />
          </a-form-item>

          <a-form-item
            label="抄送人员"
            name="ccUsers"
          >
            <a-select
              v-model:value="formData.ccUsers"
              mode="multiple"
              placeholder="请选择抄送人员"
              :options="userOptions"
              :filter-option="filterUsers"
            />
          </a-form-item>

          <a-form-item name="notifyEmail">
            <a-checkbox v-model:checked="formData.notifyEmail">
              发送邮件通知相关人员
            </a-checkbox>
          </a-form-item>
        </a-form>
      </div>

      <!-- 确认信息 -->
      <div class="confirmation-section">
        <a-alert
          message="提交确认"
          :description="confirmationText"
          type="info"
          show-icon
        />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue'
import { DeleteOutlined } from '@ant-design/icons-vue'
import { Empty } from 'ant-design-vue'
import dayjs, { type Dayjs } from 'dayjs'
import { rulesApi } from '@/api/modules/rules'

interface Props {
  visible: boolean
  selectedRules: Array<{
    id: string
    ruleNumber: string
    ruleDescription: string
    auditStatus: string
    ruleSource: string
  }>
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success', data: any): void
  (e: 'update:selectedRules', rules: any[]): void
}

interface SubmitOAForm {
  submittedBy: string
  priority: string
  expectedCompletionDate: Dayjs | null
  comments: string
  ccUsers: string[]
  notifyEmail: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  selectedRules: () => []
})

const emit = defineEmits<Emits>()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = ref<SubmitOAForm>({
  submittedBy: '',
  priority: 'medium',
  expectedCompletionDate: null,
  comments: '',
  ccUsers: [],
  notifyEmail: true
})

// 表单验证规则
const formRules: Record<string, Rule[]> = {
  submittedBy: [
    { required: true, message: '请输入提交人姓名', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择审核优先级', trigger: 'change' }
  ],
  comments: [
    { required: true, message: '请输入提交说明', trigger: 'blur' },
    { min: 10, message: '提交说明至少需要10个字符', trigger: 'blur' }
  ]
}

// 用户选项数据 (实际项目中应该从API获取)
const userOptions = ref([
  { label: '张三', value: 'zhangsan' },
  { label: '李四', value: 'lisi' },
  { label: '王五', value: 'wangwu' },
  { label: '赵六', value: 'zhaoliu' }
])

// 计算属性
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const canSubmit = computed(() => {
  return props.selectedRules.length > 0 && formData.value.submittedBy && formData.value.comments
})

const confirmationText = computed(() => {
  const count = props.selectedRules.length
  const priority = formData.value.priority
  const priorityText = priority === 'high' ? '高' : priority === 'medium' ? '中' : '低'

  return `将提交 ${count} 条规则进行OA审核，优先级：${priorityText}。提交后规则状态将变更为"已提交OA审核"。`
})

// 监听visible变化，重置表单
watch(visible, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

// 状态颜色映射
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    '待技术评估': 'orange',
    '待提交OA审核': 'blue',
    '已提交OA审核': 'purple',
    'OA审核通过': 'green'
  }
  return colorMap[status] || 'default'
}

// 禁用日期（不能选择过去的日期）
const disabledDate = (current: Dayjs) => {
  return current && current < dayjs().endOf('day')
}

// 用户筛选
const filterUsers = (input: string, option: any) => {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

// 移除规则
const removeRule = (ruleId: string) => {
  const updatedRules = props.selectedRules.filter(rule => rule.id !== ruleId)
  emit('update:selectedRules', updatedRules)
}

// 重置表单
const resetForm = () => {
  formData.value = {
    submittedBy: '',
    priority: 'medium',
    expectedCompletionDate: null,
    comments: '',
    ccUsers: [],
    notifyEmail: true
  }
  formRef.value?.resetFields()
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  if (props.selectedRules.length === 0) {
    message.warning('请选择要提交的规则')
    return
  }

  submitting.value = true

  try {
    const submitData = {
      ruleIds: props.selectedRules.map(rule => rule.id),
      submittedBy: formData.value.submittedBy,
      priority: formData.value.priority,
      expectedCompletionDate: formData.value.expectedCompletionDate?.format('YYYY-MM-DD'),
      comments: formData.value.comments,
      ccUsers: formData.value.ccUsers,
      notifyEmail: formData.value.notifyEmail
    }

    const response = await rulesApi.batchSubmitOA(submitData)

    message.success('OA审核提交成功')
    emit('success', response.data)
    handleCancel()

  } catch (error: any) {
    message.error(error.message || 'OA审核提交失败')
  } finally {
    submitting.value = false
  }
}

// 处理取消
const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
.submit-oa-modal {
  .selected-rules-section {
    margin-bottom: 24px;

    h4 {
      margin-bottom: 16px;
      font-weight: 600;
    }

    .rules-list {
      max-height: 300px;
      overflow-y: auto;
      border: 1px solid #f0f0f0;
      border-radius: 6px;
      padding: 12px;

      .rule-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 12px;
        margin-bottom: 8px;
        background: #fafafa;
        border-radius: 4px;

        &:last-child {
          margin-bottom: 0;
        }

        .rule-info {
          flex: 1;

          .rule-number {
            font-weight: 600;
            color: #1890ff;
            margin-bottom: 4px;
          }

          .rule-description {
            margin-bottom: 8px;
            color: #262626;
          }

          .rule-meta {
            display: flex;
            align-items: center;
            gap: 12px;

            .rule-source {
              color: #8c8c8c;
              font-size: 12px;
            }
          }
        }
      }
    }

    .empty-rules {
      text-align: center;
      padding: 40px 20px;
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      background: #fafafa;
    }
  }

  .submit-form-section {
    margin-bottom: 24px;
  }

  .confirmation-section {
    margin-bottom: 16px;
  }
}
</style>