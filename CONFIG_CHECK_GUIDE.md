# 开发环境配置检查指南

## 📋 配置检查清单

### 1. 数据库配置检查

#### ✅ 数据库服务状态
```bash
# 检查MySQL服务是否运行
sudo systemctl status mysql
# 或者
brew services list | grep mysql
```

#### ✅ 数据库连接测试
```bash
# 测试数据库连接
mysql -h localhost -u insurance_user -p20150826 -e "SELECT 1;" insurance_audit
```

#### ✅ 数据库初始化检查
```sql
-- 检查数据库是否存在
SHOW DATABASES LIKE 'insurance_audit';

-- 检查表是否创建
USE insurance_audit;
SHOW TABLES;

-- 检查初始数据
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM roles;
SELECT COUNT(*) FROM permissions;
```

### 2. Redis配置检查

#### ✅ Redis服务状态
```bash
# 检查Redis服务是否运行
redis-cli ping
# 应该返回 PONG
```

#### ✅ Redis连接测试
```bash
# 测试Redis连接
redis-cli -h localhost -p 6379 ping
```

### 3. 后端配置检查

#### ✅ Java环境
```bash
# 检查Java版本（需要JDK 17+）
java -version
javac -version
```

#### ✅ Maven环境
```bash
# 检查Maven版本
mvn -version
```

#### ✅ 后端配置文件
- `backend/src/main/resources/application.yml` - 主配置
- `backend/src/main/resources/application-dev.yml` - 开发环境配置

#### ✅ 关键配置项检查
```yaml
# application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/insurance_audit?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:insurance_user}
    password: ${DB_PASSWORD:20150826}

app:
  jwt:
    secret: ${JWT_SECRET:dev-insurance-audit-system-jwt-secret-key-2024-very-long-secret-must-be-at-least-64-characters}
```

### 4. 前端配置检查

#### ✅ Node.js环境
```bash
# 检查Node.js版本（需要16+）
node --version
npm --version
```

#### ✅ 前端依赖
```bash
cd frontend
npm install
```

#### ✅ 前端配置文件
- `frontend/vite.config.ts` - Vite配置
- `frontend/.env.development` - 开发环境变量

#### ✅ 关键配置项检查
```typescript
// vite.config.ts
server: {
  host: '0.0.0.0',
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🔧 配置修复步骤

### 步骤1: 数据库配置修复

如果数据库连接失败：

1. **启动MySQL服务**
```bash
# Ubuntu/Debian
sudo systemctl start mysql

# macOS
brew services start mysql

# Windows
net start mysql
```

2. **创建数据库和用户**
```sql
-- 以root用户登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE IF NOT EXISTS insurance_audit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'insurance_user'@'localhost' IDENTIFIED BY '20150826';
GRANT ALL PRIVILEGES ON insurance_audit.* TO 'insurance_user'@'localhost';
FLUSH PRIVILEGES;
```

3. **初始化数据库结构**
```bash
# 执行初始化脚本
mysql -u insurance_user -p20150826 insurance_audit < database/init/01-init-schema.sql
mysql -u insurance_user -p20150826 insurance_audit < database/init/02-init-data.sql
```

### 步骤2: Redis配置修复

如果Redis连接失败：

1. **启动Redis服务**
```bash
# Ubuntu/Debian
sudo systemctl start redis

# macOS
brew services start redis

# Windows
redis-server
```

2. **如果不需要Redis，可以在开发环境中禁用**
```yaml
# application-dev.yml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
```

### 步骤3: 后端配置修复

1. **检查并修复application-dev.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/insurance_audit?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:insurance_user}
    password: ${DB_PASSWORD:20150826}
  
app:
  security:
    disabled: true  # 开发环境使用简化安全配置
```

2. **编译和启动后端**
```bash
cd backend
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 步骤4: 前端配置修复

1. **安装依赖**
```bash
cd frontend
npm install
```

2. **检查环境变量**
```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api
```

3. **启动前端**
```bash
npm run dev
```

## 🧪 配置测试

### 自动化测试脚本

1. **运行配置检查脚本**
```bash
# 需要先安装js-yaml
npm install -g js-yaml

# 运行配置检查
node scripts/check-config.js
```

2. **运行后端测试脚本**
```bash
chmod +x scripts/test-backend.sh
./scripts/test-backend.sh
```

3. **运行前端测试脚本**
```bash
chmod +x scripts/test-frontend.sh
./scripts/test-frontend.sh
```

### 手动测试步骤

1. **测试后端健康检查**
```bash
# 启动后端后访问
curl http://localhost:8080/api/v1/health
```

2. **测试前端页面**
```bash
# 启动前端后访问
curl http://localhost:3000
```

3. **测试前后端交互**
```bash
# 通过前端代理访问后端API
curl http://localhost:3000/api/v1/health
```

## 🚀 启动顺序

### 推荐启动顺序

1. **启动数据库服务**
```bash
sudo systemctl start mysql
```

2. **启动Redis服务（可选）**
```bash
sudo systemctl start redis
```

3. **启动后端服务**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **启动前端服务**
```bash
cd frontend
npm run dev
```

5. **访问应用**
- 前端地址: http://localhost:3000
- 后端API: http://localhost:8080/api
- 健康检查: http://localhost:8080/api/v1/health

## 🐛 常见问题解决

### 问题1: 数据库连接失败
```
Error: Access denied for user 'insurance_user'@'localhost'
```
**解决方案**: 检查数据库用户权限，重新创建用户并授权

### 问题2: 端口被占用
```
Error: Port 8080 is already in use
```
**解决方案**: 
- 查找占用端口的进程: `lsof -i :8080`
- 杀死进程: `kill -9 <PID>`
- 或修改配置使用其他端口

### 问题3: JWT密钥过短
```
Error: JWT secret key must be at least 64 characters
```
**解决方案**: 在application-dev.yml中设置更长的JWT密钥

### 问题4: CORS跨域问题
```
Error: CORS policy blocked
```
**解决方案**: 检查CorsConfig配置，确保允许前端域名

### 问题5: 前端代理失败
```
Error: Proxy error
```
**解决方案**: 
- 确保后端服务已启动
- 检查vite.config.ts中的代理配置
- 确认API路径匹配

## 📞 获取帮助

如果遇到配置问题：

1. 查看详细错误日志
2. 检查服务状态
3. 运行自动化测试脚本
4. 参考本指南的解决方案
5. 检查防火墙和网络配置

## ✅ 配置验证清单

- [ ] MySQL服务运行正常
- [ ] insurance_audit数据库已创建
- [ ] 数据库表和初始数据已导入
- [ ] Redis服务运行正常（可选）
- [ ] Java 17+环境正常
- [ ] Maven环境正常
- [ ] 后端配置文件正确
- [ ] 后端服务启动成功
- [ ] 健康检查端点响应正常
- [ ] Node.js 16+环境正常
- [ ] 前端依赖安装完成
- [ ] 前端配置文件正确
- [ ] 前端服务启动成功
- [ ] 前端页面访问正常
- [ ] 前后端API交互正常
- [ ] 登录功能测试通过

完成所有检查项后，开发环境应该可以正常运行！