# 用户认证与权限管理系统实现任务

## 项目初始化和基础架构

- [x] 1. 创建前端项目基础结构













  - 在根目录创建 `frontend` 文件夹
  - 使用 `npm create vue@latest frontend` 创建 Vue 3 + TypeScript 项目
  - 配置 Vite 构建工具和开发服务器
  - 安装并配置 Ant Design Vue 4.x UI组件库
  - 设置 Pinia 状态管理和 Vue Router 路由
  - 配置 Axios HTTP客户端和请求拦截器
  - 创建标准的前端目录结构（src/components, src/views, src/stores等）
  - 配置 TypeScript、ESLint、Prettier 等开发工具
  - _需求: 1.1, 2.1, 8.1_

- [x] 2. 创建后端项目基础结构





  - 在根目录创建 `backend` 文件夹
  - 使用 Spring Initializr 创建 Spring Boot 3.2.x 项目
  - 添加必要依赖：Spring Web、Spring Security、Spring Data JPA、MySQL Driver、Redis
  - 配置 Spring Security 6.x + JWT 安全框架
  - 集成 MyBatis Plus 3.5.x 数据访问层
  - 配置 MySQL 8.0+ 数据库连接
  - 配置 Redis 7.0+ 缓存支持
  - 设置 DDD 分层架构目录结构（interfaces、application、domain、infrastructure）
  - 配置 application.yml 和环境配置文件
  - _需求: 2.1, 6.1, 7.1_

## 数据库设计和实体模型

- [x] 3. 设计和创建数据库表结构





  - 创建数据库迁移脚本 `V1__init_schema.sql`
  - 创建用户表(users)包含id、username、password、real_name、email、phone、status等字段
  - 创建角色表(roles)支持管理员、业务用户、审核用户三种角色
  - 创建用户角色关联表(user_roles)建立多对多关系
  - 创建权限表(permissions)和角色权限关联表(role_permissions)
  - 创建操作日志表(operation_logs)记录用户登录、操作等行为
  - 为所有表添加created_at、updated_at、is_deleted等审计字段
  - 添加必要的索引（用户名唯一索引、状态索引等）和外键约束
  - _需求: 2.1, 6.1, 16.1_


- [x] 4. 实现后端实体类和枚举









  - 创建 BaseEntity 基础实体类，包含通用字段和MyBatis Plus注解
  - 实现 User 用户实体类，包含@TableName、@TableField、验证注解
  - 实现 Role 角色实体类和 Permission 权限实体类
  - 创建 UserStatus 枚举（ACTIVE、INACTIVE、LOCKED）
  - 创建 RoleType 枚举（ADMIN、BUSINESS_USER、AUDIT_USER）
  - 实现 OperationLog 操作日志实体类，记录操作类型、IP地址等
  - 确保所有枚举使用统一的序列化策略（@JsonValue输出code）
  - _需求: 2.1, 6.1, 16.1_

## 后端认证和权限系统

- [x] 5. 实现JWT认证机制








  - 创建 JwtUtil 工具类，实现JWT令牌生成、验证、解析功能
  - 实现 JwtAuthenticationFilter JWT认证过滤器，拦截请求验证令牌
  - 创建 CustomUserDetailsService 用户详情服务，加载用户信息和权限
  - 实现 JwtAuthenticationEntryPoint 认证入口点，处理未认证请求
  - 实现 JwtAccessDeniedHandler 访问拒绝处理器
  - 配置 SecurityConfig 安全配置类，设置URL权限、过滤器链等
  - 配置密码编码器（BCryptPasswordEncoder）
  - _需求: 2.1, 7.1_

- [x] 6. 实现用户认证服务层









  - 创建 AuthService 认证服务接口和 AuthServiceImpl 实现类
  - 实现用户登录验证逻辑，包含用户名密码验证、BCrypt密码加密验证
  - 实现登录失败次数限制（5次）和账户锁定机制（30分钟）
  - 实现JWT令牌生成和刷新功能，设置合理的过期时间
  - 实现用户退出登录功能，将令牌加入黑名单
  - 添加登录成功/失败日志记录功能，记录IP地址、用户代理等信息
  - 实现Redis缓存用户会话信息
  - _需求: 2.1, 2.2, 2.5, 7.1, 16.1_

