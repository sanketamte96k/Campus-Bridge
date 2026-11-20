-- ============================================================================
-- Migration V1: Create users table
-- Description: Core authentication and identity entity for students and recruiters.
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_users PRIMARY KEY (id),

    -- Ensures unique email for login credentials and communication
    CONSTRAINT uq_users_email UNIQUE (email),

    -- Restricts user role strictly to supported platform roles
    CONSTRAINT ck_users_role CHECK (role IN ('STUDENT', 'RECRUITER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
