# 规则配置系统设计文档（rule-configuration-system）

## 概述（Overview）
本设计文档基于已批准的`requirements.md`与原型页面，定义规则配置系统在前后端的技术方案、接口契约、数据模型与关键流程，覆盖：
- 页面与组件设计（列表、筛选、四类规则创建、添加成功、检核跳转）
- 后端服务与接口（规则增删改查、状态流转、批量导入、提交OA等）
- 权限、安全、错误处理、测试与性能要求

## 与项目规范的对齐（Steering Document Alignment）
- 技术规范（tech.md）
  - 后端：Spring Boot 分层（Controller / Service / Repository），参数校验使用JSR-380（Hibernate Validator），统一异常处理（@ControllerAdvice），统一返回结构。
  - 前端：Vue3 + Vite + TypeScript，组件化与组合式API，Ant Design Vue风格与`UI-standards.md`一致，遵循`frontend-standards.md`命名与目录组织。
- 结构规范（structure.md）
  - 前端按`views / components / api / stores / router`组织；后端按`controller / service / repository / domain / dto / mapper`组织；公共常量与枚举集中管理。

## 代码复用分析（Code Reuse Analysis）
- 前端可复用
  - API 基础封装：`frontend/src/api/index.ts`（HTTP 客户端、拦截器、错误处理）。
  - 加载态：`frontend/src/composables/useApiLoading.ts`。
  - 主题与UI配置：`frontend/src/config/antd.ts`、`frontend/src/config/theme.ts`。
  - 路由：`frontend/src/router/*`，在此新增规则模块路由。
- 后端可复用
  - 统一返回与异常拦截（项目现有基类/切面，如已存在则沿用），数据库访问用 MyBatis / JPA（以现项目为准）。

## 集成点（Integration Points）
- user-auth-system：前端携带Token/Session；后端基于用户角色控制接口权限（RULE_ADMIN、AUDITOR、VIEWER）。
- dashboard-home：提供顶部菜单“规则管理”和快捷入口的路由跳转。
- （可选）llm-service-management：后续若在“高级规则”中引入LLM能力，通过独立服务接口集成，本期不交付UI。

## 架构（Architecture）
```mermaid
flowchart LR
  subgraph Frontend (Vue3)
    A[RulesListView 规则管理主界面]
    B[RuleCreateSingleView 单句]
    C[RuleCreateDoubleView 双句]
    D[RuleCreateFormatView 格式]
    E[RuleCreateAdvancedView 高级]
    F[RuleSuccessView 添加成功]
    G[RuleJumpView 检核跳转]
    A -->|编辑/审核/添加| B & C & D & E
  end

  subgraph API (HTTP/JSON)
    X[Rules API]
  end

  subgraph Backend (Spring Boot)
    Ctl[RuleController]
    Svc[RuleService]
    Repo[(RuleRepository & Mappers)]
    DB[(Database)]
    Ctl-->Svc-->Repo-->DB
  end

  A & B & C & D & E & F & G --> X --> Ctl
```

## 前端设计（Components and Views）
### 路由（建议，不改变现有风格）
- `/rules`：规则管理主界面（列表、筛选、批量与分页）
- `/rules/new?type=single|double|format|advanced`：四类规则创建
- `/rules/success`：添加成功
- `/rules/jump`：检核跳转（规则页跳转）

### 视图与组件
- RulesListView
  - 由筛选栏（规则来源、适用险种、规则管理部门、审核状态、有效状态、适用章节、适用经营区域、输入查询、起始时间、终止时间、已选条件/清除全部）、列表表格（对齐原型表头）、批量操作与分页组成。
  - 顶部动作：导入、添加、全部规则、删除、单条规则创建、关注、模板批量导入、提交OA审核、返回、刷新。
- 创建视图（Single/Double/Format/Advanced）
  - 与原型字段、占位、必填提示一致；提交与重置；顶部Tab切换。
- RuleSuccessView
  - 显示“提交成功”，可“返回列表/查看项目/打 印”。
- RuleJumpView
  - 标题“产品检核 - 选择待检核产品”，关闭/确定/取消；筛选项与标签展示与原型一致。

### 前端状态与校验
- 表单校验：必填“*”项、长度（规则描述0/300）、下拉必选、时间区间有效性。
- 列表筛选：本地保存上次筛选条件（可选）；与URL同步（可选）。
- 权限可见性：按钮/操作按角色显示隐藏。

## 后端接口设计（REST API）
基路径建议：`/api/rules`（如项目已有前缀，以网关/应用配置为准）。

### 列表与查询
- GET `/api/rules`
  - 查询参数（均可选）：
    - `ruleSource`（规则来源）
    - `insuranceType`（适用险种）
    - `ruleDept`（规则管理部门）
    - `auditStatus`（审核状态）∈ {TECH_REVIEW, TO_SUBMIT_OA, SUBMITTED_OA, OA_APPROVED}
    - `effectiveStatus`（有效状态）∈ {VALID, INVALID, TO_BE_DEPLOYED}
    - `chapter`（适用章节）
    - `region`（适用经营区域）
    - `keyword`（输入查询：“请输入关键词”）
    - `startTime`, `endTime`（起始/终止时间）
    - `page`, `size`, `sort`
  - 返回：分页数据，表头字段与原型一致（含操作列所需元数据）。

