# FlashPay Backend (Spring Boot)

Backend REST API for the Payment Processing System.

## Tech Stack

- Java 25
- Spring Boot
- Spring Web MVC
- Spring Data JDBC
- H2 (default local runtime)
- MySQL (optional runtime)

## Dependencies

Dependency declarations live in `pom.xml`.

### Main dependencies

- `org.springframework.boot:spring-boot-starter-data-jdbc`
- `org.springframework.boot:spring-boot-starter-webmvc`
- `com.mysql:mysql-connector-j` (runtime)
- `com.h2database:h2` (runtime)

### Test dependencies

- `org.springframework.boot:spring-boot-starter-data-jdbc-test`
- `org.springframework.boot:spring-boot-starter-webmvc-test`

### Dependency management notes

- Spring dependency versions are primarily managed by the Spring Boot parent in `pom.xml`.
- Java version is defined in `pom.xml` properties (`java.version`).
- After dependency changes, run:

```bash
./mvnw test
```

to verify compatibility.

## Run Locally

From the `server/` folder:

```bash
./mvnw spring-boot:run
```

API base URL:

```text
http://localhost:8080
```

## Profiles and Data Sources

Default configuration uses in-memory H2:

- datasource: `jdbc:h2:mem:paymentdb`
- schema loaded from `src/main/resources/schema.sql`

MySQL settings are available in:

- `src/main/resources/application-mysql.properties`

Optional MySQL service is available via:

```bash
docker compose up -d
```

## Implemented Endpoints

### Create Payment

- Method: `POST`
- Path: `/payments`

Request example:

```json
{
  "amount": 125.50,
  "status": "CREATED",
  "sourceAccount": "ACC001",
  "destinationAccount": "ACC002",
  "idempotencyKey": "idem-001",
  "currency": "USD"
}
```

Responses: `201` created payment, `400` validation error, `409` duplicate idempotency key.

### List / Filter Payments

- Method: `GET`
- Path: `/payments`
- Optional query param: `status` (`CREATED`, `VALIDATED`, `SENT`, `COMPLETED`, `FAILED`)

```text
GET /payments
GET /payments?status=VALIDATED
```

Responses: `200` with a list of payments, `400` if `status` is not a recognized value.

### Get Payment by ID

- Method: `GET`
- Path: `/payments/{id}`

Responses: `200` found, `404` not found.

### Update Payment Status

- Method: `PUT`
- Path: `/payments/{id}/status`

Request example:

```json
{
  "status": "VALIDATED"
}
```

Responses: `200` updated payment, `400` invalid/unsupported status or illegal lifecycle transition, `404` payment not found.

### Get Payment History

- Method: `GET`
- Path: `/payments/{id}/history`

Responses: `200` with a list of `PaymentHistory` entries (newest first), `404` payment not found.

### Get Payment by Idempotency Key

- Method: `GET`
- Path: `/payments/idempotency/{key}`

Responses: `200` found, `404` not found.

### Payment Stats

- Method: `GET`
- Path: `/stats/payments`

Returns aggregate counts and rates:

```json
{
  "totalPayments": 5,
  "successfulPayments": 3,
  "failedPayments": 1,
  "totalAmount": 425.50,
  "successRate": 60.0,
  "failureRate": 20.0
}
```

## Database Schema (Current)

### `accounts`

- `id`
- `account_number`
- `account_holder_name`
- `account_currency_type`
- `balance`
- `status`

### `payments`

- `id`
- `amount`
- `status`
- `source_account`
- `destination_account`
- `idempotency_key` (unique)
- `created_at`
- `updated_at`
- `currency`
- `error_code`

### `payment_history`

- `id`
- `payment_id` (foreign key to `payments.id`)
- `from_status`
- `to_status`
- `timestamp`
- `notes`

## Implemented Validation Foundation

The following are implemented and ready to be fully wired into service flows:

- payment lifecycle enum and transition checks
- structured error codes with HTTP mapping
- custom exceptions for domain failures
- global exception handler
- payment validator utilities (amount/accounts/currency/idempotency key)

See:

- `src/main/java/com/neueda/validation_framework_readme.md`

## Tests

Run tests:

```bash
./mvnw test
```

Coverage includes controller (MockMvc), service (lifecycle, validation, stats), and repository (JDBC) layers for payments, payment history, and accounts, plus global exception handling.

## Known Gaps

- frontend integration with backend APIs
- fuller negative-path/edge-case test coverage as new features are added
- API test collection (Postman/Bruno) not yet checked into the repo
