package com.digiwork.taskhive.integration;

import com.digiwork.taskhive.module.analytics.dto.AdminDashboardResponse;
import com.digiwork.taskhive.module.analytics.dto.AnomalyAlertResponse;
import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import com.digiwork.taskhive.module.analytics.repository.EmployeePerformanceCacheRepository;
import com.digiwork.taskhive.module.analytics.scheduler.MetricsCalculationScheduler;
import com.digiwork.taskhive.module.analytics.service.AnalyticsService;
import com.digiwork.taskhive.module.analytics.service.AnomalyDetectionService;
import com.digiwork.taskhive.module.analytics.service.MetricsAggregatorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * FullFlowIntegrationTest
 * ────────────────────────
 * Validates the full scheduler → aggregator → service flow and anomaly
 * detection pipeline.
 *
 * NOTE: This class intentionally does NOT use @Transactional.
 * The MetricsAggregatorService methods (calculateDailyMetrics,
 * calculateEmployeePerformance)
 * use Propagation.REQUIRES_NEW, which suspends any outer transaction and
 * commits independently.
 * Because these writes bypass any test-level transaction rollback, we handle
 * cleanup
 * explicitly in @AfterEach to ensure the database remains in a consistent state
 * between tests.
 *
 * AnomalyDetectionService.activeAlerts is an in-memory list on a Spring
 * singleton bean.
 * Each anomaly test calls runNightlyDetection() — which starts with
 * activeAlerts.clear() —
 * to guarantee a clean state regardless of test execution order.
 */
class FullFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DailyMetricsRepository dailyMetricsRepository;
    @Autowired
    private MetricsAggregatorService metricsAggregatorService;
    @Autowired
    private EmployeePerformanceCacheRepository employeePerformanceCacheRepository;
    @Autowired
    private AnalyticsService analyticsService;
    @Autowired
    private AnomalyDetectionService anomalyDetectionService;
    @Autowired
    private MetricsCalculationScheduler scheduler;

    @AfterEach
    void tearDown() {
        // Manual cleanup — rollback covers most, but scheduler flows commit
        // mid-execution.
        // @AfterEach ensures we leave the DB clean even if transactions were committed.
        dailyMetricsRepository.deleteAll();
        employeePerformanceCacheRepository.deleteAll();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Scheduler → Aggregator Flow
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("scheduler should safely handle exceptions without crashing")
    void schedulerShouldHandleExceptionsSafely() {
        // FIX: use Spring-managed proxy so @Transactional and AOP advice is active.
        assertThatNoException().isThrownBy(scheduler::calculateNightlyMetrics);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Aggregator → Service Flow
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("aggregated metrics should be retrievable via service")
    void aggregatedMetricsShouldBeRetrievableViaService() {
        // given — simulate pre-calculated metrics already saved to the DB
        DailyMetrics metrics = DailyMetrics.builder()
                .metricDate(LocalDate.of(2026, 2, 25))
                .totalTasks(50L)
                .activeTasks(20L)
                .overdueTasks(3L)
                .completedTasks(25L)
                .completionRate(BigDecimal.valueOf(50.00))
                .avgCompletionHours(BigDecimal.valueOf(18.50))
                .totalEmployees(8)
                .build();

        dailyMetricsRepository.save(metrics);

        // when
        AdminDashboardResponse dashboard = analyticsService.getAdminDashboard();

        // then
        assertThat(dashboard.getTotalTasks()).isEqualTo(50L);
        assertThat(dashboard.getActiveTasks()).isEqualTo(20L);
        assertThat(dashboard.getOverdueTasks()).isEqualTo(3L);
        assertThat(dashboard.getCompletedTasks()).isEqualTo(25L);
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

        // when
        DailyMetrics result = metricsAggregatorService.calculateDailyMetrics(date);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMetricDate()).isEqualTo(date);

        // Verify the upsert persisted to the real PostgreSQL instance
        Optional<DailyMetrics> saved = dailyMetricsRepository.findTopByOrderByMetricDateDesc();
        assertThat(saved).isPresent();
        assertThat(saved.get().getMetricDate()).isEqualTo(date);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Employee Performance Calculation
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("employee performance calculation with no employees returns zero")
    void employeePerformanceWithNoEmployees() {
        // given
        LocalDate date = LocalDate.of(2026, 2, 25);

        // when
        int count = metricsAggregatorService.calculateEmployeePerformance(date);

        // then
        assertThat(count).isZero();
        assertThat(employeePerformanceCacheRepository.count()).isZero();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Admin Dashboard Real-Time Fallback
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("admin dashboard should return a valid response via real-time fallback when no pre-calculated metrics exist")
    void adminDashboard_ReturnsValidResponseViaRealTimeFallbackWhenNoMetricsExist() {
        // given — DB has no daily_metrics (tearDown cleared them; none added in this
        // test)
        assertThat(dailyMetricsRepository.count()).isZero();

        // when — service detects no daily_metrics and falls back to live SQL queries
        AdminDashboardResponse dashboard = analyticsService.getAdminDashboard();

        // then — response is non-null and shows an empty-DB baseline
        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getTotalTasks()).isZero(); // tasks table is empty
        assertThat(dashboard.getTotalEmployees()).isZero(); // employees table is empty
        assertThat(dashboard.getMetricDate()).isEqualTo(LocalDate.now()); // real-time path sets today
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Anomaly Detection — Rule 1: Completion Rate Drop
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("anomaly detection Rule 1 should trigger when today's completions are below 60% of the 7-day average")
    void anomalyDetection_Rule1_TriggersWhenCompletionDropsBelowThreshold() {
        // given — six healthy days followed by one poor-completion day.
        //
        // findTop7ByOrderByMetricDateDesc() returns (desc): Jan7(3), Jan6(10),
        // Jan5(10),
        // Jan4(10), Jan3(10), Jan2(10), Jan1(10)
        // avg7 = (3+10+10+10+10+10+10) / 7 = 9.0
        // findTopByOrderByMetricDateDesc() = Jan7 → todayCompleted = 3
        // Threshold = 9.0 * 0.60 = 5.4 → 3 < 5.4 → RULE_1 fires.

        for (int day = 1; day <= 6; day++) {
            dailyMetricsRepository.save(DailyMetrics.builder()
                    .metricDate(LocalDate.of(2026, 1, day))
                    .completedTasks(10L)
                    .totalTasks(10L)
                    .lateTasks(0)
                    .build());
        }
        dailyMetricsRepository.save(DailyMetrics.builder()
                .metricDate(LocalDate.of(2026, 1, 7)) // most recent — low completions
                .completedTasks(3L)
                .totalTasks(10L)
                .lateTasks(0)
                .build());

        // when — runNightlyDetection() clears any stale in-memory alerts then
        // re-evaluates
        anomalyDetectionService.runNightlyDetection();

        // then
        List<AnomalyAlertResponse> alerts = anomalyDetectionService.getActiveAlerts();
        assertThat(alerts)
                .as("RULE_1 should be triggered when today's completions drop below 60%% of the 7-day average")
                .anyMatch(a -> "RULE_1".equals(a.getRuleId()));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Anomaly Detection — Rule 3: Consecutive Days with Late Tasks
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("anomaly detection Rule 3 should trigger when 3 consecutive days all have late tasks")
    void anomalyDetection_Rule3_TriggersWhenConsecutiveDaysHaveLateTasks() {
        // given — three consecutive days, each with lateTasks > 0.
        //
        // Rule 3 logic: findTop7ByOrderByMetricDateDesc().stream().limit(3)
        // → the 3 most-recent records; allDaysHadLateTasks = true → RULE_3 fires.

        for (int day = 5; day <= 7; day++) {
            dailyMetricsRepository.save(DailyMetrics.builder()
                    .metricDate(LocalDate.of(2026, 1, day))
                    .completedTasks(10L)
                    .totalTasks(10L)
                    .lateTasks(5) // > 0 for every day in the window
                    .build());
        }

        // when
        anomalyDetectionService.runNightlyDetection();

        // then
        List<AnomalyAlertResponse> alerts = anomalyDetectionService.getActiveAlerts();
        assertThat(alerts)
                .as("RULE_3 should be triggered when every day in the 3-day window has late tasks")
                .anyMatch(a -> "RULE_3".equals(a.getRuleId()));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Anomaly Detection — No Alerts on Healthy Data
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("anomaly detection should raise no alerts when completion rates and late tasks are healthy")
    void anomalyDetection_NoAlertsWhenDataIsHealthy() {
        // given — seven days with stable, equal completion counts and zero late tasks.
        //
        // avg7 = 10, todayCompleted = 10
        // Rule 1: 10 < 10 * 0.6 = 6 → false → no RULE_1
        // Rule 3: lateTasks = 0 on all days → allDaysHadLateTasks = false → no RULE_3

        for (int day = 1; day <= 7; day++) {
            dailyMetricsRepository.save(DailyMetrics.builder()
                    .metricDate(LocalDate.of(2026, 1, day))
                    .completedTasks(10L)
                    .totalTasks(10L)
                    .lateTasks(0)
                    .build());
        }

        // when
        anomalyDetectionService.runNightlyDetection();

        // then — the service's in-memory alert list must be empty
        assertThat(anomalyDetectionService.getActiveAlerts())
                .as("No anomaly alerts should be raised when all metrics are healthy")
                .isEmpty();
    }
}