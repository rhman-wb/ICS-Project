<template>
  <div class="rule-create-advanced">
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
              :maxlength="300"
              show-count
            />
          </a-form-item>
        </a-col>

        <!-- 高级配置 -->
        <a-col :span="24">
          <h3 class="section-title">高级配置</h3>
        </a-col>

        <a-col :span="8">
          <a-form-item label="规则权重" name="ruleWeight">
            <a-input-number
              v-model:value="formData.ruleWeight"
              placeholder="100"
              :min="1"
              :max="1000"
              style="width: 100%"
            />
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <a-form-item label="是否启用LLM" name="llmEnabled">
            <a-switch
              v-model:checked="formData.llmEnabled"
              :checked-children="'启用'"
              :un-checked-children="'禁用'"
            />
          </a-form-item>
        </a-col>

        <a-col :span="8">
          <div style="height: 32px;"></div>
        </a-col>

        <a-col :span="24">
          <a-form-item label="执行脚本或表达式" name="executionScript">
            <a-textarea
              v-model:value="formData.executionScript"
              placeholder="请输入执行脚本或表达式，如：return content.includes('特定关键词') && content.length > 100"
              :rows="5"
              :maxlength="5000"
              show-count
            />
            <div class="script-help">
              <a-alert
                message="脚本说明"
                description="支持JavaScript表达式，可使用content、metadata、context等变量进行复杂逻辑判断"
                type="info"
                show-icon
                style="margin-top: 8px"
              />
            </div>
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="高级规则配置JSON" name="advancedRuleConfig">
            <a-textarea
              v-model:value="formData.advancedRuleConfig"
              placeholder='{"complexity": "high", "llm_enabled": false, "timeout": 5000, "retry_count": 3}'
              :rows="4"
              :maxlength="5000"
              show-count
            />
            <div class="config-help">
              <a-typography-text type="secondary" style="font-size: 12px;">
                JSON格式配置，支持complexity、llm_enabled、timeout、retry_count等参数
              </a-typography-text>
            </div>
          </a-form-item>
        </a-col>

        <a-col :span="24" v-if="formData.llmEnabled">
          <a-form-item label="LLM模型配置" name="llmModelConfig">
            <a-textarea
              v-model:value="formData.llmModelConfig"
              placeholder='{"model": "gpt-4", "temperature": 0.7, "max_tokens": 2000, "prompt_template": "请分析以下内容..."}'
              :rows="4"
              :maxlength="3000"
              show-count
            />
            <div class="llm-help">
              <a-typography-text type="secondary" style="font-size: 12px;">
                LLM模型配置JSON，包含model、temperature、max_tokens、prompt_template等参数
              </a-typography-text>
            </div>
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

interface AdvancedRuleForm {
  ruleDescription: string
  alertType: string
  alertContent: string
  applicableRequirements: string
  applicableInsurance?: string
  applicableBusinessArea?: string
  specificLocalArea?: string
  productNature?: string
  ruleManagementDepartment?: string
  ruleSource?: string
  advancedRuleConfig?: string
  executionScript?: string
  ruleWeight?: number
  llmEnabled: boolean
  llmModelConfig?: string
}

// 表单引用
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const formData = reactive<AdvancedRuleForm>({
  ruleDescription: '',
  alertType: '',
  alertContent: '',
  applicableRequirements: '',
  applicableInsurance: undefined,
  applicableBusinessArea: undefined,
  specificLocalArea: '',
  productNature: '',
  ruleManagementDepartment: '',
  ruleSource: '',
  advancedRuleConfig: '{"complexity": "high", "llm_enabled": false}',
  executionScript: '',
  ruleWeight: 100,
  llmEnabled: false,
  llmModelConfig: ''
})

// 表单验证规则
const rules: Record<string, Rule[]> = {
  ruleDescription: [
    { required: true, message: '请输入规则描述', trigger: 'blur' },
    { max: 300, message: '规则描述长度不能超过300字符', trigger: 'blur' }
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
  ],
  ruleWeight: [
    { type: 'number', min: 1, max: 1000, message: '规则权重应在1-1000之间', trigger: 'blur' }
  ],
  advancedRuleConfig: [
    {
      validator: (rule: any, value: string) => {
        if (value) {
          try {
            JSON.parse(value)
            return Promise.resolve()
          } catch (error) {
            return Promise.reject('请输入有效的JSON格式')
          }
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ],
  llmModelConfig: [
    {
      validator: (rule: any, value: string) => {
        if (formData.llmEnabled && value) {
          try {
            JSON.parse(value)
            return Promise.resolve()
          } catch (error) {
            return Promise.reject('请输入有效的JSON格式')
          }
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

// 提交表单
const handleSubmit = async () => {
  try {
    submitting.value = true

    // 构建请求数据
    const requestData = {
      ruleName: `高级规则-${formData.ruleDescription.substring(0, 20)}`,
      ruleDescription: formData.ruleDescription,
      ruleType: 'ADVANCED',
      ruleSource: formData.ruleSource || 'CUSTOM',
      manageDepartment: formData.ruleManagementDepartment,
      applicableInsurance: formData.applicableInsurance,
      applicableRequirements: formData.applicableRequirements,
      businessArea: formData.applicableBusinessArea,
      subRuleData: {
        ruleDescription: formData.ruleDescription,
        alertType: formData.alertType,
        alertContent: formData.alertContent,
        applicableRequirements: formData.applicableRequirements,
        applicableInsurance: formData.applicableInsurance,
        applicableBusinessArea: formData.applicableBusinessArea,
        specificLocalArea: formData.specificLocalArea,
        productNature: formData.productNature,
        ruleManagementDepartment: formData.ruleManagementDepartment,
        ruleSource: formData.ruleSource,
        advancedRuleConfig: formData.advancedRuleConfig,
        executionScript: formData.executionScript,
        ruleWeight: formData.ruleWeight,
        llmEnabled: formData.llmEnabled,
        llmModelConfig: formData.llmModelConfig
      }
    }

    const response = await rulesApi.createRule(requestData)
    if (response.success) {
      message.success('高级规则创建成功')
      // 发出成功事件
      emit('success', response.data)
    } else {
      message.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error('创建高级规则失败:', error)
    message.error('创建失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  // 重置默认值
  formData.ruleWeight = 100
  formData.llmEnabled = false
  formData.advancedRuleConfig = '{"complexity": "high", "llm_enabled": false}'
}

// 定义事件
const emit = defineEmits<{
  success: [data: any]
  cancel: []
}>()
</script>

<style scoped lang="scss">
.rule-create-advanced {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
  }

  .script-help,
  .config-help,
  .llm-help {
    margin-top: 8px;
  }

  .form-actions {
    text-align: center;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>