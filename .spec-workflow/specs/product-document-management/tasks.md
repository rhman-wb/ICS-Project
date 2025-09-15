# 产品文档管理模块 - 任务列表

## 概述

本文档定义产品文档管理模块的开发任务，包括前端和后端的具体实现任务。

## 任务分类

### Phase 1: 核心基础功能 (优先级: 高)

#### 任务 1: 项目初始化和环境配置
- [x] 1.1 前端项目结构设置
  - File: frontend/src/views/ProductManagementView.vue
  - 设置 Vue 3 + TypeScript + Ant Design Vue 项目结构
  - 配置路由和基础组件架构
  - Purpose: 建立产品管理模块的前端基础架构
  - _Leverage: 现有的 dashboard-home 模块路由和组件结构_
  - _Requirements: 1, 2_

- [x] 1.2 后端项目结构设置
  - File: backend/src/main/java/com/insurance/audit/product/
  - 创建产品管理模块的包结构和基础配置
  - 设置 Spring Boot + MyBatis Plus + MySQL 配置
  - Purpose: 建立产品管理模块的后端基础架构
  - _Leverage: 现有的 common 模块配置和工具类_
  - _Requirements: 1, 2, 3_

- [x] 1.3 数据库表设计与创建
  - File: backend/src/main/resources/db/migration/product-tables.sql
  - 创建 products, documents, audit_results 等核心数据表
  - 设置表关系、索引和约束
  - Purpose: 建立产品数据存储的数据库基础
  - _Leverage: 现有的数据库配置和迁移脚本_
  - _Requirements: 2, 12, 13, 15_

- [x] 1.4 基础实体类和 DTO 定义
  - File: backend/src/main/java/com/insurance/audit/product/entity/
  - 定义 Product, Document, AuditResult 等实体类
  - 创建对应的 DTO 和转换器
  - Purpose: 建立产品数据模型的 Java 对象表示
  - _Leverage: 现有的 common.entity 和 common.dto 基础类_
  - _Requirements: 2, 5, 6, 6.1, 12, 13, 15_

#### 任务 2: 产品列表和筛选功能实现

- [x] 2.1 产品列表后端 API 实现
  - File: backend/src/main/java/com/insurance/audit/product/controller/ProductController.java
  - 实现 GET /api/products 接口，支持分页和筛选
  - 支持按文件名、报送类型、开发类型、产品类别等筛选
  - Purpose: 提供产品列表数据查询的后端接口
  - _Leverage: 现有的分页工具类和查询构建器_
  - _Requirements: 2, 4_

- [x] 2.2 产品列表前端组件实现
  - File: frontend/src/components/ProductListComponent.vue
  - 实现产品列表表格展示和筛选查询模块
  - 支持分页、排序、搜索功能
  - Purpose: 提供产品管理主界面的核心展示功能
  - _Leverage: 现有的表格组件和筛选组件模式_
  - _Requirements: 2, 4_

- [x] 2.3 产品操作功能实现
  - File: frontend/src/components/ProductOperationsComponent.vue
  - 实现导入产品、查看检核报告等操作按钮
  - MVP阶段：导入产品完整实现，其他功能使用 mock 数据
  - Purpose: 提供产品的各种操作功能入口
  - _Leverage: 现有的按钮组件和操作确认对话框_
  - _Requirements: 3_

#### 任务 3: 新增产品功能实现

- [x] 3.1 产品表单后端 API 实现
  - File: backend/src/main/java/com/insurance/audit/product/controller/ProductController.java
  - 实现 POST /api/products 创建产品接口
  - 实现表单验证和数据处理逻辑
  - Purpose: 提供产品创建的后端业务逻辑
  - _Leverage: 现有的验证框架和异常处理机制_
  - _Requirements: 5, 10_

- [x] 3.2 农险产品表单组件实现
  - File: frontend/src/components/AgriculturalProductForm.vue
  - 实现农险产品信息登记表模板的表单字段
  - 包含报送类型、产品性质、年度、产品名称等字段
  - Purpose: 提供农险产品信息录入的表单界面
  - _Leverage: 现有的表单组件和验证规则_
  - _Requirements: 5, 6_

- [x] 3.3 备案产品表单组件实现
  - File: frontend/src/components/FilingProductForm.vue
  - 实现备案产品自主注册信息登记表模板的表单字段
  - 包含开发类型、示范条款名称、经营区域等字段
  - Purpose: 提供备案产品信息录入的表单界面
  - _Leverage: 现有的表单组件和验证规则_
  - _Requirements: 5, 6.1_

