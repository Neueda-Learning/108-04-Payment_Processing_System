package com.neueda.repository;

import java.util.*;
import com.neueda.model.Payment;

public interface PaymentRepositoryInterface {
        Payment save(Payment payment);

        Optional<Payment> findById(Long id);

        List<Payment> findAll();

        Optional<Payment> findByIdempotencyKey(String key);
    
}
