package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.paytheory.lib.R
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import com.paytheory.lib.compose.transformation.PasswordFilterTransformation

/**
 * A composable function that creates a secure CVC (Card Verification Code) input field.
 *
 * This field masks the entered digits and provides validation capabilities. It leverages
 * a custom [SecureString] to handle sensitive data securely.
 *
 * @param modifier The modifier to be applied to the text field.
 * @param value The current [SecureString] value of the field.
 * @param onValueChange A callback that is invoked when the field's value changes. It provides the
 *                      new [SecureString] value.
 * @param isValid A lambda function that takes a [SecureString] and returns `true` if the value is
 *                considered valid, `false` otherwise. This is used for visual feedback on the field's
 *                validity.
 * @param isOutlined Determines if the field should be displayed as an outlined text field or a filled one.
 *                  Defaults to `true` (outlined).
 * @param clearKey An integer key used to reset the field's internal state. Changing this key
 *                 will effectively clear the field and reset it to the initial `value`. This is
 *                 useful when the field needs to be cleared programmatically. Defaults to `0`.
 */
@Composable
fun SecureCvcField(
    modifier: Modifier,
    value: SecureStringWrapper,
    onValueChange: (SecureStringWrapper) -> Unit,
    isValid: (SecureStringWrapper) -> Boolean,
    isOutlined: Boolean = true,
    clearKey: Int = 0
) {
    var secureString by rememberSaveable(stateSaver = createSecureStringWrapperSaver(), key = "clearKey_$clearKey") {
        mutableStateOf(value)
    }

    if (secureString.secureValue != value.secureValue) {
        secureString = value
    }

    SecureBaseTextField(
        value = secureString,
        onValueChange = { newSecureString ->
            secureString = newSecureString // Update the internal state
            onValueChange(newSecureString) // Notify the caller
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxChar = 4,
        modifier = modifier,
        visualTransformation = PasswordFilterTransformation(),
        label = { Text(stringResource(id = R.string.cvc)) },
        isValid = isValid,
        isOutlined = isOutlined
    )
}