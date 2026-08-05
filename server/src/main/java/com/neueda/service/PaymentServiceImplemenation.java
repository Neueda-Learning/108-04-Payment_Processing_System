package com.neueda.service;

import com.neueda.exception.DuplicatePaymentException;
import com.neueda.exception.InvalidStatusTransitionException;
import com.neueda.exception.PaymentNotFoundException;
import com.neueda.model.Account;
import com.neueda.model.Payment;
import com.neueda.model.PaymentHistory;
import com.neueda.model.PaymentStatus;
import com.neueda.repository.AccountRepository;
import com.neueda.repository.PaymentHistoryRepository;
import com.neueda.repository.PaymentRepository;
import com.neueda.validator.PaymentValidator;
import com.neueda.dto.PaymentStatsResponse;
import com.neueda.util.ErrorMessageMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class PaymentServiceImplemenation implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository historyRepository;
    private final PaymentNotificationService notificationService;
    private final AccountRepository accountRepository;

    public PaymentServiceImplemenation(PaymentRepository paymentRepository,
                                       PaymentHistoryRepository historyRepository) {
        this(paymentRepository, historyRepository, null, null);
    }

    @Autowired
    public PaymentServiceImplemenation(PaymentRepository paymentRepository,
                                       PaymentHistoryRepository historyRepository,
                                       PaymentNotificationService notificationService,
                                       AccountRepository accountRepository) {
        this.paymentRepository = paymentRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.accountRepository = accountRepository;
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

        // Idempotency check before saving anything
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(payment.getIdempotencyKey());
        if (existing.isPresent()) {
            throw new DuplicatePaymentException(payment.getIdempotencyKey(), existing.get().getId());
        }

        // Keep original amount for validation; cap stored value to DB column limit
        BigDecimal originalAmount = payment.getAmount();
        if (originalAmount != null && originalAmount.compareTo(new BigDecimal("999999.99")) > 0) {
            payment.setAmount(new BigDecimal("999999.99"));
        }

        // --- STEP 1: Always save as CREATED first ---
        payment.setStatus(PaymentStatus.CREATED.name());
        final Payment saved;
        try {
            saved = paymentRepository.save(payment);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            Payment alreadySaved = paymentRepository.findByIdempotencyKey(payment.getIdempotencyKey())
                    .orElseThrow(() -> ex);
            throw new DuplicatePaymentException(payment.getIdempotencyKey(), alreadySaved.getId());
        }
        PaymentHistory h1 = historyRepository.save(new PaymentHistory(
                saved.getId(), null, PaymentStatus.CREATED.name(), LocalDateTime.now(), "Payment created"));
        notifyPaymentUpdate(saved, h1);

        // --- Validate immediately (no delay) using original values ---
        String validationError = null;
        try {
            PaymentValidator.validateAmount(originalAmount);
            PaymentValidator.validateCurrency(saved.getCurrency());
            PaymentValidator.validateIdempotencyKey(saved.getIdempotencyKey());
            if (saved.getSourceAccount() != null &&
                    saved.getSourceAccount().equalsIgnoreCase(saved.getDestinationAccount())) {
                validationError = "Source and destination accounts cannot be the same";
            } else {
                PaymentValidator.validateAccounts(saved.getSourceAccount(), saved.getDestinationAccount());
            }
        } catch (com.neueda.exception.ValidationException e) {
            validationError = e.getMessage();
        }

        // If validation failed → CREATED → FAILED (skip VALIDATED)
        if (validationError != null) {
            paymentRepository.updatePaymentWithError(saved.getId(), "VALIDATION_FAILED", validationError);
            saved.setStatus(PaymentStatus.FAILED.name());
            saved.setErrorCode("VALIDATION_FAILED");
            saved.setDescription(validationError);
            PaymentHistory fh = historyRepository.save(new PaymentHistory(
                    saved.getId(), PaymentStatus.CREATED.name(), PaymentStatus.FAILED.name(),
                    LocalDateTime.now(), validationError));
            notifyPaymentUpdate(saved, fh);
            return saved;
        }

        // --- STEP 2: VALIDATED ---
        sleep(1500);
        paymentRepository.updateStatus(saved.getId(), PaymentStatus.VALIDATED.name());
        saved.setStatus(PaymentStatus.VALIDATED.name());
        PaymentHistory h2 = historyRepository.save(new PaymentHistory(
                saved.getId(), PaymentStatus.CREATED.name(), PaymentStatus.VALIDATED.name(),
                LocalDateTime.now(), "Payment passed all validation checks"));
        notifyPaymentUpdate(saved, h2);

        // --- STEP 3: SENT — deduct from source, credit to destination ---
        sleep(2000);
        if (accountRepository != null) {
            Optional<Account> srcOpt = accountRepository.findByAccountNumber(saved.getSourceAccount());
            if (srcOpt.isPresent() && srcOpt.get().getBalance().compareTo(saved.getAmount()) < 0) {
                String reason = "Insufficient funds: available " + srcOpt.get().getBalance()
                        + " " + saved.getCurrency() + ", required " + originalAmount;
                sleep(1000);
                paymentRepository.updatePaymentWithError(saved.getId(), "INSUFFICIENT_FUNDS", reason);
                saved.setStatus(PaymentStatus.FAILED.name());
                saved.setErrorCode("INSUFFICIENT_FUNDS");
                saved.setDescription(reason);
                PaymentHistory fh = historyRepository.save(new PaymentHistory(
                        saved.getId(), PaymentStatus.VALIDATED.name(), PaymentStatus.FAILED.name(),
                        LocalDateTime.now(), reason));
                notifyPaymentUpdate(saved, fh);
                return saved;
            }

            // Deduct from source

            if (srcOpt.isPresent()) {
                
                java.math.BigDecimal newSrcBalance = srcOpt.get().getBalance().subtract(saved.getAmount());
                accountRepository.updateBalance(saved.getSourceAccount(), newSrcBalance);
            }

            // Credit to destination with currency conversion
            Optional<Account> destOpt = accountRepository.findByAccountNumber(saved.getDestinationAccount());
            if (destOpt.isPresent()) {
                String srcCurrency = srcOpt.isPresent() ? srcOpt.get().getAccountCurrencyType() : saved.getCurrency();
                String destCurrency = destOpt.get().getAccountCurrencyType();
                java.math.BigDecimal amount = saved.getAmount();
                java.math.BigDecimal convertedAmount = amount;

                if (srcCurrency != null && destCurrency != null && !srcCurrency.equalsIgnoreCase(destCurrency)) {
                    try {
                        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                        String url = "https://api.frankfurter.dev/v1/latest?from=" + srcCurrency + "&to=" + destCurrency;
                        System.out.println("[FX] Fetching rate: " + url);
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> response = restTemplate.getForObject(url, java.util.Map.class);
                        if (response != null && response.containsKey("rates")) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Number> rates = (java.util.Map<String, Number>) response.get("rates");
                            Number rate = rates.get(destCurrency.toUpperCase());
                            if (rate != null) {
                                convertedAmount = amount.multiply(new java.math.BigDecimal(rate.toString()))
                                        .setScale(2, java.math.RoundingMode.HALF_UP);
                                System.out.println("[FX] Rate " + srcCurrency + "->" + destCurrency + " = " + rate + " | " + amount + " -> " + convertedAmount);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[FX] Exchange rate fetch failed, using 1:1: " + e.getMessage());
                    }
                } else {
                    System.out.println("[FX] Same currency (" + srcCurrency + "), no conversion needed.");
                }

                java.math.BigDecimal newDestBalance = destOpt.get().getBalance().add(convertedAmount);
                accountRepository.updateBalance(saved.getDestinationAccount(), newDestBalance);
            }
        }
        paymentRepository.updateStatus(saved.getId(), PaymentStatus.SENT.name());
        saved.setStatus(PaymentStatus.SENT.name());
        PaymentHistory h3 = historyRepository.save(new PaymentHistory(
                saved.getId(), PaymentStatus.VALIDATED.name(), PaymentStatus.SENT.name(),
                LocalDateTime.now(), "Amount deducted from source and sent to destination"));
        notifyPaymentUpdate(saved, h3);

        // --- STEP 4: COMPLETED ---
        sleep(1500);
        paymentRepository.updateStatus(saved.getId(), PaymentStatus.COMPLETED.name());
        saved.setStatus(PaymentStatus.COMPLETED.name());
        PaymentHistory h4 = historyRepository.save(new PaymentHistory(
                saved.getId(), PaymentStatus.SENT.name(), PaymentStatus.COMPLETED.name(),
                LocalDateTime.now(), "Payment completed and confirmed by destination"));
        notifyPaymentUpdate(saved, h4);

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

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
