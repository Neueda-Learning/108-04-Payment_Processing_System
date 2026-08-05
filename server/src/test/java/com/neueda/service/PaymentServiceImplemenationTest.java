package com.neueda.service;

import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.InvalidStatusTransitionException;
import com.neueda.exception.PaymentNotFoundException;
import com.neueda.exception.ValidationException;
import com.neueda.model.Payment;
import com.neueda.model.PaymentHistory;
import com.neueda.model.PaymentStatus;
import com.neueda.dto.PaymentStatsResponse;
import com.neueda.dto.DashboardStatsResponse;
import com.neueda.repository.PaymentHistoryRepository;
import com.neueda.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            () -> assertEquals(PaymentStatus.VALIDATED.name(), history.getFirst().getToStatus()),
            () -> assertEquals(PaymentStatus.CREATED.name(), history.getLast().getToStatus())
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

    @Test
    void getPaymentStatsReturnsAggregatesForPayments() {
        Payment completed = service.createPayment(validPayment("idem-stats-1"));
        completed.setStatus(PaymentStatus.COMPLETED.name());
        paymentRepository.updatePayment(completed);

        Payment failed = service.createPayment(validPayment("idem-stats-2"));
        service.transitionStatus(failed.getId(), PaymentStatus.FAILED);

        PaymentStatsResponse stats = service.getPaymentStats();

        assertAll(
            () -> assertEquals(2L, stats.getTotalPayments()),
            () -> assertEquals(1L, stats.getSuccessfulPayments()),
            () -> assertEquals(1L, stats.getFailedPayments()),
            () -> assertEquals(new BigDecimal("100.00"), stats.getTotalAmount())
        );
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

    private Payment savePaymentWithCreatedAt(String currency, String status, String errorCode, LocalDateTime createdAt) {
        Payment payment = validPayment("idem-dash-" + System.nanoTime());
        payment.setCurrency(currency);
        Payment saved = paymentRepository.save(payment);
        saved.setStatus(status);
        saved.setErrorCode(errorCode);
        saved.setCreatedAt(createdAt);
        paymentRepository.updatePayment(saved);
        return saved;
    }

    @Nested
    class GetDashboardStats {

        @Test
        void aggregatesStatusCurrencyAndFailureSections() {
            LocalDate day = LocalDate.of(2026, 8, 1);
            LocalDateTime createdAt = day.atTime(10, 0);

            savePaymentWithCreatedAt("USD", PaymentStatus.COMPLETED.name(), null, createdAt);
            savePaymentWithCreatedAt("EUR", PaymentStatus.FAILED.name(), "INSUFFICIENT_FUNDS", createdAt);
            savePaymentWithCreatedAt("USD", PaymentStatus.CREATED.name(), null, createdAt);

            DashboardStatsResponse stats = service.getDashboardStats(day, day);

            assertEquals(day, stats.getFrom());
            assertEquals(day, stats.getTo());

            Map<String, Long> statusCounts = stats.getStatusDistribution().stream()
                    .collect(Collectors.toMap(DashboardStatsResponse.StatusCount::status,
                            DashboardStatsResponse.StatusCount::count));
            assertEquals(1L, statusCounts.get(PaymentStatus.COMPLETED.name()));
            assertEquals(1L, statusCounts.get(PaymentStatus.FAILED.name()));
            assertEquals(1L, statusCounts.get(PaymentStatus.CREATED.name()));

            Map<String, Long> currencyCounts = stats.getCurrencyBreakdown().stream()
                    .collect(Collectors.toMap(DashboardStatsResponse.CurrencyBreakdown::currency,
                            DashboardStatsResponse.CurrencyBreakdown::count));
            assertEquals(2L, currencyCounts.get("USD"));
            assertEquals(1L, currencyCounts.get("EUR"));

            assertEquals(1, stats.getFailureReasons().size());
            assertEquals("INSUFFICIENT_FUNDS", stats.getFailureReasons().get(0).errorCode());
            assertEquals(1L, stats.getFailureReasons().get(0).count());

            assertEquals(1, stats.getVolumeOverTime().size());
            assertEquals(3, stats.getVolumeOverTime().get(0).count());
        }

        @Test
        void unknownErrorCodeBucketedAsUnknown() {
            LocalDate day = LocalDate.of(2026, 8, 2);
            savePaymentWithCreatedAt("USD", PaymentStatus.FAILED.name(), null, day.atTime(9, 0));

            DashboardStatsResponse stats = service.getDashboardStats(day, day);

            assertEquals("UNKNOWN", stats.getFailureReasons().get(0).errorCode());
        }

        @Test
        void computesAverageStageDurationFromHistory() {
            LocalDateTime t0 = LocalDate.of(2026, 8, 3).atTime(8, 0);
            Long paymentId = savePaymentWithCreatedAt("USD", PaymentStatus.COMPLETED.name(), null, t0).getId();

            historyRepository.save(new PaymentHistory(paymentId, null, PaymentStatus.CREATED.name(), t0, "created"));
            historyRepository.save(new PaymentHistory(paymentId, PaymentStatus.CREATED.name(),
                    PaymentStatus.VALIDATED.name(), t0.plusSeconds(10), "validated"));
            historyRepository.save(new PaymentHistory(paymentId, PaymentStatus.VALIDATED.name(),
                    PaymentStatus.SENT.name(), t0.plusSeconds(30), "sent"));
            historyRepository.save(new PaymentHistory(paymentId, PaymentStatus.SENT.name(),
                    PaymentStatus.COMPLETED.name(), t0.plusSeconds(90), "completed"));

            DashboardStatsResponse stats = service.getDashboardStats(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 3));

            Map<String, Double> stageDurations = stats.getAvgStageDuration().stream()
                    .collect(Collectors.toMap(DashboardStatsResponse.StageDuration::stage,
                            DashboardStatsResponse.StageDuration::avgSeconds));

            assertEquals(10.0, stageDurations.get("CREATED_TO_VALIDATED"));
            assertEquals(20.0, stageDurations.get("VALIDATED_TO_SENT"));
            assertEquals(60.0, stageDurations.get("SENT_TO_COMPLETED"));
            assertEquals(0.0, stageDurations.get("SENT_TO_FAILED"));
        }

        @Test
        void excludesPaymentsOutsideDateRange() {
            savePaymentWithCreatedAt("USD", PaymentStatus.COMPLETED.name(), null,
                    LocalDate.of(2026, 1, 1).atTime(12, 0));

            DashboardStatsResponse stats = service.getDashboardStats(
                    LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2));

            assertEquals(0, stats.getStatusDistribution().size());
            assertEquals(0, stats.getVolumeOverTime().size());
        }

        @Test
        void defaultsToLast30DaysWhenNoRangeProvided() {
            DashboardStatsResponse stats = service.getDashboardStats(null, null);

            assertEquals(LocalDate.now(), stats.getTo());
            assertEquals(LocalDate.now().minusDays(30), stats.getFrom());
        }
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

        @Override
        public void updatePaymentWithError(Long id, String errorCode, String userFriendlyMessage) {
            findById(id).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED.name());
                payment.setErrorCode(errorCode);
                payment.setDescription(userFriendlyMessage);
            });
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
            return saved.stream()
                    .filter(h -> paymentId.equals(h.getPaymentId()))
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))  // Most recent first
                    .toList();
        }

        @Override
        public List<PaymentHistory> findAll() {
            return List.copyOf(saved);
        }
    }
}
