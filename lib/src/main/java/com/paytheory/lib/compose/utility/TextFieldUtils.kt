package com.paytheory.lib.compose.utility

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper

/**
 * Utility functions for text field operations and validations
 */
object TextFieldUtils {
    
    /**
     * Determines if a text field is displaying an error state
     * 
     * @param text The current text value
     * @param isRequired Whether the field is required
     * @param hasBeenFocused Whether the field has been focused by the user
     * @param customValidation Optional custom validation function
     * @return True if the field should display an error
     */
    fun isFieldError(
        text: String,
        isRequired: Boolean,
        hasBeenFocused: Boolean,
        customValidation: ((String) -> Boolean)? = null
    ): Boolean {
        // Only show errors after user interaction
        if (!hasBeenFocused) return false
        
        // Check if empty when required
        if (isRequired && text.isEmpty()) return true
        
        // Run custom validation if provided and there's text to validate
        return text.isNotEmpty() && customValidation?.invoke(text) == false
    }
    
    /**
     * Converts a SecureStringWrapper to plain text for display
     * 
     * @param wrapper The SecureStringWrapper to extract text from
     * @return The plain text representation for UI display
     */
    fun secureWrapperToText(wrapper: SecureStringWrapper): String {
        return wrapper.secureValue.revealForUi()
    }
    
    /**
     * Creates a TextFieldValue from a SecureStringWrapper
     * 
     * @param wrapper The SecureStringWrapper to convert
     * @return A TextFieldValue with the same text and selection
     */
    fun secureWrapperToTextFieldValue(wrapper: SecureStringWrapper): TextFieldValue {
        val text = wrapper.secureValue.revealForUi()
        return wrapper.secureState
    }
    
    /**
     * Creates a SecureStringWrapper from a TextFieldValue
     * 
     * @param value The TextFieldValue to convert
     * @return A SecureStringWrapper with the same text and selection
     */
    fun textFieldValueToSecureWrapper(value: TextFieldValue): SecureStringWrapper {
        return SecureStringWrapper(
            initialValue = SecureString(value.text),
            selection = value.selection
        )
    }
    
    /**
     * Updates a SecureStringWrapper with new text while maintaining cursor position
     * 
     * @param text The new text content
     * @param cursorPosition The new cursor position (or null to place at end)
     * @return An updated SecureStringWrapper with the new text and cursor position
     */
    fun createSecureWrapper(
        text: String,
        cursorPosition: Int? = null
    ): SecureStringWrapper {
        val position = cursorPosition ?: text.length
        
        return SecureStringWrapper(
            initialValue = SecureString(text),
            selection = TextRange(position, position)
        )
    }
} 