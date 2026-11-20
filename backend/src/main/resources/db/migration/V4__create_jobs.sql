-- ============================================================================
-- Migration V4: Create jobs, job_allowed_branches, and job_required_skills tables
-- Description: Manages recruiter job postings and embedded eligibility rules.
-- ============================================================================

CREATE TABLE IF NOT EXISTS jobs (
    id VARCHAR(36) NOT NULL,
    title VARCHAR(150) NOT NULL,
    company VARCHAR(150) NOT NULL,
    location VARCHAR(100) NOT NULL,
    package_lpa DOUBLE NOT NULL,
    deadline DATE NOT NULL,
    description TEXT NOT NULL,
    posted_by VARCHAR(36) DEFAULT NULL,
    min_cgpa DOUBLE NOT NULL,
    max_backlogs INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_jobs PRIMARY KEY (id),

    -- Retains job drive records even if recruiter user account is removed
    CONSTRAINT fk_jobs_posted_by FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE SET NULL,

    -- Annual salary compensation package must be strictly positive
    CONSTRAINT ck_jobs_package_lpa CHECK (package_lpa > 0),

    -- Eligibility rule: minimum CGPA threshold boundary constraint (0.0 to 10.0 scale)
    CONSTRAINT ck_jobs_min_cgpa CHECK (min_cgpa >= 0.0 AND min_cgpa <= 10.0),

    -- Eligibility rule: maximum allowed active backlogs constraint cannot be negative
    CONSTRAINT ck_jobs_max_backlogs CHECK (max_backlogs >= 0),

    INDEX idx_jobs_posted_by (posted_by),
    INDEX idx_jobs_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Relation table: allowed academic branches for each job opening
CREATE TABLE IF NOT EXISTS job_allowed_branches (
    job_id VARCHAR(36) NOT NULL,
    branch VARCHAR(20) NOT NULL,

    -- Removes branch criteria when parent job opening is removed
    CONSTRAINT fk_job_allowed_branches_job_id FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,

    -- Restricts allowed branch to approved institutional disciplines
    CONSTRAINT ck_job_allowed_branches_branch CHECK (branch IN ('CS', 'IT', 'ENTC', 'MECH', 'CIVIL')),

    INDEX idx_job_allowed_branches_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Relation table: required skills for each job opening
CREATE TABLE IF NOT EXISTS job_required_skills (
    job_id VARCHAR(36) NOT NULL,
    skill VARCHAR(100) NOT NULL,

    -- Removes skill requirements when parent job opening is removed
    CONSTRAINT fk_job_required_skills_job_id FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,

    INDEX idx_job_required_skills_job_id (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
