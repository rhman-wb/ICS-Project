# 用户认证与权限管理系统设计文档

## 概述

本设计文档基于用户认证与权限管理系统的需求规格说明书，详细描述了系统的架构设计、组件设计、数据模型、接口设计和技术实现方案。

系统采用前后端分离架构，前端使用Vue 3 + Ant Design Vue构建现代化的用户界面，后端使用Spring Boot + Spring Security + JWT实现安全的认证授权机制。

## MVP阶段实现范围

### 完整实现功能（需求1-7）
- **登录界面展示**: 完整的前后端实现
- **用户登录认证**: 完整的认证逻辑和安全控制
- **输入框交互体验**: 前端交互和后端验证
- **登录按钮交互**: 完整的状态管理和反馈
- **错误提示系统**: 前后端错误处理机制
- **权限控制管理**: 基础的角色权限体系
- **用户会话管理**: JWT令牌管理和会话控制

### 前端展示功能（需求8-15）
- **系统主页面导航**: 仅前端页面展示，使用静态配置
- **工作台数据汇总展示**: 前端组件 + Mock数据
- **我的关注任务列表**: 前端组件 + Mock数据
- **任务搜索与筛选**: 前端交互逻辑 + Mock数据
- **快速开始/便捷导航**: 前端页面跳转（暂不实现具体功能）
- **近期动态展示**: 前端组件 + Mock数据
- **应用概要统计**: 前端组件 + Mock数据
- **数量统计占比图表**: 前端图表组件 + Mock数据

### 暂不实现功能
- 工作台相关的后端业务逻辑
- 实际的产品管理、规则管理等业务模块
- 真实的数据统计和分析功能
- 复杂的权限控制（仅实现基础角色区分）

## 架构设计

### 整体架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用层     │    │   网关层        │    │   后端服务层     │
│                │    │                │    │                │
│ - 登录界面      │    │ - 路由转发      │    │ - 认证服务      │
│ - 工作台界面    │◄──►│ - 负载均衡      │◄──►│ - 权限服务      │
│ - 权限控制      │    │ - 安全过滤      │    │ - 用户管理      │
│ - 状态管理      │    │ - 日志记录      │    │ - 日志服务      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                      │
                                              ┌─────────────────┐
                                              │   数据存储层     │
                                              │                │
                                              │ - MySQL数据库   │
                                              │ - Redis缓存     │
                                              │ - 日志存储      │
                                              └─────────────────┘
```

### 技术栈选择

#### 前端技术栈
- **框架**: Vue 3.4.x (Composition API)
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia 2.x
- **路由管理**: Vue Router 4.x
- **HTTP客户端**: Axios
- **构建工具**: Vite 5.x
- **开发语言**: TypeScript 5.x

#### 后端技术栈
- **框架**: Spring Boot 3.2.x
- **安全框架**: Spring Security 6.x + JWT
- **数据访问**: MyBatis Plus 3.5.x
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.0+
- **开发语言**: Java 17+

### 分层架构设计

#### 前端分层（完整结构）
```
src/
├── main.ts                       # 应用入口
├── App.vue                       # 根组件
├── assets/                       # 静态资源
│   ├── images/                   # 图片资源
│   ├── icons/                    # 图标资源
│   └── styles/                   # 全局样式
│       ├── index.scss            # 样式入口
│       ├── variables.scss        # 变量定义
│       ├── mixins.scss           # 混入函数
│       └── reset.scss            # 样式重置
├── components/                   # 通用组件
│   ├── common/                   # 基础组件
│   │   ├── BaseButton/
│   │   ├── BaseInput/
│   │   ├── BaseTable/
│   │   └── BaseModal/
│   ├── business/                 # 业务组件
│   │   ├── UserProfile/
│   │   ├── LoginForm/
│   │   └── PermissionTree/
│   └── layout/                   # 布局组件
│       ├── AppHeader/
│       ├── AppSidebar/
│       └── AppFooter/
├── views/                        # 页面组件
│   ├── auth/                     # 认证相关页面
│   │   ├── Login.vue
│   │   └── ForgotPassword.vue
│   ├── dashboard/                # 工作台页面
│   │   └── Dashboard.vue
│   └── user/                     # 用户管理页面
│       ├── UserList.vue
│       ├── UserDetail.vue
│       └── UserProfile.vue
├── router/                       # 路由配置
│   ├── index.ts                  # 路由入口
│   ├── modules/                  # 路由模块
│   │   ├── auth.ts
│   │   ├── dashboard.ts
│   │   └── user.ts
│   └── guards/                   # 路由守卫
│       ├── auth.ts
│       └── permission.ts
├── stores/                       # 状态管理
│   ├── index.ts                  # Store入口
│   ├── modules/                  # Store模块
│   │   ├── auth.ts               # 认证状态
│   │   ├── user.ts               # 用户信息
│   │   └── app.ts                # 应用状态
│   └── types/                    # Store类型定义
├── api/                          # API接口
│   ├── index.ts                  # API入口
│   ├── request.ts                # 请求封装
│   ├── types/                    # API类型定义
│   │   ├── common.ts             # 通用类型
│   │   ├── auth.ts               # 认证类型
│   │   └── user.ts               # 用户类型
│   └── modules/                  # API模块
│       ├── auth.ts               # 认证接口
│       ├── user.ts               # 用户接口
│       └── dashboard.ts          # 工作台接口
├── utils/                        # 工具函数
│   ├── index.ts                  # 工具入口
│   ├── request.ts                # 请求工具
│   ├── storage.ts                # 存储工具
│   ├── auth.ts                   # 认证工具
│   ├── format.ts                 # 格式化工具
│   ├── validate.ts               # 验证工具
│   └── constants.ts              # 常量定义
├── composables/                  # 组合式函数
│   ├── useAuth.ts                # 认证逻辑
│   ├── useTable.ts               # 表格逻辑
│   ├── useForm.ts                # 表单逻辑
│   ├── useUpload.ts              # 上传逻辑
│   └── usePermission.ts          # 权限逻辑
├── directives/                   # 自定义指令
│   ├── index.ts                  # 指令入口
│   ├── permission.ts             # 权限指令
│   └── loading.ts                # 加载指令
├── plugins/                      # 插件配置
│   ├── index.ts                  # 插件入口
│   ├── antd.ts                   # Ant Design配置
│   └── i18n.ts                   # 国际化配置
└── types/                        # 全局类型定义
    ├── index.ts                  # 类型入口
    ├── global.d.ts               # 全局类型
    ├── env.d.ts                  # 环境变量类型
    └── vue.d.ts                  # Vue类型扩展
