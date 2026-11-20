# CampusBridge – Intelligent campus recruitment and career intelligence platform

[![CI](https://github.com/sanketamte96k/Campus-Bridge/actions/workflows/ci.yml/badge.svg)](https://github.com/sanketamte96k/Campus-Bridge/actions/workflows/ci.yml)

Continuous integration runs automated verification on every push and pull request to `main`.
It validates the Spring Boot test suite against MySQL 8, executes frontend linting, tests, and production builds, and verifies Docker image creation.

CampusBridge is a centralized, role-based campus recruitment management and career intelligence platform designed for higher education institutions. It connects students, corporate recruiters, placement officers (TPOs), and administrators, while integrating AI-driven career guidance and resume intelligence.

---

## Project status

Frontend MVP complete (React 19, TypeScript, Tailwind CSS, TanStack Query). It runs on mock services stored in the browser (localStorage), so the full placement workflow works without a backend. The Spring Boot API is the next phase.

- [x] Role-based auth (student, recruiter) with protected routes
- [x] Student profile and job browsing with search
- [x] Rule-based eligibility engine with per-rule pass or fail reasons (unit tested)
- [x] Apply and withdraw with duplicate and deadline rules (unit tested)
- [x] Recruiter job posting, applicant review, and a validated status workflow (unit tested)
- [ ] Spring Boot REST API with MySQL 8 and JWT
- [ ] AI skill-gap analyzer, resume analyzer, and career assistant
- [ ] Docker, CI/CD, and deployment

---

## Features

- **Students:** save an academic profile, browse jobs, see exactly why they are or aren't eligible for each job, apply, withdraw, and track status.
- **Recruiters:** post jobs with eligibility rules, review applicants with an eligibility summary, and move candidates through Shortlisted, Selected, or Rejected.
- **Eligibility engine:** a pure TypeScript function that checks branch, CGPA, active backlogs, and required skills, and returns a reason for each rule. It is rule-based, not AI.
- **Status workflow:** allowed transitions are defined in one tested function (for example, a rejected candidate can never return to shortlisted).

---

## Problem statement

Traditional campus placement operations are often plagued by:
- **Disorganized communication:** reliance on scattered spreadsheets, messaging groups, and notice boards leads to missed deadlines and clerical errors.
- **Manual eligibility screening:** placement officers spend hundreds of hours manually verifying CGPA, backlog criteria, and branch eligibility for each job drive.
- **Lack of application transparency:** students experience anxiety due to opaque shortlisting stages and a lack of real-time application tracking.
- **Unprepared candidates and skill gaps:** students apply to jobs without clear insight into how well their resumes match job descriptions, missing crucial domain skills.
- **Reporting inefficiencies:** placement cells struggle to compile accredited, real-time institutional placement reports for audits and accreditation bodies (e.g., NAAC, NBA).

---

## Objectives

1. **Automate drive and eligibility management:** provide an automated rule engine that computes student eligibility against drive criteria instantly.
2. **Deliver end-to-end transparency:** offer real-time tracking across every stage: application, shortlisting, technical tests, interviews, and final offers.
3. **Embed AI-powered career intelligence:** equip students with an AI Resume Analyzer for ATS feedback and an AI Career Assistant for targeted skill-gap roadmaps.
4. **Empower placement cells with actionable analytics:** generate live dashboards and exportable institutional reports on placement rates, CTC distributions, and recruiter trends.
5. **Ensure secure, role-based collaboration:** implement enterprise-grade authentication and authorization tailored specifically for academic environments.

---

## User roles

| Role | Key capabilities |
| :--- | :--- |
| **Student** | Builds profile, tracks academic verification, browses eligible placement drives, applies with 1-click, tracks status, schedules interview slots, analyzes resume with AI, and reviews skill-gap recommendations. |
| **Recruiter** | Registers company profile, posts job openings and drive details, sets eligibility criteria, reviews shortlisted applicants, manages interview rounds, and releases offers. |
| **Placement Officer (TPO)** | Approves recruiters and jobs, configures and manages campus placement drives, verifies student academic records, broadcasts updates, and monitors live drive progress. |
| **Administrator** | Manages system accounts, assigns roles, configures academic departments and programs, oversees audit logs, and exports institutional compliance reports. |

---

## How to run the frontend

Prerequisite: Node.js 20 or newer.

```bash
cd frontend/campusbridge-web
npm install
npm run dev
npm test
npm run build
```

The app opens on the URL Vite prints, usually http://localhost:5173.

### Try it
1. Register an account as a recruiter and post a job with eligibility rules.
2. Register an account as a student and complete the academic profile.
3. Browse the jobs list, inspect the eligibility reasons, and submit an application.
4. Log back in as the recruiter, view the applicants list, and shortlist the student.
5. Log back in as the student to see the updated application status.

---

## Run the backend locally

Prerequisites: Java 21 (LTS) and MySQL 8 running locally on port 3306 with the `campusbridge` database created.

### Required environment variables

The backend requires the following environment variables to be set before starting. There are no hardcoded secret defaults in configuration files:
- `DB_USERNAME`: Database username (e.g. `root`)
- `DB_PASSWORD`: Database password (required; application fails to start if omitted)
- `JWT_SECRET`: HMAC-SHA256 signing secret key (required; must be at least 32 characters long)

Optional environment variables:
- `DB_URL`: JDBC database URL (defaults to `jdbc:mysql://localhost:3306/campusbridge`)
- `PORT`: Server HTTP port (defaults to `8080`)
- `SPRING_PROFILES_ACTIVE`: Active profile (set to `dev` to enable demo seed accounts)
- `CORS_ALLOWED_ORIGINS`: Allowed CORS origins (defaults to `http://localhost:5173,http://localhost:5174`)

### Test database setup

To run automated backend tests in isolation, create the dedicated test schema and assign user privileges:

```sql
CREATE DATABASE campusbridge_test;
GRANT ALL PRIVILEGES ON campusbridge_test.* TO 'campusbridge'@'localhost';
FLUSH PRIVILEGES;
```

### PowerShell example

```powershell
# Set required environment variables using placeholders
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-database-password"
$env:JWT_SECRET = "your-secure-random-jwt-secret-at-least-32-chars-long"

# Run automated tests
cd backend
.\mvnw.cmd test

# Start the Spring Boot application
.\mvnw.cmd spring-boot:run
```

---

## Run with Docker

Run the entire platform (MySQL 8 database, Spring Boot API, and React frontend) with a single command.

### Prerequisites
- Docker and Docker Compose (Docker Desktop or Docker Engine v20.10+).

### Quick start
1. Copy the environment configuration template to `.env` and adjust passwords if needed:
   ```bash
   cp .env.example .env
   ```
2. Build and launch all container services:
   ```bash
   docker compose up --build
   ```
3. Open your browser at **http://localhost:3000**. Nginx serves the React SPA and proxies all `/api/` requests directly to the Spring Boot backend (`http://backend:8080`).

### Stopping and data management
- **Stop containers:**
  ```bash
  docker compose down
  ```
- **Wipe persistent database volume:**
  ```bash
  docker compose down -v
  ```

### Seeded demo logins (Local development only)
When running with the default `SPRING_PROFILES_ACTIVE=dev` profile, `DataInitializer` automatically seeds test accounts on first database initialization:
- **Recruiter:** `recruiter@campusbridge.edu` / `Password123!`
- **Student:** `student@campusbridge.edu` / `Password123!`

*(Note: These seeded credentials are for local demo and testing only; they are not created when running under production profiles.)*

### Troubleshooting

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| **Port in use** | Host port 3000 or 3307 is already bound | Free the port or adjust the host-side port binding in `docker-compose.yml`. |
| **Backend unhealthy** | Database is starting up or migration failed | Run `docker compose logs backend` to inspect logs; verify database health status with `docker compose ps`. |
| **DB password mismatch** | `.env` passwords changed after volume creation | Run `docker compose down -v` first to wipe the stale volume, then re-run `docker compose up --build`. |

---

## Frontend structure

```text
frontend/campusbridge-web/src/
├── components/          # Reusable UI elements (Button, Field, JobCard, StatusBadge)
├── features/            # Domain logic split by feature (auth, eligibility, applications, profile, recruiter)
├── layouts/             # Page layouts for auth and role-based dashboards
├── pages/               # Route views for auth, student, and recruiter flows
├── routes/              # Routing configuration, protected route guards, and navigation definitions
├── services/            # Mock APIs backed by localStorage that will become real API calls
└── types/               # TypeScript interfaces, unions, and shared data models
```

Each mock service keeps the same function signatures the real API calls will have, so swapping in the Spring Boot backend will not require page changes.

---

## Screenshots

<!-- Add screenshots of login, jobs with eligibility, and the recruiter applicants page here -->

---

## Technology stack

### Frontend
- **Framework and language:** React 19 with TypeScript
- **Build tool:** Vite
- **Routing:** React Router v7
- **Data fetching:** TanStack Query
- **Styling:** Tailwind CSS v4
- **Testing:** Vitest

### Backend (planned next phase)
- **Language and runtime:** Java 21 (LTS)
- **Framework:** Spring Boot 3.x
- **Security:** Spring Security and JWT (JSON Web Tokens)
- **Persistence and ORM:** Spring Data JPA and Hibernate
- **Build tool:** Apache Maven
- **Testing:** JUnit 5 and Mockito

### Database
- **Engine:** MySQL 8
- **Design:** Relational schema with ACID guarantees and foreign key integrity

### AI and career intelligence service
- **Runtime and framework:** Python 3.11+ with FastAPI
- **Data validation:** Pydantic
- **PDF processing:** PyPDF / pdfplumber
- **AI integration:** External LLM API (Google Gemini / OpenAI) for structured JSON evaluations

### Development tools
- **Version control:** Git and GitHub
- **API testing:** Postman
- **Containerization (planned):** Docker and Docker Compose

---

## Planned architecture

CampusBridge follows a clean, decoupled architecture designed to remain beginner-friendly while maintaining separation of concerns:

```text
               +-------------------------------------------+
               |        Client Layer (Web Browser)         |
               |        React + TypeScript + Vite          |
               +---------------------+---------------------+
                                     |
                                HTTPS / REST
                               (JWT Bearer)
                                     v
               +-------------------------------------------+
               |         Core Backend (Spring Boot 3)      |
               |   - Authentication & Role-Based Access    |
               |   - Student & Company Management          |
               |   - Drive Lifecycle & Eligibility Engine  |
               |   - Placement Analytics & Reporting       |
               +----------+---------------------+----------+
                          |                     |
                    JDBC / SQL             Internal REST
                          |                     |
                          v                     v
+---------------------------+   +------------------------------------+
|  Relational Database      |   |  AI Intelligence Service (FastAPI) |
|  MySQL 8                  |   |  - Resume PDF Parser & ATS Scorer  |
|  - Users & Roles          |   |  - Skill-Gap Analysis Engine       |
|  - Academic Records       |   +-----------------+------------------+
|  - Drives & Applications  |                     |
+---------------------------+             External API (HTTPS)
                                                  v
                                        +-------------------+
                                        | External LLM API  |
                                        | (Gemini / OpenAI) |
                                        +-------------------+
```

---

## Repository structure

```text
Campus-Bridge/
│
├── docs/
│   ├── SRS.md                   # Software Requirements Specification
│   ├── PROJECT_ROADMAP.md       # Phased implementation roadmap & deliverables
│   └── SYSTEM_ARCHITECTURE.md   # Architectural designs, components, and data flows
│
├── frontend/
│   └── campusbridge-web/        # React single-page application
│
├── README.md                    # Project overview and high-level documentation
├── .gitignore                   # Version control exclusion rules
└── .env.example                 # Environment variables template
```

---

## Next steps

1. **Spring Boot backend:** implement entities, MySQL 8 schema, JWT authentication, and rule-based eligibility evaluation in Java with JUnit test suites.
2. **Connect frontend to API:** replace localStorage mock services in `src/services/` with Axios HTTP clients calling Spring Boot endpoints.
3. **AI features:** implement the Python FastAPI microservice for ATS resume parsing and skill-gap career intelligence.
4. **Docker and deployment:** create Dockerfiles, Docker Compose multi-container configuration, and automated deployment pipelines.

---

## Future scope

- **AI mock interview simulator:** voice-to-text interactive mock technical and behavioral interviews with automated rubric feedback.
- **Alumni mentorship network:** connecting placed alumni with graduating students for resume reviews and interview preparation.
- **Video proctored assessment integration:** native coding and aptitude assessments with automated proctoring.
- **University federation support:** multi-campus tenant support enabling university systems to oversee multiple affiliated colleges.
- **Mobile companion application:** push notifications and QR-code check-ins for on-campus physical recruitment drives.

---

## License and attribution

This project is developed for academic and campus recruitment management purposes. All documentation and architectural blueprints are maintained under the project repository.
