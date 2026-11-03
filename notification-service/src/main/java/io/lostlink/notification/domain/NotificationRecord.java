package io.lostlink.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = @Index(name = "idx_notification_user_created", columnList = "user_id,created_at"))
public class NotificationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(nullable = false, length = 40)
    private String type;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 2000)
    private String message;
    @Column(name = "related_entity_id")
    private UUID relatedEntityId;
    @Column(name = "read_flag", nullable = false)
    private boolean read;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected NotificationRecord() {}
    public NotificationRecord(UUID userId, String type, String title, String message, UUID relatedEntityId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public void markRead() { read = true; }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public UUID getRelatedEntityId() { return relatedEntityId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}
