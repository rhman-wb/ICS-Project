<template>
  <div class="product-navigation">
    <!-- 导航头部 -->
    <div class="navigation-header">
      <!-- 返回按钮 -->
      <div class="back-button-container">
        <a-button
          type="text"
          :icon="h(ArrowLeftOutlined)"
          class="back-btn"
          @click="handleGoBack"
        >
          {{ backButtonText }}
        </a-button>
      </div>

      <!-- 面包屑导航 -->
      <div class="breadcrumb-container">
        <a-breadcrumb class="breadcrumb">
          <a-breadcrumb-item
            v-for="(item, index) in breadcrumbItems"
            :key="index"
          >
            <router-link
              v-if="item.path && index < breadcrumbItems.length - 1"
              :to="item.path"
              class="breadcrumb-link"
            >
              <component
                v-if="item.icon"
                :is="item.icon"
                class="breadcrumb-icon"
              />
              {{ item.title }}
            </router-link>
            <span v-else class="breadcrumb-current">
              <component
                v-if="item.icon"
                :is="item.icon"
                class="breadcrumb-icon"
              />
              {{ item.title }}
            </span>
          </a-breadcrumb-item>
        </a-breadcrumb>
      </div>

      <!-- 操作区域 -->
      <div class="actions-container" v-if="$slots.actions">
        <slot name="actions"></slot>
      </div>
    </div>

    <!-- 页面标题区域 -->
    <div class="page-title-section" v-if="showTitle">
      <div class="title-content">
        <h1 class="page-title">
          <component
            v-if="titleIcon"
            :is="titleIcon"
            class="title-icon"
          />
          {{ pageTitle }}
        </h1>
        <div class="title-extra" v-if="$slots.titleExtra">
          <slot name="titleExtra"></slot>
        </div>
      </div>
      <div class="title-description" v-if="pageDescription">
        {{ pageDescription }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeftOutlined,
  HomeOutlined,
  ProjectOutlined,
  FileTextOutlined,
  UploadOutlined,
  AuditOutlined,
  EditOutlined,
  CopyOutlined
} from '@ant-design/icons-vue'
import { getProductBreadcrumb } from '@/router/product-routes'

interface BreadcrumbItem {
  title: string
  path?: string
  icon?: any
}

interface Props {
  // 自定义面包屑，如果不提供则自动根据路由生成
  customBreadcrumb?: BreadcrumbItem[]
  // 返回按钮文本
  backButtonText?: string
  // 是否显示页面标题
  showTitle?: boolean
  // 页面标题
  pageTitle?: string
  // 页面描述
  pageDescription?: string
  // 标题图标
  titleIcon?: any
  // 自定义返回行为
  customBackAction?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  backButtonText: '返回',
  showTitle: true
})

const route = useRoute()
const router = useRouter()

// 计算面包屑导航
const breadcrumbItems = computed<BreadcrumbItem[]>(() => {
  // 如果提供了自定义面包屑，直接使用
  if (props.customBreadcrumb) {
    return props.customBreadcrumb
  }

  // 根据当前路由自动生成面包屑
  const routeName = route.name as string
  const routeParams = route.params

  // 首先尝试从产品路由获取面包屑
  const productBreadcrumb = getProductBreadcrumb(routeName, routeParams)
  if (productBreadcrumb.length > 0) {
    return productBreadcrumb.map(item => ({
      ...item,
      icon: getBreadcrumbIcon(item.title)
    }))
  }

  // 默认面包屑生成逻辑
  const items: BreadcrumbItem[] = [
    {
      title: '工作台',
      path: '/dashboard/home',
      icon: HomeOutlined
    }
  ]

  // 根据路由路径添加面包屑
  if (route.path.startsWith('/product-management')) {
    items.push({
      title: '产品管理',
      path: '/product-management',
      icon: ProjectOutlined
    })

    if (route.path.includes('/success')) {
      items.push({ title: '提交成功' })
    } else if (route.path.includes('/new')) {
      items.push({ title: '新增产品' })
    }
  } else if (route.path.startsWith('/product/')) {
    items.push({
      title: '产品管理',
      path: '/product-management',
      icon: ProjectOutlined
    })

    if (route.path.includes('/detail/')) {
      items.push({ title: '产品详情', icon: FileTextOutlined })
    } else if (route.path.includes('/edit/')) {
      items.push({ title: '编辑产品', icon: EditOutlined })
    } else if (route.path.includes('/copy/')) {
      items.push({ title: '复制产品', icon: CopyOutlined })
    } else if (route.path.includes('/audit/')) {
      if (route.path.includes('/results')) {
        items.push({ title: '产品审核', path: `/product/audit/${routeParams.id}`, icon: AuditOutlined })
        items.push({ title: '审核结果' })
      } else {
        items.push({ title: '产品审核', icon: AuditOutlined })
      }
    } else if (route.path.includes('/import')) {
      if (route.path.includes('/batch')) {
        items.push({ title: '导入产品', path: '/product/import', icon: UploadOutlined })
        items.push({ title: '批量导入' })
      } else {
        items.push({ title: '导入产品', icon: UploadOutlined })
      }
    }
  }

  return items
})

