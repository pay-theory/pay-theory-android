package com.paytheory.lib.utils

import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Tests for CurrencyUtils helper class.
 * These tests verify currency formatting, conversion, and validation functions.
 */
class CurrencyUtilsTest {
    
    @Test
    fun `centsToDollars should convert cents to dollars correctly`() {
        // Given
        val testCases = mapOf(
            1000 to BigDecimal("10.00"),
            1234 to BigDecimal("12.34"),
            50 to BigDecimal("0.50"),
            1 to BigDecimal("0.01"),
            0 to BigDecimal("0.00"),
            -100 to BigDecimal("-1.00")
        )
        
        // When/Then
        testCases.forEach { (cents, expected) ->
            val result = CurrencyUtils.centsToDollars(cents)
            assertEquals("Failed for $cents cents", expected, result)
        }
    }
    
    @Test
    fun `dollarsToCents should convert dollars to cents correctly`() {
        // Given
        val testCases = mapOf(
            BigDecimal("10.00") to 1000,
            BigDecimal("12.34") to 1234,
            BigDecimal("0.50") to 50,
            BigDecimal("0.01") to 1,
            BigDecimal("0.00") to 0,
            BigDecimal("-1.00") to -100
        )
        
        // When/Then
        testCases.forEach { (dollars, expected) ->
            val result = CurrencyUtils.dollarsToCents(dollars)
            assertEquals("Failed for $dollars dollars", expected, result)
        }
    }
    
    @Test
    fun `formatCents should format cents as currency string correctly`() {
        // Given
        val amount = 1095 // $10.95
        
        // When
        val result = CurrencyUtils.formatCents(amount)
        
        // Then
        assertEquals("$10.95", result)
    }
    
    @Test
    fun `formatCents should respect different currency codes`() {
        // Given
        val amount = 1095 // 10.95
        
        // When
        val resultEUR = CurrencyUtils.formatCents(amount, "EUR", Locale.GERMANY)
        val resultGBP = CurrencyUtils.formatCents(amount, "GBP", Locale.UK)
        val resultJPY = CurrencyUtils.formatCents(amount, "JPY", Locale.JAPAN)
        
        // Then
        assertTrue(resultEUR.contains("10,95") && resultEUR.contains("€"))
        assertTrue(resultGBP.contains("10.95") && resultGBP.contains("£"))
        
        // JPY doesn't use decimal places
        assertTrue(resultJPY.contains("11") || resultJPY.contains("10.95"))
        assertTrue(resultJPY.contains("¥") || resultJPY.contains("￥"))
    }
    
    @Test
    fun `formatAmount should format BigDecimal amount correctly`() {
        // Given
        val amount = BigDecimal("10.95")
        
        // When
        val result = CurrencyUtils.formatAmount(amount)
        
        // Then
        assertEquals("$10.95", result)
    }
    
    @Test
    fun `formatAmount should respect different currency codes`() {
        // Given
        val amount = BigDecimal("10.95")
        
        // When
        val resultEUR = CurrencyUtils.formatAmount(amount, "EUR", Locale.GERMANY)
        val resultGBP = CurrencyUtils.formatAmount(amount, "GBP", Locale.UK)
        
        // Then
        assertTrue(resultEUR.contains("10,95") && resultEUR.contains("€"))
        assertTrue(resultGBP.contains("10.95") && resultGBP.contains("£"))
    }
    
    @Test
    fun `parseAmount should convert currency string to BigDecimal`() {
        // Given
        val currencyString = "$10.95"
        
        // When
        val result = CurrencyUtils.parseAmount(currencyString)
        
        // Then
        assertNotNull(result)
        assertEquals(0, BigDecimal("10.95").compareTo(result!!))
    }
    
    @Test
    fun `parseAmount should return null for invalid currency string`() {
        // Given
        val invalidCurrencyString = "invalid"
        
        // When
        val result = CurrencyUtils.parseAmount(invalidCurrencyString)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `isValidCurrencyCode should return true for valid codes`() {
        // Given
        val validCodes = listOf("USD", "EUR", "GBP", "JPY", "CAD", "AUD")
        
        // When/Then
        validCodes.forEach { code ->
            assertTrue("Should be valid: $code", CurrencyUtils.isValidCurrencyCode(code))
        }
    }
    
    @Test
    fun `isValidCurrencyCode should return false for invalid codes`() {
        // Given
        val invalidCodes = listOf("US", "EURO", "GP", "ABC", "123", "")
        
        // When/Then
        invalidCodes.forEach { code ->
            assertFalse("Should be invalid: $code", CurrencyUtils.isValidCurrencyCode(code))
        }
    }
    
    @Test
    fun `getCurrencySymbol should return correct symbol for valid codes`() {
        // Given/When/Then
        assertEquals("$", CurrencyUtils.getCurrencySymbol("USD"))
        assertEquals("£", CurrencyUtils.getCurrencySymbol("GBP", Locale.UK))
        assertEquals("€", CurrencyUtils.getCurrencySymbol("EUR", Locale.GERMANY))
    }
    
    @Test
    fun `getCurrencySymbol should return code itself for invalid codes`() {
        // Given
        val invalidCode = "XYZ"
        
        // When
        val result = CurrencyUtils.getCurrencySymbol(invalidCode)
        
        // Then
        assertEquals(invalidCode, result)
    }
    
    @Test
    fun `conversion should be reversible`() {
        // Given
        val originalCents = 12345
        
        // When
        val dollars = CurrencyUtils.centsToDollars(originalCents)
        val centsAgain = CurrencyUtils.dollarsToCents(dollars)
        
        // Then
        assertEquals(originalCents, centsAgain)
    }
} 