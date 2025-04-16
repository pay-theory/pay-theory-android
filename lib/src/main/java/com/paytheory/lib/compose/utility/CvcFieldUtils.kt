package com.paytheory.lib.compose.utility

/**
 * Utility functions for CVC/CVV field operations
 */
object CvcFieldUtils {
    
    /**
     * Formats a CVC code by ensuring it contains only digits
     * 
     * @param cvc The raw CVC input
     * @return CVC with only digits
     */
    fun formatCvc(cvc: String): String {
        return cvc.filter { it.isDigit() }
    }
    
    /**
     * Validates whether a CVC is valid (3-4 digits)
     * 
     * @param cvc The CVC to validate
     * @param cardNetwork Optional card network to validate correct CVC length
     * @return True if the CVC appears valid
     */
    fun isValidCvc(cvc: String, cardNetwork: CardNetwork? = null): Boolean {
        val digitsOnly = cvc.filter { it.isDigit() }
        
        // Empty CVC is not valid
        if (digitsOnly.isEmpty()) return false
        
        // If we know the card network, we can be more specific about length
        return when (cardNetwork) {
            CardNetwork.AMEX -> digitsOnly.length == 4 // Amex uses 4-digit CVC
            CardNetwork.VISA, 
            CardNetwork.MASTERCARD, 
            CardNetwork.DISCOVER -> digitsOnly.length == 3 // Others use 3-digit CVC
            null -> digitsOnly.length in 3..4 // Generic validation if network unknown
        }
    }
    
    /**
     * Gets the expected CVC length for a given card network
     * 
     * @param cardNetwork The card network
     * @return The expected CVC length (3 or 4)
     */
    fun getExpectedCvcLength(cardNetwork: CardNetwork?): Int {
        return if (cardNetwork == CardNetwork.AMEX) 4 else 3
    }
} 