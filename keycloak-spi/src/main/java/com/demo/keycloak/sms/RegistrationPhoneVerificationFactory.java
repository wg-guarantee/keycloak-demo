package com.demo.keycloak.sms;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * Factory for {@link RegistrationPhoneVerification}.
 * Discovered via META-INF/services/org.keycloak.authentication.FormActionFactory.
 */
public class RegistrationPhoneVerificationFactory implements FormActionFactory {

    private static final FormAction SINGLETON =
            new RegistrationPhoneVerification(new TencentSmsService());

    @Override public String getId()          { return RegistrationPhoneVerification.PROVIDER_ID; }
    @Override public String getDisplayType() { return "Phone Number OTP Verification"; }
    @Override public String getReferenceCategory() { return null; }
    @Override public boolean isConfigurable()      { return false; }
    @Override public boolean isUserSetupAllowed()  { return false; }
    @Override public String getHelpText() {
        return "Sends an SMS OTP to verify the phone number during registration.";
    }
    @Override public List<ProviderConfigProperty> getConfigProperties() { return List.of(); }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED,
        };
    }

    @Override public FormAction create(KeycloakSession session) { return SINGLETON; }
    @Override public void init(Config.Scope config) {}
    @Override public void postInit(KeycloakSessionFactory factory) {}
    @Override public void close() {}
}
