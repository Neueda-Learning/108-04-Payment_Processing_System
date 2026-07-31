package com.neueda.service;

import java.util.List;
import java.util.Optional;

import com.neueda.model.Account;

public interface AccountServiceInterface {
    Account createAccount(Account account);


    Optional<Account> getAccountById(Long id);


    List<Account> getAllAccounts();


    Optional<Account> getAccountByNumber(String accountNumber);

}
