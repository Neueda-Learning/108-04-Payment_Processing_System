# Service Package

This package contains business logic interfaces and implementations.

## Files

- `PaymentService.java`
	- create payment
	- get payment by id
	- get all payments
	- get payment by idempotency key

- `PaymentServiceImplemenation.java`
	- current implementation delegates directly to repository
	- includes placeholders for validation/idempotency/status logic

- `AccountService.java`
	- create account
	- get account by id
	- get all accounts
	- get account by account number

- `AccountServiceImplementation.java`
	- current implementation delegates directly to repository

## Notes

- Validation framework exists and should be wired here as next step.
- Status transition checks should be enforced in this layer.

## Service Layer Dependency Notes

- Service classes use Spring DI annotations (for example, `@Service`) and rely on dependencies configured in `server/pom.xml`.
- Do not add library-specific logic directly in services unless the dependency is already declared and justified at module level.
- When introducing a new service dependency, update `server/pom.xml` and document the reason in `server/README.md`.
