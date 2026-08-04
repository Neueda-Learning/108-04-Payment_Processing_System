package com.neueda.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountTest {

    @Test
    void fullConstructorPopulatesAllFields() {
        Account account = new Account(1L, "ACC12345", "Alice", "USD", new BigDecimal("100.50"), "ACTIVE");

        assertAll(
            () -> assertEquals(1L, account.getId()),
            () -> assertEquals("ACC12345", account.getAccountNumber()),
            () -> assertEquals("Alice", account.getAccountHolderName()),
            () -> assertEquals("USD", account.getAccountCurrencyType()),
            () -> assertEquals(new BigDecimal("100.50"), account.getBalance()),
            () -> assertEquals("ACTIVE", account.getStatus())
        );
    }

    @Test
    void settersUpdateEachField() {
        Account account = new Account();

        account.setId(7L);
        account.setAccountNumber("ACC76543");
        account.setAccountHolderName("Bob");
        account.setAccountCurrencyType("EUR");
        account.setBalance(new BigDecimal("999.99"));
        account.setStatus("BLOCKED");

        assertAll(
            () -> assertEquals(7L, account.getId()),
            () -> assertEquals("ACC76543", account.getAccountNumber()),
            () -> assertEquals("Bob", account.getAccountHolderName()),
            () -> assertEquals("EUR", account.getAccountCurrencyType()),
            () -> assertEquals(new BigDecimal("999.99"), account.getBalance()),
            () -> assertEquals("BLOCKED", account.getStatus())
        );
    }
}

