package io.lostlink.notification.service;

import io.lostlink.common.security.CurrentUser;
import io.lostlink.notification.domain.NotificationRecord;
import io.lostlink.notification.dto.NotificationRequest;
import io.lostlink.notification.dto.NotificationResponse;
import io.lostlink.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        NotificationRecord saved = notifications.save(new NotificationRecord(request.userId(), request.type(),
                request.title(), request.message(), request.relatedEntityId()));
        log.info("Notification {} queued for user {}: {}", saved.getId(), saved.getUserId(), saved.getTitle());
        return NotificationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> mine() {
        return notifications.findByUserIdOrderByCreatedAtDesc(CurrentUser.id()).stream()
                .map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID id) {
        NotificationRecord notification = notifications.findByIdAndUserId(id, CurrentUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.markRead();
        return NotificationResponse.from(notifications.save(notification));
    }

    @Transactional
    public int markAllRead() {
        List<NotificationRecord> unread = notifications.findByUserIdAndReadFalse(CurrentUser.id());
        unread.forEach(NotificationRecord::markRead);
        notifications.saveAll(unread);
        return unread.size();
    }
}
