package com.neueda.validator;

import com.neueda.exception.ValidationException;
import com.neueda.model.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentValidatorTest {

    @Test
    void validateAmountAcceptsBoundaryValues() {
        assertDoesNotThrow(() -> PaymentValidator.validateAmount(new BigDecimal("0.01")));
        assertDoesNotThrow(() -> PaymentValidator.validateAmount(new BigDecimal("1000000.00")));
        assertDoesNotThrow(() -> PaymentValidator.validateAmount(new BigDecimal("100")));
    }

    @Test
    void validateAmountRejectsNullZeroNegativeTooLargeAndTooManyDecimals() {
        ValidationException nullException = assertThrows(ValidationException.class, () -> PaymentValidator.validateAmount(null));
        ValidationException zeroException = assertThrows(ValidationException.class, () -> PaymentValidator.validateAmount(BigDecimal.ZERO));
        ValidationException negativeException = assertThrows(ValidationException.class, () -> PaymentValidator.validateAmount(new BigDecimal("-0.01")));
        ValidationException maxException = assertThrows(ValidationException.class, () -> PaymentValidator.validateAmount(new BigDecimal("1000000.01")));
        ValidationException scaleException = assertThrows(ValidationException.class, () -> PaymentValidator.validateAmount(new BigDecimal("10.999")));

        assertEquals(ErrorCode.INVALID_AMOUNT, nullException.getErrorCode());
        assertEquals(ErrorCode.INVALID_AMOUNT, zeroException.getErrorCode());
        assertEquals(ErrorCode.INVALID_AMOUNT, negativeException.getErrorCode());
        assertEquals(ErrorCode.INVALID_AMOUNT, maxException.getErrorCode());
        assertEquals(ErrorCode.INVALID_AMOUNT, scaleException.getErrorCode());
    }

    @Test
    void validateAccountsAcceptsValidBoundaryLengths() {
        assertDoesNotThrow(() -> PaymentValidator.validateAccounts("ABC12345", "ZYX98765"));
        assertDoesNotThrow(() -> PaymentValidator.validateAccounts("ABCDEFGH123456789012", "HGFEDCBA123456789012"));
    }

    @Test
    void validateAccountsRejectsBlankSameAndInvalidFormats() {
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts(null, "DEST1234")).getErrorCode());
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts("   ", "DEST1234")).getErrorCode());
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts("SOURCE123", "")).getErrorCode());
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts("SAME1234", "SAME1234")).getErrorCode());
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts("SHORT1", "TARGET123")).getErrorCode());
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts("SOURCE123", "TOO-LONG-INVALID-ACCOUNT-123")).getErrorCode());
        assertEquals(ErrorCode.INVALID_ACCOUNT,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateAccounts("SRC!1234", "TARGET123")).getErrorCode());
    }

    @Test
    void validateCurrencyAcceptsTrimmedCaseInsensitiveSupportedValue() {
        assertDoesNotThrow(() -> PaymentValidator.validateCurrency(" usd "));
    }

    @Test
    void validateCurrencyRejectsNullBlankAndUnsupportedValues() {
        assertEquals(ErrorCode.INVALID_CURRENCY,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateCurrency(null)).getErrorCode());
        assertEquals(ErrorCode.INVALID_CURRENCY,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateCurrency(" ")).getErrorCode());
        assertEquals(ErrorCode.INVALID_CURRENCY,
            assertThrows(ValidationException.class, () -> PaymentValidator.validateCurrency("BTC")).getErrorCode());
    }

    @Test
    void validateIdempotencyKeyAcceptsValidAndBoundaryLengthValues() {
        assertDoesNotThrow(() -> PaymentValidator.validateIdempotencyKey("idem-key"));
        assertDoesNotThrow(() -> PaymentValidator.validateIdempotencyKey("x".repeat(255)));
    }

    @Test
    void validateIdempotencyKeyRejectsNullBlankAndOversizedValues() {
        ValidationException nullException = assertThrows(ValidationException.class, () -> PaymentValidator.validateIdempotencyKey(null));
        ValidationException blankException = assertThrows(ValidationException.class, () -> PaymentValidator.validateIdempotencyKey("   "));
        ValidationException longException = assertThrows(ValidationException.class, () -> PaymentValidator.validateIdempotencyKey("x".repeat(256)));

        assertEquals(ErrorCode.VALIDATION_FAILED, nullException.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_FAILED, blankException.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_FAILED, longException.getErrorCode());
    }

    @Test
    void getSupportedCurrenciesReturnsDefensiveCopy() {
        Set<String> supportedCurrencies = PaymentValidator.getSupportedCurrencies();
        supportedCurrencies.add("ZZZ");

        assertTrue(supportedCurrencies.contains("ZZZ"));
        assertFalse(PaymentValidator.getSupportedCurrencies().contains("ZZZ"));
    }
}

