package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.paytheory.lib.compose.string.SecureString

@Composable
fun TextFieldWrapper(
    value: SecureString,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isOutlined: Boolean = true
) {
    if (isOutlined) {
        OutlinedTextField(
            value = value.revealForUi(),
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    } else {
        TextField(
            value = value.revealForUi(),
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    }
}

@Composable
fun TextFieldWrapper(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isOutlined: Boolean = true
) {
    if (isOutlined) {
        OutlinedTextField(
            value = value,
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    } else {
        TextField(
            value = value,
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    }
}