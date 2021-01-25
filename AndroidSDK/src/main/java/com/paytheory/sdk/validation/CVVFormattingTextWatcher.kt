package com.paytheory.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import com.paytheory.sdk.view.PayTheoryEditText


class CVVFormattingTextWatcher(pt: PayTheoryEditText) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

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
        if (!isValidNumber) {
            ptText!!.error = "invalid CVV"
        }
    }

    private fun validCVV(number: String): Boolean {
        val (digits, _) = number
            .partition(Char::isDigit)

        if (digits.length < 3 || digits.length > 4) {
            return false
        }
        return true
    }
}