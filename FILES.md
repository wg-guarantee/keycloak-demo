# 文件清单

## 📁 项目结构总览

```
keycloak-demo/
│
├── 📄 README.md                    # 项目主页
├── 📄 QUICK_START.md               # 快速开始（5分钟）
├── 📄 FILES.md                     # 文件清单（本文件）
├── 📄 .env.example                 # 环境变量模板
├── 📄 .gitignore                   # Git 忽略配置
├── 📄 .dockerignore                # Docker 忽略配置
├── 📄 docker-compose.yml           # Docker Compose 编排
├── 📄 Makefile                     # 便捷命令
├── 🔧 start.sh                     # 启动脚本
├── 🔧 stop.sh                      # 停止脚本
├── 🔧 test.sh                      # 测试脚本
│
├── 📂 keycloak/                    # Keycloak 配置
│   ├── realm-export.json           # Realm 配置（可导入导出）
│   ├── docker-entrypoint.sh        # 初始化脚本
│   └── themes/                     # 自定义主题（扩展用）
│
├── 📂 spring-boot-app/             # Spring Boot 应用
│   ├── pom.xml                     # Maven 配置（依赖、插件）
│   ├── Dockerfile                  # Docker 构建文件
│   ├── .dockerignore               # Docker 忽略配置
│   │
│   └── 📂 src/main/java/com/webank/app/
│       │
│       ├── Application.java        # 主启动类
│       │
│       ├── 📂 config/              # 配置类
│       │   ├── KeycloakConfig.java          # Keycloak 配置
│       │   ├── AliyunConfig.java            # 阿里云配置
│       │   └── SecurityConfig.java          # Spring Security 配置
│       │
│       ├── 📂 entity/              # 数据库实体（JPA）
│       │   ├── User.java                    # 用户表
│       │   ├── VerificationCode.java        # 验证码
│       │   ├── EmailLog.java                # 邮件日志
│       │   └── SmsLog.java                  # 短信日志
│       │
│       ├── 📂 repository/          # 数据访问层（Spring Data JPA）
│       │   ├── UserRepository.java          # 用户仓储
│       │   ├── VerificationCodeRepository.java  # 验证码仓储
│       │   ├── EmailLogRepository.java      # 邮件日志仓储
│       │   └── SmsLogRepository.java        # 短信日志仓储
│       │
│       ├── 📂 service/             # 业务逻辑层
│       │   ├── UserService.java             # 用户服务
│       │   ├── AuthService.java             # 认证服务
│       │   ├── VerificationCodeService.java # 验证码服务
│       │   ├── EmailService.java            # 邮件服务
│       │   ├── SmsService.java              # 短信服务
│       │   ├── AliyunSmsService.java        # 阿里云短信接口
│       │   ├── AliyunEmailService.java      # 阿里云邮件接口
│       │   └── NotificationService.java     # 通知管理服务
│       │
│       ├── 📂 controller/          # API 控制层
│       │   ├── AuthController.java          # 认证 API
│       │   └── HealthController.java        # 健康检查 API
│       │
│       ├── 📂 dto/                 # 数据传输对象
│       │   └── RequestDtos.java             # 所有 DTO 定义
│       │
│       ├── 📂 exception/           # 异常处理
│       │   ├── BusinessException.java       # 业务异常
│       │   └── GlobalExceptionHandler.java  # 全局异常处理
│       │
│       ├── 📂 util/                # 工具类
│       │   └── ValidationUtil.java          # 验证工具
│       │
│       └── 📂 resources/           # 资源文件
│           ├── application.yml              # 应用程序配置
│           └── (日志配置、模板等)
│
├── 📂 database/                    # 数据库脚本
│   ├── init.sql                    # 初始化脚本（建表）
│   └── 📂 migrations/              # Flyway 迁移版本控制
│       └── V1__initial_schema.sql   # 初始版本
│
└── 📂 docs/                        # 文档
    ├── SETUP.md                    # 详细启动指南
    ├── ARCHITECTURE.md             # 系统架构设计
    ├── API.md                      # API 完整文档
    └── FILES.md                    # 本文件

```

## 📋 文件说明

### 根目录文件

| 文件 | 类型 | 说明 |
|------|------|------|
| README.md | 📄 | 项目介绍和概览 |
| QUICK_START.md | 📄 | 5 分钟快速开始指南 |
| FILES.md | 📄 | 项目结构和文件清单 |
| .env.example | ⚙️ | 环境变量模板（必须复制为 .env） |
| .gitignore | ⚙️ | Git 忽略配置 |
| docker-compose.yml | 🐳 | Docker Compose 编排文件 |
| Makefile | 🔧 | 便捷 make 命令 |
| start.sh | 🔧 | 启动脚本 |
| stop.sh | 🔧 | 停止脚本 |
| test.sh | 🔧 | 测试脚本 |

### Keycloak 配置文件

| 文件 | 说明 |
|------|------|
| realm-export.json | Realm 配置，包含 Client、Role、User |
| docker-entrypoint.sh | 容器初始化脚本 |
| themes/ | 自定义主题目录（扩展用） |

### Spring Boot 核心文件

#### 配置文件
| 文件 | 说明 | 关键配置 |
|------|------|---------|
| pom.xml | Maven 项目配置 | 依赖、版本、插件 |
| Dockerfile | Docker 构建 | 镜像构建、端口暴露 |
| application.yml | 应用配置 | 数据库、Keycloak、阿里云 |

#### Entity 层（数据库映射）
| 文件 | 说明 | 表名 |
|------|------|------|
| User.java | 用户账户信息 | `user` |
| VerificationCode.java | 邮件/短信验证码 | `verification_code` |
| EmailLog.java | 邮件发送日志 | `email_log` |
| SmsLog.java | 短信发送日志 | `sms_log` |

