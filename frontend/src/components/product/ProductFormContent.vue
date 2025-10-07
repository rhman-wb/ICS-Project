<template>
  <div class="product-form-content">
    <!-- 产品内容标题 -->
    <div class="content-section-title">产品内容</div>

    <!-- 上传产品信息登记表 -->
    <a-form-item label="上传产品信息登记表:">
      <a-input
        v-model:value="formData.registrationTablePath"
        placeholder="选择产品信息登记表路径"
        readonly
      >
        <template #suffix>
          <a-button type="link" size="small" @click="triggerFileUpload">
            <SearchOutlined />
          </a-button>
        </template>
      </a-input>
      <input
        ref="fileInputRef"
        type="file"
        accept=".xlsx,.xls"
        style="display: none"
        @change="handleFileSelect"
      />
    </a-form-item>

    <!-- 模板选择 -->
    <a-form-item label="模板选择:" required>
      <span class="required-star">*</span>
      <a-select
        v-model:value="formData.templateSelection"
        placeholder="农险产品信息登记表/备案产品自主注册信息登记表"
        @change="handleTemplateChange"
      >
        <a-select-option value="AGRICULTURAL">
          农险产品信息登记表
        </a-select-option>
        <a-select-option value="FILING">
          备案产品自主注册信息登记表
        </a-select-option>
      </a-select>
    </a-form-item>

    <!-- 农险产品表单 -->
    <template v-if="currentTemplateType === 'AGRICULTURAL'">
      <!-- 第一行：产品名称 + 开发方式 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="产品名称:" required>
            <span class="required-star">*</span>
            <a-select
              v-model:value="formData.productName"
              placeholder="请填写产品名称"
              show-search
              allow-clear
              @change="handleFieldChange('productName')"
            >
              <a-select-option value="">请填写产品名称</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="开发方式:">
            <a-select
              v-model:value="formData.developmentMethod"
              placeholder="请选择开发方式"
              allow-clear
              @change="handleFieldChange('developmentMethod')"
            >
              <a-select-option value="自主开发">自主开发</a-select-option>
              <a-select-option value="合作开发">合作开发</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第二行：报送类型 + 主附险 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="报送类型:" required>
            <span class="required-star">*</span>
            <a-select
              v-model:value="formData.submissionType"
              placeholder="请选择报送类型"
              allow-clear
              @change="handleFieldChange('submissionType')"
            >
              <a-select-option value="新产品">新产品</a-select-option>
              <a-select-option value="修订产品">修订产品</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="主附险:">
            <a-select
              v-model:value="formData.mainSupplementary"
              placeholder="请选择主附险"
              allow-clear
              @change="handleFieldChange('mainSupplementary')"
            >
              <a-select-option value="主险">主险</a-select-option>
              <a-select-option value="附险">附险</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第三行：修订类型 + 主险情况 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="修订类型:" required>
            <span class="required-star">*</span>
            <a-select
              v-model:value="formData.revisionType"
              placeholder="请选择修订类型"
              allow-clear
              @change="handleFieldChange('revisionType')"
            >
              <a-select-option value="条款修订">条款修订</a-select-option>
              <a-select-option value="费率修订">费率修订</a-select-option>
              <a-select-option value="条款费率修订">条款费率修订</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="主险情况:">
            <a-input
              v-model:value="formData.mainInsuranceStatus"
              placeholder="请填写主险情况"
              @change="handleFieldChange('mainInsuranceStatus')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第四行：原产品名称和编号 + 经营区域 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="原产品名称和编号:">
            <a-select
              v-model:value="formData.originalProductName"
              placeholder="请填写原产品名称和编号"
              show-search
              allow-clear
              @change="handleFieldChange('originalProductName')"
            >
              <a-select-option value="">请填写原产品名称和编号</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="经营区域:">
            <a-input
              v-model:value="formData.operatingArea"
              placeholder="请填写经营区域"
              @change="handleFieldChange('operatingArea')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 第五行：产品类别 + 年度 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="产品类别:">
            <a-select
              v-model:value="formData.productCategory"
              placeholder="请选择产品类别"
              allow-clear
              @change="handleFieldChange('productCategory')"
            >
              <a-select-option value="种植险">种植险</a-select-option>
              <a-select-option value="养殖险">养殖险</a-select-option>
              <a-select-option value="不区分">不区分</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="年度:">
            <a-input
              v-model:value="formData.year"
              placeholder="请填写年度"
              @change="handleFieldChange('year')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 产品性质 -->
      <a-form-item label="产品性质:">
        <a-input
          v-model:value="formData.productNature"
          placeholder="请选择产品性质"
          @change="handleFieldChange('productNature')"
        />
      </a-form-item>
    </template>

    <!-- 备案产品表单 -->
    <template v-else-if="currentTemplateType === 'FILING'">
      <!-- 备案产品的字段布局（参考需求文档6.1节） -->
      <!-- 第一行：产品名称 + 示范条款名称 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="产品名称:" required>
            <span class="required-star">*</span>
            <a-input
              v-model:value="formData.productName"
              placeholder="请填写产品名称"
              @change="handleFieldChange('productName')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="示范条款名称:" required>
            <span class="required-star">*</span>
            <a-input
              v-model:value="formData.demonstrationClauseName"
              placeholder="请填写示范条款名称"
              @change="handleFieldChange('demonstrationClauseName')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 更多备案产品字段... -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="开发类型:" required>
            <span class="required-star">*</span>
            <a-select
              v-model:value="formData.developmentMethod"
              placeholder="请选择开发类型"
              allow-clear
              @change="handleFieldChange('developmentMethod')"
            >
              <a-select-option value="自主开发">自主开发</a-select-option>
              <a-select-option value="合作开发">合作开发</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="报送类型:">
            <a-select
              v-model:value="formData.submissionType"
              placeholder="请选择报送类型"
              allow-clear
              @change="handleFieldChange('submissionType')"
            >
              <a-select-option value="新产品">新产品</a-select-option>
              <a-select-option value="修订产品">修订产品</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 继续添加备案产品其他字段 -->
      <a-row :gutter="24">
        <a-col :span="12">
          <a-form-item label="修订类型:" required>
            <span class="required-star">*</span>
            <a-select
              v-model:value="formData.revisionType"
              placeholder="请选择修订类型"
              allow-clear
              @change="handleFieldChange('revisionType')"
            >
              <a-select-option value="条款修订">条款修订</a-select-option>
              <a-select-option value="费率修订">费率修订</a-select-option>
              <a-select-option value="条款费率修订">条款费率修订</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="主附险类型:">
            <a-select
              v-model:value="formData.mainSupplementary"
              placeholder="请选择主附险类型"
              allow-clear
              @change="handleFieldChange('mainSupplementary')"
            >
              <a-select-option value="主险">主险</a-select-option>
              <a-select-option value="附险">附险</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import type { ProductFormData, TemplateType } from '@/types/product/template'
