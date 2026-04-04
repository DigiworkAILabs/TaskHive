package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.dto.*;
import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.model.EmployeePerformanceCache;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import com.digiwork.taskhive.module.analytics.repository.EmployeePerformanceCacheRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private DailyMetricsRepository dailyMetricsRepository;

    @Mock
    private EmployeePerformanceCacheRepository employeePerformanceCacheRepository;

    @Mock
    private EntityManager entityManager;

    private AnalyticsService analyticsService;

    private UUID testEmployeeId;

    @BeforeEach
    void setUp() {
        testEmployeeId = UUID.randomUUID();
        analyticsService = new AnalyticsService(dailyMetricsRepository, employeePerformanceCacheRepository);
        ReflectionTestUtils.setField(analyticsService, "entityManager", entityManager);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getAdminDashboard Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAdminDashboard")
    class GetAdminDashboardTests {

        @Test
        @DisplayName("should return dashboard from latest daily metrics")
        void shouldReturnDashboardFromLatestMetrics() {
            // given
            DailyMetrics metrics = DailyMetrics.builder()
                    .metricDate(LocalDate.of(2026, 2, 25))
                    .totalTasks(100L)
                    .activeTasks(40L)
                    .overdueTasks(5L)
                    .completedTasks(50L)
                    .completionRate(BigDecimal.valueOf(50.00))
                    .avgCompletionHours(BigDecimal.valueOf(24.50))
                    .totalEmployees(10)
                    .build();

            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                    .thenReturn(Optional.of(metrics));

            // when
            AdminDashboardResponse response = analyticsService.getAdminDashboard();

            // then
            assertThat(response.getTotalTasks()).isEqualTo(100L);
            assertThat(response.getActiveTasks()).isEqualTo(40L);
            assertThat(response.getOverdueTasks()).isEqualTo(5L);
            assertThat(response.getCompletedTasks()).isEqualTo(50L);
            assertThat(response.getCompletionRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
            assertThat(response.getAvgCompletionHours()).isEqualByComparingTo(BigDecimal.valueOf(24.50));
            assertThat(response.getTotalEmployees()).isEqualTo(10);
            assertThat(response.getMetricDate()).isEqualTo(LocalDate.of(2026, 2, 25));
        }

        @Test
        @DisplayName("should return zeroes when no metrics exist (falls back to real-time query)")
        void shouldReturnZeroesWhenNoMetrics() {
            // given — no pre-calculated metrics, so service falls back to
            // getAdminDashboardRealTime()
            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                    .thenReturn(Optional.empty());

            // Mock the two native queries inside getAdminDashboardRealTime():
            // 1st query: task stats (total, active, overdue, completed, avgHours)
            // 2nd query: employee count (long)
            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);

            Object[] taskRow = { 0L, 0L, 0L, 0L, 0.0 };
            when(mockQuery.getSingleResult())
                    .thenReturn(taskRow) // 1st call → task stats
                    .thenReturn(0L); // 2nd call → employee count

            // when
            AdminDashboardResponse response = analyticsService.getAdminDashboard();

            // then
            assertThat(response.getTotalTasks()).isZero();
            assertThat(response.getActiveTasks()).isZero();
            assertThat(response.getOverdueTasks()).isZero();
            assertThat(response.getCompletedTasks()).isZero();
            assertThat(response.getCompletionRate()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getTotalEmployees()).isZero();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getEmployeeDashboard Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getEmployeeDashboard")
    class GetEmployeeDashboardTests {

        @Test
        @DisplayName("should return employee stats with zero completed tasks")
        void shouldReturnEmployeeStatsWithZeroCompleted() {
            // given
            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
            when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

            Object[] row = { 5L, 3L, 2L, 0L, 0L };
            when(mockQuery.getSingleResult()).thenReturn(row);

            // when
            EmployeeDashboardResponse response = analyticsService.getEmployeeDashboard(testEmployeeId);

            // then
            assertThat(response.getTotalTasks()).isEqualTo(5);
            assertThat(response.getTodoTasks()).isEqualTo(3);
            assertThat(response.getInProgressTasks()).isEqualTo(2);
            assertThat(response.getCompletedTasks()).isZero();
            assertThat(response.getOnTimeCompletionRate()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getTaskDistribution Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTaskDistribution")
    class GetTaskDistributionTests {

        @Test
        @DisplayName("should return task distribution by status")
        void shouldReturnTaskDistribution() {
            // given
            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);

            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[] { "TODO", 15L });
            rows.add(new Object[] { "IN_PROGRESS", 10L });
            rows.add(new Object[] { "DONE", 25L });
            when(mockQuery.getResultList()).thenReturn(rows);

            // when
            List<TaskDistributionResponse> result = analyticsService.getTaskDistribution();

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStatus()).isEqualTo("TODO");
            assertThat(result.get(0).getCount()).isEqualTo(15);
            assertThat(result.get(2).getStatus()).isEqualTo("DONE");
            assertThat(result.get(2).getCount()).isEqualTo(25);
        }

        @Test
        @DisplayName("should return empty list when no tasks exist")
        void shouldReturnEmptyListWhenNoTasks() {
            // given
            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(List.of());

            // when
            List<TaskDistributionResponse> result = analyticsService.getTaskDistribution();

            // then
            assertThat(result).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getTaskByPriority Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTaskByPriority")
    class GetTaskByPriorityTests {

        @Test
        @DisplayName("should return task distribution by priority")
        void shouldReturnTaskByPriority() {
            // given
            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);

            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[] { "HIGH", 20L });
            rows.add(new Object[] { "MEDIUM", 15L });
            rows.add(new Object[] { "LOW", 5L });
            when(mockQuery.getResultList()).thenReturn(rows);

            // when
            List<TaskDistributionResponse> result = analyticsService.getTaskByPriority();

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStatus()).isEqualTo("HIGH");
            assertThat(result.get(0).getCount()).isEqualTo(20);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getCompletionTrend Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getCompletionTrend")
    class GetCompletionTrendTests {

        @Test
        @DisplayName("should return 30-day completion trend")
        void shouldReturnCompletionTrend() {
            // given
            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);
            when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);

            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[] { Date.valueOf(LocalDate.of(2026, 2, 20)), 5L });
            rows.add(new Object[] { Date.valueOf(LocalDate.of(2026, 2, 21)), 3L });
            when(mockQuery.getResultList()).thenReturn(rows);

            // when
            List<TaskCompletionTrendResponse> result = analyticsService.getCompletionTrend();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 2, 20));
            assertThat(result.get(0).getCount()).isEqualTo(5);
            assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2026, 2, 21));
            assertThat(result.get(1).getCount()).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getEmployeePerformance Tests
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getEmployeePerformance")
    class GetEmployeePerformanceTests {

        @Test
        @DisplayName("should return cached employee performance data")
        void shouldReturnCachedPerformance() {
            // given
            LocalDate periodStart = LocalDate.now().minusDays(30);
            LocalDate periodEnd = LocalDate.now();

            EmployeePerformanceCache cache = EmployeePerformanceCache.builder()
                    .employeeId(testEmployeeId)
                    .tasksAssigned(10)
                    .tasksCompleted(8)
                    .onTimeRate(BigDecimal.valueOf(87.50))
                    .avgCompletionHours(BigDecimal.valueOf(12.30))
                    .build();

            when(employeePerformanceCacheRepository
                    .findByPeriodStartAndPeriodEndOrderByTasksCompletedDesc(periodStart, periodEnd))
                    .thenReturn(List.of(cache));

            // Mock for getEmployeeName
            Query nameQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(nameQuery);
            when(nameQuery.setParameter(anyString(), any())).thenReturn(nameQuery);
            when(nameQuery.getSingleResult()).thenReturn("John Doe");

            // when
            List<EmployeePerformanceResponse> result = analyticsService.getEmployeePerformance();

            // then
            assertThat(result).hasSize(1);
            EmployeePerformanceResponse perf = result.get(0);
            assertThat(perf.getEmployeeId()).isEqualTo(testEmployeeId);
            assertThat(perf.getEmployeeName()).isEqualTo("John Doe");
            assertThat(perf.getTasksAssigned()).isEqualTo(10);
            assertThat(perf.getTasksCompleted()).isEqualTo(8);
            assertThat(perf.getOnTimeRate()).isEqualByComparingTo(BigDecimal.valueOf(87.50));
        }

        @Test
        @DisplayName("should fall back to real-time query when no cached data")
        void shouldFallBackToRealTimeQuery() {
            // given
            LocalDate periodStart = LocalDate.now().minusDays(30);
            LocalDate periodEnd = LocalDate.now();

            when(employeePerformanceCacheRepository
                    .findByPeriodStartAndPeriodEndOrderByTasksCompletedDesc(periodStart, periodEnd))
                    .thenReturn(List.of());

            Query mockQuery = mock(Query.class);
            when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery);

            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[] { testEmployeeId, 5L, 3L, 2L, 10.5 });
            when(mockQuery.getResultList()).thenReturn(rows);

            // Mock for getEmployeeName (called inside the mapping)
            when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
            when(mockQuery.getSingleResult()).thenReturn("Jane Doe");

            // when
            List<EmployeePerformanceResponse> result = analyticsService.getEmployeePerformance();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmployeeId()).isEqualTo(testEmployeeId);
        }
    }
}
