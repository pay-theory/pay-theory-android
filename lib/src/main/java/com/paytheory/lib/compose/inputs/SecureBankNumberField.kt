package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * A composable function that provides a secure text field for bank account or routing numbers.
 *
 * This component handles the display and input of sensitive banking information,
 * ensuring that the data is managed securely using `SecureString`. It also provides
 * validation and character limits specific to bank account and routing numbers.
 *
 * @param modifier Modifier for styling and layout customization.
 * @param value The current `SecureString` value of the field.
 * @param onValueChange Callback invoked when the value changes. It provides the updated `SecureString`.
 * @param isValid A function to validate the entered `SecureString`. Returns `true` if valid, `false` otherwise.
 * @param isRouting Boolean flag indicating whether this field is for a routing number (true) or an account number (false).
 *                  Defaults to `false`. It determines the label and the maximum character limit.
 * @param isOutlined Boolean flag indicating if the text field should be outlined. Defaults to `true`.
 * @param clearKey An optional key to force reset the field's value state.
 *                 Changing this value will reset the stored state of the text field. Defaults to 0.
 *
 * **Usage:**
 *
 * ```kotlin
 * var accountNumber by remember { mutableStateOf(SecureString("")) }
 * SecureBankNumberField(
 *     modifier = Modifier.padding(16.dp),
 *     value = accountNumber,
 *     onValueChange = { newAccountNumber ->
 *         accountNumber = newAccountNumber
 *         // Handle the new account number here.
 *     },
 *     isValid = { secureString ->
 *         // Implement your custom validation logic here.
 */
@Composable
fun SecureBankNumberField(
    modifier: Modifier,
    value: SecureStringWrapper,
    onValueChange: (SecureStringWrapper) -> Unit,
    isValid: (SecureStringWrapper) -> Boolean,
    isRouting: Boolean = false,
    isOutlined: Boolean = true,
    clearKey: Int = 0
) {
    var secureString by rememberSaveable(stateSaver = createSecureStringWrapperSaver(), key = "clearKey_$clearKey") {
        mutableStateOf(value)
    }
    val label = if (isRouting) stringResource(id = R.string.routing_number) else stringResource(id = R.string.account_number)
    val maxChar = if (isRouting) 9 else 17

    if (secureString.secureValue != value.secureValue) {
        secureString = value
    }

    SecureBaseTextField(
        value = secureString,
        onValueChange = { newSecureString ->
            secureString =
                SecureStringWrapper(newSecureString.secureValue.takeIf { it.stringLength() <= maxChar }
                    ?: SecureString(newSecureString.secureValue.revealForUi().take(maxChar)),
                    selection = null)
            onValueChange(secureString) // Notify the caller
        },
        isValid = isValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        maxChar = maxChar,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        isOutlined = isOutlined
    )
}