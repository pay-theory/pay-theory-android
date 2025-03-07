package com.paytheory.lib.compose.inputs

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpiryDateFormatterTest {

    @Test
    fun `empty input returns empty string`() {
        assertEquals("", formatCardExpiryDate(""))
    }

    @Test
    fun `non-digit characters are ignored`() {
        assertEquals("12/34", formatCardExpiryDate("1a2b-3c4d"))
        assertEquals("02", formatCardExpiryDate("0w2"))
        assertEquals("03", formatCardExpiryDate("3!"))
    }

    @Test
    fun `single digit month formatting`() {
        assertEquals("1", formatCardExpiryDate("1"))
        assertEquals("0", formatCardExpiryDate("0"))
        assertEquals("02", formatCardExpiryDate("2"))  // 2 → 02
        assertEquals("09", formatCardExpiryDate("9"))  // 9 → 09
    }

    @Test
    fun `two digit month formatting`() {
        assertEquals("12", formatCardExpiryDate("12"))
        assertEquals("01", formatCardExpiryDate("01"))
        assertEquals("02", formatCardExpiryDate("02"))
        assertEquals("12", formatCardExpiryDate("13"))  // 13 → 12
        assertEquals("12", formatCardExpiryDate("99"))  // 99 → 12
    }

    @Test
    fun `partial year formatting`() {
        assertEquals("12/3", formatCardExpiryDate("123"))
        assertEquals("12/34", formatCardExpiryDate("1234"))
        assertEquals("02/25", formatCardExpiryDate("0225"))
        assertEquals("12/34", formatCardExpiryDate("123456"))  // Truncate to 4 digits
    }

    @Test
    fun `month with leading zero handling`() {
        assertEquals("0", formatCardExpiryDate("0"))
        assertEquals("01", formatCardExpiryDate("01"))
        assertEquals("02/3", formatCardExpiryDate("023"))
        assertEquals("00", formatCardExpiryDate("00"))  // Invalid but formatted anyway
    }

    @Test
    fun `month coercion behavior`() {
        assertEquals("12", formatCardExpiryDate("13"))  // 13 → 12
        assertEquals("12", formatCardExpiryDate("50"))  // 50 → 12
        assertEquals("12", formatCardExpiryDate("99"))  // 99 → 12
    }

    @Test
    fun `real-world patterns`() {
        assertEquals("12/34", formatCardExpiryDate("12/34"))
        assertEquals("12/34", formatCardExpiryDate("12 34"))
        assertEquals("12/3", formatCardExpiryDate("12/3"))
        assertEquals("02/20", formatCardExpiryDate("0220"))
    }

    @Test
    fun `edge cases`() {
        assertEquals("1", formatCardExpiryDate("1"))
        assertEquals("12", formatCardExpiryDate("12"))
        assertEquals("12/3", formatCardExpiryDate("123"))
        assertEquals("12/34", formatCardExpiryDate("1234"))
        assertEquals("12/34", formatCardExpiryDate("12345"))  // Extra digits ignored
    }
}