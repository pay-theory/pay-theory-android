package com.paytheory.lib.compose.inputs

import androidx.compose.ui.text.TextRange
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import org.junit.Assert.*
import org.junit.Test

class SaversTest {

    @Test
    fun `SecureStringWrapper stores and retrieves text correctly`() {
        // Create test data
        val originalText = "test123"
        val secureString = SecureString(originalText)
        val selection = TextRange(2, 5)
        val wrapper = SecureStringWrapper(initialValue = secureString, selection = selection)
        
        // Verify the wrapper correctly stores the data
        assertEquals(originalText, wrapper.secureValue.revealForUi())
    }
    
    @Test
    fun `SecureString stores and reveals text correctly`() {
        // Create test data
        val originalText = "sensitive1234"
        val secureString = SecureString(originalText)
        
        // Verify the SecureString correctly stores and reveals the text
        assertEquals(originalText, secureString.revealForUi())
    }
    
    @Test
    fun `SecureString handles special characters`() {
        // Create test data with special characters
        val originalText = "!@#$%^&*()_+{}[]|\"<>,.?/~`"
        val secureString = SecureString(originalText)
        
        // Verify special characters are preserved
        assertEquals(originalText, secureString.revealForUi())
    }
    
    @Test
    fun `SecureString handles Unicode characters`() {
        // Create test data with Unicode characters
        val originalText = "こんにちは世界😊🌍"
        val secureString = SecureString(originalText)
        
        // Verify Unicode characters are preserved
        assertEquals(originalText, secureString.revealForUi())
    }
    
    @Test
    fun `SecureStringWrapper maintains secure behavior`() {
        // Create test data
        val originalText = "password123"
        val secureString = SecureString(originalText)
        val wrapper = SecureStringWrapper(initialValue = secureString, selection = null)
        
        // Verify the toString() doesn't reveal the secure content
        val toStringResult = wrapper.toString()
        assertFalse(toStringResult.contains(originalText))
        
        // But the reveal method does expose it when needed
        assertEquals(originalText, wrapper.secureValue.revealForUi())
    }
} 