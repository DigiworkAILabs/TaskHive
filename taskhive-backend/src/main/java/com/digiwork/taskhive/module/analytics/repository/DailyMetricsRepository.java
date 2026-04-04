package com.digiwork.taskhive.module.analytics.repository;

import com.digiwork.taskhive.module.analytics.model.DailyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


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
            "VALUES (gen_random_uuid(), :#{#m.metricDate}, :#{#m.totalTasks}, :#{#m.activeTasks}, :#{#m.overdueTasks}, " +
            ":#{#m.completedTasks}, :#{#m.completionRate}, :#{#m.avgCompletionHours}, :#{#m.totalEmployees}, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (metric_date) DO UPDATE SET " +
            "total_tasks = :#{#m.totalTasks}, " +
            "active_tasks = :#{#m.activeTasks}, " +
            "overdue_tasks = :#{#m.overdueTasks}, " +
            "completed_tasks = :#{#m.completedTasks}, " +
            "completion_rate = :#{#m.completionRate}, " +
            "avg_completion_hours = :#{#m.avgCompletionHours}, " +
            "total_employees = :#{#m.totalEmployees}", nativeQuery = true)
    void upsertDailyMetrics(@Param("m") DailyMetrics m);
}
