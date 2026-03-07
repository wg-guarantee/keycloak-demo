package com.webank.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {
    
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
    
    @Column(nullable = false, length = 100)
    private String recipient;
    
    @Column(nullable = false, length = 255)
    private String subject;
    
    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.SENDING;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
