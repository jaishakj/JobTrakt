# JobTrakt

JobTrakt is a personal job application tracking and automation platform. This repository is a Java/Spring Boot and React/TypeScript monorepo.

## Project structure

```text
backend/   Spring Boot REST API (Java 21, Maven, PostgreSQL)
frontend/  React + TypeScript + Vite
docker-compose.yml
```

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker Engine with the Compose plugin (optional)
- PostgreSQL 16 (unless using Docker Compose)

## Local development

Copy `.env.example` to `.env` and set unique local PostgreSQL credentials plus a random `JWT_SECRET` of at least 32 characters. The backend requires `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`; it does not contain a fallback signing key. Flyway applies the schema migration at startup, and Hibernate validates its mappings against that schema.

Start PostgreSQL and ensure the configured database and user exist. Export `.env` variables into the shell before starting the backend (Spring Boot does not automatically load `.env` files):

```sh
set -a
source .env
set +a
cd backend
mvn spring-boot:run
```

In another terminal:

```sh
cd frontend
npm install
npm run dev
```

The frontend runs at <http://localhost:5173> and the API health endpoint is <http://localhost:8080/api/health>. `FRONTEND_URL` controls the allowed local browser origin and `JWT_EXPIRATION_SECONDS` defaults to 3600.

## Authentication API

- `POST /api/auth/register` — JSON body: `{"name":"Sam","email":"sam@example.com","password":"long-password-123"}`. Creates the account with a BCrypt password hash and returns HTTP 201 with a bearer token and public user details.
- `POST /api/auth/login` — JSON body: `{"email":"sam@example.com","password":"long-password-123"}`. Returns a bearer token and public user details.
- `GET /api/auth/me` — requires `Authorization: Bearer <accessToken>` and returns the authenticated account.

Registration requires a non-empty name up to 100 characters, a valid email, and a password between 10 and 72 characters. BCrypt's 72-byte input limit is also enforced. Emails are normalized to lowercase and duplicates return HTTP 409. Validation failures return HTTP 400; bad credentials and missing or invalid tokens return HTTP 401. `/api/health` and `/actuator/health` are public. Other `/api/**` routes require a valid JWT.

Example register request:

```sh
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Sam","email":"sam@example.com","password":"long-password-123"}'
```

## Docker Compose

```sh
cp .env.example .env
docker compose up --build
```

Compose starts PostgreSQL, the API, and the Vite development server. Copy and configure `.env` first; Compose passes database credentials and the JWT secret to the API. The first backend image build downloads Maven dependencies.

## Current implementation stage

Phase 1 established the repository layout and application entry points. Phase 2 adds user persistence, the Flyway user migration, registration/login, BCrypt password hashing, JWT authorization, CORS, and centralized API errors. Job applications, background jobs, notifications, analytics, and the authenticated product UI remain for later phases.
