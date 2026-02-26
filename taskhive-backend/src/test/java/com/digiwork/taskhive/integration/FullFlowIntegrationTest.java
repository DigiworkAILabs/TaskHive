package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.analytics.dto.AdminDashboardResponse;
import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import com.digiwork.taskhive.module.analytics.repository.EmployeePerformanceCacheRepository;
import com.digiwork.taskhive.module.analytics.scheduler.MetricsCalculationScheduler;
import com.digiwork.taskhive.module.analytics.service.AnalyticsService;
import com.digiwork.taskhive.module.analytics.service.MetricsAggregatorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration test that validates the full scheduler → aggregator → service
 * flow.
 * Uses Mockito to simulate database layer while testing business logic
 * coordination.
 */
@ExtendWith(MockitoExtension.class)
class FullFlowIntegrationTest {

    @Mock
    private DailyMetricsRepository dailyMetricsRepository;

    @Mock
    private EmployeePerformanceCacheRepository employeePerformanceCacheRepository;

    @Mock
    private EntityManager entityManager;

    private MetricsAggregatorService metricsAggregatorService;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        metricsAggregatorService = new MetricsAggregatorService(
                dailyMetricsRepository, employeePerformanceCacheRepository);
        ReflectionTestUtils.setField(metricsAggregatorService, "entityManager", entityManager);

        analyticsService = new AnalyticsService(
                dailyMetricsRepository, employeePerformanceCacheRepository);
        ReflectionTestUtils.setField(analyticsService, "entityManager", entityManager);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Scheduler → Aggregator Flow
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("scheduler should safely handle exceptions without crashing")
    void schedulerShouldHandleExceptionsSafely() {
        // given — MetricsCalculationScheduler wraps calls in try/catch
        MetricsCalculationScheduler scheduler = new MetricsCalculationScheduler(metricsAggregatorService);

        // Make aggregator throw an exception via EntityManager
        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenThrow(new RuntimeException("DB connection failed"));

        // when / then — should not throw
        assertThatNoException().isThrownBy(scheduler::calculateNightlyMetrics);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Aggregator → Service Flow
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("aggregated metrics should be retrievable via service")
    void aggregatedMetricsShouldBeRetrievableViaService() {
        // given — simulate metrics already calculated and saved
        DailyMetrics metrics = DailyMetrics.builder()
                .metricDate(LocalDate.of(2026, 2, 25))
                .totalTasks(50)
                .activeTasks(20)
                .overdueTasks(3)
                .completedTasks(25)
                .completionRate(BigDecimal.valueOf(50.00))
                .avgCompletionHours(BigDecimal.valueOf(18.50))
                .totalEmployees(8)
                .build();

        when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                .thenReturn(Optional.of(metrics));

        // when
        AdminDashboardResponse dashboard = analyticsService.getAdminDashboard();

        // then
        assertThat(dashboard.getTotalTasks()).isEqualTo(50);
        assertThat(dashboard.getActiveTasks()).isEqualTo(20);
        assertThat(dashboard.getOverdueTasks()).isEqualTo(3);
        assertThat(dashboard.getCompletedTasks()).isEqualTo(25);
        assertThat(dashboard.getCompletionRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(dashboard.getTotalEmployees()).isEqualTo(8);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MetricsAggregator daily calculation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("daily metrics calculation should compute and upsert stats")
    void dailyMetricsCalculation() {
        // given
        LocalDate date = LocalDate.of(2026, 2, 25);
        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(10L);

        // when
        DailyMetrics result = metricsAggregatorService.calculateDailyMetrics(date);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMetricDate()).isEqualTo(date);

        // Verify upsert was called
        verify(dailyMetricsRepository).upsertDailyMetrics(
                eq(date), anyInt(), anyInt(), anyInt(), anyInt(),
                any(BigDecimal.class), any(BigDecimal.class), anyInt());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Employee Performance Calculation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee performance calculation with no employees returns zero")
    void employeePerformanceWithNoEmployees() {
        // given
        LocalDate date = LocalDate.of(2026, 2, 25);
        Query mockQuery = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(java.util.List.of());

        // when
        int count = metricsAggregatorService.calculateEmployeePerformance(date);

        // then
        assertThat(count).isZero();
        verify(employeePerformanceCacheRepository, never())
                .upsertEmployeePerformance(any(), any(), any(), anyInt(), anyInt(), any(), any());
    }
}
