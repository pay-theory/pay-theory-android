package com.paytheory.lib.compose.utility

import org.junit.Assert.*
import org.junit.Test

class BankFieldUtilsTest {

    @Test
    fun `formatBankAccountNumber with empty string returns empty string`() {
        assertEquals("", BankFieldUtils.formatBankAccountNumber(""))
    }

    @Test
    fun `formatBankAccountNumber removes non-digit characters`() {
        assertEquals("12345678", BankFieldUtils.formatBankAccountNumber("1234-5678"))
        assertEquals("12345678", BankFieldUtils.formatBankAccountNumber("1234 5678"))
        assertEquals("12345678", BankFieldUtils.formatBankAccountNumber("1a2b3c4d5e6f7g8h"))
    }

    @Test
    fun `isValidBankAccountNumber with empty string returns false`() {
        assertFalse(BankFieldUtils.isValidBankAccountNumber(""))
    }

    @Test
    fun `isValidBankAccountNumber with too few digits returns false`() {
        assertFalse(BankFieldUtils.isValidBankAccountNumber("1234567")) // 7 digits
    }

    @Test
    fun `isValidBankAccountNumber with sufficient digits returns true`() {
        assertTrue(BankFieldUtils.isValidBankAccountNumber("12345678")) // 8 digits
        assertTrue(BankFieldUtils.isValidBankAccountNumber("123456789012345")) // 15 digits
        assertTrue(BankFieldUtils.isValidBankAccountNumber("12345678901234567")) // 17 digits
    }

    @Test
    fun `isValidBankAccountNumber with too many digits returns false`() {
        assertFalse(BankFieldUtils.isValidBankAccountNumber("123456789012345678")) // 18 digits
    }

    @Test
    fun `formatRoutingNumber with empty string returns empty string`() {
        assertEquals("", BankFieldUtils.formatRoutingNumber(""))
    }

    @Test
    fun `formatRoutingNumber removes non-digit characters`() {
        assertEquals("123456789", BankFieldUtils.formatRoutingNumber("123-456-789"))
        assertEquals("123456789", BankFieldUtils.formatRoutingNumber("123 456 789"))
        assertEquals("123456789", BankFieldUtils.formatRoutingNumber("1a2b3c4d5e6f7g8i9j"))
    }

    @Test
    fun `isValidRoutingNumber with empty string returns false`() {
        assertFalse(BankFieldUtils.isValidRoutingNumber(""))
    }

    @Test
    fun `isValidRoutingNumber with incorrect length returns false`() {
        assertFalse(BankFieldUtils.isValidRoutingNumber("12345678")) // 8 digits
        assertFalse(BankFieldUtils.isValidRoutingNumber("1234567890")) // 10 digits
    }

    @Test
    fun `isValidRoutingNumber with valid test routing numbers returns true`() {
        // Valid ABA routing numbers used for testing
        assertTrue(BankFieldUtils.isValidRoutingNumber("122105155"))
        assertTrue(BankFieldUtils.isValidRoutingNumber("021000021"))
        assertTrue(BankFieldUtils.isValidRoutingNumber("011401533"))
    }

    @Test
    fun `isValidRoutingNumber with invalid test routing numbers returns false`() {
        // Numbers with incorrect checksums
        assertFalse(BankFieldUtils.isValidRoutingNumber("122105156"))
        assertFalse(BankFieldUtils.isValidRoutingNumber("021000022"))
    }
} 