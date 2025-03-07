package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.transformation.NoFilterTransformation

/**
 * A composable function that provides a secure text field for sensitive data.
 * It wraps the standard TextField and handles the secure storage and manipulation
 * of text via [SecureString].
 *
 * @param value The current [SecureString] value of the text field.
 * @param modifier Modifier for styling and layout.
 * @param keyboardOptions Keyboard options for configuring the on-screen keyboard.
 * @param visualTransformation Transformation applied to the text for display,
 *        defaults to [NoFilterTransformation] which means no transformation
 * @param maxChar The maximum number of characters allowed in the text field.
 *        Defaults to 128.
 * @param preWrap A function to preprocess the input string before it's stored
 *        in the [SecureString]. Useful for formatting. Defaults to identity.
 * @param onValueChange Callback triggered when the text field's value changes.
 *        It receives the new [SecureString] value.
 * @param isValid A function to validate the current [SecureString] value.
 *        It returns true if the value is valid, false otherwise.
 * @param label Optional label to display above the text field.
 * @param isOutlined Whether to render the text field with an outlined style.
 *        Defaults to true.
 * @param isNumeric Whether to allow only numeric input. Defaults to false. If true the preWrap will be applied after the filtering.
 *
 * @see SecureString
 * @see TextFieldWrapper
 * @see NoFilterTransformation
 */
@Composable
internal fun SecureBaseTextField(
    value: SecureString,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = NoFilterTransformation(),
    maxChar: Int = 128,
    preWrap: (String) -> String = { it },
    onValueChange: (SecureString) -> Unit,
    isValid: (SecureString) -> Boolean,
    label: @Composable (() -> Unit)?,
    isOutlined: Boolean = true,
    isNumeric: Boolean = false
) {


    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isDirty by remember { mutableStateOf(false) }

    if (isFocused && !isDirty) {
        isDirty = true
    }

    val isValid = if (isDirty || value.revealForUi().isNotEmpty()) isValid(value) else true

    TextFieldWrapper(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxChar) {
                val filtered = if (isNumeric) newValue.filter { it.isDigit() } else newValue
                onValueChange(SecureString(preWrap(filtered)))
            }
        },
        isError = !isValid,
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        label = label,
        maxLines = 1,
        singleLine = true,
        isOutlined = isOutlined
    )
}