#!/usr/bin/env python3
"""
setup_realm.py
==============
One-shot script to configure the Keycloak `demo` realm via Admin REST API.

Requires:
    pip install python-keycloak

Usage:
    # Export admin password (never pass credentials as CLI args)
    export KC_ADMIN_PASSWORD="Admin@Kc2026!"
    export KC_URL="http://43.161.250.138:30080"

    # Optional: SMTP settings for email verification
    export SMTP_HOST="smtp.example.com"
    export SMTP_PORT="587"
    export SMTP_USER="noreply@example.com"
    export SMTP_PASSWORD="smtp_secret"
    export SMTP_FROM="noreply@example.com"

    python scripts/setup_realm.py
"""

import os
import sys
import requests

try:
    from keycloak import KeycloakAdmin, KeycloakOpenIDConnection
except ImportError:
    sys.exit("python-keycloak not installed. Run: pip install python-keycloak")


# ─── configuration ────────────────────────────────────────────────────────────

KC_URL            = os.environ.get("KC_URL", "http://43.161.250.138:8080")
KC_ADMIN_USER     = os.environ.get("KC_ADMIN_USER", "admin")
KC_ADMIN_PASSWORD = os.environ.get("KC_ADMIN_PASSWORD")
REALM_NAME        = "demo"
CLIENT_ID         = "demo-app"
# The FastAPI app base URL (must match APP_BASE_URL in .env)
APP_BASE_URL      = os.environ.get("APP_BASE_URL", "http://localhost:8000")

SMTP_HOST     = os.environ.get("SMTP_HOST", "")
SMTP_PORT     = os.environ.get("SMTP_PORT", "587")
SMTP_USER     = os.environ.get("SMTP_USER", "")
SMTP_PASSWORD = os.environ.get("SMTP_PASSWORD", "")
SMTP_FROM     = os.environ.get("SMTP_FROM", "noreply@demo.local")