- [x] 3.4 模板选择和动态表单切换
  - File: frontend/src/components/ProductFormTemplate.vue
  - 实现农险产品和备案产品模板的选择功能
  - 动态切换不同模板的表单字段布局
  - Purpose: 提供灵活的产品类型选择和表单切换功能
  - _Leverage: 现有的动态组件加载机制_
  - _Requirements: 5_

#### 任务 4: 文档上传和管理功能

- [x] 4.1 产品信息登记表上传与解析
  - File: backend/src/main/java/com/insurance/audit/product/service/DocumentParsingService.java
  - 实现 Excel 格式产品信息登记表的解析功能
  - 自动识别产品名称、险种类别、经营区域等信息
  - Purpose: 实现产品信息登记表的智能解析和自动填充
  - _Leverage: 现有的文件处理工具类和 Excel 解析库_
  - _Requirements: 7_

- [x] 4.2 文档上传后端 API 实现
  - File: backend/src/main/java/com/insurance/audit/product/controller/DocumentController.java
  - 实现 POST /api/documents/upload 文档上传接口
  - 支持条款、可行性报告、精算报告、费率表等要件上传
  - Purpose: 提供各类要件文档上传的后端处理
  - _Leverage: 现有的文件上传和存储服务_
  - _Requirements: 8_

- [x] 4.3 基础文档上传组件实现
  - File: frontend/src/components/DocumentUploadComponent.vue
  - 实现要件文档路径配置和文件选择功能
  - 支持四种要件类型的文件上传界面
  - Purpose: 提供要件文档上传的用户界面
  - _Leverage: 现有的文件上传组件和路径输入组件_
  - _Requirements: 8_

- [x] 4.4 文档上传对话框功能实现
  - File: frontend/src/components/DocumentUploadDialog.vue
  - 实现模态对话框形式的文档上传功能
  - 支持拖拽上传、多文件选择、上传进度显示
  - Purpose: 提供更好的文档上传用户体验
  - _Leverage: 现有的模态对话框组件和拖拽功能_
  - _Requirements: 16_

#### 任务 5: 文档校验和表单提交

- [x] 5.1 文档校验服务实现
  - File: backend/src/main/java/com/insurance/audit/product/service/DocumentValidationService.java
  - 实现要件类型完整性校验、名称一致性校验等规则
  - 提供校验结果和错误提示信息
  - Purpose: 确保上传文档的完整性和准确性
  - _Leverage: 现有的验证框架和业务规则引擎_
  - _Requirements: 9_

- [x] 5.2 前端文档校验和错误提示
  - File: frontend/src/components/DocumentValidation.vue
  - 实现前端文档校验逻辑和错误提示展示
  - 在对应要件区域显示红色错误提示信息
  - Purpose: 提供实时的文档校验反馈和错误提示
  - _Leverage: 现有的验证提示组件和错误处理机制_
  - _Requirements: 9_

- [x] 5.3 产品提交和成功页面
  - File: frontend/src/views/ProductSubmissionSuccess.vue
  - 实现产品提交成功页面，包含项目详情和操作按钮
  - 显示项目ID、负责人、生效时间等信息
  - Purpose: 提供产品提交成功的确认反馈页面
  - _Leverage: 现有的成功页面模板和操作按钮组件_
  - _Requirements: 10_

- [x] 5.4 表单重置功能
  - File: frontend/src/components/ProductFormReset.vue
  - 实现表单内容清空和重置到初始状态
  - 包含确认对话框避免误操作
  - Purpose: 提供表单重置和数据清理功能
  - _Leverage: 现有的表单重置工具和确认对话框_
  - _Requirements: 11_

#### 任务 6: 产品详情和附件管理

- [x] 6.1 产品详情页面实现
  - File: frontend/src/views/ProductDetailView.vue
  - 实现要件详情页面，显示产品完整名称和基本信息
  - 包含创建人、创建时间、文件描述等信息展示
  - Purpose: 提供产品详细信息的查看界面
  - _Leverage: 现有的详情页面布局和信息展示组件_
  - _Requirements: 12_

- [x] 6.2 附件文档管理功能
  - File: frontend/src/components/AttachmentManagement.vue
  - 实现附件列表表格展示和操作功能
  - 支持文档预览、下载、重新上传等操作
  - Purpose: 提供产品附件文档的管理功能
  - _Leverage: 现有的表格组件和文件操作组件_
  - _Requirements: 13_

