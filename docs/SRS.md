# Software Requirements Specification (SRS)
## CampusBridge – Intelligent Campus Recruitment & Career Intelligence Platform

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) document provides a detailed description of the requirements for the development of **CampusBridge**. CampusBridge is an end-to-end, role-based campus recruitment and career intelligence management system. This document is intended for project stakeholders, system architects, software developers, quality assurance engineers, and academic administrative personnel.

### 1.2 Scope
The CampusBridge platform digitizes and automates institutional campus recruitment operations. The system manages:
- Student academic profiles, verification records, and resumes.
- Recruiter profiles, job postings, and hiring criteria.
- Placement drive organization, automated student eligibility screening, and multi-round application tracking.
- Interview scheduling and slot allocation.
- Quantitative placement analytics and institutional reporting.
- AI-driven resume ATS analysis, career guidance, and skill-gap recommendations.

### 1.3 Definitions, Acronyms, and Abbreviations
- **SRS:** Software Requirements Specification
- **TPO:** Training and Placement Officer
- **RBAC:** Role-Based Access Control
- **JWT:** JSON Web Token
- **ATS:** Applicant Tracking System
- **CGPA:** Cumulative Grade Point Average
- **CTC:** Cost to Company (Annual salary package)
- **LLM:** Large Language Model
- **REST:** Representational State Transfer
- **ORM:** Object-Relational Mapping (Spring Data JPA / Hibernate)

### 1.4 References
- IEEE Std 830-1998: Recommended Practice for Software Requirements Specifications.
- Spring Boot & Spring Security Documentation (v3.x).
- MySQL 8 Documentation.
- React 18 & TypeScript Documentation.
- FastAPI Specification & OpenAPI Standards.

---

## 2. Overall Description

### 2.1 Product Perspective
CampusBridge is a web-based, client-server platform. It replaces decentralized spreadsheets, manual record verification, and disconnected messaging channels with a unified web portal powered by a Spring Boot backend, a MySQL 8 relational database, and an auxiliary Python FastAPI AI microservice for intelligent career analysis.

```text
+--------------------------------------------------------------------------+
|                              CampusBridge                                |
|                                                                          |
|  [React Web Client] <---> [Spring Boot API] <---> [MySQL 8 Database]     |
|                                   |                                      |
|                                   +---------> [FastAPI AI Service]       |
|                                                      |                   |
|                                                      v                   |
|                                              [External LLM API]          |
+--------------------------------------------------------------------------+
```

### 2.2 Product Functions (High-Level Summary)
- User registration, authentication, and role authorization.
- Academic profile creation and verification by placement staff.
- Company registration, verification, and job drive publication.
- Rule-based automated eligibility checking for placement drives.
- Application submission and multi-stage status management.
- Interview schedule publishing and slot selection.
- Analytical dashboard with exportable placement reports (Excel/PDF).
- Resume text extraction, ATS score computation, and improvement advice.
- Skill-gap analysis comparing student competencies against job requirements.

### 2.3 User Classes and Characteristics
1. **Student:** Enrolled undergraduate or postgraduate candidates seeking campus placement or career intelligence. Moderate technical proficiency; expects intuitive, responsive interfaces.
2. **Recruiter:** Corporate talent acquisition representatives posting openings, reviewing applications, and conducting interviews.
3. **Placement Officer (TPO):** College administrative staff who manage placement drives, verify student eligibility data, and coordinate with recruiters.
4. **Administrator:** System superintendents responsible for user management, master data configuration (branches, departments, degree programs), and audit oversight.

### 2.4 Operating Environment
- **Client Side:** Modern web browsers (Google Chrome 110+, Mozilla Firefox 110+, Microsoft Edge 110+, Safari 16+).
- **Backend Server:** Java Runtime Environment (JRE/JDK 21) running on Linux or Windows Server.
- **AI Microservice:** Python 3.11+ runtime environment.
- **Database Server:** MySQL 8.0+.

### 2.5 Design and Implementation Constraints
- **Architecture Simplicity:** Clean, modular structure without over-engineered distributed microservice meshes. The backend handles primary business logic; the AI service functions as a specialized processing unit.
- **Language Stack:** Core backend strictly in Java 21 / Spring Boot; AI microservice in Python / FastAPI; frontend in React / TypeScript.
- **Stateless Authentication:** Stateless session management via JWT; tokens must be cryptographically signed.
- **Budget & API Rate Limits:** External LLM calls must be throttled and formatted with strict token economy.
- **Data Integrity:** Academic records (CGPA, backlogs) must maintain referential integrity and audit trails.

