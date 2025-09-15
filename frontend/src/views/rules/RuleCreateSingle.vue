<template>
  <div class="rule-create-single">
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      layout="vertical"
      @finish="handleSubmit"
    >
      <a-row :gutter="16">
        <!-- 基础信息 -->
        <a-col :span="24">
          <h3 class="section-title">基础信息</h3>
        </a-col>

        <a-col :span="24">
          <a-form-item label="规则描述" name="ruleDescription" required>
            <a-textarea
              v-model:value="formData.ruleDescription"
              placeholder="请输入规则描述"
              :rows="3"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </a-col>

        <!-- 匹配条件 -->
        <a-col :span="24">
          <h3 class="section-title">匹配条件</h3>
        </a-col>

        <a-col :span="8">
          <a-form-item label="匹配章节" name="matchChapter" required>
            <a-select
              v-model:value="formData.matchChapter"
              placeholder="第一章"
            >
              <a-select-option value="第一章">第一章</a-select-option>
              <a-select-option value="第二章">第二章</a-select-option>
              <a-select-option value="第三章">第三章</a-select-option>
              <a-select-option value="第四章">第四章</a-select-option>
              <a-select-option value="第五章">第五章</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="匹配类型" name="matchType" required>
            <a-select
              v-model:value="formData.matchType"
              placeholder="关键词匹配"
            >
              <a-select-option value="关键词匹配">关键词匹配</a-select-option>
              <a-select-option value="正则匹配">正则匹配</a-select-option>
              <a-select-option value="模糊匹配">模糊匹配</a-select-option>
              <a-select-option value="精确匹配">精确匹配</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="触发条件" name="triggerCondition" required>
            <a-select
              v-model:value="formData.triggerCondition"
              placeholder="包含关键词"
            >
              <a-select-option value="包含关键词">包含关键词</a-select-option>
              <a-select-option value="不包含关键词">不包含关键词</a-select-option>
              <a-select-option value="等于">等于</a-select-option>
              <a-select-option value="不等于">不等于</a-select-option>
              <a-select-option value="大于">大于</a-select-option>
              <a-select-option value="小于">小于</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="匹配内容" name="matchContent" required>
            <a-textarea
              v-model:value="formData.matchContent"
              placeholder="请填写匹配内容"
              :rows="2"
              :maxlength="1000"
              show-count
            />
          </a-form-item>
        </a-col>

        <!-- 提示信息 -->
        <a-col :span="24">
          <h3 class="section-title">提示信息</h3>
        </a-col>

        <a-col :span="12">
          <a-form-item label="提示类型" name="alertType" required>
            <a-select
              v-model:value="formData.alertType"
              placeholder="错误"
            >
              <a-select-option value="错误">错误</a-select-option>
              <a-select-option value="警告">警告</a-select-option>
              <a-select-option value="提示">提示</a-select-option>
              <a-select-option value="建议">建议</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item label="适用要件" name="applicableRequirements" required>
            <a-input
              v-model:value="formData.applicableRequirements"
              placeholder="请填写适用要件"
            />
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="提示内容" name="alertContent" required>
            <a-textarea
              v-model:value="formData.alertContent"
              placeholder="请填写提示内容"
              :rows="3"
              :maxlength="500"
              show-count
            />
          </a-form-item>
        </a-col>

        <!-- 适用范围（可选） -->
        <a-col :span="24">
          <h3 class="section-title">适用范围（可选）</h3>
        </a-col>

        <a-col :span="8">
          <a-form-item label="适用险种" name="applicableInsurance">
            <a-select
              v-model:value="formData.applicableInsurance"
              placeholder="请选择险种类型"
              allowClear
            >
              <a-select-option value="人寿保险">人寿保险</a-select-option>
              <a-select-option value="财产保险">财产保险</a-select-option>
              <a-select-option value="健康保险">健康保险</a-select-option>
              <a-select-option value="意外保险">意外保险</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="适用经营区域" name="applicableBusinessArea">
            <a-select
              v-model:value="formData.applicableBusinessArea"
              placeholder="请选择经营区域"
              allowClear
            >
              <a-select-option value="全国">全国</a-select-option>
              <a-select-option value="华北">华北</a-select-option>
              <a-select-option value="华南">华南</a-select-option>
              <a-select-option value="华东">华东</a-select-option>
              <a-select-option value="华西">华西</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="具体地方区域" name="specificLocalArea">
            <a-input
              v-model:value="formData.specificLocalArea"
              placeholder="请选择具体地方区域"
            />
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="产品性质" name="productNature">
            <a-input
              v-model:value="formData.productNature"
              placeholder="请填写产品性质"
            />
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="规则管理部门" name="ruleManagementDepartment">
            <a-input
              v-model:value="formData.ruleManagementDepartment"
              placeholder="请填写规则管理部门"
            />
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="规则来源" name="ruleSource">
            <a-input
              v-model:value="formData.ruleSource"
              placeholder="请填写规则来源"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <a-space>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="$emit('cancel')">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="submitting">
            提交
          </a-button>
        </a-space>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { rulesApi } from '@/api/modules/rules'

