package com.jobtrakt.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtrakt.application.ApplicationStatus;
import com.jobtrakt.application.JobApplicationRepository;
import com.jobtrakt.notification.NotificationRecord;
import com.jobtrakt.notification.NotificationRepository;
import com.jobtrakt.notification.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JobProcessor {
    private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);
    private final JobRepository jobs;
    private final JobApplicationRepository applications;
    private final NotificationRepository notifications;
    private final JobService jobService;
    private final ObjectMapper mapper;

    public JobProcessor(JobRepository jobs, JobApplicationRepository applications,
                        NotificationRepository notifications, JobService jobService, ObjectMapper mapper) {
        this.jobs = jobs; this.applications = applications; this.notifications = notifications;
        this.jobService = jobService; this.mapper = mapper;
    }

    @Transactional
    public boolean processNextDue() {
        List<BackgroundJob> due = jobs.lockNextDue(Instant.now());
        if (due.isEmpty()) return false;
        BackgroundJob job = due.getFirst();
        job.markProcessing();
        try {
            process(job);
            job.markCompleted();
        } catch (Exception exception) {
            log.warn("Job {} failed on attempt {}: {}", job.getId(), job.getAttempts(), exception.toString());
            job.markFailed(exception.getMessage());
        }
        return true;
    }

    private void process(BackgroundJob job) throws Exception {
        if (job.getType() == JobType.WEEKLY_REPORT) {
            createWeeklySummary(job);
            jobService.ensureWeeklyReport(job.getUser());
            return;
        }
        JsonNode payload = mapper.readTree(job.getPayload());
        long applicationId = payload.path("applicationId").asLong(-1);
        var application = applications.findByIdAndUserId(applicationId, job.getUser().getId()).orElse(null);
        if (application == null || !stillRelevant(job, application, payload.path("expected").asText())) return;

        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("EEE, MMM d 'at' h:mm a")
                .withZone(ZoneId.systemDefault());
        if (job.getType() == JobType.INTERVIEW_REMINDER) {
            String when = dateTime.format(application.getInterviewDate());
            notifications.save(new NotificationRecord(job.getUser(), application, NotificationType.INTERVIEW,
                    "Interview coming up", "Interview with " + application.getCompany() + " for " +
                    application.getPosition() + " is scheduled " + when + "."));
        } else {
            String title = job.getType() == JobType.APPLICATION_FOLLOW_UP ? "Time to follow up" : "Follow-up reminder";
            notifications.save(new NotificationRecord(job.getUser(), application, NotificationType.FOLLOW_UP,
                    title, "Follow up with " + application.getCompany() + " about your " + application.getPosition() + " application."));
        }
    }

    private boolean stillRelevant(BackgroundJob job, com.jobtrakt.application.JobApplication application, String expected) {
        return switch (job.getType()) {
            case INTERVIEW_REMINDER -> matchesInstant(application.getInterviewDate(), expected);
            case FOLLOW_UP_REMINDER -> matchesInstant(application.getFollowUpDate(), expected);
            case APPLICATION_FOLLOW_UP -> application.getStatus() == ApplicationStatus.APPLIED
                    && application.getAppliedDate().toString().equals(expected);
            case WEEKLY_REPORT -> false;
        };
    }

    private boolean matchesInstant(Instant actual, String expected) {
        if (actual == null || expected == null || expected.isBlank()) return false;
        try {
            Instant requested = Instant.parse(expected);
            return actual.truncatedTo(ChronoUnit.MICROS).equals(requested.truncatedTo(ChronoUnit.MICROS));
        } catch (java.time.DateTimeException exception) {
            return false;
        }
    }

    private void createWeeklySummary(BackgroundJob job) {
        Instant now = Instant.now();
        Instant since = now.minus(7, ChronoUnit.DAYS);
        long created = applications.countByUserIdAndCreatedAtAfter(job.getUser().getId(), since);
        long interviews = applications.countByUserIdAndStatus(job.getUser().getId(), ApplicationStatus.INTERVIEW);
        String message = "You added " + created + " " + (created == 1 ? "application" : "applications") +
                " in the past week and have " + interviews + " active interview" + (interviews == 1 ? "" : "s") + ".";
        notifications.save(new NotificationRecord(job.getUser(), null, NotificationType.SYSTEM,
                "Your weekly search note", message));
    }
}
