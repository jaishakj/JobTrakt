# JobTrakt

JobTrakt is a personal job search workspace for tracking applications, scheduling follow-ups, and reviewing a weekly search summary. It uses a Spring Boot API backed by PostgreSQL and a React/TypeScript client.

## Features

- JWT registration, login, profile updates, and password changes
- User-scoped application create, search, filter, update, delete, and timeline
- PostgreSQL-backed scheduled job queue with row locking, retries, and dead-letter state
- Interview and follow-up reminders, weekly summaries, and in-app notifications
- Dashboard metrics from persisted application, notification, and queue data
- Responsive dashboard, application, automation, notification, and settings screens
- OpenAPI documentation at `/swagger-ui/index.html`

## Architecture

`backend/` is a Java 21 / Spring Boot 3.4 API. Controllers use DTOs and delegate to services, repositories, and JPA entities. Flyway owns the schema; Hibernate uses `ddl-auto=validate`. A scheduled worker claims due PostgreSQL jobs with `FOR UPDATE SKIP LOCKED`, processes one job in a transaction, and applies bounded exponential retry delays.

`frontend/` is a React, TypeScript, and Vite SPA. Its API client attaches the session JWT and clears it on unauthorized responses. The UI uses the existing lightweight history/fetch foundation and repository CSS design system.

## Requirements and configuration

- Java 21 and Maven 3.9+
- Node.js 20+ and npm
- PostgreSQL 16, or Docker Compose

Copy `.env.example` to `.env`, set a strong random `JWT_SECRET` (at least 32 characters), and choose local database credentials. Spring Boot does not load `.env` automatically, so export its values when running outside Compose:

```sh
cp .env.example .env
set -a && source .env && set +a
```

Variables: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_SECONDS`, `FRONTEND_URL`, `VITE_API_URL`, and `JOB_POLL_DELAY_MS`. Compose also uses `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`.

## Run locally

Start PostgreSQL and create the database/user configured above. Then run the API:

```sh
cd backend
mvn spring-boot:run
```

In another terminal:

```sh
cd frontend
npm ci
npm run dev
```

Open <http://localhost:5173>. The API health route is <http://localhost:8080/api/health>. Flyway applies pending migrations on backend startup.

## Docker Compose

```sh
cp .env.example .env
# Set a unique local JWT_SECRET and database password in .env.
docker compose up --build
```

Compose runs PostgreSQL, the Spring API, and the Vite development server. Persistent database data is stored in the `postgres_data` volume.

## Authentication and API

- `POST /api/auth/register` — `{ "name": "Sam", "email": "sam@example.com", "password": "long-password-123" }`
- `POST /api/auth/login` — email/password; returns a JWT and public user details
- `GET /api/auth/me` — current authenticated account
- `PATCH /api/auth/me` and `PATCH /api/auth/me/password` — account settings
- `GET|POST /api/applications`, `GET|PUT|DELETE /api/applications/{id}` — application tracking; list supports `status`, `search`, `page`, `size`, and `sort`
- `GET /api/jobs`, `GET /api/jobs/{id}`, `POST /api/jobs/{id}/retry` — queue visibility and retry
- `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all`
- `GET /api/dashboard` — application, upcoming interview, notification, and job queue metrics

Except registration, login, health, and OpenAPI routes, API routes require `Authorization: Bearer <token>`. Data access is scoped to the authenticated user. Registration stores only a BCrypt password hash. API errors use a consistent JSON response and do not return stack traces.

Example account creation:

```sh
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Sam","email":"sam@example.com","password":"long-password-123"}'
```

## Automation

An application with an interview date queues an interview reminder. Applied applications queue a follow-up after seven days; an explicit follow-up date takes precedence. Scheduled jobs create in-app notifications only while their source application still matches the dates/status used when queued. A weekly report runs Monday at 09:00 UTC and summarizes the prior seven days. Jobs retry with increasing delays and become `DEAD` after five failed attempts. Users can retry failed/dead jobs from the automation page.

## Database migrations

- `V1__create_users.sql` — accounts
- `V2__create_job_applications.sql` — user-owned applications
- `V3__create_jobs.sql` — queue and deduplication keys
- `V4__create_notifications.sql` — in-app notifications
- `V5__create_audit_logs.sql` — application change history

All tables use foreign keys, ownership indexes, and UTC-compatible timestamps. Do not use Hibernate schema generation to modify the schema.

## Tests and build

Backend tests use Spring Boot Test, JUnit 5, Mockito, and an H2 PostgreSQL compatibility database. Frontend verification runs TypeScript and Vite production builds.

```sh
cd backend && mvn test package
cd ../frontend && npm ci && npm run build
```

CI runs backend tests/package and frontend install/build on pushes and pull requests. PostgreSQL-specific worker locking should also be exercised against PostgreSQL for release verification.

## Structure

```text
backend/src/main/java/com/jobtrakt/{application,audit,auth,common,dashboard,job,notification,user}
backend/src/main/resources/db/migration/
frontend/src/{api,components,lib,pages}
.github/workflows/ci.yml
```

## Deployment notes

Provide the database connection, JWT secret, token lifetime, and allowed frontend origin through the runtime environment. Use TLS, managed secret storage, backups, and a production static frontend server in a deployed environment. The included frontend container is configured for local Vite development.
