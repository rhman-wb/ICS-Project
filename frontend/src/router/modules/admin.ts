import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    name: 'AdminLayout',
    redirect: '/admin/users',
    meta: {
      title: '系统管理',
      requiresAuth: true,
      roles: ['admin'],
      icon: 'setting'
    },
    children: [
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          roles: ['admin'],
          permissions: ['admin:users:view']
        }
      },
      {
        path: 'system',
        name: 'AdminSystem',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: {
          title: '系统配置',
          requiresAuth: true,
          roles: ['admin'],
          permissions: ['admin:system:view']
        }
      }
    ]
  }
]

export default adminRoutes