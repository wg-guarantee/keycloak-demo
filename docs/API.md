# API 文档

## 基础信息

- **基础 URL**：http://localhost:8080/api
- **默认端口**：8080
- **认证方式**：JWT Bearer Token / OAuth2

## 认证端点

### 1. 用户注册

**POST** `/auth/register`

请求体：
```json
{
  "username": "string",
  "email": "string@example.com",
  "password": "string",
  "phone": "string"
}
```

响应（201）：
```json
{
  "id": 1,
  "username": "string",
  "email": "string@example.com",
  "emailVerified": false,
  "accountActivated": false,
  "createdAt": "2024-03-06T00:00:00Z"
}
```

### 2. 用户登录

**POST** `/auth/login`

请求体：
```json
{
  "username": "string",
  "password": "string"
}
```

响应（200）：
```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "eyJ..."
}
```

### 3. 账户激活

**POST** `/auth/activate`

请求体：
```json
{
  "email": "string@example.com",
  "token": "string"
}
```

响应（200）：
```json
{
  "message": "Account activated successfully",
  "accountActivated": true
}
```

### 4. 修改密码

**POST** `/auth/change-password`

请求头：
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

请求体：
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

响应（200）：
```json
{
  "message": "Password changed successfully"
}
```

## 通知端点

### 5. 发送短信验证码

**POST** `/auth/send-sms`

请求体：
```json
{
  "phone": "string",
  "type": "VERIFICATION|OTP"
}
```

响应（200）：
```json
{
  "message": "SMS sent successfully",
  "expiresIn": 300
}
```

### 6. 发送邮件

**POST** `/auth/send-email`

请求头：
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

请求体：
```json
{
  "templateType": "PASSWORD_RESET|ACTIVATION",
  "recipient": "string@example.com"
}
```

响应（200）：
```json
{
  "message": "Email sent successfully",
  "emailAddress": "string@example.com"
}
```

## 错误响应

### 400 Bad Request

```json
{
  "error": "INVALID_REQUEST",
  "message": "Invalid email format"
}
```

### 409 Conflict

```json
{
  "error": "USER_ALREADY_EXISTS",
  "message": "Username already registered"
}
```

### 401 Unauthorized

```json
{
  "error": "INVALID_CREDENTIALS",
  "message": "Username or password is incorrect"
}
```

### 500 Internal Server Error

```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

## 状态码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 身份验证失败 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 409 | 冲突（如用户已存在） |
| 500 | 服务器错误 |

## 认证流程

### OAuth2 + Keycloak

1. 用户访问应用
2. 跳转到 Keycloak 登录页面
3. Keycloak 验证凭证
4. 返回 Authorization Code
5. 应用后端用 Code 交换 Access Token
6. 使用 Token 访问受保护资源

### JWT Token 格式

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cC...
```

## 速率限制

- 注册：5 次/分钟/IP
- 登录：10 次/分钟/IP
- 发送短信：3 次/分钟/电话号码
- 发送邮件：5 次/小时/邮箱地址

## 更新日志

### v1.0.0 (2024-03-06)
- 初版发布
- 基础认证功能
- 阿里云短信集成
- 阿里云邮件集成
