package com.digiwork.taskhive.module.audit.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.dto.AuditLogResponse;
import com.digiwork.taskhive.module.audit.dto.AuditSearchRequest;
import com.digiwork.taskhive.module.audit.dto.SecurityEventResponse;
import com.digiwork.taskhive.module.audit.model.AuditLog;
import com.digiwork.taskhive.module.audit.model.SecurityEvent;
import com.digiwork.taskhive.module.audit.repository.AuditLogRepository;
import com.digiwork.taskhive.module.audit.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityEventRepository securityEventRepository;

    // ─── Log an audit action ─────────────────────────────────────────────────

    @Transactional
    public void logAction(UUID actorId, String actorEmail, String action,
            String entityType, UUID entityId,
            String beforeState, String afterState,
            String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .actorEmail(actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .beforeState(beforeState)
                .afterState(afterState)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, entityType={}, entityId={}", action, entityType, entityId);
    }

    // ─── Log a security event ────────────────────────────────────────────────

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void logSecurityEvent(String eventType, UUID userId, String ipAddress,
            boolean success, String details) {
        SecurityEvent event = SecurityEvent.builder()
                .eventType(eventType)
                .userId(userId)
                .ipAddress(ipAddress)
                .success(success)
                .details(details)
                .build();

        securityEventRepository.save(event);
        log.debug("Security event logged: type={}, userId={}, success={}", eventType, userId, success);
    }

    // ─── Get all logs (paginated) ────────────────────────────────────────────

    public PageResponse<AuditLogResponse> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logsPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return toAuditLogPageResponse(logsPage);
    }

    // ─── Search/filter logs ──────────────────────────────────────────────────

    public PageResponse<AuditLogResponse> searchLogs(AuditSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logsPage = auditLogRepository.searchLogs(
                request.getStartDate(),
                request.getEndDate(),
                request.getAction(),
                request.getEntityType(),
                request.getActorEmail(),
                request.getIpAddress(),
                pageable);
        return toAuditLogPageResponse(logsPage);
    }

    // ─── Entity-specific timeline ────────────────────────────────────────────

    public PageResponse<AuditLogResponse> getEntityTimeline(String entityType, UUID entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logsPage = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
        return toAuditLogPageResponse(logsPage);
    }

    // ─── Get security events (paginated) ─────────────────────────────────────

    public PageResponse<SecurityEventResponse> getSecurityEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityEvent> eventsPage = securityEventRepository.findAllByOrderByTimestampDesc(pageable);
        return toSecurityEventPageResponse(eventsPage);
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private PageResponse<AuditLogResponse> toAuditLogPageResponse(Page<AuditLog> page) {
        return PageResponse.<AuditLogResponse>builder()
                .content(page.getContent().stream().map(this::toAuditLogResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .actorEmail(log.getActorEmail())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .beforeState(log.getBeforeState())
                .afterState(log.getAfterState())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private PageResponse<SecurityEventResponse> toSecurityEventPageResponse(Page<SecurityEvent> page) {
        return PageResponse.<SecurityEventResponse>builder()
                .content(page.getContent().stream().map(this::toSecurityEventResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private SecurityEventResponse toSecurityEventResponse(SecurityEvent event) {
        return SecurityEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .userId(event.getUserId())
                .ipAddress(event.getIpAddress())
                .success(event.getSuccess())
                .details(event.getDetails())
                .timestamp(event.getTimestamp())
                .build();
    }
}
