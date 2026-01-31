# Financial Journal Maker - 启动指南

本文档介绍如何在本地启动 Financial Journal Maker 应用。

## 前置要求

### 必需
- **Java 17** (推荐 Amazon Corretto 17 或 OpenJDK 17)
- **Maven 3.6+**
- **PostgreSQL 15** (可通过 Docker 运行)

### 可选
- **Docker** 和 **Docker Compose** (用于容器化部署)

## 启动方式

### 方式 1: 本地启动 (推荐开发使用)

使用本地 Java 环境运行应用，PostgreSQL 通过 Docker 启动。

```bash
# 赋予执行权限
chmod +x start-local.sh

# 启动应用
./start-local.sh
```

**特点：**
- ✅ 快速重启
- ✅ 便于调试
- ✅ 支持热重载 (使用 Spring DevTools)
- ✅ 直接查看日志

**访问地址：**
- 应用: http://localhost:8080
- API 文档: http://localhost:8080/swagger-ui/index.html
- 健康检查: http://localhost:8080/actuator/health

### 方式 2: Docker 完整启动

使用 Docker Compose 启动完整的应用栈（PostgreSQL + 应用）。

```bash
# 赋予执行权限
chmod +x start-docker.sh

# 启动应用
./start-docker.sh
```

**特点：**
- ✅ 环境隔离
- ✅ 生产环境模拟
- ✅ 一键启动所有服务
- ✅ 便于团队协作

**访问地址：**
- 应用: http://localhost:8080
- API 文档: http://localhost:8080/swagger-ui/index.html
- 健康检查: http://localhost:8080/actuator/health
- PostgreSQL: localhost:5432

### 方式 3: 仅启动数据库

如果只需要启动 PostgreSQL 数据库：

```bash
docker-compose up -d postgres
```

### 停止服务

```bash
# 赋予执行权限
chmod +x stop.sh

# 停止所有服务
./stop.sh
```

或者手动停止：

```bash
# 停止 Docker 容器
docker-compose down

# 停止 Docker 容器并删除数据
docker-compose down -v

# 停止本地 Java 进程
# 按 Ctrl+C 或查找进程 ID 后 kill
```

## 数据库配置

### 默认配置

- **主机**: localhost
- **端口**: 5432
- **数据库**: coa_db
- **用户名**: coa_user
- **密码**: coa_password

### 连接数据库

```bash
# 使用 psql 连接
psql -h localhost -p 5432 -U coa_user -d coa_db

# 使用 Docker 连接
docker exec -it coa-postgres psql -U coa_user -d coa_db
```

## 配置文件

### 本地开发配置

`backend/src/main/resources/application-local.yml`

此配置文件用于本地开发，包含：
- 数据库连接配置
- JPA/Hibernate 配置
- Flyway 迁移配置
- 日志级别配置
- Swagger UI 配置

### Docker 配置

`docker-compose.yml`

定义了完整的服务栈：
- PostgreSQL 数据库服务
- 应用服务
- 健康检查
- 数据卷

## 常见问题

### 1. Java 版本错误

**问题**: `Fatal error compiling: java.lang.ExceptionInInitializerError`

**解决**:
```bash
# 检查 Java 版本
java -version

# 设置 JAVA_HOME 为 Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 验证
echo $JAVA_HOME
```

### 2. 端口已被占用

**问题**: `Port 8080 is already in use`

**解决**:
```bash
# 查找占用端口的进程
lsof -i :8080

# 停止进程
kill -9 <PID>
```

### 3. PostgreSQL 连接失败

**问题**: `Connection refused` 或 `Database does not exist`

**解决**:
```bash
# 检查 PostgreSQL 是否运行
docker ps | grep postgres

# 重启 PostgreSQL
docker-compose restart postgres

# 查看日志
docker-compose logs postgres
```

### 4. 编译失败

**问题**: Maven 编译错误

**解决**:
```bash
# 清理并重新编译
cd backend
mvn clean compile

# 如果网络问题，配置国内镜像
# 编辑 ~/.m2/settings.xml 添加阿里云镜像
```

## 开发工作流

### 典型开发流程

1. **启动数据库**
   ```bash
   docker-compose up -d postgres
   ```

2. **启动应用**
   ```bash
   ./start-local.sh
   ```

3. **开发和测试**
   - 修改代码
   - 访问 http://localhost:8080/swagger-ui.html 测试 API
   - 查看日志输出

4. **停止服务**
   ```bash
   ./stop.sh
   ```

### 查看日志

```bash
# 本地启动：直接在终端查看

# Docker 启动：
docker-compose logs -f coa-service
docker-compose logs -f postgres

# 查看最近 100 行
docker-compose logs --tail=100 coa-service
```

### 数据库迁移

Flyway 会在应用启动时自动执行迁移脚本：

```bash
# 迁移脚本位置
backend/src/main/resources/db/migration/

# 查看迁移状态
# 访问数据库查看 flyway_schema_history 表
```

## 环境变量

可以通过环境变量覆盖默认配置：

```bash
# 数据库配置
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=coa_db
export DB_USER=coa_user
export DB_PASSWORD=coa_password

# 应用配置
export SERVER_PORT=8080
export LOG_LEVEL=DEBUG

# 启动应用
./start-local.sh
```

## 性能优化

### 本地开发优化

1. **增加 JVM 内存**
   ```bash
   export MAVEN_OPTS="-Xmx2g -Xms512m"
   ```

2. **跳过测试**
   ```bash
   mvn clean package -DskipTests
   ```

3. **使用 Spring DevTools**
   - 已在 pom.xml 中配置
   - 支持自动重启和热重载

## 技术栈

- **Java 17** - 编程语言
- **Spring Boot 3.2.2** - 应用框架
- **PostgreSQL 15** - 数据库
- **Flyway** - 数据库迁移
- **Lombok** - 代码简化
- **Swagger/OpenAPI** - API 文档
- **Maven** - 构建工具

## 下一步

- 查看 [API 文档](http://localhost:8080/swagger-ui.html)
- 阅读 [开发指南](./docs/development-guide.md)
- 查看 [架构设计](./specs/002-accounting-rules/plan.md)
