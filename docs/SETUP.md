# Keycloak Demo 快速启动指南

## 系统要求

- Docker 20.10+
- Docker Compose 2.0+
- 或本地环境：Java 17+, Maven 3.8+, MySQL 8.0+

## 快速启动（Docker Compose）

### 1. 编辑环境变量

```bash
cd keycloak-demo
cp .env.example .env
```

编辑 `.env` 文件，配置阿里云凭证：
```bash
ALIYUN_ACCESS_KEY_ID=your-access-key-id
ALIYUN_ACCESS_KEY_SECRET=your-access-key-secret
ALIYUN_SMS_SIGN_NAME=Webank
ALIYUN_SMS_TEMPLATE_CODE=SMS_TEMPLATE_ID
ALIYUN_EMAIL_FROM=noreply@webank.com
```

### 2. 启动服务

```bash
docker-compose up -d
```

### 3. 验证服务启动

```bash
# 检查所有容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 检查 Keycloak 健康状态
curl -s http://localhost:8180/health | jq .

# 检查 Spring Boot 应用
curl -s http://localhost:8080/api/health | jq .
```

### 4. 访问服务

- **Keycloak 管理界面**：http://localhost:8180
  - 用户名：admin
  - 密码：admin123

- **Spring Boot API**：http://localhost:8080/api

## 验证功能

### 注册用户

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "phone": "+8613800000000"
  }'
```

### 发送邮件激活

应检查邮箱接收激活邮件。

### 发送短信验证码

```bash
curl -X POST http://localhost:8080/api/auth/send-sms \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+8613800000000",
    "type": "VERIFICATION"
  }'
```

### 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!"
  }'
```

### 改密

```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {access_token}" \
  -d '{
    "oldPassword": "Password123!",
    "newPassword": "NewPassword456!"
  }'
```

## 故障排查

### MySQL 连接失败

```bash
# 检查 MySQL 容器
docker-compose logs mysql

# 重启 MySQL
docker-compose restart mysql
```

### Keycloak 无法启动

```bash
# 检查日志
docker-compose logs keycloak

# 清除数据重新启动
docker-compose down -v
docker-compose up -d
```

### Spring Boot 应用失败

```bash
# 查看详细日志
docker-compose logs spring-boot-app

# 检查 Keycloak 是否就绪
curl -v http://keycloak:8080 2>&1 | head -20
```

## 停止服务

```bash
docker-compose down

# 删除所有数据
docker-compose down -v
```

## 本地开发（不用 Docker）

### 前置条件

1. MySQL 8.0 运行在 localhost:3306
2. Keycloak 22 运行在 localhost:8180
3. Java 17 + Maven 3.8

### 启动 Spring Boot

```bash
cd spring-boot-app
mvn spring-boot:run
```

### 启动 Keycloak

```bash
# 下载 Keycloak
cd keycloak
./standalone.sh
```

访问 http://localhost:8180，使用 admin/admin123 登录。

## API 文档

详见 [API.md](./API.md)

## 架构说明

详见 [ARCHITECTURE.md](./ARCHITECTURE.md)
