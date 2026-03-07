package com.webank.app.repository;

import com.webank.app.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    Optional<VerificationCode> findByCode(String code);
    
    List<VerificationCode> findByUserId(Long userId);
    
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.code = :code AND vc.type = :type AND vc.used = false AND vc.expiresAt > CURRENT_TIMESTAMP")
    Optional<VerificationCode> findValidCode(@Param("code") String code, @Param("type") VerificationCode.Type type);
    
    @Query("SELECT vc FROM VerificationCode vc WHERE vc.contact = :contact AND vc.type = :type AND vc.used = false AND vc.expiresAt > CURRENT_TIMESTAMP ORDER BY vc.createdAt DESC LIMIT 1")
    Optional<VerificationCode> findLatestValidCode(@Param("contact") String contact, @Param("type") VerificationCode.Type type);
    
    @Query("SELECT COUNT(vc) FROM VerificationCode vc WHERE vc.contact = :contact AND vc.type = :type AND vc.createdAt > :since")
    long countRecentCodes(@Param("contact") String contact, @Param("type") VerificationCode.Type type, @Param("since") LocalDateTime since);
    
    void deleteByExpiresAtBefore(LocalDateTime cutoffTime);
}
