-- ===================================================================
-- V24: Phase 1 v2.5 — Foundation Hardening columns
-- ===================================================================

-- Late submission tracking
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS is_late          BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS late_by_minutes  INTEGER;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS submitted_at     TIMESTAMP;

-- Proof / approval snapshot flags (future task-type engine, Phase 3)
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS proof_required    BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS approval_required BOOLEAN NOT NULL DEFAULT FALSE;

-- Attachment purpose for proof enforcement
ALTER TABLE task_attachments ADD COLUMN IF NOT EXISTS attachment_purpose VARCHAR(50) DEFAULT 'GENERAL';

-- Analytics: late counts
ALTER TABLE daily_metrics ADD COLUMN IF NOT EXISTS late_tasks INTEGER DEFAULT 0;
ALTER TABLE employee_performance_cache ADD COLUMN IF NOT EXISTS tasks_late INTEGER DEFAULT 0;

-- Performance index
CREATE INDEX IF NOT EXISTS idx_tasks_is_late ON tasks(is_late) WHERE is_late = TRUE;
