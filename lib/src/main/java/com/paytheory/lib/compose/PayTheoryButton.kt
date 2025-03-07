package com.paytheory.lib.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.padding(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6200EE),
            disabledContainerColor = Color(0xFF6200EE).copy(alpha = 0.4f)
        )
    ) {
        content()
    }
}