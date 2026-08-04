package com.neueda.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentTest {

    @Test
    void partialConstructorLeavesAuditFieldsUnset() {
        Payment payment = new Payment(10L, new BigDecimal("125.50"), "CREATED", "SRC12345", "DST12345", "idem-10");

        assertAll(
            () -> assertEquals(10L, payment.getId()),
            () -> assertEquals(new BigDecimal("125.50"), payment.getAmount()),
            () -> assertEquals("CREATED", payment.getStatus()),
            () -> assertEquals("SRC12345", payment.getSourceAccount()),
            () -> assertEquals("DST12345", payment.getDestinationAccount()),
            () -> assertEquals("idem-10", payment.getIdempotencyKey()),
            () -> assertNull(payment.getCreatedAt()),
            () -> assertNull(payment.getUpdatedAt()),
            () -> assertNull(payment.getCurrency()),
            () -> assertNull(payment.getErrorCode())
        );
    }

    @Test
    void extendedConstructorPopulatesAuditFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 4, 10, 15);
        LocalDateTime updatedAt = createdAt.plusMinutes(5);

        Payment payment = new Payment(
            20L,
            new BigDecimal("300.00"),
            "FAILED",
            "SRC67890",
            "DST67890",
            "idem-20",
            createdAt,
            updatedAt,
            "USD",
            ErrorCode.PROCESSING_ERROR.name()
        );

        assertAll(
            () -> assertEquals(20L, payment.getId()),
            () -> assertEquals(createdAt, payment.getCreatedAt()),
            () -> assertEquals(updatedAt, payment.getUpdatedAt()),
            () -> assertEquals("USD", payment.getCurrency()),
            () -> assertEquals(ErrorCode.PROCESSING_ERROR.name(), payment.getErrorCode())
        );
    }

    @Test
    void settersUpdateEachField() {
        LocalDateTime now = LocalDateTime.now();
        Payment payment = new Payment();

        payment.setId(30L);
        payment.setAmount(new BigDecimal("10.25"));
        payment.setStatus("VALIDATED");
        payment.setSourceAccount("SOURCE111");
        payment.setDestinationAccount("TARGET222");
        payment.setIdempotencyKey("idem-30");
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now.plusSeconds(30));
        payment.setCurrency("EUR");
        payment.setErrorCode(ErrorCode.INVALID_ACCOUNT.name());

        assertAll(
            () -> assertEquals(30L, payment.getId()),
            () -> assertEquals(new BigDecimal("10.25"), payment.getAmount()),
            () -> assertEquals("VALIDATED", payment.getStatus()),
            () -> assertEquals("SOURCE111", payment.getSourceAccount()),
            () -> assertEquals("TARGET222", payment.getDestinationAccount()),
            () -> assertEquals("idem-30", payment.getIdempotencyKey()),
            () -> assertEquals(now, payment.getCreatedAt()),
            () -> assertEquals(now.plusSeconds(30), payment.getUpdatedAt()),
            () -> assertEquals("EUR", payment.getCurrency()),
            () -> assertEquals(ErrorCode.INVALID_ACCOUNT.name(), payment.getErrorCode())
        );
    }
}

