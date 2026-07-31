package com.neueda.repository;

import java.util.*;

import com.neueda.model.Account;

public interface AmountRepositoryInterface {
    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findAll();
    
}