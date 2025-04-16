package com.paytheory.lib.compose.utility

/**
 * Utility functions for expiration date field operations
 */
object ExpirationFieldUtils {
    
    /**
     * Formats a card expiry date string from a raw input string.
     *
     * This function takes a string as input and attempts to extract and format a card expiry date
     * in the MM/YY format. It handles various input scenarios, including:
     *   - Empty input strings.
     *   - Input strings containing non-digit characters.
     *   - Input strings with less than 4 digits.
     *   - Month values exceeding 12.
     *   - Month values less than 1.
     *
     * @param input The raw input string, potentially containing digits and non-digit characters.
     * @return A formatted expiry date string in the MM/YY format, or an empty string if no digits are found in the input.
     */
    fun formatCardExpiryDate(input: String): String {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return ""

        val monthLength = if (digits.length < 2) 1 else 2
        val formattedMonth = if (digits.startsWith("0") || digits.startsWith("01")) digits.substring(0,monthLength) else {
            val month = when {
                digits.length >= 2 -> digits.substring(0, 2).toInt().coerceAtMost(12)
                else -> digits.toInt().coerceAtMost(9)
            }

            if (month > 1 && month<= 9) month.toString().padStart(2, '0') else month.toString()
        }
        val year = digits.drop(2).take(2)

        return if (year.isNotEmpty()) "$formattedMonth/$year" else formattedMonth
    }
    
    /**
     * Validates an expiration date string
     * 
     * @param expirationDate The expiration date to validate
     * @return True if the expiration date is valid
     */
    fun isValidExpirationDate(expirationDate: String): Boolean {
        // Empty strings aren't valid expiration dates
        if (expirationDate.isEmpty()) return false
        
        // Must have the MM/YY format
        val parts = expirationDate.split("/")
        if (parts.size != 2) return false
        
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false
        
        // Valid month range is 1-12
        if (month !in 1..12) return false
        
        // Basic validation that year is a 2-digit number
        if (year !in 0..99) return false
        
        // Get current date for expiration validation
        val currentYear = java.time.Year.now().value % 100 // Get last 2 digits
        val currentMonth = java.time.MonthDay.now().monthValue
        
        // Expiration date cannot be in the past
        return when {
            year > currentYear -> true // Future year is valid
            year == currentYear -> month >= currentMonth // Current year but future/current month
            else -> false // Past year is invalid
        }
    }
} 