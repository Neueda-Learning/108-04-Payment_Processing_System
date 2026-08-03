package com.neueda.validator;

import com.neueda.exception.ValidationException;
import com.neueda.model.ErrorCode;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator class for payment data.
 * Implements all validation rules specified in the payment processing requirements.
 */
public class PaymentValidator {

    // Supported ISO 4217 currency codes
    private static final Set<String> SUPPORTED_CURRENCIES = new HashSet<>(Arrays.asList(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "INR", "MXN"
    ));

    // Payment amount constraints
    private static final BigDecimal MIN_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");
    private static final int MAX_DECIMAL_PLACES = 2;

    /**
     * Validate payment amount.
     * Rules:
     * - Amount must be greater than 0
     * - Amount must not exceed 1,000,000
     * - Amount must have maximum 2 decimal places
     */
    public static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ValidationException(ErrorCode.INVALID_AMOUNT, "Amount cannot be null");
        }

        if (amount.compareTo(MIN_AMOUNT) <= 0) {
            throw new ValidationException(ErrorCode.INVALID_AMOUNT, 
                "Amount must be greater than 0, provided: " + amount);
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException(ErrorCode.INVALID_AMOUNT, 
                "Amount must not exceed " + MAX_AMOUNT + ", provided: " + amount);
        }

        // Check decimal places
        if (amount.scale() > MAX_DECIMAL_PLACES) {
            throw new ValidationException(ErrorCode.INVALID_AMOUNT, 
                "Amount must have maximum " + MAX_DECIMAL_PLACES + " decimal places, provided: " + amount);
        }
    }

    /**
     * Validate account numbers.
     * Rules:
     * - Source and destination accounts must be different
     * - Account numbers must be valid format (non-empty strings)
     */
    public static void validateAccounts(String sourceAccount, String destinationAccount) {
        if (sourceAccount == null || sourceAccount.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_ACCOUNT, "Source account cannot be null or empty");
        }

        if (destinationAccount == null || destinationAccount.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_ACCOUNT, "Destination account cannot be null or empty");
        }

        if (sourceAccount.equals(destinationAccount)) {
            throw new ValidationException(ErrorCode.INVALID_ACCOUNT, 
                "Source and destination accounts must be different");
        }

        // Validate account format (example: account should be alphanumeric, 8-20 chars)
        if (!isValidAccountFormat(sourceAccount)) {
            throw new ValidationException(ErrorCode.INVALID_ACCOUNT, 
                "Source account format is invalid: " + sourceAccount);
        }

        if (!isValidAccountFormat(destinationAccount)) {
            throw new ValidationException(ErrorCode.INVALID_ACCOUNT, 
                "Destination account format is invalid: " + destinationAccount);
        }
    }

    /**
     * Validate currency code.
     * Rules:
     * - Currency code must be valid ISO 4217 (e.g., USD, EUR, GBP)
     * - System must support the specified currency
     */
    public static void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new ValidationException(ErrorCode.INVALID_CURRENCY, "Currency cannot be null or empty");
        }

        String currencyCode = currency.trim().toUpperCase();

        if (!SUPPORTED_CURRENCIES.contains(currencyCode)) {
            throw new ValidationException(ErrorCode.INVALID_CURRENCY, 
                "Currency " + currencyCode + " is not supported. Supported currencies: " + SUPPORTED_CURRENCIES);
        }
    }

    /**
     * Validate idempotency key format.
     */
    public static void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ValidationException("Idempotency key cannot be null or empty");
        }

        if (idempotencyKey.length() > 255) {
            throw new ValidationException("Idempotency key must be 255 characters or less");
        }
    }

    /**
     * Helper method to validate account format.
     * Accounts should be alphanumeric and 8-20 characters long.
     */
    private static boolean isValidAccountFormat(String account) {
        if (account == null || account.isEmpty()) {
            return false;
        }
        return account.matches("^[a-zA-Z0-9]{8,20}$");
    }

    /**
     * Get supported currencies.
     */
    public static Set<String> getSupportedCurrencies() {
        return new HashSet<>(SUPPORTED_CURRENCIES);
    }
}

