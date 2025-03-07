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