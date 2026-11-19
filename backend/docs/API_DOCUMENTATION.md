# CampusBridge REST API Specification

All endpoints communicate via standard JSON payloads. All protected routes require a Bearer token in the `Authorization` HTTP header:
`Authorization: Bearer <jwt-token>`

---

## 1. Authentication Endpoints

### Register User
- **Method:** `POST`
- **Path:** `/api/auth/register`
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
- **Response (`201 Created`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": "c89b70ad-681e-450b-93f4-79fa69911e28",
  "name": "Jane Doe",
  "email": "jane@campusbridge.edu",
  "role": "STUDENT",
  "user": {
    "id": "c89b70ad-681e-450b-93f4-79fa69911e28",
    "name": "Jane Doe",
    "email": "jane@campusbridge.edu",
    "role": "STUDENT"
  }
}
```

### Login
- **Method:** `POST`
- **Path:** `/api/auth/login`
- **Access:** Public
- **Request Body:**
```json
{
  "email": "student@campusbridge.edu",
  "password": "Password123!"
}
```
- **Response (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": "...",
  "name": "Alex Rivera",
  "email": "student@campusbridge.edu",
  "role": "STUDENT",
  "user": { ... }
}
```

### Current Authenticated User
- **Method:** `GET`
- **Path:** `/api/auth/me`
- **Access:** Authenticated
- **Response (`200 OK`):**
```json
{
  "id": "c89b70ad-681e-450b-93f4-79fa69911e28",
  "name": "Alex Rivera",
  "email": "student@campusbridge.edu",
  "role": "STUDENT"
}
```

---

## 2. Student Profile Endpoints

### Get Current Student Profile
- **Method:** `GET`
- **Path:** `/api/students/profile`
- **Access:** Authenticated (`STUDENT` role)
- **Response (`200 OK`):**
```json
{
  "branch": "CS",
  "cgpa": 8.5,
  "activeBacklogs": 0,
  "skills": ["Java", "Spring Boot", "SQL", "React", "TypeScript", "Tailwind"]
}
```

### Save / Update Profile
- **Method:** `PUT` or `POST`
- **Path:** `/api/students/profile`
- **Access:** Authenticated (`STUDENT` role)
- **Request Body:**
```json
{
  "branch": "CS",
  "cgpa": 8.7,
  "activeBacklogs": 0,
  "skills": ["Java", "Spring Boot", "MySQL", "React", "Docker"]
}
```
- **Response (`200 OK`):** Updated `StudentProfile` JSON.

---

## 3. Job Drive Endpoints

### List / Search Jobs
- **Method:** `GET`
- **Path:** `/api/jobs`
- **Query Params:** `search` (optional string, e.g. `/api/jobs?search=Java`)
- **Access:** Public
- **Response (`200 OK`):** Array of `Job` objects:
```json
[
  {
    "id": "job-1",
    "title": "Java developer",
    "company": "Northwind Technologies",
    "location": "Pune",
    "packageLpa": 6.0,
    "deadline": "2026-12-15",
    "description": "Build robust enterprise backend services and scalable RESTful APIs with clean architecture.",
    "rules": {
      "allowedBranches": ["IT", "CS"],
      "minCgpa": 7.5,
      "maxBacklogs": 0,
      "requiredSkills": ["Java", "SQL", "Spring Boot"]
    },
    "postedBy": "recruiter-uuid"
  }
]
```

### Get Job by ID
- **Method:** `GET`
- **Path:** `/api/jobs/{id}`
- **Access:** Public
- **Response (`200 OK`):** Single `Job` object.

### Create Job
- **Method:** `POST`
- **Path:** `/api/jobs`
- **Access:** Authenticated (`RECRUITER` role)
- **Request Body:**
```json
{
  "title": "Cloud Engineer",
  "company": "Apex Cloud Systems",
  "location": "Pune",
  "packageLpa": 11.5,
  "deadline": "2026-12-30",
  "description": "Develop and maintain automated cloud platforms.",
  "rules": {
    "allowedBranches": ["CS", "IT"],
    "minCgpa": 8.0,
    "maxBacklogs": 0,
    "requiredSkills": ["AWS", "Terraform", "Kubernetes", "Linux"]
  }
}
```
- **Response (`201 Created`):** Created `Job` object.

