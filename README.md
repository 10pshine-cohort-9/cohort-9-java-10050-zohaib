# Contact Management System

A full-stack web application for managing personal and professional contacts. Users register, log in, and perform complete CRUD operations — including search, pagination, export to CSV, and import from CSV — all secured with JWT authentication.

---

## Table of Contents

- [Technology Stack](#technology-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Quick Start — Docker (Recommended)](#quick-start--docker-recommended)
- [Manual Local Development Setup](#manual-local-development-setup)
- [Environment Variables](#environment-variables)
- [REST API Reference](#rest-api-reference)
- [Database Schema](#database-schema)
- [Frontend Pages](#frontend-pages)
- [Architecture](#architecture)
- [Testing](#testing)
- [Code Quality — SonarQube](#code-quality--sonarqube)
- [Security](#security)
- [Logging](#logging)

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Backend framework | Spring Boot | 3.3.4 |
| Language | Java | 21 |
| ORM | Spring Data JPA + Hibernate | (managed by Boot) |
| Security | Spring Security + JWT (JJWT) | 6.x / 0.11.5 |
| Validation | Jakarta Bean Validation | (managed) |
| Database | Microsoft SQL Server | 2022 |
| JDBC Driver | mssql-jdbc | (managed) |
| Build tool | Apache Maven | 3.9+ |
| Frontend | React | 18.2 |
| Routing | React Router | 6.x |
| HTTP client | Axios | 1.6 |
| Forms | React Hook Form | 7.x |
| Notifications | React Toastify | 9.x |
| Frontend build | Create React App (react-scripts) | 5.0.1 |
| Production server | Nginx | 1.25 |
| Containerisation | Docker + Docker Compose | v2 |
| Logging | SLF4J + Logback | (managed) |
| Code coverage | JaCoCo | 0.8.12 |
| Code quality | SonarQube | 10 Community |
| Testing (backend) | JUnit 5 + Mockito + Spring Test | (managed) |
| Testing (frontend) | React Testing Library | 13.x |
| Utilities | Lombok | 1.18.40 |

---

## Features

### Authentication & Authorization
- Register with **email** or **phone number** (at least one required)
- Log in using email or phone + password
- JWT-based stateless authentication (24-hour expiry)
- Change password with current-password verification
- Password requirements: minimum 4 characters, 1 uppercase letter, 1 special character
- Password visibility toggle on all password inputs

### Contact Management
- Create, view, edit, and delete contacts via modal dialogs
- Contact fields: first name, last name, title, address, notes
- Multiple phone numbers per contact (mobile, home, work, other)
- Multiple email addresses per contact (personal, work, other)
- Soft-delete — deleted contacts are hidden, not permanently removed

### Search & Pagination
- Real-time debounced search across first name, last name, company, phone, and email
- Paginated contact list with arrow navigation and numbered page buttons

### Export / Import
- **Export**: Download all contacts as a UTF-8 CSV file (Excel-compatible)
- **Import**: Upload a CSV file to bulk-create contacts; skipped rows are reported

### User Profile
- View account information (name, email, phone, role)
- Change password via modal (Modal 4)
- Logout clears session

### Home Page
- Public landing page with feature overview and Sign In / Register CTAs
- Automatically redirects authenticated users to the contacts page

---

## Project Structure

```
cohort-9-java-10050-zohaib/
│
├── backend/                              # Spring Boot API
│   ├── Dockerfile                        # Multi-stage: JDK build → JRE runtime
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/contactmanagement/
│       │   │   ├── ContactManagementApplication.java
│       │   │   ├── config/
│       │   │   │   └── SecurityConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   └── ContactController.java
│       │   │   ├── dto/
│       │   │   │   ├── ApiResponse.java
│       │   │   │   ├── AuthResponse.java
│       │   │   │   ├── ChangePasswordRequest.java
│       │   │   │   ├── ContactEmailDto.java
│       │   │   │   ├── ContactPhoneDto.java
│       │   │   │   ├── ContactResponse.java
│       │   │   │   ├── ContactSummaryResponse.java
│       │   │   │   ├── CreateContactRequest.java
│       │   │   │   ├── LoginRequest.java
│       │   │   │   ├── PagedResponse.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   └── UpdateContactRequest.java
│       │   │   ├── exception/
│       │   │   │   ├── BadRequestException.java
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── InvalidCredentialsException.java
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   └── UserAlreadyExistsException.java
│       │   │   ├── model/
│       │   │   │   ├── Contact.java
│       │   │   │   ├── ContactEmail.java
│       │   │   │   ├── ContactPhone.java
│       │   │   │   └── User.java
│       │   │   ├── repository/
│       │   │   │   ├── ContactEmailRepository.java
│       │   │   │   ├── ContactPhoneRepository.java
│       │   │   │   ├── ContactRepository.java
│       │   │   │   └── UserRepository.java
│       │   │   ├── security/
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtAuthEntryPoint.java
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   └── UserDetailsServiceImpl.java
│       │   │   └── service/
│       │   │       ├── AuthService.java
│       │   │       ├── ContactImportExportService.java
│       │   │       ├── ContactService.java
│       │   │       └── impl/
│       │   │           ├── AuthServiceImpl.java
│       │   │           └── ContactServiceImpl.java
│       │   └── resources/
│       │       ├── application.properties         # Default (localhost)
│       │       ├── application-dev.properties     # Dev overrides (ddl-auto=update)
│       │       ├── application-docker.properties  # Docker overrides (sqlserver hostname)
│       │       └── logback-spring.xml             # Rolling file + console logging
│       └── test/
│           └── java/com/contactmanagement/
│               ├── ContactManagementApplicationTests.java
│               ├── controller/AuthControllerTest.java
│               └── service/AuthServiceImplTest.java
│
├── frontend/                             # React SPA
│   ├── Dockerfile                        # Multi-stage: Node build → Nginx serve
│   ├── nginx.conf                        # Nginx: SPA routing + /api proxy
│   ├── package.json
│   └── src/
│       ├── App.js                        # Router + layout
│       ├── index.js
│       ├── index.css
│       ├── components/
│       │   ├── common/
│       │   │   ├── LoadingSpinner.js
│       │   │   ├── Navbar.js
│       │   │   └── PrivateRoute.js
│       │   └── contacts/
│       │       ├── ContactCard.js
│       │       └── ContactSearchBar.js
│       ├── context/AuthContext.js
│       ├── hooks/
│       │   ├── useAuth.js
│       │   └── useContacts.js
│       ├── pages/
│       │   ├── HomePage.js               # Public landing page
│       │   ├── LoginPage.js
│       │   ├── RegisterPage.js
│       │   ├── ChangePasswordPage.js
│       │   ├── ProfilePage.js
│       │   ├── ContactListPage.js        # Contacts with inline modals
│       │   ├── ContactFormPage.js        # Create / edit contact
│       │   └── ContactDetailPage.js      # View contact details
│       ├── services/
│       │   ├── api.js                    # Axios instance + JWT interceptors
│       │   ├── authService.js
│       │   └── contactService.js
│       └── utils/
│           ├── constants.js
│           └── validators.js
│
├── database/
│   ├── init/
│   │   ├── 01_init.sql                   # All migrations in one idempotent script
│   │   └── run-init.sh                   # Shell script used by Docker db-init service
│   ├── migrations/
│   │   ├── V1__create_users_table.sql
│   │   ├── V2__create_contacts_table.sql
│   │   ├── V3__add_audit_trigger.sql
│   │   ├── V4__add_phone_to_users.sql
│   │   └── V5__add_contact_title_phones_emails.sql
│   ├── scripts/
│   │   ├── create_app_user.sql
│   │   ├── create_database.sql
│   │   └── drop_database.sql
│   └── seeds/
│       └── seed_dev_data.sql
│
├── docker-compose.yml                    # Full stack: SQL Server + backend + frontend
├── sonar-project.properties              # SonarQube multi-module config
├── sonar-quality-profile.xml             # Importable Java + JS quality profiles
├── .env                                  # Your local secrets (never commit)
├── .env.example                          # Template — copy to .env
└── .gitignore
```

---

## Quick Start — Docker (Recommended)

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- At least **4 GB RAM** allocated to Docker (8 GB recommended)

### 1. Clone the repository

```bash
git clone <repository-url>
cd cohort-9-java-10050-zohaib
```

### 2. Create your `.env` file

```bash
cp .env.example .env
```

Edit `.env` and set your passwords (or leave the defaults for local dev):

```env
SA_PASSWORD=YourStrongSAPassword1!
DB_USERNAME=contact_app_user
DB_PASSWORD=AppUser@2024!
JWT_SECRET=bXktc3VwZXItc2VjcmV0LWtleS0zMi1ieXRlcy1sb25nISE
```

### 3. Build and start

```bash
docker compose up --build
```

First build takes 5–10 minutes (Maven downloads dependencies, Node installs packages). Subsequent starts are fast:

```bash
docker compose up
```

### 4. Open the application

| Service | URL |
|---|---|
| **Frontend** | http://localhost:3000 |
| **Backend API** | http://localhost:8080 |
| **SQL Server** | localhost:1433 |

### 5. Stop

```bash
docker compose down
```

To also remove all stored data (full reset):

```bash
docker compose down -v
```

### Docker Compose services

| Service | Container | Purpose |
|---|---|---|
| `sqlserver` | `contact_mgmt_sqlserver` | Microsoft SQL Server 2022 |
| `db-init` | `contact_mgmt_db_init` | Runs database migrations once, then exits |
| `backend` | `contact_mgmt_backend` | Spring Boot API on port 8080 |
| `frontend` | `contact_mgmt_frontend` | React app served by Nginx on port 3000 |
| `sonarqube` *(optional)* | `contact_mgmt_sonarqube` | Code quality — started with `--profile sonar` |

**Startup order:**
```
sqlserver (healthy) → db-init (completes) → backend (started) → frontend
```

---

## Manual Local Development Setup

### Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 21 |
| Apache Maven | 3.9+ |
| Node.js | 18+ |
| Docker Desktop | Latest (for SQL Server) |

### Step 1 — Start SQL Server

```powershell
cd "path\to\cohort-9-java-10050-zohaib"
docker compose up -d sqlserver
```

Wait ~30 seconds for the health check to pass.

### Step 2 — Run database migrations

After SQL Server is healthy, run migrations manually (or let `db-init` run via compose):

```powershell
docker compose up db-init
```

### Step 3 — Start the backend

Open a PowerShell terminal:

```powershell
cd backend

$env:DB_USERNAME = "contact_app_user"
$env:DB_PASSWORD = "AppUser@2024!"
$env:JWT_SECRET  = "bXktc3VwZXItc2VjcmV0LWtleS0zMi1ieXRlcy1sb25nISE"

mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-DskipJacoco=true"
```

Wait until you see:
```
Started ContactManagementApplication in X.XXX seconds
```

### Step 4 — Start the frontend

Open a second terminal:

```powershell
cd frontend
npm install --legacy-peer-deps
$env:SKIP_PREFLIGHT_CHECK="true"
npm start
```

Browser opens at **http://localhost:3000**.

The `"proxy": "http://localhost:8080"` in `package.json` forwards all `/api` calls to the backend automatically in development mode.

---

## Environment Variables

All variables are read from the `.env` file in the project root by Docker Compose. For local development, set them in your terminal session.

| Variable | Required | Description | Example |
|---|---|---|---|
| `SA_PASSWORD` | Yes | SQL Server system administrator password. Must satisfy complexity (upper, lower, digit, symbol). | `Instagram1$2` |
| `DB_USERNAME` | Yes | Application database login (created by `01_init.sql`). | `contact_app_user` |
| `DB_PASSWORD` | Yes | Application database password. | `AppUser@2024!` |
| `JWT_SECRET` | Yes | Base64-encoded secret key for signing JWTs. Minimum 32 bytes. | `bXktc3VwZXItc2VjcmV0...` |
| `SONAR_TOKEN` | Optional | SonarQube analysis token. Only needed with `--profile sonar`. | `squ_xxxxx` |
| `SONAR_DB_PASSWORD` | Optional | PostgreSQL password for SonarQube's internal DB. | `sonar` |

**Generate a JWT secret:**

```powershell
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("your-32-character-secret-here!!"))
```

---

## REST API Reference

All endpoints are prefixed with `/api`. Authenticated endpoints require the header:
```
Authorization: Bearer <token>
```

### Authentication — `/api/auth`

| Method | Path | Auth | Request Body | Response | Description |
|---|---|---|---|---|---|
| `POST` | `/api/auth/register` | No | `RegisterRequest` | `AuthResponse` 201 | Register new user |
| `POST` | `/api/auth/login` | No | `LoginRequest` | `AuthResponse` 200 | Login, receive JWT |
| `POST` | `/api/auth/change-password` | Yes | `ChangePasswordRequest` | `ApiResponse` 200 | Change password |

**RegisterRequest:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+923001234567",
  "password": "Pass@1"
}
```
At least one of `email` or `phone` must be provided.

**LoginRequest:**
```json
{
  "identifier": "john@example.com",
  "password": "Pass@1"
}
```
`identifier` accepts either email or phone number.

**AuthResponse:**
```json
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "userId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": null,
  "role": "ROLE_USER"
}
```

**ChangePasswordRequest:**
```json
{
  "currentPassword": "OldPass@1",
  "newPassword": "NewPass@1",
  "confirmNewPassword": "NewPass@1"
}
```
Password rules: minimum 4 characters, at least 1 uppercase letter, at least 1 special character.

---

### Contacts — `/api/contacts`

All contact endpoints require a valid JWT.

| Method | Path | Query Params | Description |
|---|---|---|---|
| `GET` | `/api/contacts` | `page`, `size`, `sortBy`, `sortDir`, `search` | Paginated contact list |
| `GET` | `/api/contacts/{id}` | — | Get contact by ID |
| `POST` | `/api/contacts` | — | Create contact |
| `PUT` | `/api/contacts/{id}` | — | Update contact |
| `DELETE` | `/api/contacts/{id}` | — | Soft-delete contact |
| `GET` | `/api/contacts/export` | — | Download all contacts as CSV |
| `POST` | `/api/contacts/import` | — | Import contacts from CSV (`multipart/form-data`, field: `file`) |

**Paginated list response (`PagedResponse`):**
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "Alice",
      "lastName": "Johnson",
      "title": "Engineer",
      "company": "Acme",
      "primaryPhone": "+1234567890",
      "primaryEmail": "alice@example.com"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

**Search:** `GET /api/contacts?search=alice` searches across first name, last name, company, phone number, and email address.

**CreateContactRequest / UpdateContactRequest:**
```json
{
  "firstName": "Alice",
  "lastName": "Johnson",
  "title": "Engineer",
  "address": "123 Main St",
  "notes": "Met at conference",
  "phones": [
    { "label": "mobile", "number": "+1234567890" },
    { "label": "work",   "number": "+0987654321" }
  ],
  "emails": [
    { "label": "work",     "address": "alice@acme.com" },
    { "label": "personal", "address": "alice@gmail.com" }
  ]
}
```
At least one phone number **or** email address is required.

**CSV Export format:**
```csv
firstName,lastName,title,company,address,notes,phone1Label,phone1Number,phone2Label,phone2Number,email1Label,email1Address,email2Label,email2Address
Alice,Johnson,Engineer,Acme,123 Main St,,mobile,+1234567890,,,work,alice@acme.com,,
```

---

## Database Schema

The database is `contact_management_db` on SQL Server 2022. Migrations are applied by the `db-init` Docker service on first start.

```
users
  id           BIGINT IDENTITY PK
  first_name   NVARCHAR(100) NOT NULL
  last_name    NVARCHAR(100) NOT NULL
  email        NVARCHAR(255) UNIQUE
  phone        NVARCHAR(50)  UNIQUE (partial index)
  password     NVARCHAR(255) NOT NULL
  role         NVARCHAR(50)  DEFAULT 'ROLE_USER'
  is_active    BIT           DEFAULT 1
  created_at   DATETIME2     DEFAULT SYSDATETIME()
  updated_at   DATETIME2     DEFAULT SYSDATETIME()  ← managed by trigger

contacts
  id           BIGINT IDENTITY PK
  user_id      BIGINT FK → users(id) ON DELETE CASCADE
  first_name   NVARCHAR(100) NOT NULL
  last_name    NVARCHAR(100) NOT NULL
  title        NVARCHAR(100)
  company      NVARCHAR(255)
  address      NVARCHAR(500)
  notes        NVARCHAR(MAX)
  is_deleted   BIT           DEFAULT 0  ← soft delete
  created_at   DATETIME2     DEFAULT SYSDATETIME()
  updated_at   DATETIME2     DEFAULT SYSDATETIME()  ← managed by trigger

contact_phones
  id           BIGINT IDENTITY PK
  contact_id   BIGINT FK → contacts(id) ON DELETE CASCADE
  label        NVARCHAR(50)  DEFAULT 'mobile'
  number       NVARCHAR(50)  NOT NULL

contact_emails
  id           BIGINT IDENTITY PK
  contact_id   BIGINT FK → contacts(id) ON DELETE CASCADE
  label        NVARCHAR(50)  DEFAULT 'personal'
  address      NVARCHAR(255) NOT NULL
```

Database triggers (`trg_users_updated_at`, `trg_contacts_updated_at`) automatically update the `updated_at` column on every row update.

---

## Frontend Pages

| Route | Page | Auth | Description |
|---|---|---|---|
| `/` | `HomePage` | No | Public landing page with feature overview and Sign In / Register buttons |
| `/login` | `LoginPage` | No | Login form — accepts email or phone number |
| `/register` | `RegisterPage` | No | Registration form — email or phone required |
| `/contacts` | `ContactListPage` | Yes | Paginated contact table with inline Create, Edit, Delete modals and search |
| `/contacts/:id` | `ContactDetailPage` | Yes | Full contact profile with phones, emails, address, notes |
| `/contacts/:id/edit` | `ContactFormPage` | Yes | Edit contact form (standalone page) |
| `/contacts/new` | `ContactFormPage` | Yes | Create contact form (standalone page) |
| `/profile` | `ProfilePage` | Yes | User details, Change Password modal, Logout button |
| `/change-password` | `ChangePasswordPage` | Yes | Standalone change password form |

### Contact List Modals

The contacts page (`/contacts`) handles all operations inline without navigation:

- **Modal 1 — Create Contact**: opens on `+ New Contact` click
- **Modal 2 — Edit Contact**: opens on `Edit` button click; pre-populated with existing data
- **Modal 3 — Delete Confirmation**: opens on `Delete` button click; requires explicit confirmation
- **Modal 4 — Change Password**: available on Profile page

---

## Architecture

```
Browser
  │
  │  http://localhost:3000
  ▼
Nginx (Docker)
  │  Serves React SPA (static files)
  │  Proxies /api/* → backend:8080
  │
  ▼
Spring Boot (Docker, port 8080)
  │  JWT authentication filter
  │  REST controllers
  │  Spring Data JPA
  │
  ▼
SQL Server 2022 (Docker, port 1433)
  │  contact_management_db
  │  users, contacts, contact_phones, contact_emails
```

**Container networking:**
All containers run in the same Docker Compose network. The frontend Nginx container resolves the backend by its service name `backend`. The backend resolves the database by `sqlserver`. No hard-coded IP addresses.

**JWT flow:**
1. Client sends credentials to `POST /api/auth/login`
2. Backend returns a signed JWT (HS256, 24-hour expiry)
3. Client stores JWT in `localStorage`
4. Every subsequent API request includes `Authorization: Bearer <token>`
5. `JwtAuthenticationFilter` validates the token on each request
6. On 401 (expired/invalid token), the frontend clears localStorage and redirects to the home page

---

## Testing

### Backend

```powershell
cd backend
$env:DB_USERNAME = "contact_app_user"
$env:DB_PASSWORD = "AppUser@2024!"
$env:JWT_SECRET  = "bXktc3VwZXItc2VjcmV0LWtleS0zMi1ieXRlcy1sb25nISE"
mvn test
```

Test files:
- `ContactManagementApplicationTests` — Spring context smoke test
- `AuthControllerTest` — `@WebMvcTest` controller layer (MockMvc)
- `AuthServiceImplTest` — Mockito unit tests for service layer

### Frontend

```powershell
cd frontend
npm test
```

---

## Code Quality — SonarQube

SonarQube is available as an optional Docker profile:

```bash
docker compose --profile sonar up -d
```

SonarQube UI: **http://localhost:9000** (default credentials: `admin` / `admin`)

**Run analysis after tests:**

```powershell
cd backend
$env:SONAR_TOKEN = "your-sonarqube-token"
mvn clean verify sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN" "-DskipJacoco=false"
```

JaCoCo coverage reports are generated at `backend/target/site/jacoco/index.html`.

The quality profiles defined in `sonar-quality-profile.xml` can be imported via **SonarQube → Quality Profiles → Restore**.

---

## Security

| Concern | Implementation |
|---|---|
| Authentication | JWT (HS256), 24-hour expiry, stored in `localStorage` |
| Password storage | BCrypt with strength 12 |
| Data isolation | Every contact query is scoped to the authenticated user's ID |
| CORS | Configured for `localhost:3000` and `localhost:80` only |
| CSRF | Disabled (stateless JWT API) |
| Session | Stateless — no server-side session |
| Container user | Backend runs as a non-root user (`appuser`) inside Docker |
| Sensitive env vars | Never hard-coded; read from `.env` file or environment |

---

## Logging

The backend uses SLF4J with Logback. Configuration is in `backend/src/main/resources/logback-spring.xml`.

- **Console appender** — all environments
- **Rolling file appender** — writes to `logs/contact-management.log`, rotates daily, max 10 MB per file, 30 days retention, gzip compressed
- Log level `DEBUG` for `com.contactmanagement` in dev; `INFO` in Docker
- Inside Docker containers, logs are persisted in the `backend_logs` named volume

---

## Author

**Cohort 9 — Java Track**  
Student: Zohaib  
Project: Contact Management System
