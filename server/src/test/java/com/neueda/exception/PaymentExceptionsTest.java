package com.neueda.exception;

import com.neueda.model.ErrorCode;
import com.neueda.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentExceptionsTest {

    @Test
    void validationExceptionWithErrorCodeRetainsMetadata() {
        ValidationException exception = new ValidationException(ErrorCode.INVALID_AMOUNT, "Amount must be positive");

        assertAll(
            () -> assertEquals(ErrorCode.INVALID_AMOUNT, exception.getErrorCode()),
            () -> assertEquals("Amount must be positive", exception.getDetails()),
            () -> assertTrue(exception.getMessage().contains(ErrorCode.INVALID_AMOUNT.getMessage()))
        );
    }

    @Test
    void validationExceptionWithMessageDefaultsToValidationFailed() {
        ValidationException exception = new ValidationException("Generic validation failure");

        assertAll(
            () -> assertEquals(ErrorCode.VALIDATION_FAILED, exception.getErrorCode()),
            () -> assertEquals("Generic validation failure", exception.getDetails()),
            () -> assertEquals("Generic validation failure", exception.getMessage())
        );
    }

    @Test
    void duplicatePaymentExceptionExposesConflictingIdentifiers() {
        DuplicatePaymentException exception = new DuplicatePaymentException("idem-dup", 42L);

        assertAll(
            () -> assertEquals(ErrorCode.DUPLICATE_PAYMENT, exception.getErrorCode()),
            () -> assertEquals("idem-dup", exception.getIdempotencyKey()),
            () -> assertEquals(42L, exception.getExistingPaymentId()),
            () -> assertTrue(exception.getMessage().contains("idem-dup"))
        );
    }

    @Test
    void invalidStatusTransitionExceptionExposesFromAndToStatus() {
        InvalidStatusTransitionException exception = new InvalidStatusTransitionException(PaymentStatus.CREATED, PaymentStatus.COMPLETED);

        assertAll(
            () -> assertEquals(ErrorCode.INVALID_STATUS_TRANSITION, exception.getErrorCode()),
            () -> assertEquals(PaymentStatus.CREATED, exception.getFromStatus()),
            () -> assertEquals(PaymentStatus.COMPLETED, exception.getToStatus()),
            () -> assertTrue(exception.getMessage().contains("CREATED")),
            () -> assertTrue(exception.getMessage().contains("COMPLETED"))
        );
    }

    @Test
    void paymentNotFoundExceptionExposesPaymentId() {
        PaymentNotFoundException exception = new PaymentNotFoundException(77L);

        assertAll(
            () -> assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode()),
            () -> assertEquals(77L, exception.getPaymentId()),
            () -> assertTrue(exception.getMessage().contains("77"))
        );
    }
}

