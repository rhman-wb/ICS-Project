# Tasks Document

- [x] 1. 集成视口与图标字体（index.html）
  - File: frontend/index.html
  - 添加固定视口：`<meta name="viewport" content="width=1440, user-scalable=no">`
  - 引入 Font Awesome 4.7：`<link href="http://cdn.bootcss.com/font-awesome/4.7.0/css/font-awesome.css" rel="stylesheet" />`
  - 目的：确保与原型一致的视口与图标渲染（方块/人形/放大镜等）
  - _Leverage: 现有 SPA 壳文件 frontend/index.html_
  - _Requirements: 8, 9, 10, 11, 12, 13, 14, 15（布局/图标/文案一致）_
  - _Prompt: Role: 前端工程师（SPA 壳集成） | Task: 在 frontend/index.html 按上述要求新增 meta 与 Font Awesome，保证图标可用、视口固定为 1440 宽 | Restrictions: 不要移除既有 meta 与脚本，避免破坏 Vite 注入；仅添加必要标签 | Success: 本地启动后字体图标正确显示，页面宽度呈现与原型一致的版心布局_

- [x] 2. 新建 Mock 数据（home.ts）
  - File: frontend/src/api/mock/home.ts
  - 定义只读 mock 对象，字段与数值严格对齐原型与需求（导航/概要/我的关注/图表/快速开始/近期动态/汇总数据）
  - 目的：为所有组件提供静态数据源（MVP 不接后端）
  - _Leverage: frontend/src/api/mock/ 目录已存在_
  - _Requirements: 全部（MVP 使用 mock 数据）_
  - _Prompt: Role: TypeScript Mock 数据工程师 | Task: 在 home.ts 内导出 `homeMock` 常量（含 topNav、appOverview、myFavorites、ratioChart、quickStart、recentActivities、summaryTotals），数值与文案100%匹配原型/需求 | Restrictions: 仅导出常量，不要包含异步/副作用；类型定义内联或单文件接口均可 | Success: 组件可直接消费 `homeMock` 渲染，数字/文案/百分比之和=100%

- [x] 3. 新建组合函数（useHomeMock.ts）
  - File: frontend/src/composables/useHomeMock.ts
  - 暴露 `useHomeMock()` 返回 `homeMock` 引用（可使用 shallowRef/readonly）
  - 目的：统一数据入口，后续替换为 API 时不影响视图层
  - _Leverage: 现有 composables 模式 frontend/src/composables/_
  - _Requirements: 设计文档 Architecture/Data 模块化_
  - _Prompt: Role: Vue 组合式专家 | Task: 实现 `useHomeMock()` 并导出，返回只读的 mock 数据对象 | Restrictions: 不要引入外部副作用；不做网络请求；仅组织数据访问 | Success: 各组件通过该函数获取数据并正确渲染

- [x] 4. 路由配置与入口跳转
  - Files: frontend/src/router/index.ts；（如使用模块）frontend/src/router/modules/dashboard.ts
  - 新增 `DashboardHome` 路由作为 `/` 落地页；若已有根路由，配置 redirect 至 `DashboardHome`
  - 为快捷入口准备占位路由：`/product/import`、`/rules/create`、`/product/import/batch`、`/templates/download`（可用简单占位组件）；产品详情：`/product/detail/:id`
  - 目的：满足导航与链接跳转场景（MVP 用占位）
  - _Leverage: 现有 router 结构 frontend/src/router/_
  - _Requirements: 8.7, 10.5, 12.3~12.6, 13.6_
  - _Prompt: Role: 前端路由工程师 | Task: 在路由中新增/修改对应路径，确保 `/` 进入 DashboardHome，快捷入口与产品链接均可路由至占位页 | Restrictions: 不破坏既有守卫；模块化场景按既有风格新增模块文件 | Success: 启动后直接进入工作台；点击各链接能进入对应占位路径

- [x] 5. 创建页面骨架（Home.vue）
  - File: frontend/src/views/dashboard/Home.vue
  - 组合布局：左列≈1100px，右列≈552px；区块顺序与原型一致：应用概要（左上）、数量统计占比（右上）、快速开始（右中）、近期动态（右下）、我的关注（左中大容器）、汇总数据（底部）
  - 目的：承载各业务组件与像素级布局
  - _Leverage: 设计文档中的组件拓扑_
  - _Requirements: 8~15（整体布局/区域顺序与样式）_
  - _Prompt: Role: Vue 页面开发工程师 | Task: 编写 Home.vue，使用 `<script setup lang="ts">` 与 `<style scoped lang="scss">`，布局采用精确 px，颜色/字号按需求文档 | Restrictions: 不引入沉重 UI 布局组件影响像素；必要处使用自定义样式 | Success: 组件占位与间距与原型严格一致

