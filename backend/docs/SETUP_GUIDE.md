# CampusBridge Backend Setup Guide

This guide walks you through setting up and running the CampusBridge Spring Boot backend locally.

---

## 1. System Requirements

- **Operating System:** Windows, macOS, or Linux
- **Java Development Kit (JDK):** JDK 21 or higher (JDK 21, 22, 23, 24, 25, 26)
- **Database:** MySQL Server 8.0+
- **Build System:** Apache Maven (Maven Wrapper `mvnw.cmd` / `./mvnw` is included)

---

## 2. MySQL Database Setup

Ensure your local MySQL service is running:

```powershell
# Open MySQL prompt
mysql -u root -p
```

Create the `campusbridge` database if it does not already exist:

```sql
CREATE DATABASE IF NOT EXISTS campusbridge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 3. Configuration & Environment Variables

Default configurations are specified in `backend/src/main/resources/application.properties`. You can override any configuration using standard environment variables:

| Environment Variable | Description | Default Value |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:mysql://localhost:3306/campusbridge` |
| `DB_USERNAME` | Database username | *(Required; no default)* |
| `DB_PASSWORD` | Database password | *(Required; no default)* |
| `JWT_SECRET` | HMAC-SHA256 256-bit secret | *(Required; minimum 32 chars)* |
| `JWT_EXPIRATION_MS` | Token lifespan in ms | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS`| Allowed frontend origins | `http://localhost:5173,http://localhost:5174` |
| `PORT` | HTTP Server port | `8080` |

---

## 4. Building & Running the Backend

From the `backend` directory in PowerShell:

### Step 1: Run Unit & Integration Tests
```powershell
.\mvnw.cmd test
```
All 33 tests will execute against your local database or mock context.

### Step 2: Start the Application
```powershell
.\mvnw.cmd spring-boot:run
```

Once started, the backend is listening on:
`http://localhost:8080`

---

## 5. Seed Accounts for Testing

When booted on an empty database, the backend automatically initializes demo accounts and 5 placement vacancies:

| Role | Email | Password |
|---|---|---|
| **Recruiter** | `recruiter@campusbridge.edu` | `Password123!` |
| **Student** | `student@campusbridge.edu` | `Password123!` |

The demo student already has a seeded profile (`CS` branch, `8.5` CGPA, `0` active backlogs, skills: `Java`, `Spring Boot`, `SQL`, `React`, `TypeScript`, `Tailwind`).

---

## 6. Frontend Integration

To connect the React frontend (`frontend/campusbridge-web`):
1. Ensure the backend is running at `http://localhost:8080`.
2. Configure frontend Axios/fetch base URL to `http://localhost:8080/api`.
3. Store the Bearer token received from `/api/auth/login` in the frontend session/localStorage and attach it to subsequent requests via `Authorization: Bearer <token>`.
