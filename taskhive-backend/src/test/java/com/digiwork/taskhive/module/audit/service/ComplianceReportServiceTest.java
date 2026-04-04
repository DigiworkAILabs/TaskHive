package com.digiwork.taskhive.module.audit.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.dto.AuditLogResponse;
import com.digiwork.taskhive.module.audit.dto.SecurityEventResponse;
import com.digiwork.taskhive.module.audit.model.AuditLog;
import com.digiwork.taskhive.module.audit.model.SecurityEvent;
import com.digiwork.taskhive.module.audit.repository.AuditLogRepository;
import com.digiwork.taskhive.module.audit.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplianceReportServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityEventRepository securityEventRepository;

    @InjectMocks
    private ComplianceReportService complianceReportService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private AuditLog sampleAuditLog;
    private SecurityEvent sampleSecurityEvent;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();

        sampleAuditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .actorId(UUID.randomUUID())
                .actorEmail("test@example.com")
                .action("USER_LOGIN")
                .entityType("USER")
                .ipAddress("192.168.1.1")
                .createdAt(LocalDateTime.now())
                .build();

        sampleSecurityEvent = SecurityEvent.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .eventType("FAILED_LOGIN")
                .success(false)
                .ipAddress("10.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getUserAccessReport should return mapped page response")
    void getUserAccessReport() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog));
        when(auditLogRepository.findByActionInAndDateRange(any(), eq(startDate), eq(endDate), any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<AuditLogResponse> response = complianceReportService.getUserAccessReport(startDate, endDate, 0, 10);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getAction()).isEqualTo("USER_LOGIN");
    }

    @Test
    @DisplayName("getUserAccessReportCsv should return formatted CSV")
    void getUserAccessReportCsv() {
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog));
        when(auditLogRepository.findByActionInAndDateRange(any(), eq(startDate), eq(endDate), any(PageRequest.class)))
                .thenReturn(page);

        String csv = complianceReportService.getUserAccessReportCsv(startDate, endDate);

        assertThat(csv)
                .isNotNull()
                .contains("USER_LOGIN")
                .contains("test@example.com");
    }

    @Test
    @DisplayName("getDataModificationReport should return mapped page response")
    void getDataModificationReport() {
        sampleAuditLog.setAction("TASK_CREATED");
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog));
        when(auditLogRepository.findByActionInAndDateRange(any(), eq(startDate), eq(endDate), any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<AuditLogResponse> response = complianceReportService.getDataModificationReport(startDate, endDate, 0, 10);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getAction()).isEqualTo("TASK_CREATED");
    }

    @Test
    @DisplayName("getDataModificationReportCsv should return formatted CSV")
    void getDataModificationReportCsv() {
        sampleAuditLog.setAction("EMPLOYEE_UPDATED");
        Page<AuditLog> page = new PageImpl<>(List.of(sampleAuditLog));
        when(auditLogRepository.findByActionInAndDateRange(any(), eq(startDate), eq(endDate), any(PageRequest.class)))
                .thenReturn(page);

        String csv = complianceReportService.getDataModificationReportCsv(startDate, endDate);

        assertThat(csv)
                .isNotNull()
                .contains("EMPLOYEE_UPDATED");
    }

    @Test
    @DisplayName("getSecurityIncidentReport should return mapped page response")
    void getSecurityIncidentReport() {
        Page<SecurityEvent> page = new PageImpl<>(List.of(sampleSecurityEvent));
        when(securityEventRepository.findByDateRange(eq(startDate), eq(endDate), any(PageRequest.class)))
                .thenReturn(page);

        PageResponse<SecurityEventResponse> response = complianceReportService.getSecurityIncidentReport(startDate, endDate, 0, 10);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getEventType()).isEqualTo("FAILED_LOGIN");
    }

    @Test
    @DisplayName("getSecurityIncidentReportCsv should return formatted CSV")
    void getSecurityIncidentReportCsv() {
        Page<SecurityEvent> page = new PageImpl<>(List.of(sampleSecurityEvent));
        when(securityEventRepository.findByDateRange(eq(startDate), eq(endDate), any(PageRequest.class)))
                .thenReturn(page);

        String csv = complianceReportService.getSecurityIncidentReportCsv(startDate, endDate);

        assertThat(csv)
                .isNotNull()
                .contains("FAILED_LOGIN")
                .contains("10.0.0.1");
    }
}
