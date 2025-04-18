package com.paytheory.lib.compose

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.paytheory.lib.compose.utility.PayTheoryButtonUtils

/**
 * A composable button designed with PayTheory's branding.
 *
 * This button component is styled with PayTheory's primary color and provides
 * a disabled state with reduced opacity. It's intended for use in actions
 * related to payment or other key interactions within a PayTheory-integrated application.
 *
 * @param enabled Boolean indicating whether the button is enabled or disabled.
 * @param onClick Lambda function to be invoked when the button is clicked.
 * @param content Composable function defining the content to be displayed within the button (e.g., Text, Icon).
 * @param modifier Modifier to be applied to the button.
 * @param isLightMode Boolean indicating whether the button is in light mode.
 */
@Composable
fun PayTheoryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isLightMode: Boolean = true
) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = PayTheoryButtonUtils.payTheoryPrimary,
        contentColor = PayTheoryButtonUtils.lightModeTextColor,
        disabledContainerColor = PayTheoryButtonUtils.payTheoryDisabled,
        disabledContentColor = PayTheoryButtonUtils.darkModeTextColor
    )
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.testTag("pay_theory_button"),
        colors = buttonColors
    ) {
        content()
    }
}