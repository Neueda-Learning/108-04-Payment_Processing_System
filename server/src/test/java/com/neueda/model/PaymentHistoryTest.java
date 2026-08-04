package com.neueda.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentHistoryTest {

    @Test
    void fullConstructorPopulatesAllFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 4, 12, 0);

        PaymentHistory history = new PaymentHistory(1L, 99L, "CREATED", "VALIDATED", timestamp, "Validation succeeded");

        assertAll(
            () -> assertEquals(1L, history.getId()),
            () -> assertEquals(99L, history.getPaymentId()),
            () -> assertEquals("CREATED", history.getFromStatus()),
            () -> assertEquals("VALIDATED", history.getToStatus()),
            () -> assertEquals(timestamp, history.getTimestamp()),
            () -> assertEquals("Validation succeeded", history.getNotes())
        );
    }

    @Test
    void insertConstructorLeavesIdUnset() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 4, 13, 30);

        PaymentHistory history = new PaymentHistory(100L, "VALIDATED", "SENT", timestamp, null);

        assertAll(
            () -> assertNull(history.getId()),
            () -> assertEquals(100L, history.getPaymentId()),
            () -> assertEquals("VALIDATED", history.getFromStatus()),
            () -> assertEquals("SENT", history.getToStatus()),
            () -> assertEquals(timestamp, history.getTimestamp()),
            () -> assertNull(history.getNotes())
        );
    }

    @Test
    void settersUpdateEachField() {
        LocalDateTime timestamp = LocalDateTime.now();
        PaymentHistory history = new PaymentHistory();

        history.setId(5L);
        history.setPaymentId(12L);
        history.setFromStatus("SENT");
        history.setToStatus("COMPLETED");
        history.setTimestamp(timestamp);
        history.setNotes("Network confirmation received");

        assertAll(
            () -> assertEquals(5L, history.getId()),
            () -> assertEquals(12L, history.getPaymentId()),
            () -> assertEquals("SENT", history.getFromStatus()),
            () -> assertEquals("COMPLETED", history.getToStatus()),
            () -> assertEquals(timestamp, history.getTimestamp()),
            () -> assertEquals("Network confirmation received", history.getNotes())
        );
    }
}

