<template>
  <div class="rule-create-format">
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

        <!-- 匹配信息 -->
        <a-col :span="24">
          <h3 class="section-title">匹配信息</h3>
        </a-col>

        <a-col :span="12">
          <a-form-item label="匹配位置" name="matchPosition" required>
            <a-input
              v-model:value="formData.matchPosition"
              placeholder="请填写匹配位置（如段落，序号，标题）"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <div style="height: 32px;"></div>
        </a-col>

        <a-col :span="24">
          <a-form-item label="匹配内容" name="matchContent" required>
            <a-textarea
              v-model:value="formData.matchContent"
              placeholder="具体文本匹配内容"
              :rows="3"
              :maxlength="1000"
              show-count
            />
          </a-form-item>
        </a-col>

        <!-- 格式要求 -->
        <a-col :span="24">
          <h3 class="section-title">格式要求</h3>
        </a-col>

        <a-col :span="24">
          <a-form-item label="段落格式要求" name="paragraphFormatRequirements" required>
            <a-textarea
              v-model:value="formData.paragraphFormatRequirements"
              placeholder="段落格式要求（支持识别缩进，居中，对齐等）"
              :rows="3"
              :maxlength="1000"
              show-count
            />
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="字体格式要求" name="fontFormatRequirements" required>
            <a-textarea
              v-model:value="formData.fontFormatRequirements"
              placeholder="字体格式要求（支持识别字号字体加粗等）"
              :rows="3"
              :maxlength="1000"
              show-count
            />
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="行文格式要求" name="textFormatRequirements" required>
            <a-textarea
              v-model:value="formData.textFormatRequirements"
              placeholder="行文格式要求，及其他要求"
              :rows="3"
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
              placeholder="请选择提示类型"
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

interface FormatRuleForm {
  ruleDescription: string
  matchPosition: string
  matchContent: string
  paragraphFormatRequirements: string
  fontFormatRequirements: string
  textFormatRequirements: string
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
const formData = reactive<FormatRuleForm>({
  ruleDescription: '',
  matchPosition: '',
  matchContent: '',
  paragraphFormatRequirements: '',
  fontFormatRequirements: '',
  textFormatRequirements: '',
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
  matchPosition: [
    { required: true, message: '请填写匹配位置', trigger: 'blur' }
  ],
  matchContent: [
    { required: true, message: '请填写匹配内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '匹配内容长度应在1-1000字符之间', trigger: 'blur' }
  ],
  paragraphFormatRequirements: [
    { required: true, message: '请填写段落格式要求', trigger: 'blur' },
    { min: 5, max: 1000, message: '段落格式要求长度应在5-1000字符之间', trigger: 'blur' }
  ],
  fontFormatRequirements: [
    { required: true, message: '请填写字体格式要求', trigger: 'blur' },
    { min: 5, max: 1000, message: '字体格式要求长度应在5-1000字符之间', trigger: 'blur' }
  ],
  textFormatRequirements: [
    { required: true, message: '请填写行文格式要求', trigger: 'blur' },
    { min: 5, max: 1000, message: '行文格式要求长度应在5-1000字符之间', trigger: 'blur' }
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
      ruleName: `格式规则-${formData.ruleDescription.substring(0, 20)}`,
      ruleDescription: formData.ruleDescription,
      ruleType: 'FORMAT',
      ruleSource: formData.ruleSource || 'CUSTOM',
      manageDepartment: formData.ruleManagementDepartment,
      applicableInsurance: formData.applicableInsurance,
      applicableRequirements: formData.applicableRequirements,
      businessArea: formData.applicableBusinessArea,
      subRuleData: {
        ruleDescription: formData.ruleDescription,
        matchPosition: formData.matchPosition,
        matchContent: formData.matchContent,
        paragraphFormatRequirements: formData.paragraphFormatRequirements,
        fontFormatRequirements: formData.fontFormatRequirements,
        textFormatRequirements: formData.textFormatRequirements,
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
      message.success('格式规则创建成功')
      // 发出成功事件
      emit('success', response.data)
    } else {
      message.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建格式规则失败:', error)
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
.rule-create-format {
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