# 后端开发规范

## 技术栈

### 核心框架
- **框架**: Spring Boot 3.2.x
- **Java版本**: JDK 17+
- **构建工具**: Maven 3.9+
- **数据库**: MySQL 8.0+
- **ORM框架**: MyBatis Plus 3.5.5+
- **向量数据库**: Milvus 2.3+
- **缓存**: Redis 7.0+
- **消息队列**: RabbitMQ 3.12+
- **安全框架**: Spring Security 6.x + JWT

### 开发工具
- **IDE**: IntelliJ IDEA 2023.3+
- **代码格式化**: Google Java Format
- **代码检查**: SpotBugs + PMD + Checkstyle + SonarQube
- **测试框架**: JUnit 5 + Mockito + TestContainers
- **API文档**: SpringDoc OpenAPI 3 (Swagger)
- **容器化**: Docker + Docker Compose
- **版本控制**: Git + GitFlow

## 项目结构

### 按DDD领域驱动设计的项目结构

采用六边形架构(Hexagonal Architecture)和领域驱动设计(DDD)，确保业务逻辑与技术实现解耦：

```
src/
├── main/
│   ├── java/
│   │   └── com/insurance/audit/
│   │       ├── InsuranceAuditApplication.java    # 启动类
│   │       ├── common/                           # 公共模块
│   │       │   ├── config/                       # 配置类
│   │       │   │   ├── DatabaseConfig.java
│   │       │   │   ├── MyBatisPlusConfig.java
│   │       │   │   ├── RedisConfig.java
│   │       │   │   ├── MilvusConfig.java
│   │       │   │   ├── SecurityConfig.java
│   │       │   │   ├── SwaggerConfig.java
│   │       │   │   └── AsyncConfig.java
│   │       │   ├── constant/                     # 常量定义
│   │       │   │   ├── CommonConstant.java
│   │       │   │   ├── RedisKeyConstant.java
│   │       │   │   └── SecurityConstant.java
│   │       │   ├── exception/                    # 异常处理
│   │       │   │   ├── GlobalExceptionHandler.java
│   │       │   │   ├── BusinessException.java
│   │       │   │   ├── SystemException.java
│   │       │   │   └── ErrorCode.java
│   │       │   ├── util/                         # 工具类
│   │       │   │   ├── JsonUtil.java
│   │       │   │   ├── DateUtil.java
│   │       │   │   ├── FileUtil.java
│   │       │   │   ├── ValidationUtil.java
│   │       │   │   ├── EncryptUtil.java
│   │       │   │   └── JwtUtil.java
│   │       │   ├── aspect/                       # 切面
│   │       │   │   ├── LoggingAspect.java
│   │       │   │   ├── PerformanceAspect.java
│   │       │   │   ├── SecurityAspect.java
│   │       │   │   └── ValidationAspect.java
│   │       │   ├── base/                         # 基础类
│   │       │   │   ├── BaseEntity.java
│   │       │   │   ├── BaseController.java
│   │       │   │   ├── BaseService.java
│   │       │   │   └── BaseMapper.java
│   │       │   └── dto/                          # 通用DTO
│   │       │       ├── ApiResponse.java
│   │       │       ├── PageRequest.java
│   │       │       └── PageResponse.java
│   │       ├── document/                         # 文档管理领域
│   │       │   ├── application/                  # 应用层
│   │       │   │   ├── service/
│   │       │   │   │   └── DocumentApplicationService.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── command/
│   │       │   │   │   │   ├── UploadDocumentCommand.java
│   │       │   │   │   │   └── ProcessDocumentCommand.java
│   │       │   │   │   ├── query/
│   │       │   │   │   │   └── DocumentQuery.java
│   │       │   │   │   └── result/
│   │       │   │   │       ├── DocumentResult.java
│   │       │   │   │       └── DocumentListResult.java
│   │       │   │   └── assembler/
│   │       │   │       └── DocumentAssembler.java
│   │       │   ├── domain/                       # 领域层
│   │       │   │   ├── model/
│   │       │   │   │   ├── Document.java         # 领域实体
│   │       │   │   │   ├── DocumentVersion.java
│   │       │   │   │   └── DocumentContent.java # 值对象
│   │       │   │   ├── service/
│   │       │   │   │   ├── DocumentDomainService.java
│   │       │   │   │   └── DocumentValidationService.java
│   │       │   │   ├── repository/
│   │       │   │   │   └── DocumentRepository.java # 领域仓储接口
│   │       │   │   ├── factory/
│   │       │   │   │   └── DocumentFactory.java
│   │       │   │   └── event/
│   │       │   │       ├── DocumentUploadedEvent.java
│   │       │   │       └── DocumentProcessedEvent.java
│   │       │   ├── infrastructure/               # 基础设施层
│   │       │   │   ├── repository/
│   │       │   │   │   ├── DocumentRepositoryImpl.java
│   │       │   │   │   └── DocumentMapper.java  # MyBatis映射器
│   │       │   │   ├── external/
│   │       │   │   │   ├── FileStorageAdapter.java
│   │       │   │   │   └── OcrServiceAdapter.java
│   │       │   │   └── converter/
│   │       │   │       └── DocumentConverter.java
│   │       │   ├── interfaces/                   # 接口层
│   │       │   │   ├── web/
│   │       │   │   │   └── DocumentController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── DocumentUploadRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       └── DocumentResponse.java
│   │       │   │   └── facade/
│   │       │   │       └── DocumentFacade.java
│   │       │   └── config/
│   │       │       └── DocumentConfig.java
│   │       ├── rule/                             # 规则引擎领域
│   │       │   ├── application/                  # 应用层
│   │       │   │   ├── service/
│   │       │   │   │   ├── RuleApplicationService.java
│   │       │   │   │   └── RuleExecutionService.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── command/
│   │       │   │   │   │   ├── CreateRuleCommand.java
│   │       │   │   │   │   └── ExecuteRuleCommand.java
│   │       │   │   │   └── result/
│   │       │   │   │       ├── RuleExecutionResult.java
│   │       │   │   │       └── RuleValidationResult.java
│   │       │   │   └── orchestrator/
│   │       │   │       └── RuleOrchestrator.java
│   │       │   ├── domain/                       # 领域层
│   │       │   │   ├── model/
│   │       │   │   │   ├── Rule.java             # 领域实体
│   │       │   │   │   ├── RuleSet.java          # 聚合根
│   │       │   │   │   ├── RuleCondition.java    # 值对象
│   │       │   │   │   ├── RuleAction.java       # 值对象
│   │       │   │   │   └── ExecutionContext.java # 值对象
│   │       │   │   ├── service/
│   │       │   │   │   ├── RuleValidator.java
│   │       │   │   │   ├── RuleCompiler.java
│   │       │   │   │   └── RuleOptimizer.java
│   │       │   │   ├── engine/
│   │       │   │   │   ├── RuleEngine.java       # 规则引擎接口
│   │       │   │   │   ├── DroolsRuleEngine.java
│   │       │   │   │   ├── GroovyRuleEngine.java
│   │       │   │   │   └── SpelRuleEngine.java
│   │       │   │   ├── dsl/
│   │       │   │   │   ├── RuleDslParser.java
│   │       │   │   │   ├── RuleDslValidator.java
│   │       │   │   │   └── RuleDslCompiler.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── RuleRepository.java
│   │       │   │   │   └── RuleExecutionLogRepository.java
│   │       │   │   └── strategy/
│   │       │   │       ├── ExecutionStrategy.java
│   │       │   │       ├── SequentialStrategy.java
│   │       │   │       └── ParallelStrategy.java
│   │       │   ├── infrastructure/               # 基础设施层
│   │       │   │   ├── repository/
│   │       │   │   │   ├── RuleRepositoryImpl.java
│   │       │   │   │   └── RuleExecutionLogRepositoryImpl.java
│   │       │   │   ├── engine/
│   │       │   │   │   ├── DroolsEngineAdapter.java
│   │       │   │   │   └── GroovyEngineAdapter.java
│   │       │   │   ├── cache/
│   │       │   │   │   └── RuleCache.java
│   │       │   │   └── monitor/
│   │       │   │       ├── RuleExecutionMonitor.java
│   │       │   │       └── RulePerformanceTracker.java
│   │       │   ├── interfaces/                   # 接口层
│   │       │   │   ├── web/
│   │       │   │   │   └── RuleController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── RuleRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       └── RuleResponse.java
│   │       │   │   └── facade/
│   │       │   │       └── RuleFacade.java
│   │       │   └── config/
│   │       │       └── RuleEngineConfig.java
│   │       ├── audit/                            # 检核引擎模块
│   │       │   ├── controller/
│   │       │   │   └── AuditController.java
│   │       │   ├── service/
│   │       │   │   ├── AuditService.java
│   │       │   │   └── impl/
│   │       │   │       └── AuditServiceImpl.java
│   │       │   ├── mapper/
│   │       │   │   ├── AuditTaskMapper.java
│   │       │   │   └── AuditResultMapper.java
│   │       │   ├── entity/
│   │       │   │   ├── AuditTask.java
│   │       │   │   └── AuditResult.java
│   │       │   ├── dto/
│   │       │   │   ├── request/
│   │       │   │   └── response/
│   │       │   ├── enums/
│   │       │   │   └── AuditStatus.java
│   │       │   └── component/
│   │       │       ├── AuditEngine.java
│   │       │       ├── SemanticMatcher.java
│   │       │       └── DocumentParser.java
│   │       ├── llm/                              # LLM服务领域
│   │       │   ├── application/                  # 应用层
│   │       │   │   ├── service/
│   │       │   │   │   ├── LlmApplicationService.java
│   │       │   │   │   └── LlmOrchestrationService.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── command/
│   │       │   │   │   │   ├── LlmCallCommand.java
│   │       │   │   │   │   └── BatchLlmCallCommand.java
│   │       │   │   │   └── result/
│   │       │   │   │       ├── LlmCallResult.java
│   │       │   │   │       └── LlmUsageResult.java
│   │       │   │   └── strategy/
│   │       │   │       ├── LlmProviderStrategy.java
│   │       │   │       └── LlmFallbackStrategy.java
│   │       │   ├── domain/                       # 领域层
│   │       │   │   ├── model/
│   │       │   │   │   ├── LlmProvider.java      # 领域实体
│   │       │   │   │   ├── LlmCall.java
│   │       │   │   │   ├── PromptTemplate.java   # 值对象
│   │       │   │   │   ├── TokenUsage.java       # 值对象
│   │       │   │   │   └── CostCalculation.java  # 值对象
│   │       │   │   ├── service/
│   │       │   │   │   ├── LlmProviderSelector.java
│   │       │   │   │   ├── CostOptimizer.java
│   │       │   │   │   └── QualityAssurance.java
│   │       │   │   ├── repository/
│   │       │   │   │   ├── LlmProviderRepository.java
│   │       │   │   │   └── LlmCallLogRepository.java
│   │       │   │   └── policy/
│   │       │   │       ├── RetryPolicy.java
│   │       │   │       ├── CircuitBreakerPolicy.java
│   │       │   │       └── RateLimitPolicy.java
│   │       │   ├── infrastructure/               # 基础设施层
│   │       │   │   ├── repository/
│   │       │   │   │   ├── LlmProviderRepositoryImpl.java
│   │       │   │   │   └── LlmCallLogRepositoryImpl.java
│   │       │   │   ├── adapter/
│   │       │   │   │   ├── OpenAiAdapter.java
│   │       │   │   │   ├── QianwenAdapter.java
│   │       │   │   │   ├── WenxinAdapter.java
│   │       │   │   │   └── ClaudeAdapter.java
│   │       │   │   ├── client/
│   │       │   │   │   ├── LlmHttpClient.java
│   │       │   │   │   └── LlmWebSocketClient.java
│   │       │   │   ├── monitor/
│   │       │   │   │   ├── LlmMetricsCollector.java
│   │       │   │   │   ├── CostTracker.java
│   │       │   │   │   └── PerformanceMonitor.java
│   │       │   │   └── cache/
│   │       │   │       └── LlmResponseCache.java
│   │       │   ├── interfaces/                   # 接口层
│   │       │   │   ├── web/
│   │       │   │   │   └── LlmController.java
│   │       │   │   ├── dto/
│   │       │   │   │   ├── request/
│   │       │   │   │   │   └── LlmCallRequest.java
│   │       │   │   │   └── response/
│   │       │   │   │       └── LlmCallResponse.java
│   │       │   │   └── facade/
│   │       │   │       └── LlmFacade.java
│   │       │   └── config/
│   │       │       ├── LlmConfig.java
│   │       │       └── LlmProviderConfig.java
│   │       ├── user/                             # 用户管理模块
│   │       │   ├── controller/
│   │       │   │   ├── AuthController.java
│   │       │   │   └── UserController.java
│   │       │   ├── service/
│   │       │   │   ├── AuthService.java
│   │       │   │   ├── UserService.java
│   │       │   │   └── impl/
│   │       │   │       ├── AuthServiceImpl.java
│   │       │   │       └── UserServiceImpl.java
│   │       │   ├── mapper/
│   │       │   │   ├── UserMapper.java
│   │       │   │   └── RoleMapper.java
│   │       │   ├── entity/
│   │       │   │   ├── User.java
│   │       │   │   ├── Role.java
│   │       │   │   └── UserRole.java
│   │       │   ├── dto/
│   │       │   │   ├── request/
│   │       │   │   │   ├── LoginRequest.java
│   │       │   │   │   └── UserCreateRequest.java
│   │       │   │   └── response/
│   │       │   │       ├── LoginResponse.java
│   │       │   │       └── UserResponse.java
│   │       │   └── enums/
│   │       │       └── UserStatus.java
│   │       └── system/                           # 系统管理模块
│   │           ├── controller/
│   │           │   ├── SystemConfigController.java
│   │           │   └── MonitorController.java
│   │           ├── service/
│   │           │   ├── SystemConfigService.java
│   │           │   └── impl/
│   │           │       └── SystemConfigServiceImpl.java
│   │           ├── mapper/
│   │           │   └── SystemConfigMapper.java
│   │           ├── entity/
│   │           │   └── SystemConfig.java
│   │           ├── dto/
│   │           │   ├── request/
│   │           │   └── response/
│   │           └── component/
│   │               └── SystemMonitor.java
│   └── resources/
│       ├── application.yml                       # 主配置文件
│       ├── application-dev.yml                   # 开发环境配置
│       ├── application-test.yml                  # 测试环境配置
│       ├── application-prod.yml                  # 生产环境配置
│       ├── mapper/                               # MyBatis XML映射文件
│       │   ├── document/
│       │   ├── rule/
│       │   ├── audit/
│       │   ├── llm/
│       │   ├── user/
│       │   └── system/
│       ├── db/migration/                         # 数据库迁移脚本
│       │   ├── V1__init_schema.sql
│       │   ├── V2__create_user_tables.sql
│       │   └── V3__create_audit_tables.sql
│       ├── static/                               # 静态资源
│       └── templates/                            # 模板文件
└── test/
    ├── java/
    │   └── com/insurance/audit/
    │       ├── document/                         # 文档模块测试
    │       ├── rule/                             # 规则模块测试
    │       ├── audit/                            # 检核模块测试
    │       ├── llm/                              # LLM模块测试
    │       ├── user/                             # 用户模块测试
    │       ├── system/                           # 系统模块测试
    │       └── integration/                      # 集成测试
    └── resources/
        ├── application-test.yml                  # 测试配置
        └── test-data/                            # 测试数据
```

