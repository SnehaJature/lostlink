package io.lostlink.auth.service;

import io.lostlink.auth.domain.UserAccount;
import io.lostlink.auth.domain.UserRole;
import io.lostlink.auth.dto.AuthResponse;
import io.lostlink.auth.dto.LoginRequest;
import io.lostlink.auth.dto.RegisterRequest;
import io.lostlink.auth.dto.UserResponse;
import io.lostlink.auth.repository.UserRepository;
import io.lostlink.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;
    private final long expirationMillis;

    public AuthService(UserRepository users, JwtService jwtService,
                       @Value("${app.jwt.expiration-ms:3600000}") long expirationMillis) {
        this.users = users;
        this.jwtService = jwtService;
        this.expirationMillis = expirationMillis;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        UserAccount user = users.save(new UserAccount(request.fullName().trim(), email,
                passwordEncoder.encode(request.password()), UserRole.USER));
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        UserAccount user = users.findByEmail(email)
                .filter(account -> passwordEncoder.matches(request.password(), account.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return users.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AuthResponse tokenFor(UserAccount user) {
        return new AuthResponse(jwtService.createToken(user.getId(), user.getEmail(), user.getRole().name()),
                "Bearer", expirationMillis / 1000, UserResponse.from(user));
    }
}
