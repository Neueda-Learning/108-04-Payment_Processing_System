package com.neueda.dto;

/**
 * DTO for failing a payment with error details.
 */
public class FailPaymentRequest {

    private String errorCode;
    private String technicalReason;

    // Constructors
    public FailPaymentRequest() {}

    public FailPaymentRequest(String errorCode, String technicalReason) {
        this.errorCode = errorCode;
        this.technicalReason = technicalReason;
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTechnicalReason() {
        return technicalReason;
    }

    public void setTechnicalReason(String technicalReason) {
        this.technicalReason = technicalReason;
    }

    @Override
    public String toString() {
        return "FailPaymentRequest{" +
                "errorCode='" + errorCode + '\'' +
                ", technicalReason='" + technicalReason + '\'' +
                '}';
    }
}

