package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import com.paytheory.android.sdk.view.PayTheoryEditText
import java.util.*


class ExpirationFormattingTextWatcher(pt: PayTheoryEditText) : TextWatcher {
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

        lock = true

        val singleCharMonth = charArrayOf('4','5','6','7','8','9')

        if (singleCharMonth.contains(s.toString()[0])) {
            s.insert(0, "0")
        }

        if (s.toString().matches("^\\d{3}".toRegex())) {
            s.insert(2, "/")
        }

        lock = false
        val isValidNumber = validExpiration(s.toString())
        if (!isValidNumber) {
            ptText!!.error = "invalid expiration number"
        }
    }

    private fun validExpiration(expiration: String): Boolean {
        val parts = expiration.split("/")
        val month = parts.first().toInt()

        if (month < 1 || month > 12) {
            return false
        }

        if (parts.size == 2 && parts.last().isNotEmpty()) {
            var year = parts.last().toInt()
            if (year < 100) {
                year += 2000
            }
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.YEAR) == year &&
                cal.get(Calendar.MONTH) >= month) {
                return false
            }
            if (cal.get(Calendar.YEAR) > year) {
                return false
            }
        }

        return true
    }
}