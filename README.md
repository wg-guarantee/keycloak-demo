# Keycloak Authorization Code Flow Demo

标准 OIDC Authorization Code Flow + PKCE 演示项目。

- **Keycloak 26.5.5** 通过 Docker Compose 部署，处理所有身份认证
- **FastAPI (Python 3.9.6)** 通过 Docker Compose 部署，只写业务逻辑，zero auth code
- **SMS OTP（邮件模拟）** 通过 Keycloak 自定义 Java SPI 实现，验证码发送至邮箱

---

## 快速体验

### 访问地址

| 服务 | 地址 |
|------|------|
| **演示应用**（FastAPI） | http://43.161.250.138:8000 |
| **Keycloak 管理控制台** | http://43.161.250.138:8080/admin |

### 关于短信验证码

> **说明：本项目采用发送邮件的方式模拟手机短信验证码。**
>
> 腾讯云短信（SMS）服务的开通需要提交企业营业执照、品牌授权书等一系列证件材料，审核周期较长，暂时无法在演示环境中接入。
>
> 因此，系统在需要发送手机验证码时，会将验证码**以邮件形式**发送到您注册时填写的邮箱，体验效果与短信验证码一致。

**注册 / 登录时获取验证码的方式：**

1. 注册时在手机号字段旁点击「**发送验证码**」，验证码将发送到您填写的邮箱
2. 登录后的二次验证（SMS 2FA）同样会将验证码发送到账号绑定的邮箱
3. 若未配置 SMTP，验证码会打印到 Keycloak 容器日志中，可通过以下命令查看：
   ```bash
   docker compose logs keycloak | grep "\[SMS-MOCK\]"
   ```

---

```
用户浏览器
    │  ① 访问 /dashboard（未登录）
    ▼
FastAPI 应用 (require_login)
    │  ② authlib 重定向到 Keycloak（含 PKCE challenge）
    ▼
Keycloak 登录页
    │  ③ 用户输入账号密码 + 短信 OTP（Mock SPI，OTP 见日志）
    │  ④ 验证通过，回调 /auth/callback?code=xxx
    ▼
FastAPI /auth/callback
    │  ⑤ authlib: code + verifier → Token（库自动完成）
    │  ⑥ authlib: JWKS 验签 ID Token（库自动完成）
    ▼
Session 写入用户信息 → 重定向到 /dashboard ✅
```

---

## 目录结构

```
keycloak-demo/
├── docker-compose.yml            # 一键启动所有服务（PostgreSQL + Keycloak + FastAPI）
├── .env.example                  # 环境变量模板（复制为 .env 后填写）
├── keycloak-spi/                 # 腾讯云 SMS Keycloak SPI (Java)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/demo/keycloak/sms/
│       ├── SmsService.java
│       ├── TencentSmsService.java    ← 腾讯云 SMS 实现
│       ├── SmsOtpAuthenticator.java  ← Keycloak Authenticator
│       └── SmsOtpAuthenticatorFactory.java
├── app/                          # FastAPI 应用（纯业务，零 auth 代码）
│   ├── main.py
│   ├── oauth.py                  ← 唯一的 OAuth2 配置（5 行）
│   ├── config.py
│   ├── dependencies.py
│   ├── routers/
│   │   ├── auth.py               ← login/callback/logout（各 1 行 authlib 调用）
│   │   ├── pages.py              ← 业务页面
│   │   └── api.py                ← 演示受保护 JSON API
│   ├── templates/
│   │   ├── base.html
│   │   ├── index.html
│   │   ├── dashboard.html
│   │   └── profile.html
│   ├── requirements.txt
│   └── .env.example
├── scripts/
│   └── setup_realm.py            ← 一键配置 Keycloak Realm
└── README.md
```

---

## 前置条件

| 工具 | 版本要求 |
|------|---------|
| Docker | 24+（含 docker compose v2） |

> JDK / Maven 和 Python **无需在宿主机安装**。
> Keycloak SPI JAR 在 `keycloak-spi/Dockerfile` 的 Maven 构建阶段自动编译；
> Realm 初始化脚本 `setup_realm.py` 由 `app` 容器在启动时自动执行。

---

## Step 1：配置环境变量

```bash
# 复制模板
cp .env.example .env
```

编辑 `.env`，**必填项**：

```bash
# 服务器公网 IP 或域名（浏览器和 JWT Issuer 使用）
KEYCLOAK_HOST=43.161.250.138

# Session 签名密钥 —— 随机生成，勿复用
SESSION_SECRET=$(python3 -c "import secrets; print(secrets.token_hex(32))")

# OIDC 回调地址（在服务器上访问时改为公网地址）
APP_BASE_URL=http://43.161.250.138:8000
```

**可选：SMTP 邮件**（邮箱激活 / 忘记密码）

> `SMTP_HOST` 和 `SMTP_USER` 必须**同时填写**，否则注册时 Keycloak 会因发件地址为空而报错。
> 留空则关闭邮件功能，`verifyEmail` 自动设为 `false`。

