# 保险产品智能检核系统部署指南

## 概述

本文档提供了保险产品智能检核系统的完整部署指南，包括生产环境配置、Docker容器化部署、监控配置和运维管理。

## 系统要求

### 硬件要求

- **CPU**: 4核心以上
- **内存**: 8GB以上（推荐16GB）
- **存储**: 100GB以上可用空间
- **网络**: 稳定的互联网连接

### 软件要求

- **操作系统**: Linux (Ubuntu 20.04+ / CentOS 8+ / RHEL 8+)
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Git**: 2.0+

## 部署架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Nginx Proxy   │    │   Frontend      │    │   Backend       │
│   (Port 443)    │────│   (Vue.js)      │────│   (Spring Boot) │
│                 │    │   (Port 80)     │    │   (Port 8080)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                                              │
         │              ┌─────────────────┐            │
         └──────────────│   MySQL 8.0     │────────────┘
                        │   (Port 3306)   │
                        └─────────────────┘
                                 │
                        ┌─────────────────┐
                        │   Redis 7.0     │
                        │   (Port 6379)   │
                        └─────────────────┘
```

## 快速部署

### 1. 克隆项目

```bash
git clone https://github.com/your-org/insurance-audit-system.git
cd insurance-audit-system
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量
vim .env
```

**重要配置项**：
```bash
# 数据库配置
DB_USERNAME=insurance_user
DB_PASSWORD=your_secure_password_here
MYSQL_ROOT_PASSWORD=your_root_password_here

# Redis配置
REDIS_PASSWORD=your_redis_password_here

# JWT配置
JWT_SECRET=your_jwt_secret_key_here_at_least_32_characters_long

# CORS配置
CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### 3. 执行部署脚本

```bash
# 设置脚本执行权限
chmod +x scripts/*.sh

# 执行部署
./scripts/deploy.sh production
```

### 4. 验证部署

部署完成后，访问以下地址验证系统：

- **前端**: https://localhost
- **后端API**: http://localhost:8080/actuator/health
- **监控**: http://localhost:9090 (Prometheus)

## 详细部署步骤

### 1. 环境准备

#### 安装Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# CentOS/RHEL
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
```

#### 安装Docker Compose

```bash
sudo curl -L \"https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)\" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. 项目配置

#### 创建必要目录

```bash
mkdir -p logs/{nginx,mysql,redis,backend,frontend}
mkdir -p data/{mysql,redis,uploads,ssl}
mkdir -p backup
```

#### SSL证书配置

**生产环境**（推荐使用Let's Encrypt）：
```bash
# 安装Certbot
sudo apt-get install certbot

# 获取SSL证书
sudo certbot certonly --standalone -d your-domain.com

# 复制证书到项目目录
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem nginx/ssl/key.pem
```

**开发环境**（自签名证书）：
```bash
# 生成自签名证书
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/key.pem \
  -out nginx/ssl/cert.pem \
  -subj \"/C=CN/ST=Beijing/L=Beijing/O=Insurance/CN=localhost\"
```

### 3. 数据库初始化

数据库会在首次启动时自动初始化，包括：

- 创建数据库表结构
- 插入初始数据（角色、权限、默认用户）
- 配置系统参数

**默认账户**：
- 管理员：`admin` / `admin123`
- 业务用户：`business` / `password123`
- 审核用户：`auditor` / `password123`

### 4. 服务启动

#### 分步启动

```bash
# 1. 启动基础服务
docker-compose up -d mysql redis

# 2. 等待数据库启动
sleep 30

# 3. 启动应用服务
docker-compose up -d backend

# 4. 等待后端启动
sleep 60

# 5. 启动前端和代理
docker-compose up -d frontend nginx
```

#### 一键启动

```bash
docker-compose up -d
```

### 5. 服务验证

```bash
# 检查容器状态
docker-compose ps

# 检查服务健康状态
curl http://localhost:8080/actuator/health
curl http://localhost/health

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

## 监控配置

### Prometheus监控

系统集成了Prometheus监控，提供以下指标：

- **应用指标**: JVM内存、GC、线程池等
- **业务指标**: 登录次数、API调用量、错误率等
- **基础设施指标**: CPU、内存、磁盘、网络等

启动监控服务：
```bash
docker-compose --profile monitoring up -d
```

访问地址：
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

### 日志管理

日志文件位置：
```
logs/
├── nginx/          # Nginx访问日志和错误日志
├── mysql/          # MySQL慢查询日志和错误日志
├── redis/          # Redis日志
├── backend/        # 后端应用日志
└── frontend/       # 前端Nginx日志
```

查看实时日志：
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f mysql
```