- [x] 6. 创建组件：TopNav.vue
  - File: frontend/src/components/dashboard/TopNav.vue
  - 功能：深蓝导航条（#001529）+ 品牌“产品检核系统” + 菜单（主页/产品管理/规则管理/设置）高亮当前 + 右侧用户名“Jay.Liu”
  - Props：`brand, menus, active, username`
  - _Requirements: 8（导航）_
  - _Prompt: Role: 前端组件工程师 | Task: 实现顶部导航，传入 props 渲染并高亮“主页”；点击菜单触发 `emit('navigate', key)` | Restrictions: 颜色/高度/间距与原型一致；使用 Font Awesome 字体类 | Success: 视觉与交互与原型一致，切换不影响布局

- [x] 7. 创建组件：AppOverview.vue
  - File: frontend/src/components/dashboard/AppOverview.vue
  - 功能：8项概要统计（含同比箭头红#F95E5A/绿#4BD863）
  - Props：`overview`（包含数值与趋势字段）
  - _Requirements: 14（应用概要）_
  - _Prompt: Role: 数据可视组件工程师 | Task: 渲染 8 张小卡片，数字 20px、标签 14px，与原型文案及数值完全一致 | Restrictions: 不使用库样式导致间距偏移；精确对齐 | Success: 数字与箭头颜色/方向正确，卡片阴影/圆角与原型一致

- [x] 8. 创建组件：MyFavorites.vue
  - File: frontend/src/components/dashboard/MyFavorites.vue
  - 功能：标题“我的关注”，上方搜索输入（占位“请输入任务名称”+放大镜），两个选择器（“按时间”“全部任务”），下方滚动任务列表（含图标、名称链接、类型、提交人“胡潇禹”、提交时间“2022/10/31”）
  - Props：`items, filters`
  - _Requirements: 10（列表）+ 11（搜索筛选）_
  - _Prompt: Role: 列表组件工程师 | Task: 实现搜索输入与两个下拉的外观，列表行渲染与滚动容器，名称为可点击链接 | Restrictions: MVP 可不实现真实过滤；行高/分隔与原型一致 | Success: 列表可滚动，行样式、图标与链接样式与原型一致

- [x] 9. 创建组件：RatioChart.vue
  - File: frontend/src/components/dashboard/RatioChart.vue
  - 功能：标题“数量统计占比”，选项卡（产品管理部门/产品类型/规则类型；默认产品类型），饼图 + 右侧类型列表（种植险36%1010、意健险20%561、车险16%445、家财险10%280、养殖险9%280、其他9%280）
  - Props：`tabs, active, productTypes`
  - _Requirements: 15（占比图表）_
  - _Prompt: Role: 图表工程师（ECharts） | Task: 使用 ECharts 静态数据渲染饼图并与右侧列表颜色一一对应；若不引入 ECharts，使用静态图片保证观感 | Restrictions: 百分比求和=100%；颜色清晰可区分；布局不漂移 | Success: 颜色、标签、列表数值与原型一致，切换标签可保持静态数据

- [x] 10. 创建组件：QuickStart.vue
  - File: frontend/src/components/dashboard/QuickStart.vue
  - 功能：六个按钮（导入产品/创建规则/批量导入/下载模板/操作四/操作五），其中两个显示“24”徽章（右上角）
  - Props：`items`（含 route 与 badge）
  - _Requirements: 12（快速开始）_
  - _Prompt: Role: 操作入口组件工程师 | Task: 实现按钮与徽章定位；点击路由跳转或触发下载 | Restrictions: 保持与原型相同的字号、间距与排列 | Success: 徽章位置与数字正确，点击跳转生效

- [x] 11. 创建组件：RecentActivities.vue
  - File: frontend/src/components/dashboard/RecentActivities.vue
  - 功能：“近期动态”时间线列表（上传/更新规则/审核通过/配置规则等），产品名为蓝色链接，底部“查看更多”
  - Props：`activities`
  - _Requirements: 13（近期动态）_
  - _Prompt: Role: 时间线组件工程师 | Task: 渲染倒序动态项，格式与原型一致，时间为相对时间字符串 | Restrictions: 保证分隔与行距一致；链接样式蓝色 #0079FE | Success: 可见 6 条样例与“查看更多”，点击链接跳占位详情

- [x] 12. 创建组件：SummaryTotals.vue
  - File: frontend/src/components/dashboard/SummaryTotals.vue
  - 功能：底部“汇总数据”，四指标（产品/要件/规则/已检核错误数量均为20000），时间标签（总计默认高亮，本日/本周/本月可切换选中态）
  - Props：`ranges, activeRange, metrics`
  - _Requirements: 9（汇总数据）_
  - _Prompt: Role: 统计汇总组件工程师 | Task: 渲染四卡片（数字28px、标签14px），时间标签有 selected 样式 | Restrictions: MVP 切换不改数值，仅样式状态 | Success: 视觉与原型一致，默认“总计”高亮

