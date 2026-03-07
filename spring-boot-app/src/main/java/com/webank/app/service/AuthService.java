package com.webank.app.service;

import com.webank.app.entity.User;
import com.webank.app.entity.VerificationCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final SmsService smsService;
    
    @Transactional
    public User register(String username, String email, String password, String phone) {
        // 创建用户（密码由 Keycloak 管理）
        User user = userService.createUser(username, email, phone);
        
        // 生成邮件激活码
        VerificationCode activationCode = verificationCodeService.generateVerificationCode(
            user.getId(),
            email,
            VerificationCode.Type.EMAIL_ACTIVATION,
            24 // 24 hours
        );
        
        // 发送激活邮件
        String activationLink = buildActivationLink(activationCode.getCode());
        emailService.sendActivationEmail(user.getId(), email, activationLink);
        
        log.info("用户注册成功: {}", username);
        return user;
    }
    
    @Transactional
    public void activate(String email, String token) {
        // 验证令牌
        VerificationCode verificationCode = verificationCodeService.verifyCode(token, VerificationCode.Type.EMAIL_ACTIVATION);
        
        // 激活用户
        User user = userService.getUserByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        
        userService.activateUser(user.getId());
        verificationCodeService.markAsUsed(verificationCode.getId());
        
        log.info("用户激活成功: {}", email);
    }
    
    @Transactional
    public void sendSmsVerificationCode(String phone) {
        // 生成短信验证码
        VerificationCode smsCode = verificationCodeService.generateVerificationCode(
            0L, // 未认证用户
            phone,
            VerificationCode.Type.SMS_VERIFICATION,
            5 // 5 minutes
        );
        
        // 发送短信
        smsService.sendVerificationCode(0L, phone, smsCode.getCode());
        
        log.info("短信验证码已发送到: {}", phone);
    }
    
    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userService.getUserByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("邮箱未注册"));
        
        // 生成密码重置令牌
        VerificationCode resetToken = verificationCodeService.generateVerificationCode(
            user.getId(),
            email,
            VerificationCode.Type.PASSWORD_RESET,
            24 // 24 hours
        );
        
        // 发送重置邮件
        String resetLink = buildPasswordResetLink(resetToken.getCode());
        emailService.sendPasswordResetEmail(user.getId(), email, resetLink);
        
        log.info("密码重置邮件已发送到: {}", email);
    }
    
    private String buildActivationLink(String token) {
        return "http://localhost:8080/api/auth/activate?token=" + token;
    }
    
    private String buildPasswordResetLink(String token) {
        return "http://localhost:8080/api/auth/reset-password?token=" + token;
    }
}
