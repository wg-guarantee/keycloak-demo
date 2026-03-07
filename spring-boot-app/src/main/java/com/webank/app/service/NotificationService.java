package com.webank.app.service;

import com.webank.app.entity.SmsLog;
import com.webank.app.repository.SmsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    
    private final AliyunSmsService aliyunSmsService;
    private final AliyunEmailService aliyunEmailService;
    private final SmsLogRepository smsLogRepository;
    private final EmailService emailService;
    
    /**
     * 发送短信验证码（通过阿里云）
     */
    @Async
    @Transactional
    public void sendSmsVerificationCode(Long userId, String phone, String code) {
        SmsLog smsLog = SmsLog.builder()
            .userId(userId)
            .phone(phone)
            .content(buildSmsContent(code))
            .build();
        
        try {
            String messageId = aliyunSmsService.sendVerificationCode(phone, code);
            smsLog.setStatus(SmsLog.Status.SUCCESS);
            smsLog.setAliyunMessageId(messageId);
            log.info("短信验证码发送成功 - 用户: {}, 电话: {}", userId, phone);
        } catch (Exception e) {
            log.error("短信验证码发送失败 - 用户: {}, 电话: {}", userId, phone, e);
            smsLog.setStatus(SmsLog.Status.FAILED);
            smsLog.setErrorMessage(e.getMessage());
        }
        
        smsLogRepository.save(smsLog);
    }
    
    /**
     * 发送激活邮件（通过阿里云）
     */
    @Async
    @Transactional
    public void sendActivationEmail(Long userId, String email, String activationLink) {
        try {
            boolean success = aliyunEmailService.sendActivationEmail(email, activationLink);
            if (success) {
                log.info("激活邮件发送成功 - 用户: {}, 邮箱: {}", userId, email);
            } else {
                log.warn("激活邮件发送失败 - 用户: {}, 邮箱: {}", userId, email);
            }
        } catch (Exception e) {
            log.error("激活邮件发送异常 - 用户: {}, 邮箱: {}", userId, email, e);
        }
    }
    
    /**
     * 发送密码重置邮件（通过阿里云）
     */
    @Async
    @Transactional
    public void sendPasswordResetEmail(Long userId, String email, String resetLink) {
        try {
            boolean success = aliyunEmailService.sendPasswordResetEmail(email, resetLink);
            if (success) {
                log.info("密码重置邮件发送成功 - 用户: {}, 邮箱: {}", userId, email);
            } else {
                log.warn("密码重置邮件发送失败 - 用户: {}, 邮箱: {}", userId, email);
            }
        } catch (Exception e) {
            log.error("密码重置邮件发送异常 - 用户: {}, 邮箱: {}", userId, email, e);
        }
    }
    
    private String buildSmsContent(String code) {
        return String.format("您的验证码是：%s，5分钟内有效，请勿泄露。", code);
    }
}
