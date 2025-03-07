package com.paytheory.lib.compose.string

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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