package com.digiwork.taskhive.module.audit.repository;

import com.digiwork.taskhive.module.audit.model.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

        Page<SecurityEvent> findAllByOrderByTimestampDesc(Pageable pageable);

        @Query("SELECT s FROM SecurityEvent s WHERE " +
                        "(CAST(:startDate AS timestamp) IS NULL OR s.timestamp >= :startDate) AND " +
                        "(CAST(:endDate AS timestamp) IS NULL OR s.timestamp <= :endDate) " +
                        "ORDER BY s.timestamp DESC")
        Page<SecurityEvent> findByDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
}
