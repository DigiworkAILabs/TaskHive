CREATE TABLE tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assigned_to     UUID NOT NULL REFERENCES employees(id),
    due_date        TIMESTAMP NOT NULL,
    completed_at    TIMESTAMP,
    estimated_hours DECIMAL(6,2),
    tags            TEXT[],
    is_deleted      BOOLEAN DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES users(id),
    updated_by      UUID REFERENCES users(id)
);

CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_tasks_status ON tasks(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_priority ON tasks(priority) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_due_date ON tasks(due_date) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_is_deleted ON tasks(is_deleted);

-- Full-text search index on title and description
CREATE INDEX idx_tasks_fulltext ON tasks USING GIN (to_tsvector('english', COALESCE(title, '') || ' ' || COALESCE(description, '')));
