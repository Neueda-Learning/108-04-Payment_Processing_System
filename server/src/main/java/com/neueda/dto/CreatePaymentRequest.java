package com.neueda.dto;

import java.math.BigDecimal;

/**
 * DTO for creating a new payment request.
 */
public class CreatePaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String sourceAccount;
    private String destinationAccount;
    private String idempotencyKey;
    private String reference;

    public CreatePaymentRequest() {}

    public CreatePaymentRequest(BigDecimal amount, String currency, String sourceAccount, 
                               String destinationAccount, String idempotencyKey) {
        this.amount = amount;
        this.currency = currency;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.idempotencyKey = idempotencyKey;
    }

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

