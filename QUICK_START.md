# 快速开始指南 - 5 分钟启动

## 🚀 快速启动（推荐）

### 1️⃣ 前置条件检查

```bash
docker --version  # 需要 Docker 20.10+
docker-compose --version  # 需要 Docker Compose 2.0+
```

### 2️⃣ 配置环境变量（可选）

```bash
cd keycloak-demo
cp .env.example .env

# 编辑 .env 文件，如果需要配置阿里云服务
# ALIYUN_ACCESS_KEY_ID=your-key-id
# ALIYUN_ACCESS_KEY_SECRET=your-secret
```

### 3️⃣ 启动服务（3 种方式选一）

**方式 A：使用启动脚本（推荐）**
```bash
./start.sh
```

**方式 B：使用 Makefile**
```bash
make up
```

**方式 C：使用 Docker Compose**
```bash
docker-compose up -d
```

### 4️⃣ 等待服务启动（约 30-60 秒）

```bash
# 查看实时日志
docker-compose logs -f

# 或查看具体服务日志
docker-compose logs -f spring-boot-app
docker-compose logs -f keycloak
docker-compose logs -f mysql
```

### 5️⃣ 访问服务

| 服务 | 地址 | 说明 |
|----|------|------|
| Keycloak 管理 | http://localhost:8180 | 用户: admin / 密码: admin123 |
| Spring Boot API | http://localhost:8080/api | 业务 API 端点 |
| API 健康检查 | http://localhost:8080/api/health | 健康状态检查 |

## 📝 验证功能（复制粘贴执行）

### 1. 用户注册

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

预期响应：201 Created，邮件发送通知

### 2. 发送短信验证码

```bash
curl -X POST http://localhost:8080/api/auth/send-sms \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+8613800000000",
    "type": "VERIFICATION"
  }'
```

预期响应：200 OK，短信已发送

### 3. 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123!"
  }'
```

预期响应：200 OK，返回 Access Token

### 4. 查看 Keycloak Realm

访问 http://localhost:8180，使用 admin/admin123 登录，查看：
- Realms → webank
- Clients → spring-boot-app
- Users → 你新建的用户

## 🛑 停止服务

```bash
# 方式 1：使用停止脚本
./stop.sh

# 方式 2：使用 Makefile
make down

# 方式 3：使用 Docker Compose
docker-compose down

# 清除所有数据（重新开始）
docker-compose down -v
```

## 📚 更多文档

| 文档 | 内容 |
|------|------|
| [API.md](./docs/API.md) | 完整 API 文档 |
| [SETUP.md](./docs/SETUP.md) | 详细启动指南 |
| [ARCHITECTURE.md](./docs/ARCHITECTURE.md) | 系统架构说明 |
| [README.md](./README.md) | 项目概览 |

## ⚙️ 常见命令

```bash
# 使用 Makefile
make up           # 启动
make down         # 停止
make logs         # 查看日志
make test         # 运行测试
make clean        # 清理所有
make ps           # 显示状态

# 或使用 Docker Compose
docker-compose up -d              # 启动
docker-compose down               # 停止
docker-compose logs -f            # 日志
docker-compose ps                 # 状态
docker volume ls                  # 列出卷
```

## 🔧 环境变量说明

| 变量 | 默认值 | 说明 |
|------|--------|------|
| MYSQL_DATABASE | webank | 数据库名 |
| MYSQL_USER | webank | MySQL 用户 |
| MYSQL_PASSWORD | webank123 | MySQL 密码 |
| KEYCLOAK_ADMIN_USER | admin | Keycloak 管理员 |
| KEYCLOAK_ADMIN_PASSWORD | admin123 | Keycloak 密码 |
| KEYCLOAK_HOSTNAME | localhost | Keycloak 主机名 |
| ALIYUN_ACCESS_KEY_ID | 无 | 阿里云 AccessKey |
| ALIYUN_ACCESS_KEY_SECRET | 无 | 阿里云 SecretKey |

## ❓ 常见问题

**Q: 服务无法启动？**
```bash
# 检查 Docker 状态
docker ps -a
docker-compose logs

# 清理重新开始
docker-compose down -v
docker-compose up -d
```

**Q: 端口已被占用？**
```bash
# 修改 docker-compose.yml 中的端口配置
# 或关闭占用端口的应用
```

**Q: 短信和邮件无法发送？**
- 检查 .env 中是否配置阿里云凭证
- 检查日志：`docker-compose logs spring-boot-app`

**Q: 如何进入数据库？**
```bash
# 进入 MySQL 容器
docker exec -it keycloak-demo-mysql mysql -u webank -p webank123 webank

# 或使用 Makefile
make shell-db
```

## 📞 获得帮助

- 查看详细文档：[SETUP.md](./docs/SETUP.md)
- 查看架构设计：[ARCHITECTURE.md](./docs/ARCHITECTURE.md)
- 查看 API 文档：[API.md](./docs/API.md)
- 查看项目日志：`docker-compose logs -f`

---

**下一步**：配置阿里云服务，开启完整功能！详见 [SETUP.md](./docs/SETUP.md) 中的 "阿里云配置" 部分。
