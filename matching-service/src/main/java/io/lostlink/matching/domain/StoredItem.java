package io.lostlink.matching.domain;

import io.lostlink.common.dto.ItemSnapshot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_snapshots", indexes = @Index(name = "idx_snapshot_type_status", columnList = "item_type,status"))
public class StoredItem {
    @Id
    private UUID id;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 5000) private String description;
    @Column(nullable = false, length = 24) private String category;
    @Column(name = "item_type", nullable = false, length = 12) private String itemType;
    @Column(nullable = false, length = 240) private String location;
    @Column(name = "item_date", nullable = false) private LocalDate itemDate;
    @Column(length = 2048) private String imageUrl;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "reported_by", nullable = false) private UUID reportedBy;

    protected StoredItem() {}

    public static StoredItem from(ItemSnapshot item) {
        StoredItem stored = new StoredItem();
        stored.id = item.id();
        stored.title = item.title();
        stored.description = item.description();
        stored.category = item.category();
        stored.itemType = item.itemType();
        stored.location = item.location();
        stored.itemDate = item.itemDate();
        stored.imageUrl = item.imageUrl();
        stored.status = item.status();
        stored.reportedBy = item.reportedBy();
        return stored;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getItemType() { return itemType; }
    public String getLocation() { return location; }
    public LocalDate getItemDate() { return itemDate; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public UUID getReportedBy() { return reportedBy; }
}