import { parseProductRegistrationTable } from '@/api/product/template'
import logger from '@/utils/logger'

// Props
interface Props {
  formData: Partial<ProductFormData>
  templateType?: TemplateType
}

const props = withDefaults(defineProps<Props>(), {
  templateType: 'AGRICULTURAL'
})

// Emits
const emit = defineEmits<{
  'update:formData': [value: Partial<ProductFormData>]
  'update:templateType': [value: TemplateType]
  'parseSuccess': [data: any]
  'validationChange': [validations: Record<string, boolean>]
}>()

// Refs
const fileInputRef = ref<HTMLInputElement>()
const currentTemplateType = ref<TemplateType>(props.templateType)

// Computed
const formData = computed({
  get: () => props.formData,
  set: (value) => emit('update:formData', value)
})

// Watch template type changes
watch(currentTemplateType, (newType) => {
  emit('update:templateType', newType)
})

// Methods

/**
 * 触发文件上传
 */
const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

/**
 * 处理文件选择
 */
const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  // 验证文件类型
  const validTypes = ['.xlsx', '.xls']
  const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()

  if (!validTypes.includes(fileExtension)) {
    message.error('请上传Excel文件 (.xlsx, .xls)')
    return
  }

  try {
    message.loading('正在解析产品信息登记表...', 0)

    // 调用解析API
    const formDataToSend = new FormData()
    formDataToSend.append('file', file)
    formDataToSend.append('templateType', currentTemplateType.value)

    const parseResult = await parseProductRegistrationTable(formDataToSend)

    message.destroy()

    if (parseResult.success) {
      // 更新表单数据
      const updatedFormData = { ...formData.value, ...parseResult.data }
      updatedFormData.registrationTablePath = file.name

      emit('update:formData', updatedFormData)
      emit('parseSuccess', parseResult.data)

      message.success('产品信息登记表解析成功')
      logger.info('Registration table parsed successfully', parseResult.data)
    } else {
      message.warning('部分信息解析失败，请手动补充')
      logger.warn('Partial parsing failure', parseResult.errors)
    }
  } catch (error: any) {
    message.destroy()
    message.error(`解析失败：${error.message || '未知错误'}`)
    logger.error('Failed to parse registration table', error)
  } finally {
    // 重置文件输入
    if (target) {
      target.value = ''
    }
  }
}

/**
 * 处理模板类型变化
 */
const handleTemplateChange = (value: TemplateType) => {
  currentTemplateType.value = value

  // 清空表单数据（保留模板选择）
  const clearedData: Partial<ProductFormData> = {
    templateSelection: value
  }

  emit('update:formData', clearedData)
  message.info(`已切换到${value === 'AGRICULTURAL' ? '农险' : '备案'}产品模板`)
}

/**
 * 处理字段变化
 */
const handleFieldChange = (fieldName: string) => {
  // 触发字段验证
  validateField(fieldName)
}

/**
 * 验证单个字段
 */
const validateField = (fieldName: string) => {
  const value = formData.value[fieldName as keyof ProductFormData]
  const isValid = value !== undefined && value !== null && value !== ''

  // 发送验证结果
  emit('validationChange', {
    [fieldName]: isValid
  })
}

/**
 * 验证所有必填字段
 */
const validateAllFields = () => {
  const requiredFields = currentTemplateType.value === 'AGRICULTURAL'
    ? ['productName', 'submissionType', 'revisionType']
    : ['productName', 'demonstrationClauseName', 'developmentMethod', 'revisionType']

  const validations: Record<string, boolean> = {}

  requiredFields.forEach(field => {
    const value = formData.value[field as keyof ProductFormData]
    validations[field] = value !== undefined && value !== null && value !== ''
  })

  emit('validationChange', validations)
}

// 初始化时验证
validateAllFields()
</script>

<style scoped lang="scss">
.product-form-content {
  .content-section-title {
    font-size: 15px;
    font-weight: 600;
    color: #262626;
    margin-bottom: 16px;
  }

  .required-star {
    color: #ff4d4f;
    margin-right: 4px;
  }

  :deep(.ant-form-item-label > label) {
    font-weight: 500;
  }

  :deep(.ant-select),
  :deep(.ant-input) {
    width: 100%;
  }
}
</style>
