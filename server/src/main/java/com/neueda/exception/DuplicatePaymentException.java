package com.neueda.exception;

import com.neueda.model.ErrorCode;

/**
 * Exception thrown when a duplicate payment is detected via idempotency key.
 */
public class DuplicatePaymentException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.DUPLICATE_PAYMENT;
    private final String idempotencyKey;
    private final Long existingPaymentId;

    public DuplicatePaymentException(String idempotencyKey, Long existingPaymentId) {
        super(ErrorCode.DUPLICATE_PAYMENT.getMessage() + ": Payment with idempotency key " + idempotencyKey + " already exists (ID: " + existingPaymentId + ")");
        this.idempotencyKey = idempotencyKey;
        this.existingPaymentId = existingPaymentId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getExistingPaymentId() {
        return existingPaymentId;
    }
}

