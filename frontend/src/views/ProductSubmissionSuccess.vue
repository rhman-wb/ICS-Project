<template>
  <div class="product-submission-success">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <router-link to="/dashboard">工作台</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>
          <router-link to="/product-management">产品管理</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item>提交成功</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <!-- 成功页面主体 -->
    <div class="success-content">
      <a-result
        status="success"
        title="产品提交成功"
        :sub-title="successSubTitle"
      >
        <template #icon>
          <check-circle-outlined style="color: #52c41a;" />
        </template>

        <!-- 产品详情信息 -->
        <div class="product-details" v-if="productDetail">
          <a-descriptions
            title="产品信息"
            :bordered="true"
            :column="2"
            size="middle"
            class="product-info"
          >
            <a-descriptions-item label="项目ID">
              <a-tag color="blue">{{ productDetail.id }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="产品名称">
              {{ productDetail.productName }}
            </a-descriptions-item>
            <a-descriptions-item label="产品类型">
              {{ getProductTypeName(productDetail.productType) }}
            </a-descriptions-item>
            <a-descriptions-item label="报送类型">
              {{ productDetail.reportType }}
            </a-descriptions-item>
            <a-descriptions-item label="负责人">
              {{ productDetail.createdBy || '系统管理员' }}
            </a-descriptions-item>
            <a-descriptions-item label="提交时间">
              {{ formatDateTime(productDetail.updatedTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="生效时间">
              {{ getEffectiveTime() }}
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="getStatusColor(productDetail.status)">
                {{ getStatusText(productDetail.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="经营区域" :span="2" v-if="productDetail.operatingRegion">
              {{ productDetail.operatingRegion }}
            </a-descriptions-item>
            <a-descriptions-item label="备注" :span="2" v-if="submissionNote">
              {{ submissionNote }}
            </a-descriptions-item>
          </a-descriptions>

          <!-- 文档完整性统计 -->
          <div class="document-summary" v-if="documentSummary">
            <h3>文档统计</h3>
            <a-row :gutter="16" class="summary-stats">
              <a-col :span="6">
                <a-card>
                  <a-statistic
                    title="已上传文档"
                    :value="documentSummary.uploadedDocuments"
                    :value-style="{ color: '#1890ff' }"
                  >
                    <template #suffix>
                      <span>/ {{ documentSummary.requiredDocuments }}</span>
                    </template>
                  </a-statistic>
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card>
                  <a-statistic
                    title="文档完整性"
                    :value="documentSummary.completenessPercentage"
                    suffix="%"
                    :value-style="{ color: '#52c41a' }"
                  />
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card>
                  <a-statistic
                    title="校验错误"
                    :value="documentSummary.totalErrors"
                    :value-style="{ color: documentSummary.totalErrors > 0 ? '#ff4d4f' : '#52c41a' }"
                  />
                </a-card>
              </a-col>
              <a-col :span="6">
                <a-card>
                  <a-statistic
                    title="校验警告"
                    :value="documentSummary.totalWarnings"
                    :value-style="{ color: documentSummary.totalWarnings > 0 ? '#faad14' : '#52c41a' }"
                  />
                </a-card>
              </a-col>
            </a-row>
          </div>
        </div>

        <!-- 操作按钮 -->
        <template #extra>
          <div class="action-buttons">
            <a-space size="middle">
              <a-button
                type="primary"
                size="large"
                @click="viewProductDetail"
                :icon="h(EyeOutlined)"
              >
                查看详情
              </a-button>
              <a-button
                size="large"
                @click="createNewProduct"
                :icon="h(PlusOutlined)"
              >
                新增产品
              </a-button>
              <a-button
                size="large"
                @click="backToList"
                :icon="h(UnorderedListOutlined)"
              >
                返回列表
              </a-button>
              <a-button
                type="link"
                @click="downloadReceipt"
                :loading="downloadingReceipt"
                :icon="h(DownloadOutlined)"
              >
                下载回执
              </a-button>
            </a-space>
          </div>
        </template>
      </a-result>

      <!-- 下一步指引 -->
      <div class="next-steps" v-if="!hideNextSteps">
        <a-card title="下一步操作指引" class="guide-card">
          <a-timeline>
            <a-timeline-item
              color="green"
              :dot="h(CheckCircleOutlined, { style: { color: '#52c41a', fontSize: '16px' } })"
            >
              <div class="timeline-content">
                <strong>产品已提交</strong>
                <p>您的产品信息已成功提交到审核系统，系统将自动进行初步校验。</p>
              </div>
            </a-timeline-item>

            <a-timeline-item
              color="blue"
              :dot="h(ClockCircleOutlined, { style: { color: '#1890ff', fontSize: '16px' } })"
            >
              <div class="timeline-content">
                <strong>审核处理中</strong>
                <p>预计审核时间：5-10个工作日，我们会通过系统消息和邮件及时通知您审核进度。</p>
              </div>
            </a-timeline-item>

            <a-timeline-item
              color="gray"
              :dot="h(BellOutlined, { style: { color: '#999', fontSize: '16px' } })"
            >
              <div class="timeline-content">
                <strong>审核结果通知</strong>
                <p>审核完成后，您将收到详细的审核结果和意见反馈。</p>
              </div>
            </a-timeline-item>

            <a-timeline-item
              color="gray"
              :dot="h(FileTextOutlined, { style: { color: '#999', fontSize: '16px' } })"
            >
              <div class="timeline-content">
                <strong>完成审核</strong>
                <p>通过审核的产品将正式生效，您可以在产品列表中查看最终状态。</p>
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </div>

      <!-- 联系方式 -->
      <div class="contact-info">
        <a-alert
          message="如有疑问请联系我们"
          description="如果您对审核流程有任何疑问，可以通过系统消息或邮件联系我们的客服团队。"
          type="info"
          show-icon
          closable
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  EyeOutlined,
  PlusOutlined,
  UnorderedListOutlined,
  DownloadOutlined,
  ClockCircleOutlined,
  BellOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'
import { formatDateTime } from '@/utils/date'

interface ProductDetail {
  id: string
  productName: string
  productType: string
  reportType: string
  createdBy: string
  updatedTime: string
  status: string
  operatingRegion?: string
}

interface DocumentSummary {
  uploadedDocuments: number
  requiredDocuments: number
  completenessPercentage: number
  totalErrors: number
  totalWarnings: number
}

const route = useRoute()
const router = useRouter()

// 响应式数据
const productDetail = ref<ProductDetail | null>(null)
const documentSummary = ref<DocumentSummary | null>(null)
const submissionNote = ref<string>('')
const downloadingReceipt = ref(false)
const hideNextSteps = ref(false)

// 计算属性
const successSubTitle = computed(() => {
  if (!productDetail.value) {
    return '您的产品已成功提交审核系统，请等待审核结果。'
  }

  return `产品「${productDetail.value.productName}」已成功提交，项目ID：${productDetail.value.id}`
})

// 方法
const loadProductDetail = async () => {
  const productId = route.params.productId as string || route.query.productId as string

  if (!productId) {
    message.error('产品ID参数缺失')
    router.push('/product-management')
    return
  }

  try {
    // 模拟API调用，实际项目中替换为真实API
    // const response = await ProductService.getProductDetail(productId)

    // 模拟数据
    productDetail.value = {
      id: productId,
      productName: '中国人寿财产保险股份有限公司西藏自治区中央财政补贴型羊养殖保险',
      productType: 'AGRICULTURAL',
      reportType: '新产品',
      createdBy: '张三',
      updatedTime: new Date().toISOString(),
      status: 'SUBMITTED',
      operatingRegion: '西藏自治区'
    }

    documentSummary.value = {
      uploadedDocuments: 5,
      requiredDocuments: 5,
      completenessPercentage: 100,
      totalErrors: 0,
      totalWarnings: 1
    }

    submissionNote.value = '文档齐全，已通过自动校验'

  } catch (error: any) {
    console.error('获取产品详情失败:', error)
    message.error('获取产品详情失败')
  }
}

const viewProductDetail = () => {
  if (productDetail.value) {
    router.push(`/product-management/detail/${productDetail.value.id}`)
  }
}

const createNewProduct = () => {
  router.push('/product-management/create')
}

const backToList = () => {
  router.push('/product-management')
}

const downloadReceipt = async () => {
  if (!productDetail.value) return

  downloadingReceipt.value = true

  try {
    // 模拟下载回执
    await new Promise(resolve => setTimeout(resolve, 2000))

    message.success('回执下载成功')
  } catch (error: any) {
    console.error('下载回执失败:', error)
    message.error('下载回执失败')
  } finally {
    downloadingReceipt.value = false
  }
}

const getProductTypeName = (productType: string): string => {
  const typeMap: Record<string, string> = {
    'AGRICULTURAL': '农险产品',
    'FILING': '备案产品'
  }
  return typeMap[productType] || productType
}

const getStatusColor = (status: string): string => {
  const colorMap: Record<string, string> = {
    'DRAFT': 'default',
    'SUBMITTED': 'processing',
    'APPROVED': 'success',
    'REJECTED': 'error'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'APPROVED': '已审批',
    'REJECTED': '已拒绝'
  }
  return textMap[status] || status
}

const getEffectiveTime = (): string => {
  // 模拟生效时间（审核通过后的下一个工作日）
  const now = new Date()
  const effectiveDate = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000) // 7天后
  return formatDateTime(effectiveDate.toISOString())
}

// 生命周期
onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped lang="scss">
.product-submission-success {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 64px);

  .page-header {
    margin-bottom: 24px;
  }

  .success-content {
    max-width: 1000px;
    margin: 0 auto;

    .ant-result {
      background-color: white;
      border-radius: 8px;
      padding: 48px 24px;
      margin-bottom: 24px;

      :deep(.ant-result-title) {
        color: #52c41a;
        font-size: 24px;
        margin-bottom: 8px;
      }

      :deep(.ant-result-subtitle) {
        color: #666;
        font-size: 16px;
        margin-bottom: 32px;
      }
    }

    .product-details {
      text-align: left;
      margin: 32px 0;

      .product-info {
        margin-bottom: 32px;

        :deep(.ant-descriptions-title) {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 16px;
        }

        :deep(.ant-descriptions-item-label) {
          font-weight: 500;
          color: #333;
        }

        :deep(.ant-descriptions-item-content) {
          color: #666;
        }
      }

      .document-summary {
        h3 {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 16px;
          color: #333;
        }

        .summary-stats {
          .ant-card {
            text-align: center;
            border-radius: 8px;

            .ant-statistic {
              .ant-statistic-title {
                color: #666;
                font-size: 14px;
              }

              .ant-statistic-content {
                font-size: 24px;
                font-weight: 600;
              }
            }
          }
        }
      }
    }

    .action-buttons {
      margin-top: 32px;

      .ant-btn {
        height: 48px;
        border-radius: 6px;
        font-weight: 500;

        &.ant-btn-primary {
          box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
        }
      }
    }

    .next-steps {
      margin-bottom: 24px;

      .guide-card {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        :deep(.ant-card-head-title) {
          font-size: 18px;
          font-weight: 600;
        }

        .ant-timeline {
          padding-top: 16px;

          .timeline-content {
            strong {
              color: #333;
              font-size: 16px;
              display: block;
              margin-bottom: 4px;
            }

            p {
              color: #666;
              font-size: 14px;
              margin: 0;
              line-height: 1.5;
            }
          }
        }
      }
    }

    .contact-info {
      .ant-alert {
        border-radius: 8px;
      }
    }
  }
}

@media (max-width: 768px) {
  .product-submission-success {
    padding: 16px;

    .success-content {
      .ant-result {
        padding: 32px 16px;
      }

      .action-buttons {
        .ant-space {
          flex-direction: column;
          width: 100%;

          .ant-btn {
            width: 100%;
          }
        }
      }

      .document-summary {
        .summary-stats {
          .ant-col {
            margin-bottom: 16px;
          }
        }
      }
    }
  }
}
</style>