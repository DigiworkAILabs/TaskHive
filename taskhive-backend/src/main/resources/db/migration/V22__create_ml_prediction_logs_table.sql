-- Phase 7.4 Migration: Create ML Prediction Logs Table
-- Support for priority, completion, workload and productivity history

CREATE TABLE IF NOT EXISTS ml_prediction_logs (
    id UUID PRIMARY KEY,
    feature_type VARCHAR(50) NOT NULL, -- PRIORITY, COMPLETION, WORKLOAD, PRODUCTIVITY
    employee_id UUID REFERENCES users(id),
    input_data JSONB NOT NULL,
    output_data JSONB NOT NULL,
    confidence DOUBLE PRECISION,
    fallback_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ml_logs_employee_feature ON ml_prediction_logs(employee_id, feature_type, created_at DESC);
