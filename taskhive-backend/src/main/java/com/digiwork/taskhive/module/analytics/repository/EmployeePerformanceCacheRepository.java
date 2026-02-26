package com.digiwork.taskhive.module.analytics.repository;

import com.digiwork.taskhive.module.analytics.model.EmployeePerformanceCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeePerformanceCacheRepository extends JpaRepository<EmployeePerformanceCache, UUID> {

    List<EmployeePerformanceCache> findByPeriodStartAndPeriodEndOrderByTasksCompletedDesc(
            LocalDate periodStart, LocalDate periodEnd);

    @Modifying
    @Query(value = "INSERT INTO employee_performance_cache " +
            "(id, employee_id, period_start, period_end, tasks_assigned, tasks_completed, " +
            "on_time_rate, avg_completion_hours, created_at) " +
            "VALUES (gen_random_uuid(), :employeeId, :periodStart, :periodEnd, :tasksAssigned, " +
            ":tasksCompleted, :onTimeRate, :avgCompletionHours, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (employee_id, period_start, period_end) DO UPDATE SET " +
            "tasks_assigned = :tasksAssigned, " +
            "tasks_completed = :tasksCompleted, " +
            "on_time_rate = :onTimeRate, " +
            "avg_completion_hours = :avgCompletionHours", nativeQuery = true)
    void upsertEmployeePerformance(
            @Param("employeeId") UUID employeeId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("tasksAssigned") int tasksAssigned,
            @Param("tasksCompleted") int tasksCompleted,
            @Param("onTimeRate") BigDecimal onTimeRate,
            @Param("avgCompletionHours") BigDecimal avgCompletionHours);
}
