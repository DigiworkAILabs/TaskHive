package com.digiwork.taskhive.module.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employee_performance_cache", uniqueConstraints = @UniqueConstraint(name = "uq_employee_period", columnNames = {
        "employee_id", "period_start", "period_end" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePerformanceCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "tasks_assigned", nullable = false)
    @Builder.Default
    private Integer tasksAssigned = 0;

    @Column(name = "tasks_completed", nullable = false)
    @Builder.Default
    private Integer tasksCompleted = 0;

    @Column(name = "on_time_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal onTimeRate = BigDecimal.ZERO;

    @Column(name = "avg_completion_hours", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal avgCompletionHours = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
