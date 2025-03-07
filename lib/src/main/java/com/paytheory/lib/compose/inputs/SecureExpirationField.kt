package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.paytheory.lib.R
import com.paytheory.lib.compose.string.SecureStringWrapper

fun formatCardExpiryDate(input: String): String {

    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return ""

    val monthLength = if (digits.length < 2) 1 else 2
    val formattedMonth = if (digits.startsWith("0") || digits.startsWith("01")) digits.substring(0,monthLength) else {
        val month = when {
            digits.length >= 2 -> digits.substring(0, 2).toInt().coerceAtMost(12)
            else -> digits.toInt().coerceAtMost(9)
        }

        if (month > 1 && month<= 9) month.toString().padStart(2, '0') else month.toString()
    }
    val year = digits.drop(2).take(2)

    return if (year.isNotEmpty()) "$formattedMonth/$year" else formattedMonth
}

@Composable
fun SecureExpirationField(
    modifier: Modifier,
    value: SecureStringWrapper,
    onValueChange: (SecureStringWrapper) -> Unit,
    isValid: (SecureStringWrapper) -> Boolean,
    isOutlined: Boolean = true,
    clearKey: Int = 0
) {

    var secureWrapper by rememberSaveable(
        stateSaver = createSecureStringWrapperSaver(),
        key = "expirationClearKey_$clearKey"
    ) {
        mutableStateOf(value)
    }

    if (secureWrapper.secureValue != value.secureValue) {
        secureWrapper = value
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isDirty by remember { mutableStateOf(false) }

    if (isFocused && !isDirty) {
        isDirty = true
    }

    val isValid = if (isDirty || secureWrapper.secureValue.revealForUi().isNotEmpty()) isValid(secureWrapper) else true

    TextFieldWrapper(
        value = secureWrapper.secureState,
        onValueChange = { newValue ->
            val rawInput = newValue.text
            val formattedText = formatCardExpiryDate(rawInput)

            // Calculate cursor position
            val newCursorPosition = when {
                newValue.selection.start < rawInput.length -> newValue.selection.start
                else -> formattedText.length
            }

            secureWrapper.updateValue(formattedText,newCursorPosition)
            onValueChange(secureWrapper)
        },
        isError = !isValid,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        label = { Text(stringResource(id = R.string.mm_dd)) },
        maxLines = 1,
        singleLine = true,
        isOutlined = isOutlined
    )
}