```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=yourGmail@gmail.com           # 必须与 SMTP_HOST 同时填
SMTP_PASSWORD=xxxx-xxxx-xxxx-xxxx       # Gmail 应用专用密码（16 位，无空格）
SMTP_FROM=yourGmail@gmail.com           # 必须与 SMTP_HOST 同时填
```

其余字段（`POSTGRES_PASSWORD`、`KC_ADMIN_PASSWORD` 等）已有合理默认值，可按需修改。

---

## Step 3：启动 Keycloak（含 PostgreSQL）

```bash
# 仅启动基础设施（Keycloak 首次启动约需 60-90 秒）
docker compose up -d postgres keycloak

# 查看启动日志
docker compose logs -f keycloak
```

等待日志出现 `Keycloak 26.5.5 on JVM (powered by Quarkus ...) started` 后访问：

```
管理控制台：http://43.161.250.138:8080/admin
账号：admin  密码：.env 中的 KC_ADMIN_PASSWORD
```

---

## Step 4：配置 Keycloak Realm

```bash
# 安装脚本依赖
pip install python-keycloak

# 设置 Keycloak 地址（与 .env 中的 KEYCLOAK_HOST 一致）
export KC_URL="http://43.161.250.138:8080"
export KC_ADMIN_PASSWORD="$(grep KC_ADMIN_PASSWORD .env | cut -d= -f2)"

# 可选：配置 SMTP（如不填写则跳过邮件功能）
export SMTP_HOST="smtp.example.com"
export SMTP_PORT="587"
export SMTP_USER="noreply@example.com"
export SMTP_PASSWORD="your_smtp_password"
export SMTP_FROM="noreply@example.com"

# 运行配置脚本（幂等，可重复运行）
python scripts/setup_realm.py
```

脚本自动完成：

- ✅ 创建 `demo` Realm（开启注册 + 邮箱验证）
- ✅ 创建 `demo-app` OIDC 客户端（Public，PKCE S256）
- ✅ 添加 `phoneNumber` 用户属性
- ✅ 创建 `demo-browser-sms` 认证流（browser flow + SMS OTP）
- ✅ 将新流绑定为 Realm 默认 browser flow
- ✅ 配置 SMTP（如填写）

---

## Step 5：启动 FastAPI 应用

```bash
# 启动 App 容器
docker compose up -d app

# 查看日志
docker compose logs -f app
```

