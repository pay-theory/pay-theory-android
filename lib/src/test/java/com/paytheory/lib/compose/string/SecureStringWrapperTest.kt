package com.paytheory.lib.compose.string

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the SecureStringWrapper class
 * 
 * The SecureStringWrapper is a key component in handling secure input for payment fields.
 * It wraps a SecureString while providing UI-friendly functionality like cursor position.
 */
class SecureStringWrapperTest {

    @Test
    fun testSecureStringWrapperCreation() {
        // Create a secure string
        val secureString = SecureString("4111111111111111")
        
        // Create a wrapper with a selection position
        val selectionPos = TextRange(4)
        val wrapper = SecureStringWrapper(secureString, selectionPos)
        
        // Test initial state
        assertEquals("4111111111111111", wrapper.visibleValue)
        assertEquals(secureString, wrapper.secureValue)
        assertEquals(selectionPos, wrapper._selection)
    }
    
    @Test
    fun testUpdateValue() {
        // Create a secure string and wrapper
        val secureString = SecureString("4111111111111111")
        val wrapper = SecureStringWrapper(secureString, TextRange(0))
        
        // Update the value with a new credit card number and cursor position
        val newValue = "4242424242424242"
        val newCursorPos = 8
        wrapper.updateValue(newValue, newCursorPos)
        
        // Verify the update was successful
        assertEquals(newValue, wrapper.visibleValue)
        assertEquals(newValue, wrapper.secureValue.revealForUi())
        assertEquals(TextRange(newCursorPos), wrapper._selection)
    }
    
    @Test
    fun testSecureStateWithSelection() {
        // Create a secure string
        val secureString = SecureString("4111111111111111")
        
        // Create a wrapper with a specific selection range
        val selectionRange = TextRange(4, 8) // Selects "1111" in the middle
        val wrapper = SecureStringWrapper(secureString, selectionRange)
        
        // Get the TextFieldValue representation
        val textFieldValue = wrapper.secureState
        
        // Verify the text and selection
        assertEquals("4111111111111111", textFieldValue.text)
        assertEquals(selectionRange, textFieldValue.selection)
    }
    
    @Test
    fun testSecureStateWithoutSelection() {
        // Create a secure string
        val secureString = SecureString("4111111111111111")
        
        // Create a wrapper without selection
        val wrapper = SecureStringWrapper(secureString, null)
        
        // Get the TextFieldValue representation
        val textFieldValue = wrapper.secureState
        
        // Verify the text and that selection is at default position
        assertEquals("4111111111111111", textFieldValue.text)
        assertEquals(TextRange(0, 0), textFieldValue.selection) // Default selection
    }
    
    @Test
    fun testMemoryProtection() {
        // Create a wrapper with sensitive data
        val sensitiveData = "4111111111111111" 
        val secureString = SecureString(sensitiveData)
        val wrapper = SecureStringWrapper(secureString, TextRange(4))
        
        // Verify initial state
        assertEquals(sensitiveData, wrapper.visibleValue)
        
        // Update the value to something else
        wrapper.updateValue("", 0)
        
        // Zero fill the original secure string
        secureString.zeroFill()
        
        // Verify the original secure string is zeroed out
        val clearedData = secureString.revealForProcessing()
        assertTrue(clearedData.all { it == 0.toByte() })
    }
    
    @Test
    fun testCursorPositionMaintenance() {
        // Create a wrapper with a cursor position
        val secureString = SecureString("4111")
        val initialCursorPos = TextRange(4) // Cursor at the end
        val wrapper = SecureStringWrapper(secureString, initialCursorPos)
        
        // Update with a longer value, placing cursor in the middle
        wrapper.updateValue("41112222", 4)
        
        // Verify cursor position is maintained
        assertEquals(TextRange(4), wrapper._selection)
        
        // Update again with another value and cursor position
        wrapper.updateValue("411122223333", 8)
        
        // Verify the new cursor position
        assertEquals(TextRange(8), wrapper._selection)
    }
}