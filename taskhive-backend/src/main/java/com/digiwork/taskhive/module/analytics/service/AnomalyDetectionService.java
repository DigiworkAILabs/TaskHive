package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.dto.AnomalyAlertResponse;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * P1.5 — Rule-based anomaly detection (no ML in Phase 1).
 *
 * Rule 1: Total completions today < 60% of 7-day average → alert.
 * Rule 2: Recurring task type with 0 completions for 2+ days → deferred to Phase 3 (no task types yet).
 * Rule 3: Employee with 3+ consecutive days of missed tasks → alert.
 *
 * This is a concrete class (not interface/impl) matching the codebase pattern
 * seen in MetricsAggregatorService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final DailyMetricsRepository dailyMetricsRepository;

    /**
     * Called nightly by MetricsCalculationScheduler after daily metrics are written.
     * Evaluates all rules and stores results in memory (active alerts list).
     * Phase 1 keeps alerts in-memory — persistent store added in Phase 4 if needed.
     */
    public void runNightlyDetection() {
        log.info("AnomalyDetectionService: running nightly anomaly detection...");
        activeAlerts.clear();

        runRule1CompletionRateDrop();
        // Rule 2 deferred — no task types in Phase 1
        runRule3EmployeeMissedTasksStreak();

        log.info("AnomalyDetectionService: detection complete. {} active alert(s)", activeAlerts.size());
    }

    /**
     * Returns current active anomaly alerts.
     */
    public List<AnomalyAlertResponse> getActiveAlerts() {
        return List.copyOf(activeAlerts);
    }

    // ─── In-memory store (reset each nightly run) ─────────────────────────────
    private final List<AnomalyAlertResponse> activeAlerts = new ArrayList<>();

    // ─── Rule 1: completion rate drop ────────────────────────────────────────

    private void runRule1CompletionRateDrop() {
        try {
            // 7-day average completions
            var last7 = dailyMetricsRepository
                    .findTop7ByOrderByMetricDateDesc();
            if (last7.isEmpty()) return;

            double avg7 = last7.stream()
                    .mapToDouble(m -> m.getCompletedTasks())
                    .average()
                    .orElse(0.0);

            // Today's completions (most recent record)
            var todayMetrics = dailyMetricsRepository.findTopByOrderByMetricDateDesc();
            if (todayMetrics.isEmpty()) return;

            long todayCompleted = todayMetrics.get().getCompletedTasks();

            if (avg7 > 0 && todayCompleted < avg7 * 0.6) {
                activeAlerts.add(AnomalyAlertResponse.builder()
                        .ruleId("RULE_1")
                        .description(String.format(
                                "Completion rate dropped: %d today vs %.1f avg (7-day). Below 60%% threshold.",
                                todayCompleted, avg7))
                        .severity("HIGH")
                        .detectedAt(LocalDateTime.now())
                        .build());
                log.warn("Anomaly Rule 1 triggered: {} completions today vs {:.1f} 7-day avg", todayCompleted, avg7);
            }
        } catch (Exception e) {
            log.error("AnomalyDetectionService Rule 1 failed: {}", e.getMessage(), e);
        }
    }

    // ─── Rule 3: employee missed task streak ──────────────────────────────────

    private void runRule3EmployeeMissedTasksStreak() {
        try {
            // Find employees with 3+ consecutive days of missed/cancelled tasks
            // Using last 3 daily_metrics records for each employee
            var last3 = dailyMetricsRepository
                    .findTop7ByOrderByMetricDateDesc()
                    .stream()
                    .limit(3)
                    .toList();

            if (last3.size() < 3) return;

            // Check if all 3 days had late tasks > 0 (proxy for missed pattern until
            // per-employee daily breakdown is available in Phase 4)
            boolean allDaysHadLateTasks = last3.stream()
                    .allMatch(m -> m.getLateTasks() != null && m.getLateTasks() > 0);

            if (allDaysHadLateTasks) {
                activeAlerts.add(AnomalyAlertResponse.builder()
                        .ruleId("RULE_3")
                        .description("Pattern detected: late task submissions have occurred for 3+ consecutive days.")
                        .severity("MEDIUM")
                        .detectedAt(LocalDateTime.now())
                        .build());
            }
        } catch (Exception e) {
            log.error("AnomalyDetectionService Rule 3 failed: {}", e.getMessage(), e);
        }
    }
}
