CREATE TABLE notification_preferences (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id),
    email_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    task_assigned   BOOLEAN NOT NULL DEFAULT TRUE,
    task_overdue    BOOLEAN NOT NULL DEFAULT TRUE,
    daily_digest    BOOLEAN NOT NULL DEFAULT FALSE,
    digest_time     TIME DEFAULT '08:00',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