## 编码规范

### 命名规范

#### 基础命名规则
- **类名**: PascalCase，如 `ProductService`、`DocumentController`
- **接口名**: PascalCase，以I开头或以Service/Repository结尾，如 `IProductService`、`ProductService`
- **方法名**: camelCase，动词开头，如 `createProduct`、`findByName`
- **变量名**: camelCase，名词性，如 `productList`、`userName`
- **常量名**: UPPER_SNAKE_CASE，如 `MAX_FILE_SIZE`、`DEFAULT_PAGE_SIZE`
- **包名**: 全小写，使用点分隔，如 `com.insurance.audit.document.service`
- **枚举名**: PascalCase，如 `ProductType`、`AuditStatus`
- **枚举值**: UPPER_SNAKE_CASE，如 `PROPERTY_INSURANCE`、`IN_PROGRESS`

#### 变量命名详细规范
```java
// 正确的变量命名
private String userName;                    // 用户名
private List<Product> productList;          // 产品列表
private Map<String, Object> configMap;      // 配置映射
private boolean isActive;                   // 布尔值以is开头
private boolean hasPermission;              // 布尔值以has开头
private boolean canEdit;                    // 布尔值以can开头
private int totalCount;                     // 总数量
private LocalDateTime createdAt;            // 创建时间
private BigDecimal unitPrice;               // 单价

// 避免的命名方式
private String s;                           // 过于简短
private List list;                          // 缺少泛型和具体含义
private Map map;                            // 缺少泛型和具体含义
private boolean flag;                       // 含义不明确
private int num;                            // 含义不明确
```

#### 方法命名规范
```java
// 查询方法
public Product findById(String id);                    // 根据ID查询
public List<Product> findByName(String name);          // 根据名称查询
public Page<Product> findByCondition(QueryCondition condition); // 条件查询
public boolean existsByName(String name);              // 判断是否存在
public long countByStatus(ProductStatus status);       // 统计数量

// 操作方法
public Product create(CreateProductRequest request);    // 创建
public Product update(String id, UpdateProductRequest request); // 更新
public void delete(String id);                         // 删除
public void batchDelete(List<String> ids);             // 批量删除

// 业务方法
public void activate(String id);                       // 激活
public void deactivate(String id);                     // 停用
public void approve(String id);                        // 审批通过
public void reject(String id, String reason);          // 审批拒绝
public AuditResult executeAudit(String taskId);        // 执行检核

// 转换方法
public ProductResponse toResponse(Product product);     // 转换为响应对象
public Product fromRequest(CreateProductRequest request); // 从请求对象转换
```

### 注解使用规范

#### Spring注解规范
```java
// Controller层注解
@RestController                                 // RESTful控制器
@RequestMapping("/api/v1/products")            // 请求映射
@Validated                                     // 参数验证
@Slf4j                                         // 日志
@Tag(name = "产品管理", description = "产品相关API") // Swagger文档
public class ProductController {
    
    @Autowired                                 // 依赖注入（推荐构造器注入）
    private final ProductService productService;
    
    @PostMapping                               // POST请求映射
    @Operation(summary = "创建产品", description = "创建新的保险产品")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }
    
    @GetMapping("/{id}")                       // GET请求映射
    @PreAuthorize("hasRole('USER')")           // 权限控制
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable @NotBlank String id) {
        return ApiResponse.success(productService.findById(id));
    }
}

// Service层注解
@Service                                       // 服务层组件
@Transactional(rollbackFor = Exception.class) // 事务管理
@Slf4j                                         // 日志
public class ProductServiceImpl implements ProductService {
    
    @Override
    @Cacheable(value = "products", key = "#id") // 缓存
    public ProductResponse findById(String id) {
        // 实现逻辑
    }
    
    @Override
    @CacheEvict(value = "products", key = "#id") // 清除缓存
    public void delete(String id) {
        // 实现逻辑
    }
    
    @Async("taskExecutor")                     // 异步执行
    public CompletableFuture<Void> processAsync(String id) {
        // 异步处理逻辑
    }
}

// Mapper层注解
@Mapper                                        // MyBatis映射器
@Repository                                    // 数据访问层组件
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT * FROM products WHERE name = #{name}")
    List<Product> findByName(@Param("name") String name);
    
    @Update("UPDATE products SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") ProductStatus status);
}

// Entity层注解
@Data                                          // Lombok数据类
@Builder                                       // 建造者模式
@NoArgsConstructor                            // 无参构造器
@AllArgsConstructor                           // 全参构造器
@EqualsAndHashCode(callSuper = true)          // equals和hashCode
@TableName("products")                        // MyBatis Plus表名
@ApiModel(description = "产品实体")            // Swagger模型
public class Product extends BaseEntity {
    
    @TableId(type = IdType.ASSIGN_UUID)       // 主键策略
    @ApiModelProperty(value = "产品ID", example = "123456")
    private String id;
    
    @TableField("product_name")               // 字段映射
    @NotBlank(message = "产品名称不能为空")
    @Length(max = 100, message = "产品名称长度不能超过100字符")
    @ApiModelProperty(value = "产品名称", required = true)
    private String name;
    
    @TableField("product_type")
    @NotNull(message = "产品类型不能为空")
    @ApiModelProperty(value = "产品类型", required = true)
    private ProductType type;
    
    @TableLogic                               // 逻辑删除
    @TableField("is_deleted")                     // 统一字段名
    @Schema(description = "是否删除", hidden = true)
    private Boolean deleted;
}
```

#### 验证注解规范
```java
// 请求DTO验证注解
@Data
@ApiModel(description = "创建产品请求")
public class CreateProductRequest {
    
    @NotBlank(message = "产品名称不能为空")
    @Length(min = 2, max = 100, message = "产品名称长度必须在2-100字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s]+$", message = "产品名称只能包含中文、英文、数字和空格")
    @ApiModelProperty(value = "产品名称", required = true, example = "财产保险产品")
    private String name;
    
    @NotNull(message = "产品类型不能为空")
    @ApiModelProperty(value = "产品类型", required = true)
    private ProductType type;
    
    @Length(max = 500, message = "产品描述长度不能超过500字符")
    @ApiModelProperty(value = "产品描述", example = "这是一个财产保险产品")
    private String description;
    
    @Valid                                    // 嵌套对象验证
    @NotEmpty(message = "产品文档不能为空")
    @ApiModelProperty(value = "产品文档列表", required = true)
    private List<DocumentInfo> documents;
    
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
    @Digits(integer = 6, fraction = 2, message = "价格格式不正确")
    @ApiModelProperty(value = "产品价格", example = "1000.00")
    private BigDecimal price;
    
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "联系邮箱")
    private String contactEmail;
    
    @Past(message = "生效日期必须是过去的时间")
    @ApiModelProperty(value = "生效日期")
    private LocalDate effectiveDate;
}
```

#### 自定义注解规范
```java
// 操作日志注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作类型
     */
    OperationType type() default OperationType.OTHER;
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录参数
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录返回值
     */
    boolean recordResult() default false;
}

// 数据权限注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    
    /**
     * 数据权限类型
     */
    DataPermissionType type() default DataPermissionType.USER;
    
    /**
     * 权限字段名
     */
    String field() default "created_by";
    
    /**
     * 权限值来源
     */
    String valueSource() default "#{@currentUser.userId}";
}

// API限流注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 每秒请求数
     */
    int value() default 10;
    
    /**
     * 突发请求数
     */
    int burst() default 20;
}

// 敏感操作审计注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    
    /**
     * 操作描述
     */
    String operation();
    
    /**
     * 是否为敏感操作
     */
    boolean sensitive() default false;
}

/**
 * API限流切面实现
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> rateLimitScript;
    
    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = createRateLimitScript();
    }
    
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = generateRateLimitKey(point);
        String userId = getCurrentUserId();
        
        // 执行限流检查
        Long result = redisTemplate.execute(rateLimitScript, 
            Collections.singletonList(key), 
            rateLimit.value(), rateLimit.burst(), System.currentTimeMillis() / 1000);
        
        if (result == 0) {
            log.warn("Rate limit exceeded for user: {}, method: {}", userId, key);
            throw new RateLimitExceededException("请求过于频繁，请稍后重试");
        }
        
        return point.proceed();
    }
    
    private String generateRateLimitKey(ProceedingJoinPoint point) {
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = point.getSignature().getName();
        String userId = getCurrentUserId();
        
        return String.format("rate_limit:%s:%s:%s", userId, className, methodName);
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        return "anonymous";
    }
    
    private RedisScript<Long> createRateLimitScript() {
        String script = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local burst = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local tokens = tonumber(bucket[1]) or burst
            local last_refill = tonumber(bucket[2]) or current_time
            
            -- 计算需要补充的令牌数
            local elapsed = current_time - last_refill
            local new_tokens = math.min(burst, tokens + elapsed * limit)
            
            if new_tokens >= 1 then
                new_tokens = new_tokens - 1
                redis.call('HMSET', key, 'tokens', new_tokens, 'last_refill', current_time)
                redis.call('EXPIRE', key, 3600)
                return 1
            else
                redis.call('HMSET', key, 'tokens', new_tokens, 'last_refill', current_time)
                redis.call('EXPIRE', key, 3600)
                return 0
            end
            """;
        
        return RedisScript.of(script, Long.class);
    }
}

// 使用自定义注解
@Service
public class ProductServiceImpl implements ProductService {
    
    @Override
    @OperationLog(type = OperationType.CREATE, description = "创建产品")
    @DataPermission(type = DataPermissionType.USER, field = "created_by")
    public ProductResponse createProduct(CreateProductRequest request) {
        // 实现逻辑
    }
}
```

### 注释规范
```java
/**
 * 产品服务接口
 * <p>
 * 提供产品的CRUD操作和业务逻辑处理
 * 包括产品创建、查询、更新、删除等功能
 * </p>
 * 
 * @author 张三
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface ProductService {
    
    /**
     * 创建产品
     * <p>
     * 根据请求参数创建新的保险产品，包括参数验证、
     * 业务规则检查、数据持久化等步骤
     * </p>
     * 
     * @param request 产品创建请求，包含产品基本信息
     * @return 创建成功的产品信息
     * @throws BusinessException 当业务规则验证失败时抛出
     * @throws SystemException 当系统异常时抛出
     * @see CreateProductRequest
     * @see ProductResponse
     */
    ProductResponse createProduct(CreateProductRequest request) throws BusinessException;
    
    /**
     * 根据ID查询产品
     * 
     * @param id 产品ID，不能为空
     * @return 产品信息，如果不存在返回null
     */
    ProductResponse findById(@NotBlank String id);
    
    /**
     * 分页查询产品列表
     * 
     * @param condition 查询条件
     * @param pageRequest 分页参数
     * @return 分页结果
     */
    PageResponse<ProductResponse> findByCondition(ProductQueryCondition condition, 
                                                 PageRequest pageRequest);
}
```

### 代码风格
- 使用Google Java Format格式化代码
- 行长度限制为100字符
- 使用4个空格缩进，不使用Tab
- 大括号不换行（K&R风格）
- 方法参数过多时换行对齐

## 领域驱动设计(DDD)架构规范

### 架构分层原则

采用六边形架构(Hexagonal Architecture)，确保业务逻辑与技术实现完全解耦：

#### 1. 接口层(Interfaces Layer)
- **职责**: 处理外部请求，数据格式转换
- **组件**: Controller、DTO、Facade
- **原则**: 只做数据转换，不包含业务逻辑

#### 2. 应用层(Application Layer)  
- **职责**: 编排业务流程，协调领域对象
- **组件**: ApplicationService、Command、Query、Result
- **原则**: 薄薄一层，不包含业务规则

#### 3. 领域层(Domain Layer)
- **职责**: 核心业务逻辑和规则
- **组件**: Entity、ValueObject、DomainService、Repository接口
- **原则**: 不依赖任何外部技术，纯业务逻辑

#### 4. 基础设施层(Infrastructure Layer)
- **职责**: 技术实现细节
- **组件**: RepositoryImpl、Adapter、Client、Config
- **原则**: 实现领域层定义的接口

### 领域模型设计规范

#### 实体(Entity)设计
```java
/**
 * 文档领域实体
 * 具有唯一标识和生命周期的业务对象
 */
@Getter
public class Document {
    
    private final DocumentId id;              // 强类型ID
    private DocumentName name;                // 值对象
    private DocumentContent content;          // 值对象
    private DocumentStatus status;            // 枚举
    private final List<DocumentVersion> versions; // 实体集合
    
    // 构造函数 - 确保实体创建时的业务规则
    public Document(DocumentId id, DocumentName name, DocumentContent content) {
        this.id = Objects.requireNonNull(id, "文档ID不能为空");
        this.name = Objects.requireNonNull(name, "文档名称不能为空");
        this.content = Objects.requireNonNull(content, "文档内容不能为空");
        this.status = DocumentStatus.DRAFT;
        this.versions = new ArrayList<>();
        
        // 发布领域事件
        DomainEventPublisher.publish(new DocumentCreatedEvent(this.id));
    }
    
    // 业务方法 - 封装业务规则
    public void publish() {
        if (!canPublish()) {
            throw new BusinessException("文档状态不允许发布");
        }
        this.status = DocumentStatus.PUBLISHED;
        DomainEventPublisher.publish(new DocumentPublishedEvent(this.id));
    }
    
    public void addVersion(DocumentContent newContent) {
        validateVersioning();
        DocumentVersion version = new DocumentVersion(
            VersionNumber.next(getLatestVersionNumber()), 
            newContent
        );
        this.versions.add(version);
        this.content = newContent;
    }
    
    private boolean canPublish() {
        return status == DocumentStatus.DRAFT && content.isValid();
    }
    
    private void validateVersioning() {
        if (versions.size() >= DocumentPolicy.MAX_VERSIONS) {
            throw new BusinessException("文档版本数量超过限制");
        }
    }
}
```

