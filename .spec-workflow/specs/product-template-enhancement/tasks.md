# 产品文档管理模板增强任务分解文档

## 阶段1：数据库扩展与模板管理

- [x] 1. 扩展Product实体以支持新模板字段
  - File: backend/src/main/java/com/insurance/audit/product/domain/entity/Product.java
  - 为Product实体添加30个新字段以支持备案产品和农险产品模板
  - 添加字段注解和验证规则，保持与现有字段一致的风格
  - Purpose: 扩展数据模型以支持新模板的所有字段
  - _Leverage: 现有Product.java实体结构和注解模式_
  - _Requirements: Requirement 2, Requirement 3, Requirement 7_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer specializing in JPA entities and database design | Task: Extend the existing Product entity in backend/src/main/java/com/insurance/audit/product/domain/entity/Product.java to include all 30 new fields from both backup product template (备案产品自主注册信息登记表) and agricultural insurance template (农险产品信息登记表), following requirements for field completeness and data storage optimization. Add proper JPA annotations, validation annotations, and Swagger documentation. | Restrictions: Do not modify existing fields, maintain backward compatibility, follow existing naming conventions and annotation patterns, ensure proper database column mappings | _Leverage: Existing Product.java entity structure, BaseEntity class, existing annotation patterns | _Requirements: Requirements 2 (backup product fields), 3 (agricultural insurance fields), 7 (data storage model) | Success: All new fields are properly defined with correct types and annotations, backward compatibility is maintained, entity compiles without errors, proper Swagger documentation is added | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 2. 创建ProductTemplate实体用于模板配置管理
  - File: backend/src/main/java/com/insurance/audit/product/domain/entity/ProductTemplate.java
  - 创建新的ProductTemplate实体，继承BaseEntity
  - 包含模板类型、配置、验证规则等字段
  - Purpose: 管理模板配置和元数据
  - _Leverage: 现有BaseEntity类和实体注解模式_
  - _Requirements: Requirement 7_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer with expertise in JPA entity design and database modeling | Task: Create a new ProductTemplate entity in backend/src/main/java/com/insurance/audit/product/domain/entity/ProductTemplate.java to manage template configurations and metadata. Include fields for template type, name, file path, field configuration JSON, validation rules JSON, enabled status, and version. | Restrictions: Must extend BaseEntity, follow existing entity patterns, use proper JPA annotations, ensure JSON fields can handle complex configurations | _Leverage: Existing BaseEntity class, entity annotation patterns from other entities | _Requirements: Requirement 7 (data storage model optimization) | Success: Entity is properly structured with all required fields, extends BaseEntity correctly, includes proper validation and documentation annotations | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 3. 创建数据库迁移脚本
  - File: backend/src/main/resources/db/migration/V2.0__Add_Template_Support.sql
  - 创建ALTER TABLE语句为products表添加新字段
  - 创建product_templates表和相关索引
  - Purpose: 执行数据库结构升级，保持数据完整性
  - _Leverage: 现有数据库迁移模式和命名约定_
  - _Requirements: Requirement 7, Requirement 8_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Database Developer with expertise in MySQL and Flyway migrations | Task: Create database migration script to add all new template fields to products table and create product_templates table. Ensure backward compatibility and data integrity. | Restrictions: Must use proper SQL syntax for MySQL 8.x, ensure all new fields are nullable for backward compatibility, add appropriate indexes for performance | _Leverage: Existing migration patterns in db/migration directory | _Requirements: Requirement 7 (data storage), Requirement 8 (backward compatibility) | Success: Migration script executes successfully, all new fields are added without data loss, indexes improve query performance | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 4. 创建ProductTemplateMapper数据访问层
  - File: backend/src/main/java/com/insurance/audit/product/infrastructure/mapper/ProductTemplateMapper.java
  - 实现ProductTemplate的CRUD操作
  - 扩展MyBatis Plus BaseMapper接口
  - Purpose: 提供模板配置的数据访问功能
  - _Leverage: 现有Mapper接口模式和MyBatis Plus配置_
  - _Requirements: Requirement 7_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer specializing in MyBatis Plus and data access layer | Task: Create ProductTemplateMapper interface extending BaseMapper for ProductTemplate entity, following existing mapper patterns in the project. | Restrictions: Must extend BaseMapper, follow existing naming conventions, add proper method documentation | _Leverage: Existing mapper interfaces like ProductMapper, MyBatis Plus BaseMapper patterns | _Requirements: Requirement 7 (data storage model optimization) | Success: Mapper interface is properly defined, extends BaseMapper correctly, follows project patterns | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