## 备份与恢复

### 自动备份

系统提供自动备份脚本，建议设置定时任务：

```bash
# 编辑crontab
crontab -e

# 添加每日凌晨2点备份任务
0 2 * * * /path/to/insurance-audit-system/scripts/backup.sh
```

### 手动备份

```bash
# 执行备份
./scripts/backup.sh

# 备份文件位置
ls -la backup/
```

### 数据恢复

```bash
# 恢复指定备份
./scripts/restore.sh ./backup/20240101_120000

# 恢复过程会自动：
# 1. 停止所有服务
# 2. 备份当前数据
# 3. 恢复配置文件
# 4. 恢复数据库和Redis
# 5. 恢复上传文件
# 6. 重启所有服务
```

## 性能优化

### 数据库优化

**MySQL配置优化**（`database/conf/my.cnf`）：
```ini
# 缓冲区配置
innodb_buffer_pool_size = 1G
innodb_log_buffer_size = 64M
key_buffer_size = 256M

# 连接配置
max_connections = 1000
max_allowed_packet = 64M

# 性能优化
innodb_flush_log_at_trx_commit = 1
sync_binlog = 1
```

### 应用优化

**JVM参数优化**：
```bash
JAVA_OPTS="-Xms512m -Xmx2g \
  -XX:+UseG1GC \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0"
```

**连接池配置**：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### 前端优化

**Nginx缓存配置**：
```nginx
# 静态资源缓存1年
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}

# HTML文件不缓存
location ~* \.html$ {
    expires -1;
    add_header Cache-Control "no-cache, no-store, must-revalidate";
}
```

## 安全配置

### 网络安全

1. **防火墙配置**：
```bash
# 只开放必要端口
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS
sudo ufw enable
```

2. **SSL/TLS配置**：
- 使用TLS 1.2+
- 配置强密码套件
- 启用HSTS

3. **访问控制**：
- 限制管理接口访问
- 配置API限流
- 实施IP白名单

### 应用安全

1. **认证授权**：
- JWT令牌管理
- 角色权限控制
- 会话管理

2. **数据保护**：
- 密码加密存储
- 敏感数据脱敏
- 审计日志记录

3. **输入验证**：
- 参数验证
- SQL注入防护
- XSS防护

## 故障排除

### 常见问题

1. **服务启动失败**：
```bash
# 检查端口占用
netstat -tlnp | grep :8080

# 检查磁盘空间
df -h

# 检查内存使用
free -h
```

2. **数据库连接失败**：
```bash
# 检查数据库状态
docker-compose exec mysql mysqladmin ping

# 检查数据库日志
docker-compose logs mysql
```

3. **前端访问异常**：
```bash
# 检查Nginx配置
docker-compose exec nginx nginx -t

# 检查前端日志
docker-compose logs frontend
```

### 日志分析

**关键日志位置**：
- 应用日志：`logs/backend/insurance-audit-system.log`
- 访问日志：`logs/nginx/access.log`
- 错误日志：`logs/nginx/error.log`
- 数据库日志：`logs/mysql/error.log`

**常用日志命令**：
```bash
# 查看错误日志
tail -f logs/backend/insurance-audit-system.log | grep ERROR

# 分析访问日志
tail -f logs/nginx/access.log | grep -v "200"

# 监控数据库慢查询
tail -f logs/mysql/slow.log
```

## 运维管理

### 服务管理

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启特定服务
docker-compose restart backend

# 查看服务状态
docker-compose ps

# 查看资源使用
docker stats
```

### 更新部署

```bash
# 拉取最新代码
git pull origin main

# 重新构建镜像
docker-compose build

# 滚动更新
docker-compose up -d --no-deps backend
docker-compose up -d --no-deps frontend
```

### 扩容配置

**水平扩容**：
```yaml
# docker-compose.yml
services:
  backend:
    deploy:
      replicas: 3
    
  nginx:
    depends_on:
      - backend
```

**垂直扩容**：
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 8G
```

## 联系支持

如遇到部署问题，请联系技术支持：

- **邮箱**: support@insurance-audit.com
- **文档**: https://docs.insurance-audit.com
- **问题反馈**: https://github.com/your-org/insurance-audit-system/issues

---

**版本**: 1.0.0  
**更新时间**: 2024-01-01  
**维护团队**: Insurance Audit Development Team