package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class CardNumberFormattingTextWatcher(var payTheoryFragment: PayTheoryFragment, var pt: PayTheoryEditText) : TextWatcher {
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
        var changes = arrayOf(5,5,5,5)
        var maxLength = 23

        if (s.toString()[0] == '3') {
            changes = arrayOf(7,5)
            maxLength = 17
        }

        lock = true

        if (s.length > maxLength) {
            s.delete(maxLength,s.length)
        }

        var changeIndex = 0
        var stringIndex = 4

        while (stringIndex < s.length) {
            if (s.toString()[stringIndex] != ' ') {
                s.insert(stringIndex, " ")
                changeIndex += 1
            }

            if (changes.size > changeIndex) {
                stringIndex += changes[changeIndex]
            } else {
                stringIndex = s.length
            }
        }

        lock = false
        val isValid = validLuhn(s.toString())
        if (isValid) {
            payTheoryFragment.cardNumberValid = true
        } else {
            payTheoryFragment.cardNumberValid = false
            pt.error = "Invalid card number"
        }
    }

    private fun validLuhn(number: String): Boolean {
        val (digits, others) = number
            .filterNot(Char::isWhitespace)
            .partition(Char::isDigit)

        if (digits.length <= 1 || others.isNotEmpty()) {
            return false
        }

        val checksum = digits
            .map { it.code - '0'.code }
            .reversed()
            .mapIndexed { index, value ->
                if (index % 2 == 1 && value < 9) value * 2 % 9 else value
            }
            .sum()

        return checksum % 10 == 0
    }
}