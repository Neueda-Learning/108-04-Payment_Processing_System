package com.neueda.repository;

import java.util.*;
import com.neueda.model.Payment;

public interface PaymentRepository {
        Payment save(Payment payment);

        Optional<Payment> findById(Long id);

        List<Payment> findAll();

        Optional<Payment> findByIdempotencyKey(String key);

        List<Payment> findAllByStatus(String status);

        void updateStatus(Long id, String status);
        
        void updatePayment(Payment payment);
    
}


