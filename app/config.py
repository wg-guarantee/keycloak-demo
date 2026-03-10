"""
config.py – Load all settings from environment variables / .env file.
The application has NO hardcoded secrets.
"""
import os
from dotenv import load_dotenv

load_dotenv()

# ── Keycloak / OIDC ──────────────────────────────────────────────────────────
KEYCLOAK_URL  = os.environ["KEYCLOAK_URL"]          # e.g. http://113.44.80.20:30080
REALM         = os.environ.get("REALM", "demo")
CLIENT_ID     = os.environ["CLIENT_ID"]             # demo-app
# Public PKCE client has no secret; OIDC_CLIENT_SECRET may be empty string
CLIENT_SECRET = os.environ.get("CLIENT_SECRET", "")

# Constructed URLs (never hard-code these; they come from OIDC Discovery anyway)
OIDC_DISCOVERY_URL = (
    f"{KEYCLOAK_URL}/realms/{REALM}/.well-known/openid-configuration"
)

# ── Application ───────────────────────────────────────────────────────────────
APP_BASE_URL   = os.environ.get("APP_BASE_URL", "http://localhost:8000")
SESSION_SECRET = os.environ["SESSION_SECRET"]       # random 32-byte hex string