def main():
    if not KC_ADMIN_PASSWORD:
        sys.exit("ERROR: set KC_ADMIN_PASSWORD environment variable")

    print(f"Connecting to Keycloak at {KC_URL} ...")
    connection = KeycloakOpenIDConnection(
        server_url=KC_URL,
        username=KC_ADMIN_USER,
        password=KC_ADMIN_PASSWORD,
        realm_name="master",
        verify=False,
    )
    admin = KeycloakAdmin(connection=connection)

    # ── 0. Disable SSL requirement on master realm (dev / HTTP environment) ──
    admin.update_realm("master", {"sslRequired": "none"})
    print("✓ master realm sslRequired set to none.")

    # ── 1. Create / update realm ─────────────────────────────────────────────
    # SMTP is only enabled when both host AND user are provided;
    # otherwise Keycloak would store from=null and fail with EmailException.
    smtp_ready = bool(SMTP_HOST and SMTP_USER)
    smtp_config = {}
    if smtp_ready:
        smtp_config = {
            "host": SMTP_HOST,
            "port": SMTP_PORT,
            "user": SMTP_USER,
            "password": SMTP_PASSWORD,
            "from": SMTP_FROM,
            "fromDisplayName": "Keycloak Demo",
            "ssl": "false",
            "starttls": "true",
            "auth": "true",
        }

    existing_realms = [r["realm"] for r in admin.get_realms()]
    if REALM_NAME not in existing_realms:
        admin.create_realm({
            "realm": REALM_NAME,
            "displayName": "Demo Realm",
            "enabled": True,
            "sslRequired": "none",
            "registrationAllowed": True,
            "registrationEmailAsUsername": False,
            "verifyEmail": smtp_ready,
            "passwordPolicy": "length(8) and upperCase(1) and digits(1)",
            "loginWithEmailAllowed": True,
            "duplicateEmailsAllowed": False,
            "resetPasswordAllowed": True,
            "rememberMe": True,
            "internationalizationEnabled": True,
            "supportedLocales": ["en", "zh-CN"],
            "defaultLocale": "zh-CN",
            "loginTheme": "sms-otp",
            "smtpServer": smtp_config,
        })
        print(f"✓ Realm '{REALM_NAME}' created.")

    # Always sync loginTheme + verifyEmail + smtpServer so re-runs pick up changes
    admin.update_realm(REALM_NAME, {
        "loginTheme": "sms-otp",
        "verifyEmail": smtp_ready,
        "smtpServer": smtp_config,
    })
    print(f"✓ Realm '{REALM_NAME}' loginTheme=sms-otp, smtp/verifyEmail updated (smtp_ready={smtp_ready}).")

    # Switch to demo realm
    admin.connection.realm_name = REALM_NAME

    # ── 2. Create OIDC client ─────────────────────────────────────────────────
    clients = admin.get_clients()
    existing_client_ids = [c["clientId"] for c in clients]

    redirect_uris = list({f"{APP_BASE_URL}/auth/callback", "http://localhost:8000/auth/callback"})
    web_origins   = list({APP_BASE_URL, "http://localhost:8000"})

    if CLIENT_ID in existing_client_ids:
        client_obj = next(c for c in clients if c["clientId"] == CLIENT_ID)
        admin.update_client(client_obj["id"], {
            "redirectUris": redirect_uris,
            "webOrigins":   web_origins,
        })
        print(f"✓ Client '{CLIENT_ID}' already exists – redirectUris/webOrigins updated.")
    else:
        admin.create_client({
            "clientId": CLIENT_ID,
            "name": "Demo Application",
            "enabled": True,
            "protocol": "openid-connect",
            "publicClient": True,           # PKCE public client – no client_secret
            "standardFlowEnabled": True,    # Authorization Code Flow
            "implicitFlowEnabled": False,
            "directAccessGrantsEnabled": False,
            "serviceAccountsEnabled": False,
            "redirectUris": redirect_uris,
            "webOrigins":   web_origins,
            "attributes": {
                "pkce.code.challenge.method": "S256",  # Enforce PKCE S256
            },
        })
        print(f"✓ Client '{CLIENT_ID}' created (public, PKCE S256).")

    # ── 2.5 Ensure phoneNumber protocol mapper on client ─────────────────────
    # Maps user attribute "phoneNumber" → userinfo/token claim "phoneNumber"
    client_obj = next(c for c in admin.get_clients() if c["clientId"] == CLIENT_ID)
    client_id_internal = client_obj["id"]
    existing_mappers = admin.get_mappers_from_client(client_id_internal)
    mapper_names = [m["name"] for m in existing_mappers]
    if "phoneNumber" not in mapper_names:
        admin.add_mapper_to_client(client_id_internal, {
            "name": "phoneNumber",
            "protocol": "openid-connect",
            "protocolMapper": "oidc-usermodel-attribute-mapper",
            "consentRequired": False,
            "config": {
                "user.attribute": "phoneNumber",
                "claim.name": "phoneNumber",
                "jsonType.label": "String",
                "id.token.claim": "true",
                "access.token.claim": "true",
                "userinfo.token.claim": "true",
                "multivalued": "false",
                "aggregate.attrs": "false",
            },
        })
        print("✓ Protocol mapper 'phoneNumber' added to client.")
    else:
        print("  Protocol mapper 'phoneNumber' already exists – skipping.")

    # ── 3. Configure user profile attributes ─────────────────────────────────
    # python-keycloak may not expose get_user_profile; use REST API directly.
    try:
        token = admin.connection.token["access_token"]
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
        }
        profile_url = f"{KC_URL}/admin/realms/{REALM_NAME}/users/profile"

        resp = requests.get(profile_url, headers=headers, verify=False)
        resp.raise_for_status()
        profile = resp.json()

        phone_attr = {
            "name": "phoneNumber",
            "displayName": "${phoneNumber}",
            "validations": {
                "pattern": {"pattern": r"^\+?[1-9]\d{7,14}$"}
            },
            "annotations": {},
            "permissions": {
                "view": ["admin", "user"],
                "edit": ["admin", "user"],
            },
            "required": {
                "roles": ["user"],
                "scopes": [],
            },
            "multivalued": False,
        }

        updated_attrs = []
        phone_found = False
        for attr in profile.get("attributes", []):
            name = attr.get("name", "")
            if name in ("firstName", "lastName"):
                # Omit `required` key entirely — empty roles/scopes still means required in KC
                updated_attrs.append({k: v for k, v in attr.items() if k != "required"})
            elif name == "phoneNumber":
                updated_attrs.append(phone_attr)
                phone_found = True
            else:
                updated_attrs.append(attr)

        if not phone_found:
            updated_attrs.append(phone_attr)

        profile["attributes"] = updated_attrs
        put_resp = requests.put(profile_url, json=profile, headers=headers, verify=False)
        put_resp.raise_for_status()
        print("✓ User profile: phoneNumber=required, firstName/lastName=optional.")
    except Exception as e:
        print(f"  Warning: could not update user profile: {e}")

    # ── 4. Create SMS OTP authentication flow ─────────────────────────────────
    # Structure:
    #   demo-browser-sms  (top-level, copy of 'browser')
    #     ├── Cookie                        ALTERNATIVE
    #     ├── Identity Provider Redirector  ALTERNATIVE
    #     └── demo-browser-sms forms        ALTERNATIVE (sub-flow, copy of 'browser forms')
    #           ├── Username Password Form  REQUIRED
    #           └── SMS OTP Authenticator   REQUIRED  ← we add this
    flows = admin.get_authentication_flows()
    existing_flow_aliases = [f["alias"] for f in flows]

    if "demo-browser-sms" in existing_flow_aliases:
        print("  SMS auth flow already exists – skipping.")
    else:
        # Copy the built-in 'browser' flow as a base
        admin.copy_authentication_flow(
            {"newName": "demo-browser-sms"},
            "browser",
        )
        print("✓ Copied 'browser' flow to 'demo-browser-sms'.")

        # Find the "forms" sub-flow inside the copied flow (alias: "demo-browser-sms forms")
        executions = admin.get_authentication_flow_executions("demo-browser-sms")

        # The copied forms sub-flow has displayName / alias containing "forms"
        forms_exec = next(
            (e for e in executions if "forms" in (e.get("displayName") or "").lower()
             or "forms" in (e.get("alias") or "").lower()),
            None,
        )

        if not forms_exec:
            # Fallback: find by flowAlias pattern
            forms_exec = next(
                (e for e in executions if e.get("flowId") and
                 "form" in str(e).lower()),
                None,
            )

        if not forms_exec:
            print("  WARNING: could not find forms sub-flow; SMS OTP may not work correctly.")
        else:
            forms_alias = forms_exec.get("displayName") or forms_exec.get("alias") or "demo-browser-sms forms"
            print(f"  Found forms sub-flow: '{forms_alias}'")

            # Add SMS OTP execution inside the forms sub-flow
            admin.create_authentication_flow_execution(
                {"provider": "sms-otp-authenticator"},
                forms_alias,
            )
            print("✓ SMS OTP execution added inside forms sub-flow.")

            # Set it to REQUIRED
            executions = admin.get_authentication_flow_executions("demo-browser-sms")
            sms_exec = next(
                (e for e in executions if e.get("providerId") == "sms-otp-authenticator"),
                None,
            )
            if sms_exec:
                admin.update_authentication_flow_executions(
                    {
                        "id": sms_exec["id"],
                        "requirement": "REQUIRED",
                        "priority": sms_exec.get("priority", 30),
                    },
                    "demo-browser-sms",
                )
                print("✓ SMS OTP execution set to REQUIRED.")

        # Bind the new flow as the realm's browser flow
        realm_rep = admin.get_realm(REALM_NAME)
        realm_rep["browserFlow"] = "demo-browser-sms"
        admin.update_realm(REALM_NAME, realm_rep)
        print("✓ 'demo-browser-sms' set as realm browser flow.")

    # ── 4.5. Add phone OTP FormAction to registration form ───────────────────
    # Cannot modify built-in "registration" flow directly.
    # Strategy (same as browser flow):
    #   1. Copy built-in "registration" → "demo-registration-phone"
    #   2. Add FormAction to the "registration form" sub-flow of the copy
    #   3. Bind the copy as the realm's registration flow
    try:
        token = admin.connection.token["access_token"]
        headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

        CUSTOM_REG_FLOW = "demo-registration-phone"

        flows_resp = requests.get(
            f"{KC_URL}/admin/realms/{REALM_NAME}/authentication/flows",
            headers=headers, verify=False
        ).json()
        existing_aliases = [f["alias"] for f in flows_resp]

        if CUSTOM_REG_FLOW not in existing_aliases:
            # Step 1: copy built-in "registration" flow
            copy_resp = requests.post(
                f"{KC_URL}/admin/realms/{REALM_NAME}/authentication/flows/registration/copy",
                json={"newName": CUSTOM_REG_FLOW},
                headers=headers, verify=False
            )
            print(f"  Copy registration flow response: {copy_resp.status_code}")
            copy_resp.raise_for_status()
            print(f"✓ Copied 'registration' flow to '{CUSTOM_REG_FLOW}'.")

        # Step 2: find the "registration form" sub-flow in our copy
        reg_execs_url = (f"{KC_URL}/admin/realms/{REALM_NAME}"
                         f"/authentication/flows/{CUSTOM_REG_FLOW}/executions")
        reg_execs = requests.get(reg_execs_url, headers=headers, verify=False).json()

        phone_exec = next(
            (e for e in reg_execs if e.get("providerId") == "registration-phone-verification"),
            None,
        )

        if not phone_exec:
            sub_flows = [e for e in reg_execs if e.get("authenticationFlow")]
            print(f"  Sub-flows in {CUSTOM_REG_FLOW}: {[(e.get('displayName'), e.get('flowId')) for e in sub_flows]}")
            form_flow_exec = next(
                (e for e in sub_flows
                 if "form" in (e.get("alias") or e.get("displayName") or "").lower()),
                None,
            )
            if not form_flow_exec:
                raise RuntimeError(f"Cannot find 'registration form' sub-flow in {CUSTOM_REG_FLOW}")

            # Get the sub-flow's alias by its ID
            flow_id = form_flow_exec["flowId"]
            flow_detail = requests.get(
                f"{KC_URL}/admin/realms/{REALM_NAME}/authentication/flows/{flow_id}",
                headers=headers, verify=False
            ).json()
            form_alias = flow_detail.get("alias")
            print(f"  Registration form sub-flow alias: '{form_alias}'")

            add_url = (f"{KC_URL}/admin/realms/{REALM_NAME}"
                       f"/authentication/flows/{form_alias}/executions/execution")
            resp = requests.post(add_url, json={"provider": "registration-phone-verification"},
                                 headers=headers, verify=False)
            print(f"  Add execution response: {resp.status_code} {resp.text[:300]}")
            resp.raise_for_status()

            # Re-fetch to get the new execution
            reg_execs = requests.get(reg_execs_url, headers=headers, verify=False).json()
            phone_exec = next(
                (e for e in reg_execs if e.get("providerId") == "registration-phone-verification"),
                None,
            )
            if not phone_exec:
                raise RuntimeError("Could not find phone-verification execution after adding")
            print("✓ Phone OTP FormAction added.")

        # Step 3: always enforce REQUIRED and raise priority
        if phone_exec.get("requirement") != "REQUIRED":
            phone_exec["requirement"] = "REQUIRED"
            requests.put(reg_execs_url, json=phone_exec, headers=headers, verify=False)
            print("✓ Phone OTP FormAction set to REQUIRED.")

        raise_url = (f"{KC_URL}/admin/realms/{REALM_NAME}"
                     f"/authentication/executions/{phone_exec['id']}/raise-priority")
        for _ in range(10):
            requests.post(raise_url, headers=headers, verify=False)
        print("✓ Phone OTP FormAction raised to highest priority.")

        # Step 4: bind as realm registration flow (idempotent)
        realm_info = requests.get(
            f"{KC_URL}/admin/realms/{REALM_NAME}", headers=headers, verify=False
        ).json()
        if realm_info.get("registrationFlow") != CUSTOM_REG_FLOW:
            requests.put(
                f"{KC_URL}/admin/realms/{REALM_NAME}",
                json={"registrationFlow": CUSTOM_REG_FLOW},
                headers=headers, verify=False
            ).raise_for_status()
            print(f"✓ '{CUSTOM_REG_FLOW}' set as realm registration flow.")
        else:
            print(f"  '{CUSTOM_REG_FLOW}' already set as realm registration flow.")

    except Exception as e:
        print(f"  Warning: could not update registration form: {e}")

    # ── 5. Print summary ──────────────────────────────────────────────────────
    print()
    print("=" * 60)
    print(f"  Realm setup complete!")
    print(f"  Admin console : {KC_URL}/admin/master/console")
    print(f"  Realm console : {KC_URL}/realms/{REALM_NAME}/account")
    print(f"  OIDC Discovery: {KC_URL}/realms/{REALM_NAME}/.well-known/openid-configuration")
    print(f"  Client ID     : {CLIENT_ID}")
    print("=" * 60)


if __name__ == "__main__":
    main()