#### Repository 层（数据访问）
| 文件 | 说明 | 功能 |
|------|------|------|
| UserRepository | 用户数据访问 | CRUD、按用户名/邮箱查询 |
| VerificationCodeRepository | 验证码数据访问 | 生成、验证、清理验证码 |
| EmailLogRepository | 邮件日志查询 | 统计、查询发送历史 |
| SmsLogRepository | 短信日志查询 | 统计、查询发送历史 |

#### Service 层（业务逻辑）
| 文件 | 说明 | 主要方法 |
|------|------|---------|
| UserService | 用户管理 | createUser, activateUser, verifyEmail |
| AuthService | 认证逻辑 | register, activate, sendSmsCode, sendPasswordReset |
| VerificationCodeService | 验证码管理 | generateCode, verifyCode, markAsUsed |
| EmailService | 邮件管理 | sendActivationEmail, sendPasswordResetEmail |
| SmsService | 短信管理（本地） | sendVerificationCode |
| AliyunSmsService | 阿里云短信 SDK | 真实发送短信 API 调用 |
| AliyunEmailService | 阿里云邮件 SDK | 真实发送邮件 API 调用 |
| NotificationService | 通知管理 | 异步发送邮件和短信 |

#### Controller 层（API 端点）
| 文件 | 路由前缀 | 端点 |
|------|---------|------|
| AuthController | /api/auth | register, login, activate, changePassword, sendSms |
| HealthController | / | /health, /actuator/health |

#### DTO 层（数据传输对象）
| DTO | 用途 | 字段 |
|-----|------|------|
| RegisterRequest | 注册请求 | username, email, password, phone |
| LoginRequest | 登录请求 | username, password |
| ActivateRequest | 激活请求 | email, token |
| SendSmsRequest | 短信请求 | phone, type |
| UserDto | 用户响应 | id, username, email, 验证状态等 |
| LoginResponse | 登录响应 | accessToken, tokenType, expiresIn |
| ApiResponse<T> | 通用响应 | code, message, data |

#### 异常处理
| 文件 | 说明 |
|------|------|
| BusinessException | 业务异常类 |
| GlobalExceptionHandler | 全局异常处理 |

#### 工具类
| 文件 | 说明 | 验证 |
|------|------|------|
| ValidationUtil | 验证工具 | 邮箱、电话、用户名、密码 |

### 数据库文件

| 文件 | 说明 | 内容 |
|------|------|------|
| init.sql | 初始化脚本 | 创建所有表和索引 |
| V1__initial_schema.sql | Flyway 版本 | 同上（用于版本管理） |

### 文档文件

| 文件 | 说明 | 读者 |
|------|------|------|
| QUICK_START.md | 5 分钟快速开始 | 所有人 |
| SETUP.md | 详细启动和配置指南 | 开发者、运维 |
| ARCHITECTURE.md | 系统架构和设计说明 | 架构师、开发者 |
| API.md | 完整 API 文档和示例 | 前端开发、集成者 |
| FILES.md | 本项目结构清单 | 项目贡献者 |

## 🚀 常用文件操作

### 修改配置
```bash
# 1. 编辑环境变量
vi .env

# 2. 编辑应用配置
vi spring-boot-app/src/main/resources/application.yml

# 3. 编辑 Docker Compose
vi docker-compose.yml
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务
docker-compose logs -f spring-boot-app
docker-compose logs -f keycloak
docker-compose logs -f mysql
```

### 进入容器
```bash
# 进入数据库
docker exec -it keycloak-demo-mysql mysql -u webank -p webank123 webank

# 进入应用容器
docker exec -it keycloak-demo-app bash

# 进入 Keycloak 容器
docker exec -it keycloak-demo-keycloak bash
```

### 构建和编译
```bash
# 构建 Spring Boot 镜像
docker-compose build spring-boot-app

# 本地 Maven 编译
cd spring-boot-app
mvn clean package
```

## 📊 文件大小估计

| 部分 | 文件数 | 代码行数 | 说明 |
|------|--------|--------|------|
| 配置文件 | 4 | ~300 | docker-compose, yml, env |
| Keycloak | 2 | ~200 | realm-export, script |
| Spring Boot Code | 20+ | ~2500 | entity, service, controller |
| Spring Boot Config | 3 | ~200 | pom, yml, docker |
| Database | 2 | ~200 | SQL 脚本 |
| Documents | 5 | ~2000 | 所有文档 |
| **Total** | **36+** | **~5400** | 完整项目 |

## 🔄 文件依赖关系

```
docker-compose.yml
  ├─ Keycloak
  │   ├─ realm-export.json
  │   ├─ MySQL
  │   └─ docker-entrypoint.sh
  │
  ├─ Spring Boot
  │   ├─ Dockerfile
  │   ├─ pom.xml
  │   ├─ application.yml
  │   └─ Java 源代码（entity, service, controller）
  │
  └─ MySQL
      ├─ init.sql
      └─ V1__initial_schema.sql

.env
  ├─ docker-compose.yml (读取环境变量)
  ├─ application.yml (SPRING_ 前缀)
  └─ Java 程序 (ALIYUN_ 前缀)
```

## 💡 开发建议

1. **第一次运行**：按 QUICK_START.md 步骤
2. **理解架构**：阅读 ARCHITECTURE.md
3. **API 集成**：查看 API.md
4. **深入开发**：查看源码注释，特别是 Service 层
5. **故障排查**：查看 SETUP.md 的 "故障排查" 部分

## 📝 文件修改检查清单

在修改文件前，确保：
- [ ] 已备份原文件
- [ ] 理解修改的影响范围
- [ ] 按 SETUP.md 重启应用
- [ ] 查看日志确认生效

---

**最后更新**：2024-03-06  
**版本**：1.0.0