### 2.6 Assumptions and Dependencies
- The hosting institution provides authenticated academic data or allows official verification by TPOs.
- Resumes uploaded by students are in standard PDF format.
- A functional internet connection is available for client-server communication and LLM API access.
- Valid API credentials for the external LLM provider are maintained.

---

## 3. User Roles and Permissions Matrix

| Feature / Action | Student | Recruiter | Placement Officer (TPO) | Administrator |
| :--- | :---: | :---: | :---: | :---: |
| Self-Registration & Login | Yes | Yes (Approval req.) | Admin Created | Admin Created |
| View / Edit Own Profile | Yes | Yes | Yes | Yes |
| Verify Student Academic Details | No | No | **Yes** | **Yes** |
| Post Job / Placement Drive | No | Yes (Approval req.) | **Yes** | **Yes** |
| Approve Recruiter / Job Posting | No | No | **Yes** | **Yes** |
| Run Automated Eligibility Check | Yes (Self-check) | No | **Yes** | **Yes** |
| Apply for Placement Drive | **Yes** | No | No | No |
| View Applicants & Resumes | No | **Yes (Own jobs)** | **Yes (All)** | **Yes (All)** |
| Advance Application Stage | No | **Yes** | **Yes** | **Yes** |
| Schedule Interviews & Slots | No | **Yes** | **Yes** | **Yes** |
| View System Analytics | Limited (Self) | Limited (Own drives)| **Full Institutional** | **Full System** |
| Access AI Resume Analyzer | **Yes** | No | No | No |
| Access AI Career Roadmap | **Yes** | No | No | No |
| Manage Departments & Master Data | No | No | No | **Yes** |
| Audit Logs & User Management | No | No | No | **Yes** |

---

## 4. Functional Requirements

### 4.1 Authentication & Role-Based Access Control (FR-1)
- **FR-1.1:** The system shall allow users to register with email, password, and designated role (Student or Recruiter).
- **FR-1.2:** The system shall require email format validation and password complexity (minimum 8 characters, at least one uppercase letter, one digit, and one special character).
- **FR-1.3:** The system shall authenticate users via credentials and issue signed JWT access tokens containing user ID, email, and assigned roles.
- **FR-1.4:** The system shall enforce endpoint authorization using role-based guards on both backend controllers and frontend routes.
- **FR-1.5:** The system shall provide secure password reset mechanisms via tokenized email verification links.

### 4.2 Student Profile & Academic Record Management (FR-2)
- **FR-2.1:** Students shall create and update personal profiles, contact details, LinkedIn/GitHub URLs, and portfolio links.
- **FR-2.2:** Students shall record academic history: 10th percentage, 12th/Diploma percentage, current degree, branch, passing year, semester-wise SGPA, overall CGPA, and current active/dead backlogs.
- **FR-2.3:** The system shall mark student academic records as `PENDING_VERIFICATION` until verified by a Placement Officer.
- **FR-2.4:** Placement Officers shall be able to lock and certify academic records to prevent unauthorized modifications before placement drives.
- **FR-2.5:** Students shall upload resumes in PDF format (maximum size: 5 MB) stored securely with unique file identifiers.

### 4.3 Company & Recruiter Management (FR-3)
- **FR-3.1:** Recruiters shall register their company profile, including company name, website, industry domain, company description, and official contact details.
- **FR-3.2:** The system shall maintain recruiter account status as `PENDING_APPROVAL` until validated by a Placement Officer or Administrator.
- **FR-3.3:** Approved recruiters shall manage multiple placement drives or designate additional recruiter representatives for their company.

### 4.4 Job & Placement Drive Management (FR-4)
- **FR-4.1:** Recruiters and TPOs shall create placement drives specifying job title, job description, work location, employment type (Full-Time, Internship, FTE+Internship), CTC package, and drive timeline.
- **FR-4.2:** Placement drives shall define strict eligibility rules:
  - Minimum CGPA threshold
  - Minimum 10th and 12th percentage thresholds
  - Maximum allowable active backlogs (e.g., 0 backlogs)
  - Maximum allowable backlog history
  - Eligible branches and degree programs
  - Eligible graduating batch years
- **FR-4.3:** Drives created by recruiters shall require TPO approval before becoming visible to students.

### 4.5 Automated Eligibility Engine (FR-5)
- **FR-5.1:** The system shall automatically evaluate a student's verified academic records against a drive's eligibility criteria.
- **FR-5.2:** When a student views a placement drive, the system shall display an immediate eligibility status (`ELIGIBLE` or `INELIGIBLE`) along with transparent failure reasons (e.g., "Current CGPA 6.8 is below required 7.5").
- **FR-5.3:** The system shall block non-eligible students from submitting applications for restricted drives, with override permission granted exclusively to TPOs.

