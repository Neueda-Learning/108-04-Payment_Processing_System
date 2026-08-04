# Controller Package

This package contains REST controllers and global API error handling.

## Files

- `PaymentController.java`
	- Base path: `/payments`
	- `POST /payments`: create payment
	- `GET /payments/{id}`: fetch payment by id

- `GlobalExceptionHandler.java`
	- centralizes exception-to-response mapping
	- returns structured `ErrorResponse` payloads

## Notes

- Current API surface is intentionally minimal.
- Future controller additions should include status transitions, list/filter, and payment history endpoints.
