package com.paytheory.lib.compose.utility

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping

/**
 * Utility functions for text transformations
 */
object TransformationUtils {
    
    /**
     * Formats credit card number text with spaces
     * 
     * @param text The raw credit card number
     * @return The formatted credit card number with spaces
     */
    fun formatCreditCardNumber(text: String): String {
        val trimmed = text.take(16)
        return trimmed.chunked(4).joinToString(" ").trim().take(19)
    }
    
    /**
     * Creates an offset mapping for credit card number formatting
     * 
     * This mapping handles the cursor position for credit card formatting with spaces
     * after every 4 digits
     */
    object CreditCardOffsetMapping : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return when {
                offset <= 4 -> offset
                offset <= 8 -> offset + 1
                offset <= 12 -> offset + 2
                else -> offset + 3
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            return when {
                offset <= 4 -> offset
                offset <= 9 -> offset - 1
                offset <= 14 -> offset - 2
                else -> offset - 3
            }
        }
    }
    
    /**
     * Formats a raw text string for display with a mask (e.g., for passwords)
     * 
     * @param text The original text
     * @param maskChar The character to use for masking
     * @return The masked text
     */
    fun maskText(text: String, maskChar: Char = '•'): String {
        return maskChar.toString().repeat(text.length)
    }
    
    /**
     * Transforms an AnnotatedString to a masked version
     * 
     * @param text The text to mask
     * @param maskChar The character to use for masking
     * @return A new AnnotatedString with the masked text
     */
    fun maskAnnotatedString(text: AnnotatedString, maskChar: Char = '•'): AnnotatedString {
        return AnnotatedString(maskChar.toString().repeat(text.text.length))
    }
} 