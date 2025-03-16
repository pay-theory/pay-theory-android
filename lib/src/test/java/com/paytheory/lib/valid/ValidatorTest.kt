package com.paytheory.lib.valid

import com.paytheory.lib.compose.string.SecureStringWrapper
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
    fun `luhnCheck returns true for valid card numbers`() {
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
    fun `luhnCheck returns false for invalid card numbers`() {
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
    fun `isValidCardNumber handles length constraints`() {
        assertTrue(validator.isValidCardNumber(mockSecureStringWrapper("4111111111111111"))) // 16 digits
        assertFalse(validator.isValidCardNumber(mockSecureStringWrapper("411111"))) // Too short
        assertFalse(validator.isValidCardNumber(mockSecureStringWrapper("1".repeat(20)))) // Too long
    }

    @Test
    fun `routingCheck validates correct routing numbers`() {
        // Valid routing number example from comment
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("054000030")))
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("325084426")))

        // Invalid check digit
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("024000026")))
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("024000025")))
    }

    @Test
    fun `isValidBankRoutingNumber requires exact 9 digits`() {
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("111000038")))
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("12345678")))
        assertFalse(validator.isValidBankRoutingNumber(mockSecureStringWrapper("1234567890")))
    }

    @Test
    fun `isValidBankAccountNumber checks length constraints`() {
        assertTrue(validator.isValidBankAccountNumber(mockSecureStringWrapper("1234567")))  // 7 digits
        assertTrue(validator.isValidBankAccountNumber(mockSecureStringWrapper("1".repeat(17)))) // 17 digits
        assertFalse(validator.isValidBankAccountNumber(mockSecureStringWrapper("123456"))) // 6 digits
        assertFalse(validator.isValidBankAccountNumber(mockSecureStringWrapper("1".repeat(18)))) // 18 digits
    }

    @Test
    fun `isValidExpiration validates MMYY format`() {
        assertTrue(validator.isValidExpiration(mockSecureStringWrapper("12/25")))
        assertTrue(validator.isValidExpiration(mockSecureStringWrapper("01/30")))

        assertFalse(validator.isValidExpiration(mockSecureStringWrapper("13/25"))) // Invalid month
        assertFalse(validator.isValidExpiration(mockSecureStringWrapper("12/3")))  // Short year
        assertFalse(validator.isValidExpiration(mockSecureStringWrapper("1225")))  // Missing slash
    }

    @Test
    fun `isValidCvc checks minimum length`() {
        assertTrue(validator.isValidCvc(mockSecureStringWrapper("123")))
        assertTrue(validator.isValidCvc(mockSecureStringWrapper("1234")))
        assertFalse(validator.isValidCvc(mockSecureStringWrapper("12")))
    }

    @Test
    fun `isValidPostalCode validates length`() {
        assertTrue(validator.isValidPostalCode(mockSecureStringWrapper("12345")))     // 5 digits
        assertTrue(validator.isValidPostalCode(mockSecureStringWrapper("123456")))    // 6 digits
        assertFalse(validator.isValidPostalCode(mockSecureStringWrapper("1234")))      // 4 digits
        assertFalse(validator.isValidPostalCode(mockSecureStringWrapper("1234567")))   // 7 digits
    }

    @Test
    fun `isNotEmpty checks for non-empty strings`() {
        assertTrue(validator.isNotEmpty(mockSecureStringWrapper("valid")))
        assertFalse(validator.isNotEmpty(mockSecureStringWrapper("")))
    }

    @Test
    fun `sanitization removes non-digit characters`() {
        // Test with formatted card number
        assertTrue(validator.isValidCardNumber(mockSecureStringWrapper("4111-1111-1111-1111")))
        // Test with routing number containing spaces
        assertTrue(validator.isValidBankRoutingNumber(mockSecureStringWrapper("0260-0959-3")))
    }
}