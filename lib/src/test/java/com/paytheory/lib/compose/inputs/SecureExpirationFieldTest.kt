package com.paytheory.lib.compose.inputs

import org.junit.Assert.assertEquals
import org.junit.Test

class SecureExpirationFieldTest {

    @Test
    fun `formatCardExpiryDate_emptyInput_returnsEmptyString`() {
        assertEquals("", formatCardExpiryDate(""))
    }

    @Test
    fun `formatCardExpiryDate_nonDigitCharacters_ignoresNonDigits`() {
        assertEquals("12/34", formatCardExpiryDate("1a2b-3c4d"))
        assertEquals("02", formatCardExpiryDate("0w2"))
        assertEquals("03", formatCardExpiryDate("3!"))
    }

    @Test
    fun `formatCardExpiryDate_singleDigitMonth_formatsCorrectly`() {
        assertEquals("1", formatCardExpiryDate("1"))
        assertEquals("0", formatCardExpiryDate("0"))
        assertEquals("02", formatCardExpiryDate("2"))  // 2 → 02
        assertEquals("09", formatCardExpiryDate("9"))  // 9 → 09
    }

    @Test
    fun `formatCardExpiryDate_twoDigitMonth_formatsCorrectly`() {
        assertEquals("12", formatCardExpiryDate("12"))
        assertEquals("01", formatCardExpiryDate("01"))
        assertEquals("02", formatCardExpiryDate("02"))
        assertEquals("12", formatCardExpiryDate("13"))  // 13 → 12
        assertEquals("12", formatCardExpiryDate("99"))  // 99 → 12
    }

    @Test
    fun `formatCardExpiryDate_partialYear_formatsCorrectly`() {
        assertEquals("12/3", formatCardExpiryDate("123"))
        assertEquals("12/34", formatCardExpiryDate("1234"))
        assertEquals("02/25", formatCardExpiryDate("0225"))
        assertEquals("12/34", formatCardExpiryDate("123456"))  // Truncate to 4 digits
    }

    @Test
    fun `formatCardExpiryDate_monthWithLeadingZero_handlesCorrectly`() {
        assertEquals("0", formatCardExpiryDate("0"))
        assertEquals("01", formatCardExpiryDate("01"))
        assertEquals("02/3", formatCardExpiryDate("023"))
        assertEquals("00", formatCardExpiryDate("00"))  // Invalid but formatted anyway
    }

    @Test
    fun `formatCardExpiryDate_invalidMonth_coercesToValidMonth`() {
        assertEquals("12", formatCardExpiryDate("13"))  // 13 → 12
        assertEquals("12", formatCardExpiryDate("50"))  // 50 → 12
        assertEquals("12", formatCardExpiryDate("99"))  // 99 → 12
    }

    @Test
    fun `formatCardExpiryDate_realWorldPatterns_formatsCorrectly`() {
        assertEquals("12/34", formatCardExpiryDate("12/34"))
        assertEquals("12/34", formatCardExpiryDate("12 34"))
        assertEquals("12/3", formatCardExpiryDate("12/3"))
        assertEquals("02/20", formatCardExpiryDate("0220"))
    }

    @Test
    fun `formatCardExpiryDate_edgeCases_handlesCorrectly`() {
        assertEquals("1", formatCardExpiryDate("1"))
        assertEquals("12", formatCardExpiryDate("12"))
        assertEquals("12/3", formatCardExpiryDate("123"))
        assertEquals("12/34", formatCardExpiryDate("1234"))
        assertEquals("12/34", formatCardExpiryDate("12345"))  // Extra digits ignored
    }
}