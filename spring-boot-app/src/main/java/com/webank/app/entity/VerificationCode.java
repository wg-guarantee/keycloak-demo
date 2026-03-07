package com.webank.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_code", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_code", columnList = "code"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {
    
    public enum Type {
        EMAIL_ACTIVATION,
        PASSWORD_RESET,
        SMS_VERIFICATION
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
    
    @Column(nullable = false, length = 10)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String contact; // email or phone
    
    @Column(nullable = false)
    private Boolean used = false;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}
