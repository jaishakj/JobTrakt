package com.jobtrakt.application;

import com.jobtrakt.job.JobResponse;
import com.jobtrakt.notification.NotificationResponse;
import java.util.List;

public record ApplicationDetailResponse(ApplicationResponse application, List<ApplicationTimelineItem> timeline,
                                        List<JobResponse> jobs, List<NotificationResponse> notifications) { }
