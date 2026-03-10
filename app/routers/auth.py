"""
routers/auth.py – Login / Callback / Logout
============================================
Each route contains exactly ONE authlib call.
No PKCE generation, no token parsing, no JWT verification here – the library
handles all of that automatically.
"""
from fastapi import APIRouter, Request
from fastapi.responses import RedirectResponse
from authlib.integrations.base_client.errors import MismatchingStateError, OAuthError

from app.oauth import oauth
from app.config import KEYCLOAK_URL, REALM, APP_BASE_URL

router = APIRouter(prefix="/auth", tags=["auth"])


@router.get("/login", summary="Redirect browser to Keycloak login page")
async def login(request: Request):
    """
    Step ①②: Redirect the user to Keycloak.
    authlib auto-generates the PKCE code_verifier + code_challenge (S256)
    and stores them in the session for the callback step.
    """
    callback_url = f"{APP_BASE_URL}/auth/callback"
    return await oauth.keycloak.authorize_redirect(request, callback_url)


@router.get("/callback", summary="Keycloak redirects back here with Authorization Code")
async def callback(request: Request):
    """
    Step ③④⑤: Exchange the Authorization Code for tokens.
    authlib handles:
      - Extracting `code` + `state` from query params
      - Verifying `state` (CSRF protection)
      - Sending the code + code_verifier to Keycloak's token endpoint
      - Verifying the ID Token signature via Keycloak's JWKS
      - Parsing the token claims
    We just store the resulting user info in the session.
    """
    try:
        token = await oauth.keycloak.authorize_access_token(request)
    except MismatchingStateError:
        # State mismatch = session cookie lost (e.g. APP_BASE_URL mismatch,
        # manually copy-pasted URL, or cross-origin cookie issue).
        # Restart the login flow cleanly instead of showing 500.
        return RedirectResponse(url="/auth/login", status_code=302)
    except OAuthError:
        # Authorization code expired / reused — restart login
        return RedirectResponse(url="/auth/login", status_code=302)
    # `token` contains: access_token, id_token, refresh_token, userinfo dict
    request.session["user"]  = token.get("userinfo")
    request.session["token"] = {
        "access_token":  token.get("access_token"),
        "refresh_token": token.get("refresh_token"),
    }
    next_url = request.session.pop("next", "/dashboard")
    return RedirectResponse(url=next_url, status_code=302)


@router.get("/logout", summary="Clear local session and sign out from Keycloak")
async def logout(request: Request):
    """
    Clear the local session and redirect to Keycloak's end_session endpoint
    so the SSO session is also terminated.
    """
    id_token_hint = None
    # If we stored the raw id_token we could pass it as a hint; omit if absent
    request.session.clear()

    end_session_url = (
        f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/logout"
        f"?post_logout_redirect_uri={APP_BASE_URL}"
    )
    return RedirectResponse(url=end_session_url, status_code=302)
