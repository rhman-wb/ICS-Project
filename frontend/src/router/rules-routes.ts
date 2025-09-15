import type { RouteRecordRaw } from 'vue-router'

/**
 * 规则配置系统模块路由配置
 *
 * 路由层级结构:
 * - /rules - 规则管理主页 (规则列表)
 * - /rules/new - 新增规则页面 (支持type参数: single|double|format|advanced)
 * - /rules/success - 规则提交成功页面
 * - /rules/jump - 检核跳转页面 (规则选择)
 */

const rulesRoutes: RouteRecordRaw[] = [
  // 规则管理主界面
  {
    path: '/rules',
    name: 'RulesManagement',
    component: () => import('@/views/rules/RulesListView.vue'),
    meta: {
      title: '规则管理',
      requiresAuth: true,
      icon: 'setting',
      keepAlive: true,
      permissions: ['RULE_VIEW'],
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '规则管理' }
      ]
    }
  },

  // 新增规则页面 - 主页面带Tab
  {
    path: '/rules/new',
    name: 'RuleCreate',
    component: () => import('@/views/rules/RuleCreateView.vue'),
    meta: {
      title: '新增规则',
      requiresAuth: true,
      hideInMenu: true,
      permissions: ['RULE_CREATE'],
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '规则管理', path: '/rules' },
        { title: '新增规则' }
      ]
    }
  },

  // 规则提交成功页面
  {
    path: '/rules/success',
    name: 'RuleSubmissionSuccess',
    component: () => import('@/views/rules/RuleSuccessView.vue'),
    meta: {
      title: '提交成功',
      requiresAuth: true,
      hideInMenu: true,
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '规则管理', path: '/rules' },
        { title: '提交成功' }
      ]
    }
  },

  // 检核跳转页面 (规则选择)
  {
    path: '/rules/jump',
    name: 'RuleJump',
    component: () => import('@/views/rules/RuleJumpView.vue'),
    meta: {
      title: '检核跳转',
      requiresAuth: true,
      hideInMenu: true,
      permissions: ['RULE_VIEW'],
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '规则管理', path: '/rules' },
        { title: '检核跳转' }
      ]
    }
  },

  // 规则详情页面
  {
    path: '/rules/detail/:id',
    name: 'RuleDetail',
    component: () => import('@/views/rules/RuleDetailView.vue'),
    meta: {
      title: '规则详情',
      requiresAuth: true,
      hideInMenu: true,
      permissions: ['RULE_VIEW'],
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '规则管理', path: '/rules' },
        { title: '规则详情' }
      ]
    }
  },

  // 规则编辑页面
  {
    path: '/rules/edit/:id',
    name: 'RuleEdit',
    component: () => import('@/views/rules/RuleEditView.vue'),
    meta: {
      title: '编辑规则',
      requiresAuth: true,
      hideInMenu: true,
      permissions: ['RULE_EDIT'],
      breadcrumb: [
        { title: '工作台', path: '/dashboard/home' },
        { title: '规则管理', path: '/rules' },
        { title: '编辑规则' }
      ]
    }
  }
]

export default rulesRoutes

/**
 * 规则管理路由导航工具函数
 */
export const rulesNavigationHelpers = {
  /**
   * 跳转到规则管理主页
   */
  goToRulesManagement: () => '/rules',

  /**
   * 跳转到新增规则页面
   * @param type 规则类型: single|double|format|advanced
   */
  goToRuleCreate: (type?: 'single' | 'double' | 'format' | 'advanced') => {
    if (type) {
      return `/rules/new?type=${type}`
    }
    return '/rules/new'
  },

  /**
   * 跳转到规则详情页面
   * @param ruleId 规则ID
   */
  goToRuleDetail: (ruleId: string) => `/rules/detail/${ruleId}`,

  /**
   * 跳转到规则编辑页面
   * @param ruleId 规则ID
   */
  goToRuleEdit: (ruleId: string) => `/rules/edit/${ruleId}`,

  /**
   * 跳转到规则提交成功页面
   */
  goToRuleSuccess: () => '/rules/success',

  /**
   * 跳转到检核跳转页面
   */
  goToRuleJump: () => '/rules/jump'
}

/**
 * 规则类型枚举
 */
export enum RuleType {
  SINGLE = 'single',
  DOUBLE = 'double',
  FORMAT = 'format',
  ADVANCED = 'advanced'
}

/**
 * 规则类型配置
 */
export const ruleTypeConfig = {
  [RuleType.SINGLE]: {
    label: '单句规则',
    description: '基于单个条件的简单规则'
  },
  [RuleType.DOUBLE]: {
    label: '双句规则',
    description: '基于两个条件的复合规则'
  },
  [RuleType.FORMAT]: {
    label: '格式规则',
    description: '基于格式验证的规则'
  },
  [RuleType.ADVANCED]: {
    label: '高级规则',
    description: '基于复杂逻辑的高级规则'
  }
}

/**
 * 根据当前路由获取面包屑导航
 * @param routeName 路由名称
 * @param params 路由参数
 * @returns 面包屑导航数组
 */
export const getRulesBreadcrumb = (routeName: string, params?: Record<string, any>) => {
  const route = rulesRoutes.find(r => r.name === routeName)
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