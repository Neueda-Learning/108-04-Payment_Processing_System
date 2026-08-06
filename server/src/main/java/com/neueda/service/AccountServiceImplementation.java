package com.neueda.service;
import com.neueda.model.Account;
import com.neueda.repository.AccountRepository;
import com.neueda.validator.PaymentValidator;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImplementation implements AccountService {

    private final AccountRepository repository;



    public AccountServiceImplementation(AccountRepository repository) {

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

        // Reject unsupported/blank/mis-cased currency codes at creation time so that
        // downstream FX lookups (e.g. Frankfurter) never receive an invalid currency
        // and fail with a 404.
        PaymentValidator.validateCurrency(account.getAccountCurrencyType());
        account.setAccountCurrencyType(account.getAccountCurrencyType().trim().toUpperCase());

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