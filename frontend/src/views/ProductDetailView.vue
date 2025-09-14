<template>
  <div class="product-detail-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <a-button
          type="text"
          :icon="h(ArrowLeftOutlined)"
          class="back-btn"
          @click="handleGoBack"
        >
          返回
        </a-button>
        <a-breadcrumb class="breadcrumb">
          <a-breadcrumb-item>
            <router-link to="/dashboard/home">工作台</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>
            <router-link to="/product-management">产品管理</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>产品详情</a-breadcrumb-item>
        </a-breadcrumb>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <a-spin size="large" tip="加载中...">
        <div class="loading-content"></div>
      </a-spin>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <a-result
        status="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <a-space>
            <a-button @click="handleGoBack">返回列表</a-button>
            <a-button type="primary" @click="fetchProductDetail">重新加载</a-button>
          </a-space>
        </template>
      </a-result>
    </div>

    <!-- 产品详情内容 -->
    <div v-else-if="productDetail" class="detail-content">
      <!-- 产品标题和基本信息 -->
      <a-card class="product-header-card" :bordered="false">
        <div class="product-title-section">
          <h1 class="product-title">{{ productDetail.productName }}</h1>
          <div class="product-status">
            <a-tag
              :color="getStatusColor(productDetail.status)"
              class="status-tag"
            >
              {{ getStatusText(productDetail.status) }}
            </a-tag>
            <a-tag
              v-if="productDetail.auditStatus"
              :color="getAuditStatusColor(productDetail.auditStatus)"
              class="audit-status-tag"
            >
              {{ productDetail.auditStatus }}
            </a-tag>
          </div>
        </div>

        <a-divider />

        <!-- 基本信息展示 -->
        <a-descriptions :column="2" size="middle" bordered>
          <a-descriptions-item label="产品ID">
            <a-typography-text copyable>{{ productDetail.id }}</a-typography-text>
          </a-descriptions-item>
          <a-descriptions-item label="报送类型">
            {{ productDetail.reportType }}
          </a-descriptions-item>
          <a-descriptions-item label="产品性质">
            {{ productDetail.productNature || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="年度">
            {{ productDetail.year || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="修订类型">
            {{ productDetail.revisionType || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="开发方式">
            {{ productDetail.developmentMethod || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="主附险">
            {{ productDetail.primaryAdditional || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="产品类别">
            {{ productDetail.productCategory || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="经营区域">
            {{ productDetail.operatingRegion || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="经营范围">
            {{ productDetail.operatingScope || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建人">
            {{ productDetail.createdBy || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatDateTime(productDetail.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatDateTime(productDetail.updatedAt) }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 文档管理区域 -->
      <a-card title="要件文档" class="documents-card" :bordered="false">
        <AttachmentManagement
          :product-id="productId"
          :documents="documents"
          :loading="documentsLoading"
          @refresh="fetchProductDetail"
          @upload-success="fetchProductDetail"
          @document-updated="handleDocumentUpdated"
          @document-deleted="handleDocumentDeleted"
        />
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined
} from '@ant-design/icons-vue'
import type { ProductResponse } from '@/api/product'
import type { Document } from '@/types/product'
import { getProduct } from '@/api/product'
import { formatDateTime } from '@/utils/format'
import AttachmentManagement from '@/components/AttachmentManagement.vue'

// 路由相关
const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id as string)

// 响应式数据
const loading = ref(false)
const documentsLoading = ref(false)
const error = ref<string>('')
const productDetail = ref<ProductResponse | null>(null)
const documents = ref<Document[]>([])

// 生命周期
onMounted(() => {
  if (productId.value) {
    fetchProductDetail()
  }
})

// 获取产品详情
const fetchProductDetail = async () => {
  try {
    loading.value = true
    error.value = ''

    const response = await getProduct(productId.value)
    if (response.success) {
      productDetail.value = response.data
      // TODO: 获取文档列表，现在使用mock数据
      documents.value = mockDocuments
    } else {
      error.value = response.message || '获取产品详情失败'
    }
  } catch (err: any) {
    error.value = err.message || '网络请求失败'
    console.error('Error fetching product detail:', err)
  } finally {
    loading.value = false
  }
}

// Mock文档数据（实际应从后端获取）
const mockDocuments: Document[] = [
  {
    id: '1',
    fileName: '保险条款.pdf',
    filePath: '/documents/terms_001.pdf',
    fileSize: 2048576,
    fileType: 'pdf',
    documentType: 'TERMS',
    productId: productId.value,
    uploadedAt: '2024-01-15 10:30:00',
    auditResults: []
  },
  {
    id: '2',
    fileName: '可行性分析报告.docx',
    filePath: '/documents/feasibility_002.docx',
    fileSize: 1536000,
    fileType: 'docx',
    documentType: 'FEASIBILITY_REPORT',
    productId: productId.value,
    uploadedAt: '2024-01-15 11:15:00',
    auditResults: []
  }
]

// 事件处理
const handleGoBack = () => {
  router.back()
}

const handleDocumentUpdated = (document: Document) => {
  // 更新文档列表中的文档
  const index = documents.value.findIndex(doc => doc.id === document.id)
  if (index !== -1) {
    documents.value[index] = document
  }
  message.success('文档已更新')
}

const handleDocumentDeleted = (documentId: string) => {
  // 从文档列表中移除已删除的文档
  documents.value = documents.value.filter(doc => doc.id !== documentId)
}

// 工具函数
const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'DRAFT': 'default',
    'SUBMITTED': 'processing',
    'APPROVED': 'success',
    'REJECTED': 'error'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'APPROVED': '已通过',
    'REJECTED': '已驳回'
  }
  return textMap[status] || status
}

const getAuditStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    '未检核': 'default',
    '检核中': 'processing',
    '检核完成': 'success',
    '检核失败': 'error'
  }
  return colorMap[status] || 'default'
}
</script>

<style scoped lang="scss">
.product-detail-view {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .back-btn {
    padding: 4px 8px;
    font-size: 14px;
    color: #1890ff;

    &:hover {
      background-color: #e6f7ff;
    }
  }

  .breadcrumb {
    :deep(.ant-breadcrumb-link) {
      color: #666;
    }
  }
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;

  .loading-content {
    width: 100%;
    height: 200px;
  }
}

.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-header-card {
  .product-title-section {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;

    .product-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #262626;
      line-height: 1.4;
    }

    .product-status {
      display: flex;
      gap: 8px;
      flex-shrink: 0;

      .status-tag,
      .audit-status-tag {
        margin: 0;
        font-size: 13px;
        padding: 4px 12px;
        border-radius: 16px;
      }
    }
  }

  :deep(.ant-descriptions-item-label) {
    font-weight: 500;
    color: #595959;
    width: 120px;
  }

  :deep(.ant-descriptions-item-content) {
    color: #262626;
  }
}

.documents-card {
  :deep(.ant-card-head-title) {
    font-size: 16px;
    font-weight: 600;
  }
}

// 响应式布局
@media (max-width: 768px) {
  .product-detail-view {
    padding: 16px;
  }

  .page-header .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .product-header-card .product-title-section {
    flex-direction: column;
    gap: 12px;

    .product-status {
      align-self: flex-start;
    }
  }

  :deep(.ant-descriptions) {
    .ant-descriptions-item {
      padding-bottom: 12px;
    }
  }
}
</style>