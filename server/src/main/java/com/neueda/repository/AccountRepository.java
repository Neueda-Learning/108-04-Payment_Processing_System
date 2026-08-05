package com.neueda.repository;

import java.math.BigDecimal;
import java.util.*;

import com.neueda.model.Account;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAll();

    int debitBalance(String accountNumber, BigDecimal amount);

    int creditBalance(String accountNumber, BigDecimal amount);
    
}