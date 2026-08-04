# Model Package

This package contains domain entities and enums used by the payment system.

## Files

- `Payment.java`
	- payment entity currently containing:
		- `id`
		- `amount`
		- `status`
		- `sourceAccount`
		- `destinationAccount`
		- `idempotencyKey`

- `Account.java`
	- account entity containing:
		- `id`
		- `accountNumber`
		- `accountHolderName`
		- `accountCurrencyType`
		- `balance`
		- `status`

- `PaymentStatus.java`
	- payment lifecycle enum:
		- CREATED
		- VALIDATED
		- SENT
		- COMPLETED
		- FAILED
	- includes transition helper `canTransitionTo(...)`

- `ErrorCode.java`
	- standardized domain error codes with HTTP status mapping

## Notes

- Model is currently minimal and suitable for baseline flow.
- Planned extension includes audit/history fields and transition history entity.
