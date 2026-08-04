package com.neueda.service;

import com.neueda.model.Payment;
import com.neueda.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PaymentServiceImplemenationTest {

    @Test
    void createPaymentDelegatesToRepositorySave() {
        InMemoryPaymentRepository repository = new InMemoryPaymentRepository();
        PaymentServiceImplemenation service = new PaymentServiceImplemenation(repository);
        Payment payment = payment(1L, "idem-1");

        Payment created = service.createPayment(payment);

        assertSame(payment, repository.savedPayment);
        assertSame(payment, created);
    }

    @Test
    void getPaymentByIdReturnsRepositoryResult() {
        Payment expected = payment(2L, "idem-2");
        InMemoryPaymentRepository repository = new InMemoryPaymentRepository();
        repository.paymentById = Optional.of(expected);
        PaymentServiceImplemenation service = new PaymentServiceImplemenation(repository);

        Optional<Payment> result = service.getPaymentById(2L);

        assertEquals(Optional.of(expected), result);
        assertEquals(2L, repository.lastRequestedId);
    }

    @Test
    void getAllPaymentsReturnsRepositoryList() {
        List<Payment> expected = List.of(payment(3L, "idem-3"), payment(4L, "idem-4"));
        InMemoryPaymentRepository repository = new InMemoryPaymentRepository();
        repository.allPayments = new ArrayList<>(expected);
        PaymentServiceImplemenation service = new PaymentServiceImplemenation(repository);

        List<Payment> result = service.getAllPayments();

        assertIterableEquals(expected, result);
    }

    @Test
    void getPaymentByIdempotencyKeyReturnsRepositoryResult() {
        Payment expected = payment(5L, "idem-5");
        InMemoryPaymentRepository repository = new InMemoryPaymentRepository();
        repository.paymentByKey = Optional.of(expected);
        PaymentServiceImplemenation service = new PaymentServiceImplemenation(repository);

        Optional<Payment> result = service.getPaymentByIdempotencyKey("idem-5");

        assertEquals(Optional.of(expected), result);
        assertEquals("idem-5", repository.lastRequestedKey);
    }

    private static Payment payment(Long id, String key) {
        return new Payment(id, new BigDecimal("50.00"), "CREATED", "SRC12345", "DST12345", key);
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        private Payment savedPayment;
        private Optional<Payment> paymentById = Optional.empty();
        private Optional<Payment> paymentByKey = Optional.empty();
        private List<Payment> allPayments = List.of();
        private Long lastRequestedId;
        private String lastRequestedKey;

        @Override
        public Payment save(Payment payment) {
            this.savedPayment = payment;
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long id) {
            this.lastRequestedId = id;
            return paymentById;
        }

        @Override
        public List<Payment> findAll() {
            return allPayments;
        }

        @Override
        public Optional<Payment> findByIdempotencyKey(String key) {
            this.lastRequestedKey = key;
            return paymentByKey;
        }

        @Override
        public List<Payment> findAllByStatus(String status) {
            return List.of();
        }

        @Override
        public void updateStatus(Long id, String status) {
        }

        @Override
        public void updatePayment(Payment payment) {
        }
    }
}

