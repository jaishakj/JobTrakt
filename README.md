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

Copy `.env.example` to `.env` and adjust credentials for your machine. Start PostgreSQL and ensure the database and user exist, then:

```sh
cd backend
mvn spring-boot:run
```

In another terminal:

```sh
cd frontend
npm install
npm run dev
```

The frontend runs at <http://localhost:5173> and the API health endpoint is <http://localhost:8080/api/health>. Database connection settings are read from `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.

## Docker Compose

```sh
cp .env.example .env
docker compose up --build
```

Compose starts PostgreSQL, the API, and the Vite development server. The first backend image build downloads Maven dependencies.

## Current implementation stage

Phase 1 establishes the repository layout and application entry points. Authentication, application management, database schema, background jobs, notifications, analytics, and the product UI are planned for subsequent implementation phases; they are not represented as complete in this starter.
