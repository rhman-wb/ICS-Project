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
  }
]

export default dashboardRoutes