package com.neueda.controller;

import com.neueda.model.Account;
import com.neueda.service.AccountService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    // Create account
    @PostMapping("/")
    public ResponseEntity<Account> createAccount(
            @RequestBody Account account) {
System.out.println("Received account creation request: " + account);
        Account savedAccount = accountService.createAccount(account);

        return ResponseEntity.ok(savedAccount);
    }


    // Get all accounts
    @GetMapping
    public ResponseEntity<java.util.List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // Get account by account number
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(
            @PathVariable String accountNumber) {

        Optional<Account> account =
                accountService.getAccountByNumber(accountNumber);

        return account
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}