package com.neueda.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentStatusTest {

    @Test
    void descriptionsArePresentForAllStatuses() {
        for (PaymentStatus status : PaymentStatus.values()) {
            assertFalse(status.getDescription().isBlank(), () -> status + " should have a description");
        }
    }

    @Test
    void validLifecycleTransitionsAreAllowed() {
        assertAll(
            () -> assertTrue(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.VALIDATED)),
            () -> assertTrue(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.FAILED)),
            () -> assertTrue(PaymentStatus.VALIDATED.canTransitionTo(PaymentStatus.SENT)),
            () -> assertTrue(PaymentStatus.VALIDATED.canTransitionTo(PaymentStatus.FAILED)),
            () -> assertTrue(PaymentStatus.SENT.canTransitionTo(PaymentStatus.COMPLETED)),
            () -> assertTrue(PaymentStatus.SENT.canTransitionTo(PaymentStatus.FAILED))
        );
    }

    @Test
    void invalidLifecycleTransitionsAreRejected() {
        assertAll(
            () -> assertFalse(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.CREATED)),
            () -> assertFalse(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.SENT)),
            () -> assertFalse(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.COMPLETED)),
            () -> assertFalse(PaymentStatus.VALIDATED.canTransitionTo(PaymentStatus.CREATED)),
            () -> assertFalse(PaymentStatus.VALIDATED.canTransitionTo(PaymentStatus.COMPLETED)),
            () -> assertFalse(PaymentStatus.SENT.canTransitionTo(PaymentStatus.CREATED)),
            () -> assertFalse(PaymentStatus.SENT.canTransitionTo(PaymentStatus.VALIDATED)),
            () -> assertFalse(PaymentStatus.COMPLETED.canTransitionTo(PaymentStatus.FAILED)),
            () -> assertFalse(PaymentStatus.COMPLETED.canTransitionTo(PaymentStatus.COMPLETED)),
            () -> assertFalse(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.CREATED)),
            () -> assertFalse(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.FAILED))
        );
    }
}

