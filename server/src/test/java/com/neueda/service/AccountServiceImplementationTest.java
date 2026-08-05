package com.neueda.service;

import com.neueda.model.Account;
import com.neueda.repository.AccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AccountServiceImplementationTest {

    @Test
    void createAccountDelegatesToRepositorySave() {
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        AccountServiceImplementation service = new AccountServiceImplementation(repository);
        Account account = account(1L, "ACC10001");

        Account created = service.createAccount(account);

        assertSame(account, repository.savedAccount);
        assertSame(account, created);
    }

    @Test
    void getAccountByIdReturnsRepositoryResult() {
        Account expected = account(2L, "ACC20002");
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        repository.accountById = Optional.of(expected);
        AccountServiceImplementation service = new AccountServiceImplementation(repository);

        Optional<Account> result = service.getAccountById(2L);

        assertEquals(Optional.of(expected), result);
        assertEquals(2L, repository.lastRequestedId);
    }

    @Test
    void getAllAccountsReturnsRepositoryList() {
        List<Account> expected = List.of(account(3L, "ACC30003"), account(4L, "ACC40004"));
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        repository.allAccounts = new ArrayList<>(expected);
        AccountServiceImplementation service = new AccountServiceImplementation(repository);

        List<Account> result = service.getAllAccounts();

        assertIterableEquals(expected, result);
    }

    @Test
    void getAccountByNumberReturnsRepositoryResult() {
        Account expected = account(5L, "ACC50005");
        InMemoryAccountRepository repository = new InMemoryAccountRepository();
        repository.accountByNumber = Optional.of(expected);
        AccountServiceImplementation service = new AccountServiceImplementation(repository);

        Optional<Account> result = service.getAccountByNumber("ACC50005");

        assertEquals(Optional.of(expected), result);
        assertEquals("ACC50005", repository.lastRequestedNumber);
    }

    private static Account account(Long id, String number) {
        return new Account(id, number, "Holder", "USD", new BigDecimal("500.00"), "ACTIVE");
    }

    private static class InMemoryAccountRepository implements AccountRepository {
        private Account savedAccount;
        private Optional<Account> accountById = Optional.empty();
        private Optional<Account> accountByNumber = Optional.empty();
        private List<Account> allAccounts = List.of();
        private Long lastRequestedId;
        private String lastRequestedNumber;

        @Override
        public Account save(Account account) {
            this.savedAccount = account;
            return account;
        }

        @Override
        public Optional<Account> findById(Long id) {
            this.lastRequestedId = id;
            return accountById;
        }

        @Override
        public Optional<Account> findByAccountNumber(String accountNumber) {
            this.lastRequestedNumber = accountNumber;
            return accountByNumber;
        }

        @Override
        public List<Account> findAll() {
            return allAccounts;
        }

        @Override
        public void updateBalance(String accountNumber, java.math.BigDecimal newBalance) {
            // no-op for tests
        }
    }
}

