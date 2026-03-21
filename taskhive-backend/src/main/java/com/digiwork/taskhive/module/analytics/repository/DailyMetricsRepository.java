package com.digiwork.taskhive.module.analytics.repository;

import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyMetricsRepository extends JpaRepository<DailyMetrics, UUID> {

    Optional<DailyMetrics> findTopByOrderByMetricDateDesc();

    List<DailyMetrics> findTop7ByOrderByMetricDateDesc();

    @Modifying
    @Query(value = "INSERT INTO daily_metrics " +
            "(id, metric_date, total_tasks, active_tasks, overdue_tasks, completed_tasks, " +
            "completion_rate, avg_completion_hours, total_employees, created_at) " +
            "VALUES (gen_random_uuid(), :metricDate, :totalTasks, :activeTasks, :overdueTasks, " +
            ":completedTasks, :completionRate, :avgCompletionHours, :totalEmployees, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (metric_date) DO UPDATE SET " +
            "total_tasks = :totalTasks, " +
            "active_tasks = :activeTasks, " +
            "overdue_tasks = :overdueTasks, " +
            "completed_tasks = :completedTasks, " +
            "completion_rate = :completionRate, " +
            "avg_completion_hours = :avgCompletionHours, " +
            "total_employees = :totalEmployees", nativeQuery = true)
    void upsertDailyMetrics(
            @Param("metricDate") LocalDate metricDate,
            @Param("totalTasks") int totalTasks,
            @Param("activeTasks") int activeTasks,
            @Param("overdueTasks") int overdueTasks,
            @Param("completedTasks") int completedTasks,
            @Param("completionRate") BigDecimal completionRate,
            @Param("avgCompletionHours") BigDecimal avgCompletionHours,
            @Param("totalEmployees") int totalEmployees);
}
