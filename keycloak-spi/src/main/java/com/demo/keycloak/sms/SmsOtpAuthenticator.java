package com.demo.keycloak.sms;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.jboss.logging.Logger;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.security.SecureRandom;

/**
 * Keycloak Authenticator extension that:
 *  1. On first call  → generates a 6-digit OTP, stores it in the auth session,
 *                      sends it via Tencent Cloud SMS, renders the OTP input form.
 *  2. On form submit → compares the submitted code against the stored OTP.
 *
 * The phone number is read from the user attribute "phoneNumber".
 * The FreeMarker template is "sms-otp-form.ftl" (deployed alongside the JAR).
 */
public class SmsOtpAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(SmsOtpAuthenticator.class);
    private static final String AUTH_NOTE_CODE = "sms_otp_code";
    private static final String FORM_FIELD_CODE = "sms_otp_code";
    private static final String PHONE_ATTR = "phoneNumber";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsService smsService;

    public SmsOtpAuthenticator(SmsService smsService) {
        this.smsService = smsService;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        String phone = user.getFirstAttribute(PHONE_ATTR);

        if (phone == null || phone.isBlank()) {
            LOG.warnf("User %s has no phoneNumber attribute – showing error", user.getUsername());
            Response challenge = context.form()
                    .setError("noPhoneNumber")
                    .createForm("sms-otp-form.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge);
            return;
        }

        String code = generateOtp();
        context.getAuthenticationSession().setAuthNote(AUTH_NOTE_CODE, code);

        try {
            smsService.sendVerificationCode(phone, code, user.getEmail());
        } catch (SmsService.SmsSendException e) {
            LOG.errorf(e, "Failed to send SMS OTP to %s", phone);
            context.failureChallenge(
                    AuthenticationFlowError.INTERNAL_ERROR,
                    context.form()
                           .setError("smsSendFailed", phone)
                           .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR)
            );
            return;
        }

        // Mask phone for display: +861380****8000 → +861380****8000
        String maskedPhone = maskPhone(phone);
        Response challenge = context.form()
                .setAttribute("phoneHint", maskedPhone)
                .createForm("sms-otp-form.ftl");
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formParams =
                context.getHttpRequest().getDecodedFormParameters();
        String submittedCode = formParams.getFirst(FORM_FIELD_CODE);
        String expectedCode  = context.getAuthenticationSession().getAuthNote(AUTH_NOTE_CODE);

        if (expectedCode == null) {
            // Session expired or tampered – restart
            authenticate(context);
            return;
        }

        if (expectedCode.equals(submittedCode)) {
            context.success();
        } else {
            Response challenge = context.form()
                    .setAttribute("phoneHint", maskPhone(
                            context.getUser().getFirstAttribute(PHONE_ATTR)))
                    .setError("invalidOtpCode")
                    .createForm("sms-otp-form.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // Always return true so Keycloak doesn't throw AuthenticationFlowException
        // for REQUIRED executions when the user has no phone number.
        // The no-phone case is handled gracefully inside authenticate().
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // no-op – we don't force any setup action
    }

    @Override
    public void close() {
        // no-op
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private static String generateOtp() {
        int code = 100_000 + RANDOM.nextInt(900_000);
        return String.valueOf(code);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        int len = phone.length();
        // Keep first 4 and last 4 chars, mask the middle
        return phone.substring(0, 4) + "****" + phone.substring(len - 4);
    }
}
