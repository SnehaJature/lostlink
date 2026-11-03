package io.lostlink.auth.service;

import io.lostlink.auth.domain.UserAccount;
import io.lostlink.auth.domain.UserRole;
import io.lostlink.auth.dto.LoginRequest;
import io.lostlink.auth.dto.RegisterRequest;
import io.lostlink.auth.repository.UserRepository;
import io.lostlink.common.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.nullable;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService service = new AuthService(users, jwtService, 3_600_000);

    @Test
    void storesOnlyBcryptPasswordAndReturnsBearerToken() {
        when(users.existsByEmail("person@example.com")).thenReturn(false);
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createToken(nullable(java.util.UUID.class), eq("person@example.com"), eq("USER"))).thenReturn("signed-token");

        var response = service.register(new RegisterRequest("Example User", "Person@Example.com", "strong-pass-1"));

        var captor = org.mockito.ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(captor.capture());
        assertNotEquals("strong-pass-1", captor.getValue().getPassword());
        assertTrue(captor.getValue().getPassword().startsWith("$2"));
        assertEquals("signed-token", response.accessToken());
        assertEquals("person@example.com", response.user().email());
    }

    @Test
    void rejectsDuplicateEmailRegistration() {
        when(users.existsByEmail("person@example.com")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> service.register(
                new RegisterRequest("Example User", "person@example.com", "strong-pass-1")));
        verify(users, never()).save(any());
    }

    @Test
    void acceptsValidPasswordAndRejectsInvalidPassword() {
        String encoded = new BCryptPasswordEncoder().encode("strong-pass-1");
        UserAccount account = new UserAccount("Example User", "person@example.com", encoded, UserRole.USER);
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(account));
        when(jwtService.createToken(nullable(java.util.UUID.class), eq("person@example.com"), eq("USER"))).thenReturn("signed-token");

        assertEquals("signed-token", service.login(new LoginRequest("person@example.com", "strong-pass-1")).accessToken());
        assertThrows(ResponseStatusException.class, () -> service.login(
                new LoginRequest("person@example.com", "wrong-pass")));
    }
}
