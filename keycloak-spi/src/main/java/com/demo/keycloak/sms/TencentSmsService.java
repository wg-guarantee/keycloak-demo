package com.demo.keycloak.sms;

import org.jboss.logging.Logger;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Mock SMS implementation — prints the OTP code to Keycloak logs and,
 * when SMTP_HOST env var is configured, also sends it by email.
 *
 * To read the verification code during a demo:
 *   docker compose logs keycloak | grep "[SMS-MOCK]"
 *
 * To enable email delivery, set these env vars in docker-compose.yml / .env:
 *   SMTP_HOST, SMTP_PORT (default 587), SMTP_USER, SMTP_PASS, SMTP_FROM
 */
public class TencentSmsService implements SmsService {

    private static final Logger LOG = Logger.getLogger(TencentSmsService.class);

    @Override
    public void sendVerificationCode(String phoneNumber, String code, String toEmail)
            throws SmsSendException {
        // Always log (mock mode – check logs for OTP)
        LOG.warnf("[SMS-MOCK] >>> Phone: %s  OTP Code: %s <<<", phoneNumber, code);

        // Send email if SMTP is configured and a recipient address is available
        String smtpHost = System.getenv("SMTP_HOST");
        if (smtpHost == null || smtpHost.isBlank() || toEmail == null || toEmail.isBlank()) {
            return;
        }

        String smtpPort = env("SMTP_PORT", "587");
        String smtpUser = System.getenv("SMTP_USER");
        String smtpPass = System.getenv("SMTP_PASS");
        String smtpFrom = env("SMTP_FROM", smtpUser != null ? smtpUser : toEmail);

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", smtpHost);

            Session mailSession;
            if (smtpUser != null && !smtpUser.isBlank()) {
                props.put("mail.smtp.auth", "true");
                final String u = smtpUser;
                final String p = smtpPass;
                mailSession = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(u, p);
                    }
                });
            } else {
                mailSession = Session.getInstance(props);
            }

            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(smtpFrom));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("【身份验证】您的验证码");
            msg.setText("您的验证码为：" + code
                    + "\n\n验证码5分钟内有效，请勿泄露给他人。"
                    + "\n\n绑定手机号：" + phoneNumber);
            Transport.send(msg);
            LOG.infof("[SMS-MOCK] OTP email sent to %s", toEmail);
        } catch (Exception e) {
            // Email failure is non-fatal: OTP is still visible in Keycloak logs
            LOG.warnf(e, "[SMS-MOCK] Failed to send OTP email to %s: %s", toEmail, e.getMessage());
        }
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v != null && !v.isBlank()) ? v : fallback;
    }
}
