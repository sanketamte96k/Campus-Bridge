# CampusBridge REST API Specification

This document details the HTTP endpoints provided by the CampusBridge Spring Boot API.

All authenticated requests require a valid JSON Web Token passed in the `Authorization` header:
```http
Authorization: Bearer <jwt_token>
```

---

## 1. Endpoint Summary

| Method | Path | Required Role | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new user (Student or Recruiter) |
| `POST` | `/api/auth/login` | Public | Authenticate with credentials and receive JWT |
| `GET` | `/api/auth/me` | Authenticated | Retrieve current user profile and role |
| `GET` | `/api/users/me` | Authenticated | Retrieve current user profile |
| `GET` | `/api/users/{id}` | Authenticated | Retrieve user profile by user UUID |
| `GET` | `/api/students/profile` | Student | Retrieve caller's academic profile |
| `PUT` | `/api/students/profile` | Student | Update caller's academic profile |
| `POST` | `/api/students/profile` | Student | Create or initialize caller's academic profile |
| `GET` | `/api/students/{userId}/profile` | Authenticated | Retrieve a student profile by user ID |
| `GET` | `/api/jobs` | Public | List all jobs (supports `?search=` filter) |
| `GET` | `/api/jobs/{id}` | Public | Retrieve detailed job listing by UUID |
| `POST` | `/api/jobs` | Recruiter | Create a new job opening with eligibility rules |
| `PUT` | `/api/jobs/{id}` | Recruiter | Update an existing job opening (ownership enforced) |
| `DELETE` | `/api/jobs/{id}` | Recruiter | Delete a job opening (ownership enforced) |
| `GET` | `/api/jobs/{id}/applicants` | Recruiter | Retrieve applicants for a job drive (ownership enforced) |
| `GET` | `/api/jobs/{id}/eligibility` | Student | Evaluate caller's eligibility against job criteria |
| `GET` | `/api/jobs/{id}/skill-gap` | Student | Compute deterministic skill gap and proximity |
| `POST` | `/api/jobs/{id}/learning-plan` | Student | Generate advisory AI learning plan for missing skills |
| `POST` | `/api/applications` | Student | Apply to a job opening (enforces eligibility & single-active rule) |
| `GET` | `/api/applications/my` | Student | Retrieve all applications submitted by caller |
| `POST` | `/api/applications/{id}/withdraw` | Student | Withdraw submitted application (from `APPLIED` state only) |
| `PATCH` | `/api/applications/{id}/status` | Recruiter | Update applicant status (validates state transition) |
| `PUT` | `/api/applications/{id}/status` | Recruiter | Update applicant status (alias of PATCH) |
| `GET` | `/api/recruiter/jobs` | Recruiter | List all jobs posted by the authenticated recruiter |
| `GET` | `/api/recruiter/jobs/applicant-counts` | Recruiter | Retrieve applicant counts mapped by job ID |
| `GET` | `/api/recruiter/jobs/{jobId}/applicants` | Recruiter | Retrieve applicants for a specific recruiter job |
| `GET` | `/api/recruiter/stats` | Recruiter | Retrieve recruitment dashboard metrics |

---

## 2. Authentication Endpoints

### 2.1. Register User
`POST /api/auth/register`
- **Access:** Public
- **Request Body:**
  ```json
  {
    "name": "Jane Doe",
    "email": "jane@campusbridge.edu",
    "password": "Password123!",
    "role": "STUDENT"
  }
  ```
