package com.webank.app.service;

import com.webank.app.config.AliyunConfig;
import com.webank.app.entity.EmailLog;
import com.webank.app.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    
    private final EmailLogRepository emailLogRepository;
    private final AliyunConfig aliyunConfig;
    
    @Async
    @Transactional
    public void sendActivationEmail(Long userId, String recipient, String activationLink) {
        EmailLog emailLog = EmailLog.builder()
            .userId(userId)
            .recipient(recipient)
            .subject("账户激活")
            .templateType("ACTIVATION")
            .build();
        
        try {
            if (!aliyunConfig.isEmailConfigured()) {
                log.warn("阿里云邮件未配置，跳过实际发送");
                emailLog.setStatus(EmailLog.Status.SUCCESS);
                emailLogRepository.save(emailLog);
                return;
            }
            
            // TODO: 实现阿里云邮件 SDK 调用
            String html = buildActivationHtml(activationLink);
            log.info("发送激活邮件到: {} (userId: {})", recipient, userId);
            
            // 后续会在 Phase 4 实现真实的阿里云调用
            emailLog.setStatus(EmailLog.Status.SUCCESS);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage(e.getMessage());
        }
        
        emailLogRepository.save(emailLog);
    }
    
    @Async
    @Transactional
    public void sendPasswordResetEmail(Long userId, String recipient, String resetLink) {
        EmailLog emailLog = EmailLog.builder()
            .userId(userId)
            .recipient(recipient)
            .subject("密码重置")
            .templateType("PASSWORD_RESET")
            .build();
        
        try {
            if (!aliyunConfig.isEmailConfigured()) {
                log.warn("阿里云邮件未配置，跳过实际发送");
                emailLog.setStatus(EmailLog.Status.SUCCESS);
                emailLogRepository.save(emailLog);
                return;
            }
            
            String html = buildPasswordResetHtml(resetLink);
            log.info("发送密码重置邮件到: {} (userId: {})", recipient, userId);
            
            emailLog.setStatus(EmailLog.Status.SUCCESS);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage(e.getMessage());
        }
        
        emailLogRepository.save(emailLog);
    }
    
    private String buildActivationHtml(String activationLink) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head><meta charset=\"utf-8\"></head>\n" +
            "<body>\n" +
            "  <h2>账户激活</h2>\n" +
            "  <p>点击下方链接激活您的账户：</p>\n" +
            "  <a href=\"" + activationLink + "\">激活账户</a>\n" +
            "  <p>链接有效期为24小时，请及时激活。</p>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private String buildPasswordResetHtml(String resetLink) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head><meta charset=\"utf-8\"></head>\n" +
            "<body>\n" +
            "  <h2>密码重置</h2>\n" +
            "  <p>点击下方链接重置您的密码：</p>\n" +
            "  <a href=\"" + resetLink + "\">重置密码</a>\n" +
            "  <p>链接有效期为24小时。</p>\n" +
            "</body>\n" +
            "</html>";
    }
}
