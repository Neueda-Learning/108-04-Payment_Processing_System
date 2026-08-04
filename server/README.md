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
  "idempotencyKey": "idem-001"
}
```

### Get Payment by ID

- Method: `GET`
- Path: `/payments/{id}`

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
- `idempotency_key`

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

Current tests cover basic app bootstrapping and controller behavior; deeper repository/service/validation tests are still pending.

## Known Gaps

- no status transition update endpoint yet
- no payment history/audit table yet
- idempotency and validation not fully enforced during create flow
- no list/filter endpoint for payments by status yet
