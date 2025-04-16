package com.paytheory.lib.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility class for currency formatting and manipulation.
 *
 * This class provides helper methods for formatting and converting currency amounts
 * between different representations (cents/dollars) and generating formatted currency strings.
 */
object CurrencyUtils {
    
    /**
     * Convert an amount in cents to dollars represented as a BigDecimal.
     *
     * @param cents The amount in cents (integer)
     * @return The amount in dollars as a BigDecimal with 2 decimal places
     */
    fun centsToDollars(cents: Int): BigDecimal {
        return BigDecimal(cents).divide(BigDecimal(100), 2, RoundingMode.HALF_EVEN)
    }
    
    /**
     * Convert an amount in dollars to cents.
     *
     * @param dollars The amount in dollars as a BigDecimal
     * @return The amount in cents as an integer
     */
    fun dollarsToCents(dollars: BigDecimal): Int {
        return dollars.movePointRight(2).setScale(0, RoundingMode.HALF_EVEN).toInt()
    }
    
    /**
     * Format an amount in cents as a currency string using the specified currency code.
     *
     * @param cents The amount in cents (integer)
     * @param currencyCode The ISO 4217 currency code (e.g., "USD")
     * @param locale The locale to use for formatting (default is US)
     * @return A formatted currency string (e.g., "$10.99")
     */
    fun formatCents(cents: Int, currencyCode: String = "USD", locale: Locale = Locale.US): String {
        val dollars = centsToDollars(cents)
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(dollars)
    }
    
    /**
     * Format a BigDecimal amount as a currency string.
     *
     * @param amount The amount as a BigDecimal
     * @param currencyCode The ISO 4217 currency code (e.g., "USD")
     * @param locale The locale to use for formatting (default is US)
     * @return A formatted currency string (e.g., "$10.99")
     */
    fun formatAmount(amount: BigDecimal, currencyCode: String = "USD", locale: Locale = Locale.US): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
    
    /**
     * Parse a currency string into a BigDecimal amount.
     *
     * @param currencyString The currency string to parse (e.g., "$10.99")
     * @param locale The locale to use for parsing (default is US)
     * @return The parsed amount as a BigDecimal, or null if parsing fails
     */
    fun parseAmount(currencyString: String, locale: Locale = Locale.US): BigDecimal? {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(locale)
            BigDecimal(formatter.parse(currencyString)?.toDouble() ?: return null)
                .setScale(2, RoundingMode.HALF_EVEN)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validate if a given string is a valid currency code according to ISO 4217.
     *
     * @param currencyCode The currency code to validate
     * @return True if the currency code is valid, false otherwise
     */
    fun isValidCurrencyCode(currencyCode: String): Boolean {
        return try {
            Currency.getInstance(currencyCode)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    /**
     * Get the currency symbol for a given currency code.
     *
     * @param currencyCode The ISO 4217 currency code (e.g., "USD")
     * @param locale The locale to use (default is US)
     * @return The currency symbol (e.g., "$")
     */
    fun getCurrencySymbol(currencyCode: String, locale: Locale = Locale.US): String {
        return try {
            Currency.getInstance(currencyCode).getSymbol(locale)
        } catch (e: IllegalArgumentException) {
            currencyCode // Return the code itself if invalid
        }
    }
} 