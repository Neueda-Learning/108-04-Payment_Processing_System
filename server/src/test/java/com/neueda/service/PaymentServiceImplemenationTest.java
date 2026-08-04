package com.neueda.service;

import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.InvalidStatusTransitionException;
import com.neueda.exception.PaymentNotFoundException;
import com.neueda.exception.ValidationException;
import com.neueda.model.Payment;
import com.neueda.model.PaymentHistory;
import com.neueda.model.PaymentStatus;
import com.neueda.repository.PaymentHistoryRepository;
import com.neueda.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentServiceImplemenationTest {

    private InMemoryPaymentRepository paymentRepository;
    private InMemoryHistoryRepository historyRepository;
    private PaymentServiceImplemenation service;

    @BeforeEach
    void setUp() {
        paymentRepository = new InMemoryPaymentRepository();
        historyRepository = new InMemoryHistoryRepository();
        service = new PaymentServiceImplemenation(paymentRepository, historyRepository);
    }

    @Nested
    class CreatePayment {

        @Test
        void happyPath_savesPaymentWithCreatedStatusAndRecordsHistory() {
            Payment payment = validPayment("idem-1");

            Payment result = service.createPayment(payment);

            assertAll(
                () -> assertNotNull(result.getId()),
                () -> assertEquals(PaymentStatus.CREATED.name(), result.getStatus()),
                () -> assertEquals(1, historyRepository.saved.size()),
                () -> assertEquals(PaymentStatus.CREATED.name(), historyRepository.saved.getFirst().getToStatus())
            );
        }

        @Test
        void forcesCreatedStatusEvenIfCallerPassesDifferentStatus() {
            Payment payment = validPayment("idem-force");
            payment.setStatus("COMPLETED");

            Payment result = service.createPayment(payment);

            assertEquals(PaymentStatus.CREATED.name(), result.getStatus());
        }

        @Test
        void duplicateIdempotencyKey_throwsDuplicatePaymentException() {
            service.createPayment(validPayment("idem-dup"));

            DuplicatePaymentException ex = assertThrows(DuplicatePaymentException.class,
                () -> service.createPayment(validPayment("idem-dup")));

            assertEquals("idem-dup", ex.getIdempotencyKey());
        }

        @Test
        void invalidAmount_throwsValidationException() {
            Payment payment = validPayment("idem-bad-amount");
            payment.setAmount(new BigDecimal("-1.00"));

            assertThrows(ValidationException.class, () -> service.createPayment(payment));
        }

        @Test
        void nullAmount_throwsValidationException() {
            Payment payment = validPayment("idem-null-amount");
            payment.setAmount(null);

            assertThrows(ValidationException.class, () -> service.createPayment(payment));
        }

        @Test
        void unsupportedCurrency_throwsValidationException() {
            Payment payment = validPayment("idem-cur");
            payment.setCurrency("BTC");

            assertThrows(ValidationException.class, () -> service.createPayment(payment));
        }

        @Test
        void sameSourceAndDestinationAccount_throwsValidationException() {
            Payment payment = validPayment("idem-same-acc");
            payment.setSourceAccount("ACC12345");
            payment.setDestinationAccount("ACC12345");

            assertThrows(ValidationException.class, () -> service.createPayment(payment));
        }
    }

    @Nested
    class TransitionStatus {

        @Test
        void createdToValidated_succeedsAndRecordsHistory() {
            Payment payment = service.createPayment(validPayment("idem-tr-1"));

            Payment result = service.transitionStatus(payment.getId(), PaymentStatus.VALIDATED);

            assertAll(
                () -> assertEquals(PaymentStatus.VALIDATED.name(), result.getStatus()),
                () -> assertEquals(2, historyRepository.saved.size()),
                () -> assertEquals(PaymentStatus.VALIDATED.name(), historyRepository.saved.getLast().getToStatus()),
                () -> assertEquals(PaymentStatus.CREATED.name(), historyRepository.saved.getLast().getFromStatus())
            );
        }

        @Test
        void invalidTransition_createdToCompleted_throwsInvalidStatusTransitionException() {
            Payment payment = service.createPayment(validPayment("idem-inv-1"));

            InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> service.transitionStatus(payment.getId(), PaymentStatus.COMPLETED));

            assertAll(
                () -> assertEquals(PaymentStatus.CREATED, ex.getFromStatus()),
                () -> assertEquals(PaymentStatus.COMPLETED, ex.getToStatus())
            );
        }

        @Test
        void nonExistentPayment_throwsPaymentNotFoundException() {
            assertThrows(PaymentNotFoundException.class,
                () -> service.transitionStatus(99999L, PaymentStatus.VALIDATED));
        }
    }

    @Test
    void getPaymentByIdReturnsRepositoryResult() {
        Payment saved = service.createPayment(validPayment("idem-get-1"));

        Optional<Payment> result = service.getPaymentById(saved.getId());

        assertEquals(Optional.of(saved), result);
    }

    @Test
    void getPaymentHistoryReturnsPersistedHistoryForExistingPayment() {
        Payment saved = service.createPayment(validPayment("idem-history-1"));
        service.transitionStatus(saved.getId(), PaymentStatus.VALIDATED);

        List<PaymentHistory> history = service.getPaymentHistory(saved.getId());

        assertAll(
            () -> assertEquals(2, history.size()),
            () -> assertEquals(PaymentStatus.CREATED.name(), history.getFirst().getToStatus()),
            () -> assertEquals(PaymentStatus.VALIDATED.name(), history.getLast().getToStatus())
        );
    }

    @Test
    void getPaymentHistoryThrowsWhenPaymentDoesNotExist() {
        assertThrows(PaymentNotFoundException.class, () -> service.getPaymentHistory(99999L));
    }

    @Test
    void getPaymentsByStatusReturnsFilteredPayments() {
        service.createPayment(validPayment("idem-status-1"));
        Payment validated = service.createPayment(validPayment("idem-status-2"));
        service.transitionStatus(validated.getId(), PaymentStatus.VALIDATED);

        List<Payment> createdPayments = service.getPaymentsByStatus("created");
        List<Payment> validatedPayments = service.getPaymentsByStatus("VALIDATED");

        assertAll(
            () -> assertEquals(1, createdPayments.size()),
            () -> assertEquals(1, validatedPayments.size()),
            () -> assertEquals(PaymentStatus.CREATED.name(), createdPayments.getFirst().getStatus()),
            () -> assertEquals(PaymentStatus.VALIDATED.name(), validatedPayments.getFirst().getStatus())
        );
    }

    @Test
    void getPaymentsByStatusRejectsNullBlankAndUnsupportedStatuses() {
        assertThrows(ValidationException.class, () -> service.getPaymentsByStatus(null));
        assertThrows(ValidationException.class, () -> service.getPaymentsByStatus("   "));
        assertThrows(ValidationException.class, () -> service.getPaymentsByStatus("UNKNOWN"));
    }

    @Test
    void getAllPaymentsReturnsRepositoryList() {
        service.createPayment(validPayment("idem-all-1"));
        service.createPayment(validPayment("idem-all-2"));

        assertEquals(2, service.getAllPayments().size());
    }

    @Test
    void getPaymentByIdempotencyKeyReturnsRepositoryResult() {
        service.createPayment(validPayment("idem-5"));

        Optional<Payment> result = service.getPaymentByIdempotencyKey("idem-5");

        assertEquals("idem-5", result.orElseThrow().getIdempotencyKey());
    }

    private static Payment validPayment(String key) {
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("50.00"));
        payment.setSourceAccount("SRC12345");
        payment.setDestinationAccount("DST12345");
        payment.setIdempotencyKey(key);
        payment.setCurrency("USD");
        return payment;
    }

    private static class InMemoryPaymentRepository implements PaymentRepository {
        private final List<Payment> store = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public Payment save(Payment payment) {
            payment.setId(nextId++);
            store.add(payment);
            return payment;
        }

        @Override
        public Optional<Payment> findById(Long id) {
            return store.stream().filter(p -> id.equals(p.getId())).findFirst();
        }

        @Override
        public List<Payment> findAll() {
            return List.copyOf(store);
        }

        @Override
        public Optional<Payment> findByIdempotencyKey(String key) {
            return store.stream().filter(p -> key != null && key.equals(p.getIdempotencyKey())).findFirst();
        }

        @Override
        public List<Payment> findAllByStatus(String status) {
            return store.stream().filter(p -> status.equals(p.getStatus())).toList();
        }

        @Override
        public void updateStatus(Long id, String status) {
            findById(id).ifPresent(payment -> payment.setStatus(status));
        }

        @Override
        public void updatePayment(Payment payment) {
            store.removeIf(existing -> payment.getId().equals(existing.getId()));
            store.add(payment);
        }
    }

    private static class InMemoryHistoryRepository implements PaymentHistoryRepository {
        private final List<PaymentHistory> saved = new ArrayList<>();

        @Override
        public PaymentHistory save(PaymentHistory history) {
            saved.add(history);
            return history;
        }

        @Override
        public List<PaymentHistory> findByPaymentId(Long paymentId) {
            return saved.stream().filter(h -> paymentId.equals(h.getPaymentId())).toList();
        }
    }
}
