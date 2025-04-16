package com.paytheory.lib.compose.utility

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TransformationUtils
 */
class TransformationUtilsTest {

    // formatCreditCardNumber tests
    
    @Test
    fun `formatCreditCardNumber handles empty input`() {
        assertEquals("", TransformationUtils.formatCreditCardNumber(""))
    }
    
    @Test
    fun `formatCreditCardNumber formats short numbers correctly`() {
        assertEquals("123", TransformationUtils.formatCreditCardNumber("123"))
        assertEquals("1234", TransformationUtils.formatCreditCardNumber("1234"))
        assertEquals("1234 5", TransformationUtils.formatCreditCardNumber("12345"))
    }
    
    @Test
    fun `formatCreditCardNumber formats full card numbers correctly`() {
        assertEquals("1234 5678 9012 3456", TransformationUtils.formatCreditCardNumber("1234567890123456"))
    }
    
    @Test
    fun `formatCreditCardNumber truncates long inputs`() {
        // Should limit to 16 digits (19 chars with spaces)
        assertEquals("1234 5678 9012 3456", TransformationUtils.formatCreditCardNumber("12345678901234567890"))
    }
    
    // CreditCardOffsetMapping tests
    
    @Test
    fun `CreditCardOffsetMapping maps original to transformed indices correctly`() {
        val mapping = TransformationUtils.CreditCardOffsetMapping
        
        // First group (no offset)
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(4, mapping.originalToTransformed(4))
        
        // Second group (offset by 1 for the space)
        assertEquals(6, mapping.originalToTransformed(5))
        assertEquals(9, mapping.originalToTransformed(8))
        
        // Third group (offset by 2 for two spaces)
        assertEquals(11, mapping.originalToTransformed(9))
        assertEquals(14, mapping.originalToTransformed(12))
        
        // Fourth group (offset by 3 for three spaces)
        assertEquals(16, mapping.originalToTransformed(13))
        assertEquals(19, mapping.originalToTransformed(16))
    }
    
    @Test
    fun `CreditCardOffsetMapping maps transformed to original indices correctly`() {
        val mapping = TransformationUtils.CreditCardOffsetMapping
        
        // First group (no offset)
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(4, mapping.transformedToOriginal(4))
        
        // After first space
        assertEquals(4, mapping.transformedToOriginal(5))
        
        // Second group (offset by 1 for the space)
        assertEquals(5, mapping.transformedToOriginal(6))
        assertEquals(8, mapping.transformedToOriginal(9))
        
        // After second space
        assertEquals(8, mapping.transformedToOriginal(10))
        
        // Third group (offset by 2 for two spaces)
        assertEquals(9, mapping.transformedToOriginal(11))
        assertEquals(12, mapping.transformedToOriginal(14))
        
        // After third space
        assertEquals(12, mapping.transformedToOriginal(15))
        
        // Fourth group (offset by 3 for three spaces)
        assertEquals(13, mapping.transformedToOriginal(16))
        assertEquals(16, mapping.transformedToOriginal(19))
    }
    
    // maskText tests
    
    @Test
    fun `maskText replaces all characters with default mask character`() {
        assertEquals("•••", TransformationUtils.maskText("123"))
        assertEquals("••••••", TransformationUtils.maskText("secret"))
        assertEquals("", TransformationUtils.maskText(""))
    }
    
    @Test
    fun `maskText uses custom mask character when provided`() {
        assertEquals("***", TransformationUtils.maskText("123", '*'))
        assertEquals("XXXX", TransformationUtils.maskText("1234", 'X'))
    }
    
    // maskAnnotatedString tests
    
    @Test
    fun `maskAnnotatedString replaces all characters with default mask character`() {
        val original = AnnotatedString("123")
        val masked = TransformationUtils.maskAnnotatedString(original)
        assertEquals("•••", masked.text)
    }
    
    @Test
    fun `maskAnnotatedString preserves length`() {
        val original = AnnotatedString("password")
        val masked = TransformationUtils.maskAnnotatedString(original)
        assertEquals(original.length, masked.length)
    }
    
    @Test
    fun `maskAnnotatedString uses custom mask character when provided`() {
        val original = AnnotatedString("1234")
        val masked = TransformationUtils.maskAnnotatedString(original, '*')
        assertEquals("****", masked.text)
    }
    
    @Test
    fun `maskAnnotatedString handles empty string`() {
        val original = AnnotatedString("")
        val masked = TransformationUtils.maskAnnotatedString(original)
        assertEquals("", masked.text)
    }
} 