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

@Composable
internal fun SecureBaseTextField(
    value: SecureString,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = NoFilterTransformation(),
    maxChar: Int = 20,
    preWrap: (String) -> String = { it },
    onValueChange: (SecureString) -> Unit,
    isValid: (SecureString) -> Boolean,
    label: @Composable (() -> Unit)?,
    isOutlined: Boolean = true,
    isNumeric: Boolean = false
) {
    // Use a custom remember that handles secure data
    //    val secureWrapper = remember { SecureStringWrapper(value,null) }

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