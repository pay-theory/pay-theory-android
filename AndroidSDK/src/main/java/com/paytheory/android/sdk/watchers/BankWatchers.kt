package com.paytheory.android.sdk.watchers

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText


var accountNumberValid: Boolean = false
var routingNumberValid: Boolean = false
var selectedBankAccountValid: Boolean = false
var bankFieldsValid: Boolean = false

private fun areFieldsValid(button: PayTheoryButton){
    // check if all card fields are valid
    bankFieldsValid = accountNumberValid && routingNumberValid && selectedBankAccountValid
    // if all card fields are valid enable button
    if (bankFieldsValid){
        button.enable()
    } else {
        button.disable()
    }
}



/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class RoutingNumberTextWatcher(pt: PayTheoryEditText, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: PayTheoryEditText? = pt

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            return
        }

        val isValidNumber = s.toString().length == 9
        handleButton(isValidNumber)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            routingNumberValid = false
            ptText!!.error = "Invalid Routing Number"
        } else {
            routingNumberValid = true
        }
        areFieldsValid(submitButton)
    }
}


/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class AccountNumberTextWatcher(pt: PayTheoryEditText, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: PayTheoryEditText? = pt

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            return
        }

        val isValidNumber = s.toString().length >= 5
        handleButton(isValidNumber)
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            accountNumberValid = false
            ptText!!.error = "Invalid Account Number"
        } else {
            accountNumberValid = true
        }
        areFieldsValid(submitButton)
    }
}

class BankAccountTypeWatcher(textView: AppCompatAutoCompleteTextView, private var submitButton: PayTheoryButton) :
    TextWatcher {
    private var ptText: AppCompatAutoCompleteTextView? = textView

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            handleButton(false)
        }

        if (s.isNotEmpty()) {
            handleButton(true)
        }
    }

    private fun handleButton(valid: Boolean){
        if (!valid) {
            selectedBankAccountValid = false
            ptText!!.error = "Invalid Account Type"
        } else {
            selectedBankAccountValid = true
        }
        areFieldsValid(submitButton)
    }
}
