package com.digiwork.taskhive.module.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "metric_date", nullable = false, unique = true)
    private LocalDate metricDate;

    @Column(name = "total_tasks", nullable = false)
    @Builder.Default
    private Long totalTasks = 0L;

    @Column(name = "active_tasks", nullable = false)
    @Builder.Default
    private Long activeTasks = 0L;

    @Column(name = "overdue_tasks", nullable = false)
    @Builder.Default
    private Long overdueTasks = 0L;

    @Column(name = "completed_tasks", nullable = false)
    @Builder.Default
    private Long completedTasks = 0L;

    @Column(name = "completion_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(name = "avg_completion_hours", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal avgCompletionHours = BigDecimal.ZERO;

    @Column(name = "total_employees", nullable = false)
    @Builder.Default
    private Integer totalEmployees = 0;

    // Phase 1 v2.5
    @Column(name = "late_tasks")
    @Builder.Default
    private Integer lateTasks = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
