package com.jobtrakt.dashboard;

import com.jobtrakt.application.ApplicationResponse;
import com.jobtrakt.application.ApplicationStatus;
import com.jobtrakt.job.JobStatus;
import com.jobtrakt.notification.NotificationResponse;
import java.util.List;
import java.util.Map;

public record DashboardResponse(long totalApplications, Map<ApplicationStatus, Long> statusCounts,
        long applicationsThisWeek, long applicationsThisMonth, double responseRate, double interviewRate,
        double offerRate, List<ApplicationResponse> upcomingInterviews,
        List<ApplicationResponse> recentApplications, long unreadNotifications,
        Map<JobStatus, Long> jobQueue, List<NotificationResponse> recentNotifications) { }
