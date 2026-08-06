package com.neueda.service;
import com.neueda.model.Payment;
import com.neueda.model.PaymentHistory;
import com.neueda.model.PaymentStatus;
import com.neueda.dto.PaymentStatsResponse;
import java.util.List;
import java.util.Optional;

public interface PaymentService {

    /**
     * Create a new payment.
     * Validates all fields, enforces idempotency, and sets default status to CREATED.
     */
    Payment createPayment(Payment payment);

    /**
     * Transition an existing payment to a new status.
     * Enforces the lifecycle state machine and records an audit history entry.
     */
    Payment transitionStatus(Long id, PaymentStatus targetStatus);

    Optional<Payment> getPaymentById(Long id);

    List<PaymentHistory> getPaymentHistory(Long id);

    List<Payment> getPaymentsByStatus(String status);

    List<Payment> getAllPayments();

    Optional<Payment> getPaymentByIdempotencyKey(String key);

    PaymentStatsResponse getPaymentStats();

    /**
     * Fail a payment with a specific error code and optional technical reason.
     * Automatically generates a user-friendly error message based on the error code.
     * 
     * @param paymentId The ID of the payment to fail
     * @param errorCode The error code indicating the failure reason
     * @param technicalReason Optional technical error reason (for logging/debugging)
     * @return The failed payment with error details
     * @throws PaymentNotFoundException if payment doesn't exist
     * @throws InvalidStatusTransitionException if payment cannot transition to FAILED
     */
    Payment failPayment(Long paymentId, String errorCode, String technicalReason);

}