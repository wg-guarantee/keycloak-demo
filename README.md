# README.md

# Keycloak + 阿里云集成 Demo 项目

这是一个完整的 Keycloak + Spring Boot + 阿里云服务集成 demo 环境，用于验证注册、登录、激活、改密、短信、邮件等功能。

## 项目特性

- ✅ **Keycloak 认证**：OAuth2 + OpenID Connect
- ✅ **用户注册**：支持用户名、邮箱、电话
- ✅ **邮箱激活**：异步发送激活链接
- ✅ **阿里云短信**：验证码发送
- ✅ **阿里云邮件**：激活和密码重置邮件
- ✅ **Docker 部署**：一键启动所有服务

## 快速开始

### 前置条件

- Docker 20.10+
- Docker Compose 2.0+

### 启动服务

1. **克隆/下载项目**

```bash
cd keycloak-demo
```

2. **配置环境变量**

```bash
cp .env.example .env
# 编辑 .env 文件，配置阿里云凭证
```

3. **启动 Docker Compose**

```bash
docker-compose up -d
```

4. **访问服务**

- **Keycloak 管理界面**：[http://localhost:8180](http://localhost:8180)
  - 用户名：admin
  - 密码：admin123

- **Spring Boot API**：[http://localhost:8080/api](http://localhost:8080/api)

- **API 文档**：[API.md](./docs/API.md)

- **快速启动指南**：[SETUP.md](./docs/SETUP.md)

## 项目结构

```
keycloak-demo/
├── keycloak/                    # Keycloak 配置
│   ├── realm-export.json        # Realm 配置文件
│   └── docker-entrypoint.sh     # 初始化脚本
├── spring-boot-app/             # Spring Boot 应用
│   ├── src/                     # Java 源代码
│   ├── pom.xml                  # Maven 配置
│   └── Dockerfile               # Docker 构建文件
├── database/                    # 数据库
│   ├── init.sql                 # 初始化脚本
│   └── migrations/              # 数据库迁移
├── docker-compose.yml           # Docker Compose 配置
├── .env.example                 # 环境变量模板
└── docs/                        # 文档
    ├── SETUP.md                 # 快速启动指南
    ├── API.md                   # API 文档
    └── ARCHITECTURE.md          # 架构文档
```

## 验证流程

### 1. 注册用户

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

### 2. 激活账户

收到邮件中的激活链接，访问即可激活。

### 3. 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!"
  }'
```

### 4. 发送验证码

```bash
curl -X POST http://localhost:8080/api/auth/send-sms \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+8613800000000",
    "type": "VERIFICATION"
  }'
```

## 依赖服务

- **Keycloak 22**：身份认证服务
- **MySQL 8.0**：数据库
- **Spring Boot 3.1.7**：Web 框架
- **Aliyun SDK**：短信和邮件服务

## 环境变量配置

编辑 `.env` 文件配置：

```env
# Keycloak
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=admin123

# MySQL
MYSQL_DATABASE=webank
MYSQL_USER=webank
MYSQL_PASSWORD=webank123

# Aliyun
ALIYUN_ACCESS_KEY_ID=your-key-id
ALIYUN_ACCESS_KEY_SECRET=your-key-secret
ALIYUN_SMS_SIGN_NAME=Webank
ALIYUN_SMS_TEMPLATE_CODE=SMS_TEMPLATE_ID
ALIYUN_EMAIL_FROM=noreply@webank.com
```

## 常见问题

### Q: Keycloak 无法启动？
A: 检查 MySQL 是否就绪，查看日志：`docker-compose logs keycloak`

### Q: 短信和邮件没有发送？
A: 检查 `.env` 中是否配置了阿里云凭证，日志：`docker-compose logs spring-boot-app`

### Q: 如何停止服务？
```bash
docker-compose down
```

### Q: 如何清除所有数据重新开始？
```bash
docker-compose down -v
docker-compose up -d
```

## 生产环境部署

### 安全检查清单

- [ ] 更改所有默认密码
- [ ] 配置 HTTPS/TLS
- [ ] 启用防火墙规则
- [ ] 设置数据库备份策略
- [ ] 配置日志聚合和监控
- [ ] 启用审计日志

### ECS 部署示例

参考 [SETUP.md](./docs/SETUP.md) 中的云环境部署说明。

## 支持

- 查看 [ARCHITECTURE.md](./docs/ARCHITECTURE.md) 了解系统架构
- 查看 [API.md](./docs/API.md) 了解 API 详情
- 查看日志排查问题：`docker-compose logs -f`

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request。

---

**开发日期**：2024-03-06  
**版本**：1.0.0
