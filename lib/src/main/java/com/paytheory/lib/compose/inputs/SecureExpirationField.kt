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
import com.paytheory.lib.compose.utility.ExpirationFieldUtils

/**
 * Formats a card expiry date string from a raw input string.
 *
 * This function takes a string as input and attempts to extract and format a card expiry date
 * in the MM/YY format. It handles various input scenarios, including:
 *   - Empty input strings.
 *   - Input strings containing non-digit characters.
 *   - Input strings with less than 4 digits.
 *   - Month values exceeding 12.
 *   - Month values less than 1.
 *
 * The function extracts digits from the input, treats the first one or two as the month,
 * and the next two as the year. It ensures that the month value is within the valid range (1-12)
 * and formats the month with a leading zero if it's a single digit (e.g., 01, 09 instead of 1, 9).
 *
 * @param input The raw input string, potentially containing digits and non-digit characters.
 * @return A formatted expiry date string in the MM/YY format, or an empty string if no digits are found in the input.
 *  If only month digits are found, it returns only the month formatted.
 *
 * Examples:
 *   - formatCardExpiryDate("1225") returns "12/25"
 *   - formatCardExpiryDate("1225abc") returns "12/25"
 *   - formatCardExpiryDate("12") returns "12"
 *   - formatCardExpiryDate("0123") returns "01/23"
 *   - formatCardExpiryDate("0999") returns "09/99"
 *   - formatCardExpiryDate("1399") returns "12/99" // Month coerced to 12
 *   - formatCardExpiryDate("9") returns "09"
 *   - formatCardExpiryDate("abc") returns ""
 *   - formatCardExpiryDate("") returns ""
 *   - formatCardExpiryDate("0") returns "0"
 *   - formatCardExpiryDate("01") returns "01"
 *   - formatCardExpiryDate("1") returns "01"
 */
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

/**
 * A composable function that provides a secure text field for entering and displaying
 * credit/debit card expiration dates (MM/YY format).
 *
 * This field uses [SecureStringWrapper] to handle the sensitive data securely,
 * ensuring it's encrypted in memory and only revealed for UI display when needed.
 *
 * @param modifier The modifier to be applied to the text field.
 * @param value The current value of the expiration date, wrapped in a [SecureStringWrapper].
 * @param onValueChange A callback that is invoked when the value of the text field changes.
 *                      It receives the new value as a [SecureStringWrapper].
 * @param isValid A function that checks if the current value is a valid expiration date.
 *                 It receives the current value as a [SecureStringWrapper] and returns a Boolean.
 * @param isOutlined Determines whether the text field should have an outlined style. Defaults to `true`.
 * @param clearKey A key that, when changed, will reset the internal state of the field.
 *                 This can be used to clear the field programmatically. Defaults to `0`.
 *
 */
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
            val formattedText = ExpirationFieldUtils.formatCardExpiryDate(rawInput)

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