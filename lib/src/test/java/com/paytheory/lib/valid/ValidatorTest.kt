package com.paytheory.lib.valid

import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the [Validator] class.
 * Tests validation methods for different types of payment information.
 */
class ValidatorTest {
    private lateinit var validator: Validator

    private fun mockSecureStringWrapper(value: String): SecureStringWrapper {
        val mock = mockk<SecureStringWrapper>()
        every { mock.secureValue.revealForUi() } returns value
        return mock
    }

    @Before
    fun setup() {
        validator = Validator()
    }

    @Test
    fun `luhnCheck_validCardNumbers_returnsTrue`() {
        val validNumbers = listOf(
            "4111111111111111",  // Visa
            "5555555555554444",  // Mastercard
            "378282246310005"   // Amex
        )

        validNumbers.forEach { number ->
            assertTrue(validator.isValidCardNumber(mockSecureStringWrapper(number)))
        }
    }

    @Test
    fun `luhnCheck_invalidCardNumbers_returnsFalse`() {
        val invalidNumbers = listOf(
            "4111111111111112",
            "5555555555554445",
            "1234567812345678"
        )

        invalidNumbers.forEach { number ->
            assertFalse(validator.isValidCardNumber(mockSecureStringWrapper(number)))
        }
    }

    @Test
    fun `isValidCardNumber_differentLengths_enforcesConstraints`() {
        assertTrue(validator.isValidCardNumber(mockSecureStringWrapper("4111111111111111"))) // 16 digits
        assertFalse(validator.isValidCardNumber(mockSecureStringWrapper("411111"))) // Too short
        assertFalse(validator.isValidCardNumber(mockSecureStringWrapper("1".repeat(20)))) // Too long
    }

