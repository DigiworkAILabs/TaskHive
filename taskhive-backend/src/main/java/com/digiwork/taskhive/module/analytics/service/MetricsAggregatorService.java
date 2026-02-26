package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import com.digiwork.taskhive.module.analytics.repository.EmployeePerformanceCacheRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsAggregatorService {

        private final DailyMetricsRepository dailyMetricsRepository;
        private final EmployeePerformanceCacheRepository employeePerformanceCacheRepository;

        @PersistenceContext
        private EntityManager entityManager;

        // ─── Calculate and upsert daily metrics ──────────────────────────────────

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @SuppressWarnings("unchecked")
        public DailyMetrics calculateDailyMetrics(LocalDate date) {
                log.info("Calculating daily metrics for date={}", date);

                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

                // Total non-deleted tasks
                String totalSql = "SELECT COUNT(*) FROM tasks WHERE is_deleted = false";
                int totalTasks = ((Number) entityManager.createNativeQuery(totalSql)
                                .getSingleResult()).intValue();

                // Active tasks (TODO, IN_PROGRESS, IN_REVIEW)
                String activeSql = "SELECT COUNT(*) FROM tasks WHERE is_deleted = false " +
                                "AND status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW')";
                int activeTasks = ((Number) entityManager.createNativeQuery(activeSql)
                                .getSingleResult()).intValue();

                // Overdue tasks
                String overdueSql = "SELECT COUNT(*) FROM tasks WHERE is_deleted = false " +
                                "AND due_date < :now AND status NOT IN ('DONE', 'CANCELLED')";
                int overdueTasks = ((Number) entityManager.createNativeQuery(overdueSql)
                                .setParameter("now", LocalDateTime.now())
                                .getSingleResult()).intValue();

                // Completed tasks
                String completedSql = "SELECT COUNT(*) FROM tasks WHERE is_deleted = false AND status = 'DONE'";
                int completedTasks = ((Number) entityManager.createNativeQuery(completedSql)
                                .getSingleResult()).intValue();

                // Completion rate
                BigDecimal completionRate = totalTasks > 0
                                ? BigDecimal.valueOf(completedTasks * 100.0 / totalTasks)
                                                .setScale(2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Average completion hours (for tasks completed on this date)
                String avgHoursSql = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM " +
                                "(completed_at - created_at)) / 3600), 0) " +
                                "FROM tasks WHERE is_deleted = false AND status = 'DONE' " +
                                "AND completed_at IS NOT NULL " +
                                "AND completed_at >= :dayStart AND completed_at < :dayEnd";
                BigDecimal avgCompletionHours = BigDecimal.valueOf(
                                ((Number) entityManager.createNativeQuery(avgHoursSql)
                                                .setParameter("dayStart", dayStart)
                                                .setParameter("dayEnd", dayEnd)
                                                .getSingleResult()).doubleValue())
                                .setScale(2, RoundingMode.HALF_UP);

                // Total active employees
                String empSql = "SELECT COUNT(*) FROM employees WHERE is_deleted = false AND status = 'ACTIVE'";
                int totalEmployees = ((Number) entityManager.createNativeQuery(empSql)
                                .getSingleResult()).intValue();

                // Upsert into daily_metrics
                dailyMetricsRepository.upsertDailyMetrics(
                                date, totalTasks, activeTasks, overdueTasks, completedTasks,
                                completionRate, avgCompletionHours, totalEmployees);

                log.info("Daily metrics upserted: date={}, total={}, active={}, overdue={}, completed={}, " +
                                "rate={}%, avgHours={}, employees={}",
                                date, totalTasks, activeTasks, overdueTasks, completedTasks,
                                completionRate, avgCompletionHours, totalEmployees);

                return DailyMetrics.builder()
                                .metricDate(date)
                                .totalTasks(totalTasks)
                                .activeTasks(activeTasks)
                                .overdueTasks(overdueTasks)
                                .completedTasks(completedTasks)
                                .completionRate(completionRate)
                                .avgCompletionHours(avgCompletionHours)
                                .totalEmployees(totalEmployees)
                                .build();
        }

        // ─── Calculate and upsert employee performance ───────────────────────────

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @SuppressWarnings("unchecked")
        public int calculateEmployeePerformance(LocalDate date) {
                log.info("Calculating employee performance for period ending date={}", date);

                LocalDate periodStart = date.minusDays(30);
                LocalDate periodEnd = date;
                LocalDateTime periodStartTime = periodStart.atStartOfDay();
                LocalDateTime periodEndTime = periodEnd.plusDays(1).atStartOfDay();

                // Get all employees with assigned tasks in the period
                String sql = "SELECT t.assigned_to, " +
                                "COUNT(*) AS assigned, " +
                                "COUNT(*) FILTER (WHERE t.status = 'DONE') AS completed, " +
                                "COALESCE(COUNT(*) FILTER (WHERE t.status = 'DONE' AND t.completed_at <= t.due_date), 0) AS on_time, "
                                +
                                "COALESCE(AVG(EXTRACT(EPOCH FROM (t.completed_at - t.created_at)) / 3600) " +
                                "FILTER (WHERE t.status = 'DONE' AND t.completed_at IS NOT NULL), 0) AS avg_hours " +
                                "FROM tasks t WHERE t.is_deleted = false " +
                                "AND t.created_at >= :periodStart AND t.created_at < :periodEnd " +
                                "GROUP BY t.assigned_to";

                List<Object[]> rows = entityManager.createNativeQuery(sql)
                                .setParameter("periodStart", periodStartTime)
                                .setParameter("periodEnd", periodEndTime)
                                .getResultList();

                int count = 0;
                for (Object[] row : rows) {
                        UUID employeeId = (UUID) row[0];
                        int assigned = ((Number) row[1]).intValue();
                        int completed = ((Number) row[2]).intValue();
                        long onTime = ((Number) row[3]).longValue();
                        double avgHours = ((Number) row[4]).doubleValue();

                        BigDecimal onTimeRate = completed > 0
                                        ? BigDecimal.valueOf(onTime * 100.0 / completed)
                                                        .setScale(2, RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO;

                        employeePerformanceCacheRepository.upsertEmployeePerformance(
                                        employeeId, periodStart, periodEnd,
                                        assigned, completed, onTimeRate,
                                        BigDecimal.valueOf(avgHours).setScale(2, RoundingMode.HALF_UP));
                        count++;
                }

                log.info("Employee performance upserted for {} employees (period: {} to {})",
                                count, periodStart, periodEnd);
                return count;
        }
}
