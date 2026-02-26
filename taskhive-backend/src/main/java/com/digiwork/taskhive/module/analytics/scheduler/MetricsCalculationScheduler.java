package com.digiwork.taskhive.module.analytics.scheduler;

import com.digiwork.taskhive.module.analytics.service.MetricsAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCalculationScheduler {

    private final MetricsAggregatorService metricsAggregatorService;

    /**
     * Runs nightly at 2:00 AM to pre-calculate dashboard metrics.
     * Calculates previous day's stats and upserts into daily_metrics
     * and employee_performance_cache tables.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void calculateNightlyMetrics() {
        log.info("═══ MetricsCalculationScheduler: Starting nightly metrics calculation ═══");

        LocalDate yesterday = LocalDate.now().minusDays(1);

        try {
            // Step 1: Calculate and upsert daily metrics
            log.info("Step 1: Calculating daily metrics for date={}", yesterday);
            metricsAggregatorService.calculateDailyMetrics(yesterday);

            // Step 2: Calculate and upsert employee performance
            log.info("Step 2: Calculating employee performance metrics");
            int employeeCount = metricsAggregatorService.calculateEmployeePerformance(yesterday);

            // Summary
            log.info("═══ MetricsCalculationScheduler: Nightly metrics calculation complete ═══");
            log.info("Summary: date={}, employees processed={}", yesterday, employeeCount);

        } catch (Exception e) {
            log.error("═══ MetricsCalculationScheduler: FAILED — {} ═══", e.getMessage(), e);
            // Exception is caught and logged — scheduler continues running for next cycle
        }
    }
}
