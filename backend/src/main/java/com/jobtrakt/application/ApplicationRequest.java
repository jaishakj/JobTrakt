package com.jobtrakt.application;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ApplicationRequest(
        @NotBlank @Size(max = 160) String company,
        @NotBlank @Size(max = 160) String position,
        @Size(max = 160) String location,
        @NotNull EmploymentType employmentType,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal salary,
        @NotNull ApplicationStatus status,
        @NotNull @PastOrPresent LocalDate appliedDate,
        Instant interviewDate,
        Instant followUpDate,
        @Size(max = 2048) @Pattern(regexp = "^$|https?://.+", message = "must be an http or https URL") String jobUrl,
        @Size(max = 10000) String notes
) {
    public ApplicationData toData() {
        return new ApplicationData(company, position, location, employmentType, salary, status,
                appliedDate, interviewDate, followUpDate, jobUrl, notes);
    }
}
