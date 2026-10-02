package com.jobtrakt.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ApplicationData(String company, String position, String location, EmploymentType employmentType,
                              BigDecimal salary, ApplicationStatus status, LocalDate appliedDate,
                              Instant interviewDate, Instant followUpDate, String jobUrl, String notes) { }
