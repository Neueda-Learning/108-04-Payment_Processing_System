package com.neueda.repository;

import com.neueda.model.Payment;
import com.neueda.model.PaymentStatus;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;


@Repository
public class PaymentRepositoryImplementation implements PaymentRepository {
    private final JdbcTemplate jdbc;

    public PaymentRepositoryImplementation(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    private RowMapper<Payment> getRowMapper() {
        return (resultset, rowNum) -> new Payment(
                resultset.getLong("id"),
                resultset.getBigDecimal("amount"),
                resultset.getString("status"),
                resultset.getString("source_account"),
                resultset.getString("destination_account"),
                resultset.getString("idempotency_key"),
                resultset.getTimestamp("created_at") != null ? 
                    resultset.getTimestamp("created_at").toLocalDateTime() : null,
                resultset.getTimestamp("updated_at") != null ? 
                    resultset.getTimestamp("updated_at").toLocalDateTime() : null,
                resultset.getString("currency"),
                resultset.getString("error_code"),
                resultset.getString("description") // Retrieve the new field
        );
    }

    public Payment save(Payment payment) {
        String sql =
        """
        INSERT INTO payments
        (
            amount,
            status,
            source_account,
            destination_account,
            idempotency_key,
            currency,
            error_code,
            created_at,
            updated_at,
            description
        )
        VALUES(?,?,?,?,?,?,?,?,?,?)
        """;
System.out.println("Executing SQL: " + sql);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, payment.getAmount());
            ps.setString(2, payment.getStatus());
            ps.setString(3, payment.getSourceAccount());
            ps.setString(4, payment.getDestinationAccount());
            ps.setString(5, payment.getIdempotencyKey());
            ps.setString(6, payment.getCurrency());
            ps.setString(7, payment.getErrorCode());
            
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(8, java.sql.Timestamp.valueOf(now));
            ps.setTimestamp(9, java.sql.Timestamp.valueOf(now));
            ps.setString(10, payment.getDescription()); // Set the new field
            return ps;
        }, keyHolder);
        Number generatedId = extractGeneratedId(keyHolder);
        if(generatedId != null){
            payment.setId(generatedId.longValue());
        }

        return payment;
    }

    private Number extractGeneratedId(KeyHolder keyHolder) {
        if (keyHolder.getKeyList().isEmpty()) {
            return null;
        }

        Map<String, Object> keys = keyHolder.getKeyList().getFirst();
        Object idValue = keys.get("id");
        if (idValue == null) {
            idValue = keys.get("ID");
        }
        if (idValue instanceof Number number) {
            return number;
        }

        for (Object value : keys.values()) {
            if (value instanceof Number number) {
                return number;
            }
        }

        return keyHolder.getKey();
    }

    public Optional<Payment> findById(Long id) {
        String sql = "SELECT * FROM payments WHERE id = ?";
        List<Payment> payments = jdbc.query(sql, getRowMapper(), id);
        if (payments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(payments.get(0));   
        }
    }

    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments";
        return jdbc.query(sql, getRowMapper());
    }

    public Optional<Payment> findByIdempotencyKey(String key) {
        String sql = "SELECT * FROM payments WHERE idempotency_key = ?";
        List<Payment> payments = jdbc.query(sql, getRowMapper(), key);
        if (payments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(payments.get(0));
        }
    }

    public List<Payment> findAllByStatus(String status) {
        String sql = "SELECT * FROM payments WHERE status = ?";
        return jdbc.query(sql, getRowMapper(), status);
    }

    public List<Payment> findRecentByAccountAndWindow(String accountNumber, LocalDateTime windowStart) {
        String sql = """
            SELECT *
            FROM payments
            WHERE (source_account = ? OR destination_account = ?)
              AND created_at >= ?
              AND status <> 'FAILED'
        """;
        return jdbc.query(sql, getRowMapper(), accountNumber, accountNumber, Timestamp.valueOf(windowStart));
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE payments SET status = ?, updated_at = ? WHERE id = ?";
        jdbc.update(sql, status, java.sql.Timestamp.valueOf(LocalDateTime.now()), id);
    }

    public void updatePayment(Payment payment) {
        String sql = """
            UPDATE payments 
            SET amount = ?, status = ?, source_account = ?, destination_account = ?, 
                currency = ?, error_code = ?, updated_at = ?
            WHERE id = ?
        """;
        jdbc.update(sql,
            payment.getAmount(),
            payment.getStatus(),
            payment.getSourceAccount(),
            payment.getDestinationAccount(),
            payment.getCurrency(),
            payment.getErrorCode(),
            java.sql.Timestamp.valueOf(LocalDateTime.now()),
            payment.getId()
        );
    }

    public void updatePaymentWithError(Long id, String errorCode, String userFriendlyMessage) {
        String sql = """
            UPDATE payments 
            SET status = ?, error_code = ?, description = ?, updated_at = ?
            WHERE id = ?
        """;
        jdbc.update(sql,
            PaymentStatus.FAILED.name(),
            errorCode,
            userFriendlyMessage,
            java.sql.Timestamp.valueOf(LocalDateTime.now()),
            id
        );
    }
   
}