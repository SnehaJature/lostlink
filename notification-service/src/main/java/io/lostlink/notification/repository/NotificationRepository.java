package io.lostlink.notification.repository;

import io.lostlink.notification.domain.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationRecord, UUID> {
    List<NotificationRecord> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<NotificationRecord> findByIdAndUserId(UUID id, UUID userId);
    List<NotificationRecord> findByUserIdAndReadFalse(UUID userId);
}
