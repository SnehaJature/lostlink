package io.lostlink.matching.repository;

import io.lostlink.matching.domain.StoredItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoredItemRepository extends JpaRepository<StoredItem, UUID> {
    List<StoredItem> findByStatus(String status);
}
