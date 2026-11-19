# CampusBridge – Intelligent Campus Recruitment & Career Intelligence Platform

CampusBridge is a centralized, role-based campus recruitment management and career intelligence platform designed for higher education institutions. It streamlines the end-to-end placement lifecycle—connecting students, corporate recruiters, placement officers (TPOs), and administrators—while integrating AI-driven career guidance and resume intelligence.

---

## 📌 Project Status

> **Current Stage:** `Phase 0: Planning & Setup`  
> **Status:** Architecture designed, software requirement specifications drafted, and repository initialized. Source code development has not yet begun.

---

## 🎯 Problem Statement

Traditional campus placement operations are often plagued by:
- **Disorganized Communication:** Reliance on scattered spreadsheets, messaging groups, and notice boards leads to missed deadlines and clerical errors.
- **Manual Eligibility Screening:** Placement officers spend hundreds of hours manually verifying CGPA, backlog criteria, and branch eligibility for each job drive.
- **Lack of Application Transparency:** Students experience anxiety due to opaque shortlisting stages and a lack of real-time application tracking.
- **Unprepared Candidates & Skill Gaps:** Students apply to jobs without clear insight into how well their resumes match job descriptions, missing crucial domain skills.
- **Reporting Inefficiencies:** Placement cells struggle to compile accredited, real-time institutional placement reports for audits and accreditation bodies (e.g., NAAC, NBA).

---

## 🚀 Objectives

1. **Automate Drive & Eligibility Management:** Provide an automated rule engine that computes student eligibility against drive criteria instantly.
2. **Deliver End-to-End Transparency:** Offer real-time tracking across every stage: application, shortlisting, technical tests, interviews, and final offers.
3. **Embed AI-Powered Career Intelligence:** Equip students with an AI Resume Analyzer for ATS feedback and an AI Career Assistant for targeted skill-gap roadmaps.
4. **Empower Placement Cells with Actionable Analytics:** Generate live dashboards and exportable institutional reports on placement rates, CTC distributions, and recruiter trends.
5. **Ensure Secure, Role-Based Collaboration:** Implement enterprise-grade authentication and authorization tailored specifically for academic environments.

---

## 👥 User Roles & Capabilities

| Role | Key Capabilities |
| :--- | :--- |
| **Student** | Builds profile, tracks academic verification, browses eligible placement drives, applies with 1-click, tracks status, schedules interview slots, analyzes resume with AI, and reviews skill-gap recommendations. |
| **Recruiter** | Registers company profile, posts job openings and drive details, sets eligibility criteria, reviews shortlisted applicants, manages interview rounds, and releases offers. |
| **Placement Officer (TPO)** | Approves recruiters and jobs, configures and manages campus placement drives, verifies student academic records, broadcasts updates, and monitors live drive progress. |
| **Administrator** | Manages system accounts, assigns roles, configures academic departments and programs, oversees audit logs, and exports institutional compliance reports. |

---

## ✨ Main Features

- **Role-Based Access Control (RBAC):** JWT-backed authentication ensuring role isolation for Students, Recruiters, Placement Officers, and Administrators.
- **Academic Record & Profile Verification:** Comprehensive student profiles with academic histories (10th, 12th, semester-wise SGPA/CGPA, active backlogs).
- **Automated Eligibility Engine:** Instant validation against drive criteria (minimum CGPA, allowed branches, backlog thresholds, gap-year limits).
- **Placement Drive & Application Lifecycle:** Multi-stage pipeline tracking (Applied &rarr; Shortlisted &rarr; Assessment &rarr; Technical Interview &rarr; HR Interview &rarr; Offered &rarr; Accepted).
- **Interview Slot & Schedule Management:** Coordination of interview rounds, time slots, and meeting venues/links.
- **Placement Analytics & Institutional Reports:** Department-wise hiring statistics, median/average/highest CTC analytics, and exportable reports (PDF/Excel).
- **AI Resume Analyzer:** PDF resume parsing, ATS scoring against job descriptions, and contextual improvement recommendations.
- **AI Career Assistant & Skill-Gap Analysis:** Identifies missing technical competencies against target job profiles and generates structured learning roadmaps.
- **Notification Center:** In-app and email announcements for drive openings, interview calls, and results.

---

## 🛠️ Technology Stack

### Frontend
- **Framework & Language:** React 18 with TypeScript
- **Build Tool:** Vite
- **Routing:** React Router v6
- **HTTP Client:** Axios
- **Styling:** Tailwind CSS (planned)
- **Testing:** React Testing Library & Jest / Vitest

### Backend
- **Language & Runtime:** Java 21 (LTS)
- **Framework:** Spring Boot 3.x
- **Security:** Spring Security & JWT (JSON Web Tokens)
- **Persistence & ORM:** Spring Data JPA & Hibernate
- **Build Tool:** Apache Maven
- **Testing:** JUnit 5 & Mockito

### Database
- **Engine:** PostgreSQL 16
- **Design:** Relational schema with ACID guarantees and foreign key integrity

### AI & Career Intelligence Service
- **Runtime & Framework:** Python 3.11+ with FastAPI
- **Data Validation:** Pydantic
- **PDF Processing:** PyPDF / pdfplumber
- **AI Integration:** External LLM API (Google Gemini / OpenAI) for structured JSON evaluations

### Development Tools
- **Version Control:** Git & GitHub
- **API Testing:** Postman
- **Containerization (Planned):** Docker & Docker Compose

---

## 🏛️ Planned Architecture

CampusBridge follows a clean, decoupled architecture designed to remain beginner-friendly while maintaining professional separation of concerns:

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
|  PostgreSQL 16            |   |  - Resume PDF Parser & ATS Scorer  |
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

## 📁 Repository Structure

```text
Campus-Bridge/
│
├── docs/
│   ├── SRS.md                   # Software Requirements Specification
│   ├── PROJECT_ROADMAP.md       # Phased implementation roadmap & deliverables
│   └── SYSTEM_ARCHITECTURE.md   # Architectural designs, components, and data flows
│
├── README.md                    # Project overview and high-level documentation
├── .gitignore                   # Version control exclusion rules
└── .env.example                 # Environment variables template
```

---

## 🔮 Future Scope

- **AI Mock Interview Simulator:** Voice-to-text interactive mock technical and behavioral interviews with automated rubric feedback.
- **Alumni Mentorship Network:** Connecting placed alumni with graduating students for resume reviews and interview preparation.
- **Video Proctored Assessment Integration:** Native coding and aptitude assessments with automated proctoring.
- **University Federation Support:** Multi-campus tenant support enabling university systems to oversee multiple affiliated colleges.
- **Mobile Companion Application:** Push notifications and QR-code check-ins for on-campus physical recruitment drives.

---

## 📄 License & Attribution

This project is developed for academic and campus recruitment management purposes. All documentation and architectural blueprints are maintained under the project repository.
