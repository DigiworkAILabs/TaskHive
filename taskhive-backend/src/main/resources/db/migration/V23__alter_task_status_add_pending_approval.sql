-- V23: Phase 1 v2.5 — PENDING_APPROVAL status support
--
-- tasks.status is VARCHAR(20) (see V11), NOT a PostgreSQL enum type.
-- VARCHAR columns accept any string value natively, so no DDL change
-- is needed to support 'PENDING_APPROVAL' as a status value.
-- The Java enum TaskStatus.java controls valid values at the application layer.
SELECT 1; -- intentional no-op to satisfy Flyway's requirement for non-empty scripts
