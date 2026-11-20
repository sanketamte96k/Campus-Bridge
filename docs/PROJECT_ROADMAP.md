# Project Implementation Roadmap
## CampusBridge – Intelligent Campus Recruitment & Career Intelligence Platform

This roadmap outlines the phased development lifecycle for **CampusBridge**. It guides developers, architects, and learners from initial architecture planning through to full-stack implementation, AI service integration, automated testing, and deployment preparation.

---

## 🗺️ Roadmap Overview

```text
[Phase 0] Setup & Planning (CURRENT)
    │
    ▼
[Phase 1] Core Backend & Database Foundation
    │
    ▼
[Phase 2] Frontend Foundation & Role Portals
    │
    ▼
[Phase 3] Placement Workflows & Eligibility Engine
    │
    ▼
[Phase 4] Analytics, Reporting & Communication
    │
    ▼
[Phase 5] Python AI Service & Career Intelligence
    │
    ▼
[Phase 6] Testing, Dockerization & Production Prep
```

---

## 📍 Phase 0: Project Setup, Planning & Architecture

### Objectives
- Define complete software specifications, system architecture, and development standards.
- Initialize the root workspace structure with required documentation and environment templates.
- Establish architectural boundaries, avoiding premature overengineering while preparing for future scaling.

### Features Planned
- High-level and low-level system designs.
- Software Requirements Specification (SRS) formulation.
- Environment template (`.env.example`) and comprehensive `.gitignore` creation.

### Skills to Learn
- Enterprise software requirements engineering (IEEE conventions).
- Decoupled client-server and microservice architecture design.
- Full-stack project organization and repository structuring.

### Expected Deliverables
- `README.md`
- `.gitignore`
- `.env.example`
- `docs/SRS.md`
- `docs/SYSTEM_ARCHITECTURE.md`
- `docs/PROJECT_ROADMAP.md`

### Completion Criteria
- [x] All planning documents and initial project structure files exist in repository root.
- [x] Clear demarcation between frontend, core backend, database, and AI service is documented.
- [x] Zero source code or dependencies generated prematurely.

---

## 📍 Phase 1: Core Backend & Database Foundation

### Objectives
- Initialize the Java 21 / Spring Boot 3 backend application.
- Configure MySQL 8 database connectivity with Spring Data JPA and Hibernate.
- Implement User entity schemas, Role-Based Access Control (RBAC), and stateless JWT authentication.

### Features Planned
- User registration, login, and token issuance (`/api/v1/auth/*`).
- Role assignment (`STUDENT`, `RECRUITER`, `OFFICER`, `ADMIN`).
- Password encryption using BCrypt with custom salting.
- Student profile data models (academic records, semester marks, backlogs, branch).
- Recruiter and company entity models.
- Global exception handling and standardized API response wrappers (`ApiResponse<T>`).

### Skills to Learn
- Java 21 modern language features (Records, Pattern Matching, Virtual Threads).
- Spring Boot 3.x setup and Maven dependency management.
- Spring Security 6 filter chains and custom `OncePerRequestFilter` for JWT validation.
- Spring Data JPA entity relationships (`@OneToOne`, `@OneToMany`, `@ManyToOne`), cascading, and lazy fetching.
- Database migrations with Flyway or Hibernate schema generation.

