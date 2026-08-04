package com.neueda.service;

import com.neueda.model.Payment;
import com.neueda.model.PaymentStatus;

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

    List<Payment> getAllPayments();

    Optional<Payment> getPaymentByIdempotencyKey(String key);

}