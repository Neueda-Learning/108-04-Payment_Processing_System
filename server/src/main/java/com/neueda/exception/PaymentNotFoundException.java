package com.neueda.exception;

import com.neueda.model.ErrorCode;

/**
 * Exception thrown when a payment is not found in the system.
 */
public class PaymentNotFoundException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.PAYMENT_NOT_FOUND;
    private final Long paymentId;

    public PaymentNotFoundException(Long paymentId) {
        super(ErrorCode.PAYMENT_NOT_FOUND.getMessage() + ": Payment ID " + paymentId + " does not exist");
        this.paymentId = paymentId;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Long getPaymentId() {
        return paymentId;
    }
}

