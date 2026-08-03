package com.neueda.dto;

import com.neueda.model.ErrorCode;
import java.time.LocalDateTime;

/**
 * DTO for standardized error responses in the API.
 */
public class ErrorResponse {
    private String errorCode;
    private String message;
    private int httpStatus;
    private String details;
    private LocalDateTime timestamp;

    public ErrorResponse(ErrorCode errorCode, String details) {
        this.errorCode = errorCode.name();
        this.message = errorCode.getMessage();
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message, int httpStatus, String details) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

