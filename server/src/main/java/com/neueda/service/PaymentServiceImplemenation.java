package com.neueda.service;

import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.InvalidStatusTransitionException;
import com.neueda.exception.PaymentNotFoundException;
import com.neueda.model.Payment;
import com.neueda.model.PaymentHistory;
import com.neueda.model.PaymentStatus;
import com.neueda.repository.PaymentHistoryRepository;
import com.neueda.repository.PaymentRepository;
import com.neueda.validator.PaymentValidator;
import com.neueda.dto.PaymentStatsResponse;
import com.neueda.dto.DashboardStatsResponse;
import com.neueda.util.ErrorMessageMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
public class PaymentServiceImplemenation implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository historyRepository;
    private final PaymentNotificationService notificationService;

    public PaymentServiceImplemenation(PaymentRepository paymentRepository,
                                       PaymentHistoryRepository historyRepository) {
        this(paymentRepository, historyRepository, null);
    }

    @Autowired
    public PaymentServiceImplemenation(PaymentRepository paymentRepository,
                                       PaymentHistoryRepository historyRepository,
                                       PaymentNotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
    }

    /**
     * Create a new payment.
     *
     * Rules enforced:
     * 1. Validate amount, accounts, currency, and idempotency key format.
     * 2. If a payment with the same idempotencyKey already exists, throw DuplicatePaymentException.
     * 3. Default status to CREATED regardless of what the caller sends.
     * 4. Persist the payment and record the initial CREATED history entry.
     */
    @Override
    public Payment createPayment(Payment payment) {

        // 1. Run field-level validation
        PaymentValidator.validateAmount(payment.getAmount());
        PaymentValidator.validateAccounts(payment.getSourceAccount(), payment.getDestinationAccount());
        PaymentValidator.validateCurrency(payment.getCurrency());
        PaymentValidator.validateIdempotencyKey(payment.getIdempotencyKey());

        // 2. Idempotency check — reject if the key is already known
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(payment.getIdempotencyKey());
        if (existing.isPresent()) {
            throw new DuplicatePaymentException(payment.getIdempotencyKey(), existing.get().getId());
        }

        // 3. Force initial status to CREATED (ignore whatever the caller supplied)
        payment.setStatus(PaymentStatus.CREATED.name());

        // 4. Persist and record the creation history entry
        final Payment saved;
        try {
            saved = paymentRepository.save(payment);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            Payment alreadySaved = paymentRepository.findByIdempotencyKey(payment.getIdempotencyKey())
                    .orElseThrow(() -> ex);
            throw new DuplicatePaymentException(payment.getIdempotencyKey(), alreadySaved.getId());
        }
        PaymentHistory history = historyRepository.save(new PaymentHistory(
                saved.getId(),
                null,               // no previous status
                PaymentStatus.CREATED.name(),
                LocalDateTime.now(),
                "Payment created"
        ));
        notifyPaymentUpdate(saved, history);

        return saved;
    }

    /**
     * Transition a payment to a new status.
     *
     * Rules enforced:
     * 1. Payment must exist.
     * 2. Transition must be permitted by the PaymentStatus state machine.
     * 3. Status is updated in the database.
     * 4. A history entry is written for every successful transition.
     */
    @Override
    public Payment transitionStatus(Long id, PaymentStatus targetStatus) {

        // 1. Load payment — throw 404 if not found
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        // 2. Validate the state machine transition
        PaymentStatus currentStatus = PaymentStatus.valueOf(payment.getStatus());
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, targetStatus);
        }

        // 3. Persist the new status
        paymentRepository.updateStatus(id, targetStatus.name());

        // 4. Record audit trail
        PaymentHistory history = historyRepository.save(new PaymentHistory(
                id,
                currentStatus.name(),
                targetStatus.name(),
                LocalDateTime.now(),
                "Status transitioned from " + currentStatus + " to " + targetStatus
        ));

        // Return the refreshed payment
        payment.setStatus(targetStatus.name());
        notifyPaymentUpdate(payment, history);
        return payment;
    }

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public List<PaymentHistory> getPaymentHistory(Long id) {
        paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return historyRepository.findByPaymentId(id);
    }

    @Override
    public List<Payment> getPaymentsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new com.neueda.exception.ValidationException(
                    com.neueda.model.ErrorCode.VALIDATION_FAILED,
                    "Status cannot be null or blank");
        }

        try {
            PaymentStatus normalized = PaymentStatus.valueOf(status.trim().toUpperCase());
            return paymentRepository.findAllByStatus(normalized.name());
        } catch (IllegalArgumentException ex) {
            throw new com.neueda.exception.ValidationException(
                    com.neueda.model.ErrorCode.VALIDATION_FAILED,
                    "Unsupported payment status: " + status);
        }
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> getPaymentByIdempotencyKey(String key) {
        return paymentRepository.findByIdempotencyKey(key);
    }

        @Override
        public PaymentStatsResponse getPaymentStats() {
        List<Payment> payments = paymentRepository.findAll();
        long totalPayments = payments.size();
        long successfulPayments = payments.stream()
            .filter(payment -> PaymentStatus.COMPLETED.name().equals(payment.getStatus()))
            .count();
        long failedPayments = payments.stream()
            .filter(payment -> PaymentStatus.FAILED.name().equals(payment.getStatus()))
            .count();

        BigDecimal totalAmount = payments.stream()
            .map(Payment::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        double successRate = totalPayments == 0 ? 0.0 : (successfulPayments * 100.0) / totalPayments;
        double failureRate = totalPayments == 0 ? 0.0 : (failedPayments * 100.0) / totalPayments;

        return new PaymentStatsResponse(
            totalPayments,
            successfulPayments,
            failedPayments,
            totalAmount,
            successRate,
            failureRate
        );
        }

    private static final List<String[]> STAGE_TRANSITIONS = List.of(
            new String[]{PaymentStatus.CREATED.name(), PaymentStatus.VALIDATED.name(), "CREATED_TO_VALIDATED"},
            new String[]{PaymentStatus.VALIDATED.name(), PaymentStatus.SENT.name(), "VALIDATED_TO_SENT"},
            new String[]{PaymentStatus.SENT.name(), PaymentStatus.COMPLETED.name(), "SENT_TO_COMPLETED"},
            new String[]{PaymentStatus.SENT.name(), PaymentStatus.FAILED.name(), "SENT_TO_FAILED"}
    );

    @Override
    public DashboardStatsResponse getDashboardStats(LocalDate from, LocalDate to) {
        LocalDate resolvedTo = to != null ? to : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(30);

        LocalDateTime rangeStart = resolvedFrom.atStartOfDay();
        LocalDateTime rangeEnd = resolvedTo.plusDays(1).atStartOfDay();

        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getCreatedAt() != null
                        && !p.getCreatedAt().isBefore(rangeStart)
                        && p.getCreatedAt().isBefore(rangeEnd))
                .toList();

        List<DashboardStatsResponse.StatusCount> statusDistribution = payments.stream()
                .collect(Collectors.groupingBy(Payment::getStatus, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new DashboardStatsResponse.StatusCount(e.getKey(), e.getValue()))
                .toList();

        Map<LocalDate, List<Payment>> byDay = payments.stream()
                .collect(Collectors.groupingBy(p -> p.getCreatedAt().toLocalDate(), TreeMap::new, Collectors.toList()));

        List<DashboardStatsResponse.VolumePoint> volumeOverTime = byDay.entrySet().stream()
                .map(e -> new DashboardStatsResponse.VolumePoint(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .map(Payment::getAmount)
                                .filter(a -> a != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();

        List<DashboardStatsResponse.SuccessRatePoint> successRateOverTime = byDay.entrySet().stream()
                .map(e -> {
                    long completed = e.getValue().stream()
                            .filter(p -> PaymentStatus.COMPLETED.name().equals(p.getStatus()))
                            .count();
                    long failed = e.getValue().stream()
                            .filter(p -> PaymentStatus.FAILED.name().equals(p.getStatus()))
                            .count();
                    long terminal = completed + failed;
                    double rate = terminal == 0 ? 0.0 : (completed * 100.0) / terminal;
                    return new DashboardStatsResponse.SuccessRatePoint(e.getKey(), rate);
                })
                .toList();

        List<DashboardStatsResponse.FailureReasonCount> failureReasons = payments.stream()
                .filter(p -> PaymentStatus.FAILED.name().equals(p.getStatus()))
                .collect(Collectors.groupingBy(
                        p -> p.getErrorCode() != null ? p.getErrorCode() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()))
                .entrySet().stream()
                .map(e -> new DashboardStatsResponse.FailureReasonCount(e.getKey(), e.getValue()))
                .toList();

        List<DashboardStatsResponse.CurrencyBreakdown> currencyBreakdown = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCurrency() != null ? p.getCurrency() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(e -> new DashboardStatsResponse.CurrencyBreakdown(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .map(Payment::getAmount)
                                .filter(a -> a != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();

        List<Long> paymentIdsInRange = payments.stream().map(Payment::getId).toList();
        Map<Long, List<PaymentHistory>> historyByPaymentId = historyRepository.findAll().stream()
                .filter(h -> paymentIdsInRange.contains(h.getPaymentId()))
                .collect(Collectors.groupingBy(PaymentHistory::getPaymentId));

        List<DashboardStatsResponse.StageDuration> avgStageDuration = new ArrayList<>();
        for (String[] transition : STAGE_TRANSITIONS) {
            String fromStatus = transition[0];
            String toStatus = transition[1];
            String label = transition[2];

            List<Double> durationsSeconds = new ArrayList<>();
            for (List<PaymentHistory> history : historyByPaymentId.values()) {
                List<PaymentHistory> sorted = history.stream()
                        .sorted(Comparator.comparing(PaymentHistory::getTimestamp))
                        .toList();
                LocalDateTime fromTimestamp = null;
                for (PaymentHistory entry : sorted) {
                    if (fromStatus.equals(entry.getFromStatus()) && toStatus.equals(entry.getToStatus())) {
                        fromTimestamp = findEntryTimestamp(sorted, fromStatus);
                        if (fromTimestamp != null) {
                            durationsSeconds.add(
                                    Duration.between(fromTimestamp, entry.getTimestamp()).toMillis() / 1000.0);
                        }
                    }
                }
            }

            double avgSeconds = durationsSeconds.isEmpty() ? 0.0
                    : durationsSeconds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            avgStageDuration.add(new DashboardStatsResponse.StageDuration(label, avgSeconds));
        }

        return new DashboardStatsResponse(
                resolvedFrom,
                resolvedTo,
                statusDistribution,
                volumeOverTime,
                failureReasons,
                avgStageDuration,
                successRateOverTime,
                currencyBreakdown
        );
    }

    /**
     * Find the timestamp at which a payment entered the given status, i.e. the
     * timestamp of the history entry whose toStatus equals the given status.
     */
    private LocalDateTime findEntryTimestamp(List<PaymentHistory> sortedHistory, String status) {
        return sortedHistory.stream()
                .filter(h -> status.equals(h.getToStatus()))
                .map(PaymentHistory::getTimestamp)
                .findFirst()
                .orElse(null);
    }

    /**
     * Fail a payment with a specific error code and optional technical reason.
     * 
     * @param paymentId The ID of the payment to fail
     * @param errorCodeString The error code as a string (e.g., "INSUFFICIENT_FUNDS")
     * @param technicalReason Optional technical reason for the failure
     * @return The failed payment
     * @throws PaymentNotFoundException if payment doesn't exist
     * @throws InvalidStatusTransitionException if payment cannot transition to FAILED
     */
    @Override
    public Payment failPayment(Long paymentId, String errorCodeString, String technicalReason) {
        // 1. Load payment — throw 404 if not found
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // 2. Validate the state machine transition
        PaymentStatus currentStatus = PaymentStatus.valueOf(payment.getStatus());
        if (!currentStatus.canTransitionTo(PaymentStatus.FAILED)) {
            throw new InvalidStatusTransitionException(currentStatus, PaymentStatus.FAILED);
        }

        // 3. Generate user-friendly message from error code
        String userFriendlyMessage = ErrorMessageMapping.getUserFriendlyMessageByString(errorCodeString);
        
        // 4. Update payment with error details
        paymentRepository.updatePaymentWithError(paymentId, errorCodeString, userFriendlyMessage);

        // 5. Record audit trail
        String historyNotes = technicalReason != null && !technicalReason.isBlank()
                ? technicalReason + " | User message: " + userFriendlyMessage
                : userFriendlyMessage;
        
        PaymentHistory history = historyRepository.save(new PaymentHistory(
                paymentId,
                currentStatus.name(),
                PaymentStatus.FAILED.name(),
                LocalDateTime.now(),
                historyNotes
        ));

        // 6. Return the updated payment
        payment.setStatus(PaymentStatus.FAILED.name());
        payment.setErrorCode(errorCodeString);
        payment.setDescription(userFriendlyMessage);
        payment.setUpdatedAt(LocalDateTime.now());
        notifyPaymentUpdate(payment, history);
        
        return payment;
    }

    private void notifyPaymentUpdate(Payment payment, PaymentHistory history) {
        if (notificationService != null) {
            notificationService.sendPaymentUpdate(payment, history);
        }
    }
}
