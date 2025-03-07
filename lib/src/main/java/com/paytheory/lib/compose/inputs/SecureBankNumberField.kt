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

@Composable
fun SecureBankNumberField(
    modifier: Modifier,
    value: SecureString,
    onValueChange: (SecureString) -> Unit,
    isValid: (SecureString) -> Boolean,
    isRouting: Boolean = false,
    isOutlined: Boolean = true,
    clearKey: Int = 0
) {
    var secureString by rememberSaveable(stateSaver = createSecureStringSaver(), key = "clearKey_$clearKey") {
        mutableStateOf(value)
    }
    val label = if (isRouting) stringResource(id = R.string.routing_number) else stringResource(id = R.string.account_number)
    val maxChar = if (isRouting) 9 else 17



    SecureBaseTextField(
        value = secureString,
        onValueChange = { newSecureString ->
            secureString =
                SecureString(newSecureString.revealForUi().takeIf { it.length <= maxChar }
                    ?: newSecureString.revealForUi().take(maxChar))
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