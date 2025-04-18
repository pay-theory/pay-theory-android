package com.paytheory.lib.compose.utility

import androidx.compose.ui.graphics.Color

/**
 * Utility class for PayTheory button styling and behavior logic
 */
object PayTheoryButtonUtils {
    
    // PayTheory primary brand color
    val payTheoryPrimary = Color(0xFF9C2AA0)
    val payTheorySecondary = Color(0xFF6A1B6D)
    val payTheoryDisabled = Color(0xFFCCCCCC)
    
    // Standard text colors
    val lightModeTextColor = Color.White
    val darkModeTextColor = Color.DarkGray
    
    /**
     * Get the appropriate hover/pressed color for PayTheory buttons
     * 
     * @return Color to use for hover/pressed states
     */
    fun getButtonHoverColor(): Color {
        return payTheorySecondary
    }
    
    /**
     * Determine if the button should use high contrast mode based on the background
     * 
     * @param backgroundColor The background color to contrast against
     * @return Whether to use high contrast mode
     */
    fun shouldUseHighContrast(backgroundColor: Color): Boolean {
        // Simple luminance calculation
        val luminance = (0.299 * backgroundColor.red + 
                        0.587 * backgroundColor.green + 
                        0.114 * backgroundColor.blue)
        
        // Use high contrast when the background is dark
        return luminance < 0.5
    }
} 