## 阶段2：模板服务层实现

- [x] 5. 实现TemplateService模板管理服务
  - File: backend/src/main/java/com/insurance/audit/product/application/service/TemplateService.java, TemplateServiceImpl.java
  - 实现模板下载、配置管理、元数据查询功能
  - 集成文件存储服务和缓存机制
  - Purpose: 提供模板管理的核心业务逻辑
  - _Leverage: 现有FileStorageService、Redis缓存配置_
  - _Requirements: Requirement 1_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Spring Boot Developer with expertise in service layer architecture and file management | Task: Create TemplateService interface and implementation for managing product templates, including download functionality, configuration management, and metadata queries. Integrate with existing FileStorageService and Redis caching. | Restrictions: Must follow existing service patterns, use proper error handling, implement caching for performance, ensure thread safety | _Leverage: Existing FileStorageService, Redis configuration, service layer patterns | _Requirements: Requirement 1 (dual template download support) | Success: Service provides all required template management operations, integrates properly with file storage and caching, follows project architecture patterns | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 6. 增强ProductService以支持新模板字段
  - File: backend/src/main/java/com/insurance/audit/product/application/service/impl/ProductServiceImpl.java
  - 扩展现有ProductService以处理新增字段
  - 添加模板类型验证和字段映射逻辑
  - Purpose: 扩展产品服务以支持新模板功能
  - _Leverage: 现有ProductService实现和业务逻辑_
  - _Requirements: Requirement 2, Requirement 3, Requirement 8_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer with expertise in service layer enhancement and business logic | Task: Enhance the existing ProductServiceImpl to support new template fields for both backup product and agricultural insurance templates. Add template type validation, field mapping logic, and ensure backward compatibility. | Restrictions: Must not break existing functionality, maintain method signatures, ensure proper validation of new fields | _Leverage: Existing ProductServiceImpl code, validation patterns, business logic structure | _Requirements: Requirements 2-3 (field completeness), Requirement 8 (backward compatibility) | Success: Service handles all new template fields correctly, validation works properly, existing functionality remains unaffected | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 7. 实现FieldValidationService字段验证服务
  - File: backend/src/main/java/com/insurance/audit/product/application/service/FieldValidationService.java, FieldValidationServiceImpl.java
  - 实现基于模板规则的字段验证逻辑
  - 支持动态验证规则和依赖字段检查
  - Purpose: 提供灵活的字段验证功能
  - _Leverage: 现有DocumentValidationService模式_
  - _Requirements: Requirement 4, Requirement 6_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer with expertise in validation frameworks and business rules | Task: Create FieldValidationService for template-based field validation, supporting dynamic validation rules, dependency checks, and template-specific validation logic based on the filling rules from template documents. | Restrictions: Must handle all validation scenarios described in requirements, support both template types, provide clear error messages in Chinese | _Leverage: Existing DocumentValidationService patterns, validation utilities | _Requirements: Requirement 4 (dynamic form logic), Requirement 6 (enhanced field validation) | Success: Validation service handles all template rules correctly, provides appropriate error messages, supports dynamic field dependencies | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 8. 扩展DocumentParsingService支持新模板解析
  - File: backend/src/main/java/com/insurance/audit/product/application/service/impl/DocumentParsingServiceImpl.java
  - 增强Excel解析功能以支持两种新模板格式
  - 添加字段映射和数据验证逻辑
  - Purpose: 实现新模板文件的自动解析功能
  - _Leverage: 现有DocumentParsingService和Excel处理工具_
  - _Requirements: Requirement 5_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer with expertise in Excel parsing and Apache POI library | Task: Enhance DocumentParsingServiceImpl to support parsing of both new template Excel files (备案产品自主注册信息登记表.xlsx and 农险产品信息登记表.xls), implementing field mapping, data validation, and error handling for template file parsing. | Restrictions: Must handle both .xlsx and .xls formats, provide detailed error messages for parsing failures, validate parsed data against template rules | _Leverage: Existing DocumentParsingService, Excel parsing utilities, Apache POI configuration | _Requirements: Requirement 5 (template file parsing and auto-fill enhancement) | Success: Service correctly parses both template types, maps fields accurately, handles parsing errors gracefully | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

## 阶段3：API层扩展

