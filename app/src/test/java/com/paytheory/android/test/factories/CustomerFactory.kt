package com.paytheory.android.test.factories

import java.util.UUID
import kotlin.random.Random

/**
 * Data class representing a customer for testing purposes
 */
data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: Address? = null
)

/**
 * Data class representing a customer address
 */
data class Address(
    val line1: String,
    val line2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String = "US"
)

/**
 * Factory for creating Customer instances for testing
 */
class CustomerFactory : BaseFactory<Customer> {
    
    override fun create(): Customer {
        return Customer(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "5551234567",
            address = Address(
                line1 = "123 Main St",
                line2 = null,
                city = "Columbus",
                state = "OH",
                postalCode = "43215"
            )
        )
    }
    
    override fun createRandom(): Customer {
        val firstNames = listOf("John", "Jane", "Mike", "Sarah", "David", "Emily", "Robert", "Lisa", "Michael", "Emma")
        val lastNames = listOf("Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor")
        
        val firstName = firstNames.random()
        val lastName = lastNames.random()
        val email = "${firstName.lowercase()}.${lastName.lowercase()}@${generateRandomDomain()}"
        
        return Customer(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = generateRandomPhone(),
            address = if (Random.nextBoolean()) generateRandomAddress() else null
        )
    }
    
    /**
     * Creates a Customer with a specific name
     *
     * @param firstName The customer's first name
     * @param lastName The customer's last name
     * @return A Customer with the specified name
     */
    fun createWithName(firstName: String, lastName: String): Customer {
        return create().copy(
            firstName = firstName,
            lastName = lastName,
            email = "${firstName.lowercase()}.${lastName.lowercase()}@example.com"
        )
    }
    
    /**
     * Creates a Customer with a specific address
     *
     * @param address The customer's address
     * @return A Customer with the specified address
     */
    fun createWithAddress(address: Address): Customer {
        return create().copy(address = address)
    }
    
    private fun generateRandomDomain(): String {
        val domains = listOf("example.com", "test.com", "mail.com", "emailprovider.net", "testmail.org")
        return domains.random()
    }
    
    private fun generateRandomPhone(): String {
        val areaCode = (100..999).random()
        val prefix = (100..999).random()
        val lineNumber = (1000..9999).random()
        
        return "$areaCode$prefix$lineNumber"
    }
    
    private fun generateRandomAddress(): Address {
        val streets = listOf("Main", "Oak", "Pine", "Maple", "Washington", "Lincoln", "Park", "Lake", "Hill")
        val streetTypes = listOf("St", "Ave", "Blvd", "Dr", "Ln", "Rd", "Way", "Circle", "Court")
        val cities = listOf("Columbus", "Cleveland", "Cincinnati", "Toledo", "Akron", "Dayton", "Canton", "Youngstown")
        val states = mapOf(
            "OH" to "Ohio",
            "PA" to "Pennsylvania", 
            "MI" to "Michigan",
            "IN" to "Indiana",
            "KY" to "Kentucky",
            "WV" to "West Virginia",
            "NY" to "New York",
            "IL" to "Illinois"
        )
        
        val streetNumber = (100..9999).random()
        val street = streets.random()
        val streetType = streetTypes.random()
        val city = cities.random()
        val stateEntry = states.entries.random()
        val zipCode = (10000..99999).random().toString()
        
        return Address(
            line1 = "$streetNumber $street $streetType",
            line2 = if (Random.nextBoolean()) "Apt ${(1..999).random()}" else null,
            city = city,
            state = stateEntry.key,
            postalCode = zipCode
        )
    }
} 