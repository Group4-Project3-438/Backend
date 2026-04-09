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

---

## Authentication
- OAuth2 login (Google, GitHub)  
- User session / token handling  
- User creation on first login  

---

## Database
- Stores all user and application data  
- Entity-based structure (tables mapped from Java classes)  

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
