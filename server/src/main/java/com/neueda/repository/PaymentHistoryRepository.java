package com.neueda.repository;

import java.util.List;
import com.neueda.model.PaymentHistory;

public interface PaymentHistoryRepository {
    
    PaymentHistory save(PaymentHistory history);
    
    List<PaymentHistory> findByPaymentId(Long paymentId);

    /**
     * Bulk fetch of all payment history rows, ordered by payment then time.
     * Used by dashboard analytics to compute stage durations without N+1 queries.
     */
    List<PaymentHistory> findAll();
}

