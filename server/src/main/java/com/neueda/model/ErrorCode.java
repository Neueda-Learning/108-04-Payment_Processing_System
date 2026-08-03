package com.neueda.model;

/**
 * Enum representing error codes for payment processing failures.
 * Used to communicate specific failure reasons to API clients.
 */
public enum ErrorCode {
    VALIDATION_FAILED("Payment failed validation checks", 400),
    INSUFFICIENT_FUNDS("Source account has insufficient funds", 400),
    INVALID_ACCOUNT("Account number is invalid or doesn't exist", 400),
    INVALID_CURRENCY("Currency code is not supported", 400),
    INVALID_AMOUNT("Amount is zero, negative, or invalid", 400),
    DUPLICATE_PAYMENT("Payment with same idempotency key exists", 409),
    INVALID_STATUS_TRANSITION("Cannot transition from current status to requested status", 400),
    PAYMENT_NOT_FOUND("Payment ID does not exist", 404),
    PROCESSING_ERROR("Internal error during payment processing", 500),
    NETWORK_ERROR("Communication failure with payment network", 503);

    private final String message;
    private final int httpStatus;

    ErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}

