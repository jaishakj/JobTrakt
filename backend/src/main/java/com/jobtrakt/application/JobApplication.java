package com.jobtrakt.application;

import com.jobtrakt.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "job_applications")
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 160) private String company;
    @Column(nullable = false, length = 160) private String position;
    @Column(length = 160) private String location;
    @Enumerated(EnumType.STRING) @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;
    @Column(precision = 12, scale = 2) private BigDecimal salary;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ApplicationStatus status;
    @Column(name = "applied_date", nullable = false) private LocalDate appliedDate;
    @Column(name = "interview_date") private Instant interviewDate;
    @Column(name = "follow_up_date") private Instant followUpDate;
    @Column(name = "job_url", length = 2048) private String jobUrl;
    @Column(columnDefinition = "text") private String notes;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected JobApplication() { }
    public JobApplication(User user) { this.user = user; }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public void update(ApplicationData data) {
        company = data.company().trim(); position = data.position().trim();
        location = clean(data.location()); employmentType = data.employmentType(); salary = data.salary();
        status = data.status(); appliedDate = data.appliedDate();
        interviewDate = databasePrecision(data.interviewDate());
        followUpDate = databasePrecision(data.followUpDate()); jobUrl = clean(data.jobUrl()); notes = clean(data.notes());
    }
    private Instant databasePrecision(Instant value) { return value == null ? null : value.truncatedTo(ChronoUnit.MICROS); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getCompany() { return company; }
    public String getPosition() { return position; }
    public String getLocation() { return location; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public BigDecimal getSalary() { return salary; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public Instant getInterviewDate() { return interviewDate; }
    public Instant getFollowUpDate() { return followUpDate; }
    public String getJobUrl() { return jobUrl; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
