package com.paytheory.lib.compose.utility

import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType

/**
 * Utility class for mapping our Google Pay button enums to Google's official
 * compose-pay-button library button types and themes.
 */
object GooglePayButtonUtils {
    /**
     * Maps our GooglePayButtonType to Google's ButtonType enum.
     *
     * @param type The button type from our configuration
     * @return The corresponding ButtonType from Google's library
     */
    fun mapButtonType(type: GooglePayButtonType): ButtonType {
        return when (type) {
            GooglePayButtonType.BUY -> ButtonType.Buy
            GooglePayButtonType.CHECKOUT -> ButtonType.Checkout
            GooglePayButtonType.ORDER -> ButtonType.Order
            GooglePayButtonType.BOOK -> ButtonType.Book
            GooglePayButtonType.SUBSCRIBE -> ButtonType.Subscribe
            GooglePayButtonType.PAY -> ButtonType.Pay
        }
    }

    /**
     * Maps our GooglePayButtonColor to Google's ButtonTheme enum.
     *
     * @param color The button color from our configuration
     * @return The corresponding ButtonTheme from Google's library
     */
    fun mapButtonTheme(color: GooglePayButtonColor): ButtonTheme {
        return when (color) {
            GooglePayButtonColor.BLACK -> ButtonTheme.Dark
            GooglePayButtonColor.WHITE -> ButtonTheme.Light
        }
    }
}