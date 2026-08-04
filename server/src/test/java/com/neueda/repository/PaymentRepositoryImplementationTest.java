package com.neueda.repository;

import com.neueda.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import(PaymentRepositoryImplementation.class)
class PaymentRepositoryImplementationTest {

    @Autowired
    private PaymentRepositoryImplementation repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM payment_history");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM accounts");
    }

    @Test
    void savePersistsPaymentAndAuditColumns() {
        Payment payment = payment(null, "CREATED", "idem-save", "100.25", "USD", null);

        Payment saved = repository.save(payment);

        assertNotNull(saved.getId());
        Payment reloaded = repository.findById(saved.getId()).orElseThrow();
        assertAll(
            () -> assertEquals(new BigDecimal("100.25"), reloaded.getAmount()),
            () -> assertEquals("CREATED", reloaded.getStatus()),
            () -> assertEquals("ACC01010", reloaded.getSourceAccount()),
            () -> assertEquals("ACC02020", reloaded.getDestinationAccount()),
            () -> assertEquals("idem-save", reloaded.getIdempotencyKey()),
            () -> assertEquals("USD", reloaded.getCurrency()),
            () -> assertNotNull(reloaded.getCreatedAt()),
            () -> assertNotNull(reloaded.getUpdatedAt())
        );
    }

    @Test
    void findByIdReturnsEmptyWhenPaymentIsMissing() {
        assertFalse(repository.findById(123456L).isPresent());
    }

    @Test
    void findByIdempotencyKeyReturnsMatchingPayment() {
        Payment saved = repository.save(payment(null, "VALIDATED", "idem-find-key", "75.00", "EUR", null));

        Optional<Payment> result = repository.findByIdempotencyKey("idem-find-key");

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void findAllByStatusFiltersCorrectly() {
        repository.save(payment(null, "CREATED", "idem-created-1", "10.00", "USD", null));
        repository.save(payment(null, "CREATED", "idem-created-2", "20.00", "USD", null));
        repository.save(payment(null, "FAILED", "idem-failed-1", "30.00", "USD", "PROCESSING_ERROR"));

        List<Payment> createdPayments = repository.findAllByStatus("CREATED");
        List<Payment> failedPayments = repository.findAllByStatus("FAILED");

        assertAll(
            () -> assertEquals(2, createdPayments.size()),
            () -> assertEquals(1, failedPayments.size()),
            () -> assertEquals("PROCESSING_ERROR", failedPayments.getFirst().getErrorCode())
        );
    }

    @Test
    void updateStatusChangesStatusAndRefreshesUpdatedTimestamp() {
        LocalDateTime originalUpdatedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        jdbcTemplate.update(
            """
            INSERT INTO payments (amount, status, source_account, destination_account, idempotency_key, currency, created_at, updated_at, error_code)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            new BigDecimal("44.00"),
            "CREATED",
            "ACC01010",
            "ACC02020",
            "idem-update-status",
            "USD",
            Timestamp.valueOf(originalUpdatedAt.minusMinutes(5)),
            Timestamp.valueOf(originalUpdatedAt),
            null
        );
        Long id = jdbcTemplate.queryForObject("SELECT id FROM payments WHERE idempotency_key = ?", Long.class, "idem-update-status");

        repository.updateStatus(id, "VALIDATED");

        Payment updated = repository.findById(id).orElseThrow();
        assertAll(
            () -> assertEquals("VALIDATED", updated.getStatus()),
            () -> assertNotEquals(originalUpdatedAt, updated.getUpdatedAt()),
            () -> assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt))
        );
    }

    @Test
    void updatePaymentChangesMutableBusinessFields() {
        Payment saved = repository.save(payment(null, "CREATED", "idem-update-payment", "90.00", "USD", null));
        LocalDateTime originalUpdatedAt = repository.findById(saved.getId()).orElseThrow().getUpdatedAt();

        saved.setAmount(new BigDecimal("120.50"));
        saved.setStatus("FAILED");
        saved.setSourceAccount("ACC77777");
        saved.setDestinationAccount("ACC88888");
        saved.setCurrency("GBP");
        saved.setErrorCode("NETWORK_ERROR");
        repository.updatePayment(saved);

        Payment reloaded = repository.findById(saved.getId()).orElseThrow();
        assertAll(
            () -> assertEquals(new BigDecimal("120.50"), reloaded.getAmount()),
            () -> assertEquals("FAILED", reloaded.getStatus()),
            () -> assertEquals("ACC77777", reloaded.getSourceAccount()),
            () -> assertEquals("ACC88888", reloaded.getDestinationAccount()),
            () -> assertEquals("GBP", reloaded.getCurrency()),
            () -> assertEquals("NETWORK_ERROR", reloaded.getErrorCode()),
            () -> assertTrue(reloaded.getUpdatedAt().isAfter(originalUpdatedAt) || reloaded.getUpdatedAt().isEqual(originalUpdatedAt))
        );
    }

    @Test
    void saveRejectsDuplicateIdempotencyKey() {
        repository.save(payment(null, "CREATED", "idem-duplicate", "15.00", "USD", null));

        assertThrows(DataAccessException.class,
            () -> repository.save(payment(null, "CREATED", "idem-duplicate", "18.00", "USD", null)));
    }

    private static Payment payment(Long id, String status, String key, String amount, String currency, String errorCode) {
        Payment payment = new Payment(id, new BigDecimal(amount), status, "ACC01010", "ACC02020", key);
        payment.setCurrency(currency);
        payment.setErrorCode(errorCode);
        return payment;
    }
}