- [x] 9. 扩展ProductController添加模板相关API
  - File: backend/src/main/java/com/insurance/audit/product/interfaces/web/ProductController.java
  - 添加模板下载、字段配置、模板解析等API端点
  - 确保API文档和错误处理的完整性
  - Purpose: 提供前端所需的模板管理API接口
  - _Leverage: 现有ProductController结构和API模式_
  - _Requirements: Requirement 1, Requirement 5_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Spring Boot Developer with expertise in REST API design and controller layer | Task: Extend ProductController to add template-related endpoints including template download, field configuration retrieval, and template parsing APIs. Ensure proper HTTP status codes, error handling, and OpenAPI documentation. | Restrictions: Must follow existing controller patterns, maintain consistent API design, ensure proper security and validation | _Leverage: Existing ProductController structure, REST API patterns, error handling mechanisms | _Requirements: Requirement 1 (dual template download), Requirement 5 (template parsing) | Success: All template-related APIs are properly implemented, documented, and tested | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 10. 创建模板相关的DTO类
  - File: backend/src/main/java/com/insurance/audit/product/interfaces/dto/request/TemplateRequest.java, response/TemplateResponse.java
  - 创建模板下载、配置、验证相关的请求和响应DTO
  - 确保DTO包含所有必要的字段和验证注解
  - Purpose: 定义API层的数据传输对象
  - _Leverage: 现有DTO模式和验证注解_
  - _Requirements: Requirement 1, Requirement 5, Requirement 6_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer with expertise in DTO design and data validation | Task: Create comprehensive DTO classes for template-related operations including TemplateDownloadRequest, TemplateConfigResponse, FieldValidationRequest, and ParseResultResponse. Include proper validation annotations and Swagger documentation. | Restrictions: Must follow existing DTO patterns, include proper validation rules, ensure all required fields are covered | _Leverage: Existing DTO structures, validation annotation patterns, Swagger documentation style | _Requirements: Requirements 1, 5, 6 (template operations, parsing, validation) | Success: All DTO classes are properly structured with validation and documentation, follow project patterns | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

## 阶段4：前端模板下载组件

- [x] 11. 创建TemplateDownload模板下载组件
  - File: frontend/src/components/product/TemplateDownload.vue
  - 实现双模板选择和下载功能
  - 提供下载进度反馈和错误处理
  - Purpose: 为用户提供模板选择和下载界面
  - _Leverage: 现有文件下载机制和UI组件样式_
  - _Requirements: Requirement 1_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Vue 3 Frontend Developer with expertise in component architecture and file download handling | Task: Create TemplateDownload component that allows users to select and download either backup product template (备案产品自主注册信息登记表) or agricultural insurance template (农险产品信息登记表). Include download progress feedback, error handling, and usage guidance. | Restrictions: Must use Vue 3 Composition API, follow existing component patterns, ensure responsive design, handle file download errors gracefully | _Leverage: Existing file download mechanisms, UI component styles, error handling patterns | _Requirements: Requirement 1 (dual template download support) | Success: Component provides clear template selection, download works reliably, proper error handling and user feedback | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 12. 创建模板API服务
  - File: frontend/src/api/product/template.ts
  - 实现模板下载、配置获取等API调用
  - 添加错误处理和类型安全
  - Purpose: 封装模板相关的API调用逻辑
  - _Leverage: 现有API服务模式和错误处理_
  - _Requirements: Requirement 1, Requirement 5_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: TypeScript Frontend Developer with expertise in API service layer and HTTP client libraries | Task: Create template API service module with functions for template download, configuration retrieval, field validation, and template parsing. Ensure proper TypeScript typing, error handling, and integration with existing API patterns. | Restrictions: Must follow existing API service patterns, use proper TypeScript types, handle network errors appropriately | _Leverage: Existing API service patterns, HTTP client configuration, error handling utilities | _Requirements: Requirement 1 (template download), Requirement 5 (template parsing) | Success: API service provides all required template operations with proper typing and error handling | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 13. 创建模板相关TypeScript类型定义
  - File: frontend/src/types/product/template.ts
  - 定义模板配置、字段验证、表单数据等类型
  - 确保与后端DTO保持一致
  - Purpose: 提供类型安全的前端开发体验
  - _Leverage: 现有类型定义模式_
  - _Requirements: Requirement 2, Requirement 3, Requirement 4_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: TypeScript Developer specializing in type systems and interface design | Task: Create comprehensive TypeScript interfaces and types for template-related functionality including TemplateInfo, TemplateFieldConfig, ValidationRule, ProductFormData, and all template-specific form field types. Ensure alignment with backend DTOs. | Restrictions: Must follow existing type definition patterns, ensure type safety, maintain consistency with backend data structures | _Leverage: Existing type definition patterns, interface naming conventions | _Requirements: Requirements 2-4 (template field completeness and dynamic logic) | Success: All template-related types are properly defined, type-safe, and consistent with backend | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

