package com.webank.app.service;

import com.webank.app.entity.User;
import com.webank.app.entity.VerificationCode;
import com.webank.app.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeService {
    
    private final VerificationCodeRepository verificationCodeRepository;
    private static final Random RANDOM = new Random();
    private static final int SMS_CODE_LENGTH = 6;
    
    @Transactional
    public VerificationCode generateVerificationCode(Long userId, String contact, VerificationCode.Type type, int expiryMinutes) {
        // 检查频率限制
        long recentCount = verificationCodeRepository.countRecentCodes(
            contact, 
            type, 
            LocalDateTime.now().minusMinutes(1)
        );
        
        if (recentCount > 0) {
            throw new IllegalStateException("请求过于频繁，请稍后重试");
        }
        
        String code = generateCode(type);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        
        VerificationCode verificationCode = VerificationCode.builder()
            .userId(userId)
            .type(type)
            .code(code)
            .contact(contact)
            .expiresAt(expiresAt)
            .build();
        
        return verificationCodeRepository.save(verificationCode);
    }
    
    public VerificationCode verifyCode(String code, VerificationCode.Type type) {
        VerificationCode verificationCode = verificationCodeRepository.findValidCode(code, type)
            .orElseThrow(() -> new IllegalArgumentException("验证码无效或已过期"));
        
        return verificationCode;
    }
    
    @Transactional
    public void markAsUsed(Long codeId) {
        VerificationCode code = verificationCodeRepository.findById(codeId)
            .orElseThrow(() -> new IllegalArgumentException("验证码不存在"));
        code.setUsed(true);
        code.setUsedAt(LocalDateTime.now());
        verificationCodeRepository.save(code);
    }
    
    private String generateCode(VerificationCode.Type type) {
        if (type == VerificationCode.Type.SMS_VERIFICATION) {
            return String.format("%06d", RANDOM.nextInt(1000000));
        } else {
            return generateRandomToken();
        }
    }
    
    private String generateRandomToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
