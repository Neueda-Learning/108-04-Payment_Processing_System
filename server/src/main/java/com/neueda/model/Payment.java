package com.neueda.model;

public class Payment {

    private Long id;
    private Double amount;
    private String currency;
    private String status;
    private String sourceAccount;
    private String destinationAccount;
    private String idempotencyKey;

    // Empty constructor (required for frameworks)
    public Payment() {}

    // Full constructor
    public Payment(Long id, Double amount, String currency, String status,
                   String sourceAccount, String destinationAccount, String idempotencyKey) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.idempotencyKey = idempotencyKey;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}

