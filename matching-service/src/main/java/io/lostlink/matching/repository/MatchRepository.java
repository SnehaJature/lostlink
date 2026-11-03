package io.lostlink.matching.repository;

import io.lostlink.matching.domain.MatchRecord;
import io.lostlink.matching.domain.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<MatchRecord, UUID> {
    boolean existsByItemOneAndItemTwo(UUID itemOne, UUID itemTwo);
    List<MatchRecord> findByItemOneOrItemTwoOrderByScoreDesc(UUID itemOne, UUID itemTwo);
    List<MatchRecord> findByStatusInOrderByCreatedAtDesc(List<MatchStatus> statuses);
}
