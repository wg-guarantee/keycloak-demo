#!/bin/bash
# =============================================================================
# init-keycloak.sh  —  手动初始化 / 重新初始化 Keycloak Realm
#
# 适用场景：
#   • 首次部署后 app 容器自动初始化失败，需手动补跑
#   • 修改了 setup_realm.py 后，将最新脚本同步到 app 容器并重新执行
#   • 数据库残留旧数据导致 sslRequired=EXTERNAL 错误，需手动重置
#
# 用法（在项目根目录执行）：
#   bash scripts/init-keycloak.sh
# =============================================================================

set -eu

# ── 读取 .env 中的管理员密码 ────────────────────────────────────────────────
if [ ! -f .env ]; then
    echo "ERROR: .env not found. Run: cp .env.example .env"
    exit 1
fi

KC_ADMIN_USERNAME=$(grep '^KC_ADMIN_USERNAME=' .env | cut -d= -f2 | tr -d '[:space:]')
KC_ADMIN_PASSWORD=$(grep '^KC_ADMIN_PASSWORD=' .env | cut -d= -f2 | tr -d '[:space:]')
KC_ADMIN_USERNAME=${KC_ADMIN_USERNAME:-admin}

if [ -z "$KC_ADMIN_PASSWORD" ]; then
    echo "ERROR: KC_ADMIN_PASSWORD is empty in .env"
    exit 1
fi

echo "==> [1/3] Fixing sslRequired via kcadm.sh ..."
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user "$KC_ADMIN_USERNAME" \
    --password "$KC_ADMIN_PASSWORD"

docker compose exec keycloak /opt/keycloak/bin/kcadm.sh update realms/master \
    -s sslRequired=none

echo "    ✓ master realm sslRequired set to none"

echo "==> [2/3] Syncing setup_realm.py to app container ..."
docker compose cp scripts/setup_realm.py app:/code/setup_realm.py
echo "    ✓ setup_realm.py synced"

echo "==> [3/3] Running setup_realm.py ..."
# Pass SMTP vars explicitly so they override whatever is in the running container's env
SMTP_HOST=$(grep '^SMTP_HOST=' .env | cut -d= -f2 | tr -d '[:space:]')
SMTP_PORT=$(grep '^SMTP_PORT=' .env | cut -d= -f2 | tr -d '[:space:]')
SMTP_USER=$(grep '^SMTP_USER=' .env | cut -d= -f2 | tr -d '[:space:]')
SMTP_PASSWORD=$(grep '^SMTP_PASSWORD=' .env | cut -d= -f2 | tr -d '[:space:]')
SMTP_FROM=$(grep '^SMTP_FROM=' .env | cut -d= -f2 | tr -d '[:space:]')

docker compose exec \
    -e SMTP_HOST="$SMTP_HOST" \
    -e SMTP_PORT="${SMTP_PORT:-587}" \
    -e SMTP_USER="$SMTP_USER" \
    -e SMTP_PASSWORD="$SMTP_PASSWORD" \
    -e SMTP_FROM="$SMTP_FROM" \
    app python /code/setup_realm.py

echo ""
echo "✅  Keycloak initialization complete."
