package com.webank.app.repository;

import com.webank.app.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    List<EmailLog> findByUserId(Long userId);
    
    @Query("SELECT COUNT(el) FROM EmailLog el WHERE el.recipient = :recipient AND el.createdAt > :since AND el.status = 'SUCCESS'")
    long countSuccessfulEmails(@Param("recipient") String recipient, @Param("since") LocalDateTime since);
    
    @Query("SELECT el FROM EmailLog el WHERE el.status = 'FAILED' ORDER BY el.createdAt DESC")
    List<EmailLog> findFailedEmails();
}
