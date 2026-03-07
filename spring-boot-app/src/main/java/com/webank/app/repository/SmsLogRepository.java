package com.webank.app.repository;

import com.webank.app.entity.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
    
    List<SmsLog> findByUserId(Long userId);
    
    @Query("SELECT COUNT(sl) FROM SmsLog sl WHERE sl.phone = :phone AND sl.createdAt > :since AND sl.status = 'SUCCESS'")
    long countSuccessfulSms(@Param("phone") String phone, @Param("since") LocalDateTime since);
    
    @Query("SELECT sl FROM SmsLog sl WHERE sl.status = 'FAILED' ORDER BY sl.createdAt DESC")
    List<SmsLog> findFailedSms();
}
