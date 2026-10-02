package com.jobtrakt.notification;

import com.jobtrakt.application.JobApplication;
import com.jobtrakt.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class NotificationRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "application_id") private JobApplication application;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private NotificationType type;
    @Column(nullable = false, length = 180) private String title;
    @Column(nullable = false, length = 1000) private String message;
    @Column(name = "is_read", nullable = false) private boolean read;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected NotificationRecord() { }
    public NotificationRecord(User user, JobApplication application, NotificationType type, String title, String message) {
        this.user = user; this.application = application; this.type = type; this.title = title; this.message = message;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public void markRead() { read = true; }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public JobApplication getApplication() { return application; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
