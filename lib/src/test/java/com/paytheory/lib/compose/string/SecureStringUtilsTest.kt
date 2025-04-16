package com.paytheory.lib.compose.string

import com.paytheory.lib.compose.string.SecureString.Companion.toSecureString
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Test class for utility functions and special cases of secure string handling
 */
class SecureStringUtilsTest {

    @Test
    fun testSecureString_memoryClearing() {
        // Create a secure string
        val originalString = "sensitive_data_123"
        val secureString = SecureString(originalString)
        
        // Verify it contains the data
        assertEquals(originalString, secureString.revealForUi())
        
        // Zero fill it
        secureString.zeroFill()
        
        // Verify data is zeroed out
        val allZeros = ByteArray(originalString.length * 2)
        assertArrayEquals(allZeros, secureString.revealForProcessing())
        
        // Verify UI representation is now zeros
        val zeroChars = allZeros.toSecureString()
        assertEquals(zeroChars, secureString.revealForUi())
    }
    
    @Test
    fun testSecureString_resetValue() {
        // Create a secure string
        val originalString = "sensitive_data_123"
        val secureString = SecureString(originalString)
        
        // Verify it contains the data
        assertEquals(originalString, secureString.revealForUi())
        
        // Reset the value
        val originalModified = secureString._isModified.value
        secureString.resetValue()
        
        // Verify the string is now empty
        assertEquals("", secureString.revealForUi())
        assertArrayEquals(ByteArray(0), secureString.revealForProcessing())
        
        // Verify the modified flag toggled
        assertNotEquals(originalModified, secureString._isModified.value)
    }
    
    @Test
    fun testSecureStringWrapper_stateUpdatesOnChanges() {
        // Create wrapper
        val secureString = SecureString("initial")
        val wrapper = SecureStringWrapper(secureString, null)
        
        // Initial state check
        assertEquals("initial", wrapper.visibleValue)
        assertEquals("initial", wrapper.secureState.text)
        
        // Update and check state propagation
        wrapper.updateValue("updated", 3)
        assertEquals("updated", wrapper.visibleValue)
        assertEquals("updated", wrapper.secureState.text)
    }
    
    @Test
    fun testSecureString_concurrentAccess() {
        // Test concurrent access patterns to secure string
        val secureString = SecureString("concurrent_test")
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        
        try {
            // Create multiple threads that access the secure string
            for (i in 0 until threadCount) {
                executor.submit {
                    try {
                        // Read the value multiple times
                        for (j in 0 until 100) {
                            val value = secureString.revealForUi()
                            assertEquals("concurrent_test", value)
                            
                            val bytes = secureString.revealForProcessing()
                            assertEquals(30, bytes.size) // 15 chars * 2 bytes per char
                        }
                    } finally {
                        latch.countDown()
                    }
                }
            }
            
            // Wait for all threads to complete (with timeout)
            assertTrue(latch.await(5, TimeUnit.SECONDS))
        } finally {
            executor.shutdownNow()
        }
    }
    
    @Test
    fun testSecureString_hashCodeConsistency() {
        // Create two SecureString objects with the same content
        val s1 = SecureString("hashtest")
        val s2 = SecureString("hashtest")
        
        // Their hashCodes should be equal
        assertEquals(s1.hashCode(), s2.hashCode())
        
        // Create a different SecureString
        val s3 = SecureString("different")
        
        // Its hashCode should be different
        assertNotEquals(s1.hashCode(), s3.hashCode())
    }
    
    @Test
    fun testSecureString_equalsConsistency() {
        // Create two SecureString objects with the same content
        val s1 = SecureString("equaltest")
        val s2 = SecureString("equaltest")
        
        // They should have the same visible representation
        assertEquals(s1.revealForUi(), s2.revealForUi())
        
        // They should have the same internal data
        assertTrue(s1.revealForProcessing().contentEquals(s2.revealForProcessing()))
        
        // Create a third equal object for transitivity test
        val s3 = SecureString("equaltest")
        assertTrue(s1.revealForProcessing().contentEquals(s3.revealForProcessing()))
        
        // Create a different object
        val s4 = SecureString("different")
        assertFalse(s1.revealForProcessing().contentEquals(s4.revealForProcessing()))
    }
} 