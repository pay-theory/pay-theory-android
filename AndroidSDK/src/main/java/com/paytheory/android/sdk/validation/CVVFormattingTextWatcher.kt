package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CVVFormattingTextWatcher(pt: PayTheoryEditText, private var submitButton: Button) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (lock || s.isEmpty()) {
            return
        }

        val maxLength = 4

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        lock = false
        val isValidNumber = validCVV(s.toString())
        handleButton(isValidNumber)
    }

    private fun validCVV(number: String): Boolean {
        val (digits, _) = number
            .partition(Char::isDigit)

        if (digits.length < 3 || digits.length > 4) {
            return false
        }
        return true
    }
    private fun handleButton(valid: Boolean){
        if (!valid) {
            submitButton.isEnabled = false
            ptText!!.error = "Invalid CVV"
        }
    }
}