- [-] 13. 图表依赖与静态资源
  - Files: package.json（新增依赖 `echarts`）；frontend/src/assets/images/ratio-chart.png（如走静态图备选）
  - 目的：保证图表区域可渲染或降级
  - _Leverage: 现有构建链 Vite_
  - _Requirements: 15（图表）_
  - _Prompt: Role: 前端依赖工程师 | Task: 安装并按需引入 ECharts；若不装则添加静态图并在组件内选择渲染路径 | Restrictions: 体积控制；仅引入所需模块 | Success: 图表区域正常显示，颜色/布局与列表一致

- [x] 14. “下载模板”资源与点击行为
  - Files: frontend/public/template.xlsx（占位文件）；在 QuickStart 中点击“下载模板”触发下载 `/<base>/template.xlsx`
  - 目的：满足快捷入口的下载功能
  - _Requirements: 12.6_
  - _Prompt: Role: 前端功能工程师 | Task: 提供 public 资源并在点击时触发浏览器下载 | Restrictions: 不依赖后端；兼容现代浏览器 | Success: 点击后浏览器开始下载或弹保存对话框

- [x] 15. 占位页面（可选）
  - Files: frontend/src/views/product/Import.vue；frontend/src/views/rules/Create.vue；frontend/src/views/product/ImportBatch.vue；frontend/src/views/product/Detail.vue
  - 目的：配合路由点击跳转的可见结果
  - _Requirements: 12, 13_
  - _Prompt: Role: 前端页面工程师 | Task: 创建简洁占位页，显示标题与返回链接 | Restrictions: 不实现业务逻辑 | Success: 能通过路由进入并看到标题

- [-] 16. 样式与主题
  - Files: 每组件内 `<style scoped lang="scss">`；可选新增 `frontend/src/assets/styles/dashboard/_tokens.scss`
  - 目的：精确控制像素、颜色、圆角与阴影；集中可复用变量
  - _Leverage: frontend/src/assets/styles/ 现有结构_
  - _Requirements: 视觉与原型一致（字体/字号/颜色/间距）_
  - _Prompt: Role: 前端样式工程师 | Task: 依照原型补齐样式，必要处使用 px 固定；抽取通用 token | Restrictions: 避免全局样式污染；不改变其他页面样式 | Success: 对比原型达到像素级一致

- [x] 17. 单元测试（Vitest）
  - Files: frontend/src/components/__tests__/*.spec.ts（新增多个用例）
  - 覆盖：TopNav 渲染与高亮；AppOverview 数字/箭头；MyFavorites 列表行/链接；RatioChart 列表与颜色映射；QuickStart 徽章与点击；RecentActivities 文案格式；SummaryTotals 选中态
  - _Leverage: 现有 Vitest 配置 frontend/vitest.config.ts_
  - _Requirements: 各功能区渲染正确_
  - _Prompt: Role: QA（单测） | Task: 为各组件编写渲染与关键交互的快照/断言 | Restrictions: 避免依赖真实网络；图表可断言数据结构或占位渲染 | Success: 用例通过且具备基本覆盖率

- [x] 18. 端到端测试（Playwright）
  - Files: frontend/tests/e2e/dashboard-home.spec.ts
  - 场景：登录后进入 `/`；存在“应用概要/数量统计占比/快速开始/近期动态/我的关注/汇总数据”；点击“导入产品/创建规则/批量导入/下载模板”有反馈；产品链接可进入详情占位
  - _Leverage: 现有 e2e 基础 frontend/tests/e2e/_
  - _Requirements: 8~15 关键路径_
  - _Prompt: Role: QA（E2E） | Task: 编写端到端脚本覆盖主要用户路径 | Restrictions: 使用本地 mock；不依赖后端服务 | Success: 在本地跑通并稳定

- [ ] 19. 可访问性与键盘支持（基础）
  - Files: 相关组件内（tabIndex、aria-label、语义标签）
  - 目的：满足需求文档的键盘导航与可读性要求（基础版本）
  - _Requirements: 交互体验/可用性要求_
  - _Prompt: Role: 可访问性工程师 | Task: 为关键交互元素提供键盘可达与语义属性 | Restrictions: 不改变视觉；不引入额外库 | Success: 通过键盘可聚焦并触发主要操作

- [ ] 20. 文档与验收清单
  - Files: Docs/DashboardHome-UAT.md（新增）；记录对照需求的逐条验收项与截图
  - 目的：拉齐需求与实现，便于评审
  - _Requirements: 全部_
  - _Prompt: Role: 技术文档工程师 | Task: 产出 UAT 文档并贴图对照原型 | Restrictions: 简明对照，不赘述实现细节 | Success: 审阅者可据此完成验收
