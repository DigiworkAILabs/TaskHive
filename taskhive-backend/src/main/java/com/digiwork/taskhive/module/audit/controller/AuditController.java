package com.digiwork.taskhive.module.audit.controller;

import com.digiwork.taskhive.common.dto.ApiResponse;
import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.dto.AuditLogResponse;
import com.digiwork.taskhive.module.audit.dto.AuditSearchRequest;
import com.digiwork.taskhive.module.audit.dto.SecurityEventResponse;
import com.digiwork.taskhive.module.audit.service.AuditService;
import com.digiwork.taskhive.module.audit.service.ComplianceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final ComplianceReportService complianceReportService;

    // ─── GET /api/v1/audit/logs ──────────────────────────────────────────────

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getAllLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", logs));
    }

    // ─── GET /api/v1/audit/logs/search ───────────────────────────────────────

    @GetMapping("/logs/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> searchLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AuditSearchRequest request = AuditSearchRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .action(action)
                .entityType(entityType)
                .actorEmail(actorEmail)
                .ipAddress(ipAddress)
                .build();

        PageResponse<AuditLogResponse> logs = auditService.searchLogs(request, page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs search results", logs));
    }

    // ─── GET /api/v1/audit/logs/entity/{type}/{id} ───────────────────────────

    @GetMapping("/logs/entity/{type}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getEntityTimeline(
            @PathVariable String type,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AuditLogResponse> logs = auditService.getEntityTimeline(type, id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Entity timeline retrieved successfully", logs));
    }

    // ─── GET /api/v1/audit/security-events ───────────────────────────────────

    @GetMapping("/security-events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<SecurityEventResponse>>> getSecurityEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<SecurityEventResponse> events = auditService.getSecurityEvents(page, size);
        return ResponseEntity.ok(ApiResponse.success("Security events retrieved successfully", events));
    }

    // ─── GET /api/v1/audit/compliance/report ─────────────────────────────────

    @GetMapping("/compliance/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getComplianceReport(
            @RequestParam String reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if ("csv".equalsIgnoreCase(format)) {
            return handleCsvExport(reportType, startDate, endDate);
        }

        return handleJsonReport(reportType, startDate, endDate, page, size);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private ResponseEntity<?> handleJsonReport(String reportType, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size) {
        return switch (reportType.toUpperCase()) {
            case "USER_ACCESS" -> {
                var report = complianceReportService.getUserAccessReport(startDate, endDate, page, size);
                yield ResponseEntity.ok(ApiResponse.success("User Access Report generated", report));
            }
            case "DATA_MODIFICATION" -> {
                var report = complianceReportService.getDataModificationReport(startDate, endDate, page, size);
                yield ResponseEntity.ok(ApiResponse.success("Data Modification Report generated", report));
            }
            case "SECURITY_INCIDENT" -> {
                var report = complianceReportService.getSecurityIncidentReport(startDate, endDate, page, size);
                yield ResponseEntity.ok(ApiResponse.success("Security Incident Report generated", report));
            }
            default -> ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Invalid report type. Valid types: USER_ACCESS, DATA_MODIFICATION, SECURITY_INCIDENT"));
        };
    }

    private ResponseEntity<byte[]> handleCsvExport(String reportType, LocalDateTime startDate,
            LocalDateTime endDate) {
        String csvContent = switch (reportType.toUpperCase()) {
            case "USER_ACCESS" -> complianceReportService.getUserAccessReportCsv(startDate, endDate);
            case "DATA_MODIFICATION" -> complianceReportService.getDataModificationReportCsv(startDate, endDate);
            case "SECURITY_INCIDENT" -> complianceReportService.getSecurityIncidentReportCsv(startDate, endDate);
            default -> null;
        };

        if (csvContent == null) {
            return ResponseEntity.badRequest().build();
        }

        String filename = reportType.toLowerCase() + "_report.csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent.getBytes());
    }
}