访问 [http://localhost:8000](http://localhost:8000)

**或者一次性启动所有服务：**

```bash
# 先构建 JAR，再一键启动全部
cd keycloak-spi && mvn clean package -DskipTests && cd ..
docker compose up -d
```

---

## 常用命令

```bash
# 查看所有服务状态
docker compose ps

# 查看 Keycloak 日志
docker compose logs -f keycloak

# 查看 App 日志
docker compose logs -f app

# 停止所有服务（保留数据卷）
docker compose down

# 停止并清除所有数据（含 PostgreSQL 数据）
docker compose down -v

# 重新构建 App 镜像（代码修改后）
docker compose build app && docker compose up -d app

# 重新构建 Keycloak 镜像（SPI 修改后）
cd keycloak-spi && mvn clean package -DskipTests && cd ..
docker compose build keycloak && docker compose up -d keycloak

# 手动重新执行 Realm 初始化（脚本修改后，或初始化失败时；幂等，可重复运行）
# 方式一：一键脚本（推荐）—— 自动修复 sslRequired + 同步脚本 + 执行初始化
bash scripts/init-keycloak.sh

# 方式二：逐步手动执行
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master \
  --user admin --password <KC_ADMIN_PASSWORD>
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=none
docker compose cp scripts/setup_realm.py app:/code/setup_realm.py
docker compose exec app python /code/setup_realm.py
```

---

## Step 5：演示场景操作

### 场景 1：标准登录（Authorization Code Flow + PKCE）

1. 访问 `http://localhost:8000` → 点击 **"点击登录/注册"**
2. 浏览器跳转到 Keycloak 登录页
3. 输入已有账号密码
4. 短信 OTP 验证页出现 → 查收邮箱中的 6 位验证码邮件并输入
5. 验证成功 → 自动跳回 Dashboard
6. Dashboard 显示用户信息 + Token 状态

### 场景 2：注册新账号

1. Keycloak 登录页 → 点击 **"Register"**
2. 填写：用户名、邮箱、密码、手机号（E.164 格式，如 `+8613800138000`）
3. 点击手机号旁的「发送验证码」→ 验证码将发送至所填邮箱
4. 输入邮件中的 6 位验证码完成注册
5. 若配置了 SMTP：Keycloak 自动发送**邮箱激活**邮件
6. 点击邮件中链接激活，邮箱变为 ✓ 已验证

### 场景 3：邮箱激活

- 注册完成后 Keycloak 自动发送（需配置 SMTP）
- 也可登录后访问 `/profile` 点击"去激活邮箱"跳转到 Account Console

### 场景 4：验证码（邮件模拟）

- 每次登录都会触发 SMS OTP（因为在 browser flow 中设置为 REQUIRED）
- 验证码由系统**以邮件形式**发送到账号绑定的邮箱（模拟短信；腾讯云 SMS 因证件审核暂未接入）
- 若未配置 SMTP，可通过 `docker compose logs keycloak | grep "[SMS-MOCK]"` 在日志中查看验证码

### 场景 5：修改密码

1. 已登录 → 访问 `http://localhost:8000/change-password`
2. 自动跳转到 Keycloak Account Console 的密码修改页
3. 按要求修改（长度≥8，含大写+数字）
4. 保存后可用新密码登录

### 演示受保护 API

```bash
# 先在浏览器登录，然后：
curl http://localhost:8000/api/me
# → {"sub":"...","preferred_username":"...","email":"..."}

curl http://localhost:8000/api/token-info
# → {"has_access_token":true,"has_refresh_token":true,...}

# 未登录时
curl http://localhost:8000/api/me
# → {"error":"not_authenticated","detail":"Please login first."}
```

---

## 代码架构说明

### 应用层零 auth 代码

```python
# routers/auth.py  – 整个登录流程就这 3 个路由

@router.get("/login")
async def login(request: Request):
    return await oauth.keycloak.authorize_redirect(request, callback_url)
    # ↑ authlib 自动处理：PKCE、state、redirect_uri

@router.get("/callback")
async def callback(request: Request):
    token = await oauth.keycloak.authorize_access_token(request)
    # ↑ authlib 自动处理：state 验证、code 换 token、JWKS 验签
    request.session["user"] = token.get("userinfo")

@router.get("/logout")
async def logout(request: Request):
    request.session.clear()
    return RedirectResponse(url=keycloak_end_session_url)
```

### 业务保护

```python
# dependencies.py – 只关心"有没有登录"
def require_login(request: Request):
    if not request.session.get("user"):
        return RedirectResponse(url="/auth/login")
    return None
```

### 唯一 OAuth2 配置

```python
# oauth.py – 唯一涉及 OAuth2 的文件
oauth = OAuth()
oauth.register(
    name="keycloak",
    client_id=CLIENT_ID,
    server_metadata_url=OIDC_DISCOVERY_URL,  # 自动发现所有端点
    client_kwargs={"scope": "openid email profile", "code_challenge_method": "S256"},
)
```

---

## 安全说明

| 安全点 | 实现方式 |
|--------|---------|
| PKCE S256 | authlib 自动生成 code_verifier/challenge，防止 code 截获 |
| CSRF (state) | authlib 自动生成/验证 state 参数 |
| JWT 验签 | authlib 通过 Keycloak JWKS 端点验证签名，防 token 伪造 |
| Session 签名 | itsdangerous HMAC，防 session 篡改 |
| 凭据隔离 | 所有密钥通过环境变量（.env 文件）注入，代码里无硬编码 |
| SMS OTP | 6位随机数 + SecureRandom，存入 Keycloak AuthSession（内存，不落库） |

---

## 常见问题

**Q: 启动时报 `KeyError: 'KEYCLOAK_URL'`**  
A: 检查 `app/.env` 是否存在，并填写了所有必填项。

**Q: Keycloak 容器启动失败，日志报 `Failed to connect to PostgreSQL`**  
A: 等待 PostgreSQL 容器健康检查通过后 Keycloak 会自动重试，可通过 `docker compose ps` 查看各服务状态。

**Q: 登录后回调报 `mismatching_state`**  
A: Session 中间件签名密钥 `SESSION_SECRET` 不能包含特殊字符导致 Cookie 解析失败，建议用 `secrets.token_hex(32)` 生成。

**Q: 短信收不到验证码**  
A: 检查 Keycloak 容器日志：`docker compose logs keycloak | grep -i sms`。确认腾讯云 SMS 配置、签名/模板状态为"审核通过"，以及手机号格式为 E.164（`+86`开头）。

**Q: SPI 不生效，Keycloak 看不到 "SMS OTP (Tencent Cloud)" authenticator**  
A: 确认 JAR 正确打包（`mvn package` 有无报错），并且在 `up` 前已执行 `mvn clean package -DskipTests`。可以进容器检查：`docker compose exec keycloak ls /opt/keycloak/providers/`。

**Q: 登录报 `HTTPS required` 或 `sslRequired` 错误**  
A: PostgreSQL 数据库中旧数据可能残留 `sslRequired=EXTERNAL`。执行以下命令通过 `kcadm.sh` 重置：

```bash
# Step 1：登录 kcadm（用 Keycloak 管理员账号）
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password ************

# Step 2：将 master realm 的 sslRequired 改为 none
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh update realms/master \
  -s sslRequired=none
```

> 如果 `demo` realm 也报同样错误，重复 Step 2 并改为 `realms/demo`。
> 正常情况下，`app` 容器启动时 `setup_realm.py` 会自动完成此操作；该命令适用于手动排查场景。
