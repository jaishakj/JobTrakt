package com.jobtrakt.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(Long userId, String entityType, Long entityId, Pageable pageable);
    List<AuditLog> findTop8ByUserIdOrderByCreatedAtDesc(Long userId);
}
