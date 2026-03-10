package com.demo.keycloak.sms;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.jboss.logging.Logger;
import jakarta.ws.rs.core.MultivaluedMap;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FormAction: verify phone number via SMS OTP during registration.
 *
 * UX flow (all on one page):
 *   1. User fills in phone number and clicks "Send Code"
 *      → POST with reg_action=send_code → OTP sent, form re-renders with OTP field
 *   2. User enters OTP and fills rest of form, clicks "Register"
 *      → POST without reg_action → OTP validated, registration proceeds
 *
 * Rate limiting (stored in Keycloak auth session, cannot be bypassed client-side):
 *   - 60 s minimum between sends
 *   - Max 5 sends per registration session
 */
public class RegistrationPhoneVerification implements FormAction {

    private static final Logger LOG = Logger.getLogger(RegistrationPhoneVerification.class);

    static final String PROVIDER_ID = "registration-phone-verification";

    private static final String PHONE_FIELD   = "user.attributes.phoneNumber";
    private static final String OTP_FIELD     = "phone_otp";
    private static final String ACTION_FIELD  = "reg_action";
    private static final String NOTE_OTP      = "reg_sms_otp";
    private static final String NOTE_PHONE    = "reg_sms_phone";
    private static final String NOTE_OTP_SENT = "reg_phone_otp_sent";
    private static final String NOTE_LAST_TS  = "reg_sms_last_ts";
    private static final String NOTE_SEND_CNT = "reg_sms_send_cnt";

    private static final long COOLDOWN_MS = 60_000L;
    private static final int  MAX_SENDS   = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsService smsService;

    public RegistrationPhoneVerification(SmsService smsService) {
        this.smsService = smsService;
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        String sent = context.getAuthenticationSession().getAuthNote(NOTE_OTP_SENT);
        if ("true".equals(sent)) {
            form.setAttribute("phoneOtpSent", Boolean.TRUE);
            String phone = context.getAuthenticationSession().getAuthNote(NOTE_PHONE);
            if (phone != null) {
                form.setAttribute("phoneOtpMasked", maskPhone(phone));
            }
        }
    }

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();
        String action = params.getFirst(ACTION_FIELD);
        String phone  = trim(params.getFirst(PHONE_FIELD));
        List<FormMessage> errors = new ArrayList<>();

        if ("send_code".equals(action)) {
            // ── Validate phone format ────────────────────────────────────────
            if (phone == null || phone.isBlank()) {
                errors.add(new FormMessage(PHONE_FIELD, "requiredPhoneNumber"));
                context.validationError(params, errors);
                return;
            }
            if (!phone.matches("^\\+[1-9]\\d{6,14}$")) {
                errors.add(new FormMessage(PHONE_FIELD, "invalidPhoneFormat"));
                context.validationError(params, errors);
                return;
            }
            // ── Backend rate limiting ────────────────────────────────────────
            String cntStr = context.getAuthenticationSession().getAuthNote(NOTE_SEND_CNT);
            int sendCnt = cntStr != null ? Integer.parseInt(cntStr) : 0;
            if (sendCnt >= MAX_SENDS) {
                errors.add(new FormMessage(PHONE_FIELD, "phoneOtpTooManyRequests"));
                context.validationError(params, errors);
                return;
            }
            String lastTsStr = context.getAuthenticationSession().getAuthNote(NOTE_LAST_TS);
            if (lastTsStr != null) {
                long elapsed = System.currentTimeMillis() - Long.parseLong(lastTsStr);
                if (elapsed < COOLDOWN_MS) {
                    long remaining = (COOLDOWN_MS - elapsed + 999) / 1000;
                    errors.add(new FormMessage(PHONE_FIELD, "phoneOtpCooldown", remaining));
                    context.validationError(params, errors);
                    return;
                }
            }
            // ── Send OTP ─────────────────────────────────────────────────────
            String code = String.format("%06d", RANDOM.nextInt(1_000_000));
            context.getAuthenticationSession().setAuthNote(NOTE_OTP, code);
            context.getAuthenticationSession().setAuthNote(NOTE_PHONE, phone);
            context.getAuthenticationSession().setAuthNote(NOTE_OTP_SENT, "true");
            context.getAuthenticationSession().setAuthNote(NOTE_LAST_TS, String.valueOf(System.currentTimeMillis()));
            context.getAuthenticationSession().setAuthNote(NOTE_SEND_CNT, String.valueOf(sendCnt + 1));
            String emailForOtp = trim(params.getFirst("email"));
            try {
                smsService.sendVerificationCode(phone, code, emailForOtp);
                LOG.infof("[RegPhone] OTP sent to %s (send #%d)", maskPhone(phone), sendCnt + 1);
            } catch (SmsService.SmsSendException e) {
                LOG.errorf(e, "Failed to send registration OTP to %s", phone);
                context.getAuthenticationSession().removeAuthNote(NOTE_OTP_SENT);
                errors.add(new FormMessage(PHONE_FIELD, "smsSendFailed", phone));
                context.validationError(params, errors);
                return;
            }
            // Re-render the form (with OTP field visible); empty error list = no error shown
            context.validationError(params, errors);
            return;
        }

        // ── Final submit: validate OTP ────────────────────────────────────────
        String sent = context.getAuthenticationSession().getAuthNote(NOTE_OTP_SENT);
        if (!"true".equals(sent)) {
            errors.add(new FormMessage(PHONE_FIELD, "phoneOtpNotSent"));
            context.validationError(params, errors);
            return;
        }
        String savedPhone = context.getAuthenticationSession().getAuthNote(NOTE_PHONE);
        if (!Objects.equals(phone, savedPhone)) {
            context.getAuthenticationSession().removeAuthNote(NOTE_OTP_SENT);
            context.getAuthenticationSession().removeAuthNote(NOTE_OTP);
            errors.add(new FormMessage(PHONE_FIELD, "phoneChangedResend"));
            context.validationError(params, errors);
            return;
        }
        String submitted = trim(params.getFirst(OTP_FIELD));
        String expected  = context.getAuthenticationSession().getAuthNote(NOTE_OTP);
        if (submitted == null || !submitted.equals(expected)) {
            errors.add(new FormMessage(OTP_FIELD, "invalidOtpCode"));
            context.validationError(params, errors);
            return;
        }
        context.success();
    }

    @Override
    public void success(FormContext context) { /* no-op */ }

    @Override
    public boolean requiresUser() { return false; }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

    @Override
    public void close() {}

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        int len = phone.length();
        return phone.substring(0, len - 8) + "****" + phone.substring(len - 4);
    }
}
