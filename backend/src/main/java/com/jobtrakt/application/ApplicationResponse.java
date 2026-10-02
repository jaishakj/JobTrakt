package com.jobtrakt.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ApplicationResponse(Long id, String company, String position, String location,
        EmploymentType employmentType, BigDecimal salary, ApplicationStatus status, LocalDate appliedDate,
        Instant interviewDate, Instant followUpDate, String jobUrl, String notes, Instant createdAt, Instant updatedAt) {
    public static ApplicationResponse from(JobApplication application) {
        return new ApplicationResponse(application.getId(), application.getCompany(), application.getPosition(),
                application.getLocation(), application.getEmploymentType(), application.getSalary(),
                application.getStatus(), application.getAppliedDate(), application.getInterviewDate(),
                application.getFollowUpDate(), application.getJobUrl(), application.getNotes(),
                application.getCreatedAt(), application.getUpdatedAt());
    }
}
