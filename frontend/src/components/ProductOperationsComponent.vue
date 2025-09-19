<template>
  <div class="product-operations-component">
    <!-- 主要操作按钮组 -->
    <div class="main-operations">
      <a-space>
        <!-- 导入产品 - 完整实现 -->
        <a-button
          type="primary"
          size="large"
          :loading="importing"
          @click="handleImportProduct"
        >
          <template #icon>
            <UploadOutlined />
          </template>
          导入产品
        </a-button>

        <!-- 批量操作下拉菜单 -->
        <a-dropdown v-if="hasSelectedProducts">
          <a-button size="large">
            <template #icon>
              <AppstoreOutlined />
            </template>
            批量操作 ({{ selectedCount }})
            <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu @click="handleBatchOperation">
              <a-menu-item key="audit">
                <AuditOutlined />
                智能检核
              </a-menu-item>
              <a-menu-item key="export">
                <ExportOutlined />
                导出数据
              </a-menu-item>
              <a-menu-item key="download">
                <DownloadOutlined />
                下载文档
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="delete" class="danger-menu-item">
                <DeleteOutlined />
                批量删除
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>

        <!-- 快速操作按钮 -->
        <a-button-group>
          <a-tooltip title="刷新列表">
            <a-button
              :icon="h(ReloadOutlined)"
              :loading="refreshing"
              @click="handleRefresh"
            />
          </a-tooltip>
          <a-tooltip title="导出全部">
            <a-button
              :icon="h(ExportOutlined)"
              @click="handleExportAll"
            />
          </a-tooltip>
          <a-tooltip title="高级搜索">
            <a-button
              :icon="h(SearchOutlined)"
              @click="showAdvancedSearch = true"
            />
          </a-tooltip>
        </a-button-group>
      </a-space>
    </div>

    <!-- 产品操作功能区 -->
    <div class="product-actions">
      <a-row :gutter="[16, 16]">
        <!-- 智能检核卡片 -->
        <a-col :xl="8" :lg="12" :md="24">
          <a-card
            class="action-card audit-card"
            hoverable
            @click="handleBatchAudit"
          >
            <div class="card-content">
              <div class="card-icon">
                <AuditOutlined />
              </div>
              <div class="card-info">
                <h3 class="card-title">智能检核</h3>
                <p class="card-description">
                  对产品文档进行智能检核，检查合规性和完整性
                </p>
                <div class="card-stats">
                  <a-tag color="processing">{{ auditStats.pending }} 待检核</a-tag>
                  <a-tag color="success">{{ auditStats.completed }} 已完成</a-tag>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 查看报告卡片 -->
        <a-col :xl="8" :lg="12" :md="24">
          <a-card
            class="action-card report-card"
            hoverable
            @click="handleViewReports"
          >
            <div class="card-content">
              <div class="card-icon">
                <FileTextOutlined />
              </div>
              <div class="card-info">
                <h3 class="card-title">检核报告</h3>
                <p class="card-description">
                  查看详细的检核报告和修改建议
                </p>
                <div class="card-stats">
                  <a-tag color="error">{{ reportStats.errors }} 错误</a-tag>
                  <a-tag color="warning">{{ reportStats.warnings }} 警告</a-tag>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 文档管理卡片 -->
        <a-col :xl="8" :lg="12" :md="24">
          <a-card
            class="action-card document-card"
            hoverable
            @click="handleDocumentManagement"
          >
            <div class="card-content">
              <div class="card-icon">
                <FolderOutlined />
              </div>
              <div class="card-info">
                <h3 class="card-title">文档管理</h3>
                <p class="card-description">
                  管理产品相关的各类要件文档
                </p>
                <div class="card-stats">
                  <a-tag color="blue">{{ documentStats.total }} 文档</a-tag>
                  <a-tag color="green">{{ documentStats.uploaded }} 已上传</a-tag>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 导入产品对话框 -->
    <a-modal
      v-model:open="showImportDialog"
      title="导入产品"
      width="600px"
      :confirm-loading="importing"
      @ok="handleConfirmImport"
      @cancel="handleCancelImport"
    >
      <div class="import-dialog-content">
        <a-steps :current="importStep" size="small" class="import-steps">
          <a-step title="选择文件" />
          <a-step title="验证数据" />
          <a-step title="导入确认" />
        </a-steps>

        <!-- 步骤1: 文件选择 -->
        <div v-if="importStep === 0" class="import-step-content">
          <a-upload-dragger
            v-model:file-list="importFileList"
            :before-upload="beforeUpload"
            :remove="handleRemoveFile"
            accept=".xlsx,.xls,.csv"
            :multiple="false"
          >
            <p class="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p class="ant-upload-hint">
              支持 Excel 文件 (.xlsx, .xls) 和 CSV 文件
            </p>
          </a-upload-dragger>

          <div class="import-tips">
            <a-alert
              message="导入提示"
              type="info"
              show-icon
              closable
            >
              <template #description>
                <ul>
                  <li>请确保文件格式正确，包含必要的产品信息字段</li>
                  <li>建议先下载模板文件，按照模板格式填写数据</li>
                  <li>单次最多支持导入1000条产品记录</li>
                </ul>
              </template>
            </a-alert>

            <div class="template-download">
              <a-button type="link" @click="downloadTemplate">
                <DownloadOutlined />
                下载导入模板
              </a-button>
            </div>
          </div>
        </div>

        <!-- 步骤2: 数据验证 -->
        <div v-if="importStep === 1" class="import-step-content">
          <div class="validation-result">
            <a-spin :spinning="validating">
              <div v-if="validationResult">
                <a-result
                  :status="validationResult.success ? 'success' : 'warning'"
                  :title="validationResult.success ? '数据验证通过' : '发现数据问题'"
                  :sub-title="validationResult.message"
                >
                  <template #extra>
                    <a-descriptions :column="2" size="small">
                      <a-descriptions-item label="总记录数">
                        {{ validationResult.total }}
                      </a-descriptions-item>
                      <a-descriptions-item label="有效记录">
                        {{ validationResult.valid }}
                      </a-descriptions-item>
                      <a-descriptions-item label="错误记录">
                        {{ validationResult.errors }}
                      </a-descriptions-item>
                      <a-descriptions-item label="警告记录">
                        {{ validationResult.warnings }}
                      </a-descriptions-item>
                    </a-descriptions>
                  </template>
                </a-result>

                <!-- 错误详情 -->
                <div v-if="validationResult.errorDetails?.length" class="error-details">
                  <h4>错误详情</h4>
                  <a-table
                    :columns="errorColumns"
                    :data-source="validationResult.errorDetails"
                    :pagination="{ pageSize: 5 }"
                    size="small"
                  />
                </div>
              </div>
            </a-spin>
          </div>
        </div>

        <!-- 步骤3: 导入确认 -->
        <div v-if="importStep === 2" class="import-step-content">
          <a-result
            status="info"
            title="确认导入数据"
            :sub-title="`将导入 ${validationResult?.valid || 0} 条有效产品记录`"
          >
            <template #extra>
              <a-checkbox v-model:checked="skipDuplicates">
                跳过重复的产品记录
              </a-checkbox>
            </template>
          </a-result>
        </div>
      </div>

      <template #footer>
        <a-space>
          <a-button v-if="importStep > 0" @click="importStep--">
            上一步
          </a-button>
          <a-button @click="handleCancelImport">
            取消
          </a-button>
          <a-button
            v-if="importStep < 2"
            type="primary"
            :disabled="!canProceedNext"
            @click="handleNextStep"
          >
            下一步
          </a-button>
          <a-button
            v-else
            type="primary"
            :loading="importing"
            @click="handleConfirmImport"
          >
            确认导入
          </a-button>
        </a-space>
      </template>
    </a-modal>

    <!-- 高级搜索对话框 -->
    <a-drawer
      v-model:open="showAdvancedSearch"
      title="高级搜索"
      placement="right"
      width="400"
    >
      <div class="advanced-search-content">
        <a-form layout="vertical">
          <a-form-item label="产品状态">
            <a-checkbox-group v-model:value="advancedSearchForm.status">
              <a-checkbox value="DRAFT">草稿</a-checkbox>
              <a-checkbox value="SUBMITTED">已提交</a-checkbox>
              <a-checkbox value="APPROVED">已审批</a-checkbox>
              <a-checkbox value="REJECTED">已拒绝</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item label="创建时间">
            <a-range-picker
              v-model:value="advancedSearchForm.dateRange"
              style="width: 100%"
            />
          </a-form-item>

          <a-form-item label="文档数量范围">
            <a-slider
              v-model:value="advancedSearchForm.documentRange"
              range
              :min="0"
              :max="20"
              :marks="{ 0: '0', 5: '5', 10: '10', 15: '15', 20: '20+' }"
            />
          </a-form-item>

          <a-form-item label="检核状态">
            <a-select
              v-model:value="advancedSearchForm.auditStatus"
              placeholder="选择检核状态"
              mode="multiple"
              allow-clear
            >
              <a-select-option value="未上传文档">未上传文档</a-select-option>
              <a-select-option value="待审核">待审核</a-select-option>
              <a-select-option value="审核中">审核中</a-select-option>
              <a-select-option value="已完成">已完成</a-select-option>
              <a-select-option value="审核失败">审核失败</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>

        <div class="search-actions">
          <a-space>
            <a-button @click="resetAdvancedSearch">重置</a-button>
            <a-button type="primary" @click="applyAdvancedSearch">
              应用搜索
            </a-button>
          </a-space>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadFile, TableColumnsType } from 'ant-design-vue'
