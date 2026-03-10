#!/bin/sh
# docker-kc-entrypoint.sh
# Starts Keycloak start-dev, waits until HTTP is up,
# then sets sslRequired=none on master realm via kcadm.sh.

echo "[kc-init] Starting Keycloak start-dev..."
/opt/keycloak/bin/kc.sh start-dev &
KC_PID=$!

echo "[kc-init] Waiting for Keycloak HTTP on port 8080 (max 120s)..."
TRIES=0
while [ "$TRIES" -lt 40 ]; do
    if wget -q -O /dev/null http://localhost:8080/realms/master 2>/dev/null; then
        echo "[kc-init] Keycloak HTTP is ready."
        break
    fi
    TRIES=$((TRIES + 1))
    sleep 3
done

if [ "$TRIES" -ge 40 ]; then
    echo "[kc-init] WARNING: Keycloak did not become ready in time, proceeding anyway."
fi

echo "[kc-init] Setting master realm sslRequired=none..."
/opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user "${KC_BOOTSTRAP_ADMIN_USERNAME:-admin}" \
    --password "${KC_BOOTSTRAP_ADMIN_PASSWORD}"
RET=$?
if [ $RET -eq 0 ]; then
    /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=none
    echo "[kc-init] sslRequired=none applied."
else
    echo "[kc-init] WARNING: kcadm login failed (code $RET), skipping sslRequired fix."
fi

# Write sentinel file so app container knows setup can proceed
touch /tmp/kc-ready
echo "[kc-init] Sentinel /tmp/kc-ready written. Waiting for Keycloak process..."

wait "$KC_PID"
