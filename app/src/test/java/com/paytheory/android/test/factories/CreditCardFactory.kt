package com.paytheory.android.test.factories

import java.util.UUID
import kotlin.random.Random

/**
 * Data class representing a credit card for testing purposes
 */
data class CreditCard(
    val number: String,
    val expirationMonth: String,
    val expirationYear: String,
    val cvv: String,
    val cardholderName: String,
    val brand: String,
    val isValid: Boolean
)

/**
 * Factory for creating CreditCard instances for testing
 */
class CreditCardFactory : BaseFactory<CreditCard> {
    private val validBrands = listOf("visa", "mastercard", "amex", "discover")
    
    override fun create(): CreditCard {
        return CreditCard(
            number = "4111111111111111",
            expirationMonth = "12",
            expirationYear = (java.time.Year.now().value + 2).toString().substring(2),
            cvv = "123",
            cardholderName = "Test User",
            brand = "visa",
            isValid = true
        )
    }
    
    override fun createRandom(): CreditCard {
        val brand = validBrands.random()
        val number = when(brand) {
            "visa" -> "4" + (0..14).map { Random.nextInt(0, 10) }.joinToString("")
            "mastercard" -> "5" + (0..14).map { Random.nextInt(0, 10) }.joinToString("")
            "amex" -> "34" + (0..13).map { Random.nextInt(0, 10) }.joinToString("")
            "discover" -> "6" + (0..15).map { Random.nextInt(0, 10) }.joinToString("")
            else -> (0..15).map { Random.nextInt(0, 10) }.joinToString("")
        }
        
        val currentYear = java.time.Year.now().value
        val expirationYear = (currentYear + Random.nextInt(1, 5)).toString().substring(2)
        val expirationMonth = String.format("%02d", Random.nextInt(1, 13)) 
        
        return CreditCard(
            number = number,
            expirationMonth = expirationMonth,
            expirationYear = expirationYear,
            cvv = (100..999).random().toString(),
            cardholderName = generateRandomName(),
            brand = brand,
            isValid = Random.nextBoolean()
        )
    }
    
    /**
     * Creates a CreditCard with a specific brand
     *
     * @param brand The card brand (visa, mastercard, amex, discover)
     * @return A CreditCard with the specified brand
     */
    fun createWithBrand(brand: String): CreditCard {
        require(validBrands.contains(brand.lowercase())) {
            "Brand must be one of: ${validBrands.joinToString(", ")}"
        }
        
        return create().copy(brand = brand)
    }
    
    /**
     * Creates an invalid CreditCard
     * 
     * @return An invalid CreditCard
     */
    fun createInvalid(): CreditCard {
        return create().copy(
            expirationYear = (java.time.Year.now().value - 1).toString().substring(2),
            isValid = false
        )
    }
    
    private fun generateRandomName(): String {
        val firstNames = listOf("John", "Jane", "Mike", "Sarah", "David", "Emily", "Robert", "Lisa")
        val lastNames = listOf("Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson")
        
        return "${firstNames.random()} ${lastNames.random()}"
    }
} 