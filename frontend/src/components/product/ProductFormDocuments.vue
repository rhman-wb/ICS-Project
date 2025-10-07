<template>
  <div class="product-form-documents">
    <!-- 条款路径 -->
    <template v-if="section === 'clause'">
      <a-form-item label="条款路径:" required>
        <span class="required-star">*</span>
        <a-input
          v-model:value="clausePath"
          placeholder="请填写条款文档路径"
          readonly
        >
          <template #suffix>
            <a-button type="link" size="small" @click="triggerFileUpload('clause')">
              <SearchOutlined />
            </a-button>
          </template>
        </a-input>
        <input
          ref="clauseFileRef"
          type="file"
          accept=".docx,.doc,.pdf"
          style="display: none"
          @change="handleFileSelect('clause', $event)"
        />
      </a-form-item>
    </template>

    <!-- 可行性报告路径 -->
    <template v-if="section === 'feasibilityReport'">
      <a-form-item label="可行性报告路径:" required>
        <span class="required-star">*</span>
        <a-input
          v-model:value="feasibilityReportPath"
          placeholder="请填写可行性报告文档路径"
          readonly
        >
          <template #suffix>
            <a-button type="link" size="small" @click="triggerFileUpload('feasibilityReport')">
              <SearchOutlined />
            </a-button>
          </template>
        </a-input>
        <input
          ref="feasibilityFileRef"
          type="file"
          accept=".docx,.doc,.pdf"
          style="display: none"
          @change="handleFileSelect('feasibilityReport', $event)"
        />
      </a-form-item>
    </template>

    <!-- 精算报告路径（可选） -->
    <template v-if="section === 'actuarialReport'">
      <a-form-item label="精算报告路径:">
        <a-input
          v-model:value="actuarialReportPath"
          placeholder="请填写精算报告文档路径"
          readonly
        >
          <template #suffix>
            <a-button type="link" size="small" @click="triggerFileUpload('actuarialReport')">
              <SearchOutlined />
            </a-button>
          </template>
        </a-input>
        <input
          ref="actuarialFileRef"
          type="file"
          accept=".docx,.doc,.pdf"
          style="display: none"
          @change="handleFileSelect('actuarialReport', $event)"
        />
      </a-form-item>
    </template>

    <!-- 费率表路径（可选） -->
    <template v-if="section === 'rateTable'">
      <a-form-item label="费率表路径:">
        <a-input
          v-model:value="rateTablePath"
          placeholder="请填写费率表文档路径"
          readonly
        >
          <template #suffix>
            <a-button type="link" size="small" @click="triggerFileUpload('rateTable')">
              <SearchOutlined />
            </a-button>
          </template>
        </a-input>
        <input
          ref="rateTableFileRef"
          type="file"
          accept=".xlsx,.xls,.docx,.doc,.pdf"
          style="display: none"
          @change="handleFileSelect('rateTable', $event)"
        />
      </a-form-item>
    </template>

    <!-- 验证错误提示 -->
    <div v-if="validationErrors[section]" class="validation-error">
      <CloseCircleOutlined />
      {{ validationErrors[section] }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'
import type { ProductDocuments } from '@/types/product/template'
import { uploadDocument, validateDocument } from '@/api/product/document'
import logger from '@/utils/logger'

// Props
interface Props {
  documents: ProductDocuments
  section: 'clause' | 'feasibilityReport' | 'actuarialReport' | 'rateTable'
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:documents': [value: ProductDocuments]
  'validationChange': [validations: Record<string, boolean>]
}>()

// Refs
const clauseFileRef = ref<HTMLInputElement>()
const feasibilityFileRef = ref<HTMLInputElement>()
const actuarialFileRef = ref<HTMLInputElement>()
const rateTableFileRef = ref<HTMLInputElement>()

// 文档路径
const clausePath = ref(props.documents.clause?.path || '')
const feasibilityReportPath = ref(props.documents.feasibilityReport?.path || '')
const actuarialReportPath = ref(props.documents.actuarialReport?.path || '')
const rateTablePath = ref(props.documents.rateTable?.path || '')

// 验证错误
const validationErrors = ref<Record<string, string>>({})

// Computed
const documentsData = computed({
  get: () => props.documents,
  set: (value) => emit('update:documents', value)
})

// Watch 文档路径变化
watch([clausePath, feasibilityReportPath, actuarialReportPath, rateTablePath], () => {
  updateDocumentsData()
  validateAllDocuments()
})

// Methods

/**
 * 触发文件上传
 */
const triggerFileUpload = (type: string) => {
  switch (type) {
    case 'clause':
      clauseFileRef.value?.click()
      break
    case 'feasibilityReport':
      feasibilityFileRef.value?.click()
      break
    case 'actuarialReport':
      actuarialFileRef.value?.click()
      break
    case 'rateTable':
      rateTableFileRef.value?.click()
      break
  }
}

/**
 * 处理文件选择
 */
const handleFileSelect = async (
  type: 'clause' | 'feasibilityReport' | 'actuarialReport' | 'rateTable',
  event: Event
) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  // 验证文件类型
  const validTypes = type === 'rateTable'
    ? ['.xlsx', '.xls', '.docx', '.doc', '.pdf']
    : ['.docx', '.doc', '.pdf']

  const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()

  if (!validTypes.includes(fileExtension)) {
    message.error(`请上传正确格式的文件 (${validTypes.join(', ')})`)
    return
  }

  // 验证文件大小（限制50MB）
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    message.error('文件大小不能超过50MB')
    return
  }

  try {
    message.loading('正在上传文档...', 0)

    // 调用上传API
    const formData = new FormData()
    formData.append('file', file)
    formData.append('documentType', type)

    const uploadResult = await uploadDocument(formData)

    message.destroy()

    if (uploadResult.success) {
      // 更新路径
      switch (type) {
        case 'clause':
          clausePath.value = uploadResult.data.path || file.name
          break
        case 'feasibilityReport':
          feasibilityReportPath.value = uploadResult.data.path || file.name
          break
        case 'actuarialReport':
          actuarialReportPath.value = uploadResult.data.path || file.name
          break
        case 'rateTable':
          rateTablePath.value = uploadResult.data.path || file.name
          break
      }

      // 更新文档数据
      updateDocumentsData()

      // 验证文档
      await validateDocumentContent(type, file)

      message.success('文档上传成功')
      logger.info('Document uploaded successfully', { type, fileName: file.name })
    } else {
      message.error('文档上传失败')
    }
  } catch (error: any) {
    message.destroy()
    message.error(`上传失败：${error.message || '未知错误'}`)
    logger.error('Failed to upload document', error)
  } finally {
    // 重置文件输入
    if (target) {
      target.value = ''
    }
  }
}

