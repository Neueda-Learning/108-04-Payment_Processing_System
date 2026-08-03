package com.neueda.exception;

import com.neueda.model.ErrorCode;
import com.neueda.model.PaymentStatus;

/**
 * Exception thrown when an invalid payment status transition is attempted.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.INVALID_STATUS_TRANSITION;
    private final PaymentStatus fromStatus;
    private final PaymentStatus toStatus;

    public InvalidStatusTransitionException(PaymentStatus fromStatus, PaymentStatus toStatus) {
        super(String.format("Cannot transition from %s to %s", fromStatus, toStatus));
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public PaymentStatus getFromStatus() {
        return fromStatus;
    }

    public PaymentStatus getToStatus() {
        return toStatus;
    }
}

