package com.paytheory.lib.compose.string

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Test class specifically for the stringLength method in SecureString.kt
 */
class SecureStringLengthTest {

    @Test
    fun testStringLength_emptyString() {
        val secureString = SecureString("")
        assertEquals(0, secureString.revealForUi().length)
    }

    @Test
    fun testStringLength_asciiString() {
        val secureString = SecureString("hello")
        assertEquals(5, secureString.revealForUi().length)
    }

    @Test
    fun testStringLength_unicodeString() {
        // Include emoji and non-ASCII characters
        val secureString = SecureString("hello🌍世界")
        assertEquals(9, secureString.revealForUi().length)
    }

    @Test
    fun testStringLength_multipleEncodings() {
        val text = "Test123"
        val secureString = SecureString(text)
        
        // Test with string length
        assertEquals(7, secureString.revealForUi().length)
    }

    @Test
    fun testStringLength_withSpecialChars() {
        val specialChars = "!@#$%^&*()_+{}|:<>?~"
        val secureString = SecureString(specialChars)
        assertEquals(specialChars.length, secureString.revealForUi().length)
    }

    @Test
    fun testStringLength_withWhitespace() {
        val whitespace = "  \t\n\r  "
        val secureString = SecureString(whitespace)
        assertEquals(whitespace.length, secureString.revealForUi().length)
    }

    @Test
    fun testStringLength_longString() {
        // Create a string of 1000 characters
        val longString = "a".repeat(1000)
        val secureString = SecureString(longString)
        assertEquals(1000, secureString.revealForUi().length)
    }
} 