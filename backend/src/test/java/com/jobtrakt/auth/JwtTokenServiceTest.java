package com.jobtrakt.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {
    private final JwtTokenService tokens = new JwtTokenService(new JwtProperties(
            "unit-test-secret-with-more-than-thirty-two-characters", 3600));

    @Test
    void generatedTokenHasExpectedSubjectAndValidSignature() {
        String token = tokens.generateToken("sam@example.com");

        assertEquals("sam@example.com", tokens.extractSubject(token));
        assertTrue(tokens.isValid(token, "sam@example.com"));
        assertFalse(tokens.isValid(token, "other@example.com"));
    }

    @Test
    void rejectsTamperedToken() {
        String token = tokens.generateToken("sam@example.com");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(tokens.isValid(tampered, "sam@example.com"));
    }
}