```

## UI设计规范

### 设计原则

#### 专业性与可信度
- 体现保险行业的专业性和权威性，使用稳重、可信的视觉元素
- 保持界面的简洁性和一致性，避免过度装饰
- 采用现代化的扁平设计风格，突出功能性

#### 用户体验优先
- 界面简洁直观，操作流程清晰易懂
- 提供及时的反馈和状态提示，增强用户操作信心
- 支持键盘快捷键和无障碍访问，提升使用效率

#### 响应式设计
- 适配桌面端和移动端设备，确保跨平台一致性
- 兼容不同分辨率的显示设备（1200px以上为主要目标）
- 确保在不同浏览器下的一致性体验

### 色彩规范

#### 主色调系统
- **主色**: #1890FF (蓝色) - 体现专业性和可信度，用于主要按钮、链接、选中状态
- **辅助色**: #52C41A (绿色) - 表示成功、通过状态，用于成功提示、确认操作
- **警告色**: #FAAD14 (橙色) - 表示警告、需注意状态，用于警告提示
- **错误色**: #F5222D (红色) - 表示错误、失败状态，用于错误提示、危险操作

#### 中性色系统
- **文本主色**: #262626 (深灰) - 用于主要文字内容
- **文本辅色**: #595959 (中灰) - 用于次要文字、说明文字
- **文本禁用**: #BFBFBF (浅灰) - 用于禁用状态文字
- **边框色**: #D9D9D9 (边框灰) - 用于组件边框、分割线
- **背景色**: #FAFAFA (背景灰) - 用于页面背景、卡片背景
- **纯白**: #FFFFFF - 用于组件背景、内容区域

#### 状态色彩应用
- **信息提示**: #1890FF - 用于一般信息提示
- **成功状态**: #52C41A - 用于操作成功反馈
- **警告状态**: #FAAD14 - 用于需要注意的提示
- **错误状态**: #F5222D - 用于错误信息提示
- **处理中**: #722ED1 (紫色) - 用于加载、处理中状态

### 字体规范

#### 字体族定义
- **中文字体**: "PingFang SC", "Microsoft YaHei", "微软雅黑", sans-serif
- **英文字体**: "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif
- **等宽字体**: "SF Mono", Monaco, Inconsolata, "Roboto Mono", Consolas, "Courier New", monospace

#### 字体大小层级
- **标题1**: 24px (页面主标题) - 用于登录页面系统标题
- **标题2**: 20px (区块标题) - 用于工作台各区域标题
- **标题3**: 16px (小节标题) - 用于卡片标题、表单标题
- **正文**: 14px (主要内容) - 用于正文内容、按钮文字、输入框文字
- **辅助文字**: 12px (说明文字) - 用于提示信息、时间戳、标签
- **小字**: 10px (版权信息等) - 用于版权信息、徽章数字

#### 字重规范
- **粗体**: 600 - 用于重要标题、强调文字
- **中等**: 500 - 用于次要标题、导航菜单
- **常规**: 400 - 用于正文内容、一般文字

### 布局规范

#### 栅格系统
- **容器最大宽度**: 1200px - 主要内容区域最大宽度
- **栅格列数**: 24列 - 基于Ant Design的24列栅格系统
- **基础间距单位**: 16px - 所有间距的基础倍数

#### 页面布局标准
- **顶部导航高度**: 64px - 固定导航栏高度
- **侧边栏宽度**: 256px (展开) / 80px (收起) - 预留侧边栏规范
- **内容区域边距**: 24px - 主要内容区域的内边距
- **卡片间距**: 16px - 卡片组件之间的间距

#### 组件间距规范
- **大间距**: 32px - 用于页面区块之间的间距
- **中间距**: 16px - 用于组件之间的间距
- **小间距**: 8px - 用于相关元素之间的间距
- **微间距**: 4px - 用于紧密相关元素之间的间距

### 组件设计规范

#### 按钮设计标准

**主要按钮**
- **背景色**: #1890FF (主色)
- **文字色**: #FFFFFF (白色)
- **圆角**: 6px
- **高度**: 32px (默认) / 40px (大) / 24px (小)
- **内边距**: 15px (水平)
- **字体大小**: 14px
- **字重**: 400

**次要按钮**
- **背景色**: #FFFFFF (白色)
- **边框**: 1px solid #D9D9D9
- **文字色**: #595959 (中灰)
- **悬停状态**: 边框色变为 #40A9FF

**危险按钮**
- **背景色**: #F5222D (错误色)
- **文字色**: #FFFFFF (白色)
- **用于删除、退出等危险操作**

**按钮状态设计**
- **正常状态**: 默认样式
- **悬停状态**: 背景色加深 10%
- **激活状态**: 背景色加深 15%
- **禁用状态**: 背景色 #F5F5F5，文字色 #BFBFBF
- **加载状态**: 显示旋转加载图标，禁用点击，文字变为"处理中..."

#### 输入框设计标准

**基础样式**
- **高度**: 32px (与按钮高度保持一致)
- **边框**: 1px solid #D9D9D9
- **圆角**: 6px
- **内边距**: 4px 11px
- **字体大小**: 14px
- **背景色**: #FFFFFF

**状态样式**
- **聚焦状态**: 边框色 #40A9FF，阴影 0 0 0 2px rgba(24, 144, 255, 0.2)
- **错误状态**: 边框色 #F5222D
- **禁用状态**: 背景色 #F5F5F5，边框色 #D9D9D9

**图标配置**
- **用户图标**: 用户名输入框左侧显示用户图标
- **锁定图标**: 密码输入框左侧显示锁定图标
- **清除按钮**: 有内容时右侧显示清除按钮

#### 卡片设计标准
- **背景色**: #FFFFFF (纯白)
- **边框**: 1px solid #F0F0F0
- **圆角**: 8px
- **阴影**: 0 2px 8px rgba(0, 0, 0, 0.06)
- **内边距**: 24px
- **标题字体**: 16px，字重 500
- **内容字体**: 14px，字重 400

#### 导航设计标准

**顶部导航**
- **背景色**: #001529 (深蓝色)
- **文字色**: #FFFFFF (白色)
- **高度**: 64px
- **Logo区域**: 左侧，宽度 256px
- **菜单项悬停**: 背景色 rgba(255, 255, 255, 0.1)
- **选中菜单项**: 背景色 #1890FF

**侧边导航**（预留规范）
- **背景色**: #FFFFFF (白色)
- **选中项背景**: #E6F7FF (浅蓝)
- **选中项文字**: #1890FF (主色)
- **悬停背景**: #F5F5F5 (浅灰)

### 交互规范

#### 反馈机制设计

**加载状态**
- **按钮加载**: 显示旋转图标，文字变为"登录中..."、"处理中..."
- **页面加载**: 显示骨架屏或加载动画
- **数据加载**: 显示进度条或加载提示

**成功反馈**
- **操作成功**: 绿色消息提示，3秒后自动消失
- **保存成功**: 按钮短暂变绿，显示对勾图标

**错误处理**
- **表单错误**: 字段下方显示红色错误文字
- **系统错误**: 红色消息提示，需手动关闭
- **网络错误**: 显示重试按钮和友好提示

#### 动画效果规范
- **过渡时间**: 0.3s (标准) / 0.2s (快速) / 0.5s (慢速)
- **缓动函数**: cubic-bezier(0.645, 0.045, 0.355, 1)
- **淡入淡出**: opacity 变化
- **滑动效果**: transform translateX/Y
- **页面切换**: 淡入淡出效果，时长 0.3s

### 图标规范

#### 图标库选择
- 使用 Ant Design Icons 作为主要图标库
- 保持图标风格一致性，统一使用线性图标
- **图标大小**: 14px (小) / 16px (默认) / 20px (大)

#### 常用图标定义
- **用户**: UserOutlined - 用于用户相关功能
- **密码**: LockOutlined - 用于密码输入框
- **搜索**: SearchOutlined - 用于搜索功能
- **上传**: UploadOutlined - 用于文件上传
- **下载**: DownloadOutlined - 用于文件下载
- **编辑**: EditOutlined - 用于编辑操作
- **删除**: DeleteOutlined - 用于删除操作
- **查看**: EyeOutlined - 用于查看详情
- **设置**: SettingOutlined - 用于设置功能
- **帮助**: QuestionCircleOutlined - 用于帮助信息
- **警告**: ExclamationCircleOutlined - 用于警告提示

### 响应式设计规范

#### 断点定义
- **xs**: < 576px (手机) - 单列布局，简化功能
- **sm**: ≥ 576px (大手机) - 单列布局，保持功能完整
- **md**: ≥ 768px (平板) - 两列布局，适当调整间距
- **lg**: ≥ 992px (桌面) - 标准布局，完整功能展示
- **xl**: ≥ 1200px (大桌面) - 标准布局，最佳体验
- **xxl**: ≥ 1600px (超大桌面) - 扩展布局

#### 适配规则
- **移动端**: 登录界面保持居中，工作台采用单列布局
- **平板**: 工作台采用两列布局，卡片适当调整大小
- **桌面**: 标准多列布局，完整功能展示

### 无障碍设计规范

#### 键盘导航
- 支持 Tab 键切换焦点，焦点顺序符合逻辑
- 支持 Enter 键确认操作（登录、提交等）
- 支持 Esc 键取消操作（关闭弹窗等）
- 焦点状态清晰可见，使用明显的视觉标识

#### 颜色对比
- 文字与背景对比度 ≥ 4.5:1
- 重要信息对比度 ≥ 7:1
- 不依赖颜色传达信息，配合图标和文字

#### 屏幕阅读器支持
- 提供 alt 属性描述图片内容
- 使用语义化 HTML 标签
- 提供 aria-label 标签描述交互元素

### 性能优化规范

#### 加载优化
- 首屏加载时间 < 3秒
- 交互响应时间 < 100ms
- 页面切换动画流畅，避免卡顿

#### 图片优化
- 使用 WebP 格式（支持的浏览器）
- 提供多种尺寸适配不同设备
- 实现懒加载减少初始加载时间

### 浏览器兼容性

#### 支持范围
- **Chrome**: 最新版本及前两个版本
- **Firefox**: 最新版本及前两个版本
- **Safari**: 最新版本及前两个版本
- **Edge**: 最新版本及前两个版本

#### 降级策略
- 渐进增强设计，核心功能在所有浏览器中可用
- 优雅降级处理，新特性不影响基础功能

### 具体界面设计规范

#### 登录界面设计规范

**整体布局**
- **容器**: 全屏居中布局，最小高度100vh
- **背景**: 渐变背景色 linear-gradient(135deg, #667eea 0%, #764ba2 100%)
- **登录面板**: 居中卡片，宽度400px，圆角8px，阴影效果
- **响应式**: 移动端宽度调整为90%，最大宽度400px

**系统标识区域**
- **Logo**: 尺寸64x64px，位于标题上方，居中对齐
- **系统标题**: 字体大小24px，字重600，颜色#262626
- **标题间距**: Logo与标题间距16px，标题与表单间距32px

**登录表单设计**
- **表单容器**: 内边距32px，背景色#FFFFFF
- **输入框间距**: 表单项之间间距24px
- **输入框样式**: 高度40px（大尺寸），圆角6px，边框#D9D9D9
- **图标配置**: 用户图标和锁定图标，颜色#8C8C8C，大小16px
- **占位符文字**: 颜色#BFBFBF，字体大小14px

**错误提示设计**
- **提示容器**: 位于表单上方，背景色#FFF2F0，边框#FFCCC7
- **图标**: ExclamationCircleOutlined，颜色#F5222D，大小16px
- **文字**: 颜色#F5222D，字体大小14px
- **内边距**: 12px 16px，圆角4px

**登录按钮设计**
- **尺寸**: 高度40px，宽度100%，圆角6px
- **颜色**: 背景色#1890FF，文字色#FFFFFF
- **状态**: 禁用时背景色#F5F5F5，文字色#BFBFBF
- **加载**: 显示旋转图标，文字"登录中..."

**页脚设计**
- **位置**: 固定在页面底部
- **文字**: 颜色#8C8C8C，字体大小12px，居中对齐
- **内容**: 版权信息和年份

#### 工作台界面设计规范

**顶部导航栏设计**
- **高度**: 64px，背景色#001529
- **品牌区域**: 左侧256px宽度，显示"产品检核系统"，字体16px，颜色#FFFFFF
- **导航菜单**: 水平排列，字体14px，颜色#FFFFFF
- **用户区域**: 右侧，显示用户名和下拉菜单，字体14px

**主内容区域布局**
- **容器**: 最大宽度1200px，居中对齐，内边距24px
- **栅格系统**: 使用24列栅格，基础间距16px
- **区域间距**: 各功能区域之间间距32px

**汇总数据区域设计**
- **标题**: "汇总数据"，字体20px，字重500，颜色#262626
- **卡片布局**: 4个指标卡片，每个占6列（xl:6, lg:6, md:12, sm:24）
- **卡片样式**: 背景#FFFFFF，圆角8px，内边距24px，阴影效果
- **数值显示**: 字体28px，字重600，颜色#262626
- **标签显示**: 字体14px，字重400，颜色#8C8C8C
- **时间筛选**: 标签页样式，选中状态背景#E6F7FF，文字#1890FF

**我的关注区域设计**
- **标题**: "我的关注"，字体20px，字重500
- **搜索区域**: 输入框高度32px，宽度200px，占位符"请输入任务名称"
- **筛选器**: 下拉选择器，宽度120px，高度32px
- **任务列表**: 每项高度54px，悬停背景#F5F5F5
- **任务图标**: 大小16px，颜色#1890FF
- **任务名称**: 字体14px，颜色#1890FF（链接样式）
- **提交信息**: 字体12px，颜色#8C8C8C

**快速开始区域设计**
- **标题**: "快速开始 / 便捷导航"，字体20px，字重500
- **按钮布局**: 2x4网格布局，按钮间距16px
- **按钮样式**: 高度80px，圆角8px，背景#FFFFFF，边框#F0F0F0
- **按钮文字**: 字体14px，字重400，颜色#262626
- **徽章数字**: 右上角，背景#F5222D，文字#FFFFFF，字体10px

**近期动态区域设计**
- **标题**: "近期动态"，字体20px，字重500
- **动态列表**: 每项间距12px，分割线#F0F0F0
- **用户名**: 字体14px，字重500，颜色#262626
- **操作描述**: 字体14px，字重400，颜色#595959
- **产品链接**: 字体14px，颜色#1890FF
- **时间信息**: 字体12px，颜色#8C8C8C

**应用概要区域设计**
- **标题**: "应用概要"，字体20px，字重500
- **统计卡片**: 2x4布局，卡片间距16px
- **数值显示**: 字体20px，字重600，颜色#262626
- **标签文字**: 字体12px，颜色#8C8C8C
- **趋势图标**: 箭头图标，上升#F5222D，下降#52C41A
- **百分比**: 字体12px，对应趋势颜色

**统计图表区域设计**
- **标题**: "数量统计占比"，字体20px，字重500
- **选项卡**: 高度32px，字体14px，选中背景#E6F7FF
- **饼图**: 直径200px，颜色使用主题色系
- **图例列表**: 每项高度32px，圆点8px，文字14px
- **占比显示**: 百分比字重500，数量字重400

#### 后端分层（DDD架构）
```
com.insurance.audit.user/                   # 用户管理领域
├── interfaces/                             # 接口层
│   ├── web/                               # REST控制器
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── dto/                               # 数据传输对象
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── UserCreateRequest.java
│   │   │   └── ChangePasswordRequest.java
│   │   └── response/
│   │       ├── LoginResponse.java
│   │       ├── UserResponse.java
│   │       └── UserInfoResponse.java
│   └── facade/
│       └── UserFacade.java
├── application/                            # 应用层
│   ├── service/
│   │   ├── AuthApplicationService.java
│   │   └── UserApplicationService.java
│   ├── dto/
│   │   ├── command/
│   │   │   ├── LoginCommand.java
│   │   │   ├── CreateUserCommand.java
│   │   │   └── ChangePasswordCommand.java
│   │   ├── query/
│   │   │   ├── UserQuery.java
│   │   │   └── UserListQuery.java
│   │   └── result/
│   │       ├── AuthResult.java
│   │       ├── UserResult.java
│   │       └── UserListResult.java
│   └── assembler/
│       └── UserAssembler.java
├── domain/                                 # 领域层
│   ├── model/
│   │   ├── User.java                      # 用户聚合根
│   │   ├── Role.java                      # 角色实体
│   │   ├── Permission.java                # 权限实体
│   │   ├── LoginSession.java              # 登录会话值对象
│   │   └── UserProfile.java               # 用户档案值对象
│   ├── service/
│   │   ├── UserDomainService.java
│   │   ├── AuthDomainService.java
│   │   └── PasswordPolicyService.java
│   ├── repository/
│   │   ├── UserRepository.java            # 领域仓储接口
│   │   ├── RoleRepository.java
│   │   └── PermissionRepository.java
│   ├── factory/
│   │   └── UserFactory.java
│   └── event/
│       ├── UserCreatedEvent.java
│       ├── UserLoginEvent.java
│       └── PasswordChangedEvent.java
└── infrastructure/                         # 基础设施层
    ├── repository/
    │   ├── UserRepositoryImpl.java
    │   ├── UserMapper.java                # MyBatis映射器
    │   ├── RoleMapper.java
    │   └── PermissionMapper.java
    ├── security/
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtAuthenticationEntryPoint.java
    │   ├── JwtAccessDeniedHandler.java
    │   ├── CustomUserDetailsService.java
    │   ├── LoginAttemptService.java
    │   └── SecurityConfig.java
    ├── config/
    │   ├── UserConfig.java
    │   └── JwtConfig.java
    ├── external/
    │   └── LdapAdapter.java
    └── converter/
        └── UserConverter.java