#### 值对象(Value Object)设计
```java
/**
 * 文档名称值对象
 * 不可变，通过值相等判断
 */
@Value
@EqualsAndHashCode
public class DocumentName {
    
    private static final int MAX_LENGTH = 100;
    private static final Pattern VALID_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s._-]+$");
    
    private final String value;
    
    public DocumentName(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("文档名称不能为空");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("文档名称长度不能超过" + MAX_LENGTH + "字符");
        }
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("文档名称包含非法字符");
        }
        this.value = value.trim();
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

#### 领域服务(Domain Service)设计
```java
/**
 * 文档领域服务
 * 处理跨实体的业务逻辑
 */
@Component
public class DocumentDomainService {
    
    private final DocumentRepository documentRepository;
    private final DocumentPolicy documentPolicy;
    
    /**
     * 文档去重检查
     */
    public boolean isDuplicate(DocumentContent content) {
        String contentHash = content.calculateHash();
        return documentRepository.existsByContentHash(contentHash);
    }
    
    /**
     * 文档合并
     */
    public Document mergeDocuments(List<Document> documents, MergeStrategy strategy) {
        if (documents.size() < 2) {
            throw new IllegalArgumentException("至少需要两个文档才能合并");
        }
        
        validateMergePermission(documents);
        
        DocumentContent mergedContent = strategy.merge(
            documents.stream()
                .map(Document::getContent)
                .collect(Collectors.toList())
        );
        
        DocumentName mergedName = generateMergedName(documents);
        
        return new Document(
            DocumentId.generate(),
            mergedName,
            mergedContent
        );
    }
    
    private void validateMergePermission(List<Document> documents) {
        boolean hasPermission = documents.stream()
            .allMatch(doc -> documentPolicy.canMerge(doc));
        
        if (!hasPermission) {
            throw new BusinessException("部分文档不允许合并");
        }
    }
}
```

#### 仓储接口(Repository)设计
```java
/**
 * 文档仓储接口 - 领域层定义
 * 使用领域语言，不暴露技术细节
 */
public interface DocumentRepository {
    
    // 基础CRUD
    void save(Document document);
    Optional<Document> findById(DocumentId id);
    void remove(DocumentId id);
    
    // 业务查询方法
    List<Document> findByOwner(UserId ownerId);
    List<Document> findByStatus(DocumentStatus status);
    List<Document> findByNameContaining(String keyword);
    boolean existsByContentHash(String contentHash);
    
    // 复杂查询
    Page<Document> findBySpecification(DocumentSpecification spec, Pageable pageable);
    List<Document> findSimilarDocuments(DocumentContent content, double threshold);
    
    // 聚合查询
    long countByStatus(DocumentStatus status);
    Map<DocumentType, Long> countGroupByType();
}
```

## 传统分层架构规范

### Controller层
- 只处理HTTP请求和响应
- 参数验证和格式转换
- 调用Service层处理业务逻辑
- 统一返回格式

```java
@RestController
@RequestMapping("/api/v1/products")
@Validated
@Slf4j
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable @NotBlank String id) {
        ProductResponse response = productService.getProductById(id);
        return ApiResponse.success(response);
    }
}
```

### Service层
- 处理业务逻辑
- 事务管理
- 调用Repository层进行数据操作
- 业务异常处理

```java
@Service
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final DocumentService documentService;
    
    public ProductServiceImpl(ProductRepository productRepository, 
                             DocumentService documentService) {
        this.productRepository = productRepository;
        this.documentService = documentService;
    }
    
    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        // 参数验证
        validateProductRequest(request);
        
        // 检查产品名称是否重复
        if (productRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_EXISTS);
        }
        
        // 创建产品实体
        Product product = Product.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .status(ProductStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();
        
        // 保存产品
        Product savedProduct = productRepository.save(product);
        
        // 处理文档上传
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            documentService.uploadDocuments(savedProduct.getId(), request.getDocuments());
        }
        
        log.info("Product created successfully: {}", savedProduct.getId());
        return ProductResponse.from(savedProduct);
    }
    
    private void validateProductRequest(CreateProductRequest request) {
        if (StringUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "产品名称不能为空");
        }
        if (request.getType() == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "产品类型不能为空");
        }
    }
}
```

### Mapper层（MyBatis Plus）
- 数据访问操作
- 使用MyBatis Plus框架
- 继承BaseMapper获得基础CRUD功能
- 自定义复杂查询方法

```java
@Mapper
@Repository
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据产品名称查询是否存在
     */
    default boolean existsByName(String name) {
        return selectCount(Wrappers.<Product>lambdaQuery()
                .eq(Product::getName, name)) > 0;
    }
    
    /**
     * 根据产品类型查询产品列表
     */
    default List<Product> findByType(ProductType type) {
        return selectList(Wrappers.<Product>lambdaQuery()
                .eq(Product::getType, type)
                .orderByDesc(Product::getCreatedAt));
    }
    
    /**
     * 根据状态查询产品列表，按创建时间倒序
     */
    default List<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status) {
        return selectList(Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, status)
                .orderByDesc(Product::getCreatedAt));
    }
    
    /**
     * 分页查询产品列表
     */
    default IPage<Product> findProducts(String name, ProductType type, 
                                       ProductStatus status, IPage<Product> page) {
        return selectPage(page, Wrappers.<Product>lambdaQuery()
                .like(StringUtils.isNotBlank(name), Product::getName, name)
                .eq(type != null, Product::getType, type)
                .eq(status != null, Product::getStatus, status)
                .orderByDesc(Product::getCreatedAt));
    }
    
    /**
     * 自定义SQL查询 - 复杂统计
     */
    @Select("""
        SELECT p.type, COUNT(*) as count, AVG(p.price) as avg_price
        FROM products p 
        WHERE p.is_deleted = 0 
          AND p.created_at >= #{startDate}
          AND p.created_at <= #{endDate}
        GROUP BY p.type
        """)
    List<ProductStatistics> getProductStatistics(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * 批量更新状态
     */
    @Update("""
        UPDATE products 
        SET status = #{status}, updated_at = NOW() 
        WHERE id IN 
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        """)
    int batchUpdateStatus(@Param("ids") List<String> ids, @Param("status") ProductStatus status);
    
    /**
     * 使用XML映射的复杂查询
     */
    List<ProductDetailVO> findProductsWithDetails(@Param("condition") ProductQueryCondition condition);
}
```

#### MyBatis Plus配置
```java
@Configuration
@EnableTransactionManagement
@MapperScan("com.insurance.audit.**.mapper")
public class MyBatisPlusConfig {
    
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大分页数量限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 数据权限插件
        interceptor.addInnerInterceptor(new DataPermissionInterceptor());
        
        return interceptor;
    }
    
    /**
     * 元数据填充器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }
    
    /**
     * ID生成器
     */
    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new CustomIdGenerator();
    }
}

