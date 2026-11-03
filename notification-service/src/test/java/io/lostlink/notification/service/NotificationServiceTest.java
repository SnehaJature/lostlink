package io.lostlink.notification.service;

import io.lostlink.notification.domain.NotificationRecord;
import io.lostlink.notification.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final NotificationService service = new NotificationService(notifications);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void marksOnlyCurrentUsersNotificationAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationRecord notification = new NotificationRecord(userId, "POSSIBLE_MATCH",
                "Possible match", "A match was found", UUID.randomUUID());
        authenticate(userId);
        when(notifications.findByIdAndUserId(notificationId, userId)).thenReturn(Optional.of(notification));
        when(notifications.save(notification)).thenReturn(notification);

        var response = service.markRead(notificationId);

        assertTrue(response.read());
    }

    @Test
    void doesNotAllowReadingAnotherUsersNotification() {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        when(notifications.findByIdAndUserId(any(), eq(userId))).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.markRead(UUID.randomUUID()));

        assertEquals(404, error.getStatusCode().value());
    }

    private void authenticate(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
