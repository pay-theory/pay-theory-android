package com.paytheory.lib.compose

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
 */
@Composable
fun PayTheoryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        content()
    }
}