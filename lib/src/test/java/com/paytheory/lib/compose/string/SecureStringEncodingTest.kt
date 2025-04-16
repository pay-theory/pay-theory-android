package com.paytheory.lib.compose.string

import com.paytheory.lib.compose.string.SecureString.Companion.toSecureBytes
import com.paytheory.lib.compose.string.SecureString.Companion.toSecureString
import org.junit.Assert.*
import org.junit.Test

/**
 * Test class focused on the secure encoding/decoding methods in SecureString.kt
 */
class SecureStringEncodingTest {

    @Test
    fun testToSecureBytes_byteStructure() {
        // Test the byte structure to ensure it follows the expected pattern
        val text = "AB"  // Simple 2-character string
        val bytes = text.toSecureBytes()
        
        // Each character should be encoded as 2 bytes
        assertEquals(4, bytes.size)
        
        // For 'A' (Unicode 65), we should have bytes for 0 and 65
        assertEquals(0, bytes[0].toInt() and 0xFF)
        assertEquals(65, bytes[1].toInt() and 0xFF)
        
        // For 'B' (Unicode 66), we should have bytes for 0 and 66
        assertEquals(0, bytes[2].toInt() and 0xFF)
        assertEquals(66, bytes[3].toInt() and 0xFF)
    }
    
    @Test
    fun testToSecureBytes_unicode() {
        // Test handling of non-ASCII characters
        val text = "Ä"  // German A-umlaut (Unicode 196)
        val bytes = text.toSecureBytes()
        
        assertEquals(2, bytes.size)
        assertEquals(0, bytes[0].toInt() and 0xFF)
        assertEquals(196, bytes[1].toInt() and 0xFF)
    }
    
    @Test
    fun testToSecureBytes_highUnicode() {
        // Test handling of characters outside the BMP
        val text = "🌍"  // Earth globe emoji (Unicode 127757)
        val bytes = text.toSecureBytes()
        
        // Verified the earth emoji is converted to a surrogate pair
        // which will be represented as two chars in Java
        assertEquals(4, bytes.size)  // 2 bytes per char, 2 chars for surrogate pair
        
        // Verify the round-trip conversion works
        val roundTrip = bytes.toSecureString()
        assertEquals(text, roundTrip)
    }
    
    @Test
    fun testToSecureString_roundTrip() {
        // Test complete round-trip conversion
        val original = "Hello World 123 !@#$%^&*()_+{}|:\"<>?~"
        val bytes = original.toSecureBytes()
        val result = bytes.toSecureString()
        
        assertEquals(original, result)
    }
    
    @Test
    fun testToSecureString_unicodeRoundTrip() {
        // Test round-trip with Unicode characters
        val original = "こんにちは世界🌍🌎🌏"
        val bytes = original.toSecureBytes()
        val result = bytes.toSecureString()
        
        assertEquals(original, result)
    }
    
    @Test
    fun testToSecureBytes_oddByteArray() {
        // Create an odd-length byte array (not a valid SecureString encoding)
        val oddBytes = byteArrayOf(0, 65, 0)
        
        // This should not throw an exception, but should handle it gracefully
        val result = oddBytes.toSecureString()
        assertEquals("A", result)  // Should only convert the valid part
    }
    
    @Test
    fun testToSecureBytes_emptyString() {
        val bytes = "".toSecureBytes()
        assertEquals(0, bytes.size)
    }
    
    @Test
    fun testToSecureString_emptyByteArray() {
        val string = ByteArray(0).toSecureString()
        assertEquals("", string)
    }
    
    @Test
    fun testToSecureBytes_longString() {
        // Test with a longer string (1000 chars)
        val text = "A".repeat(1000)
        val bytes = text.toSecureBytes()
        
        assertEquals(2000, bytes.size)
        val result = bytes.toSecureString()
        assertEquals(text, result)
    }
} 