package io.lostlink.item.repository;

import io.lostlink.item.domain.ItemCategory;
import io.lostlink.item.domain.ItemReport;
import io.lostlink.item.domain.ItemStatus;
import io.lostlink.item.domain.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ItemRepository extends JpaRepository<ItemReport, UUID> {
    Page<ItemReport> findByReportedBy(UUID reportedBy, Pageable pageable);

    @Query("""
            select i from ItemReport i
            where (:category is null or i.category = :category)
              and (:itemType is null or i.itemType = :itemType)
              and (:status is null or i.status = :status)
              and (:location is null or lower(i.location) like lower(concat('%', :location, '%')))
              and (:term is null or lower(i.title) like lower(concat('%', :term, '%'))
                   or lower(i.description) like lower(concat('%', :term, '%')))
            """)
    Page<ItemReport> search(@Param("category") ItemCategory category, @Param("itemType") ItemType itemType,
                            @Param("status") ItemStatus status, @Param("location") String location,
                            @Param("term") String term, Pageable pageable);
}
