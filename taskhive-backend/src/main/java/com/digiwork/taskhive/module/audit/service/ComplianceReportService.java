package com.digiwork.taskhive.module.audit.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.dto.AuditLogResponse;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceReportService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityEventRepository securityEventRepository;

    // ─── User Access Report ──────────────────────────────────────────────────
    // All login events, role assignments, account activations for a time range

    private static final List<String> USER_ACCESS_ACTIONS = List.of(
            "USER_LOGIN", "USER_LOGOUT", "ACCOUNT_ACTIVATED",
            "PASSWORD_CHANGED", "PASSWORD_RESET_REQUESTED");

    public PageResponse<AuditLogResponse> getUserAccessReport(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logsPage = auditLogRepository.findByActionInAndDateRange(
                USER_ACCESS_ACTIONS, startDate, endDate, pageable);
        return toAuditLogPageResponse(logsPage);
    }

    public String getUserAccessReportCsv(LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<AuditLog> logsPage = auditLogRepository.findByActionInAndDateRange(
                USER_ACCESS_ACTIONS, startDate, endDate, pageable);
        return toAuditLogCsv(logsPage.getContent());
    }

    // ─── Data Modification Report ────────────────────────────────────────────
    // All entity create/update/delete events for a time range

    private static final List<String> DATA_MODIFICATION_ACTIONS = List.of(
            "EMPLOYEE_CREATED", "EMPLOYEE_UPDATED", "EMPLOYEE_ACTIVATED",
            "EMPLOYEE_DEACTIVATED", "EMPLOYEE_DELETED",
            "TASK_CREATED", "TASK_UPDATED", "TASK_STATUS_CHANGED",
            "TASK_ASSIGNED", "TASK_COMMENT_ADDED");

    public PageResponse<AuditLogResponse> getDataModificationReport(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logsPage = auditLogRepository.findByActionInAndDateRange(
                DATA_MODIFICATION_ACTIONS, startDate, endDate, pageable);
        return toAuditLogPageResponse(logsPage);
    }

    public String getDataModificationReportCsv(LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<AuditLog> logsPage = auditLogRepository.findByActionInAndDateRange(
                DATA_MODIFICATION_ACTIONS, startDate, endDate, pageable);
        return toAuditLogCsv(logsPage.getContent());
    }

    // ─── Security Incident Report ────────────────────────────────────────────
    // All failed logins, lockouts, unauthorized access events

    public PageResponse<SecurityEventResponse> getSecurityIncidentReport(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityEvent> eventsPage = securityEventRepository.findByDateRange(
                startDate, endDate, pageable);
        return toSecurityEventPageResponse(eventsPage);
    }

    public String getSecurityIncidentReportCsv(LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<SecurityEvent> eventsPage = securityEventRepository.findByDateRange(
                startDate, endDate, pageable);
        return toSecurityEventCsv(eventsPage.getContent());
    }

    // ─── CSV Generation ──────────────────────────────────────────────────────

    private String toAuditLogCsv(List<AuditLog> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "ID,Actor ID,Actor Email,Action,Entity Type,Entity ID,Before State,After State,IP Address,User Agent,Created At\n");
        for (AuditLog l : logs) {
            sb.append(csvValue(l.getId()))
                    .append(',').append(csvValue(l.getActorId()))
                    .append(',').append(csvValue(l.getActorEmail()))
                    .append(',').append(csvValue(l.getAction()))
                    .append(',').append(csvValue(l.getEntityType()))
                    .append(',').append(csvValue(l.getEntityId()))
                    .append(',').append(csvValue(l.getBeforeState()))
                    .append(',').append(csvValue(l.getAfterState()))
                    .append(',').append(csvValue(l.getIpAddress()))
                    .append(',').append(csvValue(l.getUserAgent()))
                    .append(',').append(csvValue(l.getCreatedAt()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String toSecurityEventCsv(List<SecurityEvent> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Event Type,User ID,IP Address,Success,Details,Timestamp\n");
        for (SecurityEvent e : events) {
            sb.append(csvValue(e.getId()))
                    .append(',').append(csvValue(e.getEventType()))
                    .append(',').append(csvValue(e.getUserId()))
                    .append(',').append(csvValue(e.getIpAddress()))
                    .append(',').append(csvValue(e.getSuccess()))
                    .append(',').append(csvValue(e.getDetails()))
                    .append(',').append(csvValue(e.getTimestamp()))
                    .append('\n');
        }
        return sb.toString();
    }

    private String csvValue(Object value) {
        if (value == null)
            return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
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
