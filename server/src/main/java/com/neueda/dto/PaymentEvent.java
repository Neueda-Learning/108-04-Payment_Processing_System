package com.neueda.dto;

import java.time.LocalDateTime;

public class PaymentEvent {

    private Long paymentId;
    private String fromStatus;
    private String toStatus;
    private LocalDateTime timestamp;
    private String notes;
    private String errorCode;

    public PaymentEvent() {}

    public PaymentEvent(Long paymentId, String fromStatus, String toStatus,
                        LocalDateTime timestamp, String notes, String errorCode) {
        this.paymentId = paymentId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.timestamp = timestamp;
        this.notes = notes;
        this.errorCode = errorCode;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
