package com.digiwork.taskhive.module.analytics.service;

import com.digiwork.taskhive.module.analytics.dto.*;
import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import com.digiwork.taskhive.module.analytics.model.EmployeePerformanceCache;
import com.digiwork.taskhive.module.analytics.repository.DailyMetricsRepository;
import com.digiwork.taskhive.module.analytics.repository.EmployeePerformanceCacheRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

        private final DailyMetricsRepository dailyMetricsRepository;
        private final EmployeePerformanceCacheRepository employeePerformanceCacheRepository;

        @PersistenceContext
        private EntityManager entityManager;

        // ─── Admin Dashboard ─────────────────────────────────────────────────────

        public AdminDashboardResponse getAdminDashboard() {
                log.debug("Fetching admin dashboard data");

                Optional<DailyMetrics> latestMetrics = dailyMetricsRepository.findTopByOrderByMetricDateDesc();

                if (latestMetrics.isPresent()) {
                        DailyMetrics m = latestMetrics.get();
                        return AdminDashboardResponse.builder()
                                        .totalTasks(m.getTotalTasks())
                                        .activeTasks(m.getActiveTasks())
                                        .overdueTasks(m.getOverdueTasks())
                                        .completedTasks(m.getCompletedTasks())
                                        .completionRate(m.getCompletionRate())
                                        .totalEmployees(m.getTotalEmployees())
                                        .avgCompletionHours(m.getAvgCompletionHours())
                                        .metricDate(m.getMetricDate())
                                        .build();
                }

                // No pre-calculated metrics yet — fallback to real-time queries
                log.debug("No daily_metrics found, falling back to real-time admin dashboard query");
                return getAdminDashboardRealTime();
        }

        // ─── Admin Dashboard Real-Time Fallback ──────────────────────────────────

        @SuppressWarnings("unchecked")
        private AdminDashboardResponse getAdminDashboardRealTime() {
                // Single query: total, active (TODO+IN_PROGRESS+IN_REVIEW), overdue, completed,
                // avg hours
                String taskSql = "SELECT " +
                                "COUNT(*) AS total, " +
                                "COUNT(*) FILTER (WHERE t.status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW')) AS active, " +
                                "COUNT(*) FILTER (WHERE t.status != 'DONE' AND t.due_date < NOW()) AS overdue, " +
                                "COUNT(*) FILTER (WHERE t.status = 'DONE') AS completed, " +
                                "COALESCE(AVG(EXTRACT(EPOCH FROM (t.completed_at - t.created_at)) / 3600) " +
                                "FILTER (WHERE t.status = 'DONE' AND t.completed_at IS NOT NULL), 0) AS avg_hours " +
                                "FROM tasks t WHERE t.is_deleted = false";

                Object[] row = (Object[]) entityManager.createNativeQuery(taskSql).getSingleResult();

                long total = ((Number) row[0]).longValue();
                long active = ((Number) row[1]).longValue();
                long overdue = ((Number) row[2]).longValue();
                long completed = ((Number) row[3]).longValue();
                double avgHours = ((Number) row[4]).doubleValue();

                BigDecimal completionRate = total > 0
                                ? BigDecimal.valueOf(completed * 100.0 / total)
                                                .setScale(2, java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                // Count distinct active employees (those with at least one task)
                String empSql = "SELECT COUNT(DISTINCT e.user_id) FROM employees e " +
                                "WHERE e.status = 'ACTIVE'";
                long totalEmployees = ((Number) entityManager.createNativeQuery(empSql)
                                .getSingleResult()).longValue();

                return AdminDashboardResponse.builder()
                                .totalTasks((int) total)
                                .activeTasks((int) active)
                                .overdueTasks((int) overdue)
                                .completedTasks((int) completed)
                                .completionRate(completionRate)
                                .avgCompletionHours(BigDecimal.valueOf(avgHours)
                                                .setScale(2, java.math.RoundingMode.HALF_UP))
                                .totalEmployees((int) totalEmployees)
                                .metricDate(LocalDate.now())
                                .build();
        }

        @SuppressWarnings("unchecked")
        public EmployeeDashboardResponse getEmployeeDashboard(UUID employeeId) {
                log.debug("Fetching employee dashboard for employeeId={}", employeeId);

                String sql = "SELECT " +
                                "COUNT(*) AS total, " +
                                "COUNT(*) FILTER (WHERE t.status = 'TODO') AS todo, " +
                                "COUNT(*) FILTER (WHERE t.status = 'IN_PROGRESS') AS in_progress, " +
                                "COUNT(*) FILTER (WHERE t.status = 'IN_REVIEW') AS in_review, " +
                                "COUNT(*) FILTER (WHERE t.status = 'DONE') AS done " +
                                "FROM tasks t WHERE t.is_deleted = false AND t.assigned_to = :employeeId";

                Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                                .setParameter("employeeId", employeeId)
                                .getSingleResult();

                long total = ((Number) row[0]).longValue();
                long todo = ((Number) row[1]).longValue();
                long inProgress = ((Number) row[2]).longValue();
                long inReview = ((Number) row[3]).longValue();
                long done = ((Number) row[4]).longValue();

                // Calculate on-time completion rate
                BigDecimal onTimeRate = BigDecimal.ZERO;
                BigDecimal avgHours = BigDecimal.ZERO;

                if (done > 0) {
                        String onTimeSql = "SELECT " +
                                        "COUNT(*) FILTER (WHERE t.completed_at <= t.due_date) AS on_time, " +
                                        "AVG(EXTRACT(EPOCH FROM (t.completed_at - t.created_at)) / 3600) AS avg_hours "
                                        +
                                        "FROM tasks t WHERE t.is_deleted = false AND t.assigned_to = :employeeId " +
                                        "AND t.status = 'DONE' AND t.completed_at IS NOT NULL";

                        Object[] onTimeRow = (Object[]) entityManager.createNativeQuery(onTimeSql)
                                        .setParameter("employeeId", employeeId)
                                        .getSingleResult();

                        long onTimeCount = ((Number) onTimeRow[0]).longValue();
                        onTimeRate = BigDecimal.valueOf(onTimeCount)
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(BigDecimal.valueOf(done), 2, java.math.RoundingMode.HALF_UP);

                        if (onTimeRow[1] != null) {
                                avgHours = BigDecimal.valueOf(((Number) onTimeRow[1]).doubleValue())
                                                .setScale(2, java.math.RoundingMode.HALF_UP);
                        }
                }

                return EmployeeDashboardResponse.builder()
                                .totalTasks(total)
                                .todoTasks(todo)
                                .inProgressTasks(inProgress)
                                .inReviewTasks(inReview)
                                .completedTasks(done)
                                .onTimeCompletionRate(onTimeRate)
                                .avgCompletionHours(avgHours)
                                .build();
        }

        // ─── Task Distribution by Status ─────────────────────────────────────────

        @SuppressWarnings("unchecked")
        public List<TaskDistributionResponse> getTaskDistribution() {
                log.debug("Fetching task distribution by status");

                String sql = "SELECT t.status, COUNT(*) AS cnt " +
                                "FROM tasks t WHERE t.is_deleted = false " +
                                "GROUP BY t.status ORDER BY cnt DESC";

                List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

                return rows.stream()
                                .map(row -> TaskDistributionResponse.builder()
                                                .status((String) row[0])
                                                .count(((Number) row[1]).longValue())
                                                .build())
                                .toList();
        }

        // ─── Task Distribution by Priority ───────────────────────────────────────

        @SuppressWarnings("unchecked")
        public List<TaskDistributionResponse> getTaskByPriority() {
                log.debug("Fetching task distribution by priority");

                String sql = "SELECT t.priority, COUNT(*) AS cnt " +
                                "FROM tasks t WHERE t.is_deleted = false " +
                                "GROUP BY t.priority ORDER BY cnt DESC";

                List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

                return rows.stream()
                                .map(row -> TaskDistributionResponse.builder()
                                                .status((String) row[0])
                                                .count(((Number) row[1]).longValue())
                                                .build())
                                .toList();
        }

        // ─── 30-Day Completion Trend ─────────────────────────────────────────────

        @SuppressWarnings("unchecked")
        public List<TaskCompletionTrendResponse> getCompletionTrend() {
                log.debug("Fetching 30-day completion trend");

                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

                String sql = "SELECT DATE(t.completed_at) AS completion_date, COUNT(*) AS cnt " +
                                "FROM tasks t WHERE t.is_deleted = false " +
                                "AND t.status = 'DONE' AND t.completed_at IS NOT NULL " +
                                "AND t.completed_at >= :since " +
                                "GROUP BY DATE(t.completed_at) ORDER BY completion_date ASC";

                List<Object[]> rows = entityManager.createNativeQuery(sql)
                                .setParameter("since", thirtyDaysAgo)
                                .getResultList();

                return rows.stream()
                                .map(row -> TaskCompletionTrendResponse.builder()
                                                .date(((java.sql.Date) row[0]).toLocalDate())
                                                .count(((Number) row[1]).longValue())
                                                .build())
                                .toList();
        }

        // ─── Employee Performance Table ──────────────────────────────────────────

        @SuppressWarnings("unchecked")
        public List<EmployeePerformanceResponse> getEmployeePerformance() {
                log.debug("Fetching employee performance data");

                LocalDate periodStart = LocalDate.now().minusDays(30);
                LocalDate periodEnd = LocalDate.now();

                List<EmployeePerformanceCache> cached = employeePerformanceCacheRepository
                                .findByPeriodStartAndPeriodEndOrderByTasksCompletedDesc(periodStart, periodEnd);

                if (!cached.isEmpty()) {
                        // Use cached data and join employee names
                        return cached.stream().map(perf -> {
                                String name = getEmployeeName(perf.getEmployeeId());
                                return EmployeePerformanceResponse.builder()
                                                .employeeId(perf.getEmployeeId())
                                                .employeeName(name)
                                                .tasksAssigned(perf.getTasksAssigned())
                                                .tasksCompleted(perf.getTasksCompleted())
                                                .onTimeRate(perf.getOnTimeRate())
                                                .avgCompletionHours(perf.getAvgCompletionHours())
                                                .build();
                        }).toList();
                }

                // Fallback: real-time query if no cached data exists yet
                String sql = "SELECT t.assigned_to, " +
                                "COUNT(*) AS assigned, " +
                                "COUNT(*) FILTER (WHERE t.status = 'DONE') AS completed, " +
                                "COALESCE(COUNT(*) FILTER (WHERE t.status = 'DONE' AND t.completed_at <= t.due_date), 0) AS on_time, "
                                +
                                "COALESCE(AVG(EXTRACT(EPOCH FROM (t.completed_at - t.created_at)) / 3600) " +
                                "FILTER (WHERE t.status = 'DONE' AND t.completed_at IS NOT NULL), 0) AS avg_hours " +
                                "FROM tasks t WHERE t.is_deleted = false " +
                                "GROUP BY t.assigned_to ORDER BY completed DESC";

                List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();

                return rows.stream().map(row -> {
                        UUID empId = (UUID) row[0];
                        int assigned = ((Number) row[1]).intValue();
                        int completed = ((Number) row[2]).intValue();
                        long onTime = ((Number) row[3]).longValue();
                        double avgHours = ((Number) row[4]).doubleValue();

                        BigDecimal onTimeRate = completed > 0
                                        ? BigDecimal.valueOf(onTime * 100.0 / completed)
                                                        .setScale(2, java.math.RoundingMode.HALF_UP)
                                        : BigDecimal.ZERO;

                        return EmployeePerformanceResponse.builder()
                                        .employeeId(empId)
                                        .employeeName(getEmployeeName(empId))
                                        .tasksAssigned(assigned)
                                        .tasksCompleted(completed)
                                        .onTimeRate(onTimeRate)
                                        .avgCompletionHours(BigDecimal.valueOf(avgHours)
                                                        .setScale(2, java.math.RoundingMode.HALF_UP))
                                        .build();
                }).toList();
        }

        // ─── Helper: Get employee name by user ID ────────────────────────────────

        private String getEmployeeName(UUID userId) {
                try {
                        String sql = "SELECT CONCAT(e.first_name, ' ', e.last_name) " +
                                        "FROM employees e WHERE e.user_id = :userId";
                        Object result = entityManager.createNativeQuery(sql)
                                        .setParameter("userId", userId)
                                        .getSingleResult();
                        return result != null ? result.toString() : "Unknown";
                } catch (Exception e) {
                        log.warn("Could not find employee name for userId={}", userId);
                        return "Unknown";
                }
        }
}