- [x] 7. 实现用户管理服务层









  - 创建 UserService 用户管理服务接口和实现类
  - 实现用户CRUD操作（创建、查询、更新、删除）
  - 实现密码管理功能（修改密码、重置密码）
  - 实现角色分配功能，支持用户角色的添加和移除
  - 实现权限验证功能，检查用户是否具有特定权限
  - 创建 UserMapper MyBatis映射器，实现数据访问层
  - 创建 RoleMapper 和 PermissionMapper 映射器
  - 添加用户操作的审计日志，记录所有用户管理操作
  - _需求: 6.1, 6.2, 16.2_


## 后端API接口实现

- [x] 8. 实现认证相关API接口












  - 创建 AuthController 认证控制器，使用@RestController和@RequestMapping注解
  - 实现 POST /api/v1/auth/login 登录接口，接收LoginRequest返回LoginResponse
  - 实现 POST /api/v1/auth/logout 退出接口，清除用户会话和令牌黑名单
  - 实现 POST /api/v1/auth/refresh 令牌刷新接口，验证refresh token生成新的access token
  - 创建LoginRequest、LoginResponse等DTO类，添加@Valid验证注解
  - 添加全局异常处理器，统一处理认证异常和参数验证异常
  - 配置Swagger接口文档，添加@Operation和@ApiResponse注解
  - 编写单元测试和集成测试用例

  - _需求: 2.1, 2.2, 7.3_


- [x] 9. 实现用户管理API接口








  - 创建 UserController 用户管理控制器
  - 实现 GET /api/v1/users/profile 获取当前用户信息接口
  - 实现 PUT /api/v1/users/profile 更新用户信息接口
  - 实现 POST /api/v1/users/change-password 修改密码接口
  - 创建UserProfileRequest、UserProfileResponse、ChangePasswordRequest等DTO
  - 添加@PreAuthorize权限控制注解，确保用户只能访问自己的信息
  - 添加参数验证注解（@NotBlank、@Email、@Length等）
  - 实现统一的API响应格式（ApiResponse<T>）
  - _需求: 6.1, 7.1_

## 前端登录界面实现

- [x] 10. 创建登录页面基础结构








  - 创建 `src/views/auth/Login.vue` 登录页面组件
  - 实现响应式布局，使用Flexbox居中登录面板
  - 添加系统标识区域，显示"保险产品智能检核系统"标题和Logo
  - 设置专业的渐变背景设计（深蓝到紫色渐变）
  - 添加底部版权信息区域，显示年份和公司信息
  - 使用CSS媒体查询确保在不同设备（桌面、平板、手机）下的适配效果
  - 配置路由，设置登录页为默认路由
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_
-

- [x] 11. 实现登录表单和输入框交互










  - 使用Ant Design Vue的a-form和a-input组件创建登录表单
  - 添加用户名输入框，配置UserOutlined图标和占位符"请输入用户账号"
  - 添加密码输入框，配置LockOutlined图标和a-input-password组件
  - 实现输入框焦点状态的视觉高亮效果（蓝色边框和阴影）
  - 添加allow-clear属性实现清除按钮功能
  - 设置输入长度限制（最大20个字符）
  - 实现Tab键在输入框间切换焦点
  - 实现Enter键提交表单功能
  - _需求: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_


- [x] 12. 实现登录按钮交互和状态管理









  - 使用a-button组件创建登录按钮，设置type="primary"和size="large"
  - 实现按钮禁用/启用状态逻辑（用户名或密码为空时禁用）
  - 添加loading状态，使用:loading属性显示加载动画
  - 实现按钮文字动态变化（正常："登录"，加载中："登录中..."）
  - 添加防重复点击保护机制，登录过程中禁用按钮
  - 使用Vue 3 Composition API管理按钮状态
  - 添加按钮悬停和点击的视觉反馈效果
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.6_

- [x] 13. 实现错误提示系统







  - 创建错误提示组件，使用a-alert组件显示错误信息
  - 实现不同类型错误的提示信息映射（用户名为空、密码为空、认证失败等）
  - 添加ExclamationCircleOutlined红色警告图标
  - 实现错误提示的自动消失机制（用户开始输入时清除错误）
  - 添加网络异常、服务器错误等特殊错误处理
  - 使用message.error()显示全局错误提示
  - 实现错误提示的淡入淡出动画效果
  - _需求: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

## 前端状态管理和API集成

