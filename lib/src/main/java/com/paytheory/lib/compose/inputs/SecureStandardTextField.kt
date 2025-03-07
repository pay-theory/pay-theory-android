package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.transformation.NoFilterTransformation

/**
 * A secure text field that handles sensitive string input.
 *
 * This composable provides a text field that works with [SecureString] to manage
 * sensitive data securely. It masks the input and provides validation.
 * It also internally uses [rememberSaveable] to maintain state across configuration changes.
 *
 * @param label The label to display above the text field.
 * @param modifier Modifier to apply to the text field.
 * @param value The initial [SecureString] value of the text field.
 * @param onValueChange Callback invoked when the text field's value changes. It provides the new [SecureString] value.
 * @param isValid Lambda function to validate the current [SecureString] input. Returns `true` if valid, `false` otherwise.
 * @param isOutlined Determines whether to render an outlined or filled text field. Defaults to `true` (outlined).
 * @param clearKey A key used to clear and reset the saved state of the text field. Changing this value will reset the
 *                 internal state of the [SecureString]. Defaults to 0. This is useful for scenarios where you need to
 *                 completely clear the field and its history.
 * @param maxLength The maximum number of characters allowed in the text field. Defaults to 128.
 *
 * @see SecureString
 * @see SecureBaseTextField
 * @see NoFilterTransformation
 */
@Composable
fun SecureStandardTextField(
    label: String,
    modifier: Modifier,
    value: SecureString,
    onValueChange: (SecureString) -> Unit,
    isValid: (SecureString) -> Boolean,
    isOutlined: Boolean = true,
    clearKey: Int = 0,
    maxLength: Int = 128,
) {
    var secureString by rememberSaveable(stateSaver = createSecureStringSaver(), key = "clearKey_$clearKey") {
        mutableStateOf(value)
    }

    SecureBaseTextField(
        value = secureString,
        onValueChange = { newSecureString ->
            secureString = newSecureString // Update the internal state
            onValueChange(newSecureString) // Notify the caller
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified),
        maxChar = maxLength,
        modifier = modifier,
        visualTransformation = NoFilterTransformation(),
        label = { Text(label) },
        isValid = isValid,
        isOutlined = isOutlined
    )
}