## 阶段5：增强产品表单组件

- [x] 14. 创建FieldValidation字段验证组件
  - File: frontend/src/components/product/FieldValidation.vue
  - 实现实时字段验证和错误提示功能
  - 支持动态验证规则和依赖检查
  - Purpose: 提供灵活的字段验证UI组件
  - _Leverage: 现有表单验证模式和错误提示样式_
  - _Requirements: Requirement 6_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Vue 3 Frontend Developer with expertise in form validation and user experience | Task: Create FieldValidation component that provides real-time field validation with error messages, supporting dynamic validation rules, dependency checking, and template-specific validation logic. Include clear error display and user guidance. | Restrictions: Must use Vue 3 Composition API, follow existing validation patterns, ensure accessibility, provide clear error messages in Chinese | _Leverage: Existing form validation patterns, error display styles, accessibility guidelines | _Requirements: Requirement 6 (enhanced field validation and rule integration) | Success: Component provides real-time validation with clear feedback, handles all validation scenarios correctly | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 15. 创建DynamicForm动态表单组件
  - File: frontend/src/components/product/DynamicForm.vue
  - 实现基于配置的动态表单渲染
  - 支持字段联动和条件显示逻辑
  - Purpose: 根据模板配置动态生成表单界面
  - _Leverage: 现有表单组件和Ant Design Vue组件_
  - _Requirements: Requirement 4_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Vue 3 Frontend Developer with expertise in dynamic component rendering and form architecture | Task: Create DynamicForm component that renders forms based on template configuration, supporting field dependencies, conditional display logic, and dynamic field validation based on template rules. | Restrictions: Must use Vue 3 Composition API, leverage Ant Design Vue components, ensure performance with large forms, handle field dependencies correctly | _Leverage: Existing form components, Ant Design Vue components, form handling patterns | _Requirements: Requirement 4 (dynamic form logic and field linkage) | Success: Component dynamically renders forms correctly, handles field dependencies and conditional logic properly | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 16. 增强EnhancedProductForm产品表单组件
  - File: frontend/src/components/product/EnhancedProductForm.vue
  - 集成模板选择、动态表单、字段验证等功能
  - 替换现有的AgriculturalProductForm组件功能
  - Purpose: 提供完整的产品表单录入体验
  - _Leverage: 现有AgriculturalProductForm.vue、表单处理逻辑_
  - _Requirements: Requirement 2, Requirement 3, Requirement 4_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Vue 3 Frontend Developer with expertise in complex form management and component integration | Task: Create EnhancedProductForm component that integrates template selection, dynamic form rendering, field validation, and file upload functionality. This should replace and enhance the existing AgriculturalProductForm component while maintaining backward compatibility. | Restrictions: Must maintain existing functionality, ensure smooth user experience, handle large forms efficiently, provide clear user guidance | _Leverage: Existing AgriculturalProductForm.vue, form handling patterns, component integration methods | _Requirements: Requirements 2-4 (field completeness and dynamic form logic) | Success: Form component provides complete template-based product entry experience, integrates all required functionality seamlessly | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 17. 创建模板解析和自动填充功能
  - File: frontend/src/utils/templateParsing.ts, components/product/TemplateParser.vue
  - 实现Excel文件解析和表单自动填充
  - 提供解析进度反馈和错误处理
  - Purpose: 提供模板文件解析和自动填充功能
  - _Leverage: 现有文件上传处理机制_
  - _Requirements: Requirement 5_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Frontend Developer with expertise in file processing and data parsing | Task: Create template parsing functionality including utility functions and UI component for Excel file upload, parsing, and automatic form field population. Include progress feedback, error handling, and validation of parsed data. | Restrictions: Must handle both .xlsx and .xls formats, provide clear error messages for parsing failures, ensure data validation before auto-fill | _Leverage: Existing file upload mechanisms, data processing patterns | _Requirements: Requirement 5 (template file parsing and auto-fill enhancement) | Success: Template parsing works reliably for both formats, auto-fill populates correct fields, proper error handling and user feedback | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

