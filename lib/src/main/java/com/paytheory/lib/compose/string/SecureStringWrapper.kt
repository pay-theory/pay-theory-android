package com.paytheory.lib.compose.string

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * `SecureStringWrapper` is a class that manages a `SecureString` while providing a safe way to
 * interact with its visible representation in UI elements. It also manages the selection range
 * for text input fields.
 *
 * This class encapsulates a `SecureString`, which is designed to hold sensitive data securely
 * (e.g., passwords, API keys). It exposes a `visibleValue` property, which is the decrypted
 * (revealed) string for UI display. However, the underlying `SecureString` remains encrypted.
 *
 * The `visibleValue` is a Compose `MutableState`, ensuring UI updates whenever it changes.
 *
 * @property initialValue The initial `SecureString` to be wrapped.
 * @property selection The initial text selection range within the visible value. If null, no selection is applied.
 */
class SecureStringWrapper(initialValue: SecureString, selection: TextRange?) {

    private var _secureValue: SecureString = initialValue
    var _selection: TextRange? = selection

    // Use a MutableState to hold the visible value
    var visibleValue: String by mutableStateOf(_secureValue.revealForUi())
        private set

    fun updateValue(newValue: String, newSelection: Int) {
        _secureValue = SecureString(newValue)
        // Update the MutableState's value directly
        visibleValue = newValue
        _selection = TextRange(newSelection)
    }

    val secureValue: SecureString get() = _secureValue

    val secureState: TextFieldValue get() = if (_selection != null)
        TextFieldValue(visibleValue,selection=_selection!!)
    else TextFieldValue(visibleValue)

}