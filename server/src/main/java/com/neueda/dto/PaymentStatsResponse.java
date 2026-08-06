package com.neueda.dto;

import java.math.BigDecimal;

public class PaymentStatsResponse {

    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;
    private BigDecimal totalAmount;
    private double successRate;
    private double failureRate;
    private BigDecimal averageAmount;
    private BigDecimal largestAmount;

    public PaymentStatsResponse() {}

    public PaymentStatsResponse(long totalPayments,
                                long successfulPayments,
                                long failedPayments,
                                BigDecimal totalAmount,
                                double successRate,
                                double failureRate,
                                BigDecimal averageAmount,
                                BigDecimal largestAmount) {
        this.totalPayments = totalPayments;
        this.successfulPayments = successfulPayments;
        this.failedPayments = failedPayments;
        this.totalAmount = totalAmount;
        this.successRate = successRate;
        this.failureRate = failureRate;
        this.averageAmount = averageAmount;
        this.largestAmount = largestAmount;
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

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }

    public BigDecimal getLargestAmount() {
        return largestAmount;
    }

    public void setLargestAmount(BigDecimal largestAmount) {
        this.largestAmount = largestAmount;
    }
}