# Technology Stack

## Project Type
Web应用（前后端分离）：前端基于 Vue 3 + Vite + TS，后端提供RESTful API与任务编排服务，支持本地与容器化部署，面向企业内网与专有云/数据中心环境。

## Core Technologies

### Primary Language(s)
- TypeScript（前端）
- Java（或 Kotlin）/ Spring 生态（后端服务）
- Python（AI/文本解析与向量/LLM侧车服务，可选）

### Key Dependencies/Libraries
- 前端：Vue 3、Pinia、Vue Router、Ant Design Vue、Axios、Vitest/Playwright、Sentry（可选）
- 后端：Spring Boot、Spring Security、MyBatis Plus、MySQL、Redis、XXL-Job/Quartz（任务调度）、OpenFeign（可选）、MinIO（对象存储，可选）
- AI与检核：
  - 语义检索：向量数据库 Milvus（或 PGVector/Faiss 作为备选）
  - 向量化模型：句向量（bge/ernie/simcse等）本地或云端
  - LLM：主流大模型（如 OpenAI、Azure OpenAI、Moonshot、阿里通义、百度文心、讯飞星火等）通过抽象提供商适配层接入
  - OCR/版面：PaddleOCR/PDFPlumber/DocLayNet（可选），用于PDF/表格/签章/日期等多模态增强

### Application Architecture
分层+可插拔能力：
- 网关/API 层：统一鉴权、限流、审计日志
- 业务域层：产品管理、检核任务、规则管理、报告中心
- 引擎层：
  - 规则引擎（关键词/正则/格式规则）
  - 语义引擎（向量召回+相似度）
  - LLM匹配器（复杂语义/跨要件校核，生成可追溯“理由”）
- 数据与索引层：MySQL（结构化）、Milvus（向量）、对象存储（原文/带批注文档）
- 集成层：OA/产品工厂单点跳转、回链检核结果

### Data Storage
- MySQL：产品、要件、规则、任务、报告、审批状态等
- Milvus：规则与要件片段的向量索引
- Redis：会话、幂等、短期队列与缓存
- MinIO/OSS：原文与带批注产物
- 数据格式：JSON 为主，报告可导出为 HTML/CSV/Excel/Word

### External Integrations
- LLM提供商：抽象接口+提供商适配，支持API Key / AK/SK，代理与重试
- OA/产品工厂：HTTP(S) 单点跳转+回链URL，必要时增加签名/一次性票据
- 鉴权：JWT（内部用户）/ SSO（企业统一登录，可选）

### Monitoring & Dashboard Technologies
- 前端工作台：检核任务实时进度、命中率趋势、规则健康度
- 后端监控：Spring Actuator、Prometheus/Grafana、ELK/EFK 日志栈
- 追踪：OpenTelemetry（可选）

## Development Environment

### Build & Development Tools
- 前端：Vite、pnpm、ESLint + Prettier、Vitest/Playwright
- 后端：Maven/Gradle、Checkstyle/SpotBugs、JUnit、Testcontainers
- AI侧车：Python venv/Poetry，FastAPI（推理服务），Docker 化

### Code Quality Tools
- 静态检查：ESLint/TypeScript-ESLint、Checkstyle/SpotBugs、Mypy（可选）
- 测试：单测+集成+E2E，CI 并行执行
- 文档：OpenAPI/Swagger、ADR（Architecture Decision Records）

### Version Control & Collaboration
- Git + GitFlow；代码评审必须；语义化提交与变更日志

### Dashboard Development
- HMR 热更新；端口：前端 3000，后端 8080，可配置；多实例靠不同端口与 Docker 网络

## Deployment & Distribution
- 目标环境：企业内网/专有云；Docker/K8s 优先
- 交付方式：镜像+Helm（可选）；离线私有部署
- 依赖要求：MySQL、Redis、Milvus、MinIO（可选）；允许替换为已管控组件
- 升级：灰度/蓝绿；向量索引与规则版本兼容策略

## Technical Requirements & Constraints

### Performance Requirements
- 单文档≤20s；单产品（≥5要件）≤100s；批量检核支持并行度可调
- LLM调用设置超时/并发上限与退避重试；规则>150条生效可用

### Compatibility Requirements
- 前端：现代浏览器；
- 后端：JDK 17+；MySQL 8.x；Milvus 2.x；Redis 6+
- 标准：HTTP/REST、JWT、OpenAPI 3、RBAC、审计留痕

### Security & Compliance
- 认证鉴权：JWT/SSO；细粒度 RBAC
- 传输与存储：TLS/HTTPS；敏感字段脱敏；日志脱敏；对象存储签名URL
- 访问边界：LLM走企业合规通道；可选本地化模型以实现“数据不出域”
- 审计：请求ID/链路追踪；操作日志；规则变更审计

### Scalability & Reliability
- 预计负载：万级历史产品批量回归检核；
- 可用性：多实例无状态服务；Milvus/DB 高可用；任务断点续跑
- 灾备：备份策略、RPO/RTO 目标明确

## Technical Decisions & Rationale
1. 向量数据库选型：优先 Milvus，因生态成熟与检索性能；如受限则 PGVector/Faiss 备选
2. LLM接入策略：抽象统一接口，支持多厂商切换与降级；保留本地推理通道
3. 引擎策略：先“关键词/正则+向量语义”兜底，再由 LLM 处理难以关键词化的规则；LLM 结果需可解释与可评估
4. 多模态：MVP 仅识别常见 Word 与结构化片段；PDF表格/签章在第二阶段引入
5. 对接：以“单点跳转+回链URL”为最小方案，后续逐步深度集成

## Known Limitations
- LLM 结果稳定性对提示工程敏感，需A/B评估与阈值门控
- PDF/表格/签章等多模态能力在MVP仅限基础版，复杂版面需要二期
- 云外网访问受限时需本地模型与离线化方案，资源占用较高