- [x] 14. 实现认证状态管理







  - 创建 `src/stores/modules/auth.ts` 认证状态管理store
  - 使用Pinia defineStore定义auth store，管理登录状态、用户信息、令牌等
  - 实现login action，调用登录API并存储用户信息和令牌
  - 实现logout action，清除用户信息、令牌并跳转到登录页
  - 实现refreshToken action，自动刷新过期令牌
  - 添加用户信息（id、username、realName、roles）和权限信息存储
  - 实现localStorage和sessionStorage的令牌持久化
  - 添加isLoggedIn、hasPermission等计算属性
  - 实现认证状态的响应式更新，支持多组件同步
  - _需求: 2.1, 7.1, 7.2_

- [x] 15. 集成登录API调用







  - 创建 `src/api/modules/auth.ts` 认证相关API服务模块
  - 配置Axios实例，设置baseURL、timeout等基础配置
  - 实现login、logout、refreshToken等API调用函数
  - 创建请求拦截器，自动添加Authorization头部
  - 创建响应拦截器，处理401未授权、403禁止访问等状态码
  - 实现自动令牌刷新机制，当access token过期时自动使用refresh token
  - 实现错误处理和重试机制，网络错误时自动重试
  - 添加请求加载状态管理，与UI组件的loading状态联动
  - 创建API类型定义（LoginRequest、LoginResponse等）
  - _需求: 2.1, 2.2, 5.6_

## 前端工作台页面实现（Mock数据）

- [x] 16. 创建工作台页面基础布局



  - 创建 `src/views/dashboard/Dashboard.vue` 工作台主页面
  - 使用a-layout组件实现页面整体布局结构
  - 实现顶部导航栏（a-layout-header），设置深蓝色背景（#001529）
  - 添加系统品牌标识区域，左侧显示"产品检核系统"文字
  - 添加用户信息显示区域，右上角显示当前用户名和下拉菜单
  - 创建主导航菜单（a-menu），包含"主页"、"产品管理"、"规则管理"、"设置"四个菜单项
  - 实现当前页面高亮功能，使用selectedKeys属性
  - 实现菜单切换功能，点击菜单项时更新路由
  - 设置响应式布局，确保在不同屏幕尺寸下的适配
  - _需求: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [x] 17. 实现数据汇总展示组件





  - 创建 `src/components/dashboard/SummaryDataSection.vue` 汇总数据组件
  - 使用a-row和a-col实现四个指标的响应式卡片布局（xl:6, lg:6, md:12, sm:24）
  - 使用a-card组件展示产品数量、要件数量、规则数量、已检核错误数量
  - 设置大号字体（28px）显示数值，小号字体（14px）显示标签
  - 实现时间筛选选项，使用a-tabs组件（总计、本日、本周、本月）
  - 创建Mock数据服务，模拟不同时间维度的统计信息
  - 实现数据切换动画效果和loading状态
  - _需求: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

- [x] 18. 实现我的关注任务列表组件





  - 创建 `src/components/dashboard/MyFocusSection.vue` 关注任务组件
  - 使用a-list组件实现任务列表的展示，支持虚拟滚动
  - 添加任务名称（可点击链接）、提交人、提交时间（YYYY/MM/DD格式）显示
  - 在任务名称前添加相应的图标标识（FileTextOutlined等）
  - 实现任务点击跳转功能（暂时跳转到占位页面）
  - 使用Mock数据模拟任务信息，包含不同类型的产品检核任务
  - 当任务列表较长时，实现滚动查看更多任务功能
  - _需求: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

- [x] 19. 实现任务搜索与筛选功能





  - 在MyFocusSection组件中添加搜索输入框（a-input），占位符"请输入任务名称"
  - 实现实时搜索过滤功能，使用computed属性过滤任务列表
  - 添加按时间筛选的下拉选择器（a-select），选项包含"全部时间"、"本周"、"本月"
  - 添加按任务类型筛选的下拉选择器，选项包含"全部任务"、"产品检核"、"规则配置"
  - 实现搜索条件清空功能，提供清除按钮
  - 当搜索无结果时，使用a-empty组件显示友好的空状态提示
  - 实现筛选条件的组合逻辑，支持多条件同时筛选
  - _需求: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_

- [x] 20. 实现快速开始便捷导航组件





  - 创建 `src/components/dashboard/QuickStartSection.vue` 快速导航组件
  - 使用a-row和a-col实现功能按钮的响应式卡片布局（2x4网格）
  - 添加功能按钮：导入产品、创建规则、批量导入、下载模板
  - 使用a-button组件，设置合适的图标（UploadOutlined、PlusOutlined等）
  - 实现按钮点击跳转功能（暂时跳转到占位页面或显示message提示）
  - 在按钮右上角添加数字徽章（a-badge），显示待处理数量（如"24"）
  - 预留扩展按钮位置（操作四、操作五），保持布局平衡
  - 添加按钮悬停效果和点击反馈动画
  - _需求: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8_

