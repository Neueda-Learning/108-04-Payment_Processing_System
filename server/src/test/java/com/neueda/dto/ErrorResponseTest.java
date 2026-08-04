package com.neueda.dto;

import com.neueda.model.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorResponseTest {

    @Test
    void enumBasedConstructorMapsErrorMetadata() {
        ErrorResponse response = new ErrorResponse(ErrorCode.INVALID_AMOUNT, "Amount cannot be zero");

        assertAll(
            () -> assertEquals("INVALID_AMOUNT", response.getErrorCode()),
            () -> assertEquals(ErrorCode.INVALID_AMOUNT.getMessage(), response.getMessage()),
            () -> assertEquals(400, response.getHttpStatus()),
            () -> assertEquals("Amount cannot be zero", response.getDetails()),
            () -> assertNotNull(response.getTimestamp())
        );
    }

    @Test
    void explicitConstructorAndSettersAllowCustomisation() {
        ErrorResponse response = new ErrorResponse("CUSTOM", "Custom message", 418, "Teapot details");
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 4, 16, 45);

        response.setErrorCode("UPDATED");
        response.setMessage("Updated message");
        response.setHttpStatus(422);
        response.setDetails("Updated details");
        response.setTimestamp(timestamp);

        assertAll(
            () -> assertEquals("UPDATED", response.getErrorCode()),
            () -> assertEquals("Updated message", response.getMessage()),
            () -> assertEquals(422, response.getHttpStatus()),
            () -> assertEquals("Updated details", response.getDetails()),
            () -> assertEquals(timestamp, response.getTimestamp())
        );
    }
}

