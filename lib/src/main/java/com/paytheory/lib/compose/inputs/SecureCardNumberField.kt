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
import com.paytheory.lib.compose.transformation.CreditCardNumberTransformation

/**
 * A composable function that provides a secure field for entering credit card numbers.
 *
 * This field masks the entered digits using [CreditCardNumberTransformation] and
 * limits the input to a maximum of 16 numeric characters. It also supports
 * validation and different TextField styles (outlined or filled).
 *
 * @param modifier The modifier to be applied to the field.
 * @param value The current [SecureString] value of the field.
 * @param onValueChange A callback that is invoked when the value of the field changes.
 *                      It provides the new [SecureString] value.
 * @param isValid A lambda function that checks if the current [SecureString] value is valid.
 *                Returns `true` if the value is valid, `false` otherwise.
 * @param isOutlined Determines whether the field should be rendered as an outlined text field.
 *                   Defaults to `true`.
 * @param clearKey A key used to reset the field's state. If this key changes, the field will
 *                 be cleared and reset to its initial state. Defaults to `0`. It helps to clear the field in some cases like when a dialog close or cancel is triggered.
 *
 */
@Composable
fun SecureCardNumberField(
    modifier: Modifier,
    value: SecureString,
    onValueChange: (SecureString) -> Unit,
    isValid: (SecureString) -> Boolean,
    isOutlined: Boolean = true,
    clearKey: Int = 0
) {
    var secureString by rememberSaveable(stateSaver = createSecureStringSaver(), key = "clearKey_$clearKey") {
        mutableStateOf(value)
    }

    SecureBaseTextField(
        value = secureString,
        onValueChange = { newSecureString ->
            secureString = SecureString(newSecureString.revealForUi().take(20)) // Update the internal state
            onValueChange(secureString) // Notify the caller
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxChar = 16,
        modifier = modifier,
        visualTransformation = CreditCardNumberTransformation(),
        label = { Text(stringResource(id = R.string.card_number)) },
        isValid = isValid,
        isOutlined = isOutlined
    )
}