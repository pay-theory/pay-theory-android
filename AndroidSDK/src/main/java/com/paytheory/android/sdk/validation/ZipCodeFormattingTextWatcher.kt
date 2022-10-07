package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class ZipCodeFormattingTextWatcher(var payTheoryFragment: PayTheoryFragment, var pt: PayTheoryEditText) : TextWatcher {
    private var lock = false

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

        val maxLength = 5

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        lock = false
        val isValidNumber = validZip(s.toString())
        if (isValidNumber) {
            payTheoryFragment.zipCodeValid = true
        }
        if (!isValidNumber) {
            payTheoryFragment.zipCodeValid = false
            pt.error = "Invalid zip code"
        }
    }

    private fun validZip(number: String): Boolean {
        val (digits, _) = number
            .partition(Char::isDigit)

        if (digits.length != 5) {
            return false
        }
        return true
    }
}