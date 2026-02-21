CREATE TABLE employee_status_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL REFERENCES employees(id),
    old_status      VARCHAR(50),
    new_status      VARCHAR(50) NOT NULL,
    changed_by      UUID NOT NULL REFERENCES users(id),
    reason          VARCHAR(500),
    changed_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_emp_status_history_employee_id ON employee_status_history(employee_id);
