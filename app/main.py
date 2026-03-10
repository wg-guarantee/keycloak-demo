"""
main.py – FastAPI application entry point
==========================================
Wires together:
  - SessionMiddleware  (signed cookie via itsdangerous)
  - Jinja2 templates
  - All routers
"""
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from starlette.middleware.sessions import SessionMiddleware

from app.config import SESSION_SECRET
from app.routers import auth, pages, api

app = FastAPI(
    title="Keycloak Authorization Code Flow Demo",
    description="Demonstrates standard OIDC Authorization Code Flow + PKCE with Keycloak 26.",
    version="1.0.0",
)

# Session middleware – all auth state lives in a signed, server-side cookie
# SESSION_SECRET must be a strong random value (min 32 bytes)
app.add_middleware(
    SessionMiddleware,
    secret_key=SESSION_SECRET,
    session_cookie="kcdemo_session",
    max_age=3600,          # 1 hour
    https_only=False,      # Set True in production with HTTPS
    same_site="lax",
)

# Routers
app.include_router(auth.router)
app.include_router(pages.router)
app.include_router(api.router)
