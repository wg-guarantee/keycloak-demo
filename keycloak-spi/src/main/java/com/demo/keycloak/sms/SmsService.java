package com.demo.keycloak.sms;

/**
 * SPI interface for sending SMS verification codes.
 * Implement this to plug in any SMS provider.
 */
public interface SmsService {

    /**
     * Send a 6-digit OTP to the given phone number.
     * If {@code toEmail} is non-null and SMTP_HOST env var is configured,
     * the code is also delivered by email.
     *
     * @param phoneNumber E.164 format, e.g. "+8613800138000"
     * @param code        The 6-digit OTP to send
     * @param toEmail     Recipient email address (nullable – skips email if null/blank)
     * @throws SmsSendException if the SMS could not be delivered
     */
    void sendVerificationCode(String phoneNumber, String code, String toEmail) throws SmsSendException;

    class SmsSendException extends Exception {
        public SmsSendException(String message) {
            super(message);
        }
        public SmsSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
