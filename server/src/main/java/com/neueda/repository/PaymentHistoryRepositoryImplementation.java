package com.neueda.repository;

import com.neueda.model.PaymentHistory;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;

@Repository
public class PaymentHistoryRepositoryImplementation implements PaymentHistoryRepository {
    
    private final JdbcTemplate jdbc;

    public PaymentHistoryRepositoryImplementation(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private RowMapper<PaymentHistory> getRowMapper() {
        return (resultset, rowNum) -> new PaymentHistory(
                resultset.getLong("id"),
                resultset.getLong("payment_id"),
                resultset.getString("from_status"),
                resultset.getString("to_status"),
                resultset.getTimestamp("timestamp") != null ? 
                    resultset.getTimestamp("timestamp").toLocalDateTime() : null,
                resultset.getString("notes")
        );
    }

    @Override
    public PaymentHistory save(PaymentHistory history) {
        String sql = """
            INSERT INTO payment_history
            (payment_id, from_status, to_status, timestamp, notes)
            VALUES(?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime timestamp = history.getTimestamp() != null ? history.getTimestamp() : LocalDateTime.now();
        history.setTimestamp(timestamp);

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, history.getPaymentId());
            ps.setString(2, history.getFromStatus());
            ps.setString(3, history.getToStatus());
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(timestamp));
            ps.setString(5, history.getNotes());

            return ps;
        }, keyHolder);

        Number generatedId = extractGeneratedId(keyHolder);
        if (generatedId != null) {
            history.setId(generatedId.longValue());
        }

        return history;
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

    @Override
    public List<PaymentHistory> findByPaymentId(Long paymentId) {
        String sql = "SELECT * FROM payment_history WHERE payment_id = ? ORDER BY timestamp DESC";
        return jdbc.query(sql, getRowMapper(), paymentId);
    }

}

