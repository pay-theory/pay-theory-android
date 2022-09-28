package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class RoutingNumberFormattingTextWatcher(pt: PayTheoryEditText, submitButton: Button) : TextWatcher {
    private var ptText: PayTheoryEditText? = pt
    private var submitButton = submitButton

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            return
        }

        val isValidNumber = s.toString().length == 9
        handleButton(isValidNumber)
    }

    private fun handleButton(valid: Boolean){
        if (valid) {
            submitButton.isEnabled = true
        }
        if (!valid) {
            submitButton.isEnabled = false
            ptText!!.error = "Invalid Routing Number"
        }
    }
}