### 4.6 Application Lifecycle & Tracking (FR-6)
- **FR-6.1:** Eligible students shall apply to open placement drives with a single confirmation step.
- **FR-6.2:** Each application shall transition through formalized stages:
  - `APPLIED` &rarr; `SHORTLISTED` &rarr; `ASSESSMENT_PENDING` &rarr; `TECHNICAL_INTERVIEW` &rarr; `HR_INTERVIEW` &rarr; `OFFERED` &rarr; `ACCEPTED` / `REJECTED`.
- **FR-6.3:** Recruiters and TPOs shall update candidate stages individually or in bulk via batch selection.
- **FR-6.4:** Students shall access an Application Tracker dashboard displaying real-time status and historical updates for all applications.

### 4.7 Interview Scheduling & Management (FR-7)
- **FR-7.1:** Recruiters and TPOs shall define interview schedules including round type, date, time slots, panel details, and meeting links or campus venue locations.
- **FR-7.2:** The system shall notify shortlisted candidates of scheduled interviews via in-app alerts and email.
- **FR-7.3:** The system shall prevent scheduling conflicts where a candidate is assigned overlapping interview slots across multiple drives.

### 4.8 Placement Analytics & Reports (FR-8)
- **FR-8.1:** The system shall calculate real-time institutional metrics:
  - Overall placement percentage
  - Department-wise placement distribution
  - Highest, median, and average CTC packages
  - Gender-wise and category-wise placement ratios
  - Top recruiting companies by offer volume
- **FR-8.2:** Placement Officers and Admins shall export structured reports in CSV, Excel, and PDF formats for academic accreditation audits.

### 4.9 Notification & Communication System (FR-9)
- **FR-9.1:** The system shall issue notifications for major events: new drive published, eligibility verified, application status changed, interview scheduled, and offer extended.
- **FR-9.2:** In-app notification center shall display unread badges and allow marking alerts as read.

### 4.10 AI Resume Analyzer (FR-10)
- **FR-10.1:** The system shall allow students to submit their uploaded PDF resume for automated parsing.
- **FR-10.2:** The Python AI service shall extract text, sections (Education, Skills, Experience, Projects), and technical keywords from the resume.
- **FR-10.3:** The AI service shall compute an ATS compatibility score (0–100) based on formatting, keyword density, and section completeness.
- **FR-10.4:** When evaluated against a specific job description, the AI service shall identify matching keywords, missing keywords, and actionable suggestions to improve the resume.

### 4.11 AI Career Assistant & Skill-Gap Analysis (FR-11)
- **FR-11.1:** The AI service shall compare a student's parsed skill inventory against desired target career roles (e.g., Full Stack Developer, Data Engineer, Cloud Architect).
- **FR-11.2:** The AI service shall generate a personalized skill-gap summary detailing missing prerequisite and advanced competencies.
- **FR-11.3:** The AI service shall provide a milestone-based career learning roadmap (weeks 1–12) with recommended project concepts and study topics.

---

## 5. Non-Functional Requirements

### 5.1 Performance Requirements
- **NFR-1.1 (API Latency):** Standard transactional API endpoints (CRUD operations, eligibility checks) shall respond within 500 milliseconds under normal load.
- **NFR-1.2 (AI Service Latency):** AI resume analysis and skill-gap recommendations shall complete within 6 seconds.
- **NFR-1.3 (Throughput):** The system shall comfortably support up to 500 concurrent active users during peak placement drive registration windows.

### 5.2 Scalability
- **NFR-2.1:** The Spring Boot backend shall maintain a stateless architecture to enable horizontal scaling behind a standard load balancer.
- **NFR-2.2:** The Python AI service shall operate as an independent worker process that can be scaled independently of the main transactional database.

### 5.3 Reliability and Availability
- **NFR-3.1:** The system shall target 99.5% uptime during academic operating hours.
- **NFR-3.2:** Database transactions shall conform to ACID properties, preventing partial writes or corrupted application status states.

