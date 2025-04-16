package com.paytheory.lib.compose.utility

/**
 * Utility functions for bank account field operations
 */
object BankFieldUtils {
    
    /**
     * Formats a bank account number by ensuring it contains only digits
     * 
     * @param accountNumber The raw account number input
     * @return Account number with only digits
     */
    fun formatBankAccountNumber(accountNumber: String): String {
        return accountNumber.filter { it.isDigit() }
    }
    
    /**
     * Validates whether a bank account number appears to be valid
     * 
     * @param accountNumber The account number to validate
     * @return True if the account number appears valid based on length
     */
    fun isValidBankAccountNumber(accountNumber: String): Boolean {
        val digitsOnly = accountNumber.filter { it.isDigit() }
        // US bank account numbers are typically 8-17 digits long
        return digitsOnly.length in 8..17
    }
    
    /**
     * Formats a routing number by ensuring it contains only digits
     * 
     * @param routingNumber The raw routing number input
     * @return Routing number with only digits
     */
    fun formatRoutingNumber(routingNumber: String): String {
        return routingNumber.filter { it.isDigit() }
    }
    
    /**
     * Validates whether a routing number appears to be valid (9 digits, passes checksum)
     * 
     * @param routingNumber The routing number to validate
     * @return True if the routing number appears valid
     */
    fun isValidRoutingNumber(routingNumber: String): Boolean {
        val digitsOnly = routingNumber.filter { it.isDigit() }
        
        // US routing numbers are exactly 9 digits
        if (digitsOnly.length != 9) return false
        
        // Apply ABA routing number checksum algorithm
        val digits = digitsOnly.map { it.toString().toInt() }
        
        // Weights for each position
        val weights = listOf(3, 7, 1, 3, 7, 1, 3, 7, 1)
        
        // Calculate checksum: sum of (digit * weight) must be divisible by 10
        val checksum = digits.zip(weights).sumOf { (digit, weight) -> digit * weight }
        
        return checksum % 10 == 0
    }
} 