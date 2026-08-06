package com.neueda.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private BigDecimal amount;
    private String status;
    private String sourceAccount;
    private String destinationAccount;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String currency;
    private String errorCode;
    private String description; // New field for payment description
    private String paymentType;
    private Integer scheduledDelaySeconds;

    // Empty constructor (required for frameworks)
    public Payment() {}

    // Partial constructor (without audit fields)
    public Payment(Long id, BigDecimal amount, String status,
                   String sourceAccount, String destinationAccount, String idempotencyKey,String description) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
    }

    // Extended constructor (with audit fields)
    public Payment(Long id, BigDecimal amount, String status,
                   String sourceAccount, String destinationAccount, String idempotencyKey,
                   LocalDateTime createdAt, LocalDateTime updatedAt, String currency, String errorCode, String description) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.currency = currency;
        this.errorCode = errorCode;
        this.description = description; // Initialize the new field
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getScheduledDelaySeconds() {
        return scheduledDelaySeconds;
    }

    public void setScheduledDelaySeconds(Integer scheduledDelaySeconds) {
        this.scheduledDelaySeconds = scheduledDelaySeconds;
    }
}