// 计算页面标题
const pageTitle = computed(() => {
  if (props.pageTitle) {
    return props.pageTitle
  }

  // 自动从路由元信息获取标题
  return route.meta.title || '页面'
})

// 处理返回操作
const handleGoBack = () => {
  if (props.customBackAction) {
    props.customBackAction()
  } else {
    router.back()
  }
}

// 根据面包屑标题获取对应图标
const getBreadcrumbIcon = (title: string) => {
  const iconMap: Record<string, any> = {
    '工作台': HomeOutlined,
    '产品管理': ProjectOutlined,
    '产品详情': FileTextOutlined,
    '新增产品': ProjectOutlined,
    '编辑产品': EditOutlined,
    '复制产品': CopyOutlined,
    '产品审核': AuditOutlined,
    '审核结果': FileTextOutlined,
    '导入产品': UploadOutlined,
    '批量导入': UploadOutlined,
    '提交成功': FileTextOutlined
  }
  return iconMap[title]
}
</script>

<style scoped lang="scss">
.product-navigation {
  background: white;
  border-bottom: 1px solid #f0f0f0;

  .navigation-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    gap: 16px;

    .back-button-container {
      flex-shrink: 0;
    }

    .back-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      font-size: 14px;
      color: #1890ff;
      border-radius: 6px;
      transition: all 0.3s;

      &:hover {
        background-color: #e6f7ff;
        transform: translateX(-2px);
      }

      &:focus {
        background-color: #e6f7ff;
      }
    }

    .breadcrumb-container {
      flex: 1;
      min-width: 0;
    }

    .breadcrumb {
      margin: 0;

      .breadcrumb-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: #666;
        text-decoration: none;
        transition: color 0.3s;

        &:hover {
          color: #1890ff;
        }

        .breadcrumb-icon {
          font-size: 14px;
        }
      }

      .breadcrumb-current {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: #262626;
        font-weight: 500;

        .breadcrumb-icon {
          font-size: 14px;
        }
      }

      :deep(.ant-breadcrumb-separator) {
        color: #d9d9d9;
        margin: 0 8px;
      }
    }

    .actions-container {
      flex-shrink: 0;
    }
  }

  .page-title-section {
    padding: 0 24px 16px;

    .title-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;

      .page-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #262626;
        line-height: 1.4;
        display: flex;
        align-items: center;
        gap: 8px;

        .title-icon {
          font-size: 20px;
          color: #1890ff;
        }
      }

      .title-extra {
        flex-shrink: 0;
      }
    }

    .title-description {
      color: #666;
      font-size: 14px;
      line-height: 1.5;
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .product-navigation {
    .navigation-header {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;
      padding: 12px 16px;

      .back-button-container {
        align-self: flex-start;
      }

      .breadcrumb-container {
        order: 1;
      }

      .actions-container {
        order: 2;
        align-self: flex-end;
      }
    }

    .page-title-section {
      padding: 0 16px 12px;

      .title-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;

        .page-title {
          font-size: 18px;
        }

        .title-extra {
          align-self: flex-end;
        }
      }
    }
  }
}

// 高对比度模式支持
@media (prefers-contrast: high) {
  .product-navigation {
    border-bottom: 2px solid #000;

    .back-btn {
      border: 1px solid #1890ff;

      &:hover {
        border-color: #0050b3;
      }
    }

    .breadcrumb {
      .breadcrumb-link {
        &:hover {
          color: #0050b3;
          text-decoration: underline;
        }
      }
    }
  }
}

// 减少动画模式支持
@media (prefers-reduced-motion: reduce) {
  .product-navigation {
    .back-btn {
      transition: none;

      &:hover {
        transform: none;
      }
    }

    .breadcrumb-link {
      transition: none;
    }
  }
}
</style>