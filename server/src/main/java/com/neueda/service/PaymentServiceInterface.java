package com.neueda.service;

import com.neueda.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentServiceInterface {

    Payment createPayment(Payment payment);

    Optional<Payment> getPaymentById(Long id);

    List<Payment> getAllPayments();

    Optional<Payment> getPaymentByIdempotencyKey(String key);

}