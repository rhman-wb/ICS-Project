/**
 * 检核模块路由配置
 * 提供检核功能的路由导航支持
 */

import type { RouteRecordRaw } from 'vue-router'

const auditRoutes: RouteRecordRaw[] = [
  {
    path: '/audit',
    name: 'Audit',
    meta: {
      title: '智能检核',
      requiresAuth: true,
      icon: 'AuditOutlined'
    },
    redirect: '/audit/rule-selection',
    children: [
      {
        path: 'rule-selection',
        name: 'AuditRuleSelection',
        component: () => import('@/views/audit/RuleSelection.vue'),
        meta: {
          title: '规则选择',
          requiresAuth: true,
          breadcrumb: [
            { title: '首页', path: '/' },
            { title: '产品管理', path: '/product' },
            { title: '智能检核', path: '/audit' },
            { title: '规则选择' }
          ]
        }
      },
      {
        path: 'process-monitor',
        name: 'AuditProcessMonitor',
        component: () => import('@/views/audit/ProcessMonitor.vue'),
        meta: {
          title: '检核监控',
          requiresAuth: true,
          breadcrumb: [
            { title: '首页', path: '/' },
            { title: '产品管理', path: '/product' },
            { title: '智能检核', path: '/audit' },
            { title: '检核监控' }
          ]
        }
      },
      {
        path: 'results',
        name: 'AuditResults',
        component: () => import('@/views/audit/ResultDisplay.vue'),
        meta: {
          title: '检核结果',
          requiresAuth: true,
          breadcrumb: [
            { title: '首页', path: '/' },
            { title: '产品管理', path: '/product' },
            { title: '智能检核', path: '/audit' },
            { title: '检核结果' }
          ]
        }
      },
      {
        path: 'history',
        name: 'AuditHistory',
        component: () => import('@/views/audit/HistoryManager.vue'),
        meta: {
          title: '历史记录',
          requiresAuth: true,
          breadcrumb: [
            { title: '首页', path: '/' },
            { title: '产品管理', path: '/product' },
            { title: '智能检核', path: '/audit' },
            { title: '历史记录' }
          ]
        }
      }
    ]
  }
]

export default auditRoutes