package com.paytheory.lib

import org.junit.Test
import org.junit.Assert.*

/**
 * Test for string validation
 */
class StringValidationTest {

    @Test
    fun `strings should be equal`() {
        val expected = "test-string"
        val actual = "test-string"
        
        assertEquals(expected, actual)
    }
    
    @Test
    fun `strings should be different`() {
        val string1 = "test-string-1"
        val string2 = "test-string-2"
        
        assertNotEquals(string1, string2)
    }
} 