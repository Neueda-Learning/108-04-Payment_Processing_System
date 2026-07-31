package com.neueda.repository;

import com.neueda.model.Payment;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;


@Repository
public class PaymentRepository implements PaymentRepositoryInterface {
    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    private RowMapper<Payment> getRowMapper() {
        return (resultset, rowNum) -> new Payment(
                resultset.getLong("id"),
                resultset.getBigDecimal("amount"),
                resultset.getString("currency"),
                resultset.getString("status"),
                resultset.getString("source_account"),
                resultset.getString("destination_account"),
                resultset.getString("idempotency_key")
        );
    }

    public Payment save(Payment payment) {
        String sql =
        """
        INSERT INTO payments
        (
            amount,
            currency,
            status,
            source_account,
            destination_account,
            idempotency_key
        )
        VALUES(?,?,?,?,?,?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, payment.getAmount());
            ps.setString(2, payment.getCurrency());
            ps.setString(3, payment.getStatus());
            ps.setString(4, payment.getSourceAccount());
            ps.setString(5, payment.getDestinationAccount());
            ps.setString(6, payment.getIdempotencyKey());
            return ps;
        }, keyHolder);
        if(keyHolder.getKey()!=null){
            payment.setId(keyHolder.getKey().longValue());
        }

        return payment;
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
   
}