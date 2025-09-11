import type { RouteRecordRaw } from 'vue-router'

const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue'),
    meta: {
      title: '工作台',
      requiresAuth: true,
      icon: 'dashboard',
      keepAlive: true
    }
  },
  {
    path: '/dashboard/home',
    name: 'DashboardHome',
    component: () => import('@/views/dashboard/Home.vue'),
    meta: {
      title: '主页',
      requiresAuth: true,
      icon: 'home',
      keepAlive: true
    }
  },
  {
    path: '/product/import',
    name: 'ProductImport',
    component: () => import('@/views/product/Import.vue'),
    meta: {
      title: '导入产品',
      requiresAuth: true,
      icon: 'upload'
    }
  },
  {
    path: '/product/import/batch',
    name: 'ProductBatchImport',
    component: () => import('@/views/product/ImportBatch.vue'),
    meta: {
      title: '批量导入',
      requiresAuth: true,
      icon: 'batch-upload'
    }
  },
  {
    path: '/product/detail/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/Detail.vue'),
    meta: {
      title: '产品详情',
      requiresAuth: true,
      icon: 'detail'
    }
  },
  {
    path: '/rules/create',
    name: 'RulesCreate',
    component: () => import('@/views/rules/Create.vue'),
    meta: {
      title: '创建规则',
      requiresAuth: true,
      icon: 'rule-add'
    }
  },
  {
    path: '/template/download',
    name: 'TemplateDownload',
    component: () => import('@/views/template/Download.vue'),
    meta: {
      title: '下载模板',
      requiresAuth: true,
      icon: 'download'
    }
  }
]

export default dashboardRoutes