package com.jobtrakt.job;

import com.jobtrakt.application.JobApplication;
import com.jobtrakt.common.PageResponse;
import com.jobtrakt.common.exception.ConflictException;
import com.jobtrakt.common.exception.ResourceNotFoundException;
import com.jobtrakt.user.User;
import com.jobtrakt.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;

@Service
public class JobService {
    private final JobRepository jobs;
    private final UserRepository users;
    public JobService(JobRepository jobs, UserRepository users) { this.jobs = jobs; this.users = users; }

    @Transactional
    public BackgroundJob schedule(User user, JobApplication application, JobType type, JobPriority priority,
                                  Instant scheduledAt, String payload) {
        Long applicationId = application == null ? null : application.getId();
        String key = type + ":" + (applicationId == null ? user.getId() : applicationId) + ":" + payload;
        var existing = jobs.findByDedupeKey(key);
        if (existing.isPresent()) return existing.get();
        return jobs.save(new BackgroundJob(user, application, key, type, priority, payload, scheduledAt, 5));
    }

    @Transactional
    public void ensureWeeklyReport(User user) {
        Instant now = Instant.now();
        var nextMonday = now.atZone(ZoneOffset.UTC).with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(9).withMinute(0).withSecond(0).withNano(0).toInstant();
        schedule(user, null, JobType.WEEKLY_REPORT, JobPriority.LOW, nextMonday,
                "{\"weekOf\":\"" + nextMonday.atZone(ZoneOffset.UTC).toLocalDate() + "\"}");
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> list(Long userId, JobStatus status, int page, int size) {
        checkPage(page, size);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        var result = status == null ? jobs.findByUserId(userId, pageable) : jobs.findByUserIdAndStatus(userId, status, pageable);
        return PageResponse.from(result.map(JobResponse::from));
    }

    @Transactional(readOnly = true)
    public JobResponse get(Long userId, Long id) {
        return jobs.findByIdAndUserId(id, userId).map(JobResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Job"));
    }

    @Transactional
    public JobResponse retry(Long userId, Long id) {
        BackgroundJob job = jobs.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job"));
        if (job.getStatus() != JobStatus.FAILED && job.getStatus() != JobStatus.DEAD) {
            throw new ConflictException("Only failed or dead jobs can be retried");
        }
        job.retryNow();
        return JobResponse.from(job);
    }

    private void checkPage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new com.jobtrakt.common.exception.InvalidRequestException("Page must be non-negative and size must be between 1 and 100");
    }
}
