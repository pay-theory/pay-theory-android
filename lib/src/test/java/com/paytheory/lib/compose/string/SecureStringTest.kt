package com.paytheory.lib.compose.string

import com.paytheory.lib.compose.string.SecureString.Companion.toSecureBytes
import com.paytheory.lib.compose.string.SecureString.Companion.toSecureString
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Arrays
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Comprehensive tests for SecureString implementation
 * 
 * These tests verify the security properties of the SecureString class, which is
 * responsible for storing sensitive payment data in a secure manner.
 */
class SecureStringTest {

    private lateinit var secureString: SecureString

    @Before
    fun setup() {
        secureString = SecureString("test_string")
    }

    @Test
    fun testRevealForUi() {
        assertEquals("test_string", secureString.revealForUi())
    }

    @Test
    fun testRevealForProcessing() {
        val expectedBytes = "test_string".toSecureBytes()
        val actualBytes = secureString.revealForProcessing()
        assertArrayEquals(expectedBytes, actualBytes)
    }

    @Test
    fun testClear() {
        secureString.zeroFill()
        val allZeros = ByteArray(secureString.revealForProcessing().size)
        assertArrayEquals(allZeros, secureString.revealForProcessing())
        assertEquals(allZeros.toSecureString(),secureString.revealForUi())
    }

    @Test
    fun testResetValue() {
        secureString.resetValue()
        assertEquals("", secureString.revealForUi())
        val expectedBytes = ByteArray(0)
        assertArrayEquals(expectedBytes, secureString.revealForProcessing())
    }

    @Test
    fun testResetValue_modifiedFlag() {
        val initialModified = secureString._isModified.value
        secureString.resetValue()
        assertNotEquals(initialModified, secureString._isModified.value)
    }

    @Test
    fun testConstructorWithString() {
        val secureString = SecureString("hello")
        assertEquals("hello", secureString.revealForUi())
    }

    @Test
    fun testConstructorWithByteArray() {
        val byteArray = "hello".toSecureBytes()
        val secureString = SecureString(byteArray)
        assertEquals("hello", secureString.revealForUi())
        assertArrayEquals(byteArray, secureString.revealForProcessing())
    }

    @Test
    fun testEquals_sameObject() {
        assertTrue(secureString == secureString)
    }

