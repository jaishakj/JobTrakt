CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT REFERENCES job_applications(id) ON DELETE SET NULL,
    dedupe_key VARCHAR(255) UNIQUE,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(12) NOT NULL DEFAULT 'NORMAL',
    payload TEXT NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT jobs_type_check CHECK (type IN ('INTERVIEW_REMINDER','FOLLOW_UP_REMINDER','APPLICATION_FOLLOW_UP','WEEKLY_REPORT')),
    CONSTRAINT jobs_status_check CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','DEAD')),
    CONSTRAINT jobs_priority_check CHECK (priority IN ('LOW','NORMAL','HIGH')),
    CONSTRAINT jobs_attempts_check CHECK (attempts >= 0 AND max_attempts > 0)
);
CREATE INDEX jobs_queue_pick_idx ON jobs(status, scheduled_at, priority, created_at);
CREATE INDEX jobs_user_idx ON jobs(user_id, created_at DESC);
CREATE INDEX jobs_application_idx ON jobs(application_id);
