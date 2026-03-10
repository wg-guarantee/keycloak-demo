"""
oauth.py – Single place where authlib is configured.
The rest of the application never touches OAuth2 / JWT internals.
"""
from authlib.integrations.starlette_client import OAuth
from app.config import CLIENT_ID, CLIENT_SECRET, OIDC_DISCOVERY_URL

oauth = OAuth()

oauth.register(
    name="keycloak",
    client_id=CLIENT_ID,
    client_secret=CLIENT_SECRET or None,
    server_metadata_url=OIDC_DISCOVERY_URL,
    client_kwargs={
        "scope": "openid email profile",
        # authlib automatically uses PKCE S256 for public clients
        "code_challenge_method": "S256",
    },
)
