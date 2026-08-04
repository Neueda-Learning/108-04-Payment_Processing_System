package com.neueda.controller;

import com.neueda.dto.UpdatePaymentStatusRequest;
import com.neueda.exception.ValidationException;
import com.neueda.model.ErrorCode;
import com.neueda.model.Payment;
import com.neueda.model.PaymentStatus;
import com.neueda.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.createPayment(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getPayments(@RequestParam(required = false) String status) {
        List<Payment> payments = status == null || status.isBlank()
                ? paymentService.getAllPayments()
                : paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Long id,
                                                       @RequestBody UpdatePaymentStatusRequest request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED, "Status cannot be null or blank");
        }

        final PaymentStatus targetStatus;
        try {
            targetStatus = PaymentStatus.valueOf(request.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(ErrorCode.VALIDATION_FAILED, "Unsupported payment status: " + request.getStatus());
        }

        return ResponseEntity.ok(paymentService.transitionStatus(id, targetStatus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