/**
 * 自动填充处理器
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        
        // 填充创建时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        
        // 填充创建人（从当前登录用户获取）
        String currentUserId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createdBy", String.class, currentUserId);
        this.strictInsertFill(metaObject, "updatedBy", String.class, currentUserId);
        
        // 填充默认值
        this.strictInsertFill(metaObject, "deleted", Boolean.class, false);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        
        // 填充更新人
        String currentUserId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updatedBy", String.class, currentUserId);
    }
    
    private String getCurrentUserId() {
        // 从Spring Security上下文获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        return "system";
    }
}
```

## 数据库设计规范

### 表设计规范
- 表名使用小写字母和下划线，复数形式
- 主键统一使用`id`，类型为VARCHAR(32)
- 必须包含`created_at`和`updated_at`字段
- 软删除使用`is_deleted`字段
- 外键字段以`_id`结尾

```sql
-- 产品表
CREATE TABLE products (
    id VARCHAR(32) PRIMARY KEY COMMENT '产品ID',
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    type VARCHAR(20) NOT NULL COMMENT '产品类型',
    description TEXT COMMENT '产品描述',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '产品状态',
    created_by VARCHAR(32) COMMENT '创建人ID',
    updated_by VARCHAR(32) COMMENT '更新人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';
```

### 枚举序列化规范

#### 枚举序列化策略选择

根据不同的业务场景，选择合适的枚举序列化策略：

**1. 简单序列化（推荐）- 仅输出code**
```java
@Getter
@AllArgsConstructor
public enum ProductType implements IEnum<String> {
    
    PROPERTY("PROPERTY", "财产保险");
    
    @EnumValue
    @JsonValue  // 输出: "PROPERTY"
    private final String code;
    
    private final String description;
}
```

**2. 对象序列化 - 输出完整信息**
```java
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)  // 输出: {"code":"PROPERTY","description":"财产保险"}
public enum ProductType implements IEnum<String> {
    
    PROPERTY("PROPERTY", "财产保险");
    
    @EnumValue
    private final String code;
    
    private final String description;
    
    // 注意：使用OBJECT序列化时，不要同时使用@JsonValue
}
```

**3. 混合策略 - 提供toDto()方法**
```java
// 默认序列化为简单code，需要完整信息时调用toDto()
public EnumDto toDto() {
    return EnumDto.builder()
        .code(this.code)
        .description(this.description)
        .build();
}
```

#### 枚举序列化最佳实践

1. **统一性原则**: 项目内所有枚举使用相同的序列化策略
2. **避免冲突**: 不要同时使用`@JsonFormat(OBJECT)`和`@JsonValue`
3. **前后端约定**: 与前端团队明确枚举数据格式
4. **向后兼容**: 枚举值变更时保持向后兼容性

### 实体类规范

#### 基础实体类
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

/**
 * 产品实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("products")
@ApiModel(description = "产品实体")
public class Product extends BaseEntity {
    
    /**
     * 产品名称
     */
    @TableField("product_name")
    @NotBlank(message = "产品名称不能为空")
    @Length(max = 100, message = "产品名称长度不能超过100字符")
    @ApiModelProperty(value = "产品名称", required = true, example = "财产保险产品A")
    private String name;
    
    /**
     * 产品类型
     */
    @TableField("product_type")
    @NotNull(message = "产品类型不能为空")
    @ApiModelProperty(value = "产品类型", required = true)
    private ProductType type;
    
    /**
     * 产品描述
     */
    @TableField("description")
    @Length(max = 500, message = "产品描述长度不能超过500字符")
    @ApiModelProperty(value = "产品描述", example = "这是一个财产保险产品")
    private String description;
    
    /**
     * 产品状态
     */
    @TableField("status")
    @NotNull(message = "产品状态不能为空")
    @Builder.Default
    @ApiModelProperty(value = "产品状态", required = true)
    private ProductStatus status = ProductStatus.DRAFT;
    
    /**
     * 产品价格
     */
    @TableField("price")
    @DecimalMin(value = "0.01", message = "产品价格必须大于0")
    @Digits(integer = 10, fraction = 2, message = "价格格式不正确")
    @ApiModelProperty(value = "产品价格", example = "1000.00")
    private BigDecimal price;
    
    /**
     * 生效日期
     */
    @TableField("effective_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "生效日期", example = "2024-01-01")
    private LocalDate effectiveDate;
    
    /**
     * 失效日期
     */
    @TableField("expiry_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "失效日期", example = "2024-12-31")
    private LocalDate expiryDate;
    
    /**
     * 产品配置（JSON格式）
     */
    @TableField(value = "config", typeHandler = JacksonTypeHandler.class)
    @ApiModelProperty(value = "产品配置")
    private Map<String, Object> config;
    
    /**
     * 产品标签
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "产品标签列表")
    private List<String> tags;
    
    /**
     * 关联文档数量
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "关联文档数量")
    private Integer documentCount;
}

/**
 * 产品类型枚举 - 明确序列化策略
 */
@Getter
@AllArgsConstructor
public enum ProductType implements IEnum<String> {
    
    PROPERTY("PROPERTY", "财产保险"),
    LIABILITY("LIABILITY", "责任保险"),
    ACCIDENT("ACCIDENT", "意外保险"),
    HEALTH("HEALTH", "健康保险"),
    LIFE("LIFE", "人寿保险");
    
    @EnumValue
    @JsonValue  // 明确：输出code而非description，避免混淆
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public static ProductType fromCode(String code) {
        for (ProductType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown product type code: " + code);
    }
    
    /**
     * 获取完整信息的DTO（用于下拉框等场景）
     */
    public EnumDto toDto() {
        return EnumDto.builder()
            .code(this.code)
            .description(this.description)
            .build();
    }
}

/**
 * 通用枚举DTO - 用于需要同时返回code和description的场景
 */
@Data
@Builder
@Schema(description = "枚举数据传输对象")
public class EnumDto {
    
    @Schema(description = "枚举代码", example = "DRAFT")
    private String code;
    
    @Schema(description = "枚举描述", example = "草稿")
    private String description;
}

/**
 * 产品状态枚举 - 统一序列化策略
 */
@Getter
@AllArgsConstructor
public enum ProductStatus implements IEnum<String> {
    
    DRAFT("DRAFT", "草稿"),
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已审核"),
    PUBLISHED("PUBLISHED", "已发布"),
    SUSPENDED("SUSPENDED", "已暂停"),
    ARCHIVED("ARCHIVED", "已归档");
    
    @EnumValue
    @JsonValue  // 统一：输出code，保持与ProductType一致
    private final String code;
    
    private final String description;
    
    @Override
    public String getValue() {
        return this.code;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public static ProductStatus fromCode(String code) {
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown product status code: " + code);
    }
    
    /**
     * 获取完整信息的DTO（用于下拉框等场景）
     */
    public EnumDto toDto() {
        return EnumDto.builder()
            .code(this.code)
            .description(this.description)
            .build();
    }
}
```

## DTO/Entity转换规范

### 数据传输对象(DTO)设计原则

#### 1. 严格分离原则
- **禁止直接返回Entity**: Controller层绝不能直接返回领域实体
- **禁止Entity作为参数**: 不能将Entity作为方法参数传递
- **单向转换**: DTO → Entity 或 Entity → DTO，避免双向依赖

#### 2. DTO分类规范
```java
// 命令DTO - 用于接收客户端请求
@Data
@Valid
public class CreateDocumentCommand {
    
    @NotBlank(message = "文档名称不能为空")
    @Length(max = 100, message = "文档名称长度不能超过100字符")
    private String name;
    
    @NotNull(message = "文档类型不能为空")
    private DocumentType type;
    
    @Valid
    @NotNull(message = "文档内容不能为空")
    private DocumentContentDto content;
    
    // 转换为领域对象的方法
    public Document toDomain() {
        return new Document(
            DocumentId.generate(),
            new DocumentName(this.name),
            new DocumentContent(this.content.getData(), this.content.getFormat())
        );
    }
}

// 查询DTO - 用于查询条件
@Data
public class DocumentQuery {
    
    private String nameKeyword;
    private DocumentType type;
    private DocumentStatus status;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    private Integer page = 0;
    private Integer size = 20;
    
    // 转换为规约对象
    public DocumentSpecification toSpecification() {
        return DocumentSpecification.builder()
            .nameContaining(this.nameKeyword)
            .type(this.type)
            .status(this.status)
            .createdBetween(this.createdAfter, this.createdBefore)
            .build();
    }
}

// 结果DTO - 用于返回客户端
@Data
@Builder
public class DocumentResult {
    
    private String id;
    private String name;
    private String type;
    private String status;
    private Long size;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 从领域对象转换的静态方法
    public static DocumentResult from(Document document) {
        return DocumentResult.builder()
            .id(document.getId().getValue())
            .name(document.getName().getValue())
            .type(document.getType().name())
            .status(document.getStatus().name())
            .size(document.getContent().getSize())
            .createdBy(document.getCreatedBy().getValue())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .build();
    }
}
```

#### 3. 转换器(Assembler)规范
```java
/**
 * 文档组装器 - 负责复杂的DTO转换逻辑
 */
@Component
public class DocumentAssembler {
    
    /**
     * 领域对象转换为详细结果DTO
     */
    public DocumentDetailResult toDetailResult(Document document) {
        return DocumentDetailResult.builder()
            .id(document.getId().getValue())
            .name(document.getName().getValue())
            .type(document.getType().name())
            .status(document.getStatus().name())
            .content(toContentResult(document.getContent()))
            .versions(toVersionResults(document.getVersions()))
            .metadata(toMetadataResult(document.getMetadata()))
            .createdBy(document.getCreatedBy().getValue())
            .createdAt(document.getCreatedAt())
            .updatedAt(document.getUpdatedAt())
            .build();
    }
    
    /**
     * 批量转换
     */
    public List<DocumentResult> toResults(List<Document> documents) {
        return documents.stream()
            .map(DocumentResult::from)
            .collect(Collectors.toList());
    }
    
    /**
     * 分页结果转换
     */
    public PageResult<DocumentResult> toPageResult(Page<Document> documentPage) {
        List<DocumentResult> results = toResults(documentPage.getContent());
        return PageResult.<DocumentResult>builder()
            .content(results)
            .totalElements(documentPage.getTotalElements())
            .totalPages(documentPage.getTotalPages())
            .currentPage(documentPage.getNumber())
            .size(documentPage.getSize())
            .build();
    }
    
    private DocumentContentResult toContentResult(DocumentContent content) {
        return DocumentContentResult.builder()
            .format(content.getFormat().name())
            .size(content.getSize())
            .hash(content.getHash())
            .preview(content.getPreview())
            .build();
    }
}
```

#### 4. 防腐层(Anti-Corruption Layer)
```java
/**
 * 外部系统防腐层
 * 将外部系统的数据模型转换为内部领域模型
 */
@Component
public class ExternalDocumentAdapter {
    
    /**
     * 第三方OCR服务结果转换
     */
    public DocumentContent fromOcrResult(OcrServiceResponse ocrResponse) {
        // 验证外部数据
        validateOcrResponse(ocrResponse);
        
        // 转换为内部格式
        String cleanedText = cleanOcrText(ocrResponse.getText());
        DocumentFormat format = mapOcrFormat(ocrResponse.getFormat());
        
        return new DocumentContent(cleanedText, format);
    }
    
    /**
     * 外部文件存储系统适配
     */
    public Document fromFileStorageMetadata(FileMetadata metadata) {
        DocumentName name = new DocumentName(metadata.getFileName());
        DocumentContent content = new DocumentContent(
            metadata.getContent(), 
            DocumentFormat.fromMimeType(metadata.getMimeType())
        );
        
        return new Document(
            DocumentId.fromString(metadata.getFileId()),
            name,
            content
        );
    }
    
    private void validateOcrResponse(OcrServiceResponse response) {
        if (response == null || StringUtils.isBlank(response.getText())) {
            throw new ExternalServiceException("OCR服务返回无效结果");
        }
    }
}
```

## API设计规范

### RESTful API规范
- 使用标准HTTP方法（GET, POST, PUT, DELETE）
- URL使用小写字母和连字符
- 版本控制：`/api/v1/`
- 统一响应格式

### 统一响应格式
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private Integer code;
    private String message;
    private T data;
    private Boolean success;
    private Long timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    // 兼容方法，但建议使用String类型错误码
    @Deprecated
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return error(String.valueOf(code), message);
    }
}
```

### 分页响应格式
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
    private Integer pages;
    
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .pages(page.getTotalPages())
                .build();
    }
}
```

## 异常处理规范

### 全局异常处理
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation exception: {}", message);
        return ApiResponse.error(ErrorCode.INVALID_PARAMETER.getCode(), message);
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "系统内部错误");
    }
}
```

### 业务异常定义
```java
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    SUCCESS(200, "操作成功"),
    INVALID_PARAMETER(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    
    // 业务错误码
    PRODUCT_NOT_FOUND(1001, "产品不存在"),
    PRODUCT_NAME_EXISTS(1002, "产品名称已存在"),
    DOCUMENT_UPLOAD_FAILED(2001, "文档上传失败"),
    AUDIT_TASK_NOT_FOUND(3001, "检核任务不存在");
    
    private final Integer code;
    private final String message;
}
```

## 配置管理

### 应用配置
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: insurance-audit-system
  profiles:
    active: dev
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/insurance_audit?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000  # 连接泄漏检测
      connection-test-query: SELECT 1  # 连接测试查询

  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:}
  
  # 链路追踪配置
  sleuth:
    zipkin:
      base-url: ${ZIPKIN_BASE_URL:http://zipkin:9411}
    sampler:
      probability: ${TRACE_SAMPLE_RATE:0.1}

# 自定义配置
app:
  milvus:
    host: ${MILVUS_HOST:localhost}
    port: ${MILVUS_PORT:19530}
    database: ${MILVUS_DATABASE:insurance_audit}
  
  llm:
    # 成本与配额控制
    cost-control:
      daily-budget: ${LLM_DAILY_BUDGET:1000.00}     # 每日预算(USD)
      monthly-budget: ${LLM_MONTHLY_BUDGET:30000.00} # 月度预算(USD)
      alert-threshold: 0.8                           # 预算告警阈值
      emergency-stop: 0.95                          # 紧急停止阈值
    
    # Token使用限制
    token-limits:
      max-input-tokens: 32000                       # 单次最大输入Token
      max-output-tokens: 4000                       # 单次最大输出Token
      daily-token-limit: 1000000                    # 每日Token限制
      rate-limit-per-minute: 100                    # 每分钟请求限制
    
    # 模型切换策略
    fallback-strategy:
      enabled: true
      primary-models: ["gpt-4", "qianwen-max"]      # 主要模型
      fallback-models: ["gpt-3.5-turbo", "qianwen-plus"] # 备用模型
      emergency-models: ["qianwen-turbo"]           # 紧急备用模型
      health-check-interval: 30s                   # 健康检查间隔
    
    # 安全过滤配置
    security:
      content-filter:
        enabled: true
        sensitive-keywords: ${LLM_SENSITIVE_KEYWORDS:}
        pii-detection: true                         # 个人信息检测
        compliance-check: true                      # 合规检查
      output-filter:
        enabled: true
        max-similarity-threshold: 0.9               # 输出相似度阈值
        hallucination-detection: true               # 幻觉检测
    
    # Embedding配置
    embedding:
      default-model: "text-embedding-ada-002"
      dimension-validation: true                    # 维度校验
      supported-dimensions: [1536, 768, 512]       # 支持的维度
      similarity-threshold: 0.7                    # 相似度阈值
      batch-size: 100                             # 批处理大小
    
    # 提供商配置
    providers:
      openai:
        api-key: ${OPENAI_API_KEY:}
        base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
        timeout: 60s
        retry-attempts: 3
        circuit-breaker:
          failure-threshold: 5
          timeout: 30s
          reset-timeout: 60s
        models:
          - name: "gpt-4"
            input-cost-per-1k: 0.03
            output-cost-per-1k: 0.06
            max-tokens: 8192
          - name: "gpt-3.5-turbo"
            input-cost-per-1k: 0.001
            output-cost-per-1k: 0.002
            max-tokens: 4096
      
      qianwen:
        api-key: ${QIANWEN_API_KEY:}
        base-url: ${QIANWEN_BASE_URL:}
        timeout: 60s
        retry-attempts: 3
        circuit-breaker:
          failure-threshold: 5
          timeout: 30s
          reset-timeout: 60s
        models:
          - name: "qianwen-max"
            input-cost-per-1k: 0.02
            output-cost-per-1k: 0.04
            max-tokens: 6000
          - name: "qianwen-plus"
            input-cost-per-1k: 0.008
            output-cost-per-1k: 0.016
            max-tokens: 4000
    
    # Prompt版本管理
    prompt-management:
      versioning: true
      storage-type: "database"                      # database/file/redis
      auto-backup: true
      retention-days: 90
      approval-required: true                       # 生产环境需要审批
  
  file:
    upload-path: ${FILE_UPLOAD_PATH:/tmp/uploads}
    max-size: ${FILE_MAX_SIZE:100MB}
    allowed-types: pdf,doc,docx,xls,xlsx

logging:
  level:
    com.insurance.audit: INFO
    org.springframework.web: INFO                 # 生产环境应为INFO，细粒度trace用MDC+采样
    org.springframework.security: WARN
    com.baomidou.mybatisplus: WARN
    org.apache.http: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/insurance-audit-system.log
    max-size: 100MB
    max-history: 30

# 监控配置
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

# 注意：链路追踪配置应合并到主spring配置中，避免重复定义
# 在主spring配置中添加：
#   sleuth:
#     zipkin:
#       base-url: ${ZIPKIN_BASE_URL:http://zipkin:9411}
#     sampler:
#       probability: ${TRACE_SAMPLE_RATE:0.1}
```

## 安全规范

### Spring Security + JWT认证授权
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAccessDeniedHandler jwtAccessDeniedHandler,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }
    
    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // 平衡安全性和性能
    }
    
    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * 认证提供者
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }
    
    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT不需要CSRF保护）
            .csrf(csrf -> csrf.disable())
            
            // 禁用CORS（如需要可单独配置）
            .cors(cors -> cors.disable())
            
            // 会话管理 - 无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            
            // 请求授权配置
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/actuator/**").permitAll()
                
                // Swagger文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**", "/favicon.ico").permitAll()
                
                // 静态资源
                .requestMatchers("/static/**", "/public/**").permitAll()
                
                // 管理员接口
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // 用户接口
                .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN")
                
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            
            // 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            
            // 认证提供者
            .authenticationProvider(authenticationProvider())
            
            // JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 安全头配置
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
            );
        
        return http.build();
    }
    
    /**
     * Web安全配置
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers("/error")
            .requestMatchers("/favicon.ico");
    }
    
    /**
     * API限流配置
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);  // 每秒10个请求，突发20个
    }
}

/**
 * JWT认证过滤器
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, 
                                 CustomUserDetailsService userDetailsService,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            try {
                // 验证token
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    // 检查token是否在黑名单中
                    if (isTokenBlacklisted(token)) {
                        log.warn("Token is blacklisted: {}", token);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    // 加载用户信息
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("User {} authenticated successfully", username);
                }
            } catch (Exception e) {
                log.error("Cannot set user authentication: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isTokenBlacklisted(String token) {
        String key = RedisKeyConstant.TOKEN_BLACKLIST + jwtUtil.getJtiFromToken(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

/**
 * 自定义用户详情服务
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    
    public CustomUserDetailsService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
            .eq(User::getUsername, username)
            .eq(User::getDeleted, false));
        
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        if (!user.getEnabled()) {
            throw new DisabledException("用户已被禁用: " + username);
        }
        
        if (!user.getAccountNonLocked()) {
            throw new LockedException("用户账户已被锁定: " + username);
        }
        
        // 查询用户角色和权限
        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
        
        return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getEnabled(),
            user.getAccountNonExpired(),
            user.getCredentialsNonExpired(),
            user.getAccountNonLocked(),
            authorities
        );
    }
}

/**
 * 自定义用户详情
 */
@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private String userId;
    private String username;
    private String password;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private Collection<? extends GrantedAuthority> authorities;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
```

### 数据脱敏
```java
@Component
public class DataMaskingUtil {
    
    /**
     * 手机号脱敏
     */
    public static String maskPhone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 身份证号脱敏
     */
    public static String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
}
```

## 测试规范

### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private DocumentService documentService;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    @Test
    @DisplayName("创建产品成功")
    void createProduct_Success() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
                .name("测试产品")
                .type(ProductType.PROPERTY)
                .description("测试描述")
                .build();
        
        Product savedProduct = Product.builder()
                .id("test-id")
                .name("测试产品")
                .type(ProductType.PROPERTY)
                .status(ProductStatus.DRAFT)
                .build();
        
        when(productRepository.existsByName("测试产品")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        
        // When
        ProductResponse response = productService.createProduct(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("测试产品");
        assertThat(response.getType()).isEqualTo(ProductType.PROPERTY);
        
        verify(productRepository).existsByName("测试产品");
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("创建产品失败 - 产品名称已存在")
    void createProduct_NameExists() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
                .name("已存在产品")
                .type(ProductType.PROPERTY)
                .build();
        
        when(productRepository.existsByName("已存在产品")).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("产品名称已存在");
        
        verify(productRepository).existsByName("已存在产品");
        verify(productRepository, never()).save(any(Product.class));
    }
}
```

### 集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class ProductControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    @DisplayName("创建产品接口测试")
    void createProduct_Integration() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
                .name("集成测试产品")
                .type(ProductType.PROPERTY)
                .description("集成测试描述")
                .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/products", request, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        
        // 验证数据库
        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("集成测试产品");
    }
}
```

## 性能优化

### 缓存策略
```java
@Service
@CacheConfig(cacheNames = "products")
public class ProductServiceImpl implements ProductService {
    
    @Cacheable(key = "#id")
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }
    
    @CacheEvict(key = "#id")
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
    
    @CachePut(key = "#result.id")
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        // 更新逻辑
        return ProductResponse.from(updatedProduct);
    }
}
```

### 异步处理
```java
@Service
@Slf4j
public class AuditService {
    
    @Async("auditTaskExecutor")
    public CompletableFuture<AuditResult> executeAuditAsync(String taskId) {
        try {
            log.info("Starting async audit task: {}", taskId);
            
            // 执行检核逻辑
            AuditResult result = performAudit(taskId);
            
            log.info("Audit task completed: {}", taskId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Audit task failed: {}", taskId, e);
            throw new CompletionException(e);
        }
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean("auditTaskExecutor")
    public TaskExecutor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("audit-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

## 监控和日志

### 应用监控
```java
@Component
public class ApplicationMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter auditTaskCounter;
    private final Timer auditTaskTimer;
    
    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.auditTaskCounter = Counter.builder("audit.task.count")
                .description("Number of audit tasks")
                .register(meterRegistry);
        this.auditTaskTimer = Timer.builder("audit.task.duration")
                .description("Audit task execution time")
                .register(meterRegistry);
    }
    
    public void incrementAuditTaskCount() {
        auditTaskCounter.increment();
    }
    
    public Timer.Sample startAuditTaskTimer() {
        return Timer.start(meterRegistry);
    }
}
```

### 日志规范
```java
@Slf4j
@Service
public class ProductService {
    
