"""
routers/pages.py – Business pages (dashboard, profile, change-password)
=======================================================================
No auth logic here – only business concerns:
  - Is the user logged in? (checked via require_login dependency)
  - What data do we display?
"""
from fastapi import APIRouter, Request, Depends
from fastapi.responses import RedirectResponse
from fastapi.templating import Jinja2Templates

from app.dependencies import require_login
from app.config import KEYCLOAK_URL, REALM, APP_BASE_URL

router = APIRouter(tags=["pages"])
templates = Jinja2Templates(directory="app/templates")


@router.get("/", summary="Home / landing page")
async def index(request: Request):
    user = request.session.get("user")
    return templates.TemplateResponse("index.html", {
        "request": request,
        "user": user,
    })


@router.get("/dashboard", summary="Protected dashboard (requires login)")
async def dashboard(request: Request, _=Depends(require_login)):
    # require_login returns a RedirectResponse if not logged in;
    # FastAPI will short-circuit the handler in that case.
    redirect = require_login(request)
    if redirect:
        return redirect

    user = request.session["user"]
    return templates.TemplateResponse("dashboard.html", {
        "request": request,
        "user": user,
        # Pass the raw token dict so the template can display it for demo purposes
        "token": request.session.get("token", {}),
    })


@router.get("/profile", summary="User profile – shows OIDC claims")
async def profile(request: Request):
    redirect = require_login(request)
    if redirect:
        return redirect

    user = request.session["user"]
    return templates.TemplateResponse("profile.html", {
        "request": request,
        "user": user,
        # Account console URL for self-service (change password, manage account)
        "account_console_url": f"{KEYCLOAK_URL}/realms/{REALM}/account",
    })


@router.get("/change-password", summary="Redirect to Keycloak account console for password change")
async def change_password(request: Request):
    redirect = require_login(request)
    if redirect:
        return redirect
    # Keycloak Account Console handles password change natively
    return RedirectResponse(
        url=f"{KEYCLOAK_URL}/realms/{REALM}/account/#/security/signingin",
        status_code=302,
    )
