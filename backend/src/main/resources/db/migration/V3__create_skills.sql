-- ============================================================================
-- Migration V3: Create skills and student_profile_skills tables
-- Description: Master skills lookup catalog and student profile skill mappings.
-- ============================================================================

-- Master skill catalog for standardized skill tagging and search
CREATE TABLE IF NOT EXISTS skills (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,

    CONSTRAINT pk_skills PRIMARY KEY (id),

    -- Prevents duplicate skill names
    CONSTRAINT uq_skills_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Normalized collection table mapped by StudentProfile JPA @ElementCollection
CREATE TABLE IF NOT EXISTS student_profile_skills (
    profile_id VARCHAR(36) NOT NULL,
    skill VARCHAR(100) NOT NULL,

    -- Removes associated skill entries when a student profile is deleted
    CONSTRAINT fk_student_profile_skills_profile_id FOREIGN KEY (profile_id) REFERENCES student_profiles(id) ON DELETE CASCADE,

    INDEX idx_student_profile_skills_profile_id (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
