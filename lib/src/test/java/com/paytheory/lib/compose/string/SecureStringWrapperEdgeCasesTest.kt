package com.paytheory.lib.compose.string

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test class for edge cases and advanced functionality of SecureStringWrapper.kt
 */
class SecureStringWrapperEdgeCasesTest {

    private lateinit var secureString: SecureString
    private lateinit var secureStringWrapper: SecureStringWrapper

    @Before
    fun setup() {
        secureString = SecureString("initial_string")
        secureStringWrapper = SecureStringWrapper(secureString, TextRange(0))
    }

    @Test
    fun testUpdateValue_emptyString() {
        secureStringWrapper.updateValue("", 0)
        assertEquals("", secureStringWrapper.visibleValue)
        assertEquals("", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(0), secureStringWrapper._selection)
    }

    @Test
    fun testUpdateValue_selectionPastEndOfString() {
        // Test when selection position is beyond string length
        secureStringWrapper.updateValue("short", 10)
        assertEquals("short", secureStringWrapper.visibleValue)
        assertEquals(TextRange(10), secureStringWrapper._selection) 
        // This is expected behavior as TextRange doesn't validate the position
    }
    
    @Test
    fun testUpdateValue_selectionAtStartOfString() {
        // Test selection at position 0 (beginning of string)
        secureStringWrapper.updateValue("test", 0)
        assertEquals("test", secureStringWrapper.visibleValue)
        assertEquals(TextRange(0), secureStringWrapper._selection)
    }
    
    @Test
    fun testSecureStateAccessAfterUpdates() {
        // Test that secureState correctly reflects changes
        secureStringWrapper.updateValue("first", 2)
        var state = secureStringWrapper.secureState
        assertEquals("first", state.text)
        assertEquals(TextRange(2), state.selection)
        
        secureStringWrapper.updateValue("second", 4)
        state = secureStringWrapper.secureState
        assertEquals("second", state.text)
        assertEquals(TextRange(4), state.selection)
    }
    
    @Test
    fun testUpdateValue_withUnicodeChars() {
        // Test updating with emoji and other Unicode characters
        secureStringWrapper.updateValue("hello🌍世界", 7)
        assertEquals("hello🌍世界", secureStringWrapper.visibleValue)
        assertEquals("hello🌍世界", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(7), secureStringWrapper._selection)
    }
    
    @Test
    fun testSecureState_nilSelectionAfterExplicitUpdate() {
        // Start with null selection
        val wrapper = SecureStringWrapper(secureString, null)
        assertNull(wrapper._selection)
        
        // Update, which should set a selection
        wrapper.updateValue("new value", 3)
        assertEquals(TextRange(3), wrapper._selection)
        
        // Check secureState
        val state = wrapper.secureState
        assertEquals("new value", state.text)
        assertEquals(TextRange(3), state.selection)
    }
    
    @Test
    fun testSecureValueChange_doesntAffectOriginal() {
        // Verify that updating the wrapper doesn't affect the original SecureString
        val originalValue = secureString.revealForUi()
        secureStringWrapper.updateValue("new value", 3)
        
        // Original SecureString should be unchanged
        assertEquals(originalValue, secureString.revealForUi())
        
        // Wrapper's SecureString should be updated
        assertEquals("new value", secureStringWrapper.secureValue.revealForUi())
    }
} 