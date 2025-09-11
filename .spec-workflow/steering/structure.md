# Project Structure

## Directory Organization

建议在项目根目录建立以下结构（可与现有 Docs/、Architecture/ 并存）：

```
ICS-Project/
├── Architecture/                  # 规范文档（已存在）
├── Docs/                          # 需求/功能集/交流材料（已存在）
├── .spec-workflow/                # 规格与转向工作流（自动生成）
│   └── steering/                  # product.md / tech.md / structure.md
├── backend/                       # 后端服务（Spring Boot）
│   ├── app/                       # 核心应用（API、业务域）
│   ├── engine/                    # 检核引擎子系统（规则/语义/LLM）
│   ├── integration/               # OA/产品工厂等对接
│   ├── common/                    # 共用组件（异常/审计/安全）
│   ├── scripts/                   # 构建/运维脚本
│   └── docker/                    # Docker/Helm/K8s
├── frontend/                      # 前端Web（Vue 3 + Vite）
│   ├── src/
│   │   ├── assets/                # 样式/图标/图片
│   │   ├── components/            # 通用/业务组件
│   │   ├── views/                 # 页面（工作台/产品/检核/规则）
│   │   ├── router/ stores/ api/   # 路由/状态/API封装
│   │   ├── utils/ composables/    # 工具与组合式函数
│   │   └── types/                 # 类型定义
│   └── tests/                     # 单元与E2E 测试
├── services/                      # AI侧车与工具服务（Python/Go）
│   ├── vectorizer/                # 向量化与向量入库
│   ├── llm-adapter/               # 多厂商LLM 适配
│   ├── ocr/                       # OCR/版面分析（可选）
│   └── common/                    # 共享协议/模型
├── data/                          # 示例数据/词表/提示词模板（脱敏）
├── ops/                           # DevOps/监控/仪表盘/脚本
└── README.md
```

## Naming Conventions

### Files
- 前端组件/页面：`PascalCase.vue`；工具/类型：`camelCase.ts`
- 后端包与类：`com.company.ics.module`，类名 `PascalCase`
- 服务/处理器：`XxxService`、`XxxController`、`XxxHandler`
- 测试：`*Test.java` / `*.spec.ts`

### Code
- 类型/类：PascalCase；函数：camelCase；常量：UPPER_SNAKE_CASE；变量：camelCase

## Import Patterns

- 前端使用路径别名 `@/...`；模块内相对导入
- 后端按分层/领域导入，禁止循环依赖；公共常量/类型集中在 `common`

## Code Structure Patterns

### Module/Class Organization
1. Imports
2. 常量/配置
3. 类型/接口
4. 主实现（领域服务/控制器/引擎算子）
5. 辅助函数
6. 导出/公开API

### Function/Method Organization
- 先做参数与权限校验；
- 中部核心业务/引擎逻辑；
- 失败早返回与有意义的异常；
- 清晰返回结构（统一响应模型）。

### File Organization Principles
- 一文件一职责；
- 相关功能聚合；
- 对外API清晰，隐藏实现细节；
- 遵循前后端各自规范文档（Architecture/）。

## Code Organization Principles
1. 单一职责
2. 高内聚、低耦合
3. 可测试性优先（接口抽象、依赖注入、可替换适配器）
4. 一致性与可读性

## Module Boundaries
- 核心域与引擎解耦：业务编排调用检核引擎，规则/语义/LLM 算子作为插件式组件
- 公共设施（审计/安全/存储/消息）独立模块，对上暴露稳定接口
- 对外集成层（OA/工厂/LLM供应商）通过适配器与防腐层隔离
- 依赖方向：上层依赖下层接口，禁止反向耦合

## Code Size Guidelines
- 前端 `.vue` 单文件建议 ≤300 行；逻辑抽到 composables
- 后端类文件建议 ≤400 行；方法 ≤80 行；嵌套 ≤3 层
- 引擎算子与规则处理器独立小单元，便于测试与复用

## Dashboard/Monitoring Structure
```
ops/
└── monitoring/
    ├── grafana/         # 仪表盘配置
    ├── prometheus/      # 指标抓取
    ├── loki/            # 日志聚合
    └── alerts/          # 告警规则
```
- 与业务解耦，可单独启停；最小依赖主应用

## Documentation Standards
- 所有公开API以 OpenAPI/Swagger 同步；
- 复杂引擎/规则含时序图或流程图；
- `README.md` 级联到各模块；
- 采用 ADR 记录关键架构决策与取舍。