package com.neueda.service;
import com.neueda.model.Account;
import com.neueda.repository.AmountRepositoryInterface;

import java.util.List;
import java.util.Optional;

public class AccountService implements AccountServiceInterface {

    private final AmountRepositoryInterface repository;



    public AccountService(AmountRepositoryInterface repository) {

        this.repository = repository;

    }



    @Override
    public Account createAccount(Account account) {


        /*
          Later:
          - validate account number
          - check duplicate accounts
          - set default status

          For now:
          save directly
        */


        return repository.save(account);

    }




    @Override
    public Optional<Account> getAccountById(Long id) {


        return repository.findById(id);

    }




    @Override
    public List<Account> getAllAccounts() {


        return repository.findAll();

    }




    @Override
    public Optional<Account> getAccountByNumber(String accountNumber) {


        return repository.findByAccountNumber(accountNumber);

    }

}