### 5.4 Security Requirements
- **NFR-4.1 (Password Protection):** User passwords shall be salted and hashed using BCrypt with an appropriate work factor (default: 10).
- **NFR-4.2 (Token Security):** JWTs shall be signed using HMAC-SHA256 (or RSA-256) with a minimum 256-bit secret key, and enforce strict expiration times.
- **NFR-4.3 (Data Privacy):** Confidential student data (phone numbers, addresses, grades) shall only be visible to authorized college staff and approved recruiters whose drives the student applied to.
- **NFR-4.4 (Input Sanitization):** All inputs must be sanitized on both frontend and backend to mitigate Cross-Site Scripting (XSS) and SQL Injection risks.
- **NFR-4.5 (Prompt Injection Mitigation):** User resumes and descriptions fed to the AI service must be sanitized to prevent prompt manipulation.

### 5.5 Usability & Accessibility
- **NFR-5.1:** User interfaces shall be fully responsive across desktop, tablet, and mobile viewport widths (320px to 2560px).
- **NFR-5.2:** Clear error messages and validation guidance must accompany all forms and actions.

---

## 6. Main Use Cases

### Use Case UC-1: Student Applies for Eligible Placement Drive
- **Primary Actor:** Student
- **Preconditions:** Student is logged in, has a verified academic profile, and a placement drive is currently open.
- **Trigger:** Student clicks "Apply Now" on a drive listing.
- **Main Success Scenario:**
  1. System checks student's verified CGPA, branch, batch, and active backlogs against drive criteria.
  2. Eligibility engine confirms all criteria are satisfied (`ELIGIBLE`).
  3. System prompts student to select their active resume.
  4. Student confirms application submission.
  5. System records application in `APPLIED` state with timestamp.
  6. System displays success confirmation and sends in-app notification.
- **Exceptions:**
  - *Criteria Mismatch:* System displays dialog explaining specific unmet criteria (e.g., minimum CGPA requirement not met); application button remains disabled.
  - *Profile Unverified:* System prompts student to contact the TPO for profile verification.

### Use Case UC-2: TPO Creates and Publishes Placement Drive
- **Primary Actor:** Placement Officer (TPO)
- **Preconditions:** TPO is authenticated with `ROLE_OFFICER` or `ROLE_ADMIN`.
- **Trigger:** TPO navigates to "Create Placement Drive".
- **Main Success Scenario:**
  1. TPO selects registered company and enters job role details (CTC, job description, location).
  2. TPO configures eligibility rules (min CGPA, allowed branches, max backlogs).
  3. TPO sets application deadlines and interview dates.
  4. TPO publishes the drive.
  5. System saves the drive and broadcasts notifications to all eligible students.
- **Exceptions:**
  - *Invalid Date Range:* Deadline in past; system flags validation error.

### Use Case UC-3: Recruiter Reviews Applicants and Updates Stages
- **Primary Actor:** Recruiter
- **Preconditions:** Recruiter is authenticated and has an approved job posting with active applications.
- **Trigger:** Recruiter opens the drive's applicant roster.
- **Main Success Scenario:**
  1. Recruiter filters applicants by branch or CGPA.
  2. Recruiter reviews candidate profiles and downloads resumes.
  3. Recruiter selects candidates and updates status to `SHORTLISTED`.
  4. System updates database records and dispatches notifications to students.
- **Exceptions:**
  - *Network Failure:* System displays rollback error message and preserves previous state.

### Use Case UC-4: Student Evaluates Resume with AI Resume Analyzer
- **Primary Actor:** Student
- **Preconditions:** Student is logged in and has uploaded a PDF resume.
- **Trigger:** Student clicks "Analyze Resume with AI" on the Career Intelligence tab.
- **Main Success Scenario:**
  1. Student optionally selects a target job description or enters a role title.
  2. Spring Boot backend forwards request and resume text to Python AI Service.
  3. AI service extracts text, parses sections, evaluates formatting, and prompts the LLM.
  4. AI service computes ATS score (0-100) and compiles structured improvement suggestions.
  5. Frontend renders interactive ATS breakdown card with missing keywords and recommendations.
- **Exceptions:**
  - *Corrupted PDF:* System notifies student to re-upload a clean, text-based PDF.
  - *LLM Timeout:* System displays friendly retry message without crashing the user session.

---

## 7. System Constraints

- **Single Institutional Scope:** Phase 1 focuses on a single college/university installation with a unified database schema.
- **File Storage:** Resumes are limited to PDF format up to 5 MB per file.
- **AI Dependency:** AI features require external LLM connectivity; fallback indicators must be shown if the API is unreachable.
- **Deployment Topology:** Lightweight, resource-efficient deployment allowing local development and staging on standard hardware.

---

## 8. Assumptions

- Academic administrators will accurately audit student records prior to major recruitment seasons.
- Recruiters will adhere to institutional placement guidelines and honor released offers.
- College networking infrastructure permits outbound HTTPS requests to external AI APIs.