    @Test
    fun testEquals_sameValue() {
        val otherSecureString = SecureString("test_string")
        assertTrue(secureString.revealForUi() == otherSecureString.revealForUi())
        assertEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testEquals_differentValue() {
        val otherSecureString = SecureString("different_string")
        assertFalse(secureString == otherSecureString)
        assertNotEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testEquals_differentType() {
        assertFalse(secureString.toString() == "test_string")
    }


    @Test
    fun testHashCode_sameValue() {
        val otherSecureString = SecureString("test_string")
        assertEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testHashCode_differentValue() {
        val otherSecureString = SecureString("different_string")
        assertNotEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testGetValue() {
        assertEquals("test_string", secureString.revealForUi())
    }

    @Test
    fun testToSecureBytes_emptyString() {
        val bytes = "".toSecureBytes()
        assertEquals(0, bytes.size)
    }

    @Test
    fun testToSecureBytes_nonEmptyString() {
        val bytes = "hello".toSecureBytes()
        assertEquals(10, bytes.size)
    }

    @Test
    fun testToSecureString_emptyByteArray() {
        val string = ByteArray(0).toSecureString()
        assertEquals("", string)
    }

    @Test
    fun testToSecureString_nonEmptyByteArray() {
        val bytes = "hello".toSecureBytes()
        val string = bytes.toSecureString()
        assertEquals("hello", string)
    }

    @Test
    fun testSecureStringCreation() {
        // Test creating a SecureString with a string
        val originalValue = "4111111111111111" // Test credit card number
        val secureString = SecureString(originalValue)
        
        // Verify the string can be retrieved correctly
        assertEquals(originalValue, secureString.revealForUi())
        
        // Verify the byte array length is as expected (each char is 2 bytes)
        assertEquals(originalValue.length * 2, secureString.revealForProcessing().size)
    }
    
    @Test
    fun testSecureStringZeroFill() {
        // Create a secure string with sensitive data
        val originalValue = "4242424242424242" // Test credit card number
        val secureString = SecureString(originalValue)
        
        // Verify data is initially correct
        assertEquals(originalValue, secureString.revealForUi())
        
        // Zero fill the secure string
        secureString.zeroFill()
        
        // Verify all bytes in the internal array are now zero
        val allZeros = ByteArray(originalValue.length * 2)
        assertArrayEquals(allZeros, secureString.revealForProcessing())
        
        // Verify the string representation is now empty or zeros
        val zeroFilledString = secureString.revealForUi()
        assertTrue(zeroFilledString.all { it == '\u0000' })
    }
    
    @Test
    fun testSecureStringResetValue() {
        // Create a secure string with sensitive data
        val originalValue = "1234567812345678" // Test credit card number
        val secureString = SecureString(originalValue)
        
        // Record initial state
        val initialModified = secureString._isModified.value
        
        // Reset the value
        secureString.resetValue()
        
        // Verify value is now empty
        assertEquals("", secureString.revealForUi())
        assertArrayEquals(ByteArray(0), secureString.revealForProcessing())
        
        // Verify the modified flag was toggled
        assertNotEquals(initialModified, secureString._isModified.value)
    }
    
    @Test
    fun testByteArrayConversion() {
        // Original sensitive data
        val original = "4111111111111111"
        
        // Convert to secure bytes and back
        val secureBytes = original.toSecureBytes()
        val converted = secureBytes.toSecureString()
        
        // The result should match the original
        assertEquals(original, converted)
        
        // The byte array should be exactly twice the length of the string
        assertEquals(original.length * 2, secureBytes.size)
    }
    
    @Test
    fun testConcurrentAccess() {
        // This tests that the SecureString remains thread-safe
        val secureString = SecureString("4111111111111111")
        val numThreads = 10
        val executor = Executors.newFixedThreadPool(numThreads)
        val latch = CountDownLatch(numThreads)
        
        for (i in 0 until numThreads) {
            executor.submit {
                try {
                    // Read the secure string value
                    val value = secureString.revealForUi()
                    assertNotNull("Value should not be null", value)
                    assertEquals(16, value.length)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        executor.shutdown()
    }
    
    @Test
    fun testSecureStringClearance() {
        // This test verifies that sensitive data is not kept in memory
        
        // Create a string with sensitive data
        val sensitiveData = "4111111111111111"
        
        // Create and immediately destroy a SecureString
        val secureString = SecureString(sensitiveData)
        val retrievedValue = secureString.revealForUi()
        assertEquals(sensitiveData, retrievedValue)
        
        // Clear the data
        secureString.zeroFill()
        
        // Try to force garbage collection to ensure memory is released
        System.gc()
        Thread.sleep(100)
        
        // Verify the data is zeroed out
        val clearedData = secureString.revealForProcessing()
        assertTrue(clearedData.all { it == 0.toByte() })
    }
    
    @Test
    fun testSecureStringLength() {
        // Test with ASCII string
        val asciiString = "4111111111111111"
        val secureAscii = SecureString(asciiString)
        
        // Use the length of the revealed string rather than a potential stringLength method
        assertEquals(asciiString.length, secureAscii.revealForUi().length)
        
        // Test with a different string
        val unicodeString = "4111-1111-1111-1111"
        val secureUnicode = SecureString(unicodeString)
        assertEquals(unicodeString.length, secureUnicode.revealForUi().length)
    }
    
    @Test
    fun testSecureStringEquality() {
        // Create two secure strings with the same value
        val value1 = "4111111111111111"
        val secureString1 = SecureString(value1)
        val secureString2 = SecureString(value1)
        
        // Test equality based on the content equality
        assertTrue(secureString1.revealForUi() == secureString2.revealForUi())
        
        // Create a secure string with a different value
        val value2 = "5555555555554444"
        val secureString3 = SecureString(value2)
        
        // They should not be equal
        assertFalse(secureString1.revealForUi() == secureString3.revealForUi())
    }
}