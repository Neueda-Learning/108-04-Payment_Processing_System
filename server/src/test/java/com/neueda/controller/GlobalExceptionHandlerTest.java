package com.neueda.controller;

import com.neueda.dto.ErrorResponse;
import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.InvalidStatusTransitionException;
import com.neueda.exception.PaymentNotFoundException;
import com.neueda.exception.ValidationException;
import com.neueda.model.ErrorCode;
import com.neueda.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationExceptionReturnsStructuredBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(
            new ValidationException(ErrorCode.INVALID_CURRENCY, "Currency must be supported")
        );

        assertAll(
            () -> assertEquals(400, response.getStatusCode().value()),
            () -> assertEquals("INVALID_CURRENCY", response.getBody().getErrorCode()),
            () -> assertEquals("Currency must be supported", response.getBody().getDetails()),
            () -> assertNotNull(response.getBody().getTimestamp())
        );
    }

    @Test
    void handlePaymentNotFoundExceptionReturnsNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handlePaymentNotFoundException(new PaymentNotFoundException(88L));

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode()),
            () -> assertEquals("PAYMENT_NOT_FOUND", response.getBody().getErrorCode()),
            () -> assertEquals("Payment ID 88 does not exist", response.getBody().getDetails())
        );
    }

    @Test
    void handleInvalidStatusTransitionExceptionReturnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidStatusTransitionException(
            new InvalidStatusTransitionException(PaymentStatus.CREATED, PaymentStatus.COMPLETED)
        );

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
            () -> assertEquals("INVALID_STATUS_TRANSITION", response.getBody().getErrorCode()),
            () -> assertEquals("Cannot transition from CREATED to COMPLETED", response.getBody().getDetails())
        );
    }

    @Test
    void handleDuplicatePaymentExceptionReturnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicatePaymentException(
            new DuplicatePaymentException("idem-88", 88L)
        );

        assertAll(
            () -> assertEquals(HttpStatus.CONFLICT, response.getStatusCode()),
            () -> assertEquals("DUPLICATE_PAYMENT", response.getBody().getErrorCode()),
            () -> assertEquals("Idempotency key 'idem-88' already exists with Payment ID 88", response.getBody().getDetails())
        );
    }

    @Test
    void handleHttpMessageNotReadableExceptionReturnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadableException(
            new HttpMessageNotReadableException("Malformed JSON", new MockHttpInputMessage(new byte[0]))
        );

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
            () -> assertEquals("VALIDATION_FAILED", response.getBody().getErrorCode()),
            () -> assertEquals("Malformed request body", response.getBody().getDetails())
        );
    }

    @Test
    void handleGenericExceptionReturnsProcessingError() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(new RuntimeException("unexpected failure"));

        assertAll(
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode()),
            () -> assertEquals("PROCESSING_ERROR", response.getBody().getErrorCode()),
            () -> assertEquals("unexpected failure", response.getBody().getDetails())
        );
    }
}



