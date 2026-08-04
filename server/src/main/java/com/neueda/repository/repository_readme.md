# Repository Package

This package contains persistence interfaces and JDBC implementations.

## Files

- `PaymentRepository.java`
	- `save(Payment)`
	- `findById(Long)`
	- `findAll()`
	- `findByIdempotencyKey(String)`

- `PaymentRepositoryImplementation.java`
	- uses `JdbcTemplate`
	- reads/writes `payments` table

- `AccountRepository.java`
	- `save(Account)`
	- `findById(Long)`
	- `findByAccountNumber(String)`
	- `findAll()`

- `AccountRepositoryImplementation.java`
	- uses `JdbcTemplate`
	- reads/writes `accounts` table

## Notes

- Repositories currently focus on basic CRUD-style operations.
- Planned additions include status update and status-filter queries for payments.
