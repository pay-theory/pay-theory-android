package com.paytheory.lib

import org.junit.Test
import org.junit.Assert.*

/**
 * Simple unit test for the PayTheoryConfiguration class
 */
class SimplePayTheoryConfigurationTest {

    @Test
    fun `test configuration with special test API key`() {
        // This test uses the special 'test-paytheory-apikey' value which is specifically
        // handled in the PayTheoryConfiguration class
        val config = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setAmount(10) // Setting a valid amount (minimum required is 10)
            .build()
        
        // The special test key should be accepted without validation errors
        assertEquals("test-paytheory-apikey", config.apiKey)
    }
} 