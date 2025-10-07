import type { RouteRecordRaw } from 'vue-router'

/**
 * 产品管理模块路由配置
 *
 * 路由层级结构:
 * - /product-management - 产品管理主页 (产品列表)
 * - /product-management/new - 新增产品页面
 * - /product-management/success - 产品提交成功页面
 * - /product/detail/:id - 产品详情页面
 * - /product/import - 产品导入页面
 * - /product/import/batch - 批量导入页面
 */

const productRoutes: RouteRecordRaw[] = [
  // 产品管理主界面
  {
    path: '/product-management',
    name: 'ProductManagement',
    component: () => import('@/views/ProductManagementView.vue'),
    meta: {
      title: '产品管理',
      requiresAuth: true,
      icon: 'project',
      keepAlive: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理' }
      ]
    }
  },

  // 新增产品页面
  {
    path: '/product-management/new',
    name: 'ProductNew',
    component: () => import('@/views/product/Add.vue'),
    meta: {
      title: '新增产品',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '新增产品' }
      ]
    }
  },

  // 产品提交成功页面
  {
    path: '/product-management/success',
    name: 'ProductSubmissionSuccess',
    component: () => import('@/views/ProductSubmissionSuccess.vue'),
    meta: {
      title: '提交成功',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '提交成功' }
      ]
    }
  },

  // 产品详情页面
  {
    path: '/product/detail/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/Detail.vue'),
    meta: {
      title: '产品详情',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '产品详情' }
      ]
    }
  },

  // 产品导入页面
  {
    path: '/product/import',
    name: 'ProductImport',
    component: () => import('@/views/product/Import.vue'),
    meta: {
      title: '导入产品',
      requiresAuth: true,
      icon: 'upload',
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '导入产品' }
      ]
    }
  },

  // 批量导入页面
  {
    path: '/product/import/batch',
    name: 'ProductBatchImport',
    component: () => import('@/views/product/ImportBatch.vue'),
    meta: {
      title: '批量导入',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '导入产品', path: '/product/import' },
        { title: '批量导入' }
      ]
    }
  },

  // 产品编辑页面 (预留)
  {
    path: '/product/edit/:id',
    name: 'ProductEdit',
    component: () => import('@/views/product/Edit.vue'),
    meta: {
      title: '编辑产品',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '编辑产品' }
      ]
    }
  },

  // 产品复制页面 (预留)
  {
    path: '/product/copy/:id',
    name: 'ProductCopy',
    component: () => import('@/views/product/Copy.vue'),
    meta: {
      title: '复制产品',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '复制产品' }
      ]
    }
  },

  // 产品审核页面 (预留)
  {
    path: '/product/audit/:id',
    name: 'ProductAudit',
    component: () => import('@/views/product/Audit.vue'),
    meta: {
      title: '产品审核',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '产品审核' }
      ]
    }
  },

  // 审核结果页面 (预留)
  {
    path: '/product/audit/:id/results',
    name: 'ProductAuditResults',
    component: () => import('@/views/product/AuditResults.vue'),
    meta: {
      title: '审核结果',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '产品管理', path: '/product-management' },
        { title: '产品审核', path: '/product/audit/:id' },
        { title: '审核结果' }
      ]
    }
  }
]

export default productRoutes

/**
 * 产品管理路由导航工具函数
 */
export const productNavigationHelpers = {
  /**
   * 跳转到产品管理主页
   */
  goToProductManagement: () => '/product-management',

  /**
   * 跳转到新增产品页面
   */
  goToProductNew: () => '/product-management/new',

  /**
   * 跳转到产品详情页面
   * @param productId 产品ID
   */
  goToProductDetail: (productId: string) => `/product/detail/${productId}`,

  /**
   * 跳转到产品编辑页面
   * @param productId 产品ID
   */
  goToProductEdit: (productId: string) => `/product/edit/${productId}`,

  /**
   * 跳转到产品复制页面
   * @param productId 产品ID
   */
  goToProductCopy: (productId: string) => `/product/copy/${productId}`,

  /**
   * 跳转到产品审核页面
   * @param productId 产品ID
   */
  goToProductAudit: (productId: string) => `/product/audit/${productId}`,

  /**
   * 跳转到审核结果页面
   * @param productId 产品ID
   */
  goToProductAuditResults: (productId: string) => `/product/audit/${productId}/results`,

  /**
   * 跳转到产品导入页面
   */
  goToProductImport: () => '/product/import',

  /**
   * 跳转到批量导入页面
   */
  goToProductBatchImport: () => '/product/import/batch',

  /**
   * 跳转到产品提交成功页面
   */
  goToProductSuccess: () => '/product-management/success'
}

/**
 * 根据当前路由获取面包屑导航
 * @param routeName 路由名称
 * @param params 路由参数
 * @returns 面包屑导航数组
 */
export const getProductBreadcrumb = (routeName: string, params?: Record<string, any>) => {
  const route = productRoutes.find(r => r.name === routeName)
  if (!route?.meta?.breadcrumb) {
    return []
  }

  // 处理动态路由参数
  return route.meta.breadcrumb.map((item: any) => {
    if (item.path && params && item.path.includes(':')) {
      // 替换路由参数
      let path = item.path
      Object.keys(params).forEach(key => {
        path = path.replace(`:${key}`, params[key])
      })
      return { ...item, path }
    }
    return item
  })
}
