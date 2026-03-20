package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.dto.AnomalyAlertResponse;
import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnomalyDetectionService (Phase 1 v2.5 — P1.5).
 * Covers Rule 1 (completion rate drop) and Rule 3 (consecutive late task streak).
 */
@ExtendWith(MockitoExtension.class)
class AnomalyDetectionServiceTest {

    @Mock
    private DailyMetricsRepository dailyMetricsRepository;

    private AnomalyDetectionService service;

    @BeforeEach
    void setUp() {
        service = new AnomalyDetectionService(dailyMetricsRepository);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Initial State
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getActiveAlerts returns empty list before runNightlyDetection is called")
    void initialStateHasNoAlerts() {
        assertThat(service.getActiveAlerts()).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Rule 1 — Completion Rate Drop
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Rule 1: completion rate drop")
    class Rule1Tests {

        @Test
        @DisplayName("triggers alert when today's completion is below 60% of 7-day average")
        void triggersAlertWhenCompletionRateDrops() {
            // 7-day avg = 10 completions → threshold = 6
            List<DailyMetrics> last7 = buildMetricsWithCompletions(10, 10, 10, 10, 10, 10, 10);
            when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(last7);

            // Today: only 3 completions (below 60% = 6)
            DailyMetrics today = DailyMetrics.builder()
                    .metricDate(LocalDate.now())
                    .completedTasks(3)
                    .build();
            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc()).thenReturn(Optional.of(today));

            service.runNightlyDetection();

            List<AnomalyAlertResponse> alerts = service.getActiveAlerts();
            assertThat(alerts).hasSize(1);
            assertThat(alerts.get(0).getRuleId()).isEqualTo("RULE_1");
            assertThat(alerts.get(0).getSeverity()).isEqualTo("HIGH");
        }

        @Test
        @DisplayName("does NOT trigger alert when today's completion is at or above 60% of 7-day average")
        void noAlertWhenCompletionRateIsNormal() {
            // 7-day avg = 10 → threshold = 6; today = 7 → fine
            List<DailyMetrics> last7 = buildMetricsWithCompletions(10, 10, 10, 10, 10, 10, 10);
            when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(last7);

            DailyMetrics today = DailyMetrics.builder()
                    .metricDate(LocalDate.now())
                    .completedTasks(7)
                    .build();
            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc()).thenReturn(Optional.of(today));

            service.runNightlyDetection();

            assertThat(service.getActiveAlerts()).isEmpty();
        }

        @Test
        @DisplayName("does NOT trigger alert when no 7-day history exists")
        void noAlertWhenNoHistory() {
            when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(List.of());

            service.runNightlyDetection();

            assertThat(service.getActiveAlerts()).isEmpty();
            verify(dailyMetricsRepository, never()).findTopByOrderByMetricDateDesc();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Rule 3 — Consecutive Late Task Streak
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Rule 3: consecutive late task streak")
    class Rule3Tests {

        @Test
        @DisplayName("triggers MEDIUM alert when 3 consecutive days have late tasks > 0")
        void triggersAlertWhenThreeConsecutiveDaysLate() {
            List<DailyMetrics> last7 = List.of(
                    metricsWithLateTasks(2),
                    metricsWithLateTasks(1),
                    metricsWithLateTasks(3)
            );
            when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(last7);
            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                    .thenReturn(Optional.of(last7.get(0)));

            service.runNightlyDetection();

            List<AnomalyAlertResponse> alerts = service.getActiveAlerts();
            boolean hasRule3 = alerts.stream().anyMatch(a -> "RULE_3".equals(a.getRuleId()));
            assertThat(hasRule3).isTrue();

            AnomalyAlertResponse rule3 = alerts.stream()
                    .filter(a -> "RULE_3".equals(a.getRuleId()))
                    .findFirst().orElseThrow();
            assertThat(rule3.getSeverity()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("does NOT trigger Rule 3 when fewer than 3 days of history")
        void noAlertWithInsufficientHistory() {
            List<DailyMetrics> onlyTwo = List.of(
                    metricsWithLateTasks(2),
                    metricsWithLateTasks(1)
            );
            when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(onlyTwo);
            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                    .thenReturn(Optional.of(onlyTwo.get(0)));

            service.runNightlyDetection();

            boolean hasRule3 = service.getActiveAlerts().stream()
                    .anyMatch(a -> "RULE_3".equals(a.getRuleId()));
            assertThat(hasRule3).isFalse();
        }

        @Test
        @DisplayName("does NOT trigger Rule 3 when one of the 3 days had zero late tasks")
        void noAlertWhenStreakBroken() {
            List<DailyMetrics> last7 = List.of(
                    metricsWithLateTasks(3),
                    metricsWithLateTasks(0),   // streak broken
                    metricsWithLateTasks(2)
            );
            when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(last7);
            when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                    .thenReturn(Optional.of(last7.get(0)));

            service.runNightlyDetection();

            boolean hasRule3 = service.getActiveAlerts().stream()
                    .anyMatch(a -> "RULE_3".equals(a.getRuleId()));
            assertThat(hasRule3).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // State Reset between Runs
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("active alerts are cleared at the start of each nightly run")
    void alertsAreClearedOnEachRun() {
        // First run with a trigger
        List<DailyMetrics> last7 = buildMetricsWithCompletions(10, 10, 10, 10, 10, 10, 10);
        when(dailyMetricsRepository.findTop7ByOrderByMetricDateDesc()).thenReturn(last7);
        when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                .thenReturn(Optional.of(DailyMetrics.builder().completedTasks(2).build()));

        service.runNightlyDetection();
        assertThat(service.getActiveAlerts()).isNotEmpty();

        // Second run: all normal — alerts should clear
        when(dailyMetricsRepository.findTopByOrderByMetricDateDesc())
                .thenReturn(Optional.of(DailyMetrics.builder().completedTasks(10).build()));

        service.runNightlyDetection();
        assertThat(service.getActiveAlerts()).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private List<DailyMetrics> buildMetricsWithCompletions(int... completions) {
        java.util.List<DailyMetrics> list = new java.util.ArrayList<>();
        for (int c : completions) {
            list.add(DailyMetrics.builder()
                    .metricDate(LocalDate.now().minusDays(list.size()))
                    .completedTasks(c)
                    .lateTasks(0)
                    .build());
        }
        return list;
    }

    private DailyMetrics metricsWithLateTasks(int lateTasks) {
        return DailyMetrics.builder()
                .metricDate(LocalDate.now())
                .completedTasks(5)
                .lateTasks(lateTasks)
                .build();
    }
}
