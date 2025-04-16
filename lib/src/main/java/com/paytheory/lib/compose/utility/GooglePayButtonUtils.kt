package com.paytheory.lib.compose.utility

import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType

/**
 * Utility functions for Google Pay Button components
 * 
 * These functions handle the mapping between Pay Theory's internal types
 * and the Google Pay SDK types.
 */
object GooglePayButtonUtils {
    
    /**
     * Maps our internal GooglePayButtonType to the library's ButtonType
     * 
     * @param buttonType Our internal button type enum
     * @return The corresponding Google Pay SDK ButtonType
     */
    fun mapButtonType(buttonType: GooglePayButtonType): ButtonType {
        return when (buttonType) {
            GooglePayButtonType.PAY -> ButtonType.Pay
            GooglePayButtonType.CHECKOUT -> ButtonType.Checkout
            GooglePayButtonType.ORDER -> ButtonType.Order
            GooglePayButtonType.SUBSCRIBE -> ButtonType.Subscribe
            GooglePayButtonType.BOOK -> ButtonType.Book
            GooglePayButtonType.BUY -> ButtonType.Buy
        }
    }

    /**
     * Maps our internal GooglePayButtonColor to the library's ButtonTheme
     * 
     * @param buttonColor Our internal button color enum
     * @return The corresponding Google Pay SDK ButtonTheme
     */
    fun mapButtonTheme(buttonColor: GooglePayButtonColor): ButtonTheme {
        return when (buttonColor) {
            GooglePayButtonColor.BLACK -> ButtonTheme.Dark
            GooglePayButtonColor.WHITE -> ButtonTheme.Light
        }
    }
} 