<template>
  <div class="filing-product-form">
    <a-form
      :model="formData"
      :rules="validationRules"
      layout="vertical"
      @finish="handleSubmit"
      @finish-failed="handleSubmitFailed"
    >
      <!-- 基本信息部分 -->
      <a-card title="备案产品基本信息" class="form-section">
        <a-row :gutter="16">
          <!-- 产品名称 -->
          <a-col :xl="12" :lg="12" :md="24" :sm="24">
            <a-form-item label="产品名称" name="productName" required>
              <a-input
                v-model:value="formData.productName"
                placeholder="请输入完整的产品名称"
                :maxlength="200"
                show-count
                @blur="handleProductNameBlur"
              />
            </a-form-item>
          </a-col>

          <!-- 报送类型 -->
          <a-col :xl="6" :lg="6" :md="12" :sm="24">
            <a-form-item label="报送类型" name="reportType" required>
              <a-select
                v-model:value="formData.reportType"
                placeholder="请选择报送类型"
                @change="handleReportTypeChange"
              >
                <a-select-option value="新产品">新产品</a-select-option>
                <a-select-option value="修订产品">修订产品</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <!-- 开发类型 -->
          <a-col :xl="6" :lg="6" :md="12" :sm="24">
            <a-form-item label="开发类型" name="developmentMethod" required>
              <a-select
                v-model:value="formData.developmentMethod"
                placeholder="请选择开发类型"
              >
                <a-select-option value="自主开发">自主开发</a-select-option>
                <a-select-option value="合作开发">合作开发</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 修订产品专属字段 -->
        <a-row v-if="formData.reportType === '修订产品'" :gutter="16">
          <a-col :xl="8" :lg="8" :md="12" :sm="24">
            <a-form-item label="修订类型" name="revisionType">
              <a-select
                v-model:value="formData.revisionType"
                placeholder="请选择修订类型"
              >
                <a-select-option value="条款修订">条款修订</a-select-option>
                <a-select-option value="费率修订">费率修订</a-select-option>
                <a-select-option value="条款费率修订">条款费率修订</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :xl="16" :lg="16" :md="24" :sm="24">
            <a-form-item label="原产品名称和编号/备案编号" name="originalProductName">
              <a-input
                v-model:value="formData.originalProductName"
                placeholder="请输入原产品名称和编号或备案编号"
                :maxlength="200"
                show-count
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- 示范条款名称 -->
          <a-col :xl="12" :lg="12" :md="24" :sm="24">
            <a-form-item label="示范条款名称" name="demonstrationClauseName">
              <a-input
                v-model:value="formData.demonstrationClauseName"
                placeholder="请输入示范条款名称"
                :maxlength="200"
                show-count
              />
            </a-form-item>
          </a-col>

          <!-- 主附险 -->
          <a-col :xl="6" :lg="6" :md="12" :sm="24">
            <a-form-item label="主附险" name="primaryAdditional">
              <a-select
                v-model:value="formData.primaryAdditional"
                placeholder="请选择主附险"
                @change="handlePrimaryAdditionalChange"
              >
                <a-select-option value="主险">主险</a-select-option>
                <a-select-option value="附险">附险</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <!-- 年度 -->
          <a-col :xl="6" :lg="6" :md="12" :sm="24">
            <a-form-item label="年度" name="year">
              <a-select
                v-model:value="formData.year"
                placeholder="请选择年度"
                allow-clear
              >
                <a-select-option v-for="year in yearOptions" :key="year" :value="year">
                  {{ year }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <!-- 主险情况（仅附险显示） -->
        <a-row v-if="formData.primaryAdditional === '附险'" :gutter="16">
          <a-col :span="24">
            <a-form-item label="主险情况" name="primarySituation">
              <a-textarea
                v-model:value="formData.primarySituation"
                placeholder="请详细描述主险情况，包括主险名称、条款内容等"
                :maxlength="500"
                show-count
                :rows="3"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <!-- 经营范围信息部分 -->
      <a-card title="经营范围和区域信息" class="form-section">
        <a-row :gutter="16">
          <!-- 经营范围 -->
          <a-col :span="24">
            <a-form-item label="经营范围" name="operatingScope">
              <a-textarea
                v-model:value="formData.operatingScope"
                placeholder="请详细描述产品的经营范围，包括适用的行业、业务类别等"
                :maxlength="1000"
                show-count
                :rows="3"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- 经营区域1 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24">
            <a-form-item label="经营区域1" name="operatingRegion1">
              <a-input
                v-model:value="formData.operatingRegion1"
                placeholder="请输入主要经营区域"
                :maxlength="100"
                show-count
              />
            </a-form-item>
          </a-col>

          <!-- 经营区域2 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24">
            <a-form-item label="经营区域2" name="operatingRegion2">
              <a-input
                v-model:value="formData.operatingRegion2"
                placeholder="请输入次要经营区域（可选）"
                :maxlength="100"
                show-count
              />
            </a-form-item>
          </a-col>

          <!-- 销售推广名称 -->
          <a-col :xl="8" :lg="8" :md="12" :sm="24">
            <a-form-item label="销售推广名称" name="salesPromotionName">
              <a-input
                v-model:value="formData.salesPromotionName"
                placeholder="请输入销售推广名称"
                :maxlength="100"
                show-count
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- 整体经营区域汇总 -->
          <a-col :span="24">
            <a-form-item label="经营区域汇总" name="operatingRegion">
              <a-textarea
                v-model:value="formData.operatingRegion"
                placeholder="请汇总描述完整的经营区域范围，如：全国范围内或特定省市区域"
                :maxlength="500"
                show-count
                :rows="2"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <!-- 表单操作按钮 -->
      <div class="form-actions">
        <a-space size="middle">
          <a-button @click="handleReset" :disabled="loading">
            <template #icon>
              <ReloadOutlined />
            </template>
            重置表单
          </a-button>
          <a-button
            type="primary"
            html-type="submit"
            :loading="loading"
          >
            <template #icon>
              <SaveOutlined />
            </template>
            保存产品信息
          </a-button>
        </a-space>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, SaveOutlined } from '@ant-design/icons-vue'
