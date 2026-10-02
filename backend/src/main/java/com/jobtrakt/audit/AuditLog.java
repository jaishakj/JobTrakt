package com.jobtrakt.audit;

import com.jobtrakt.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(nullable = false, length = 80) private String action;
    @Column(name = "entity_type", nullable = false, length = 80) private String entityType;
    @Column(name = "entity_id") private Long entityId;
    @Column(columnDefinition = "text") private String details;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected AuditLog() { }
    public AuditLog(User user, String action, String entityType, Long entityId, String details) {
        this.user = user; this.action = action; this.entityType = entityType; this.entityId = entityId; this.details = details;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}