## 阶段6：页面集成与优化

- [x] 18. 更新产品导入页面
  - File: frontend/src/views/product/Import.vue
  - 替换占位页面，集成完整的产品导入功能
  - 包含模板下载、表单填写、文件上传等完整流程
  - Purpose: 提供完整的产品导入用户界面
  - _Leverage: 新创建的组件和现有页面布局模式_
  - _Requirements: All requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Vue 3 Frontend Developer with expertise in page-level integration and user experience design | Task: Replace the placeholder Import.vue page with a complete product import interface that integrates template download, enhanced product form, file upload, and validation functionality. Ensure smooth user workflow and proper error handling. | Restrictions: Must provide intuitive user experience, handle all error scenarios gracefully, ensure responsive design, maintain consistency with existing pages | _Leverage: All newly created components, existing page layout patterns, navigation structure | _Requirements: All requirements for complete product import functionality | Success: Page provides complete product import workflow, all components work together seamlessly, user experience is intuitive and error-free | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 19. 更新产品管理列表页面
  - File: frontend/src/views/ProductManagementView.vue, frontend/src/components/ProductListComponent.vue
  - 扩展产品列表以显示新增字段信息
  - 更新筛选和排序功能以支持新字段
  - Purpose: 展示扩展后的产品信息和筛选功能
  - _Leverage: 现有产品列表组件和数据展示模式_
  - _Requirements: Requirement 8_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Vue 3 Frontend Developer with expertise in data display and list management | Task: Update product management list page and components to display new template fields, enhance filtering and sorting capabilities, and ensure backward compatibility with existing product data. | Restrictions: Must maintain existing functionality, ensure performance with additional fields, provide clear data display, handle null values for legacy data | _Leverage: Existing ProductListComponent.vue, ProductManagementView.vue, data display patterns | _Requirements: Requirement 8 (backward compatibility and extended data display) | Success: List displays all new fields appropriately, filtering and sorting work with new fields, legacy data displays correctly | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 20. 添加路由和导航更新
  - File: frontend/src/router/index.ts, navigation components
  - 确保所有新页面和功能可以正确访问
  - 更新导航菜单和面包屑路径
  - Purpose: 确保新功能的可访问性和导航完整性
  - _Leverage: 现有路由配置和导航组件_
  - _Requirements: All requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Frontend Developer with expertise in Vue Router and navigation design | Task: Update routing configuration and navigation components to ensure all new template-enhanced functionality is properly accessible, including route guards, navigation menus, and breadcrumb paths. | Restrictions: Must maintain existing routes, ensure proper authentication and authorization, follow existing navigation patterns | _Leverage: Existing router configuration, navigation components, route guard patterns | _Requirements: All requirements for complete navigation and accessibility | Success: All new functionality is properly accessible through navigation, routes work correctly, navigation is intuitive | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

## 阶段7：测试与文档

