import type { RouteRecordRaw } from "vue-router"

const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: "/dashboard",
    name: "Dashboard",
    component: () => import("@/views/dashboard/Dashboard.vue"),
    meta: {
      title: "工作台",
      requiresAuth: true,
      icon: "dashboard",
      keepAlive: true,
    },
  },
  {
    path: "/dashboard/home",
    name: "DashboardHome",
    component: () => import("@/views/dashboard/Home.vue"),
    meta: {
      title: "主页",
      requiresAuth: true,
      icon: "home",
      keepAlive: true,
    },
  },
  {
    path: "/rules/create",
    name: "RulesCreate",
    component: () => import("@/views/rules/Create.vue"),
    meta: {
      title: "创建规则",
      requiresAuth: true,
      icon: "rule-add",
    },
  },
  {
    path: "/template/download",
    name: "TemplateDownload",
    component: () => import("@/views/template/Download.vue"),
    meta: {
      title: "下载模板",
      requiresAuth: true,
      icon: "download",
    },
  },
]

export default dashboardRoutes
