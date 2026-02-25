package com.digiwork.taskhive.module.audit.service;

import com.digiwork.taskhive.common.dto.PageResponse;
import com.digiwork.taskhive.module.audit.dto.AuditLogResponse;
import com.digiwork.taskhive.module.audit.dto.AuditSearchRequest;
import com.digiwork.taskhive.module.audit.dto.SecurityEventResponse;
import com.digiwork.taskhive.module.audit.model.AuditLog;
import com.digiwork.taskhive.module.audit.model.SecurityEvent;
import com.digiwork.taskhive.module.audit.repository.AuditLogRepository;
import com.digiwork.taskhive.module.audit.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityEventRepository securityEventRepository;

    @InjectMocks
    private AuditService auditService;

    private UUID testUserId;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // logAction Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("logAction")
    class LogActionTests {

        @Test
        @DisplayName("should save audit log with all fields")
        void shouldSaveAuditLogWithAllFields() {
            // given
            String email = "admin@taskhive.com";
            String action = "EMPLOYEE_CREATED";
            String entityType = "EMPLOYEE";
            String beforeState = null;
            String afterState = "{\"name\":\"John\"}";
            String ipAddress = "192.168.1.1";
            String userAgent = "Mozilla/5.0";

            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            auditService.logAction(testUserId, email, action, entityType,
                    testEntityId, beforeState, afterState, ipAddress, userAgent);

            // then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getActorId()).isEqualTo(testUserId);
            assertThat(saved.getActorEmail()).isEqualTo(email);
            assertThat(saved.getAction()).isEqualTo(action);
            assertThat(saved.getEntityType()).isEqualTo(entityType);
            assertThat(saved.getEntityId()).isEqualTo(testEntityId);
            assertThat(saved.getBeforeState()).isNull();
            assertThat(saved.getAfterState()).isEqualTo(afterState);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getUserAgent()).isEqualTo(userAgent);
        }

        @Test
        @DisplayName("should save audit log with nullable fields")
        void shouldSaveAuditLogWithNullableFields() {
            // given
            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            auditService.logAction(null, null, "USER_LOGIN", "USER",
                    testUserId, null, null, null, null);

            // then
            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getActorId()).isNull();
            assertThat(saved.getActorEmail()).isNull();
            assertThat(saved.getAction()).isEqualTo("USER_LOGIN");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // logSecurityEvent Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("logSecurityEvent")
    class LogSecurityEventTests {

        @Test
        @DisplayName("should save security event with all fields")
        void shouldSaveSecurityEventWithAllFields() {
            // given
            String eventType = "LOGIN_FAILED";
            String ipAddress = "10.0.0.1";
            String details = "{\"email\":\"test@taskhive.com\",\"failedAttempts\":3}";

            when(securityEventRepository.save(any(SecurityEvent.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            auditService.logSecurityEvent(eventType, testUserId, ipAddress, false, details);

            // then
            ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
            verify(securityEventRepository).save(captor.capture());

            SecurityEvent saved = captor.getValue();
            assertThat(saved.getEventType()).isEqualTo(eventType);
            assertThat(saved.getUserId()).isEqualTo(testUserId);
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getSuccess()).isFalse();
            assertThat(saved.getDetails()).isEqualTo(details);
        }

        @Test
        @DisplayName("should save ACCOUNT_LOCKED event")
        void shouldSaveAccountLockedEvent() {
            // given
            when(securityEventRepository.save(any(SecurityEvent.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            auditService.logSecurityEvent("ACCOUNT_LOCKED", testUserId, null, false,
                    "{\"reason\":\"max failed attempts\"}");

            // then
            ArgumentCaptor<SecurityEvent> captor = ArgumentCaptor.forClass(SecurityEvent.class);
            verify(securityEventRepository).save(captor.capture());
            assertThat(captor.getValue().getEventType()).isEqualTo("ACCOUNT_LOCKED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getAllLogs Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllLogs")
    class GetAllLogsTests {

        @Test
        @DisplayName("should return paginated audit logs")
        void shouldReturnPaginatedAuditLogs() {
            // given
            AuditLog log1 = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .actorId(testUserId)
                    .actorEmail("admin@taskhive.com")
                    .action("USER_LOGIN")
                    .entityType("USER")
                    .entityId(testUserId)
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log1), PageRequest.of(0, 20), 1);
            when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

            // when
            PageResponse<AuditLogResponse> result = auditService.getAllLogs(0, 20);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.isLast()).isTrue();

            AuditLogResponse response = result.getContent().get(0);
            assertThat(response.getAction()).isEqualTo("USER_LOGIN");
            assertThat(response.getActorEmail()).isEqualTo("admin@taskhive.com");
        }

        @Test
        @DisplayName("should return empty page when no logs exist")
        void shouldReturnEmptyPage() {
            // given
            Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(emptyPage);

            // when
            PageResponse<AuditLogResponse> result = auditService.getAllLogs(0, 20);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // searchLogs Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("searchLogs")
    class SearchLogsTests {

        @Test
        @DisplayName("should delegate search to repository with filters")
        void shouldDelegateSearchToRepository() {
            // given
            AuditSearchRequest request = AuditSearchRequest.builder()
                    .action("EMPLOYEE_CREATED")
                    .entityType("EMPLOYEE")
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(auditLogRepository.searchLogs(any(), any(), eq("EMPLOYEE_CREATED"),
                    eq("EMPLOYEE"), any(), any(), any(Pageable.class))).thenReturn(page);

            // when
            PageResponse<AuditLogResponse> result = auditService.searchLogs(request, 0, 20);

            // then
            verify(auditLogRepository).searchLogs(
                    isNull(), isNull(), eq("EMPLOYEE_CREATED"),
                    eq("EMPLOYEE"), isNull(), isNull(), any(Pageable.class));
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("should search with date range")
        void shouldSearchWithDateRange() {
            // given
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

            AuditSearchRequest request = AuditSearchRequest.builder()
                    .startDate(start)
                    .endDate(end)
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(auditLogRepository.searchLogs(eq(start), eq(end), any(), any(), any(), any(),
                    any(Pageable.class))).thenReturn(page);

            // when
            auditService.searchLogs(request, 0, 20);

            // then
            verify(auditLogRepository).searchLogs(
                    eq(start), eq(end), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getEntityTimeline Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getEntityTimeline")
    class GetEntityTimelineTests {

        @Test
        @DisplayName("should return timeline for specific entity")
        void shouldReturnTimelineForEntity() {
            // given
            AuditLog log1 = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .action("TASK_CREATED")
                    .entityType("TASK")
                    .entityId(testEntityId)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .build();

            AuditLog log2 = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .action("TASK_UPDATED")
                    .entityType("TASK")
                    .entityId(testEntityId)
                    .createdAt(LocalDateTime.now())
                    .build();

            Page<AuditLog> page = new PageImpl<>(List.of(log2, log1), PageRequest.of(0, 20), 2);
            when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                    eq("TASK"), eq(testEntityId), any(Pageable.class))).thenReturn(page);

            // when
            PageResponse<AuditLogResponse> result = auditService.getEntityTimeline("TASK", testEntityId, 0, 20);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getAction()).isEqualTo("TASK_UPDATED");
            assertThat(result.getContent().get(1).getAction()).isEqualTo("TASK_CREATED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getSecurityEvents Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getSecurityEvents")
    class GetSecurityEventsTests {

        @Test
        @DisplayName("should return paginated security events")
        void shouldReturnPaginatedSecurityEvents() {
            // given
            SecurityEvent event = SecurityEvent.builder()
                    .id(UUID.randomUUID())
                    .eventType("LOGIN_FAILED")
                    .userId(testUserId)
                    .ipAddress("192.168.1.1")
                    .success(false)
                    .details("{\"email\":\"test@taskhive.com\"}")
                    .timestamp(LocalDateTime.now())
                    .build();

            Page<SecurityEvent> page = new PageImpl<>(List.of(event), PageRequest.of(0, 20), 1);
            when(securityEventRepository.findAllByOrderByTimestampDesc(any(Pageable.class))).thenReturn(page);

            // when
            PageResponse<SecurityEventResponse> result = auditService.getSecurityEvents(0, 20);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            SecurityEventResponse response = result.getContent().get(0);
            assertThat(response.getEventType()).isEqualTo("LOGIN_FAILED");
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("should return empty page when no security events")
        void shouldReturnEmptySecurityEvents() {
            // given
            Page<SecurityEvent> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(securityEventRepository.findAllByOrderByTimestampDesc(any(Pageable.class))).thenReturn(emptyPage);

            // when
            PageResponse<SecurityEventResponse> result = auditService.getSecurityEvents(0, 20);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}
