package io.lostlink.item.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_item_reported_by", columnList = "reported_by"),
        @Index(name = "idx_item_type_category_status", columnList = "item_type,category,status"),
        @Index(name = "idx_item_date", columnList = "item_date")
})
public class ItemReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, length = 5000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ItemCategory category;
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 12)
    private ItemType itemType;
    @Column(nullable = false, length = 240)
    private String location;
    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;
    @Column(length = 2048)
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ItemStatus status = ItemStatus.OPEN;
    @Column(name = "reported_by", nullable = false)
    private UUID reportedBy;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected ItemReport() {}

    public ItemReport(String title, String description, ItemCategory category, ItemType itemType,
                      String location, LocalDate itemDate, String imageUrl, UUID reportedBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.itemType = itemType;
        this.location = location;
        this.itemDate = itemDate;
        this.imageUrl = imageUrl;
        this.reportedBy = reportedBy;
    }

    public void update(String title, String description, ItemCategory category, ItemType itemType,
                       String location, LocalDate itemDate, String imageUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.itemType = itemType;
        this.location = location;
        this.itemDate = itemDate;
        this.imageUrl = imageUrl;
    }

    public void setStatus(ItemStatus status) { this.status = status; }
    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ItemCategory getCategory() { return category; }
    public ItemType getItemType() { return itemType; }
    public String getLocation() { return location; }
    public LocalDate getItemDate() { return itemDate; }
    public String getImageUrl() { return imageUrl; }
    public ItemStatus getStatus() { return status; }
    public UUID getReportedBy() { return reportedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