### 创建与读取
- POST `/api/rules`
  - Body：通用规则对象，`type` ∈ {SINGLE, DOUBLE, FORMAT, ADVANCED}，并带对应子结构（见“数据模型”）。
  - 返回：规则详情。
- GET `/api/rules/{id}`：获取详情。

### 更新与删除
- PUT `/api/rules/{id}`：更新规则（按`type`校验对应字段）。
- DELETE `/api/rules/{id}`、DELETE `/api/rules`（批量，ids）。

### 状态流转与审核
- PATCH `/api/rules/{id}/audit-status`（或批量`/audit-status`）
  - Body：`{ auditStatus: "TO_SUBMIT_OA|SUBMITTED_OA|OA_APPROVED|TECH_REVIEW" }`
- PATCH `/api/rules/{id}/effective-status`
  - Body：`{ effectiveStatus: "VALID|INVALID|TO_BE_DEPLOYED" }`
- POST `/api/rules/submit-oa`
  - Body：`{ ids: number[] }` 批量提交OA审核（将状态置为`SUBMITTED_OA`），后续与OA系统集成预留Hook。

### 模板批量导入
- POST `/api/rules/import`（multipart/form-data）
  - 表单字段：`file`（Excel），模板字段与前端`public/template.xlsx`一致。
  - 返回：导入结果（成功/失败条目、错误明细）。

## 数据模型（Data Models）
为兼顾结构化查询与灵活扩展，采用“主表 + 分表/扩展JSON”方案：

### 1) 通用主表字段（Rule）
```json
{
  "id": 0,
  "type": "SINGLE|DOUBLE|FORMAT|ADVANCED",
  "ruleDescription": "规则描述",
  "ruleSource": "规则来源",
  "ruleDept": "规则管理部门",
  "insuranceType": "适用险种",
  "applicableRequirement": "适用要件",
  "chapter": "适用章节",
  "region": "适用经营区域",
  "specificRegion": "具体地方区域",
  "productNature": "产品性质",
  "auditStatus": "TECH_REVIEW|TO_SUBMIT_OA|SUBMITTED_OA|OA_APPROVED",
  "effectiveStatus": "VALID|INVALID|TO_BE_DEPLOYED",
  "lastUpdatedAt": "2022-10-31T00:00:00Z"
}
```

### 2) 单句规则（SingleRule）
```json
{
  "matchChapter": "匹配章节",
  "matchType": "匹配类型",
  "matchContent": "匹配内容",
  "triggerCondition": "触发条件",
  "hintType": "提示类型",
  "hintContent": "提示内容"
}
```

### 3) 双句规则（DoubleRule）
```json
{
  "A": {"matchChapter":"","matchType":"","matchContent":""},
  "B": {"matchChapter":"","matchType":"","matchContent":""},
  "triggerCondition": "触发条件",
  "hintType": "提示类型",
  "hintContent": "提示内容"
}
```

### 4) 格式规则（FormatRule）
```json
{
  "position": "匹配位置",
  "textContent": "具体文本匹配内容",
  "paragraphRules": "段落格式要求",
  "fontRules": "字体格式要求",
  "writingRules": "行文格式要求",
  "hintType": "提示类型",
  "hintContent": "提示内容"
}
```

### 5) 高级规则（AdvancedRule）
```json
{
  "hintType": "提示类型",
  "hintContent": "提示内容"
}
```

### 6) 枚举映射（与UI一致）
- 审核状态：TECH_REVIEW=待技术评估，TO_SUBMIT_OA=待提交OA审核，SUBMITTED_OA=已提交OA审核，OA_APPROVED=OA审核通过。
- 有效状态：VALID=有效，INVALID=无效，TO_BE_DEPLOYED=待开发上线。

## 权限与安全（Security）
- 角色：RULE_ADMIN（创建/编辑/删除/导入/提交OA/变更状态），AUDITOR（审核相关操作），VIEWER（只读）。
- 前端基于路由守卫与按钮级权限指令；后端基于注解/拦截器校验。

## 错误处理（Error Handling）
- 校验错误：400（字段/必填/长度/枚举非法），返回字段级错误明细。
- 业务错误：如非法状态流转、重复提交OA，返回业务码与可读提示。
- 服务错误：统一500，记录traceId，日志落盘。

## 性能与分页（Performance）
- 列表分页与条件查询均走索引；`last_updated_at`、`audit_status`、`effective_status`等字段建索引。
- 批量导入分批写入，单批大小可配（如500）。

## 测试策略（Testing Strategy）
- 单元测试：Service层规则校验、状态流转、导入解析。
- 集成测试：Controller接口契约、分页与筛选、OA提交流。
- 前端：组件与视图的vitest单测，Playwright E2E（列表筛选、四类创建、空态、成功页、检核跳转）。

## 日志与可观测性
- 关键操作（创建/编辑/删除/导入/提交OA/状态变更）审计日志；
- 接口耗时、错误率监控，阈值告警（后端AOP埋点或网关采集）。

## 迁移与兼容
- 数据库Migration（Flyway/Liquibase）创建：rules主表与四类子表（或主表+JSON扩展列）；
- 与现有产品文档模块联动留接口预留，不强绑定本期。


