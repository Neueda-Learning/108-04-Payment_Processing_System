package com.neueda.service;

import com.neueda.dto.PaymentEvent;
import com.neueda.model.Payment;
import com.neueda.model.PaymentHistory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public PaymentNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendPaymentUpdate(Payment payment, PaymentHistory history) {
        PaymentEvent event = new PaymentEvent(
                history.getPaymentId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getTimestamp(),
                history.getNotes(),
                payment.getErrorCode()
        );

        messagingTemplate.convertAndSend("/topic/payment/" + history.getPaymentId(), event);
        if (payment.getIdempotencyKey() != null) {
            messagingTemplate.convertAndSend("/topic/payment/" + payment.getIdempotencyKey(), event);
        }
    }
}