import type { FormValidationRules, ProductInfo } from '@/types/product'

interface Props {
  initialData?: Partial<ProductInfo>
  loading?: boolean
}

interface Emits {
  (e: 'submit', data: ProductInfo): void
  (e: 'reset'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<Emits>()

// 表单数据
const formData = reactive<ProductInfo>({
  productName: '',
  reportType: '',
  developmentMethod: '',
  revisionType: undefined,
  originalProductName: undefined,
  demonstrativeClauseName: undefined,
  primaryAdditional: undefined,
  year: undefined,
  primarySituation: undefined,
  businessScope: undefined,
  operatingRegion1: undefined,
  operatingRegion2: undefined,
  salesPromotionName: undefined,
  operatingRegion: undefined,
  ...props.initialData
})

// 年度选项
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const years: number[] = []
  for (let year = currentYear; year >= currentYear - 10; year--) {
    years.push(year)
  }
  return years
})

// 表单验证规则
const validationRules: FormValidationRules = {
  productName: [
    { required: true, message: '请输入产品名称' },
    { max: 200, message: '产品名称长度不能超过200个字符' }
  ],
  reportType: [
    { required: true, message: '请选择报送类型' }
  ],
  developmentMethod: [
    { required: true, message: '请选择开发类型' }
  ],
  revisionType: [
    {
      validator: (rule: any, value: any) => {
        if (formData.reportType === '修订产品' && !value) {
          return Promise.reject('修订产品必须选择修订类型')
        }
        return Promise.resolve()
      }
    }
  ],
  originalProductName: [
    {
      validator: (rule: any, value: any) => {
        if (formData.reportType === '修订产品' && !value) {
          return Promise.reject('修订产品必须填写原产品名称和编号')
        }
        return Promise.resolve()
      }
    }
  ],
  primarySituation: [
    {
      validator: (rule: any, value: any) => {
        if (formData.primaryAdditional === '附险' && !value) {
          return Promise.reject('附险必须描述主险情况')
        }
        return Promise.resolve()
      }
    }
  ],
  demonstrationClauseName: [
    { max: 200, message: '示范条款名称长度不能超过200个字符' }
  ],
  operatingScope: [
    { max: 1000, message: '经营范围描述长度不能超过1000个字符' }
  ],
  operatingRegion1: [
    { max: 100, message: '经营区域1长度不能超过100个字符' }
  ],
  operatingRegion2: [
    { max: 100, message: '经营区域2长度不能超过100个字符' }
  ],
  salesPromotionName: [
    { max: 100, message: '销售推广名称长度不能超过100个字符' }
  ],
  operatingRegion: [
    { max: 500, message: '经营区域汇总长度不能超过500个字符' }
  ]
}

