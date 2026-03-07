package com.webank.app.service;

import com.webank.app.config.AliyunConfig;
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
public class SmsService {
    
    private final SmsLogRepository smsLogRepository;
    private final AliyunConfig aliyunConfig;
    
    @Async
    @Transactional
    public void sendVerificationCode(Long userId, String phone, String code) {
        SmsLog smsLog = SmsLog.builder()
            .userId(userId)
            .phone(phone)
            .content(buildSmsContent(code))
            .build();
        
        try {
            if (!aliyunConfig.isSmsConfigured()) {
                log.warn("阿里云短信未配置，跳过实际发送");
                smsLog.setStatus(SmsLog.Status.SUCCESS);
                smsLogRepository.save(smsLog);
                return;
            }
            
            // TODO: 实现阿里云短信 SDK 调用
            log.info("发送验证码短信到: {} (userId: {})", phone, userId);
            
            // 后续会在 Phase 4 实现真实的阿里云调用
            smsLog.setStatus(SmsLog.Status.SUCCESS);
        } catch (Exception e) {
            log.error("短信发送失败", e);
            smsLog.setStatus(SmsLog.Status.FAILED);
            smsLog.setErrorMessage(e.getMessage());
        }
        
        smsLogRepository.save(smsLog);
    }
    
    private String buildSmsContent(String code) {
        return String.format("您的验证码是：%s，5分钟内有效，请勿泄露。", code);
    }
}
