# 规则配置系统 - 任务列表（rule-configuration-system）

## 概述
基于已批准的`requirements.md`与`design.md`，本任务清单拆解前后端实现步骤，覆盖路由/页面/组件、后端API、数据模型、状态流转、导入、权限与测试。

## 任务分类

### Phase 1: 前端路由与页面框架（优先级：高）

- [x] 1.1 新增规则模块路由
  - File: frontend/src/router/rules-routes.ts
  - 配置`/rules`、`/rules/new?type=single|double|format|advanced`、`/rules/success`、`/rules/jump`
  - Purpose: 建立规则模块导航体系，支持从dashboard-home跳转
  - _Leverage: frontend/src/router/*_
  - _Requirements: 导航与入口_
  - _Prompt: Role: 前端路由工程师 | Task: 新增并注册规则模块路由，支持参数与守卫 | Restrictions: 不破坏既有路由，保持命名一致 | Success: 路由可达、刷新可恢复、权限守卫生效_

- [x] 1.2 规则管理主界面骨架
  - File: frontend/src/views/rules/RulesListView.vue
  - 实现筛选栏、工具栏、表格与分页骨架（静态UI占位）
  - Purpose: 对齐原型结构，为后续数据接入做准备
  - _Leverage: Ant Design Vue 表单/表格组件_
  - _Requirements: 规则管理主界面_
  - _Prompt: Role: 前端开发 | Task: 构建页面结构与控件布局 | Restrictions: 文案/占位严格按原型 | Success: 视觉与交互结构与原型一致_

### Phase 2: 后端API与数据模型（优先级：高）

- [x] 2.1 规则主表与枚举建模
  - File: backend/src/main/java/.../domain/Rule.java, enums/*
  - 定义Rule主实体与审核/有效状态枚举
  - Purpose: 为列表筛选与状态流转提供基础
  - _Leverage: 现有实体与枚举模式_
  - _Requirements: 数据模型、状态机_
  - _Prompt: Role: 后端开发 | Task: 建模Rule与状态枚举并加索引字段 | Restrictions: 命名与设计对齐 | Success: 编译通过、迁移可执行_

- [x] 2.2 四类规则子结构
  - File: backend/src/main/java/.../domain/ruletypes/*
  - 定义SingleRule/DoubleRule/FormatRule/AdvancedRule结构
  - Purpose: 支撑创建/编辑字段校验
  - _Leverage: 设计文档数据模型章节_
  - _Requirements: 四类创建页面字段_
  - _Prompt: Role: 后端开发 | Task: 建模四类规则结构并与主表关联 | Restrictions: 字段名与原型一致 | Success: CRUD契约清晰、校验可用_

- [x] 2.3 数据库迁移脚本
  - File: backend/src/main/resources/db/migration/Vxxx__rules.sql
  - 建表、索引（last_updated_at、audit_status、effective_status）
  - Purpose: 提升查询与筛选性能
  - _Leverage: 现有Flyway/Liquibase配置_
  - _Requirements: 性能与分页_
  - _Prompt: Role: 数据库工程师 | Task: 编写迁移并验证回滚 | Restrictions: 生产安全、可重复执行 | Success: 上线无阻塞、索引命中良好_

- [x] 2.4 REST API 控制器与服务
  - File: backend/src/main/java/.../controller/RuleController.java
  - Endpoints: GET /api/rules, POST /api/rules, GET/PUT/DELETE /api/rules/{id}
  - Purpose: 提供规则列表与CRUD
  - _Leverage: 统一返回/异常处理框架_
  - _Requirements: 列表与查询、创建与更新_
  - _Prompt: Role: 后端开发 | Task: 实现契约、参数校验、分页 | Restrictions: 遵循REST与项目规范 | Success: 集成测试通过、代码评审通过_

- [x] 2.5 状态流转与提交OA接口
  - File: backend/src/main/java/.../controller/RuleStatusController.java
  - Endpoints: PATCH /audit-status, PATCH /effective-status, POST /submit-oa
  - Purpose: 支持审核状态与有效状态变更、批量提交OA
  - _Leverage: 设计文档接口定义_
  - _Requirements: 规则状态管理_
  - _Prompt: Role: 后端开发 | Task: 实现状态机与约束校验，返回友好信息 | Restrictions: 禁止非法流转 | Success: 用例齐全，错误码清晰_

- [x] 2.6 模板批量导入接口
  - File: backend/src/main/java/.../controller/RuleImportController.java
  - Endpoint: POST /api/rules/import (multipart)
  - Purpose: 支持Excel模板导入，返回明细
  - _Leverage: 现有文件上传与Excel库_
  - _Requirements: 模板批量导入_
  - _Prompt: Role: 后端开发 | Task: 实现导入解析、校验与事务 | Restrictions: 大文件与并发友好 | Success: 明细准确、失败可追踪_

### Phase 3: 前端数据接入与交互（优先级：高）

- [x] 3.1 列表与筛选接入
  - File: frontend/src/views/rules/RulesListView.vue
  - 调用GET /api/rules，绑定筛选项、已选条件与分页
  - Purpose: 实现“全部规则”列表与筛选交互
  - _Leverage: frontend/src/api/index.ts, useApiLoading_
  - _Requirements: 规则管理主界面_
  - _Prompt: Role: 前端开发 | Task: 完成API接入与状态管理 | Restrictions: UI文案一致、性能<3s | Success: 交互顺滑、筛选准确_

- [x] 3.2 顶部动作与批量操作
  - File: frontend/src/views/rules/RulesListView.vue
  - 实现导入、添加、全部规则、删除、单条规则创建、关注、模板批量导入、提交OA审核，支持全选/反选/批量操作
  - Purpose: 完成列表操作闭环
  - _Leverage: Ant Design 组件与现有确认弹窗模式_
  - _Requirements: 列表操作与状态管理_
  - _Prompt: Role: 前端开发 | Task: 完成批量选择与操作 | Restrictions: 按钮权限控制 | Success: 批量操作正确、反馈清晰_

- [x] 3.3 四类创建页面实现
  - File: frontend/src/views/rules/RuleCreate*.vue
  - 单句/双句/格式/高级：字段、占位、必填校验、提交/重置、Tab
  - Purpose: 对齐原型与需求的表单交互
  - _Leverage: 表单校验与组合式API_
  - _Requirements: 添加规则单句/双句/格式/高级_
  - _Prompt: Role: 前端开发 | Task: 实现四类表单与提交成功跳转 | Restrictions: 字段名与占位逐字一致 | Success: 表单校验完整、成功页可达_

- [x] 3.4 检核跳转页面实现
  - File: frontend/src/views/rules/RuleJumpView.vue
  - 标题与筛选控件、标签、选中规则列表，确定/取消
  - Purpose: 完成检核前选择流程
  - _Leverage: 现有筛选组件模式_
  - _Requirements: 检核跳转（规则页跳转）_
  - _Prompt: Role: 前端开发 | Task: 按原型实现交互 | Restrictions: 一致性与可访问性 | Success: 流程顺畅、条件生效_

### Phase 4: 测试与质量（优先级：高）

- [x] 4.1 后端集成测试
  - File: backend/src/test/java/.../rule/*
  - 覆盖列表分页筛选、CRUD、状态流转、导入
  - Purpose: 保证接口契约与业务正确
  - _Leverage: 现有测试基类与Mock工具_
  - _Requirements: 全部后端接口_
  - _Prompt: Role: QA | Task: 编写高覆盖率集成测试 | Restrictions: 独立可重复、含边界用例 | Success: 覆盖率≥80%，用例通过_

- [x] 4.2 前端组件与视图测试
  - File: frontend/src/test/rules/*
  - 组件单测（表单校验、筛选交互）、视图集成测试
  - Purpose: 保证主要交互稳定性
  - _Leverage: vitest + Vue Test Utils_
  - _Requirements: 主要前端功能_
  - _Prompt: Role: 前端QA | Task: 覆盖关键交互与错误提示 | Restrictions: 不依赖真实后端 | Success: 关键路径覆盖充分_

- [-] 4.3 E2E 测试
  - File: frontend/tests/e2e/rules.e2e.spec.ts
  - 场景：从工作台进入→规则列表筛选→创建单句规则→提交成功→返回列表；检核跳转筛选→确定
  - Purpose: 验证端到端用户旅程
  - _Leverage: Playwright_
  - _Requirements: 端到端_
  - _Prompt: Role: 自动化QA | Task: 编写稳定E2E脚本 | Restrictions: 使用mock或测试环境数据 | Success: CI稳定通过_

### Phase 5: 性能、权限与运维（优先级：中）

- [x] 5.1 权限控制与可见性
  - File: frontend/src/directives/permission.ts, backend 权限注解
  - Purpose: RULE_ADMIN/AUDITOR/VIEWER 按钮与接口权限
  - _Leverage: user-auth-system_
  - _Requirements: 权限与安全_
  - _Prompt: Role: 全栈 | Task: 接入权限指令与后端鉴权 | Restrictions: 最小权限原则 | Success: 非授权操作被拦截_

- [x] 5.2 性能优化与索引验证
  - File: backend/…，DB指标与Explain
  - Purpose: 确保筛选<3s、列表分页稳定
  - _Leverage: 数据库索引、缓存（可选）_
  - _Requirements: 性能_
  - _Prompt: Role: 后端性能工程师 | Task: 验证并优化慢查询 | Restrictions: 不改变业务语义 | Success: SLA满足、监控通过_

---

## 备注
- 开发顺序建议：Phase1→2→3并行穿插→4→5。
- 交付前核对UI文案与原型逐字一致；状态字典与接口枚举一致。


