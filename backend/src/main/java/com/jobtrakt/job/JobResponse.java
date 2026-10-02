package com.jobtrakt.job;

import java.time.Instant;

public record JobResponse(Long id, Long applicationId, String company, String position, JobType type,
        JobStatus status, JobPriority priority, int attempts, int maxAttempts, Instant scheduledAt,
        Instant startedAt, Instant completedAt, String errorMessage, Instant createdAt) {
    public static JobResponse from(BackgroundJob job) {
        var application = job.getApplication();
        return new JobResponse(job.getId(), application == null ? null : application.getId(),
                application == null ? null : application.getCompany(), application == null ? null : application.getPosition(),
                job.getType(), job.getStatus(), job.getPriority(), job.getAttempts(), job.getMaxAttempts(),
                job.getScheduledAt(), job.getStartedAt(), job.getCompletedAt(), job.getErrorMessage(), job.getCreatedAt());
    }
}
