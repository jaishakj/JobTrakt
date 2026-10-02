package com.jobtrakt.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.jobtrakt.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthApiIntegrationTest {
    @Autowired private TestRestTemplate http;
    @Autowired private UserRepository users;

    @BeforeEach
    void clearUsers() { users.deleteAll(); }

    @Test
    void registersUserWithHashedPasswordAndAllowsAuthenticatedMe() throws Exception {
        ResponseEntity<JsonNode> registration = post("/api/auth/register",
                new RegisterPayload("Sam", "Sam@example.com", "long-password-123"));
        org.junit.jupiter.api.Assertions.assertEquals(201, registration.getStatusCode().value());
        JsonNode body = registration.getBody();
        org.junit.jupiter.api.Assertions.assertNotNull(body);
        org.junit.jupiter.api.Assertions.assertEquals("Bearer", body.path("tokenType").asText());
        org.junit.jupiter.api.Assertions.assertEquals("sam@example.com", body.path("user").path("email").asText());
        String token = body.path("accessToken").asText();
        var savedUser = users.findByEmail("sam@example.com").orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotEquals("long-password-123", savedUser.getPasswordHash());
        org.junit.jupiter.api.Assertions.assertTrue(savedUser.getPasswordHash().startsWith("$2"));

        JsonNode current = get("/api/auth/me", token).getBody();
        org.junit.jupiter.api.Assertions.assertNotNull(current);
        org.junit.jupiter.api.Assertions.assertEquals(savedUser.getId().intValue(), current.path("id").asInt());
        org.junit.jupiter.api.Assertions.assertEquals("sam@example.com", current.path("email").asText());
    }

    @Test
    void duplicateEmailIsConflict() throws Exception {
        register("sam@example.com", "long-password-123");

        ResponseEntity<JsonNode> duplicate = post("/api/auth/register",
                new RegisterPayload("Sam Two", "SAM@example.com", "another-password-123"));
        org.junit.jupiter.api.Assertions.assertEquals(409, duplicate.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("DUPLICATE_EMAIL", duplicate.getBody().path("error").asText());
    }

    @Test
    void loginAcceptsCorrectPasswordAndRejectsIncorrectPassword() throws Exception {
        register("sam@example.com", "long-password-123");

        ResponseEntity<JsonNode> login = post("/api/auth/login", new LoginPayload("sam@example.com", "long-password-123"));
        org.junit.jupiter.api.Assertions.assertEquals(200, login.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertFalse(login.getBody().path("accessToken").asText().isBlank());

        ResponseEntity<JsonNode> wrongPassword = post("/api/auth/login", new LoginPayload("sam@example.com", "bad-password"));
        org.junit.jupiter.api.Assertions.assertEquals(401, wrongPassword.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("INVALID_CREDENTIALS", wrongPassword.getBody().path("error").asText());

        ResponseEntity<JsonNode> unknownUser = post("/api/auth/login", new LoginPayload("missing@example.com", "long-password-123"));
        org.junit.jupiter.api.Assertions.assertEquals(401, unknownUser.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("INVALID_CREDENTIALS", unknownUser.getBody().path("error").asText());
    }

    @Test
    void protectedRoutesRejectMissingOrMalformedToken() throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals(200, get("/api/health", null).getStatusCode().value());
        ResponseEntity<JsonNode> missing = get("/api/auth/me", null);
        org.junit.jupiter.api.Assertions.assertEquals(401, missing.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("UNAUTHORIZED", missing.getBody().path("error").asText());
        ResponseEntity<JsonNode> malformed = get("/api/future-resource", "not-a-jwt");
        org.junit.jupiter.api.Assertions.assertEquals(401, malformed.getStatusCode().value());
    }

    @Test
    void registrationValidatesFieldsAndReportsCleanError() throws Exception {
        ResponseEntity<JsonNode> invalid = post("/api/auth/register",
                new RegisterPayload(" ", "invalid", "short"));
        org.junit.jupiter.api.Assertions.assertEquals(400, invalid.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("VALIDATION_ERROR", invalid.getBody().path("error").asText());
        org.junit.jupiter.api.Assertions.assertTrue(invalid.getBody().path("message").asText().contains("email"));
    }

    private void register(String email, String password) throws Exception {
        ResponseEntity<JsonNode> response = post("/api/auth/register", new RegisterPayload("Sam", email, password));
        org.junit.jupiter.api.Assertions.assertEquals(201, response.getStatusCode().value());
    }

    private ResponseEntity<JsonNode> post(String path, Object body) {
        return http.postForEntity(path, body, JsonNode.class);
    }

    private ResponseEntity<JsonNode> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) headers.setBearerAuth(token);
        return http.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
    }

    private record RegisterPayload(String name, String email, String password) { }
    private record LoginPayload(String email, String password) { }
}
