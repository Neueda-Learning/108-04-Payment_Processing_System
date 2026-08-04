package com.neueda.dto;

import java.math.BigDecimal;

public class PaymentStatsResponse {

    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;
    private BigDecimal totalAmount;
    private double successRate;
    private double failureRate;

    public PaymentStatsResponse() {}

    public PaymentStatsResponse(long totalPayments,
                                long successfulPayments,
                                long failedPayments,
                                BigDecimal totalAmount,
                                double successRate,
                                double failureRate) {
        this.totalPayments = totalPayments;
        this.successfulPayments = successfulPayments;
        this.failedPayments = failedPayments;
        this.totalAmount = totalAmount;
        this.successRate = successRate;
        this.failureRate = failureRate;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public long getSuccessfulPayments() {
        return successfulPayments;
    }

    public void setSuccessfulPayments(long successfulPayments) {
        this.successfulPayments = successfulPayments;
    }

    public long getFailedPayments() {
        return failedPayments;
    }

    public void setFailedPayments(long failedPayments) {
        this.failedPayments = failedPayments;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }
}