- [x] 21. 创建后端单元测试
  - File: backend/src/test/java/com/insurance/audit/product/application/service/TemplateServiceTest.java, FieldValidationServiceTest.java
  - 为新增的服务类创建全面的单元测试
  - 确保85%以上的代码覆盖率
  - Purpose: 确保后端新功能的质量和可靠性
  - _Leverage: 现有测试工具和模式_
  - _Requirements: All backend requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Java Backend Developer with expertise in unit testing and JUnit framework | Task: Create comprehensive unit tests for all new service classes including TemplateService, FieldValidationService, and enhanced ProductService methods. Ensure high code coverage (85%+) and test both success and error scenarios. | Restrictions: Must test business logic thoroughly, mock external dependencies properly, ensure tests are isolated and repeatable | _Leverage: Existing test utilities, JUnit configuration, MockMVC patterns | _Requirements: All backend-related requirements | Success: All new service methods are thoroughly tested, code coverage meets requirements, tests pass consistently | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 22. 创建前端组件测试
  - File: frontend/src/test/components/product/*.spec.ts
  - 为新增的Vue组件创建单元测试
  - 使用Vue Test Utils和Vitest进行测试
  - Purpose: 确保前端组件的功能正确性和用户体验
  - _Leverage: 现有前端测试配置和工具_
  - _Requirements: All frontend requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Frontend Developer with expertise in Vue 3 testing and component testing frameworks | Task: Create comprehensive unit tests for all new Vue components including TemplateDownload, DynamicForm, FieldValidation, and EnhancedProductForm. Use Vue Test Utils and Vitest, ensure proper component behavior testing. | Restrictions: Must test component interactions, props/events, user interactions, ensure tests are maintainable and reliable | _Leverage: Existing Vue Test Utils configuration, Vitest setup, component testing patterns | _Requirements: All frontend component requirements | Success: All new components are thoroughly tested, user interactions work correctly, tests provide good coverage of component functionality | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 23. 创建集成测试
  - File: backend/src/test/java/com/insurance/audit/product/integration/TemplateIntegrationTest.java
  - 创建API端点的集成测试，包括完整的数据流测试
  - 使用TestContainers进行数据库集成测试
  - Purpose: 验证系统各组件之间的集成正确性
  - _Leverage: 现有TestContainers配置和集成测试模式_
  - _Requirements: All requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: QA Engineer with expertise in integration testing and TestContainers | Task: Create comprehensive integration tests for template functionality including API endpoints, database operations, and complete data flow testing. Use TestContainers for real database testing environment. | Restrictions: Must test complete user workflows, ensure data consistency, test error scenarios, maintain test isolation | _Leverage: Existing TestContainers configuration, integration test patterns, MockMVC setup | _Requirements: All requirements for complete system integration | Success: Integration tests cover all major workflows, database operations work correctly, tests run reliably in CI environment | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 24. 创建端到端测试
  - File: frontend/tests/e2e/product-template-enhancement.spec.ts
  - 使用Playwright创建完整用户流程的E2E测试
  - 测试从模板下载到产品提交的完整流程
  - Purpose: 验证完整用户场景的正确性和用户体验
  - _Leverage: 现有Playwright配置和E2E测试模式_
  - _Requirements: All requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: QA Automation Engineer with expertise in Playwright and end-to-end testing | Task: Create comprehensive E2E tests using Playwright that cover the complete user journey from template download through form filling, file upload, validation, and product submission. Test both template types and error scenarios. | Restrictions: Must test real user workflows, ensure tests are stable and maintainable, cover both successful and error paths | _Leverage: Existing Playwright configuration, E2E test patterns, test data setup | _Requirements: All requirements for complete user workflow validation | Success: E2E tests cover all critical user journeys, tests run reliably, user experience is validated end-to-end | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 25. 更新API文档
  - File: backend API documentation, OpenAPI specifications
  - 更新Swagger/OpenAPI文档以包含所有新增的API端点
  - 确保API文档的完整性和准确性
  - Purpose: 提供完整和准确的API文档
  - _Leverage: 现有OpenAPI配置和文档生成工具_
  - _Requirements: All API-related requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: Technical Writer with expertise in API documentation and OpenAPI specifications | Task: Update all API documentation to include new template-related endpoints, ensure Swagger/OpenAPI specifications are complete and accurate, add examples and detailed descriptions for all new APIs. | Restrictions: Must document all new endpoints completely, provide clear examples, ensure documentation accuracy, follow existing documentation standards | _Leverage: Existing OpenAPI configuration, Swagger setup, documentation patterns | _Requirements: All API-related requirements | Success: API documentation is complete and accurate, all new endpoints are properly documented with examples | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._

- [x] 26. 最终集成测试和部署验证
  - File: 部署脚本和验证测试
  - 执行完整的系统集成测试和部署验证
  - 确保所有功能在生产环境中正常工作
  - Purpose: 验证整个系统的部署和生产就绪性
  - _Leverage: 现有部署脚本和环境配置_
  - _Requirements: All requirements_
  - _Prompt: Implement the task for spec product-template-enhancement, first run spec-workflow-guide to get the workflow guide then implement the task: Role: DevOps Engineer with expertise in deployment and system integration | Task: Perform final integration testing and deployment validation to ensure all template enhancement functionality works correctly in production environment. Execute comprehensive system testing and verify all requirements are met. | Restrictions: Must test in production-like environment, ensure no breaking changes to existing functionality, validate performance requirements | _Leverage: Existing deployment scripts, environment configurations, monitoring tools | _Requirements: All requirements for complete system functionality | Success: All functionality works correctly in production environment, no breaking changes, performance meets requirements, system is production-ready | Instructions: Set this task to in_progress in tasks.md before starting, and mark as completed when finished._