- [x] 21. 实现近期动态展示组件





  - 创建 `src/components/dashboard/RecentActivitiesSection.vue` 近期动态组件
  - 使用a-timeline组件实现时间线式布局
  - 添加不同类型动态的格式化显示："[用户名]上传了 [产品名称]"、"[用户名] 更新了[数量]条规则"
  - 产品名称使用a-button type="link"实现蓝色链接样式
  - 实现相对时间显示（几秒前、14分钟前、20分钟前等），使用dayjs库
  - 添加"查看更多"链接，点击展开更多历史动态
  - 使用Mock数据模拟不同类型的系统活动
  - 设置适当的间距样式和分隔线
  - _需求: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8_

- [x] 22. 实现应用概要统计组件





  - 创建 `src/components/dashboard/ApplicationOverviewSection.vue` 应用概要组件
  - 使用a-row和a-col实现2x4布局，展示累计统计和近期统计
  - 累计统计：累计上传产品数量、累计导入规则数量、错误检出率、平均检核时间
  - 近期统计：近7日上传产品数量、近30日上传产品数量、近7日导入规则数量、近30日导入规则数量
  - 使用大号字体（20px）显示关键数字，设置字重600
  - 添加同比数据显示，包含百分比变化和上升/下降箭头图标
  - 上升趋势使用红色箭头（ArrowUpOutlined），下降趋势使用绿色箭头（ArrowDownOutlined）
  - 平均检核时间使用"HH:MM:SS"格式显示（如"00:02:27"）
  - _需求: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8_

- [x] 23. 实现数量统计占比图表组件





  - 创建 `src/components/dashboard/StatisticsChartSection.vue` 统计图表组件
  - 使用a-tabs实现选项卡切换：产品管理部门、产品类型、规则类型
  - 集成图表库（如Apache ECharts或Chart.js），实现饼图展示
  - 产品类型包含：种植险、意健险、车险、家财险、养殖险、其他
  - 在饼图旁边显示类型列表，包含彩色圆点标识、类型名称、占比百分比、具体数量
  - 确保所有百分比总和为100%，使用不同颜色区分各个类型
  - 实现选项卡切换时图表和列表数据的更新
  - 使用Mock数据模拟各种统计占比信息
  - _需求: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8_

## 路由和权限控制

- [x] 24. 实现前端路由配置和守卫





  - 配置 `src/router/index.ts` Vue Router路由表，包含登录、工作台、404等路由
  - 实现路由懒加载，使用() => import()语法进行代码分割
  - 创建 `src/router/guards/auth.ts` 认证路由守卫
  - 在beforeEach守卫中检查认证状态，未登录用户重定向到登录页
  - 实现登录成功后的页面跳转逻辑，支持redirect参数
  - 添加页面标题设置，根据路由meta.title动态更新document.title
  - 实现路由权限验证，检查用户是否有访问特定页面的权限
  - 配置路由模式为history模式，设置base路径
  - _需求: 4.5, 6.4, 8.1_

- [x] 25. 实现权限控制和角色管理




  - 创建 `src/directives/permission.ts` 权限验证指令（v-permission）
  - 创建 `src/components/common/PermissionWrapper.vue` 权限包装组件
  - 实现基于角色的功能显示控制（管理员、业务用户、审核用户）
  - 在auth store中添加hasRole、hasPermission等权限检查方法
  - 实现菜单项的权限控制，根据用户角色显示/隐藏菜单
  - 创建权限不足的提示页面（403 Forbidden）
  - 在路由守卫中添加页面级权限验证
  - 实现功能按钮的权限控制，无权限时禁用或隐藏按钮
  - _需求: 6.1, 6.2, 6.3, 6.4, 6.5_

## Mock数据服务


