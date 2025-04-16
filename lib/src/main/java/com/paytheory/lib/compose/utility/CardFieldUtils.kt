package com.paytheory.lib.compose.utility

/**
 * Utility functions for credit card field operations
 */
object CardFieldUtils {
    
    /**
     * Formats a credit card number with spaces after every 4 digits
     * 
     * @param cardNumber The raw card number input
     * @return Formatted card number with spaces
     */
    fun formatCardNumber(cardNumber: String): String {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        return digitsOnly.chunked(4).joinToString(" ")
    }
    
    /**
     * Validates whether a card number appears to be valid
     * 
     * @param cardNumber The card number to validate
     * @return True if the card number appears valid based on length
     */
    fun isValidCardNumber(cardNumber: String): Boolean {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        // Most card networks use 13-19 digits
        return digitsOnly.length in 13..19
    }
    
    /**
     * Applies the Luhn algorithm to check if a card number is potentially valid
     * 
     * @param cardNumber The card number to validate
     * @return True if the card number passes the Luhn check
     */
    fun passesLuhnCheck(cardNumber: String): Boolean {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return false
        
        val digits = digitsOnly.map { it.toString().toInt() }
        val checksum = digits.reversed()
            .mapIndexed { index, digit -> 
                if (index % 2 == 1) { // Odd indices (second, fourth, etc. from right)
                    val doubled = digit * 2
                    if (doubled > 9) doubled - 9 else doubled
                } else {
                    digit
                }
            }.sum()
        
        return checksum % 10 == 0
    }
    
    /**
     * Detects the card network (brand) based on the card number prefix
     * 
     * @param cardNumber The card number to check
     * @return The detected card network or null if unknown
     */
    fun detectCardNetwork(cardNumber: String): CardNetwork? {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return null
        
        return when {
            digitsOnly.startsWith("4") -> CardNetwork.VISA
            digitsOnly.startsWith("5") && digitsOnly[1].toString().toInt() in 1..5 -> CardNetwork.MASTERCARD
            digitsOnly.startsWith("34") || digitsOnly.startsWith("37") -> CardNetwork.AMEX
            digitsOnly.startsWith("6011") || 
                digitsOnly.startsWith("644") || 
                digitsOnly.startsWith("65") -> CardNetwork.DISCOVER
            else -> null
        }
    }
}

/**
 * Enum representing major credit card networks
 */
enum class CardNetwork {
    VISA,
    MASTERCARD,
    AMEX,
    DISCOVER
} 