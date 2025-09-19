<template>
  <div class="product-management">
    <!-- 页面标题和返回按钮 -->
    <div class="page-header">
      <div class="header-left">
        <a-button 
          type="link" 
          class="back-button"
          @click="handleBack"
        >
          <template #icon>
            <ArrowLeftOutlined />
          </template>
          返回
        </a-button>
        <h1 class="page-title">产品管理</h1>
      </div>
      <div class="header-right">
        <a-button 
          type="primary"
          @click="handleImportProduct"
        >
          <template #icon>
            <PlusOutlined />
          </template>
          导入产品
        </a-button>
      </div>
    </div>

    <!-- 产品操作功能组件 -->
    <ProductOperationsComponent
      ref="productOperationsRef"
      :selected-products="selectedProducts"
      @refresh="handleRefresh"
      @batch-operation="handleBatchOperation"
      @import-complete="handleImportComplete"
      @advanced-search="handleAdvancedSearch"
    />

    <!-- 产品列表组件 -->
    <ProductListComponent
      ref="productListRef"
      @view-detail="handleViewDetail"
      @refresh="handleRefresh"
      @select-product="handleSelectProduct"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import ProductListComponent from '@/components/ProductListComponent.vue'
import ProductOperationsComponent from '@/components/ProductOperationsComponent.vue'
import type { ProductInfo } from '@/api/modules/product'

// 路由
const router = useRouter()

// 组件引用
const productListRef = ref<InstanceType<typeof ProductListComponent>>()
const productOperationsRef = ref<InstanceType<typeof ProductOperationsComponent>>()

// 选中的产品
const selectedProducts = ref<string[]>([])

// 事件处理方法
const handleBack = () => {
  router.push('/dashboard/home')
}

const handleImportProduct = () => {
  productOperationsRef.value?.openImportDialog()
}

const handleViewDetail = (product: ProductInfo) => {
  router.push(`/product/detail/${product.id}`)
}

const handleRefresh = () => {
  productListRef.value?.refresh()
  message.success('产品列表已刷新')
}

const handleSelectProduct = (product: ProductInfo) => {
  // 处理产品选择
  console.log('Selected product:', product)
}

const handleBatchOperation = (operation: string, products: string[]) => {
  switch (operation) {
    case 'audit':
      if (products.length === 0) {
        message.warning('请先选择要检核的产品')
        return
      }
      // 导航到检核规则选择页面，传递选中的产品ID
      router.push({
        path: '/audit/rule-selection',
        query: {
          productIds: products.join(','),
          fromPage: 'product-management'
        }
      })
      break
    case 'export':
      message.info(`导出 ${products.length} 个产品的数据`)
      break
    case 'download':
      message.info(`下载 ${products.length} 个产品的文档`)
      break
    case 'delete':
      message.warning(`确认删除 ${products.length} 个产品？`)
      break
    default:
      message.info(`批量操作: ${operation}, 产品数量: ${products.length}`)
  }
}

const handleImportComplete = (result: any) => {
  message.success('产品导入完成')
  handleRefresh()
}

const handleAdvancedSearch = (filters: any) => {
  message.info('应用高级搜索条件')
  // 这里可以将筛选条件传递给ProductListComponent
  console.log('Advanced search filters:', filters)
}
</script>

<style scoped lang="scss">
.product-management {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;

      .back-button {
        padding: 4px 8px;
        margin-right: 12px;
        color: #1890ff;

        &:hover {
          background: #f0f9ff;
        }
      }

      .page-title {
        font-size: 20px;
        font-weight: 600;
        color: #262626;
        margin: 0;
      }
    }
  }
}
</style>