```## 
组件设计

### 前端组件架构

#### 1. 登录组件 (LoginView)

**组件职责**:
- 用户登录界面展示
- 用户输入验证
- 登录状态管理
- 错误提示处理

**组件结构**:
```vue
<template>
  <div class="login-container">
    <div class="login-panel">
      <div class="system-header">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
        <h1 class="system-title">保险产品智能检核系统</h1>
      </div>
      
      <div class="login-form">
        <div v-if="errorMessage" class="error-alert">
          <ExclamationCircleOutlined class="error-icon" />
          <span>{{ errorMessage }}</span>
        </div>
        
        <a-form :model="loginForm" @finish="handleLogin">
          <a-form-item name="username" :rules="usernameRules">
            <a-input
              v-model:value="loginForm.username"
              placeholder="请输入用户账号"
              size="large"
              :prefix="h(UserOutlined)"
              allow-clear
            />
          </a-form-item>
          
          <a-form-item name="password" :rules="passwordRules">
            <a-input-password
              v-model:value="loginForm.password"
              placeholder="请输入登录密码"
              size="large"
              :prefix="h(LockOutlined)"
              allow-clear
            />
          </a-form-item>
          
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loginLoading"
              :disabled="!isFormValid"
            >
              {{ loginLoading ? '登录中...' : '登录' }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
    
    <div class="footer">
      <p>&copy; 2024 保险产品智能检核系统. All rights reserved.</p>
    </div>
  </div>
</template>
```

**状态管理**:
```typescript
interface LoginState {
  loginForm: {
    username: string
    password: string
  }
  loginLoading: boolean
  errorMessage: string
  loginAttempts: number
  lockoutTime: number | null
}
```

#### 2. 工作台组件 (DashboardView)

**组件职责**:
- 系统主页面布局
- 导航菜单管理
- 数据汇总展示
- 任务列表管理
- 快速导航功能

**组件结构**:
```vue
<template>
  <a-layout class="dashboard-layout">
    <!-- 顶部导航栏 -->
    <a-layout-header class="dashboard-header">
      <div class="header-left">
        <span class="system-brand">产品检核系统</span>
      </div>
      
      <div class="header-nav">
        <a-menu
          v-model:selectedKeys="selectedMenuKeys"
          mode="horizontal"
          theme="dark"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </div>
      
      <div class="header-right">
        <a-dropdown>
          <span class="user-info">
            <UserOutlined />
            {{ currentUser.name }}
          </span>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="profile">个人设置</a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout">退出登录</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>
    
    <!-- 主内容区域 -->
    <a-layout-content class="dashboard-content">
      <div class="content-wrapper">
        <!-- 汇总数据区域 -->
        <SummaryDataSection />
        
        <!-- 应用概要区域 -->
        <ApplicationOverviewSection />
        
        <!-- 我的关注区域 -->
        <MyFocusSection />
        
        <!-- 快速开始区域 -->
        <QuickStartSection />
        
        <!-- 近期动态区域 -->
        <RecentActivitiesSection />
        
        <!-- 数量统计占比区域 -->
        <StatisticsChartSection />
      </div>
    </a-layout-content>
  </a-layout>
</template>
```

#### 3. 汇总数据组件 (SummaryDataSection)

**组件职责**:
- 显示系统关键指标
- 时间维度筛选
- 数据实时更新

**数据结构**:
```typescript
interface SummaryData {
  productCount: number      // 产品数量
  documentCount: number     // 要件数量
  ruleCount: number        // 规则数量
  errorCount: number       // 已检核错误数量
}

interface TimeFilter {
  key: 'total' | 'today' | 'week' | 'month'
  label: string
  active: boolean
}
```

#### 4. 我的关注组件 (MyFocusSection)

**组件职责**:
- 显示关注的任务列表
- 任务搜索和筛选
- 任务详情跳转

**数据结构**:
```typescript
interface FocusTask {
  id: string
  name: string
  submitter: string
  submitTime: string
  type: string
  status: string
  icon: string
}

interface SearchFilter {
  keyword: string
  timeFilter: string
  typeFilter: string
}
```

#### 5. Mock数据服务设计

**服务职责**:
- 提供工作台页面所需的模拟数据
- 模拟真实的数据结构和业务逻辑
- 支持搜索、筛选等交互功能

**Mock服务实现**:
```typescript
// src/api/mock/dashboardMock.ts
export class DashboardMockService {
  
  // 汇总数据Mock
  getSummaryData(timeRange: string) {
    const mockData = {
      total: { productCount: 156, documentCount: 1247, ruleCount: 89, errorCount: 23 },
      today: { productCount: 5, documentCount: 42, ruleCount: 3, errorCount: 1 },
      week: { productCount: 23, documentCount: 187, ruleCount: 12, errorCount: 7 },
      month: { productCount: 67, documentCount: 523, ruleCount: 28, errorCount: 15 }
    }
    return Promise.resolve(mockData[timeRange] || mockData.total)
  }
  
  // 应用概要Mock
  getOverviewData() {
    return Promise.resolve({
      cumulative: {
        totalProducts: 156,
        totalRules: 89,
        errorDetectionRate: "85.6%",
        avgAuditTime: "00:02:27"
      },
      recent: {
        products7Days: 23,
        products30Days: 67,
        rules7Days: 12,
        rules30Days: 28,
        productGrowthRate: "+12.5%",
        ruleGrowthRate: "+8.3%"
      }
    })
  }
  
  // 关注任务Mock（支持搜索筛选）
  getFocusTasks(params: { keyword?: string, timeFilter?: string, typeFilter?: string }) {
    let tasks = [
      {
        id: "1",
        name: "财产保险产品A检核任务",
        submitter: "胡潇禹",
        submitTime: "2022/10/31",
        type: "产品检核",
        status: "进行中",
        icon: "file-text"
      },
      {
        id: "2",
        name: "意外险产品B规则配置",
        submitter: "张三",
        submitTime: "2022/10/30",
        type: "规则配置", 
        status: "待审核",
        icon: "setting"
      },
      {
        id: "3",
        name: "车险产品C合规检查",
        submitter: "李四",
        submitTime: "2022/10/29",
        type: "合规检查",
        status: "已完成",
        icon: "check-circle"
      }
      // ... 更多Mock数据
    ]
    
    // 模拟搜索筛选
    if (params.keyword) {
      tasks = tasks.filter(task => 
        task.name.includes(params.keyword!) || 
        task.submitter.includes(params.keyword!)
      )
    }
    
    if (params.typeFilter && params.typeFilter !== '全部任务') {
      tasks = tasks.filter(task => task.type === params.typeFilter)
    }
    
    return Promise.resolve({
      records: tasks,
      total: tasks.length,
      current: 1,
      size: 10
    })
  }
  
  // 近期动态Mock
  getRecentActivities() {
    return Promise.resolve({
      activities: [
        {
          id: "1",
          type: "upload",
          user: "胡潇禹", 
          content: "上传了",
          productName: "财产保险产品A",
          productId: "prod_001",
          time: "2022-10-31 14:30:00",
          relativeTime: "14分钟前"
        },
        {
          id: "2",
          type: "rule_update",
          user: "张三",
          content: "更新了3条规则",
          ruleCount: 3,
          time: "2022-10-31 14:10:00",
          relativeTime: "20分钟前"
        },
        {
          id: "3",
          type: "rule_approve", 
          user: "李四",
          content: "审核通过了5条规则",
          ruleCount: 5,
          time: "2022-10-31 13:50:00",
          relativeTime: "40分钟前"
        }
        // ... 更多Mock数据
      ]
    })
  }
  
  // 统计图表Mock
  getStatisticsData(type: string) {
    const mockData = {
      productType: {
        chartData: [
          { name: "种植险", value: 45, percentage: "28.8%", color: "#1890ff" },
          { name: "意健险", value: 32, percentage: "20.5%", color: "#52c41a" },
          { name: "车险", value: 28, percentage: "17.9%", color: "#faad14" },
          { name: "家财险", value: 25, percentage: "16.0%", color: "#f5222d" },
          { name: "养殖险", value: 18, percentage: "11.5%", color: "#722ed1" },
          { name: "其他", value: 8, percentage: "5.1%", color: "#13c2c2" }
        ],
        total: 156
      },
      department: {
        chartData: [
          { name: "产品部", value: 68, percentage: "43.6%", color: "#1890ff" },
          { name: "核保部", value: 42, percentage: "26.9%", color: "#52c41a" },
          { name: "理赔部", value: 28, percentage: "17.9%", color: "#faad14" },
          { name: "其他部门", value: 18, percentage: "11.5%", color: "#f5222d" }
        ],
        total: 156
      },
      ruleType: {
        chartData: [
          { name: "基础规则", value: 35, percentage: "39.3%", color: "#1890ff" },
          { name: "业务规则", value: 28, percentage: "31.5%", color: "#52c41a" },
          { name: "合规规则", value: 16, percentage: "18.0%", color: "#faad14" },
          { name: "自定义规则", value: 10, percentage: "11.2%", color: "#f5222d" }
        ],
        total: 89
      }
    }
    
    return Promise.resolve(mockData[type] || mockData.productType)
  }
}

// 导出Mock服务实例
export const dashboardMockService = new DashboardMockService()
```## 数
据模型设计

### 基础实体设计

#### BaseEntity (基础实体)
```java
/**
 * 基础实体类
 * 包含所有实体的公共字段
 */
@Data
@Schema(description = "基础实体")
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID", example = "123e4567e89b12d3a456426614174000")
    private String id;
    
    /**
     * 创建人ID
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", hidden = true)
    private String createdBy;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createdAt;
    
    /**
     * 更新人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", hidden = true)
    private String updatedBy;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "是否删除", hidden = true)
    private Boolean deleted;
    
    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Schema(description = "版本号", hidden = true)
    private Integer version;
}
```

### 用户实体模型

#### User (用户实体)
```java
/**
 * 用户聚合根
 * 领域驱动设计中的用户实体，包含用户相关的业务逻辑
 */
@Getter
@TableName("users")
@Schema(description = "用户实体")
public class User extends BaseEntity {
    
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Length(max = 50, message = "用户名长度不能超过50字符")
    @Schema(description = "用户名", required = true)
    private String username;
    
    @TableField("password")
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;
    
    @TableField("real_name")
    @Length(max = 50, message = "真实姓名长度不能超过50字符")
    @Schema(description = "真实姓名")
    private String realName;
    
    @TableField("email")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;
    
    @TableField("phone")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;
    
    @TableField("status")
    @NotNull(message = "用户状态不能为空")
    @Schema(description = "用户状态")
    private UserStatus status;
    
    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
    
    @TableField("last_login_ip")
    @Schema(description = "最后登录IP")
    private String lastLoginIp;
    
    @TableField("login_attempts")
    @Schema(description = "登录尝试次数")
    private Integer loginAttempts;
    
    @TableField("locked_until")
    @Schema(description = "锁定截止时间")
    private LocalDateTime lockedUntil;
    
    @TableField(exist = false)
    @Schema(description = "用户角色列表")
    private List<Role> roles;
    
    // 构造函数 - 确保实体创建时的业务规则
    public User(String username, String password, String realName) {
        this.username = Objects.requireNonNull(username, "用户名不能为空");
        this.password = Objects.requireNonNull(password, "密码不能为空");
        this.realName = realName;
        this.status = UserStatus.ACTIVE;
        this.loginAttempts = 0;
        this.roles = new ArrayList<>();
    }
    
    // 业务方法 - 封装业务规则
    public void recordSuccessfulLogin(String clientIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = clientIp;
        this.loginAttempts = 0;
        this.lockedUntil = null;
        
        // 发布领域事件
        DomainEventPublisher.publish(new UserLoginEvent(this.getId(), clientIp));
    }
    
    public void recordFailedLogin() {
        this.loginAttempts++;
        if (this.loginAttempts >= 5) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
            DomainEventPublisher.publish(new UserLockedEvent(this.getId()));
        }
    }
    
    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(LocalDateTime.now());
    }
    
    public void changePassword(String newPassword) {
        validatePasswordPolicy(newPassword);
        this.password = newPassword;
        DomainEventPublisher.publish(new PasswordChangedEvent(this.getId()));
    }
    
    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new BusinessException("用户已经是激活状态");
        }
        this.status = UserStatus.ACTIVE;
    }
    
    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new BusinessException("用户已经是停用状态");
        }
        this.status = UserStatus.INACTIVE;
    }
    
    public void assignRole(Role role) {
        if (this.roles.contains(role)) {
            throw new BusinessException("用户已拥有该角色");
        }
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        if (!this.roles.contains(role)) {
            throw new BusinessException("用户未拥有该角色");
        }
        this.roles.remove(role);
    }
    
    private void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("密码长度不能少于8位");
        }
        // 其他密码策略验证...
    }
}
```

#### Role (角色实体)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("roles")
@Schema(description = "角色实体")
public class Role extends BaseEntity {
    
    @TableField("role_name")
    @NotBlank(message = "角色名称不能为空")
    @Length(max = 50, message = "角色名称长度不能超过50字符")
    @Schema(description = "角色名称", required = true)
    private String roleName;
    
    @TableField("role_code")
    @NotBlank(message = "角色编码不能为空")
    @Length(max = 50, message = "角色编码长度不能超过50字符")
    @Schema(description = "角色编码", required = true)
    private String roleCode;
    
    @TableField("description")
    @Length(max = 200, message = "角色描述长度不能超过200字符")
    @Schema(description = "角色描述")
    private String description;
    
    @TableField("status")
    @NotNull(message = "角色状态不能为空")
    @Builder.Default
    @Schema(description = "角色状态")
    private RoleStatus status = RoleStatus.ACTIVE;
    
    @TableField(exist = false)
    @Schema(description = "角色权限列表")
    private List<Permission> permissions;
}
```

