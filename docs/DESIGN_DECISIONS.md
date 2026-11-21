# Architecture and Engineering Decision Records

This document captures the key architectural, engineering, and security design decisions in CampusBridge for technical discussions and engineering reviews. Each record details the context, the decision taken, alternatives evaluated, and the concrete trade-offs accepted.

---

## One active application per job

**Context.** A student may apply to a job once, but can apply again after withdrawing. Two quick clicks must not create duplicates.

**Decision.** The `applications` table has a plain `UNIQUE (job_id, user_id)`. Re-applying after a withdrawal reactivates the existing row (status back to APPLIED, `applied_at` reset) instead of inserting a new one.

**Alternatives.** A partial unique index on rows where status is not WITHDRAWN (works in PostgreSQL, but MySQL has no partial indexes). A separate history table. A duplicate check in code only (this races under concurrent requests).

**Trade-off.** The database guarantees no duplicates even under concurrency. The cost is that the earlier withdrawn application's timestamp is overwritten, so past attempts aren't recorded. If that history matters later, add an `application_events` table.

---

## Application status as a single transition map

**Context.** Recruiters move applicants through a pipeline, and invalid moves (rejected back to shortlisted) must be impossible. Students may withdraw only submitted applications.

**Decision.** One map of allowed transitions per status lives in the service, with terminal states mapping to empty sets. Controllers never set a status directly. The frontend has a copy of the table only to decide which buttons to show.

**Alternatives.** if/else chains per endpoint. A database trigger. State-pattern classes.

**Trade-off.** One small map is easy to read and to test exhaustively (every pair of statuses). Adding a status means editing one place. There is no audit trail of who changed a status and when, which would need an event log.

---

## CGPA precision

**Context.** Academic cutoffs (e.g. minimum 7.50 CGPA) determine whether candidates qualify for placement drives. Binary floating-point representation errors can cause qualifying students to fail threshold comparisons.

**Decision.** Student `cgpa` and job `min_cgpa` columns use `DECIMAL(4,2)` in MySQL and `java.math.BigDecimal` in Java entities and DTOs, compared using `BigDecimal.compareTo()`.

**Alternatives.** Binary floating-point (`FLOAT`/`DOUBLE`), which suffers from precision drift (e.g. `6.99999999` failing a `7.00` cutoff check). Scaled integer arithmetic (e.g. storing `750` for `7.50`), which requires manual conversion at every architectural boundary.

**Trade-off.** Mathematical comparisons against academic cutoffs are exact and deterministic. The trade-off is slight verbosity in Java arithmetic and entity mappings compared to primitive numeric types.

---

## Pure-function eligibility

**Context.** Students require immediate, criterion-by-criterion eligibility feedback while browsing jobs, but application submissions require tamper-proof server-side enforcement.

**Decision.** `evaluateEligibility` is implemented as a pure function on both client (TypeScript) and server (Java). The client evaluation runs in-memory to provide instant UX feedback and disable the apply button. The server independently re-evaluates all criteria during submission; client-provided eligibility status is never accepted or trusted.

**Alternatives.** Calling a server endpoint for every job card on page load (creates network latency and UI lag). Trusting client-computed eligibility during submission (creates a severe security bypass vulnerability).

**Trade-off.** Keeping the logic synchronized across TypeScript and Java requires maintaining duplicate test suites. In exchange, students experience zero UI latency while preserving absolute server authorization authority.

---

## Learning plan safety

**Context.** Personalized learning plans assist students lacking skills for target jobs, but invoking external LLMs introduces prompt injection, hallucination, latency, and data leakage risks.

**Decision.** Treat AI learning plans as purely advisory suggestions that never affect application eligibility. Enforce zero PII transmission (no student names, emails, user IDs, or CGPA sent to the model). Sanitize input skills (maximum 30 characters, maximum 3 words, strict character whitelist), validate JSON output against strict schema bounds, apply sliding-window rate limiting (10 requests/hour per user), cache responses for 24 hours, and fall back to an offline deterministic template on any failure or missing API key.

