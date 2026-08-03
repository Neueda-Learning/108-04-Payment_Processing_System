package com.neueda.model;

/**
 * Enum representing the lifecycle status of a payment.
 * 
 * Payment lifecycle:
 * CREATED → VALIDATED → SENT → COMPLETED
 *                   ↓
 *              FAILED (can occur at any stage)
 */
public enum PaymentStatus {
    CREATED("Payment has been submitted but not yet validated"),
    VALIDATED("Payment has passed all validation rules and is ready to be sent"),
    SENT("Payment has been transmitted to the destination system"),
    COMPLETED("Payment has been successfully processed and confirmed"),
    FAILED("Payment has failed at some point in the process");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if transition from current status to target status is valid
     */
    public boolean canTransitionTo(PaymentStatus targetStatus) {
        if (targetStatus == FAILED) {
            // Can transition to FAILED from any status
            return !this.equals(COMPLETED) && !this.equals(FAILED);
        }

        return switch (this) {
            case CREATED -> targetStatus == VALIDATED;
            case VALIDATED -> targetStatus == SENT;
            case SENT -> targetStatus == COMPLETED;
            case COMPLETED, FAILED -> false; // Terminal states
        };
    }
}

