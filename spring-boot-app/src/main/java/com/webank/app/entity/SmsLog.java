package com.webank.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsLog {
    
    public enum Status {
        SENDING,
        SUCCESS,
        FAILED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.SENDING;
    
    @Column(name = "aliyun_message_id", length = 100)
    private String aliyunMessageId;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
