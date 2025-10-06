# 用户认证与权限管理系统 - 后端

保险产品智能检核系统的用户认证与权限管理模块后端实现。

## 技术栈

- 框架: Spring Boot 3.2.1
- 安全: Spring Security 6.x + JWT
- 数据访问: MyBatis Plus 3.5.5
- 数据库: MySQL 8.0+
- 缓存: Redis 7.0+
- 文档: SpringDoc OpenAPI 3 (Swagger)
- 构建工具: Maven 3.9+
- Java 版本: JDK 17+

## 项目结构

```
src/
├── main/
│  ├── java/
│  │  └── com/insurance/audit/
│  │      ├── UserAuthSystemApplication.java    # 启动类
│  │      ├── common/                           # 公共模块
│  │      │  ├── base/                          # 基础类
│  │      │  ├── config/                        # 配置类
│  │      │  ├── dto/                           # 通用 DTO
│  │      │  ├── exception/                     # 异常处理
│  │      │  └── util/                          # 工具类
│  │      └── user/                             # 用户管理领域
│  │          ├── domain/                        # 领域层
│  │          │  ├── model/                      # 实体模型
│  │          │  └── enums/                      # 枚举定义
│  │          └── infrastructure/                # 基础设施层
│  │              ├── mapper/                    # 数据访问层
│  │              └── security/                  # 安全组件
│  └── resources/
│      ├── application.yml                       # 主配置文件
│      ├── application-dev.yml                   # 开发环境配置
│      ├── application-prod.yml                  # 生产环境配置
│      └── db/migration/                         # 数据库迁移脚本
└── test/                                         # 测试代码
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+

### 2. Maven 镜像配置（推荐）

为了加速依赖下载，强烈建议配置阿里云镜像：

#### 方式一：使用项目配置文件（推荐）
```bash
# Linux/macOS
cp maven-settings.xml ~/.m2/settings.xml

# Windows
copy maven-settings.xml %USERPROFILE%\.m2\settings.xml
```

#### 方式二：IDEA 中配置
1. File → Settings → Build Tools → Maven
2. User settings file 选择项目根目录下的 `maven-settings.xml`
3. 勾选 "Override" 选项

#### 验证配置
```bash
# 清理并重新下载依赖
mvn clean dependency:resolve
```

> 详细配置说明请参考：MAVEN_MIRROR_SETUP.md

### 3. 数据库准备

```sql
-- 创建数据库（示例）
CREATE DATABASE insurance_audit_dev 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
-- 运行 src/main/resources/db/migration/V1__init_schema.sql
-- 运行 src/main/resources/db/migration/V2__init_data.sql
```

### 4. 配置文件

复制并修改配置文件：

```yaml
# application-dev.yml
spring:
  datasource:
    username: your_db_username
    password: your_db_password
  data:
    redis:
      host: your_redis_host
      password: your_redis_password
```

### 5. 启动应用

> 小贴士：从当前版本起，后端已支持从项目根目录 `.env` 自动加载本地环境变量（仅本地开发）。
>
> 数据库密码传递方式（推荐优先级）
>
> 1) 使用 `.env` 文件（推荐）
> - 复制 `.env.example` 为 `.env`，填写：
> ```
> DB_USERNAME=insurance_user
> DB_PASSWORD=20150826
> ```
> - 启动命令保持不变：
> ```
> mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev
> ```
>
> 2) 通过命令行参数传入 JVM 属性
> - Bash/zsh：
> ```
> mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-DDB_PASSWORD=20150826"
> ```
> - PowerShell：
> ```
> mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev "-Dspring-boot.run.jvmArguments=-DDB_PASSWORD=20150826"
> ```
>
> 3) 临时设置环境变量后启动
> - Bash/zsh：
> ```
> DB_PASSWORD=20150826 mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev
> ```
> - PowerShell：
> ```
> $env:DB_PASSWORD = '20150826'; mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev
> ```
>
> 注意：生产环境请勿在配置文件中写入明文密码；开发环境推荐使用 `.env` 或临时环境变量。

```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者
mvn clean package
java -jar target/user-auth-system-1.0.0.jar --spring.profiles.active=dev
```

### 6. 访问应用

- 应用地址: http://localhost:8080/api
- API 文档: http://localhost:8080/api/swagger-ui.html
- 健康检查: http://localhost:8080/api/actuator/health

## 默认账户

系统初始化后会创建默认管理员账户：

- 用户名: admin
- 密码: admin123
- 角色: 系统管理员

## API 文档

启动应用后，访问 Swagger UI 查看完整的 API 文档：
http://localhost:8080/api/swagger-ui.html

### 主要 API 端点

- POST /api/v1/auth/login - 用户登录
- POST /api/v1/auth/logout - 用户退出
- POST /api/v1/auth/refresh - 刷新令牌
- GET /api/v1/users/profile - 获取用户信息
- PUT /api/v1/users/profile - 更新用户信息

## 开发指南

### 1. 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 减少样板代码
- 统一异常处理和响应格式
- 完善的注释和文档

### 2. 安全规范

- JWT 令牌认证
- BCrypt 密码加密
- 登录失败锁定机制
- 权限控制和角色管理

### 3. 数据库规范

- 统一字段命名规范
- 逻辑删除和乐观锁
- 审计字段自动填充
- 索引优化

## 部署说明

### Docker 部署

```dockerfile
FROM openjdk:17-jre-slim
COPY target/user-auth-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 环境变量

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=insurance_audit
DB_USERNAME=root
DB_PASSWORD=password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT 配置
JWT_SECRET=your-jwt-secret-key
JWT_EXPIRATION=3600
```

## 监控和日志

- 健康检查: /actuator/health
- 应用信息: /actuator/info
- 指标监控: /actuator/metrics
- 日志级别: 可通过配置文件调整

## 故障排除

### 常见问题

1. 数据库连接失败
   - 检查数据库服务是否启动
   - 验证连接参数是否正确
   - 确认数据库用户权限

2. Redis 连接失败
   - 检查 Redis 服务是否启动
   - 验证连接参数和密码
   - 检查网络连通性

3. JWT 密钥过短
   - 错误: JWT secret key must be at least 64 characters
   - 解决: 在 application-dev.yml 中设置更长的 JWT 密钥

4. CORS 跨域问题
   - 错误: CORS policy blocked
   - 解决: 检查 CorsConfig 配置，确保允许前端域名

5. 前端代理失败
   - 错误: Proxy error
   - 解决: 
     - 确保后端服务已启动
     - 检查 vite.config.ts 中的代理配置
     - 确认 API 路径匹配

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码变更
4. 创建 Pull Request

## 许可证

MIT License