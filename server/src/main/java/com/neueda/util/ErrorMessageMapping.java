package com.neueda.util;

import com.neueda.model.ErrorCode;

/**
 * Utility class to map technical error codes to user-friendly error messages.
 * Provides non-technical explanations for each error code that can be displayed to end users.
 */
public class ErrorMessageMapping {

    /**
     * Maps an ErrorCode to a user-friendly message that can be displayed to customers.
     * 
     * @param errorCode The ErrorCode enum value
     * @return A user-friendly explanation of the error
     */
    public static String getUserFriendlyMessage(ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_FAILED -> 
                "The payment details you provided are invalid. Please check the amount, account numbers, and try again.";
            
            case INSUFFICIENT_FUNDS -> 
                "Your account doesn't have enough funds to complete this payment. Please verify your account balance and try with a lower amount.";
            
            case INVALID_ACCOUNT -> 
                "One or both of the account numbers provided are invalid or don't exist. Please verify the account details and try again.";
            
            case INVALID_CURRENCY -> 
                "The currency you selected is not supported. Please choose from our supported currencies and try again.";
            
            case INVALID_AMOUNT -> 
                "The payment amount is invalid. Please ensure the amount is greater than zero and within acceptable limits.";
            
            case DUPLICATE_PAYMENT -> 
                "This payment appears to have already been processed. Please check your payment history to confirm.";
            
            case INVALID_STATUS_TRANSITION -> 
                "This payment cannot be transitioned to the requested status. Please contact support if you believe this is an error.";
            
            case PAYMENT_NOT_FOUND -> 
                "The payment you're looking for could not be found. Please verify the payment ID and try again.";
            
            case PROCESSING_ERROR -> 
                "We encountered an internal error while processing your payment. Our team has been notified. Please try again later.";
            
            case NETWORK_ERROR -> 
                "We're having trouble connecting to the payment network. This is temporary. Please try again in a few moments.";
        };
    }

    /**
     * Get a brief, one-line user-friendly message for quick display.
     * 
     * @param errorCode The ErrorCode enum value
     * @return A brief user-friendly message
     */
    public static String getBriefMessage(ErrorCode errorCode) {
        return switch (errorCode) {
            case VALIDATION_FAILED -> "Invalid payment details";
            case INSUFFICIENT_FUNDS -> "Insufficient account balance";
            case INVALID_ACCOUNT -> "Invalid account number";
            case INVALID_CURRENCY -> "Unsupported currency";
            case INVALID_AMOUNT -> "Invalid payment amount";
            case DUPLICATE_PAYMENT -> "Payment already processed";
            case INVALID_STATUS_TRANSITION -> "Invalid payment status transition";
            case PAYMENT_NOT_FOUND -> "Payment not found";
            case PROCESSING_ERROR -> "Processing error occurred";
            case NETWORK_ERROR -> "Network connection error";
        };
    }

    /**
     * Get a user-friendly message by error code string.
     * 
     * @param errorCodeString The error code as a string
     * @return A user-friendly explanation, or a default message if code is not found
     */
    public static String getUserFriendlyMessageByString(String errorCodeString) {
        try {
            ErrorCode errorCode = ErrorCode.valueOf(errorCodeString);
            return getUserFriendlyMessage(errorCode);
        } catch (IllegalArgumentException ex) {
            return "An unexpected error occurred. Please contact support.";
        }
    }
}

