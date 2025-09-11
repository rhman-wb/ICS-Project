import type { RouteRecordRaw } from 'vue-router'

const userRoutes: RouteRecordRaw[] = [
  {
    path: '/user',
    name: 'UserLayout',
    redirect: '/user/profile',
    meta: {
      title: '用户管理',
      requiresAuth: true,
      icon: 'user'
    },
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: {
          title: '个人资料',
          requiresAuth: true,
          permissions: ['user:profile:view']
        }
      },
      {
        path: 'settings',
        name: 'UserSettings',
        component: () => import('@/views/user/UserSettings.vue'),
        meta: {
          title: '用户设置',
          requiresAuth: true,
          permissions: ['user:settings:view']
        }
      }
    ]
  }
]

export default userRoutes