#### Permission (权限实体)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("permissions")
@Schema(description = "权限实体")
public class Permission extends BaseEntity {
    
    @TableField("permission_name")
    @NotBlank(message = "权限名称不能为空")
    @Length(max = 50, message = "权限名称长度不能超过50字符")
    @Schema(description = "权限名称", required = true)
    private String permissionName;
    
    @TableField("permission_code")
    @NotBlank(message = "权限编码不能为空")
    @Length(max = 100, message = "权限编码长度不能超过100字符")
    @Schema(description = "权限编码", required = true)
    private String permissionCode;
    
    @TableField("resource_type")
    @NotNull(message = "资源类型不能为空")
    @Schema(description = "资源类型", required = true)
    private ResourceType resourceType;
    
    @TableField("resource_path")
    @Length(max = 200, message = "资源路径长度不能超过200字符")
    @Schema(description = "资源路径")
    private String resourcePath;
    
    @TableField("parent_id")
    @Schema(description = "父权限ID")
    private String parentId;
    
    @TableField("sort_order")
    @Builder.Default
    @Schema(description = "排序顺序")
    private Integer sortOrder = 0;
}
```

### 枚举定义

#### UserStatus (用户状态)
```java
@Getter
@AllArgsConstructor
public enum UserStatus implements IEnum<String> {
    
