-- =============================================
-- V25: Widen daily_metrics task-count columns from INTEGER to BIGINT
-- Reason: DailyMetrics entity fields changed from Integer to Long
--         to match PostgreSQL COUNT(*) return type (bigint).
--         Hibernate validate mode requires column types to match.
-- =============================================

ALTER TABLE daily_metrics
    ALTER COLUMN total_tasks     TYPE BIGINT,
    ALTER COLUMN active_tasks    TYPE BIGINT,
    ALTER COLUMN overdue_tasks   TYPE BIGINT,
    ALTER COLUMN completed_tasks TYPE BIGINT;