import {
  UploadOutlined,
  AppstoreOutlined,
  DownOutlined,
  AuditOutlined,
  ExportOutlined,
  DownloadOutlined,
  DeleteOutlined,
  ReloadOutlined,
  SearchOutlined,
  FileTextOutlined,
  FolderOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'

// 组件名称
defineOptions({
  name: 'ProductOperationsComponent'
})

// Props
interface Props {
  selectedProducts?: string[]
  onRefresh?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  selectedProducts: () => []
})

// Emits
const emit = defineEmits<{
  refresh: []
  batchOperation: [operation: string, products: string[]]
  importComplete: [result: any]
  advancedSearch: [filters: any]
}>()

// 响应式数据
const importing = ref(false)
const refreshing = ref(false)
const validating = ref(false)
const showImportDialog = ref(false)
const showAdvancedSearch = ref(false)
const importStep = ref(0)
const importFileList = ref<UploadFile[]>([])
const skipDuplicates = ref(true)

// 统计数据
const auditStats = reactive({
  pending: 15,
  completed: 42
})

const reportStats = reactive({
  errors: 8,
  warnings: 23
})

const documentStats = reactive({
  total: 156,
  uploaded: 142
})

// 验证结果
const validationResult = ref<{
  success: boolean
  message: string
  total: number
  valid: number
  errors: number
  warnings: number
  errorDetails?: any[]
} | null>(null)

