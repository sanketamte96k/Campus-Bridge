-- ============================================================================
-- Migration V6: Schema Hardening & Integrity Enforcement
-- Description:
--   1. Converts student CGPA and job minimum CGPA to DECIMAL(4,2) for exact arithmetic.
--   2. Renames users.password to password_hash to explicitly document credential storage.
--   3. Replaces cascade deletes on applications with ON DELETE RESTRICT to preserve audit history.
--   4. Drops the unused master skills catalog table.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Convert student_profiles.cgpa to DECIMAL(4,2) NOT NULL
-- Why: Binary floating-point (DOUBLE) arithmetic causes IEEE 754 precision inaccuracies
-- (e.g. 6.999999999 vs 7.00). Academic CGPAs are decimal quantities defined to two
-- decimal places. DECIMAL(4,2) ensures exact, reproducible eligibility checks.
-- The existing CHECK constraint ck_student_profiles_cgpa is preserved by MySQL 8.
-- ----------------------------------------------------------------------------
ALTER TABLE student_profiles
    MODIFY COLUMN cgpa DECIMAL(4,2) NOT NULL;

-- ----------------------------------------------------------------------------
-- 2. Convert jobs.min_cgpa to DECIMAL(4,2) NOT NULL
-- Why: Eliminates floating-point threshold drift when evaluating student eligibility
-- cutoffs against job opening requirements.
-- The existing CHECK constraint ck_jobs_min_cgpa is preserved by MySQL 8.
-- ----------------------------------------------------------------------------
ALTER TABLE jobs
    MODIFY COLUMN min_cgpa DECIMAL(4,2) NOT NULL;

-- ----------------------------------------------------------------------------
-- 3. Rename users.password to password_hash
-- Why: Column naming should accurately communicate domain semantics and security
-- posture. Plaintext passwords are never stored; the column strictly holds 60-character
-- one-way BCrypt cryptographic hashes.
-- ----------------------------------------------------------------------------
ALTER TABLE users
    RENAME COLUMN password TO password_hash;

-- ----------------------------------------------------------------------------
-- 4. Replace applications foreign keys with ON DELETE RESTRICT
-- Why: Campus recruitment records, selection statuses, and application audits are
-- institutional compliance history. ON DELETE CASCADE risked accidental destruction
-- of candidate placement history if a job listing or user was deleted.
-- ON DELETE RESTRICT guarantees that applications cannot be deleted unintentionally.
-- ----------------------------------------------------------------------------
ALTER TABLE applications
    DROP FOREIGN KEY fk_applications_job_id;

ALTER TABLE applications
    DROP FOREIGN KEY fk_applications_user_id;

ALTER TABLE applications
    ADD CONSTRAINT fk_applications_job_id FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE RESTRICT;

ALTER TABLE applications
    ADD CONSTRAINT fk_applications_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- ----------------------------------------------------------------------------
-- 5. Drop unused skills table
-- Why: Skills are stored as text collections directly in student_profile_skills and
-- job_required_skills, which are mapped by JPA @ElementCollection on the respective
-- entities. The standalone master skills table is unmapped in the JPA entity layer
-- and unused by the application.
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS skills;
