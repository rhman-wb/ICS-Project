# Spring Boot 启动问题排查记录

## 问题描述
在debug环境下启动Spring Boot应用时，出现依赖注入失败的问题。

## 问题分析

### 根本原因
在`application-debug.yml`配置文件中设置了：
```yaml
app:
  mybatis:
    enabled: false
```

这导致以下服务类因为条件注解`@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "true", matchIfMissing = true)`而不被Spring容器创建：

1. `AuthServiceImpl` - 认证服务实现
2. `UserServiceImpl` - 用户服务实现  
3. `OperationLogServiceImpl` - 操作日志服务实现

但是对应的Controller仍然需要这些依赖，导致启动失败。

## 解决方案

### 已创建的Mock实现

1. **MockAuthServiceImpl** - 认证服务Mock实现
   - 条件：`@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")`
   - 提供基本的认证功能模拟

2. **MockUserServiceImpl** - 用户服务Mock实现
   - 条件：`@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")`
   - 提供完整的用户管理功能模拟

3. **MockOperationLogService** - 操作日志服务Mock实现（已存在）
   - 条件：`@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")`
   - 提供日志记录功能模拟

### 配置策略

debug环境配置保持：
```yaml
app:
  mybatis:
    enabled: false  # 使用Mock服务，不依赖数据库
  redis:
    enabled: false  # 禁用Redis
```

### Mock服务特点

1. **无数据库依赖** - 所有Mock服务都不需要数据库连接
2. **返回模拟数据** - 提供合理的测试数据
3. **完整日志记录** - 记录所有调用以便调试
4. **功能完整性** - 实现接口的所有方法

## 启动命令

```bash
mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=debug
```

## 验证步骤

1. 启动应用成功
2. 访问健康检查端点：`GET /api/health`
3. 测试认证接口：`POST /api/v1/auth/login`
4. 测试用户接口：`GET /api/v1/users/profile`

## 注意事项

1. Mock服务仅用于开发和测试环境
2. 生产环境必须使用真实的服务实现
3. Mock数据不会持久化
4. 所有操作都会记录日志但不会影响实际数据

## 扩展指南

如果需要添加新的服务Mock实现：

1. 创建Mock实现类
2. 添加条件注解：`@ConditionalOnProperty(name = "app.mybatis.enabled", havingValue = "false")`
3. 实现接口的所有方法
4. 提供合理的模拟数据
5. 添加适当的日志记录

## 编译错误修复

### 字段名错误修复
在MockUserServiceImpl中修复了以下字段名错误：

1. **UserResponse字段**：
   - `accountLocked(boolean)` → `locked(boolean)`
   - 移除了不存在的`enabled`字段引用

2. **Role字段**：
   - 添加了缺失的`code`字段
   - 移除了不存在的`enabled`字段

3. **Permission字段**：
   - 添加了缺失的`code`字段
   - 移除了不存在的`description`和`enabled`字段

4. **UserProfileResponse字段**：
   - 移除了不存在的`enabled`和`accountLocked`字段
   - `lastLoginAt` → `lastLoginTime`

## 问题解决状态

- ✅ AuthService依赖问题已解决
- ✅ UserService依赖问题已解决  
- ✅ OperationLogService依赖问题已解决
- ✅ 所有Mock实现已创建
- ✅ 配置文件已优化
- ✅ 编译错误已修复

## Spring Boot 3.x Jackson2JsonRedisSerializer API变更问题 ⭐

### 问题症状
```
java.lang.IllegalArgumentException: Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
    at org.springframework.beans.factory.support.FactoryBeanRegistrySupport.getTypeForFactoryBeanFromAttributes
```

### 根本原因
- Spring Boot 3.x中Jackson2JsonRedisSerializer的构造函数API发生了变化
- 旧版本的构造函数`new Jackson2JsonRedisSerializer<>(objectMapper, Object.class)`不再支持
- debug环境正常（禁用Redis），dev环境失败（启用Redis）

### 解决方案
在`RedisConfig.java`中修复Jackson2JsonRedisSerializer的用法：

```java
// ❌ 错误的用法 (Spring Boot 2.x)
Jackson2JsonRedisSerializer<Object> serializer = 
    new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

// ✅ 正确的用法 (Spring Boot 3.x)
Jackson2JsonRedisSerializer<Object> serializer = 
    new Jackson2JsonRedisSerializer<>(Object.class);
serializer.setObjectMapper(objectMapper);
```

### 排查技巧
1. 如果debug环境正常，dev环境失败，检查Redis配置
2. 查看是否有`@ConditionalOnProperty`导致的配置差异
3. 检查Spring Boot版本和相关依赖的兼容性

### 版本兼容性注意事项
Spring Boot 2.x → 3.x 升级常见问题：
1. Jackson2JsonRedisSerializer API变更
2. Spring Security配置方式变更
3. MyBatis Plus兼容性问题
4. Java版本要求提升至17+

## 下一步

应用现在应该可以在debug和dev环境下正常启动。如果遇到其他依赖问题，请按照相同的模式创建对应的Mock实现。