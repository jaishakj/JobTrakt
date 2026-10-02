package com.jobtrakt.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record PasswordChangeRequest(@NotBlank String currentPassword,
        @NotBlank @Size(min=10,max=72) String newPassword) { }
