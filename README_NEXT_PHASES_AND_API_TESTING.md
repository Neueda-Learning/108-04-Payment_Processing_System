# FlashPay Roadmap (After Latest `main`) + API Testing Plan

This plan is based on the latest `main` branch state (including backend, frontend, and chatbot).

## 1) Current Project Snapshot

### Backend (`server/`)
- Spring Boot API is running with H2 by default and optional MySQL profile.
- Implemented payment endpoints:
  - `POST /payments`
  - `GET /payments/{id}`
- Validation foundation exists (`PaymentValidator`, `ErrorCode`, exceptions, global handler).
- Data model now includes audit fields in `Payment` and includes `PaymentHistory` model/repository.
- `schema.sql` includes accounts, payments, and payment history table structure updates.
- Several unit tests were added recently across controller/dto/model/repository/service/validator.

### Frontend (`frontend/`)
- React + Vite app with routes:
  - `/`, `/home`, `/payments`, `/history`, `/stats`, `/faq`
- UI pages are built and navigable.
- Integration gap: payment form/history/stats are mostly static or local-only behavior; backend wiring is limited.

### Chatbot (`chatbot/`)
- FastAPI service for FAQ support with retrieval + optional Gemini integration.
- Can be run independently and later linked from frontend FAQ/help workflows.

## 2) Next Phases (from here)

## Phase 2: Payment Lifecycle Logic (Backend Core)

### Goal
Make payment processing behavior correct and enforce lifecycle rules.

### Deliverables
- Enforce valid status transitions using `PaymentStatus`.
- Default status on create (`CREATED`) if missing.
- Implement idempotency behavior using `idempotencyKey`.
- Store history records on each status change in `payment_history`.
- Validate create and update flows through `PaymentValidator`.

### Suggested Team Split
- Backend Dev 1: `PaymentServiceImplemenation` lifecycle + validation wiring.
- Backend Dev 2: repository updates for status/history read/write.
- QA Dev: service + repository tests for lifecycle paths.

### Definition of Done
- Invalid transitions are blocked.
- Duplicate idempotency key behavior is consistent.
- History entries are persisted for each transition.
- Tests pass for happy path and failure path.

---

## Phase 3: API Surface Completion

### Goal
Expose the full workflow needed by frontend and testing.

### Deliverables (new endpoints)
- `GET /payments` (optionally filter by `status`)
- `PUT /payments/{id}/status`
- `GET /payments/{id}/history`
- `GET /payments/idempotency/{key}` (or query param equivalent)
- Optional: `GET /stats/payments` (count, success/fail rate, volume)

### Suggested Team Split
- Backend Dev 1: controller + DTO contracts.
- Backend Dev 2: service/repository implementation.
- QA Dev: API contract tests and negative tests.

### Definition of Done
- Endpoints documented with request/response examples.
- Error format is consistent (`ErrorResponse`).
- 404/400/409/500 behavior is deterministic and tested.

---

## Phase 4: Frontend-Backend Integration

### Goal
Replace static placeholders with live backend data.

### Deliverables
- `PaymentsPage`: submit real `POST /payments` request.
- `PaymentHistory`: load real list/history data from API.
- `StatsPage`: use backend aggregates or compute from fetched payments.
- Global error handling in UI (validation and server errors).
- Add API client layer (`axios` service module) and environment config (`VITE_API_BASE_URL`).

### Suggested Team Split
- Frontend Dev 1: payment form integration + validation display.
- Frontend Dev 2: history/stats integration.
- Backend Support: fill missing endpoints/fields for UI needs.

### Definition of Done
- User can create payment and immediately see real status/history.
- No hardcoded placeholder transaction data on core screens.
- Core flows are demoable end-to-end.

---

## Phase 5: Quality, Automation, and Demo Readiness

### Goal
Stabilize and prepare for presentation/review.

### Deliverables
- Expand backend tests (controller + service + repository integration).
- Add frontend smoke tests (at least route render + payment submit workflow).
- API test collection in Postman/Bruno + optional Newman run.
- README cleanup across root/server/frontend/chatbot.
- Demo script and sample data setup.

### Definition of Done
- Repeatable runbook for backend/frontend/chatbot.
- Regression tests pass locally.
- Team can execute a timed demo without manual patching.

---

## 3) API Testing Strategy (What we should do)

## A) Tools
- Manual + collaboration: Postman (or Bruno/Insomnia)
- CLI quick checks: `curl` or PowerShell `Invoke-RestMethod`
- Automated backend tests: JUnit + MockMvc (and repository tests)
- Optional CI execution: Newman for Postman collections

## B) Environments
Create two environments in Postman:
- `local-h2`: `baseUrl = http://localhost:8080`
- `local-mysql`: `baseUrl = http://localhost:8080` (backend running with mysql profile)

## C) Minimum Test Suite (must-have)

### Current endpoints (already available)
1. Create payment (happy path)
2. Create payment (invalid amount)
3. Get payment by id (found)
4. Get payment by id (not found)

### Phase 3 endpoints (add tests when implemented)
5. Get all payments
6. Filter payments by status
7. Update status valid transition
8. Update status invalid transition
9. Get payment history by id
10. Duplicate idempotency behavior

## D) Example API Tests (PowerShell)

Run backend first from `server/`:

```powershell
./mvnw spring-boot:run
```

### 1. Create Payment

```powershell
$body = @{
  amount = 125.50
  status = "CREATED"
  sourceAccount = "ACC001"
  destinationAccount = "ACC002"
  idempotencyKey = "idem-001"
  currency = "USD"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/payments" -ContentType "application/json" -Body $body
```

### 2. Get Payment by ID

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/payments/1"
```

### 3. Get Not Found Case

```powershell
try {
  Invoke-RestMethod -Method Get -Uri "http://localhost:8080/payments/999999"
} catch {
  $_.Exception.Response.StatusCode.value__
}
```

## E) Expected Assertions
- Create returns `201` and non-null `id`.
- Fetch existing returns `200` and same payment fields.
- Missing resource returns `404`.
- Validation errors return structured `ErrorResponse` with proper `errorCode` and `httpStatus`.
- Duplicate idempotency handling returns expected status (`200`/`409`) based on final team design.

## F) Automation Plan
- Keep API collection under version control (for example `tests/api/FlashPay.postman_collection.json`).
- Run collection locally before PR merge.
- Add CI job later to run backend tests + Newman API tests.

## 4) Recommended Execution Order

1. Finish Phase 2 backend lifecycle behavior.
2. Implement Phase 3 endpoint expansion.
3. Wire frontend in Phase 4.
4. Lock quality and demo assets in Phase 5.

This order minimizes frontend rework and gives a stable API contract early.

