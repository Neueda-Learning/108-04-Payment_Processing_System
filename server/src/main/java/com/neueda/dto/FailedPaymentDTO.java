package com.neueda.dto;

import java.time.LocalDateTime;

/**
 * DTO for representing a failed payment with error details.
 * Includes both technical error codes and user-friendly messages.
 */
public class FailedPaymentDTO {

    private Long paymentId;
    private String errorCode;
    private String userFriendlyMessage;
    private String technicalErrorReason;
    private LocalDateTime timestamp;

    // Constructors
    public FailedPaymentDTO() {}

    public FailedPaymentDTO(Long paymentId, String errorCode, String userFriendlyMessage, String technicalErrorReason, LocalDateTime timestamp) {
        this.paymentId = paymentId;
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
        this.technicalErrorReason = technicalErrorReason;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }

    public void setUserFriendlyMessage(String userFriendlyMessage) {
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public String getTechnicalErrorReason() {
        return technicalErrorReason;
    }

    public void setTechnicalErrorReason(String technicalErrorReason) {
        this.technicalErrorReason = technicalErrorReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FailedPaymentDTO{" +
                "paymentId=" + paymentId +
                ", errorCode='" + errorCode + '\'' +
                ", userFriendlyMessage='" + userFriendlyMessage + '\'' +
                ", technicalErrorReason='" + technicalErrorReason + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

