package com.digiwork.taskhive.module.audit.repository;

import com.digiwork.taskhive.module.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

        Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

        Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        String entityType, UUID entityId, Pageable pageable);

        @Query("SELECT a FROM AuditLog a WHERE " +
                        "(CAST(:startDate AS timestamp) IS NULL OR a.createdAt >= :startDate) AND " +
                        "(CAST(:endDate AS timestamp) IS NULL OR a.createdAt <= :endDate) AND " +
                        "(COALESCE(:action, '') = '' OR a.action = :action) AND " +
                        "(COALESCE(:entityType, '') = '' OR a.entityType = :entityType) AND " +
                        "(COALESCE(:actorEmail, '') = '' OR a.actorEmail = :actorEmail) AND " +
                        "(COALESCE(:ipAddress, '') = '' OR a.ipAddress = :ipAddress) " +
                        "ORDER BY a.createdAt DESC")
        Page<AuditLog> searchLogs(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("action") String action,
                        @Param("entityType") String entityType,
                        @Param("actorEmail") String actorEmail,
                        @Param("ipAddress") String ipAddress,
                        Pageable pageable);

        @Query("SELECT a FROM AuditLog a WHERE " +
                        "a.action IN :actions AND " +
                        "(CAST(:startDate AS timestamp) IS NULL OR a.createdAt >= :startDate) AND " +
                        "(CAST(:endDate AS timestamp) IS NULL OR a.createdAt <= :endDate) " +
                        "ORDER BY a.createdAt DESC")
        Page<AuditLog> findByActionInAndDateRange(
                        @Param("actions") java.util.List<String> actions,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
}
