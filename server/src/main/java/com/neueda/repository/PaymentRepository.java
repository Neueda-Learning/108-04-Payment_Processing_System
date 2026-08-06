package com.neueda.repository;

import java.time.LocalDateTime;
import java.util.*;
import com.neueda.model.Payment;

public interface PaymentRepository {
        Payment save(Payment payment);

        Optional<Payment> findById(Long id);

        List<Payment> findAll();

        Optional<Payment> findByIdempotencyKey(String key);

        List<Payment> findAllByStatus(String status);

        List<Payment> findRecentByAccountAndWindow(String accountNumber, LocalDateTime windowStart);

        void updateStatus(Long id, String status);
        
        void updatePayment(Payment payment);

        /**
         * Update a payment with error details (error code and user-friendly message).
         * 
         * @param id The payment ID
         * @param errorCode The error code
         * @param userFriendlyMessage The user-friendly error message
         */
        void updatePaymentWithError(Long id, String errorCode, String userFriendlyMessage);
    
}