    @Test
    fun `routingCheck_validInput_returnsTrue`() {
        // Valid routing number example from comment
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("054000030")))
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("325084426")))

        // Invalid check digit
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("024000026")))
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("024000025")))
    }

    @Test
    fun `isValidBankRoutingNumber_nonNineDigitInput_returnsFalse`() {
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("111000038")))
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("12345678")))
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("1234567890")))
    }

    @Test
    fun `isValidBankAccountNumber_variousLengths_enforcesConstraints`() {
        assertTrue(validator.isValidBankAccountNumber(mockSecureStringWrapper("1234567")))  // 7 digits
        assertTrue(validator.isValidBankAccountNumber(mockSecureStringWrapper("1".repeat(17)))) // 17 digits
        assertFalse(validator.isValidBankAccountNumber(mockSecureStringWrapper("123456"))) // 6 digits
        assertFalse(validator.isValidBankAccountNumber(mockSecureStringWrapper("1".repeat(18)))) // 18 digits
    }

    @Test
    fun `isValidExpiration_differentFormats_validatesCorrectly`() {
        assertTrue(validator.isValidExpiration(mockSecureStringWrapper("12/25")))
        assertTrue(validator.isValidExpiration(mockSecureStringWrapper("01/30")))

        assertFalse(validator.isValidExpiration(mockSecureStringWrapper("13/25"))) // Invalid month
        assertFalse(validator.isValidExpiration(mockSecureStringWrapper("12/3")))  // Short year
        assertFalse(validator.isValidExpiration(mockSecureStringWrapper("1225")))  // Missing slash
    }

    @Test
    fun `isValidCvc_shortInput_returnsFalse`() {
        assertTrue(validator.isValidCvc(mockSecureStringWrapper("123")))
        assertTrue(validator.isValidCvc(mockSecureStringWrapper("1234")))
        assertFalse(validator.isValidCvc(mockSecureStringWrapper("12")))
    }

    @Test
    fun `isValidPostalCode_differentLengths_validatesCorrectly`() {
        assertTrue(validator.isValidPostalCode(mockSecureStringWrapper("12345")))     // 5 digits
        assertTrue(validator.isValidPostalCode(mockSecureStringWrapper("123456")))    // 6 digits
        assertFalse(validator.isValidPostalCode(mockSecureStringWrapper("1234")))      // 4 digits
        assertFalse(validator.isValidPostalCode(mockSecureStringWrapper("1234567")))   // 7 digits
    }

    @Test
    fun `isNotEmpty_variousInputs_detectsEmptyStrings`() {
        assertTrue(validator.isNotEmpty(mockSecureStringWrapper("valid")))
        assertFalse(validator.isNotEmpty(mockSecureStringWrapper("")))
    }

    @Test
    fun `sanitization_mixedInput_removesNonDigits`() {
        // Test with formatted card number
        assertTrue(validator.isValidCardNumber(mockSecureStringWrapper("4111-1111-1111-1111")))
        // Test with routing number containing spaces
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("0260-0959-3")))
    }

    @Test
    fun `isValidBankAccountNumber should validate account numbers correctly`() {
        // Valid account numbers (between 7-17 digits)
        assertTrue(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("1234567"), null)))
        assertTrue(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("12345678901"), null)))
        assertTrue(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("12345678901234567"), null)))
        
        // Invalid account numbers (too short or too long)
        assertFalse(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("123456"), null)))
        assertFalse(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("123456789012345678"), null)))
        
        // Invalid account numbers (non-digits are ignored when counting length)
        assertFalse(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("123-456"), null)))
        assertTrue(validator.isValidBankAccountNumber(SecureStringWrapper(SecureString("123-4567"), null)))
    }

    @Test
    fun `isValidBankRoutingNumber should validate routing numbers correctly`() {
        // Valid routing number (9 digits and passes check)
        // Using a known valid routing number format for test
        assertTrue(validator.isValidBankRoutingNumber(SecureStringWrapper(SecureString("122105155"), null)))
        
        // Invalid routing numbers (wrong length)
        assertFalse(validator.isValidBankRoutingNumber(SecureStringWrapper(SecureString("12345678"), null)))
        assertFalse(validator.isValidBankRoutingNumber(SecureStringWrapper(SecureString("1234567890"), null)))
        
        // Invalid routing numbers (wrong check digit)
        assertFalse(validator.isValidBankRoutingNumber(SecureStringWrapper(SecureString("122105156"), null)))
    }

    @Test
    fun `isValidCardNumber should validate card numbers correctly`() {
        // Valid card numbers (passes Luhn check)
        assertTrue(validator.isValidCardNumber(SecureStringWrapper(SecureString("4111111111111111"), null))) // Visa
        assertTrue(validator.isValidCardNumber(SecureStringWrapper(SecureString("5555555555554444"), null))) // Mastercard
        assertTrue(validator.isValidCardNumber(SecureStringWrapper(SecureString("378282246310005"), null))) // Amex
        
        // Invalid card numbers (fails Luhn check)
        assertFalse(validator.isValidCardNumber(SecureStringWrapper(SecureString("4111111111111112"), null)))
        
        // Invalid card numbers (wrong length)
        assertFalse(validator.isValidCardNumber(SecureStringWrapper(SecureString("41111111"), null)))
        assertFalse(validator.isValidCardNumber(SecureStringWrapper(SecureString("41111111111111111"), null)))
        
        // Valid with formatting (formatting is ignored)
        assertTrue(validator.isValidCardNumber(SecureStringWrapper(SecureString("4111-1111-1111-1111"), null)))
    }

    @Test
    fun `isValidExpiration should validate expiration dates correctly`() {
        // Valid expiration dates (MM/YY format)
        assertTrue(validator.isValidExpiration(SecureStringWrapper(SecureString("01/23"), null)))
        assertTrue(validator.isValidExpiration(SecureStringWrapper(SecureString("12/99"), null)))
        
        // Invalid expiration dates (wrong format)
        assertFalse(validator.isValidExpiration(SecureStringWrapper(SecureString("1/23"), null)))
        assertFalse(validator.isValidExpiration(SecureStringWrapper(SecureString("01-23"), null)))
        assertFalse(validator.isValidExpiration(SecureStringWrapper(SecureString("13/23"), null))) // invalid month
        assertFalse(validator.isValidExpiration(SecureStringWrapper(SecureString("00/23"), null))) // invalid month
    }

    @Test
    fun `isValidCvc should validate CVCs correctly`() {
        // Valid CVCs (3 or more digits)
        assertTrue(validator.isValidCvc(SecureStringWrapper(SecureString("123"), null)))
        assertTrue(validator.isValidCvc(SecureStringWrapper(SecureString("1234"), null)))
        
        // Invalid CVCs (too short)
        assertFalse(validator.isValidCvc(SecureStringWrapper(SecureString("12"), null)))
        assertFalse(validator.isValidCvc(SecureStringWrapper(SecureString(""), null)))
    }

    @Test
    fun `isValidPostalCode should validate postal codes correctly`() {
        // Valid postal codes (5-6 characters)
        assertTrue(validator.isValidPostalCode(SecureStringWrapper(SecureString("12345"), null)))
        assertTrue(validator.isValidPostalCode(SecureStringWrapper(SecureString("123456"), null)))
        
        // Invalid postal codes (too short or too long)
        assertFalse(validator.isValidPostalCode(SecureStringWrapper(SecureString("1234"), null)))
        assertFalse(validator.isValidPostalCode(SecureStringWrapper(SecureString("1234567"), null)))
    }

    @Test
    fun `isNotEmpty should validate non-empty strings correctly`() {
        // Non-empty strings
        assertTrue(validator.isNotEmpty(SecureStringWrapper(SecureString("test"), null)))
        assertTrue(validator.isNotEmpty(SecureStringWrapper(SecureString(" "), null)))
        
        // Empty strings
        assertFalse(validator.isNotEmpty(SecureStringWrapper(SecureString(""), null)))
    }
}