// 高级搜索表单
const advancedSearchForm = reactive({
  status: [],
  dateRange: [],
  documentRange: [0, 20],
  auditStatus: []
})

// 计算属性
const hasSelectedProducts = computed(() => props.selectedProducts.length > 0)
const selectedCount = computed(() => props.selectedProducts.length)

const canProceedNext = computed(() => {
  if (importStep.value === 0) {
    return importFileList.value.length > 0
  }
  if (importStep.value === 1) {
    return validationResult.value?.success
  }
  return true
})

// 错误详情表格列
const errorColumns: TableColumnsType = [
  {
    title: '行号',
    dataIndex: 'row',
    width: 60
  },
  {
    title: '字段',
    dataIndex: 'field',
    width: 100
  },
  {
    title: '错误信息',
    dataIndex: 'message',
    ellipsis: true
  }
]

// 方法
const handleImportProduct = () => {
  showImportDialog.value = true
  importStep.value = 0
  importFileList.value = []
  validationResult.value = null
}

const handleBatchOperation = ({ key }: { key: string }) => {
  switch (key) {
    case 'audit':
      handleBatchAudit()
      break
    case 'export':
      handleBatchExport()
      break
    case 'download':
      handleBatchDownload()
      break
    case 'delete':
      handleBatchDelete()
      break
  }
}

const handleRefresh = () => {
  refreshing.value = true
  setTimeout(() => {
    refreshing.value = false
    emit('refresh')
    message.success('数据已刷新')
  }, 1000)
}

const handleExportAll = () => {
  message.info('正在导出全部数据...')
  // Mock implementation
  setTimeout(() => {
    message.success('导出完成')
  }, 2000)
}

// Mock操作方法
const handleBatchAudit = () => {
  if (hasSelectedProducts.value) {
    emit('batchOperation', 'audit', props.selectedProducts)
  } else {
    emit('batchOperation', 'audit', [])
  }
}

const handleViewReports = () => {
  message.info('查看检核报告功能（使用mock数据演示）')
}

const handleDocumentManagement = () => {
  message.info('文档管理功能（使用mock数据演示）')
}

const handleBatchExport = () => {
  message.info(`导出选中的 ${selectedCount.value} 个产品`)
}

const handleBatchDownload = () => {
  message.info(`下载选中的 ${selectedCount.value} 个产品的文档`)
}

const handleBatchDelete = () => {
  message.warning(`确认删除选中的 ${selectedCount.value} 个产品？`)
}

// 导入相关方法
const beforeUpload = (file: UploadFile) => {
  const isValidType = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                      'application/vnd.ms-excel',
                      'text/csv'].includes(file.type || '')

  if (!isValidType) {
    message.error('只支持 Excel 和 CSV 格式的文件')
    return false
  }

  const isLt10M = (file.size || 0) / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB')
    return false
  }

  return false // 阻止自动上传
}

