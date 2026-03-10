#!/bin/sh
# entrypoint.sh — wait for Keycloak HTTP, fix sslRequired, run realm setup, start app
set -e

echo "[entrypoint] Waiting for Keycloak HTTP on port 8080..."
MAX_TRIES=40
i=0
while [ $i -lt $MAX_TRIES ]; do
    if python -c "
import urllib.request, sys
try:
    urllib.request.urlopen('http://keycloak:8080/realms/master', timeout=3)
    sys.exit(0)
except Exception:
    sys.exit(1)
" 2>/dev/null; then
        echo "[entrypoint] Keycloak HTTP is ready."
        break
    fi
    i=$((i + 1))
    echo "[entrypoint] Not ready yet ($i/$MAX_TRIES), retrying in 5s..."
    sleep 5
done

if [ $i -ge $MAX_TRIES ]; then
    echo "[entrypoint] ERROR: Keycloak did not become ready in time."
    exit 1
fi

echo "[entrypoint] Initializing Keycloak realm (idempotent)..."
KC_URL="http://keycloak:8080" python /code/setup_realm.py

echo "[entrypoint] Starting FastAPI application..."
exec uvicorn app.main:app --host 0.0.0.0 --port 8000