- [x] 6.3 批量文档操作功能
  - File: frontend/src/components/BatchDocumentOperations.vue
  - 实现批量选择、批量下载等操作功能
  - 支持原文档下载和检核后文档下载
  - Purpose: 提供批量文档操作的效率工具
  - _Leverage: 现有的批量操作组件和下载功能_
  - _Requirements: 14_

#### 任务 7: 导航和路由集成

- [x] 7.1 页面导航功能实现
  - File: frontend/src/router/product-routes.ts
  - 实现从工作台页面到产品管理功能的路由配置
  - 包含产品管理主界面、新增产品、产品详情等路由
  - Purpose: 建立产品管理模块的完整导航体系
  - _Leverage: 现有的路由配置和导航组件_
  - _Requirements: 1_

- [x] 7.2 返回按钮和面包屑导航
  - File: frontend/src/components/ProductNavigation.vue
  - 实现返回工作台的箭头按钮和页面导航
  - 显示当前页面层级关系的面包屑导航
  - Purpose: 提供清晰的页面导航和返回功能
  - _Leverage: 现有的导航组件和面包屑组件_
  - _Requirements: 1, 2_

### Phase 2: 智能审核功能 (优先级: 中)

#### 任务 8: 智能审核结果显示

- [ ] 8.1 智能审核结果后端 API
  - File: backend/src/main/java/com/insurance/audit/product/controller/AuditController.java
  - 实现 GET /api/documents/:id/audit-results 审核结果接口
  - 提供规则ID、适用章节、原文内容、修改建议等数据
  - Purpose: 提供智能审核结果的数据接口
  - _Leverage: 现有的审核引擎接口和数据处理服务_
  - _Requirements: 15_

- [ ] 8.2 文档审核结果显示组件
  - File: frontend/src/components/DocumentAuditView.vue
  - 实现双栏布局的审核结果显示界面
  - 左侧文档预览，右侧命中规则列表
  - Purpose: 提供智能审核结果的可视化展示
  - _Leverage: 现有的双栏布局组件和规则展示组件_
  - _Requirements: 15_

- [ ] 8.3 章节跳转和规则定位
  - File: frontend/src/components/DocumentChapterNavigation.vue
  - 实现文档章节导航和规则跳转定位功能
  - 支持保险责任、责任免除、总则等章节跳转
  - Purpose: 提供便捷的文档内容导航和规则定位
  - _Leverage: 现有的章节导航组件和内容定位功能_
  - _Requirements: 15_

### Phase 3: 测试和集成 (优先级: 高)

#### 任务 9: 单元测试和集成测试

- [x] 9.1 后端单元测试实现
  - File: backend/src/test/java/com/insurance/audit/product/
  - 编写 Controller、Service、Repository 层的单元测试
  - 确保代码覆盖率达到 80% 以上
  - Purpose: 保证后端代码质量和功能正确性
  - _Leverage: 现有的测试框架和测试工具类_
  - _Requirements: All backend functionality_

- [x] 9.2 前端组件测试实现
  - File: frontend/src/test/components/
  - 编写主要组件的单元测试和集成测试
  - 测试用户交互和数据流
  - Purpose: 保证前端组件质量和用户体验
  - _Leverage: 现有的 Vue Test Utils 和测试工具_
  - _Requirements: All frontend functionality_

- [ ] 9.3 E2E 测试实现
  - File: frontend/tests/e2e/product-management.spec.ts
  - 编写产品创建、文档上传、审核查看等关键流程的 E2E 测试
  - 确保完整业务流程的正确性
  - Purpose: 验证完整的用户操作流程
  - _Leverage: 现有的 Playwright 测试框架_
  - _Requirements: 5, 7, 8, 15_

## 风险评估

### 高风险任务
- **任务 4.1**: 产品信息登记表解析 - Excel 解析复杂度较高
- **任务 5.1**: 文档校验服务 - 业务规则复杂，需要准确理解需求

### 中风险任务
- **任务 8.2**: 智能审核结果显示 - 双栏布局和交互复杂度中等
- **任务 4.4**: 文档上传对话框 - 拖拽功能和文件处理需要注意兼容性

### 低风险任务
- **任务 2.1-2.3**: 产品列表功能 - 标准的 CRUD 操作
- **任务 7.1-7.2**: 导航功能 - 基础的路由和导航实现

## 质量保证

### 代码质量要求
- 单元测试覆盖率 > 80%
- 代码审查制度
- 静态代码分析

### 性能要求
- 页面加载时间 < 3秒
- API 响应时间 < 500ms
- 文件上传支持进度显示

### 安全要求
- 文件上传类型和大小验证
- 用户权限控制
- 数据传输加密