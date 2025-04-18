package com.paytheory.lib.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.google.pay.button.PayButton
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.compose.utility.GooglePayButtonUtils
import com.paytheory.lib.googlepay.GooglePayFactory

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
    // Get the allowedPaymentMethods from GooglePayUtil via the Factory
    val googlePayUtil = GooglePayFactory.getGooglePayUtil()
    val allowedPaymentMethods = googlePayUtil.getAllowedPaymentMethodsJson().toString()
    
    PayButton(
        onClick = onClick,
        allowedPaymentMethods = allowedPaymentMethods,
        modifier = modifier.testTag("google_pay_button"),
        type = GooglePayButtonUtils.mapButtonType(buttonType),
        radius = 4.dp,  // Match corner radius to our design system
        enabled = enabled,
        theme = GooglePayButtonUtils.mapButtonTheme(buttonColor)
    )
} 