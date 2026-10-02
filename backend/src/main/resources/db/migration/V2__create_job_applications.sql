CREATE TABLE job_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company VARCHAR(160) NOT NULL,
    position VARCHAR(160) NOT NULL,
    location VARCHAR(160),
    employment_type VARCHAR(20) NOT NULL,
    salary NUMERIC(12, 2),
    status VARCHAR(20) NOT NULL,
    applied_date DATE NOT NULL,
    interview_date TIMESTAMP WITH TIME ZONE,
    follow_up_date TIMESTAMP WITH TIME ZONE,
    job_url VARCHAR(2048),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT job_applications_employment_type_check CHECK (employment_type IN ('FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','FREELANCE')),
    CONSTRAINT job_applications_status_check CHECK (status IN ('SAVED','APPLIED','SCREENING','INTERVIEW','OFFER','REJECTED','WITHDRAWN')),
    CONSTRAINT job_applications_salary_check CHECK (salary IS NULL OR salary >= 0)
);
CREATE INDEX job_applications_user_idx ON job_applications(user_id);
CREATE INDEX job_applications_user_status_idx ON job_applications(user_id, status);
CREATE INDEX job_applications_created_at_idx ON job_applications(created_at DESC);
CREATE INDEX job_applications_interview_idx ON job_applications(user_id, interview_date);
