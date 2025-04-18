package com.paytheory.lib.compose.utility

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PayTheoryButtonUtils
 */
class PayTheoryButtonUtilsTest {

    /**
     * Test that button colors are correctly defined
     */
    @Test
    fun `color constants are correctly defined`() {
        // Verify the primary color is the expected purple
        assertEquals(Color(0xFF9C2AA0), PayTheoryButtonUtils.payTheoryPrimary)
        
        // Verify the secondary color is a darker purple
        assertEquals(Color(0xFF6A1B6D), PayTheoryButtonUtils.payTheorySecondary)
        
        // Verify the disabled color is light gray
        assertEquals(Color(0xFFCCCCCC), PayTheoryButtonUtils.payTheoryDisabled)
        
        // Verify text colors
        assertEquals(Color.White, PayTheoryButtonUtils.lightModeTextColor)
        assertEquals(Color.DarkGray, PayTheoryButtonUtils.darkModeTextColor)
    }
    
    /**
     * Test that hover color is correctly returned
     */
    @Test
    fun `getButtonHoverColor returns correct color`() {
        val hoverColor = PayTheoryButtonUtils.getButtonHoverColor()
        
        // This is a darker shade of purple
        assertEquals(Color(0xFF6A1B6D), hoverColor)
    }
    
    /**
     * Test that high contrast detection works for dark backgrounds
     */
    @Test
    fun `shouldUseHighContrast returns true for dark backgrounds`() {
        // Dark colors should return true for high contrast
        assertTrue(PayTheoryButtonUtils.shouldUseHighContrast(Color.Black))
        assertTrue(PayTheoryButtonUtils.shouldUseHighContrast(Color.DarkGray))
        assertTrue(PayTheoryButtonUtils.shouldUseHighContrast(Color(0xFF123456)))
    }
    
    /**
     * Test that high contrast detection works for light backgrounds
     */
    @Test
    fun `shouldUseHighContrast returns false for light backgrounds`() {
        // Light colors should return false for high contrast
        assertFalse(PayTheoryButtonUtils.shouldUseHighContrast(Color.White))
        assertFalse(PayTheoryButtonUtils.shouldUseHighContrast(Color.LightGray))
        assertFalse(PayTheoryButtonUtils.shouldUseHighContrast(Color(0xFFEEEEEE)))
    }
    
    /**
     * Test high contrast threshold behavior
     */
    @Test
    fun `shouldUseHighContrast handles threshold correctly`() {
        // Create colors just above and below the threshold
        val justAboveThreshold = Color(0xFF808080) // Gray (128, 128, 128)
        val justBelowThreshold = Color(0xFF7F7F7F) // Slightly darker gray
        
        // Just above threshold should be considered light
        assertFalse(PayTheoryButtonUtils.shouldUseHighContrast(justAboveThreshold))
        
        // Just below threshold should be considered dark
        assertTrue(PayTheoryButtonUtils.shouldUseHighContrast(justBelowThreshold))
    }
} 