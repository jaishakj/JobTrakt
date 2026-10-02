package com.jobtrakt.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {
    Page<NotificationRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    java.util.List<NotificationRecord> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<NotificationRecord> findByIdAndUserId(Long id, Long userId);
    List<NotificationRecord> findTop20ByApplicationIdAndUserIdOrderByCreatedAtDesc(Long applicationId, Long userId);
    long countByUserIdAndReadFalse(Long userId);
    int countByUserIdAndReadFalseAndApplicationId(Long userId, Long applicationId);
    int countByUserIdAndApplicationId(Long userId, Long applicationId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update NotificationRecord n set n.read=true where n.user.id=:userId and n.read=false")
    int markAllRead(@org.springframework.data.repository.query.Param("userId") Long userId);
}
