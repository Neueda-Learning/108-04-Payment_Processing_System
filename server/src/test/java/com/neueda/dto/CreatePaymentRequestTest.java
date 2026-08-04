package com.neueda.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreatePaymentRequestTest {

    @Test
    void constructorPopulatesCoreFields() {
        CreatePaymentRequest request = new CreatePaymentRequest(
            new BigDecimal("150.75"),
            "USD",
            "SOURCE123",
            "TARGET123",
            "idem-123"
        );

        assertAll(
            () -> assertEquals(new BigDecimal("150.75"), request.getAmount()),
            () -> assertEquals("USD", request.getCurrency()),
            () -> assertEquals("SOURCE123", request.getSourceAccount()),
            () -> assertEquals("TARGET123", request.getDestinationAccount()),
            () -> assertEquals("idem-123", request.getIdempotencyKey()),
            () -> assertNull(request.getReference())
        );
    }

    @Test
    void settersUpdateAllFields() {
        CreatePaymentRequest request = new CreatePaymentRequest();

        request.setAmount(new BigDecimal("10.00"));
        request.setCurrency("EUR");
        request.setSourceAccount("ACC11111");
        request.setDestinationAccount("ACC22222");
        request.setIdempotencyKey("idem-456");
        request.setReference("invoice-2026-08");

        assertAll(
            () -> assertEquals(new BigDecimal("10.00"), request.getAmount()),
            () -> assertEquals("EUR", request.getCurrency()),
            () -> assertEquals("ACC11111", request.getSourceAccount()),
            () -> assertEquals("ACC22222", request.getDestinationAccount()),
            () -> assertEquals("idem-456", request.getIdempotencyKey()),
            () -> assertEquals("invoice-2026-08", request.getReference())
        );
    }
}

