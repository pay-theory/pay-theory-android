package com.paytheory.lib.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.googlepay.GooglePayUtil
import org.json.JSONArray

/**
 * A composable Google Pay button that uses Google's official compose-pay-button library.
 *
 * This component wraps Google's official PayButton to maintain API consistency with
 * our other components while leveraging Google's latest branding and Material 3 design.
 *
 * @param onClick Lambda function to be invoked when the button is clicked.
 * @param enabled Boolean indicating whether the button is enabled or disabled.
 * @param modifier Modifier to be applied to the button.
 * @param buttonType The type of Google Pay button to display (PAY, CHECKOUT, etc.)
 * @param buttonColor The color scheme of the button (BLACK or WHITE)
 */
@Composable
fun GooglePayButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    buttonType: GooglePayButtonType = GooglePayButtonType.PAY,
    buttonColor: GooglePayButtonColor = GooglePayButtonColor.BLACK
) {
    // Get the allowedPaymentMethods from GooglePayUtil
    val allowedPaymentMethods = GooglePayUtil.getAllowedPaymentMethodsJson().toString()
    
    PayButton(
        onClick = onClick,
        allowedPaymentMethods = allowedPaymentMethods,
        modifier = modifier,
        type = mapButtonType(buttonType),
        radius = 4.dp,  // Match corner radius to our design system
        enabled = enabled,
        theme = mapButtonTheme(buttonColor)
    )
}

/**
 * Maps our internal GooglePayButtonType to the library's ButtonType
 */
private fun mapButtonType(buttonType: GooglePayButtonType): ButtonType {
    return when (buttonType) {
        GooglePayButtonType.PAY -> ButtonType.PAY
        GooglePayButtonType.CHECKOUT -> ButtonType.PAY_CHECKOUT
        GooglePayButtonType.ORDER -> ButtonType.PAY_ORDER
        GooglePayButtonType.SUBSCRIBE -> ButtonType.PAY_SUBSCRIBE
        GooglePayButtonType.BOOK -> ButtonType.PAY_BOOK
        GooglePayButtonType.BUY -> ButtonType.PAY_BUY
    }
}

/**
 * Maps our internal GooglePayButtonColor to the library's ButtonTheme
 */
private fun mapButtonTheme(buttonColor: GooglePayButtonColor): ButtonTheme {
    return when (buttonColor) {
        GooglePayButtonColor.BLACK -> ButtonTheme.DARK
        GooglePayButtonColor.WHITE -> ButtonTheme.LIGHT
    }
} 