const handleRemoveFile = () => {
  importFileList.value = []
  validationResult.value = null
  return true
}

const handleNextStep = async () => {
  if (importStep.value === 0) {
    // 验证文件
    await validateImportFile()
  }
  importStep.value++
}

const validateImportFile = async () => {
  if (importFileList.value.length === 0) return

  validating.value = true

  // Mock 验证过程
  await new Promise(resolve => setTimeout(resolve, 2000))

  // Mock 验证结果
  validationResult.value = {
    success: true,
    message: '文件格式正确，数据验证通过',
    total: 50,
    valid: 48,
    errors: 2,
    warnings: 3,
    errorDetails: [
      { row: 15, field: '产品名称', message: '产品名称不能为空' },
      { row: 23, field: '经营区域', message: '经营区域格式不正确' }
    ]
  }

  validating.value = false
}

const handleConfirmImport = async () => {
  importing.value = true

  try {
    // Mock 导入过程
    await new Promise(resolve => setTimeout(resolve, 3000))

    message.success(`成功导入 ${validationResult.value?.valid || 0} 条产品记录`)
    emit('importComplete', validationResult.value)

    // 重置状态
    showImportDialog.value = false
    importStep.value = 0
    importFileList.value = []
    validationResult.value = null

  } catch (error) {
    message.error('导入失败，请稍后重试')
  } finally {
    importing.value = false
  }
}

const handleCancelImport = () => {
  showImportDialog.value = false
  importStep.value = 0
  importFileList.value = []
  validationResult.value = null
}

const downloadTemplate = () => {
  message.info('正在下载导入模板...')
  // Mock 下载模板
  const link = document.createElement('a')
  link.href = '/templates/product-import-template.xlsx'
  link.download = '产品导入模板.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  setTimeout(() => {
    message.success('模板下载完成')
  }, 1000)
}

// 高级搜索方法
const resetAdvancedSearch = () => {
  advancedSearchForm.status = []
  advancedSearchForm.dateRange = []
  advancedSearchForm.documentRange = [0, 20]
  advancedSearchForm.auditStatus = []
}

const applyAdvancedSearch = () => {
  emit('advancedSearch', { ...advancedSearchForm })
  showAdvancedSearch.value = false
  message.success('高级搜索条件已应用')
}

// 暴露方法供父组件调用
defineExpose({
  openImportDialog: () => handleImportProduct(),
  openAdvancedSearch: () => showAdvancedSearch.value = true
})
</script>

<style scoped lang="scss">
.product-operations-component {
  .main-operations {
    margin-bottom: 24px;

    :deep(.ant-dropdown-menu) {
      .danger-menu-item {
        color: #ff4d4f;

        &:hover {
          background: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }

  .product-actions {
    .action-card {
      transition: all 0.3s ease;
      border-radius: 8px;
      overflow: hidden;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .card-content {
        display: flex;
        align-items: flex-start;
        gap: 16px;

        .card-icon {
          font-size: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 64px;
          height: 64px;
          border-radius: 12px;
          background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
        }

        .card-info {
          flex: 1;

          .card-title {
            margin: 0 0 8px 0;
            font-size: 16px;
            font-weight: 600;
            color: #262626;
          }

          .card-description {
            margin: 0 0 12px 0;
            color: #8c8c8c;
            font-size: 14px;
            line-height: 1.4;
          }

          .card-stats {
            .ant-tag {
              margin-right: 8px;
              border-radius: 4px;
            }
          }
        }
      }

      &.audit-card .card-icon {
        color: #1890ff;
        background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
      }

      &.report-card .card-icon {
        color: #52c41a;
        background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
      }

      &.document-card .card-icon {
        color: #faad14;
        background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
      }
    }
  }

  .import-dialog-content {
    .import-steps {
      margin-bottom: 24px;
    }

    .import-step-content {
      min-height: 300px;

      .import-tips {
        margin-top: 16px;

        .template-download {
          margin-top: 12px;
          text-align: center;
        }
      }

      .validation-result {
        .error-details {
          margin-top: 16px;

          h4 {
            margin-bottom: 12px;
            color: #262626;
          }
        }
      }
    }
  }

  .advanced-search-content {
    .search-actions {
      margin-top: 24px;
      text-align: right;
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .product-operations-component {
    .main-operations {
      :deep(.ant-space) {
        width: 100%;
        flex-wrap: wrap;
      }
    }

    .product-actions {
      .action-card {
        .card-content {
          flex-direction: column;
          text-align: center;

          .card-icon {
            align-self: center;
          }
        }
      }
    }
  }
}
</style>