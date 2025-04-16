package com.paytheory.lib.compose.utility

import org.junit.Assert.*
import org.junit.Test

class CardFieldUtilsTest {

    @Test
    fun `formatCardNumber with empty string returns empty string`() {
        assertEquals("", CardFieldUtils.formatCardNumber(""))
    }

    @Test
    fun `formatCardNumber with non-digit characters formats correctly`() {
        assertEquals("1234 5678 9012 3456", CardFieldUtils.formatCardNumber("1234-5678-9012-3456"))
        assertEquals("1234 5678 9012 3456", CardFieldUtils.formatCardNumber("1234 5678 9012 3456"))
        assertEquals("1234 5678 9012 3456", CardFieldUtils.formatCardNumber("1234abcd5678efgh9012ijkl3456"))
    }

    @Test
    fun `formatCardNumber with incomplete card number formats correctly`() {
        assertEquals("1234", CardFieldUtils.formatCardNumber("1234"))
        assertEquals("1234 5", CardFieldUtils.formatCardNumber("12345"))
        assertEquals("1234 56", CardFieldUtils.formatCardNumber("123456"))
    }

    @Test
    fun `isValidCardNumber with empty string returns false`() {
        assertFalse(CardFieldUtils.isValidCardNumber(""))
    }

    @Test
    fun `isValidCardNumber with too few digits returns false`() {
        assertFalse(CardFieldUtils.isValidCardNumber("1234"))
        assertFalse(CardFieldUtils.isValidCardNumber("123456789012")) // 12 digits
    }

    @Test
    fun `isValidCardNumber with valid length returns true`() {
        assertTrue(CardFieldUtils.isValidCardNumber("1234567890123")) // 13 digits
        assertTrue(CardFieldUtils.isValidCardNumber("4111111111111111")) // 16 digits
        assertTrue(CardFieldUtils.isValidCardNumber("1234567890123456789")) // 19 digits
    }

    @Test
    fun `isValidCardNumber with too many digits returns false`() {
        assertFalse(CardFieldUtils.isValidCardNumber("12345678901234567890")) // 20 digits
    }

    @Test
    fun `passesLuhnCheck with empty string returns false`() {
        assertFalse(CardFieldUtils.passesLuhnCheck(""))
    }

    @Test
    fun `passesLuhnCheck with valid test cards returns true`() {
        // Valid test cards that pass Luhn check
        assertTrue(CardFieldUtils.passesLuhnCheck("4111111111111111")) // Visa test
        assertTrue(CardFieldUtils.passesLuhnCheck("5555555555554444")) // Mastercard test
        assertTrue(CardFieldUtils.passesLuhnCheck("378282246310005")) // Amex test
        assertTrue(CardFieldUtils.passesLuhnCheck("6011111111111117")) // Discover test
    }

    @Test
    fun `passesLuhnCheck with invalid test cards returns false`() {
        // Invalid cards that don't pass Luhn check
        assertFalse(CardFieldUtils.passesLuhnCheck("4111111111111112"))
        assertFalse(CardFieldUtils.passesLuhnCheck("5555555555554445"))
    }

    @Test
    fun `detectCardNetwork correctly identifies Visa`() {
        assertEquals(CardNetwork.VISA, CardFieldUtils.detectCardNetwork("4111111111111111"))
        assertEquals(CardNetwork.VISA, CardFieldUtils.detectCardNetwork("4012888888881881"))
    }

    @Test
    fun `detectCardNetwork correctly identifies Mastercard`() {
        assertEquals(CardNetwork.MASTERCARD, CardFieldUtils.detectCardNetwork("5555555555554444"))
        assertEquals(CardNetwork.MASTERCARD, CardFieldUtils.detectCardNetwork("5105105105105100"))
    }

    @Test
    fun `detectCardNetwork correctly identifies Amex`() {
        assertEquals(CardNetwork.AMEX, CardFieldUtils.detectCardNetwork("378282246310005"))
        assertEquals(CardNetwork.AMEX, CardFieldUtils.detectCardNetwork("371449635398431"))
    }

    @Test
    fun `detectCardNetwork correctly identifies Discover`() {
        assertEquals(CardNetwork.DISCOVER, CardFieldUtils.detectCardNetwork("6011111111111117"))
        assertEquals(CardNetwork.DISCOVER, CardFieldUtils.detectCardNetwork("6011000990139424"))
    }

    @Test
    fun `detectCardNetwork returns null for unknown card types`() {
        assertNull(CardFieldUtils.detectCardNetwork("9999999999999999"))
        assertNull(CardFieldUtils.detectCardNetwork(""))
    }
} 