### Expected Deliverables
- Maven `pom.xml` configured with Spring Web, Security, Data JPA, MySQL driver, and Lombok.
- Clean layered backend package structure: `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `service`.
- Postman collection validating authentication and profile CRUD endpoints.

### Completion Criteria
- Successful user registration and login yielding valid JWT tokens.
- Secure endpoints reject unauthorized or role-mismatched requests with `401 Unauthorized` or `403 Forbidden`.
- Student profile and academic records can be saved and retrieved from MySQL 8.

---

## 📍 Phase 2: Frontend Foundation & Role Portals

### Objectives
- Initialize the React 18 frontend with TypeScript using Vite.
- Establish global state management, routing, and HTTP client interceptors.
- Build responsive, accessible layout shells for each of the four user roles.

### Features Planned
- Authentication views: Login, Registration, Password Reset, and Role Selector.
- Protected route guards (`ProtectedRoute`) redirecting unauthenticated or unauthorized users.
- Role-specific dashboard shells and navigation headers/sidebars:
  - Student Dashboard (Profile progress, Drive feed, Applications)
  - Recruiter Dashboard (Posted jobs, Candidate review)
  - Placement Officer Dashboard (Verifications, Drive approvals)
  - Admin Dashboard (User accounts, Department settings)
- Profile management forms with client-side validation.

### Skills to Learn
- Vite project configuration and TypeScript strict typing for React components.
- React Router v6 layout nesting, dynamic route params, and loader functions.
- Axios instance configuration with request/response interceptors for JWT token injection and 401 handling.
- Form handling and validation using React Hook Form and Zod or standard TypeScript validators.
- Modern CSS layout design (Tailwind CSS or modular CSS).

### Expected Deliverables
- Functional React SPA with TypeScript build setup.
- Working login/register UI communicating with Spring Boot auth endpoints.
- Role-based portal layouts that render conditionally based on user claims.

### Completion Criteria
- User can log in from the UI, store the JWT in memory/local storage, and access role-specific dashboard pages.
- Token refresh or logout properly clears state and redirects to login.

---

## 📍 Phase 3: Placement Workflows & Eligibility Engine

### Objectives
- Implement core placement business logic: drive creation, job listings, and multi-stage application processing.
- Develop the automated eligibility evaluation engine.
- Implement interview scheduling and candidate time-slot booking.

### Features Planned
- Placement Drive CRUD operations with comprehensive criteria configuration (CGPA, backlogs, branches).
- **Automated Eligibility Engine:** Rule evaluator that compares student records against drive rules in real time.
- Application submission workflow with eligibility pre-check enforcement.
- Application tracking state machine (`APPLIED` &rarr; `SHORTLISTED` &rarr; `INTERVIEW` &rarr; `OFFERED`).
- Recruiter applicant management: filtering, status transition buttons, and resume viewer.
- Interview slot scheduling and conflict-free allocation.

### Skills to Learn
- Complex JPA specifications, JPQL queries, and database indexing for fast candidate filtering.
- Business rule engine patterns and defensive programming in Spring services.
- Database transaction management (`@Transactional`) to guarantee consistency during status updates.
- State machine design patterns for multi-stage application tracking.

### Expected Deliverables
- Spring Boot controllers and services for Drives, Applications, and Interviews.
- Frontend interfaces for creating drives, browsing eligible drives, applying, and managing applicants.
- Conflict-detection algorithm preventing double-booking of interview slots.

### Completion Criteria
- Student can view whether they are eligible for a given drive with clear visual reasoning.
- Non-eligible students are prevented from applying.
- Recruiters can advance candidate application stages and schedule interview rounds.

---

## 📍 Phase 4: Analytics, Reporting & Communication

### Objectives
- Build real-time analytics aggregation services and visual reporting dashboards.
- Provide exportable placement reports in PDF and Excel formats.
- Implement an in-app notification center and email alert triggers.

### Features Planned
- Placement KPI metrics: overall placement %, branch-wise placement breakdown, average/median/highest CTC packages, recruiter hiring volumes.
- Interactive charting on TPO and Admin dashboards (bar charts, pie charts, salary distribution curves).
- PDF and Excel export engine for accreditation audit reports (NAAC/NBA compliant formats).
- In-app notification center displaying real-time updates on application status changes and new drive announcements.
- Email dispatch service using Spring Boot Starter Mail (for interview invitations and drive announcements).

### Skills to Learn
- SQL aggregation functions (`GROUP BY`, `COUNT`, `AVG`, window functions) optimized with JPA projections.
- Data visualization in React using Chart.js or Recharts.
- Document generation in Java using Apache POI (Excel) and iText or OpenPDF (PDF).
- Asynchronous task processing in Spring Boot (`@Async`, `@EnableAsync`) for unblocking email sends.

### Expected Deliverables
- Dedicated analytics REST endpoints delivering pre-aggregated metrics.
- High-resolution interactive charts on the TPO dashboard.
- Export buttons generating formatted `.xlsx` and `.pdf` files.
- Notification bell component with unread badges and notification history.

### Completion Criteria
- TPO can view instant placement statistics without noticeable database latency.
- Exported spreadsheets and PDFs match official academic reporting formats.
- Candidate receives immediate in-app notification upon status transition.

---

## 📍 Phase 5: Python AI Service & Career Intelligence

### Objectives
- Build and run the Python FastAPI AI microservice.
- Implement automated PDF resume text extraction and ATS scoring.
- Develop the AI Career Assistant for skill-gap identification and personalized learning roadmaps.

### Features Planned
- FastAPI service architecture with structured Pydantic data schemas.
- PDF text extraction and semantic section classification (Education, Projects, Work Experience, Skills).
- **AI Resume Analyzer:**
  - Evaluates resume formatting and completeness.
  - Matches resume keywords against target job descriptions.
  - Generates numerical ATS score (0–100) and actionable improvement suggestions.
- **AI Career Assistant & Skill-Gap Analyzer:**
  - Identifies missing skills required for desired roles (e.g., SDE-1, Data Analyst).
  - Produces structured 12-week learning roadmaps with recommended milestones.
- Secure internal REST communication between Spring Boot backend and Python AI service.

### Skills to Learn
- Python 3.11 asynchronous programming with FastAPI and Uvicorn.
- PDF processing with libraries such as PyPDF or pdfplumber.
- Prompt engineering for Large Language Models (LLMs) to ensure reliable JSON-formatted structured outputs.
- Integration with external LLM APIs (e.g., Google Gemini API or OpenAI API).
- Inter-service authentication using shared API tokens or HMAC signatures.

### Expected Deliverables
- Functional FastAPI service codebase with endpoints `/api/ai/resume/analyze` and `/api/ai/career/roadmap`.
- Pydantic models for request validation and response typing.
- Frontend Career Intelligence hub with resume upload, visual ATS score gauges, and interactive roadmap timelines.

### Completion Criteria
- Uploaded PDF resume receives structured ATS feedback and keyword match percentages within 5 seconds.
- Student receives a realistic, formatted career roadmap customized to their existing skills.
- Spring Boot backend successfully acts as the authenticated gateway to the AI service.

---

## 📍 Phase 6: Testing, Dockerization & Production Prep

### Objectives
- Implement comprehensive automated test suites across frontend and backend.
- Containerize all system components using Docker and Docker Compose.
- Perform security audits, environment configuration hardening, and final production readiness checks.

### Features Planned
- Backend unit and integration tests (JUnit 5, Mockito, SpringBootTest, Testcontainers).
- Frontend component and integration tests (React Testing Library, Vitest).
- Multi-container `docker-compose.yml` orchestrating MySQL 8, Spring Boot, Python FastAPI, and React frontend.
- Production-ready environment variable configurations and logging setups.
- API documentation generation using Springdoc OpenAPI / Swagger UI.

### Skills to Learn
- Mocking and integration testing patterns in Spring Boot with `@MockBean` and `@DataJpaTest`.
- Component testing, mocking Axios calls, and UI state verification in React.
- Docker multi-stage builds for Java (Maven build stage & slim JRE runtime stage) and React (Vite build & Nginx hosting).
- Docker Compose networking, volumes, and healthchecks.
- Security hardening: OWASP Top 10 prevention, rate limiting, and secure HTTP headers.

### Expected Deliverables
- Comprehensive unit and integration test coverage (>75% on core services).
- Working `Dockerfile` for each service and a unified `docker-compose.yml`.
- Live Swagger UI accessible at `/swagger-ui.html`.
- Deployment runbook with step-by-step instructions.

### Completion Criteria
- Entire platform builds and boots cleanly via `docker compose up --build`.
- All automated unit and integration tests pass cleanly without errors.
- System handles end-to-end user journeys across all four roles without functional defects.

---

## 📊 Summary of Phases & Key Deliverables

| Phase | Title | Primary Tech | Key Deliverable |
| :---: | :--- | :--- | :--- |
| **0** | **Setup & Planning** | Markdown, Git | Architecture blueprints, SRS, Roadmap, .gitignore |
| **1** | **Core Backend & DB** | Java 21, Spring Boot, MySQL 8 | Auth, JWT, RBAC, Student/Recruiter CRUD |
| **2** | **Frontend Foundation** | React 18, TypeScript, Vite | Role dashboards, Auth UI, Protected routes |
| **3** | **Placement Workflows** | Spring Boot, React, JPA | Eligibility engine, Drive lifecycle, Interviews |
| **4** | **Analytics & Reports** | Spring Boot, Chart.js, Apache POI | Placement KPI dashboards, Excel/PDF exports |
| **5** | **AI Career Intelligence** | Python, FastAPI, Gemini/LLM | Resume ATS analyzer, Skill-gap roadmap engine |
| **6** | **Testing & Docker** | JUnit, Vitest, Docker Compose | Automated tests, Multi-container containerization |