**Alternatives.** Delegating candidate filtering or scoring to an LLM (introduces hallucination and compliance risks). Exposing unsanitized markdown responses directly to the client (risks layout breaks and injection).

**Trade-off.** AI advice is completely decoupled from recruitment gatekeeping and cannot leak student data or break the application when quotas are exhausted. The trade-off is that advice is restricted to missing skill keywords rather than a holistic resume evaluation.

---

## Forward-only Flyway migrations with Hibernate validation

**Context.** Database schema evolution must remain deterministic, reproducible, and synchronized across local workstations, Docker containers, and CI pipelines.

**Decision.** Manage all schema changes through forward-only Flyway migration scripts (`V1` through `V6`). Set `spring.jpa.hibernate.ddl-auto=validate` so Hibernate validates JPA entity mappings against the database schema at startup without executing DDL commands. Never edit applied migrations.

**Alternatives.** `hibernate.ddl-auto=update` (unpredictable in multi-instance environments, lacks change history, and risks silent index drops). Manual SQL execution (lacks versioning and leads to environment drift).

**Trade-off.** Schema consistency is guaranteed across all developers, containers, and test suites. The trade-off is that any schema correction requires authoring, testing, and committing a new migration file.

---

## Dedicated test schema with profile-layered environment loading

**Context.** Integration tests must run against real MySQL tables with Flyway migrations and Hibernate validation, but must never modify development database records.

**Decision.** Direct automated integration tests to an isolated `campusbridge_test` database via `@ActiveProfiles("test")` and `application-test.properties`. A custom `DotEnvEnvironmentPostProcessor` in the backend resolves credentials from the root `.env` while preserving OS environment variable precedence.

**Alternatives.** An in-memory H2 database (differs significantly from MySQL 8 regarding collations, table locking, constraints, and Flyway SQL compatibility). Sharing the development database (causes data corruption and test flakiness under concurrency).

**Trade-off.** Testing against the exact production database engine guarantees total SQL and schema fidelity. The trade-off is requiring a running MySQL 8 instance during test runs.

---

## Client-side JWT storage in localStorage versus httpOnly cookies

**Context.** Stateless authentication for a single-page React frontend communicating with a Spring Boot REST API.

**Decision.** Issue standard HMAC-SHA256 JWT tokens upon authentication, stored in browser `localStorage` and dispatched via the `Authorization: Bearer <token>` header on subsequent requests.

**Alternatives.** Server-set `httpOnly`, `Secure`, `SameSite=Strict` cookies (protects against script-based token theft). Stateful session cookies backed by Redis or Spring Session.

**Trade-off.** Provides zero cookie configuration friction across different development ports and straightforward external API interoperability. The trade-off is vulnerability to Cross-Site Scripting (XSS) if a script injection vulnerability exists. Documented as a known trade-off slated for cookie-based hardening in production.

---

## Same-origin reverse proxy in containerized environment

**Context.** In a containerized setup, the client browser must access both static React assets and backend REST API endpoints without CORS configuration issues.

**Decision.** Run an Nginx reverse proxy on port 3000 that serves Vite production build assets and forwards all `/api/` traffic internally to `http://backend:8080`.

**Alternatives.** Exposing the backend directly on port 8080 and the frontend on port 3000, requiring wide-open Cross-Origin Resource Sharing (CORS) configurations and browser preflight requests.

**Trade-off.** Eliminates CORS risks and preflight latency while mirroring production enterprise ingress architectures. The trade-off is an additional proxy layer in the Docker networking stack.

---

## Stable service abstraction with mock-to-API swap

**Context.** Frontend user interface development commenced before the backend REST API implementation was finalized.

**Decision.** Define stable TypeScript service interfaces in `src/services/` that initially operated against browser `localStorage`. When the Spring Boot REST API was implemented, service implementations were transitioned to Axios HTTP calls while preserving exported function signatures and hook contracts.

**Alternatives.** Blocking frontend development until backend endpoints were complete. Direct component mocking, which requires extensive refactoring across UI pages during backend integration.

**Trade-off.** Accelerated UI development and interaction testing. The trade-off was writing throwaway client-side persistence and mock state utilities that were later deprecated.
