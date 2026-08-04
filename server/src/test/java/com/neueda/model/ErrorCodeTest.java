package com.neueda.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ErrorCodeTest {

    @Test
    void eachErrorCodeExposesExpectedHttpStatusAndMessage() {
        assertAll(
            () -> assertEquals(400, ErrorCode.VALIDATION_FAILED.getHttpStatus()),
            () -> assertEquals(400, ErrorCode.INSUFFICIENT_FUNDS.getHttpStatus()),
            () -> assertEquals(400, ErrorCode.INVALID_ACCOUNT.getHttpStatus()),
            () -> assertEquals(400, ErrorCode.INVALID_CURRENCY.getHttpStatus()),
            () -> assertEquals(400, ErrorCode.INVALID_AMOUNT.getHttpStatus()),
            () -> assertEquals(409, ErrorCode.DUPLICATE_PAYMENT.getHttpStatus()),
            () -> assertEquals(400, ErrorCode.INVALID_STATUS_TRANSITION.getHttpStatus()),
            () -> assertEquals(404, ErrorCode.PAYMENT_NOT_FOUND.getHttpStatus()),
            () -> assertEquals(500, ErrorCode.PROCESSING_ERROR.getHttpStatus()),
            () -> assertEquals(503, ErrorCode.NETWORK_ERROR.getHttpStatus())
        );

        for (ErrorCode errorCode : ErrorCode.values()) {
            assertFalse(errorCode.getMessage().isBlank(), () -> errorCode + " should have a message");
        }
    }
}