- [x] 26. 创建Mock数据服务




  - 创建 `src/api/mock/dashboardMock.ts` 工作台模拟数据服务
  - 实现getSummaryData方法，生成不同时间维度的汇总数据Mock
  - 实现getOverviewData方法，生成应用概要统计的Mock数据
  - 创建getFocusTasks方法，生成关注任务列表Mock数据，支持搜索和筛选参数
  - 实现getRecentActivities方法，生成近期动态Mock数据，包含不同类型的操作
  - 实现getStatisticsData方法，生成统计图表Mock数据，支持不同类型切换
  - 使用合理的随机数据生成，确保数据的真实性和一致性
  - 添加数据更新的模拟延迟，使用Promise.resolve()包装返回结果
  - 创建数据类型定义文件，确保Mock数据与实际API接口类型一致
  - _需求: 9.1-15.8（所有工作台展示需求）_

## 样式和UI优化

- [x] 27. 实现全局样式和主题配置





  - 创建 `src/assets/styles/` 样式目录结构
  - 创建variables.scss文件，定义全局CSS变量（主色#1890FF、辅助色、间距等）
  - 创建mixins.scss文件，定义常用的混入函数（flex-center、text-ellipsis等）
  - 实现深蓝色主题配置，体现保险行业的专业性和可信度
  - 配置Ant Design Vue主题定制，覆盖默认的主色调和组件样式
  - 添加响应式设计断点（xs、sm、md、lg、xl），确保移动端适配
  - 配置字体栈（中文：PingFang SC、微软雅黑；英文：Segoe UI、Roboto）
  - 创建全局样式重置文件，统一不同浏览器的默认样式
  - _需求: 所有UI/UX设计要求_

- [x] 28. 优化交互体验和动画效果





  - 添加页面切换动画，使用Vue Transition组件实现淡入淡出效果
  - 实现数据加载的骨架屏效果，使用a-skeleton组件
  - 添加按钮悬停和点击反馈动画，使用CSS transition和transform
  - 实现错误提示的淡入淡出效果，配置message组件的动画时长
  - 优化表单验证的实时反馈，添加输入框状态变化动画
  - 添加卡片悬停效果，提升视觉层次感
  - 实现数据更新时的loading状态指示器
  - 配置全局动画时长和缓动函数，保持动画效果的一致性
  - _需求: 所有交互体验要求_

## 编译错误修复和代码一致性

- [x] 31. 修复User实体类编译错误






  - 为User实体类添加@SuperBuilder和@Data注解，支持测试中的builder模式
  - 修复字段名与数据库表一致：loginFailureCount对应login_failure_count
  - 修复字段名与数据库表一致：avatar对应avatar（而非bio）
  - 确保User实体支持toBuilder()方法和继承字段的builder支持
  - 验证所有字段的getter/setter方法正确生成
  - 添加兼容性方法支持旧的字段名（getLoginAttempts/setLoginAttempts）
  - _需求: 测试编译通过，数据库字段一致性_

- [x] 32. 修复UserMapper接口缺失方法




  - 基于数据库表结构添加lockAccount方法，更新locked_until字段
  - 添加updatePassword方法，更新password字段（需要BCrypt加密）
  - 添加updateStatus方法，更新status字段为UserStatus枚举值
  - 添加unlockUser方法，清空locked_until和重置login_failure_count
  - 添加updateLastLogin方法，更新last_login_time和last_login_ip字段
  - 添加resetLoginAttempts方法，重置login_failure_count为0
  - 添加incrementLoginAttempts方法，递增login_failure_count
  - 添加deleteByUsername方法，根据用户名软删除用户
  - 添加selectAll方法，查询所有未删除用户
  - 确保所有方法与数据库表字段名一致（login_failure_count而非loginAttempts）
  - _需求: 测试编译通过，数据库字段一致性_

- [ ] 33. 重新生成测试工具类和测试文件



  - 删除现有的测试文件，重新生成完整的测试套件
  - 重新创建TestDataFactory，确保与更新后的实体类一致
  - 重新生成CustomUserDetails相关测试工具类
  - 重新创建JwtUtil测试，确保方法签名一致
  - 重新生成LoginResponse、RefreshTokenRequest等DTO测试
  - 重新创建MockDataService测试工具类
  - 重新生成TestSecurityUtils安全测试工具类
  - 确保所有测试数据与数据库表结构一致
  - 创建完整的单元测试和集成测试覆盖
  - _需求: 测试编译通过，完整测试覆盖_

