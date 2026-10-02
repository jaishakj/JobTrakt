package com.jobtrakt.application;

import com.jobtrakt.audit.AuditLog;
import java.time.Instant;
public record ApplicationTimelineItem(Long id, String action, String details, Instant createdAt) {
    public static ApplicationTimelineItem from(AuditLog row) {
        return new ApplicationTimelineItem(row.getId(), row.getAction(), row.getDetails(), row.getCreatedAt());
    }
}
