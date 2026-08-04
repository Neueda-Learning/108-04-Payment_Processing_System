# FlashPay Payment Processing System

Payment Processing System built by Room 108 Group 4.

## Project Structure

- `server/` - Spring Boot REST API (Java, JDBC, H2/MySQL)
- `frontend/` - React + Vite UI for payment flows
- `chatbot/` - FastAPI retrieval chatbot with Gemini fallback
- `Rules/` - project requirements and delivery guidance

## Dependency Overview

Dependency definitions are managed per module:

- Backend dependencies: `server/pom.xml`
- Frontend dependencies: `frontend/package.json`
- Chatbot dependencies: `chatbot/requirements.txt`

Current top-level dependency groups:

- Backend: Spring Boot Web MVC, Spring Data JDBC, H2, MySQL connector
- Frontend: React, React Router, Axios, Vite, Tailwind CSS
- Chatbot: FastAPI, Uvicorn, sentence-transformers, NumPy, requests, Pydantic

Install/update dependencies inside each module folder rather than from repository root.

## Current State

- Backend: minimal working API for creating a payment and fetching by id
- Frontend: complete page flow and navigation, currently mock-data driven
- Chatbot: working FAQ retrieval + LLM answer generation with fallback
- Validation framework: implemented (error codes, validator, exception handling), partial runtime wiring

## Quick Start

### 1. Backend API

From `server/`:

```bash
./mvnw spring-boot:run
```

Default API base URL:

```text
http://localhost:8080
```

### 2. Frontend

From `frontend/`:

```bash
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

### 3. Chatbot

From `chatbot/`:

```bash
pip install -r requirements.txt
python src/precompute_embeddings.py
uvicorn src.server:app --reload --host 0.0.0.0 --port 8000
```

Chatbot base URL:

```text
http://localhost:8000
```

Optional environment variable for LLM responses:

```text
GEMINI_API_KEY=<your_key>
```

Without a Gemini key, the chatbot still works using FAQ fallback responses.

## Backend API (Implemented)

- `POST /payments` - create payment
- `GET /payments/{id}` - fetch payment by id

Example create request:

```json
{
	"amount": 125.50,
	"status": "CREATED",
	"sourceAccount": "ACC001",
	"destinationAccount": "ACC002",
	"idempotencyKey": "idem-001"
}
```

## Database Modes

- Default: in-memory H2 for quick local development
- Optional: MySQL profile via `application-mysql.properties`
- Docker compose file for MySQL is available in `server/docker-compose.yml`

## Documentation

- Project brief and lifecycle rules: `Rules/payment_processing.md`
- Team/project workflow: `Rules/getting_started.md`
- Validation framework notes: `server/src/main/java/com/neueda/validation_framework_readme.md`
- Next phases and API testing roadmap: `README_NEXT_PHASES_AND_API_TESTING.md`

## Known Gaps

The following are not fully implemented yet:

- status transition endpoints (VALIDATED/SENT/COMPLETED/FAILED)
- payment history/audit trail persistence
- frontend integration with backend APIs
- analytics and history based on live backend data
- fuller unit/integration test coverage