### Update Job
- **Method:** `PUT`
- **Path:** `/api/jobs/{id}`
- **Access:** Authenticated (`RECRUITER` role, owner only)

### Delete Job
- **Method:** `DELETE`
- **Path:** `/api/jobs/{id}`
- **Access:** Authenticated (`RECRUITER` role, owner only)
- **Response (`204 No Content`)**

### Evaluate Eligibility for Job
- **Method:** `GET`
- **Path:** `/api/jobs/{id}/eligibility`
- **Access:** Authenticated (`STUDENT` role)
- **Response (`200 OK`):**
```json
{
  "eligible": true,
  "checks": [
    {
      "rule": "BRANCH",
      "passed": true,
      "message": "Your branch (CS) is allowed."
    },
    {
      "rule": "CGPA",
      "passed": true,
      "message": "Your CGPA (8.5) meets the minimum of 7.5."
    },
    {
      "rule": "BACKLOGS",
      "passed": true,
      "message": "You have 0 active backlogs. The maximum is 0."
    },
    {
      "rule": "SKILLS",
      "passed": true,
      "message": "You have all required skills."
    }
  ]
}
```

---

## 4. Application Endpoints

### Apply to Job
- **Method:** `POST`
- **Path:** `/api/applications`
- **Access:** Authenticated (`STUDENT` role)
- **Request Body:**
```json
{
  "jobId": "job-1"
}
```
- **Response (`201 Created`):**
```json
{
  "id": "e89b3f30-811c-4b36-a518-8f5539fa07f6",
  "jobId": "job-1",
  "userId": "student-uuid",
  "status": "APPLIED",
  "appliedAt": "2026-11-19T18:30:00"
}
```

### Get My Applications
- **Method:** `GET`
- **Path:** `/api/applications/my`
- **Access:** Authenticated (`STUDENT` role)
- **Response (`200 OK`):** Array of `Application` objects.

### Withdraw Application
- **Method:** `POST`
- **Path:** `/api/applications/{id}/withdraw`
- **Access:** Authenticated (`STUDENT` role, owner only)
- **Constraint:** Application status must currently be `APPLIED`.
- **Response (`200 OK`):** Updated `Application` object with `status: "WITHDRAWN"`.

### Update Application Status
- **Method:** `PATCH` or `PUT`
- **Path:** `/api/applications/{id}/status`
- **Access:** Authenticated (`RECRUITER` role, owner of the job)
- **Request Body:**
```json
{
  "status": "SHORTLISTED"
}
```
- **Allowed Transitions:**
  - `APPLIED` -> `SHORTLISTED` | `REJECTED`
  - `SHORTLISTED` -> `SELECTED` | `REJECTED`
- **Response (`200 OK`):** Updated `Application` object.

---

## 5. Recruiter Portal Endpoints

### Get Recruiter's Posted Jobs
- **Method:** `GET`
- **Path:** `/api/recruiter/jobs`
- **Access:** Authenticated (`RECRUITER` role)
- **Response (`200 OK`):** Array of `Job` objects created by the current recruiter.

### Get Applicants for Job
- **Method:** `GET`
- **Path:** `/api/recruiter/jobs/{jobId}/applicants` (or `/api/jobs/{id}/applicants`)
- **Access:** Authenticated (`RECRUITER` role, owner only)
- **Response (`200 OK`):**
```json
[
  {
    "application": {
      "id": "app-uuid",
      "jobId": "job-1",
      "userId": "usr-student",
      "status": "APPLIED",
      "appliedAt": "2026-11-19T18:30:00"
    },
    "name": "Alex Rivera",
    "email": "student@campusbridge.edu",
    "profile": {
      "branch": "CS",
      "cgpa": 8.5,
      "activeBacklogs": 0,
      "skills": ["Java", "Spring Boot", "SQL", "React"]
    }
  }
]
```

### Recruiter Analytics & Stats
- **Method:** `GET`
- **Path:** `/api/recruiter/stats`
- **Access:** Authenticated (`RECRUITER` role)
- **Response (`200 OK`):**
```json
{
  "totalJobs": 5,
  "totalApplications": 12,
  "shortlistedCount": 4,
  "selectedCount": 2,
  "rejectedCount": 3,
  "pendingReviewCount": 3
}
```
