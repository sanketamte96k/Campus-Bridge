-- ============================================================================
-- Migration V2: Create student_profiles table
-- Description: Stores academic credentials and department info for students.
-- ============================================================================

CREATE TABLE IF NOT EXISTS student_profiles (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    branch VARCHAR(20) NOT NULL,
    cgpa DOUBLE NOT NULL,
    active_backlogs INT NOT NULL DEFAULT 0,

    CONSTRAINT pk_student_profiles PRIMARY KEY (id),

    -- Enforces strict one-to-one relationship between a student user and their profile
    CONSTRAINT uq_student_profiles_user_id UNIQUE (user_id),

    -- Automatically removes academic profile when the parent user record is deleted
    CONSTRAINT fk_student_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Limits branch selection to eligible institutional engineering departments
    CONSTRAINT ck_student_profiles_branch CHECK (branch IN ('CS', 'IT', 'ENTC', 'MECH', 'CIVIL')),

    -- Enforces standard 10-point cumulative grade point average boundary
    CONSTRAINT ck_student_profiles_cgpa CHECK (cgpa >= 0.0 AND cgpa <= 10.0),

    -- Active backlogs count cannot be negative
    CONSTRAINT ck_student_profiles_active_backlogs CHECK (active_backlogs >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
