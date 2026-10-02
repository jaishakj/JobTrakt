package com.jobtrakt.job;

import com.jobtrakt.application.JobApplication;
import com.jobtrakt.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "jobs")
public class BackgroundJob {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "application_id") private JobApplication application;
    @Column(name = "dedupe_key", unique = true, length = 255) private String dedupeKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private JobType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private JobStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private JobPriority priority;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false) private int attempts;
    @Column(name = "max_attempts", nullable = false) private int maxAttempts;
    @Column(name = "scheduled_at", nullable = false) private Instant scheduledAt;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "error_message", length = 1000) private String errorMessage;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected BackgroundJob() { }
    public BackgroundJob(User user, JobApplication application, String dedupeKey, JobType type,
                        JobPriority priority, String payload, Instant scheduledAt, int maxAttempts) {
        this.user = user; this.application = application; this.dedupeKey = dedupeKey; this.type = type;
        this.priority = priority; this.payload = payload; this.scheduledAt = scheduledAt;
        this.maxAttempts = maxAttempts; this.status = JobStatus.PENDING;
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public void markProcessing() { status = JobStatus.PROCESSING; attempts++; startedAt = Instant.now(); errorMessage = null; }
    public void markCompleted() { status = JobStatus.COMPLETED; completedAt = Instant.now(); errorMessage = null; }
    public void markFailed(String message) {
        errorMessage = message == null ? "Job failed" : message.substring(0, Math.min(message.length(), 1000));
        if (attempts >= maxAttempts) { status = JobStatus.DEAD; completedAt = Instant.now(); }
        else { status = JobStatus.FAILED; scheduledAt = Instant.now().plusSeconds(Math.min(3600, 30L << Math.min(attempts - 1, 7))); }
    }
    public void retryNow() { status = JobStatus.PENDING; scheduledAt = Instant.now(); errorMessage = null; completedAt = null; }
    public boolean belongsTo(Long userId) { return user != null && user.getId().equals(userId); }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public JobApplication getApplication() { return application; }
    public String getDedupeKey() { return dedupeKey; }
    public JobType getType() { return type; }
    public JobStatus getStatus() { return status; }
    public JobPriority getPriority() { return priority; }
    public String getPayload() { return payload; }
    public int getAttempts() { return attempts; }
    public int getMaxAttempts() { return maxAttempts; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