- [x] 34. 修复服务层接口与实现一致性




  - 检查AuthService接口与AuthServiceImpl实现类方法签名一致性
  - 添加缺失的isTokenBlacklisted方法到AuthService接口
  - 检查UserService接口与UserServiceImpl实现类方法签名一致性
  - 添加缺失的getAllUsers方法到UserService接口
  - 修复resetPassword方法参数类型，使用ResetPasswordRequest而非String参数
  - 确保所有服务方法返回类型与接口定义一致
  - 检查方法参数注解（@Valid、@NotNull等）在接口和实现中的一致性
  - 验证异常声明在接口和实现中的一致性
  - _需求: 服务层接口实现一致性，测试编译通过_

- [x] 35. 完善实体类与数据库表结构一致性




  - 验证User实体类字段与users表完全一致
  - 验证Role实体类字段与roles表完全一致（确保包含name、code、description、status字段）
  - 验证Permission实体类字段与permissions表完全一致（确保包含name、code、type、parent_id、path、component、icon、sort_order、status字段）
  - 检查所有@TableField注解与数据库列名完全匹配
  - 验证字段类型与数据库列类型兼容（VARCHAR对应String，INT对应Integer等）
  - 检查字段长度限制与数据库列定义一致（@Size注解与VARCHAR长度匹配）
  - 确保枚举字段的数据库存储值与枚举定义一致
  - 验证外键关联字段的正确性（user_id、role_id等）
  - 检查索引字段在实体类中有对应的查询方法
  - _需求: 实体类与数据库表结构完全一致_

- [x] 36. 重新生成完整的DTO类和请求响应对象








  - 删除现有的DTO类，重新生成与实体类一致的DTO
  - 重新创建LoginRequest、LoginResponse，确保字段与User实体一致
  - 重新创建RefreshTokenRequest、RefreshTokenResponse
  - 重新创建CreateUserRequest、UpdateUserRequest、UserResponse
  - 重新创建ChangePasswordRequest、ResetPasswordRequest
  - 重新创建UserQueryRequest、UserProfileRequest、UserProfileResponse
  - 确保所有DTO类包含正确的getter/setter方法
  - 添加完整的验证注解（@NotBlank、@Email、@Size等）
  - 确保DTO与实体类字段映射的正确性
  - 添加Swagger文档注解（@Schema、@ApiModel等）
  - _需求: DTO类完整性，与实体类字段一致_

## 任务执行建议

**执行顺序建议：**
1. 先执行任务35（实体类与数据库一致性）- 确保基础数据模型正确
2. 再执行任务32（UserMapper接口）- 基于正确的实体类添加数据访问方法
3. 然后执行任务36（DTO类重新生成）- 基于正确的实体类生成传输对象
4. 接着执行任务34（服务层一致性）- 确保服务接口与实现匹配
5. 最后执行任务33（重新生成测试）- 基于所有修复后的类生成完整测试

**重点注意事项：**
- 所有涉及实体类的修改都要确保与database/init/01-init-schema.sql中的表结构完全一致
- 字段名必须与数据库列名匹配（如login_failure_count而非loginAttempts）
- 字段类型和长度限制必须与数据库定义兼容
- 测试文件建议完全删除重新生成，避免旧代码的兼容性问题
- 所有枚举值必须与数据库中存储的值一致

## 测试和部署

- [x] 29. 编写单元测试和集成测试








  - 为后端认证服务编写JUnit 5单元测试，测试登录、令牌生成、权限验证等功能
  - 为前端登录组件编写Vue Test Utils组件测试，测试表单验证、按钮状态等
  - 添加后端API接口的集成测试，使用@SpringBootTest和TestContainers
  - 实现前端端到端测试，使用Playwright覆盖登录到工作台的完整用户流程
  - 配置测试覆盖率报告，使用JaCoCo（后端）和Vitest（前端）
  - 设置CI/CD集成，在GitHub Actions中自动运行测试
  - 创建测试数据和Mock服务，确保测试的独立性和可重复性
  - _需求: 所有功能需求的测试覆盖_

- [x] 30. 配置生产环境部署



  - 配置前端项目的生产构建，优化打包体积和加载性能
  - 创建后端项目的Dockerfile，实现多阶段构建优化镜像大小
  - 创建docker-compose.yml文件，编排前端、后端、数据库、Redis等服务
  - 配置数据库迁移脚本和初始数据（管理员账户、基础角色权限等）
  - 实现环境变量配置，分离开发、测试、生产环境的配置
  - 配置Nginx反向代理，处理前端静态资源和API请求转发
  - 添加健康检查端点，配置监控和日志收集
  - 实现HTTPS配置和安全头设置，确保生产环境安全性
  - _需求: 性能要求、安全性要求、可用性要求_