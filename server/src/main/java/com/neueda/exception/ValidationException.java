package com.neueda.exception;

import com.neueda.model.ErrorCode;

/**
 * Custom exception for payment validation errors.
 */
public class ValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String details;

    public ValidationException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ValidationException(String message) {
        super(message);
        this.errorCode = ErrorCode.VALIDATION_FAILED;
        this.details = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }
}

