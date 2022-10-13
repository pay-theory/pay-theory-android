package com.paytheory.android.sdk.validation

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.paytheory.android.sdk.view.PayTheoryEditText
import java.text.SimpleDateFormat
import java.util.*


class ExpirationTextWatcher(pt: PayTheoryEditText, private var submitButton: Button) : TextWatcher {
    private var lock = false
    private var ptText: PayTheoryEditText? = pt
    private var isDelete = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        isDelete = before != 0
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

        val isValidNumber = validExp(s.toString())
        handleButton(isValidNumber)
    }

    @SuppressLint("SimpleDateFormat")
    private fun validExp(number: String): Boolean {
        val length = number.length
        val stringBuilder = StringBuilder()
        stringBuilder.append(number)
        if (length > 0 && length == 3) {
            if (isDelete) stringBuilder.deleteCharAt(length - 1) else stringBuilder.insert(
                length - 1,
                "/"
            )
            ptText!!.setText(stringBuilder)
            ptText!!.setSelection(ptText!!.text!!.length)
        }

        //get current two digit year
        val currentTwoDigitYear: Int = SimpleDateFormat("yy").format(Calendar.getInstance().time).toInt()
        //get month value and check
        val currentText = ptText!!.text.toString()
        return (currentText.length == 5) && (currentText.substringBefore("/").toInt() < 13) && (currentText.substringAfter("/").toInt() >= currentTwoDigitYear)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            ptText!!.error = "Invalid Expiration"
        }
    }
}