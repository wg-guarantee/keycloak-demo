# 系统架构文档

## 总体架构

```
┌─────────────────┐
│   Frontend      │
│   (Browser)     │
└────────┬────────┘
         │ HTTP/HTTPS
         │
    ┌────▼────────────────────┐
    │   Spring Boot App        │
    │   (Port 8080)            │
    ├─────────────────────────┤
    │  Controllers             │
    │  - AuthController       │
    │  - UserController       │
    │  - NotificationCtrl     │
    └────┬────────────────────┘
         │
    ┌────┼──────────────────────────┐
    │    │                          │
    │    ▼                          ▼
    │ ┌──────────────┐    ┌──────────────────┐
    │ │  Keycloak    │    │   Aliyun Cloud   │
    │ │  (OAuth2)    │    │                  │
    │ │ Port 8180    │    │ - SMS Service    │
    │ └──────┬───────┘    │ - Email Service  │
    │        │            └──────────────────┘
    └────────┼─────────────────────────────┘
             │
             ▼
        ┌─────────────┐
        │   MySQL     │
        │ Port 3306   │
        └─────────────┘
```

## 核心组件

### 1. Keycloak（Realm: webank）

**责任**：身份认证和授权管理

**功能**：
- 用户认证（OIDC 协议）
- 角色管理（admin, user）
- 令牌颁发（Access Token, Refresh Token）
- 单点登录（SSO）

**通信**：
```
Spring Boot <---> Keycloak OAuth2 Provider
                  (Token Endpoint, Userinfo Endpoint)
```

### 2. Spring Boot 应用

**层级架构**：

```
Request
   │
   ▼
Controller Layer
   ├─ AuthController
   ├─ UserController
   └─ NotificationController
   │
   ▼
Service Layer
   ├─ AuthService
   │  └─ Keycloak 集成
   ├─ UserService
   │  └─ User Repository
   ├─ NotificationService
   │  ├─ SmsService (Aliyun SDK)
   │  └─ EmailService (Aliyun SDK)
   │
   ▼
Repository Layer
   ├─ UserRepository
   ├─ VerificationCodeRepository
   ├─ EmailLogRepository
   └─ SmsLogRepository
   │
   ▼
Database (MySQL)
```

### 3. 数据库设计

**业务表**：

| 表名 | 用途 | 关键字段 |
|-----|------|---------|
| `user` | 用户账户信息 | id, username, email, phone |
| `verification_code` | 验证码 token | id, user_id, type, code, expires_at |
| `email_log` | 邮件发送日志 | id, user_id, status, created_at |
| `sms_log` | 短信发送日志 | id, user_id, status, created_at |
| `password_history` | 密码历史（可选） | id, user_id, old_password_hash |

**ER 图**：

```
user (PK: id)
  │
  ├─► verification_code (FK: user_id)
  │
  ├─► email_log (FK: user_id)
  │
  ├─► sms_log (FK: user_id)
  │
  └─► password_history (FK: user_id)
```

### 4. 消息流

#### 注册流程

```
User Register Request
  │
  ▼
AuthController.register()
  │
  ├─► Create User (Keycloak + DB)
  │
  ├─► Generate Email Token
  │
  ├─► Send Activation Email (Aliyun)
  │
  └─► Save Email Log
      │
      ▼
  Response: 201 Created
```

#### 登录流程

```
User Login Request
  │
  ▼
AuthController.login()
  │
  ├─► Validate Credentials (against Keycloak)
  │
  ├─► Generate JWT (from Keycloak)
  │
  └─► Response with Access Token
      │
      ▼
  Browser stores Token → Use in headers
```

#### 激活账户流程

```
User Click Email Link
  │
  └─ {baseUrl}/auth/activate?token={token}
     │
     ▼
AuthController.activate()
  │
  ├─► Verify Token (JWT validation)
  │
  ├─► Mark User as Activated
  │
  └─► Response: Account Activated
```

#### 发送短信流程

```
Send SMS Request
  │
  ▼
NotificationController.sendSms()
  │
  ├─► Generate Verification Code
  │
  ├─► Cache/Store Code (DB with TTL)
  │
  ├─► Call Aliyun SMS API
  │   └─ Parameter: {phone, template_code, code}
  │
  ├─► Log SMS Result
  │
  └─► Response: SMS Sent Successfully
```

#### 发送邮件流程

```
Send Email Request
  │
  ▼
NotificationController.sendEmail()
  │
  ├─► Generate Email Link
  │
  ├─► Call Aliyun DirectMail API
  │   └─ Parameter: {recipient, subject, html_body}
  │
  ├─► Log Email Result
  │
  └─► Response: Email Sent Successfully
```

## 安全设计

### 1. 认证与授权

- **认证**：OAuth2 + OpenID Connect（Keycloak）
- **授权**：Spring Security + 角色基访问控制（RBAC）
- **Token**：JWT（RS256 签名）

### 2. 敏感信息保护

- **密码**：不存储明文，Keycloak 管理
- **验证码**：存储前加密，5 分钟过期
- **邮件 Token**：JWT 格式，自签名，24 小时过期

### 3. API 安全

- **HTTPS**：生产环境强制使用
- **CORS**：配置白名单
- **速率限制**：防暴力破解
- **输入验证**：所有请求参数验证

### 4. 阿里云安全

- **AccessKey 管理**：环境变量存储，不提交代码
- **签名验证**：所有 API 请求签名验证
- **日志审计**：记录所有通知操作

## 部署拓扑

### 本地开发

```
Docker Compose
├─ Keycloak:8180
├─ Spring Boot:8080
├─ MySQL:3306
└─ Network: webank-network
```

### 云环境（ECS + RDS）

```
Internet Gateway
   │
   ▼
ECS Instance (Spring Boot + Keycloak)
   │
   ├─ Keycloak:8180 (内部)
   ├─ Spring Boot:8080 (公网)
   │
   └─ Connection Pool ──► RDS MySQL:3306
```

## 性能优化

### 1. 数据库

- **连接池**：HikariCP（默认 10-20 连接）
- **索引**：外键、用户名、邮箱字段建索引
- **分项**：短期存储表可定期清理旧记录

### 2. 缓存

- **令牌缓存**：Spring Cache（可选升级 Redis）
- **验证码缓存**：数据库 + TTL（可升级 Redis）
- **用户信息**：Keycloak 内置缓存

### 3. 异步处理

- **邮件发送**：@Async 异步发送，防止阻塞
- **短信发送**：@Async 异步发送，消息队列可选
- **日志记录**：异步保存数据库

## 扩展性

### 支持多租户

添加 `tenant_id` 字段到相关表，使用 Keycloak realm 隔离。

### 支持社交登录

Keycloak 原生支持：
- Google OAuth2
- GitHub OAuth2
- 钉钉（Custom IDP）

### 支持多因素认证（MFA）

Keycloak 配置 TOTP（Time-based One-Time Password）。

### 国际化

- **UI 文案**：i18n 配置
- **短信模板**：按地区选择模板
- **邮件模板**：HTML 国际化

## 监控告警

### 关键指标

- API 响应时间
- 数据库连接池使用率
- 认证成功/失败率
- 邮件发送成功率
- 短信发送成功率

### 日志聚合

建议集成：
- ELK Stack（Elasticsearch + Logstash + Kibana）
- 或阿里云日志服务（SLS）

## 容灾恢复

### RTO / RPO 目标

- RTO（恢复时间目标）：< 30 分钟
- RPO（恢复点目标）：< 1 小时

### 备份策略

- MySQL 每日备份，保留 7 天
- Keycloak 配置导出周期性备份
- 代码仓库启用 GitHub Actions 自动备份
