CREATE TABLE task_status_history (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID NOT NULL REFERENCES tasks(id),
    old_status  VARCHAR(20),
    new_status  VARCHAR(20) NOT NULL,
    changed_by  UUID NOT NULL REFERENCES users(id),
    comment     VARCHAR(500),
    ip_address  VARCHAR(45),
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_status_history_task_id ON task_status_history(task_id);