- **Responses:**
  - `201 Created`: Returns JWT token and user details:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsIn...",
      "role": "STUDENT",
      "name": "Jane Doe",
      "email": "jane@campusbridge.edu",
      "userId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
    }
    ```
  - `400 Bad Request`: Validation error or email already in use.

### 2.2. Login
`POST /api/auth/login`
- **Access:** Public
- **Request Body:**
  ```json
  {
    "email": "jane@campusbridge.edu",
    "password": "Password123!"
  }
  ```
- **Responses:**
  - `200 OK`: Returns JWT token and user info.
  - `401 Unauthorized`: Invalid credentials.

### 2.3. Get Current User
`GET /api/auth/me`
- **Access:** Authenticated
- **Responses:**
  - `200 OK`: Returns current user record (`id`, `name`, `email`, `role`).
  - `401 Unauthorized`: Unauthenticated.

---

## 3. Student Profile Endpoints

### 3.1. Get My Profile
`GET /api/students/profile`
- **Access:** Role `STUDENT`
- **Responses:**
  - `200 OK`: Returns student profile object:
    ```json
    {
      "branch": "CS",
      "cgpa": 8.50,
      "activeBacklogs": 0,
      "skills": ["Java", "Spring Boot", "SQL", "React"]
    }
    ```
  - `404 Not Found`: Profile not yet created.

### 3.2. Update or Create Profile
`PUT /api/students/profile` or `POST /api/students/profile`
- **Access:** Role `STUDENT`
- **Request Body:**
  ```json
  {
    "branch": "CS",
    "cgpa": 8.50,
    "activeBacklogs": 0,
    "skills": ["Java", "Spring Boot", "SQL", "Docker"]
  }
  ```
- **Responses:**
  - `200 OK`: Returns saved profile DTO.
  - `400 Bad Request`: Validation failure (CGPA out of 0.00-10.00 range, backlogs < 0, invalid branch).

---

## 4. Job Endpoints

### 4.1. List Jobs
`GET /api/jobs`
- **Access:** Public
- **Query Parameters:** `search` (optional string for filtering by title, company, or skills)
- **Responses:**
  - `200 OK`: Array of job listings.

### 4.2. Create Job
`POST /api/jobs`
- **Access:** Role `RECRUITER`
- **Request Body:**
  ```json
  {
    "title": "Backend engineer",
    "company": "Cloudmesh",
    "location": "Pune",
    "packageLpa": 10.0,
    "deadline": "2027-01-20",
    "description": "Architect distributed backend systems.",
    "rules": {
      "allowedBranches": ["IT", "CS"],
      "minCgpa": 8.00,
      "maxBacklogs": 0,
      "requiredSkills": ["Java", "Spring Boot", "MySQL", "Docker"]
    }
  }
  ```
- **Responses:**
  - `201 Created`: Returns created `JobDto`.
  - `400 Bad Request`: Invalid dates or criteria.
  - `403 Forbidden`: Caller lacks `RECRUITER` role.

### 4.3. Check Eligibility
`GET /api/jobs/{id}/eligibility`
- **Access:** Role `STUDENT`
- **Responses:**
  - `200 OK`: Returns detailed pass/fail evaluation:
    ```json
    {
      "eligible": false,
      "reasons": [
        "CGPA 8.50 meets minimum 8.00 requirement.",
        "0 active backlogs within maximum limit of 0.",
        "Branch CS is eligible.",
        "Missing required skills: MySQL, Docker."
      ]
    }
    ```

### 4.4. Skill Gap Analysis
`GET /api/jobs/{id}/skill-gap`
- **Access:** Role `STUDENT`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "matchedSkills": ["Java", "Spring Boot"],
      "missingSkills": ["MySQL", "Docker"],
      "cgpaGap": 0.00,
      "backlogsOver": 0,
      "branchAllowed": true,
      "requiredBranches": ["IT", "CS"],
      "closeToEligible": true
    }
    ```
  - `400 Bad Request`: Student profile not yet created.
  - `404 Not Found`: Job does not exist.

### 4.5. Generate Advisory Learning Plan
`POST /api/jobs/{id}/learning-plan`
- **Access:** Role `STUDENT`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "aiGenerated": false,
      "disclaimer": "AI-generated suggestions. They do not affect your eligibility.",
      "summary": "Targeted study plan to help you build the missing skills required for this role.",
      "steps": [
        {
          "skill": "MySQL",
          "whyItMatters": "Proficiency in MySQL is required for this role.",
          "practiceIdeas": [
            "Build a small project that uses MySQL",
            "Complete guided tutorials and exercises for MySQL",
            "Review official documentation and core concepts of MySQL"
          ],
          "estimatedHours": 10
        }
      ]
    }
    ```
  - `429 Too Many Requests`: Caller exceeded rate limit (10 requests/hour per user).

---

## 5. Application Endpoints

### 5.1. Apply to Job
`POST /api/applications`
- **Access:** Role `STUDENT`
- **Request Body:** `{"jobId": "e798e973-8139-4c51-920b-3b716eb0cf65"}`
- **Responses:**
  - `201 Created`: Returns created/reactivated `ApplicationDto`.
  - `400 Bad Request`: Deadline passed, student does not meet eligibility requirements, or duplicate active application exists.

### 5.2. Get My Applications
`GET /api/applications/my`
- **Access:** Role `STUDENT`
- **Responses:**
  - `200 OK`: List of application records with current status.

### 5.3. Withdraw Application
`POST /api/applications/{id}/withdraw`
- **Access:** Role `STUDENT`
- **Responses:**
  - `200 OK`: Updated application with `status: "WITHDRAWN"`.
  - `400 Bad Request`: Application is not in `APPLIED` status (e.g. already shortlisted).
  - `403 Forbidden`: Application belongs to another student.

### 5.4. Update Application Status
`PATCH /api/applications/{id}/status` or `PUT /api/applications/{id}/status`
- **Access:** Role `RECRUITER`
- **Request Body:** `{"status": "SHORTLISTED"}`
- **Responses:**
  - `200 OK`: Returns updated `ApplicationDto`.
  - `400 Bad Request`: Illegal status transition according to state machine.
  - `403 Forbidden`: Job does not belong to authenticated recruiter.

---

## 6. Recruiter Endpoints

### 6.1. Get Recruiter Statistics
`GET /api/recruiter/stats`
- **Access:** Role `RECRUITER`
- **Responses:**
  - `200 OK`: Aggregated placement metrics:
    ```json
    {
      "totalJobs": 5,
      "activeDrives": 3,
      "totalApplications": 42,
      "shortlistedCount": 12,
      "selectedCount": 4
    }
    ```
