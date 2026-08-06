package com.neueda.repository;

import java.util.List;
import com.neueda.model.PaymentHistory;

public interface PaymentHistoryRepository {
    
    PaymentHistory save(PaymentHistory history);
    
    List<PaymentHistory> findByPaymentId(Long paymentId);
}

