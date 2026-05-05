# CST 438 – Project 3 Backend

---

## Overview

This repository contains the backend service for our CST 438 project.  
It is responsible for handling business logic, API endpoints, authentication, and database interactions.

> This is part of an **early-stage development setup** and will evolve over time.

---

## Core Responsibilities

- Provide REST API endpoints  
- Handle authentication (OAuth2 via Spring Security)  
- Manage database interactions  
- Enforce business logic and validation  
- Handle CORS for frontend communication

---

## Tech Stack

- Java + Spring Boot  
- Spring Security (OAuth2)  
- PostgreSQL (*Supabase or Firebase TBD*)  
- Docker

---

## API Requirements

- Minimum of **8 endpoints**  
- CRUD operations for core features  
- User authentication and management routes  
- Chat room endpoints linked to external entity IDs

### Chat API (new sample)

- `GET /api/chat/rooms/{entityId}`  
Ensures a room exists for `entityId` and pulls payload from:
`external.api.base-url + /entities/{entityId}` on first creation.
- `GET /api/chat/rooms/{entityId}/messages`
- `POST /api/chat/rooms/{entityId}/messages`

Sample POST body:

```json
{
  "senderId": "user-1",
  "senderName": "Josh",
  "text": "Hello from popup chat"
}
```

---

## Authentication

- OAuth2 login (Google, GitHub)  
- User session / token handling  
- User creation on first login  
- Auth is managed by this Spring Boot app (`application-oauth2`), not by Supabase Auth

---

## Database

- Stores all user and application data  
- Entity-based structure (tables mapped from Java classes)  
- Runtime DB is configured for Supabase Postgres via env vars:
  - `SUPABASE_DB_URL`
  - `SUPABASE_DB_USER`
  - `SUPABASE_DB_PASSWORD`
- Supabase is used for Postgres data storage only in this backend

### Local env setup

1. Fill `.env` with Supabase and OAuth values
2. Run locally with:
  - `./run-local.sh` (auto-loads `.env`)

---

## Testing

- JUnit for unit testing  
- Postman for endpoint testing *(optional)*

---

## Deployment

- Hosted separately from frontend  
- Docker containerized  
- Connected to GitHub for automatic deployment  
- Tests must pass before deployment

---

## Notes

> Backend must remain independent and communicate with frontend via API  
> Structure and endpoints may evolve during development  

