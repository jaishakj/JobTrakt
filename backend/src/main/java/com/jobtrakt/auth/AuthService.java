package com.jobtrakt.auth;

import com.jobtrakt.auth.dto.AuthResponse;
import com.jobtrakt.auth.dto.LoginRequest;
import com.jobtrakt.auth.dto.RegisterRequest;
import com.jobtrakt.auth.dto.ProfileUpdateRequest;
import com.jobtrakt.auth.dto.PasswordChangeRequest;
import com.jobtrakt.auth.dto.UserResponse;
import com.jobtrakt.common.exception.DuplicateEmailException;
import com.jobtrakt.common.exception.InvalidCredentialsException;
import com.jobtrakt.common.exception.InvalidRequestException;
import com.jobtrakt.user.User;
import com.jobtrakt.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {
    private static final String DUMMY_PASSWORD_HASH = "$2b$12$89dp4NjXMrSuCg8Umyb5je.ysziUwfglu48dCCXjTSScZ8DTPJVNK";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtTokenService tokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = users.findById(userId).orElseThrow(() -> new com.jobtrakt.common.exception.ResourceNotFoundException("User"));
        user.updateName(request.name());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = users.findById(userId).orElseThrow(() -> new com.jobtrakt.common.exception.ResourceNotFoundException("User"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        ensureBcryptCanEncode(request.newPassword());
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        ensureBcryptCanEncode(request.password());
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = users.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password())));
        return createAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = users.findByEmail(email).orElse(null);
        String passwordHash = user == null ? DUMMY_PASSWORD_HASH : user.getPasswordHash();
        boolean withinBcryptLimit = request.password().getBytes(StandardCharsets.UTF_8).length <= 72;
        boolean passwordMatches = withinBcryptLimit
                && passwordEncoder.matches(request.password(), passwordHash);
        if (!withinBcryptLimit) {
            passwordEncoder.matches("dummy-auth-password", DUMMY_PASSWORD_HASH);
        }
        if (user == null || !passwordMatches) {
            throw new InvalidCredentialsException();
        }
        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        return new AuthResponse(tokens.generateToken(user.getEmail()), "Bearer",
                tokens.getExpirationSeconds(), UserResponse.from(user));
    }

    private void ensureBcryptCanEncode(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidRequestException("Password must be at most 72 UTF-8 bytes");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
