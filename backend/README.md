# CampusBridge Backend Service

The official Java Spring Boot backend for **CampusBridge — Intelligent Campus Recruitment & Career Intelligence Platform**.

This service provides enterprise-grade, secure, and type-aligned RESTful APIs connecting Students and Recruiters through automated eligibility checking, vacancy lifecycle tracking, role-based access control, and campus placement management.

---

## Technology Stack

- **Language:** Java 21 (with compatibility for modern JDKs)
- **Framework:** Spring Boot 4.1.2 / Spring Framework 7
- **Security:** Spring Security with Stateless JWT Authentication & BCrypt Password Hashing
- **Database:** MySQL 8.0
- **ORM & Data:** Spring Data JPA with Hibernate ORM 7.4
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Build & Dependency Tool:** Apache Maven (via Maven Wrapper `mvnw`)
- **Testing:** JUnit 5, Mockito, Spring Security Test, Spring Test MockMvc

---

## Core Capabilities

1. **Role-Based Authentication & Authorization**
   - User registration and login for `STUDENT` and `RECRUITER` roles.
   - Stateless JWT tokens containing user identity, claims, and role grants.
   - Granular endpoint-level protection using `@PreAuthorize` and Spring Security matchers.

2. **Deterministic Eligibility Engine**
   - Pure, rule-based verification matching the frontend contract:
     - **Branch Check:** Verifies candidate degree matches required branch (`CS`, `IT`, `ENTC`, `MECH`, `CIVIL`).
     - **CGPA Check:** Boundary-tested numerical threshold evaluation.
     - **Backlogs Check:** Validates maximum permitted active backlog constraint.
     - **Skills Check:** Case-insensitive, whitespace-sanitized candidate skills matching against required competencies.

3. **Placement Drive & Job Lifecycle**
   - Recruiter posting, updating, and removal of campus job drives.
   - Search across job title, company name, and location.
   - Dynamic eligibility rule binding per vacancy.

4. **Application State Machine**
   - Safe student application flow with multi-tier precondition enforcement:
     - Academic profile completeness.
     - Deadline window verification (open until 23:59:59 on the deadline date).
     - Duplicate active application prevention.
     - Strict server-side eligibility check.
   - Student withdrawal restricted strictly to `APPLIED` status.
   - Recruiter candidate management adhering strictly to approved state transitions:
     - `APPLIED` -> `SHORTLISTED` | `REJECTED`
     - `SHORTLISTED` -> `SELECTED` | `REJECTED`
     - Terminal states (`SELECTED`, `REJECTED`, `WITHDRAWN`) are immutable.

5. **Recruiter Insights & Metrics**
   - Aggregated placement pipeline analytics: total posted drives, application volume, shortlisted count, selections, rejections, and pending candidate reviews.

---

## Directory Layout

```text
backend/
├── pom.xml
├── README.md
├── docs/
│   ├── API_DOCUMENTATION.md
│   ├── DATABASE_SCHEMA.md
│   └── SETUP_GUIDE.md
└── src/
    ├── main/
    │   ├── java/com/campusbridge/backend/
    │   │   ├── config/             # Seed data and application configurations
    │   │   ├── controller/         # REST Controllers (/api/auth, /api/jobs, etc.)
    │   │   ├── dto/                # Data Transfer Objects aligned with frontend types
    │   │   ├── entity/             # JPA Entities mapped to MySQL
    │   │   ├── enums/              # Core domain enums (Role, Branch, ApplicationStatus)
    │   │   ├── exception/          # Custom exceptions and GlobalExceptionHandler
    │   │   ├── repository/         # Spring Data JPA repositories
    │   │   ├── security/           # JWT service, filter, UserDetails, and SecurityConfig
    │   │   └── service/            # Business logic and eligibility engine
    │   └── resources/
    │       └── application.properties # Environment-driven configuration
    └── test/
        └── java/com/campusbridge/backend/ # JUnit 5 & Mockito test suites
```

---

## Running the Application

### Prerequisites
- JDK 21 or higher installed
- MySQL 8 service running on `localhost:3306` with database `campusbridge`

### Start Command (Windows PowerShell)
```powershell
.\mvnw.cmd spring-boot:run
```

### Run Tests
```powershell
.\mvnw.cmd test
```

Default local server port is `8080`.
The application automatically seeds initial demo accounts and 5 sample placement drives if the database is clean.
