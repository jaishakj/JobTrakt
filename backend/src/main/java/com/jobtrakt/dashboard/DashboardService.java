package com.jobtrakt.dashboard;

import com.jobtrakt.application.*;
import com.jobtrakt.job.*;
import com.jobtrakt.notification.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class DashboardService {
    private final JobApplicationRepository applications;
    private final NotificationRepository notifications;
    private final JobRepository jobs;
    public DashboardService(JobApplicationRepository applications, NotificationRepository notifications, JobRepository jobs) {
        this.applications = applications; this.notifications = notifications; this.jobs = jobs;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get(Long userId) {
        long total = applications.countByUserId(userId);
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) counts.put(status, applications.countByUserIdAndStatus(userId, status));
        long denominator = total - counts.get(ApplicationStatus.SAVED) - counts.get(ApplicationStatus.WITHDRAWN);
        long responses = counts.get(ApplicationStatus.SCREENING) + counts.get(ApplicationStatus.INTERVIEW)
                + counts.get(ApplicationStatus.OFFER) + counts.get(ApplicationStatus.REJECTED);
        long interviews = counts.get(ApplicationStatus.INTERVIEW) + counts.get(ApplicationStatus.OFFER);
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay().toInstant(ZoneOffset.UTC);
        LocalDate monthStart = today.withDayOfMonth(1);
        List<ApplicationResponse> upcoming = applications.upcomingInterviews(userId, now, PageRequest.of(0, 6))
                .stream().map(ApplicationResponse::from).toList();
        List<ApplicationResponse> recent = applications.findTop6ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ApplicationResponse::from).toList();
        Map<JobStatus, Long> queue = new EnumMap<>(JobStatus.class);
        for (JobStatus status : JobStatus.values()) queue.put(status, 0L);
        jobs.countStatuses(userId).forEach(row -> queue.put(row.getStatus(), row.getTotal()));
        return new DashboardResponse(total, Map.copyOf(counts),
                applications.countByUserIdAndAppliedDateBetween(userId, today.with(DayOfWeek.MONDAY), today),
                applications.countByUserIdAndAppliedDateBetween(userId, monthStart, today),
                rate(responses, denominator), rate(interviews, denominator), rate(counts.get(ApplicationStatus.OFFER), denominator),
                upcoming, recent, notifications.countByUserIdAndReadFalse(userId), Map.copyOf(queue),
                notifications.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationResponse::from).toList());
    }

    private double rate(long count, long denominator) {
        return denominator == 0 ? 0.0 : Math.round(count * 10000.0 / denominator) / 100.0;
    }
}
