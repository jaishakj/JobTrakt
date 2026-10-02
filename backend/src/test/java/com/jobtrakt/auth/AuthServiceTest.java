package com.jobtrakt.auth;

import com.jobtrakt.auth.dto.LoginRequest;
import com.jobtrakt.auth.dto.RegisterRequest;
import com.jobtrakt.common.exception.DuplicateEmailException;
import com.jobtrakt.common.exception.InvalidCredentialsException;
import com.jobtrakt.user.User;
import com.jobtrakt.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository users;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenService tokens;
    @InjectMocks private AuthService service;

    @Test
    void registerNormalizesEmailHashesPasswordAndReturnsToken() {
        when(users.existsByEmail("sam@example.com")).thenReturn(false);
        when(passwordEncoder.encode("long-password-123")).thenReturn("bcrypt-hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokens.generateToken("sam@example.com")).thenReturn("signed-token");
        when(tokens.getExpirationSeconds()).thenReturn(3600L);

        var response = service.register(new RegisterRequest(" Sam ", "SAM@Example.com", "long-password-123"));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(users).save(savedUser.capture());
        assertEquals("Sam", savedUser.getValue().getName());
        assertEquals("sam@example.com", savedUser.getValue().getEmail());
        assertNotEquals("long-password-123", savedUser.getValue().getPasswordHash());
        assertEquals("bcrypt-hash", savedUser.getValue().getPasswordHash());
        assertEquals("signed-token", response.accessToken());
    }

    @Test
    void registerRejectsExistingEmailBeforeSaving() {
        when(users.existsByEmail("sam@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.register(
                new RegisterRequest("Sam", "sam@example.com", "long-password-123")));
        verify(users, never()).save(any());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        User user = new User("Sam", "sam@example.com", "stored-hash");
        when(users.findByEmail("sam@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("sam@example.com", "wrong-password")));
    }

    @Test
    void loginRejectsPasswordLongerThanBcryptByteLimit() {
        String tooLongPassword = "a".repeat(73);
        when(users.findByEmail("sam@example.com")).thenReturn(Optional.of(
                new User("Sam", "sam@example.com", "stored-hash")));
        when(passwordEncoder.matches("dummy-auth-password", "$2b$12$89dp4NjXMrSuCg8Umyb5je.ysziUwfglu48dCCXjTSScZ8DTPJVNK"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("sam@example.com", tooLongPassword)));
    }
}