    ACTIVE("ACTIVE", "正常"),
    INACTIVE("INACTIVE", "停用"),
    LOCKED("LOCKED", "锁定"),
    EXPIRED("EXPIRED", "过期");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
}
```

#### RoleStatus (角色状态)
```java
@Getter
@AllArgsConstructor
public enum RoleStatus implements IEnum<String> {
    
    ACTIVE("ACTIVE", "启用"),
    INACTIVE("INACTIVE", "停用");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
}
```

#### ResourceType (资源类型)
```java
@Getter
@AllArgsConstructor
public enum ResourceType implements IEnum<String> {
    
    MENU("MENU", "菜单"),
    BUTTON("BUTTON", "按钮"),
    API("API", "接口"),
    DATA("DATA", "数据");
    
    @EnumValue
    @JsonValue
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
}
```

### 关联表设计

#### UserRole (用户角色关联)
```sql
CREATE TABLE user_roles (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    created_by VARCHAR(32) COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

#### RolePermission (角色权限关联)
```sql
CREATE TABLE role_permissions (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(32) NOT NULL COMMENT '权限ID',
    created_by VARCHAR(32) COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

### 领域仓储接口设计

#### UserRepository (用户仓储接口)
```java
/**
 * 用户仓储接口 - 领域层定义
 * 使用领域语言，不暴露技术细节
 */
public interface UserRepository {
    
    // 基础CRUD
    void save(User user);
    Optional<User> findById(String id);
    void remove(String id);
    
    // 业务查询方法
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // 复杂查询
    Page<User> findBySpecification(UserSpecification spec, Pageable pageable);
    List<User> findByRoleCode(String roleCode);
    
    // 聚合查询
    long countByStatus(UserStatus status);
    Map<UserStatus, Long> countGroupByStatus();
}

/**
 * 用户仓储实现 - 基础设施层
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    private final UserConverter userConverter;
    
    public UserRepositoryImpl(UserMapper userMapper, UserConverter userConverter) {
        this.userMapper = userMapper;
        this.userConverter = userConverter;
    }
    
    @Override
    public void save(User user) {
        UserPO userPO = userConverter.toDataObject(user);
        if (userPO.getId() == null) {
            userMapper.insert(userPO);
            user.setId(userPO.getId()); // 回填ID
        } else {
            userMapper.updateById(userPO);
        }
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        UserPO userPO = userMapper.selectOne(Wrappers.<UserPO>lambdaQuery()
            .eq(UserPO::getUsername, username)
            .eq(UserPO::getDeleted, false));
        
        return Optional.ofNullable(userPO)
            .map(userConverter::toDomainObject);
    }
    
    // 其他方法实现...
}
```## 
接口设计

### 认证相关接口

#### 1. 用户登录接口

**接口路径**: `POST /api/v1/user/auth/login`

**请求参数**:
```typescript
interface LoginRequest {
  username: string    // 用户名，必填，长度1-50
  password: string    // 密码，必填，长度1-100
  captcha?: string    // 验证码，可选
  rememberMe?: boolean // 记住我，可选，默认false
}
```

**响应数据**:
```typescript
interface LoginResponse {
  token: string           // JWT访问令牌
  refreshToken: string    // 刷新令牌
  expiresIn: number      // 令牌过期时间（秒）
  user: {
    id: string
    username: string
    realName: string
    email: string
    roles: string[]
    permissions: string[]
  }
}
```

**错误码**:
- `USER_001`: 用户名或密码错误
- `USER_002`: 账户已被锁定
- `USER_003`: 账户已过期
- `AUTH_001`: 验证码错误
- `AUTH_002`: 登录尝试次数过多

#### 2. 用户退出接口

**接口路径**: `POST /api/v1/user/auth/logout`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```typescript
interface LogoutResponse {
  message: string  // 退出成功提示
}
```

#### 3. 刷新令牌接口

**接口路径**: `POST /api/v1/user/auth/refresh`

**请求参数**:
```typescript
interface RefreshTokenRequest {
  refreshToken: string  // 刷新令牌
}
```

**响应数据**:
```typescript
interface RefreshTokenResponse {
  token: string        // 新的访问令牌
  expiresIn: number   // 令牌过期时间（秒）
}
```

### 用户管理接口

#### 1. 获取当前用户信息

**接口路径**: `GET /api/v1/user/me`

**请求头**:
```
Authorization: Bearer {token}
```

**响应数据**:
```typescript
interface UserInfoResponse {
  id: string
  username: string
  realName: string
  email: string
  phone: string
  status: string
  lastLoginTime: string
  roles: Array<{
    id: string
    roleName: string
    roleCode: string
  }>
  permissions: string[]
}
```

#### 2. 修改密码接口

**接口路径**: `PUT /api/v1/user/password`

**请求参数**:
```typescript
interface ChangePasswordRequest {
  oldPassword: string  // 原密码
  newPassword: string  // 新密码
}
```

### 工作台数据接口（MVP阶段暂不实现）

> **注意**: 以下接口在MVP阶段暂不实现后端逻辑，前端使用Mock数据进行展示。

#### Mock数据结构定义

**汇总数据Mock**:
```typescript
interface SummaryMockData {
  total: {
    productCount: 156      // 产品数量
    documentCount: 1247    // 要件数量
    ruleCount: 89         // 规则数量
    errorCount: 23        // 已检核错误数量
  }
  today: {
    productCount: 5
    documentCount: 42
    ruleCount: 3
    errorCount: 1
  }
  week: {
    productCount: 23
    documentCount: 187
    ruleCount: 12
    errorCount: 7
  }
  month: {
    productCount: 67
    documentCount: 523
    ruleCount: 28
    errorCount: 15
  }
}
```

**应用概要Mock数据**:
```typescript
interface OverviewMockData {
  cumulative: {
    totalProducts: 156           // 累计上传产品数量
    totalRules: 89              // 累计导入规则数量
    errorDetectionRate: "85.6%" // 错误检出率
    avgAuditTime: "00:02:27"    // 平均检核时间
  }
  recent: {
    products7Days: 23           // 近7日上传产品数量
    products30Days: 67          // 近30日上传产品数量
    rules7Days: 12             // 近7日导入规则数量
    rules30Days: 28            // 近30日导入规则数量
    productGrowthRate: "+12.5%" // 产品增长率
    ruleGrowthRate: "+8.3%"    // 规则增长率
  }
}
```

**关注任务Mock数据**:
```typescript
interface FocusTasksMockData {
  records: [
    {
      id: "1",
      name: "财产保险产品A检核任务",
      submitter: "胡潇禹",
      submitTime: "2022/10/31",
      type: "产品检核",
      status: "进行中",
      icon: "file-text"
    },
    {
      id: "2", 
      name: "意外险产品B规则配置",
      submitter: "张三",
      submitTime: "2022/10/30",
      type: "规则配置",
      status: "待审核",
      icon: "setting"
    }
    // ... 更多Mock数据
  ],
  total: 25,
  current: 1,
  size: 10
}
```

**近期动态Mock数据**:
```typescript
interface ActivitiesMockData {
  activities: [
    {
      id: "1",
      type: "upload",
      user: "胡潇禹",
      content: "上传了",
      productName: "财产保险产品A",
      productId: "prod_001",
      time: "2022-10-31 14:30:00",
      relativeTime: "14分钟前"
    },
    {
      id: "2",
      type: "rule_update",
      user: "张三",
      content: "更新了3条规则",
      ruleCount: 3,
      time: "2022-10-31 14:10:00", 
      relativeTime: "20分钟前"
    }
    // ... 更多Mock数据
  ]
}
```

**统计图表Mock数据**:
```typescript
interface StatisticsMockData {
  productType: {
    chartData: [
      { name: "种植险", value: 45, percentage: "28.8%", color: "#1890ff" },
      { name: "意健险", value: 32, percentage: "20.5%", color: "#52c41a" },
      { name: "车险", value: 28, percentage: "17.9%", color: "#faad14" },
      { name: "家财险", value: 25, percentage: "16.0%", color: "#f5222d" },
      { name: "养殖险", value: 18, percentage: "11.5%", color: "#722ed1" },
      { name: "其他", value: 8, percentage: "5.1%", color: "#13c2c2" }
    ],
    total: 156
  },
  department: {
    chartData: [
      { name: "产品部", value: 68, percentage: "43.6%", color: "#1890ff" },
      { name: "核保部", value: 42, percentage: "26.9%", color: "#52c41a" },
      { name: "理赔部", value: 28, percentage: "17.9%", color: "#faad14" },
      { name: "其他部门", value: 18, percentage: "11.5%", color: "#f5222d" }
    ],
    total: 156
  },
  ruleType: {
    chartData: [
      { name: "基础规则", value: 35, percentage: "39.3%", color: "#1890ff" },
      { name: "业务规则", value: 28, percentage: "31.5%", color: "#52c41a" },
      { name: "合规规则", value: 16, percentage: "18.0%", color: "#faad14" },
      { name: "自定义规则", value: 10, percentage: "11.2%", color: "#f5222d" }
    ],
    total: 89
  }
}

## 技术实现方案

### MVP阶段实现策略

#### 1. 认证功能（完整实现）

**后端实现**:
- Spring Security + JWT完整认证体系
- 用户登录、退出、令牌刷新
- 密码加密和安全策略
- 登录失败锁定机制
- 操作日志记录

**前端实现**:
- 完整的登录界面和交互
- JWT令牌管理和自动刷新
- 路由守卫和权限控制
- 错误处理和用户反馈

#### 2. 工作台功能（前端展示）

**前端实现**:
- 完整的工作台页面布局
- 所有数据展示组件
- 交互功能（搜索、筛选、切换）
- 图表展示和数据可视化

**Mock数据策略**:
- 使用本地Mock服务模拟后端接口
- 提供真实的数据结构和格式
- 支持基本的搜索筛选交互
- 为后续真实接口对接做准备

#### 3. 暂不实现功能

**后端业务逻辑**:
- 产品管理相关业务
- 规则管理相关业务  
- 实际的数据统计分析
- 复杂的权限控制逻辑

**复杂交互功能**:
- 文件上传下载
- 批量操作
- 实时数据更新
- 高级搜索功能

### 前端Mock数据集成

#### Mock服务配置
```typescript
// src/config/mock.ts
export const MOCK_CONFIG = {
  enabled: true,  // MVP阶段启用Mock
  delay: 300,     // 模拟网络延迟
  baseURL: '/mock/api/v1'
}

// 在生产环境中可以通过环境变量控制
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'
```

#### API适配器设计
```typescript
// src/api/adapters/dashboardAdapter.ts
import { USE_MOCK } from '@/config/mock'
import { dashboardMockService } from '@/api/mock/dashboardMock'
import { dashboardApiService } from '@/api/modules/dashboard'

export const dashboardAdapter = {
  getSummaryData: USE_MOCK 
    ? dashboardMockService.getSummaryData.bind(dashboardMockService)
    : dashboardApiService.getSummaryData.bind(dashboardApiService),
    
  getOverviewData: USE_MOCK
    ? dashboardMockService.getOverviewData.bind(dashboardMockService) 
    : dashboardApiService.getOverviewData.bind(dashboardApiService),
    
  // ... 其他方法
}
```

## 安全设计

### JWT令牌设计

#### 令牌结构
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user_id",
    "username": "admin",
    "roles": ["ADMIN", "USER"],
    "permissions": ["product:read", "product:write"],
    "iat": 1640995200,
    "exp": 1641081600,
    "jti": "unique_token_id"
  }
}
```

#### 令牌管理策略
- **访问令牌有效期**: 2小时
- **刷新令牌有效期**: 7天
- **令牌存储**: Redis缓存，支持主动失效
- **令牌刷新**: 自动刷新机制，无感知更新

### 密码安全策略

#### 密码加密
```java
@Component
public class PasswordEncoder {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

### MVP阶段安全考虑

#### 简化的权限控制
- 基础的角色区分（管理员、普通用户）
- 简单的菜单权限控制
- 基本的API访问控制
- 暂不实现复杂的数据权限

#### 安全最佳实践
- 所有密码必须加密存储
- JWT令牌安全管理
- 基本的输入验证和XSS防护
- HTTPS传输加密（生产环境）

#### 密码策略
- **最小长度**: 8位
- **复杂度要求**: 包含大小写字母、数字、特殊字符中的至少3种
- **历史密码**: 不能与最近3次密码相同
- **有效期**: 90天（可配置）

### 防暴力破解机制

#### 登录限制策略
```java
@Component
public class LoginAttemptService {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    public void recordFailedAttempt(String username, String ip) {
        // 记录失败尝试
        String userKey = "login_attempts:user:" + username;
        String ipKey = "login_attempts:ip:" + ip;
        
        redisTemplate.opsForValue().increment(userKey);
        redisTemplate.opsForValue().increment(ipKey);
        redisTemplate.expire(userKey, Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
        redisTemplate.expire(ipKey, Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
    }
    
    public boolean isBlocked(String username, String ip) {
        String userKey = "login_attempts:user:" + username;
        String ipKey = "login_attempts:ip:" + ip;
        
        Integer userAttempts = (Integer) redisTemplate.opsForValue().get(userKey);
        Integer ipAttempts = (Integer) redisTemplate.opsForValue().get(ipKey);
        
        return (userAttempts != null && userAttempts >= MAX_ATTEMPTS) ||
               (ipAttempts != null && ipAttempts >= MAX_ATTEMPTS);
    }
}
```

### 权限控制设计

#### RBAC权限模型
```
用户(User) ←→ 角色(Role) ←→ 权限(Permission)
     ↓              ↓              ↓
  用户信息        角色定义        资源访问
```

#### 权限注解
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasPermission('product', 'read')")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
```

#### 数据权限控制
```java
@DataPermission(type = DataPermissionType.USER, field = "created_by")
public List<Product> getUserProducts() {
    // 自动添加数据权限过滤条件
    return productService.findAll();
}
```## 
技术实现方案

### 前端实现

#### 1. 状态管理设计 (Pinia)

```typescript
// stores/auth.ts
export const useAuthStore = defineStore('auth', () => {
  // 状态定义
  const token = ref<string>(getToken() || '')
  const refreshToken = ref<string>(getRefreshToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])
  const loginLoading = ref(false)
  const loginAttempts = ref(0)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const hasPermission = computed(() => (permission: string) => {
    return permissions.value.includes(permission)
  })
  
  // 登录方法
  const login = async (loginForm: LoginRequest): Promise<void> => {
    try {
      loginLoading.value = true
      const response = await authApi.login(loginForm)
      
      token.value = response.data.token
      refreshToken.value = response.data.refreshToken
      userInfo.value = response.data.user
      permissions.value = response.data.user.permissions
      
      setToken(token.value)
      setRefreshToken(refreshToken.value)
      
      // 重置登录尝试次数
      loginAttempts.value = 0
    } catch (error) {
      loginAttempts.value++
      throw error
    } finally {
      loginLoading.value = false
    }
  }
  
  // 退出登录
  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      permissions.value = []
      removeToken()
      removeRefreshToken()
    }
  }
  
  return {
    token,
    userInfo,
    permissions,
    loginLoading,
    loginAttempts,
    isLoggedIn,
    hasPermission,
    login,
    logout
  }
})
```

#### 2. 路由守卫实现

```typescript
// router/guards/auth.ts
export function setupAuthGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()
    
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
      if (!authStore.isLoggedIn) {
        next({
          name: 'Login',
          query: { redirect: to.fullPath }
        })
        return
      }
      
      // 检查权限
      if (to.meta.permissions && Array.isArray(to.meta.permissions)) {
        const hasPermission = to.meta.permissions.some(permission => 
          authStore.hasPermission(permission)
        )
        
        if (!hasPermission) {
          message.error('没有权限访问该页面')
          next({ name: 'Dashboard' })
          return
        }
      }
    }
    
    next()
  })
}
```

#### 3. HTTP拦截器实现

```typescript
// api/request.ts
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const authStore = useAuthStore()
    
    if (error.response?.status === 401) {
      // 尝试刷新令牌
      try {
        await authStore.refreshToken()
        // 重新发送原请求
        return request(error.config)
      } catch (refreshError) {
        // 刷新失败，跳转到登录页
        authStore.logout()
        router.push('/login')
      }
    }
    
    return Promise.reject(error)
  }
)
```

### 后端实现

#### 1. Spring Security配置

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/user/auth/**").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler))
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

#### 2. JWT工具类实现

```java
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUser = (CustomUserDetails) userDetails;
            claims.put("userId", customUser.getUserId());
            claims.put("roles", customUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        }
        
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setId(UUID.randomUUID().toString())
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

#### 3. 认证服务实现

```java
@Service
@Transactional
@Slf4j
public class AuthApplicationService implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String username = request.getUsername();
        
        // 检查是否被锁定
        if (loginAttemptService.isBlocked(username, clientIp)) {
            throw new AuthenticationException("账户已被锁定，请稍后重试");
        }
        
        // 查询用户
        User user = userRepository.findByUsername(username)
            .orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailedAttempt(username, clientIp);
            throw new AuthenticationException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("账户已被禁用");
        }
        
        // 生成令牌
        UserDetails userDetails = loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = generateRefreshToken(user.getId());
        
        // 更新登录信息
        user.recordSuccessfulLogin(clientIp);
        userRepository.save(user);
        
        // 清除失败尝试记录
        loginAttemptService.clearFailedAttempts(username, clientIp);
        
        // 构建响应
        return LoginResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .expiresIn(jwtExpirationInMs / 1000)
            .user(UserInfoResponse.from(user))
            .build();
    }
    
    @Override
    public void logout(String token) {
        // 将令牌加入黑名单
        String jti = jwtUtil.getJtiFromToken(token);
        String blacklistKey = RedisKeyConstant.TOKEN_BLACKLIST + jti;
        
        long expiration = jwtUtil.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", 
                Duration.ofMilliseconds(expiration));
        }
    }
}
```

## 测试策略

### 单元测试

#### 1. 前端组件测试

```typescript
// tests/components/LoginView.test.ts
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import LoginView from '@/views/auth/LoginView.vue'

describe('LoginView', () => {
  const createWrapper = (props = {}) => {
    return mount(LoginView, {
      props,
      global: {
        plugins: [createPinia()]
      }
    })
  }

  it('renders login form correctly', () => {
    const wrapper = createWrapper()
    
    expect(wrapper.find('.system-title').text()).toBe('保险产品智能检核系统')
    expect(wrapper.find('input[placeholder="请输入用户账号"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="请输入登录密码"]').exists()).toBe(true)
  })

  it('validates required fields', async () => {
    const wrapper = createWrapper()
    
    await wrapper.find('form').trigger('submit')
    
    expect(wrapper.find('.ant-form-item-explain-error').exists()).toBe(true)
  })

  it('handles login success', async () => {
    const mockLogin = vi.fn().mockResolvedValue({})
    const wrapper = createWrapper()
    
    await wrapper.find('input[placeholder="请输入用户账号"]').setValue('admin')
    await wrapper.find('input[placeholder="请输入登录密码"]').setValue('password')
    await wrapper.find('form').trigger('submit')
    
    expect(mockLogin).toHaveBeenCalledWith({
      username: 'admin',
      password: 'password'
    })
  })
})
```

#### 2. 后端服务测试

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private LoginAttemptService loginAttemptService;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    @Test
    @DisplayName("登录成功测试")
    void login_Success() {
        // Given
        LoginRequest request = LoginRequest.builder()
            .username("admin")
            .password("password")
            .build();
        
        User user = User.builder()
            .id("user-id")
            .username("admin")
            .password("encoded-password")
            .status(UserStatus.ACTIVE)
            .build();
        
        when(loginAttemptService.isBlocked("admin", "127.0.0.1")).thenReturn(false);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");
        
        // When
        LoginResponse response = authService.login(request, "127.0.0.1");
        
        // Then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("admin");
        
        verify(userMapper).updateById(any(User.class));
        verify(loginAttemptService).clearFailedAttempts("admin", "127.0.0.1");
    }
    
    @Test
    @DisplayName("登录失败 - 用户名或密码错误")
    void login_InvalidCredentials() {
        // Given
        LoginRequest request = LoginRequest.builder()
            .username("admin")
            .password("wrong-password")
            .build();
        
        when(loginAttemptService.isBlocked("admin", "127.0.0.1")).thenReturn(false);
        when(userMapper.selectOne(any())).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("用户名或密码错误");
        
        verify(loginAttemptService).recordFailedAttempt("admin", "127.0.0.1");
    }
}
```

### 集成测试

#### API集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    @DisplayName("登录接口集成测试")
    void loginIntegration() {
        // Given
        User testUser = createTestUser();
        userMapper.insert(testUser);
        
        LoginRequest request = LoginRequest.builder()
            .username("testuser")
            .password("password123")
            .build();
        
        // When
        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.postForEntity(
            "/api/v1/auth/login", request, 
            new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData().getToken()).isNotBlank();
    }
}
```

### 性能测试

#### JMeter测试脚本

```xml
<!-- 登录接口性能测试 -->
<TestPlan>
  <ThreadGroup>
    <stringProp name="ThreadGroup.num_threads">100</stringProp>
    <stringProp name="ThreadGroup.ramp_time">10</stringProp>
    <stringProp name="ThreadGroup.duration">60</stringProp>
  </ThreadGroup>
  
  <HTTPSamplerProxy>
    <stringProp name="HTTPSampler.domain">localhost</stringProp>
    <stringProp name="HTTPSampler.port">8080</stringProp>
    <stringProp name="HTTPSampler.path">/api/v1/auth/login</stringProp>
    <stringProp name="HTTPSampler.method">POST</stringProp>
    <stringProp name="HTTPSampler.postBodyRaw">
      {
        "username": "admin",
        "password": "password123"
      }
    </stringProp>
  </HTTPSamplerProxy>
</TestPlan>
```

## 部署方案

### Docker容器化

#### Dockerfile
```dockerfile
# 前端构建
FROM node:18-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --only=production
COPY frontend/ ./
RUN npm run build

# 后端构建
FROM maven:3.9-openjdk-17 AS backend-builder
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline
COPY backend/src ./src
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17-jre-slim
WORKDIR /app

# 复制后端应用
COPY --from=backend-builder /app/backend/target/*.jar app.jar

# 复制前端静态文件
COPY --from=frontend-builder /app/frontend/dist ./static

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose配置
```yaml
version: '3.8'

services:
  auth-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
    networks:
      - auth-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: insurance_audit
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - auth-network

  redis:
    image: redis:7.0-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    networks:
      - auth-network

volumes:
  mysql_data:
  redis_data:

networks:
  auth-network:
    driver: bridge
```

### 监控和日志

#### 应用监控配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 日志配置
```yaml
logging:
  level:
    com.insurance.audit.auth: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/auth-service.log
```

## 规范一致性检查

### ✅ **已确保的规范一致性**

#### 后端规范对齐
- **DDD架构**: 采用完整的六边形架构和领域驱动设计
- **项目结构**: 按照backend-standards.md的完整DDD分层结构组织
- **模块命名**: 统一使用`com.insurance.audit.user`模块
- **实体设计**: 符合MyBatis Plus规范，使用统一的BaseEntity
- **枚举序列化**: 使用@JsonValue统一输出code
- **仓储模式**: 领域层定义接口，基础设施层实现

#### 前端规范对齐  
- **项目结构**: 完整的目录结构，包含composables、directives、plugins
- **技术栈**: Vue 3.4.x + Ant Design Vue 4.x + Pinia 2.x + TypeScript 5.x
- **组件设计**: 符合Composition API规范
- **状态管理**: 使用Pinia进行状态管理
- **路由管理**: 完整的路由守卫和权限控制

#### API设计规范
- **接口路径**: 统一使用`/api/v1/user/*`前缀
- **错误码**: 区分USER_和AUTH_前缀，符合规范要求
- **响应格式**: 统一的ApiResponse格式
- **安全设计**: JWT令牌管理和Spring Security配置

### 🎯 **设计特色**

1. **领域驱动**: 完整的DDD实现，业务逻辑封装在领域层
2. **安全优先**: 多层次的安全防护机制
3. **现代架构**: 前后端分离，微服务友好
4. **可扩展性**: 模块化设计，支持功能扩展
5. **规范统一**: 严格遵循项目开发规范

这个设计文档完全符合项目的前后端开发规范要求，为后续的开发实现提供了准确的技术指导，确保系统的安全性、可扩展性和可维护性。
##
 MVP阶段开发计划

### 第一阶段：认证功能开发（1-2周）

#### 后端开发任务
1. **用户实体和数据库设计**
   - 创建用户、角色、权限相关表结构
   - 实现基础的实体类和枚举定义
   - 配置MyBatis Plus映射关系

2. **Spring Security配置**
   - JWT认证过滤器实现
   - 安全配置和路由保护
   - 自定义用户详情服务

3. **认证接口实现**
   - 用户登录接口
   - 用户退出接口
   - 令牌刷新接口
   - 获取用户信息接口

4. **安全功能实现**
   - 密码加密和验证
   - 登录失败锁定机制
   - 操作日志记录

#### 前端开发任务
1. **项目基础搭建**
   - Vue 3 + Vite项目初始化
   - Ant Design Vue组件库集成
   - 路由和状态管理配置

2. **登录功能实现**
   - 登录页面UI实现
   - 表单验证和交互逻辑
   - JWT令牌管理
   - 错误处理和提示

3. **路由守卫实现**
   - 认证状态检查
   - 权限路由控制
   - 自动令牌刷新

### 第二阶段：工作台页面开发（1-2周）

#### 前端开发任务
1. **工作台布局实现**
   - 顶部导航栏组件
   - 主页面布局设计
   - 响应式适配

2. **数据展示组件**
   - 汇总数据卡片组件
   - 应用概要统计组件
   - 任务列表组件
   - 近期动态组件
   - 统计图表组件

3. **Mock数据服务**
   - Mock数据服务实现
   - API适配器设计
   - 搜索筛选逻辑模拟

4. **交互功能实现**
   - 时间维度切换
   - 任务搜索和筛选
   - 图表类型切换
   - 页面导航跳转

#### 测试和优化
1. **功能测试**
   - 登录流程测试
   - 页面交互测试
   - 响应式布局测试

2. **性能优化**
   - 组件懒加载
   - 图片资源优化
   - 代码分割优化

## 后续扩展规划

### 第三阶段：业务功能扩展

#### 产品管理模块
- 产品信息管理
- 产品文档上传
- 产品检核流程

#### 规则管理模块  
- 规则配置管理
- 规则执行引擎
- 规则审核流程

#### 数据统计模块
- 实时数据统计
- 报表生成功能
- 数据导出功能

### 第四阶段：高级功能

#### 权限管理增强
- 细粒度权限控制
- 数据权限管理
- 权限审批流程

#### 系统监控
- 操作日志查询
- 系统性能监控
- 异常告警机制

#### 集成功能
- 第三方系统集成
- API开放平台
- 数据同步机制

## 技术债务管理

### 当前技术债务
1. **Mock数据替换**: 需要在后续版本中替换为真实的后端接口
2. **权限控制简化**: 当前只实现基础角色控制，需要扩展为细粒度权限
3. **错误处理**: 需要完善全局错误处理和用户友好的错误提示
4. **性能优化**: 需要添加缓存策略和性能监控

### 技术债务偿还计划
1. **第三阶段**: 替换Mock数据，实现真实业务接口
2. **第四阶段**: 完善权限控制体系
3. **持续优化**: 性能监控和错误处理改进

## 部署和运维

### MVP阶段部署
- **前端**: 静态文件部署到CDN或Web服务器
- **后端**: Spring Boot应用部署到应用服务器
- **数据库**: MySQL数据库部署
- **缓存**: Redis缓存部署

### 监控和日志
- **应用监控**: 基础的健康检查和性能监控
- **日志管理**: 结构化日志记录和查询
- **错误追踪**: 异常信息收集和告警

### 安全措施
- **HTTPS**: 生产环境强制HTTPS
- **防火墙**: 网络访问控制
- **备份**: 数据库定期备份
- **更新**: 安全补丁及时更新

## 样式实现规范

### 全局样式变量定义

#### SCSS变量文件 (variables.scss)
```scss
// 色彩变量 - 与UI设计规范保持一致
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #faad14;
$error-color: #f5222d;
$info-color: #1890ff;
$processing-color: #722ed1;

// 中性色变量
$text-color: #262626;
$text-color-secondary: #595959;
$text-color-disabled: #bfbfbf;
$border-color: #d9d9d9;
$background-color: #fafafa;
$component-background: #ffffff;

// 字体变量
$font-family-chinese: "PingFang SC", "Microsoft YaHei", "微软雅黑", sans-serif;
$font-family-english: "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
$font-family-monospace: "SF Mono", Monaco, Inconsolata, "Roboto Mono", Consolas, "Courier New", monospace;

// 字体大小变量
$font-size-base: 14px;
$font-size-lg: 16px;
$font-size-xl: 20px;
$font-size-xxl: 24px;
$font-size-sm: 12px;
$font-size-xs: 10px;

// 字重变量
$font-weight-normal: 400;
$font-weight-medium: 500;
$font-weight-semibold: 600;

// 间距变量
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 圆角变量
$border-radius-sm: 4px;
$border-radius-base: 6px;
$border-radius-lg: 8px;

// 阴影变量
$box-shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.03);
$box-shadow-base: 0 2px 8px rgba(0, 0, 0, 0.06);
$box-shadow-lg: 0 4px 12px rgba(0, 0, 0, 0.15);

// 布局变量
$layout-header-height: 64px;
$layout-sidebar-width: 256px;
$layout-sidebar-collapsed-width: 80px;
$layout-content-padding: 24px;
$container-max-width: 1200px;

// 响应式断点
$screen-xs: 576px;
$screen-sm: 768px;
$screen-md: 992px;
$screen-lg: 1200px;
$screen-xl: 1600px;
```

#### 混入函数文件 (mixins.scss)
```scss
// 布局混入
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@mixin flex-column {
  display: flex;
  flex-direction: column;
}

// 文字处理混入
@mixin text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@mixin multi-line-ellipsis($lines: 2) {
  display: -webkit-box;
  -webkit-line-clamp: $lines;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// 按钮样式混入
@mixin button-variant($bg-color, $text-color: #fff, $border-color: $bg-color) {
  background-color: $bg-color;
  color: $text-color;
  border: 1px solid $border-color;
  
  &:hover {
    background-color: lighten($bg-color, 10%);
    border-color: lighten($border-color, 10%);
  }
  
  &:active {
    background-color: darken($bg-color, 5%);
    border-color: darken($border-color, 5%);
  }
  
  &:disabled {
    background-color: #f5f5f5;
    color: #bfbfbf;
    border-color: #d9d9d9;
    cursor: not-allowed;
  }
}

// 卡片样式混入
@mixin card-style {
  background: $component-background;
  border: 1px solid #f0f0f0;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow-base;
  padding: $spacing-lg;
}

// 响应式混入
@mixin respond-to($breakpoint) {
  @if $breakpoint == xs {
    @media (max-width: #{$screen-xs - 1px}) { @content; }
  }
  @if $breakpoint == sm {
    @media (min-width: $screen-xs) and (max-width: #{$screen-sm - 1px}) { @content; }
  }
  @if $breakpoint == md {
    @media (min-width: $screen-sm) and (max-width: #{$screen-md - 1px}) { @content; }
  }
  @if $breakpoint == lg {
    @media (min-width: $screen-md) and (max-width: #{$screen-lg - 1px}) { @content; }
  }
  @if $breakpoint == xl {
    @media (min-width: $screen-lg) { @content; }
  }
}

// 动画混入
@mixin transition($property: all, $duration: 0.3s, $timing: ease) {
  transition: $property $duration $timing;
}

@mixin fade-in($duration: 0.3s) {
  animation: fadeIn $duration ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
```

### 组件样式实现

#### 登录页面样式 (Login.vue)
```scss
<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  @include flex-center;
  padding: $spacing-lg;
  
  .login-panel {
    width: 100%;
    max-width: 400px;
    @include card-style;
    @include fade-in;
    
    @include respond-to(xs) {
      width: 90%;
      padding: $spacing-lg;
    }
  }
  
  .system-header {
    text-align: center;
    margin-bottom: $spacing-xl;
    
    .logo {
      width: 64px;
      height: 64px;
      margin-bottom: $spacing-md;
    }
    
    .system-title {
      font-size: $font-size-xxl;
      font-weight: $font-weight-semibold;
      color: $text-color;
      margin: 0;
    }
  }
  
  .error-alert {
    @include flex-center;
    gap: $spacing-sm;
    padding: 12px $spacing-md;
    margin-bottom: $spacing-lg;
    background-color: #fff2f0;
    border: 1px solid #ffccc7;
    border-radius: $border-radius-sm;
    color: $error-color;
    font-size: $font-size-base;
    
    .error-icon {
      font-size: $font-size-md;
    }
  }
  
  .login-form {
    .ant-form-item {
      margin-bottom: $spacing-lg;
    }
    
    .ant-input-affix-wrapper,
    .ant-input-password {
      height: 40px;
      border-radius: $border-radius-base;
      
      &:focus,
      &.ant-input-affix-wrapper-focused {
        border-color: #40a9ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      }
    }
    
    .ant-btn {
      height: 40px;
      border-radius: $border-radius-base;
      font-size: $font-size-base;
      font-weight: $font-weight-normal;
      
      &.ant-btn-primary {
        @include button-variant($primary-color);
      }
    }
  }
  
  .footer {
    position: fixed;
    bottom: $spacing-lg;
    left: 50%;
    transform: translateX(-50%);
    
    p {
      color: #8c8c8c;
      font-size: $font-size-sm;
      margin: 0;
    }
  }
}
</style>
```

#### 工作台页面样式 (Dashboard.vue)
```scss
<style lang="scss" scoped>
.dashboard-layout {
  min-height: 100vh;
  
  .dashboard-header {
    background: #001529;
    padding: 0;
    @include flex-between;
    
    .header-left {
      width: 256px;
      padding: 0 $spacing-lg;
      
      .system-brand {
        color: $component-background;
        font-size: $font-size-md;
        font-weight: $font-weight-medium;
      }
    }
    
    .header-nav {
      flex: 1;
      
      .ant-menu {
        background: transparent;
        border-bottom: none;
        
        .ant-menu-item {
          color: $component-background;
          
          &:hover {
            background-color: rgba(255, 255, 255, 0.1);
          }
          
          &.ant-menu-item-selected {
            background-color: $primary-color;
          }
        }
      }
    }
    
    .header-right {
      padding: 0 $spacing-lg;
      
      .user-info {
        color: $component-background;
        cursor: pointer;
        @include flex-center;
        gap: $spacing-sm;
        
        &:hover {
          color: #40a9ff;
        }
      }
    }
  }
  
  .dashboard-content {
    padding: $layout-content-padding;
    background: $background-color;
    
    .content-wrapper {
      max-width: $container-max-width;
      margin: 0 auto;
      display: grid;
      grid-template-columns: 2fr 1fr;
      grid-template-rows: auto auto auto;
      gap: $spacing-xl;
      
      @include respond-to(md) {
        grid-template-columns: 1fr;
        gap: $spacing-lg;
      }
    }
  }
}
</style>
```

#### 汇总数据组件样式 (SummaryDataSection.vue)
```scss
<style lang="scss" scoped>
.summary-data-section {
  grid-column: 1 / -1;
  
  .section-title {
    font-size: $font-size-xl;
    font-weight: $font-weight-medium;
    color: $text-color;
    margin-bottom: $spacing-lg;
  }
  
  .time-filters {
    margin-bottom: $spacing-lg;
    
    .ant-tabs {
      .ant-tabs-tab {
        font-size: $font-size-base;
        
        &.ant-tabs-tab-active {
          .ant-tabs-tab-btn {
            color: $primary-color;
          }
        }
      }
      
      .ant-tabs-ink-bar {
        background: $primary-color;
      }
    }
  }
  
  .summary-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: $spacing-md;
    
    .summary-card {
      @include card-style;
      text-align: center;
      @include transition;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: $box-shadow-lg;
      }
      
      .card-value {
        font-size: 28px;
        font-weight: $font-weight-semibold;
        color: $text-color;
        margin-bottom: $spacing-sm;
      }
      
      .card-label {
        font-size: $font-size-base;
        color: $text-color-secondary;
      }
    }
  }
}
</style>
```

### Ant Design Vue主题定制

#### 主题配置文件 (antd.ts)
```typescript
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

export const antdTheme: ThemeConfig = {
  token: {
    // 主色调
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#f5222d',
    colorInfo: '#1890ff',
    
    // 字体
    fontFamily: '"PingFang SC", "Microsoft YaHei", "微软雅黑", sans-serif',
    fontSize: 14,
    fontSizeHeading1: 24,
    fontSizeHeading2: 20,
    fontSizeHeading3: 16,
    
    // 圆角
    borderRadius: 6,
    borderRadiusLG: 8,
    borderRadiusSM: 4,
    
    // 间距
    padding: 16,
    paddingLG: 24,
    paddingSM: 12,
    paddingXS: 8,
    
    // 高度
    controlHeight: 32,
    controlHeightLG: 40,
    controlHeightSM: 24,
    
    // 阴影
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)',
    boxShadowSecondary: '0 4px 12px rgba(0, 0, 0, 0.15)',
  },
  components: {
    Button: {
      borderRadius: 6,
      controlHeight: 32,
      controlHeightLG: 40,
      controlHeightSM: 24,
    },
    Input: {
      borderRadius: 6,
      controlHeight: 32,
      controlHeightLG: 40,
    },
    Card: {
      borderRadius: 8,
      paddingLG: 24,
    },
    Layout: {
      headerHeight: 64,
      siderWidth: 256,
    },
    Menu: {
      itemHeight: 40,
      itemMarginInline: 4,
    },
  },
}
```

### CSS-in-JS样式方案（可选）

#### 样式工具函数
```typescript
// utils/styles.ts
export const createStyles = (theme: any) => ({
  // 布局样式
  flexCenter: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  
  flexBetween: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  
  // 文字样式
  textEllipsis: {
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  
  // 卡片样式
  cardStyle: {
    background: theme.token.colorBgContainer,
    border: `1px solid ${theme.token.colorBorder}`,
    borderRadius: theme.token.borderRadiusLG,
    boxShadow: theme.token.boxShadow,
    padding: theme.token.paddingLG,
  },
  
  // 响应式样式
  responsive: {
    xs: '@media (max-width: 575px)',
    sm: '@media (min-width: 576px)',
    md: '@media (min-width: 768px)',
    lg: '@media (min-width: 992px)',
    xl: '@media (min-width: 1200px)',
  },
})
```

---

本设计文档将随着MVP阶段的开发进展持续更新和完善，确保设计与实现的一致性。所有UI组件的实现都严格遵循UI设计规范标准，保证系统界面的专业性、一致性和用户体验。