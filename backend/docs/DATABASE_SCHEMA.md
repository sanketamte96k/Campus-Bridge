# CampusBridge Database Schema

Database engine: **MySQL 8.0**
Default database name: `campusbridge`

---

## Entity Relationship Overview

```text
+---------------------+           +------------------------+
|        users        | 1       1 |    student_profiles    |
+---------------------+-----------+------------------------+
| id (PK, VARCHAR)    |           | id (PK, VARCHAR)       |
| name (VARCHAR)      |           | user_id (FK, UNIQUE)   |
| email (VARCHAR, UNQ)|           | branch (VARCHAR)       |
| password (VARCHAR)  |           | cgpa (DOUBLE)          |
| role (VARCHAR)      |           | active_backlogs (INT)  |
| created_at          |           +-----------+------------+
| updated_at          |                       | 1
+----------+----------+                       | N
           | 1                                +------------------------+
           |                                  | student_profile_skills |
           | N                                +------------------------+
+----------+----------+                       | profile_id (FK)        |
|        jobs         |                       | skill (VARCHAR)        |
+---------------------+                       +------------------------+
| id (PK, VARCHAR)    |
| title (VARCHAR)     |
| company (VARCHAR)   |
| location (VARCHAR)  |
| package_lpa (DOUBLE)|
| deadline (DATE)     |
| description (TEXT)  |
| posted_by (VARCHAR) |
| min_cgpa (DOUBLE)   |
| max_backlogs (INT)  |
| created_at          |
| updated_at          |
+----------+----------+
           | 1
           +-------------+
           | 1           | 1
           | N           | N
+----------+----------+  +----------------------+
|job_allowed_branches |  | job_required_skills  |
+---------------------+  +----------------------+
| job_id (FK)         |  | job_id (FK)          |
| branch (VARCHAR)    |  | skill (VARCHAR)      |
+---------------------+  +----------------------+

+---------------------+
|    applications     |
+---------------------+
| id (PK, VARCHAR)    |
| job_id (VARCHAR)    |
| user_id (VARCHAR)   |
| status (VARCHAR)    |
| applied_at          |
+---------------------+
```

---

## Table Definitions

### 1. `users`
Stores user credentials and role grants.
- `id` (VARCHAR(36), PK): UUID identifier.
- `name` (VARCHAR(100), NOT NULL): Full name of the user.
- `email` (VARCHAR(150), UNIQUE, NOT NULL): Normalized lowercase email address.
- `password` (VARCHAR(255), NOT NULL): BCrypt hashed password string.
- `role` (VARCHAR(20), NOT NULL): `STUDENT` or `RECRUITER`.
- `created_at` (DATETIME, NOT NULL): Registration timestamp.
- `updated_at` (DATETIME, NOT NULL): Last update timestamp.

### 2. `student_profiles`
Academic credentials for students.
- `id` (VARCHAR(36), PK): UUID identifier.
- `user_id` (VARCHAR(36), UNIQUE, NOT NULL): Foreign key referencing `users(id)`.
- `branch` (VARCHAR(20), NOT NULL): Student branch (`CS`, `IT`, `ENTC`, `MECH`, `CIVIL`).
- `cgpa` (DOUBLE, NOT NULL): Academic CGPA between 0.0 and 10.0.
- `active_backlogs` (INT, NOT NULL): Number of active backlogs.

### 3. `student_profile_skills`
Normalized collection table for student skills.
- `profile_id` (VARCHAR(36), FK): References `student_profiles(id)`.
- `skill` (VARCHAR(100), NOT NULL): Skill keyword (e.g., "Java", "SQL").

### 4. `jobs`
Job vacancies and campus placement drives.
- `id` (VARCHAR(36), PK): UUID identifier.
- `title` (VARCHAR(150), NOT NULL): Job position title.
- `company` (VARCHAR(150), NOT NULL): Company name.
- `location` (VARCHAR(100), NOT NULL): Job location.
- `package_lpa` (DOUBLE, NOT NULL): Annual compensation in LPA.
- `deadline` (DATE, NOT NULL): Application closing date.
- `description` (TEXT, NOT NULL): Detailed job description.
- `posted_by` (VARCHAR(36)): User ID of the posting recruiter.
- `min_cgpa` (DOUBLE, NOT NULL): Embedded eligibility minimum CGPA.
- `max_backlogs` (INT, NOT NULL): Embedded eligibility maximum allowed backlogs.
- `created_at` (DATETIME, NOT NULL): Timestamp of posting.
- `updated_at` (DATETIME, NOT NULL): Timestamp of modification.

### 5. `job_allowed_branches`
- `job_id` (VARCHAR(36), FK): References `jobs(id)`.
- `branch` (VARCHAR(20)): Permitted branch name.

### 6. `job_required_skills`
- `job_id` (VARCHAR(36), FK): References `jobs(id)`.
- `skill` (VARCHAR(100)): Mandatory skill keyword.

### 7. `applications`
Placement applications submitted by students.
- `id` (VARCHAR(36), PK): UUID identifier.
- `job_id` (VARCHAR(36), NOT NULL): Target job reference.
- `user_id` (VARCHAR(36), NOT NULL): Applicant student reference.
- `status` (VARCHAR(20), NOT NULL): `APPLIED`, `SHORTLISTED`, `SELECTED`, `REJECTED`, or `WITHDRAWN`.
- `applied_at` (DATETIME, NOT NULL): Timestamp of submission.
- **Indexes:**
  - `idx_applications_job_id` on `(job_id)`
  - `idx_applications_user_id` on `(user_id)`