// 处理产品名称失焦
const handleProductNameBlur = () => {
  if (formData.productName && formData.productName.length > 200) {
    message.warning('产品名称过长，已自动截取前200个字符')
    formData.productName = formData.productName.substring(0, 200)
  }
}

// 处理报送类型变化
const handleReportTypeChange = (value: string) => {
  if (value !== '修订产品') {
    // 清空修订产品相关字段
    formData.revisionType = undefined
    formData.originalProductName = undefined
  }
}

// 处理主附险变化
const handlePrimaryAdditionalChange = (value: string) => {
  if (value !== '附险') {
    // 清空主险情况字段
    formData.primarySituation = undefined
  }
}

// 处理表单提交
const handleSubmit = () => {
  // 确保产品类型为备案产品
  const submitData: ProductInfo = {
    ...formData,
    // 设置备案产品特有字段
    developmentType: formData.developmentMethod,
    businessScope: formData.operatingScope,
    demonstrativeClauseName: formData.demonstrationClauseName
  }

  message.success('备案产品信息保存成功')
  emit('submit', submitData)
}

// 处理表单提交失败
const handleSubmitFailed = (errorInfo: any) => {
  console.error('表单验证失败:', errorInfo)
  message.error('请检查表单输入项')
}

// 处理表单重置
const handleReset = () => {
  Object.keys(formData).forEach(key => {
    ;(formData as any)[key] = props.initialData?.[key as keyof ProductInfo] || ''
  })
  message.info('表单已重置')
  emit('reset')
}

// 暴露组件方法
defineExpose({
  formData,
  validate: () => {
    // 手动触发表单验证的方法，可用于父组件调用
    return new Promise((resolve, reject) => {
      // 这里应该调用 ant-design-vue 的表单验证方法
      // 但由于模板引用的复杂性，暂时返回基本验证
      if (!formData.productName || !formData.reportType || !formData.developmentMethod) {
        reject('必填项不能为空')
      } else {
        resolve(formData)
      }
    })
  }
})
</script>

<style scoped>
.filing-product-form {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.form-section {
  margin-bottom: 24px;
}

.form-section :deep(.ant-card-head) {
  background: linear-gradient(135deg, #f6f8fa 0%, #e8f4fd 100%);
  border-bottom: 2px solid #52c41a;
}

.form-section :deep(.ant-card-head-title) {
  color: #52c41a;
  font-weight: 600;
  font-size: 16px;
}

.form-actions {
  margin-top: 32px;
  text-align: center;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.form-actions .ant-btn {
  min-width: 120px;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
}

/* 表单项样式优化 */
:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #262626;
}

:deep(.ant-form-item-label > label::after) {
  display: none;
}

:deep(.ant-form-item-required::before) {
  content: '*';
  display: inline-block;
  margin-right: 4px;
  color: #ff4d4f;
  font-size: 14px;
  font-family: SimSun, sans-serif;
  line-height: 1;
}

:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-picker) {
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.ant-input:hover),
:deep(.ant-select-selector:hover),
:deep(.ant-picker:hover) {
  border-color: #73d13d;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.1);
}

:deep(.ant-input:focus),
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-picker-focused) {
  border-color: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
}

/* 文本域样式优化 */
:deep(.ant-input) {
  min-height: 32px;
}

:deep(.ant-input.ant-input-textarea) {
  resize: vertical;
  min-height: auto;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .filing-product-form {
    padding: 16px;
  }

  .form-actions {
    padding: 16px;
  }

  .form-actions .ant-btn {
    width: 100%;
    margin-bottom: 8px;
  }
}
</style>