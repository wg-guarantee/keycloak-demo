"""
dependencies.py – Shared FastAPI dependencies.
Only concern: is the user logged in? No token parsing, no JWT logic.
"""
from fastapi import Request
from fastapi.responses import RedirectResponse


def require_login(request: Request):
    """
    Dependency that redirects to /auth/login if no active session exists.
    Stores the original URL so the user lands back after login.
    """
    if not request.session.get("user"):
        # Save the intended destination
        request.session["next"] = str(request.url)
        return RedirectResponse(url="/auth/login", status_code=302)
    return None
