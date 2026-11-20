-- ============================================================================
-- Migration V5: Create applications table
-- Description: Tracks student job applications, statuses, and recruitment lifecycle.
-- ============================================================================

CREATE TABLE IF NOT EXISTS applications (
    id VARCHAR(36) NOT NULL,
    job_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    applied_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_applications PRIMARY KEY (id),

    -- Automatically cascades deletion if target job is removed
    CONSTRAINT fk_applications_job_id FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,

    -- Automatically cascades deletion if applicant student user is removed
    CONSTRAINT fk_applications_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Restricts application state to approved lifecycle statuses
    CONSTRAINT ck_applications_status CHECK (status IN ('APPLIED', 'SHORTLISTED', 'SELECTED', 'REJECTED', 'WITHDRAWN')),

    -- Crucial business rule: Prevents duplicate applications for the same job and student
    CONSTRAINT uq_applications_job_user UNIQUE (job_id, user_id),

    INDEX idx_applications_job_id (job_id),
    INDEX idx_applications_user_id (user_id),
    INDEX idx_applications_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
