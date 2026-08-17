# Contact Management System

A full-stack web application for managing personal and professional contacts. Users can register, log in, and perform full CRUD operations on their contacts — including search, view, add, edit, and delete.

---

## Technology Stack

| Layer       | Technology                                      |
|-------------|-------------------------------------------------|
| Backend     | Java 17, Spring Boot 3.2, Spring Data JPA, Hibernate |
| Security    | Spring Security, JWT                            |
| Frontend    | React 18, React Router v6, Axios                |
| Database    | Microsoft SQL Server 2022                       |
| Testing     | JUnit 5, Mockito, React Testing Library         |
| Logging     | SLF4J + Logback                                 |
| Code Quality| SonarQube, JaCoCo                               |
| Dev Tools   | Docker Compose, Maven, npm                      |

---

## Project Structure

```
cohort-9-java-10050-zohaib/
│
├── backend/                         # Spring Boot API
│   ├── src/main/java/com/contactmanagement/
│   │   ├── config/                  # Spring Security, CORS, beans
│   │   ├── controller/              # REST controllers
│   │   ├── dto/                     # Request / Response DTOs
│   │   ├── exception/               # Global exception handler
│   │   ├── model/                   # JPA entities
│   │   ├── repository/              # Spring Data JPA repositories
│   │   ├── security/                # JWT filter, UserDetailsService
│   │   ├── service/                 # Service interfaces
│   │   │   └── impl/                # Service implementations
│   │   └── util/                    # Shared helpers
│   ├── src/main/resources/
│   │   ├── application.properties   # Main config
│   │   ├── application-dev.properties
│   │   └── logback-spring.xml       # Logging config
│   └── src/test/                    # Unit & integration tests
│
├── frontend/                        # React SPA
│   ├── public/
│   └── src/
│       ├── components/
│       │   ├── auth/                # LoginForm, RegisterForm
│       │   ├── common/              # Navbar, PrivateRoute, Spinner
│       │   └── contacts/            # ContactCard, SearchBar
│       ├── context/                 # AuthContext (global auth state)
│       ├── hooks/                   # useAuth, useContacts
│       ├── pages/                   # LoginPage, RegisterPage, ContactListPage, etc.
│       ├── services/                # api.js (Axios), authService, contactService
│       └── utils/                   # validators, constants
│
├── database/
│   ├── migrations/                  # Versioned SQL schema scripts (V1, V2, V3)
│   ├── seeds/                       # Dev-only seed data
│   └── scripts/                     # DB setup & user creation scripts
│
├── .editorconfig
├── .env.example                     # Environment variable template
├── .gitignore
├── docker-compose.yml               # SQL Server + SonarQube (dev)
└── sonar-project.properties         # SonarQube multi-module config
```

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker Desktop (for SQL Server and SonarQube)
- Maven 3.8+

### 1. Clone the repository

```bash
git clone <repository-url>
cd cohort-9-java-10050-zohaib
```

### 2. Set up environment variables

```bash
cp .env.example .env
# Edit .env and fill in DB password, JWT secret, and SonarQube token
```

### 3. Start infrastructure (SQL Server + SonarQube)

```bash
docker-compose up -d
```

SQL Server is available at `localhost:1433`.
SonarQube is available at `http://localhost:9000` (default login: `admin` / `admin`).

### 4. Set up the database

Run these scripts in order using SQL Server Management Studio or `sqlcmd`:

```
database/scripts/create_database.sql
database/scripts/create_app_user.sql
database/migrations/V1__create_users_table.sql
database/migrations/V2__create_contacts_table.sql
database/migrations/V3__add_audit_trigger.sql
```

For dev data:
```
database/seeds/seed_dev_data.sql
```

### 5. Run the backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API runs on `http://localhost:8080`.

### 6. Run the frontend

```bash
cd frontend
npm install
npm start
```

App runs on `http://localhost:3000`. API calls are proxied to `localhost:8080` automatically.

---

## API Endpoints (planned)

| Method | Endpoint                  | Description              | Auth Required |
|--------|---------------------------|--------------------------|---------------|
| POST   | `/api/auth/register`      | Register a new user      | No            |
| POST   | `/api/auth/login`         | Login, receive JWT token | No            |
| GET    | `/api/contacts`           | List all contacts        | Yes           |
| GET    | `/api/contacts/{id}`      | Get contact by ID        | Yes           |
| POST   | `/api/contacts`           | Create a new contact     | Yes           |
| PUT    | `/api/contacts/{id}`      | Update a contact         | Yes           |
| DELETE | `/api/contacts/{id}`      | Delete a contact         | Yes           |
| GET    | `/api/contacts/search`    | Search contacts          | Yes           |

---

## Running Tests

**Backend:**
```bash
cd backend
mvn test
```

**Frontend:**
```bash
cd frontend
npm test
```

---

## Code Quality (SonarQube)

Ensure SonarQube is running (`docker-compose up -d`), then:

```bash
cd backend
mvn clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN
```

Results at `http://localhost:9000/dashboard?id=contact-management-system`.

---

## Development Workflow

1. Create a feature branch from `main`: `git checkout -b feature/<your-feature>`
2. Implement and test your changes.
3. Run SonarQube analysis and ensure the quality gate passes.
4. Open a pull request against `main`.

---

## Author

Cohort 9 — Java Track | Student: zohaib