interface SingleRuleForm {
  ruleDescription: string
  matchChapter: string
  matchType: string
  matchContent: string
  triggerCondition: string
  alertType: string
  alertContent: string
  applicableRequirements: string
  applicableInsurance?: string
  applicableBusinessArea?: string
  specificLocalArea?: string
  productNature?: string
  ruleManagementDepartment?: string
  ruleSource?: string
}

// 定义事件
defineEmits<{
  success: [data: any]
  cancel: []
}>()

// 表单引用
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const formData = reactive<SingleRuleForm>({
  ruleDescription: '',
  matchChapter: '',
  matchType: '',
  matchContent: '',
  triggerCondition: '',
  alertType: '',
  alertContent: '',
  applicableRequirements: '',
  applicableInsurance: undefined,
  applicableBusinessArea: undefined,
  specificLocalArea: '',
  productNature: '',
  ruleManagementDepartment: '',
  ruleSource: ''
})

// 表单验证规则
const rules: Record<string, Rule[]> = {
  ruleDescription: [
    { required: true, message: '请输入规则描述', trigger: 'blur' },
    { min: 10, max: 500, message: '规则描述长度应在10-500字符之间', trigger: 'blur' }
  ],
  matchChapter: [
    { required: true, message: '请选择匹配章节', trigger: 'change' }
  ],
  matchType: [
    { required: true, message: '请选择匹配类型', trigger: 'change' }
  ],
  matchContent: [
    { required: true, message: '请填写匹配内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '匹配内容长度应在1-1000字符之间', trigger: 'blur' }
  ],
  triggerCondition: [
    { required: true, message: '请选择触发条件', trigger: 'change' }
  ],
  alertType: [
    { required: true, message: '请选择提示类型', trigger: 'change' }
  ],
  alertContent: [
    { required: true, message: '请填写提示内容', trigger: 'blur' },
    { min: 5, max: 500, message: '提示内容长度应在5-500字符之间', trigger: 'blur' }
  ],
  applicableRequirements: [
    { required: true, message: '请填写适用要件', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  try {
    submitting.value = true

    // 构建请求数据
    const requestData = {
      ruleName: `单句规则-${formData.ruleDescription.substring(0, 20)}`,
      ruleDescription: formData.ruleDescription,
      ruleType: 'SINGLE',
      ruleSource: formData.ruleSource || 'CUSTOM',
      manageDepartment: formData.ruleManagementDepartment,
      applicableInsurance: formData.applicableInsurance,
      applicableRequirements: formData.applicableRequirements,
      businessArea: formData.applicableBusinessArea,
      subRuleData: {
        ruleDescription: formData.ruleDescription,
        matchChapter: formData.matchChapter,
        matchType: formData.matchType,
        matchContent: formData.matchContent,
        triggerCondition: formData.triggerCondition,
        alertType: formData.alertType,
        alertContent: formData.alertContent,
        applicableRequirements: formData.applicableRequirements,
        applicableInsurance: formData.applicableInsurance,
        applicableBusinessArea: formData.applicableBusinessArea,
        specificLocalArea: formData.specificLocalArea,
        productNature: formData.productNature,
        ruleManagementDepartment: formData.ruleManagementDepartment,
        ruleSource: formData.ruleSource
      }
    }

    const response = await rulesApi.createRule(requestData)
    if (response.success) {
      message.success('单句规则创建成功')
      // 发出成功事件
      emit('success', response.data)
    } else {
      message.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建单句规则失败:', error)
    message.error('创建失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
}

// 获取emit函数
const emit = defineEmits<{
  success: [data: any]
  cancel: []
}>()
</script>

<style scoped lang="scss">
.rule-create-single {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }

  .form-actions {
    text-align: center;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>