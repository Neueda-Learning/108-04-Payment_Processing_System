package com.neueda.repository;

import com.neueda.model.Account;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;

//import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@Repository
public class AccountRepositoryImplementation implements AccountRepository {
    private final JdbcTemplate jdbc;

    public AccountRepositoryImplementation(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private RowMapper<Account> getRowMapper() {
        return (rs, rowNum) -> new Account(
                rs.getLong("id"),
                rs.getString("account_number"),
                rs.getString("account_holder_name"),
                rs.getString("account_currency_type"),
                rs.getBigDecimal("balance"),
                rs.getString("status")

        );
    }

    public Account save(Account account) {
        String sql =
        """
        INSERT INTO accounts
        (
            account_number,
            account_holder_name,
            balance,
            account_currency_type,
            status
        )

        VALUES(?,?,?,?,?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,account.getAccountNumber());
            ps.setString(2,account.getAccountHolderName());
            ps.setBigDecimal(3,account.getBalance());
            ps.setString(4,account.getAccountCurrencyType());
            ps.setString(5,account.getStatus());
            return ps;
        }, keyHolder);
        if(keyHolder.getKey()!=null){
            account.setId(keyHolder.getKey().longValue());
        }
        return account;
    }

    public Optional<Account> findById(Long id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        List<Account> accounts = jdbc.query(sql, getRowMapper(), id);
        if (accounts.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(accounts.get(0));
        }
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        List<Account> accounts = jdbc.query(sql, getRowMapper(), accountNumber);
        if (accounts.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(accounts.get(0));
        }
    }

    public List<Account> findAll() {
        String sql = "SELECT * FROM accounts";
        return jdbc.query(sql, getRowMapper());
    }

    public void updateBalance(String accountNumber, java.math.BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        jdbc.update(sql, newBalance, accountNumber);
    }

}