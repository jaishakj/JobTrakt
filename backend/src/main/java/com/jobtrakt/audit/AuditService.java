package com.jobtrakt.audit;

import com.jobtrakt.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository logs;
    public AuditService(AuditLogRepository logs) { this.logs = logs; }

    @Transactional
    public void record(User user, String action, String entityType, Long entityId, String details) {
        logs.save(new AuditLog(user, action, entityType, entityId, details));
    }
}