/**
 * 更新文档数据
 */
const updateDocumentsData = () => {
  const updatedDocuments: ProductDocuments = {
    clause: {
      path: clausePath.value,
      file: null
    },
    feasibilityReport: {
      path: feasibilityReportPath.value,
      file: null
    },
    actuarialReport: {
      path: actuarialReportPath.value,
      file: null
    },
    rateTable: {
      path: rateTablePath.value,
      file: null
    }
  }

  emit('update:documents', updatedDocuments)
}

/**
 * 验证文档内容
 */
const validateDocumentContent = async (
  type: 'clause' | 'feasibilityReport' | 'actuarialReport' | 'rateTable',
  file: File
) => {
  try {
    // 调用验证API
    const formData = new FormData()
    formData.append('file', file)
    formData.append('documentType', type)

    const validationResult = await validateDocument(formData)

    if (!validationResult.success) {
      // 显示验证错误
      validationErrors.value[type] = validationResult.message || '文档验证失败'

      // 发送验证失败状态
      emit('validationChange', {
        [type]: false
      })
    } else {
      // 清除验证错误
      delete validationErrors.value[type]

      // 发送验证成功状态
      emit('validationChange', {
        [type]: true
      })
    }
  } catch (error: any) {
    logger.error('Document validation error', error)
    validationErrors.value[type] = error.message || '文档验证失败'

    emit('validationChange', {
      [type]: false
    })
  }
}

/**
 * 验证所有文档
 */
const validateAllDocuments = () => {
  const validations: Record<string, boolean> = {}

  // 必填项验证
  validations.clause = !!clausePath.value
  validations.feasibilityReport = !!feasibilityReportPath.value

  // 可选项验证
  validations.actuarialReport = true // 可选项默认为true
  validations.rateTable = true // 可选项默认为true

  emit('validationChange', validations)
}

// 初始化时验证
validateAllDocuments()
</script>

<style scoped lang="scss">
.product-form-documents {
  .required-star {
    color: #ff4d4f;
    margin-right: 4px;
  }

  .validation-error {
    color: #ff4d4f;
    font-size: 14px;
    margin-top: -16px;
    margin-bottom: 16px;
    padding: 8px 12px;
    background: #fff2f0;
    border: 1px solid #ffccc7;
    border-radius: 4px;
    display: flex;
    align-items: center;
    gap: 8px;

    .anticon {
      font-size: 16px;
    }
  }

  :deep(.ant-input) {
    cursor: pointer;
  }

  :deep(.ant-form-item) {
    margin-bottom: 24px;
  }
}
</style>
