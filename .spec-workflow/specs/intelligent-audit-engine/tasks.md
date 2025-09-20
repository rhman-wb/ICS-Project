# Tasks Document

- [x] 1. 建立检核作业编排器 Orchestrator 与任务API
  - File: backend/audit/Orchestrator.ts, backend/api/auditJobs.ts
  - 实现创建/查询/结果导出的API端点；Orchestrator 负责编排任务、并发、重试与链路追踪
  - Purpose: 提供任务创建与执行入口，承载批量/并发与可观测性
  - _Leverage: backend/api/base.ts, backend/utils/logger.ts, backend/utils/trace.ts_
  - _Requirements: IAER-001, IAER-010, 7.3, 7.6_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Backend Engineer (Node/TS). Task: Implement Orchestrator (createJob/execute/status/result export) and REST endpoints. Restrictions: follow existing API patterns; add tracing and error handling; do not block on long jobs (async). _Leverage: base api, logger, trace utils. _Requirements: IAER-001, IAER-010, 7.3, 7.6. Success: endpoints return 2xx; jobs execute asynchronously; progress and logs visible; errors retriable._

- [x] 2. 集成 RuleProvider 读取已生效规则版本
  - File: backend/audit/RuleProvider.ts
  - 从 `rule-configuration-system` 拉取规则快照（版本/阈值），含缓存与版本绑定
  - Purpose: 确保执行时使用正确版本与阈值，支持复检可追溯
  - _Leverage: backend/services/httpClient.ts_
  - _Requirements: IAER-001, IAER-015, 7.2, 10_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Integration Engineer. Task: Implement RuleProvider.getEffectiveRuleSet(ruleSetId) with caching and version pinning. Restrictions: read-only; do not mutate rule state; handle network timeouts. _Leverage: http client. _Requirements: IAER-015, 7.2, 10. Success: returns consistent snapshot with version + thresholds; cached with TTL; graceful retries._

- [x] 3. 文档提供与解析（Word/Excel）与切分
  - File: backend/audit/DocumentProvider.ts, backend/audit/parsers/WordParser.ts, .../ExcelParser.ts, backend/audit/Chunker.ts
  - 从 `product-document-management` 获取要件内容；解析样式/结构；句/章切分并生成索引
  - Purpose: 为匹配器提供标准化文本与格式元数据
  - _Leverage: backend/services/storage.ts, docx/office 解析库_
  - _Requirements: 7.4, 7.3_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Document Processing Engineer. Task: Implement Word/Excel parsing and chunking with section/sentence granularity and style metadata. Restrictions: preserve positions; support Chinese typography fields; robust to malformed docs. _Leverage: storage service, docx/xlsx libs. _Requirements: 7.4, 7.3. Success: parsed structure + styles available; chunk indices map back to page/paragraph/cell._

- [x] 4. 匹配器实现：关键词/正则、格式校验、语义相似度
  - File: backend/audit/matchers/KeywordMatcher.ts, FormatChecker.ts, SemanticMatcher.ts
  - 实现 A 出现/未出现/单独出现、A-B 关系；格式规则校验；向量检索（TopK/阈值）
  - Purpose: 实现核心检核能力
  - _Leverage: backend/utils/regex.ts, embedding svc client, vector db client_
  - _Requirements: IAER-001, IAER-010, 7.3, 7.4, 7.5_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: NLP/IR Engineer. Task: Implement keyword/regex and format checks; add semantic matching using embeddings + vector DB with thresholds and section constraints. Restrictions: deterministic for keyword/format; configurable thresholds; provide evidence spans. _Leverage: regex utils, embedding client, vector client. _Requirements: IAER-001, 7.3, 7.5. Success: unit tests pass; hits include evidence + location + similarity._

- [x] 5. LLM 检核编排（指令直推/逻辑拆解两种模式）
  - File: backend/audit/llm/LLMOrchestrator.ts
  - 根据规则类型与上下文选择模式，生成可追溯决策链
  - Purpose: 处理复杂语义/跨段落场景的裁决
  - _Leverage: llm-service-management 网关_
  - _Requirements: IAER-011, 7.3, 7.6, 7.7_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: LLM Engineer. Task: Implement LLMOrchestrator supporting prompt-mode and chain-of-thought task decomposition with context windows. Restrictions: redact sensitive data; log model/version/cost; add timeouts + retries. _Leverage: llm gateway. _Requirements: IAER-011, 7.3, 7.7. Success: decisions include rationale; fallbacks on timeout; cost accounted._

- [x] 6. 证据装配与结果存储、导出
  - File: backend/audit/EvidenceAssembler.ts, backend/audit/ReportExporter.ts
  - 装配命中项（规则ID/版本、证据、阈值、定位、建议）；导出 Word/PDF/JSON
  - Purpose: 可追溯、可审计与可导出
  - _Leverage: backend/services/report.ts_
  - _Requirements: IAER-008, IAER-009, 7.6_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Backend Engineer. Task: Implement evidence assembly and exporters (Word/PDF/JSON). Restrictions: preserve formatting for highlights/bold; include rule-version linkage; paginate large results. _Leverage: report service. _Requirements: IAER-008, IAER-009. Success: exported files open correctly; annotations visible; JSON schema validated._

- [x] 7. 性能与并发治理
  - File: backend/audit/Orchestrator.ts (并发/队列), backend/audit/matchers/SemanticMatcher.ts（批量检索）
  - 并发/队列、批量检索、缓存（规则快照/嵌入）、指标分层
  - Purpose: 满足 P95 与批量目标
  - _Leverage: backend/utils/queue.ts, metrics.ts, cache.ts_
  - _Requirements: IAER-010, 7.5, 7.6_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Performance Engineer. Task: Implement concurrency controls, batching, caching, and metrics. Restrictions: avoid thundering herd; backpressure; protect LLM/vector costs. _Leverage: queue/metrics/cache utils. _Requirements: 7.5, 7.6. Success: P95 meets targets; metrics dashboards show breakdown._

- [x] 8. 安全与合规落地
  - File: backend/audit/security.ts
  - 接入 `user-auth-system` 权限与数据域；脱敏与审计
  - Purpose: 合规与最小化原则落实
  - _Leverage: backend/middleware/auth.ts, audit logger_
  - _Requirements: 7.7, 8_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Security Engineer. Task: Enforce RBAC/data-scope checks; add PII redaction; ensure audit logs for decisions. Restrictions: deny by default; log access; configurable redaction. _Leverage: auth middleware. _Requirements: 7.7, 8. Success: access controlled; sensitive text redacted; audit trails complete._

- [x] 9. 端到端与基准测试
  - File: tests/e2e/audit.e2e.test.ts, tests/fixtures/golden/
  - 构建黄金集，覆盖文本/格式/高级规则；验证查全/查准、性能目标
  - Purpose: 质量门禁与回归
  - _Leverage: tests/helpers/testUtils.ts_
  - _Requirements: 12, 7.5_
  - _Prompt: Implement the task for spec intelligent-audit-engine, first run spec-workflow-guide to get the workflow guide then implement the task: Role: QA Automation Engineer. Task: Build golden set and E2E tests for coverage/accuracy/perf. Restrictions: stable seeds; CI runnable; perf thresholds enforced. _Leverage: test utils. _Requirements: 12, 7.5. Success: all journeys covered; perf asserts pass; regressions detected._


