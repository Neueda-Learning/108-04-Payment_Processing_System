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
import java.util.Comparator;
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
        void happyPath_savesPaymentAndRunsFullLifecycleToCompleted() {
            // createPayment() drives the payment synchronously through the whole
            // CREATED -> VALIDATED -> SENT -> COMPLETED lifecycle (broadcasting each
            // step over websockets for the frontend's progress stepper), so a
            // successful call returns a COMPLETED payment with 4 history entries.
            Payment payment = validPayment("idem-1");

            Payment result = service.createPayment(payment);

            assertAll(
                () -> assertNotNull(result.getId()),
                () -> assertEquals(PaymentStatus.COMPLETED.name(), result.getStatus()),
                () -> assertEquals(4, historyRepository.saved.size()),
                () -> assertEquals(PaymentStatus.CREATED.name(), historyRepository.saved.getFirst().getToStatus()),
                () -> assertEquals(PaymentStatus.COMPLETED.name(), historyRepository.saved.getLast().getToStatus())
            );
        }

        @Test
        void forcesCreatedStatusEvenIfCallerPassesDifferentStatus() {
            Payment payment = validPayment("idem-force");
            payment.setStatus("COMPLETED");

            service.createPayment(payment);

            // Regardless of the final (auto-completed) status, the very first
            // persisted state must always be CREATED, ignoring caller input.
            assertEquals(PaymentStatus.CREATED.name(), historyRepository.saved.getFirst().getToStatus());
        }

        @Test
        void duplicateIdempotencyKey_throwsDuplicatePaymentException() {
            service.createPayment(validPayment("idem-dup"));

            DuplicatePaymentException ex = assertThrows(DuplicatePaymentException.class,
                () -> service.createPayment(validPayment("idem-dup")));

            assertEquals("idem-dup", ex.getIdempotencyKey());
        }

        @Test
        void invalidAmount_resultsInFailedPayment() {
            // createPayment() catches validation failures internally and returns a
            // FAILED payment (CREATED -> FAILED) instead of throwing, so the caller
            // always gets a response back rather than an HTTP 500.
            Payment payment = validPayment("idem-bad-amount");
            payment.setAmount(new BigDecimal("-1.00"));

            Payment result = service.createPayment(payment);

            assertAll(
                () -> assertEquals(PaymentStatus.FAILED.name(), result.getStatus()),
                () -> assertEquals("VALIDATION_FAILED", result.getErrorCode())
            );
        }

        @Test
        void nullAmount_resultsInFailedPayment() {
            Payment payment = validPayment("idem-null-amount");
            payment.setAmount(null);

            Payment result = service.createPayment(payment);

            assertAll(
                () -> assertEquals(PaymentStatus.FAILED.name(), result.getStatus()),
                () -> assertEquals("VALIDATION_FAILED", result.getErrorCode())
            );
        }

        @Test
        void unsupportedCurrency_resultsInFailedPayment() {
            Payment payment = validPayment("idem-cur");
            payment.setCurrency("BTC");

            Payment result = service.createPayment(payment);

            assertAll(
                () -> assertEquals(PaymentStatus.FAILED.name(), result.getStatus()),
                () -> assertEquals("VALIDATION_FAILED", result.getErrorCode())
            );
        }

        @Test
        void sameSourceAndDestinationAccount_resultsInFailedPayment() {
            Payment payment = validPayment("idem-same-acc");
            payment.setSourceAccount("ACC12345");
            payment.setDestinationAccount("ACC12345");

            Payment result = service.createPayment(payment);

            assertAll(
                () -> assertEquals(PaymentStatus.FAILED.name(), result.getStatus()),
                () -> assertEquals("VALIDATION_FAILED", result.getErrorCode())
            );
        }
    }

    @Nested
    class TransitionStatus {

        // NOTE: createPayment() now drives a successful payment synchronously all
        // the way to COMPLETED, so these tests seed a payment directly into the
        // repository at the desired starting status rather than going through
        // createPayment(), in order to exercise transitionStatus() in isolation.

        @Test
        void createdToValidated_succeedsAndRecordsHistory() {
            Payment payment = paymentRepository.save(validPayment("idem-tr-1"));

            Payment result = service.transitionStatus(payment.getId(), PaymentStatus.VALIDATED);

            assertAll(
                () -> assertEquals(PaymentStatus.VALIDATED.name(), result.getStatus()),
                () -> assertEquals(1, historyRepository.saved.size()),
                () -> assertEquals(PaymentStatus.VALIDATED.name(), historyRepository.saved.getLast().getToStatus()),
                () -> assertEquals(PaymentStatus.CREATED.name(), historyRepository.saved.getLast().getFromStatus())
            );
        }

        @Test
        void invalidTransition_createdToCompleted_throwsInvalidStatusTransitionException() {
            Payment payment = paymentRepository.save(validPayment("idem-inv-1"));

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

        @Test
        void completingPayment_transfersFundsBetweenAccounts() {
            InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
            accountRepository.save(new com.neueda.model.Account(
                    null, "SRC12345", "Source Holder", "USD", new BigDecimal("500.00"), "ACTIVE"));
            accountRepository.save(new com.neueda.model.Account(
                    null, "DST12345", "Dest Holder", "USD", new BigDecimal("100.00"), "ACTIVE"));

            PaymentServiceImplemenation serviceWithAccounts = new PaymentServiceImplemenation(
                    paymentRepository, historyRepository, null, accountRepository);

            Payment sentPayment = validPayment("idem-transfer-1");
            sentPayment.setStatus(PaymentStatus.SENT.name());
            paymentRepository.save(sentPayment);

            Payment result = serviceWithAccounts.transitionStatus(sentPayment.getId(), PaymentStatus.COMPLETED);

            assertAll(
                () -> assertEquals(PaymentStatus.COMPLETED.name(), result.getStatus()),
                () -> assertEquals(new BigDecimal("450.00"),
                        accountRepository.findByAccountNumber("SRC12345").orElseThrow().getBalance()),
                () -> assertEquals(new BigDecimal("150.00"),
                        accountRepository.findByAccountNumber("DST12345").orElseThrow().getBalance())
            );
        }
    }

    @Nested
    class GetDashboardStats {

        @Test
        void groupsPaymentsByStatusCurrencyAndDay() {
            LocalDate day = LocalDate.of(2026, 7, 15);
            seedPayment("idem-d1", "USD", PaymentStatus.COMPLETED.name(), "50.00", day.atTime(9, 0));
            seedPayment("idem-d2", "USD", PaymentStatus.COMPLETED.name(), "25.00", day.atTime(10, 0));
            seedPayment("idem-d3", "EUR", PaymentStatus.FAILED.name(), "10.00", day.atTime(11, 0));

            DashboardStatsResponse stats = service.getDashboardStats(day.minusDays(1), day.plusDays(1));

            assertAll(
                () -> assertEquals(2, stats.getStatusDistribution().size()),
                () -> assertEquals(1, stats.getVolumeOverTime().size()),
                () -> assertEquals(3, stats.getVolumeOverTime().getFirst().count()),
                () -> assertEquals(new BigDecimal("85.00"), stats.getVolumeOverTime().getFirst().totalAmount()),
                () -> assertEquals(2, stats.getCurrencyBreakdown().size())
            );
        }

        @Test
        void groupsFailedPaymentsByErrorCode() {
            LocalDate day = LocalDate.of(2026, 7, 15);
            Payment failed = seedPayment("idem-f1", "USD", PaymentStatus.FAILED.name(), "10.00", day.atTime(9, 0));
            failed.setErrorCode("INSUFFICIENT_FUNDS");
            paymentRepository.updatePayment(failed);

            DashboardStatsResponse stats = service.getDashboardStats(day, day);

            assertAll(
                () -> assertEquals(1, stats.getFailureReasons().size()),
                () -> assertEquals("INSUFFICIENT_FUNDS", stats.getFailureReasons().getFirst().errorCode()),
                () -> assertEquals(1, stats.getFailureReasons().getFirst().count())
            );
        }

        @Test
        void computesSuccessRatePerDay() {
            LocalDate day = LocalDate.of(2026, 7, 15);
            seedPayment("idem-s1", "USD", PaymentStatus.COMPLETED.name(), "10.00", day.atTime(9, 0));
            seedPayment("idem-s2", "USD", PaymentStatus.FAILED.name(), "10.00", day.atTime(10, 0));

            DashboardStatsResponse stats = service.getDashboardStats(day, day);

            assertAll(
                () -> assertEquals(1, stats.getSuccessRateOverTime().size()),
                () -> assertEquals(50.0, stats.getSuccessRateOverTime().getFirst().successRate())
            );
        }

        @Test
        void computesAverageStageDurationFromHistory() {
            Payment payment = seedPayment("idem-stage-1", "USD", PaymentStatus.COMPLETED.name(), "10.00",
                    LocalDateTime.of(2026, 7, 15, 9, 0));
            historyRepository.save(new PaymentHistory(payment.getId(), null, "CREATED",
                    LocalDateTime.of(2026, 7, 15, 9, 0, 0), "created"));
            historyRepository.save(new PaymentHistory(payment.getId(), "CREATED", "VALIDATED",
                    LocalDateTime.of(2026, 7, 15, 9, 0, 5), "validated"));

            DashboardStatsResponse stats = service.getDashboardStats(
                    LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15));

            assertAll(
                () -> assertEquals(1, stats.getAvgStageDuration().size()),
                () -> assertEquals("CREATED_TO_VALIDATED", stats.getAvgStageDuration().getFirst().stage()),
                () -> assertEquals(5.0, stats.getAvgStageDuration().getFirst().avgSeconds())
            );
        }

        @Test
        void defaultsToLast30DaysWhenNoRangeGiven() {
            DashboardStatsResponse stats = service.getDashboardStats(null, null);

            assertAll(
                () -> assertEquals(LocalDate.now(), stats.getTo()),
                () -> assertEquals(LocalDate.now().minusDays(29), stats.getFrom())
            );
        }

        @Test
        void emptyRangeReturnsEmptySections() {
            DashboardStatsResponse stats = service.getDashboardStats(
                    LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2));

            assertAll(
                () -> assertEquals(0, stats.getStatusDistribution().size()),
                () -> assertEquals(0, stats.getVolumeOverTime().size()),
                () -> assertEquals(0, stats.getFailureReasons().size()),
                () -> assertEquals(0, stats.getAvgStageDuration().size()),
                () -> assertEquals(0, stats.getSuccessRateOverTime().size()),
                () -> assertEquals(0, stats.getCurrencyBreakdown().size())
            );
        }

        private Payment seedPayment(String idempotencyKey, String currency, String status,
                                     String amount, LocalDateTime createdAt) {
            Payment payment = new Payment();
            payment.setAmount(new BigDecimal(amount));
            payment.setSourceAccount("SRC12345");
            payment.setDestinationAccount("DST12345");
            payment.setIdempotencyKey(idempotencyKey);
            payment.setCurrency(currency);
            payment.setStatus(status);
            payment.setCreatedAt(createdAt);
            return paymentRepository.save(payment);
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

        List<PaymentHistory> history = service.getPaymentHistory(saved.getId());

        assertAll(
            () -> assertEquals(4, history.size()),
            () -> assertEquals(PaymentStatus.COMPLETED.name(), history.getFirst().getToStatus()),
            () -> assertEquals(PaymentStatus.CREATED.name(), history.getLast().getToStatus())
        );
    }

    @Test
    void getPaymentHistoryThrowsWhenPaymentDoesNotExist() {
        assertThrows(PaymentNotFoundException.class, () -> service.getPaymentHistory(99999L));
    }

    @Test
    void getPaymentsByStatusReturnsFilteredPayments() {
        Payment created = validPayment("idem-status-1");
        created.setStatus(PaymentStatus.CREATED.name());
        paymentRepository.save(created);

        Payment validated = validPayment("idem-status-2");
        validated.setStatus(PaymentStatus.VALIDATED.name());
        paymentRepository.save(validated);

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
        Payment completed = validPayment("idem-stats-1");
        completed.setStatus(PaymentStatus.COMPLETED.name());
        paymentRepository.save(completed);

        Payment failed = validPayment("idem-stats-2");
        failed.setStatus(PaymentStatus.FAILED.name());
        paymentRepository.save(failed);

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
        payment.setStatus(PaymentStatus.CREATED.name());
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

        @Override
        public void updatePaymentWithError(Long id, String errorCode, String userFriendlyMessage) {
            findById(id).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED.name());
                payment.setErrorCode(errorCode);
                payment.setDescription(userFriendlyMessage);
            });
        }
    }

    private static class InMemoryAccountRepository implements com.neueda.repository.AccountRepository {
        private final List<com.neueda.model.Account> accounts = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public com.neueda.model.Account save(com.neueda.model.Account account) {
            if (account.getId() == null) {
                account.setId(nextId++);
            }
            accounts.add(account);
            return account;
        }

        @Override
        public Optional<com.neueda.model.Account> findById(Long id) {
            return accounts.stream().filter(a -> id.equals(a.getId())).findFirst();
        }

        @Override
        public Optional<com.neueda.model.Account> findByAccountNumber(String accountNumber) {
            return accounts.stream().filter(a -> accountNumber.equals(a.getAccountNumber())).findFirst();
        }

        @Override
        public List<com.neueda.model.Account> findAll() {
            return List.copyOf(accounts);
        }

        @Override
        public void updateBalance(String accountNumber, BigDecimal newBalance) {
            findByAccountNumber(accountNumber).ifPresent(a -> a.setBalance(newBalance));
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
            return saved.stream()
                    .sorted(Comparator.comparing(PaymentHistory::getPaymentId)
                            .thenComparing(PaymentHistory::getTimestamp))
                    .toList();
        }
    }
}