    public ProductResponse createProduct(CreateProductRequest request) {
        // 使用结构化日志
        log.info("Creating product: name={}, type={}", request.getName(), request.getType());
        
        try {
            Product product = doCreateProduct(request);
            
            // 记录成功日志
            log.info("Product created successfully: id={}, name={}", 
                    product.getId(), product.getName());
            
            return ProductResponse.from(product);
        } catch (Exception e) {
            // 记录错误日志
            log.error("Failed to create product: name={}, error={}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        }
    }
}
```

## 代码质量控制

### Maven配置
```xml
<!-- pom.xml 代码质量插件配置 -->
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    
    <!-- 版本管理 -->
    <spring-boot.version>3.2.1</spring-boot.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <mysql.version>8.0.33</mysql.version>
    <spring-security.version>6.2.1</spring-security.version>
    <redis.version>7.0.15</redis.version>
    
    <!-- 代码质量工具版本 -->
    <spotbugs.version>4.8.3</spotbugs.version>
    <checkstyle.version>10.12.7</checkstyle.version>
    <pmd.version>3.21.2</pmd.version>
    <jacoco.version>0.8.11</jacoco.version>
    <sonar.version>3.10.0.2594</sonar.version>
    <maven-compiler.version>3.12.1</maven-compiler.version>
</properties>

<build>
    <plugins>
        <!-- Spring Boot插件 -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        
        <!-- 编译插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>${maven-compiler.version}</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <encoding>UTF-8</encoding>
                <compilerArgs>
                    <arg>-parameters</arg>
                </compilerArgs>
            </configuration>
        </plugin>
        
        <!-- Checkstyle代码风格检查 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
            <version>3.3.1</version>
            <configuration>
                <configLocation>checkstyle.xml</configLocation>
                <encoding>UTF-8</encoding>
                <consoleOutput>true</consoleOutput>
                <failsOnError>true</failsOnError>
                <includeTestSourceDirectory>true</includeTestSourceDirectory>
            </configuration>
            <executions>
                <execution>
                    <id>validate</id>
                    <phase>validate</phase>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- PMD代码质量检查 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>${pmd.version}</version>
            <configuration>
                <rulesets>
                    <ruleset>pmd-rules.xml</ruleset>
                </rulesets>
                <printFailingErrors>true</printFailingErrors>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- SpotBugs静态分析 -->
        <plugin>
            <groupId>com.github.spotbugs</groupId>
            <artifactId>spotbugs-maven-plugin</artifactId>
            <version>${spotbugs.version}</version>
            <configuration>
                <effort>Max</effort>
                <threshold>Low</threshold>
                <xmlOutput>true</xmlOutput>
                <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>check</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- JaCoCo测试覆盖率 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>${jacoco.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        
        <!-- SonarQube代码质量分析 -->
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
            <version>${sonar.version}</version>
        </plugin>
    </plugins>
</build>
```

### 代码质量标准
```yaml
# 代码质量要求
quality_standards:
  test_coverage:
    line_coverage: ">= 85%"      # 从80%提升
    branch_coverage: ">= 75%"    # 从70%提升
    method_coverage: ">= 90%"    # 从85%提升
  
  complexity:
    cyclomatic_complexity: "<= 10"
    cognitive_complexity: "<= 15"
    method_length: "<= 50 lines"
    class_length: "<= 500 lines"
  
  code_smells:
    duplicated_lines: "< 3%"
    maintainability_rating: "A"
    reliability_rating: "A"
    security_rating: "A"
  
  technical_debt:
    debt_ratio: "< 5%"
    debt_per_file: "< 30min"
```

## 版本控制规范

### Git工作流规范
```bash
# 分支命名规范
main                    # 主分支，生产环境代码
develop                 # 开发分支，集成最新功能
release/v1.2.0         # 发布分支
hotfix/fix-login-bug   # 热修复分支
feature/user-auth      # 功能分支
bugfix/fix-audit-error # 缺陷修复分支

# 提交信息规范（Conventional Commits）
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整（不影响功能）
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建过程或辅助工具的变动
ci: CI/CD相关
revert: 回滚提交

# 提交信息格式
<type>(<scope>): <subject>

<body>

<footer>

# 示例
feat(user): add user authentication module

- Implement JWT token generation and validation
- Add user login and logout endpoints
- Create user details service for Spring Security

Closes #123
```

### Git Hooks配置
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "Running pre-commit checks..."

# 运行代码格式检查
echo "Checking code style..."
mvn checkstyle:check
if [ $? -ne 0 ]; then
    echo "Code style check failed. Please fix the issues and try again."
    exit 1
fi

# 运行单元测试
echo "Running unit tests..."
mvn test
if [ $? -ne 0 ]; then
    echo "Unit tests failed. Please fix the failing tests and try again."
    exit 1
fi

# 运行静态代码分析
echo "Running static code analysis..."
mvn spotbugs:check pmd:check
if [ $? -ne 0 ]; then
    echo "Static code analysis failed. Please fix the issues and try again."
    exit 1
fi

echo "All pre-commit checks passed!"
exit 0
```

### 版本发布规范
```yaml
# 版本号规范（语义化版本）
version_format: "MAJOR.MINOR.PATCH"

# 版本递增规则
major: # 主版本号（不兼容的API修改）
  - 架构重大变更
  - 不兼容的API变更
  - 删除已弃用的功能

minor: # 次版本号（向下兼容的功能性新增）
  - 新增功能
  - 标记功能为弃用
  - 内部功能的大幅改进

patch: # 修订号（向下兼容的问题修正）
  - 缺陷修复
  - 安全补丁
  - 性能优化

# 预发布版本
alpha: "1.2.0-alpha.1"    # 内部测试版本
beta: "1.2.0-beta.1"      # 公开测试版本
rc: "1.2.0-rc.1"          # 发布候选版本
```

## 部署规范

### Docker配置
```dockerfile
# Dockerfile - 多阶段构建
FROM maven:3.9.6-openjdk-17-slim AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17-jre-slim

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# 复制构建产物
COPY --from=builder /app/target/insurance-audit-system.jar app.jar

# 设置文件权限
RUN chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 健康检查 - 替换为/actuator/health (限定只返回UP)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseContainerSupport"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Compose配置
```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: 
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
      - MILVUS_HOST=milvus
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - app-network
    volumes:
      - app-logs:/app/logs
      - app-uploads:/app/uploads
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 4G
        reservations:
          cpus: '1.0'
          memory: 2G

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: insurance_audit
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./db/init:/docker-entrypoint-initdb.d
    networks:
      - app-network
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10
    restart: unless-stopped

  redis:
    image: redis:7.0-alpine
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    networks:
      - app-network
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
    restart: unless-stopped

  milvus:
    image: milvusdb/milvus:v2.3.4
    command: ["milvus", "run", "standalone"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - milvus_data:/var/lib/milvus
    networks:
      - app-network
    ports:
      - "19530:19530"
    depends_on:
      - etcd
      - minio
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
  milvus_data:
  app-logs:
  app-uploads:

networks:
  app-network:
    driver: bridge
```
## 
最佳实践

### 1. 数据库设计最佳实践
```sql
-- 表设计原则
-- 1. 所有表必须有主键
-- 2. 使用统一的字段命名规范
-- 3. 合理使用索引
-- 4. 避免使用外键约束（在应用层控制）

-- 示例表结构
CREATE TABLE `products` (
  `id` varchar(32) NOT NULL COMMENT '产品ID',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `product_type` varchar(20) NOT NULL COMMENT '产品类型',
  `description` text COMMENT '产品描述',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '产品状态',
  `price` decimal(12,2) DEFAULT NULL COMMENT '产品价格',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '失效日期',
  `config` json DEFAULT NULL COMMENT '产品配置',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人ID',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `version` int NOT NULL DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_name` (`product_name`),
  KEY `idx_product_type` (`product_type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';
```

### 2. 缓存策略最佳实践
```java
/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .computePrefixWith(cacheName -> "insurance:audit:" + cacheName + ":")
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .transactionAware()  // 添加事务感知
            .build();
    }
}

/**
 * 缓存使用示例
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    // 查询缓存
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public ProductResponse findById(String id) {
        // 查询逻辑
    }
    
    // 更新缓存
    @CachePut(value = "products", key = "#result.id")
    public ProductResponse update(String id, UpdateProductRequest request) {
        // 更新逻辑
    }
    
    // 删除缓存
    @CacheEvict(value = "products", key = "#id")
    public void delete(String id) {
        // 删除逻辑
    }
    
    // 批量删除缓存
    @CacheEvict(value = "products", allEntries = true)
    public void batchDelete(List<String> ids) {
        // 批量删除逻辑
    }
}
```

### 3. 异常处理最佳实践
```java
/**
 * 业务异常基类
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final String code;
    private final String message;
    private final Object[] args;
    
    protected BaseException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = args;
    }
}

/**
 * 业务异常
 */
public class BusinessException extends BaseException {
    
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getCode(), errorCode.getMessage(), args);
    }
    
    public BusinessException(String code, String message, Object... args) {
        super(code, message, args);
    }
}

/**
 * 系统异常
 */
public class SystemException extends BaseException {
    
    public SystemException(ErrorCode errorCode, Object... args) {
        super(errorCode.getCode(), errorCode.getMessage(), args);
    }
    
    public SystemException(String code, String message, Throwable cause, Object... args) {
        super(code, message, args);
        initCause(cause);
    }
}

/**
 * 全局异常处理器增强
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception occurred: {} at {}", e.getMessage(), request.getRequestURI());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(SystemException.class)
    public ApiResponse<Void> handleSystemException(SystemException e, HttpServletRequest request) {
        log.error("System exception occurred: {} at {}", e.getMessage(), request.getRequestURI(), e);
        return ApiResponse.error(e.getCode(), "系统内部错误，请稍后重试");
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        log.warn("Validation failed: {}", errors);
        return ApiResponse.error("VALIDATION_FAILED", "参数验证失败", errors);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ApiResponse.error("ACCESS_DENIED", "访问被拒绝");
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception occurred: {} at {}", e.getMessage(), request.getRequestURI(), e);
        return ApiResponse.error("INTERNAL_ERROR", "系统内部错误");
    }
}
```

### 4. 日志记录最佳实践
```java
/**
 * 操作日志切面
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {
    
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();
        
        // 记录请求参数
        Object[] args = point.getArgs();
        if (operationLog.recordParams() && args.length > 0) {
            log.info("Method {}.{} called with params: {}", 
                className, methodName, JsonUtil.toJson(args));
        }
        
        try {
            Object result = point.proceed();
            
            // 记录返回结果
            if (operationLog.recordResult() && result != null) {
                log.info("Method {}.{} returned: {}", 
                    className, methodName, JsonUtil.toJson(result));
            }
            
            // 记录执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Method {}.{} executed in {} ms", 
                className, methodName, executionTime);
            
            // 保存操作日志到数据库
            saveOperationLog(operationLog, className, methodName, args, result, executionTime, true, null);
            
            return result;
        } catch (Exception e) {
            log.error("Method {}.{} failed: {}", className, methodName, e.getMessage(), e);
            
            // 保存错误日志
            saveOperationLog(operationLog, className, methodName, args, null, 
                System.currentTimeMillis() - startTime, false, e.getMessage());
            
            throw e;
        }
    }
    
    private void saveOperationLog(OperationLog operationLog, String className, String methodName,
                                Object[] args, Object result, long executionTime, 
                                boolean success, String errorMessage) {
        // 异步保存操作日志
        CompletableFuture.runAsync(() -> {
            try {
                OperationLogEntity logEntity = OperationLogEntity.builder()
                    .operationType(operationLog.type().name())
                    .description(operationLog.description())
                    .className(className)
                    .methodName(methodName)
                    .params(operationLog.recordParams() ? JsonUtil.toJson(args) : null)
                    .result(operationLog.recordResult() ? JsonUtil.toJson(result) : null)
                    .executionTime(executionTime)
                    .success(success)
                    .errorMessage(errorMessage)
                    .userId(getCurrentUserId())
                    .userIp(getCurrentUserIp())
                    .build();
                
                operationLogService.save(logEntity);
            } catch (Exception e) {
                log.error("Failed to save operation log", e);
            }
        });
    }
}
```

### 5. 性能监控最佳实践
```java
/**
 * 性能监控配置
 */
@Configuration
public class MonitoringConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }
}

/**
 * 性能监控使用示例
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    private final Counter productCreatedCounter;
    private final Timer productQueryTimer;
    
    public ProductServiceImpl(MeterRegistry meterRegistry) {
        this.productCreatedCounter = Counter.builder("product.created")
            .description("Number of products created")
            .register(meterRegistry);
        
        this.productQueryTimer = Timer.builder("product.query.duration")
            .description("Product query duration")
            .register(meterRegistry);
    }
    
    @Override
    @Timed(name = "product.create", description = "Time taken to create product")
    public ProductResponse createProduct(CreateProductRequest request) {
        ProductResponse response = doCreateProduct(request);
        productCreatedCounter.increment();
        return response;
    }
    
    @Override
    public ProductResponse findById(String id) {
        return productQueryTimer.recordCallable(() -> doFindById(id));
    }
}
```

### 6. 数据权限控制最佳实践
```java
/**
 * 数据权限拦截器 - 统一注解+解析+SQL重写
 */
@Component
@Slf4j
public class DataPermissionInterceptor implements InnerInterceptor {
    
    private final DataPermissionAnnotationParser annotationParser;
    private final SqlRewriter sqlRewriter;
    
    public DataPermissionInterceptor(DataPermissionAnnotationParser annotationParser,
                                   SqlRewriter sqlRewriter) {
        this.annotationParser = annotationParser;
        this.sqlRewriter = sqlRewriter;
    }
    
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                          RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        
        try {
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            // 解析方法上的数据权限注解
            DataPermissionConfig config = annotationParser.parseAnnotation(ms.getId());
            if (config == null || !config.isEnabled()) {
                return;
            }
            
            // 检查用户权限
            if (hasAdminRole(userDetails) && config.isSkipForAdmin()) {
                return; // 管理员跳过数据权限控制
            }
            
            // 重写SQL添加数据权限条件
            String originalSql = boundSql.getSql();
            String permissionSql = sqlRewriter.rewriteWithPermission(
                originalSql, config, userDetails);
            
            if (!originalSql.equals(permissionSql)) {
                // 使用反射替换SQL
                Field sqlField = BoundSql.class.getDeclaredField("sql");
                sqlField.setAccessible(true);
                sqlField.set(boundSql, permissionSql);
                
                log.debug("Applied data permission filter for user: {}, method: {}", 
                    userDetails.getUserId(), ms.getId());
            }
            
        } catch (Exception e) {
            log.error("Failed to apply data permission filter", e);
            // 数据权限失败时，为了安全考虑，应该抛出异常
            throw new DataPermissionException("数据权限检查失败", e);
        }
    }
    
    private boolean hasAdminRole(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}

/**
 * 数据权限注解解析器
 */
@Component
public class DataPermissionAnnotationParser {
    
    private final Map<String, DataPermissionConfig> configCache = new ConcurrentHashMap<>();
    
    public DataPermissionConfig parseAnnotation(String mapperId) {
        return configCache.computeIfAbsent(mapperId, this::doParseAnnotation);
    }
    
    private DataPermissionConfig doParseAnnotation(String mapperId) {
        try {
            // 解析Mapper方法上的@DataPermission注解
            Class<?> mapperClass = getMapperClass(mapperId);
            String methodName = getMethodName(mapperId);
            
            Method method = findMethod(mapperClass, methodName);
            if (method == null) {
                return null;
            }
            
            DataPermission annotation = method.getAnnotation(DataPermission.class);
            if (annotation == null) {
                return null;
            }
            
            return DataPermissionConfig.builder()
                .enabled(true)
                .type(annotation.type())
                .field(annotation.field())
                .valueSource(annotation.valueSource())
                .skipForAdmin(true)
                .build();
                
        } catch (Exception e) {
            log.warn("Failed to parse data permission annotation for: {}", mapperId, e);
            return null;
        }
    }
}

/**
 * SQL重写器
 */
@Component
public class SqlRewriter {
    
    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    
    public String rewriteWithPermission(String originalSql, 
                                      DataPermissionConfig config, 
                                      CustomUserDetails userDetails) {
        
        // 解析权限值
        String permissionValue = resolvePermissionValue(config.getValueSource(), userDetails);
        
        // 构建权限条件
        String permissionCondition = buildPermissionCondition(
            config.getField(), permissionValue, config.getType());
        
        // 重写SQL
        return appendWhereCondition(originalSql, permissionCondition);
    }
    
    private String resolvePermissionValue(String valueSource, CustomUserDetails userDetails) {
        if (valueSource.startsWith("#{")) {
            // SpEL表达式解析
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("currentUser", userDetails);
            
            Expression expression = spelParser.parseExpression(
                valueSource.substring(2, valueSource.length() - 1));
            return expression.getValue(context, String.class);
        } else {
            // 直接返回字面值
            return valueSource;
        }
    }
    
    private String buildPermissionCondition(String field, String value, DataPermissionType type) {
        switch (type) {
            case USER:
                return field + " = '" + value + "'";
            case DEPARTMENT:
                return field + " IN (SELECT user_id FROM user_departments WHERE dept_id = '" + value + "')";
            case ROLE:
                return field + " IN (SELECT user_id FROM user_roles WHERE role_id = '" + value + "')";
            default:
                return field + " = '" + value + "'";
        }
    }
    
    private String appendWhereCondition(String sql, String condition) {
        // 简单的SQL重写逻辑，实际项目中建议使用JSqlParser
        String lowerSql = sql.toLowerCase();
        
        if (lowerSql.contains(" where ")) {
            return sql + " AND (" + condition + ")";
        } else {
            return sql + " WHERE " + condition;
        }
    }
}
}
```

## 开发工具配置

### IDEA配置文件
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="java" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="javax" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="org" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="com" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="" withSubpackages="true" static="true"/>
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

### EditorConfig配置
```ini
# .editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{yml,yaml}]
indent_style = space
indent_size = 2

[*.xml]
indent_style = space
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

## 核心业务模块实现规范

### LLM服务策略模式实现

#### 1. Prompt模板版本管理
```java
/**
 * Prompt模板领域实体 - 支持版本管理和审计
 */
@Data
@Builder
@TableName("prompt_templates")
public class PromptTemplate {
    
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    
    @TableField("template_name")
    private String name;
    
    @TableField("template_content")
    private String content;
    
    @TableField("version")
    private String version;
    
    @TableField("status")
    private PromptStatus status;
    
    @TableField("variables")
    private String variables;  // JSON格式存储变量定义
    
    @TableField("description")
    private String description;
    
    @TableField("category")
    private String category;
    
    @TableField("created_by")
    private String createdBy;
    
    @TableField("approved_by")
    private String approvedBy;
    
    @TableField("approved_at")
    private LocalDateTime approvedAt;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 渲染Prompt模板
     */
    public String render(Map<String, Object> variables) {
        TemplateEngine engine = new TemplateEngine();
        return engine.process(this.content, variables);
    }
    
    /**
     * 验证模板变量
     */
    public ValidationResult validateVariables(Map<String, Object> variables) {
        Set<String> requiredVars = extractRequiredVariables();
        Set<String> providedVars = variables.keySet();
        
        Set<String> missingVars = Sets.difference(requiredVars, providedVars);
        if (!missingVars.isEmpty()) {
            return ValidationResult.failure("缺少必需变量: " + missingVars);
        }
        
        return ValidationResult.success();
    }
}

/**
 * Prompt模板服务 - 版本管理和安全控制
 */
@Service
@Slf4j
public class PromptTemplateService {
    
    private final PromptTemplateMapper promptTemplateMapper;
    private final PromptVersionMapper promptVersionMapper;
    private final SecurityFilterService securityFilterService;
    
    /**
     * 创建新版本Prompt模板
     */
    @Transactional
    public PromptTemplate createVersion(CreatePromptTemplateCommand command) {
        // 安全检查
        securityFilterService.validatePromptContent(command.getContent());
        
        // 获取当前最新版本
        String latestVersion = getLatestVersion(command.getName());
        String newVersion = VersionUtil.increment(latestVersion);
        
        PromptTemplate template = PromptTemplate.builder()
            .name(command.getName())
            .content(command.getContent())
            .version(newVersion)
            .status(PromptStatus.DRAFT)
            .variables(JsonUtil.toJson(command.getVariables()))
            .description(command.getDescription())
            .category(command.getCategory())
            .createdBy(getCurrentUserId())
            .createdAt(LocalDateTime.now())
            .build();
        
        promptTemplateMapper.insert(template);
        
        // 记录版本历史
        recordVersionHistory(template);
        
        log.info("Created prompt template version: {} v{}", 
            template.getName(), template.getVersion());
        
        return template;
    }
    
    /**
     * 审批Prompt模板
     */
    @Transactional
    public void approveTemplate(String templateId, String approver) {
        PromptTemplate template = promptTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BusinessException("Prompt模板不存在");
        }
        
        if (template.getStatus() != PromptStatus.DRAFT) {
            throw new BusinessException("只能审批草稿状态的模板");
        }
        
        // 安全二次检查
        securityFilterService.validatePromptContent(template.getContent());
        
        template.setStatus(PromptStatus.APPROVED);
        template.setApprovedBy(approver);
        template.setApprovedAt(LocalDateTime.now());
        
        promptTemplateMapper.updateById(template);
        
        log.info("Approved prompt template: {} v{} by {}", 
            template.getName(), template.getVersion(), approver);
    }
    
    /**
     * 获取生产环境可用的模板
     */
    public PromptTemplate getProductionTemplate(String name) {
        return promptTemplateMapper.selectOne(
            Wrappers.<PromptTemplate>lambdaQuery()
                .eq(PromptTemplate::getName, name)
                .eq(PromptTemplate::getStatus, PromptStatus.APPROVED)
                .orderByDesc(PromptTemplate::getVersion)
                .last("LIMIT 1")
        );
    }
}
```

#### 2. 成本与配额监控
```java
/**
 * LLM成本追踪器 - 实时监控和预算控制
 */
@Component
@Slf4j
public class LlmCostTracker {
    
    private final LlmUsageRepository usageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LlmConfigProperties llmConfig;
    
    /**
     * 记录LLM调用成本
     */
    public void recordUsage(LlmCallContext context, LlmCallResult result) {
        TokenUsage usage = result.getUsage();
        CostCalculation cost = calculateCost(context.getProviderType(), usage);
        
        // 记录详细使用情况
        LlmUsageRecord record = LlmUsageRecord.builder()
            .requestId(context.getRequestId())
            .userId(context.getUserId())
            .providerType(context.getProviderType())
            .model(context.getModel())
            .inputTokens(usage.getInputTokens())
            .outputTokens(usage.getOutputTokens())
            .totalTokens(usage.getTotalTokens())
            .inputCost(cost.getInputCost())
            .outputCost(cost.getOutputCost())
            .totalCost(cost.getTotalCost())
            .duration(result.getDuration())
            .success(result.isSuccess())
            .createdAt(LocalDateTime.now())
            .build();
        
        usageRepository.save(record);
        
        // 更新实时统计
        updateRealTimeStats(record);
        
        // 检查预算限制
        checkBudgetLimits(record);
    }
    
    /**
     * 检查是否超出预算限制
     */
    public BudgetCheckResult checkBudget(String userId) {
        LocalDate today = LocalDate.now();
        
        // 检查每日预算
        BigDecimal dailyUsage = getDailyUsage(userId, today);
        BigDecimal dailyBudget = llmConfig.getCostControl().getDailyBudget();
        
        if (dailyUsage.compareTo(dailyBudget.multiply(
            BigDecimal.valueOf(llmConfig.getCostControl().getEmergencyStop()))) >= 0) {
            return BudgetCheckResult.blocked("已达到每日预算紧急停止阈值");
        }
        
        if (dailyUsage.compareTo(dailyBudget.multiply(
            BigDecimal.valueOf(llmConfig.getCostControl().getAlertThreshold()))) >= 0) {
            return BudgetCheckResult.warning("接近每日预算限制");
        }
        
        // 检查月度预算
        BigDecimal monthlyUsage = getMonthlyUsage(userId, today.withDayOfMonth(1));
        BigDecimal monthlyBudget = llmConfig.getCostControl().getMonthlyBudget();
        
        if (monthlyUsage.compareTo(monthlyBudget.multiply(
            BigDecimal.valueOf(llmConfig.getCostControl().getEmergencyStop()))) >= 0) {
            return BudgetCheckResult.blocked("已达到月度预算紧急停止阈值");
        }
        
        return BudgetCheckResult.allowed();
    }
    
    /**
     * 获取成本统计报告
     */
    public CostReport generateCostReport(String userId, LocalDate startDate, LocalDate endDate) {
        List<LlmUsageRecord> records = usageRepository.findByUserIdAndDateRange(
            userId, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        
        Map<LlmProviderType, BigDecimal> costByProvider = records.stream()
            .collect(Collectors.groupingBy(
                LlmUsageRecord::getProviderType,
                Collectors.reducing(BigDecimal.ZERO, 
                    LlmUsageRecord::getTotalCost, BigDecimal::add)
            ));
        
        Map<String, BigDecimal> costByModel = records.stream()
            .collect(Collectors.groupingBy(
                LlmUsageRecord::getModel,
                Collectors.reducing(BigDecimal.ZERO, 
                    LlmUsageRecord::getTotalCost, BigDecimal::add)
            ));
        
        return CostReport.builder()
            .userId(userId)
            .startDate(startDate)
            .endDate(endDate)
            .totalCost(records.stream()
                .map(LlmUsageRecord::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .totalTokens(records.stream()
                .mapToLong(LlmUsageRecord::getTotalTokens)
                .sum())
            .costByProvider(costByProvider)
            .costByModel(costByModel)
            .requestCount(records.size())
            .successRate(calculateSuccessRate(records))
            .averageLatency(calculateAverageLatency(records))
            .build();
    }
}
```

#### 3. 安全过滤服务
```java
/**
 * LLM安全过滤服务 - 输入输出安全检查
 */
@Service
@Slf4j
public class LlmSecurityFilterService {
    
    private final SensitiveWordDetector sensitiveWordDetector;
    private final PiiDetector piiDetector;
    private final HallucinationDetector hallucinationDetector;
    private final ComplianceChecker complianceChecker;
    
    /**
     * 输入安全过滤
     */
    public SecurityFilterResult filterInput(String input, String userId) {
        List<SecurityViolation> violations = new ArrayList<>();
        
        // 敏感词检测
        if (sensitiveWordDetector.containsSensitiveWords(input)) {
            violations.add(SecurityViolation.builder()
                .type(ViolationType.SENSITIVE_CONTENT)
                .description("输入包含敏感词汇")
                .severity(Severity.HIGH)
                .build());
        }
        
        // PII检测
        List<PiiEntity> piiEntities = piiDetector.detectPii(input);
        if (!piiEntities.isEmpty()) {
            violations.add(SecurityViolation.builder()
                .type(ViolationType.PII_DETECTED)
                .description("输入包含个人敏感信息: " + 
                    piiEntities.stream()
                        .map(PiiEntity::getType)
                        .collect(Collectors.joining(", ")))
                .severity(Severity.MEDIUM)
                .build());
        }
        
        // 合规检查
        ComplianceResult complianceResult = complianceChecker.check(input, userId);
        if (!complianceResult.isCompliant()) {
            violations.add(SecurityViolation.builder()
                .type(ViolationType.COMPLIANCE_VIOLATION)
                .description("输入违反合规要求: " + complianceResult.getReason())
                .severity(Severity.HIGH)
                .build());
        }
        
        // 记录安全事件
        if (!violations.isEmpty()) {
            recordSecurityEvent(userId, input, violations, "INPUT_FILTER");
        }
        
        return SecurityFilterResult.builder()
            .allowed(violations.stream().noneMatch(v -> v.getSeverity() == Severity.HIGH))
            .violations(violations)
            .filteredContent(maskSensitiveContent(input, piiEntities))
            .build();
    }
    
    /**
     * 输出安全过滤
     */
    public SecurityFilterResult filterOutput(String output, String originalInput, String userId) {
        List<SecurityViolation> violations = new ArrayList<>();
        
        // 幻觉检测
        if (hallucinationDetector.isHallucination(output, originalInput)) {
            violations.add(SecurityViolation.builder()
                .type(ViolationType.HALLUCINATION)
                .description("输出可能包含幻觉内容")
                .severity(Severity.MEDIUM)
                .build());
        }
        
        // 输出相似度检查
        double similarity = calculateSimilarityWithTrainingData(output);
        if (similarity > 0.9) {
            violations.add(SecurityViolation.builder()
                .type(ViolationType.HIGH_SIMILARITY)
                .description("输出与训练数据相似度过高: " + similarity)
                .severity(Severity.HIGH)
                .build());
        }
        
        // 敏感信息泄露检测
        if (containsLeakedInformation(output)) {
            violations.add(SecurityViolation.builder()
                .type(ViolationType.INFORMATION_LEAK)
                .description("输出可能包含敏感信息泄露")
                .severity(Severity.HIGH)
                .build());
        }
        
        // 记录安全事件
        if (!violations.isEmpty()) {
            recordSecurityEvent(userId, output, violations, "OUTPUT_FILTER");
        }
        
        return SecurityFilterResult.builder()
            .allowed(violations.stream().noneMatch(v -> v.getSeverity() == Severity.HIGH))
            .violations(violations)
            .filteredContent(sanitizeOutput(output))
            .build();
    }
    
    private void recordSecurityEvent(String userId, String content, 
                                   List<SecurityViolation> violations, String filterType) {
        SecurityEvent event = SecurityEvent.builder()
            .userId(userId)
            .filterType(filterType)
            .contentHash(DigestUtils.sha256Hex(content))
            .violations(violations)
            .timestamp(LocalDateTime.now())
            .build();
        
        // 异步记录安全事件
        CompletableFuture.runAsync(() -> {
            try {
                securityEventRepository.save(event);
                
                // 高风险事件立即告警
                if (violations.stream().anyMatch(v -> v.getSeverity() == Severity.HIGH)) {
                    alertService.sendSecurityAlert(event);
                }
            } catch (Exception e) {
                log.error("Failed to record security event", e);
            }
        });
    }
}
```

#### 4. Embedding维度校验
```java
/**
 * Embedding服务 - 维度校验和向量管理
 */
@Service
@Slf4j
public class EmbeddingService {
    
    private final Map<String, EmbeddingProvider> providers;
    private final VectorDimensionValidator dimensionValidator;
    private final EmbeddingCache embeddingCache;
    
    /**
     * 生成文本Embedding
     */
    public EmbeddingResult generateEmbedding(String text, String model) {
        // 预处理文本
        String processedText = preprocessText(text);
        
        // 检查缓存
        String cacheKey = generateCacheKey(processedText, model);
        EmbeddingResult cached = embeddingCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 获取提供商
        EmbeddingProvider provider = getProviderForModel(model);
        
        // 生成向量
        float[] vector = provider.embed(processedText);
        
        // 维度校验
        ValidationResult validation = dimensionValidator.validate(vector, model);
        if (!validation.isValid()) {
            throw new EmbeddingException("向量维度校验失败: " + validation.getMessage());
        }
        
        EmbeddingResult result = EmbeddingResult.builder()
            .text(processedText)
            .model(model)
            .vector(vector)
            .dimension(vector.length)
            .timestamp(LocalDateTime.now())
            .build();
        
        // 缓存结果
        embeddingCache.put(cacheKey, result);
        
        return result;
    }
    
    /**
     * 批量生成Embedding
     */
    public List<EmbeddingResult> batchGenerateEmbedding(List<String> texts, String model) {
        // 分批处理
        int batchSize = llmConfig.getEmbedding().getBatchSize();
        List<List<String>> batches = Lists.partition(texts, batchSize);
        
        return batches.parallelStream()
            .flatMap(batch -> batch.stream()
                .map(text -> generateEmbedding(text, model)))
            .collect(Collectors.toList());
    }
    
    /**
     * 向量相似度计算
     */
    public double calculateSimilarity(float[] vector1, float[] vector2) {
        // 维度检查
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配: " + 
                vector1.length + " vs " + vector2.length);
        }
        
        // 余弦相似度计算
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}

/**
 * 向量维度验证器
 */
@Component
public class VectorDimensionValidator {
    
    private final LlmConfigProperties llmConfig;
    
    public ValidationResult validate(float[] vector, String model) {
        int dimension = vector.length;
        
        // 检查是否为支持的维度
        List<Integer> supportedDimensions = llmConfig.getEmbedding().getSupportedDimensions();
        if (!supportedDimensions.contains(dimension)) {
            return ValidationResult.failure(
                "不支持的向量维度: " + dimension + 
                ", 支持的维度: " + supportedDimensions);
        }
        
        // 检查向量是否为零向量
        boolean isZeroVector = Arrays.stream(vector).allMatch(v -> v == 0.0f);
        if (isZeroVector) {
            return ValidationResult.failure("检测到零向量，可能存在问题");
        }
        
        // 检查向量范数
        double norm = Math.sqrt(Arrays.stream(vector)
            .mapToDouble(v -> v * v)
            .sum());
        
        if (norm < 0.1 || norm > 10.0) {
            return ValidationResult.warning(
                "向量范数异常: " + norm + ", 建议检查输入文本");
        }
        
        return ValidationResult.success();
    }
}
```

#### 5. LLM提供商策略接口
```java
/**
 * LLM提供商策略接口
 */
public interface LlmProviderStrategy {
    
    /**
     * 支持的提供商类型
     */
    LlmProviderType getProviderType();
    
    /**
     * 调用LLM服务
     */
    LlmCallResult call(LlmCallRequest request);
    
    /**
     * 批量调用
     */
    List<LlmCallResult> batchCall(List<LlmCallRequest> requests);
    
    /**
     * 流式调用
     */
    Flux<LlmStreamResult> streamCall(LlmCallRequest request);
    
    /**
     * 健康检查
     */
    boolean isHealthy();
    
    /**
     * 获取成本信息
     */
    CostInfo getCostInfo(TokenUsage usage);
}
```

#### 2. 具体策略实现
```java
/**
 * OpenAI策略实现
 */
@Component
public class OpenAiProviderStrategy implements LlmProviderStrategy {
    
    private final OpenAiClient openAiClient;
    private final CircuitBreaker circuitBreaker;
    private final RetryTemplate retryTemplate;
    private final RateLimiter rateLimiter;
    
    @Override
    public LlmProviderType getProviderType() {
        return LlmProviderType.OPENAI;
    }
    
    @Override
    public LlmCallResult call(LlmCallRequest request) {
        return circuitBreaker.executeSupplier(() -> {
            rateLimiter.acquire();
            return retryTemplate.execute(context -> {
                OpenAiRequest openAiRequest = convertRequest(request);
                OpenAiResponse response = openAiClient.chat(openAiRequest);
                return convertResponse(response);
            });
        });
    }
    
    @Override
    public boolean isHealthy() {
        try {
            openAiClient.healthCheck();
            return true;
        } catch (Exception e) {
            log.warn("OpenAI health check failed", e);
            return false;
        }
    }
    
    @Override
    public CostInfo getCostInfo(TokenUsage usage) {
        // OpenAI定价计算
        BigDecimal inputCost = usage.getInputTokens()
            .multiply(OpenAiPricing.INPUT_TOKEN_PRICE);
        BigDecimal outputCost = usage.getOutputTokens()
            .multiply(OpenAiPricing.OUTPUT_TOKEN_PRICE);
        
        return CostInfo.builder()
            .inputCost(inputCost)
            .outputCost(outputCost)
            .totalCost(inputCost.add(outputCost))
            .currency("USD")
            .build();
    }
}
```

#### 3. LLM服务编排器
```java
/**
 * LLM服务编排器 - 实现智能路由和故障转移
 */
@Service
@Slf4j
public class LlmOrchestrationService {
    
    private final Map<LlmProviderType, LlmProviderStrategy> strategies;
    private final LlmProviderSelector providerSelector;
    private final LlmCallLogRepository callLogRepository;
    private final CostTracker costTracker;
    
    public LlmOrchestrationService(List<LlmProviderStrategy> strategies,
                                  LlmProviderSelector providerSelector,
                                  LlmCallLogRepository callLogRepository,
                                  CostTracker costTracker) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(
                LlmProviderStrategy::getProviderType,
                Function.identity()
            ));
        this.providerSelector = providerSelector;
        this.callLogRepository = callLogRepository;
        this.costTracker = costTracker;
    }
    
    /**
     * 智能调用 - 自动选择最优提供商，集成完整风险控制
     */
    public LlmCallResult smartCall(LlmCallCommand command) {
        String userId = getCurrentUserId();
        LlmCallContext context = LlmCallContext.from(command);
        
        // 1. 预算检查
        BudgetCheckResult budgetCheck = costTracker.checkBudget(userId);
        if (budgetCheck.isBlocked()) {
            throw new BudgetExceededException(budgetCheck.getMessage());
        }
        if (budgetCheck.isWarning()) {
            log.warn("Budget warning for user {}: {}", userId, budgetCheck.getMessage());
        }
        
        // 2. 输入安全过滤
        SecurityFilterResult inputFilter = securityFilterService.filterInput(
            command.getPrompt(), userId);
        if (!inputFilter.isAllowed()) {
            throw new SecurityViolationException("输入安全检查失败: " + 
                inputFilter.getViolations().stream()
                    .map(SecurityViolation::getDescription)
                    .collect(Collectors.joining(", ")));
        }
        
        // 3. Prompt模板渲染和验证
        PromptTemplate template = promptTemplateService.getProductionTemplate(
            command.getTemplateName());
        if (template == null) {
            throw new TemplateNotFoundException("未找到生产环境可用的模板: " + 
                command.getTemplateName());
        }
        
        ValidationResult templateValidation = template.validateVariables(command.getVariables());
        if (!templateValidation.isValid()) {
            throw new TemplateValidationException(templateValidation.getMessage());
        }
        
        String renderedPrompt = template.render(command.getVariables());
        
        // 4. 选择最优提供商（基于成本、延迟、成功率）
        List<LlmProviderType> candidates = providerSelector.selectProviders(context);
        
        LlmCallResult result = null;
        Exception lastException = null;
        
        // 5. 故障转移逻辑
        for (LlmProviderType providerType : candidates) {
            LlmProviderStrategy strategy = strategies.get(providerType);
            
            // 健康检查
            if (!strategy.isHealthy()) {
                log.warn("Provider {} is unhealthy, skipping", providerType);
                continue;
            }
            
            // 速率限制检查
            if (!rateLimitService.isAllowed(userId, providerType)) {
                log.warn("Rate limit exceeded for user {} on provider {}", userId, providerType);
                continue;
            }
            
            try {
                long startTime = System.currentTimeMillis();
                
                // 构建请求
                LlmCallRequest request = LlmCallRequest.builder()
                    .prompt(renderedPrompt)
                    .model(selectBestModel(providerType, context))
                    .maxTokens(command.getMaxTokens())
                    .temperature(command.getTemperature())
                    .userId(userId)
                    .requestId(context.getRequestId())
                    .build();
                
                result = strategy.call(request);
                
                long duration = System.currentTimeMillis() - startTime;
                
                // 6. 输出安全过滤
                SecurityFilterResult outputFilter = securityFilterService.filterOutput(
                    result.getContent(), renderedPrompt, userId);
                if (!outputFilter.isAllowed()) {
                    log.warn("Output security check failed for user {}: {}", userId, 
                        outputFilter.getViolations());
                    
                    // 如果输出不安全，尝试下一个提供商
                    recordFailedCall(context, providerType, 
                        new SecurityViolationException("输出安全检查失败"));
                    continue;
                }
                
                // 7. 应用输出过滤结果
                if (!outputFilter.getFilteredContent().equals(result.getContent())) {
                    result = result.toBuilder()
                        .content(outputFilter.getFilteredContent())
                        .filtered(true)
                        .filterReasons(outputFilter.getViolations().stream()
                            .map(SecurityViolation::getDescription)
                            .collect(Collectors.toList()))
                        .build();
                }
                
                // 8. 记录成功调用
                recordSuccessfulCall(context, providerType, result, duration);
                
                // 9. 成本追踪
                costTracker.recordUsage(context, result);
                
                // 10. 更新提供商性能指标
                updateProviderMetrics(providerType, duration, true);
                
                break;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("Provider {} call failed: {}", providerType, e.getMessage());
                
                // 记录失败调用
                recordFailedCall(context, providerType, e);
            }
        }
        
        if (result == null) {
            // 记录全部失败事件
            recordAllProvidersFailedEvent(context, candidates, lastException);
            throw new LlmServiceException("所有LLM提供商调用失败", lastException);
        }
        
        return result;
    }
    
    /**
     * 智能模型选择 - 基于成本和性能
     */
    private String selectBestModel(LlmProviderType providerType, LlmCallContext context) {
        List<String> availableModels = getAvailableModels(providerType);
        
        // 根据任务复杂度选择模型
        TaskComplexity complexity = analyzeTaskComplexity(context);
        
        switch (complexity) {
            case SIMPLE:
                return availableModels.stream()
                    .filter(model -> model.contains("turbo") || model.contains("plus"))
                    .findFirst()
                    .orElse(availableModels.get(0));
                    
            case MEDIUM:
                return availableModels.stream()
                    .filter(model -> !model.contains("turbo") && !model.contains("max"))
                    .findFirst()
                    .orElse(availableModels.get(0));
                    
            case COMPLEX:
                return availableModels.stream()
                    .filter(model -> model.contains("max") || model.contains("4"))
                    .findFirst()
                    .orElse(availableModels.get(availableModels.size() - 1));
                    
            default:
                return availableModels.get(0);
        }
    }
    
    /**
     * 任务复杂度分析
     */
    private TaskComplexity analyzeTaskComplexity(LlmCallContext context) {
        String prompt = context.getRequest().getPrompt();
        
        // 基于prompt长度和内容复杂度判断
        if (prompt.length() < 500) {
            return TaskComplexity.SIMPLE;
        } else if (prompt.length() < 2000) {
            return TaskComplexity.MEDIUM;
        } else {
            return TaskComplexity.COMPLEX;
        }
    }
    
    /**
     * 更新提供商性能指标
     */
    private void updateProviderMetrics(LlmProviderType providerType, long duration, boolean success) {
        String metricsKey = "llm:metrics:" + providerType.name();
        
        // 更新成功率
        String successRateKey = metricsKey + ":success_rate";
        redisTemplate.opsForHash().increment(successRateKey, "total", 1);
        if (success) {
            redisTemplate.opsForHash().increment(successRateKey, "success", 1);
        }
        
        // 更新平均延迟
        String latencyKey = metricsKey + ":latency";
        redisTemplate.opsForList().leftPush(latencyKey, duration);
        redisTemplate.expire(latencyKey, Duration.ofHours(24));
        
        // 保持最近100次调用的延迟记录
        redisTemplate.opsForList().trim(latencyKey, 0, 99);
    }
    
    /**
     * 记录全部提供商失败事件
     */
    private void recordAllProvidersFailedEvent(LlmCallContext context, 
                                             List<LlmProviderType> candidates, 
                                             Exception lastException) {
        LlmFailureEvent event = LlmFailureEvent.builder()
            .requestId(context.getRequestId())
            .userId(context.getUserId())
            .attemptedProviders(candidates)
            .lastException(lastException.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        // 异步记录失败事件
        CompletableFuture.runAsync(() -> {
            try {
                failureEventRepository.save(event);
                
                // 发送告警
                if (candidates.size() >= 3) {  // 如果尝试了3个以上提供商都失败
                    alertService.sendLlmFailureAlert(event);
                }
            } catch (Exception e) {
                log.error("Failed to record LLM failure event", e);
            }
        });
    }
    
    /**
     * 批量智能调用 - 负载均衡
     */
    public List<LlmCallResult> smartBatchCall(List<LlmCallCommand> commands) {
        // 按优先级分组
        Map<LlmProviderType, List<LlmCallCommand>> groupedCommands = 
            commands.stream().collect(Collectors.groupingBy(
                command -> providerSelector.selectBestProvider(
                    LlmCallContext.from(command)
                )
            ));
        
        // 并行调用
        List<CompletableFuture<List<LlmCallResult>>> futures = 
            groupedCommands.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> {
                    LlmProviderStrategy strategy = strategies.get(entry.getKey());
                    return entry.getValue().stream()
                        .map(cmd -> strategy.call(LlmCallContext.from(cmd).getRequest()))
                        .collect(Collectors.toList());
                }))
                .collect(Collectors.toList());
        
        // 等待所有调用完成
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
    
    private void recordSuccessfulCall(LlmCallContext context, 
                                    LlmProviderType providerType,
                                    LlmCallResult result, 
                                    long duration) {
        LlmCallLog log = LlmCallLog.builder()
            .requestId(context.getRequestId())
            .providerType(providerType)
            .model(context.getRequest().getModel())
            .inputTokens(result.getUsage().getInputTokens())
            .outputTokens(result.getUsage().getOutputTokens())
            .duration(duration)
            .cost(result.getCost())
            .status(CallStatus.SUCCESS)
            .createdAt(LocalDateTime.now())
            .build();
            
        callLogRepository.save(log);
    }
}
```

### 规则引擎DSL实现

#### 1. 规则DSL语法定义
```java
/**
 * 规则DSL解析器
 */
@Component
public class RuleDslParser {
    
    private final AntlrParser antlrParser;
    
    /**
     * 解析规则DSL
     * 
     * 示例DSL:
     * RULE "产品名称检查"
     * WHEN 
     *   product.name IS_EMPTY OR 
     *   product.name.length > 100 OR
     *   product.name CONTAINS "测试"
     * THEN
     *   REJECT WITH "产品名称不符合规范"
     *   SET priority = HIGH
     * END
     */
    public ParsedRule parse(String dslContent) {
        try {
            RuleLexer lexer = new RuleLexer(CharStreams.fromString(dslContent));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            RuleParser parser = new RuleParser(tokens);
            
            RuleParser.RuleContext ruleContext = parser.rule();
            
            return new RuleDslVisitor().visit(ruleContext);
            
        } catch (Exception e) {
            throw new RuleDslParseException("规则DSL解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证DSL语法
     */
    public ValidationResult validate(String dslContent) {
        try {
            ParsedRule rule = parse(dslContent);
            return RuleDslValidator.validate(rule);
        } catch (RuleDslParseException e) {
            return ValidationResult.failure(e.getMessage());
        }
    }
}
```

#### 2. 规则执行引擎
```java
/**
 * 规则执行引擎实现
 */
@Component
public class DroolsRuleEngine implements RuleEngine {
    
    private final KieContainer kieContainer;
    private final RuleCompiler ruleCompiler;
    
    @Override
    public RuleExecutionResult execute(RuleSet ruleSet, ExecutionContext context) {
        KieSession kieSession = kieContainer.newKieSession();
        
        try {
            // 设置执行上下文
            kieSession.setGlobal("context", context);
            kieSession.setGlobal("result", new RuleExecutionResult());
            
            // 插入事实对象
            context.getFacts().forEach(kieSession::insert);
            
            // 执行规则
            int firedRules = kieSession.fireAllRules();
            
            // 获取执行结果
            RuleExecutionResult result = (RuleExecutionResult) kieSession.getGlobal("result");
            result.setFiredRulesCount(firedRules);
            
            return result;
            
        } finally {
            kieSession.dispose();
        }
    }
    
    @Override
    public CompletableFuture<RuleExecutionResult> executeAsync(RuleSet ruleSet, 
                                                              ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> execute(ruleSet, context));
    }
}
```

## 总结

本开发规范涵盖了后端开发的各个方面，包括：

1. **技术栈选择**：Spring Boot 3.x + MyBatis Plus + MySQL + Redis等
2. **项目结构**：按业务功能模块化组织
3. **编码规范**：详细的命名规范、注解使用规范
4. **数据访问**：MyBatis Plus最佳实践
5. **安全框架**：Spring Security + JWT完整配置
6. **代码质量**：Maven插件配置、质量标准
7. **版本控制**：Git工作流、提交规范
8. **部署规范**：Docker容器化部署
9. **最佳实践**：缓存、异常处理、日志、监控等

## LLM专项风险缓解措施

### 已解决的LLM关键风险

#### 1. ✅ **Prompt缺少版本管理** (中等→已解决)
- **问题**: PromptTemplate未持久化/不可回溯，结果不稳定/难复盘
- **解决方案**:
  - 实现完整的Prompt版本管理系统
  - 支持模板审批流程和版本回溯
  - 提供模板变量验证和渲染功能
  - 建立模板分类和权限管理
- **技术实现**:
  ```java
  // Prompt模板版本管理
  @TableName("prompt_templates")
  public class PromptTemplate {
      private String version;           // 版本号
      private PromptStatus status;      // 审批状态
      private String approvedBy;        // 审批人
      private LocalDateTime approvedAt; // 审批时间
  }
  ```

#### 2. ✅ **成本与配额不可见** (高→已解决)
- **问题**: 未记录token消耗、延迟、失败率，成本失控
- **解决方案**:
  - 实时成本追踪和预算控制系统
  - 多维度使用统计和报告
  - 预算告警和紧急停止机制
  - 详细的调用日志和性能监控
- **技术实现**:
  ```yaml
  # 成本控制配置
  cost-control:
    daily-budget: 1000.00           # 每日预算
    monthly-budget: 30000.00        # 月度预算
    alert-threshold: 0.8            # 告警阈值
    emergency-stop: 0.95            # 紧急停止
  ```

#### 3. ✅ **模型切换策略缺失** (中等→已解决)
- **问题**: 没有优先级/回退策略，单点性能抖动
- **解决方案**:
  - 智能模型选择算法
  - 多层级故障转移策略
  - 基于任务复杂度的模型匹配
  - 实时健康检查和性能监控
- **技术实现**:
  ```yaml
  # 模型切换策略
  fallback-strategy:
    primary-models: ["gpt-4", "qianwen-max"]
    fallback-models: ["gpt-3.5-turbo", "qianwen-plus"]
    emergency-models: ["qianwen-turbo"]
  ```

#### 4. ✅ **Embedding与维度校验缺失** (中等→已解决)
- **问题**: 不同向量模型混用风险，检索结果异常
- **解决方案**:
  - 严格的向量维度校验机制
  - 支持多种Embedding模型管理
  - 向量质量检测和异常告警
  - 相似度计算优化和缓存
- **技术实现**:
  ```java
  // 向量维度校验
  public ValidationResult validate(float[] vector, String model) {
      List<Integer> supportedDimensions = getSupportedDimensions();
      if (!supportedDimensions.contains(vector.length)) {
          return ValidationResult.failure("不支持的向量维度");
      }
      return ValidationResult.success();
  }
  ```

#### 5. ✅ **安全过滤缺失** (高→已解决)
- **问题**: 未引入敏感输出过滤、越权问题防护，合规风险
- **解决方案**:
  - 双重安全过滤机制（输入+输出）
  - 敏感词检测和PII识别
  - 幻觉检测和相似度校验
  - 合规检查和安全事件记录
- **技术实现**:
  ```java
  // 安全过滤服务
  public SecurityFilterResult filterInput(String input, String userId) {
      // 敏感词检测、PII检测、合规检查
      return SecurityFilterResult.builder()
          .allowed(violations.isEmpty())
          .violations(violations)
          .build();
  }
  ```

### LLM风险评级对比

| 风险项 | 修改前 | 修改后 | 缓解措施 |
|--------|--------|--------|----------|
| Prompt版本管理 | 🟡 中等 | 🟢 已解决 | 版本管理+审批流程 |
| 成本配额控制 | 🔴 高 | 🟢 已解决 | 实时监控+预算控制 |
| 模型切换策略 | 🟡 中等 | 🟢 已解决 | 智能选择+故障转移 |
| Embedding校验 | 🟡 中等 | 🟢 已解决 | 维度校验+质量检测 |
| 安全过滤 | 🔴 高 | 🟢 已解决 | 双重过滤+合规检查 |

## 架构风险缓解措施

### 已解决的关键风险

#### 1. ✅ JPA注解与MyBatis Plus混用风险 (已解决)
- **问题**: BaseEntity使用@MappedSuperclass (JPA语义)
- **解决**: 移除@MappedSuperclass注解，统一使用MyBatis Plus注解体系
- **影响**: 避免团队混淆，防止后续引入Spring Data JPA冲突

#### 2. ✅ 领域层未显式风险 (已解决)  
- **问题**: 缺乏Application/Domain/Infrastructure分层
- **解决**: 采用DDD六边形架构，明确分层职责
- **影响**: 业务逻辑集中在领域层，支持复杂策略扩展

#### 3. ✅ DTO/Entity混用风险 (已解决)
- **问题**: 直接返回Entity或Entity构建Response
- **解决**: 严格DTO转换规范，防腐层设计
- **影响**: 防止数据泄露，降低耦合度

#### 4. ✅ LLM模块抽象不足风险 (已解决)
- **问题**: LlmClient未体现Provider策略
- **解决**: 策略模式+编排器，支持重试、熔断、成本控制
- **影响**: 成本可控，高可用性，易扩展

#### 5. ✅ 规则引擎简化风险 (已解决)
- **问题**: RuleEngine缺乏DSL/策略模式
- **解决**: DSL解析器+多引擎策略+执行编排
- **影响**: 支持规则爆炸式增长，可视化配置

#### 6. ✅ 文件/向量处理耦合风险 (已解决)
- **问题**: DocumentParser与AuditEngine耦合
- **解决**: 适配器模式+防腐层隔离
- **影响**: 易于替换向量库/Embedding模型

### 架构质量提升

**修改前风险评级**: 🔴 高风险 (多个中高风险项)  
**修改后风险评级**: 🟢 低风险 (已缓解所有关键风险)

### 持续改进建议

1. **定期架构评审**: 每季度进行架构债务评估
2. **领域模型演进**: 根据业务发展持续优化领域模型
3. **性能监控**: 建立完善的性能指标监控体系
4. **技术债务管理**: 建立技术债务识别和偿还机制

## 🔧 最新架构修复 - 枚举序列化统一

### ✅ 枚举序列化冲突修复 (已解决)

**发现问题**: 
- `ProductStatus`枚举同时使用`@JsonFormat(shape = JsonFormat.Shape.OBJECT)`和`@JsonValue`
- 这会导致序列化冲突，Jackson无法确定使用哪种策略
- 与`ProductType`枚举的序列化策略不一致

**修复方案**:
1. **统一序列化策略**: 所有枚举统一使用`@JsonValue`输出code
2. **提供完整信息方法**: 通过`toDto()`方法支持返回`{code, description}`
3. **定义通用DTO**: 创建`EnumDto`类标准化枚举完整信息格式
4. **添加最佳实践**: 提供详细的枚举序列化指南

**修复效果**:
```java
// 修复前 - 存在冲突
@JsonFormat(shape = JsonFormat.Shape.OBJECT)  // 冲突！
public enum ProductStatus {
    @JsonValue  // 冲突！
    private final String description;
}

// 修复后 - 统一策略
public enum ProductStatus {
    @JsonValue  // 统一输出code
    private final String code;
    
    public EnumDto toDto() {  // 需要完整信息时调用
        return EnumDto.builder()
            .code(this.code)
            .description(this.description)
            .build();
    }
}
```

### 📊 枚举序列化质量对比

| 检查项 | 修复前 | 修复后 | 改进措施 |
|--------|--------|--------|----------|
| 序列化一致性 | 🔴 冲突 | 🟢 统一 | 统一使用@JsonValue输出code |
| 注解使用规范 | 🔴 混用 | 🟢 规范 | 避免@JsonFormat+@JsonValue冲突 |
| 前后端约定 | 🟡 不明确 | 🟢 清晰 | 明确枚举数据格式约定 |
| 扩展性支持 | 🟡 有限 | 🟢 完善 | 提供toDto()方法支持完整信息 |

### 🎯 最终架构质量评估

**枚举序列化**: 🔴 存在冲突 → 🟢 完全统一  
**代码一致性**: 🟡 部分一致 → 🟢 高度一致  
**最佳实践**: 🟡 缺少指南 → 🟢 完善指南  

---

遵循这些规范可以确保代码质量、提高开发效率、降低维护成本，为项目的长期发展奠定坚实基础。通过DDD架构设计和统一的枚举序列化策略，系统具备了良好的扩展性和维护性，能够应对复杂的业务场景和技术挑战。