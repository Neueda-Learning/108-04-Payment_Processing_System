package com.neueda.repository;

import com.neueda.model.PaymentHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
// schema.sql uses MySQL-flavored DDL (inline INDEX(...) in CREATE TABLE) that relies on
// the app's configured H2 URL running in MODE=MySQL (see application.properties). Spring's
// default @JdbcTest datasource replacement drops that URL, so keep the real one instead.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PaymentHistoryRepositoryImplementation.class)
class PaymentHistoryRepositoryImplementationTest {

    @Autowired
    private PaymentHistoryRepositoryImplementation repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM payment_history");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM accounts");
    }

    @Test
    void savePersistsHistoryEntryAndGeneratesId() {
        Long paymentId = insertPayment("idem-history-1");
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 4, 18, 0);
        PaymentHistory history = new PaymentHistory(paymentId, "CREATED", "VALIDATED", timestamp, "Validation passed");

        PaymentHistory saved = repository.save(history);

        assertNotNull(saved.getId());
        List<PaymentHistory> historyEntries = repository.findByPaymentId(paymentId);
        assertEquals(1, historyEntries.size());
        assertAll(
            () -> assertEquals("CREATED", historyEntries.getFirst().getFromStatus()),
            () -> assertEquals("VALIDATED", historyEntries.getFirst().getToStatus()),
            () -> assertEquals(timestamp, historyEntries.getFirst().getTimestamp()),
            () -> assertEquals("Validation passed", historyEntries.getFirst().getNotes())
        );
    }

    @Test
    void saveUsesCurrentTimestampWhenTimestampIsNotProvided() {
        Long paymentId = insertPayment("idem-history-2");

        repository.save(new PaymentHistory(paymentId, "VALIDATED", "SENT", null, "Queued for dispatch"));

        PaymentHistory reloaded = repository.findByPaymentId(paymentId).getFirst();
        assertNotNull(reloaded.getTimestamp());
        assertEquals("Queued for dispatch", reloaded.getNotes());
    }

    @Test
    void findByPaymentIdReturnsEntriesInReverseChronologicalOrder() {
        Long paymentId = insertPayment("idem-history-3");
        repository.save(new PaymentHistory(paymentId, "CREATED", "VALIDATED", LocalDateTime.of(2026, 8, 4, 10, 0), "validated"));
        repository.save(new PaymentHistory(paymentId, "VALIDATED", "SENT", LocalDateTime.of(2026, 8, 4, 11, 0), "sent"));
        repository.save(new PaymentHistory(paymentId, "SENT", "COMPLETED", LocalDateTime.of(2026, 8, 4, 12, 0), "completed"));

        List<PaymentHistory> historyEntries = repository.findByPaymentId(paymentId);

        assertEquals(3, historyEntries.size());
        assertAll(
            () -> assertEquals("COMPLETED", historyEntries.get(0).getToStatus()),
            () -> assertEquals("SENT", historyEntries.get(1).getToStatus()),
            () -> assertEquals("VALIDATED", historyEntries.get(2).getToStatus())
        );
    }

    @Test
    void findByPaymentIdReturnsEmptyListWhenNoEntriesExist() {
        assertTrue(repository.findByPaymentId(9999L).isEmpty());
    }

    private Long insertPayment(String idempotencyKey) {
        jdbcTemplate.update(
            """
            INSERT INTO payments (amount, status, source_account, destination_account, idempotency_key, currency, created_at, updated_at, error_code)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            new BigDecimal("55.00"),
            "CREATED",
            "ACC03030",
            "ACC04040",
            idempotencyKey,
            "USD",
            Timestamp.valueOf(LocalDateTime.of(2026, 8, 4, 9, 0)),
            Timestamp.valueOf(LocalDateTime.of(2026, 8, 4, 9, 0)),
            null
        );
        return jdbcTemplate.queryForObject("SELECT id FROM payments WHERE idempotency_key = ?", Long.class, idempotencyKey);
    }
}



