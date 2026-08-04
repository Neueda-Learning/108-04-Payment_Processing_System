package com.neueda.dto;

/**
 * DTO for updating a payment's status.
 */
public class UpdatePaymentStatusRequest {

    private String status;

    public UpdatePaymentStatusRequest() {}

    public UpdatePaymentStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}