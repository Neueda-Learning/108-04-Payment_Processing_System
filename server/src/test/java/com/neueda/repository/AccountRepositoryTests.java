package com.neueda.repository;



import com.neueda.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import(AccountRepositoryImplementation.class)
class AccountRepositoryTests {

    @Autowired
    private AccountRepositoryImplementation repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE FROM payment_history");
        jdbcTemplate.update("DELETE FROM payments");
        jdbcTemplate.update("DELETE FROM accounts");
    }

    @Test
    void savePersistsAccountAndGeneratesId() {
        Account account = account(null, "ACC10001", "Alice", "USD", "1500.25", "ACTIVE");

        Account saved = repository.save(account);

        assertNotNull(saved.getId());
        Optional<Account> reloaded = repository.findById(saved.getId());
        assertTrue(reloaded.isPresent());
        assertAll(
            () -> assertEquals("ACC10001", reloaded.get().getAccountNumber()),
            () -> assertEquals("Alice", reloaded.get().getAccountHolderName()),
            () -> assertEquals("USD", reloaded.get().getAccountCurrencyType()),
            () -> assertEquals(new BigDecimal("1500.25"), reloaded.get().getBalance()),
            () -> assertEquals("ACTIVE", reloaded.get().getStatus())
        );
    }

    @Test
    void findByIdReturnsEmptyWhenAccountDoesNotExist() {
        assertFalse(repository.findById(9999L).isPresent());
    }

    @Test
    void findByAccountNumberReturnsMatchingAccount() {
        Account saved = repository.save(account(null, "ACC20002", "Bob", "EUR", "250.00", "ACTIVE"));

        Optional<Account> result = repository.findByAccountNumber("ACC20002");

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void findAllReturnsAllPersistedAccounts() {
        repository.save(account(null, "ACC30003", "Carol", "GBP", "75.00", "ACTIVE"));
        repository.save(account(null, "ACC40004", "Dave", "USD", "95.00", "BLOCKED"));

        List<Account> accounts = repository.findAll();

        assertEquals(2, accounts.size());
    }

    @Test
    void saveRejectsDuplicateAccountNumber() {
        repository.save(account(null, "ACC50005", "Eve", "USD", "600.00", "ACTIVE"));

        assertThrows(DataAccessException.class,
            () -> repository.save(account(null, "ACC50005", "Frank", "USD", "700.00", "ACTIVE")));
    }

    private static Account account(Long id, String number, String holder, String currency, String balance, String status) {
        return new Account(id, number, holder, currency, new BigDecimal(balance), status);
    }
}
