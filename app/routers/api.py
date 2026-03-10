"""
routers/api.py – Demo protected JSON API
=========================================
Shows how a backend API endpoint verifies login state.
The session (populated by authlib during login) is the only check needed.
No JWT parsing, no token introspection code here.
"""
from fastapi import APIRouter, Request
from fastapi.responses import JSONResponse

from app.dependencies import require_login

router = APIRouter(prefix="/api", tags=["api"])


@router.get("/me", summary="Return current user's OIDC claims (protected endpoint)")
async def api_me(request: Request):
    """
    Demonstrates calling a protected backend API.
    The Access Token was already validated by authlib during the login callback;
    we simply read the stored user claims from the session.
    """
    redirect = require_login(request)
    if redirect:
        return JSONResponse(
            status_code=401,
            content={"error": "not_authenticated", "detail": "Please login first."},
        )

    user = request.session.get("user", {})
    return JSONResponse(content={
        "sub":            user.get("sub"),
        "preferred_username": user.get("preferred_username"),
        "email":          user.get("email"),
        "email_verified": user.get("email_verified"),
        "name":           user.get("name"),
        "phone_number":   user.get("phoneNumber"),
        "roles":          user.get("realm_access", {}).get("roles", []),
    })


@router.get("/token-info", summary="Show raw token metadata for demo/debugging")
async def api_token_info(request: Request):
    """
    Demo endpoint: shows which tokens the session holds.
    Only exposes non-sensitive metadata (not the raw token strings).
    """
    redirect = require_login(request)
    if redirect:
        return JSONResponse(status_code=401, content={"error": "not_authenticated"})

    token = request.session.get("token", {})
    user  = request.session.get("user", {})
    return JSONResponse(content={
        "has_access_token":  bool(token.get("access_token")),
        "has_refresh_token": bool(token.get("refresh_token")),
        "token_claims": {
            "iss": user.get("iss"),
            "exp": user.get("exp"),
            "iat": user.get("iat"),
        },
    })
