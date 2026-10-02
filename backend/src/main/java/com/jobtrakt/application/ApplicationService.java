package com.jobtrakt.application;

import com.jobtrakt.audit.AuditLogRepository;
import com.jobtrakt.audit.AuditService;
import com.jobtrakt.common.PageResponse;
import com.jobtrakt.common.exception.InvalidRequestException;
import com.jobtrakt.common.exception.ResourceNotFoundException;
import com.jobtrakt.job.JobPriority;
import com.jobtrakt.job.JobResponse;
import com.jobtrakt.job.JobRepository;
import com.jobtrakt.job.JobService;
import com.jobtrakt.job.JobType;
import com.jobtrakt.notification.NotificationRepository;
import com.jobtrakt.notification.NotificationResponse;
import com.jobtrakt.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class ApplicationService {
    private final JobApplicationRepository applications;
    private final UserRepository users;
    private final AuditLogRepository auditLogs;
    private final AuditService audit;
    private final JobService jobService;
    private final JobRepository jobs;
    private final NotificationRepository notifications;

    public ApplicationService(JobApplicationRepository applications, UserRepository users, AuditLogRepository auditLogs,
                              AuditService audit, JobService jobService, JobRepository jobs,
                              NotificationRepository notifications) {
        this.applications = applications; this.users = users; this.auditLogs = auditLogs; this.audit = audit;
        this.jobService = jobService; this.jobs = jobs; this.notifications = notifications;
    }

    @Transactional
    public ApplicationResponse create(Long userId, ApplicationRequest request) {
        validateSensible(request);
        var owner = users.getReferenceById(userId);
        JobApplication application = new JobApplication(owner);
        application.update(request.toData());
        application = applications.saveAndFlush(application);
        audit.record(owner, "APPLICATION_CREATED", "APPLICATION", application.getId(),
                application.getCompany() + " · " + application.getPosition());
        scheduleAutomation(application);
        return ApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> list(Long userId, ApplicationStatus status, String search,
                                                   int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100");
        String[] parts = (sort == null || sort.isBlank() ? "createdAt,desc" : sort).split(",", 2);
        String field = switch (parts[0]) {
            case "createdAt", "appliedDate", "company", "status", "updatedAt" -> parts[0];
            default -> throw new InvalidRequestException("Unsupported sort field");
        };
        Sort.Direction direction;
        try { direction = parts.length > 1 ? Sort.Direction.fromString(parts[1]) : Sort.Direction.DESC; }
        catch (IllegalArgumentException exception) { throw new InvalidRequestException("Sort direction must be asc or desc"); }
        String term = search == null || search.isBlank() ? null : search.trim();
        var results = applications.search(userId, status, term, PageRequest.of(page, size, Sort.by(direction, field)));
        return PageResponse.from(results.map(ApplicationResponse::from));
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse get(Long userId, Long id) {
        JobApplication application = findOwned(userId, id);
        var timeline = auditLogs.findByUserIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(userId, "APPLICATION", id,
                PageRequest.of(0, 30)).stream().map(ApplicationTimelineItem::from).toList();
        var relatedJobs = jobs.findTop20ByApplicationIdAndUserIdOrderByCreatedAtDesc(id, userId).stream().map(JobResponse::from).toList();
        var relatedNotifications = notifications.findTop20ByApplicationIdAndUserIdOrderByCreatedAtDesc(id, userId)
                .stream().map(NotificationResponse::from).toList();
        return new ApplicationDetailResponse(ApplicationResponse.from(application), timeline, relatedJobs, relatedNotifications);
    }

    @Transactional
    public ApplicationResponse update(Long userId, Long id, ApplicationRequest request) {
        validateSensible(request);
        JobApplication application = findOwned(userId, id);
        var owner = application.getUser();
        ApplicationStatus previousStatus = application.getStatus();
        application.update(request.toData());
        applications.flush();
        String action = previousStatus == application.getStatus() ? "APPLICATION_UPDATED" : "STATUS_CHANGED";
        String details = previousStatus == application.getStatus()
                ? application.getCompany() + " · " + application.getPosition()
                : previousStatus + " → " + application.getStatus();
        audit.record(owner, action, "APPLICATION", id, details);
        scheduleAutomation(application);
        return ApplicationResponse.from(application);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        JobApplication application = findOwned(userId, id);
        audit.record(application.getUser(), "APPLICATION_DELETED", "APPLICATION", id,
                application.getCompany() + " · " + application.getPosition());
        applications.delete(application);
    }

    private JobApplication findOwned(Long userId, Long id) {
        return applications.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Application"));
    }

    private void validateSensible(ApplicationRequest request) {
        if (request.status() == ApplicationStatus.INTERVIEW && request.interviewDate() == null) {
            throw new InvalidRequestException("An interview date is required for INTERVIEW status");
        }
    }

    private void scheduleAutomation(JobApplication application) {
        Instant now = Instant.now();
        if (application.getInterviewDate() != null) {
            Instant reminderAt = application.getInterviewDate().minus(Duration.ofHours(24));
            if (reminderAt.isBefore(now)) reminderAt = now;
            jobService.schedule(application.getUser(), application, JobType.INTERVIEW_REMINDER, JobPriority.HIGH,
                    reminderAt, payload(application.getId(), application.getInterviewDate().toString()));
        }
        if (application.getFollowUpDate() != null) {
            Instant scheduled = application.getFollowUpDate().isBefore(now) ? now : application.getFollowUpDate();
            jobService.schedule(application.getUser(), application, JobType.FOLLOW_UP_REMINDER, JobPriority.NORMAL,
                    scheduled, payload(application.getId(), application.getFollowUpDate().toString()));
        } else if (application.getStatus() == ApplicationStatus.APPLIED) {
            Instant followUp = application.getAppliedDate().plusDays(7).atTime(9, 0).toInstant(ZoneOffset.UTC);
            jobService.schedule(application.getUser(), application, JobType.APPLICATION_FOLLOW_UP, JobPriority.LOW,
                    followUp.isBefore(now) ? now : followUp, payload(application.getId(), application.getAppliedDate().toString()));
        }
        jobService.ensureWeeklyReport(application.getUser());
    }

    private String payload(Long applicationId, String expected) {
        return "{\"applicationId\":" + applicationId + ",\"expected\":\"" + expected + "\"}";
    }
}
