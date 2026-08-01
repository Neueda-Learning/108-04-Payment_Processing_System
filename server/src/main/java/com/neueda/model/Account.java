package com.neueda.model;

import java.math.BigDecimal;

public class Account {

    private Long id;
    private String accountNumber;
    private String accountHolderName;
    private BigDecimal balance;
    private String accountCurrencyType; // USD, EUR, GBP, etc.
    private String status;   // ACTIVE, INACTIVE, BLOCKED

    // Empty constructor (required for frameworks)
    public Account() {}

    // Full constructor
    public Account(Long id, String accountNumber, String accountHolderName, String accountCurrencyType
                   , BigDecimal balance, String status) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.accountCurrencyType = accountCurrencyType;
        this.balance = balance;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountCurrencyType() {
        return accountCurrencyType;
    }

    public void setAccountCurrencyType(String accountCurrencyType) {
        this.accountCurrencyType = accountCurrencyType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
