-- =============================================
-- Phase 6: Analytics Module — Daily Metrics & Employee Performance Cache
-- =============================================

-- Daily pre-calculated metrics (populated by nightly scheduler)
CREATE TABLE daily_metrics (
    id              UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    metric_date     DATE NOT NULL UNIQUE,
    total_tasks     INTEGER NOT NULL DEFAULT 0,
    active_tasks    INTEGER NOT NULL DEFAULT 0,
    overdue_tasks   INTEGER NOT NULL DEFAULT 0,
    completed_tasks INTEGER NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    avg_completion_hours DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_employees INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_daily_metrics_date ON daily_metrics (metric_date);

-- Cached employee performance per period (populated by nightly scheduler)
CREATE TABLE employee_performance_cache (
    id                   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    employee_id          UUID NOT NULL REFERENCES users(id),
    period_start         DATE NOT NULL,
    period_end           DATE NOT NULL,
    tasks_assigned       INTEGER NOT NULL DEFAULT 0,
    tasks_completed      INTEGER NOT NULL DEFAULT 0,
    on_time_rate         DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    avg_completion_hours DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_employee_period UNIQUE (employee_id, period_start, period_end)
);

CREATE INDEX idx_emp_perf_employee ON employee_performance_cache (employee_id);
CREATE INDEX idx_emp_perf_period ON employee_performance_cache (period_start, period_end);
