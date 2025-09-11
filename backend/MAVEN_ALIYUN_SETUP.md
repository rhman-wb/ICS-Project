# Maven阿里云镜像配置说明

## 配置概述

本项目已配置使用阿里云Maven镜像，以加速依赖下载。配置包括：

### 1. pom.xml配置
- 添加了阿里云仓库配置
- 包含central、public、spring等多个仓库
- 配置了插件仓库

### 2. settings.xml配置
- 项目根目录下的settings.xml文件
- 配置了阿里云镜像
- 设置了本地仓库路径

### 3. Maven Wrapper配置
- .mvn/wrapper/maven-wrapper.properties
- 使用阿里云镜像下载Maven

## 使用方法

### 方式一：使用项目配置的settings.xml（推荐）

```bash
# 编译项目
mvn clean compile -s settings.xml

# 启动开发环境
mvn spring-boot:run -s settings.xml -Dspring-boot.run.profiles=dev

# 打包项目
mvn clean package -s settings.xml -DskipTests
```

### 方式二：使用便捷脚本

**Windows:**
```cmd
# 编译
compile.bat

# 启动开发环境
run-dev.bat
```

**Linux/Mac:**
```bash
# 编译
./compile.sh

# 启动开发环境
./run-dev.sh
```

### 方式三：全局配置Maven（可选）

如果想要全局使用阿里云镜像，可以将settings.xml复制到Maven安装目录：

**Windows:**
```cmd
copy settings.xml %MAVEN_HOME%\conf\settings.xml
```

**Linux/Mac:**
```bash
cp settings.xml $MAVEN_HOME/conf/settings.xml
```

## 阿里云镜像仓库说明

| 仓库名称 | 地址 | 说明 |
|---------|------|------|
| central | https://maven.aliyun.com/repository/central | Maven中央仓库镜像 |
| public | https://maven.aliyun.com/repository/public | 公共仓库，包含多个仓库聚合 |
| spring | https://maven.aliyun.com/repository/spring | Spring官方仓库镜像 |
| spring-plugin | https://maven.aliyun.com/repository/spring-plugin | Spring插件仓库镜像 |

## 验证配置

运行以下命令验证配置是否生效：

```bash
# 查看有效的仓库配置
mvn help:effective-settings -s settings.xml

# 查看依赖树（会显示下载源）
mvn dependency:tree -s settings.xml
```

## 常见问题

### 1. 下载速度仍然很慢
- 检查网络连接
- 确认使用了 `-s settings.xml` 参数
- 清理本地仓库：`mvn clean -s settings.xml`

### 2. 某些依赖下载失败
- 检查依赖版本是否正确
- 尝试使用不同的阿里云仓库
- 查看Maven日志获取详细错误信息

### 3. SSL证书问题
已在maven.config中配置了SSL相关参数，如仍有问题可以：
```bash
mvn clean compile -s settings.xml -Dmaven.wagon.http.ssl.insecure=true
```

## 性能优化建议

1. **本地仓库位置**: 已配置为项目内的.m2/repository，避免全局污染
2. **内存设置**: 脚本中已设置MAVEN_OPTS优化内存使用
3. **并行下载**: Maven 3.x默认支持并行下载
4. **离线模式**: 首次下载后可使用 `-o` 参数启用离线模式

## 更新日志

- 2024-01-01: 初始配置阿里云镜像
- 配置了多个阿里云仓库确保依赖完整性
- 添加了便捷脚本简化使用
- 优化了Maven内存配置