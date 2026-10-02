package com.jobtrakt.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) { }
